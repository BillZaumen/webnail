package webnail;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.net.URLEncoder;

import javax.swing.*;
import java.text.NumberFormat;
import java.awt.event.*;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;
import java.net.*;

import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.TemplateProcessor.KeyMap;
import org.bzdev.util.ErrorMessage;
import org.bzdev.swing.SwingErrorMessage;
import org.bzdev.imageio.ImageScaler;
import org.bzdev.imageio.ImageMimeInfo;
import org.bzdev.util.CopyUtilities;
import org.bzdev.net.WebEncoder;
import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.xml.sax.SAXException;

/*
 * Webnail main program, constants, and 'generate' function.
 */
public class Webnail {

    static private final String resourceBundleName = "webnail.Webnail";
    static ResourceBundle bundle =
	ResourceBundle.getBundle(resourceBundleName);
    static String localeString(String name) {
	return bundle.getString(name);
    }

    static String WEBNAIL_XML_MIME_TYPE = "application/x.webnail+xml";
    static String WEBNAIL_LAYOUT_XML_MIME_TYPE =
	"application/x.webnail-layout+xml";
    static String XML_MIME_TYPE = "application/xml";
    static String ALT_XML_MIME_TYPE = "text/xml";
    static String GENERIC_MIME_TYPE = "application/octet-stream";

    static String ACCEPT_VALUE = WEBNAIL_XML_MIME_TYPE
	+ ", " + XML_MIME_TYPE
	+ ", " + ALT_XML_MIME_TYPE
	+ ", " + GENERIC_MIME_TYPE;



    // IETF standards don't seem to include this and suggest
    // application/octet-stream if the type can't be determined, but
    // it is what java.net.URLConnection.getContentType() returns ...
    static String BOGUS_MIME_TYPE = "content/unknown";

    static final int TWIDTH = 100;
    static final int THEIGHT = 100;
    static final int MWIDTH = 500;
    static final int MHEIGHT = 450;
    static final int MARGINW = 10;
    static final int MARGINH = 10;
    static final int MARGIN_HPAD = 30;
    static final int MARGIN_VPAD = 40;
    static final int T_VPAD = 4;
    static final int NUM_T_IMAGES = 5;
    // static final int T_VCORRECTION = 10;
    static final int T_VCORRECTION = -30;
    static final int MAX_NOT_NORMAL_THUMBWIDTH = 200;
    static final int MAX_NOT_NORMAL_THUMBHEIGHT = 200;

    static final String THUMB_STRUT_HEIGHT = "" + (THEIGHT + (2 * T_VPAD));

    static final String T_IFRAME_WIDTH = ""
	+ (TWIDTH + 2 * MARGINW + MARGIN_HPAD);

    static final String T_IFRAME_HEIGHT = ""
	+ (((THEIGHT + (2 * T_VPAD)) * NUM_T_IMAGES) + (2 * MARGINH)
	   + MARGIN_VPAD + T_VCORRECTION);


    static final String I_IFRAME_WIDTH= ""
	+ (MWIDTH + 2 * MARGINW + MARGIN_HPAD);

    static final String I_IFRAME_HEIGHT = ""
	+ (MHEIGHT + 2 * MARGINH + MARGIN_VPAD);

    static final String TD_TABLE_WIDTH="" +
	((TWIDTH + 2 * MARGINW + MARGIN_HPAD) + (MWIDTH + 2 * MARGINW));

    static final long DEFAULT_IMAGE_TIME = 10000;
    static final long DEFAULT_MIN_IMAGE_TIME = 4000;

    static final String DEFAULT_BGCOLOR = "#c0c0c0";
    static final String DEFAULT_FGCOLOR = "#000000";

    static ImageScaler scaler = new ImageScaler();

    static String type = "jpeg";
    static String extension  = "jpg";

    static private void usage() {
	System.err.println(localeString("Usage1"));
	System.err.println(localeString("Usage2"));
	System.err.println(localeString("Usage3"));
	System.err.println(localeString("Usage4"));
	System.err.println(localeString("Usage5"));
    }

    static void createWebXml(String title, File webxmlFile)
	throws IOException
    {
	OutputStream os = new FileOutputStream(webxmlFile);
	createWebXml(title, os);
	os.close();
    }

    static void createWebXml(String title, OutputStream os)
	throws IOException
    {
	// System.out.println("createWebXml called");
	TemplateProcessor.KeyMap wmap = new TemplateProcessor.KeyMap();
	wmap.put("title", title);
	int sz = ImageMimeInfo.numberOfSuffixes();
	if (ImageMimeInfo.getMIMETypeForSuffix("js") != null) sz--;
	if (ImageMimeInfo.getMIMETypeForSuffix("html") != null) sz--;
	if (ImageMimeInfo.getMIMETypeForSuffix("gif") != null) sz--;
	if (ImageMimeInfo.getMIMETypeForSuffix("png") != null) sz--;
	TemplateProcessor.KeyMap[] emaps = new TemplateProcessor.KeyMap[sz];
	int index = 0;
	for (Map.Entry<String,String>entry: ImageMimeInfo.getSuffixEntrySet()) {
	    String suffix = entry.getKey();
	    if (suffix.equals("js")) continue;
	    if (suffix.equals("html")) continue;
	    if (suffix.equals("gif")) continue;
	    if (suffix.equals("png")) continue;
	    String mtype = entry.getValue();
	    TemplateProcessor.KeyMap emap = new TemplateProcessor.KeyMap();
	    emaps[index++] = emap;
	    emap.put("extension", suffix);
	    emap.put("mimetype", mtype);
	}
	wmap.put("repeatMimeTypes", emaps);
	TemplateProcessor tp = new TemplateProcessor(wmap);
	tp.processSystemResource("webnail/web.wnt", "UTF-8", os);
    }

    static private boolean noxml = false;
    static private void noXML(File xmlf, URL xmlURL) {
	if (noxml && (xmlf != null || xmlURL != null)) {
	    System.err.println(localeString("noXMLError"));
	    System.exit(0);
	}
	noxml = true;
    }


    static String getLeadingZeros(int index, int n) {
	String leading = "";
	int limit = 1;
	n /= 10;
	while (n > 0) {
	    n /= 10;
	    limit *= 10;
	}
	while (index < limit) {
	    leading = leading + "0";
	    index *= 10;
	}
	return leading;
    }

    private static Object dirOptions[] = {
	localeString("clear"),
	localeString("abort"),
    };


