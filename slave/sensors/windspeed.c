#include <avr/io.h>
#include <avr/interrupt.h>

#define SPEED_PORT	PORTB
#define SPEED_DDR	DDRB
#define SPEED_PIN	PB0

#define TIMER_CRA	TCCR1A
#define TIMER_CRB	TCCR1B
#define TIMSK		TIMSK1

#define TIMER_PRESC	64.0f
#define TIMER_RANGE	65536

// calc with [unit]/10
#define COMMA_FAC	10.0f
// used to ensure values are in integer range
#define TIME_SCALE	(COMMA_FAC * 1000000)
// TIMER_T = 0,00000512s
#define TIMER_T		TIMER_PRESC / F_CPU
// PERIOD = 51 = 5,12us
#define PERIOD		(uint16_t)(TIME_SCALE * TIMER_T)
// OVL_PERIOD = 3.355.443 = 0,33554432s
#define OVL_PERIOD	(uint32_t)(TIMER_RANGE * TIME_SCALE * TIMER_T)
// WIND_SPEED = 39.525.691
#define WIND_SPEED	(uint32_t)(COMMA_FAC * TIME_SCALE * 100 / 253)

// TIME_MAXSP for 100m/s = 39.525
#define TIME_MAXSP	(WIND_SPEED / 1000)
// OVL_MIN for 1s = 3
#define OVL_MIN		(uint8_t)(1 + TIME_SCALE / OVL_PERIOD)

// pointer to result memories
static uint16_t volatile* windspeed;
static uint16_t volatile* errcnt;
static uint16_t volatile* cnt;

/// global overflow counter
static uint8_t volatile overflows;


/**
 * Interrupt handler for timer overflow
**/
ISR(TIMER1_OVF_vect) {
	overflows++;
}

/**
 * Interrupt handler for input capture
**/
ISR(TIMER1_CAPT_vect) {
	static uint16_t old_start = 0;
	uint32_t time = 0;
	uint16_t starttime;	
	int32_t diff;
	uint8_t ov = overflows;
	
	overflows = 0; // reset overflows for next run
	
	starttime = ICR1;
	diff = starttime-old_start;
	old_start = starttime;
	
	if (ov > OVL_MIN) {
		// too long => no speed
		time = 0;
	} else {
		// if overflow make diff positiv
		if (ov) {
			ov--;
			diff += TIMER_RANGE;
		}
		time = OVL_PERIOD*ov + diff*PERIOD;
	}
	
	if (time) {
		if (time > TIME_MAXSP)
			*windspeed = WIND_SPEED / time;
		else
			(*errcnt)++; // count invalid messurements
	} else
		*windspeed = 0;
	
	(*cnt)++; // count messurements
}

/**
 * Initialize windspeed unit
 *
 * @param	_windspeed	Pointer to windspeed value storage
 * @param	_errcnt		Pointer to messurement error counter storage
 * @param	_cnt		Pointer to messurement counter storage
**/
void windspeed_init(uint16_t volatile* _windspeed,uint16_t volatile* _errcnt,uint16_t volatile* _cnt) {
	windspeed = _windspeed;
	errcnt = _errcnt;
	cnt = _cnt;
	
	// disable pullup (externally provided)
	SPEED_PORT &= ~_BV(SPEED_PIN);
	SPEED_DDR &= ~_BV(SPEED_PIN);

	TIMER_CRB &= ~_BV(ICNC1);
	TIMSK |= _BV(TOIE1) | _BV(ICIE1);
}

/**
 * Start windspeed messurement
**/
void windspeed_start(void) {
	overflows = 0;
	/* Pre Scaler 64 = 195.312,5 Hz = 5,12 µs */
	TIMER_CRB |= _BV(CS11) | _BV(CS10);
}

/**
 * Stop windspeed messurement
**/
void windspeed_stop(void) {
	TIMER_CRB &= ~0x07;
	*windspeed = 0xFFFF; // write value for "no results"
}
