/*========================================================*/
/*     Author: Markus Klein                               */
/*       Date: 09.04.2010                                 */
/*                                                        */
/*     Revisions:                                         */
/*       Date: 12.12.2012 - Additional temperature sensor */
/*========================================================*/

#include <string.h>
#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/eeprom.h>

#include "modbus/modbus.h"
#include "sensors/adc.h"
#include "sensors/led.h"
#include "sensors/windspeed.h"

#include "sensor_main.h"

#define SENS_PORT	PORTD
#define SENS_DDR	DDRD
#define SENS_PIN	PD7

#define IP_HIGH		0
#define IP_LOW		1
#define ENABLENUM	2
#define TEMPNUM		3

// registers for Modbus communication
static volatile uint16_t winddir; // in ° x10
static volatile uint16_t errcnt; // in ° x10
static volatile uint16_t cnt; // in ° x10
static volatile uint16_t windspeed; // in m/s x10
static volatile int16_t temperature; // in °C x10
static volatile int16_t insideTemperature; // in °C x10

// persistent variables, located in EEPROM
static configuration_t EEMEM eeConfig = { DEFAULT_ENABLE, DEFAULT_IP, {DEFAULT_TEMP_K, DEFAULT_TEMP_D, DEFAULT_TEMP_DIV} };

// local variables
static const configuration_t defaultConfig = { DEFAULT_ENABLE, DEFAULT_IP, {DEFAULT_TEMP_K, DEFAULT_TEMP_D, DEFAULT_TEMP_DIV} };
static configuration_t config;
static volatile uint8_t renewIP = 0;

/**
 * Callback for winddirection
 *
 * @param	value	Data from ADC
**/
void read_winddir(uint16_t value) {
	// if error occured
	if (value == 0x7fff)
		winddir = value;
	else {
		// ADC in [0..720] / 48 = [0..15] = 16 directions = 22,5° per direction
		winddir = (value / 48) * 225;
	}
}

/**
 * Callback for temperature
 *
 * @param	value	Data from ADC
**/
void read_temperature(uint16_t value) {
	// if error occured
	if (value == 0x7fff)
		temperature = value;
	else {
		// 485 = 14°C
		// 388 = 25°C
		temperature = (config.temperatureCalibration[0] * value + config.temperatureCalibration[1]) / config.temperatureCalibration[2];
	}
}

/**
 * Callback for Modbus write register function
 * for enabling/disabling external power supply to sensor
 *
 * @param	num		Must be equal to ENABLENUM to accept new value
 * @param	value	New enable value
 * @return			True if value was written successfully
**/
uint8_t write_enable(uint8_t num, uint16_t value) {
	if (num != ENABLENUM)
		return 0;

	config.enable = value ? 1 : 0;
	eeprom_update_word(&eeConfig.enable, config.enable);
	setSensorStatus();
	return 1;
}

/**
 * Callback for Modbus write register function
 * for setting a new IP address.
 *
 * High-Byte of value is always the left most IP segment!
 * How to set a new IP:
 * 1) Write higher 2 segments at num IP_HIGH.
 * 2) Write lower 2 segments at num IP_LOW.
 *    New IP is stored in EEPROM.
 *    New IP address will be activated after Modbus transmission has
 *    been confirmed with old IP address.
 *
 * Example:
 *  write_IP(IP_HIGH, 0xC0A8); // 192.168
 *  write_IP(IP_LOW, 0x00A8); // 0.111
 *
 * @param	num		Register number
 * @param	value	Two 8-bit values for IPv4 segments
 * @return			True if value was written successfully
**/
uint8_t write_IP(uint8_t num, uint16_t value) {
	switch (num) {
	case IP_HIGH:
		config.ip[0] = (ARRAY_TYPE(config.ip))(value >> 8);
		config.ip[1] = (ARRAY_TYPE(config.ip))value;
		break;
	case IP_LOW:
		config.ip[2] = (ARRAY_TYPE(config.ip))(value >> 8);
		config.ip[3] = (ARRAY_TYPE(config.ip))value;
		eeprom_update_block(config.ip, eeConfig.ip, sizeof(config.ip));
		renewIP = 1;
		break;
	default:
		return 0;
	}
	return 1;
}

