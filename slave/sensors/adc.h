/** \file adc.h \brief ADC library. */
/*========================================================*/
/*     Author: Markus Klein                               */
/*       Date: 26.05.2010                                 */
/*========================================================*/

#ifndef __ADC_H__
#define __ADC_H__

/**
 * Callback type for ADC result
 * @param	value	Last ADC value
**/
typedef void(*callback_t)(uint16_t value);

/**
 * Initialize ADC unit
**/
extern void adc_init(void);

/**
 * Register a new ADC channel
 *
 * @param	chnum	Channel number to use
 * @param	cb	Result ready callback for this channel. Set to NULL to disable channel.
**/
extern void adc_register(uint8_t chnum,callback_t cb);

/**
 * Start ADC conversions
**/
extern void adc_start(void);

/**
 * Stop ADC conversions
**/
extern void adc_stop(void);

#endif  /* __ADC_H__ */
