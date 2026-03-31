#ifndef DEVICES_H
#define DEVICES_H
    extern volatile bool fan_state;
    extern bool light_state;
    void set_fan(bool on);
    void set_light(bool on);
#endif
