#ifndef I2S_MIC_H
#define I2S_MIC_H

#include "driver/i2s_std.h"

// Initialize the I2S peripheral for the INMP441
esp_err_t i2s_mic_init(void);

// Read a batch of samples from the microphone
esp_err_t i2s_mic_read(int32_t *samples, size_t size, size_t *bytes_read);
void mic_filter(int32_t *raw_samples, int count, int16_t *send_buffer);

#endif