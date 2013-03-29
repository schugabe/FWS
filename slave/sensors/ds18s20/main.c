/*********************************************
 * vim:sw=8:ts=8:si:et
 * To use the above modeline in vim you must have "set modeline" in your .vimrc
 * Author: Guido Socher
 * Copyright: GPL V2
 * See http://www.gnu.org/licenses/gpl.html
 *
 * Ethernet remote switch and 1wire sensor with 
 * HTTP interface and measurement graphs
 * See http://tuxgraphics.org/electronics/
 *
 * Chip type           : Atmega168 or Atmega328 with ENC28J60
 *********************************************/
#include <avr/io.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <avr/pgmspace.h>
#include <avr/eeprom.h>
#include <avr/interrupt.h>
#include "ip_arp_udp_tcp.h"
#include "enc28j60.h"
#include "timeout.h"
#include "net.h"
#include "websrv_help_functions.h"
#include "onewire.h"
#include "ds18x20.h"

// set output to VCC, red LED off
#define LEDOFF PORTB|=(1<<PORTB1)
// set output to GND, red LED on
#define LEDON PORTB&=~(1<<PORTB1)
// to test the state of the LED
#define LEDISOFF PORTB&(1<<PORTB1)

// please modify the following lines. mac and ip have to be unique
// in your local area network. You can not have the same numbers in
// two devices. The IP address may be changed at run-time but the
// MAC address is hardcoded:
static uint8_t mymac[6] = {0x54,0x55,0x58,0x10,0x00,0x29};
// how did I get the mac addr? Translate the first 3 numbers into ascii is: TUX
//
// The IP of this device (can also be changed at run-time, see README file):
static uint8_t myip[4] = {10,0,0,29};
//static uint8_t myip[4] = {192,168,0,88};
// listen port for tcp/www:
static uint16_t mywwwport=80;
// uncomment this to make an RSS feed available:
//#define RSS_FEATURE
// The URL under which the main page can be found. This will be
// used in the RSS code only:
#define RSSBASEURL "http://dyn.tuxgraphics.org:88"
// the password string (only characters: a-z,0-9,_,.,-,# ), max len 8 char:
static char password[9]="secret";
static char label[2][11]={"sensor0","sensor1"};
// -------------- do not change anything below this line ----------
// The buffer is the packet size we can handle and its upper limit is
// given by the amount of RAM that the atmega chip has.
#define BUFFER_SIZE 640
static uint8_t buf[BUFFER_SIZE+1];

// global string buffer
#define STR_BUFFER_SIZE 24
static char gStrbuf[STR_BUFFER_SIZE+1];
static uint16_t gPlen;
// if you want to have a page where you can see debug information about the 
// attached sensors then set this to one (dbg) otherwise to zero:
#define DEBUG_SENSORS 0
// uncomment this is you want the graphs in farenheit
//#define GRAPH_IN_F
#define MAXSENSORS 3
static uint8_t gSensorIDs[MAXSENSORS][OW_ROMCODE_SIZE];
static int16_t gTempdata[MAXSENSORS]; // temperature times 10
static int8_t gNsensors=0;
#define TEMPHIST_BUFFER_SIZE 60
// how often (in minutes times 10, 12=120min, 24=240min) to record the data to a graph (max value=255 ):
static uint8_t rec_interval=12;
// position at which the EEPROM is used for storage of temp. data:
#define EEPROM_TEMP_POS_OFFSET 40 
static uint8_t gTemphistnextptr=0;
//
static volatile uint8_t cnt2step=0;
static volatile uint8_t gSec=0;
static volatile uint8_t gMeasurementTimer=0; // sec
static int16_t gRelay_timeout_min=0; // 0=no timeout, other timeout in min
static uint8_t gRecMin=0; // miniutes counter for recording of history graphis
#ifdef RSS_FEATURE
static uint8_t rss_guid=0; // a counter stepped every rec_interval to generate
                           // a unique guid string
#endif
static uint8_t gTemp_measurementstatus=0; // 0=ok,1=error
//

// sensor=0..MAXSENSORS-1
int8_t read_temphist(uint8_t pos,uint8_t sensor)
{
        return ((int8_t)eeprom_read_byte((uint8_t *)(pos+EEPROM_TEMP_POS_OFFSET+TEMPHIST_BUFFER_SIZE*sensor)));
}

// no overflow protection here make sure you stay within the available eeprom size:
void store_temphist(int8_t val, uint8_t pos,uint8_t sensor)
{
        eeprom_write_byte((uint8_t *)(pos+EEPROM_TEMP_POS_OFFSET+TEMPHIST_BUFFER_SIZE*sensor),val); 
}

// 
uint8_t verify_password(char *str)
{
        if (strncmp(password,str,strlen(password))==0){
                return(1);
        }
        return(0);
}

uint16_t http200ok(uint8_t nocache)
{
        uint16_t plen;
        plen=fill_tcp_data_p(buf,0,PSTR("HTTP/1.0 200 OK\r\nContent-Type: text/html\r\n"));
        if (nocache){
                plen=fill_tcp_data_p(buf,plen,PSTR("Pragma: no-cache\r\n"));
        }
        return(fill_tcp_data_p(buf,plen,PSTR("\r\n")));
}