    public static void generate(Parser parser, File dir, ZipOutputStream zos,
				ProgMonitor pm)
	throws Exception
    {
	int maxThumbWidth = parser.getWidth();
	int maxThumbHeight = parser.getHeight();

	int n = parser.imageListSize();
	boolean warmode = parser.getWebArchiveMode();
	// System.out.println("warmode = " +warmode);
	int index = 0;
	boolean webmode = parser.getWebMode();
	boolean zipped = parser.getZipped();

	LayoutParms lparms = parser.getLayoutParms();
	// wait if we are loading over the network.
	if (lparms != null) {
	    try {
		lparms.join();
	    } catch (InterruptedException ie) {
		return;
	    }
	}
	if (lparms == null || !lparms.isValid() || lparms.downloadFailed()) {
	    throw new Exception("lparmsNotValid");
	}

	boolean multi = lparms.isMulti();
	boolean single = lparms.isSingle();
	boolean tiledImages = lparms.isTiled();
	int tiledWidth = lparms.getTiledWidth();
	if (n == 0 && multi) return;
	if (single) {
	    if (maxThumbWidth == 0) {
		maxThumbWidth = lparms.getMaxThumbWidth();
	    }
	    if (maxThumbHeight == 0) {
		maxThumbHeight = lparms.getMaxThumbHeight();
	    }
	    if (lparms.isTiled() && maxThumbWidth > tiledWidth) {
		maxThumbWidth = tiledWidth;
	    }
	}
	String windowTitle = parser.getValue("windowTitle");
	if (windowTitle == null) windowTitle = localeString("windowTitle");
	windowTitle = WebEncoder.htmlEncode(windowTitle);
	String title = parser.getValue("title");
	if (title == null) title = "";
	title = WebEncoder.htmlEncode(title);
	String descr = parser.getValue("descr");
	if (descr == null) descr = "";
	descr = WebEncoder.htmlEncode(descr);
	String iFrameWindowTitle = "Medium-Resolution Image";
	// boolean atTop = false;
	boolean flat = parser.getFlatMode();
	if (maxThumbWidth != 0 || maxThumbHeight != 0) {
	    flat = false;
	}

	boolean hasAllImages = parser.getHighResMode() || single;

	String head = parser.getValue("head");
	String header = parser.getValue("header");
	String trailer = parser.getValue("trailer");
	String finalHtml = parser.getValue("finalHtml");
	boolean syncMode = parser.getSyncMode();
	boolean waitOnError = parser.getWaitOnError();
	String bgcolor = parser.getValue("bgcolor");
	String fgcolor = parser.getValue("fgcolor");
	boolean rvmode = parser.getRVMode();
	bgcolor = WebEncoder.htmlEncode(bgcolor);
	fgcolor = WebEncoder.htmlEncode(fgcolor);
	String mtype = parser.getMimeType();
	String type = ImageMimeInfo.getFormatNameForMimeType(mtype);
	String extension = ImageMimeInfo.getExtensionForMimeType(mtype);
	mtype = WebEncoder.htmlEncode(mtype);
	boolean hrefToOrig = parser.getHrefToOrig();

	File tdir = null; File mdir = null; File cdir = null;
	File idir = null;
	File wdir = null;

	if (dir != null && webmode) {
	    // already tested that dir is a directory, etc.
	    idir = (hasAllImages? ((flat)? dir: new File(dir, "high")):
		    new File(dir, "medium"));
	    if (hasAllImages) {
		if (idir.exists()) {
		    if (!idir.isDirectory()) {
			ErrorMessage.display(String.format
					     (localeString("idirError1"),
					      dir.toString()));
			if (pm == null) System.exit(1); else return;
		    } else if (!flat && idir.list().length != 0) {
			if (pm == null) {
			    ErrorMessage.display(String.format
						 ((hasAllImages?
						   localeString("idirError2"):
						   localeString
						   ("mdirError2")),
						  dir.toString()));
			    System.exit(1);
			} else {
			    if (0 != JOptionPane
				.showOptionDialog(Gui.frame,
						  String.format
						  ((hasAllImages?
						    localeString("idirError2"):
						    localeString
						    ("mdirError2")),
						   dir.toString()),
						  localeString("dirError"),
						  JOptionPane.YES_NO_OPTION,
						  JOptionPane.QUESTION_MESSAGE,
						  null,
						  dirOptions, dirOptions[0])) {
				return;
			    } else {
				// clear the directory.
				for (File f: idir.listFiles()) {
				    if (!f.delete()) {
					ErrorMessage.display
					    (String.format
					     (localeString("canNotDelete"),
					      f.getAbsolutePath()));
					return;
				    }
				}
			    }
			}
		    }
		} else if (!idir.mkdir()) {
		    ErrorMessage.display(String.format
					 (localeString("idirError3"),
					  dir.toString()));
		    if (pm == null) System.exit(1); else return;
		}
	    }
	    tdir = new File(dir, "thumbnails");
	    mdir = new File(dir, "medium");
	    cdir = new File(dir, "controls");
	    if (multi) {
		if (tdir.exists()) {
		    if (!tdir.isDirectory()) {
			ErrorMessage.display(String.format
					     (localeString("tdirError1"),
					      dir.toString()));
			if (pm == null) System.exit(1); else return;
		    } else if (tdir.list().length != 0) {
			if (pm == null) {
			    ErrorMessage.display(String.format
						 (localeString("tdirError2"),
						  dir.toString()));
			    System.exit(1);
			} else {
			    if (0 != JOptionPane
				.showOptionDialog(Gui.frame,
						  String.format
						  (localeString("tdirError2"),
						   dir.toString()),
						  localeString("dirError"),
						  JOptionPane.YES_NO_OPTION,
						  JOptionPane.QUESTION_MESSAGE,
						  null,
						  dirOptions, dirOptions[0])) {
				return;
			    } else {
				// clear the directory.
				for (File f: tdir.listFiles()) {
				    if (!f.delete()) {
					ErrorMessage.display
					    (String.format
					     (localeString("canNotDelete"),
					      f.getAbsolutePath()));
					return;
				    }
				}
			    }
			}
		    }
		} else if (!tdir.mkdir()) {
		    ErrorMessage.display(String.format
					 (localeString("tdirError3"),
					  dir.toString()));
		    if (pm == null) System.exit(1); else return;
		}
	    }
	    if (mdir.exists()) {
		if (!mdir.isDirectory()) {
		    ErrorMessage.display(String.format
					 (localeString("mdirError1"),
					  dir.toString()));
		    if (pm == null) System.exit(1); else return;
		} else if (mdir.list().length != 0) {
		    if (pm == null) {
			ErrorMessage.display(String.format
					     (localeString("mdirError2"),
					      dir.toString()));
			System.exit(1);
		    } else {
			if (0 != JOptionPane
				.showOptionDialog(Gui.frame,
						  String.format
						  (localeString("mdirError2"),
						   dir.toString()),
						  localeString("dirError"),
						  JOptionPane.YES_NO_OPTION,
						  JOptionPane.QUESTION_MESSAGE,
						  null,
						  dirOptions, dirOptions[0])) {
				return;
			    } else {
				// clear the directory.
				for (File f: mdir.listFiles()) {
				    if (!f.delete()) {
					ErrorMessage.display
					    (String.format
					     (localeString("canNotDelete"),
					      f.getAbsolutePath()));
					return;
				    }
				}
			    }
		    }
		}
	    } else if (!mdir.mkdir()) {
		ErrorMessage.display(String.format
				     (localeString("mdirError3"),
				      dir.toString()));
		if (pm == null) System.exit(1); else return;
	    }
	    if (multi) {
		if (cdir.exists()) {
		    if (!cdir.isDirectory()) {
			ErrorMessage.display(String.format
					     (localeString("cdirError1"),
					      dir.toString()));
			if (pm == null) System.exit(1); else return;
		    } else if (cdir.list().length != 0) {
			if (pm == null) {
			    ErrorMessage.display(String.format
						 (localeString("cdirError2"),
						  dir.toString()));
			    System.exit(1);
			} else {
			    if (0 != JOptionPane
				.showOptionDialog(Gui.frame,
						  String.format
						  (localeString("cdirError2"),
						   dir.toString()),
						  localeString("dirError"),
						  JOptionPane.YES_NO_OPTION,
						  JOptionPane.QUESTION_MESSAGE,
						  null,
						  dirOptions, dirOptions[0])) {
				return;
			    } else {
				// clear the directory.
				for (File f: cdir.listFiles()) {
				    if (!f.delete()) {
					ErrorMessage.display
					    (String.format
					     (localeString("canNotDelete"),
					      f.getAbsolutePath()));
					return;
				    }
				}
			    }
			}
		    }
		} else if (!cdir.mkdir()) {
		    ErrorMessage.display(String.format
					 (localeString("cdirError3"),
					  dir.toString()));
		    if (pm == null) System.exit(1); else return;
		}
	    }
	    if (warmode) {
		wdir = new File(dir, "WEB-INF");
		if (wdir.exists()) {
		    if (!wdir.isDirectory()) {
			ErrorMessage.display(String.format
					     (localeString("wdirError1"),
					      dir.toString()));
			if (pm == null) System.exit(1); else return;
		    } else if (wdir.list().length != 0) {
			if (pm == null) {
			    ErrorMessage.display(String.format
						 (localeString("wdirError2"),
						  dir.toString()));
			    System.exit(1);
			} else {
			    if (0 != JOptionPane
				.showOptionDialog(Gui.frame,
						  String.format
						  (localeString("wdirError2"),
						   dir.toString()),
						  localeString("dirError"),
						  JOptionPane.YES_NO_OPTION,
						  JOptionPane.QUESTION_MESSAGE,
						  null,
						  dirOptions, dirOptions[0])) {
				return;
			    } else {
				// clear the directory.
				for (File f: wdir.listFiles()) {
				    if (!f.delete()) {
					ErrorMessage.display
					    (String.format
					     (localeString("canNotDelete"),
					      f.getAbsolutePath()));
					return;
				    }
				}
			    }
			}
		    }
		} else if (!wdir.mkdir()) {
		    ErrorMessage.display(String.format
					 (localeString("wdirError3"),
					  dir.toString()));
		    if (pm == null) System.exit(1); else return;
		}
	    }
	}

	LinkedList<TemplateProcessor.KeyMap> flist =
	    new LinkedList<TemplateProcessor.KeyMap>();
	TemplateProcessor.KeyMap rmap =
	    new TemplateProcessor.KeyMap();
	if (webmode) {
	    rmap.put("bgcolor",
		     ((bgcolor == null)? DEFAULT_BGCOLOR: bgcolor));
	    rmap.put("fgcolor",
		     ((fgcolor == null)? DEFAULT_FGCOLOR: fgcolor));
	    rmap.put("hasAllImages",
		     (hasAllImages? "true": "false"));
	    rmap.put("imageTime", parser.getValue("imageTime"));
	    rmap.put("minImageTime", parser.getValue("minImageTime"));
	    rmap.put("syncMode", (syncMode? "true": "false"));
	    rmap.put("waitOnError", (waitOnError? "true": "false"));
	    rmap.put("highResDir", (hasAllImages?
				    (flat? ".": "high"):
				    "medium"));
	    // rmap.put("wOffset", "20");
	    // rmap.put("hOffset", "20");
	    // rmap.put("wPercent", "100");
	    // rmap.put("hPercent", "100");
	    rmap.put("thumbStrutHeight",lparms.getThumbStrutHeight());
	    rmap.put("thumbStrutWidth90", lparms.getThumb90StrutWidth());
	    rmap.put("marginw", "" + lparms.getMarginW());
	    rmap.put("marginh", "" + lparms.getMarginH());
	    rmap.put("mWidth", "" + lparms.getMWidth());
	    rmap.put("mHeight", "" + lparms.getMHeight());
	    rmap.put("tWidth", lparms.getTIFrameWidth());
	    rmap.put("tHeight", lparms.getTIFrameHeight());
	    rmap.put("tWidth90", lparms.getT90IFrameWidth());
	    rmap.put("tHeight90", lparms.getT90IFrameHeight());
	    rmap.put("iWidth", lparms.getIIFrameWidth());
	    rmap.put("iHeight", lparms.getIIFrameHeight());
	    rmap.put("tdWidth", lparms.getTDTableWidth());
	    rmap.put("tdHeight90", lparms.getTD90TableHeight());
	    rmap.put("numTImages", "" + lparms.getNumTImages());
	    rmap.put("windowTitle", windowTitle);
	    rmap.put("title", title);
	    rmap.put("description", descr);

	    if (head != null) rmap.put("head", head);
	    if (header != null) rmap.put("header", header);
	    if (trailer != null) rmap.put("trailer", trailer);
	    if (finalHtml != null) rmap.put("finalHtml", finalHtml);
	    rmap.put("iFrameWindowTitle", iFrameWindowTitle);
	    if (single) {
		rmap.put("width", "" + maxThumbWidth);
		rmap.put("height", "" + maxThumbHeight);
		rmap.put("txtwidth", "" +(700-maxThumbWidth));
	    }
	}
	if (pm != null) pm.startProgress(n);
	for (int ind = 0; ind < n; ind++) {
	    String inputFileStr = parser.getValue("filename", ind);
	    String inputURL = parser.getValue("url", ind);
	    String leading = getLeadingZeros(ind+1, n);
	    File inputFile = (inputFileStr != null)? new File(inputFileStr):
		new File ("img" + leading + (ind+1));
	    URL url = (inputURL != null)? new URL(inputURL): null;
	    boolean urlmode = (url != null);
	    boolean linkmode = parser.getLinkMode(ind);
	    String hrefURL = parser.getValue("hrefURL", ind);
	    if (hrefURL == null && hrefToOrig) {
		hrefURL = (inputURL == null)?
		    inputFile.toURI().toURL().toString(): inputURL;
	    }
	    String hrefTarget = parser.getValue("hrefTarget", ind);
	    if (hrefTarget == null) hrefTarget="_blank";
	    if (!urlmode &&
		!(inputFile.isFile() && inputFile.canRead())) {
		ErrorMessage.display(String.format(localeString("skipMsg"),
						   inputFileStr));
		continue;
	    }
	    String ifn = inputFile.getName();
	    int lind = ifn.lastIndexOf(".");
	    if (zos == null) {
		File outputFile;
		String fname = null;
		String name = null;
		String hrExt = null;
		File odir = webmode? idir: dir;
		boolean scaling = true;
		boolean copying = false;
		boolean checkCopying = false;

		if (maxThumbWidth == 0 && maxThumbHeight == 0) {
		    scaling = false;
		    copying = true;
		    if (urlmode) {
			if (inputURL.startsWith("file:")) {
			    File f;
			    try {
				f = new File(url.toURI());
			    } catch (URISyntaxException urie) {
				String s = inputURL.substring(5);
				while (s.startsWith("//")) {
				    s = s.substring(1);
				}
				f = new File(s);
			    }
			    if (odir.getAbsoluteFile().equals
				(f.getParentFile().getAbsoluteFile())) {
				checkCopying = true;
			    }
			    String nm = f.getName();
			    int le = nm.lastIndexOf('.');
			    String suffix = (le == -1)? "": nm.substring(le+1);
			    String mt1 = ImageMimeInfo.getMIMETypeForSuffix
				(extension);
			    String mt2 = ImageMimeInfo.getMIMETypeForSuffix
				(suffix);
			    if (mt1 != null && mt2 != null) {
				if (!mt1.equals(mt2)) {
				    scaling = true;
				    copying = false;
				    checkCopying = false;
				}
			    }
			} else {
			    try {
				URLConnection urlc = url.openConnection();
				urlc.setConnectTimeout(30000);
				String umtype = urlc.getContentType();
				if (umtype == null) {
				    InputStream is = urlc.getInputStream();
				    umtype = URLConnection
					.guessContentTypeFromStream(is);
				    is.close();
				    if (umtype == null) {
					String nm = url.getPath();
					umtype = URLConnection
					    .guessContentTypeFromName(nm);
				    }
				}
				if (umtype != null) {
				    String mt1 =
					ImageMimeInfo.getMIMETypeForSuffix
					(extension);
				    if (mt1 != null) {
					if (!mt1.equals(umtype)) {
					    scaling = true;
					    copying = false;
					    checkCopying = false;
					}
				    }
				}
			    } catch (Exception eurlc) {
				// if we can't do these tests, we'll
				// just use the initial assumptions as to
				// the right values for scaling and copying.
			    }
			}
		    } else if (odir.getAbsoluteFile().equals
			       (inputFile.getParentFile()
				.getAbsoluteFile())) {
			checkCopying = true;
		    }
		}
		if (lind == -1) {
		    name = URLEncoder.encode(ifn, "UTF-8");
		    if (!scaling && !webmode) {
			if (checkCopying) {
			    fname = ifn;
			    copying = false;
			} else {
			    fname = ifn + "." + extension;
			}
		    } else {
			fname = ifn + "." + extension;
		    }
		    outputFile = new File(odir, fname);
		} else {
		    hrExt = (checkCopying)?
			ifn.substring(lind+1,
				      ifn.length()):
			null;
		    if ((hrExt != null) &&
			ImageMimeInfo.getMIMETypeForSuffix(hrExt)
			.equals(ImageMimeInfo.getMIMETypeForSuffix(extension))){
			if (checkCopying) copying = false;
		    } else {
			hrExt = null;
		    }
		    fname = (!scaling)? ifn:
			ifn.substring(0, lind) + "."
			+ extension;
		    name =
			URLEncoder.encode(ifn.substring(0,lind),
					  "UTF-8");
		    outputFile =
			new File(odir, fname);
		}
		if (hrExt != null) {
		    if (hrExt.equals(extension)) {
			hrExt = null;
		    } else {
			hrExt = URLEncoder.encode(hrExt,
						  "UTF-8");
		    }
		}
		if ((copying || scaling)
		    && inputFile.getAbsolutePath()
		    .equals(outputFile.getAbsolutePath())) {
		    throw new
			Exception("input and output file(s)"
				  +"identical");
		}
		if (!webmode || hasAllImages || single) {
		    if (copying) {
			if (urlmode) {
			    if (linkmode == false) {
				CopyUtilities.copyURL(url, outputFile);
			    }
			} else {
			    CopyUtilities.copyFile(inputFile, outputFile);
			}
		    } else if (scaling) {
			if (urlmode) {
			    if (linkmode == false) {
				scaler.scaleImage(maxThumbWidth,
						maxThumbHeight,
						url, outputFile,
						type);
			    }
			} else {
			    scaler.scaleImage(maxThumbWidth,
					    maxThumbHeight,
					    inputFileStr, outputFile,
					    type);
			}
		    }
		}
		if (webmode && (fname != null)) {
		    TemplateProcessor.KeyMap map =
			new TemplateProcessor.KeyMap();
		    map.put("highImageURL",
			    (urlmode && linkmode)? url.toString():
			    "../" +
			    URLEncoder.encode
			    ((String)rmap.get("highResDir"),
			     "UTF-8")
			    + "/"
			    + URLEncoder.encode(fname, "UTF-8"));
		    map.put("fsImageURL",
			    (urlmode && linkmode)? url.toString():
			    "./" +
			    URLEncoder.encode
			    ((String)rmap.get("highResDir"),
			     "UTF-8")
			    + "/"
			    + URLEncoder.encode(fname, "UTF-8"));
		    map.put("hrefTarget", hrefTarget);
		    map.put("hrefURL", ((hrefURL == null)?
					(multi? map.get("highImageURL"):
					 map.get("fsImageURL")):
					WebEncoder.quoteEncode(hrefURL)));
		    map.put("imageFile",
			    URLEncoder.encode(fname, "UTF-8"));
		    map.put("name", name);
		    map.put("mediumFile", name +"."
			    + URLEncoder.encode(extension, "UTF-8"));
		    map.put("ext", extension);
		    map.put("index", "" + flist.size());
		    map.put("imageURL", name + "."
			    + URLEncoder.encode(extension, "UTF-8"));
		    map.put("imageHtmlURL", name + ".html");
		    String otherProps = parser.getValue("otherProps", ind);
		    String x = parser.getValue("title", ind);
		    if (x != null) {
			if (single) {
			    map.put("title", WebEncoder.htmlEncode(x));
			} else {
			    map.put("imgTitle", WebEncoder.htmlEncode(x));
			}
			x = WebEncoder.quoteEncode(x);
			if (otherProps == null) {
			    otherProps = ", title: \"" + x + "\"";
			} else {
			    otherProps = otherProps + ", title: \"" + x + "\"";
			}
		    }
		    x = parser.getValue("descr", ind);
		    if (x != null) {
			x = WebEncoder.quoteEncode(x);
			if (otherProps == null) {
			    otherProps = ", descr: \"" + x + "\"";
			} else {
			    otherProps = otherProps + ", descr: \"" + x + "\"";
			}
		    }
		    x = parser.getValue("imageTime", ind);
		    if (x != null) {
			long lx = parser.getImageTime(ind);
			if (lx > -1) {
			    x = "" + lx;
			}
			if (otherProps == null) {
			    otherProps = ", duration: \"" + x + "\"";
			} else {
			    otherProps = otherProps + ", duration: \""
				+ x + "\"";
			}
		    }

		    x = parser.getValue("minImageTime", ind);
		    if (x != null) {
			long lx = parser.getMinImageTime(ind);
			if (lx > -1) {
			    x = "" + lx;
			}
			if (otherProps == null) {
			    otherProps = ", minImageTime: \"" + x + "\"";
			} else {
			    otherProps = otherProps + ", minImageTime: \""
				+ x + "\"";
			}
		    }

		    if (hrExt != null) {
			if (otherProps == null) {
			    otherProps = ", hrExt: \"" +hrExt + "\"";
			} else {
			    otherProps = otherProps +
				", hrExt: \"" +hrExt + "\"";
			}
		    }
		    if (otherProps != null) {
			map.put("otherProps", otherProps);
		    }
		    map.put("commaSeparator", ",");
		    if (tiledImages) {
			int modulus = tiledWidth / maxThumbWidth;

			// System.out.println("modulus = " + modulus);

			if ((ind+1) < n && ((ind + 1) % modulus) == 0) {
			    map.put("newTableRow", "</tr><tr>");
			}
		    }

		    flist.add(map);
		    if (multi) {
			odir = tdir;
			if (lind == -1) {
			    outputFile = new File(odir,
						  ifn + "."
						  + extension);
			} else {
			    outputFile =
				new File(odir,
					 ifn.substring(0, lind)
					 + "."
					 + extension);
			}
			if (urlmode) {
			    scaler.scaleImage(lparms.getTWidth(),
					    lparms.getTHeight(),
					    url, outputFile,
					    type);
			} else {
			    scaler.scaleImage(lparms.getTWidth(),
					    lparms.getTHeight(),
					    inputFileStr, outputFile,
					    type);
			}
			odir = mdir;
			File htmlOutputFile = null;
			if (lind == -1) {
			    outputFile = new File(odir,
						  ifn + "."
						  + extension);
			    htmlOutputFile =
				new File(odir, ifn + ".html");
			} else {
			    outputFile =
				new File(odir,
					 ifn.substring(0, lind)
					 + "."
					 + extension);
			    htmlOutputFile =
				new File(odir,
					 ifn.substring(0, lind)
					 + ".html");
			}
			if (urlmode) {
			    scaler.scaleImage(lparms.getMWidth(),
					    lparms.getMHeight(),
					    url, outputFile,
					    type);
			} else {
			    scaler.scaleImage(lparms.getMWidth(),
					    lparms.getMHeight(),
					    inputFileStr, outputFile,
					    type);
			}
			map.put("width", "" + scaler.getLastImageWidth());
			map.put("height", "" + scaler.getLastImageHeight());

			TemplateProcessor mp = new TemplateProcessor(rmap,
						       map);
			mp.processSystemResource
			    ("webnail/mediumHTML.wnt", "UTF-8", htmlOutputFile);
		    }
		}
	    } else {
		String ofn;
		String name = null;
		String hrExt = null;
		String fname = null;
		boolean scaling = true;
		boolean copying = false;
		if (maxThumbWidth == 0 && maxThumbHeight == 0) {
		    scaling = false;
		    copying = true;
		}
		if (lind == -1) {
		    fname = ifn +"." + extension;
		    ofn = (webmode? "high/": "")
			+ ifn +"." + extension;
		    name = URLEncoder.encode(ifn, "UTF-8");
		} else {
		    name = ifn.substring(0, lind);
		    fname = name +"." + extension;
		    name = URLEncoder.encode(name, "UTF-8");
		    ofn = (webmode? "high/": "") + fname;
		}
		if (!webmode || hasAllImages || single) {
		    zos.setLevel(0);
		    zos.setMethod(ZipOutputStream.STORED);
		    ZipEntry ze = new ZipEntry(ofn);
		    ByteArrayOutputStream bos =
			new ByteArrayOutputStream(2<<16);
		    if (copying) {
			if (urlmode) {
			    if (linkmode == false) {
				CopyUtilities.copyURL(url, bos);
			    }
			} else {
			    CopyUtilities.copyFile(inputFile, bos);
			}
		    } else {
			if (urlmode) {
			    if (linkmode == false) {
				scaler.scaleImage(maxThumbWidth,
						maxThumbHeight,
						url, bos, type);
			    }
			} else {
			    scaler.scaleImage(maxThumbWidth,
					    maxThumbHeight,
					    inputFileStr, bos, type);
			}
		    }
		    int sz = bos.size();
		    byte[] array = bos.toByteArray();
		    CRC32 crc = new CRC32();
		    crc.update(array);
		    ze.setSize(sz);
		    ze.setCompressedSize(sz);
		    ze.setCrc(crc.getValue());
		    zos.putNextEntry(ze);
		    zos.write(array, 0, sz);
		    array = null; bos.close(); bos = null;
		    zos.closeEntry();
		}
		if (webmode) {
		    TemplateProcessor.KeyMap map =
			new TemplateProcessor.KeyMap();
		    map.put("highImageURL",
			    (urlmode && linkmode)?
			    url.toString():
			    "../" +
			    URLEncoder.encode
			    ((String)rmap.get("highResDir"),
			     "UTF-8")
			    + "/"
			    + URLEncoder.encode
			    (fname, "UTF-8"));
		    map.put("fsImageURL",
			    (urlmode && linkmode)?
			    url.toString():
			    "./" +
			    URLEncoder.encode
			    ((String)rmap.get("highResDir"),
			     "UTF-8")
			    + "/"
			    + URLEncoder.encode(fname, "UTF-8"));
		    map.put("hrefTarget", hrefTarget);
		    map.put("hrefURL", ((hrefURL == null)?
					map.get("highImageURL"):
					WebEncoder.quoteEncode(hrefURL)));
		    map.put("imageFile",
			    URLEncoder.encode(fname, "UTF-8"));
		    map.put("name", name);
		    map.put("mediumFile",
			    URLEncoder.encode(name +"." +
					      extension,
					      "UTF-8"));
		    map.put("width", "" + scaler.getLastImageWidth());
		    map.put("height", "" + scaler.getLastImageHeight());
		    map.put("ext", extension);
		    map.put("index", "" + flist.size());
		    map.put("imageURL", name + "."
			    + URLEncoder.encode(extension, "UTF-8"));
		    map.put("imageHtmlURL", name +"."
			    + "html");
		    String otherProps = parser.getValue("otherProps", ind);
		    String x = parser.getValue("title", ind);
		    if (x != null) {
			if (single) {
			    map.put("title", WebEncoder.htmlEncode(x));
			} else {
			    map.put("imgTitle", WebEncoder.htmlEncode(x));
			}
			x = WebEncoder.quoteEncode(x);
			if (otherProps == null) {
			    otherProps = ", title: \"" + x + "\"";
			} else {
			    otherProps = otherProps + ", title: \"" + x + "\"";
			}
		    }
		    /*
		    x = parser.getValue("descr", ind);
		    if (x != null) {
			x = WebEncoder.quoteEncode(x);
			if (otherProps == null) {
			    otherProps = ", descr: \"" + x + "\"";
			} else {
			    otherProps = otherProps + ", descr: \"" + x + "\"";
			}
		    }
		    */
		    x = parser.getValue("imageTime", ind);
		    if (x != null) {
			if (otherProps == null) {
			    otherProps = ", duration: \"" + x + "\"";
			} else {
			    otherProps = otherProps + ", duration: \""
				+ x + "\"";
			}
		    }

		    x = parser.getValue("minImageTime", ind);
		    if (x != null) {
			if (otherProps == null) {
			    otherProps = ", minImageTime: \"" + x + "\"";
			} else {
			    otherProps = otherProps + ", minImageTime: \""
				+ x + "\"";
			}
		    }

		    if (hrExt != null) {
			if (otherProps == null) {
			    otherProps = ", hrExt: \"" +hrExt + "\"";
			} else {
			    otherProps = otherProps +
				", hrExt: \"" +hrExt + "\"";
			}
		    }
		    if (otherProps != null) {
			map.put("otherProps", otherProps);
		    }
		    map.put("commaSeparator", ",");
		    if (tiledImages) {
			int modulus = tiledWidth / maxThumbWidth;
			if ((ind+1) < n && ((ind + 1) % modulus) == 0) {
			    map.put("newTableRow", "</tr><tr>");
			}
		    }
		    flist.add(map);
		    if (lind == -1) {
			ofn = "thumbnails/"
			    + ifn +"." + extension;
		    } else {
			ofn = "thumbnails/"
			    +ifn.substring(0, lind)
			    + "." + extension;
		    }

		    if (multi) {
			zos.setLevel(0);
			zos.setMethod(ZipOutputStream.STORED);
			ZipEntry ze = new ZipEntry(ofn);
			ByteArrayOutputStream bos =
			    new ByteArrayOutputStream(2<<16);
			if (urlmode) {
			    scaler.scaleImage(lparms.getTWidth(),
					    lparms.getTHeight(),
					    url, bos, type);
			} else {
			    scaler.scaleImage(lparms.getTWidth(),
					    lparms.getTHeight(),
					    inputFileStr, bos, type);
			}
			int sz = bos.size();
			byte[] array = bos.toByteArray();
			CRC32 crc = new CRC32();
			crc.update(array);
			ze.setSize(sz);
			ze.setCompressedSize(sz);
			ze.setCrc(crc.getValue());
			zos.putNextEntry(ze);
			zos.write(array, 0, sz);
			array = null; bos.close(); bos = null;
			zos.closeEntry();
			String ofnHTML = null;
			if (lind == -1) {
			    ofn = "medium/" + ifn +"." + extension;
			    ofnHTML = "medium/" + ifn + ".html";
			} else {
			    ofn = "medium/"
				+ifn.substring(0, lind)
				+ "." + extension;
			    ofnHTML = "medium/"
				+ ifn.substring(0, lind) + ".html";
			}

			zos.setLevel(0);
			zos.setMethod(ZipOutputStream.STORED);
			ze = new ZipEntry(ofn);
			bos = new ByteArrayOutputStream(2<<16);
			if (urlmode) {
			    scaler.scaleImage(lparms.getMWidth(),
					    lparms.getMHeight(),
					    url, bos, type);
			} else {
			    scaler.scaleImage(lparms.getMWidth(),
					    lparms.getMHeight(),
					    inputFileStr, bos, type);
			}
			sz = bos.size();
			array = bos.toByteArray();
			crc = new CRC32();
			crc.update(array);
			ze.setSize(sz);
			ze.setCompressedSize(sz);
			ze.setCrc(crc.getValue());
			zos.putNextEntry(ze);
			zos.write(array, 0, sz);
			array = null; bos.close(); bos = null;
			zos.closeEntry();
			map.put("width", "" + scaler.getLastImageWidth());
			map.put("height", "" + scaler.getLastImageHeight());
			TemplateProcessor mp = new TemplateProcessor(rmap,
						       map);
			zos.setMethod(ZipOutputStream.DEFLATED);
			zos.setLevel(9);
			ze = new ZipEntry(ofnHTML);
			zos.putNextEntry(ze);
			mp.processSystemResource
			    ("webnail/mediumHTML.wnt", "UTF-8",
			     zos);
			zos.closeEntry();
		    }
		}
	    }
	    if (pm != null) pm.incrProgressCount();
	}
	if (webmode) {
	    if (flist.size() > 0) {
		flist.getLast().remove("commaSeparator");
	    }
	    rmap.put("repeatRows",
		     flist.toArray(new
				   TemplateProcessor.KeyMap[flist
						     .size()]));
	    rmap.put("repeatImageArrayEntries",
		     rmap.get("repeatRows"));

	    // rmap.put("repeatDomEntries", parser.getDomArray());

	    TemplateProcessor tp = new TemplateProcessor(rmap);

	    if (dir != null) {
		File indexFile = new File(dir, "index.html");
		tp.processURL(parser.getTemplateURL(), "UTF-8", indexFile);
		if (multi /*parser.getLayoutIndex() == 0*/) {
		    File strutFile = new File(cdir, "strut.gif");
		    CopyUtilities.copyResourceToFile("webnail/strut.gif",
						     strutFile);
		    File initImage = new File(cdir, "initImage.png");
		    CopyUtilities.copyResourceToFile("webnail/initImage.png",
						     initImage);
		    File fleft = new File(cdir, "fleft.gif");
		    CopyUtilities.copyResourceToFile(rvmode?
						     "webnail/fleftRV.gif":
						     "webnail/fleft.gif",
						     fleft);
		    File left = new File(cdir, "left.gif");
		    CopyUtilities.copyResourceToFile(rvmode?
						     "webnail/leftRV.gif":
						     "webnail/left.gif",
						     left);
		    File expand = new File(cdir, "expand.png");
		    CopyUtilities.copyResourceToFile(rvmode?
						     "webnail/expandRV.png":
						     "webnail/expand.png",
						     expand);
		    File right = new File(cdir, "right.gif");
		    CopyUtilities.copyResourceToFile(rvmode?
						     "webnail/rightRV.gif":
						     "webnail/right.gif",
						     right);
		    File fright = new File(cdir, "fright.gif");
		    CopyUtilities.copyResourceToFile(rvmode?
						     "webnail/frightRV.gif":
						     "webnail/fright.gif",
						     fright);
		    File slideshow1 = new File(cdir, "slideshow1.js");
		    CopyUtilities.copyResourceToFile("webnail/slideshow1.js",
						     slideshow1);
		    File slideshow2 = new File(cdir, "slideshow2.js");
		    CopyUtilities.copyResourceToFile("webnail/slideshow2.js",
						     slideshow2);
		    File params = new File(cdir, "params.js");
		    tp.processSystemResource("webnail/params.wnt", "UTF-8",
					     params);
		    File tindex = new File(tdir, "index.html");
		    tp.processSystemResource("webnail/tindexHTML.wnt", "UTF-8",
					     tindex);
		    File tindex90 = new File(tdir, "index90.html");
		    tp.processSystemResource("webnail/tindex90HTML.wnt",
					     "UTF-8", tindex90);
		    File initial = new File(cdir, "initial.html");
		    CopyUtilities.copyResourceToFile("webnail/initial.html",
						     initial);
		    File medium = new File(cdir, "medium.html");
		    CopyUtilities.copyResourceToFile("webnail/medium.html",
						     medium);
		    /*
		    File slideshow = new File(cdir, "slideshow.html");
		    tp.processSystemResource("webnail/slideshowHTML.wnt",
					     "UTF-8",
					     slideshow);
		    */
		}
		if (warmode) {
		    File webxml = new File(wdir, "web.xml");
		    createWebXml(title, webxml);
		    File errorFile = new File(cdir, "error.jsp");
		    CopyUtilities.copyResourceToFile("webnail/error.jsp",
						     errorFile);
		}
	    } else if (zos != null) {
		zos.setMethod(ZipOutputStream.DEFLATED);
		zos.setLevel(9);
		ZipEntry ze = new ZipEntry("index.html");
		zos.putNextEntry(ze);
		tp.processURL(parser.getTemplateURL(), "UTF-8", zos);
		zos.closeEntry();
		if (multi /*parser.getLayoutIndex() == 0*/) {
		    CopyUtilities.copyResourceToZipStream("webnail/strut.gif",
							  "controls/strut.gif",
							  zos, true);
		    CopyUtilities.copyResourceToZipStream
			("webnail/initImage.png",
			 "controls/initImage.png", zos, true);
		    CopyUtilities.copyResourceToZipStream("webnail/fleft.gif",
							  "controls/fleft.gif",
							  zos, true);
		    CopyUtilities.copyResourceToZipStream("webnail/left.gif",
							  "controls/left.gif",
							  zos, true);
		    CopyUtilities.copyResourceToZipStream("webnail/expand.png",
							  "controls/expand.png",
							  zos, true);
		    CopyUtilities.copyResourceToZipStream("webnail/right.gif",
							  "controls/right.gif",
							  zos, true);
		    CopyUtilities.copyResourceToZipStream("webnail/fright.gif",
							  "controls/fright.gif",
							  zos, true);
		    CopyUtilities.copyResourceToZipStream
			("webnail/slideshow1.js", "controls/slideshow1.js",
			 zos, false);
		    CopyUtilities.copyResourceToZipStream
			("webnail/slideshow2.js",
			 "controls/slideshow2.js", zos, false);
		    zos.setMethod(ZipOutputStream.DEFLATED);
		    zos.setLevel(9);
		    ze = new ZipEntry("thumbnails/index.html");
		    zos.putNextEntry(ze);
		    tp.processSystemResource("webnail/tindexHTML.wnt", "UTF-8",
					     zos);
		    zos.closeEntry();
		    zos.setMethod(ZipOutputStream.DEFLATED);
		    zos.setLevel(9);
		    ze = new ZipEntry("controls/params.js");
		    zos.putNextEntry(ze);
		    tp.processSystemResource("webnail/params.wnt", "UTF-8",
					     zos);
		    zos.closeEntry();

		    CopyUtilities.copyResourceToZipStream
			("webnail/initial.html", "controls/initial.html",
			 zos, false);
		    CopyUtilities.copyResourceToZipStream
			("webnail/medium.html", "controls/medium.html",
			 zos, false);
		    zos.setMethod(ZipOutputStream.DEFLATED);
		    /*
		     zos.setLevel(9);
		    ze = new ZipEntry("controls/slideshow.html");
		    zos.putNextEntry(ze);
		    tp.processSystemResource("webnail/slideshowHTML.wnt",
					     "UTF-8",
					     zos);
		    zos.closeEntry();
		    */
		}
		if (warmode) {
		    zos.setMethod(ZipOutputStream.DEFLATED);
		    zos.setLevel(9);
		    ze = new ZipEntry("WEB-INF/web.xml");
		    zos.putNextEntry(ze);
		    createWebXml(title, zos);
		    zos.closeEntry();
		    CopyUtilities.copyResourceToZipStream("webnail/error.jsp",
							  "controls/error.jsp",
							  zos, false);
		}
	    }
	}
	if (zos != null) {
	    zos.finish();
	    zos.close();
	}
	// Let caller do it - that stops the progress monitor if we exit
	// early due to an exception.
	// if (pm != null) pm.stopProgress();
    }


