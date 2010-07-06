/*========================================================*/
/*     Author: Markus Klein                               */
/*       Date: 09.04.2010                                 */
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

#define ENABLENUM	2
#define TEMPNUM		3

// Registers for Modbus communication
static volatile uint16_t winddir; // in ° x10
static volatile uint16_t errcnt; // in ° x10
static volatile uint16_t cnt; // in ° x10
static volatile uint16_t windspeed; // in m/s x10
static volatile int16_t temperature; // in °C x10

// persistent variables, located in EEPROM
static uint16_t eeEnable EEMEM = DEFAULT_ENABLE;
static uint8_t eeIp[] EEMEM = DEFAULT_IP_ARR;
static int32_t eeTemp[] EEMEM = {DEFAULT_TEMP_K,DEFAULT_TEMP_D,DEFAULT_TEMP_DIV};

static uint8_t newIp;
static typeof(eeEnable) enable;
static typeof(eeIp[0]) ip[sizeof(eeIp)/sizeof(eeIp[0])];
static typeof(eeTemp[0]) temp[sizeof(eeTemp)/sizeof(eeTemp[0])];

/**
 * callback for winddirection
 *
 * @param	value	Data from ADC
**/
void read_winddir(uint16_t value) {
	// if error occured
	if (value == 0xffff)
		winddir = value;
	else {
		// ADC in [0..720] / 48 = [0..15] = 16 directions = 22,5° per direction
		winddir = (value / 48) * 225;
	}
}

/**
 * callback for temperature
 *
 * @param	value	Data from ADC
**/
void read_temperature(uint16_t value) {
	// if error occured
	if (value == 0xffff)
		temperature = value;
	else {
		// 485 = 14°C
		// 388 = 25°C
		temperature = (temp[0] * value + temp[1]) / temp[2];
	}
}

/**
 * callback for Modbus write register function
 *
 * @param	num	Must be equal to ENABLENUM to accept new value
 * @param	value	New enable value
 * @return		True if value was written successfully
**/
uint8_t write_enable(uint8_t num, uint16_t value) {
	if (num != ENABLENUM)
		return 0;
	enable = value ? 1 : 0;
	eeprom_write_word(&eeEnable, enable);
	enableSensor();
	return 1;
}

/**
 * Callback for Modbus write register function
 *
 * High-Byte of value is always the left most IP segment!
 * How to set a new IP:
 * 1) Write higher 2 segments at num 0.
 * 2) Write lower 2 segments at num 1.
 *    New IP is stored in EEPROM.
 *    New IP address will be activated after Modbux transmission has
 *    been confirmed with old IP address.
 *
 * @param	num		Register number
 * @param	value	Two 8-bit values for IPv4 segments
 * @return			True if value was written successfully
**/
uint8_t write_IP(uint8_t num, uint16_t value) {
	if (num == 0) {
		ip[0] = (typeof(ip[0]))(value >> 8);
		ip[1] = (typeof(ip[0]))value;
	} else if (num == 1) {
		ip[2] = (typeof(ip[0]))(value >> 8);
		ip[3] = (typeof(ip[0]))value;
		eeprom_write_block(ip,eeIp,sizeof(eeIp));
		newIp = 1;
	} else
		return 0;
	return 1;
}

/**
 * Callback for Modbus write register function
 *
 * How to set new temperature parameters:
 * - Write 32bit values k,d and div beginning with num TEMPNUM.
 * - Write higher word first.
 * - New parameters are stored in EEPROM.
 *
 * @param	num	Register number
 * @param	value	value
 * @return		True if value was written successfully
**/
uint8_t write_temp(uint8_t num, uint16_t value) {
	static typeof(temp[0]) tmp[sizeof(temp)/sizeof(temp[0])];
	if (num < TEMPNUM || num > TEMPNUM+sizeof(tmp)/sizeof(uint16_t)-1)
		return 0;
	num -= TEMPNUM;
	if (num % 2)
		tmp[num-1] |= value;
	else
		tmp[num] = (uint32_t)value << 16;
	if (num == sizeof(tmp)/sizeof(uint16_t)) {
		eeprom_write_block(tmp,eeTemp,sizeof(eeTemp));
		memcpy(temp,tmp,sizeof(tmp));
	}
	return 1;
}

/**
 * Enable/disable external power supply and start measurements
**/
void enableSensor(void) {
	if (enable) {
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
void loadeepromValues(void) {
	// load values from eeprom
	eeprom_read_block(ip,eeIp,sizeof(eeIp));
	enable = eeprom_read_word(&eeEnable);
	eeprom_read_block(temp,eeTemp,sizeof(eeTemp));
	// check if eeprom has been erased and apply default values if so
	if (ip[0] == 0xFF)
		*((uint32_t*)ip) = DEFAULT_IP;
	if (enable == 0xFFFF)
		enable = DEFAULT_ENABLE;
	if (temp[0] == (int32_t)0xFFFFFFFF) {
		temp[0] = DEFAULT_TEMP_K;
		temp[1] = DEFAULT_TEMP_D;
		temp[2] = DEFAULT_TEMP_DIV;
	}
}

/**
 * Main function
 * Initialize controller and handle Modbus requests
**/
int main(void) {
	uint8_t i;
	
	// load config data from eeprom
	loadeepromValues();
	
	// init modules
	led_init();
	adc_init();
	windspeed_init(&windspeed,&errcnt,&cnt);
	mb_init();
	
	// force init with current IP
	newIp = 1;
		
	// register adc handlers
	adc_register(0,read_temperature);
	adc_register(1,read_winddir);
	
	// register Modbus handlers and registers
	mb_addReadRegister(ENABLENUM,&enable);
	mb_addReadRegister(3,(uint16_t*)&winddir);
	mb_addReadRegister(4,(uint16_t*)&windspeed);
	mb_addReadRegister(5,(uint16_t*)&temperature);
	mb_addReadRegister(6,(uint16_t*)&errcnt);
	mb_addReadRegister(7,(uint16_t*)&cnt);
	
	mb_addWriteRegister(0,write_IP);
	mb_addWriteRegister(1,write_IP);
	mb_addWriteRegister(ENABLENUM,write_enable);
	for (i = TEMPNUM; i < TEMPNUM+sizeof(eeTemp)/sizeof(uint16_t); i++)
		mb_addWriteRegister(i,write_temp);
	
	// set ddr for sensor on/off
	SENS_DDR |= _BV(SENS_PIN);

	// enable interrupts
	sei();
	
	// start proccessing
	enableSensor();
	
	while (1) {
		// apply new Ip address if requested
		if (newIp) {
			newIp = 0;
			mb_setIP(ip);
		}
		mb_handleRequest();
	}
}