uint16_t http200okjs(uint8_t nocache)
{
        uint16_t plen;
        plen=fill_tcp_data_p(buf,0,PSTR("HTTP/1.0 200 OK\r\nContent-Type: application/x-javascript\r\n"));
        if (nocache){
                plen=fill_tcp_data_p(buf,plen,PSTR("Pragma: no-cache\r\n"));
        }
        return(fill_tcp_data_p(buf,plen,PSTR("\r\n")));
}

uint16_t print_line_mobile_webpage(uint16_t plen)
{
        plen=fill_tcp_data_p(buf,plen,PSTR("<meta name=viewport content=\"width=device-width\">\n"));
        return(plen);
}

uint16_t fill_tcp_data_uint(uint8_t *buf,uint16_t plen,uint8_t i)
{
        itoa(i,gStrbuf,10); // convert integer to string
        return(fill_tcp_data(buf,plen,gStrbuf));
}

uint16_t fill_tcp_data_int(uint8_t *buf,uint16_t plen,int8_t i)
{
        itoa(i,gStrbuf,10); // convert integer to string
        return(fill_tcp_data(buf,plen,gStrbuf));
}

// prepare the webpage by writing the data to the tcp send buffer
uint16_t print_webpage_relay_ok()
{
        uint16_t pl;
        pl=http200ok(0);
        pl=print_line_mobile_webpage(pl);
        pl=fill_tcp_data_p(buf,pl,PSTR("<a href=/>[home]</a>\n"));
        pl=fill_tcp_data_p(buf,pl,PSTR("<h2>OK</h2><hr>\n"));
        return(pl);
}

uint16_t print_webpage_relay(uint8_t on)
{
        uint16_t pl;
        pl=http200ok(1);
        pl=print_line_mobile_webpage(pl);
        pl=fill_tcp_data_p(buf,pl,PSTR("<a href=/>[home]</a> <a href=/sw>[refresh]</a>\n"));
        pl=fill_tcp_data_p(buf,pl,PSTR("<h2>remote switch control</h2>\n<pre>state: "));
        if (on){
                pl=fill_tcp_data_p(buf,pl,PSTR("<font color=#00FF00>ON</font>"));
                if (gRelay_timeout_min>0){
                        pl=fill_tcp_data_p(buf,pl,PSTR(" [for "));
                        // gRelay_timeout_min is 16bit we do not use fill_tcp_data_int
                        itoa(gRelay_timeout_min,gStrbuf,10); // convert integer to string
                        pl=fill_tcp_data(buf,pl,gStrbuf);
                        pl=fill_tcp_data_p(buf,pl,PSTR(" more min]"));
                }
        }else{
                pl=fill_tcp_data_p(buf,pl,PSTR("OFF"));
        }
        pl=fill_tcp_data_p(buf,pl,PSTR("\n<form action=/ method=get>"));
        pl=fill_tcp_data_p(buf,pl,PSTR("<input type=hidden name=sc value="));
        if (on){
                pl=fill_tcp_data_p(buf,pl,PSTR("0>\npassw: <input type=password size=10 name=pw>"));
                pl=fill_tcp_data_p(buf,pl,PSTR("<input type=submit value=\"switch off\"></form>\n"));
        }else{
                pl=fill_tcp_data_p(buf,pl,PSTR("1>\nOn for <input type=text name=tm value=0 size=3> minutes (0=no timeout)\n\n"));
                pl=fill_tcp_data_p(buf,pl,PSTR("passw: <input type=password size=10 name=pw>"));
                pl=fill_tcp_data_p(buf,pl,PSTR("<input type=submit value=\"switch on\"></form>\n"));
        }
        pl=fill_tcp_data_p(buf,pl,PSTR("</pre><hr>tuxgraphics"));
        return(pl);
}


// writes to global array gSensorIDs
int8_t search_sensors(void)
{
	uint8_t diff;
        uint8_t nSensors=0;
	for( diff = OW_SEARCH_FIRST; 
		diff != OW_LAST_DEVICE && nSensors < MAXSENSORS ; )
	{
		DS18X20_find_sensor( &diff, &(gSensorIDs[nSensors][0]) );
		
		if( diff == OW_PRESENCE_ERR ) {
			return(-1); //No Sensor found
		}
		if( diff == OW_DATA_ERR ) {
			return(-2); //Bus Error
		}
		nSensors++;
	}
	return nSensors;
}

// start a measurement for all sensors on the bus:
void start_temp_meas(void){
        gTemp_measurementstatus=0;
        if ( DS18X20_start_meas(NULL) != DS18X20_OK) {
                gTemp_measurementstatus=1;
        }
}

// convert celsius times 10 values to Farenheit
int8_t c2f(int16_t celsiustimes10){
        return((int8_t)((int16_t)(celsiustimes10 * 18)/100) + 32);
}


// read the latest measurement off the scratchpad of the ds18x20 sensor
// and store it in gTempdata
void read_temp_meas(void){
        uint8_t i;
        uint8_t subzero, cel, cel_frac_bits;
        for ( i=0; i<gNsensors; i++ ) {
        
                if ( DS18X20_read_meas( &gSensorIDs[i][0], &subzero,
                                &cel, &cel_frac_bits) == DS18X20_OK ) {
                        gTempdata[i]=cel*10;
                        gTempdata[i]+=DS18X20_frac_bits_decimal(cel_frac_bits);
                        if (subzero){
                                gTempdata[i]=-gTempdata[i];
                        }
                }else{
                        gTempdata[i]=0;
                }
        }
}

