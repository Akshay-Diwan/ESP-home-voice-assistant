import speech_recognition as sr
import os

def speechToText(AudioFile, x_mac_id:str):
    recog = sr.Recognizer()
    with sr.AudioFile(AudioFile) as source:
        audio_data = recog.record(source)
        try: 
            text = recog.recognize_google(audio_data)
            print('Transcribed Text: ' + text)
            with open(f"Transcripts/{x_mac_id.replace(':','_')}.txt", 'a') as file:
                file.write(" " + text) 
            return 1
        except sr.UnknownValueError:
            print("Speech Recognition could not understand audio")
            return -1
        except sr.RequestError as e:
            print(f"Could not request results from Google Speech Recognition service; {e}")
            return -2
    

if __name__ == "__main__":
    speechToText("Recording.wav")




