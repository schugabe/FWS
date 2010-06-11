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

// Registers for Modbus communication
static volatile uint16_t winddir; // in ° x10
static volatile uint16_t errcnt; // in ° x10
static volatile uint16_t cnt; // in ° x10
static volatile uint16_t windspeed; // in m/s x10
static volatile uint16_t temperature; // in °C x10

// persistent variables, located in EEPROM
static uint16_t eeEnable EEMEM = DEFAULT_ENABLE;
static uint8_t eeIp[] EEMEM = DEFAULT_IP_ARR;

static uint8_t newIp;
static uint16_t enable;
static uint8_t ip[4];

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
		temperature = (690000-((uint32_t)value*1134UL))/1000;
	}
}

/**
 * callback for Modbus write register function
 *
 * @param	num		Must be equal to ENABLENUM to accept new value
 * @param	value	New enable value
 * @return			True if value was written successfully
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
		ip[0] = (uint8_t)(value >> 8);
		ip[1] = (uint8_t)value;
	} else {
		ip[2] = (uint8_t)(value >> 8);
		ip[3] = (uint8_t)value;
		eeprom_write_block(ip,eeIp,sizeof(eeIp));
		newIp = 1;
	}
	return 1;
}

/**
 * Enable/disable external power supply and start messurements
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
 * Main function
 * Initialize controller and handle Modbus requests
**/
int main(void) {
	// load values from eeprom
	eeprom_read_block(ip,eeIp,sizeof(eeIp));
	enable = eeprom_read_word(&eeEnable);
	// check if eeprom has been erased and apply default values if so
	if (ip[0] == 0xFF)
		*((uint32_t*)ip) = DEFAULT_IP;
	if (enable == 0xFFFF)
		enable = DEFAULT_ENABLE;
	
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

