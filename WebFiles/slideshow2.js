var domMap;
if ((typeof domMap) == "undefined") {
    domMap = {}
}

var maxIndex = imageArray.length - 1;

var index = -1;
var firstTwo = true;

var actualTime = 0;
var desiredTime = 0;

var defaultTitle = null;
var defaultDescr = null;

var loop = false;

var configured = false;

function configure() {
    var button = window.document.getElementById("slideshow");
    button.disabled = true;
    var element = window.document.getElementById("title");
    defaultTitle = (element == null)? "": element.innerHTML;
    element = window.document.getElementById("descr");
    defaultDescr = (element == null)? "": element.innerHTML;
    index = 0;
    updateLocations(".");
    var button = window.document.getElementById("slideshow");
    button.disabled = false;
    configured = true;
}

function configureNS() {
    index = 0;
    updateLocations(".");
}

if (minImageTime < 0) minImageTime = 0;

var appendError = function(str) {
    throw new Error("slideshow2: " + str);
}
function log(str) {
    setTimeout("appendError('" + str + "')", 1);
}

var fselem = null;
var fscnt = 0;


var readyToDisplaySSW = false;

function updateWindow(ind) {
    index = ind;
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

function updateExpand() {
    if (index < 0) return;
    var link = document.getElementById("expand");
    link.href = imageArray[index].fsImageURL;
    if (fselem != null) {
	var sswurl = imageArray[index].fsImageURL;
	if (sswurl.startsWith("./")) {
	    fselem.src = (new URL(sswurl, document.URL)).href;
	} else {
	    fselem.src = imageArray[index].highImageURL;
	}
    }
}

var hrefCount = 0;
function updateLocations(cdir) {
    window.frames["images"].location =
	cdir + "/medium/" + imageArray[index].name + ".html";
    updateExpand();
    return;
}

function closeSlideshowWindow() {

    if(fselem != null) {
	if (fselem.exitFullscreen) {
	    fselem.exitFullscreen();
	} else if (fselem.webkitExitFullscreen) { /* Safari */
	    fselem.webkitExitFullscreen();
	} else if (fselem.msExitFullscreen) { /* IE11 */
	    fselem.msExitFullscreen();
	}
	document.body.removeChild(fselem);
	fselem = null;
	fscnt = 0;
    }
    return;
}

// ourwindow used to make sure we get the right window when doSlideshowReady
// is called from within the window we create.
var ourwindow = window;

function displayWindow() {
    if (fselem == null) {
	fselem = document.createElement("IMG");
	document.body.appendChild(fselem);
	if (fselem.requestFullscreen) {
	    fselem.requestFullscreen();
	} else if (fselem.webkitRequestFullscreen) { /* Safari */
	    fselem.webkitRequestFullscreen();
	} else if (fselem.msRequestFullscreen) { /* IE11 */
	    fselem.msRequestFullscreen();
	}
	cnt = 0;
	fselem.onfullscreenchange = function() {
	    if (cnt > 0) {
		document.body.removeChild(fselem);
		fselem = null;
		cnt = 0;
		stopSlideshow();
		return;
	    }
	    cnt++;
	};
    }
    return;
}

var timeoutID = 0;

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
	}
	return false;
    }
    return true;
}

function assertErrorHandlingOK(evt, source, line) {
    errorHandlingBroken = false;
}

function checkErrorHandling() {
    var img = new Image();
    img.onerror = assertErrorHandlingOK;
    img.src = "controls/NoFile.jpg";
}
checkErrorHandling();

var nextImage = null;
var cacheID = 0;
var caching = false;
var cacheOffset = 1;
function onCacheError(evt, source, line) {
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
	&& fselem != null) {
	var ind = index + cacheOffset;
	if (loop) {
	    while (ind < 0) ind += imageArray.length;
	    while (ind > maxIndex) ind -= imageArray.length;
	    
	}
	var w = imageArray[ind].width;
	var h = imageArray[ind].height;
	if (fselem != null) {
	    // estimate bacause of security constraints
	    var sw = screen.availWidth - 20;
	    var sh = screen.availHeight - 50;
	    sw = Math.round((sw * wPercent)/100);
	    sh = Math.round((sh * hPercent)/100);
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
	nextImage.onerror = onCacheError;
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
	doSlideshow();
    }
}

function httpStepSlideshow(ind) {
    // ask server for permission to step.
    // for a preliminary test, just set a timeout.
    if (timeoutID != 0) clearTimeout(timeoutID);
    timeoutID = setTimeout("stepSlideshow()", 7000);
}


function doSlideshow() {
    var co = cacheOffset;
    var ourindex = index + cacheOffset;
    if (caching && !imageComplete(nextImage)) {
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
	if (index < 0) alert("doSlideshow: index < 0 (loc 1");
	updateLocations(".");
	timeoutID = 0;
	// finalSlide = true;
	var dur = ((typeof imageArray[index].duration) == "undefined")?
	    imageTime: imageArray[index].duration;
	if (dur != "*" && dur != "?") {
	    if (cacheOffset > 1) dur = 0;
	    timeoutID = setTimeout("stopSlideshow()", dur);
	} else {
	    if (dur == "?") {
		httpStepSlideshow(index);
	    }
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
	if (fsCheckbox != null) {
	    fsCheckbox.disabled = false;
	}
    }
    document.getElementById("loop").disabled = false;
}

function controlSlideshow() {
    var button = document.getElementById("slideshow");
    if (button.value == "slideshow") {
	runSlideshow();
    } else {
	stopSlideshow();
    }
}

function runSlideshow() {
    var button = document.getElementById("slideshow");
    var canRun = (button.value == "slideshow");
    if (canRun == false) return;
    var fsCheckbox = document.getElementById("fullscreen");
    readyToDisplaySSW = false;
    caching = false;
    count = 0;
    if (canRun) {
	actualTime = 0;
	desiredTime = 0;
	// ready = true;
	// var ourReady = ready;
	if (hasAllImages && fsCheckbox.checked) {
	    /*
	    ready = false;
	    ourReady = ready;
	    */
	    if (index >= 0) index--;
	    displayWindow();
	    // updateCache();
	}
	if (hasAllImages) fsCheckbox.disabled = true;
	var loopCheckbox = document.getElementById("loop");
	loop = loopCheckbox.checked;
	loopCheckbox.disabled = true;

	button.value = "stop slideshow";
	// when a slideshow window is used, doSlideshow will
	// be called by its BODY tag's onload method
	cacheOffset = 1;
	nextImage = null;
	updateCache();
	doSlideshow();
    } else {
	stopSlideshow();
    }
}

ensureImageFrameLocation(".");
