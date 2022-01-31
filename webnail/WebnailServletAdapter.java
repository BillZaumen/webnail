package webnail;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import org.bzdev.net.*;
import org.bzdev.net.ServletAdapter.ServletException;

public class WebnailServletAdapter implements ServletAdapter {

    volatile int index = -1;
    volatile long nextTime;
    volatile boolean loop = false;
    volatile int maxIndex = -1;

    @Override
    public void init(Map<String,String> parameters) {
	index = -1;
	nextTime = System.currentTimeMillis();
	loop = false;
	maxIndex = -1;
    }

    @Override
    public synchronized void doPost(HttpServerRequest req,
				    HttpServerResponse res)
    {
	String cpath = req.getContextPath();
	// EJWS returns an empty string when getContextPath() is called,
	// but real servlets may return something else.
	String path = req.getRequestURI().substring(cpath.length());;
	if (req.getContentLengthLong() > 0) {
	    if (path.equals("/sync/set") && req.isUserInRole("writer")) {
		InputStream is = req.getDecodedInputStream();
		if (req.getMediaType()
		    .equals("application/x-www-form-urlencoded")) {
		    try {
			Map<String,String> map = WebDecoder.formDecode(is);
			index = Integer.parseInt(map.get("index"));
			long delay = Long.parseLong(map.get("delay"));
			nextTime = System.currentTimeMillis() + delay;
			loop = Boolean.parseBoolean(map.get("loop"));
			maxIndex = Integer.parseInt(map.get("maxIndex"));
			/*
			System.out.format("/sync/set: "
					  + "index = %d, delay = %d, loop = %b"
					  + ", maxIndex = %d\n",
					  index, delay, loop, maxIndex);
			*/
			res.sendResponseHeaders(200, -1);
		    } catch (IOException e) {
			e.printStackTrace();
			res.sendError(400);
		    }
		} else {
		    res.sendError(400);
		}
	    } else {
		res.sendError(403);
	    }
	} else {
	    // System.out.println("path = \"" + path + "\"");
	    if (path.equals("/sync/status")) {
		String status = req.isUserInRole("writer")? "true":
		    req.isUserInRole("remote")? "false":
		    null;
		if (status == null) {
		    // Indicates that this servlet should be ignored.
		    res.sendError(404);
		    return;
		}
		// System.out.println("/sync/status: status = " + status);
		res.addHeader("Content-type", "application/json");
		int len = status.length();
		var bos = new ByteArrayOutputStream(len);
		// System.out.println("len = " + len);
		res.sendResponseHeaders(200, (long)len);
		try {
		    OutputStream os = res.getOutputStream();
		    // System.out.println("os = " + os);
		    var w = new OutputStreamWriter(os, "UTF-8");
		    w.write(status, 0, len);
		    w.flush();
		    w.close();
		} catch (IOException e) {
		    // should not happen unless the connection fails
		    // or the server is shut down
		}
	    } else if (path.equals("/sync/get")) {
		long delay = nextTime - System.currentTimeMillis();
		if (index == -1) {
		    delay = 2000;
		} else	if (delay <= 0) {
		    if (index >= maxIndex && loop == false) {
			index = -1;
			delay = 2000;
		    } else {
			delay = 100;
		    }
		}
		String result = String
		    .format("{\"index\": %d, \"delay\": %d, "
			    + "\"loop\": %b, \"cont\": %b, \"maxIndex\": %d}",
			    index, delay, loop, (loop || index < maxIndex),
			    maxIndex);
		// System.out.println("/sync/get: " + result);

		int len = result.length();
		var bos = new ByteArrayOutputStream(len);
						    
		res.addHeader("Content-type", "application/json");
		res.sendResponseHeaders(200, (long)len);
		try {
		    var w = new OutputStreamWriter(res.getOutputStream(),
						   "UTF-8");
		    w.write(result, 0, len);
		    w.flush();
		    w.close();
		} catch (IOException e) {
		    // should not happen unless the connection fails
		    // or the server is shut down
		}
	    } else {
		res.sendError(404);
	    }
	}
    }
}
