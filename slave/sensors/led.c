/*========================================================*/
/*     Author: Markus Klein                               */
/*       Date: 29.05.2010                                 */
/*========================================================*/

#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>

#include "led.h"

#define LED_PORT	PORTB
#define LED_PPIN	PINB
#define LED_DDR		DDRB
#define LED_PIN		PB1

#define TIMER_CRA	TCCR0A
#define TIMER_CRB	TCCR0B
#define TIMSK		TIMSK0

#define TIMER_PRESC	1024UL
#define TIMER_RANGE	256
// DENOM = 524.288
#define DENOM		(BLINK_FREQ * 2 * TIMER_RANGE * TIMER_PRESC)
// OVERFLOWS rounded = 24
#define OVERFLOWS	(uint8_t)((10*F_CPU/DENOM + 5)/10)

/// global overflow counter
static volatile uint8_t overflows;

/**
 * Interrupt handler for timer overflow
*/
ISR(TIMER0_OVF_vect) {
	// toggle LED
	if (overflows == 0) {
		if (LED_PPIN & _BV(LED_PIN))
			LED_PORT &= ~_BV(LED_PIN);
		else
			LED_PORT |= _BV(LED_PIN);
	}
	++overflows;
	overflows %= OVERFLOWS;
}

/**
 * Initialize LED
**/
void led_init(void) {
	led_off();
	LED_DDR |= _BV(LED_PIN);
	
	TIMER_CRA = 0;
	TIMSK |= _BV(TOIE0);
}

/**
 * Switch on LED permanently
**/
inline void led_on(void) {
	TIMER_CRB = 0;
	LED_PORT &= ~_BV(LED_PIN);
}

/**
 * Switch off LED permanently
**/
inline void led_off(void) {
	TIMER_CRB = 0;
	LED_PORT |= _BV(LED_PIN);
}

/**
 * Blink LED
**/
inline void led_blink(void) {
	overflows = 0;
	/* Pre Scaler 1024 = 12.207,03125 Hz = 81,92 µs */
	TIMER_CRB = 0x5;
}
