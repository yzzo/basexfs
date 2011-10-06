package org.basex.fs.fsml.build;

import static org.basex.util.Token.token;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.core.MainProp;
import org.basex.fs.util.Utils;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.util.Atts;
import org.basex.util.Token;
import org.basex.util.list.TokenList;

public class FileHierarchyParser extends Parser {

	private static final byte[] DEEPFS = token("deepfs");
	private static final byte[] FSML = token("fsml");
	private static final byte[] BACKING = token("backing_store");
	private static final byte[] DIR = token("dir");
	private static final byte[] FILE = token("file");
	private static final byte[] NAME = token("name");
	private static final byte[] SUFFIX = token("suffix");
	public static final byte[] ST_SIZE = token("st_size");
	private static final byte[] ST_MTIME = token("st_mtime");
	private static final byte[] ST_ATIME = token("st_atime");
	private static final byte[] ST_CTIME = token("st_ctime");
	private static final byte[] ST_NLINK = token("st_nlink");
	private static final byte[] ST_UID = token("st_uid");
	private static final byte[] ST_GID = token("st_gid");
	private static final byte[] ST_MODE = token("st_mode");
	private static final byte[] BSID = token("bsid"); // backing store id
	private static final byte[] PDF = token("pdf");
	private static TokenList MUSIC = new TokenList();
	private static TokenList MAILS = new TokenList();
	private static TokenList IMGS = new TokenList();
	
	private Builder builder;
	private IOFile backingStore;
	private String dbname;
	
	public FileHierarchyParser(final String db, final String path) throws IOException {
		super(path);
		dbname = db;
	}

	@Override
	public void parse(Builder b) throws IOException {
		FillLists();
		builder = b;
		
		if (!createBackingStore())
			throw new IOException("Can not create backing store for db '" 
					+ dbname + "' in '" + backingStore.path() + "'");
		
		builder.startDoc(DEEPFS);
		atts.add(BACKING, token(backingStore.path()));
		atts.add(ST_MODE, token("040755"));
		attsStat(src);
		builder.startElem(FSML, atts);
		
		if (src.isDir()) parseDirectory(src);
		else parseFile(src);
			
		builder.endElem();
		builder.endDoc();
	}
	
	private boolean createBackingStore() {
		IOFile dbpath = new MainProp().dbpath();
		backingStore = dbpath.merge(dbname).merge("bin");
		return backingStore.md();
	}
	
	private void parseDirectory(IO io) throws IOException {
		attsDir(io);
		builder.startElem(DIR, atts);
		for(final IO f : ((IOFile) io).children())
			if (f.isDir()) parseDirectory(f);
			else parseFile(f);
		builder.endElem();
	}
	
	private void parseFile(IO f) throws IOException {
		byte[] sfx = attsFile(f);
		byte[] bsid = token(UUID.randomUUID().toString());
		atts.add(BSID, bsid);
		builder.startElem(FILE, atts);
		// get file-specific parser based on suffix
		Parser parser = getFileParser(sfx, f);
		if (parser != null) 
			parser.parse(builder);
		builder.endElem();
		// copy to backing store	
		copyIntoBackingStore(f, bsid);
	}
	
	/**
	 * Fill global temporary attribute array with file attributes.
	 * @return suffix of file
	 */
	private byte[] attsFile(IO f) {
		atts.reset();
		String name = f.name();
		byte[] sfx = token(name.substring(name.lastIndexOf('.') + 1).toLowerCase());
		atts.add(NAME, token(name));
		atts.add(SUFFIX, sfx);
		atts.add(ST_MODE, token("0100644"));
		attsStat(f);
		return sfx;
	}
	
	private Atts attsDir(IO dir) {
		atts.reset();
		String name = dir.name();
		atts.add(NAME, token(name));
		atts.add(ST_MODE, token("040755"));
		attsStat(dir);
		return atts;
	}
	
	private void attsStat(IO f) {
		atts.add(ST_SIZE, token(f.length()));
		atts.add(ST_MTIME, token(0));	
		atts.add(ST_ATIME, token(0));
		atts.add(ST_CTIME, token(0));
		atts.add(ST_UID, token(1000)); // XXX: uid/gid: 1000 := holu on test system
		atts.add(ST_GID, token(1000));
		atts.add(ST_NLINK, token(1));
	}
	
	private Parser getFileParser(byte[] suffix, IO f) {
		if (Token.eq(suffix, PDF)) 
			return new PDFParser(f);
		else if (MAILS.contains(suffix))
			return new EMailParser(f);
		else if (IMGS.contains(suffix))
			return new IMGParser(f);
		else if (MUSIC.contains(suffix))
			return new MusicParser(f);
//		System.err.println("No parser for suffix : " + new String(suffix));
		return null;
	}
	
	private void copyIntoBackingStore(final IO file, final byte[] name) {
		File target = new File(backingStore.merge(Token.string(name)).path());
		System.out.format("Writing pdf binary into backing store: %s\n", new String(name));
		Utils.copyFile(new File(file.path()), target);
	}
	
	private void FillLists(){
		MUSIC.add(token("mp3"));
		MUSIC.add(token("ogg"));
		MUSIC.add(token("wma"));
		MUSIC.add(token("mp4"));
		MUSIC.add(token("flac"));
		MUSIC.add(token("m4a"));
		MUSIC.add(token("m4p"));
		MAILS.add(token("mbox"));
		MAILS.add(token("mbs"));
		MAILS.add(token("emlx"));
		IMGS.add(token("jpg"));
		IMGS.add(token("jpeg"));
		IMGS.add(token("bmp"));
		IMGS.add(token("gif"));
		IMGS.add(token("tif"));
		IMGS.add(token("tiff"));
		IMGS.add(token("png"));
	}
}
