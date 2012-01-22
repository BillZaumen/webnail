var domMap;
if ((typeof domMap) == "undefined") {
    domMap = {}
}

var maxIndex = imageArray.length - 1;

var index = -1;
var firstTwo = true;

var slideshowWindow = null;

var updateSlideshowWindowID = 0;

var actualTime = 0;
var desiredTime = 0;

var defaultTitle = null;
var defaultDescr = null;

var loop = false;

var configured = false;

var finalSlide = false;
function onSlideFinished() {
    if (finalSlide) {
	doSlideshowEnding();
    }
}

function configure() {
    var button = window.document.getElementById("slideshow");
    button.disabled = true;
    var element = window.document.getElementById("title");
    defaultTitle = (element == null)? "": element.innerHTML;
    element = window.document.getElementById("descr");
    defaultDescr = (element == null)? "": element.innerHTML;
    index = 0;
    updateLocations(".");
    tryToEnableSlideshow();
    configured = true;
}

function updateDOMAux(win, minCallMode, maxCallMode) {
    for (var key in domMap) {
	var mode = domMap[key].mode;
	var callMode = domMap[key].callMode;
	// if (callMode > 2) continue;
	if (callMode < minCallMode || callMode > maxCallMode) continue;
	var entry = imageArray[index][key];
	var prop;
	var method;
	var funct;
	if (mode == 0) {
	    var ids = domMap[key].ids;
	    for (var ind in ids) {
		var id = ids[ind];
		element = win.document.getElementById(id);
		if (element != null) {
		    if (((typeof entry) != "undefined") 
			&& (entry != null)) {
			if (callMode == 0 || callMode == 1 ||
			    callMode == 3 || callMode == 4) {
			    prop = domMap[key].prop;
			    element[prop] = entry;
			}
		    } else {
			if (callMode == 0 || callMode == 2 ||
			    callMode == 3 || callMode == 4) {
			    prop = domMap[key].prop;
			    element[prop] = domMap[key].defaultValue;
			}
		    }
		}
	    }
	} else if (mode == 1) {
	    var ids = domMap[key].ids;
	    for (var ind in ids) {
		var id = ids[ind];
		element = win.document.getElementById(id);
		if (element != null) {
		    if (((typeof entry) != "undefined") 
			&& (entry != null)) {
			if (callMode == 0 || callMode == 1 ||
			    callMode == 3 || callMode == 4) {
			    method = domMap[key].method;
			    element[method]();
			}
		    } else {
			if (callMode == 0 || callMode == 2 ||
			    callMode == 3 || callMode == 4) {
			    method = domMap[key].method;
			    element[method]();
			}
		    }
		}
	    }
	} else if (mode == 2) {
	    var ids = domMap[key].ids;
	    for (var ind in ids) {
		var id = ids[ind];
		element = win.document.getElementById(id);
		if (element != null) {
		    if (((typeof entry) != "undefined") 
			&& (entry != null)) {
			if (callMode == 0 || callMode == 1 ||
			    callMode == 3 || callMode == 4) {
			    method = domMap[key].method;
			    element[method](entry);
			}
		    } else {
			if (callMode == 0 || callMode == 2 ||
			    callMode == 3 || callMode == 4) {
			    method = domMap[key].method;
			    element[method](domMap[key].defaultValue);
			}
		    }
		}
	    }
	} else if (mode == 3) {
	    if (element != null) {
		if (((typeof entry) != "undefined") && (entry != null)) {
		    if (callMode == 0 || callMode == 1 ||
			callMode == 3 || callMode == 4) {
			funct = win[domMap[key].funct];
			funct(entry);
		    }
		} else {
		    if (callMode == 0 || callMode == 2 ||
			callMode == 3 || callMode == 4) {
			funct = win[domMap[key].funct];
			funct(domMap[key].defaultValue);
		    }
		}
	    }
	} 
    }
}

