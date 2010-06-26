#include <avr/io.h>
#include <avr/interrupt.h>
#include <stdlib.h>

#include "adc.h"

#define CHANNELS_SIZE 4
static callback_t channels[CHANNELS_SIZE] = {NULL};

#define AVG_SIZE	256

/// currently selected channel
static uint8_t volatile curind = 0;

/**
 * Interrupt handler for ADC completed
**/
ISR(ADC_vect) {
	static uint16_t values[AVG_SIZE] = {0}; // store 256 values
	static uint8_t i = 0; // index for next value
	
	values[i] = ADC;
	// if we have the last one
	if (i == AVG_SIZE-1) {
		uint8_t i2 = 0;
		uint32_t sum = 0;
		
		// sum up all entries
		for (; 1; i2++) {
			sum += values[i2];
			if (i2 == AVG_SIZE-1)
				break;
		}
		// callback with average measurement
		channels[curind](sum/AVG_SIZE);
		// select next active channel
		do {
			++curind;
			curind %= CHANNELS_SIZE;
		} while (channels[curind] == NULL);
		ADMUX = (ADMUX & 0xF0) | curind;
	}
	i++;
	// start next conversion
	ADCSRA |= _BV(ADSC);
}

/**
 * Initialize ADC unit
**/
void adc_init(void) {
	ADMUX |= _BV(REFS0);
	ADCSRA = 0x0F;
}

/**
 * Register a new ADC channel
 *
 * @param	chnum	Channel number to use
 * @param	cb	Result ready callback for this channel. Set to NULL to disable channel.
**/
void adc_register(uint8_t chnum,callback_t cb) {
	if (chnum >= CHANNELS_SIZE)
		return;
	channels[chnum] = cb;
}

/**
 * Start ADC conversions
**/
void adc_start(void) {
	curind = 0;
	ADMUX = (ADMUX & 0xF0) | curind;
	ADCSRA |= _BV(ADEN) | _BV(ADSC);
}

/**
 * Stop ADC conversions
**/
void adc_stop(void) {
	ADCSRA &= ~_BV(ADEN);
	uint8_t i = 0;
	for (; i < CHANNELS_SIZE; i++)
		if (channels[i] != NULL)
			channels[i](0xFFFF); // callbacks with value for "no results"
}
