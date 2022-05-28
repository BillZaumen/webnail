#!/bin/sh
java -p BZDEV_DIR --add-modules org.bzdev.ejws,org.bzdev.desktop \
     -classpath JARDIRECTORY/webnail-VERSION.jar \
     webnail/Webnail "$@"
