/*========================================================*/
/*     Author: Markus Klein                               */
/*       Date: 28.03.2013                                 */
/*========================================================*/


#include <string.h>
#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>

#include "ds18s20/ds18x20.h"
#include "ds18s20/onewire.h"

#define MAXSENSORS 1

// Sensors
static uint8_t gSensorIDs[MAXSENSORS][OW_ROMCODE_SIZE];
static int16_t gTempdata[MAXSENSORS];
static int8_t gNsensors = 0;

// Timer 2
static volatile uint8_t cnt2step = 0;
static volatile uint8_t gMeasurementTimer = 0; // sec

// called when TCNT2==OCR2A
// that is in 50Hz intervals
ISR(TIMER2_COMPA_vect) {
	cnt2step++;
	if (cnt2step > 49) { 
		gMeasurementTimer++;
		cnt2step = 0;
	}
}

// writes to global array gSensorIDs
static int8_t search_sensors(void) {
	uint8_t diff = OW_SEARCH_FIRST;
	uint8_t nSensors = 0;
	for(; diff != OW_LAST_DEVICE && nSensors < MAXSENSORS; nSensors++) {
		DS18X20_find_sensor(&diff, &(gSensorIDs[nSensors][0]));
		if (diff == OW_PRESENCE_ERR) {
			// No sensors found
			return -1; 
		}
		if (diff == OW_DATA_ERR) {
			// Bus Error
			return -2;
		}
	}
	return nSensors;
}

// read the latest measurement off the scratchpad of the ds18x20 sensor
// and store it in gTempdata
static int16_t read_temp_meas(void) {
	uint8_t i = 0;
	uint8_t subzero, cel, cel_frac_bits;
	for (; i < gNsensors; i++) {
		if (DS18X20_read_meas(&gSensorIDs[i][0], &subzero, &cel, &cel_frac_bits) == DS18X20_OK) {
			gTempdata[i] = cel * 10;
			gTempdata[i] += DS18X20_frac_bits_decimal(cel_frac_bits);
			if (subzero) {
				gTempdata[i] = -gTempdata[i];
			}
		}
	}
	return gTempdata[gNsensors-1];
}


/**
 * Initialize onewiretemp unit
**/
void onewiretemp_init(void) {
	cnt2step = 0;
	PRR &= ~_BV(PRTIM2); // write power reduction register to zero
	TIMSK2 = _BV(OCIE2A); // compare match on OCR2A
	TCNT2 = 0;  // init counter
	OCR2A = 244; // value to compare against
	TCCR2A = _BV(WGM21); // do not change any output pin, clear at compare match
	// divide clock by 1024: 12.5MHz/1024=12207.0313 Hz
	TCCR2B = _BV(CS22) | _BV(CS21) | _BV(CS20); // clock divider, start counter
	// OCR2A=244 is a division factor of 245
	// 12207.0313 / 245= 49.82461
	
	int i=0;
	for (; i < MAXSENSORS; i++) {
		gTempdata[i] = 0;
	}
	gNsensors = search_sensors();
	if (gNsensors > 0) {
		// Start measurement
		DS18X20_start_meas(NULL);
	}
}


/**
 * Update current temperature
 *
 * @param	temp	Temperature storage variable
**/
void onewiretemp_update(volatile int16_t* temp) {
	static uint8_t state = 0;
	// we need at least 750ms time between 
	// start of measurement and reading
	if (gMeasurementTimer == 2 && state == 0) {
		*temp = read_temp_meas();
		state = 1;
	}
	if (gMeasurementTimer == 3 && state == 1) {
		// Start measurement
		DS18X20_start_meas(NULL);
		state = 2;
	}
	if (gMeasurementTimer == 4) {
		gMeasurementTimer = 0;
		state = 0;
	}
}

/*
 // show the raw scratchpad (temperature info is stored there):
if (DS18X20_read_scratchpad(&gSensorIDs[i][0], sp) == DS18X20_OK) {
	int j = 0;
	plen = fill_tcp_data_p(buf, plen, PSTR("SP:"));
	for (; j < DS18X20_SP_SIZE; j++) {
		if (j) {
			plen = fill_tcp_data_p(buf, plen, PSTR(":"));
		}
		plen = fill_tcp_data_uint(buf, plen, sp[j]);
	}
}
*/

