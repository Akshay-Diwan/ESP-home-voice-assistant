#ifndef WIFI_COMM_H
#define WIFI_COMM_H

#include "esp_err.h"            // Standard ESP-IDF error codes
#include "freertos/FreeRTOS.h"  // Required for Event Groups
#include "freertos/event_groups.h"

#define WIFI_CONNECTED_BIT BIT0
// Your declarations here...
extern EventGroupHandle_t s_wifi_event_group;
void wifi_init_sta(void);

#endif