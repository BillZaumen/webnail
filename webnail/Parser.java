package webnail;

import java.awt.Component;
import javax.xml.parsers.*;
import org.xml.sax.helpers.*;
import org.xml.sax.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.text.MessageFormat;
import java.text.DecimalFormat;
import java.nio.*;
import java.nio.charset.Charset;
import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.TemplateProcessor.KeyMap;
import org.bzdev.swing.ErrorMessage;
import org.bzdev.imageio.ImageMimeInfo;
import org.bzdev.net.WebEncoder;

public class Parser {

    static final String PUBLICID = "-//wtz//Webnail 1.0//EN";
    static final String SYSTEMID = "http://bzdev.org/DTD/webnail-1.0.dtd";
    static final String OUR_SYSTEMID = "resource:webnail-1.0.dtd";

    Component comp = null;
    public void setComponent(Component c) {
	comp = c;
    }


    static private final String resourceBundleName = "webnail/Parser";
    static ResourceBundle bundle = 
	ResourceBundle.getBundle(resourceBundleName);
    static String localeString(String name) {
	return bundle.getString(name);
    }

    SAXParser parser;

    /*
    public void setAuthenticationParams(final String user, 
					final String password) 
    {
	if (user != null) {
	    Authenticator.setDefault(new Authenticator() {
		    protected PasswordAuthentication 
			getPasswordAuthentication() {
			return 
			    new PasswordAuthentication(user,
						       password.toCharArray());
		    }
            });
	} else {
	    Authenticator.setDefault(null);
	}
    }

    private String epassword = null;
    private String dpassword = null;

    public void setEncryptionPassword(String pw) {
	epassword = pw;
    }

    public void setDecryptionPassword(String pw) {
	dpassword = pw;
    }


    private String decrypt(String password) {
	// add decryption later
	return password;
    }

    private String encrypt(String password) {
	// add encryption later
	return password;
    }
    */
    public Parser() throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        parser = factory.newSAXParser();
    }

    public void parse(InputStream is) throws SAXException, IOException {
        OurDefaultHandler handler = new OurDefaultHandler();
	webmode = false;
	linkmode = false;
	flatmode = false;
	highResMode = true;
	webArchiveMode = false;
	syncMode = false;
	waitOnError = false;
	rmap = new TemplateProcessor.KeyMap();
	map = null;
        parser.parse(is, handler);
        if (handler.errorSeen)
            throw new SAXException(localeString("badDocument"));
	// write(System.out);
    }

    void displayMessage(String msg, String title) {
	ErrorMessage.display(msg/*, title*/);
        // simplify for now
        // System.err.println(msg);
    }

    String xmlFilename = null;
    
    public void setXMLFilename(String name) {
	xmlFilename = name;
    }


    void displayMessage(Locator locator, 
			String msg, String title) {
	ErrorMessage.display(xmlFilename, locator.getLineNumber(), msg);
    }

    TemplateProcessor.KeyMap rmap = new TemplateProcessor.KeyMap();
    TemplateProcessor.KeyMap map = null;
    TemplateProcessor.KeyMap domMap = null;

    LinkedList<TemplateProcessor.KeyMap> ilist = 
	new LinkedList<TemplateProcessor.KeyMap>();

    LinkedList<TemplateProcessor.KeyMap> domlist = 
	new LinkedList<TemplateProcessor.KeyMap>();


    TemplateProcessor.KeyMap[] iArray = null;
    TemplateProcessor.KeyMap[] domArray = null;
    int propIndex = 0;


    public void setAttributes (String mtype, boolean hrmode, 
			       boolean webMode, boolean webArchiveMode,
			       boolean linkMode, boolean flatMode,
			       boolean syncMode, boolean waitOnError,
			       long imageTime, long minImageTime,
			       String bgcolor, int width, int height,
			       boolean hrefToOrig)
    {
	mimeType = (mtype == null)? "image/jpeg": mtype;
	webmode = webMode;
	linkmode = linkMode;
	flatmode = flatMode;
	highResMode = hrmode;
	this.webArchiveMode = webArchiveMode;
	this.syncMode = syncMode;
	rmap.put("syncMode", "" + syncMode);
	this.waitOnError = waitOnError;
	rmap.put("waitOnError", "" + waitOnError);
	if (imageTime == -1) {
	    rmap.put("imageTime", "*");
	} else if (imageTime == -2) {
	    rmap.put("imageTime", "?");
	} else {
	    rmap.put("imageTime", "" + imageTime);
	}
	if (minImageTime == -1) minImageTime = 0;
	rmap.put("minImageTime", "" + minImageTime);
	rmap.put("bgcolor", 
		 ((bgcolor == null)? Webnail.DEFAULT_BGCOLOR: bgcolor));

	this.width = width;
	this.height = height;
	this.hrefToOrig = hrefToOrig;
	// default values of various attributes
	rmap.put("windowTitle", localeString("windowTitle"));

    }

    public void setWindowTitle(String title) {
	rmap.put("windowTitle", title);
    }

    public void setTitleURL(String titleURL) {
	((map == null)? rmap: map).put("titleURL", titleURL);
	try {
	    loadText(new URL(titleURL));
	    setTitle(text.toString());
	    text.setLength(0);
	} catch (MalformedURLException urle) {
	    ErrorMessage.display(String.format
				 (localeString("malformedURL2"),
				  titleURL.toString()),
				 localeString("titleErrorTitle"),
				 comp);
	    // System.err.println(urle.getMessage());
	} catch (IOException eio) {
	    ErrorMessage.display(MessageFormat.format
				 (localeString("ioErrorURL"),
				  eio.getMessage(),
				  titleURL.toString()),
				 localeString("titleErrorTitle"),
				 comp);
	    // System.err.println(eio.getMessage());
	}
    }
    public void setTitle(String title) {
	((map == null)? rmap: map).put("title", title);
    }

    public void setDescrURL(String descrURL) {
	((map == null)? rmap: map).put("descrURL", descrURL);
	try {
	    loadText(new URL(descrURL));
	    setDescr(text.toString());
	    text.setLength(0);
	} catch (MalformedURLException urle) {
	    ErrorMessage.display(String.format
				 (localeString("malformedURL2"),
				  descrURL.toString()),
				 localeString("descrErrorTitle"),
				 comp);
	    // System.err.println(urle.getMessage());
	} catch (IOException eio) {
	    ErrorMessage.display(MessageFormat.format
				 (localeString("ioErrorURL"),
				  eio.getMessage(),
				  descrURL.toString()),
				 localeString("descrErrorTitle"),
				 comp);
	    // System.err.println(eio.getMessage());
	}
    }
    public void setDescr(String descr) {
	((map == null)? rmap: map).put("descr", descr);
    }

    public void setFilename(String filename) {
	map.put("filename", filename);
    }

    public void setURL(String url) {
	map.put("url", url);
    }

    public void setHrefURL(String url) {
	map.put("hrefURL", url);
    }

    static String[] modes = {
	"property",
	"method0",
	"method1",
	"function",
	"test"
    };

    static String[] condModes = {
	"onImageChange",
	"onOverridden",
	"onNotOverridden",
	"onSlideshowStart",
	"onSlideshowEnd",
	"asSlideshowTest",
	"asSlideshowEnabledTest"
    };
    
    static private HashMap<String,Integer> condModesInv =
	new HashMap<String,Integer>();
    static private HashMap<String,Integer> modesInv =
	new HashMap<String,Integer>();

    static {
	for (int i = 0; i < modes.length; i++) {
	    modesInv.put(modes[i], i);
	}
	for (int i = 0; i < condModes.length; i++) {
	    condModesInv.put(condModes[i], i);
	}
    }
    static public String getModeIndex(String mode) {
	return modesInv.get(mode).toString();
    }
    static public String getCondModeIndex(String mode) {
	return condModesInv.get(mode).toString();
    }

    // static public final  String defaultLayout = "normal";
    static public final  String defaultLayout =
	"sresource:/webnail/normalLayout.xml";
    static public final String html5Layout =
	"sresource:/webnail/html5Layout.xml";
    static ArrayList<LayoutParms> layouts = new ArrayList<LayoutParms>();

    static public int getNumberOfLayouts() {return layouts.size();}

    static void setLayouts(Map<String,LayoutParms> map) {
	try {
	    layouts.clear();
	    LayoutParser lp = new LayoutParser();
	    layouts.add(lp.parse(defaultLayout));
	    layouts.add(lp.parse(html5Layout));
	    for (int i = 1; i < 6; i++) {
		String url = "sresource:/webnail/single" + i + ".xml";
		layouts.add(lp.parse(url));
	    }
	    if (map != null) {
		for (LayoutParms parms: map.values()) {
		    layouts.add(parms);
		}
	    }
	} catch (Exception e) {
	    // e.printStackTrace(System.err);
	    ErrorMessage.display(e);
	}
    }

    static {
	setLayouts(null);
	/*
	try {
	    lp = new LayoutParser();
	    layouts.add(lp.parse("sresource:/normalLayout.xml"));
	    for (int i = 1; i < 6; i++) {
		String url = "sresource:/single" + i + ".xml";
		layouts.add(lp.parse(url));
	    }
	} catch (Exception e) {
	    // e.printStackTrace(System.err);
	    ErrorMessage.display(e.getMessage());
	}
	*/
    }
    
    String layout = defaultLayout;
    int layoutIndex = 0;
    LayoutParms layoutParms = layouts.get(0);
    LayoutParms customParms = null;
    // LayoutParser cplp = new LayoutParser();

    public LayoutParms getCustomParms() {
	return customParms;
    }

    public void setCustomParms(LayoutParms parms) {
	customParms = parms;
    }

    public void setCustomParms(final String url) 
	throws MalformedURLException, SAXException, IOException
    {
	if (customParms != null && url.equals(customParms.getCanonicalName())) {
	    return;
	}
	customParms = null;
	int sz = layouts.size();
	for (int i = 0; i < sz; i++) {
	    if (url.equals(layouts.get(i).getCanonicalName())) {
		customParms = layouts.get(i);
		break;
	    }
	}
	if (customParms == null) {
	    // don't have a copy cached.
	    if (url.startsWith("file:")) {
		try {
		    customParms = (new LayoutParser()).parse(url);
		} catch (Exception e) {
		    ErrorMessage.display(e);
		}
	    } else {
		final LayoutParms parms = new LayoutParms(url);
		parms.thread = new Thread (new Runnable() {
			public void run() {
			    try {
				LayoutParms vparms = 
				    (new LayoutParser()).parse(url);
				parms.set(vparms);
			    } catch (Exception e) {
				ErrorMessage.display(e);
			    }
			}
		    });
		parms.thread.start();
		customParms = parms;
	    }
	}
    }

    public LayoutParms getLayoutParms() {
	return layoutParms;
    }

    static public LayoutParms getLayoutParms(int index) {
	return layouts.get(index);
    }

    static public String getLayoutName(int index) {
	int sz = layouts.size();
	if (index == sz) {
	    return localeString("custom");
	} else if (index > sz || index < 0) {
	    return null;
	} else {
	    // System.out.println(layouts.get(index).getName());
	    return layouts.get(index).getName();
	}
	/*
	switch (index) {
	case 0: return defaultLayout;
	case 1: return "headerThenImagesThenTrailer";
	case 2: return "headerThenImagesAndTrailer";
	case 3: return "headerThenTrailerAndImages";
	case 4: return "imagesAndHeaderWithTrailer";
	case 5: return "headerWithTrailerAndImages";
	}
	return null;
	*/
    }

    public String getLayout() {return layout;}
    public int getLayoutIndex() {return layoutIndex;}


    public void setLayoutByIndex(int index) {
	int sz = layouts.size();
	if (index == sz) {
	    // custom layout.
	    layoutIndex = index;
	    layoutParms = customParms;
	    layout = (layoutParms == null)? null:
		layoutParms.getCanonicalName();
	} else if (index > sz) {
	    throw new IllegalArgumentException("index out of range");
	} else {
	    if (index < layouts.size()) {
		layoutParms = layouts.get(index);
		layout = layoutParms.getCanonicalName();
		layoutIndex = index;
	    }
	}
	/*
	switch (index) {
	case 0:
	    layout = defaultLayout;
	    layoutIndex = 0;
	    break;
	case 1:
	    layout = "headerThenImagesThenTrailer";
	    layoutIndex = 1;
	    break;
	case 2:
	    layout = "headerThenImagesAndTrailer";
	    layoutIndex = 2;
	    break;
	case 3:
	    layout = "headerThenTrailerAndImages";
	    layoutIndex = 3;
	    break;
	case 4:
	    layout = "imagesAndHeaderWithTrailer";
	    layoutIndex = 4;
	    break;
	case 5:
	    layout = "headerWithTrailerAndImages";
	    layoutIndex = 5;
	    break;
	default:
	    throw new IllegalArgumentException("index out of range");
	}
	*/
    }



    public void setLayout(String layout) {
	if (layout == null) {
	    layout = defaultLayout;
	}
	int index = 0;
	for (LayoutParms parms: layouts) {
	    if (layout.equals(parms.getCanonicalName())) {
		layoutParms = parms;
		layoutIndex = index;
		this.layout = layout;
		return;
	    }
	    index++;
	}
	try {
	    setCustomParms(layout);
	    // layoutParms = cplp.parse(layout);
	    // customParms = layoutParms;
	    layoutParms = customParms;
	    layoutIndex = layouts.size();
	    this.layout = layoutParms.getCanonicalName();
	} catch (MalformedURLException murle) {
	    throw new IllegalArgumentException("unknown layout");
	} catch (IOException ioe) {
	    throw new IllegalArgumentException("unknown layout");
	} catch (SAXParseException spe) {
	    throw new IllegalArgumentException("unknown layout");
	} catch (SAXException se) {
	    throw new IllegalArgumentException("unknown layout");
	}
	/*
	if (layout == null) {
	    this.layout = defaultLayout;
	    layoutIndex = 0;
	} else if (layout.equals("normal")) {
	    this.layout = layout;
	    layoutIndex = 0;
	} else if (layout.equals("headerThenImagesThenTrailer")) {
	    this.layout = layout;
	    layoutIndex = 1;
	} else if (layout.equals("headerThenImagesAndTrailer")) {
	    this.layout = layout;
	    layoutIndex = 2;
	} else if (layout.equals("headerThenTrailerAndImages")) {
	    this.layout = layout;
	    layoutIndex = 3;
	} else if (layout.equals("imagesAndHeaderWithTrailer")) {
	    this.layout = layout;
	    layoutIndex = 4;
	} else if (layout.equals("headerWithTrailerAndImages")) {
	    this.layout = layout;
	    layoutIndex = 5;
	} else {
	    throw new IllegalArgumentException("unknown layout");
	}
	*/
    }

    public URL getTemplateURL() {
	return layoutParms.getURL(hrefToOrig);
	/*
	switch (layoutIndex) {
	case 0:
	    return "indexHTML.wnt";
	case 1:
	    return "singleHTML1" + (hrefToOrig? "link": "") + ".wnt";
	case 2:
	    return "singleHTML2" + (hrefToOrig? "link": "") + ".wnt";
	case 3:
	    return "singleHTML3" + (hrefToOrig? "link": "") + ".wnt";
	case 4:
	    return "singleHTML4" + (hrefToOrig? "link": "") + ".wnt";
	case 5:
	    return "singleHTML5" + (hrefToOrig? "link": "") + ".wnt";
	default:
	    throw new IllegalStateException("unknown layout index");
	}
	*/
    }

    public void setHrefTarget(String target) {
	if (target.equals("_blank") || target.equals("_top")) {
	    map.put("hrefTarget", target);
	} else {
	    throw new 
		IllegalArgumentException(localeString("illegalTarget"));
	}
    }



    /*
    public void setUser(String user) {
	((map == null)? rmap: map).put("user", user);
    }

    public void setPassword(String password) {
	((map == null)? rmap: map).put("password", password);
    }
    */
    StringBuilder text = new StringBuilder();
    java.nio.CharBuffer cb = java.nio.CharBuffer.allocate(4096);

    private void loadText(InputStream is, String charset) throws IOException {
	cb.clear();
	text.setLength(0);
	Reader rd = new InputStreamReader(is, charset);
	while (rd.read(cb) != -1) {
	    cb.flip();
	    text.append(cb);
	    cb.clear();
	}
    }

    boolean loadText(File file) throws IOException {
	loadText(new FileInputStream(file), Charset.defaultCharset().name());
	return true;
    }
    private String getCharset(String contentType) {
	if (contentType == null) return Charset.defaultCharset().name();
	String[] fields = contentType.split("\\s*;\\s*");
	if (!fields[0].trim().toUpperCase().startsWith("TEXT/"))
	    return null;
	if (fields.length < 2) return Charset.defaultCharset().name();
	for (int i = 1; i < fields.length; i++) {
	    String[] components = fields[i].split("=");
	    if (components[0].trim().toUpperCase().equals("CHARSET")) {
		String charset = components[1].trim().toUpperCase();
		if (Charset.isSupported(charset)) {
		    return charset;
		} else {
		    return null;
		}
	    }
	}
	return null;
    }
    
    static String ACCEPT_TEXT_VALUES =
	"text/html, text/plain, wwwserver/html-ssi"; 

    boolean loadText(URL url) throws IOException {
	if (url == null) return false;
	String path = url.getPath();
	
	URLConnection c = url.openConnection();
	if (c instanceof HttpURLConnection) {
	    c.setRequestProperty("accept", ACCEPT_TEXT_VALUES);
			    if (c instanceof HttpURLConnection) {
				c.setRequestProperty("accept", 
						     Webnail.ACCEPT_VALUE);
				((HttpURLConnection) c).setRequestMethod("GET");
				c.connect();
				if (((HttpURLConnection) c).getResponseCode() 
				    != HttpURLConnection.HTTP_OK) {
				    throw new IOException(String.format
							  (localeString
							   ("couldNotConnect"), 
							   url.toString())); 
				}
			    }
	}
	String contentType = c.getContentType();
	// System.out.println(contentType);
	String charset = getCharset(contentType);
	if (charset == null) {
	    if ((path.endsWith(".shtml") || path.endsWith(".SHTML"))
		&& url.getProtocol().equals("file")) {
		charset = Charset.defaultCharset().name();
	    } else {
		charset = "UTF-8";
	    }
	}
	loadText(c.getInputStream(), charset);
	return true;
    }



    public void setHeadURL(String headURL) {
	rmap.put("headURL", headURL);
	try {
	    loadText(new URL(headURL));
	    setHead(text.toString());
	    text.setLength(0);
	} catch (MalformedURLException urle) {
	    ErrorMessage.display(String.format
				 (localeString("malformedURL2"),
				  headURL.toString()),
				 localeString("headErrorTitle"),
				 comp);
	    // System.err.println(urle.getMessage());
	} catch (IOException eio) {
	    ErrorMessage.display(MessageFormat.format
				 (localeString("ioErrorURL"),
				  eio.getMessage(),
				  headURL.toString()),
				 localeString("headErrorTitle"),
				 comp);
	    // System.err.println(eio.getMessage());
	}
    }
    public void setHead(String head) {
	rmap.put("head", head);
    }

    public void setHeaderURL(String headerURL) {
	rmap.put("headerURL", headerURL);
	try {
	    loadText(new URL(headerURL));
	    setHeader(text.toString());
	    text.setLength(0);
	} catch (MalformedURLException urle) {
	    ErrorMessage.display(String.format
				 (localeString("malformedURL2"),
				  headerURL.toString()),
				 localeString("headerErrorTitle"),
				 comp);
	    // System.err.println(urle.getMessage());
	} catch (IOException eio) {
	    ErrorMessage.display(MessageFormat.format
				 (localeString("ioErrorURL"),
				  eio.getMessage(),
				  headerURL.toString()),
				 localeString("headerErrorTitle"),
				 comp);
	    // System.err.println(eio.getMessage());
	}
    }
    public void setHeader(String header) {
	rmap.put("header", header);
    }

    public void setTrailerURL(String trailerURL) {
	rmap.put("trailerURL", trailerURL);
	try {
	    loadText(new URL(trailerURL));
	    setTrailer(text.toString());
	    text.setLength(0);
	} catch (MalformedURLException urle) {
	    ErrorMessage.display(String.format
				 (localeString("malformedURL2"),
				  trailerURL.toString()),
				 localeString("trailerErrorTitle"),
				 comp);
	    // System.err.println(urle.getMessage());
	} catch (IOException eio) {
	    ErrorMessage.display(MessageFormat.format
				 (localeString("ioErrorURL"),
				  eio.getMessage(),
				  trailerURL.toString()),
				 localeString("trailerErrorTitle"),
				 comp);
	    // System.err.println(eio.getMessage());
	}
    }
    public void setTrailer(String trailer) {
	rmap.put("trailer", trailer);
    }

    public void setAfterScriptURL(String finalHtmlURL) {
	rmap.put("finalHtmlURL", finalHtmlURL);
	try {
	    loadText(new URL(finalHtmlURL));
	    setAfterScript(text.toString());
	    text.setLength(0);
	} catch (MalformedURLException urle) {
	    ErrorMessage.display(String.format
				 (localeString("malformedURL2"),
				  finalHtmlURL.toString()),
				 localeString("finalHTMLErrorTitle"),
				 comp);
	    // System.err.println(urle.getMessage());
	} catch (IOException eio) {
	    ErrorMessage.display(MessageFormat.format
				 (localeString("ioErrorURL"),
				  eio.getMessage(),
				  finalHtmlURL.toString()),
				 localeString("finalHTMLErrorTitle"),
				 comp);
	    // System.err.println(eio.getMessage());
	}
    }
    public void setAfterScript(String finalHtml) {
	rmap.put("finalHtml", finalHtml);
    }

    public void addImageMap(TemplateProcessor.KeyMap map) {
	try {
	    String ns =(String)map.get("nProps");
		int n = (ns == null)? 0: Integer.parseInt(ns);
	    props = "dom: {";
	    for (int i = 0; i < n; i++) {
		String key = (String)map.get("propKey" + i);
		TemplateProcessor.KeyMap dmap = null;
		for (TemplateProcessor.KeyMap dm: domlist) {
		    String k = (String)dm.get("domKey");
		    if (k != null && key.equals(k)) {
			dmap = dm;
			break;
		    }
		}
		String mode = (dmap == null)? null:
		    (String) dmap.get("domMode");
		if (mode != null && mode.equals("property")) {
		    props = props + ((i == 0)?"": ",") + key
			+": \""
			+ WebEncoder.quoteEncode((String)
						 map.get("propValue" + i))
			+"\"";
		} else if (mode != null &&
			   (mode.equals("function")
			    || mode.equals("method1"))) {
		    // if mode is not "property", the value is a method or
		    // function argument for those that take an argument.
		    String arg = ((String)map.get("propValue" + i)).trim();
		    if (arg.length() == 0) arg = "null";
		    props = props + ((i == 0)? "": ",") + key + ": " + arg;
		}
	    }
	    props = props + "}";
	    map.put("otherProps", props);
	    ilist.add(map);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }

    private int nextIndex = 0;
    public void startImage() {
	map = new TemplateProcessor.KeyMap();
	map.put("index", "" + nextIndex);
	nextIndex++;
    }

    String props = "";
    /*
    public void addProperty(String key, String value) {
	String propValue = value.trim();
	props =  props + ", " + key + ": \"" +propEncode(propValue) + "\"";
	map.put("propKey" + propIndex, key);
	map.put("propValue" + propIndex, propValue);
	propIndex++;
    }
    */

    public void imageComplete() {
	ilist.add(map);
	// map.put("otherProps", props);
	map.put("nProps", "" + propIndex);
	map = null;
	propIndex = 0;
	props = "";
    }
   
    


    public void imagesComplete() {
	// System.out.println("ilist.size() = " + ilist.size());
	iArray = ilist.toArray(new TemplateProcessor.KeyMap[ilist.size()]);
    }

    public void startDomMappings() {
	domlist.clear();
	domArray = null;
    }

    public void addMapping(TemplateProcessor.KeyMap map) {
	TemplateProcessor.KeyMap newmap = (TemplateProcessor.KeyMap)map.clone();
	String mode = (String)map.get("domMode");
	// newmap.remove("domCallAsDefaultInsert");
	newmap.remove("domPropInsert");
	newmap.remove("domMethodInsert");
	newmap.remove("domFunctionInsert");
	newmap.remove("domDefaultValueInsert");
	newmap.remove("domDefaultArgumentInsert");
	if (mode.equals("property") || mode.equals("method0")
	    || mode.equals("method1")) {
	    String ids = (String) map.get("domIDs");
	    String[] idarray = ids.trim().split(", *");
	    StringBuilder sb = new StringBuilder(ids.length());
	    if (ids == null) ids = "";
	    boolean firstTime = true;
	    for (String id: idarray) {
		if (firstTime) {
		    sb.append("\"");
		    firstTime = false;
		} else {
		    sb.append(", \"");
		}
		sb.append(xmlEncode(id));
		sb.append("\"");
	    }
	    String idsq = sb.toString();
	    newmap.put("domIDsQuoted", idsq);
	    newmap.put("domIDsInsert", ", ids: [" + idsq + "]");
	}
	newmap.put("domModeCode", getModeIndex(mode));
	newmap.put("domCondModeInsert",
		   ", condMode: "
		   + getCondModeIndex((String)newmap.get("domCondMode")));
	if (mode.equals("property")) {
	    newmap.put("domPropInsert", 
		       ", prop: \"" + (String)newmap.get("domProp") + "\"");
	    String dv = (String)map.get("domDefaultValue");
	    if (dv == null) dv="";
	    dv = WebEncoder.quoteEncode(dv);
	    newmap.put("domDefaultValueQuoted", dv);
	    newmap.put("domDefaultValueInsert", 
		       ", defaultValue: \"" + dv + "\"");
	} else if (mode.equals("method0")) {
	    newmap.put("domMethodInsert", 
		       ", method: \"" + (String)newmap.get("domMethod") + "\"");
	} else if (mode.equals("method1")) {
	    newmap.put("domMethodInsert", 
		       ", method: \"" + (String)newmap.get("domMethod") + "\"");
	    String dv = (String)map.get("domDefaultArgument");
	    if (dv == null || dv.trim().equals("")) dv="null";
	    newmap.put("domDefaultArgumentInsert", 
		       ", defaultArgument: " + dv );
	} else if (mode.equals("function")) {
	    newmap.put("domFunctionInsert", 
		       ", funct: \"" + (String)newmap.get("domFunction") +"\"");
	    String dv = (String)map.get("domDefaultArgument");
	    if (dv == null || dv.trim().equals("")) dv="null";
	    newmap.put("domDefaultArgumentInsert", ", defaultArgument: " + dv);
	} else if (mode.equals("test")) {
	    newmap.remove("domDefaultArgument");
	    newmap.remove("domDefaultArgumentInsert");
	    newmap.put("domFunctionInsert", 
		       ", funct: \"" + (String)newmap.get("domFunction") +"\"");
	}
	domlist.add((TemplateProcessor.KeyMap)newmap);
    }

    public void addMapping(String key, String mode, String condMode,
			   String ids, String prop, String defaultValue) 
    {
	domMap = new TemplateProcessor.KeyMap();
	if (mode.equals("property") || mode.equals("method0")
	    || mode.equals("method1")) {
	    if (ids == null) ids = "";
	    String[] idarray = ids.trim().split(", *");
	    StringBuilder sb = new StringBuilder(ids.length());
	    boolean firstTime = true;
	    for (String id: idarray) {
		if (firstTime) {
		    sb.append("\"");
		    firstTime = false;
		} else {
		    sb.append(", \"");
		}
		sb.append(xmlEncode(id));
		sb.append("\"");
	    }
	    String idsq = sb.toString();
	    if (ids != null) {
		domMap.put("domIDs", ids);
		domMap.put("domIDsQuoted", idsq);
		domMap.put("domIDsInsert", ", ids: [" + idsq + "]");
	    }
	}

	domMap.put("domKey", key);
	domMap.put("domMode", mode);
	domMap.put("domModeCode", getModeIndex(mode));
	domMap.put("domCondMode", condMode);
	domMap.put("domCondModeInsert",
		   ", condMode: " + getCondModeIndex(condMode));

	if (mode.equals("property")) {
	    if (prop == null) prop = "";
	    domMap.put("domProp", prop);
	    if (defaultValue == null) defaultValue="";
	    domMap.put("domDefaultValue", defaultValue);
	    String dv = WebEncoder.quoteEncode(defaultValue);
	    domMap.put("domDefaultValueQuoted", dv);
	    domMap.put("domPropInsert", 
		       ", prop: \"" + prop + "\"");
	    domMap.put("domDefaultValueInsert", 
		       ", defaultValue: \"" + dv + "\"");
	} else if (mode.equals("method0")) {
	    domMap.put("domMethod", prop);
	    domMap.put("domMethodInsert", ", method: \"" + prop + "\"");
	    /*
	    if (defaultValue == null) defaultValue="false";
	    domMap.put("domCallAsDefault", defaultValue);
	    domMap.put("domCallAsDefaultInsert", 
		       ", callAsDefault: \"" + defaultValue  + "\"");
	    */
	} else if (mode.equals("method1")) {
	    domMap.put("domMethod", prop);
	    domMap.put("domMethodInsert", ", method: \"" + prop + "\"");
	    if (defaultValue == null) defaultValue = "null";
	    domMap.put("domDefaultArgument", defaultValue);
	    domMap.put("domDefaultArgumentInsert",
		       ", defaultArgument: " + defaultValue);
	} else if (mode.equals("function")) {
	    domMap.put("domFunction", prop);
	    domMap.put("domFunctionInsert", ", funct: \"" + prop + "\"");
	    if (defaultValue == null) defaultValue = "null";
	    domMap.put("domDefaultArgument", defaultValue);
	    domMap.put("domDefaultArgumentInsert",
		       ", defaultArgument: " + defaultValue);
	} else if (mode.equals("test")) {
	    domMap.put("domFunction", prop);
	    domMap.put("domFunctionInsert", ", funct: \"" + prop + "\"");
	    domMap.remove("domDefaultArgument");
	    domMap.remove("domDefaultArgumentInsert");
	}

	domMap.put("commaSeparator", ",\n");
	domlist.add(domMap);
	domMap = null;
    }
    public void endDomMappings() {
	if (domlist.size() > 0) {
	    domlist.getLast().remove("commaSeparator");
	}
	domArray = domlist.toArray(new 
				   TemplateProcessor.KeyMap[domlist.size()]);
	/*
	for(TemplateProcessor.KeyMap map: domArray) {
	    System.out.println((String)map.get("domKey")
			       + " " + (String)map.get("domMode")
			       + " " + (String)map.get("domCondMode")
			       +" " + map.hashCode());
	}
	*/
    }

    public TemplateProcessor.KeyMap[] getDomArray() {
	if (domArray != null) {
	    return domArray;
	} else {
	    return new TemplateProcessor.KeyMap[0];
	}
    }

    public List<TemplateProcessor.KeyMap> getDomList() {
	List<TemplateProcessor.KeyMap> list =
	    Collections.unmodifiableList(domlist);
	for(TemplateProcessor.KeyMap map: list) {
	    System.out.println((String)map.get("domKey")
			       + " " + (String)map.get("domMode")
			       + " " + (String)map.get("domCondMode")
			       +" " + map.hashCode());
	}
	return list;
    }

    public String getValue(String key) {
	Object value = rmap.get(key);
	if (value instanceof String) {
	    return (String) value;
	} else {
	    return null;
	}
    }

    public TemplateProcessor.KeyMap[] getImageArray() {
	if (iArray != null) {
	    return iArray;
	} else {
	    return new TemplateProcessor.KeyMap[0];
	}
    }

    public String getValue(String key, int index) {
	Object value = iArray[index].get(key);
	if (value instanceof String) {
	    return (String) value;
	} else {
	    return null;
	}
    }

    public String getValueWithDefault(String key, int index) {
	String value = getValue(key, index);
	if (value == null) value = getValue(key);
	return value;
    }


    public int imageListSize() {return iArray.length;}


    String mimeType = null;
    public String getMimeType() {return mimeType;}

    public String getMimeType(int ind) {
	String type = getValue("mimeType", ind);
	if (type == null) return getMimeType();
	else return type;
    }

    boolean webmode = false;
    public boolean getWebMode() {return webmode;}

    boolean linkmode = false;
    public boolean getLinkMode() {return linkmode;}

    boolean getLinkMode(int ind) {
	String mode = getValue("linkMode", ind);
	if (mode == null) return getLinkMode();
	else return mode.equals("true");
    }

    boolean flatmode = false;
    public boolean getFlatMode() {return flatmode;}

    boolean highResMode = false;
    public boolean getHighResMode() {return highResMode;}

    boolean webArchiveMode = false;
    public boolean getWebArchiveMode() {return webArchiveMode;}


    boolean syncMode = false;
    public boolean getSyncMode() {return syncMode;}

    boolean waitOnError = false;
    public boolean getWaitOnError() {return waitOnError;}

    boolean hrefToOrig = false;
    public boolean getHrefToOrig() {return hrefToOrig;}

    


    static String formatTime(long time) {
	long seconds = time /1000;
	long ms = time % 1000;
	long minutes = seconds / 60;
	long hours = minutes / 60;
	if (hours > 0) {
	    minutes = minutes % 60;
	}
	String msString;
	char decimalSeparator = (new DecimalFormat()).getDecimalFormatSymbols()
	    .getDecimalSeparator();

	if (decimalSeparator != '.' && decimalSeparator != ',') {
	    // just in case some locale uses a completely different format.
	    decimalSeparator = '.'; 
	    
	}

	if (ms >= 100) {
	    msString = "" + decimalSeparator + ms;
	} else if (ms >= 10) {
	    msString = decimalSeparator + "0" + ms;
	} else {
	    msString = decimalSeparator + "00" + ms;
	}
	String hoursString = (hours >= 10)? ("" + hours): "0" + hours;
	String minutesString = (minutes >- 10)? ("" + minutes): "0" + minutes;
	return ((hours > 0)? (hoursString + ":"): "")
	    +((minutes > 0 || hours > 0)? (minutesString + ":"): "")
	    + seconds + msString;
    }

    static long parseTime(String value) {
	if (value.equals("*")) return -1;
	if (value.equals("?")) return -2;
	int i;
	value = value.trim();
	if (!value.matches("([0-9]+:){0,2}[0-9]+((\\.|,)[0-9]*)?"))
	    throw new IllegalArgumentException();
	String values[] = value.split(":");
	// String val = values[0];
	long time = 0;
	for (i = 0; i < values.length-1; i++) {
	    time *=60;
	    time += Long.parseLong(values[i]);
	}
	values = values[i].split("\\.|,");
	switch (values.length) {
	case 1:
	    time *= 60;
	    time += Long.parseLong(values[0]);
	    time *= 1000;
	    break;
	case 2:
	    time *= 60;
	    time += Long.parseLong(values[0]);
	    time *= 1000;
	    value = values[1];
	    long ms;
	    switch (value.length()) {
	    case 0:
		ms = 0;
		break;
	    case 1:
		ms = Long.parseLong(value) * 100;
		break;
	    case 2:
		ms = Long.parseLong(value) * 10;
		break;
	    case 3:
		ms = Long.parseLong(value);
		break;
	    default:
		ms = Long.parseLong(value.substring(0,3));
		if (value.charAt(3) > '4') ms++;
	    }
	    time += ms;
	    break;
	default:
	    throw new IllegalArgumentException();
	}
	return time;
    }

    static long parseTime(String value, Locator locator,
			  long defaultValue)
	throws SAXParseException 
    {
	if (value == null) return defaultValue;
	if (value.equals("*")) return -1;
	if (value.equals("?")) return -2;
	try {
	    int i;
	    value = value.trim();
	    if (!value.matches("([0-9]+:){0,2}[0-9]+((\\.|,)[0-9]*)?"))
		throw new SAXParseException(localeString("timeExpected"),
					    locator);
	    String values[] = value.split(":");
	    // String val = values[0];
	    long time = 0;
	    for (i = 0; i < values.length-1; i++) {
		time *=60;
		time += Long.parseLong(values[i]);
	    }
	    values = values[i].split("\\.|,");
	    switch (values.length) {
	    case 1:
		time *= 60;
		time += Long.parseLong(values[0]);
		time *= 1000;
		break;
	    case 2:
		time *= 60;
		time += Long.parseLong(values[0]);
		time *= 1000;
		value = values[1];
		long ms;
		switch (value.length()) {
		case 0:
		    ms = 0;
		    break;
		case 1:
		    ms = Long.parseLong(value) * 100;
		    break;
		case 2:
		    ms = Long.parseLong(value) * 10;
		    break;
		case 3:
		    ms = Long.parseLong(value);
		    break;
		default:
		    ms = Long.parseLong(value.substring(0,3));
		    if (value.charAt(3) > '4') ms++;
		}
		time += ms;
		break;
	    default:
		throw new SAXParseException(localeString("timeExpected"),
					    locator);
	    }
	    return time;
	} catch (SAXParseException es) {
	    throw es;
	} catch(Exception e) {
	    throw new SAXParseException(localeString("longExpected"),
					locator);
	}
    }

    int height = 0;
    int width = 0;
    public int getHeight() {return height;}
    public int getWidth() {return width;}
    
    // boolean usePasswordEncryption = false;

    /*
    static String propEncode(String string) {
	StringTokenizer tk = new StringTokenizer(string, "\"\n", true);
	StringBuilder sb = new StringBuilder(64);

	while (tk.hasMoreTokens()) {
	    String s = tk.nextToken();
	    if (s.equals("\"")) sb.append("\\\"");
	    else if (s.equals("\n")) sb.append("\\n");
	    else if (s.equals("\f")) sb.append("\\f");
	    else if (s.equals("\r")) sb.append("\\r");
	    else sb.append(s);
	}
	return sb.toString();
    }
    */
    static String formatTime(String tstring) {
	return formatTime(Long.parseLong(tstring));
    }
    
    static String xmlEncode(String string) {
	if (string == null) return null;
	StringTokenizer st = new StringTokenizer(string, "<>&\"", true);
	StringBuilder sb = new StringBuilder(string.length());
	while (st.hasMoreTokens()) {
	    String s = st.nextToken();
	    if (s.equals("<")) {
		sb.append("&lt;");
	    } else if (s.equals(">")) {
		sb.append("&gt;");
	    } else if (s.equals("&")) {
		sb.append("&amp;");
	    } else if (s.equals("\"")) {
		sb.append("&quot;");
	    } else {
		sb.append(s);
	    }
	}
	return sb.toString();
    }

    public String getImageTime() {
	String time = getValue("imageTime");
	return (time == null || time.equals("*") || time.equals("?"))? time:
	    formatTime(time);
    }
    public String getMinImageTime() {
	String time = getValue("minImageTime");
	return (time == null)? time: formatTime(time);
    }

    public long getImageTime(int i) {
	String time = getValue("imageTime", i);
	return (time == null || time.equals("*"))? -1: 
	    (time.equals("?")? -2: parseTime(time));
    }
    public long getMinImageTime(int i) {
	String time = getValue("minImageTime", i);
	return (time == null || time.equals("*") || time.equals("?"))? 0:
	    parseTime(time);
    }


    public void write(PrintStream out) {
	out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		    +"<?M.T " + Webnail.WEBNAIL_XML_MIME_TYPE
		    +"?>");
	out.println("<!DOCTYPE webnail PUBLIC "
		    + "\"" + PUBLICID + "\""
		    + " "
		    + "\"" + SYSTEMID + "\">");
	out.printf("<webnail webMode=\"%b\" linkMode=\"%b\"\n", 
		   getWebMode(), getLinkMode());
	out.printf("        mimeType=\"%s\" windowTitle=\"%s\"\n", 
		   xmlEncode(getMimeType()), 
		   xmlEncode(getValue("windowTitle")));
	out.printf("        flatMode=\"%b\" highResMode=\"%b\"\n",
		   getFlatMode(), getHighResMode());
	out.printf("        webArchiveMode=\"%b\" bgcolor=\"%s\"\n",
		   getWebArchiveMode(), xmlEncode(getValue("bgcolor")));
	out.printf("        syncMode=\"%b\" waitOnError=\"%b\"\n",
		   getSyncMode(), getWaitOnError());
	String t1 = getValue("imageTime");
	String t2 = getValue("minImageTime");
	if (t1 != null || t2 != null) {
	    out.printf("       ");
	    if (t1 != null) out.printf(" imageTime=\"%s\"", formatTime(t1));
	    if (t2 != null) out.printf(" minImageTime=\"%s\"", formatTime(t2));
	    out.printf("\n");
	}
	out.printf("        height=\"%d\" width=\"%d\"",
		   getHeight(), getWidth());
	out.printf("\n        layout=\"%s\"", layoutParms.getCanonicalName());
	/*
	if (epassword != null) {
	    out.printf("\n        usePasswordEncryption=\"true\"");
	}
	*/
	out.printf(">\n");
	
	if (domlist.size() > 0) {
	    out.printf("  <domMap>\n");
	    for (TemplateProcessor.KeyMap dmap: domlist) {
		String dkey = xmlEncode((String)dmap.get("domKey"));
		String dMode = (String)dmap.get("domMode");
		String dID = xmlEncode((String)dmap.get("domIDs"));
		String dCondMode = xmlEncode((String)dmap.get("domCondMode"));
		String format;
		if (dMode.equals("property")) {
		    String dprop = xmlEncode((String)dmap.get("domProp"));
		    String dv = xmlEncode((String)dmap.get("domDefaultValue"));
		    format = "    <mapping key=\"%s\" mode=\"%s\" "
			+ "ids=\"%s\" prop=\"%s\" condMode=\"%s\" "
			+ "defaultValue=\"%s\"/>\n";
		    out.printf(format, dkey, dMode, dID, dprop, dCondMode, dv);
		} else if (dMode.equals("method0")) {
		    String dmeth = xmlEncode((String)dmap.get("domMethod"));
		    format = "    <mapping key=\"%s\" mode=\"%s\" "
			+ "ids=\"%s\" method=\"%s\"  condMode=\"%s\"/>\n";
		    out.printf(format, dkey, dMode, dID, dmeth, dCondMode);
		} else if (dMode.equals("method1")) {
		    String dmeth = xmlEncode((String)dmap.get("domMethod"));
		    String dv = 
			xmlEncode((String)dmap.get("domDefaultArgument"));
		    format = "    <mapping key=\"%s\" mode=\"%s\" "
			+ "ids=\"%s\" method=\"%s\"  condMode=\"%s\" " +
			"defaultArgument=\"%s\"/>\n";
		    out.printf(format, dkey, dMode, dID, dmeth, dCondMode, dv);
		} else if (dMode.equals("function")) {
		    String dfunc = xmlEncode((String)dmap.get("domFunction"));
		    String dv = 
			xmlEncode((String)dmap.get("domDefaultArgument"));
		    format = "    <mapping key=\"%s\" mode=\"%s\" "
			+ "function=\"%s\" condMode=\"%s\" " +
			"defaultArgument=\"%s\"/>\n";
		    out.printf(format, dkey, dMode, dfunc, dCondMode, dv);
		} else if (dMode.equals("test")) {
		    String dfunc = xmlEncode((String)dmap.get("domFunction"));
		    format = "    <mapping key=\"%s\" mode=\"%s\" "
			+ "function=\"%s\" condMode=\"%s\"/>\n";
		    out.printf(format, dkey, dMode, dfunc, dCondMode);
		}
	    }
	    out.printf("  </domMap>\n");
	}
	String titleURL = xmlEncode((String)rmap.get("titleURL"));
	if (titleURL != null) {
	    out.printf("  <title url=\"%s\"/>\n", 
		       titleURL);
	} else {
	    String title = xmlEncode((String)rmap.get("title"));
	    if (title != null && title.length() > 0) {
		out.printf("  <title>%s</title>\n", title);
	    }
	}
	String descrURL = xmlEncode((String)rmap.get("descrURL"));
	if (descrURL != null) {
	    out.printf("  <descr url=\"%s\"/>\n", 
		       descrURL);
	} else {
	    String descr = xmlEncode((String)rmap.get("descr"));
	    if (descr != null && descr.length() > 0) {
		out.printf("  <descr>%s</descr>\n", descr);
	    }
	}
	/*
	String user = xmlEncode((String)rmap.get("user"));
	String password = (String)rmap.get("password");
	if (epassword != null) {
	    password = encrypt(password);
	}
	password = xmlEncode(password);
	if (user != null && password != null) {
	    out.printf("  <user>%s</user>\n", user);
	    out.printf("  <password>%s</password>\n", password);
	}
	*/
	String headURL = xmlEncode((String)rmap.get("headURL"));
	if (headURL != null) {
		out.printf("  <head url=\"%s\"/>\n", 
			   headURL);

	} else {
	    String head = xmlEncode((String)rmap.get("head"));
	    if (head != null && head.length() > 0) {
		out.printf("  <head>%s</head>\n", head);
	    }
	}


	String headerURL = xmlEncode((String)rmap.get("headerURL"));
	if (headerURL != null) {
		out.printf("  <header url=\"%s\"/>\n", 
			   headerURL);

	} else {
	    String header = xmlEncode((String)rmap.get("header"));
	    if (header != null && header.length() > 0) {
		out.printf("  <header>%s</header>\n", header);
	    }
	}
	if (iArray.length > 0) {
	    for (TemplateProcessor.KeyMap imap: iArray) {
		String imimeType = xmlEncode((String)imap.get("mimeType"));
		String ilinkMode = xmlEncode((String)imap.get("linkMode"));
		String ihrefURL = xmlEncode((String)imap.get("hrefURL"));
		String ihrefTarget = xmlEncode((String)imap.get("hrefTarget"));
		String iimageTime = xmlEncode((String)imap.get("imageTime"));
		String iminImageTime = 
		    xmlEncode((String)imap.get("minImageTime"));
		String ifilename = xmlEncode((String)imap.get("filename"));
		String iurl = xmlEncode((String)imap.get("url"));
		String ititle = xmlEncode((String)imap.get("title"));
		String idescr = xmlEncode((String)imap.get("descr"));
		// String iuser = xmlEncode((String)imap.get("user"));
		// String ipw = (String)imap.get("password");
		// String idigest = null;
		/*
		if (epassword != null) {
		    ipw = encrypt(ipw);
		}
		ipw = xmlEncode(ipw);
		*/
		String npString = (String)imap.get("nProps");
		int np = (npString == null)? 0: Integer.parseInt(npString);

		out.printf("  <image");
		if (imimeType != null) 
		    out.printf(" mimeType=\"%s\"", imimeType);
		if (ilinkMode != null) 
		    out.printf(" linkMode=\"%s\"", ilinkMode);
		if (iimageTime != null) {
		    out.printf(" imageTime=\"%s\"", iimageTime);
		}
		if (iminImageTime != null) {
		    out.printf(" minImageTime=\"%s\"", iminImageTime);
		}
		if (ihrefURL != null)
		    out.printf(" hrefURL=\"%s\"", ihrefURL);
		if (ihrefTarget != null) {
		    out.printf(" hrefTarget=\"%s\"", ihrefTarget);
		}
		out.printf(">\n");
		
		if (ifilename != null) {
		    out.printf("<filename>%s</filename>\n", ifilename);
		}
		if (iurl != null) {
		    out.printf("<url>%s</url>\n", iurl);
		}
		String ititleURL = xmlEncode((String)imap.get("titleURL"));
		if (ititleURL != null) {
		    out.printf("  <title url=\"%s\"/>\n", 
			       ititleURL);
		} else {
		    if (ititle != null && ititle.length() > 0) {
			out.printf("<title>%s</title>\n", ititle);
		    }
		}
		String idescrURL = xmlEncode((String)imap.get("descrURL"));
		if (idescrURL != null) {
		    out.printf("  <descr url=\"%s\"/>\n", 
		       idescrURL);
		} else {
		    if (idescr != null && idescr.length() > 0) {
			out.printf("<descr>%s</descr>\n", idescr);
		    }
		}
		/*
		if (iuser != null) {
		    out.printf("<user>%s</user>\n", iuser);
		}
		if (ipw != null) {
		    out.printf("<password>$s</password>\n", ipw);
		}
		*/
		for (int i = 0; i < np; i++) {
		    String key = xmlEncode((String)imap.get("propKey" + i));
		    String value = xmlEncode((String)imap.get("propValue" + i));
		    out.printf("<property key=\"%s\">%s</property>\n",
			       key, value);
		}
		out.printf("</image>\n");
	    }
	}
	String trailerURL = xmlEncode((String)rmap.get("trailerURL"));
	if (trailerURL != null) {
		out.printf("  <trailer url=\"%s\"/>\n", 
			   trailerURL);

	} else {
	    String trailer = xmlEncode((String) rmap.get("trailer"));
	    if (trailer != null && trailer.length() > 0) {
		out.printf("<trailer>%s</trailer>\n", trailer);
	    }
	}
	String finalHtmlURL = xmlEncode((String)rmap.get("finalHtmlURL"));
	if (finalHtmlURL != null) {
	    out.printf("  <finalHtml url=\"%s\"/>\n", 
		       finalHtmlURL);
	} else {
	    String finalHtml = xmlEncode((String) rmap.get("finalHtml"));
	    if (finalHtml != null && finalHtml.length() > 0) {
		out.printf("<finalHtml>%s</finalHtml>\n", finalHtml);
	    }
	}
	out.printf("</webnail>\n");
    }


    class OurDefaultHandler extends DefaultHandler {

        Locator locator = null;
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

	boolean mimeTypePISeen = false;

	boolean processingXML = false;
	boolean processingImage = false;
	boolean processingProperty = false;
	String imageKey = null;
	long imageTime = 0;
	long minImageTime = 0;

	public void startDocument() {
	    errorSeen = false;
	    text.setLength(0);
	    mimeTypePISeen = false;
	    processingXML = false;
	    processingImage = false;
	    processingProperty = false;
	    imageKey = null;
	    imageTime = 0;
	    minImageTime = 0;
	}

        public void startElement(String uri, String localName,
                                 String qName, Attributes attr)
            throws SAXException 
        {
	    if (!mimeTypePISeen) {
		throw new SAXException(String.format("missingMIMEType",
						     xmlFilename));
	    }

	    if (qName.equals("webnail")) {
		String windowTitleStr = attr.getValue("windowTitle");
		rmap.put("windowTitle",
			 ((windowTitleStr == null)?
			  localeString("windowTitle"):
			  windowTitleStr));
		mimeType = attr.getValue("mimeType");
		if (mimeType == null) mimeType = "image/jpeg";
		if (!ImageMimeInfo.supportsMIMEType(mimeType)) {
		    error(new SAXParseException(localeString
						("unsupportedMIMEType"),
						locator));
		}
		String webmodeStr = attr.getValue("webMode");
		if (webmodeStr == null) {
		    webmode = false;
		} else {
		    webmode = webmodeStr.equals("true");
		}
		String linkmodeStr = attr.getValue("linkMode");
		if (linkmodeStr == null) {
		    linkmode = false;
		} else {
		    linkmode = linkmodeStr.equals("true");
		}
		String flatmodeStr = attr.getValue("flatMode");
		if (flatmodeStr == null) {
		    flatmode = false;
		} else {
		    flatmode = flatmodeStr.equals("true");
		}
		String highResModeStr = attr.getValue("highResMode");
		if (highResModeStr == null) {
		    highResMode = true;
		} else {
		    highResMode = highResModeStr.equals("true");
		}
		String webArchiveModeStr = attr.getValue("webArchiveMode");
		if (webArchiveModeStr == null) {
		    webArchiveMode = false;
		} else {
		    webArchiveMode = webArchiveModeStr.equals("true");
		}
		String syncModeStr = attr.getValue("syncMode");
		if (syncModeStr == null) {
		    syncMode = false;
		} else {
		    syncMode = syncModeStr.equals("true");
		    rmap.put("syncMode", syncModeStr);
		}
		String waitOnErrorStr = attr.getValue("waitOnError");
		if (waitOnErrorStr == null) {
		    waitOnError = false;
		} else {
		    waitOnError = waitOnErrorStr.equals("true");
		    rmap.put("waitOnError", waitOnErrorStr);
		}
		String imageTimeString = attr.getValue("imageTime");
		imageTime = parseTime(imageTimeString, locator,
				      Webnail.DEFAULT_IMAGE_TIME);
		if (imageTime == -1) {
		    rmap.put("imageTime", "*");
		} else if (imageTime == -2) {
		    rmap.put("imageTime", "?");
		} else {
		    rmap.put("imageTime", "" + imageTime);
		}
		String minImageTimeString = attr.getValue("minImageTime");
		minImageTime = parseTime(minImageTimeString, locator,
					 Webnail.DEFAULT_MIN_IMAGE_TIME);
		if (minImageTime == -1 || minImageTime == -2) {
		    rmap.put("minImageTime", "*");
		} else {
		    rmap.put("minImageTime", "" + minImageTime);
		}
		String bgcolorStr = attr.getValue("bgcolor");
		if (bgcolorStr != null) {
		    rmap.put("bgcolor", bgcolorStr);
		} else {
		    rmap.put("bgcolor", Webnail.DEFAULT_BGCOLOR);
		}
		String widthStr = attr.getValue("width");
		if (widthStr == null) {
		    width = 0;
		} else {
		    try {
			width = Integer.parseInt(widthStr);
			if (width < 0) throw new NumberFormatException();
		    } catch (NumberFormatException e) {
			error(new SAXParseException
			      (localeString("numbExpected"), locator));
		    }
		}
		String heightStr = attr.getValue("height");
		if (heightStr == null) {
		    height = 0;
		} else {
		    try {
			height = Integer.parseInt(heightStr);
			if (height < 0) throw new NumberFormatException();
		    } catch (NumberFormatException e) {
			error(new SAXParseException
			      (localeString("numbExpected"), locator));
		    }
		}

		String hrefToOrigString = attr.getValue("hrefToOrig");
		if (hrefToOrigString == null) {
		    hrefToOrig = false;
		} else if (hrefToOrigString.equals("true")) {
		    hrefToOrig = true;
		    
		} else if (hrefToOrigString.equals("false")) {
		    hrefToOrig = false;
		} else {
		    error(new SAXParseException
			  (localeString("booleanExpected"), locator));
		}
		String layoutString = attr.getValue("layout");
		setLayout(layoutString);
		/*
		String usePasswordEncryptionStr = 
		    attr.getValue("usePasswordEncryption");
		if (usePasswordEncryptionStr != null && 
		    usePasswordEncryptionStr.equals("true")) {
		    usePasswordEncryption = true;
		}
		*/  
		processingXML = true;
	    } else if (qName.equals("domMap")) {
		startDomMappings();
	    } else if (qName.equals("mapping")) {
		String mode = attr.getValue("mode");
		if (mode == null) mode = "property";
		String condMode = attr.getValue("condMode");
		if (condMode == null) condMode = "onImageChange";
		String key = attr.getValue("key");
		String ids = null;
		String prop = null;
		String defaultValue = null;
		if (mode.equals("property")) {
		    ids = attr.getValue("ids");
		    if (ids == null) ids="";
		    prop = attr.getValue("prop");
		    defaultValue = attr.getValue("defaultValue");
		    if (key == null || ids == null || 
			prop == null || defaultValue == null) {
			error(new SAXParseException(localeString("missingAttr"),
						    locator));
		    }
		} else if (mode.equals("method0")) {
		    ids = attr.getValue("ids");
		    if (ids == null) ids="";
		    prop = attr.getValue("method");
		    // defaultValue = attr.getValue("callAsDefault");
		    defaultValue = null;
		    if (key == null || ids == null || 
			prop == null) {
			error(new SAXParseException(localeString("missingAttr"),
						    locator));
		    }
		    // if (defaultValue == null) defaultValue = "true";
		} else if (mode.equals("method1")) {
		    ids = attr.getValue("ids");
		    if (ids == null) ids="";
		    prop = attr.getValue("method");
		    defaultValue = attr.getValue("defaultArgument");
		    if (key == null || ids == null || 
			prop == null || defaultValue == null) {
			error(new SAXParseException(localeString("missingAttr"),
						    locator));
		    }
		} else if (mode.equals("function")) {
		    ids = null; 
		    prop = attr.getValue("function");
		    defaultValue = attr.getValue("defaultArgument");
		    if (key == null || prop == null) {
			error(new SAXParseException(localeString("missingAttr"),
						    locator));
		    }
		    if (defaultValue == null) defaultValue="";
		    
		} else if (mode.equals("test")) {
		    ids = null;
		    prop = attr.getValue("function");
		    if (key == null || prop == null) {
			error(new SAXParseException(localeString("missingAttr"),
						    locator));
		    }
		    defaultValue = null;
			
		}
		if (key == null) {
		    error (new SAXParseException(localeString("missingKey"),
						 locator));
		}
		if (!key.matches("[\\p{L}_$][\\p{L}_$0-9]*")) {
		    error (new SAXParseException(localeString("invalidKey"),
						 locator));
		}
		addMapping(key, mode, condMode, ids, prop, defaultValue);
	    } else if (qName.equals("image")) {
		map = new TemplateProcessor.KeyMap();
		String mimeTypeStr = attr.getValue("mimeType");
		if (mimeTypeStr != null) {
		    map.put("mimeType", mimeTypeStr);
		    if (!ImageMimeInfo.supportsMIMEType(mimeTypeStr)) {
			error(new SAXParseException(localeString
						    ("unsupportedMIMEType"),
						    locator));
		    }
		}
		String linkModeStr = attr.getValue("linkMode");
		if (linkModeStr != null) {
		    map.put("linkMode", linkModeStr);
		}

		String imageTimeString = attr.getValue("imageTime");
		if (imageTimeString != null) {
		    imageTime = parseTime(imageTimeString, locator,
					  Webnail.DEFAULT_IMAGE_TIME);
		    if (imageTime == -1) {
			map.put("imageTime", "*");
		    } else if (imageTime == -2) {
			map.put("imageTime", "?");
		    } else {
			map.put("imageTime", imageTimeString);
		    }
		}
		String minImageTimeString = attr.getValue("minImageTime");
		if (minImageTimeString != null) {
		    minImageTime = parseTime(minImageTimeString, locator,
					     Webnail.DEFAULT_MIN_IMAGE_TIME);
		    if (minImageTime == -1 || minImageTime == -2) {
			map.put("minImageTime", "*");
		    } else {
			map.put("minImageTime", minImageTimeString);
		    }
		}
		processingImage = true;
		String hrefURL = attr.getValue("hrefURL");
		if (hrefURL != null) {
		    map.put("hrefURL", hrefURL);
		}
		String hrefTarget = attr.getValue("hrefTarget");
		if (hrefTarget == null) hrefTarget = "_blank";
		map.put("hrefTarget", hrefTarget);
	    
	    } else if (qName.equals("property")) {
		imageKey = attr.getValue("key");
		if (imageKey == null) {
		    error(new SAXParseException(localeString
						("noPropertyKey"),
						locator));
		}
		if (!imageKey.matches("[\\p{L}_$][\\p{L}_$0-9]*")) {
		    error (new SAXParseException(localeString("invalidKey"),
						 locator));
		}
	    } else if (qName.equals("title")) {
		theURL = attr.getValue("url");
		if (theURL != null) {
		    if (processingImage) {
			map.put("titleURL", theURL);
		    } else  if (processingXML) {
			rmap.put("titleURL", theURL);
		    }
		}
	    } else if (qName.equals("descr")) {
		theURL = attr.getValue("url");
		if (theURL != null) {
		    if (processingImage) {
			map.put("descrURL", theURL);
		    } else  if (processingXML) {
			rmap.put("descrURL", theURL);
		    }
		}
	    } else if (qName.equals("head")) {
		theURL = attr.getValue("url");
		if (theURL != null) rmap.put("headURL", theURL);
	    } else if (qName.equals("header")) {
		theURL = attr.getValue("url");
		if (theURL != null) rmap.put("headerURL", theURL);
	    } else if (qName.equals("trailer")) {
		theURL = attr.getValue("url");
		if (theURL != null) rmap.put("trailerURL", theURL);
	    } else if (qName.equals("finalHtml")) {
		theURL = attr.getValue("url");
		if (theURL != null) rmap.put("finalHtmlURL", theURL);
	    }
	}

	String theURL = null;

        public void endElement(String uri, String localName, String qName)
            throws SAXException
        {
	    if (qName.equals("webnail")) {
		processingXML = false;
	    } else if (qName.equals("domMap")) {
		endDomMappings();
	    } else if (qName.equals("title")) {
		if (theURL != null) {
		    try {
			loadText(new URL(theURL));
 		    } catch (MalformedURLException urle) {
			error(new SAXParseException
			      (localeString("urlExpected"), locator));
		    } catch (IOException eio) {
			error( new SAXParseException
			       (String.format(localeString("urlIOError"),
					      eio.getMessage()),
				locator));
		    }
		    theURL = null;
		}
		if (processingImage) {
		    map.put("title", text.toString().trim());
		} else if (processingXML) {
		    rmap.put("title", text.toString().trim());
		}
		text.setLength(0);
	    } else if (qName.equals("descr")) {
		if (theURL != null) {
		    try {
			loadText(new URL(theURL));
		    } catch (MalformedURLException urle) {
			error(new SAXParseException
			      (localeString("urlExpected"), locator));
		    } catch (IOException eio) {
			error(new SAXParseException
			      (String.format(localeString("urlIOError"),
					     eio.getMessage()),
			       locator));
		    }
		    theURL = null;
		}
		if (processingImage) {
		    map.put("descr", text.toString().trim());
		} else if (processingXML) {
		    rmap.put("descr", text.toString().trim());
		}
		text.setLength(0);
	    } else if (qName.equals("filename")) {
		if (processingImage) {
		    map.put("filename", text.toString().trim());
		    map.put("lineNo", "" +  locator.getLineNumber());
		    if (xmlFilename != null) {
			map.put("xmlFilename", xmlFilename);
		    }
		}
		text.setLength(0);
	    } else if (qName.equals("url")) {
		if (processingImage) {
		    map.put("url", text.toString().trim());
		    map.put("lineNo", "" +  locator.getLineNumber());
		    if (xmlFilename != null) {
			map.put("xmlFilename", xmlFilename);
		    }
		}
		text.setLength(0);
		/*
	    } else if (qName.equals("user")) {
		if (processingImage) {
		    map.put("user", text.toString().trim());
		} else if (processingXML) {
		    rmap.put("user", text.toString().trim());
		}
		text.setLength(0);
	    } else if (qName.equals("password")) {
		String pw = text.toString().trim();
		String digest = (String)(processingImage? map.get("digest"):
					 rmap.get("digest"));

		if (usePasswordEncryption) {
		    pw = decrypt(pw);
		}
		if (processingImage) {
		    map.put("password", pw);
		} else if (processingXML) {
		    rmap.put("password", pw);
		}
		text.setLength(0);
		*/
	    } else if (qName.equals("property")) {
		if (processingImage) {
		    String otherProps = (String)map.get("otherProps");
		    boolean firsttime = false;
		    if (otherProps == null) {
			otherProps = ", dom: {";
			firsttime = true;
		    }
		    TemplateProcessor.KeyMap dmap = null;
		    for (TemplateProcessor.KeyMap dm: domlist) {
			String k = (String)dm.get("domKey");
			if (k != null && imageKey.equals(k)) {
			    dmap = dm;
			    break;
			}
		    }
		    String mode = (dmap == null)? null:
			(String) dmap.get("domMode");
		    String propValue = text.toString();
		    if (mode != null && mode.equals("property")) {
			map.put("otherProps",
				otherProps
				+ (firsttime? "": ", ") + imageKey + ": \""
				+ WebEncoder.quoteEncode(propValue) + "\"");
		    } else if (mode != null &&
			   (mode.equals("function")
			    || mode.equals("method1"))) {
			propValue = propValue.trim();
			if (propValue.length() == 0) propValue = "null";
			map.put("otherProps",
				otherProps
				+ (firsttime? "": ",") + imageKey + ": "
				+ propValue);
		    }
		    map.put("propKey" + propIndex, imageKey);
		    map.put("propValue" + propIndex, propValue);
		    propIndex++;
		}
		imageKey = null;
		text.setLength(0);
	    } else if (qName.equals("head")) {
		if (theURL != null) {
		    try {
			loadText(new URL(theURL));
		    } catch (MalformedURLException urle) {
			error(new SAXParseException
			      (localeString("urlExpected"), locator));
		    } catch (IOException eio) {
			error(new SAXParseException
			      (String.format(localeString("urlIOError"),
					     eio.getMessage()),
			       locator));
		    }
		    theURL = null;
		}
		if (processingXML) {
		    rmap.put("head", text.toString());
		}
		text.setLength(0);
	    } else if (qName.equals("header")) {
		if (theURL != null) {
		    try {
			loadText(new URL(theURL));
		    } catch (MalformedURLException eurl) {
			error(new SAXParseException
			      (localeString("urlExpected"), locator));
		    } catch (IOException eio) {
			error(new SAXParseException
			      (String.format(localeString("urlIOError"),
					     eio.getMessage()),
			       locator));
		    }
		    theURL = null;
		}
		if (processingXML) {
		    rmap.put("header", text.toString());
		}
		text.setLength(0);
	    } else if (qName.equals("trailer")) {
		if (theURL != null) {
		    try {
			loadText(new URL(theURL));
		    } catch (MalformedURLException eurl) {
			error(new SAXParseException
			      (localeString("urlExpected"), locator));
		    } catch (IOException eio) {
			error(new SAXParseException
			      (String.format(localeString("urlIOError"),
					     eio.getMessage()),
			       locator));
		    }
		    theURL = null;
		}
		if (processingXML) {
		    rmap.put("trailer", text.toString());
		}
		text.setLength(0);
	    } else if (qName.equals("finalHtml")) {
		if (theURL != null) {
		    try {
			loadText(new URL(theURL));
		    } catch (MalformedURLException eurl) {
			error(new SAXParseException
			      (localeString("urlExpected"), locator));
		    } catch (IOException eio) {
			error(new SAXParseException
			      (String.format(localeString("urlIOError"),
					     eio.getMessage()),
			       locator));
		    }
		    theURL = null;
		}
		if (processingXML) {
		    rmap.put("finalHtml", text.toString());
		}
		text.setLength(0);
	    } else if (qName.equals("image")) {
		processingImage = false;
		ilist.add(map);
		String otherProps = (String)map.get("otherProps");
		if (otherProps == null) {
		    map.put("otherProps", ", dom: {}");
		} else {
		    map.put("otherProps", otherProps + "}");
		}
		map.put("nProps", "" + propIndex);
		map = null;
		propIndex = 0;
	    }
	}

	public void processingInstruction(String target,
					  String data) 
	    throws SAXException
	{
	    if (target.equals("M.T")) {
		if (data.equals("application/prs.wtz.webnail+xml")) {
		    mimeTypePISeen = true;
		    return;
	    } else {
		    throw new SAXException("wrongMIMEType");
		}
	    }
	}

        public void characters(char [] ch, int start, int length)
            throws SAXException 
        {
            text.append(ch, start, length);
        }

	public void endDocument() {
	    iArray = ilist.toArray(new TemplateProcessor.KeyMap[ilist.size()]);
	}

       public boolean errorSeen = false;
        public void warning(SAXParseException e) {

            String msg = (xmlFilename == null)?
		MessageFormat.format(localeString("warningAtLine"),
				     e.getLineNumber(),
				     e.getMessage()):
		MessageFormat.format(localeString("warningAtLineFN"),
				     xmlFilename,
				     e.getLineNumber(),
				     e.getMessage());
            displayMessage(msg, localeString("warningAtLineTitle"));
        }

	private void error(String msg) {
	    displayMessage(locator, msg, localeString("errorAtLineTitle"));
	    errorSeen = true;
	}

        public void error(SAXParseException e) {
            String msg = (xmlFilename == null)?
		MessageFormat.format(localeString("errorAtLine"),
				     e.getLineNumber(),
				     e.getMessage()):
		MessageFormat.format(localeString("errorAtLineFN"),
				     xmlFilename,
				     e.getLineNumber(),
				     e.getMessage());
            displayMessage(msg, localeString("errorAtLineTitle"));
            // System.err.println(msg);
            errorSeen = true;
        }
        public void fatalerror(SAXParseException e) {
            String msg = (xmlFilename == null)?
		MessageFormat.format(localeString("fatalErrorAtLine"),
				     e.getLineNumber(),
				     e.getMessage()):
		MessageFormat.format(localeString("fatalErrorAtLineFN"),
				     xmlFilename,
				     e.getLineNumber(),
				     e.getMessage());
            displayMessage(msg, localeString("fatalErrorAtLineTitle"));
            // System.err.println(msg);
            errorSeen = true;
        }
       public InputSource resolveEntity(String publicID, 
                                         String systemID)
            throws SAXException, IOException
        {
	    if (publicID != null) {
		if (publicID.equals(PUBLICID)) {
		    systemID = OUR_SYSTEMID;
		} else {
		    throw new SAXException
			(MessageFormat.format(localeString("illegalPublicID"),
					      publicID));
		}
	    }
            if (systemID.matches("resource:.*")) {
                // our DTD is built into the applications JAR file.
                String resource = systemID.substring(9);
                try {
                    if (resource.endsWith(".dtd")) {
                        InputStream stream =
                            ClassLoader.getSystemResourceAsStream(resource);
                            if (stream == null) {
                                throw new IOException();
                            } else {
                                return new InputSource(stream);
                            }
                    } else {
			throw new Exception("operation not allowed");
			/*
                        InputStream stream =
                            ClassLoader.getSystemResourceAsStream(resource);
                            if (stream == null) {
                                throw new IOException();
                            } else {
                                return new InputSource(stream);
                            }
			*/
                    }
                } catch (Exception e) {
                    String msg = 
                        MessageFormat.format(localeString("resolveEntity"),
                                             systemID);
                    throw new SAXException(msg);
                }
            } else  {
                return null;
            }
        }
    }

    public void printState(PrintStream out) {
	    out.println("title: " + getValue("title"));
	    out.println("descr: " + getValue("descr"));
	    // out.println("user: " + getValue("user"));
	    // out.println("password: " + getValue("password"));
	    out.println("mimeType: " + getMimeType());
	    out.println("webMode: " + getWebMode());
	    out.println("linkMode: " + getLinkMode());
	    out.println("flatMode: " + getFlatMode());
	    out.println("highResMode: " + getHighResMode());
	    out.println("webArchiveMode: " + getWebArchiveMode());
	    out.println("syncMode: " + getSyncMode());
	    out.println("waitOnError: " + getWaitOnError());
	    out.println("imageTime: " + getValue("imageTime"));
	    out.println("minImageTime: " 
			       + getValue("minImageTime"));
	    out.println("title: " + getValue("title"));
	    out.println("titleURL: " + getValue("titleURL"));
	    out.println("descr: " + getValue("descr"));
	    out.println("descrURL: " + getValue("descrURL"));
	    out.println("head: " + getValue("head"));
	    out.println("headURL: " + getValue("headURL"));
	    out.println("header: " + getValue("header"));
	    out.println("headerURL: " + getValue("headerURL"));
	    out.println("trailer: " + getValue("trailer"));
	    out.println("trailerURL: " + getValue("trailerURL"));
	    out.println("finalHtml: " +getValue("finalHtml"));
	    out.println("finalHtmlURL: " +getValue("finalHtmlURL"));
	    for (int i = 0; i < imageListSize(); i++) {
		out.println("----------");
		out.println("image " + i +":");
		out.println("mimeType: " + getMimeType(i));
		out.println("linkMode: " + getLinkMode(i));
		out.println("imageTime: " 
				   + getValue("imageTime", i));
		out.println("minImageTime: " 
				   + getValue("minImageTime", i));
		out.println("filename: " 
				   + getValue("filename", i));
		out.println("url: " + getValue("url", i));
		out.println("title: " + getValue("title", i));
		out.println("titleURL: " + getValue("titleURL", i));
		out.println("descr: " + getValue("descr", i));
		out.println("descrURL: " + getValue("descrURL", i));
		// out.println("user: " + getValue("user", i));
		/*
		out.println("password: " 
				   + getValue("password", i));
		*/
	    }
    }


    static public void main(String argv[]) {
	try {
	    FileInputStream is = new FileInputStream("test.xml");
	    Parser parser = new Parser();
	    parser.parse(is);
	    parser.printState(System.out);

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
