// #include <stdio.h>
// #include <string.h>
// #include <math.h>
// #include "freertos/FreeRTOS.h"
// #include "freertos/task.h"
// #include "driver/i2c.h"
// #include "driver/gpio.h"
// #include "esp_log.h"
// #include "esp_timer.h"
// #include "rom/ets_sys.h"

// static const char *TAG = "ROOM";

// // ═══════════════════════════════════════════
// //  PIN CONFIG
// // ═══════════════════════════════════════════
// #define I2C_PORT        I2C_NUM_0
// #define I2C_SDA         21
// #define I2C_SCL         22
// #define I2C_FREQ        400000

// #define DHT11_PIN       GPIO_NUM_4
// #define RELAY_FAN       GPIO_NUM_26
// #define RELAY_LIGHT     GPIO_NUM_27

// // ═══════════════════════════════════════════
// //  OLED CONFIG (SSD1306 0x3C)
// // ═══════════════════════════════════════════
// #define OLED_ADDR       0x3C
// #define OLED_WIDTH      128
// #define OLED_PAGES      8

// // ═══════════════════════════════════════════
// //  DS3231 CONFIG (0x68)
// // ═══════════════════════════════════════════
// #define DS3231_ADDR     0x68

// // ═══════════════════════════════════════════
// //  AUTOMATION CONFIG
// // ═══════════════════════════════════════════
// #define FAN_ON_TEMP         28      // Fan ON  if temp >= 28°C
// #define FAN_OFF_TEMP        25      // Fan OFF if temp <= 25°C
// #define LIGHT_ON_HOUR       18      // Light ON  at 6 PM
// #define LIGHT_OFF_HOUR      6       // Light OFF at 6 AM