    private static void checkForMissingArg(int ind, int len) {
	if (ind >= len) {
	    System.err.println(localeString("missingArgument"));
	    System.exit(1);
	}
    }


    public static EmbeddedWebServer openBrowser(String fname, int port)
	throws Exception
    {
	File cdir = new File(System.getProperty("user.dir"));
	File f = new File(fname);
	if (!f.isAbsolute()) {
	    f = new File (cdir, fname);
	}
	EmbeddedWebServer ews = new EmbeddedWebServer(port, 48, 2, null);
	if (port == 0) port = ews.getPort();
	if (f.isDirectory()) {
	    File wx = new File(f, "WEB-INF" + File.separator + "web.xml");
	    boolean noWebxml = !wx.isFile() || !wx.canRead();
	    ews.add("/", DirWebMap.class, f, null, noWebxml, true, false);
	} else {
	    String name = f.getName();
	    if (name.endsWith(".zip")) {
		boolean noWebxml = false;
		ZipFile zf = null;
		try {
		    zf = new ZipFile(f);
		    ZipEntry ze = zf.getEntry("WEB-INF/web.xml");
		    noWebxml = (ze == null);
		} catch (IOException e) {
		    noWebxml = true;
		    // System.out.println("IOException in checking ZIP files");
		} finally {
		    if (zf != null) zf.close();
		}
		ews.add("/", ZipWebMap.class, f,
			null, noWebxml, true, !noWebxml);
	    } else if (name.endsWith(".war")) {
		    ews.add("/", ZipWebMap.class, f, null, false, true, true);
	    } else {
		throw new IOException(localeString("notDirZipWar"));
	    }
	}
	WebMap wmap = ews.getWebMap("/");
	if (wmap != null) wmap.addWelcome("index.html");
	ews.start();
	URI uri = new URL("http://localhost:" + port + "/").toURI();
	Desktop.getDesktop().browse(uri);
	return ews;
    }

