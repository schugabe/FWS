/** \file sensor_main.h \brief Default parameter values.  */
/*========================================================*/
/*     Author: Markus Klein                               */
/*       Date: 09.04.2010                                 */
/*                                                        */
/*     Revisions:                                         */
/*       Date: 12.12.2012 - Additional temperature sensor */
/*========================================================*/

#ifndef _sensor_main_h_
#define _sensor_main_h_

// Device enabled
#define DEFAULT_ENABLE	1

// IP
#define DEFAULT_IP	{192,168,123,111}

// Temperature scaling factors
#define DEFAULT_TEMP_K		(-1134L)
#define DEFAULT_TEMP_D		(690000UL)
#define DEFAULT_TEMP_DIV	(1000)

#define ARRAY_SIZE(arr)	(sizeof(arr) / sizeof(arr[0]))
#define ARRAY_TYPE(arr) typeof(arr[0])

typedef struct {
	uint16_t enable;
	uint8_t ip[4];
	int32_t temperatureCalibration[3];
} configuration_t;

void read_winddir(uint16_t value);
void read_temperature(uint16_t value);
uint8_t write_enable(uint8_t num, uint16_t value);
uint8_t write_IP(uint8_t num, uint16_t value);
uint8_t write_temperatureCalibration(uint8_t num, uint16_t value);
void setSensorStatus(void);
void loadEepromValues(void);
int main(void);

#endif

