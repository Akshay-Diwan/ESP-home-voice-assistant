#include "i2s_mic.h"
#include "esp_log.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"

static const char *TAG = "I2S_MIC";
static i2s_chan_handle_t rx_chan = NULL;

esp_err_t i2s_mic_init(void) {
    i2s_chan_config_t chan_cfg = {
    .id = I2S_NUM_0,              // Use I2S port 0
    .role = I2S_ROLE_MASTER,      // ESP32 provides the clock
    .dma_desc_num = 6,            // Number of DMA descriptors
    .dma_frame_num = 240,         // Number of frames in one descriptor
    .auto_clear = false,          // Don't auto-clear the DMA buffer
};
    ESP_ERROR_CHECK(i2s_new_channel(&chan_cfg, NULL, &rx_chan));

    i2s_std_config_t std_cfg = {
        .clk_cfg = I2S_STD_CLK_DEFAULT_CONFIG(16000),
        .slot_cfg = I2S_STD_PHILIPS_SLOT_DEFAULT_CONFIG(I2S_DATA_BIT_WIDTH_32BIT, I2S_SLOT_MODE_MONO),
        .gpio_cfg = {
            .mclk = I2S_GPIO_UNUSED,
            .bclk = GPIO_NUM_27,
            .ws   = GPIO_NUM_33,
            .dout = I2S_GPIO_UNUSED,
            .din  = GPIO_NUM_32, 
        },
    };

    ESP_ERROR_CHECK(i2s_channel_init_std_mode(rx_chan, &std_cfg));
    return i2s_channel_enable(rx_chan);
}

esp_err_t i2s_mic_read(int32_t *samples, size_t size, size_t *bytes_read) {
    return i2s_channel_read(rx_chan, samples, size, bytes_read, portMAX_DELAY);
}
// void mic_filter(int32_t *raw_samples, int count, int16_t *send_buffer) {
//     // 'static' ensures these stay in memory between calls
//     static float prev_raw_f = 0;
//     static float prev_filtered_f = 0;
    
//     float alpha = 0.95; // 0.6 is a bit aggressive, 0.95 is smoother for voice
 
//     for(int i = 0; i < count; i++) {
//         // 1. Better alignment: Most INMP441 setups use >> 14 or >> 12
//         float current_raw_f = (float)(raw_samples[i] >> 14);

//         // 2. High-Pass Filter Equation
//         float filtered_val = alpha * (prev_filtered_f + current_raw_f - prev_raw_f);

//         // 3. Update history
//         prev_raw_f = current_raw_f;  
//         prev_filtered_f = filtered_val;

//         // 4. Convert to int16_t
//         int16_t final_sample = (int16_t)filtered_val;
                 
//         // 5. Noise Gate (Be careful with 600, try 300 if words get cut off)
//         if (abs(final_sample) < 600) {
//             final_sample = 0;
//         }

//         // 6. Store back as int16_t (Cast properly to the buffer)
//         // Since you are sending this to Python as 16-bit, ensure the 
//         // receiving side knows to treat the buffer as int16.
//         raw_samples[i] = final_sample;
//     }
// }
/**
 * @brief High-Pass Filter and Noise Gate for INMP441
 * @param raw_samples Pointer to the 32-bit I2S buffer
 * @param count Number of samples to process
 * @param send_buffer Pointer to the 16-bit buffer to be sent to Python
 */
void mic_filter(int32_t *raw_samples, int count, int16_t *send_buffer) {
    // static variables persist in memory between function calls
    static float prev_raw_f = 0;
    static float prev_filtered_f = 0;
    
    // Alpha 0.99 is great for keeping voice body while removing DC hum
    const float alpha = 0.99f; 
    const int16_t noise_gate_threshold = 600;

    for(int i = 0; i < count; i++) {
        // 1. Shift 24-bit data from INMP441 to 16-bit range
        // We use float for high-precision filtering math
        float current_raw_f = (float)(raw_samples[i] >> 14);

        // 2. High-Pass Filter Equation: y[n] = alpha * (y[n-1] + x[n] - x[n-1])
        float filtered_val = alpha * (prev_filtered_f + current_raw_f - prev_raw_f);

        // 3. Update history for the next sample
        prev_raw_f = current_raw_f;
        prev_filtered_f = filtered_val;

        // 4. Convert to 16-bit integer
        int16_t final_sample = (int16_t)filtered_val;

        // 5. Apply Noise Gate to kill background hiss from the 10W speaker/buck converter
        if (abs(final_sample) < noise_gate_threshold) {
            final_sample = 0;
        }

        // 6. Store in the "tight" 16-bit buffer for the WebSocket
        send_buffer[i] = final_sample;
    }
}
