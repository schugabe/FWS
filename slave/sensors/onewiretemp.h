/** \file onewiretemp.h \brief DS18s20 sensor library. */
/*========================================================*/
/*     Author: Markus Klein                               */
/*       Date: 28.03.2013                                 */
/*========================================================*/

#ifndef __ONEWIRETEMP_H__
#define __ONEWIRETEMP_H__

/**
 * Initialize onewiretemp unit
**/
extern void onewiretemp_init(void);

/**
 * Update current temperature
 *
 * @param	temp	Temperature storage variable
**/
extern void onewiretemp_update(volatile int16_t* temp);

#endif  /* __ONEWIRETEMP_H__ */

