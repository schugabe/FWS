/** \file modbus.h \brief Modbus library */
/*========================================================*/
/*     Author: Markus Klein                               */
/*       Date: 11.04.2010                                 */
/*========================================================*/

#ifndef _modbus_h_
#define _modbus_h_

/**
 * callback type for write register function
 *
 * @param	num		Addressed register number
 * @param	value	Data
 * @return			Return true if successfully processed, otherwise false.
**/
typedef uint8_t(*mb_writecb_t)(uint8_t num, uint16_t value);

/**
 * Initialize ENC28J60 Chip
**/
extern void mb_init(void);

/**
 * Set IP address
 *
 * @param	ip	New IP address. Index 0 is highest IP segment
**/
extern void mb_setIP(uint8_t ip[]);

/**
 * Add a new register for read function
 *
 * @param	num	Address of register
 * @param	addr	Pointer to memory location where data is stored. Set NULL to disable this register.
**/
extern void mb_addReadRegister(uint8_t num, uint16_t* addr);

/**
 * Add a new register for write function
 *
 * @param	num	Address of register
 * @param	cb	Callback function for this register. Set NULL to disable this register.
**/
extern void mb_addWriteRegister(uint8_t num, mb_writecb_t cb);

/**
 * Handle Modbus requests
 * Checks for new TCP data and looks for new modbus messages
**/
extern void mb_handleRequest(void);

#endif
