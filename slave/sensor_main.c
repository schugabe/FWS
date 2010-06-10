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

static volatile uint16_t winddir; // in ° x10
static volatile uint16_t errcnt; // in ° x10
static volatile uint16_t cnt; // in ° x10
static volatile uint16_t windspeed; // in ms
static volatile uint16_t temperature; // in °C

static uint16_t eeEnable EEMEM = DEFAULT_ENABLE;
static uint8_t eeIp[] EEMEM = DEFAULT_IP_ARR;

static uint8_t newIp;
static uint16_t enable;
static uint8_t ip[4];

void read_winddir(uint16_t value) {
	// if error occured
	if (value == 0xffff)
		winddir = value;
	else {
		// ADC in [0..720]
		// ADC / 48 in [0..15] = 16 directions = 22,5° per direction
		winddir = (value / 48) * 225;
	}
}

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

uint8_t write_enable(uint8_t num, uint16_t value) {
	if (num != ENABLENUM)
		return 0;
	enable = value ? 1 : 0;
	eeprom_write_word(&eeEnable, enable);
	enableSensor();
	return 1;
}

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

void enableSensor(void) {
	// Wenn enable = 0 dann messen, ob eh kein Bezug zu Masse im Sensor, weil + auf 6.6 V
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

int main(void) {
	// load values from eeprom
	eeprom_read_block(ip,eeIp,sizeof(eeIp));
	enable = eeprom_read_word(&eeEnable);
	if (ip[0] == 0xFF)
		*((uint32_t*)ip) = DEFAULT_IP;
	if (enable == 0xFFFF)
		enable = DEFAULT_ENABLE;
	
	led_init();
		
	adc_init();
	adc_register(0,read_temperature);
	adc_register(1,read_winddir);
	
	windspeed_init(&windspeed,&errcnt,&cnt);
	
	mb_init();
	mb_setIP(ip);
	
	mb_addReadRegister(ENABLENUM,&enable);
	mb_addReadRegister(3,(uint16_t*)&winddir);
	mb_addReadRegister(4,(uint16_t*)&windspeed);
	mb_addReadRegister(5,(uint16_t*)&temperature);
	mb_addReadRegister(6,(uint16_t*)&errcnt);
	mb_addReadRegister(7,(uint16_t*)&cnt);
	
	mb_addWriteRegister(0,write_IP);
	mb_addWriteRegister(1,write_IP);
	mb_addWriteRegister(ENABLENUM,write_enable);
	
	sei();
	
	// Set port for sensor on/off
	SENS_DDR |= _BV(SENS_PIN);
	
	enableSensor();
	
	while (1) {
		mb_handleRequest();
		if (newIp) {
			newIp = 0;
			mb_setIP(ip);
		}
	}
}

