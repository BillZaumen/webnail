package webnail;

import javax.swing.DefaultListModel;
import java.net.URL;
import java.net.MalformedURLException;
import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.TemplateProcessor.KeyMap;

public class NamedMapElement implements MapElement {

    TemplateProcessor.KeyMap map = new TemplateProcessor.KeyMap();

    public TemplateProcessor.KeyMap getKeyMap() {return map;}

    public NamedMapElement(String url, DefaultListModel<Object> model)
	throws MalformedURLException 
    {
	this(new URL(url), model);
    }

    public NamedMapElement(final URL url,
			   final DefaultListModel<Object> model) {
	map.put("url", url.toString());
	model.addElement(this);

    }
		    
    public void put(String key, Object obj) {
	if (key.equals("url")) {
	    throw new IllegalArgumentException("key=\"url\"");
	}
	map.put(key, obj);
    }
    public Object get(String key) {return map.get(key);}

    public void remove(String key) {map.remove(key);}

    public String toString() {
	String url = (String) map.get("url");
	int last = url.lastIndexOf("/");
	if (last == -1) {
	    return url;
	} else {
	    return url.substring(last+1);
	}
    }

}
