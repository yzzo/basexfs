package org.basex.fs.fsml.inject;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

/**
 * used to inject metadata into files
 *
 */
public class InjectionUtils {
	
	private String path;
	private String type = null;
	
	/**
	 * Constructor
	 * Checks automatically for valid filetype
	 * @param path = path of file to inject into
	 */
	public InjectionUtils(String path){
		this.setPath(path);
	}
	
	/**
	 * injects metadata into set file
	 * @param key name of the tag/field to set
	 * @param value value to be set
	 */
	public void inject(String key, String value){
		if(this.type == "music"){
			try {
				Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
				AudioFile audio = AudioFileIO.read(new File(path));
				Tag tag = audio.getTag();
				if(key == "artist")
					tag.setField(FieldKey.ARTIST, value);
				else if(key == "title")
					tag.setField(FieldKey.TITLE, value);
				else if(key == "album-artist")
					tag.setField(FieldKey.ALBUM_ARTIST, value);
				else if(key == "album")
					tag.setField(FieldKey.ALBUM, value);
				else if(key == "track-number")
					tag.setField(FieldKey.TRACK, value);
				else if(key == "comment")
					tag.setField(FieldKey.COMMENT, value);
				else if(key == "year")
					tag.setField(FieldKey.YEAR, value);
				else if(key == "disc-number")
					tag.setField(FieldKey.DISC_NO, value);
				else{
					System.out.println("Unknown key for filetype music");
					return;
				}
				audio.commit();
				System.out.println("injection done");
			} catch (CannotReadException e) {
				System.out.println("Detected empty or corrupt Music file '" 
						+ path + "': " + e.getMessage());
			} catch (TagException e) {
				System.out.println("Detected empty or corrupt Music file '" 
						+ path + "': " + e.getMessage());
			} catch (ReadOnlyFileException e) {
				System.out.println("Detected empty or corrupt Music file '" 
						+ path + "': " + e.getMessage());
			} catch (InvalidAudioFrameException e) {
				System.out.println("Detected empty or corrupt Music file '" 
						+ path + "': " + e.getMessage());
			} catch (IOException e) {
				System.out.println("Cannot open file");
				e.printStackTrace();
			} catch (CannotWriteException e) {
				System.out.println("Cannot write to file");
				e.printStackTrace();
			}
		}
	}
	
	private String checkType(String path){
		String sfx = path.substring(path.lastIndexOf("."),path.length());
		if(sfx.equals(".mp3")||sfx.equals(".ogg")||sfx.equals(".wma")||sfx.equals(".mp4")||sfx.equals(".flac")||sfx.equals(".m4a")||sfx.equals(".m4p"))
			return "music";
		return null;
	}

	/**
	 * sets InjectionUtils to a new filepath
	 * Automatically checks for valid filetype
	 * @param path path of file to inject into
	 */
	public void setPath(String path) {
		if((this.type = checkType(path)) != null)
			this.path = path;
		else
			System.out.println("Not a valid filetype to inject metadata");
	}
}
