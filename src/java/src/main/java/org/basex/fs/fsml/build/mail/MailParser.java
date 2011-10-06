package org.basex.fs.fsml.build.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MailParser {
	
	private static BufferedReader br;
	private String type;
	
	public MailParser (String path) {
		if (path.endsWith(".mbox")) {
			type = "From -";
		}
		else if (path.endsWith(".mbs")) {
			type = "From ";
		}
		else if (path.endsWith(".emlx")) {
			type = path.substring(path.lastIndexOf("/")+1, path.length()-5);
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
	
	public MailObject getNext () {
		String line;
		MailObject mail = new MailObject();
		boolean isNext = false;
		try {
			while ((line = br.readLine()) != null) {
				isNext = true;
				if (line.startsWith("From:"))
					mail.setFrom(line.substring(6));
				if (line.startsWith("To:"))
					mail.setTo(line.substring(4));
				if (line.startsWith("Subject:"))
					mail.setSubject(line.substring(9));
				if (line.startsWith("Date:"))
					mail.setDate(line.substring(6));
				if (line.isEmpty()) {
					StringBuffer content = new StringBuffer();
					while ((line = br.readLine()) != null) {
						if (line.startsWith(type)){
							break;
						}
						else if (line.startsWith("Content-Transfer-Encoding: base64")){
								line = br.readLine();
								if (line.startsWith("Content-Disposition: attachment;"))
									insertAttachment(mail, line);
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
	
	private void insertAttachment(MailObject mail, String line) throws IOException{
			if(!line.contains("\""))
				line = br.readLine();
			line = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""));
			mail.setAttachmentPath(line);
			br.readLine();
			StringBuffer content = new StringBuffer();
			while ((line = br.readLine()) != null) {
				if (line.isEmpty())
					break;
				content.append(line);
			}
			mail.setAttachmentContent(content.toString());
			System.out.println("\n\ngot it\n\n");
	}
}