// p1.js
uint16_t print_p1_js(void)
{
        // I am sorry that the javascript is a bit unreadable 
        // but we absolutely need to keep the amount of code
        // in bytes small
        uint16_t pl;
        pl=http200okjs(0);
        // g = graphdata array
        // m = minutes since last data, global variable
        // d = recording interval in min, global variable
        // t = new Date(), global variable
        pl=fill_tcp_data_p(buf,pl,PSTR("\
function pad(n){\
var s=n.toString(10);\
if (s.length<2){return(\"0\"+s)}\
return(s);\
}\n\
function dw(s){document.writeln(s)}\n\
function scsv(g1,g2){\
document.open();\
dw(\"<pre>#Copy/paste as txt file:\");\
"));
        pl=fill_tcp_data_p(buf,pl,PSTR("dw(\"#date;"));
        pl=fill_tcp_data(buf,pl,label[0]);
        pl=fill_tcp_data(buf,pl,";");
        pl=fill_tcp_data(buf,pl,label[1]);
        pl=fill_tcp_data(buf,pl,";");
        pl=fill_tcp_data_p(buf,pl,PSTR("\");\n"));
        pl=fill_tcp_data_p(buf,pl,PSTR("\
var i,ms,pt;\
ms=t.getTime();\
var l=new Date();\
for(i=0;i<g1.length;i++){\
pt=ms-((m+d*i)*60*1000);\
l.setTime(pt);\
dw(l.getFullYear()+\"-\"+pad(l.getMonth()+1)+\"-\"+pad(l.getDate())+\" \"+pad(l.getHours())+\":\"+pad(l.getMinutes())+\";\"+g1[i]+\";\"+g2[i]+\";\");\
}\
document.close();\
}\n\
"));
        return(pl);
}

// p2.js
uint16_t print_p2_js(void)
{
        // I am sorry that the javascript is a bit unreadable 
        // but we absolutely need to keep the amount of code
        // in bytes small
        uint16_t pl;
        pl=http200okjs(0);
        // gd = graphdata array
        // c = color
        // a = amplification factor
        // h = heading
        // m = minutes since last data, global variable
        // d = recording interval in min, global variable
        // t = new Date(), global variable
        //
        // pbar(w,c,t) w=width c=color t=time
        pl=fill_tcp_data_p(buf,pl,PSTR("\
function pbar(w,c,t){\
if(w<5){w=5;}\
dw(\"<p style=\\\"width:\"+w+\"px;background-color:#\"+c+\";margin:2px;border:1px #999 solid;white-space:pre;\\\">\"+t+\"</p>\");\
}\n\
function bpt(gd,c,a,h){\
dw(\"<h2>\"+h+\"</h2>\");\
var i,n,ms,pt;\
ms=t.getTime();\
var l=new Date();\
for(i=0;i<gd.length;i++){\
 pt=ms-((m+d*i)*60*1000);\
 l.setTime(pt);\
 n=parseInt(gd[i]);\
 pbar((a*n+120),c,pad(l.getDate())+\"-\"+pad(l.getHours())+\":\"+pad(l.getMinutes())+\"=\"+n);\
}\n\
dw(\"<br>TZ diff GMT: \"+t.getTimezoneOffset()/60+\"h. Format: day-hh:mm=val<br>\");\
}\n\
"));
        return(pl);
}

// sensor = 0 or 1
uint16_t print_webpage_graph_array(uint8_t *buf,uint8_t sensor)
{
        int8_t i;
        uint8_t nptr;
        uint16_t pl;
        pl=http200okjs(1);
        nptr=gTemphistnextptr; // gTemphistnextptr may change so we make a snapshot here
        i=nptr;
        pl=fill_tcp_data_p(buf,pl,PSTR("gdat"));
        pl=fill_tcp_data_int(buf,pl,sensor);
        pl=fill_tcp_data_p(buf,pl,PSTR("=Array("));
        if (!eeprom_is_ready()){
                // don't sit and wait 
                pl=fill_tcp_data_p(buf,pl,PSTR("0);\n"));
                return(pl);
        }
        while(1){
                if (i!=nptr){ // not first time
                        pl=fill_tcp_data_p(buf,pl,PSTR(","));
                }
                i--;
                if (i<0){
                        i=TEMPHIST_BUFFER_SIZE-1;
                }
#ifdef GRAPH_IN_F
                pl=fill_tcp_data_int(buf,pl,c2f(read_temphist(i,sensor)*10));
#else
                pl=fill_tcp_data_int(buf,pl,read_temphist(i,sensor));
#endif
                if (i==nptr){
                        break; // end loop
                }
        }
        pl=fill_tcp_data_p(buf,pl,PSTR(");\n"));
        return(pl);
}

