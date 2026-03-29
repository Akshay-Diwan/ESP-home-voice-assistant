// #include "esp_bt.h"
// #include "esp_bt_main.h"
// #include "esp_bt_device.h"
// #include "esp_gap_bt_api.h"
// #include "esp_a2dp_api.h"
// #include "esp_log.h"
// #include "string.h"
// static void a2d_cb(esp_a2d_cb_event_t event, esp_a2d_cb_param_t *param) {
//     switch (event) {
//         case ESP_A2D_CONNECTION_STATE_EVT:
//             if (param->conn_stat.state == ESP_A2D_CONNECTION_STATE_CONNECTED) {
//                 ESP_LOGI("BT_A2DP", "CONNECTED to KDM!");
//                 // Now you can start sending audio data
//             } else if (param->conn_stat.state == ESP_A2D_CONNECTION_STATE_DISCONNECTED) {
//                 ESP_LOGI("BT_A2DP", "Disconnected. Attempting reconnect...");
//             }
//             break;

//         case ESP_A2D_AUDIO_STATE_EVT:
//             if (param->audio_stat.state == ESP_A2D_AUDIO_STATE_STARTED) {
//                 ESP_LOGI("BT_A2DP", "Audio Stream is LIVE.");
//             }
//             break;

//         default:
//             ESP_LOGD("BT_A2DP", "Unhandled A2DP event: %d", event);
//             break;
//     }
// }


// // This function is called by the BT stack to get audio data
// // 'data' is the buffer you fill, 'len' is how many bytes it wants
// int32_t a2d_data_cb(uint8_t *data, int32_t len) {
//     if (len < 0 || data == NULL) {
//         return 0;
//     }

//     // Example: Fill with zeros (silence) for now
//     // Later, you will copy your WAV/PCM data here
//     memset(data, 0, len);

//     return len; // Return how many bytes you actually wrote
// }


// void bt_speaker_init(){
//     esp_bt_controller_mem_release(ESP_BT_MODE_BLE); // Save RAM
//      // 1. Initialize Controller
//     esp_bt_controller_config_t bt_cfg = BT_CONTROLLER_INIT_CONFIG_DEFAULT();
//     esp_err_t ret = esp_bt_controller_init(&bt_cfg);
//     if (ret != ESP_OK) {
//     ESP_LOGE("BT", "initialize controller failed: %s", esp_err_to_name(ret));
//     return;
// }
//     ret = esp_bt_controller_enable(ESP_BT_MODE_CLASSIC_BT);
//     if (ret != ESP_OK) {
//         ESP_LOGE("BT", "enable controller failed: %s", esp_err_to_name(ret));
//         return;
//     }
//     // 2. Initialize Bluedroid
//     ret = esp_bluedroid_init();
//     if (ret != ESP_OK) {
//         ESP_LOGE("BlueDroid", "bluedroid init failed: %s", esp_err_to_name(ret));
//         return;
//     }
//     esp_bluedroid_enable();

//     esp_bt_dev_set_device_name("ESP32_HOME_ASST");
//     esp_bt_sp_param_t param_type = ESP_BT_SP_IOCAP_MODE;
//     esp_bt_io_cap_t iocap = ESP_BT_IO_CAP_NONE;
//     esp_bt_gap_set_security_param(param_type, &iocap, sizeof(uint8_t));

//     // Register the event callback (for connection status)
//     esp_a2d_register_callback(a2d_cb);
    
//     // Register the DATA callback (for the actual audio bytes)
//     esp_a2d_source_register_data_callback(a2d_data_cb);
    
//     // Initialize the A2DP Source
//     esp_a2d_source_init();
    
//     // Start looking for your KDM headphones
//     esp_bt_gap_set_scan_mode(ESP_BT_CONNECTABLE, ESP_BT_GENERAL_DISCOVERABLE);
//     esp_bt_gap_start_discovery(ESP_BT_INQ_MODE_GENERAL_INQUIRY, 10, 0);

//     ESP_LOGI("BT_INIT", "Bluetooth Source Stack Ready!");

// }