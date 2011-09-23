package org.basex.fs.fsml.build;

import static org.basex.util.Token.token;

import java.io.IOException;
import java.util.Calendar;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
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
	
	private Builder builder;
	private PDDocument document;
	
	/**
	 * Constructor.
	 * @param path pathname to PDF file
	 */
	public PDFParser(String path) {
		this(IO.get(path));
	}
	
	public PDFParser(IO path) {
		super(path, "");
	}

	@Override
	public void parse(Builder b) throws IOException {
		try {
			document = PDDocument.load(file.path());
		} catch (IOException e) {
			System.out.println("Detected empty or corrupt PDF file'" 
					+ file.path() + "': " + e.getMessage());
			return;
		}
		try {
			builder = b;
			atts.add(NAME, token("." + file.name() + ".deepfs"));
			atts.add(TYPE, META);
			builder.startElem(FOLDER, atts);
			insertPDFMetadata();
			insertPages();
			document.close();
			builder.endElem(FOLDER);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void insertPDFMetadata() throws IOException {
		atts.reset();
		System.out.format("Extracting pdf metadata from %s.\n", file.path());
		PDDocumentInformation info = document.getDocumentInformation();
		insert(PAGECOUNT, document.getNumberOfPages());
		insert(TITLE, info.getTitle());
		insert(AUTHOR, info.getAuthor());
		insert(SUBJECT, info.getSubject());
		insert(KEYWORDS, info.getKeywords());
		insert(CREATOR, info.getCreator());
		insert(PRODUCER, info.getProducer());
		insert(CREATIONDATE, info.getCreationDate());
		insert(MODIFICATIONDATE, info.getModificationDate());
	}
	
	private void insertPages() throws IOException {
		PDFTextStripper stripper = new PDFTextStripper("UTF-8");
		int numberOfPages = document.getNumberOfPages();
		
		atts.add(NAME, PAGES);
		atts.add(TYPE, CONTENT);
		builder.startElem(FOLDER, atts);
		atts.reset();
		System.out.format("Extracting pdf text (%d pages) from: %s ", numberOfPages, file.name());
		for (int i = 1; i <= numberOfPages; i++) {
			System.out.print(".");
			stripper.setStartPage(i);
			stripper.setEndPage(i);
			String text = stripper.getText(document);
			atts.add(NAME, token(i));
			atts.add(TYPE, PAGE);
			builder.startElem(FACT, atts);
			builder.text(toToken(text));
			builder.endElem(FACT);
			atts.reset();
		}
		System.out.println(" DONE.");
		builder.endElem(FOLDER);
	}

	private void insert(final byte[] tag, final Calendar date) throws IOException {
		DateTime datetime = new DateTime(date);
		add(tag, toToken(datetime.toString()));
	}
	
	private void insert(final byte[] tag, final int value) throws IOException {
		add(tag, token(value));
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
		builder.endElem(FACT);
		atts.reset();
	}
	
}