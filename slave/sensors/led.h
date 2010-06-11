#ifndef __LED_H__
#define __LED_H__

/// define blink frequency here [Hz]
#define	BLINK_FREQ	1

extern void led_init(void);
extern inline void led_on(void);
extern inline void led_off(void);
extern inline void led_blink(void);

#endif  /* __LED_H__ */
