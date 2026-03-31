from fastapi import FastAPI, Request, Header, WebSocket
from fastapi.responses import FileResponse
import wave
from stt import speechToText, sttBuffer
import uvicorn
import os
from AIcomm import getAIResponse, getAIRes
from generate_voice import generate_assistant_voice
import numpy as np
from startend import is_silence

app = FastAPI()

index = 1

@app.websocket("/ws/audio")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    print("ESP32 Connected via WebSocket")
    
    is_listening = False
    audio_buffer = bytearray()
    silence_counter = 0
    # ... other variables ...
    SAMPLE_RATE = 16000
    SILENCE_DURATION_S = 3

    try:
        while True:
            # 1. Receive the data (fast)
            audio_chunk = await websocket.receive_bytes()
            # 2. Convert to numpy for AI (fast)
            data = np.frombuffer(audio_chunk, dtype=np.int32)

            # if not is_listening:
            #     # IMPORTANT: Ensure this function is fast and uses ONNX
            #     is_listening = await detect_wake_word(data)
            #     if is_listening:
            #         print("Wake word detected! Switching to buffer mode.")
            
            # if is_listening:
            audio_buffer.extend(audio_chunk)

            if is_silence(data): # Pass 'data' (numpy) not 'audio_chunk' (bytes)
                silence_counter += len(audio_chunk) / (2 * SAMPLE_RATE) 

                if silence_counter >= SILENCE_DURATION_S:

                    print("End of question. Sending for transcription now.")
                    with wave.open('full_query.wav', "wb") as wav_file:
                            wav_file.setnchannels(1)       # Mono
                            wav_file.setsampwidth(4)      # 16-bit
                            wav_file.setframerate(16000)  # 16kHz
                            wav_file.writeframes(audio_buffer)
                    op = sttBuffer(audio_buffer)
                    if op != -1 and op != -2:
                        print(getAIRes(op))
                    # Reset for next time
                    audio_buffer = bytearray()
                    # is_listening = False
                    silence_counter = 0
            else:
                silence_counter = 0 
                    
    except Exception as e:
        print(f"Loop error: {e}")

@app.get("/get-audio")
async def get_audio():
    file_path = "response.wav" # Ensure this is 16kHz, 16-bit, Mono
    if os.path.exists(file_path):
        return FileResponse(file_path, media_type="audio/wav")
    return {"error": "File not found"}

@app.get('/answer')
async def get_answer(x_mac_id: str = Header(None)):
        print("Sending transcript to AI....")
        reply = getAIResponse(x_mac_id)
        file_path =  f"{x_mac_id.replace(':', '_')}.wav" # Ensure this is 16kHz, 16-bit, Mono
        
        if len(reply) > 0:
            if(reply.find(':') != -1):
                lst = [item.strip() for item in reply.split(':')]
                if len(lst) == 3:
                    ans = {
                        "Device": lst[1],
                        "Command": lst[2]
                    }
                    print(ans)
                    return ans
                else: 
                    ans = {
                        "Sensor": lst[1]
                    }
                    print(ans)
                    return ans
            else:    
                generate_assistant_voice(reply, file_path)
                if os.path.exists(file_path):
                    return FileResponse(file_path, media_type="audio/wav")
                return {'text': reply, 'error': 'Could not generate voice'}
        else:
            print("reply not working...")
        return {'status': 'processing'}
    

@app.post("/stt") 
async def handle_audio(request: Request, x_mac_id: str = Header(None)):
    print("X-Mac-ID : " + x_mac_id)
    device_dir = f"Recordings/{x_mac_id.replace(':', '_')}"
    os.makedirs(device_dir, exist_ok = True)

    audio_data = await request.body()
    global index
    # Save as a WAV file
    temp_wave = f"{device_dir}/query{index}.wav"

    index+=1
    with wave.open(temp_wave, "wb") as wav_file:
        wav_file.setnchannels(1)       # Mono
        wav_file.setsampwidth(2)      # 16-bit
        wav_file.setframerate(16000)  # 16kHz
        wav_file.writeframes(audio_data)
        
    print("Received audio! Saved to query.wav")
    transcriptStatus = speechToText(temp_wave, x_mac_id)

    if transcriptStatus == -1:
        reply = getAIResponse(x_mac_id)
        file_path =  f"{x_mac_id.replace(':', '_')}.wav" # Ensure this is 16kHz, 16-bit, Mono
        if len(reply) > 0:
            if(reply.find(':') != -1):
                lst = [item.strip() for item in reply.split(':')]
                if len(lst) == 3:
                    ans = {
                        "Device": lst[1],
                        "Command": lst[2]
                    }
                    print(ans)
                    return ans
                else: 
                    ans = {
                        "Sensor": lst[1]
                    }
                    
                    print(ans)
                    return ans
            else:    
                generate_assistant_voice(reply, file_path)
                if os.path.exists(file_path):
                    return FileResponse(file_path, media_type="audio/wav")
                return {'text': reply, 'error': 'Could not generate voice'}
        else:
            print("reply not working...")
        
    return {"status": "processing"}


if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8080, log_level="info")