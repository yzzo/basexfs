package org.basex.fs.fuse;

import java.io.IOException;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.server.LocalSession;

/**
 * Platform independent FUSE part (native part is provided by basexfs library).
 */
public class BaseXFS 
{
    static {
        System.loadLibrary("basexfs");
    }
    
    /** Reference context for current session. */
    private Context ctx;
	/** Reference (mounted/opened) FSML database on BaseXServer. */
	private LocalSession dbsession;
	
	/** Opens session to BaseX Server and loads FSML database. */
    public boolean openSession(String dbname) {
		try {
			ctx = new Context();
			dbsession = new LocalSession(ctx);
			dbsession.execute("open " + dbname);
			System.out.println(dbsession.info());
		} catch (BaseXException e) {
			System.out.println(e.getMessage());
			dbsession = null;
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			dbsession = null;
			return false;
		}
		return true;
    }
    
    public LocalSession getSession() {
    	return dbsession;
    }
    
    public boolean isMounted() {
    	return dbsession != null;
    }
    
    /* --------------------------------------------------------------------- */
    /* --------- Calls to native code (j2c) -------------------------------- */
    /* --------------------------------------------------------------------- */
    private native void    j2cInfo();
    private native boolean j2cMount(int argc, byte[][] argv); 

    /** Prints some info about native library. */ 
	public void info() {
		j2cInfo();
	}
	
	/** Reference thread running FUSE. */
	private Thread mountThread;
	
	/** Thread to launch native FUSE mount operation with arguments. */
	private class MountThread implements Runnable {
		private String[] args;
		
		public MountThread(String[] args) {
			this.args = args;
		}
		
		public void run() {
			int argc = this.args.length;
			byte[][] argv = new byte[argc][];
			
			for (int i = 0; i < argc; i++)
				argv[i] = this.args[i].getBytes();
			
			j2cMount(argc, argv);
		}
	}
	
	/** Mounts FUSE with user arguments and loads FSML database into Server. */
    public boolean mount(String database, String mountpoint) {
    	// XXX: open database
    	String[] args = { "basexfs", "-f", mountpoint}; // passed to FUSE
    	Runnable r = new MountThread(args);
    	mountThread = new Thread(r);
    	mountThread.start();
    	return false;
    }
    
    /* --------------------------------------------------------------------- */
    /* --------- Callbacks from native code (c2j) -------------------------- */
    /* --------------------------------------------------------------------- */
    private void c2jMkdir(byte[] path, byte[] mode) {
    	System.err.println("[BaseXFS.java:c2jMkdir] path: " + new String(path) + " mode: " + new String(mode));
    	try {
    		
    		dbsession.execute("xquery insert node <dir name='" + new String(path) + "' mode='"  + new String(mode) + "'/> into /fsml");
    		System.err.println("[c2jMkdir] " + dbsession.info());
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
}


