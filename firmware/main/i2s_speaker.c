#include "driver/i2s_std.h"
#include "esp_log.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
static const char *TAG = "SPEAKER_TEST";


i2s_chan_handle_t speaker_init(){
    i2s_chan_handle_t tx_handle;
    // 1. Channel Configuration (Using I2S_NUM_1 to avoid Mic conflict)
    // i2s_chan_config_t chan_cfg = I2S_CHANNEL_DEFAULT_CONFIG(I2S_NUM_1, I2S_ROLE_MASTER);
    i2s_chan_config_t chan_cfg = {
        .id = I2S_NUM_1,
        .role = I2S_ROLE_MASTER,
        .dma_desc_num = 8,
        .dma_frame_num = 256,
        .auto_clear = true
    };
    ESP_ERROR_CHECK(i2s_new_channel(&chan_cfg, &tx_handle, NULL));

    // 2. Standard Mode Configuration (16kHz, 16-bit)
    i2s_std_config_t std_cfg = {
        .clk_cfg = I2S_STD_CLK_DEFAULT_CONFIG(16000),
        .slot_cfg = I2S_STD_MSB_SLOT_DEFAULT_CONFIG(I2S_DATA_BIT_WIDTH_16BIT, I2S_SLOT_MODE_MONO),
        .gpio_cfg = {
            .mclk = I2S_GPIO_UNUSED,
            .bclk = GPIO_NUM_14,    
            .ws   = GPIO_NUM_26,    
            .dout = GPIO_NUM_25,    
            .din  = I2S_GPIO_UNUSED,
        },
    };
      // 3. Initialize and Enable the Channel
    ESP_ERROR_CHECK(i2s_channel_init_std_mode(tx_handle, &std_cfg));
    ESP_ERROR_CHECK(i2s_channel_enable(tx_handle));
    ESP_LOGI("SPEAKER", "Speaker initialized Successfully !! ");
    return tx_handle;


}
// void speaker_test_task(void *pvParameters) {
//     i2s_chan_handle_t tx_handle;

//     // 1. Channel Configuration (Using I2S_NUM_1 to avoid Mic conflict)
//     i2s_chan_config_t chan_cfg = I2S_CHANNEL_DEFAULT_CONFIG(I2S_NUM_1, I2S_ROLE_MASTER);
//     ESP_ERROR_CHECK(i2s_new_channel(&chan_cfg, &tx_handle, NULL));

//     // 2. Standard Mode Configuration (16kHz, 16-bit)
//     i2s_std_config_t std_cfg = {
//         .clk_cfg = I2S_STD_CLK_DEFAULT_CONFIG(16000),
//         .slot_cfg = I2S_STD_MSB_SLOT_DEFAULT_CONFIG(I2S_DATA_BIT_WIDTH_16BIT, I2S_SLOT_MODE_MONO),
//         .gpio_cfg = {
//             .mclk = I2S_GPIO_UNUSED,
//             .bclk = GPIO_NUM_26,    // Shared Clock with Mic
//             .ws   = GPIO_NUM_25,    // Shared WS with Mic
//             .dout = GPIO_NUM_22,    // Dedicated Speaker Data Pin
//             .din  = I2S_GPIO_UNUSED,
//         },
//     };

//     // 3. Initialize and Enable the Channel
//     ESP_ERROR_CHECK(i2s_channel_init_std_mode(tx_handle, &std_cfg));
//     ESP_ERROR_CHECK(i2s_channel_enable(tx_handle));

//     ESP_LOGI(TAG, "Speaker test started on GPIO 22...");

//     // 4. Generate a 400Hz Square Wave
//     // At 16000Hz, a 400Hz wave repeats every 40 samples (16000/400 = 40)
//     int16_t samples[120]; // 3 full cycles
//     size_t bytes_written;

//     while (1) {
//         for (int i = 0; i < 120; i++) {
//             // High for half the cycle (20 samples), Low for half
//             samples[i] = (i % 40 < 20) ? 3000 : -3000; 
//         }

//         // Write to I2S (This blocks until the buffer is free)
//         i2s_channel_write(tx_handle, samples, sizeof(samples), &bytes_written, portMAX_DELAY);
        
//         // Small delay to prevent watchdog issues if needed, 
//         // though i2s_channel_write handles timing.
//         vTaskDelay(pdMS_TO_TICKS(10)); 
//     }
// }

void speaker_test_task(void *pvParameters) {
    i2s_chan_handle_t tx_handle = (i2s_chan_handle_t)pvParameters;
    
    // 1kHz Square Wave at 16kHz Sample Rate
    // A full cycle is 16 samples (16000 / 1000 = 16)
    int16_t beep_buffer[160]; // 10 full cycles
    for (int i = 0; i < 160; i++) {
        // High for 8 samples, Low for 8 samples
        beep_buffer[i] = (i % 16 < 8) ? 60000 : -60000; 
    }

    size_t bytes_written;
    while (1) {
        // Play the beep for 200ms
        for(int i = 0; i < 20; i++) {
            i2s_channel_write(tx_handle, beep_buffer, sizeof(beep_buffer), &bytes_written, portMAX_DELAY);
        }

        // Silence for 500ms
        vTaskDelay(pdMS_TO_TICKS(1000)); 
        
        ESP_LOGI("BEEP", "Heartbeat Beep Sent");
    }
}