/**
 * Callback for Modbus write register function
 *
 * How to set new temperature calibration parameters:
 * - Write 32bit values for k,d and div
 * - Addresses:
 *     - k: TEMPNUM and TEMPNUM+1
 *     - d: TEMPNUM+2 and TEMPNUM+3
 *     - div: TEMPNUM+4 and TEMPNUM+5
 * - Write higher word first.
 * - New parameters are stored in EEPROM.
 *
 * @param	num		Register number
 * @param	value	value
 * @return			True if value was written successfully
**/
uint8_t write_temperatureCalibration(uint8_t num, uint16_t value) {
	static ARRAY_TYPE(config.temperatureCalibration) tmp[ARRAY_SIZE(config.temperatureCalibration)];
	if (num < TEMPNUM || num >= TEMPNUM+2*ARRAY_SIZE(config.temperatureCalibration))
		return 0;

	num -= TEMPNUM;
	if (num % 2)
		tmp[num-1] |= value;
	else
		tmp[num] = (uint32_t)value << 16;
	if (num == 2*ARRAY_SIZE(config.temperatureCalibration)-1) {
		eeprom_update_block(tmp, eeConfig.temperatureCalibration, sizeof(tmp));
		memcpy(config.temperatureCalibration, tmp, sizeof(tmp));
	}
	return 1;
}

/**
 * Enable/disable external power supply and start measurements
**/
void setSensorStatus(void) {
	if (config.enable) {
		SENS_PORT |= _BV(SENS_PIN);
		adc_start();
		windspeed_start();
		led_on();
	} else {
		SENS_PORT &= ~_BV(SENS_PIN);
		adc_stop();
		windspeed_stop();
		led_blink();
	}
}

/**
 * Load values from EEPROM. Apply default values, if data is not valid.
**/
void loadEepromValues(void) {
	// load values from eeprom
	eeprom_read_block(&config, &eeConfig, sizeof(config));

	// check if EEPROM has been erased and apply default values
	if (config.enable == 0xFFFF) {
		memcpy(&config, &defaultConfig, sizeof(config));
		eeprom_update_block(&config, &eeConfig, sizeof(eeConfig));
	}
}

/**
 * Main function
 * Initialize controller and handle Modbus requests
**/
int main(void) {
	uint8_t i;

	// load config data from eeprom
	loadEepromValues();

	// init modules
	led_init();
	adc_init();
	windspeed_init(&windspeed, &errcnt, &cnt);
	mb_init();
	mb_setIP(config.ip);

	// register adc handlers
	adc_register(0, read_temperature);
	adc_register(1, read_winddir);

	// register Modbus handlers and registers
	mb_addReadRegister(ENABLENUM, &(config.enable));
	mb_addReadRegister(3, (uint16_t*)&winddir);
	mb_addReadRegister(4, (uint16_t*)&windspeed);
	mb_addReadRegister(5, (uint16_t*)&temperature);
	mb_addReadRegister(6, (uint16_t*)&errcnt);
	mb_addReadRegister(7, (uint16_t*)&cnt);
	mb_addReadRegister(8, (uint16_t*)&insideTemperature);

	mb_addWriteRegister(IP_HIGH, write_IP);
	mb_addWriteRegister(IP_LOW, write_IP);
	mb_addWriteRegister(ENABLENUM, write_enable);
	for (i = TEMPNUM; i < TEMPNUM+2*ARRAY_SIZE(config.temperatureCalibration); i++)
		mb_addWriteRegister(i, write_temperatureCalibration);

	// set DDR for sensor on/off
	SENS_DDR |= _BV(SENS_PIN);

	// enable interrupts
	sei();

	// start proccessing
	setSensorStatus();

	while (1) {
		// apply new IP address if requested
		if (renewIP) {
			renewIP = 0;
			mb_setIP(config.ip);
		}
		mb_handleRequest();
	}
}

