#include <avr/io.h>
#include <avr/interrupt.h>

#define DIR_PORT	PORTD
#define DIR_DDR		DDRD
#define DIR_PIN		PD7

/*========================*/
/*     Interrupts         */
/*========================*/

/*
 * Interrupt Handler
*/
ISR(ADC_vect) {
	
}

/*========================*/
/*     Procedures         */
/*========================*/

void winddir_init(const volatile uint16_t* mem) {
	winddir = mem;
	
	DIR_PORT |= _BV(DIR_PIN);
	DIR_DDR |= _BV(DIR_PIN);
	
	ADMUX |= _BV(REFS0) | _BV(MUX0);
	ADCSRA = 0xFF;
}

