#ifndef __WINDSPEED_H__
#define __WINDSPEED_H__

extern void windspeed_init(uint16_t volatile* _windspeed,uint16_t volatile* _errcnt,uint16_t volatile* _cnt);
extern void windspeed_start(void);
extern void windspeed_stop(void);

#endif  /* __WINDSPEED_H__ */
