from gtts import gTTS
from pydub import AudioSegment
import os

def generate_assistant_voice(text, filename="response.wav"):
    # 1. Generate Speech using Google TTS (Indian English accent)
    tts = gTTS(text=text, lang='en', tld='co.in')
    tts.save("temp.mp3")

    # 2. Convert MP3 to ESP32-friendly WAV
    # We force 16000Hz, Mono, 16-bit PCM
    audio = AudioSegment.from_mp3("temp.mp3")
    audio = audio.set_frame_rate(16000).set_channels(1).set_sample_width(2)
    
    # 3. Export to your server's static folder
    audio.export(filename, format="wav")
    
    # Clean up temp file
    os.remove("temp.mp3")
    print(f"Assistant says: {text}")

# Example Usage:
# generate_assistant_voice("Turning on the fan now.")