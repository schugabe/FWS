#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>

#include "windspeed.h"

#define SPEED_PORT	PORTB
#define SPEED_DDR	DDRB
#define SPEED_PIN	PB0

#define TIMER_CRA	TCCR1A	
#define TIMER_CRB	TCCR1B
#define TIMSK		TIMSK1

#define PRESC		1024
#define COUNT_RANGE	65536L
#define OVERFLOW_PERIOD_MS (uint16_t)((10000.0f * COUNT_RANGE * PRESC) / F_CPU)
#define PERIOD_MS 	(uint16_t)(10000000.0f * PRESC / F_CPU)

static uint16_t volatile* windspeed;
static uint8_t volatile overflows;

/*========================*/
/*     Interrupts         */
/*========================*/

/*
 * Interrupt Handler
*/
ISR(TIMER1_OVF_vect) {
	overflows++;
}

/*
 * Interrupt Handler
*/
ISR(TIMER1_CAPT_vect) {
	static uint16_t old_start = 0;
	uint8_t time_difference;
	uint8_t time_overflow;
	uint16_t starttime;	
	int16_t diff;
	uint8_t ov = overflows;
	overflows = 0;

	starttime = ICR1;

	diff = starttime-old_start;
	if(ov) {
		ov--;
		time_overflow = ov*OVERFLOW_PERIOD_MS;
		time_difference = (COUNT_RANGE+diff)*PERIOD_MS / 1000L;
	} else {
		time_overflow = 0;
		time_difference = diff*PERIOD_MS / 1000L;
	}
	*windspeed = time_overflow+time_difference;
	
	old_start = starttime;
}


/*========================*/
/*     Procedures         */
/*========================*/

void windspeed_init(uint16_t volatile* mem) {
	windspeed = mem;
	
	// disable pullup (externally provided)
	SPEED_PORT &= ~_BV(SPEED_PIN);
	SPEED_DDR &= ~_BV(SPEED_PIN);

	TIMER_CRB &= ~_BV(ICNC1);
	TIMSK |= _BV(TOIE1);
}

void windspeed_start(void) {
	overflows = 0;
	/* Pre Scaler 1024 = 12.207,03125 Hz = 81,92 µs */ 
	TIMER_CRB |= _BV(CS12) | _BV(CS10);
}

void windspeed_stop(void) {
	TIMER_CRB &= ~0x07;
	*windspeed = 0xFFFF;
}