uint16_t print_webpage_graph(void)
{
        uint16_t pl;
        pl=http200ok(0);
        // global javascript variables:
        pl=fill_tcp_data_p(buf,pl,PSTR("<script>var t=new Date();var m="));
        pl=fill_tcp_data_int(buf,pl,gRecMin);
        pl=fill_tcp_data_p(buf,pl,PSTR(";var d="));
        pl=fill_tcp_data_int(buf,pl,rec_interval);
        pl=fill_tcp_data_p(buf,pl,PSTR("0;</script>\n"));
        pl=fill_tcp_data_p(buf,pl,PSTR("<script src=p1.js></script><script src=p2.js></script>\n"));
        pl=fill_tcp_data_p(buf,pl,PSTR("<script src=gdat0.js></script>"));
        pl=fill_tcp_data_p(buf,pl,PSTR("<script src=gdat1.js></script>"));
        pl=fill_tcp_data_p(buf,pl,PSTR("<a href=./>[home]</a> "));
        pl=fill_tcp_data_p(buf,pl,PSTR("<a href=javascript:scsv(gdat0,gdat1)>[CSV data]</a>\n"));
        pl=fill_tcp_data_p(buf,pl,PSTR("<script>\n"));
        pl=fill_tcp_data_p(buf,pl,PSTR("dw(\"<table border=1><tr><td>\");\n"));
        pl=fill_tcp_data_p(buf,pl,PSTR("bpt(gdat0,\"ac2\",2,\""));
        pl=fill_tcp_data(buf,pl,label[0]);
        pl=fill_tcp_data_p(buf,pl,PSTR("\");\n"));
        pl=fill_tcp_data_p(buf,pl,PSTR("dw(\"</td><td>\");\n"));
        // orange: eb0 green: ac2 blue: 78c orange2: f72
        pl=fill_tcp_data_p(buf,pl,PSTR("bpt(gdat1,\"f91\",2,\""));
        pl=fill_tcp_data(buf,pl,label[1]);
        pl=fill_tcp_data_p(buf,pl,PSTR("\");\n"));
        pl=fill_tcp_data_p(buf,pl,PSTR("dw(\"</td></tr></table>\");\n</script>\n<br><hr>\n"));
        return(pl);
}

uint16_t print_webpage_sensoronly(uint8_t *buf,uint8_t num,uint8_t farenh)
{
        uint16_t pl;
        pl=http200ok(1);
        pl=print_line_mobile_webpage(pl);
        if (num<gNsensors){
                if (farenh==0){
                        pl=fill_tcp_data_int(buf,pl,gTempdata[num]/10);
                        pl=fill_tcp_data_p(buf,pl,PSTR("."));
                        pl=fill_tcp_data_int(buf,pl,abs(gTempdata[num])%10);
                }else{
                        pl=fill_tcp_data_int(buf,pl,c2f(gTempdata[num]));
                }
                pl=fill_tcp_data_p(buf,pl,PSTR("\n"));
        }else{
                pl=fill_tcp_data_p(buf,pl,PSTR("err\n"));
        }
        return(pl);
}

#ifdef RSS_FEATURE
// call this function every time we draw a graph point to
// check if we should update the rss guid value.
// A change in the rss guid value forces a page refresh to the
// rss reader as it indicates that the "article is new".
void increment_rss_guid(void){
        int8_t i=0;
        int8_t refreshdone=0;
        int8_t old_F_temp[3]={0,0,0}; // max 3 sensors
        while(i<gNsensors && i<3){ 
                if (refreshdone==0 && c2f(gTempdata[i])!=old_F_temp[i]){
                        rss_guid++; // to modify guid in the rss page and force a refresh
                        refreshdone=1;
                }
                old_F_temp[i]=c2f(gTempdata[i]);
                i++;
        }
}

// rss syntax: http://www.rssboard.org/rss-specification
uint16_t print_rss_page(uint8_t *buf)
{
        uint16_t pl;
        int8_t i=0;
        pl=fill_tcp_data_p(buf,0,PSTR("HTTP/1.0 200 OK\r\nContent-Type: text/xml\r\n\r\n"));
        pl=fill_tcp_data_p(buf,pl,PSTR("<?xml version=\"1.0\"?>\n<rss version=\"2.0\">\n"));
        pl=fill_tcp_data_p(buf,pl,PSTR("<channel>\n<title>Temp. sensors</title>\n"));
        pl=fill_tcp_data_p(buf,pl,PSTR("<link>"));
        pl=fill_tcp_data(buf,pl,RSSBASEURL);
        pl=fill_tcp_data_p(buf,pl,PSTR("</link>\n"));
        pl=fill_tcp_data_p(buf,pl,PSTR("<description>Temp. RSS feed</description>\n"));
        // max 3 sensors otherwise this page might be bigger than the buf variable:
        while(i<gNsensors && i<3){ 
                pl=fill_tcp_data_p(buf,pl,PSTR("<item>\n<title>s"));
                pl=fill_tcp_data_int(buf,pl,i); // sensor number
                pl=fill_tcp_data_p(buf,pl,PSTR(": "));
                pl=fill_tcp_data_int(buf,pl,gTempdata[i]/10);
                pl=fill_tcp_data_p(buf,pl,PSTR("."));
                pl=fill_tcp_data_int(buf,pl,abs(gTempdata[i])%10);
                pl=fill_tcp_data_p(buf,pl,PSTR("'C ["));
                pl=fill_tcp_data_int(buf,pl,c2f(gTempdata[i]));
                pl=fill_tcp_data_p(buf,pl,PSTR("'F]</title>\n"));
                pl=fill_tcp_data_p(buf,pl,PSTR("<link>"));
                pl=fill_tcp_data(buf,pl,RSSBASEURL);
                pl=fill_tcp_data(buf,pl,"/?ts=");
                pl=fill_tcp_data_int(buf,pl,i); // sensor number
                pl=fill_tcp_data_p(buf,pl,PSTR("</link>\n"));
                // readers use guid to determine if the artice
                // is new and needs to be refreshed. Therefore
                // we need to change guid in order to show current values
                // guid is just a string. We must however only change
                // it if the temperature really changed. We use
                // the units 'F to do that.
                pl=fill_tcp_data_p(buf,pl,PSTR("<guid>"));
                pl=fill_tcp_data_int(buf,pl,myip[2]);
                pl=fill_tcp_data(buf,pl,"-");
                pl=fill_tcp_data_int(buf,pl,myip[3]);
                pl=fill_tcp_data(buf,pl,"-");
                pl=fill_tcp_data_int(buf,pl,i); //  sensor number
                pl=fill_tcp_data(buf,pl,"-");
                pl=fill_tcp_data_int(buf,pl,rss_guid); //  counter
                pl=fill_tcp_data_p(buf,pl,PSTR("</guid>\n"));
                pl=fill_tcp_data_p(buf,pl,PSTR("</item>\n"));
                i++;
        }
        pl=fill_tcp_data_p(buf,pl,PSTR("</channel>\n</rss>\n"));
        return(pl);
}
#endif // RSS_FEATURE

