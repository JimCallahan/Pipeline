// $Id: Native.java,v 1.6 2005/01/22 06:10:09 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   N A T I V E                                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Loads the shared library containing JNI based methods used by subclasses.
 * 
 * @see NativeProcesss
 * @see NativeSystem
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
      
      String lib = "libNative.so";
      if(PackageInfo.sNativeSubdir != null) 
	lib = (PackageInfo.sNativeSubdir + "/libNative.so");
      String path = (PackageInfo.sInstDir + "/lib/" + lib);

      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Fine,
	 "Loading Native Library: " + path);
      System.load(path);
            
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
