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

#define _NENN		524288UL

#define	BLINK_FREQ	1
#define OVERFLOWS	(uint8_t)(10*F_CPU/(BLINK_FREQ*_NENN) + 5)/10

static volatile uint8_t overflows;

/*========================*/
/*     Interrupts         */
/*========================*/

/*
 * Interrupt Handler
*/
ISR(TIMER0_OVF_vect) {
	if (overflows == 0) {
		if (LED_PPIN & _BV(LED_PIN))
			LED_PORT &= ~_BV(LED_PIN);
		else
			LED_PORT |= _BV(LED_PIN);
	}
	++overflows;
	overflows %= OVERFLOWS;
}

/*========================*/
/*     Procedures         */
/*========================*/

void led_init(void) {
	led_off();
	LED_DDR |= _BV(LED_PIN);
	
	TIMER_CRA = 0;
	TIMSK |= _BV(TOIE0);
}

inline void led_on(void) {
	TIMER_CRB = 0;
	LED_PORT &= ~_BV(LED_PIN);
}

inline void led_off(void) {
	TIMER_CRB = 0;
	LED_PORT |= _BV(LED_PIN);
}

inline void led_blink(void) {
	overflows = 0;
	/* Pre Scaler 1024 = 12.207,03125 Hz = 81,92 µs */
	TIMER_CRB = 0x5;
}
