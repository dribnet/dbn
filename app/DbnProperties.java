import java.util.*;


public class DbnProperties {
    static DbnApplet applet;

    static public void setApplet(DbnApplet app) {
	applet = app;
    }

    static public String get(String attribute, String defaultValue) {
	String value = applet.getParameter(attribute);
	return (value == null) ? defaultValue : value;
    }

    static public boolean getBoolean(String attribute, boolean defaultValue) {
	String value = get(attribute, null);
	return (value == null) ? defaultValue : 
	    (new Boolean(value)).booleanValue();
    }

    static public int getInteger(String attribute, int defaultValue) {
	String value = get(attribute, null);
	return (value == null) ? defaultValue : 
	    Integer.parseInt(value);
    }
}
