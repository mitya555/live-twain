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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.*;

//import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
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
		
		if (req.getParameter("pdf") != null) {
			BlobInfo file;
			List<BlobInfo> bi = blobstoreService.getBlobInfos(req).get("file");
			if (bi != null && bi.size() > 0 && (file = bi.get(0)) != null && file.getSize() > 0) {
				// return PDF blob key
				resp.getWriter().print(file.getBlobKey().getKeyString());
			}
		}
		else if (req.getParameter("newpdf") != null) {
			newPdf(req, resp);
		}
		else {
			HttpSession ses = req.getSession();
			
			List<BlobKey> bkeys = sessionList(ses, "img-blob-key");
			List<Long> bsizes = sessionList(ses, "img-blob-size");
			List<ImageInfo> binfo = sessionList(ses, "img-blob-info");

			int image_num = req.getParameter("image_num") != null ?
					Integer.parseInt(req.getParameter("image_num")) : -1;

			if (image_num == 0 || image_num == 1)
				ses.setAttribute("invalidate-cache", true);
			
			BlobInfo file;
			List<BlobInfo> bi = null;
			if (image_num > 0)
				bi = blobstoreService.getBlobInfos(req).get("image");
			if (bi != null && bi.size() > 0 && (file = bi.get(0)) != null && file.getSize() > 0) {
				if (ses.getAttribute("invalidate-cache") != null) {
					// delete image blobs
					if (bkeys.size() > 0) {
						deleteBlobs(bkeys, this.blobstoreService, this.datastoreService);
					}
					bkeys.clear();
					bsizes.clear();
					binfo.clear();
//					// delete old PDF blob
//					BlobKey old_pdf_blob = replacePdfBlob(ses, null);
//					if (old_pdf_blob != null)
//						blobstoreService.delete(old_pdf_blob);
					// drop the "invalidate-cache" session variable
					ses.removeAttribute("invalidate-cache");
				}
				BlobKey bk = file.getBlobKey();
				bkeys.add(bk);
				Entity ds_bk = new Entity("_BLOB_REF", bk.getKeyString(), createSessionKey(ses));
				ds_bk.setProperty("_blobkey", bk);
				datastoreService.put(ds_bk);
				bsizes.add(file.getSize());
				binfo.add(new ImageInfo(
						req.getParameter("w"), req.getParameter("h"), req.getParameter("bpp"),
						req.getParameter("w2"), req.getParameter("h2"), req.getParameter("bpp2")));
				// save lists back to Session 
				ses.setAttribute("img-blob-key", bkeys);
				ses.setAttribute("img-blob-size", bsizes);
				ses.setAttribute("img-blob-info", binfo);
			}
	
			if (req.getParameter("complete") != null) {
//				try {
//					HttpPost post = new HttpPost(
//							new URL(blobstoreService.createUploadUrl("/twain")), null);
//					post.addParam("pdf").writeBytes("True");
//					java.io.OutputStream outstream = post.addParam("file", "test.pdf", "application/pdf"); 
//					generatePdf(req, bkeys, bsizes, outstream);
//					StringBuilder pdf_blob_key = post.send(true, new StringBuilder());
//					// delete old PDF blob and save new one in the session
//					BlobKey old_pdf_blob = replacePdfBlob(ses, "" + pdf_blob_key);
//					if (old_pdf_blob != null)
//						blobstoreService.delete(old_pdf_blob);
					// return images and PDF blob keys in JSON format
					getBlobsJson(resp.getWriter(), bkeys, binfo/*, pdf_blob_key*/);
//				}
//				catch (Throwable e) {
//					e.printStackTrace();
////					resp.sendError(500, e.getMessage());
//					// return image blob keys in JSON format
//					getBlobsJson(resp.getWriter(), bkeys, binfo, pdf_blob_key);
//				}
			}
			else
				resp.getWriter().print("\"" + blobstoreService.createUploadUrl("/twain") + "\"");
		}
