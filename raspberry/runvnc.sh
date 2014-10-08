#!/bin/sh
## Requires port forwarding of 5900 with putty
echo "Starting VNC"
echo "Don't forget the port forwarding of 5900 with putty"

x11vnc -localhost -display :0