uint16_t print_webpage_main(uint8_t *buf)
{
        int8_t i=0;
        uint16_t pl;
        pl=http200ok(1);
        pl=print_line_mobile_webpage(pl);
#ifdef RSS_FEATURE
        pl=fill_tcp_data_p(buf,pl,PSTR("<link rel=alternate type=application/rss+xml href=/rss>\n"));
#endif // RSS_FEATURE
        pl=fill_tcp_data_p(buf,pl,PSTR("<a href=/sw>[switch]</a> <a href=./gp>[graph]</a>"));
        if (DEBUG_SENSORS==1){
                pl=fill_tcp_data_p(buf,pl,PSTR(" <a href=./?ds=1>[dbg sensor]</a>"));
        }
        pl=fill_tcp_data_p(buf,pl,PSTR(" <a href=/>[refresh]</a>\n<h2>Temperature</h2>"));
        pl=fill_tcp_data_p(buf,pl,PSTR("<pre>"));
        // max 3 sensors otherwise this page might be bigger than the buf variable:
        while(i<gNsensors && i<3){ 
                if (i<2){
                        pl=fill_tcp_data(buf,pl,label[i]);
                }else{
                        pl=fill_tcp_data_p(buf,pl,PSTR("sensor"));
                        pl=fill_tcp_data_int(buf,pl,i);
                }
                pl=fill_tcp_data_p(buf,pl,PSTR(": <a href=./?ts="));
                pl=fill_tcp_data_int(buf,pl,i);
                pl=fill_tcp_data_p(buf,pl,PSTR(">"));
                pl=fill_tcp_data_int(buf,pl,gTempdata[i]/10);
                pl=fill_tcp_data_p(buf,pl,PSTR("."));
                pl=fill_tcp_data_int(buf,pl,abs(gTempdata[i])%10);
                pl=fill_tcp_data_p(buf,pl,PSTR("</a>&deg;C [<a href=./?f=1&ts="));
                pl=fill_tcp_data_int(buf,pl,i);
                pl=fill_tcp_data_p(buf,pl,PSTR(">"));
                pl=fill_tcp_data_int(buf,pl,c2f(gTempdata[i]));
                pl=fill_tcp_data_p(buf,pl,PSTR("</a>&deg;F]\n"));
                i++;
        }
        if (gTemp_measurementstatus==1){
                pl=fill_tcp_data_p(buf,pl,PSTR("warning: sensor error\n"));
        }
/*
                pl=fill_tcp_data_p(buf,pl,PSTR("dbg time since startup: "));
                pl=fill_tcp_data_int(buf,pl,gRecMin-1);
                pl=fill_tcp_data_p(buf,pl,PSTR(":"));
                pl=fill_tcp_data_int(buf,pl,gSec);
*/
        pl=fill_tcp_data_p(buf,pl,PSTR("</pre>"));
#ifdef RSS_FEATURE
        pl=fill_tcp_data_p(buf,pl,PSTR("<p align=right><a href=/rss>[rss]</a></p>\n"));
#endif // RSS_FEATURE
        pl=fill_tcp_data_p(buf,pl,PSTR("<hr>&copy; tuxgraphics\n"));
        return(pl);
}

