// vim:sw=8:ts=8:si:et: 
/*********************************************************************************
Title:    DS18X20-Functions via One-Wire-Bus
Author:   Martin Thomas <eversmith@heizung-thomas.de>   
          http://www.siwawi.arubi.uni-kl.de/avr-projects
          
          Modifications and updates by Guido Socher, June 2009
          The code was also reduced to only the needed function for
          this application.
          http://tuxgraphics.org/electronics/
Copyright: GPL

Partly based on code from Peter Dannegger and others

Extended measurements for DS18(S)20 contributed by Carsten Foss (CFO)

**********************************************************************************/

#include <avr/io.h>
#include <stdlib.h>
#include "ds18x20.h"
#include "onewire.h"
#include "crc8.h"

#include <string.h>

/* 
   convert raw value from DS18x20 to Celsius
   input is: 
   - familycode fc (0x10/0x28 see header)
   - scratchpad-buffer
   output is:
   - cel full celsius
   - fractions of celsius in millicelsius*(10^-1)/625 (the 4 LS-Bits)
   - subzero =0 positiv / 1 negativ
   TODO invalid-values detection (but should be covered by CRC)
*/
void DS18X20_meas_to_cel( uint8_t fc, uint8_t *sp, 
        uint8_t* subzero, uint8_t* cel, uint8_t* cel_frac_bits)
{
        uint16_t meas;
        uint8_t  i;
        
        meas = sp[0];  // LSB
        meas |= ((uint16_t)sp[1])<<8; // MSB
        //meas = 0xff5e; meas = 0xfe6f;
        
        //  only work on 12bit-base
        if( fc == DS18S20_ID ) { // 9 -> 12 bit if 18S20
                /* Extended measurements for DS18S20 contributed by Carsten Foss */
                meas &= (uint16_t) 0xfffe;        // Discard LSB , needed for later extended precicion calc
                meas <<= 3;                                        // Convert to 12-bit , now degrees are in 1/16 degrees units
                meas += (16 - sp[6]) - 4;        // Add the compensation , and remember to subtract 0.25 degree (4/16)
        }
        
        // check for negative 
        if ( meas & 0x8000 )  {
                *subzero=1;      // mark negative
                meas ^= 0xffff;  // convert to positive => (twos complement)++
                meas++;
        }
        else *subzero=0;
        
        // clear undefined bits for B != 12bit
        if ( fc == DS18B20_ID ) { // check resolution 18B20
                i = sp[DS18B20_CONF_REG];
                if ( (i & DS18B20_12_BIT) == DS18B20_12_BIT ) ;
                else if ( (i & DS18B20_11_BIT) == DS18B20_11_BIT ) 
                        meas &= ~(DS18B20_11_BIT_UNDF);
                else if ( (i & DS18B20_10_BIT) == DS18B20_10_BIT ) 
                        meas &= ~(DS18B20_10_BIT_UNDF);
                else { // if ( (i & DS18B20_9_BIT) == DS18B20_9_BIT ) { 
                        meas &= ~(DS18B20_9_BIT_UNDF);
                }
        }                        
        
        *cel  = (uint8_t)(meas >> 4); 
        *cel_frac_bits = (uint8_t)(meas & 0x000F);
        
}

// convert fraction bits to decimal (0-9)
// This number represents one digit behind the decimal point.
// cel_frac_bits is a number from 0 to 15
// cel_frac_bits   meaning  returnValue
//   0             0.0        0
//   1             0.0625     1
//   2             0.1250     1
//   3             0.1875     2
//   4             0.2500     2
//   5             0.3125     3
//   6             0.3750     4
//   7             0.4375     4
//   8             0.5000     5
//   9             0.5625     5
//   10            0.6250     6
//   11            0.6875     7
//   12            0.7500     7
//   13            0.8125     8
//   14            0.8750     9
//   15            0.9375     9
//   Note this rouding makes sense because the accuracy of
//   the sensor is anyhow only 0.5'C
uint8_t DS18X20_frac_bits_decimal(uint8_t cel_frac_bits)
{
        uint8_t d;
        d=cel_frac_bits*6 + 5;
        if (cel_frac_bits>12) d+=3;
        return(d/10);
}

/* find DS18X20 Sensors on 1-Wire-Bus
   input/ouput: diff is the result of the last rom-search
   output: id is the rom-code of the sensor found */
void DS18X20_find_sensor(uint8_t *diff, uint8_t *id)
{
        while(1) {
                *diff = ow_rom_search( *diff, id );
                if ( *diff==OW_PRESENCE_ERR || *diff==OW_DATA_ERR ||
                  *diff == OW_LAST_DEVICE ) return;
                if ( id[0] == DS18B20_ID || id[0] == DS18S20_ID ) return;
        }
}

/* start measurement (CONVERT_T) for all sensors if input id==NULL 
   or for single sensor. then id is the rom-code */
uint8_t DS18X20_start_meas(  uint8_t *id)
{
        ow_reset(); 
        if( ow_input_pin_state() ) { // only send if bus is "idle" = high
                ow_command( DS18X20_CONVERT_T, id );
                return DS18X20_OK;
        } else { 
                // Short Circuit !
                return DS18X20_START_FAIL;
        }
}

/* reads temperature (scratchpad) of sensor with rom-code id
   output: subzero==1 if temp.<0, cel: full celsius, mcel: frac 
   in millicelsius*0.1
   i.e.: subzero=1, cel=18, millicel=5000 = -18,5000°C */
uint8_t DS18X20_read_meas(uint8_t *id, uint8_t *subzero, 
        uint8_t *cel, uint8_t *cel_frac_bits)
{
        uint8_t i;
        uint8_t sp[DS18X20_SP_SIZE];
        
        ow_reset(); 
        ow_command(DS18X20_READ, id);
        for ( i=0 ; i< DS18X20_SP_SIZE; i++ ) sp[i]=ow_byte_rd();
        if ( crc8( &sp[0], DS18X20_SP_SIZE ) ) 
                return DS18X20_ERROR_CRC;
        DS18X20_meas_to_cel(id[0], sp, subzero, cel, cel_frac_bits);
        return DS18X20_OK;
}


// writes to sp[]
uint8_t DS18X20_read_scratchpad( uint8_t *id, uint8_t sp[] )
{
        uint8_t i;
        
        ow_reset(); 
        if( ow_input_pin_state() ) { // only send if bus is "idle" = high
                ow_command( DS18X20_READ, id );
                for ( i=0 ; i< DS18X20_SP_SIZE; i++ )        sp[i]=ow_byte_rd();
                return DS18X20_OK;
        } else { 
                return DS18X20_ERROR; // Short Circuit !
        }
}


