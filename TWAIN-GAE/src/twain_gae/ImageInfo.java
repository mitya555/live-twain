package twain_gae;

import java.io.Serializable;

import com.google.appengine.api.datastore.EmbeddedEntity;

@SuppressWarnings("serial")
public class ImageInfo implements Serializable {
	public long w, h, bpp, w2, h2, bpp2;
	private static long string2long(String s) { return s != null ? new Long(s) : 0; }
	public ImageInfo(String w, String h, String bpp, String w2, String h2, String bpp2) {
		this.w = string2long(w);
		this.h = string2long(h);
		this.bpp = string2long(bpp);
		this.w2 = string2long(w2);
		this.h2 = string2long(h2);
		this.bpp2 = string2long(bpp2);
	}
	public ImageInfo(EmbeddedEntity e) {
		this.w = (Long)e.getProperty("w");
		this.h = (Long)e.getProperty("h");
		this.bpp = (Long)e.getProperty("bpp");
		this.w2 = (Long)e.getProperty("w2");
		this.h2 = (Long)e.getProperty("h2");
		this.bpp2 = (Long)e.getProperty("bpp2");
	}
	public EmbeddedEntity getEmbeddedEntity() {
		EmbeddedEntity e = new EmbeddedEntity();
		e.setProperty("w", this.w);
		e.setProperty("h", this.h);
		e.setProperty("bpp", this.bpp);
		e.setProperty("w2", this.w2);
		e.setProperty("h2", this.h2);
		e.setProperty("bpp2", this.bpp2);
		return e;
	}
}
