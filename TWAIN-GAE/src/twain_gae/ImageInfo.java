package twain_gae;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ImageInfo implements Serializable {
	public int w, h, bpp, w2, h2, bpp2;
	private static int string2int(String s) { return s != null ? new Integer(s) : 0; }
	public ImageInfo(String w, String h, String bpp, String w2, String h2, String bpp2) {
		this.w = string2int(w);
		this.h = string2int(h);
		this.bpp = string2int(bpp);
		this.w2 = string2int(w2);
		this.h2 = string2int(h2);
		this.bpp2 = string2int(bpp2);
	}
}
