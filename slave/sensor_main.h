#ifndef _sensor_main_h_
#define _sensor_main_h_

void read_winddir(uint16_t value);
void read_temperature(uint16_t value);
uint8_t write_enable(uint8_t num, uint16_t value);
void enableSensor(void);
int main(void);

#endif
