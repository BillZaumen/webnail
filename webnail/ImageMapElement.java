package webnail;

import java.io.File;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.TemplateProcessor.KeyMap;
import org.bzdev.swing.ErrorMessage;

public class ImageMapElement extends ImageIcon implements MapElement {
    static public final URL blankURL = 
	ClassLoader.getSystemClassLoader().getResource("webnail/blank.png");
    static public final ImageIcon blankImageIcon = new ImageIcon(blankURL);
    static public final Image blankImage = blankImageIcon.getImage();

    TemplateProcessor.KeyMap map = null;

    public TemplateProcessor.KeyMap getKeyMap() {return map;}

    static private final String resourceBundleName = "webnail.ImageMapElement";
    static ResourceBundle bundle = 
	ResourceBundle.getBundle(resourceBundleName);
    static String localeString(String name) {
	return bundle.getString(name);
    }



    public ImageMapElement(String url, DefaultListModel<Object> model)
	throws MalformedURLException 
    {
	this(new URL(url), model);
    }

    public ImageMapElement(String url, DefaultListModel<Object> model,
			   int index)
	throws MalformedURLException 
    {
	this(new URL(url), model, index);
    }

    static private Object syncPoint = new Object();
    static private int outstanding = 0;
    static private int errCount = 0;
    static public int getOutstandingCount() {
	synchronized(syncPoint) {
	    return outstanding;
	}
    }
    static public int getErrCount() {
	synchronized(syncPoint) {
	    return errCount;
	}
    }

    static public String getProgressString() {
	return String.format(InputPane.localeString("imageMapString"),
			     getOutstandingCount());
    }

    static private boolean getErrorCountCalled = false;

    static public int getErrorCount() {
	synchronized(syncPoint) {
	    getErrorCountCalled = true;
	    return errCount;
	}	
    }

    public ImageMapElement(TemplateProcessor.KeyMap map, 
			   DefaultListModel<Object> model)
	throws MalformedURLException
    {
	this(map, false, new URL((String)map.get("url")), model, -1);
    }

    public ImageMapElement(TemplateProcessor.KeyMap map,
			   DefaultListModel<Object> model,
			   int index) 
	throws MalformedURLException
    {
	this(map, false, new URL((String)map.get("url")), model, index);
    }

    public ImageMapElement(URL url, DefaultListModel<Object> model) {
	this(new TemplateProcessor.KeyMap(), true, url, model, -1);
    }

    public ImageMapElement(URL url, DefaultListModel<Object> model, int index) {
	this(new TemplateProcessor.KeyMap(), true, url, model, index);
    }



    void removeMe(final DefaultListModel<Object> model) {
	// System.out.println("removing from list");
	String fn = (String)get("xmlFilename");
	String lineNoStr = (String)get("lineNo");
	int lineNo = (lineNoStr == null)? (-1):
	    Integer.valueOf(lineNoStr);
	String urlstr = (String)get("url");
	String ifn = (String)get("filename");
	try {
	    if (ifn != null) {
		urlstr = (new File(ifn)).toURI().toURL().toString();
	    }
	} catch (Exception e) {
	    urlstr = "<unknown>";
	}
	if (lineNo != -1) {
	    String msg = String.format
		(InputPane.localeString("couldNotLoadImage"), urlstr);
		
	    ErrorMessage.display(fn, lineNo, msg);
	    /*
	    if (fn == null) {
		System.err.println("line " + lineNo + ": " + msg);
	    } else {
		System.err.println("\"" + fn + "\", line " 
				   + lineNo + ": " +  msg);
	    }
	    */
	}
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    if(!model.removeElement(ImageMapElement.this)){
			// System.err.println("list remove failed");
			ErrorMessage.display("ImageMapElement: "
					     + "list remove failed");
		    }
		}
	    });
    }

    private ImageMapElement(TemplateProcessor.KeyMap map, boolean addURL,
			    final URL url,
			    final DefaultListModel<Object> model,
			    int index) {
	super(blankImage);
	this.map = map;
	if (addURL) map.put("url", url.toString());
	synchronized(syncPoint) {
	    if (outstanding == 0) {
		if (getErrorCountCalled) {
		    errCount = 0;
		    getErrorCountCalled = false;
		}
	    }
	    outstanding++;
	}
	if (index == -1) {
	    model.addElement(this);
	} else {
	    model.add(index, this);
	}
	(new Thread(new Runnable() {
		public void run() {
		    Image scaledImage;
		    // memory intensive and time consuming so we just
		    // let one run at a time
		    //
		    synchronized(ImageMapElement.class) {
			try {
			    ImageIcon tmp = new ImageIcon(url);
			    Image old = tmp.getImage();
			    final int h = tmp.getIconHeight();
			    final int w = tmp.getIconWidth();
			    int ww, hh;
			    if (h < w) {
				ww = 100;
				hh = (h * 100) / w;
			    } else if (h != 0) {
				hh = 100;
				ww = (w * 100) / h;
			    } else {
				// System.out.println("image size bad");
				synchronized(syncPoint) {
				    outstanding--;
				    errCount++;
				}
				removeMe(model);
				return;
			    }
			    if (tmp.getImageLoadStatus() !=
				MediaTracker.COMPLETE) {
				synchronized(syncPoint) {
				    outstanding--;
				    errCount++;
				}
				removeMe(model);
				ErrorMessage.display
				    (String.format(localeString("loadImageErr"),
						   url.toString()));
				return;
			    }
			    scaledImage = 
				old.getScaledInstance(ww, hh,
						      Image.SCALE_DEFAULT);
			    // To free memory - the full-sized images used in
			    // creating the tmp Icon can be from multi-megabyte
			    // files.
			    old = null;
			    tmp = null;
			} catch (Exception e) {
			    /*
			    System.out.println("error reading/scaling Image "
					       + "given URL");
			    */
			    synchronized(syncPoint) {
				outstanding--;
				errCount++;
			    }
			    removeMe(model);
			    ErrorMessage.display
				(String.format(localeString("loadImageErr"),
					       url.toString()));
			    return;
			}
		    }
		    final Image xScaledImage = scaledImage;
		    SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
				ImageMapElement.this
				    .setImage(xScaledImage);
				synchronized(syncPoint) {
				    outstanding--;
				}
				int index = 
				    model.indexOf(ImageMapElement.this);
				model.setElementAt(ImageMapElement.this,
						   index);
			    }
			});
		}
	    })).start();
    }
		    
    public void put(String key, Object obj) {
	if (key.equals("url")) {
	    throw new IllegalArgumentException("key=\"url\"");
	}
	map.put(key, obj);
    }
    public Object get(String key) {return map.get(key);}

    public void remove(String key) {map.remove(key);}

}
