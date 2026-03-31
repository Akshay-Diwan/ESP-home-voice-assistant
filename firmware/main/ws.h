#ifndef WS_H
#define WS_H
    void websocket_app_start(void);
    void stream_mic_to_ws();
    void audio_stream_task(void *pvParameters);
#endif