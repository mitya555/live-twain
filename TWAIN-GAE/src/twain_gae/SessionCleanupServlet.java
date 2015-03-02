package twain_gae;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
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
		PreparedQuery query = datastore.prepare(
				new Query(SESSION_ENTITY_TYPE)
				.setFilter(new FilterPredicate(EXPIRES_PROP, Query.FilterOperator.LESS_THAN, System.currentTimeMillis()))
				.setKeysOnly()
			);

	    int count = query.countEntities(withDefaults());

	    for (Entity session : query.asIterable()) {
	    	deleteSessionBlobs(session.getKey(), this.blobstore, this.datastore);
	    }

		datastore.delete(new KeyIterable(query.asIterable()));
	    response.setStatus(200);
	    try {
	    	response.getWriter().println("Cleared " + count + " expired sessions.");
	    }
	    catch (IOException ex) {
	    }
	}

	public static void deleteSessionBlobs(Key sessionKey, BlobstoreService blobstore, DatastoreService datastore) {
		for (Entity entity : datastore.prepare(new Query("_BLOB_REF").setAncestor(sessionKey).setKeysOnly()).asIterable()) {
			blobstore.delete(new BlobKey(entity.getKey().getName()));
			datastore.delete(entity.getKey());
		}
	}

	private void sendForm(String actionUrl, HttpServletResponse response) {
		PreparedQuery query = datastore.prepare(
				new Query(SESSION_ENTITY_TYPE)
				.setFilter(new FilterPredicate(EXPIRES_PROP, Query.FilterOperator.LESS_THAN, System.currentTimeMillis()))
				.setKeysOnly()
			);

	    int count = query.countEntities(withDefaults());

	    response.setContentType("text/html");
	    response.setCharacterEncoding("utf-8");
	    try {
	    	PrintWriter writer = response.getWriter();
	    	writer.println("<html><head><title>Session Cleanup</title></head>");
	    	writer.println("<body>There are currently " + count + " expired sessions.");
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
