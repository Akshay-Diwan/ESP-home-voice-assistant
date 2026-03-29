#include <stdio.h>
#include <string.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/event_groups.h"
#include "esp_system.h"
#include "esp_wifi.h"
#include "esp_event.h"
#include "esp_log.h"

#include "wifi_comm.h" 

static const char *TAG = "WIFI_CONNECT";

// --- SET YOUR DETAILS HERE ---
#define EXAMPLE_ESP_WIFI_SSID      CONFIG_ESP_WIFI_SSID
#define EXAMPLE_ESP_WIFI_PASS      CONFIG_ESP_WIFI_PASSWORD

EventGroupHandle_t s_wifi_event_group;

// Event handler to manage connection status
static void event_handler(void* arg, esp_event_base_t event_base,
                                int32_t event_id, void* event_data) {
    if (event_base == WIFI_EVENT && event_id == WIFI_EVENT_STA_START) {
        ESP_LOGI(TAG, "Connecting to WiFi...");
        esp_wifi_connect();
    } else if (event_base == WIFI_EVENT && event_id == WIFI_EVENT_STA_DISCONNECTED) {
        // --- THIS PART CAPTURES THE REASON CODE ---
        wifi_event_sta_disconnected_t* disconnected = (wifi_event_sta_disconnected_t*) event_data;
        ESP_LOGE(TAG, "Disconnected! Reason Code: %d", disconnected->reason);
        xEventGroupClearBits(s_wifi_event_group, WIFI_CONNECTED_BIT);
        // Retry connection
        esp_wifi_connect();
        ESP_LOGI(TAG, "Retrying...");
    } else if (event_base == IP_EVENT && event_id == IP_EVENT_STA_GOT_IP) {
        ip_event_got_ip_t* event = (ip_event_got_ip_t*) event_data;
        xEventGroupSetBits(s_wifi_event_group, WIFI_CONNECTED_BIT);
        ESP_LOGI(TAG, "Got IP:" IPSTR, IP2STR(&event->ip_info.ip));
    }
}
void wifi_init_sta(void) {
    s_wifi_event_group = xEventGroupCreate();
     ESP_ERROR_CHECK(esp_netif_init());
     ESP_ERROR_CHECK(esp_event_loop_create_default());
     esp_netif_create_default_wifi_sta();

     wifi_init_config_t cfg = WIFI_INIT_CONFIG_DEFAULT();
     ESP_ERROR_CHECK(esp_wifi_init(&cfg));

     esp_event_handler_instance_t instance_any_id;
     esp_event_handler_instance_t instance_got_ip;
     ESP_ERROR_CHECK(esp_event_handler_instance_register(WIFI_EVENT, ESP_EVENT_ANY_ID, &event_handler, NULL, &instance_any_id));
     ESP_ERROR_CHECK(esp_event_handler_instance_register(IP_EVENT, IP_EVENT_STA_GOT_IP, &event_handler, NULL, &instance_got_ip));

     wifi_config_t wifi_config = {
    .sta = {
        .ssid = EXAMPLE_ESP_WIFI_SSID,
        .password = EXAMPLE_ESP_WIFI_PASS,
        /* Setting threshold ensures the device doesn't try to connect to open networks if you don't want it to */
        .threshold.rssi = -127
    },
};
     ESP_ERROR_CHECK(esp_wifi_set_mode(WIFI_MODE_STA));
     ESP_ERROR_CHECK(esp_wifi_set_config(WIFI_IF_STA, &wifi_config));
     // Comment out start and just see if it stays alive after init
    // esp_wifi_set_max_tx_power(8); 
    ESP_LOGI("DEBUG", "Starting radio with reduced power...");
    ESP_ERROR_CHECK(esp_wifi_start()); 
    ESP_LOGI("DEBUG", "Init complete radio started.");

 }