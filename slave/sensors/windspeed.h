/** \file windspeed.h \brief Wind speed sensor library. */
/*========================================================*/
/*     Author: Markus Klein                               */
/*       Date: 21.05.2010                                 */
/*========================================================*/

#ifndef __WINDSPEED_H__
#define __WINDSPEED_H__

/**
 * Initialize wind speed unit
 *
 * @param	_windspeed	Pointer to wind speed value storage
 * @param	_errcnt		Pointer to measurement error counter storage
 * @param	_cnt		Pointer to measurement counter storage
**/
extern void windspeed_init(uint16_t volatile* _windspeed,uint16_t volatile* _errcnt,uint16_t volatile* _cnt);

/**
 * Start wind speed measurement
**/
extern void windspeed_start(void);

/**
 * Stop wind speed measurement
**/
extern void windspeed_stop(void);

#endif  /* __WINDSPEED_H__ */
