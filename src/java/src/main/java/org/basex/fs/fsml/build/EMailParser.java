package org.basex.fs.fsml.build;

import static org.basex.util.Token.token;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.fs.fsml.build.mail.MailObject;
import org.basex.fs.fsml.build.mail.MailParser;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.util.Base64;
import org.basex.util.Token;
import org.basex.util.list.TokenList;

/** Extract metadata and content from Mailboxes and produce XML. */
public class EMailParser extends Parser {

	private static final byte[] FACT = token("fact");
	private static final byte[] FOLDER = token("folder");
	private static final byte[] NAME = token("name");
	private static final byte[] FROM = token("from");
	private static final byte[] SUBJECT = token("subject");
	private static final byte[] TO = token("to");
	private static final byte[] DATE = token("date");
	private static final byte[] META = token("meta");
	private static final byte[] CONTENT = token("content");
	private static final byte[] TYPE = token("type");
	private static final byte[] MAILS = token("mails");
	private static final byte[] MAIL = token("mail");
	private static final byte[] PDF = token("pdf");
	private static final byte[] ATTACH = token("attachment");
	private static TokenList MUSIC = new TokenList();
	private static TokenList IMGS = new TokenList();
	
	private Builder builder;
	private MailObject document;
	private MailParser mp;
	
	/**
	 * Constructor.
	 * @param path pathname to Mailbox file
	 */
	public EMailParser(String path) {
		this(IO.get(path));
	}
	
	public EMailParser(IO path) {
		super(path);
	}

	@Override
	public void parse(Builder b) throws IOException {
		mp = new MailParser (src.path());
		try {
			builder = b;
			atts.add(NAME, token("." + src.name() + ".deepfs"));
			atts.add(TYPE, META);
			builder.startElem(FOLDER, atts);
			if (src.name().endsWith(".emlx"))
				insertMailData();
			else insertBoxData();
			builder.endElem();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void insertBoxData() throws IOException {
		atts.reset();
		atts.add(NAME, MAILS);
		atts.add(TYPE, CONTENT);
		builder.startElem(FOLDER, atts);
		atts.reset();
		System.out.format("Extracting mailbox metadata from %s.\n", src.path());
		for(int count = 1; (document = mp.getNext()) != null; count++) {
			System.out.print(".");
			atts.add(NAME, token(count));
			atts.add(TYPE, MAIL);
			builder.startElem(FOLDER, atts);
			atts.reset();
			insert(DATE, document.getDate());
			insert(FROM, document.getFrom());
			insert(TO, document.getTo());
			insert(SUBJECT, document.getSubject());
			insert(CONTENT, document.getContent());
			addAttachement();
			builder.endElem();
			atts.reset();
		}
		builder.endElem();
	}
	
	private void insertMailData() throws IOException {
		atts.reset();
		atts.add(NAME, MAIL);
		atts.add(TYPE, CONTENT);
		builder.startElem(FOLDER, atts);
		atts.reset();
		System.out.format("Extracting mailbox metadata from %s.\n", src.path());
		for(int count = 1; (document = mp.getNext()) != null; count++) {
			insert(DATE, document.getDate());
			insert(FROM, document.getFrom());
			insert(TO, document.getTo());
			insert(SUBJECT, document.getSubject());
			insert(CONTENT, document.getContent());
		}
		builder.endElem();
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
	
	private void addAttachement() throws IOException{
		if(document.hasAttachment()){
			OutputStream out = new FileOutputStream(document.getAttachmentPath());
			byte cont[] = document.getAttachmentContent().getBytes();
			out.write(Base64.decode(cont));
			out.close();
			builder.startElem(ATTACH, atts);
			Parser parser = getFileParser(document.getAttachmentPath());
			if(parser != null)
				parser.parse(builder);
			new File(document.getAttachmentPath()).delete();
			builder.endElem();

		}
	}
	
	private Parser getFileParser(String path) {
		FillLists();
		byte[] suffix = token(path.substring(path.lastIndexOf(".")+1));
		IOFile f = new IOFile(path);
		if (Token.eq(suffix, PDF)) 
			return new PDFParser(f);
		else if (IMGS.contains(suffix))
			return new IMGParser(f);
		else if (MUSIC.contains(suffix))
			return new MusicParser(f);
		System.err.println("No parser for suffix : " + new String(suffix));
		return null;
	}
	
	private void FillLists(){
		MUSIC.add(token("mp3"));
		MUSIC.add(token("ogg"));
		MUSIC.add(token("wma"));
		MUSIC.add(token("mp4"));
		MUSIC.add(token("flac"));
		MUSIC.add(token("m4a"));
		MUSIC.add(token("m4p"));
		IMGS.add(token("jpg"));
		IMGS.add(token("jpeg"));
		IMGS.add(token("bmp"));
		IMGS.add(token("gif"));
		IMGS.add(token("tif"));
		IMGS.add(token("tiff"));
		IMGS.add(token("png"));
	}
}