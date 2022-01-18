package webnail;

import java.net.URL;

public class LayoutParms {

    Thread thread = null;
    boolean valid = false;
    public boolean isValid() {return valid;}

    public LayoutParms() {
	valid = false;
    }

    public LayoutParms(String layoutURL) {
	this.layoutURL = layoutURL;
	valid = false;
    }

    public LayoutParms(String layoutURL, String name) {
	this.layoutURL = layoutURL;
	this.name = name;
	valid = false;
    }

    public void join() throws InterruptedException {
	Thread t = thread;
	if (t != null) {
	    t.join();
	}
    }

    public void set(LayoutParms parms) {
	valid = parms.valid;
	thread = parms.thread;
	single = parms.single;
	multi = parms.multi;
	layoutURL = parms.layoutURL;
	twidth = parms.twidth;
	theight = parms.theight;
	mwidth = parms.twidth;
	mheight = parms.theight;
	marginw = parms.marginw;
	marginh = parms.marginh;
	margin_hpad = parms.margin_hpad;
	margin_vpad = parms.margin_vpad;
	t_vpad = parms.t_vpad;
	t_hpad = parms.t_hpad;
	num_t_images = parms.num_t_images;
	t_vcorrection = parms.t_vcorrection;
	t_hcorrection = parms.t_hcorrection;
	url = parms.url;
	max_thumbwidth = parms.max_thumbwidth;
	max_thumbheight = parms.max_thumbheight;
	tiled =parms.tiled;
	tiledWidth = parms.tiledWidth;
	linkedURL = parms.linkedURL;
	noLinkURL = parms.noLinkURL;
    }


    boolean single = false;
    boolean multi = false;

    String layoutURL;

    public String getCanonicalName() {
	return layoutURL;
    }


    boolean isSingle() {return single;}
    boolean isMulti() {return multi;}

    int twidth = 0;

    public int getTWidth() {return twidth;}

    int theight = 0;
    public int getTHeight() {return theight;}

    int mwidth = 0;
    public int getMWidth() {return mwidth;}

    int mheight = 0;
    public int getMHeight() {return mheight;}

    int marginw = 0;
    public int getMarginW() {return marginw;}

    int marginh = 0;
    public int getMarginH() {return marginh;}

    int margin_hpad = 0;
    public int getMarginHPad() {return margin_hpad;}

    int margin_vpad = 0;
    public int getMarginVPad() {return margin_vpad;}

    int t_vpad = 0;
    public int getTVPad() {return t_vpad;}

    int t_hpad = 0;
    public int getTHPad() {return t_hpad;}

    int num_t_images = 0;
    public int getNumTImages() {return num_t_images;}

    int t_vcorrection = 0;
    public int getTVCorrection() {return t_vcorrection;}

    int t_hcorrection = 0;
    public int getTHCorrection() {return t_hcorrection;}

    String name = null;
    public String getName() {return name;}

    URL url = null;

    public URL getURL() {
	return getURL(false);
    }

    public URL getURL(boolean linked) {
	if (multi) {
	    return url;
	} else if (single) {
	    if (linked) {
		return linkedURL;
	    } else {
		return noLinkURL;
	    }
	} else {
	    return null;
	}
    }

    int max_thumbwidth = 0;
    public int getMaxThumbWidth() {return max_thumbwidth;}

    int max_thumbheight = 0;
    public int getMaxThumbHeight() {return max_thumbheight;}
    boolean tiled = false;
    public boolean isTiled() {return tiled;}

    int tiledWidth = 670;	// default value.
    public int getTiledWidth() {return tiledWidth;}
    
    URL linkedURL = null;
    public URL getLinkedURL() {return linkedURL;}

    URL noLinkURL = null;
    public URL getNoLinkURL() {return noLinkURL;}

    // for use in a JComboBox, which should display the name.
    public String toString() {
	String s = getName();
	if (s == null && !valid) s = getCanonicalName();
	return s;
    }

    public String toFullString() {
	String result = "[ single=" + single
	    + " multi="  + multi
	    + "\n    twidth=" + twidth
	    + " theight=" + theight
	    + "\n    mwidth=" + mwidth
	    + " mheight=" + mheight
	    + " marginw=" + marginw
	    + " marginh=" +marginh
	    + "\n    margin_hpad=" + margin_hpad
	    + " margin_vpad=" + margin_vpad
	    + " t_vpad=" + t_vpad
	    + " t_hpad=" + t_hpad
	    + "\n    num_t_images=" + num_t_images
	    + "\n    t_vcorrection=" + t_vcorrection
	    + "\n    t_hcorrection=" + t_hcorrection
	    + ((url == null)? "": "\n    url=" + url)
	    + "\n    name=\"" + name + "\""
	    + "\n    max_thumbwidth=" + max_thumbwidth
	    + " max_thumbheight=" + max_thumbheight
	    + " tiled=" + tiled
	    + " tiledWidth=" + tiledWidth
	    + ((linkedURL == null)? "": "\n    linkedURL=" + linkedURL)
	    + ((noLinkURL == null)? "": "\n    noLinkURL=" + noLinkURL)
	    + " ]";
	return result;
    }

    public String getThumbStrutHeight() {
	return "" + (theight + (2 * t_vpad));
    }

    public String getThumb90StrutWidth() {
	return "" + (twidth + (2 * t_hpad));
    }

    public String getTIFrameWidth() {
	return "" + (twidth + 2 * marginw + margin_hpad);
    }

    public String getT90IFrameHeight() {
	return "" + (theight + 2 * marginh + margin_vpad);
    }

    public String getTIFrameHeight() {
	return "" + (((theight + (2 * t_vpad)) * num_t_images) +
		     marginh + margin_vpad/2 + t_vcorrection);
    }

    public String getT90IFrameWidth() {
	return "" + (((twidth + (2 * t_hpad)) * num_t_images) +
		     marginw + margin_hpad/2 + t_hcorrection);
    }

    public String getIIFrameWidth() {
	return "" + (mwidth + 2 * marginw + margin_hpad);
    }

    public String getIIFrameHeight() {
	return "" + (mheight + 2 * marginh + margin_vpad);
    }

    public String getTDTableWidth() {
	return "" + ((twidth + 2 * marginw + margin_hpad) 
		     + (mwidth + 2 * marginw));
    }

    public String getTD90TableHeight() {
	return "" + ((theight + 2 * marginh + margin_vpad)
		     + (mheight + 2 * marginh));
    }

}
