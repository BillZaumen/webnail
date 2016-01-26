@echo off
if -%1-==-- (
    javaw -jar WEBNAIL_DIR/webnail.jar
) else (
    if %1==--gui  (
            if not -%2-==-- (
                javaw -jar WEBNAIL_DIR/webnail.jar %*
	    ) else (
	        java -jar WEBNAIL_DIR/webnail.jar %*
            )
    ) else (
        java -jar WEBNAIL_DIR/webnail.jar %*
    )
)
