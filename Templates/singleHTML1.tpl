$(!M.T application/prs.wtz.webnail-template)<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" 
	  "http://www.w3.org/TR/html4/strict.dtd">
<HTML>
<HEAD>
<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
<META http-equiv="Content-Style-Type" content="text/css">
<TITLE>$(windowTitle)</TITLE>
$(head)</HEAD>
<BODY  style="background-color: white" id="body">
$(header)
<TABLE frame="border" border="1" rules="all" cellpadding="5"
       style="border-style: inset; border-color: #909090" id="imagesTable">
  <TR>
    $(repeatRows:endRows)
    <TD ALIGN="center"><!-- A href="$(hrefURL)" target="$(hrefTarget)" -->
	<IMG SRC="$(fsImageURL)" TITLE="$(title)"><!-- /A -->
    </TD>
    $(newTableRow)$(endRows)
  </TR>
</TABLE>
$(trailer)$(afterScript)
</BODY>
