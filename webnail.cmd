@echo off
if -%1-==-- (
    javaw -p BZDEV_DIR --add-modules org.bzdev.desktop -classpath WEBNAIL_DIR/webnail.jar webnail/Webnail
) else (
    if %1==--gui  (
            if not -%2-==-- (
                javaw -p BZDEV_DIR --add-modules org.bzdev.desktop --add-modules org.bzdev.ejws -classpath WEBNAIL_DIR/webnail.jar webnail/Webnail %*
	    ) else (
	        java -p BZDEV_DIR --add-modules org.bzdev.desktop --add-modules org.bzdev.ejws -classpath WEBNAIL_DIR/webnail.jar webnail/Webnail %*
            )
    ) else (
        java -p BZDEV_DIR --add-modules org.bzdev.desktop --add-modules org.bzdev.ejws -classpath WEBNAIL_DIR/webnail.jar webnail/Webnail %*
    )
)
