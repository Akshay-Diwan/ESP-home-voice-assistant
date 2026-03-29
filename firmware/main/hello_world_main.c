// /*
//  * SPDX-FileCopyrightText: 2010-2022 Espressif Systems (Shanghai) CO LTD
//  *
//  * SPDX-License-Identifier: CC0-1.0
//  */

#include "nvs_flash.h" // For storing WiFi credentials
#include <stdio.h>
#include <math.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_log.h"
#include "i2s_mic.h"
#include "wifi_comm.h"
#include "esp_http_client.h"
#include "esp_mac.h"
#include "util_functions.h"
#include "i2s_speaker.h"
#include "get_response.h"
// #include "bt_audio.h"

void init_nvs()
{
    esp_err_t ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND)
    {
        ESP_ERROR_CHECK(nvs_flash_erase());
        ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK(ret);
}

#define RECORD_TIME_SEC 3
#define SAMPLE_RATE 16000
#define CHUNK_SIZE (RECORD_TIME_SEC * SAMPLE_RATE * sizeof(int16_t))


i2s_chan_handle_t speaker_handle;
char mac_str[18];

 void upload_audio(int16_t *buffer, size_t size)
 {
     esp_http_client_config_t config = {
         .url = "http://192.168.1.2:8080/stt",
         .method = HTTP_METHOD_POST,
         .timeout_ms = 5000,
         .user_data = speaker_handle,
         .event_handler = _post_audio_event_handler
     };
     esp_http_client_handle_t client = esp_http_client_init(&config);

     // Set headers so FastAPI knows this is raw audio data
     esp_http_client_set_header(client, "Content-Type", "application/octet-stream");
     printf(mac_str);
     esp_http_client_set_header(client, "X-Mac-ID", mac_str);
     esp_http_client_set_post_field(client, (const char *)buffer, size);

     esp_err_t err = esp_http_client_perform(client);
     if (err == ESP_OK)
     {
         ESP_LOGI("AI_LINK", "Audio uploaded! HTTP Status: %d", esp_http_client_get_status_code(client));
     }
     else
     {
         ESP_LOGE("AI_LINK", "Upload failed: %s", esp_err_to_name(err));
     }
     esp_http_client_cleanup(client);
 }

//  esp_err_t _http_event_handler(esp_http_client_event_t *evt) {
//     switch(evt->event_id) {
//         case HTTP_EVENT_ON_DATA:
//             if (!esp_http_client_is_chunked_response(evt->client)) {
//                 // evt->data contains the JSON string from your Uvicorn server
//                 parse_backend_response((char*)evt->data);
//             }
//             break;
//         default:
//             break;
//     }
//     return ESP_OK;
// }
 void mic_task(void *pvParameters)
 {
     // 1. Buffers
     static int32_t raw_samples[512]; // The "sniff" buffer (32-bit from I2S)
     size_t bytes_read;

     // Filter variables
    float alpha = 0.99f; 
    float filtered_val = 0;
    float prev_raw_f = 0;
    float prev_filtered_f = 0;

     int16_t *recording_buffer = malloc(CHUNK_SIZE); // The 3-second upload buffer
     int frame_size = 320;
     int16_t *vad_frame = malloc(frame_size * sizeof(int16_t));
     int current_idx = 0;
     int vad_idx = 0;
     int32_t avg_eng = 0;
     bool isVoice = false;
     bool previousIsVoice = false;



     while (1)
     {
         // 2. Always read a "sniff" using your helper function
         if (i2s_mic_read(raw_samples, sizeof(raw_samples), &bytes_read) == ESP_OK)
         {
             int count = bytes_read / 4; // 32-bit I2S data
             
             for (int i = 0; i < count; i++)
             {
                // 1. Shift and Cast to float for precision
                float current_raw_f = (float)(raw_samples[i] >> 14);

                // 2. High-Pass Filter Equation: 
                // y[n] = alpha * (y[n-1] + x[n] - x[n-1])
                filtered_val = alpha * (prev_filtered_f + current_raw_f - prev_raw_f);

                // 3. Update history for next iteration
                prev_raw_f = current_raw_f;
                prev_filtered_f = filtered_val;

                // 4. Convert back to int16_t for the recording buffer
                int16_t final_sample = (int16_t)filtered_val;

                //5. Noise Gate 
                if (abs(final_sample) < 600) {
                    final_sample = 0;
                }

                 // 1. Check if the Big Recording Buffer is full
                 if (current_idx >= (CHUNK_SIZE / sizeof(int16_t)))
                 {
                     if (isVoice)
                     {
                         ESP_LOGI("MIC", "Buffer full, uploading...");
                         upload_audio(recording_buffer, CHUNK_SIZE);
                        }
                     else if(previousIsVoice){
                          get_answer(speaker_handle, mac_str);
                     }
                    previousIsVoice = isVoice;
                    isVoice = false; // Reset for next block
                     current_idx = 0;
                 }
                 recording_buffer[current_idx++] = final_sample;

                 // 2. Fill the VAD frame
                 vad_frame[vad_idx++] = final_sample;
                 avg_eng += abs(final_sample);

                 // 3. Check if VAD frame is ready for analysis
                 if (vad_idx >= frame_size)
                 {
                     // Only run VAD if we haven't found voice in this 3s block yet
                     if (!isVoice)
                     {
                         isVoice = avg_eng / frame_size > 500;                         
                         if (isVoice)
                             ESP_LOGI("MIC", "Speech detected in current block!");
                     }
                    ESP_LOGI("VAD_TEST", "Average Energy: %ld", avg_eng / frame_size);
                    avg_eng = 0;
                     vad_idx = 0; // Reset for next 20ms frame
                 }
             }
         }
         vTaskDelay(pdMS_TO_TICKS(1)); // Fast loop for smooth audio
     }
 }

 void audio_fetch_task(void *pvParameters) {
    // Call your function
    get_audio(speaker_handle);
    // IMPORTANT: A task must delete itself when done!

    vTaskDelete(NULL);
}

