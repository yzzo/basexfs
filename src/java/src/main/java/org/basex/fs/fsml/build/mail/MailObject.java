package org.basex.fs.fsml.build.mail;

public class MailObject {
	
	private String from;
	private String to;
	private String date;
	private String subject;
	private String content;
	
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
}