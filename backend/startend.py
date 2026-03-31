import numpy as np
import openwakeword
from openwakeword.model import Model

# Sensitivity settings
SILENCE_THRESHOLD = 100  # Adjust based on your "Decent" mic noise

def is_silence(audio_chunk):
    # Convert raw bytes back to integers (assuming 16-bit PCM)
    # Convert to float64 first, then square, then mean, then sqrt
    samples = np.frombuffer(audio_chunk, dtype=np.int16)
    # Calculate RMS (Root Mean Square) energy
    energy = np.sqrt(np.mean(samples.astype(np.float64)**2))
    print(f"Energy: {energy}")
    return energy < SILENCE_THRESHOLD


# # Inside your WebSocket loop:
# if is_silence(audio_chunk):
#     silence_counter += len(audio_chunk) / (2 * SAMPLE_RATE) 
#     if silence_counter >= SILENCE_DURATION_S:
#         print("End of question detected! Processing with Gemini...")
#         process_with_gemini(full_buffer)
#         silence_counter = 0
# else:
#     silence_counter = 0 # Reset if they start talking again

# Initialize with a default model (e.g., "hey_jarvis" or "alexa")
openwakeword.utils.download_models()
model = Model(wakeword_models=["alexa"], inference_framework="onnx")

async def detect_wake_word(data):
    prediction = model.predict(data)
    for mdl, score in prediction.items():
        print(f"Prediction Score : {score}")
        if score > 0.5:
            print(f"Wake word detected! (Score: {score})")
            return True
    return False