#ifndef _sensor_main_h_
#define _sensor_main_h_

// Device enabled
#define DEFAULT_ENABLE 1
// IP: 192.168.0.111
#define DEFAULT_IP 0xC0A8006F
#define DEFAULT_IP_ARR {192,168,0,111}

void read_winddir(uint16_t value);
void read_temperature(uint16_t value);
uint8_t write_enable(uint8_t num, uint16_t value);
void enableSensor(void);
int main(void);

#endif
