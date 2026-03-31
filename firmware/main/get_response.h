#ifndef GET_RESPONSE_H
#define GET_RESPONSE_H
#include "driver/i2s_std.h"
    extern volatile bool is_speaker_playing;
    void get_audio(i2s_chan_handle_t speaker_handle);
    void get_answer(i2s_chan_handle_t speaker_handle, char* mac_str);
    esp_err_t _post_audio_event_handler(esp_http_client_event_t *evt);
#endif