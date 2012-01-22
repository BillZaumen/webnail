#!/bin/sh
if [ -r /etc/webnail.conf ]
then
. /etc/webnail.conf
fi
exec ${java:-java} -jar JARDIRECTORY/webnail-VERSION.jar "$@"
