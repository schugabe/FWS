#!/bin/bash
/usr/bin/java -jar /home/pi/fws_master_filter.jar > /dev/null

#sendemail -f fws@v-i-p.tv -t klein@v-i-p.tv johannes@wordproject.net -u 'FWS crash report' -m 'Leider crash!' -a ~/.fwsmaster/fws_master0.log -s mail.v-i-p.tv -xu fws@v-i-p.tv -xp zgachgfrogt