function updateDOM(win) {
    if (index < 0 || index >= imageArray.length) return;
    imageEntryReference.entry = imageArray[index];
    var element = win.document.getElementById("title");
    if (element != null) {
	if (((typeof imageArray[index].title) != "undefined")
	    && (imageArray[index].title != null)) {
	    element.innerHTML = imageArray[index].title;
	} else {
	    element.innerHTML = defaultTitle;
	}
    }
    element = win.document.getElementById("descr");
    if (element != null) {
	if (((typeof imageArray[index].descr) != "undefined")
	    && (imageArray[index].descr != null)) {
	    element.innerHTML = imageArray[index].descr;
	} else {
	    element.innerHTML = defaultDescr;
	}
    }
    // we assume the toplevel index.html file contains a BODY element
    // whose ID is "body" and whose class contains the class name
    // "webnailIndexHTML". The remaining DOM updates apply only to the
    // toplevel index.html page.
    element = win.document.getElementById("body");
    if (element != null &&
	element.className.search(/(^|\s)webnailIndexHTML($|\s)/) != -1) {
	element = win.document.getElementById("expand");
	if (((typeof imageArray[index].hrefTarget) != "undefined")
	    && (imageArray[index].hrefTarget != null)) {
	    element.target = imageArray[index].hrefTarget;
	} else {
	    element.target = "_blank";
	}
	if (((typeof imageArray[index].highImageURL) != "undefined")
	    && (imageArray[index].highImageURL != null)) {
	    if (((typeof imageArray[index].fsImageURL) != "undefined")
		&& (imageArray[index].fsImageURL != null)
		&& ((typeof imageArray[index].hrefURL) != "undefined")
		&& (imageArray[index].hrefURL != null)) {
		if (imageArray[index].highImageURL ==
		    imageArray[index].hrefURL) {
		    element.href = imageArray[index].fsImageURL;
		} else {
		    element.href = imageArray[index].hrefURL;
		}
	    } else {
		element.href = "controls/initImage.png";
	    }
	} else {
	    if (((typeof imageArray[index].fsImageURL) != "undefined")
		&& (imageArray[index].fsImageURL != null)) {
		element.href = imageArray[index].fsImageURL;
	    } else {
		element.href = "controls/initImage.png";
	    }
	}
	updateDOMAux(win, 0, 2);
    }
}

if (minImageTime < 0) minImageTime = 0;

function getInnerWidth(win) {
    var value1 = 0;
    var value2 = 0;
    if (((typeof win.innerWidth) != "undefined") &&
	(win.innerWidth != null || win.innerWidth != undefined)) {
	return win.innerWidth;
    } else {
	if ((typeof win.document.documentElement) != "undefined" 
	    && win.document.documentElement != null
	    && win.document.documentElement != undefined
	    && (typeof win.document.documentElement.clientWidth) != "undefined"
	    && win.document.documentElement.clientWidth != undefined
	    && win.document.documentElement.clientWidth != null) {
	    value1 = win.document.documentElement.clientWidth;
	}
	if ((typeof win.document.body) != "undefined"
	    && win.document.body != null &&
	    win.document.body != undefined
	    && (typeof win.document.body.clientWidth) != "undefined"
	    && win.document.body.clientWidth != undefined
	    && win.document.body.clientHeight != null) {
	    value2 = win.document.body.clientWidth;
	}
	return (value1 > value2)? value1: value2;
    }
}

function getInnerHeight(win) {
    var value1 = 0;
    var value2 = 0;
    if (((typeof win.innerHeight) != "undefined") &&
	(win.innerHeight != null || win.innerHeight != undefined)) {
	return win.innerHeight;
    } else {
	if ((typeof win.document.documentElement) != "undefined"
	    && win.document.documentElement != null &&
	       win.document.documentElement != undefined
	    && (typeof win.document.documentElement.clientHeight) != "undefined"
	    && win.document.documentElement.clientHeight != undefined
	    && win.document.documentElement.clientHeight != null) {
	    value1 = win.document.documentElement.clientHeight;
	}
	if ((typeof win.document.body) != "undefined"
	    && win.document.body != null &&
	    win.document.body != undefined
	    && (typeof win.document.body.clientHeight) != "undefined"
	    && win.document.body.clientHeight != undefined
	    && win.document.body.clientHeight != null) {
	    value2 = win.document.body.clientHeight;
	}
	return (value1 > value2)? value1: value2;
    }
}

