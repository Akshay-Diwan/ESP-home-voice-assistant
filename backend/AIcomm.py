import os
import requests
from requests import Response
from google import genai
from google.genai import types
from dotenv import load_dotenv
from generate_voice import generate_assistant_voice
load_dotenv()

AIclient = genai.Client(api_key=os.getenv('GEMINI_API_KEY'))
SYSTEM_PROMPT = '''
 Your name is Jarvis.
 You are a smart home assistant. You control lights and fans you can also read temperature and time and answer questions related general news.
 If the user asks to turn something on or off, respond with a clear confirmation. 
 For other questions, provide brief, helpful answers. Keep all responses under 30 words.
 
 If the user asks to turn something on or off, reply in the following format 
 CMD:[LIGHT/FAN]:[ON/OFF]
 If the user asks for temperature or time
 RD:[TEMP/TIME]

 EXAMPLE:
 QUESTION: At what time IPL starts
 RESPONSE: IPL start at 7:30 PM

 QUESTION: Turn off the lights
 RESPONSE: CMD:[LIGHT][OFF]

 QUESTION: What is temperature
 RESPONSE: RD:[TEMP]
'''

# def transcripContainsName(fullprompt: str):
#     return fullprompt.find("Jarvis")
    
def getAIRes(text):
    reply = AIclient.models.generate_content(
                model="gemini-2.5-flash",
                contents=text,
                config=types.GenerateContentConfig(
                system_instruction=SYSTEM_PROMPT,
                ),
            )
    return reply.text
    
def getAIResponse(mac_id: str) -> str:
    text_file = f"Transcripts/{mac_id.replace(":", "_")}.txt"

    reply_text: str = ""
    if os.path.exists(text_file):
        with open(text_file, "r") as f:
            full_prompt = f.read().strip()
        
        # if full_prompt and transcripContainsName(full_prompt) != -1:
        reply_text = "Sorry I could not understand what you are saying"
        print(f"User finished talking. Sending to AI: {full_prompt}")

        # reply = postRequest(full_prompt)
        reply = AIclient.models.generate_content(
            model="gemini-2.5-flash",
            contents=full_prompt,
            config=types.GenerateContentConfig(
            system_instruction=SYSTEM_PROMPT,
            ),
        )
        reply_text = reply.text
        print(reply.text)
        # ai_response = call_ollama(full_prompt)
        # print(f"AI says: {ai_response}")
        
        # Cleanup: Delete the text file so the next session is fresh
        os.remove(text_file)
    return reply_text
    
    



# def postRequest(message: str):
#     jsonData = {
#         "model": "my-tinyllama",
#         "prompt": message,
#         "stream": False
#     }
#     res: Response = requests.post('http://localhost:11434/api/generate', json=jsonData)
#     print(res.json()['response'])
#     # generate_assistant_voice(res.json()['response'])
#     return res.json()['response']

if __name__ == '__main__':
    getAIResponse('demo')





