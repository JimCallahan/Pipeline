// $Id: Native.java,v 1.9 2006/12/05 17:45:34 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   N A T I V E                                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Loads the shared library containing JNI based methods used by subclasses.
 */
class Native
{  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N I T I A L I Z A T I O N                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Load the native library.
   */ 
  protected static void
  loadLibrary()
  {
    synchronized(sLibraryLoadLock) {
      if(sIsLibraryLoaded) 
	return;

      String ext = null;
      switch(PackageInfo.sOsType) {
      case Unix:
	ext = ".so";
	break;

      case MacOS:
	ext = ".jnilib";
	break;

      case Windows:
	ext = ".dll";
      }
      
      Path path = new Path(PackageInfo.sInstPath, 
			   "/lib/" + PackageInfo.sOsType + "/libNative" + ext);
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Fine,
	 "Loading Native Library: " + path.toOsString());
      System.load(path.toOsString());
            
      sIsLibraryLoaded = true;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   F I E L D S                                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * A lock which serializes access to the library loading code.
   */ 
  private static Object  sLibraryLoadLock = new Object();

  /**
   * Whether the native library has already been loaded.
   */ 
  private static boolean  sIsLibraryLoaded = false;

}
