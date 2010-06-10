#include <avr/io.h>
#include <avr/interrupt.h>

#include "led.h"

#define SPEED_PORT	PORTB
#define SPEED_DDR	DDRB
#define SPEED_PIN	PB0

#define TIMER_CRA	TCCR1A
#define TIMER_CRB	TCCR1B
#define TIMSK		TIMSK1

#define TIMER_PRESC	1024UL
#define TIMER_RANGE	65536UL
#define SCALE_FAC	1000
// calc with milliseconds/10
#define COMMA_FAC	10.0f
// TIMER_T = 0,8192s
#define TIMER_T		COMMA_FAC * SCALE_FAC * TIMER_PRESC / F_CPU
// OVL_PERIOD = 53.687 = 5.3s
#define OVL_PERIOD	(uint16_t)(TIMER_RANGE * TIMER_T)
// PERIOD = 819 = 81,9us
#define PERIOD		(uint16_t)(SCALE_FAC * TIMER_T)
// WIND_SPEED = 39520
#define WIND_SPEED	(uint16_t)(COMMA_FAC * SCALE_FAC * 1000 / 253)
// MIN_TIME = 39
#define MIN_TIME	(COMMA_FAC * SCALE_FAC / 253)

static uint16_t volatile* windspeed;
static uint16_t volatile* errcnt;
static uint16_t volatile* cnt;
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
	uint32_t time = 0;
	uint16_t starttime;	
	int32_t diff;
	uint8_t ov = overflows;
	
	overflows = 0;
	starttime = ICR1;

	diff = starttime-old_start;
	if (ov > 1) {
		// too long => no speed
		time = 0;
	} else {
		// if overflow make diff positiv
		if (ov) {
			diff += TIMER_RANGE;
		}
		time = (diff*PERIOD) / SCALE_FAC;
	}
	if (time) {
		if (time > MIN_TIME)
			*windspeed = WIND_SPEED / time;
		else
			(*errcnt)++;
		(*cnt)++;
	} else
		*windspeed = 0;
	
	old_start = starttime;
}


/*========================*/
/*     Procedures         */
/*========================*/

void windspeed_init(uint16_t volatile* mem,uint16_t volatile* err,uint16_t volatile* scnt) {
	windspeed = mem;
	errcnt = err;
	cnt = scnt;
	
	// disable pullup (externally provided)
	SPEED_PORT &= ~_BV(SPEED_PIN);
	SPEED_DDR &= ~_BV(SPEED_PIN);

	TIMER_CRB &= ~_BV(ICNC1);
	TIMSK |= _BV(TOIE1) | _BV(ICIE1);
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
