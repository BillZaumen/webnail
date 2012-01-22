package webnail;

import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.TemplateProcessor.KeyMap;

public interface MapElement {
    void put(String key, Object obj);
    Object get(String key);
    void remove(String key);
    String toString();
    TemplateProcessor.KeyMap getKeyMap();
}