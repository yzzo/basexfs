package org.basex.fs.fsml.build;

import static org.basex.util.Token.token;

import java.io.IOException;

import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.fs.fsml.build.mail.MailObject;
import org.basex.fs.fsml.build.mail.MailParser;
import org.basex.io.IO;
import org.basex.util.Token;

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
}