#if (DEBUG_SENSORS==1)
uint16_t print_webpage_sensordetails_dbg_login(uint8_t *buf)
{
        uint16_t plen;
        plen=http200ok(1);
        plen=fill_tcp_data_p(buf,plen,PSTR("<a href=/>[home]</a>\n"));
        plen=fill_tcp_data_p(buf,plen,PSTR("<h2>Sensor debug login</h2>\n<pre>"));
        plen=fill_tcp_data_p(buf,plen,PSTR("\n<form action=/ method=get>"));
        plen=fill_tcp_data_p(buf,plen,PSTR("<input type=hidden name=ds value=1>\n"));
        plen=fill_tcp_data_p(buf,plen,PSTR("passw: <input type=password size=10 name=pw>"));
        plen=fill_tcp_data_p(buf,plen,PSTR("<input type=submit value=\"login\"></form>\n"));
        plen=fill_tcp_data_p(buf,plen,PSTR("\n</pre><hr>tuxgraphics"));
        return(plen);
}
// debug information, read sensor data at the moment when
// showing this page
uint16_t print_webpage_sensordetails_dbg(uint8_t *buf)
{
        uint16_t plen;
        uint8_t subzero, cel, cel_frac_bits;
        int8_t i,j;
        uint8_t sp[DS18X20_SP_SIZE];
        plen=http200ok(1);
        plen=fill_tcp_data_p(buf,plen,PSTR("<a href=./>[home]</a> <a href=./?ds=1&pw="));
        urlencode(password,gStrbuf);
        plen=fill_tcp_data(buf,plen,gStrbuf);
        plen=fill_tcp_data_p(buf,plen,PSTR(">[refresh]</a>\n"));
        plen=fill_tcp_data_p(buf,plen,PSTR("<h2>1wire sensors</h2>"));
        plen=fill_tcp_data_p(buf,plen,PSTR("<pre>"));
        if (gNsensors==-1){
                plen=fill_tcp_data_p(buf,plen,PSTR("no sensor found"));
                goto EOSENSOR;
        }
        if (gNsensors==-2){
                plen=fill_tcp_data_p(buf,plen,PSTR("OW_DATA_ERR"));
                goto EOSENSOR;
        }
        if (gNsensors>0){
                plen=fill_tcp_data_p(buf,plen,PSTR("Num. of sens:"));
                plen=fill_tcp_data_int(buf,plen,gNsensors);
        }
        plen=fill_tcp_data_p(buf,plen,PSTR("\n"));
        for (i=0; i<gNsensors; i++) {
                plen=fill_tcp_data_p(buf,plen,PSTR("sensor:"));
                plen=fill_tcp_data_int(buf,plen,i);
                plen=fill_tcp_data_p(buf,plen,PSTR(" "));
                plen=DS18X20_show_id_print_buf( &gSensorIDs[i][0], plen,buf);
                plen=fill_tcp_data_p(buf,plen,PSTR("\n"));
        }
        if (gTemp_measurementstatus==1){
                plen=fill_tcp_data_p(buf,plen,PSTR("DS18X20_start_meas failed!\n"));
        }
        // The measurement is started from the main program. This will just read the
        // Data off the scratch pad of the sensors.
        for ( i=0; i<gNsensors; i++ ) {
                plen=fill_tcp_data_p(buf,plen,PSTR("sensor:"));
                plen=fill_tcp_data_int(buf,plen,i);
                plen=fill_tcp_data_p(buf,plen,PSTR(" "));
                // show the raw scratchpad (temperature info is stored there):
                if (DS18X20_read_scratchpad(&gSensorIDs[i][0],sp)==DS18X20_OK){
                        plen=fill_tcp_data_p(buf,plen,PSTR("SP:"));
                        for ( j=0 ; j< DS18X20_SP_SIZE; j++ ){
                                if (j>0)plen=fill_tcp_data_p(buf,plen,PSTR(":"));
                                plen=fill_tcp_data_uint(buf,plen,sp[j]);
                        }
                }
                plen=fill_tcp_data_p(buf,plen,PSTR(" "));
                if ( DS18X20_read_meas( &gSensorIDs[i][0], &subzero,
                                &cel, &cel_frac_bits) == DS18X20_OK ) {
                        // display temp
                        if (subzero){
                                plen=fill_tcp_data_p(buf,plen,PSTR("-"));
                        }else{
                                plen=fill_tcp_data_p(buf,plen,PSTR("+"));
                        }
                        plen=fill_tcp_data_int(buf,plen,cel);
                        plen=fill_tcp_data_p(buf,plen,PSTR("."));
                        plen=fill_tcp_data_int(buf,plen,DS18X20_frac_bits_decimal(cel_frac_bits));
                        plen=fill_tcp_data_p(buf,plen,PSTR("'C"));
                } else {
                        plen=fill_tcp_data_p(buf,plen,PSTR("ERR: CRC/connection"));
                }
                plen=fill_tcp_data_p(buf,plen,PSTR("\n"));
        }
EOSENSOR:

        plen=fill_tcp_data_p(buf,plen,PSTR("\nSensors are searched at power-on only.\n<hr>tuxgraphics"));
        return(plen);
}
#endif