var readyToDisplaySSW = false;
function updateSlideshowWindow() {
    if (hasAllImages && slideshowWindow != null && !slideshowWindow.closed
	&& readyToDisplaySSW) {

	// var colElement = slideshowWindow.document.getElementById("col");
	var imgElement = slideshowWindow.document.getElementById("img");
	var bodyElement = slideshowWindow.document.getElementById("body");
	var spacerElement = slideshowWindow.document.getElementById("spacer");

	if (bodyElement == null || bodyElement == undefined ||
	    imgElement == null || imgElement == undefined ||
	    spacerElement == null || spacerElement == undefined) {
	    updateSlideshowWindowID = setTimeout(updateSlideshowWindow, 100);
	} else {
	    updateSlideshowWindowID = 0;
	    var w = getInnerWidth(slideshowWindow) - wOffset;
	    var h = getInnerHeight(slideshowWindow) - hOffset;
	    w = Math.round((w * wPercent)/100);
	    h = Math.round((h * hPercent)/100);
	    /*
	    if (((typeof imageArray[index]) == "undefined")
		|| (imageArray[index] == undefined) 
		|| imageArray[index] == null) {
		alert("imageArray[" + index +"] not defined");
	    }
	    */
	    var xscale = w / imageArray[index].width;
	    var yscale = h / imageArray[index].height;
	    var scale = (xscale < yscale)? xscale: yscale;
	    if (scale == 0) {scale = 1;}

	    var iw = Math.floor(imageArray[index].width * scale);
	    var ih = Math.floor(imageArray[index].height * scale);

	    spacerElement.width = iw;
	    spacerElement.height = Math.floor((h - ih)/2);
	    
		// imgElement.src = "../high/" + array[index] + ".jpg"

		/*
		  imgElement.src = "../" + highResDir + "/" + imageArray[index].name 
		+ "." + imageArray[index].ext ;
		*/
	    imgElement.src = imageArray[index].highImageURL;
	    imgElement.width = iw;
	    imgElement.height = ih;
	    updateDOM(slideshowWindow);
	}
    } else {
	updateSlideshowWindowID = 0;
    }
}

function updateWindow(ind) {
    index = ind;
    /*
    var tindex = index - 2;
    if (tindex < 0) tindex = 0;
    // called from the thumbnails directory
    window.frames["thumbnails"].location 
	= "index.html#" + tindex;
    updateDOM(window);
    if (slideshowWindow != null) updateDOM(slideshowWindow);
    */
    updateLocations("..");
}

function ensureImageFrameLocation(cdir) {
    var href = window.frames["images"].document.getElementById("href");
    if (href == null) {
	// recover - this can happen if javascript is toggled on
	// after everything is loaded
	window.frames["images"].location = cdir + "/controls/medium.html";
	href = window.frames["images"].document.getElementById("href");
    }
    return href;
}

var hrefCount = 0;
function updateLocations(cdir) {
    if (index < 0) {
	alert("index < 0 in updateLocations");
    }
    var href = ensureImageFrameLocation(cdir)
    if (href == null) {
	// recover - this can happen if javascript is toggled on
	// after everything is loaded
	// window.frames["images"].location = cdir + "/controls/medium.html";
	// href = window.frames["images"].document.getElementById("href");
	if (hrefCount++ > 0) {
	    alert("failed to load image frame");
	}
	if (href == null) return;
    }
    hrefCount = 0;
    var tindex = (index < maxIndex - 2)? (index - 2): (maxIndex - 4);
    if (tindex < 0) tindex = 0;
    /*
    window.frames["thumbnails"].location 
	=  cdir + "/" + "thumbnails/index.html#" + tindex;
    */
    //window.frames["thumbnails"].scrollTo(0, 128*tindex);
    // window.frames["thumbnails"].scrollTo(0, scrollSz * tindex + scrollOffset);
    
    var currentStrut = 
	window.frames["thumbnails"].document.getElementById("Strut" + tindex);
    var scrollIncr = currentStrut.offsetParent.offsetParent.offsetTop;
    scrollIncr = Math.floor(scrollIncr/2);
    var sloc = currentStrut.offsetParent.offsetTop + scrollIncr;
    window.frames["thumbnails"].scrollTo(0, sloc);
    //  alert(window.frames["thumbnails"].pageYOffset);

    // window.frames["images"].location = "medium/" + array[index] + ".html";
    try {
	/*
	window.frames["images"].location = "medium/" + imageArray[index].name
	    + ".html";
	*/
	if (href == null) {
	    alert("no href: location = " + window.frames["images"].location);
	}
	/*
	href.href = ".." + "/" +  highResDir + "/" 
	    + imageArray[index].name + "." + imageArray[index].ext;
	*/
	// href.href = imageArray[index].highImageURL;
	href.href = imageArray[index].hrefURL;
	href.target = imageArray[index].hrefTarget;
	var img = window.frames["images"].document.getElementById("img");
	if (img == null) alert("no img");
	img.src= ".." + "/" + "medium/" + imageArray[index].name + "."
	    + imageArray[index].ext;
	updateDOM(window);
    } catch (err2) {
	alert(err2);
    }
    readyToDisplaySSW = true;
    updateSlideshowWindow();
}

