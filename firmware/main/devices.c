#include "stdlib.h"
#include "driver/gpio.h"
#include "esp_log.h"
#define RELAY_ON    0
#define RELAY_OFF   1
#define RELAY_FAN       GPIO_NUM_18
#define RELAY_LIGHT     GPIO_NUM_19

char*TAG = "DEVICES";
volatile bool fan_state   = false;
volatile bool light_state = false;

void set_fan(bool on)
{
    fan_state = on;
    gpio_set_level(RELAY_FAN, on ? RELAY_ON : RELAY_OFF);
    ESP_LOGI(TAG, "FAN: %s", on ? "ON" : "OFF");
}

void set_light(bool on)
{
    light_state = on;
    gpio_set_level(RELAY_LIGHT, on ? RELAY_ON : RELAY_OFF);
    ESP_LOGI(TAG, "LIGHT: %s", on ? "ON" : "OFF");
}