/**
  * SecurityContext
  *
  *  Simple utils for Netscape Capabilities API
  * 
  * @author Joseph Bowbeer
  */

import java.util.Hashtable;
import netscape.security.PrivilegeManager;

public class SecurityContext {

    private static Boolean communicator;

    public static synchronized boolean isCommunicator() {

        if (communicator == null) {
            communicator = Boolean.FALSE;
            try {
                // Try to find one of the netscape.security classes. 
                Class t = Class.forName("netscape.security.UserDialogHelper");
                communicator = Boolean.TRUE;
            }
            catch (Exception e) {
                // Can't find netscape.security package.
            }
        }

        return communicator.booleanValue();
    }

    private static Hashtable hash = new Hashtable();

    public static synchronized boolean isCapableOf(String s) {

        if (!isCommunicator()) return false;

        Boolean granted = (Boolean) hash.get(s);

        if (granted == null) {
            granted = Boolean.FALSE;
            try {
                // Ask for the capability. 
                PrivilegeManager.enablePrivilege(s);
                granted = Boolean.TRUE;
            }
            catch (Exception e) {
                // Capability not granted. 
            }
            hash.put(s, granted);
        }

        return granted.booleanValue();
    }
}
