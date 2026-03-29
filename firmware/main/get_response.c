// #include "esp_http_client.h"
// #include "cJSON.h"
// char* parse_backend_response(char* raw_data){
//     cJSON *root = cJSON_Parse(raw_data);
//     if(root == NULL){
//         ESP_LOGE("JSON", "Failed to parse JSON: [%s]", cJSON_GetErrorPtr());
//         return;
//     }
//     cJSON_Delete(root);
// }
//  esp_err_t _event_handler(esp_http_client_event_t *evt){
//     switch(evt->event_id){
//         case HTTP_EVENT_ON_DATA:
//             if(!esp_http_client_is_chunked_response(evt->client)){
//                 parse_backend_response((char*)evt->data);
//             }
//             break;
//         default: break;
//     }
//     return ESP_OK;
//  }
//  void sendToAI(char* mac_str){
//     esp_http_client_config_t config = {
//         .url = 'http://192.168.1.2:8080/answer',
//         .method = HTTP_METHOD_GET,
//         .timeout_ms = 7000,
//     };
//     esp_http_client_handle_t client = esp_http_client_init(&config);
//     esp_http_client_set_header(client, "X-Mac-ID", mac_str);
//     esp_err_t err = esp_http_client_perform(client);
//     if (err == ESP_OK)
//      {
//          ESP_LOGI("AI_LINK", "HTTP Status: %d", esp_http_client_get_status_code(client));
//      }
//      else
//      {
//          ESP_LOGE("AI_LINK", "Upload failed: %s", esp_err_to_name(err));
//      }
//  }

#include "esp_http_client.h"
#include "cJSON.h"
#include "driver/i2s_std.h"
#include "esp_log.h"
#include "string.h"

static int total_bytes_received = 0;
static int isAudio = 0;
esp_err_t _post_audio_event_handler(esp_http_client_event_t *evt)
{
    i2s_chan_handle_t speaker_handle = (i2s_chan_handle_t)evt->user_data;

    switch (evt->event_id)
    {
    case HTTP_EVENT_ON_HEADER:
        printf("Header: %s = %s\n", evt->header_key, evt->header_value);
        // Example: Check if the response is JSON
        if (strcmp(evt->header_key, "content-type") == 0)
        {
            if (strstr(evt->header_value, "application/json") != NULL)
            {
                printf("Detected JSON response!\n");
                isAudio = 0;
            }
            if (strstr(evt->header_key, "audio/wav") == 0)
            {
                isAudio = 1;
                printf("WAV file detected\n");
            }
        }

        // Example: Check for your custom AI backend version

        break;
    case HTTP_EVENT_ON_CONNECTED:
        if (isAudio)
            total_bytes_received = 0;
        break;
    case HTTP_EVENT_ON_DATA:
        if (isAudio)
        {
            char *data_ptr = (char *)evt->data;
            int data_len = evt->data_len;
            if (total_bytes_received < 44)
            {
                int offset = 44 - total_bytes_received;
                if (data_len >= offset)
                {
                    data_len = data_len - offset;
                    data_ptr = data_ptr + offset;
                }
                else
                {
                    total_bytes_received += data_len;
                    return ESP_OK;
                }
            }
            total_bytes_received += data_len;
            printf("data length = %d", data_len);
            size_t bytes_written;
            i2s_channel_write(speaker_handle, evt->data, evt->data_len, &bytes_written, portMAX_DELAY);
        }

        break;

    case HTTP_EVENT_ON_FINISH:
        ESP_LOGI("HTTP", "Audio playback finished.");
        break;

    default:
        break;
    }
    return ESP_OK;
}

void get_audio(i2s_chan_handle_t speaker_handle)
{
    esp_http_client_config_t config = {
        .url = "http://192.168.1.2:8080/get-audio",
        .method = HTTP_METHOD_GET,
        .timeout_ms = 5000,
        .event_handler = _post_audio_event_handler,
        .user_data = speaker_handle};
    esp_http_client_handle_t client = esp_http_client_init(&config);
    esp_err_t err = esp_http_client_perform(client);
    if (err == ESP_OK)
    {
        ESP_LOGI("AI_LINK", "HTTP Status: %d", esp_http_client_get_status_code(client));
    }
    else
    {
        ESP_LOGE("AI_LINK", "Upload failed: %s", esp_err_to_name(err));
    }
}
void get_answer(i2s_chan_handle_t speaker_handle, char* mac_str){
    esp_http_client_config_t config = {
       .url = "http://192.168.1.2:8080/answer",
        .method = HTTP_METHOD_GET,
        .timeout_ms = 5000,
        .event_handler = _post_audio_event_handler,
        .user_data = speaker_handle
    };
    esp_http_client_handle_t client = esp_http_client_init(&config);
    esp_http_client_set_header(client, "X-Mac-ID", mac_str);
    esp_err_t err = esp_http_client_perform(client);

    if (err == ESP_OK)
    {
        ESP_LOGI("AI_LINK", "HTTP Status: %d", esp_http_client_get_status_code(client));
    }
    else
    {
        ESP_LOGE("AI_LINK", "Upload failed: %s", esp_err_to_name(err));
    }
    
}
