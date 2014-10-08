#! /bin/sh
### BEGIN INIT INFO
# Provides:          fwsMaster
# Required-Start:    $remote_fs $syslog $time $network $named 
# Required-Stop:     $remote_fs $syslog $time $network $named 
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: starts/stops weater station master
# Description:       ...
### END INIT INFO
# Author: Johannes Kasberger

# Setup variables
EXEC=/usr/bin/jsvc
JAVA_HOME=/opt/java/jdk1.7.0_21
CLASS_PATH="/home/pi/fws.jar"
CLASS=fws_master.DaemonLoader
USER=pi
PID=/tmp/fws.pid

do_exec()
{
 $EXEC -home "$JAVA_HOME" -cp $CLASS_PATH  -user $USER -pidfile $PID $1 $CLASS
}

case "$1" in
    start)
        do_exec
            ;;
    stop)
        do_exec "-stop"
            ;;
    restart)
        if [ -f "$PID" ]; then
            do_exec "-stop"
            do_exec
        else
            echo "service not running, will do nothing"
            exit 1
        fi
            ;;
    *)
            echo "usage: fws {start|stop|restart}" >&2
            exit 3
            ;;
esac
