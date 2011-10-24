package org.basex.fs.fsml.build.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Parses mails of type mbox, mbs and emlx
 * @author yzzo
 *
 */
public class MailParser {

	private static BufferedReader br;
	private String start;
	private String path = "";

	/**
	 * Constructor
	 * @param path to file
	 */
	public MailParser (String path) {
		if (path.endsWith(".mbox")) {
			start = "From -";
		}
		else if (path.endsWith(".mbs")) {
			start = "From ";
		}
		else if (path.endsWith(".emlx")) {
			this.path = path;
		}
		else System.out.println("cannot read: unknown file");
		try {
			File mbox = new File(path);
			br = new BufferedReader(new FileReader(mbox));
		}
		catch (Exception e) {
			System.out.println("File corrupted");		
		}
	}

	/**
	 * Get the next mail of a mailbox
	 * @return MailObject
	 */
	public MailObject getNext () {
		String line;
		MailObject mail = new MailObject();
		boolean isNext = false;
		try {
			while ((line = br.readLine()) != null) {
				isNext = true;
				if (line.startsWith("From:"))
					mail.setFrom(line.substring(6));
				else if (line.startsWith("To:"))
					mail.setTo(line.substring(4));
				else if (line.startsWith("Subject:"))
					mail.setSubject(line.substring(9));
				else if (line.startsWith("Date:"))
					mail.setDate(line.substring(6));
				else if (line.isEmpty()) {
					StringBuffer content = new StringBuffer();
					while ((line = br.readLine()) != null) {
						if (line.startsWith(start)){
							break;
						}
						else if (line.startsWith("Content-Transfer-Encoding: base64")){
							line = br.readLine();
							if (line.startsWith("Content-Disposition: attachment;"))
								insertAttachment(mail);
						}
						line = line + "\n";
						content.append(line);
					}
					mail.setContent(content.toString());
					break;
				}
			}
		} catch (IOException e) {
			System.out.println("Cannot Read File");
		}
		if(isNext)
			return mail;
		return null;
	}
	
	/**
	 * Get the Mail of an emlx-file
	 * @return
	 */
	public MailObject getMail () {
		String line;
		String boundary = null;
		MailObject mail = new MailObject();
		boolean isFirstBoundary = true;
		boolean checkedAttachments = false;
		try {
			while ((line = br.readLine()) != null) {
				if (line.startsWith("From:"))
					mail.setFrom(line.substring(6));
				else if (line.startsWith("To:"))
					mail.setTo(line.substring(4));
				else if (line.startsWith("Subject:"))
					mail.setSubject(line.substring(9));
				else if (line.startsWith("Date:"))
					mail.setDate(line.substring(6));
				else if (line.startsWith("Content-Type: multipart/mixed;"))
					boundary = line.substring(line.indexOf("=")+1).replace("\"", "");
				else if (line.startsWith("Content-Transfer-Encoding: base64"))
					mail.setBase64Content(true);
				else if (line.isEmpty()&&isFirstBoundary) {
					StringBuffer content = new StringBuffer();
					while ((line = br.readLine()) != null) {
						if(boundary != null){
						if (line.contains(boundary)&&isFirstBoundary){
							content = new StringBuffer();
							isFirstBoundary = false;
						}
						else if (line.contains(boundary))
							break;
						}
						line = line + "\n";
						content.append(line);
					}
					mail.setContent(content.toString());
				}
				else if (line.startsWith("Content-Disposition: attachment;")&&!checkedAttachments){
						checkAttachment(mail);
						checkedAttachments = true;
				}
			}
		} catch (IOException e) {
			System.out.println("Cannot Read File");
		}
		return mail;
	}

	/**
	 * inserts internal attachments of mailboxes
	 * @param mail MailObject of the mailbox
	 * @throws IOException
	 */
	private void insertAttachment(MailObject mail) throws IOException{
		String line = "";
		String filename = "";
		while(!(line = br.readLine()).contains("filename"));
		filename = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""));
		while((line = br.readLine()).contains("filename"))
			filename += line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""));
		StringBuffer content = new StringBuffer();
		while ((line = br.readLine()) != null) {
			if (line.isEmpty()||line.startsWith("-"))
				break;
			content.append(line);
		}
		mail.addAttachment(filename, content.toString());
	}
	
	/**
	 * inserts external attachments of emlx-files
	 * @param mail
	 */
	private void checkAttachment(MailObject mail){
		String attPath = new File(path).getParentFile().getParent() + "\\Attachments\\" + new File(path).getName().replaceAll(".emlx", "");
		File[] files = new File(attPath).listFiles();
		File[] attachments;
		if(files!=null){
		for(int i = 0; i<files.length; i++)
			if(files[i].isDirectory()){
				attachments = files[i].listFiles();
				for(int n = 0; n<attachments.length; n++)
					if(attachments[n].isFile())
						mail.addAttachmentsPath(attachments[n].getAbsolutePath());
			}
		}
	}
}
