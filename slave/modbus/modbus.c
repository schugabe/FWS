/*********************************************
 * vim:sw=8:ts=8:si:et
 * To use the above modeline in vim you must have "set modeline" in your .vimrc
 * Author: Guido Socher
 * Copyright: GPL V2
 *
 * Tuxgraphics AVR webserver/ethernet board
 *
 * http://tuxgraphics.org/electronics/
 * Chip type           : Atmega88/168/328 with ENC28J60
 *********************************************/
#include <avr/io.h>
#include <stdlib.h>

#include "../sensor_main.h"

#include "tcp/ip_arp_udp_tcp.h"
#include "tcp/enc28j60.h"
#include "tcp/net.h"

#include "modbus_int.h"

// MAC Address: First 3 Numbers are: FWS
static uint8_t mymac[6] = {0x46,0x57,0x53,0x10,0x00,0x29};
// IP Address:
static uint8_t myip[4] = {10,0,0,29};

// TCP Buffer
#define BUFFER_SIZE 800
static uint8_t buf[BUFFER_SIZE+1];

// Register file for indirect addressing of values
#define MODBUS_REGISTER_COUNT 8
static uint16_t* registers[MODBUS_REGISTER_COUNT];

static uint8_t mb_set_error(modbusmsg_t* msg,uint8_t err) {
	msg->error.function |= 0x80;
	msg->error.errorcode = err;
	return sizeof(error_t);
}

void mb_init(void) {
	//initialize the hardware driver for the enc28j60
	enc28j60Init(mymac);
	enc28j60clkout(2); // change clkout from 6.25MHz to 12.5MHz
	_delay_us(60); // 60us
	enc28j60PhyWrite(PHLCON,0x476);
	
	//init the ethernet/ip layer:
	init_ip_arp_udp_tcp(mymac,myip,MODBUSPORT);
	
	uint8_t i = 0;
	for (; i < MODBUS_REGISTER_COUNT; i++)
		registers[i] = (uint16_t*)NULL;
}

// Register the address of a register
void mb_registerRegister(uint8_t num, uint16_t* addr) {
	registers[num] = addr;
}
// Unregister a register
void mb_removeRegister(uint8_t num) {
	registers[num] = (uint16_t*)NULL;
}

// Handle data
void mb_handleRequest(void) {
	// read packet, handle ping and wait for a tcp packet
	uint16_t dat_p = packetloop_icmp_tcp(buf,enc28j60PacketReceive(BUFFER_SIZE, buf));
	
	// check if data was received
	if (dat_p == 0)
		return;

	// make structure pointer to buffer 
	modbusmsg_t* msg = (modbusmsg_t*)&buf[dat_p];

	// check for modbus protocol
	if (FROM_UINT16(msg->mbap.protoId) > 0)
		return;

	uint16_t amount, addr, len;
	switch (msg->request.function) {
		case MB_FUNC_READREG:
			amount = FROM_UINT16(msg->request.amount);
			addr = FROM_UINT16(msg->request.start_address);
			
			if (amount == 0 || amount > 0x7D) {
				len = mb_set_error(msg,MB_ERR_AMOUNT);
				break;
			}
			if (addr + amount <= MODBUS_REGISTER_COUNT) {
				uint16_t *ptr = (uint16_t*)&(msg->response.function) + 1;
				uint16_t cnt = 0;
				for (; cnt < amount; cnt++) {
					if (registers[addr+cnt] == NULL) {
						len = mb_set_error(msg,MB_ERR_PROC);
						break;
					}
					*ptr = TO_UINT16(*(registers[addr+cnt]));
				}
				// if an error occurred
				if (cnt < amount)
					break;
				
				msg->response.bytecount = cnt*sizeof(uint16_t);
				len = sizeof(response_t)+cnt*sizeof(uint16_t);
			} else
				len = mb_set_error(msg,MB_ERR_ADDR);
			break;
		default: // set error
			len = mb_set_error(msg,MB_ERR_FUNC);
			break;
	}
	msg->mbap.length = TO_UINT16(len+1);
	www_server_reply(buf,sizeof(mbap_t)+len);
}

