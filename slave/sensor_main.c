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
#include <avr/interrupt.h>

#include "tcp/ip_arp_udp_tcp.h"
#include "tcp/enc28j60.h"
#include "tcp/timeout.h"
#include "tcp/net.h"

#include "sensor_main.h"

// This software is a web server only. 
//
// please modify the following two lines. mac and ip have to be unique
// in your local area network. You can not have the same numbers in
// two devices:
static uint8_t mymac[6] = {0x54,0x55,0x58,0x10,0x00,0x29};
// how did I get the mac addr? Translate the first 3 numbers into ascii is: TUX
static uint8_t myip[4] = {10,0,0,29};

// server listen port for www
#define MYWWWPORT 502

#define BUFFER_SIZE 800
static uint8_t buf[BUFFER_SIZE+1];

#define MODBUS_REGISTER_COUNT 1
uint16_t* mb_register[MODBUS_REGISTER_COUNT];

uint16_t windspeed = 0x4114;

uint8_t set_error(modbusmsg_t* msg,uint8_t err) {
	msg->error.function |= 0x80;
	msg->error.errorcode = err;
	return sizeof(error_t);
}

int main(void){
        uint16_t dat_p;
        
        //initialize the hardware driver for the enc28j60
        enc28j60Init(mymac);
        enc28j60clkout(2); // change clkout from 6.25MHz to 12.5MHz
        _delay_loop_1(0); // 60us
        enc28j60PhyWrite(PHLCON,0x476);
        
        //init the ethernet/ip layer:
        init_ip_arp_udp_tcp(mymac,myip,MYWWWPORT);
        
        PORTB &= ~_BV(PB1);
        DDRB |= _BV(PB1);
        
        mb_register[0] = &windspeed;
        
        TIMSK0 |= _BV(TOIE0);
        TCCR0A = 0x00;
        TCCR0B = 0x05; // 1024 pres, normal mode
        
        sei();

        while (1) {
                // read packet, handle ping and wait for a tcp packet:
                dat_p=packetloop_icmp_tcp(buf,enc28j60PacketReceive(BUFFER_SIZE, buf));
                
                // check if data was received
                if (dat_p == 0)
                	goto ENDE;
                
                // make structure pointer to buffer 
		modbusmsg_t* msg = (modbusmsg_t*)&buf[dat_p];
		
		// check for modbus protocol
		if (FROM_UINT16(msg->mbap.protoId) > 0)
			goto ENDE;
		
		uint16_t amount, addr, len;
		switch (msg->request.function) {
			case MB_FUNC_READREG: // read register
				amount = FROM_UINT16(msg->request.amount);
				addr = FROM_UINT16(msg->request.start_address);
				
				if (amount == 0 || amount > 0x7D) {
					len = set_error(msg,MB_ERR_AMOUNT);
					break;
				}
				if (addr + amount <= MODBUS_REGISTER_COUNT) {
					uint16_t *ptr = (uint16_t*)&(msg->response.function) + 1;
					uint16_t cnt = 0;
					for (; cnt < amount; cnt++) {
						*ptr = TO_UINT16(*(mb_register[addr+cnt]));
					}
					msg->response.bytecount = cnt*sizeof(uint16_t);
					len = sizeof(response_t)+cnt*sizeof(uint16_t);
				} else
					len = set_error(msg,MB_ERR_ADDR);
				
				break;
			default: // set error
				len = set_error(msg,MB_ERR_FUNC);
				break;
		}
		msg->mbap.length = TO_UINT16(len+1);
		www_server_reply(buf,sizeof(mbap_t)+len);
		
ENDE:
		; // further code will come here maybe
        }
}

ISR(TIMER0_OVF_vect) {
	if (PORTB & _BV(PB1))
		PORTB &= ~_BV(PB1);
	else
		PORTB |= _BV(PB1);
}