function escapeHandler(e) {
    if (slideshowWindow != null) {
	var e = slideshowWindow.event || e;
	var kc = e.charCode || e.keyCode;
	if (kc == 27) {
	    stopSlideshow();
	}
    }
}

function closeSlideshowWindow() {
    if (slideshowWindow != null) {
	if (!slideshowWindow.closed) {slideshowWindow.close();}
	slideshowWindow = null;
    }
    return;
}

var ready = true;
var ourwindow = window;
function doSlideshowReady() {
    if (ready == false) {
	ready = true;
	var arg;
	if (index >= 0) {
	    imageEntryReference.entry = imageArray[index];
	} else {
	    imageEntryReference.entry = null;
	}
	updateDOMAux(ourwindow, 3,3);
	/*
	for (var key in domMap) {
	    if (index >= 0) {
		arg = imageArray[index][key];
	    }
	    if (typeof (arg) == "undefined" || index < 0) {
		arg = domMap[key].defaultArgument;
		if (typeof(entry) == "undefined") arg = null;
	    }
	    var mode = domMap[key].mode;
	    var callMode = domMap[key].callMode;
	    if (callMode != 3) continue;
	    if (mode != 4) continue;
	    var funct = domMap[key].funct;
	    funct(arg);
	}
	*/
    }
}

var canCallDoSlideshowEnding = false;

function doSlideshowStarting() {
    var arg;
    if (index >= 0) {
	imageEntryReference.entry = imageArray[index];
    } else {
	imageEntryReference.entry = null;
    }
    updateDOMAux(window, 3,3);
    /*
    for (var key in domMap) {
	if (index >= 0) {
	    arg = imageArray[index][key];
	}
	if (typeof (arg) == "undefined" || index < 0) {
	    arg = domMap[key].defaultArgument;
	    if (typeof(arg) == "undefined") arg = null;
	}
	var mode = domMap[key].mode;
	var callMode = domMap[key].callMode;
	if (callMode != 3) continue;
	if (mode != 4) continue;
	var funct = domMap[key].funct;
	funct(arg);
    }
    */
    canCallDoSlideshowEnding = true;
}


function doSlideshowEnding() {
    if (canCallDoSlideshowEnding == false) return;
    canCallDoSlideshowEnding = false;
    updateDOMAux(window,4,4);
    /*
    var arg;
    for (var key in domMap) {
	if (index >= 0) {
	    arg = imageArray[index][key];
	}
	if (typeof (entry) == "undefined" || index < 0) {
	    arg = domMap[key].defaultArgument;
	    if (typeof(arg) == "undefined") arg = null;
	}
	var mode = domMap[key].mode;
	var callMode = domMap[key].callMode;
	if (callMode != 4) continue;
	if (mode != 4) continue;
	var funct = domMap[key].funct;
	funct(arg);
    }
    */
    imageEntryReference.entry = null;
}

function tryToEnableSlideshow() {
    if (testIfSlideshowCanBeEnabled()) {
	var button = window.document.getElementById("slideshow");
	button.disabled = false;
    }
}

function testIfSlideshowCanBeEnabled() {
    // var global = (function(){return this;}).call(null);
    for (var key in domMap) {
	var mode = domMap[key].mode;
	var callMode = domMap[key].callMode;
	if (callMode != 6) continue;
	if (mode != 4) continue;
	// var funct = eval(domMap[key].funct);
	var funct = window[domMap[key].funct];
	if (funct() == false) {
	    alert("test to enable slideshow failed");
	    return false;
	}
    }
    return true;
}


