var controlStrings1 = [
 '<FORM action="javascript:void(0)">',
 '<TABLE width=500>',
 '<TR>',
 '<TD align="center">',
 '<INPUT TYPE="image" SRC="controls/fleft.gif"  id="fleft"',
 ' onclick="index = 0; updateLocations(\'.\')">',
 '</TD>',
 '<TD align="center">',
 '<INPUT TYPE="image" SRC="controls/left.gif" id="left"',
 ' onclick="index--; while (index &lt; 0) index++; updateLocations(\'.\');">',
 '</TD>',
 '<TD align="center">',
 '<A id="expand" href="controls/initImage.png" target="_blank"><IMG src="controls/expand.png"></A>',		       
 '</TD>',
 '<TD align="center">',
 '<INPUT TYPE="image" SRC="controls/right.gif" id="right"',
 ' onclick="index++; if (index &gt; imageArray.length - 1) index--; updateLocations(\'.\');">',
 '</TD>',
 '<TD align="center">',
 '<INPUT TYPE="image" SRC="controls/fright.gif" id="fright"',
 ' onclick="index = imageArray.length-1; if (index &lt; 0) index = 0; updateLocations(\'.\');">',
 '</TD>',
 '<TD align="center">',
 '<INPUT TYPE="button" value="slideshow" id="slideshow"',
 ' onclick="runSlideshow();">',
 '</TD>'
 ];
var controlStrings2 = ['<TD>',
 '<INPUT TYPE="checkbox" id="fullscreen" value="Full Screen"',
 ' name="FullScreen"> Full Screen',
 '</TD>',
 ];

var controlStrings3 = [
 '<TD>',
 '<INPUT TYPE="checkbox" id="loop" value="Loop" name="Loop"> Loop',
 '</TD>',
 '</TR>',
 '</TABLE>',
 '</FORM>'
 ];

var configured = false;

// We do it this way so that the controls do not show up unless
// Javascript is turned on - they won't work if Javascript is off.
// One control, indicating if a (nearly) fullscreen window is wanted,
// appears only when hasAllImages is set to true.
function createControls() {
    var i;
    for (i = 0; i < controlStrings1.length; i++) {
	document.write(controlStrings1[i]);
    }
    if (hasAllImages) {
	for (i = 0; i < controlStrings2.length; i++) {
	    document.write(controlStrings2[i]);
	}
    }
    for (i = 0; i < controlStrings3.length; i++) {
	document.write(controlStrings3[i]);
    }
    if (configured == true) {
	window["tryToEnableSlideshow"]();
    }
}

var iframeStrings = 
    [
     '<IFRAME marginwidth=10 marginheight=10 width=540 height=540',
     ' frameborder=0 src="controls/medium.html" name="images">',
     '</IFRAME>'
     ];


function createImageIframe() {
    for (var i = 0; i < iframeStrings.length; i++) {
	document.write(iframeStrings[i]);
    }
}
