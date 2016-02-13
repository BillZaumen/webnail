package webnail;

import javax.xml.parsers.*;
import org.xml.sax.helpers.*;
import org.xml.sax.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.text.MessageFormat;
import java.text.DecimalFormat;
import java.nio.*;
import java.nio.charset.Charset;
import org.bzdev.swing.ErrorMessage;


public class LayoutParser {

    static final String PUBLICID = "-//BZDev//Webnail_Layout_Info 1.0//EN";
    static final String SYSTEMID = "sresource:webnail-layout-info-1.0.dtd";
    static final String NAMESPACE =
	"http://bzdev.org/DTD/webnail-layout-info-1.0";
    static final String OUR_SYSTEMID = "sresource:webnail-layout-info-1.0.dtd";

    // share same error messages as Parser.
    static private final String resourceBundleName = "webnail.Parser";

    static ResourceBundle bundle = 
	ResourceBundle.getBundle(resourceBundleName);
    static String localeString(String name) {
	return bundle.getString(name);
    }

    SAXParser parser;
    OurDefaultHandler handler = new OurDefaultHandler();

    public LayoutParser() throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        parser = factory.newSAXParser();
    }

    LayoutParms parms = null;

    private static String HTTP_ACCEPT_VALUE =
	"application/x.webnail-layout+xml"
	+", " + "application/xml"
	+", " + "text/xml"
	+", " + "application/octet-stream";

    public LayoutParms parse(String urlAsString) 
	throws MalformedURLException, SAXException, IOException
    {
	setXMLFilename(urlAsString);
	URL url = new URL(urlAsString);
	URLConnection c = url.openConnection();
	if (c instanceof HttpURLConnection) {
	    c.setRequestProperty("accept", HTTP_ACCEPT_VALUE);
	    ((HttpURLConnection) c).setRequestMethod("GET");
	    c.connect();
	    if (((HttpURLConnection) c).getResponseCode() != 
		HttpURLConnection.HTTP_OK) {
		throw new IOException(String.format
				      (localeString("couldNotConnect"), 
				       urlAsString)); 
	    }
	}
	return parse(c.getInputStream(), urlAsString);
    }

    public LayoutParms parse(URL url) throws SAXException, IOException {
	String urlAsString = url.toString();
	setXMLFilename(urlAsString);
	URLConnection c = url.openConnection();
	if (c instanceof HttpURLConnection) {
	    c.setRequestProperty("accept", HTTP_ACCEPT_VALUE);
	    ((HttpURLConnection) c).setRequestMethod("GET");
	    c.connect();
	    if (((HttpURLConnection) c).getResponseCode() !=
		HttpURLConnection.HTTP_OK) {
		throw new IOException(String.format
				      (localeString("couldNotConnect"), 
				       urlAsString)); 
	    }
	}
	return parse(c.getInputStream(), urlAsString);
    }

    private LayoutParms parse(InputStream is, String url) 
	throws SAXException, IOException 
    {
        OurDefaultHandler handler = new OurDefaultHandler();
	parms = new LayoutParms();
	parms.layoutURL = url;
	handler.errorSeen = false;
	handler.publicIDSeen = false;
	handler.locator = null;
        parser.parse(is, handler);
        if (handler.errorSeen) {
            throw new SAXException(localeString("badDocument"));
	} else {
	    parms.valid = true;
	}
	// write(System.out);
	LayoutParms result = parms;
	parms = null;
	return result;
    }


    void displayMessage(String msg, String title) {
	ErrorMessage.display(msg/*, title*/);
        // simplify for now
        // System.err.println(msg);
    }

    String xmlFilename = null;
    
    private void setXMLFilename(String name) {
	if (name.startsWith("file:")) {
	    try {
		File f = new File(new URI(name));
		name = f.getCanonicalPath();
	    } catch (URISyntaxException e) {
		// no need to handle this - we would fail anyway
	    } catch (IOException eio) {
	    }
	}
	xmlFilename = name;
    }



    void displayMessage(Locator locator, 
			String msg, String title) {
	ErrorMessage.display(xmlFilename, locator.getLineNumber(), msg);
    }


    class OurDefaultHandler extends DefaultHandler {
	boolean errorSeen = false;
        Locator locator = null;
	boolean publicIDSeen = false;

	StringBuilder text = new StringBuilder();
	int matchlen = 0;
	boolean record = false;
	boolean done = false;

	boolean mimeTypePISeen = false;

	public void startDocument() {
	    errorSeen = false;
	    text.setLength(0);
	    matchlen = 0;
	    record = false;
	    done = false;
	    mimeTypePISeen = false;
	    
	}

        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }
	
        public void startElement(String uri, String localName,
                                 String qName, Attributes attr)
            throws SAXException 
        {
	    if (!publicIDSeen) {
		throw new SAXException(localeString("missingDOCTYPE"));
	    }

	    if (qName.equals("layout")) {
		String ns = attr.getValue("xmlns");
		if (ns == null ||
		    !ns.equals(NAMESPACE)) {
		    throw new SAXException(String.format(localeString
							 ("namespaceError"),
							 NAMESPACE));
		}
		matchlen = 0;
		text.setLength(0);
		record = false;
		done = false;
	    } else if (qName.equals("multi")) {
		parms.multi = true; parms.single = false;
		try {
		    String value;
		    value = attr.getValue("twidth");
		    parms.twidth = Integer.parseInt(value);
		    value = attr.getValue("theight");
		    parms.theight = Integer.parseInt(value);
		    value = attr.getValue("mwidth");
		    parms.mwidth = Integer.parseInt(value);
		    value = attr.getValue("mheight");
		    parms.mheight = Integer.parseInt(value);
		    value = attr.getValue("marginw");
		    parms.marginw = Integer.parseInt(value);
		    value = attr.getValue("marginh");
		    parms.marginh = Integer.parseInt(value);
		    value = attr.getValue("margin_hpad");
		    parms.margin_hpad = Integer.parseInt(value);
		    value = attr.getValue("margin_vpad");
		    parms.margin_vpad = Integer.parseInt(value);
		    value = attr.getValue("t_vpad");
		    parms.t_vpad = Integer.parseInt(value);
		    value = attr.getValue("num_t_images");
		    parms.num_t_images = Integer.parseInt(value);
		    value = attr.getValue("t_vcorrection");
		    parms.t_vcorrection = Integer.parseInt(value);
		    parms.name = attr.getValue("name");
		    value = attr.getValue("url");
		    parms.url = new URL(value);
		} catch (NumberFormatException e) {
			error(new SAXParseException
			      (localeString("numbExpected"), locator));
		} catch (MalformedURLException murle) {
			error(new SAXParseException
			      (localeString("urlExpected"), locator));
		}
	    } else if (qName.equals("single")) {
		String value;
		parms.single = true; parms.multi = false;
		try {
		    value = attr.getValue("max_thumbwidth");
		    parms.max_thumbwidth = Integer.parseInt(value);
		    value = attr.getValue("max_thumbheight");
		    parms.max_thumbheight = Integer.parseInt(value);
		    value = attr.getValue("tiled");
		    if (value != null) {
			parms.tiled = Boolean.parseBoolean(value);
		    }
		    value = attr.getValue("tiledWidth");
		    if (value != null) {
			parms.tiledWidth = Integer.parseInt(value);
		    }
		    parms.name = attr.getValue("name");
		    value = attr.getValue("linkedURL");
		    parms.linkedURL = new URL(value);
		    value = attr.getValue("noLinkURL");
		    parms.noLinkURL = new URL(value);
		} catch (NumberFormatException e) {
			error(new SAXParseException
			      (localeString("numbExpected"), locator));
		} catch (MalformedURLException murle) {
		    error(new SAXParseException
			  (localeString("urlExpected"), locator));
		}
	    } else if (qName.equals("name")) {
		text.setLength(0);
		String lang = attr.getValue("lang");
		String ourLang = Locale.getDefault().toString();
		int langlen = lang.length();
		if (ourLang.equals(lang)) {
		    record = true;
		    done = true;
		} else if (lang.startsWith(ourLang) && !done) {
		    record = true;
		    done = true;
		} else if (ourLang.startsWith(lang) && langlen > matchlen
			   && !done) {
		    matchlen = langlen;
		    record = true;
		} else {
		    record = false;
		}
	    }
	}

        public void endElement(String uri, String localName, String qName)
            throws SAXException
        {
	    if (qName.equals("name")) {
		if (record) parms.name = text.toString().trim();
		record = false;
	    }
	}

        public void characters(char [] ch, int start, int length)
            throws SAXException 
        {
	    if (record) text.append(ch, start, length);
        }


	public void endDocument() {
	}

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
		    publicIDSeen = true;
		} else {
		    throw new SAXException
			(String.format(localeString("illegalPublicID"),
				       publicID));
		}
	    } else {
		throw new SAXException(localeString("missingPublicID"));
	    }
            if (systemID.matches("sresource:.*")) {
                // our DTD is built into the applications JAR file.
                String resource = systemID.substring(10);
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

}
