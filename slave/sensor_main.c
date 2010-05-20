/*********************************************
 * vim:sw=8:ts=8:si:et
 * To use the above modeline in vim you must have "set modeline" in your .vimrc
 * Author: Guido Socher
 * Copyright: GPL V2
 *
 * Tuxgraphics AVR webserver/ethernet board
 *
 * http://tuxgraphics.org/electronics/
 * Chip type           : Atmega88/168/328 with ENC28J60
 *********************************************/
#include <avr/io.h>
#include <avr/interrupt.h>

#include "sensor_main.h"
#include "modbus/modbus.h"

volatile uint16_t winddir = 0x0; // 0 = N, 4 = O, 8 = S, 12 = W

int main(void) {
	mb_init();
	
	mb_registerRegister(0,&winddir);
	
	PORTB &= ~_BV(PB1);
	DDRB |= _BV(PB1);
	
	PORTD |= _BV(PD7);
	DDRD |= _BV(PD7);
	
	ADMUX |= _BV(REFS0) | _BV(MUX0);
	ADCSRA = 0xFF;
	
	sei();

	while (1) {
		mb_handleRequest();
	}
}

ISR(ADC_vect) {
	winddir = ADC / 48;
}
