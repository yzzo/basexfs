package org.basex.fs.fsml.build;

import static org.basex.util.Token.token;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.io.IO;
import org.basex.util.Token;
import org.jaudiotagger.audio.*;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

/** Extract metadata and content from an image and produce XML. */
public class MusicParser extends Parser {

	private static final byte[] FACT = token("fact");
	private static final byte[] FOLDER = token("folder");
	private static final byte[] NAME = token("name");
	private static final byte[] TITLE = token("title");
	private static final byte[] ARTIST = token("artist");
	private static final byte[] ALBUMARTIST = token("album-artist");
	private static final byte[] ALBUM = token("album");
	private static final byte[] TRACKNR = token("track-number");
	private static final byte[] META = token("meta");
	private static final byte[] TYPE = token("type");
	private static final byte[] COMMENT = token("comment");
	private static final byte[] FORMAT = token("format");
	private static final byte[] ENCODINGTYPE = token("encoding-type");
	private static final byte[] BITRATE = token("bit-rate");
	private static final byte[] SAMPLERATE = token("sample-rate");
	private static final byte[] LENGTH = token("track-length");
	private static final byte[] YEAR = token("year");
	private static final byte[] DISCNR = token("disc-number");
	
	private Builder builder;
	AudioFile audio;
	
	/**
	 * Constructor.
	 * @param path pathname to image file
	 */
	public MusicParser(String path) {
		this(IO.get(path));
	}
	
	public MusicParser(IO path) {
		super(path);
	}

	@Override
	public void parse(Builder b) throws IOException {
		
		Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
		
		try {
			audio = AudioFileIO.read(new File(src.path()));
		} catch (CannotReadException e) {
			System.out.println("Detected empty or corrupt Music file '" 
					+ src.path() + "': " + e.getMessage());
		} catch (TagException e) {
			System.out.println("Detected empty or corrupt Music file '" 
					+ src.path() + "': " + e.getMessage());
		} catch (ReadOnlyFileException e) {
			System.out.println("Detected empty or corrupt Music file '" 
					+ src.path() + "': " + e.getMessage());
		} catch (InvalidAudioFrameException e) {
			System.out.println("Detected empty or corrupt Music file '" 
					+ src.path() + "': " + e.getMessage());
		}
		try {
			builder = b;
			atts.add(NAME, token("." + src.name() + ".deepfs"));
			atts.add(TYPE, META);
			builder.startElem(FOLDER, atts);
			insertMusicMetadata();
			builder.endElem();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void insertMusicMetadata() throws IOException {
		atts.reset();
		System.out.format("Extracting music metadata from %s.\n", src.path());
		Tag tags = audio.getTag();
		AudioHeader header = audio.getAudioHeader();
		insert(FORMAT, header.getFormat());
		insert(ENCODINGTYPE, header.getEncodingType());
		insert(BITRATE, header.getBitRate());
		insert(SAMPLERATE, header.getSampleRate());
		insert(LENGTH, header.getTrackLength());
		insert(ALBUM, tags.getFirst(FieldKey.ALBUM));
		insert(ALBUMARTIST, tags.getFirst(FieldKey.ALBUM_ARTIST));
		insert(ARTIST, tags.getFirst(FieldKey.ARTIST));
		insert(TITLE, tags.getFirst(FieldKey.TITLE));
		insert(TRACKNR, tags.getFirst(FieldKey.TRACK));
		insert(DISCNR, tags.getFirst(FieldKey.DISC_NO));
		insert(YEAR, tags.getFirst(FieldKey.YEAR));
		insert(COMMENT, tags.getFirst(FieldKey.COMMENT));
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
		builder.endElem();
		atts.reset();
	}
	
}