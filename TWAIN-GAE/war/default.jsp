<%@page import="java.io.PrintWriter"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="twain_gae.ImageInfo"%>
<%@page import="twain_gae.TWAINServlet"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreService" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.appengine.api.blobstore.BlobKey" %>
<%@ page import="com.itextpdf.text.Document" %>
<%--<%!
private String pad(String str,int len,String chr){if(str.length()>=len)return "";String pad="";for(int i=0;i<len-str.length();i++)pad+=chr;return "<span style='visibility:hidden;'>"+pad+"</span>";}
private String rpad(String str,int len,String chr){return str+pad(str,len,chr);}
private String lpad(String str,int len,String chr){return pad(str,len,chr)+str;}
private enum SizeSuffix { B, KB, MB, GB, TB } 
private double round(double num, int dec) {
	double tmp = Math.pow(10d, (double)dec);
	return Math.round(num * tmp) / tmp;
}
private String imgsize(int w,int h,int bpp){
	double sz = (((w*bpp+31)>>5)<<2)*h;
	SizeSuffix suffix = SizeSuffix.B;
	while (sz >= 1000d) {
		sz /= 1024d;
		switch (suffix) {
		case B: suffix = SizeSuffix.KB; break;
		case KB: suffix = SizeSuffix.MB; break;
		case MB: suffix = SizeSuffix.GB; break;
		case GB: suffix = SizeSuffix.TB; break;
		}
	}
	return lpad(new DecimalFormat("#.00").format(round(sz, 2)), 6, "8") + " " + lpad("" + suffix, 2, "B");
}
%>--%>
<%--<%
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
	Document doc = TWAINServlet.initDoc(new Document(), false);
	float w = doc.right() - doc.left(),
			h = doc.top() - doc.bottom();
	TWAINServlet.initDoc(doc, true);
	float wl = doc.right() - doc.left(),
			hl = doc.top() - doc.bottom();
%>--%>
<%
	Document doc = TWAINServlet.initDoc(new Document(), true); // landscape
	float cw = doc.right() - doc.left(), ch = doc.top() - doc.bottom();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>TWAIN Applet</title>
<link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.3/themes/smoothness/jquery-ui.css" />
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.1/jquery.min.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.3/jquery-ui.min.js"></script>
<script src="//cdnjs.cloudflare.com/ajax/libs/jquery.blockUI/2.66.0-2013.10.09/jquery.blockUI.min.js"></script>
<script type="text/javascript">
$.extend($.blockUI.defaults.css, {
	border: 'none', 
	padding: '15px', 
	backgroundColor: '#000', 
	'-webkit-border-radius': '10px', 
	'-moz-border-radius': '10px', 
	opacity: .5, 
	color: '#fff' 
});
</script>
<script type="text/javascript">
if (!String.prototype.endsWith) {
	(function() {
		var _endsWith = function (searchString, position) {
			position = position || this.length;
			position = position - searchString.length;
			var lastIndex = this.lastIndexOf(searchString);
			return lastIndex !== -1 && lastIndex === position;
		};
		try {
			Object.defineProperty(String.prototype, 'endsWith', { enumerable: false, configurable: false, writable: false, value: _endsWith });
		} catch(ex) {
			String.prototype.endsWith = _endsWith;
		}
	})();
}
Number.prototype.round = function(dec) { var tmp = Math.pow(10, dec); return Math.round(this * tmp) / tmp; };
Number.prototype.oldToFixed = Number.prototype.toFixed;
Number.prototype.toFixed = function(dec) { return this.round(dec).oldToFixed(dec); };
function pad(str,len,chr,color_white){if(str.length>=len)return "";var pad="";for(var i=0;i<len-str.length;i++)pad+=chr;return "<span style='"+(color_white?"color:white;":"visibility:hidden;")+"'>"+pad+"</span>";}
function rpad(str,len,chr,color_white){return str+pad(str,len,chr,color_white);}
function lpad(str,len,chr,color_white){return pad(str,len,chr,color_white)+str;}