function testIfSlideshowCanStart() {
    for (var key in domMap) {
	var mode = domMap[key].mode;
	var callMode = domMap[key].callMode;
	if (callMode != 5) continue;
	if (mode != 4) continue;
	// var funct = domMap[key].funct;
	var funct = window[domMap[key].funct];
	if (typeof funct == "string") funct = eval(funct);
	if (funct() == false) {
	    return false;
	}
    }
    return true;
}


function displayWindow() {
    var parms = "width=" + screen.availWidth;
    parms += ",height=" + screen.availHeight;
    parms += ",top=0,left=0";
    // parms += ",fullscreen=yes"; (no longer supported)
    parms += ",toolbar=no";
    parms += ",directories=no";
    parms += ",location=no";
    parms += ",menubar=no";
    parms += ",personalbar=no";
    parms += ",status=no";
    parms += ",scrollbar=no";
    parms += ",resizable=yes";

    if (slideshowWindow != null) {
	if (slideshowWindow.closed) {
	    slideshowWindow = null;
	} else {
	    closeSlideshowWindow();
	}
    }

    slideshowWindow = window.open("controls/slideshow.html", "slideshow",
				  parms);
    slideshowWindow.onkeypress=escapeHandler;
    // W3C documentation claims this is the right way to set
    // onkeypress but experimentally with Firefox, the line above
    // works and this one doesn't
    slideshowWindow.document.onkeypress=escapeHandler;

    // to resize, we seem to have to use the HTML code, so we set
    // a property so the page in the new window can find the method.
    slideshowWindow.updateSlideshowWindow = updateSlideshowWindow;

    // used by unload in BODY tag for slideshow window so we stop
    // everything if the user closes the window.
    slideshowWindow.stopSlideshow = stopSlideshow;

    slideshowWindow.doSlideshowReady = doSlideshowReady;

    if (window.focus) {
	slideshowWindow.focus();
    }
    // alert("height = " + slideshowWindow.innerHeight +" <= " +screen.height);

    // in case event handler code doesn't work.
    // setTimeout(closeSlideshowWindow, 30000);
    // monitorSlideshowWindow();

    return;
}


var timeoutID = 0;

/*
function doNothing() {
    return;
}
*/
// some browsers don't call the onerror method if loading an image fails.
var errorHandlingBroken = true;

function imageComplete(image) {
    if (!image.complete) {
	return false;
    }
    if (((typeof image.naturalWidth) != "undefined" && image.naturalWidth == 0)
	|| ((typeof image.naturalHeight) != "undefined" 
	    && image.naturalHeight == 0)) {
	if (errorHandlingBroken && image.complete) {
	    onCacheError();
	    // setTimeout("onCacheError()", 0);
	    // return true; (want false so doSlideshow will try again)
	}
	return false;
    }
    return true;
}

function checkErrorHandling() {
    var img = new Image();
    img.onerror="errorHandlingBroken = false";
    img.src = "controls/NoFile.jpg";
}
checkErrorHandling();

var nextImage = null;
var cacheID = 0;
var caching = false;
var cacheOffset = 1;
function onCacheError(/*event*/) {
    if (cacheID != 0) {
	clearTimeout(cacheID);
	cacheID = 0;
    }
    // alert("cache error detected");
    cacheOffset++;
    updateCache();
}

function updateCache() {
    cacheID = 0;
    if ((loop || (index + cacheOffset) <= maxIndex) && hasAllImages 
	&& slideshowWindow != null && !slideshowWindow.closed) {
	var ind = index + cacheOffset;
	if (loop) {
	    while (ind < 0) ind += imageArray.length;
	    while (ind > maxIndex) ind -= imageArray.length;
	    
	}
	var w = imageArray[ind].width;
	var h = imageArray[ind].height;
	if (slideshowWindow != null) {
	    var sw = getInnerWidth(slideshowWindow) - wOffset;
	    var sh = getInnerHeight(slideshowWindow) - hOffset;
	    sw = Math.round((w * wPercent)/100);
	    sh = Math.round((h * hPercent)/100);
	    var xscale = sw / w;
	    var yscale = sh / h;
	    var scale = (xscale < yscale)? xscale: yscale;
	    if (scale == 0) {scale = 1;}
	    w = Math.floor(w * scale);
	    h = Math.floor(h * scale);
	}
	if (nextImage != null) nextImage.onerror = null;
	nextImage = new Image(w,h);

	// this is relative to the toplevel directory for these
	// images.
	nextImage.onerror = "onCacheError()";
	// nextImage.onload = "alert(\"loaded " +imageArray[ind].name +"\")";
	/*
	if (((typeof imageArray[ind].hrExt) != "undefined") &&
	    (imageArray[ind].hrExt != null)) {
	    nextImage.src =  highResDir  +"/" + imageArray[ind].name 
		+ "." + imageArray[ind].hrExt;
	} else {
	    nextImage.src =  highResDir + "/" + imageArray[ind].name 
		+ "." + imageArray[ind].ext ;
	}
	*/
	nextImage.src = imageArray[ind].fsImageURL;
	caching = true;
    }
}


