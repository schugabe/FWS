OUTDIR = bin/

OBJ = $(OUTDIR)sensor_main.o 
OBJ += $(OUTDIR)modbus.o $(OUTDIR)enc28j60.o $(OUTDIR)ip_arp_udp_tcp.o $(OUTDIR)windspeed.o $(OUTDIR)adc.o $(OUTDIR)led.o

VPATH = modbus modbus/tcp sensors

TARGET=$(OUTDIR)sensor

MCU=atmega328p
DUDECPUTYPE=m328p
DEF=F_CPU=12500000UL

LOADCMD=avrdude
LOADARG=-p $(DUDECPUTYPE) -c stk500v2 -e -U flash:w:
FUSEARG=-p $(DUDECPUTYPE) -v -c stk500v2

CC=avr-gcc
OBJCOPY=avr-objcopy
# optimize for size:
CFLAGS=-g -D$(DEF) -mmcu=$(MCU) -Wall -W -Os -mcall-prologues

all: $(TARGET).hex

$(TARGET).hex : $(TARGET).elf
	$(OBJCOPY) -R .eeprom -O ihex $(TARGET).elf $(TARGET).hex
	avr-size $(TARGET).elf
	@echo " "
	@echo "Expl.: data=initialized data, bss=uninitialized data, text=code"
	@echo " "

$(TARGET).elf: $(OBJ)
	$(CC) $(CFLAGS) -o $@ -Wl,-Map,$(TARGET).map $(OBJ)

$(OUTDIR)%.o: %.c
	$(CC) $(CFLAGS) -o $@ -c $<

load: $(TARGET).hex
	$(LOADCMD) $(LOADARG)$(TARGET).hex

fuse:
	$(LOADCMD) $(FUSEARG)
	
clean:
	rm -f $(OUTDIR)*
