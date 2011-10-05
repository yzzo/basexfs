package org.basex.fs.fsml.build;

import static org.basex.util.Token.token;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.io.IO;
import org.basex.util.Token;

import com.thebuzzmedia.exiftool.ExifTool;

/** Extract metadata and content from an image and produce XML. */
public class IMGParser extends Parser {

	private static final byte[] FACT = token("fact");
	private static final byte[] FOLDER = token("folder");
	private static final byte[] NAME = token("name");
	private static final byte[] TITLE = token("title");
	private static final byte[] AUTHOR = token("author");
	private static final byte[] SUBJECT = token("subject");
	private static final byte[] KEYWORDS = token("keywords");
	private static final byte[] CREATIONDATE = token("creationdate");
	private static final byte[] META = token("meta");
	private static final byte[] TYPE = token("type");
	private static final byte[] HEIGHT = token("height");
	private static final byte[] WIDTH = token("width");
	private static final byte[] COLOR_SPACE = token("colorspace");
	private static final byte[] COMMENT = token("comment");
	private static final byte[] CONTRAST = token("contrast");
	
	private Builder builder;
	Map<ExifTool.Tag, String> img_info;
	ExifTool tool = new ExifTool();
	
	/**
	 * Constructor.
	 * @param path pathname to image file
	 */
	public IMGParser(String path) {
		this(IO.get(path));
	}
	
	public IMGParser(IO path) {
		super(path);
	}

	@Override
	public void parse(Builder b) throws IOException {
		try {
			img_info = tool.getImageMeta(new File(src.path()), ExifTool.Tag.IMAGE_HEIGHT, ExifTool.Tag.IMAGE_WIDTH, ExifTool.Tag.AUTHOR, ExifTool.Tag.COLOR_SPACE, ExifTool.Tag.COMMENT, ExifTool.Tag.CONTRAST, ExifTool.Tag.DATE_TIME_ORIGINAL, ExifTool.Tag.KEYWORDS, ExifTool.Tag.TITLE, ExifTool.Tag.SUBJECT);
		} catch (IOException e) {
			System.out.println("Detected empty or corrupt PDF file'" 
					+ src.path() + "': " + e.getMessage());
			return;
		}
		try {
			builder = b;
			atts.add(NAME, token("." + src.name() + ".deepfs"));
			atts.add(TYPE, META);
			builder.startElem(FOLDER, atts);
			insertIMGMetadata();
			builder.endElem();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void insertIMGMetadata() throws IOException {
		atts.reset();
		System.out.format("Extracting image metadata from %s.\n", src.path());
		insert(HEIGHT, img_info.get(ExifTool.Tag.IMAGE_HEIGHT));
		insert(WIDTH, img_info.get(ExifTool.Tag.IMAGE_WIDTH));
		insert(AUTHOR, img_info.get(ExifTool.Tag.AUTHOR));
		insert(COLOR_SPACE, img_info.get(ExifTool.Tag.COLOR_SPACE));
		insert(COMMENT, img_info.get(ExifTool.Tag.COMMENT));
		insert(KEYWORDS, img_info.get(ExifTool.Tag.KEYWORDS));
		insert(CREATIONDATE, img_info.get(ExifTool.Tag.DATE_TIME_ORIGINAL));
		insert(TITLE, img_info.get(ExifTool.Tag.TITLE));
		insert(SUBJECT, img_info.get(ExifTool.Tag.SUBJECT));
		insert(CONTRAST, img_info.get(ExifTool.Tag.CONTRAST));
	}
	
	private void insert(final byte[] tag, final String value) throws IOException {
		add(tag, toToken(value));
	}
	
	private byte[] toToken(String value){
		return (value != null) ? token(value) : Token.EMPTY;
	}
	
	private void add(byte[] tag, byte[] text) throws IOException {
		if (text == null) return;
		atts.add(NAME, tag);
		atts.add(TYPE, tag);
		builder.startElem(FACT, atts);
		builder.text(text);
		builder.endElem();
		atts.reset();
	}
	
}