//		resp.setContentType("text/plain");
//		resp.getWriter().println("Hello, world");
	}

	public static Key createSessionKey(HttpSession session) {
		return KeyFactory.createKey("_ah_SESSION", "_ahs" + session.getId());
	}
	
	public static void deleteBlobs(List<BlobKey> bkeys, BlobstoreService blobstore, DatastoreService datastore) {
		blobstore.delete(bkeys.toArray(new BlobKey[0]));
		datastore.delete(new KeyIterable(datastore.prepare(
				new Query("_BLOB_REF")
				.setFilter(new FilterPredicate("_blobkey", FilterOperator.IN, bkeys))
				.setKeysOnly()
			).asIterable()));
	}

	private void newPdf(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, MalformedURLException, ServletException {

		resp.setContentType("application/pdf");
		
		HttpSession ses = req.getSession();
		
		List<BlobKey> bkeys = sessionList(ses, "img-blob-key");
		List<Long> bsizes = sessionList(ses, "img-blob-size");

		try {
			generatePdf(req, bkeys, bsizes, resp.getOutputStream());
		}
		catch (DocumentException ex) {
			ex.printStackTrace();
			throw new ServletException(ex);
		}
	}

	private void generatePdf(HttpServletRequest req, List<BlobKey> bkeys,
			List<Long> bsizes, java.io.OutputStream outstream)
			throws DocumentException, IOException, MalformedURLException {
		Document document = initDoc(new Document(), "landscape".equalsIgnoreCase(req.getParameterValues("orientation")[0]));
		PdfWriter writer = PdfWriter.getInstance(document, outstream);
		writer.setCloseStream(false);
		document.open();
//		document.add(new Paragraph("This is a paragraph"));
		for (int i = 0; i < bkeys.size(); i++) {
			if (i != 0) {
				final String orientation = req.getParameterValues("orientation")[i];
				if (strNotEmpty(orientation))
					document.setPageSize("landscape".equalsIgnoreCase(orientation) ? PageSize.LETTER.rotate() : PageSize.LETTER);
				document.newPage();
			}
//			byte[] imgb = ImagesServiceFactory.makeImageFromBlob(bkeys.get(i)).getImageData();
//			byte[] imgb = blobstoreService.fetchData(bkeys.get(i), 0L, bsizes.get(i));
			byte[] imgb = readBlobData(bkeys.get(i), bsizes.get(i));
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

	public static void getBlobsJson(PrintWriter writer, List<BlobKey> bkeys, List<ImageInfo> binfo/*, Object pdf_blob_key*/) {
		// return images and PDF blob keys in JSON format
		writer.print("{img_blob_key:[");
		for (int i = 0; i < bkeys.size(); i++)
			writer.print((i > 0 ? "," : "") + 
					"'" + bkeys.get(i).getKeyString() + "'");
		writer.print("]");
		writer.print(",img_blob_info:[");
		for (int i = 0; i < binfo.size(); i++)
			writer.print((i > 0 ? "," : "") + 
					"{w:" + binfo.get(i).w +
					",h:" + binfo.get(i).h +
					",bpp:" + binfo.get(i).bpp +
					",w2:" + binfo.get(i).w2 +
					",h2:" + binfo.get(i).h2 +
					",bpp2:" + binfo.get(i).bpp2 + "}");
		writer.print("]");
//		writer.print(",pdf_blob_key:'" + (pdf_blob_key != null ? pdf_blob_key : "") + "'");
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

//	private static final String pdf_ses_var_name = "pdf-blob-key";
//
//	public static BlobKey getPdfBlobKey(HttpSession ses) {
//		BlobKey res = null;
//		if (ses.getAttribute(pdf_ses_var_name) != null &&
//				(ses.getAttribute(pdf_ses_var_name) instanceof BlobKey))
//			res = (BlobKey)ses.getAttribute(pdf_ses_var_name);
//		return res;
//	}
//
//	public static BlobKey replacePdfBlob(HttpSession ses, String pdf_blob_key) {
//		BlobKey res = getPdfBlobKey(ses);
//		if (pdf_blob_key != null)
//			ses.setAttribute(pdf_ses_var_name, new BlobKey(pdf_blob_key));
//		else
//			ses.removeAttribute(pdf_ses_var_name);
//		return res;
//	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> sessionList(HttpSession ses, final String ses_var_name) {
		if (ses.getAttribute(ses_var_name) == null ||
				!(ses.getAttribute(ses_var_name) instanceof List))
			ses.setAttribute(ses_var_name, new ArrayList<T>());
		return (List<T>)ses.getAttribute(ses_var_name);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (req.getParameter("newpdf") != null) {
			newPdf(req, resp);
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
