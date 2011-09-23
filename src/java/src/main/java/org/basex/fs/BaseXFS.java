package org.basex.fs;

/**
 * Platform independent FUSE part (native part is provided by basexfs library).
 */
public class BaseXFS 
{
    static {
        System.loadLibrary("basexfs");
    }

    /* --------------------------------------------------------------------- */
    /* --------- Calls to native code (j2c) -------------------------------- */
    /* --------------------------------------------------------------------- */
    private native void    j2cInfo();
    private native boolean j2cMount(int argc, byte[][] argv); 

    /** Prints some info about native library. */ 
	void info() {
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
    boolean mount(String database, String mountpoint) {
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
    }
}


