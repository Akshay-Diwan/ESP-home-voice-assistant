#include "esp_websocket_client.h"
#include "i2s_mic.h"
#include "esp_log.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"

esp_websocket_client_handle_t client;

static void websocket_event_handler(void *handler_args, esp_event_base_t base, int32_t event_id, void *event_data) {
    esp_websocket_event_data_t *data = (esp_websocket_event_data_t *)event_data;
    switch (event_id) {
        case WEBSOCKET_EVENT_CONNECTED:
            ESP_LOGI("WS", "Connected to Assistant Backend");
            break;
        case WEBSOCKET_EVENT_DATA:
            // This is where the AI's response (text or audio metadata) comes back
            ESP_LOGI("WS", "Received response: %.*s", data->data_len, (char *)data->data_ptr);
            break;
        case WEBSOCKET_EVENT_DISCONNECTED:
            ESP_LOGW("WS", "Disconnected");
            break;
    }
}

void websocket_app_start(void) {
    const esp_websocket_client_config_t ws_cfg = {
        .uri = "ws://192.168.1.2:8080/ws/audio", // Your FastAPI IP
        .task_stack = 8192,
        .task_prio = 8,
    };

    client = esp_websocket_client_init(&ws_cfg);
    esp_websocket_register_events(client, WEBSOCKET_EVENT_ANY, websocket_event_handler, (void *)client);
    esp_websocket_client_start(client);
}

void stream_mic_to_ws() {
    int32_t samples[1024]; // Roughly 4KB of audio data
    int16_t send_buffer[1024];
    size_t bytes_read;

    ESP_LOGI("AUDIO", "Entering continuous stream loop...");

    while (1) { // <--- ADD THIS LOOP
        // Read from INMP441 Mic (GPIO 32, 33, 27)
        i2s_mic_read(samples, sizeof(samples), &bytes_read);

        if (bytes_read > 0 && esp_websocket_client_is_connected(client)) {
            int count = bytes_read / 4; //for 32 bits
            mic_filter(samples, count, send_buffer);
            // Send binary data to the server
            // Using portMAX_DELAY here is fine as it ensures the data is sent
            esp_websocket_client_send_bin(client, (char *)send_buffer, bytes_read, portMAX_DELAY);
        }

        // The i2s_mic_read usually handles the 'tick' yielding, 
        // but if you find the ESP32 getting hot or lagging, 
        // you can add vTaskDelay(1); here.
    }
}

// This is your streaming logic
void audio_stream_task(void *pvParameters) {
    // 1. Wait for WebSocket to be ready
    while (!esp_websocket_client_is_connected(client)) {
        vTaskDelay(pdMS_TO_TICKS(100)); 
    }
    
    ESP_LOGI("AUDIO", "WebSocket connected, starting stream...");

    // 2. Call your streaming function
    // This function contains the 'while(is_recording)' loop we wrote earlier
    stream_mic_to_ws(); 

    // 3. Cleanup if the loop ever breaks
    vTaskDelete(NULL);
}