// // ═══════════════════════════════════════════
// //  FONT 5x7
// // ═══════════════════════════════════════════
// static const uint8_t font5x7[][5] = {
//     {0x00,0x00,0x00,0x00,0x00}, // ' '
//     {0x00,0x00,0x5F,0x00,0x00}, // '!'
//     {0x00,0x07,0x00,0x07,0x00}, // '"'
//     {0x14,0x7F,0x14,0x7F,0x14}, // '#'
//     {0x24,0x2A,0x7F,0x2A,0x12}, // '$'
//     {0x23,0x13,0x08,0x64,0x62}, // '%'
//     {0x36,0x49,0x55,0x22,0x50}, // '&'
//     {0x00,0x05,0x03,0x00,0x00}, // '''
//     {0x00,0x1C,0x22,0x41,0x00}, // '('
//     {0x00,0x41,0x22,0x1C,0x00}, // ')'
//     {0x14,0x08,0x3E,0x08,0x14}, // '*'
//     {0x08,0x08,0x3E,0x08,0x08}, // '+'
//     {0x00,0x50,0x30,0x00,0x00}, // ','
//     {0x08,0x08,0x08,0x08,0x08}, // '-'
//     {0x00,0x60,0x60,0x00,0x00}, // '.'
//     {0x20,0x10,0x08,0x04,0x02}, // '/'
//     {0x3E,0x51,0x49,0x45,0x3E}, // '0'
//     {0x00,0x42,0x7F,0x40,0x00}, // '1'
//     {0x42,0x61,0x51,0x49,0x46}, // '2'
//     {0x21,0x41,0x45,0x4B,0x31}, // '3'
//     {0x18,0x14,0x12,0x7F,0x10}, // '4'
//     {0x27,0x45,0x45,0x45,0x39}, // '5'
//     {0x3C,0x4A,0x49,0x49,0x30}, // '6'
//     {0x01,0x71,0x09,0x05,0x03}, // '7'
//     {0x36,0x49,0x49,0x49,0x36}, // '8'
//     {0x06,0x49,0x49,0x29,0x1E}, // '9'
//     {0x00,0x36,0x36,0x00,0x00}, // ':'
//     {0x00,0x56,0x36,0x00,0x00}, // ';'
//     {0x08,0x14,0x22,0x41,0x00}, // '<'
//     {0x14,0x14,0x14,0x14,0x14}, // '='
//     {0x00,0x41,0x22,0x14,0x08}, // '>'
//     {0x02,0x01,0x51,0x09,0x06}, // '?'
//     {0x32,0x49,0x79,0x41,0x3E}, // '@'
//     {0x7E,0x11,0x11,0x11,0x7E}, // 'A'
//     {0x7F,0x49,0x49,0x49,0x36}, // 'B'
//     {0x3E,0x41,0x41,0x41,0x22}, // 'C'
//     {0x7F,0x41,0x41,0x22,0x1C}, // 'D'
//     {0x7F,0x49,0x49,0x49,0x41}, // 'E'
//     {0x7F,0x09,0x09,0x09,0x01}, // 'F'
//     {0x3E,0x41,0x49,0x49,0x7A}, // 'G'
//     {0x7F,0x08,0x08,0x08,0x7F}, // 'H'
//     {0x00,0x41,0x7F,0x41,0x00}, // 'I'
//     {0x20,0x40,0x41,0x3F,0x01}, // 'J'
//     {0x7F,0x08,0x14,0x22,0x41}, // 'K'
//     {0x7F,0x40,0x40,0x40,0x40}, // 'L'
//     {0x7F,0x02,0x0C,0x02,0x7F}, // 'M'
//     {0x7F,0x04,0x08,0x10,0x7F}, // 'N'
//     {0x3E,0x41,0x41,0x41,0x3E}, // 'O'
//     {0x7F,0x09,0x09,0x09,0x06}, // 'P'
//     {0x3E,0x41,0x51,0x21,0x5E}, // 'Q'
//     {0x7F,0x09,0x19,0x29,0x46}, // 'R'
//     {0x46,0x49,0x49,0x49,0x31}, // 'S'
//     {0x01,0x01,0x7F,0x01,0x01}, // 'T'
//     {0x3F,0x40,0x40,0x40,0x3F}, // 'U'
//     {0x1F,0x20,0x40,0x20,0x1F}, // 'V'
//     {0x3F,0x40,0x38,0x40,0x3F}, // 'W'
//     {0x63,0x14,0x08,0x14,0x63}, // 'X'
//     {0x07,0x08,0x70,0x08,0x07}, // 'Y'
//     {0x61,0x51,0x49,0x45,0x43}, // 'Z'
//     {0x00,0x7F,0x41,0x41,0x00}, // '['
//     {0x02,0x04,0x08,0x10,0x20}, // '\'
//     {0x00,0x41,0x41,0x7F,0x00}, // ']'
//     {0x04,0x02,0x01,0x02,0x04}, // '^'
//     {0x40,0x40,0x40,0x40,0x40}, // '_'
//     {0x00,0x01,0x02,0x04,0x00}, // '`'
//     {0x20,0x54,0x54,0x54,0x78}, // 'a'
//     {0x7F,0x48,0x44,0x44,0x38}, // 'b'
//     {0x38,0x44,0x44,0x44,0x20}, // 'c'
//     {0x38,0x44,0x44,0x48,0x7F}, // 'd'
//     {0x38,0x54,0x54,0x54,0x18}, // 'e'
//     {0x08,0x7E,0x09,0x01,0x02}, // 'f'
//     {0x0C,0x52,0x52,0x52,0x3E}, // 'g'
//     {0x7F,0x08,0x04,0x04,0x78}, // 'h'
//     {0x00,0x44,0x7D,0x40,0x00}, // 'i'
//     {0x20,0x40,0x44,0x3D,0x00}, // 'j'
//     {0x7F,0x10,0x28,0x44,0x00}, // 'k'
//     {0x00,0x41,0x7F,0x40,0x00}, // 'l'
//     {0x7C,0x04,0x18,0x04,0x78}, // 'm'
//     {0x7C,0x08,0x04,0x04,0x78}, // 'n'
//     {0x38,0x44,0x44,0x44,0x38}, // 'o'
//     {0x7C,0x14,0x14,0x14,0x08}, // 'p'
//     {0x08,0x14,0x14,0x18,0x7C}, // 'q'
//     {0x7C,0x08,0x04,0x04,0x08}, // 'r'
//     {0x48,0x54,0x54,0x54,0x20}, // 's'
//     {0x04,0x3F,0x44,0x40,0x20}, // 't'
//     {0x3C,0x40,0x40,0x20,0x7C}, // 'u'
//     {0x1C,0x20,0x40,0x20,0x1C}, // 'v'
//     {0x3C,0x40,0x30,0x40,0x3C}, // 'w'
//     {0x44,0x28,0x10,0x28,0x44}, // 'x'
//     {0x0C,0x50,0x50,0x50,0x3C}, // 'y'
//     {0x44,0x64,0x54,0x4C,0x44}, // 'z'
// };

