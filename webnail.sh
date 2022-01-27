#!/bin/sh
java -p BZDEV_DIR --add-modules org.bzdev.desktop \
     --add-modules org.bzdev.ejws \
     -classpath JARDIRECTORY/webnail-VERSION.jar webnail/Webnail "$@"
