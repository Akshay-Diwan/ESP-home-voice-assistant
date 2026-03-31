# The following lines register your source files and link necessary ESP-IDF components
idf_component_register(
    SRCS 
       "i2s_speaker.c"
        "get_response.c" 
        "util_functions.c" 
        "post_audio.c" 
        "wifi_comm.c" 
        "i2s_mic.c" 
        "hello_world_main.c"
        "ws.c"
        "automation.c"
        "devices.c"
    INCLUDE_DIRS "."
    PRIV_REQUIRES 
        driver          # For I2S and GPIO
        esp_http_client # For sending audio to your backend
        esp_wifi        # For Wi-Fi connectivity
        esp_event       # For IP and Wi-Fi events
        nvs_flash       # For storing Wi-Fi credentials
        json            # This provides cJSON.h for your response
        esp_hw_support  # This provides esp_mac.h in newer IDF versions
        esp_websocket_client
        # bt

) 