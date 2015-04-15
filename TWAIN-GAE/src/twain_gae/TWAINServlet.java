package twain_gae;

//import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
//import java.io.DataOutputStream;
import java.io.IOException;
//import java.io.InputStreamReader;
import java.io.PrintWriter;
//import java.io.Serializable;
//import java.net.HttpURLConnection;
import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.Map;


import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
//import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
//import com.google.appengine.api.datastore.PreparedQuery;
//import com.google.appengine.api.datastore.Query.FilterOperator;
//import com.google.appengine.api.datastore.Query.FilterPredicate;
//import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.api.datastore.Query;
//import com.google.appengine.api.images.ImagesService;
//import com.google.appengine.api.images.ImagesServiceFactory;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
//import com.itextpdf.text.DocumentException;
//import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import static com.google.appengine.api.datastore.FetchOptions.Builder.*;

@SuppressWarnings("serial")
public class TWAINServlet extends HttpServlet {

	private static final boolean DEBUG = true;
	@SuppressWarnings("unused")
	private static void error(String msg) {
		if (DEBUG)
			System.err.println(msg);
	}
//	private static void debug(String msg) {
//		if (DEBUG)
//			System.out.println(msg);
//	}
	private static boolean strNotEmpty(String str) {
		return str != null && str.length() != 0;
	}

//	private class HttpPost {
//		private static final String crlf = "\r\n";
//		private static final String twoHyphens = "--";
//		private static final String boundary = "*****";
//		private HttpURLConnection conn = null;
//		private DataOutputStream wr = null;
//		private boolean hasParam = false;
//		public HttpPost(URL url, String cookie) throws IOException {
//			debug("Connect to: " + url);
//			conn = (HttpURLConnection)url.openConnection();
//			conn.setDoOutput(true);
//			conn.setRequestMethod("POST");
//			conn.setRequestProperty("Connection", "Keep-Alive");
//			conn.setRequestProperty("Cache-Control", "no-cache");
//			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
//			String cookie_ = conn.getRequestProperty("Cookie");
//			debug("Cookie(auto preset): " + cookie_);
//			if (!strNotEmpty(cookie_)) {
//				debug("Cookie(func param): " + cookie);
//				if (strNotEmpty(cookie))
//					conn.setRequestProperty("Cookie", cookie);
//			}
//			wr = new DataOutputStream(conn.getOutputStream());
//		}
//		public DataOutputStream addParam(String name, String filename, String contenttype) throws IOException {
//			if (hasParam)
//				wr.writeBytes(crlf);				
//			wr.writeBytes(twoHyphens + boundary + crlf);
//			wr.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" +
//					(strNotEmpty(filename) && strNotEmpty(contenttype) ?
//							"; filename=\"" + filename + "\"" + crlf +
//							"Content-Type: " + contenttype + crlf +
//							"Content-Transfer-Encoding: binary" + crlf :
//								crlf));
//			wr.writeBytes(crlf);
//			hasParam = true;
//			return wr;
//		}
//		public DataOutputStream addParam(String name) throws IOException {
//			return addParam(name, null, null);
//		}
//		public StringBuilder send(boolean getresponse, StringBuilder response) throws IOException {
//			if (hasParam)
//				wr.writeBytes(crlf);				
//			wr.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
//			wr.flush();
//			wr.close();
//			debug("Response: " + conn.getResponseCode() + " " + conn.getResponseMessage());
//			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//			String line;
//			debug("Response Body:");
//			while ((line = rd.readLine()) != null) {
//				if (getresponse)
//					response.append(line);
//				debug(line);
//			}
//			rd.close();
//			return response;
//		}
//	}
	
	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
	private DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

//		boolean isMultiPart = ServletFileUpload.isMultipartContent(req);
		
