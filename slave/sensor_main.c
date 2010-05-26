#include <avr/io.h>
#include <avr/interrupt.h>

#include "sensor_main.h"
#include "modbus/modbus.h"
#include "sensors/winddir.h"
#include "sensors/windspeed.h"

static volatile uint16_t winddir = 0x0; // in ° x10
static volatile uint16_t temperature = 0x0; // in °C
static volatile uint16_t windspeed = 0x0; // in ms

void winddir(void) {
	winddir = ADC / 48;
}

void temperature(void) {
	temperature = ADC;
}

int main(void) {
	adc_init(&winddir);
	windspeed_init(&windspeed);
	
	mb_init();
	mb_registerRegister(0,(uint16_t*)&winddir);
	mb_registerRegister(1,(uint16_t*)&windspeed);
		
	// Activate LED
	PORTB &= ~_BV(PB1);
	DDRB |= _BV(PB1);
	
	sei();

	while (1) {
		mb_handleRequest();
	}
}
