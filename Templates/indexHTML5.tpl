$(!M.T application/prs.wtz.webnail-template)<!DOCTYPE HTML>
<HTML>
<HEAD>
<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
<META http-equiv="Content-Style-Type" content="text/css">
<META http-equiv="Content-Script-Type" content="text/javascript">
<SCRIPT type="text/javascript" charset="UTF-8" src="controls/params.js"></SCRIPT>
<SCRIPT type="text/javascript" charset="UTF-8" src="controls/slideshow1.js"></SCRIPT>
<TITLE>$(windowTitle)</TITLE>
$(head)</HEAD>
<BODY  style="background-color: lightgray"
       onload="configure()" id="body" class="webnailIndexHTML">
$(audio)$(header)<TABLE id="imagesTable">
<TR>
<TD valign="top">
  <TABLE border=1 frame="border" id="thumbnailTable">
    <TR><TD>
	<IFRAME marginwidth=$(marginw) marginheight=$(marginh) width=$(tWidth) height=$(tHeight)
		frameborder=0
		name ="thumbnails" src="thumbnails/index.html">
	</IFRAME>
    </TD></TR>
  </TABLE>
</TD>
<TD>
  <TABLE width=545 id="imageTable">
    <NOSCRIPT>
      <TR>
	<TD align="center">
	  <TABLE>
	    <TR>
	      <TD style="color: red" id="noJavascriptMsg">
		(Javascript required for nagivation controls)
	      </TD>
	    </TR>
	  </TABLE>
	</TD>
      </TR>
    </NOSCRIPT>
    <SCRIPT type="text/javascript">
      createControls(); 
    </Script>
    <TR>
      <TD>
	<SCRIPT type="text/javascript">
	  createImageIframe();
	</SCRIPT>
	<NOSCRIPT>
	<IFRAME marginwidth=$(marginw) marginheight=$(marginh) width=$(iWidth) height=$(iHeight)
		frameborder=0
		src="controls/initial.html" name="images">
	</IFRAME>
	</NOSCRIPT>
      </TD>
    </TR>
  </TABLE>
</TD>
</TR>
</TABLE>
<TABLE marginwidth=$(marginw) width=$(tdWidth) id="titleDescrTable">
  <TR>
    <TD width=$(tWidth)></TD>
    <TD align="center" colspan=2>
      <span style="font-size: larger; font-weight: bold" id="title">
	$(title)
      </span>
    </TD>
  </TR>
  <TR>
    <TD width=$(tWidth)><IMG src="controls/strut.gif" width=2 height="20"></TD>
    <TD align="center" valign="top" id="descr">$(description)</TD>
  </TR>
</TABLE>
$(trailer)<SCRIPT type="text/javascript" charset="UTF-8" src="controls/slideshow2.js"></SCRIPT>
$(afterScript)</BODY>
</HTML>
