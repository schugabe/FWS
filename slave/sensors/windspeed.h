#ifndef __WINDSPEED_H__
#define __WINDSPEED_H__

extern void windspeed_init(uint16_t volatile* mem,uint16_t volatile* err,uint16_t volatile* scnt);
extern void windspeed_start(void);
extern void windspeed_stop(void);

#endif  /* __WINDSPEED_H__ */