var TYPE_CUSTOM            = 0x00;
var TYPE_INT_RGB           = 0x01;
var TYPE_INT_ARGB          = 0x02;
var TYPE_INT_ARGB_PRE      = 0x03;
var TYPE_INT_BGR           = 0x04;
var TYPE_3BYTE_BGR         = 0x05;
var TYPE_4BYTE_ABGR        = 0x06;
var TYPE_4BYTE_ABGR_PRE    = 0x07;
var TYPE_USHORT_565_RGB    = 0x08;
var TYPE_USHORT_555_RGB    = 0x09;
var TYPE_BYTE_GRAY         = 0x0A;
var TYPE_USHORT_GRAY       = 0x0B;
var TYPE_BYTE_BINARY       = 0x0C;
var TYPE_BYTE_INDEXED      = 0x0D;

function imgsz(w,h,bpp){return (((w*bpp+31)>>5)<<2)*h;}
function img_dim_from_size(width,height,bpp,type,size){
	if (bpp < 16 || type == TYPE_USHORT_GRAY)
		bpp = 24;
	//(w*bpp/8 + 3)*w*height/width = size
	// w^2 * bpp/8 + w * 3 + (-size)*width/height = 0
	var a_ = bpp/8, b_ = 3, c_ = -size*width/height;
	var w_ = Math.floor((-b_ + Math.sqrt(b_*b_ - 4*a_*c_))/(2*a_)),
		h_ = Math.floor(w_*height/width);
	return { width: w_, height: h_ };
}
function img_resize(up,down,cw,ch,iw,ih){
	if (iw != cw || ih != ch) {
		var scale_up = (cw > iw && ch > ih);
		return ((up && scale_up) || (down && !scale_up));
	}
	return false;
}
function imgsize(w,h,bpp){
	var sz = h || bpp ? imgsz(w,h,bpp) : w;
	var suffix = "B";
	while (sz >= 1000) {
		sz /= 1024;
		switch (suffix) {
		case "B": suffix = "KB"; break;
		case "KB": suffix = "MB"; break;
		case "MB": suffix = "GB"; break;
		case "GB": suffix = "TB"; break;
		}
	}
	return lpad("" + sz.toFixed(2), 6, "8") + " " + lpad(suffix, 2, "B");
}