		if (req.getParameter("newpdf") != null) {
			newPdf(req, resp);
		}
		else if (req.getParameter("clear") != null) {
			deleteSessionBlobs(req.getSession(), this.blobstoreService, this.datastoreService);
		}
		else {
			
			HttpSession session = req.getSession();

			int image_num = req.getParameter("image_num") != null ? Integer.parseInt(req.getParameter("image_num")) : -1;

//			if (image_num == 0 || image_num == 1)
//				session.setAttribute("invalidate-cache", true);
			
			if (image_num > 0) {
				List<BlobInfo> files = blobstoreService.getBlobInfos(req).get("image");
				if (files != null)
					for (BlobInfo file : files)
						if (file != null && file.getSize() > 0) {
//							if (session.getAttribute("invalidate-cache") != null) {
//								// delete image blobs
//								deleteBlobs(session, this.blobstoreService, this.datastoreService);
//								// drop the "invalidate-cache" session variable
//								session.removeAttribute("invalidate-cache");
//							}
							BlobKey bk = file.getBlobKey();
							Entity ds_bk = new Entity("_BLOB_REF", bk.getKeyString(), createSessionKey(session));
							ds_bk.setProperty("_filesize", file.getSize());
							if (req.getParameter("w") != null && req.getParameter("h") != null)
								ds_bk.setProperty("_imginfo", new ImageInfo(
										req.getParameter("w"), req.getParameter("h"), req.getParameter("bpp"),
										req.getParameter("w2"), req.getParameter("h2"), req.getParameter("bpp2")).getEmbeddedEntity());
							else {
						        com.google.appengine.api.images.Image img = // ImagesServiceFactory.makeImageFromBlob(bk); // throws java.lang.UnsupportedOperationException: No image data is available.
						        	ImagesServiceFactory.makeImage(readBlobData(bk, file.getSize()));
								ds_bk.setProperty("_imginfo", new ImageInfo(
										img.getWidth(), img.getHeight(), req.getParameter("bpp"),
										req.getParameter("w2"), req.getParameter("h2"), req.getParameter("bpp2"),
										img.getFormat().toString(), file.getSize()).getEmbeddedEntity());
							}
							long _next_ordinal;
							session.setAttribute("img-cnt", _next_ordinal = 
								session.getAttribute("img-cnt") != null ? (Long)session.getAttribute("img-cnt") + 1L : 0L);
							ds_bk.setProperty("_ordinal", _next_ordinal);
							datastoreService.put(ds_bk);
							datastoreService.put(new Entity("_SESSION_REF", createSessionKeyName(session))); 
						}
			}
	
			if (req.getParameter("sort") != null) {
				String[] _bks = req.getParameterValues("blob-key");
				for (int i = 0; i < _bks.length; i++) {
					try {
						Entity ds_bk = this.datastoreService.get(KeyFactory.createKey(createSessionKey(req.getSession()), "_BLOB_REF", _bks[i]));
						ds_bk.setProperty("_ordinal", i);
						this.datastoreService.put(ds_bk);
					} catch (EntityNotFoundException ex) {
						ex.printStackTrace();
					}
				}
			}

			if (req.getParameter("complete") != null) {
					// return images blob keys and info in JSON format
					getBlobsJson(resp.getWriter(), getBlobRefs(session, this.datastoreService));
			}
			else
				resp.getWriter().print("\"" + blobstoreService.createUploadUrl("/twain") + "\"");
		}
//		resp.setContentType("text/plain");
//		resp.getWriter().println("Hello, world");
	}
	
	public static List<Entity> getBlobRefs(HttpSession session, DatastoreService datastore) {
		return datastore.prepare(
				new Query("_BLOB_REF").setAncestor(createSessionKey(session))
				.addSort("_ordinal")).asList(withDefaults());
	}

	public static String createSessionKeyName(HttpSession session) {
		return "_ahs" + session.getId();
	}

	public static Key createSessionKey(HttpSession session) {
		return KeyFactory.createKey("_ah_SESSION", createSessionKeyName(session));
	}
	
	public static void deleteSessionBlobs(HttpSession session, BlobstoreService blobstore, DatastoreService datastore) {
		SessionCleanupServlet.deleteSessionBlobs(createSessionKey(session), blobstore, datastore);
	}
	
	private void newPdf(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, MalformedURLException, ServletException {

		resp.setContentType("application/pdf");

		try {
			generatePdf(req, getBlobRefs(req.getSession(), this.datastoreService), resp.getOutputStream());
		}
		catch (DocumentException ex) {
			ex.printStackTrace();
			throw new ServletException(ex);
		}
	}

	private void generatePdf(HttpServletRequest req, List<Entity> brefs, 
			java.io.OutputStream outstream) throws DocumentException, IOException, MalformedURLException {
		
		Document document = initDoc(new Document(), "landscape".equalsIgnoreCase(req.getParameterValues("orientation")[0]));
		PdfWriter writer = PdfWriter.getInstance(document, outstream);
		writer.setCloseStream(false);
		document.open();
//		document.add(new Paragraph("This is a paragraph"));
		for (int i = 0; i < brefs.size(); i++) {
			if (i != 0) {
				final String orientation = req.getParameterValues("orientation")[i];
				if (strNotEmpty(orientation))
					document.setPageSize("landscape".equalsIgnoreCase(orientation) ? PageSize.LETTER.rotate() : PageSize.LETTER);
				document.newPage();
			}
//			byte[] imgb = ImagesServiceFactory.makeImageFromBlob(new BlobKey(brefs.get(i).getKey().getName())).getImageData();
//			byte[] imgb = blobstoreService.fetchData(bkeys.get(i), 0L, (Long)brefs.get(i).getProperty("_filesize"));
			byte[] imgb = readBlobData(new BlobKey(brefs.get(i).getKey().getName()), (Long)brefs.get(i).getProperty("_filesize"));
			Image img = Image.getInstance(imgb);
			final String crop = req.getParameterValues("crop-data")[i];
			if (strNotEmpty(crop)) {
				String[] arr = crop.split(",");
				float cropL = Float.parseFloat(arr[0]), cropT = Float.parseFloat(arr[1]),
					cropW = Float.parseFloat(arr[2]), cropH = Float.parseFloat(arr[3]),
					imgW = img.getWidth(), imgH = img.getHeight();
				PdfTemplate t = writer.getDirectContent().createTemplate(cropW, cropH);
				t.rectangle(0, 0, cropW, cropH);
				t.clip();
				t.newPath();
				t.addImage(img, imgW, 0, 0, imgH, -cropL, -imgH + cropT + cropH);
        		img = Image.getInstance(t);
			}
			final String rotate = req.getParameterValues("rotate")[i];
			if (strNotEmpty(rotate)) {
				float rotate_ = Float.parseFloat(rotate);
				if (Math.abs(rotate_) == 90)
					rotate_ = -rotate_;
				img.setRotationDegrees(rotate_);
			}
			final String stf = req.getParameterValues("scale-to-fit")[i];
			if (strNotEmpty(stf)) {
				boolean up_only = "up".equalsIgnoreCase(stf), down_only = "down".equalsIgnoreCase(stf);
				float cw = document.right() - document.left(), ch = document.top() - document.bottom(), iw = img.getWidth(), ih = img.getHeight();
				if ((int)iw != (int)cw || (int)ih != (int)ch) {
					boolean scale_up = (cw > iw && ch > ih);
					if ((up_only && scale_up) || (down_only && !scale_up) || (!up_only && !down_only))
						img.scaleToFit(cw, ch);
				}
			}
			try {
				document.add(img);
			}
			catch (BadElementException ex) {
				ex.printStackTrace();
			}
		}
		document.close();
	}

	public static void getBlobsJson(PrintWriter writer, List<Entity> brefs) {
		// return images blob keys and info in JSON format
		writer.print("{img_blob_key:[");
		for (int i = 0; i < brefs.size(); i++)
			writer.print((i > 0 ? "," : "") + 
					"'" + brefs.get(i).getKey().getName() + "'");
		writer.print("]");
		writer.print(",img_blob_info:[");
		for (int i = 0; i < brefs.size(); i++) {
			ImageInfo ii = new ImageInfo((EmbeddedEntity)brefs.get(i).getProperty("_imginfo"));
			writer.print((i > 0 ? "," : "") + 
					"{w:" + ii.w + ",h:" + ii.h + ",bpp:" + ii.bpp + ",w2:" + ii.w2 + ",h2:" + ii.h2 + ",bpp2:" + ii.bpp2 + ",fmt:'" + ii.fmt + "',filesize:" + ii.filesize + "}");
		}
		writer.print("]");
		writer.print("}");
	}

	public static Document initDoc(Document document, boolean isLandscape) {
		document.setMargins(0f, 0f, 0f, 0f);
		document.setPageSize(isLandscape ? PageSize.LETTER.rotate() : PageSize.LETTER);
		return document;
	}

	private byte[] readBlobData(BlobKey blobKey, long blobSize) throws IOException {
//	    byte[] allTheBytes = new byte[0];
	    long amountLeftToRead = blobSize;
	    long startIndex = 0;
	    ByteArrayOutputStream res = new ByteArrayOutputStream((int)blobSize);
	    while (amountLeftToRead > 0) {
	        long amountToReadNow = Math.min(
	                BlobstoreService.MAX_BLOB_FETCH_SIZE - 1, amountLeftToRead);
	        byte[] chunkOfBytes = blobstoreService.fetchData(blobKey,
	                startIndex, startIndex + amountToReadNow - 1);
//	        allTheBytes = ArrayUtils.addAll(allTheBytes, chunkOfBytes);
	        res.write(chunkOfBytes);
	        amountLeftToRead -= amountToReadNow;
	        startIndex += amountToReadNow;
	    }
//	    return allTheBytes;
	    return res.toByteArray();
	}


	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		if (req.getParameter("newpdf") != null) {
			newPdf(req, resp);
		}
		else if (req.getParameter("clear") != null) {
			deleteSessionBlobs(req.getSession(), this.blobstoreService, this.datastoreService);
		}
		else {
			BlobKey blobKey = new BlobKey(req.getParameter("blob-key"));
			String crop = req.getParameter("crop"), rotate = req.getParameter("rotate"), cw = req.getParameter("cw"), ch = req.getParameter("ch");
			if (crop == null && rotate == null && cw == null && ch == null)
				blobstoreService.serve(blobKey, resp);
			else {
				ImagesService imagesService = ImagesServiceFactory.getImagesService();
				com.google.appengine.api.images.Image oldImage = ImagesServiceFactory.makeImageFromBlob(blobKey);
		        Transform transform = null;
		        if (crop != null) {
					String[] arr = crop.split(",");
					float cropL = Float.parseFloat(arr[0]), cropT = Float.parseFloat(arr[1]),
						cropR = Float.parseFloat(arr[2]), cropB = Float.parseFloat(arr[3]);
					transform = ImagesServiceFactory.makeCrop(cropL, cropT, cropR, cropB);
		        }
		        if (rotate != null) {
		        	Transform rotate_ = ImagesServiceFactory.makeRotate(Integer.parseInt(rotate));
		        	transform = transform == null ? rotate_ : ImagesServiceFactory.makeCompositeTransform(Arrays.asList(transform, rotate_));
		        }
		        if (cw != null && ch != null) {
		        	Transform resize_ = ImagesServiceFactory.makeResize(Integer.parseInt(cw), Integer.parseInt(ch));
		        	transform = transform == null ? resize_ : ImagesServiceFactory.makeCompositeTransform(Arrays.asList(transform, resize_));
		        }
		        com.google.appengine.api.images.Image newImage = imagesService.applyTransform(transform, oldImage);
		        resp.getOutputStream().write(newImage.getImageData());
			}
		}
//		resp.setContentType("text/plain");
//		resp.getWriter().println("Hello, world");
	}
}