// // ═══════════════════════════════════════════
// //  OLED FUNCTIONS
// // ═══════════════════════════════════════════
// static esp_err_t oled_cmd(uint8_t cmd)
// {
//     i2c_cmd_handle_t h = i2c_cmd_link_create();
//     i2c_master_start(h);
//     i2c_master_write_byte(h, (OLED_ADDR << 1)|I2C_MASTER_WRITE, true);
//     i2c_master_write_byte(h, 0x00, true);
//     i2c_master_write_byte(h, cmd,  true);
//     i2c_master_stop(h);
//     esp_err_t r = i2c_master_cmd_begin(I2C_PORT, h, pdMS_TO_TICKS(100));
//     i2c_cmd_link_delete(h);
//     return r;
// }

// static void oled_data(uint8_t *buf, size_t len)
// {
//     i2c_cmd_handle_t h = i2c_cmd_link_create();
//     i2c_master_start(h);
//     i2c_master_write_byte(h, (OLED_ADDR << 1)|I2C_MASTER_WRITE, true);
//     i2c_master_write_byte(h, 0x40, true);
//     i2c_master_write(h, buf, len, true);
//     i2c_master_stop(h);
//     i2c_master_cmd_begin(I2C_PORT, h, pdMS_TO_TICKS(100));
//     i2c_cmd_link_delete(h);
// }

// static void oled_cursor(uint8_t page, uint8_t col)
// {
//     oled_cmd(0xB0 | page);
//     oled_cmd(0x00 | (col & 0x0F));
//     oled_cmd(0x10 | (col >> 4));
// }

// static esp_err_t oled_init(void)
// {
//     vTaskDelay(pdMS_TO_TICKS(100));
//     uint8_t cmds[] = {
//         0xAE, 0xD5, 0x80, 0xA8, 0x3F,
//         0xD3, 0x00, 0x40, 0x8D, 0x14,
//         0x20, 0x00, 0xA1, 0xC8,
//         0xDA, 0x12, 0x81, 0xCF,
//         0xD9, 0xF1, 0xDB, 0x40,
//         0xA4, 0xA6, 0xAF,
//     };
//     for (int i = 0; i < (int)sizeof(cmds); i++) {
//         if (oled_cmd(cmds[i]) != ESP_OK) return ESP_FAIL;
//     }
//     return ESP_OK;
// }

// static void oled_clear(void)
// {
//     uint8_t blank[OLED_WIDTH];
//     memset(blank, 0, sizeof(blank));
//     for (uint8_t p = 0; p < OLED_PAGES; p++) {
//         oled_cursor(p, 0);
//         oled_data(blank, sizeof(blank));
//     }
// }

// static void oled_print(uint8_t page, uint8_t col, const char *text)
// {
//     oled_cursor(page, col);
//     while (*text) {
//         uint8_t c = (uint8_t)*text++;
//         if (c < 32 || c > 122) c = 32;
//         uint8_t g[6];
//         memcpy(g, font5x7[c - 32], 5);
//         g[5] = 0x00;
//         oled_data(g, 6);
//     }
// }

// // Clear single row
// static void oled_clear_row(uint8_t page)
// {
//     uint8_t blank[OLED_WIDTH];
//     memset(blank, 0, sizeof(blank));
//     oled_cursor(page, 0);
//     oled_data(blank, sizeof(blank));
// }

// // ═══════════════════════════════════════════
// //  DS3231 FUNCTIONS
// // ═══════════════════════════════════════════
// static uint8_t bcd2dec(uint8_t bcd) { return (bcd >> 4) * 10 + (bcd & 0x0F); }
// static uint8_t dec2bcd(uint8_t dec) { return ((dec / 10) << 4) | (dec % 10); }

// typedef struct {
//     uint8_t sec, min, hour;
//     uint8_t day, month;
//     uint16_t year;
// } rtc_time_t;

