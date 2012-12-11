/** \file sensor_main.h \brief Default parameter values. */
/*========================================================*/
/*     Author: Markus Klein                               */
/*       Date: 09.04.2010                                 */
/*========================================================*/

#ifndef _sensor_main_h_
#define _sensor_main_h_

// Device enabled
#define DEFAULT_ENABLE 1
// IP: 192.168.0.111
#define DEFAULT_IP 0xC0A8006F
#define DEFAULT_IP_ARR {192,168,123,111}
// Temperature scaling factors
#define DEFAULT_TEMP_K (-1134UL)
#define DEFAULT_TEMP_D (690000UL)
#define DEFAULT_TEMP_DIV 1000

void read_winddir(uint16_t value);
void read_temperature(uint16_t value);
uint8_t write_enable(uint8_t num, uint16_t value);
uint8_t write_IP(uint8_t num, uint16_t value);
uint8_t write_temp(uint8_t num, uint16_t value);
void enableSensor(void);
void loadeepromValues(void);
int main(void);

#endif