    /*
     * main program.
     * @param argv command-line arguments consisting of the maximum
     *        thumbnail image width in pixels, followed by the mazimum
     *        thumbnail image height in pixels, followed by any number
     *        of pairs of arguments consisting of an input file name
     *        followed by an output file name.
     */
    public static void main(String[] argv) {

	if (argv.length == 0 || (argv.length == 1 && !argv[0].startsWith("-"))
	    || argv[0].equals("--gui")) {
	    Gui.configureGui();
	    if (argv.length == 1 || argv.length == 2) {
		/*
		 * We have a single argument - an xml file or
		 * a wnl file (Webnail xml input file).  With
		 * the Gui configured, we just read the file in
		 * as if we had opened it.  The --gui argument is provided
		 * for supporting window systems (e.g., the Gnome
		 * desktop files).
		 */
		if (argv[0].equals("--gui")) {
		    Gui.checkCdir = true;
		}
		final String name = argv[argv.length - 1];
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    try {
				URL iurl;
				if (name.startsWith("http://")
				    || name.startsWith("https://")
				    || name.startsWith("ftp://")) {
				    iurl = new URL(name);
				    Gui.load(iurl);
				    Thread w = Gui.worker;
				    if (w != null) {
					try {
					    w.join();
					} catch (Exception we) {}
				    }
				    Gui.checkConsole();
				} else if (name.startsWith("file:")) {
				    iurl = new URL(name);
				    File f = new File(iurl.toURI());
				    Gui.load(f.getCanonicalPath(),
					     new FileInputStream(f));
				} else if (name.endsWith(".xml")
					   || name.endsWith(".wnl")) {
				    Gui.load(name, new FileInputStream(name));
				}
			    } catch (Exception e) {
				SwingErrorMessage.display(e);
				Gui.checkConsole();
			    }
			}
		    });
	    } else if (argv.length > 3) {
		System.err.println(localeString("tooManyArgsAfterGUI"));
		System.exit(1);
	    }
	    return;
	}
	/*
	 * If we get here, we are running in command-line mode.
	 */
	File dir = null;
	File tdir = null; File mdir = null; File cdir = null;
	File idir = null;
	File wdir = null;
	File zipFile = null;
	boolean useStdout = false;
	String xmlFilename = null;
	File xmlFile = null;
	URL xmlURL = null;
	boolean useStdin = false;
	ZipOutputStream zos = null;
	boolean warmode = false;
	int index = 0;
	boolean webmode = false;
	boolean zipped = false;
	boolean urlmode = false;
	boolean linkmode = false;
	String user = null;
	char[] password = null;

	String windowTitle = "Photos";
	String title = "";
	String descr = "";
	String iFrameWindowTitle = "Medium-Resolution Image";
	boolean flat = false;
	boolean hasAllImages = true;
	boolean syncMode = false;
	boolean waitOnError = false;
	String bgcolor = null;
	String fgcolor = null;
	boolean rvmode = false;
	boolean hrefToOrig = false;

	long imageTime = DEFAULT_IMAGE_TIME;
	long minImageTime = DEFAULT_MIN_IMAGE_TIME;

	String mtype = "image/jpeg";

	int port = 0;
	String bfile = null;

	type = ImageMimeInfo.getFormatNameForMimeType(mtype);
	extension = ImageMimeInfo.getExtensionForMimeType(mtype);
	while (index < argv.length) {
	    if (argv[index].startsWith("-")) {
		if (argv[index].equals("-l")) {
		    System.out.println(localeString("outputFormats"));
		    for (String name: ImageMimeInfo.getMimeTypes()) {
			System.out.print("    " + name + " (");
			boolean ft = true;
			for (String ext: ImageMimeInfo.getSuffixes(name)) {
			    System.out.print((ft? ".": " .") + ext);
			    ft = false;
			}
			System.out.println(")");
		    }
		    System.exit(0);
		} else if (argv[index].equals("-a")) {
		    index++;
		    checkForMissingArg(index, argv.length);
		    user = argv[index];
		    index++;
		    checkForMissingArg(index, argv.length);
		    password = argv[index].toCharArray();
		} else if (argv[index].equals("-u")) {
		    if (noxml ||  !(xmlFile == null && xmlURL == null)) {
			noXML(xmlFile, xmlURL);
		    }
		    urlmode = true;
		} else if (argv[index].equals("-U")) {
		    noXML(xmlFile, xmlURL);
		    urlmode = true;
		    linkmode = true;
		} else if (argv[index].equals("-C")) {
		    noXML(xmlFile, xmlURL);
		    index++;
		    checkForMissingArg(index, argv.length);
		    bgcolor = argv[index];
		} else if (argv[index].equals("-X")) {
		    noXML(xmlFile, xmlURL);
		    index++;
		    checkForMissingArg(index, argv.length);
		    fgcolor = argv[index];
		} else if (argv[index].equals("-r")) {
		    noXML(xmlFile, xmlURL);
		    rvmode = true;
		} else if (argv[index].equals("-t")) {
		    noXML(xmlFile, xmlURL);
		    index++;
		    checkForMissingArg(index, argv.length);
		    if (index == argv.length) {
			System.err.println(localeString("arg-t-missing"));
			System.exit(1);
		    }
		    mtype = argv[index];
		    String ext =
			ImageMimeInfo.getExtensionForMimeType(argv[index]);
		    String fmt =
			ImageMimeInfo.getFormatNameForMimeType(argv[index]);
		    if (ext == null || fmt == null) {
			System.err.println(localeString("arg-t-invalid"));
			System.exit(1);
		    } else {
			type = fmt;
			extension = ext;
		    }
		} else if (argv[index].equals("-F")) {
		    noXML(xmlFile, xmlURL);
		    flat = true;
		} else if (argv[index].equals("-d")) {
		    index++;
		    if (index == argv.length) {
			System.err.println(localeString("arg-d-missing"));
			System.exit(1);
		    }
		    dir = new File(argv[index]);
		    if (!dir.exists()) {
			if (!dir.mkdirs()) {
			    System.err.println(localeString
					       ("dirCreateFailed"));
			    System.exit(1);
			}
			if (!dir.setWritable(true)) {
			    System.err.println(localeString
					       ("dirNotWritable"));
			    System.exit(1);
			}
		    }
		    if (!(dir.isDirectory() && dir.canWrite())) {
			System.err.println(localeString("dirError"));
			System.exit(1);
		    }
		} else if (argv[index].equals("-z")) {
		    index++;
		    zipped = true;
		    if (index == argv.length) {
			System.err.println(localeString("arg-z-missing"));
			System.exit(1);
		    }
		    zipFile = new File(argv[index]);
		    if (argv[index].equals("-")) {
			useStdout = true;
		    }
		    if (!useStdout && zipFile.exists() && !zipFile.isFile()) {
			System.err.println(String.format
					   (localeString("zipNotFile"),
					    argv[index]));
			System.exit(1);
		    }
		    if (!useStdout && zipFile.exists() && !zipFile.canWrite()) {
			System.err.println(String.format
					   (localeString("zipNotWritable"),
					    argv[index]));
			System.exit(1);
		    }
		    if (!useStdout && zipFile.getName().endsWith(".war")) {
			warmode = true;
			webmode = true;
		    }
		} else if (argv[index].equals("-f")) {
		    index++;
		    checkForMissingArg(index, argv.length);
		    if (urlmode) {
			try {
			    xmlURL = new URL(argv[index]);
			    noXML(xmlFile, xmlURL);
			    urlmode = false;
			} catch (MalformedURLException mfue) {
			    System.err.println("");
			    System.exit(1);
			}
		    } else {
			if (argv[index].equals("-")) {
			    useStdin = true;
			}
			xmlFilename = argv[index];
			xmlFile = new File(argv[index]);
			noXML(xmlFile, xmlURL);
			if (!useStdin && !xmlFile.isFile()) {
			    System.err.println(String.format
					       (localeString("inputFileError"),
						argv[index]));
			    System.exit(1);
			}
		    }
		} else if (argv[index].equals("-i")) {
		    noXML(xmlFile, xmlURL);
		    warmode = true;
		    webmode = true;
		} else if (argv[index].equals("-w")) {
		    noXML(xmlFile, xmlURL);
		    webmode = true;
		} else if (argv[index].equals("-W")) {
		    noXML(xmlFile, xmlURL);
		    waitOnError = true;
		} else if (argv[index].equals("-S")) {
		    noXML(xmlFile, xmlURL);
		    syncMode = true;
		} else if (argv[index].equals("-M")) {
		    noXML(xmlFile, xmlURL);
		    hasAllImages = false;
		} else if (argv[index].equals("-T")) {
		    noXML(xmlFile, xmlURL);
		    index++;
		    checkForMissingArg(index, argv.length);
		    title = argv[index];
		} else if (argv[index].equals("-X")) {
		    noXML(xmlFile, xmlURL);
		    index++;
		    checkForMissingArg(index, argv.length);
		    windowTitle = argv[index];
		} else if (argv[index].equals("-D")) {
		    noXML(xmlFile, xmlURL);
		    index++;
		    checkForMissingArg(index, argv.length);
		    descr = argv[index];
		} else if (argv[index].equals("-I")) {
		    noXML(xmlFile, xmlURL);
		    index++;
		    checkForMissingArg(index, argv.length);
		    if (index == argv.length) {
			System.err.println(localeString("arg-I-missing"));
			System.exit(1);
		    }
		    try {
			imageTime = Long.parseLong(argv[index]);
		    } catch(NumberFormatException eI) {
			System.err.println(localeString("arg-I-invalid"));
			System.exit(1);
		    }
		} else if (argv[index].equals("-L")) {
		    noXML(xmlFile, xmlURL);
		    index++;
		    if (index == argv.length) {
			System.err.println(localeString("arg-L-missing"));
			System.exit(1);
		    }
		    try {
			minImageTime = Long.parseLong(argv[index]);
		    } catch (NumberFormatException eL) {
			System.err.println(localeString("arg-L-invalid"));
			System.exit(1);
		    }
		} else if (argv[index].equals("-H")) {
		    noXML(xmlFile, xmlURL);
		    // index++;
		    hrefToOrig = true;
		} else if (argv[index].equals("--port")) {
		    noXML(xmlFile, xmlURL);
		    index++;
		    checkForMissingArg(index, argv.length);
		    try {
			port = Integer.parseInt(argv[index]);
			if (port < 0 || port >= (1<<16)) {
			    System.err.println(localeString("badRange"));
			}
		    } catch (Exception eport) {
			System.err.println("badPort");
			System.exit(1);
		    }
		} else if (argv[index].equals("--browse")) {
		    noXML(xmlFile, xmlURL);
		    index++;
		    checkForMissingArg(index, argv.length);
		    bfile = argv[index];
		} else {
		    System.err.println(String.format
				       (localeString("unknownOption"),
					argv[index]));
		    System.exit(1);
		}
	    } else {
		break;
	    }
	    index++;
	}

	if (bfile != null) {
	    try {
		openBrowser(bfile, port);
		return;
	    } catch (Exception be) {
		System.err.println(be.getMessage());
		System.exit(1);
	    }
	}

	if (user != null && password != null) {
	    final String xuser = user;
	    final char[] xpwd = password;
	    user = null; password = null;
	    Authenticator.setDefault(new Authenticator() {
		    protected PasswordAuthentication
			getPasswordAuthentication()
		    {
			return new PasswordAuthentication(xuser, xpwd);
		    }
		});
	}

	if (zipFile != null && flat) {
	    System.err.println(localeString("zipFlatError"));
	    System.exit(1);
	}
	int maxThumbWidth = 0;
	int maxThumbHeight = 0;
	if ((argv.length - index) >= 2) {
	    maxThumbWidth = Integer.parseInt(argv[index + 0]);
	    maxThumbHeight = Integer.parseInt(argv[index + 1]);
	    if (maxThumbWidth != 0 || maxThumbHeight != 0) {
		flat = false;
	    }
	}
	if (dir == null && zipFile == null) {
	    if (xmlFile != null || xmlURL != null) {
		// if there is no output file or directory, just
		// parse the input and report errors.
		try {
		    org.bzdev.protocols.Handlers.enable();
		    Parser parser = new Parser();
		    if (xmlURL != null) {
			URLConnection c = xmlURL.openConnection();
			if (c instanceof HttpURLConnection) {
			    c.setRequestProperty("accept", ACCEPT_VALUE);
			    ((HttpURLConnection) c).setRequestMethod("GET");
			    if (((HttpURLConnection) c).getResponseCode()
				!= HttpURLConnection.HTTP_OK) {
				throw new IOException(String.format
						      (localeString
						       ("couldNotConnect"),
						       xmlURL.toString()));
			    }
			}
			String ct = c.getContentType();
			if (ct != null) {
			    if ((!ct.equals(XML_MIME_TYPE)) &&
				(!ct.equals(WEBNAIL_XML_MIME_TYPE)) &&
				(!ct.equals(ALT_XML_MIME_TYPE))) {
				throw new Exception
				    (String.format(localeString
						   ("notWebnailFile"),
						   xmlURL.toString(), ct));
			    }
			} else {
			    InputStream is = c.getInputStream();
			    ct = c.guessContentTypeFromStream(is);
			    is.close();
			    if (ct == null) {
				ct = "application/octet-stream";
			    }
			    if ((!ct.equals(XML_MIME_TYPE)) &&
				(!ct.equals(WEBNAIL_XML_MIME_TYPE)) &&
				(!ct.equals(ALT_XML_MIME_TYPE))) {
				throw new Exception
				    (String.format
				     (localeString("notWebnailFile"),
				      xmlURL.toString(), ct));
			    }
			}
		    } else if (xmlFile != null) {
			URL xmlFileURL = xmlFile.toURI().toURL();
			URLConnection c = xmlFileURL.openConnection();
			String ct =c.getContentType();
			if ((ct != null)) {
			    if ((!ct.equals(XML_MIME_TYPE)) &&
			    (!ct.equals(WEBNAIL_XML_MIME_TYPE)) &&
				(!ct.equals(ALT_XML_MIME_TYPE))) {
				throw new Exception
				    (String.format
				     (localeString("notWebnailFile"),
				      xmlURL.toString(), ct));
			    }
			} else {
			    InputStream is = c.getInputStream();
			    ct = c.guessContentTypeFromStream(is);
			    is.close();
			    if (ct == null) {
				ct = "application/octet-stream";
			    }
			    if ((!ct.equals(XML_MIME_TYPE)) &&
				(!ct.equals(WEBNAIL_XML_MIME_TYPE)) &&
				(!ct.equals(ALT_XML_MIME_TYPE))) {
				throw new Exception
				    (String.format
				     (localeString("notWebnailFile"),
				      xmlURL.toString(), ct));
			    }
			}
		    }
		    parser.parse((xmlURL != null)? xmlURL.openStream():
				 (useStdin? System.in:
				  new FileInputStream(xmlFile)));
		} catch (Exception e) {
		    SwingErrorMessage.display(e);
		    System.exit(1);
		}
		System.exit(0);
	    }
	    if (((argv.length - index) % 2) != 0 || (argv.length - index) < 2
		|| webmode) {
		usage();
		System.exit(1);
	    }
	} else {
	    if ((xmlFile == null && xmlURL == null)
		&& argv.length - index < 2) {
		usage();
		System.exit(1);
	    }
	    if ((xmlFile != null || xmlURL != null) && argv.length != index) {
		usage();
		System.exit(1);
	    }
	    if (dir != null && webmode) {
	    }
	}
	if ((argv.length - index) != 0 || xmlFile != null || xmlURL != null) {
	    try {
		if (zipFile != null) {
		    OutputStream os = useStdout? System.out:
			new FileOutputStream(zipFile);
		    zos = warmode? new JarOutputStream(os):
			new ZipOutputStream(os);
		    zos.setLevel(0);
		    zos.setMethod(ZipOutputStream.STORED);
		}

		if (dir == null && zos == null) {
		    for (int ind = index + 2; ind < argv.length; ind++, ind++) {
			File f = urlmode? null: new File(argv[ind]);
			URL url = urlmode? new URL(argv[ind]): null;
			if (!urlmode && !(f.isFile() && f.canRead())) {
			    System.err.println
				(String.format(localeString("skipMsg"),
					       argv[ind]));

			    continue;
			}
			if (urlmode) {
			    scaler.scaleImage(maxThumbWidth, maxThumbHeight,
					    url, new File(argv[ind+1]), type);
			} else {
			    scaler.scaleImage(maxThumbWidth, maxThumbHeight,
					    argv[ind], argv[ind+1], type);
			}
		    }
		} else {
		    org.bzdev.protocols.Handlers.enable();
		    Parser parser = new Parser();
		    if (xmlFile == null && xmlURL == null) {
			parser.setAttributes(mtype, hasAllImages,
					     webmode, warmode, zipped,
					     linkmode, flat,
					     syncMode, waitOnError,
					     imageTime, minImageTime,
					     bgcolor, fgcolor, rvmode,
					     maxThumbWidth, maxThumbHeight,
					     hrefToOrig);
			if (title != null) parser.setTitle(title);
			if (descr != null) parser.setDescr(descr);
			if (windowTitle != null) {
			    parser.setWindowTitle(windowTitle);
			}
			for (int ind = index + 2; ind < argv.length; ind++) {
			    parser.startImage();
			    if (urlmode) {
				parser.setURL( argv[ind]);
			    } else {
				parser.setFilename(argv[ind]);
			    }
			    parser.imageComplete();
			}
			parser.imagesComplete();
		    } else {
			parser.setXMLFilename(xmlFilename);
			if (xmlURL != null) {
			    URLConnection c = xmlURL.openConnection();
			    if (c instanceof HttpURLConnection) {
				c.setRequestProperty("accept", ACCEPT_VALUE);
				((HttpURLConnection) c).setRequestMethod("GET");
				if (((HttpURLConnection) c).getResponseCode()
				    != HttpURLConnection.HTTP_OK) {
				    throw new IOException(String.format
							  (localeString
							   ("couldNotConnect"),
							   xmlURL.toString()));
				}
			    }
			    String ct = c.getContentType();
			    if ((!ct.equals(XML_MIME_TYPE)) &&
				(!ct.equals(WEBNAIL_XML_MIME_TYPE)) &&
				(!ct.equals(ALT_XML_MIME_TYPE))) {
				throw new
				    Exception(String.format
					      (localeString("notWebnailFile"),
					       xmlURL.toString(), ct));
			    }
			} else if (xmlFile != null) {
			    URL xmlFileURL = xmlFile.toURI().toURL();
			    URLConnection c = xmlFileURL.openConnection();
			    String ct = c.getContentType();
			    if ((!ct.equals(XML_MIME_TYPE)) &&
				(!ct.equals(WEBNAIL_XML_MIME_TYPE)) &&
				(!ct.equals(ALT_XML_MIME_TYPE))) {
				throw new
				    Exception(String.format
					      (localeString("notWebnailFile"),
					       xmlURL.toString(), ct));
			    }
			}
			parser.parse((xmlURL != null)? xmlURL.openStream():
				     (useStdin? System.in:
				      new FileInputStream(xmlFile)));
		    }
		    // parser.printState(System.out);
		    generate(parser, dir, zos, null);
		}
	    } catch (Exception e) {
		System.err.println
		    (String.format(localeString("errorMsg"),
				   e.toString()));
		e.printStackTrace();
		System.exit(1);
	    }
	}
	System.exit(0);
    }

}
