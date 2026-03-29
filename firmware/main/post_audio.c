#include <stdio.h>
#include <string.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/event_groups.h"
#include "esp_system.h"
#include "esp_wifi.h"
#include "esp_log.h"
#include "esp_http_client.h"
#include "cJSON.h"
#include "wifi_comm.h"
#define MAX_HTTP_OUTPUT_BUFFER 4096
static char response_buffer[MAX_HTTP_OUTPUT_BUFFER];
static int response_len = 0;

// Response Handler
 esp_err_t _http_event_handler(esp_http_client_event_t *evt) {
         switch (evt->event_id) {
         case HTTP_EVENT_ON_DATA:
             // Check if we have enough space in our "bucket"
             if (response_len + evt->data_len < MAX_HTTP_OUTPUT_BUFFER) {
                 memcpy(response_buffer + response_len, evt->data, evt->data_len);
                 response_len += evt->data_len;
                 response_buffer[response_len] = '\0'; // Always keep it as a valid string
             }
             break;

         case HTTP_EVENT_ON_FINISH:
             ESP_LOGI("AI_LINK", "Full Response Received. Parsing...");
            
             // Now we use cJSON to extract just the 'response' field
             cJSON *root = cJSON_Parse(response_buffer);
             if (root) {
                 cJSON *ai_response = cJSON_GetObjectItem(root, "response");
                 if (cJSON_IsString(ai_response)) {
                     printf("\n--- AI SAYS ---\n%s\n--------------\n", ai_response->valuestring);
                 }
                 cJSON_Delete(root);
             }
             // Reset for the next request
             response_len = 0;
             break;

         default:
             break;
     }
     return ESP_OK;
 }

 //Post Request
void talk_to_laptop_ai() {
     esp_http_client_config_t config = {
         .url = "http://192.168.1.2:11434/api/generate", 
         .method = HTTP_METHOD_POST,
         .event_handler = _http_event_handler, //Attach our receiver
         .timeout_ms = 15000, //15s thinking time
     };
     esp_http_client_handle_t client = esp_http_client_init(&config);

     // This is the JSON format Ollama expects

     const char *post_data = "{\"model\": \"tinyllama:1.1b\", \"prompt\": \"<|system|>You are a helpful smart home automation assistant.<|end|><|user|>Hi, how are you?<|end|><|assistant|>\", \"stream\": false}";
    
     esp_http_client_set_post_field(client, post_data, strlen(post_data));
     esp_http_client_set_header(client, "Content-Type", "application/json");

     esp_err_t err = esp_http_client_perform(client);
     if (err == ESP_OK) { //Success
         ESP_LOGI("AI_LINK", "Status = %d, content_length = %lld",
                 esp_http_client_get_status_code(client),
                 esp_http_client_get_content_length(client));
     } else { //Failure
         ESP_LOGE("AI_LINK", "HTTP POST request failed: %s", esp_err_to_name(err));
     }

     esp_http_client_cleanup(client);
 }


//Task
 void ai_communication_task(void *pvParameters) {
     ESP_LOGI("STATUS", "Waiting for WiFi connection...");
    
     xEventGroupWaitBits(s_wifi_event_group,
                         WIFI_CONNECTED_BIT,
                         pdFALSE,        // Don't clear the bit on exit
                         pdTRUE,         // Wait for all bits (just one here)
                         portMAX_DELAY); 

     ESP_LOGI("STATUS", "WiFi should be ready. Starting AI Request.");
     talk_to_laptop_ai();

     // Delete the task when done so it doesn't run in a loop
     vTaskDelete(NULL);
 }