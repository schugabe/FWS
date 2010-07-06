/*========================================================*/
/*     Author: Markus Klein                               */
/*       Date: 11.04.2010                                 */
/*========================================================*/

#include <avr/io.h>
#include <string.h>
#include <stdlib.h>
#include <util/delay.h>

#include "tcp/ip_arp_udp_tcp.h"
#include "tcp/enc28j60.h"
#include "tcp/net.h"

#include "modbus_int.h"

/// MAC Address: First 3 Numbers are: FWS
static const uint8_t mymac[6] = {0x46,0x57,0x53,0x10,0x00,0x29};

#define BUFFER_SIZE 800
/// TCP Buffer approx. half of default MTU to save RAM
static uint8_t buf[BUFFER_SIZE+1];

// Register file for indirect addressing of values
#define MODBUS_REGISTER_COUNT 16
static uint16_t* registers[MODBUS_REGISTER_COUNT] = {NULL};
static mb_writecb_t writecbs[MODBUS_REGISTER_COUNT] = {NULL};

/**
 * Set error values in current message.
 *
 * @param	msg	Pointer to msg to work on
 * @param	err	Error number. Use one of the MB_ERR_* constants
 * @return		Size of error PDU
**/
static uint8_t mb_set_error(modbusmsg_t* msg,uint8_t err) {
	msg->error.function |= 0x80;
	msg->error.errorcode = err;
	return sizeof(error_t);
}

/**
 * Initialize ENC28J60 Chip
**/
void mb_init(void) {
	//initialize the hardware driver for the enc28j60
	enc28j60Init((uint8_t*)mymac);
	enc28j60clkout(2); // change clkout from 6.25MHz to 12.5MHz
	_delay_us(60); // 60us
	enc28j60PhyWrite(PHLCON,0x476);
}

/**
 * Set IP address
 *
 * @param	ip	New IP address. Index 0 is highest IP segment
**/
void mb_setIP(uint8_t ip[]) {
	init_ip_arp_udp_tcp((uint8_t*)mymac,ip,MODBUSPORT);
}

/**
 * Add a new register for read function
 *
 * @param	num	Address of register
 * @param	addr	Pointer to memory location where data is stored. Set NULL to disable this register.
**/
void mb_addReadRegister(uint8_t num, uint16_t* addr) {
	if (num >= MODBUS_REGISTER_COUNT)
		return;
	registers[num] = addr;
}

/**
 * Add a new register for write function
 *
 * @param	num	Address of register
 * @param	cb	Callback function for this register. Set NULL to disable this register.
**/
void mb_addWriteRegister(uint8_t num, mb_writecb_t cb) {
	if (num >= MODBUS_REGISTER_COUNT)
		return;
	writecbs[num] = cb;
}

/**
 * Handle Modbus requests
 * Checks for new TCP data and looks for new modbus messages
**/
void mb_handleRequest(void) {
	// read packet, handle ping and wait for a tcp packet
	uint16_t dat_p = packetloop_icmp_tcp(buf,enc28j60PacketReceive(BUFFER_SIZE, buf));
	
	// check if data was received
	if (dat_p == 0)
		return;

	// make structure pointer to buffer 
	modbusmsg_t* msg = (modbusmsg_t*)&buf[dat_p];

	// check for modbus protocol id
	if (FROM_UINT16(msg->mbap.protoId) > 0)
		return;

	uint16_t len;
	switch (msg->function) {
		// Modbus read function
		case MB_FUNC_READREG: {
			readreg_req_t* req = (readreg_req_t*)&(msg->function);
			readreg_res_t* res = (readreg_res_t*)&(msg->function);
			uint16_t amount = FROM_UINT16(req->amount);
			uint16_t addr = FROM_UINT16(req->start_address);
			
			if (amount == 0 || amount > 0x7D) {
				len = mb_set_error(msg,MB_ERR_DATA);
				break;
			}
			if (addr + amount <= MODBUS_REGISTER_COUNT) {
				uint16_t *ptr = (uint16_t*)&(res->function);
				uint16_t cnt = 0;
				for (; cnt < amount; cnt++) {
					if (registers[addr+cnt] == NULL) {
						len = mb_set_error(msg,MB_ERR_PROC);
						break;
					}
					++ptr;
					*ptr = TO_UINT16(*(registers[addr+cnt]));
				}
				// if an error occurred
				if (cnt < amount)
					break;
				
				res->bytecount = cnt*sizeof(uint16_t);
				len = sizeof(readreg_res_t)+cnt*sizeof(uint16_t);
			} else
				len = mb_set_error(msg,MB_ERR_ADDR);
			break;
			}
		// Modbus write function
		case MB_FUNC_WRITEREG: {
			writereg_req_t* req = (writereg_req_t*)&(msg->function);
			uint16_t addr = FROM_UINT16(req->start_address);
			uint16_t value = FROM_UINT16(req->value);
			
			if (addr >= MODBUS_REGISTER_COUNT || writecbs[addr] == NULL) {
				len = mb_set_error(msg,MB_ERR_ADDR);
				break;
			}
			if (writecbs[addr](addr,value))
				len = sizeof(writereg_req_t);
			else
				len = mb_set_error(msg,MB_ERR_PROC);
			break;
			}
		// Modbus device id function
		// Not implemented completely
		case MB_FUNC_DEV_ID: {
			devid_req_t* req = (devid_req_t*)&(msg->function);
			devid_res_t* res = (devid_res_t*)&(msg->function);
			devid_obj_t* obj = (devid_obj_t*)&(res->nextobjid);
			uint8_t i = req->objid;
			
			if (req->type != 0x0E) {
				len = mb_set_error(msg,MB_ERR_FUNC);
				break;
			}
			if (!req->code || req->code > 4) {
				len = mb_set_error(msg,MB_ERR_DATA);
				break;
			}
			res->conflevel = 0x01;
			res->more = 0x00;
			res->nextobjid = 0x00;
			res->amount = 3;
			len = sizeof(devid_res_t);
			if (i > 2)
				i = 0;
			// add requested objects
			for (; i < 3; i++) {
				++obj;
				obj->id = i;
				obj->length = 0;
				len += sizeof(devid_obj_t)+obj->length;
			}
			break;
			}
		default: // set error
			len = mb_set_error(msg,MB_ERR_FUNC);
			break;
	}
	msg->mbap.length = TO_UINT16(len+1);
	// send response
	www_server_reply(buf,sizeof(mbap_t)+len);
}

