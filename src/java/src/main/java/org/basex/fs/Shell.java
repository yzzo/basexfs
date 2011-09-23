package org.basex.fs;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.basex.build.Parser;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.CreateDB;
import org.basex.fs.fsml.build.FileHierarchyParser;

/**
 * Rudimentary shell to interact with a file hierarchy stored in XML.
 *
 * @author BaseX Team 2011, BSD License
 * @author Alexander Holupirek <alex@holupirek.de>
 */
public class Shell {

	/** Shell command description. */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Command {
		/** Description of expected arguments. */
		String args() default "";
		/** Shortcut key for command. */
		char shortcut();
		/** Short help message. */
		String help();
	}
	  
	/** Reference FUSE request handlers. */
	private BaseXFS fs;
	
	/** Shell prompt. */
	private static final String PS1 = "[basexfs] $ ";
	  
	/** Constructor. */
	Shell() {
		fs = new BaseXFS();		
	}
	
	/**
	 * Returns the next user input.
	 * 
	 * @param prompt
	 *            prompt string
	 * @return user input
	 */
	private String input(final String prompt) {
		System.out.print(prompt);
		return new Scanner(System.in).nextLine();
	}
	

	/**
	 * Tokenizes argument line.
	 * 
	 * @param line
	 *            string to split in tokens
	 * @return argument vector
	 */
	private String[] tokenize(final String line) {
		final StringTokenizer st = new StringTokenizer(line);
		final String[] toks = new String[st.countTokens()];
		int i = 0;
		while (st.hasMoreTokens()) {
			toks[i++] = st.nextToken();
		}
		return toks;
	}

	/**
	 * Command line-arguments if any.
	 * 
	 * @param args
	 *            user arguments
	 */
	private void exec(final String[] args) {
	    try {
	      final Method[] ms = getClass().getMethods();
	      for(final Method m : ms) {
	        if(m.isAnnotationPresent(Command.class)) {
	          final Command c = m.getAnnotation(Command.class);
	          if(args[0].equals(m.getName())
	              || args[0].length() == 1 
	              && args[0].charAt(0) == c.shortcut()) {
	            m.invoke(this, (Object) args);
	            return;
	          }
	        }
	      }
	      System.err.printf(
	          "%s: commmand not found. Type 'help' for available commands.\n",
	          args[0] == null ? "" : args[0]);
	    } catch(final Exception ex) {
	    	ex.printStackTrace();
	    }
	}

	/** Rudimentary shell. */
	private void loop() {
		fs.info();
		do {
			final String[] args = tokenize(input(PS1));
			if (args.length != 0)
				exec(args);
		} while (true);
	}
	

	/**
	 * Mount database as filesystem in userspace.
	 * 
	 * @param args argument vector
	 */
	@Command(shortcut = 'm', args = "<FSML database> <mountpoint>", help = "mount fsml db on path")
	public void mount(final String[] args) {
		if (args.length != 3) {
			help(new String[] { "help", "mount" });
			return;
		}
		fs.mount(args[1], args[2]);
		System.err.println("Mount command submitted.");
	}
	
	/**
	 * Create Filesystem Markup Language (FSML) document from existing file hierarchy.
	 * 
	 * @param args argument vector
	 */
	@Command(shortcut = 'c', args = "<dbname> <path>"
			, help = "Create a new FSML database from data stored beneath <path>")
	public void mkfs(final String[] args) {
		if (args.length != 3) {
			help(new String[] { "help", "mkfs" });
			return;
		}
		final String dbname = args[1];
		String path = args[2];
		// System.out.println("Usage: mkfs.deepfs [-h] [-d dbname] [-b db|fs|git|svn] <path>");
		// System.out.println("Create a DeepFS database filesystem from data stored beneath <path>");
		// System.out.println("-d    database name for FSML data (default: 'fsml')");
		// System.out.println("-b    backing store               (default: 'db')");
		if (path.equals("test"))
			path = "./java/harness";
		
    	System.out.println("DeepFS database filesystem using Filesystem Markup Language 1.0");
    	System.out.println("---------------------------------------------------------------");

		try {
			Context ctx = new Context();
			Prop prop = new Prop(true);
			Parser parser = new FileHierarchyParser(dbname, path, prop);
			String info = CreateDB.create(dbname, parser, ctx);
			System.out.println("Info: " + info);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BaseXException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Prints short help message for available commands.
	 * 
	 * @param args argument vector
	 */
	@Command(shortcut = 'h', help = "print this message")
	public void help(final String[] args) {
		final Method[] ms = getClass().getMethods();
		for (final Method m : ms)
			if (m.isAnnotationPresent(Command.class)) {
				final Command c = m.getAnnotation(Command.class);
				if (args.length == 1 && args[0].charAt(0) == 'h'
						|| args.length > 1 && m.getName().equals(args[1])
						|| args.length > 1 && args[1].length() == 1
						&& c.shortcut() == args[1].charAt(0))
					System.out.printf("%-40s %-40s%n",
							m.getName() + " " + c.args(),
							c.help() + " (" + c.shortcut() + ")");
			}
	}
	  
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Shell().loop();
	}

}
