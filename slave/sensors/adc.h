#ifndef __ADC_H__
#define __ADC_H__

/**
 * Callback type for ADC result
**/
typedef void(*callback_t)(uint16_t value);

extern void adc_init(void);
extern void adc_register(uint8_t chnum,callback_t cb);
extern void adc_start(void);
extern void adc_stop(void);

#endif  /* __ADC_H__ */
