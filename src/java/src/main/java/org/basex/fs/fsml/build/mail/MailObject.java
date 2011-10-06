package org.basex.fs.fsml.build.mail;

public class MailObject {
	
	private String from;
	private String to;
	private String date;
	private String subject;
	private String content;
	private Attachment attachment = new Attachment();
	
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
	public void setAttachmentPath(String path) {
		this.attachment.path = path;
	}
	public void setAttachmentContent(String content) {
		this.attachment.content = content;
	}
	public String getAttachmentPath() {
		return attachment.path;
	}
	public String getAttachmentContent() {
		return attachment.content;
	}
	public boolean hasAttachment(){
		if(attachment.getPath()!=null)
			return true;
		return false;
	}

	class Attachment{
		private String path;
		private String content;
		
		public void setPath(String path) {
			this.path = path;
		}
		public String getPath() {
			return path;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public String getContent() {
			return content;
		}
	}
}