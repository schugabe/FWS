OBJ = sensor_main.o 

OBJ += modbus.o enc28j60.o ip_arp_udp_tcp.o windspeed.o winddir.o

VPATH = modbus modbus/tcp sensors

TARGET=sensor

MCU=atmega328p
DUDECPUTYPE=m328p

LOADCMD=avrdude
LOADARG=-p $(DUDECPUTYPE) -c stk500v2 -e -U flash:w:

CC=avr-gcc
OBJCOPY=avr-objcopy
# optimize for size:
CFLAGS=-g -mmcu=$(MCU) -Wall -W -Os -mcall-prologues

all: $(TARGET).hex

$(TARGET).hex : $(TARGET).elf
	$(OBJCOPY) -R .eeprom -O ihex $(TARGET).elf $(TARGET).hex
	avr-size $(TARGET).elf
	@echo " "
	@echo "Expl.: data=initialized data, bss=uninitialized data, text=code"
	@echo " "

$(TARGET).elf: $(OBJ)
	$(CC) $(CFLAGS) -o $@ -Wl,-Map,$(TARGET).map $(OBJ)

%.o: %.c
	$(CC) $(CFLAGS) -c $<

load: $(TARGET).hex
	$(LOADCMD) $(LOADARG)$(TARGET).hex

clean:
	rm -f *.o *.map *.elf *.hex *.s *.i
