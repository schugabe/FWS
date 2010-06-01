#ifndef _modbus_h_
#define _modbus_h_

typedef uint8_t(*mb_writecb_t)(uint8_t num, uint16_t value);

extern void mb_init(void);
extern void mb_setIP(uint8_t ip[]);
extern void mb_addReadRegister(uint8_t num, uint16_t* addr);
extern void mb_addWriteRegister(uint8_t num, mb_writecb_t cb);
extern void mb_handleRequest(void);

#endif