// static esp_err_t ds3231_write_reg(uint8_t reg, uint8_t *data, size_t len)
// {
//     i2c_cmd_handle_t h = i2c_cmd_link_create();
//     i2c_master_start(h);
//     i2c_master_write_byte(h, (DS3231_ADDR << 1)|I2C_MASTER_WRITE, true);
//     i2c_master_write_byte(h, reg, true);
//     i2c_master_write(h, data, len, true);
//     i2c_master_stop(h);
//     esp_err_t r = i2c_master_cmd_begin(I2C_PORT, h, pdMS_TO_TICKS(100));
//     i2c_cmd_link_delete(h);
//     return r;
// }

// static esp_err_t ds3231_read_reg(uint8_t reg, uint8_t *data, size_t len)
// {
//     i2c_cmd_handle_t h = i2c_cmd_link_create();
//     i2c_master_start(h);
//     i2c_master_write_byte(h, (DS3231_ADDR << 1)|I2C_MASTER_WRITE, true);
//     i2c_master_write_byte(h, reg, true);
//     i2c_master_stop(h);
//     i2c_master_cmd_begin(I2C_PORT, h, pdMS_TO_TICKS(100));
//     i2c_cmd_link_delete(h);

//     h = i2c_cmd_link_create();
//     i2c_master_start(h);
//     i2c_master_write_byte(h, (DS3231_ADDR << 1)|I2C_MASTER_READ, true);
//     if (len > 1)
//         i2c_master_read(h, data, len - 1, I2C_MASTER_ACK);
//     i2c_master_read_byte(h, data + len - 1, I2C_MASTER_NACK);
//     i2c_master_stop(h);
//     esp_err_t r = i2c_master_cmd_begin(I2C_PORT, h, pdMS_TO_TICKS(100));
//     i2c_cmd_link_delete(h);
//     return r;
// }

// // Call this ONCE to set time — change values as needed
// static void ds3231_set_time(void)
// {
//     // SET YOUR TIME HERE: 25 Mar 2026, Wednesday, 13:08:00
//     uint8_t data[7] = {
//         dec2bcd(0),     // seconds
//         dec2bcd(00),     // minutes
//         dec2bcd(10),    // hours (24hr)
//         dec2bcd(4),     // day of week (1=Sun, 4=Wed)
//         dec2bcd(25),    // date
//         dec2bcd(3),     // month
//         dec2bcd(26),    // year (last 2 digits)
//     };
//     ds3231_write_reg(0x00, data, 7);
//     ESP_LOGI(TAG, "RTC time set!");
// }

// static esp_err_t ds3231_get_time(rtc_time_t *t)
// {
//     uint8_t data[7];
//     esp_err_t r = ds3231_read_reg(0x00, data, 7);
//     if (r != ESP_OK) return r;
//     t->sec   = bcd2dec(data[0] & 0x7F);
//     t->min   = bcd2dec(data[1]);
//     t->hour  = bcd2dec(data[2] & 0x3F);
//     t->day   = bcd2dec(data[4]);
//     t->month = bcd2dec(data[5] & 0x1F);
//     t->year  = bcd2dec(data[6]) + 2000;
//     return ESP_OK;
// }

// // ═══════════════════════════════════════════
// //  DHT11 FUNCTIONS
// // ═══════════════════════════════════════════
// typedef struct {
//     int temperature;
//     int humidity;
//     bool valid;
// } dht11_data_t;

// static dht11_data_t dht11_read(void)
// {
//     dht11_data_t result = {0, 0, false};
//     uint8_t data[5] = {0};

//     // Send start signal
//     gpio_set_direction(DHT11_PIN, GPIO_MODE_OUTPUT);
//     gpio_set_level(DHT11_PIN, 0);
//     vTaskDelay(pdMS_TO_TICKS(20));
//     gpio_set_level(DHT11_PIN, 1);
//     ets_delay_us(30);
//     gpio_set_direction(DHT11_PIN, GPIO_MODE_INPUT);

//     // Wait for DHT11 response
//     int timeout = 100;
//     while (gpio_get_level(DHT11_PIN) == 1 && timeout--) ets_delay_us(1);
//     if (timeout <= 0) return result;

//     timeout = 100;
//     while (gpio_get_level(DHT11_PIN) == 0 && timeout--) ets_delay_us(1);
//     if (timeout <= 0) return result;

//     timeout = 100;
//     while (gpio_get_level(DHT11_PIN) == 1 && timeout--) ets_delay_us(1);
//     if (timeout <= 0) return result;

