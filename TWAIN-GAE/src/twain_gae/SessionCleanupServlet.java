package twain_gae;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
//import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
//import com.google.appengine.api.datastore.Query.FilterOperator;
//import com.google.appengine.api.memcache.MemcacheService;
//import com.google.appengine.api.memcache.MemcacheServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
//import java.util.ArrayList;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.appengine.api.datastore.FetchOptions.Builder.*;

@SuppressWarnings("serial")
public class SessionCleanupServlet extends HttpServlet {
	
	static final String SESSION_ENTITY_TYPE = "_ah_SESSION";
	static final String EXPIRES_PROP = "_expires";
	private DatastoreService datastore;
	private BlobstoreService blobstore;

	public void init() {
		this.datastore = DatastoreServiceFactory.getDatastoreService();
		this.blobstore = BlobstoreServiceFactory.getBlobstoreService();
	}

	public void service(HttpServletRequest request, HttpServletResponse response) {
		if ("clear".equals(request.getQueryString()))
			clearAll(response);
		else
			sendForm(request.getRequestURI() + "?clear", response);
	}

	private void clearAll(HttpServletResponse response) {
		PreparedQuery query = datastore.prepare(expiredSessions());
	    int count = query.countEntities(withDefaults());
	    for (Entity session : query.asIterable())
	    	deleteSessionBlobs(session.getKey(), this.blobstore, this.datastore);
		datastore.delete(new KeyIterable(query.asIterable()));
	    try {
	    	response.getWriter().println("Cleared " + count + " expired sessions.");
	    } catch (IOException ex) { }
	    count = 0;
	    for (Entity session_ref : datastore.prepare(new Query("_SESSION_REF").setKeysOnly()).asIterable()) {
	    	Key session_key = KeyFactory.createKey("_ah_SESSION", session_ref.getKey().getName());
	    	try {
	    		datastore.get(session_key);
	    	} catch (EntityNotFoundException ex) {
	    		deleteSessionBlobs(session_key, this.blobstore, this.datastore);
	    		count++;
	    	}
	    }
	    try {
	    	response.getWriter().println("Cleared " + count + " orphaned sessions.");
	    } catch (IOException ex) { }
	    response.setStatus(200);
	}

	public static Query expiredSessions() {
		return new Query("_ah_SESSION")
		.setFilter(new FilterPredicate("_expires", Query.FilterOperator.LESS_THAN, System.currentTimeMillis()))
		.setKeysOnly();
	}

	public static void deleteSessionBlobs(Key sessionKey, BlobstoreService blobstore, DatastoreService datastore) {
		for (Entity entity : datastore.prepare(new Query("_BLOB_REF").setAncestor(sessionKey).setKeysOnly()).asIterable()) {
			blobstore.delete(new BlobKey(entity.getKey().getName()));
			datastore.delete(entity.getKey());
		}
		datastore.delete(KeyFactory.createKey("_SESSION_REF", sessionKey.getName()));
	}

	private void sendForm(String actionUrl, HttpServletResponse response) {
	    response.setContentType("text/html");
	    response.setCharacterEncoding("utf-8");
	    try {
	    	PrintWriter writer = response.getWriter();
	    	writer.println("<html><head><title>Session Cleanup</title></head>");
			PreparedQuery query = datastore.prepare(expiredSessions());
		    int count = query.countEntities(withDefaults());
	    	writer.println("<body>There are currently " + count + " expired sessions.");
		    count = 0;
		    for (Entity session_ref : datastore.prepare(new Query("_SESSION_REF").setKeysOnly()).asIterable()) {
		    	Key session_key = KeyFactory.createKey("_ah_SESSION", session_ref.getKey().getName());
		    	try {
		    		datastore.get(session_key);
		    	} catch (EntityNotFoundException ex) {
		    		// just count
		    		//deleteSessionBlobs(session_key, this.blobstore, this.datastore);
		    		count++;
		    	}
		    }
	    	writer.println("<br />There are currently " + count + " orphaned sessions.");
	    	writer.println("<p><form method=\"POST\" action=\"" + actionUrl + "\">");
	    	writer.println("<input type=\"submit\" value=\"Erase them all\" >");
	    	writer.println("</form></body></html>");
	    }
	    catch (IOException ex) {
	    	response.setStatus(500);
	    	try {
	    		response.getWriter().println(ex);
	    	}
	    	catch (IOException innerEx) {
	    	}
	    }
	    response.setStatus(200);
	}
}
