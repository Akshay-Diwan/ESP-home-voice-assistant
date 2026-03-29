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