// takes a string of the form command/Number and analyse it (e.g "?sw=pd7&a=1 HTTP/1.1")
// The first char of the url ('/') is already removed.
int8_t analyse_get_url(char *str)
{
        int8_t i=0;
        int8_t temp;
        if (str[0]==' '){
                return(1); // end of url, main page
        }
        if (strncmp("favicon.ico",str,11)==0){
                gPlen=fill_tcp_data_p(buf,0,PSTR("HTTP/1.0 301 Moved Permanently\r\nLocation: "));
                gPlen=fill_tcp_data_p(buf,gPlen,PSTR("http://tuxgraphics.org/ico/therm.ico"));
                gPlen=fill_tcp_data_p(buf,gPlen,PSTR("\r\n\r\n"));
                return(10);
        }
        // --------
        if (find_key_val(str,gStrbuf,STR_BUFFER_SIZE,"sc")){
                if (gStrbuf[0]=='1'){
                        i=1;
                }
                if (find_key_val(str,gStrbuf,STR_BUFFER_SIZE,"pw")){
                        urldecode(gStrbuf);
                        if (verify_password(gStrbuf)){
                                if (i){
                                        PORTD|= (1<<PORTD7);// transistor on
                                        if (find_key_val(str,gStrbuf,STR_BUFFER_SIZE,"tm")){
                                                urldecode(gStrbuf);
                                                gRelay_timeout_min=atoi(gStrbuf);
                                        }

                                }else{
                                        PORTD &= ~(1<<PORTD7);// transistor off
                                        gRelay_timeout_min=0;
                                }
                                return(2);
                        }
                }
                return(-1);
        }
        //
        if (strncmp("sw ",str,3)==0){
                return(3);
        }
        if (find_key_val(str,gStrbuf,STR_BUFFER_SIZE,"ts")){
                gStrbuf[1]='\0';
                temp=atoi(gStrbuf);
                i=0;
                if (find_key_val(str,gStrbuf,STR_BUFFER_SIZE,"f")){
                        i=1;
                }
                // gStrbuf[0] contains the number of the sensor
                gPlen=print_webpage_sensoronly(buf,temp,i);
                return(10);
        }
#ifdef RSS_FEATURE
        if (strncmp("rss",str,3)==0){
                gPlen=print_rss_page(buf);
                return(10);
        }
#endif // RSS_FEATURE
        if (strncmp("p1.js",str,5)==0){
                gPlen=print_p1_js();
                return(10);
        }
        if (strncmp("p2.js",str,5)==0){
                gPlen=print_p2_js();
                return(10);
        }
#if (DEBUG_SENSORS==1)
        if (find_key_val(str,gStrbuf,STR_BUFFER_SIZE,"ds")){
                if (find_key_val(str,gStrbuf,STR_BUFFER_SIZE,"pw")){
                        urldecode(gStrbuf);
                        if (verify_password(gStrbuf)){
                                return(6);
                        }
                        return(-1);
                }
                return(7);
        }
#endif
        if (strncmp("gp",str,2)==0){
                gPlen=print_webpage_graph();
                return(10);
        }
        if (strncmp("gdat0.js",str,8)==0){
                gPlen=print_webpage_graph_array(buf,0);
                return(10);
        }
        if (strncmp("gdat1.js",str,8)==0){
                gPlen=print_webpage_graph_array(buf,1);
                return(10);
        }
        return(0);
}

// called when TCNT2==OCR2A
// that is in 50Hz intervals
ISR(TIMER2_COMPA_vect){
        cnt2step++;
        if (cnt2step>49){ 
                gSec++;
                gMeasurementTimer++;
                cnt2step=0;
        }
}

/* setup timer T2 as an interrupt generating time base.
* You must call once sei() in the main program */
void init_cnt2(void)
{
        cnt2step=0;
        PRR&=~(1<<PRTIM2); // write power reduction register to zero
        TIMSK2=(1<<OCIE2A); // compare match on OCR2A
        TCNT2=0;  // init counter
        OCR2A=244; // value to compare against
        TCCR2A=(1<<WGM21); // do not change any output pin, clear at compare match
        // divide clock by 1024: 12.5MHz/1024=12207.0313 Hz
        TCCR2B=(1<<CS22)|(1<<CS21)|(1<<CS20); // clock divider, start counter
        // OCR2A=244 is a division factor of 245
        // 12207.0313 / 245= 49.82461
}

// called every minute:
void store_graph_dat(void){
        static uint8_t gCompSecCnt=0; // how often we corrected the clock
        int8_t i,j;
        if (gRecMin==0){
                j=gNsensors;
                if (j>3){
                        j=3;
                }
                for ( i=0; i<j; i++ ) {
                        // round up to full 'C:
                        if (gTempdata[i]%10>5){
                                store_temphist(1+gTempdata[i]/10,gTemphistnextptr,i);
                        }else{
                                store_temphist(gTempdata[i]/10,gTemphistnextptr,i);
                        }
                }
                gTemphistnextptr++;
                gTemphistnextptr=gTemphistnextptr%TEMPHIST_BUFFER_SIZE;
        }
        // compensate after 17 intervals of 5 min
        if (gCompSecCnt>16){ // executed 1min after gCompSecCnt==17
                gSec+=1;
                gCompSecCnt=0;
        }
        // We calibrate/compensate the clock here a bit.
        // Our clock is a bit behind by 50/49.82461=1.00352
        // We need to compenstate by 3600*1.00352-3600sec=12.672sec every
        // hour or 1sec every minute.
        // The gSec is zero or max one when this function is called.
        if (gRecMin%5==0){
                gSec+=1;
                gCompSecCnt++;
        }
        // we have a remaining compensation of 16.12800sec per day
        // after the above. There are 288 5min units in 24h: 288/16.128=17.85
        // In the next minute we will fix that (see code at "compensate after 17 intervals")
        // - - - -
        gRecMin++;
        if (gRecMin==((uint16_t)rec_interval*10)){
#ifdef RSS_FEATURE
                increment_rss_guid(); // update rss guid value to force a page refresh if there was a temperature change.
#endif // RSS_FEATURE
                gRecMin=0; 
        }
}

