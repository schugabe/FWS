#ifndef __ADC_H__
#define __ADC_H__

void adc_init(void);
void adc_register(uint8_t pin,(void*)callback(void));
void adc_start(void);

#endif  /* __ADC_H__ */
