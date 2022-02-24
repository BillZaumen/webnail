package webnail;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.zip.*;
import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.net.HttpMethod;

// specifically for the webnail-server package, which is intended
// for Docker (so we aren't dependent on the org.bzdev.desktop module).
public class Server {

    static private final String resourceBundleName = "webnail.Server";
    static ResourceBundle bundle =
	ResourceBundle.getBundle(resourceBundleName);
    static String localeString(String name) {
	return bundle.getString(name);
    }


    // Also used by Webnail.
    static EmbeddedWebServer start(String fname, int port, String password)
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
	if (password != null && password.length() > 0) {
	    WebnailServletAdapter sa = new WebnailServletAdapter();
	    ews.setTracer("webnail: ", System.out);
	    try {
		ews.add("/sync/", ServletWebMap.class,
			new ServletWebMap.Config(sa, null, false,
						 HttpMethod.POST,
						 HttpMethod.HEAD,
						 HttpMethod.OPTIONS,
						 HttpMethod.TRACE),
			new WebnailAuthenticator(password),
			false, false, true);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	WebMap wmap = ews.getWebMap("/");
	if (wmap != null) wmap.addWelcome("index.html");
	ews.start();
	return ews;
    }

    // To just use webnail.Server to start the web server without
    // opening a browser window.
    public static void main(String argv[]) throws Exception {
	int port = 80;
	String password = null;

	if (argv.length == 0) {
	    // Configure and run  using environment variables, which
	    // is more convenient when running inside a Docker container.
	    String portVar = System.getenv("PORT");
	    if (portVar != null) {
		try {
		    port = Integer.parseInt(portVar);
		} catch (Exception e) {
		    System.err.println(localeString("badPort"));
		    System.exit(1);
		}
		if (port < 1 || port > 65535) {
		    System.err.println(localeString("badPort"));
		    System.exit(1);
		}
	    }
	    password = System.getenv("PASSWORD");
	    if (password != null) {
		password = password.trim();
		if (password.length() == 0) password = null;
	    }
	    String fileName = System.getenv("FILENAME");
	    if (fileName == null) {
		System.err.println(localeString("missingFileName"));
		System.exit(1);
	    }
	    start(fileName, port, password);
	} else {
	    int index = 0;
	    while (index < argv.length && argv[index].startsWith("-")) {
		if (argv[index].equals("--port")) {
		    index++;
		    if (index == argv.length) {
			System.err.println(localeString("missingArgument1"));
			System.exit(1);
		    }
		    try {
			port = Integer.parseInt(argv[index]);
		    } catch (Exception e) {
			System.err.println(localeString("badPort"));
			System.exit(1);
		    }
		    if (port < 1 || port > 65535) {
			System.err.println(localeString("badPort"));
			System.exit(1);
		    }
		} else if (argv[index].equals("--password")) {
		    index++;
		    if (index == argv.length) {
			System.err.println(localeString("missingArgument2"));
			System.exit(1);
		    }
		    password = argv[index].trim();
		    if (password.length() == 0) password = null;
		} else {
		    System.err.println(localeString("unknownArg") + ": "
				       + argv[index]);
		    System.exit(1);
		}
		index++;
	    }
	    if (index == argv.length) {
		System.err.println(localeString("missingArgument3"));
		System.exit(1);
	    }
	    String fname = argv[index];
	    start(fname, port, password);
	}
	// do not explicitly exit as that would stop the server.
    }
}
