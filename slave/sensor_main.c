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

static volatile uint16_t winddir = 0x0; // in ° x10
static volatile uint16_t windspeed = 0x0; // in ms
static volatile uint16_t temperature = 0x0; // in °C

static uint16_t enable = 1;

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
		// TODO: calibration
		temperature = value;
	}
}

uint8_t write_enable(uint8_t num, uint16_t value) {
	if (num != 3)
		return 0;
	enable = value ? 1 : 0;
	// TODO: store in eeprom
	enableSensor();
	return 1;
}

void enableSensor(void) {
	// Set port for sensor on/off
	SENS_DDR |= _BV(SENS_PIN);
	
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
	// TODO: eeprom
	
	led_init();
		
	adc_init();
	adc_register(0,read_temperature);
	adc_register(1,read_winddir);
	
	windspeed_init(&windspeed);
	
	mb_init();
	mb_addReadRegister(0,(uint16_t*)&winddir);
	mb_addReadRegister(1,(uint16_t*)&windspeed);
	mb_addReadRegister(2,(uint16_t*)&temperature);
	mb_addReadRegister(3,&enable);
	mb_addWriteRegister(3,write_enable);
	
	sei();
	
	enableSensor();
	
	while (1) {
		mb_handleRequest();
		// Wenn enable = 0 dann messen, ob eh kein Bezug zu Masse im Sensor, weil + auf 6.6 V
	}
}

