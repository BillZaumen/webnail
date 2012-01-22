<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" 
	  "http://www.w3.org/TR/html4/strict.dtd">
<HTML>
<HEAD>
<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
<META http-equiv="Content-Script-Type" content="text/javascript">
<TITLE>Error</TITLE>
</HEAD>
<BODY  style="background-color: lightgray">
<P>
  An error occurred:
</P>
<ul>
  <li>Error code: ${pageContext.errorData.statusCode}
  <li>Request URI: ${pageContext.errorData.requestURI}
</ul>
<BODY>
</BODY>
</HTML>