void test_backend_ping()
{
    esp_http_client_config_t config = {
        .url = "http://192.168.1.2:8080/", // Your Laptop's IP
        .method = HTTP_METHOD_GET,
    };
    esp_http_client_handle_t client = esp_http_client_init(&config);

    ESP_LOGI("AI_LINK", "Attempting to ping backend...");
    esp_err_t err = esp_http_client_perform(client);

    if (err == ESP_OK)
    {
        int status = esp_http_client_get_status_code(client);
        ESP_LOGI("AI_LINK", "Success! Server responded with status: %d", status);
    }
    else
    {
        ESP_LOGE("AI_LINK", "Ping failed: %s. Check your Firewall!", esp_err_to_name(err));
    }
    esp_http_client_cleanup(client);
}
void app_main(void)
{
    esp_err_t ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND)
    {
        ESP_ERROR_CHECK(nvs_flash_erase());
        ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK(ret);
    ESP_LOGI("TEST", "NVS Done. Initializing WiFi...");

    // 2. Call the init function
    wifi_init_sta();
    uint8_t mac[6];
    esp_read_mac(mac, ESP_MAC_WIFI_STA);
    snprintf(mac_str, sizeof(mac_str), "%02X:%02X:%02X:%02X:%02X:%02X",
              mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);

    ESP_LOGI("TEST", "WiFi Initialized! If you see this, the init was successful.");
    ESP_LOGI("AI_LINK", "Waiting for WiFi before pinging...");
    xEventGroupWaitBits(s_wifi_event_group, WIFI_CONNECTED_BIT, pdFALSE, pdTRUE, portMAX_DELAY);

    // Now it is safe to ping
    test_backend_ping();

    i2s_mic_init(); // Clean and simple
    speaker_handle = speaker_init();
    if(!speaker_handle){
        ESP_LOGI("SPEAKER", "COULD NOT ATTACH HANDLE TO SPEAKER");
    }
    // if(speaker_handle){
    //     get_audio(speaker_handle);
    //     xTaskCreatePinnedToCore(audio_fetch_task, "audio_fetch_task", 8192, NULL, 5, NULL, 1);
    // }
    // else {
    //         ESP_LOGI("SPEAKER", "COULD NOT ATTACH HANDLE TO SPEAKER");
    // }

    xTaskCreate(mic_task, "mic_task", 8192, NULL, 5, NULL);
    //  if(speaker_handle)
    //      xTaskCreate(speaker_test_task, "speaker_task", 4096, speaker_handle, 5, NULL);
    //  else
    //      ESP_LOGE("SPEAKER", "COULD NOT ATTACH HANDLE TO SPEAKER");

    // bt_speaker_init();

}