int main(void){

        uint16_t dat_p;
        int8_t i,j,firstsample=1;
        static int8_t state=0;
        // set the clock speed to "no pre-scaler" (8MHz with internal osc or 
        // full external speed)
        // set the clock prescaler. First write CLKPCE to enable setting of clock the
        // next four instructions.
        CLKPR=(1<<CLKPCE); // change enable
        CLKPR=0; // "no pre-scaler"
        _delay_loop_1(0); // 60us

        /*initialize enc28j60*/
        enc28j60Init(mymac);
        enc28j60clkout(2); // change clkout from 6.25MHz to 12.5MHz
        _delay_loop_1(0); // 60us
        //
        // time keeping
        init_cnt2();
        sei();
        // LED
        DDRB|= (1<<DDB1); // enable PB1, LED as output
        LEDOFF;

        // the transistor on PD7 (relay)
        DDRD|= (1<<DDD7);
        PORTD &= ~(1<<PORTD7);// transistor off
        /* Magjack leds configuration, see enc28j60 datasheet, page 11 */
        // LEDB=yellow LEDA=green
        //
        // 0x476 is PHLCON LEDA=links status, LEDB=receive/transmit
        // enc28j60PhyWrite(PHLCON,0b0000 0100 0111 01 10);
        enc28j60PhyWrite(PHLCON,0x476);

        //init the ethernet/ip layer:
        init_ip_arp_udp_tcp(mymac,myip,mywwwport);

        // initialize:
        i=0;
        while(i<TEMPHIST_BUFFER_SIZE){
                store_temphist(0,i,0);
                store_temphist(0,i,1);
                i++;
        }
        gNsensors=search_sensors();
        // initialize gTempdata in case somebody reads the web page immediately after reboot
        i=0;
        while(i<MAXSENSORS){
                gTempdata[i]=0;
                i++;
        }
        if (gNsensors>0){
                start_temp_meas();
        }

        gMeasurementTimer=0; // set again to zero before starting while loop
        while(1){
                // handle ping and wait for a tcp packet:
                gPlen=enc28j60PacketReceive(BUFFER_SIZE, buf);
                dat_p=packetloop_icmp_tcp(buf,gPlen);

                // dat_p will ne unequal to zero if there is a valid http get 
                if(dat_p==0){
                        // we need at least 750ms time between 
                        // start of measurement and reading
                        if (gMeasurementTimer==2 && state==0 && eeprom_is_ready()){ 
                                LEDON;
                                read_temp_meas();
                                state=1;
                                if (firstsample==1){
                                        firstsample=0;
                                        store_graph_dat();
                                }
                        }
                        if (gMeasurementTimer==3 && state==1){ 
                                LEDOFF;
                                start_temp_meas();
                                state=2;
                        }
                        if (gMeasurementTimer==5){ // every 5 sec new measurement:
                                gMeasurementTimer=0;
                                state=0;
                        }
                        if (gSec>59){
                                gSec=0;
                                if (gRelay_timeout_min>0){
                                        gRelay_timeout_min--;
                                        if (gRelay_timeout_min==0){
                                                PORTD &= ~(1<<PORTD7);// transistor off
                                        }
                                }
                                store_graph_dat();
                        }
                        continue;
                }
                if (strncmp("GET ",(char *)&(buf[dat_p]),4)!=0){
                        // head, post and other methods:
                        //
                        // for possible status codes see:
                        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
                        gPlen=fill_tcp_data_p(buf,0,PSTR("HTTP/1.0 200 OK\r\nContent-Type: text/html\r\n\r\n<h1>200 OK</h1>"));
                        goto SENDTCP;
                }
                // Cut the size for security reasons. If we are almost at the
                // end of the buffer then there is a zero but normally there is
                // a lot of room and we can cut down the processing time as
                // correct URLs should be short in our case:
                if ((dat_p+5+55) < BUFFER_SIZE){
                        buf[dat_p+5+55]='\0';
                }
                // analyse the url and do possible port changes:
                // move one char ahead:
                i=analyse_get_url((char *)&(buf[dat_p+5]));
                // for possible status codes see:
                // http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
                if (i==-1){
                        gPlen=fill_tcp_data_p(buf,0,PSTR("HTTP/1.0 401 Unauthorized\r\nContent-Type: text/html\r\n\r\n<h1>401 Unauthorized</h1><a href=/>continue</a>"));
                        goto SENDTCP;
                }
                if (i==0){
                        gPlen=fill_tcp_data_p(buf,0,PSTR("HTTP/1.0 404 Not Found\r\nContent-Type: text/html\r\n\r\n<h1>404 Not Found</h1>"));
                        goto SENDTCP;
                }
                if (i==2){
                        gPlen=print_webpage_relay_ok();
                        goto SENDTCP;
                }
                if (i==3){
                        j=0;
                        // state of the transistor/relay
                        if (PORTD&(1<<PORTD7)){
                                j=1;
                        }
                        gPlen=print_webpage_relay(j);
                        goto SENDTCP;
                }
#if (DEBUG_SENSORS==1)
                if (i==6){
                        gPlen=print_webpage_sensordetails_dbg(buf);
                        goto SENDTCP;
                }
                if (i==7){
                        gPlen=print_webpage_sensordetails_dbg_login(buf);
                        goto SENDTCP;
                }
#endif
                if (i==10){
                        goto SENDTCP;
                }
                // just display the status:
                // normally i==1
                gPlen=print_webpage_main(buf);
                //
SENDTCP:
                www_server_reply(buf,gPlen); // send web page data
                // tcp port www end
                //
        }
        return (0);
}
