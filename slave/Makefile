OUTDIR = bin/

OBJ = $(OUTDIR)sensor_main.o
OBJ += $(OUTDIR)modbus.o $(OUTDIR)enc28j60.o $(OUTDIR)ip_arp_udp_tcp.o $(OUTDIR)windspeed.o $(OUTDIR)adc.o $(OUTDIR)led.o $(OUTDIR)crc8.o $(OUTDIR)ds18x20.o $(OUTDIR)onewire.o $(OUTDIR)onewiretemp.o
#OBJ += $(OUTDIR)windspeed.o $(OUTDIR)adc.o $(OUTDIR)led.o

VPATH = modbus modbus/tcp sensors sensors/ds18s20

TARGET=$(OUTDIR)sensor

MCU=atmega328p
DEF=F_CPU=12500000UL

LOADCMD=avrdude -p m328p -c stk500v2
LOADHEX=-e -U flash:w:$(TARGET).hex
LOADEEP=-U eeprom:w:$(TARGET).eep
FUSEARG=-v

CC=avr-gcc
OBJCOPY=avr-objcopy
# optimize for size:
CFLAGS=-g -D$(DEF) -mmcu=$(MCU) -Wall -Wextra -Os -mcall-prologues

all: $(TARGET).hex $(TARGET).eep

$(TARGET).hex : $(TARGET).elf
	$(OBJCOPY) -R .eeprom -O ihex $(TARGET).elf $(TARGET).hex
	avr-size $(TARGET).elf
	@echo " "
	@echo "Expl.: data=initialized data, bss=uninitialized data, text=code"
	@echo " "

$(TARGET).eep: $(TARGET).elf
	$(OBJCOPY) -j .eeprom --set-section-flags=.eeprom="alloc,load" \
	--change-section-lma .eeprom=0 --no-change-warnings -O ihex $(TARGET).elf $(TARGET).eep || exit 0

$(TARGET).elf: $(OBJ)
	$(CC) $(CFLAGS) -o $@ -Wl,-Map,$(TARGET).map $(OBJ)

$(OUTDIR)%.o: %.c
	@$(CC) $(CFLAGS) -o $@ -c $<

clean:
	rm -f $(OUTDIR)*

load: $(TARGET).hex
	$(LOADCMD) $(LOADHEX)

loadeep: $(TARGET).eep
	$(LOADCMD) $(LOADEEP)

fuse:
	$(LOADCMD) $(FUSEARG)

