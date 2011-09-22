package org.basex.fs;

/**
 * Platform independent FUSE part (native part is provided by basexfs library).
 *
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
    private native boolean j2cMount(String[] argv); 

	void info() {
		j2cInfo();
	}
	
	private Thread mountThread;
	
	private class MountThread implements Runnable {
		private String[] args;
		
		public MountThread(String[] args) {
			this.args = args;
		}
		
		public void run() {
			j2cMount(this.args);
		}
	}
	
    boolean mount(String[] args) {
    	Runnable r = new MountThread(args);
    	mountThread = new Thread(r);
    	mountThread.start();
    	return false;
    }
    
    /* --------------------------------------------------------------------- */
    /* --------- Callbacks from native code (c2j) -------------------------- */
    /* --------------------------------------------------------------------- */
    private void c2jDummyCallback() {
    	System.err.println("[BaseXFS.java] I was called by libbasexfs.");
    }
}


