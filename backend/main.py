from fastapi import FastAPI, Request, Header
from fastapi.responses import FileResponse
import wave
from stt import speechToText
import uvicorn
import os
from AIcomm import getAIResponse
from generate_voice import generate_assistant_voice

app = FastAPI()

index = 1

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
        if (len(reply) > 0):
            generate_assistant_voice(reply, file_path)
            if os.path.exists(file_path):
                return FileResponse(file_path, media_type="audio/wav")
            return {'text': reply, 'error': 'Could not generate voice'}
        else:
            print("reply not working...")
        
    return {"status": "processing"}


if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8080, log_level="info")