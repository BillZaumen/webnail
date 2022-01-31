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


var remotesConfiguredSet = false;
var remotesConfigured = false;
var isMaestro = false;

function  checkRemoteModes() {
    // console.log("starting request");
    var request = new XMLHttpRequest();
    request.onreadystatechange = function() {
	if (this.readyState == 4) {
	    var stat = this.status;
	    if (stat == 200) {
		remotesConfigured = true;
		if (this.responseText.trim() == "true") {
		    isMaestro = true;
		} else {
		    isMaestro = false;
		}
		// console.log("remote slideshow status: " + isMaestro);
		remotesConfiguredSet = true;
	    } else {
		if (stat == 0) {
		    // we couldn't complete the request
		    // console.log("checkRemoteModes: try again (status = 0)");
		    remotesConfigured = false;
		    remotesConfiguredSet = false;
		} else {
		    /*
		    console.log("no remote slideshow, response code = "
				+ stat);
		    */
		    remotesConfigured = false;
		    isMaestro = false;
		    remotesConfiguredSet = true;
		}
	    }
	}
    };
    request.open("POST", "/sync/status", true);
    request.send();
}

function configure() {
    var button = window.document.getElementById("slideshow");
    button.disabled = true;
    var element = window.document.getElementById("title");
    defaultTitle = (element == null)? "": element.innerHTML;
    element = window.document.getElementById("descr");
    defaultDescr = (element == null)? "": element.innerHTML;
    index = 0;
    try {
	checkRemoteModes();
    } catch (err) {
	console.log("XMLHttpRequest failed: " + err);
    }

    if (!remotesConfiguredSet) {
	setTimeout(function() {
	    // just in case the first attempt fails.
	    try {
		checkRemoteModes();
	    } catch (err) {
		console.log("XMLHttpRequest failed: " + err);
	    }
	}, 2000);
    }

    updateLocations(".");
    // Sometimes we don't get to the right image, possibly
    // due to a race condition.
    if (!window.frames["images"].location.href
	.endsWith(imageArray[index].name + ".html")) {
	setTimeout(function() {	updateLocations(".");}, 4500);
    }

    var button = window.document.getElementById("slideshow");
    button.disabled = false;
    configured = true;
    // console.log("configured, index = " + index);
}

function syncIndex() {
    try {
	var loc = window.frames["images"].location.href;
	if (index < 0) index = 0;
	var tail = imageArray[index].name + ".html";
	if (loc != null && tail != null) {
	    if (!loc.endsWith(tail)) {
		for (var i = 0; i < imageArray.length; i++) {
		    tail = imageArray[i].name + ".html";
		    if (loc.endsWith(tail)) {
			index = i;
			break;
		    }
		}
	    }
	}
    } catch (err) {
        console.log("syncIndex() failed: " + err);
    }
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

function updateClient() {
    var request = new XMLHttpRequest();
    request.onreadystatechange = function() {
	if (this.readyState == 4) {
	    var stat = this.status;
	    if (stat == 200) {
		var data = JSON.parse(this.responseText);
		/*
		console.log("data.index = " + data.index
			    + ", data.delay = " + data.delay
			    + ", data.loop = " + data.loop
			    + ", data.cont = " + data.cont
			    + ", data.maxIndex = " + data.maxIndex);
		*/
		if (data.cont) {
		    if (data.index < 0) {
			resetClient();
		    } else {
			if (data.delay < 0) {
			    setTimeout(updateClient, 2000);
			} else {
			    if (fselem != null) {
				var sswurl = imageArray[data.index].fsImageURL;
				if (sswurl.startsWith("./")) {
				    fselem.src = (new URL(sswurl,
							  document.URL))
					.href;
				} else {
				    fselem.src = imageArray[data.index]
					.highImageURL;
				}
			    } else {
				console.log("full-screen image is null");
			    }
			    setTimeout(updateClient, data.delay);
			}
		    }
		} else if (data.index >= data.maxIndex) {
		    if (fselem != null) {
			var sswurl = (data.maxIndex == -1)?
			    "./controls/initial.png":
			    imageArray[data.maxIndex].fsImageURL;
			if (sswurl.startsWith("./")) {
			    fselem.src = (new URL(sswurl,document.URL)).href;
			} else {
			    fselem.src = imageArray[data.maxIndex].highImageURL;
			}
		    } else {
			console.log("full-screen image is null");
		    }
		    if (data.index == -1) {
			resetClient();
		    } else {
			setTimeout(updateClient, data.delay);
		    }
		} else {
		    // console.log("will reset client");
		    setTimeout(resetClient, 2000);
		}
	    } else {
		// We failed - try again.
		setTimeout(updateClient, 2000);
	    }
	}
    };
    request.open("POST", "/sync/get", true);
    request.send();
}

var clientStarted = false;

function startClient() {
    displayWindow();
    if (fselem != null) {
	fselem.src = new URL("./controls/initial.png", document.URL).href;
    }
    clientStarted = true;
    updateClient();
}

function resetClient() {
    if (fselem != null) {
	fselem.src = new URL("./controls/initial.png", document.URL).href;
	setTimeout(updateClient, 2000);
    }
    // for testing when fselem always is null.
    // console.log("resetting client");
    // setTimeout(updateClient, 2000);
}


function updateExpand() {
    if (index < 0) return;
    try {
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
    } catch (err) {
	console.log("updateExpand() failed: " + err);
    }
}

var hrefCount = 0;
function updateLocations(cdir) {
    if (index == -1) {
	console.log("updateLocations called when index == -1");
    }
    window.frames["images"].location =
	cdir + "/medium/" + imageArray[index].name + ".html";
    updateExpand();
    var tnails = window.frames["thumbnails"];
    if (tnails != null) {
	try {
	    if (index >= 0) {
		tnails.frameElement.contentWindow.updatePosition(index);
	    }
	} catch (err) {
	    console.log("cannot update thumbnails: " + err);
	}
    }
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
	clientStarted = null;
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

function updateServer(dur) {
    updateServerAux(index, dur);
}

function updateServerAux(ind, dur) {
    var request = new XMLHttpRequest();
    request.open("POST", "/sync/set", true);
    request.setRequestHeader("Content-type",
			     "application/x-www-form-urlencoded");
    request.send("index=" + ind + "&delay=" + dur
		 +"&loop=" + loop + "&maxIndex=" + maxIndex);
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
	    if (remotesConfigured && isMaestro) updateServer(dur);
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
	if (remotesConfigured && isMaestro) updateServer(duration);
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
	    if (remotesConfigured && isMaestro) updateServer(duration);
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
    if (isMaestro) {
	updateServerAux(-1, 0);
    }
    clientStarted = false;
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
    if (remotesConfigured && isMaestro == false) {
	// console.log("runSlideshow: isMeastro = " + isMaestro);
	if (clientStarted == false) {
	    startClient();
	}
	return;
    }

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
