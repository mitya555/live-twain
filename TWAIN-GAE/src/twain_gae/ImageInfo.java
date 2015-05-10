package twain_gae;

import java.io.Serializable;

import com.google.appengine.api.datastore.EmbeddedEntity;

@SuppressWarnings("serial")
public class ImageInfo implements Serializable {
	public long w, h, bpp, w2, h2, bpp2, filesize, initpos;
	public String fmt = "", filename = "";
	private static long string2long(String s) { return s != null ? new Long(s) : 0; }
	public ImageInfo(String w, String h, String bpp, String w2, String h2, String bpp2, long initpos) {
		this.w = string2long(w);
		this.h = string2long(h);
		this.bpp = string2long(bpp);
		this.w2 = string2long(w2);
		this.h2 = string2long(h2);
		this.bpp2 = string2long(bpp2);
		this.initpos = initpos;
	}
	public ImageInfo(long w, long h, String bpp, String w2, String h2, String bpp2, long initpos, String fmt, long filesize, String filename) {
		this("" + w, "" + h, bpp, w2, h2, bpp2, initpos);
		this.fmt = fmt;
		this.filesize = filesize;
		this.filename = filename;
	}
	public ImageInfo(EmbeddedEntity e) {
		this.w = (Long)e.getProperty("w");
		this.h = (Long)e.getProperty("h");
		this.bpp = (Long)e.getProperty("bpp");
		this.w2 = (Long)e.getProperty("w2");
		this.h2 = (Long)e.getProperty("h2");
		this.bpp2 = (Long)e.getProperty("bpp2");
		this.fmt = (String)e.getProperty("fmt");
		this.filesize = (Long)e.getProperty("filesize");
		this.filename = (String)e.getProperty("filename");
		this.initpos = (Long)e.getProperty("initpos");
	}
	public EmbeddedEntity getEmbeddedEntity() {
		EmbeddedEntity e = new EmbeddedEntity();
		e.setProperty("w", this.w);
		e.setProperty("h", this.h);
		e.setProperty("bpp", this.bpp);
		e.setProperty("w2", this.w2);
		e.setProperty("h2", this.h2);
		e.setProperty("bpp2", this.bpp2);
		e.setProperty("fmt", this.fmt);
		e.setProperty("filesize", this.filesize);
		e.setProperty("filename", this.filename);
		e.setProperty("initpos", this.initpos);
		return e;
	}
}
