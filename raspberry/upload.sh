#!/bin/bash

cd /home/pi/.fwsmaster/output
rm -rf tmp
mkdir tmp
cd tmp
testing=1
while [ $testing -eq 1 ]
do
        rm *.png
        rm *.txt
        cp ../*.png .
        cp ../*.txt .
        result=`stat -c %s *.* | grep '^0$'`
        if [ "$?" -ne "0" ];
        then
                testing=0
        fi
done

ftp -n -v mfc-ikarus-ohlsdorf.at << cmd
user user password
bin
lcd /home/pi/.fwsmaster/output/tmp
put Flugplatz_AussentemperaturFlugplatz_Windspeed1.png
put Flugplatz_Innentemperatur5.png
put Flugplatz_Windrichtung_c0.png
put Flugplatz_Windrichtung_h1.png
put result.txt
lcd /home/pi
get remote.sh
del remote.sh
quit
cmd

cd /home/pi
if [ -f remote.sh ]
then
        chmod a+x remote.sh
        source remote.sh
        rm -f remote.sh
fi
