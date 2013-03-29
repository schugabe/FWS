// vim:sw=8:ts=8:si:et:
#ifndef _1wire_h_
#define _1wire_h_

/*******************************************/

#define OW_CONF_DELAYOFFSET 0

/*******************************************/

// #define OW_SHORT_CIRCUIT 0x01

#define OW_MATCH_ROM 0x55
#define OW_SKIP_ROM 0xCC
#define OW_SEARCH_ROM 0xF0

#define OW_SEARCH_FIRST 0xFF // start new search
#define OW_PRESENCE_ERR 0xFF
#define OW_DATA_ERR 0xFE
#define OW_LAST_DEVICE 0x00 // last device found 0x01 ... 0x40: continue searching

// rom-code size including CRC
#define OW_ROMCODE_SIZE 8

extern uint8_t ow_reset(void);

extern uint8_t ow_bit_io( uint8_t b );
extern uint8_t ow_byte_wr( uint8_t b );
extern uint8_t ow_byte_rd( void );

extern uint8_t ow_rom_search( uint8_t diff, uint8_t *id );

extern void ow_command( uint8_t command, uint8_t *id );

extern uint8_t ow_input_pin_state(void);


#endif

