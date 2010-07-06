/** \file led.h \brief LED control library. */
/*========================================================*/
/*     Author: Markus Klein                               */
/*       Date: 29.05.2010                                 */
/*========================================================*/

#ifndef __LED_H__
#define __LED_H__

/// define blink frequency here [Hz]
#define	BLINK_FREQ	1

/**
 * Initialize LED
**/
extern void led_init(void);

/**
 * Switch on LED permanently
**/
extern inline void led_on(void);

/**
 * Switch off LED permanently
**/
extern inline void led_off(void);

/**
 * Blink LED
**/
extern inline void led_blink(void);

#endif  /* __LED_H__ */