function scan_start_(num, complete) {
	if (num > 0)
		$('#scan-message').text('Scanning page #' + num + '...');
}
function scan_send_(num, complete, img_info) {
	if (num > 0)
		$('#scan-message').text(complete ? 'Creating PDF document...' : 'Processing page #' + num + '...');<%--
	var	scale_down = ($('input:checkbox#scale-to-fit-client-down:checked').length > 0),
		scale_up = ($('input:checkbox#scale-to-fit-client-up:checked').length > 0);
	if (scale_down || scale_up) {
		var landscape = ($('input:radio[name=orientation]:checked').val() == 'landscape');
		return {
			width: (landscape ? <%= wl %> : <%= w %>),
			height: (landscape ? <%= hl %> : <%= h %>),
			'scale-up-down-both': !scale_down ? 'up' : !scale_up ? 'down' : 'both'/* ,
			scaleFactor: parseFloat($("#scale-factor-slider").slider("value")), // 0.85,
			interpolationHintUpscale: "BICUBIC",
			interpolationHintDownscale: "BILINEAR" */
		};
	}--%>
	var max_image_size_in_KB = parseInt($('#max-image-size-in-KB').val());
	if (max_image_size_in_KB && img_info && img_info['width'] && img_info['height'] && img_info['bpp'] &&
			imgsz(img_info.width, img_info.height, img_info.bpp) > max_image_size_in_KB * 1024) {
		return img_dim_from_size(img_info.width, img_info.height, img_info.bpp, img_info.type, max_image_size_in_KB * 1024);
	}
}
function page_preview(i, no_toggle) {
	var $blob_key_info = $("input#blob-key-" + i);
	var src = "/twain?blob-key=" + $blob_key_info.val();
	var crop = $("input#crop-data-" + i).val();
	if (crop) {
		var img_w = parseFloat($blob_key_info.data("w")), img_h = parseFloat($blob_key_info.data("h"));
		var arr = crop.split(",");
		var arr1 = [ parseFloat(arr[0])/img_w, parseFloat(arr[1])/img_h, 
		     		(parseFloat(arr[0]) + parseFloat(arr[2]))/img_w, (parseFloat(arr[1]) + parseFloat(arr[3]))/img_h ];
		src += "&crop=" + encodeURIComponent(arr1.join(","));
	}
	var rotate = $("select#rotate-" + i).val();
	if (rotate)
		src += "&rotate=" + rotate;
	var is_landscape = ($("select#orientation-" + i).val() === "landscape"),
		cw = is_landscape ? <%=cw%> : <%=ch%>,
		ch = is_landscape? <%=ch%> : <%=cw%>;
	if ($("input#scale-to-fit-checkbox-" + i).is(":checked")) {
		src += "&cw=" + cw;
		src += "&ch=" + ch;
	}
	if (is_landscape)
		src += "&orientation=landscape";
	var $page_div = $("div#page-" + (i + 1));
	if ($page_div.find("img").length == 0) {
		$page_div.empty().append("<div style='border: solid black 1px; overflow: hidden;'><img /></div>");
	}
	var $page_img = $page_div.find("img");
	$page_img.closest("div").css({ width: cw + "px", height: ch + "px" });
	var page_img = $page_img[0];
	if (!page_img.src || !page_img.src.endsWith(src)) {
		page_img.src = src;
		if ($page_div.is(":visible"))
			return;
	}
	if (no_toggle)
		return;
	$page_div.toggle();
}
function page_preview_onchange(i) {
	var $page_div = $("div#page-" + (i + 1));
	if ($page_div.is(":visible") && $page_div.find("img").length > 0)
		page_preview(i, true);
}
function page_preview_onchange_all() {
	for (var i = 0; i < $("input[name='blob-key']").length; i++)
		page_preview_onchange(i);
}
function img_preview(i) {
	var double_border = 4; // 2 x 2
	var $page_div = $("div#page-" + (i + 1));
	if ($page_div.find("img").length > 0 || $page_div.find("div").length == 0) {
		var $blob_key_info = $("input#blob-key-" + i),
			img_size = { w: $blob_key_info.data("w"), h: $blob_key_info.data("h") },
			$crop_data = $("input#crop-data-" + i),
			crop_origin, crop_size = { w: img_size.w - double_border, h: img_size.h - double_border };
		if ($crop_data.val())
		{
			var arr = $crop_data.val().split(","); 
			crop_origin = { l: parseInt(arr[0]), t: parseInt(arr[1]) };
			crop_size = { w: parseInt(arr[2]) - double_border, h: parseInt(arr[3]) - double_border };
		}
			
		$page_div.empty().append("\
<span style='color:brown;font-size:8pt;'>Selection frame can be resized and dragged with Left mouse button. Resizing with Shift key preserves aspect ratio of the frame.</span>\
<div style='width:" + img_size.w + "px;height:" + img_size.h + "px;background:url(/twain?blob-key=" + $blob_key_info.val() + ");position:relative;'>\
<div class='crop-frame' style='" + (crop_origin ? "left:" + crop_origin.l + "px;top:" + crop_origin.t + "px;" : "") + "width:" + crop_size.w + "px;height:" + crop_size.h + "px;'></div>\
</div>\
");
		$page_div.find("div.crop-frame")
			.resizable({
				handles: "n,ne,e,se,s,sw,w,nw",
				containment: "parent",
				resize: function( event, ui ) {
					var $crop_span = $blob_key_info.prevAll("span.crop-span");
					if ($crop_span.length == 0) {
						$crop_span = $("<span class='crop-span'></span>").insertBefore($blob_key_info);
					}
					crop_size = { w: ~~((ui.size.width + double_border) + 0.5), h: ui.size.height + double_border };
					$crop_span.html(lpad("", 8, "8") + "Crop:" +
							" origin: <span class='crop-origin-span'>" + ui.position.left + "x" + ui.position.top + "</span>;" +
							" size: <span class='crop-size-span'>" + crop_size.w + "x" + crop_size.h + "</span>");
					var arr = [ ui.position.left, ui.position.top, crop_size.w, crop_size.h ];
					$crop_data.val(arr.join(","));
				}
			})
			.draggable({
				containment: "parent",
				drag: function( event, ui ) {
					if (ui.position.left + crop_size.w > img_size.w)
						ui.position.left = img_size.w - crop_size.w;
					if (ui.position.top + crop_size.h > img_size.h)
						ui.position.top = img_size.h - crop_size.h;
					var $crop_span = $blob_key_info.prevAll("span.crop-span");
					if ($crop_span.length > 0 && ui.position.left + crop_size.w <= img_size.w && ui.position.top + crop_size.h <= img_size.h) {
						$crop_span.find("span.crop-origin-span").html(ui.position.left + "x" + ui.position.top);
						var arr = [ ui.position.left, ui.position.top, crop_size.w, crop_size.h ];
						$crop_data.val(arr.join(","));
					}
					else
						return false;
				}
			});
		if ($page_div.is(":visible"))
			return;
	}
	$page_div.toggle();
}
function create_links(data) {
	for (var i = 0; i < data.img_blob_key.length; i++) {
		var img_resized = (data.img_blob_info[i].w2 > 0 && data.img_blob_info[i].h2 > 0 && data.img_blob_info[i].bpp2 > 0),
			ii = img_resized ? { w: data.img_blob_info[i].w2, h: data.img_blob_info[i].h2, bpp: data.img_blob_info[i].bpp2 } : data.img_blob_info[i];
		$('\
<div>\
<span style="white-space:nowrap;">\
	' + lpad("" + (i + 1), 2, "0", true) + '. \
	<a href="javascript://" onclick="javascript:page_preview(' + i + ');" title="Page Preview">Page</a>: \
	&nbsp; \
</span>\
<span style="font-family:Arial;font-size:8pt;">\
	<span style="white-space:nowrap;">Orientation:\
		<select id="orientation-' + i + '" name="orientation" \
				onchange="javascript:page_preview_onchange(' + i + ');" \
				style="font-family:Arial;font-size:8pt;vertical-align:2px;">\
			<option>portrait</option>\
			<option>landscape</option>\
		</select>\
		&nbsp; &nbsp;\
	</span>\
</span>\
<a href="javascript://" onclick="javascript:img_preview(' + i + ');" title="Image Preview">Image</a>: \
&nbsp; &nbsp;\
<span style="font-family:Arial;font-size:8pt;">\
	<span style="white-space:nowrap;">Rotate:\
		<select id="rotate-' + i + '" name="rotate" \
				onchange="javascript:page_preview_onchange(' + i + ');" \
				style="font-family:Arial;font-size:8pt;vertical-align:2px;">\
			<option />\
			<option value="90">90&#xb0; CW</option>\
			<option value="-90">90&#xb0; CCW</option>\
			<option value="180">180&#xb0;</option>\
		</select>\
		&nbsp; &nbsp;\
	</span>\
	<span style="white-space:nowrap;">\
		<label for="scale-to-fit-checkbox-' + i + '">Scale to fit:</label>\
		<input type="checkbox" id="scale-to-fit-checkbox-' + i + '" name="scale-to-fit-checkbox" \
			onchange="javascript:page_preview_onchange(' + i + ');" \
			style="vertical-align:-1px;" />\
		<input type="hidden" id="scale-to-fit-hidden-' + i + '" name="scale-to-fit" value="" />\
		&nbsp; &nbsp;\
	</span>\
</span>\
' + (data.img_blob_info.length > i ?
		"<span style='white-space:nowrap;'>" + display_img_blob_info(ii, true) + (img_resized ?
				" &nbsp; (resized from " + display_img_blob_info(data.img_blob_info[i]) + ")" : "") + "</span>" : "") + "\
<input type='hidden' id='blob-key-" + i + "' name='blob-key'\
	data-w='" + ii.w + "'\
	data-h='" + ii.h + "'\
	value='" + data.img_blob_key[i] + "' />\
<input type='hidden' id='crop-data-" + i + "' name='crop-data' />\
<div id='page-" + (i + 1) + "' style='display:none;'>\<%--
	<a href='javascript://' onclick='javascript:$(this).closest(\"div\").css(\"display\",\"none\");' style='vertical-align:top;'>Close&raquo;</a>\--%><%--
	<div style='display:inline-block;'><img src='/twain?blob-key=" + data.img_blob_key[i] + "' /></div>\--%><%--
	<div style='width:" + ii.w + "px;height:" + ii.h + "px;background:url(/twain?blob-key=" + data.img_blob_key[i] + ");position:relative;'>\
	<div class='crop-frame' style='width:" + ii.w + "px;height:" + ii.h + "px;'></div>\
	</div>\--%>
</div>\
</div>").appendTo('div#file-links');
	}
	if (data.img_blob_key.length > 0) {
		$('<hr /><div>' + pdf_link() + '</div>').appendTo('div#file-links');
		$('<div>' + pdf_link() + '</div><hr />').prependTo('div#file-links');
	}
	set_stf(true);
}
function pdf_link() {
	return '<a href="javascript://" onclick="javascript:form_submit();" target="_blank" ' + 
		'style="color:black;font-weight:bold;text-decoration:none;margin-left:20px;">PDF</a>' +
		'<a href="javascript://" onclick="javascript:clear_all();" ' + 
		'style="color:black;font-weight:bold;text-decoration:none;margin-left:50px;">Clear All</a>';
}
function display_img_blob_info(ii, lpad_) {
	var sz = ii.w + "x" + ii.h + ";";
	if (lpad_)
		sz = lpad(sz, 12, "8");
	return sz + " " + (ii.bpp ? lpad("" + ii.bpp, 2, "8") + " bpp; " + imgsize(ii.w, ii.h, ii.bpp) :
		lpad(ii.fmt, 6, "8") + "; " + imgsize(ii.filesize));
}
function get_orientation() { return $('select[name="orientation"]'); }
function set_orientation() { return $('select[name="orientation"]').val($('select#orientation').val()); }
function get_rotate() { return $('select[name="rotate"]'); }
function set_rotate() { return $('select[name="rotate"]').val($('select#rotate').val()); }
function set_stf(reset_others) {
	var $orientation = reset_others ? set_orientation() : get_orientation();
	var $rotate = reset_others ? set_rotate() : get_rotate();
	var $stfcb = $('input:checkbox[name="scale-to-fit-checkbox"]');
	var	scale_down = ($('input:checkbox#scale-to-fit-down:checked').length > 0);
	var scale_up = ($('input:checkbox#scale-to-fit-up:checked').length > 0);
	$('input[name="blob-key"]').each(function(i) {
		var $this = $(this);
		var is_landscape = ($orientation.eq(i).val() === "landscape");
		var is_90_deg = ($rotate.eq(i).val() === "90" || $rotate.eq(i).val() === "-90"); 
		$stfcb[i].checked = img_resize(scale_up,scale_down,
				is_landscape?<%=cw%>:<%=ch%>,is_landscape?<%=ch%>:<%=cw%>,
				$this.data(is_90_deg?"h":"w"),$this.data(is_90_deg?"w":"h"));
	});
}
function form_submit() {
	var $stf = $('input[name="scale-to-fit"]');
	$('input:checkbox[name="scale-to-fit-checkbox"]').each(function(i) {
		$stf[i].value = this.checked ? 'both' : '';
	});
	$('form#pages')[0].submit();
}
function clear_all() {
	$('#scan-applet').css('visibility', 'hidden');
	$.blockUI();
	$.ajax({
		url: '/twain?clear=yes',
		success: function() {
			$('div#file-links').empty();
		},
		complete: function() {
			$.unblockUI({
				onUnblock: function() {
					$('#scan-applet').css('visibility', 'visible');
				}
			});
		}
	});
}
function scan_success_(num, complete, data) {
	if (num > 0)
		$('#scan-message').text(complete ? '' : 'Page #' + num + ' complete');
	else
		$('div#file-links').empty();
	if (complete) {
		create_links(data);
		return "twain";
	} else
		return data;
}
function scan_error_(msg) {
	$('#scan-message').text(msg);
}
function scan_params_(i) {
	switch (i) {
	case 0:
		var orientation = $('select#orientation').val();
		return orientation ? { name: 'orientation', value: orientation } : { value: null };
	case 1:
		var	scale_down = ($('input:checkbox#scale-to-fit-down:checked').length > 0),
			scale_up = ($('input:checkbox#scale-to-fit-up:checked').length > 0);
		return scale_down || scale_up ? { name: 'scale-to-fit', value: !scale_down ? 'up' : !scale_up ? 'down' : 'both' } : { value: null };
	}
}
/* $(function() {
	$("#scale-factor-slider").slider({
		range: "min",
		value: 0.85,
		min: 0,
		max: 0.95,
		step: 0.05,
		slide: function(event, ui) {
			$("#scale-factor-amount").val(ui.value);
		}
	});
	$("#scale-factor-amount").val($("#scale-factor-slider").slider("value"));
}); */
</script>
<style type="text/css">
/* #scale-factor-slider { font-size: 0.6em; } */
div.crop-frame { border: 2px dotted red; }
span.crop-span { white-space: nowrap; font-family: Arial; font-size: 8pt; }
span.pipe-delim { font-size: 20px; /* font-weight: bold; */ }
</style>
</head>
<body style="font-family:Arial;font-size:9pt;">
<div style="text-align:right;"><a href='http://icons8.com/license/' style='font-size:8pt;font-style:italic;color:green;'>icons8</a></div>
<div id="applet-container">
<applet code="TwainApplet.class" archive="TwainApplet.jar" id="scan-applet" style="vertical-align:bottom;"
	width="150" height="36" class="image-input" MAYSCRIPT><%--
	width="150" height="26" --%>
	<param name="permissions" value="all-permissions" />
	<%--<param name="ImageHandler" value="<%= blobstoreService.createUploadUrl("/twain") %>" /> --%>
	<param name="ImageHandler" value="twain" />
	<param name="Layout" value="5" />
	<%--<param name="HorizontalSpacing" value="10" />--%>
	<param name="JSCallback" value="scan_success_" />
	<param name="PassJsonResult" value="yes" />
	<param name="CallServerOnAcquire" value="yes" />
	<param name="JSBeforeScanCallback" value="scan_start_" />
	<param name="JSBeforeSendCallback" value="scan_send_" />
	<param name="JSErrorCallback" value="scan_error_" />
	<param name="JSBeforeAcquireCallback" value="scan_params_" />
	<%--<param name="JSCheckedChange" value="check_" />
	<param name="JSEnabledChange" value="enable_" />
	<param name="HideGui" value="yes" />--%>
	<param name="HideCheckbox" value="yes" />
	<param name="Cookie" value="<%= request.getHeader("Cookie") %>" />
	<param name="AcquireImgUrl" value="images/scanner.png" />
	<param name="SelectImgUrl" value="images/list.png" />
	<param name="AcquireLabel" value="Capture" />
	<param name="SelectLabel" value="Source" />
	<param name="AcquireToolTip" value="Capture" />
	<param name="SelectToolTip" value="Select Source" />

	<!--Your browser is completely ignoring the &lt;applet&gt; tag!-->
	<span style="font-style:italic;font-size:8pt;">To enable the Scan function on Windows machine please install <a href="http://java.com/en/download/">Java&trade;</a></span>
</applet> &nbsp;<span class="pipe-delim">|</span>&nbsp; 
<form id="file-upload" method="POST" enctype="multipart/form-data" style="display:inline-block;"><input id="image-file" type="file" name="image" multiple="true"></form>
<a href="javascript://" onclick="javascript:file_upload();" style="color:black;font-weight:bold;text-decoration:none;margin-left:0px;">Upload</a> &nbsp; 
<script src="//cdnjs.cloudflare.com/ajax/libs/jquery.form/3.51/jquery.form.min.js"></script>
<script type="text/javascript">
	function file_upload() {
		$.ajax({
			url: '/twain',
			method: 'post',
			dataType: 'json',
			success: function(data) {
				var input = document.getElementById('image-file');
				$('form#file-upload').ajaxSubmit({
					url: data,
					dataType: 'json',
					data: {
						image_num: input.files && input.files.length ? input.files.length : input.value ? 1 : 0,
						complete: 'yes'
					},
					success: function(data) {
						//
						scan_success_(0, true, data);
					},
					error: function() {
						//
						var i = 1;
					}
				});
			},
			error: function() {
				//
				var i = 1;
			}
		});
	}
</script>
<a href="javascript://" onclick="javascript:$('#options-container').toggle();$(this).html($(this).html()=='&laquo;'?'&raquo;':'&laquo;');" 
	style="color:black;font-size:12pt;font-weight:bold;text-decoration:none;margin-bottom:8px;display:inline-block;" 
	title="Display/Hide Options">&raquo;</a> &nbsp; 
<span id="options-container" style="display:none;">
<span style="white-space:nowrap;"><label for="max-image-size-in-KB">Image size threshold: </label>
	<select id="max-image-size-in-KB" style="margin-bottom:8px;">
		<option value=""></option>
		<option value="32">32K</option>
		<option value="64">64K</option>
		<option value="128">128K</option>
		<option value="256">256K</option>
		<option value="512">512K</option>
		<option value="1024">1M</option>
		<option value="2048">2M</option>
		<option value="4096">4M</option>
		<option value="8192">8M</option>
		<option value="16384">16M</option>
		<option value="32768">32M</option>
	</select> &nbsp;<span class="pipe-delim">|</span>&nbsp; 
</span>
<%--<hr />--%>
Page: 
<span style="white-space:nowrap;">Orientation: 
	<select id="orientation" 
			onchange="javascript:set_orientation();page_preview_onchange_all();" 
			style="/*vertical-align:2px;*/">
		<option>portrait</option>
		<option>landscape</option>
	</select> &nbsp;<span class="pipe-delim">|</span>&nbsp; 
</span>
Image: 
<span style="white-space:nowrap;">Rotate: 
	<select id="rotate" 
			onchange="javascript:set_rotate();page_preview_onchange_all();" 
			style="/*vertical-align:2px;*/">
		<option />
		<option value="90">90&#xb0; CW</option>
		<option value="-90">90&#xb0; CCW</option>
		<option value="180">180&#xb0;</option>
	</select> &nbsp; &nbsp; 
</span>
<span style="white-space:nowrap;">Scale to fit: 
	<label for="scale-to-fit-down">down: </label>
	<%--<input type="checkbox" id="scale-to-fit-client-down" />--%><%-- onclick="javascript:$('div#scale-factor-slider-container').css('display',this.checked?'inline-block':'none');"--%><%-- <label for="scale-to-fit-client-down">after capture</label>--%>
	<%--<div id="scale-factor-slider-container" style="display:none;">Down-Scale Factor: <input type="text" id="scale-factor-amount" style="border:0;width:40pt;" /> <div id="scale-factor-slider"></div></div>--%>
	<input type="checkbox" id="scale-to-fit-down" 
			onchange="javascript:set_stf(false);page_preview_onchange_all();" 
			style="vertical-align:-1px;" /><%-- <label for="scale-to-fit-down">before PDF</label>--%> 
	<label for="scale-to-fit-up">up: </label>
	<%--<input type="checkbox" id="scale-to-fit-client-up" /> <label for="scale-to-fit-client-up">after capture</label>--%>
	<input type="checkbox" id="scale-to-fit-up" 
			onchange="javascript:set_stf(false);page_preview_onchange_all();" 
			style="vertical-align:-1px;" /><%-- <label for="scale-to-fit-up">before PDF</label>--%>
</span> &nbsp; &nbsp; 
</span>
</div>
<br />
<span id="scan-message"></span>
<form id="pages" action="/twain?newpdf=yes" method="post" target="_blank">
<div id="file-links"><%--<%
HttpSession ses = request.getSession();
List<BlobKey> bkeys = TWAINServlet.sessionList(ses, "img-blob-key");
List<twain_gae.ImageInfo> binfo = TWAINServlet.sessionList(ses, "img-blob-info");
for (int i = 0; i < bkeys.size(); i++) { %>
	<div><a href='javascript://' onclick='javascript:$("div#page-<%= i + 1 %>").toggle();'>Page <%= i + 1 %></a> <%
	if (binfo.size() > i) {
	%><span style='font-family:Arial;font-size:8pt;white-space:nowrap;'><%
		if (binfo.get(i).w2 > 0 && binfo.get(i).h2 > 0 && binfo.get(i).bpp2 > 0) {
	%><%= lpad(binfo.get(i).w2 + "x" + binfo.get(i).h2 + ";", 12, "8") %> <%= lpad("" + binfo.get(i).bpp2, 2, "8") %> bpp; <%= imgsize(binfo.get(i).w2, binfo.get(i).h2, binfo.get(i).bpp2) %>; &nbsp; resized from <%
		}
	%><%= lpad(binfo.get(i).w  + "x" + binfo.get(i).h +  ";", 12, "8") %> <%= lpad("" + binfo.get(i).bpp,  2, "8") %> bpp; <%= imgsize(binfo.get(i).w, binfo.get(i).h, binfo.get(i).bpp) %></span><%
	}
	%></div>
	<div id='page-<%= i + 1 %>' style='display:none;'><img src='/twain?blob-key=<%= bkeys.get(i).getKeyString() %>' /></div><%
}
BlobKey res = null;
final String pdf_ses_var_name = "pdf-blob-key";
if (ses.getAttribute(pdf_ses_var_name) != null &&
		(ses.getAttribute(pdf_ses_var_name) instanceof BlobKey))
	res = (BlobKey)ses.getAttribute(pdf_ses_var_name);
if (res != null) { %>
	<div><a href='/twain?blob-key=<%=  res.getKeyString() %>' target='_blank'>PDF</a></div><%
}
%>--%>
</div>
</form>
</body>
<script type="text/javascript">
	create_links(<%
			TWAINServlet.getBlobsJson(new PrintWriter(out), // response.getWriter(),
					TWAINServlet.getBlobRefs(request.getSession(), DatastoreServiceFactory.getDatastoreService()));
	%>);<%--
	$("div.crop-frame").resizable({ handles: "n,ne,e,se,s,sw,w,nw", containment: "parent" }).draggable({ containment: "parent" });--%>
</script>
</html>