//     // Read 40 bits
//     for (int i = 0; i < 40; i++) {
//         timeout = 100;
//         while (gpio_get_level(DHT11_PIN) == 0 && timeout--) ets_delay_us(1);
//         if (timeout <= 0) return result;

//         ets_delay_us(40);

//         if (gpio_get_level(DHT11_PIN) == 1) {
//             data[i / 8] |= (1 << (7 - (i % 8)));
//         }

//         timeout = 100;
//         while (gpio_get_level(DHT11_PIN) == 1 && timeout--) ets_delay_us(1);
//     }

//     // Checksum
//     if (data[4] != ((data[0] + data[1] + data[2] + data[3]) & 0xFF))
//         return result;

//     result.humidity    = data[0];
//     result.temperature = data[2];
//     result.valid       = true;
//     return result;
// }

// // ═══════════════════════════════════════════
// //  RELAY CONTROL
// // ═══════════════════════════════════════════
// // Relay is ACTIVE LOW (most relay modules)
// #define RELAY_ON    0
// #define RELAY_OFF   1

// static bool fan_state   = false;
// static bool light_state = false;

// static void set_fan(bool on)
// {
//     fan_state = on;
//     gpio_set_level(RELAY_FAN, on ? RELAY_ON : RELAY_OFF);
//     ESP_LOGI(TAG, "FAN: %s", on ? "ON" : "OFF");
// }

// static void set_light(bool on)
// {
//     light_state = on;
//     gpio_set_level(RELAY_LIGHT, on ? RELAY_ON : RELAY_OFF);
//     ESP_LOGI(TAG, "LIGHT: %s", on ? "ON" : "OFF");
// }

// // ═══════════════════════════════════════════
// //  DISPLAY UPDATE
// // ═══════════════════════════════════════════
// static void update_display(rtc_time_t *t, dht11_data_t *dht)
// {
//     char buf[32];

//     // Row 0: Title
//     oled_print(0, 0, "  Room Automation  ");

//     // Separator line
//     uint8_t line[OLED_WIDTH];
//     memset(line, 0xFF, sizeof(line));
//     oled_cursor(1, 0);
//     oled_data(line, sizeof(line));

//     // Row 2: Time
//     snprintf(buf, sizeof(buf), "Time: %02d:%02d:%02d",
//              t->hour, t->min, t->sec);
//     oled_clear_row(2);
//     oled_print(2, 0, buf);

//     // Row 3: Date
//     snprintf(buf, sizeof(buf), "Date: %02d/%02d/%04d",
//              t->day, t->month, t->year);
//     oled_clear_row(3);
//     oled_print(3, 0, buf);

//     // Row 4: Temperature
//     if (dht->valid) {
//         snprintf(buf, sizeof(buf), "Temp: %dC  Hum: %d%%",
//                  dht->temperature, dht->humidity);
//     } else {
//         snprintf(buf, sizeof(buf), "Temp: --  Hum: --");
//     }
//     oled_clear_row(4);
//     oled_print(4, 0, buf);

//     // Separator
//     memset(line, 0xFF, sizeof(line));
//     oled_cursor(5, 0);
//     oled_data(line, sizeof(line));

//     // Row 6: Fan status
//     snprintf(buf, sizeof(buf), "Fan: %-3s  Thr:%dC",
//              fan_state ? "ON" : "OFF", FAN_ON_TEMP);
//     oled_clear_row(6);
//     oled_print(6, 0, buf);

//     // Row 7: Light status
//     snprintf(buf, sizeof(buf), "Light: %-3s (%02d-%02dh)",
//              light_state ? "ON" : "OFF",
//              LIGHT_ON_HOUR, LIGHT_OFF_HOUR);
//     oled_clear_row(7);
//     oled_print(7, 0, buf);
// }

// // ═══════════════════════════════════════════
// //  AUTOMATION LOGIC
// // ═══════════════════════════════════════════
// static void run_automation(rtc_time_t *t, dht11_data_t *dht)
// {
//     // Fan control by temperature (hysteresis)
//     if (dht->valid) {
//         if (!fan_state && dht->temperature >= FAN_ON_TEMP)
//             set_fan(true);
//         else if (fan_state && dht->temperature <= FAN_OFF_TEMP)
//             set_fan(false);
//     }