var count = 0;

var indefinite = false;

// Step's slideshow when timing is indefinite
function stepSlideshow() {
    if (indefinite) {
	indefinite = false;
	if (ready) {
	    doSlideshow();
	}
    }
}

function httpStepSlideshow(ind) {
    // ask server for permission to step.
    // for a preliminary test, just set a timeout.
    if (timeoutID != 0) clearTimeout(timeoutID);
    timeoutID = setTimeout("stepSlideshow()", 7000);
}


function doSlideshow() {
    if (ready) {
	var co = cacheOffset;
	var ourindex = index + cacheOffset;
	// alert("ourindex = " + ourindex +" for index = " + index);
	if (caching && !imageComplete(nextImage)) {
	    /*
	    if (index == 2 && firstTwo) {
		firstTwo = false;
		alert("index = 2");
	    }
	    */
	    // index -= co;
	    if (timeoutID != 0) clearTimeout(timeoutID);
	    timeoutID = setTimeout("doSlideshow()", 200);
	    actualTime += 200;
	} else if (ourindex >= maxIndex && !loop) {
	    if (cacheOffset == 1) {
		// last image is valid; otherwise only current image
		// is valid.
		index = ourindex;
		if (index > maxIndex) index = maxIndex;
	    }
	    /*
	    if (co > 1) {
		index -= co;
		if (index < 0) index = 0;
	    } else {
		index = imageArray.length - 1;
	    }
	    */
	    if (index < 0) alert("doSlideshow: index < 0 (loc 1");
	    updateLocations(".");
	    timeoutID = 0;
	    finalSlide = true;
	    var dur = ((typeof imageArray[index].duration) == "undefined")?
		imageTime: imageArray[index].duration;
	    if (dur != "*" && dur != "?") {
		if (cacheOffset > 1) dur = 0;
		timeoutID = setTimeout("stopSlideshow()", dur);
	    } else {
		if (dur == "?") {
		    // alert("duration = " + duration);
		    httpStepSlideshow(index);
		}
		/*
		document.getElementById("slideshow").value="slideshow";
		if (hasAllImages) {
		    var fsCheckbox = document.getElementById("fullscreen");
		    fsCheckbox.disabled = false;
		}
		document.getElementById("loop").disabled = false;
		doSlideshowEnding();
		*/
	    }
	} else if (co > 1 && waitOnError) {
	    // image errors - we have to skip a broken image while adding
	    // to the duration.
	    var offset = actualTime - desiredTime;
	    var duration = 0;
	    for (var ind = 1; ind < co; ind++) {
		var cindex = ourindex - ind;
		if (loop) {
		    while (cindex < 0) cindex += imageArray.length;
		    while (cindex > maxIndex) cindex -= imageArray.length;
		}
		/*
		duration += 
		    ((typeof imageArray[cindex].duration) == "undefined")?
		    imageTime: imageArray[cindex].duration;
		    ... replaced with:
		*/
		var incr;
		incr = ((typeof imageArray[cindex].duration) == "undefined")?
		    imageTime: imageArray[cindex].duration;
		if (incr != "*" && incr != "?") {
		    duration += incr;
		}
	    }
	    desiredTime += duration;
	    if (syncMode) {
		if (offset > 0) {
		    duration -= offset;
		    var mit = ((typeof imageArray[cindex].minImageTime) == 
			       "undefined")? minImageTime:
			imageArray[cindex].minImageTime;
		    if (mit < 0) mit = 0;
		    if (duration < mit) {
			duration = mit;
		    }
		}
	    }
	    actualTime += duration;
	    index = ourindex - 1;
	    if (loop) {
		while (index < 0) index += imageArray.length;
		while (index > maxIndex) index -= imageArray.length;
	    }
	    cacheOffset = 1;
	    if (timeoutID != 0) clearTimeout(timeoutID);
	    /*
	    if ((count++) < 20) {
		alert("* index = " + index + ", dur = " + duration
		      + ", maxIndex = " + maxIndex 
		      + ", cacheOffset = " + cacheOffset);
	    }
	    */
	    timeoutID = setTimeout("doSlideshow()", duration);
	} else {
	    index = ourindex;
	    if (loop) {
		    while (index < 0) index += imageArray.length;
		    while (index > maxIndex) index -= imageArray.length;
	    }
	    cacheOffset = 1;
	    if (index < 0) alert("doSlideshow: index < 0 (loc 2");
	    updateLocations(".");
	    cacheID = setTimeout("updateCache()", 10);

	    var duration;
	    duration = ((typeof imageArray[index].duration) == "undefined")?
		imageTime: imageArray[index].duration;
	    if (duration != "*" && duration != "?") {
		var offset = actualTime - desiredTime;
		desiredTime += duration;
		if (syncMode) {
		    if (offset > 0) {
			duration -= offset;
			var mit = ((typeof imageArray[index].minImageTime) == 
				   "undefined")? minImageTime:
			    imageArray[index].minImageTime;
			if (mit < 0) mit = 0;
			if (duration < mit) {
			    duration = mit;
			}
		    }
		}
		actualTime += duration;
		if (timeoutID != 0) clearTimeout(timeoutID);
		timeoutID = setTimeout("doSlideshow()", duration);
	    } else {
		// we can't adjust if timing is indefinite.
		desiredTime = actualTime;
		indefinite = true;
		if (duration == "?") {
		    httpStepSlideshow(index);
		}
	    }
	}
    } else {
	// relative URLs are based on the location of the HTML
	// document in which a script is called, so for updateLocations()
	// to work properly, we have to start this function from a
	// specific place.  We need to wait for a new window to be
	// created and for all the tags to be accessible via the DOM
	// interfaces, so we use a timeout to poll until this happens.
	if (timeoutID != 0) clearTimeout(timeoutID);
	timeoutID = setTimeout("doSlideshow()", 100);
	actualTime += 100;
    }
}

