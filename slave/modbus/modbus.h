#ifndef _modbus_h_
#define _modbus_h_

extern void mb_init(void);
extern void mb_registerRegister(uint8_t num, uint16_t* addr);
extern void mb_removeRegister(uint8_t num);
extern void mb_handleRequest(void);

#endif
