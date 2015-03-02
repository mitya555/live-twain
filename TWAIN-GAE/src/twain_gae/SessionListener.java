package twain_gae;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

//import com.google.appengine.api.blobstore.BlobKey;
//import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
//import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

public class SessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent evt) {
		// delete image blobs
		TWAINServlet.deleteSessionBlobs(evt.getSession(), 
				BlobstoreServiceFactory.getBlobstoreService(), 
				DatastoreServiceFactory.getDatastoreService());
	}
}
