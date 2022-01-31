package webnail;
import java.util.Set;
import org.bzdev.ejws.*;


public class WebnailAuthenticator extends EjwsBasicAuthenticator {
    public WebnailAuthenticator(String password) {
	super("webnail");
	Set<String> roles = Set.of("writer");
	add("maestro", password, roles);
	add("remote", "remote", Set.of("remote"));
	add("standalone", "standalone", Set.of("default"));
    }

    /*
    @Override
    public boolean  checkCredentials(String username, String password) {
	if (username.equals("maestro")) {
	    return super.checkCredentials(username, password);
	} else {
	    // a superlass will automatically create a Principal
	    // from information in the HTTP headers.
	    return true;
	}
    }
    */
}
