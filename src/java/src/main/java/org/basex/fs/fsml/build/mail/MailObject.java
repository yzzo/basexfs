package org.basex.fs.fsml.build.mail;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents an e-mail
 * @author yzzo
 */
public class MailObject {
	
	private String from;
	private String to;
	private String date;
	private String subject;
	private String content;
	private boolean base64Content;
	private List<Attachment> attachments = new ArrayList<Attachment>();
	private List<String> attachmentsPath = new ArrayList<String>();
	
	public void setFrom(String from) {
		this.from = from;
	}
	public String getFrom() {
		return from;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getTo() {
		return to;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getDate() {
		return date;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getSubject() {
		return subject;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getContent() {
		return content;
	}
	/**
	 * Adds a mail-internal attachment
	 * @param path
	 * @param content
	 */
	public void addAttachment(String path, String content){
		this.attachments.add(new Attachment(path, content));
	}
	/**
	 * Returns the original filename of an internal attachment
	 * @param id
	 * @return String filename
	 */
	public String getAttachmentPath(int id) {
		return this.attachments.get(id).path;
	}
	/**
	 * Returns the content of an internal attachment
	 * @param id
	 * @return String content
	 */
	public String getAttachmentContent(int id) {
		return this.attachments.get(id).content;
	}
	/**
	 * Returns the number of internal attachments for this mail
	 * @return int number of attachments
	 */
	public int countAttachments(){
		return this.attachments.size();
	}
	public void setBase64Content(boolean base64Content) {
		this.base64Content = base64Content;
	}
	public boolean isBase64Content() {
		return base64Content;
	}
	/**
	 * Adds an external attachment
	 * @param attachmentsPath
	 */
	public void addAttachmentsPath(String attachmentsPath) {
		this.attachmentsPath.add(attachmentsPath);
	}
	/**
	 * Returns the filepath of an external attachment
	 * @param id
	 * @return String filepath
	 */
	public String getAttachmentsPath(int id) {
		return attachmentsPath.get(id);
	}
	/**
	 * Returns the number of external attachments of this mail
	 * @return int number of attachments
	 */
	public int countAttachmentsPath(){
		return attachmentsPath.size();
	}

	class Attachment{
		private String path;
		private String content;
		
		public Attachment(String path, String content){
			this.path = path;
			this.content = content;
		}
	}
}