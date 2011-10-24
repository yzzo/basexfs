package org.basex.fs.fsml.build;

import static org.basex.util.Token.token;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.util.PDFTextStripper;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.io.IO;
import org.basex.util.Token;
import org.joda.time.DateTime;

/** Extract metadata and content from PDF document and produce XML. */
public class PDFParser extends Parser {

	private static final byte[] FACT = token("fact");
	private static final byte[] FOLDER = token("folder");
	private static final byte[] NAME = token("name");
	private static final byte[] PAGECOUNT = token("pagecount");
	private static final byte[] TITLE = token("title");
	private static final byte[] AUTHOR = token("author");
	private static final byte[] SUBJECT = token("subject");
	private static final byte[] KEYWORDS = token("keywords");
	private static final byte[] CREATOR = token("creator");
	private static final byte[] PRODUCER = token("producer");
	private static final byte[] CREATIONDATE = token("creationdate");
	private static final byte[] MODIFICATIONDATE = token("modificationdate");
	private static final byte[] META = token("meta");
	private static final byte[] CONTENT = token("content");
	private static final byte[] PAGE = token("page");
	private static final byte[] PAGES = token("pages");
	private static final byte[] TYPE = token("type");
	private static final byte[] TEXT = token("text");
	private static final byte[] ANNOTATIONS = token("annotation");
	private static final byte[] HYPERLINK = token("hyperlink");
	private static final byte[] FILE = token("file");
	private static final byte[] URL = token("url");

	private Builder builder;
	private PDDocument document;

	/**
	 * Constructor.
	 * @param path pathname to PDF file
	 */
	public PDFParser(String path) {
		super(path);
	}

	public PDFParser(IO path) {
		super(path);
	}

	@Override
	public void parse(Builder b){
		try {
			document = PDDocument.load(src.path());
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
			insertPDFMetadata();
			insertPages();
			document.close();
			builder.endElem();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void insertPDFMetadata() throws IOException{
		atts.reset();
		System.out.format("Extracting pdf metadata from %s.\n", src.path());
		PDDocumentInformation info = document.getDocumentInformation();
		insert(PAGECOUNT, document.getNumberOfPages());
		insert(TITLE, info.getTitle());
		insert(AUTHOR, info.getAuthor());
		insert(SUBJECT, info.getSubject());
		insert(KEYWORDS, info.getKeywords());
		insert(CREATOR, info.getCreator());
		insert(PRODUCER, info.getProducer());
		try {
			insert(CREATIONDATE, info.getCreationDate());
		} catch (IOException e) {
			insert(CREATIONDATE, "01.01.1970 00:00");
			System.err.println("Invalid creationdate");
		}
		try {
			insert(MODIFICATIONDATE, info.getModificationDate());
		} catch (IOException e) {
			insert(MODIFICATIONDATE, "01.01.1970 00:00");
			System.err.println("Invalid modificationdate");
		}
	}

	private void insertPages() throws IOException {
		List<?> pages = document.getDocumentCatalog().getAllPages();
		int numOfPages = pages.size();
		PDFTextStripper stripper = new PDFTextStripper("UTF-8");
		atts.add(NAME, PAGES);
		atts.add(TYPE, CONTENT);
		builder.startElem(FOLDER, atts);
		atts.reset();
		System.out.format("Extracting pdf text (%d pages) from: %s ", numOfPages, src.name());
		for (int i = 1; i <= numOfPages; i++) {
			System.out.print(".");
			stripper.setStartPage(i);
			stripper.setEndPage(i);
			String text = stripper.getText(document);
			atts.add(NAME, token(i));
			atts.add(TYPE, PAGE);
			builder.startElem(FOLDER, atts);
			atts.reset();
			insertAnnotations((PDPage) pages.get(i-1));
			atts.add(NAME, CONTENT);
			atts.add(TYPE, TEXT);
			builder.startElem(FACT, atts);
			builder.text(toToken(text));
			builder.endElem();
			builder.endElem();
			atts.reset();
		}
		System.out.println(" DONE.");
		builder.endElem();

	}

	private void insertAnnotations(PDPage page) throws IOException{
		List<?> annots = page.getAnnotations();
		atts.add(NAME, ANNOTATIONS);
		atts.add(TYPE, HYPERLINK);
		builder.startElem(FOLDER, atts);
		atts.reset();
		for( int j=0; j<annots.size(); j++ )
		{
			PDAnnotation annot = (PDAnnotation)annots.get( j );
			if( annot instanceof PDAnnotationLink )
			{
				PDAnnotationLink link = (PDAnnotationLink)annot;
				PDAction action = link.getAction();
				if( action instanceof PDActionURI )
				{
					PDActionURI uri = (PDActionURI)action;
					String url = uri.getURI();
					if(url.startsWith("file:"))
						insert(FILE, url);
					else
						insert(URL, url);
				}
			}
		}
		builder.endElem();
	}

private void insert(final byte[] tag, final Calendar date) throws IOException{
	DateTime datetime = new DateTime(date);
	add(tag, toToken(datetime.toString()));
}

private void insert(final byte[] tag, final int value) throws IOException{
	add(tag, token(value));
}

private void insert(final byte[] tag, final String value) throws IOException{
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