package twain_gae;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

public class SessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent evt) {
		DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
//		// delete PDF blob
//		BlobKey pdf_blob = TWAINServlet.replacePdfBlob(evt.getSession(), null);
//		if (pdf_blob != null) {
//			blobstoreService.delete(pdf_blob);
//		}
		// delete image blobs
		HttpSession session = evt.getSession();
		List<BlobKey> bkeys = TWAINServlet.sessionList(session, "img-blob-key");
		if (bkeys.size() > 0) {
			TWAINServlet.deleteBlobs(bkeys, blobstoreService, datastoreService);
		}
		SessionCleanupServlet.deleteSessionBlobs(TWAINServlet.createSessionKey(session), blobstoreService, datastoreService);
	}
}