//     // Light control by time
//     // ON from LIGHT_ON_HOUR (18:00) to LIGHT_OFF_HOUR (06:00)
//     bool should_light = false;
//     if (LIGHT_ON_HOUR > LIGHT_OFF_HOUR) {
//         // Spans midnight: ON if hour >= 18 OR hour < 6
//         should_light = (t->hour >= LIGHT_ON_HOUR || t->hour < LIGHT_OFF_HOUR);
//     } else {
//         // Normal range
//         should_light = (t->hour >= LIGHT_ON_HOUR && t->hour < LIGHT_OFF_HOUR);
//     }

//     if (should_light != light_state)
//         set_light(should_light);
// }


// // ═══════════════════════════════════════════
// //  app_main
// // ═══════════════════════════════════════════
// void app_main(void)
// {
//     printf("=== ROOM AUTOMATION STARTED ===\n");

//     // ── 1. I2C Init ───────────────────────
//     i2c_config_t conf = {
//         .mode             = I2C_MODE_MASTER,
//         .sda_io_num       = I2C_SDA,
//         .scl_io_num       = I2C_SCL,
//         .sda_pullup_en    = GPIO_PULLUP_ENABLE,
//         .scl_pullup_en    = GPIO_PULLUP_ENABLE,
//         .master.clk_speed = I2C_FREQ,
//     };
//     ESP_ERROR_CHECK(i2c_param_config(I2C_PORT, &conf));
//     ESP_ERROR_CHECK(i2c_driver_install(I2C_PORT, I2C_MODE_MASTER, 0, 0, 0));
//     printf("I2C OK\n");

//     // ── 2. GPIO Init ──────────────────────
//     gpio_config_t relay_conf = {
//         .pin_bit_mask = (1ULL << RELAY_FAN) | (1ULL << RELAY_LIGHT),
//         .mode         = GPIO_MODE_OUTPUT,
//         .pull_up_en   = GPIO_PULLUP_DISABLE,
//         .pull_down_en = GPIO_PULLDOWN_DISABLE,
//         .intr_type    = GPIO_INTR_DISABLE,
//     };
//     gpio_config(&relay_conf);

//     // Start with relays OFF
//     gpio_set_level(RELAY_FAN,   RELAY_OFF);
//     gpio_set_level(RELAY_LIGHT, RELAY_OFF);
//     printf("GPIO OK\n");

//     // ── 3. OLED Init ──────────────────────
//     if (oled_init() != ESP_OK) {
//         printf("OLED FAILED — check wiring!\n");
//         while(1) vTaskDelay(pdMS_TO_TICKS(1000));
//     }
//     oled_clear();
//     oled_print(3, 10, "Starting...");
//     printf("OLED OK\n");

//     // ── 4. RTC Init ───────────────────────
//     // ⚠️ UNCOMMENT ds3231_set_time() ONLY FIRST TIME
//     // then comment it out again so time doesn't reset on reboot
//     // ds3231_set_time();

//     rtc_time_t t;
//     if (ds3231_get_time(&t) != ESP_OK) {
//         printf("DS3231 FAILED — check wiring!\n");
//         oled_print(4, 0, "RTC ERROR!");
//         while(1) vTaskDelay(pdMS_TO_TICKS(1000));
//     }
//     printf("RTC OK — %02d:%02d:%02d\n", t.hour, t.min, t.sec);

//     vTaskDelay(pdMS_TO_TICKS(1000));
//     oled_clear();

//     // ── 5. Main Loop ──────────────────────
//     dht11_data_t dht = {0, 0, false};
//     int dht_counter = 0;

//     while (1) {
//         // Read DHT11 every 3 seconds (DHT11 needs 2s between reads)
//         if (dht_counter % 3 == 0) {
//             dht = dht11_read();
//             if (dht.valid) {
//                 printf("DHT11 — Temp: %d°C  Hum: %d%%\n",
//                        dht.temperature, dht.humidity);
//             } else {
//                 printf("DHT11 read failed\n");
//             }
//         }
//         dht_counter++;

//         // Read RTC
//         ds3231_get_time(&t);
//         printf("Time: %02d:%02d:%02d\n", t.hour, t.min, t.sec);

//         // Run automation
//         run_automation(&t, &dht);

//         // Update OLED
//         update_display(&t, &dht);

//         vTaskDelay(pdMS_TO_TICKS(1000));
//     }
// }