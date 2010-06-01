#include <avr/io.h>
#include <avr/interrupt.h>
#include <stdlib.h>

#include "adc.h"

#define CHANNELS_SIZE 4
static callback_t channels[CHANNELS_SIZE] = {NULL};
static uint8_t volatile curind = 0;

/*========================*/
/*     Interrupts         */
/*========================*/

/*
 * Interrupt Handler
*/
ISR(ADC_vect) {
	static uint16_t values[256] = {0};
	static uint8_t i = 0;
	
	values[i] = ADC;
	if (i == 255) {
		uint8_t i2 = 0;
		uint32_t sum = 0;
		
		for (; 1; i2++) {
			sum += values[i2];
			if (i2 == 255)
				break;
		}
		// callback
		channels[curind](sum/256);
		// select next channel
		do {
			++curind;
			curind %= CHANNELS_SIZE;
		} while (channels[curind] == NULL);
		ADMUX = (ADMUX & 0xF0) | curind;
	}
	i++;
	// start conversion
	ADCSRA |= _BV(ADSC);
}

/*========================*/
/*     Procedures         */
/*========================*/

void adc_init(void) {
	ADMUX |= _BV(REFS0);
	ADCSRA = 0x0F;
}

void adc_register(uint8_t chnum,callback_t cb) {
	if (chnum >= CHANNELS_SIZE)
		return;
	channels[chnum] = cb;
}

void adc_start(void) {
	curind = 0;
	ADMUX = (ADMUX & 0xF0) | curind;
	ADCSRA |= _BV(ADEN) | _BV(ADSC);
}

void adc_stop(void) {
	ADCSRA &= ~_BV(ADEN);
	uint8_t i = 0;
	for (; i < CHANNELS_SIZE; i++)
		if (channels[i] != NULL)
			channels[i](0xFFFF);
}
