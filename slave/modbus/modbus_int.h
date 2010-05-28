#ifndef _modbus_int_h_
#define _modbus_int_h_

#include "modbus.h"

// server listen port
#define MODBUSPORT 502

// Message structures
typedef struct {
	uint16_t transmId;
	uint16_t protoId;
	uint16_t length;
	uint8_t unitId;
} mbap_t;

typedef struct {
	uint8_t function;
	uint8_t errorcode;
} error_t;

// Modbus Message incl. MBAP and PDU
typedef struct {
	mbap_t mbap;
	union {
		uint8_t function;
		error_t error;
	};
} modbusmsg_t;

// Modbus error codes
#define MB_ERR_FUNC 1
#define MB_ERR_ADDR 2
#define MB_ERR_DATA 3
#define MB_ERR_PROC 4

#define MB_FUNC_READREG 0x04
#define MB_FUNC_WRITEREG 0x06
#define MB_FUNC_DEV_ID 0x2B

typedef struct {
	uint8_t function;
	uint16_t start_address;
	uint16_t amount;
} readreg_req_t;

typedef struct {
	uint8_t function;
	uint8_t bytecount;
} readreg_res_t;

typedef struct {
	uint8_t function;
	uint16_t start_address;
	uint16_t value;
} writereg_req_t;

typedef struct {
	uint8_t function;
	uint8_t type;
	uint8_t code;
	uint8_t objid;
} devid_req_t;

typedef struct {
	uint8_t id;
	uint8_t length;
} devid_obj_t;

typedef struct {
	uint8_t function;
	uint8_t type;
	uint8_t code;
	uint8_t conflevel;
	uint8_t more;
	uint8_t nextobjid;
	uint8_t amount;
} devid_res_t;

/*
   The following macros are used to convert from node 
   byteorder (little endian) to MODBUS byteorder (big endian)
*/
#define CONVBYTEORDER_W(intype, outtype, input) \
({                                                  \
	intype __param = (intype) input;            \
	outtype __result;                           \
	__asm__ volatile(                           \
                "mov %A0, %B1" "\n\t"               \
		"mov %B0, %A1" "\n\t"               \
		: "=&r" (__result)                  \
		: "r" (__param)                     \
	);                                          \
	__result;                                   \
})

#define TO_UINT16(input) CONVBYTEORDER_W(uint16_t, uint16_t, input)
#define FROM_UINT16(input) CONVBYTEORDER_W(uint16_t, uint16_t, input)

#endif