function stopSlideshow() {
    var button = document.getElementById("slideshow");
    if (timeoutID != 0) clearTimeout(timeoutID);
    timeoutID = 0;
    if (cacheID != 0) clearTimeout(cacheID);
    button.value = "slideshow";
    if (hasAllImages) {
	closeSlideshowWindow();
	var fsCheckbox = document.getElementById("fullscreen");
	fsCheckbox.disabled = false;
    }
    document.getElementById("loop").disabled = false;
    doSlideshowEnding();
}

function runSlideshow() {

    var button = document.getElementById("slideshow");
    var canRun = (button.value == "slideshow");
    if (canRun && testIfSlideshowCanStart() == false) {
	return;
    }
    var fsCheckbox = document.getElementById("fullscreen");
    readyToDisplaySSW = false;
    caching = false;
    count = 0;
    if (canRun) {
	actualTime = 0;
	desiredTime = 0;
	ready = true;
	var ourReady = ready;
	if (hasAllImages && fsCheckbox.checked) {
	    ready = false;
	    ourReady = ready;
	    if (index >= 0) index--;
	    displayWindow();
	    // updateCache();
	}
	if (hasAllImages) fsCheckbox.disabled = true;
	var loopCheckbox = document.getElementById("loop");
	loop = loopCheckbox.checked;
	loopCheckbox.disabled = true;

	button.value="stop slideshow";
	// when a slideshow window is used, doSlideshow will
	// be called by its BODY tag's onload method
	cacheOffset = 1;
	nextImage = null;
	if (ourReady) doSlideshowStarting();
	updateCache();
	doSlideshow();
    } else {
	stopSlideshow();
    }
}

ensureImageFrameLocation(".");
