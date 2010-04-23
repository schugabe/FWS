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

#include "modbus/modbus.h"
#include "sensor_main.h"

uint16_t windspeed = 0x4114;

int main(void) {
	mb_init();
	
	mb_registerRegister(0,&windspeed);
        
        PORTB &= ~_BV(PB1);
        DDRB |= _BV(PB1);
        
        TIMSK0 |= _BV(TOIE0);
        TCCR0A = 0x00;
        TCCR0B = 0x05; // 1024 pres, normal mode
        
        sei();

        while (1) {
               mb_handleRequest();
        }
}

ISR(TIMER0_OVF_vect) {
	if (PORTB & _BV(PB1))
		PORTB &= ~_BV(PB1);
	else
		PORTB |= _BV(PB1);
}
