// $Id: TestNativeFileSysApp.java,v 1.4 2005/01/22 01:36:36 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;

import java.io.*; 
import java.util.*; 
import java.util.logging.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   N A T I V E   F I L E   S Y S                                          */
/*------------------------------------------------------------------------------------------*/

public 
class TestNativeFileSysApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  public static void 
  main
  (
   String[] args  /* IN: command line arguments */
  )
  {
    
    Logs.sub.setLevel(Level.FINEST);

    try {
      TestNativeFileSysApp app = new TestNativeFileSysApp();
      app.run();
    } 
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    } 
 
    System.exit(0);
  }  


  public void 
  run() 
    throws IOException
  {
    File dir = new File(System.getProperty("user.dir") + "/data");

    File sometext    = new File("sometext");
    File somelink    = new File(dir, "sometext.link");
    File anotherlink = new File(dir, "anotherlink");

    /* create some symbolic links */ 
    {
      System.out.print("-----------------------------------\n");
      testSymlink(sometext, somelink);
      testSymlink(sometext, anotherlink);
      testSymlink(anotherlink, new File(dir, "link-to-link"));
    }

    /* resolve the realpath to some files */ 
    {
      {
	File rpath = testRealpath(somelink);
	assert(rpath.equals(new File(dir, "sometext")));
      }
      
      {
	File rpath = testRealpath(new File("data/link-to-link"));
	assert(rpath.equals(new File(dir, "sometext")));
      }
    }

    /* get the total and free disk space for the filesystem containing /usr/tmp */ 
    {
      System.out.print("-----------------------------------\n" + 
		       "Disk Usage (/usr/tmp):\n");

      long total = 0; 
      long free = 0;
      try {
	File tmp = new File("/usr/tmp");
	total = NativeFileSys.totalDiskSpace(tmp);
	free = NativeFileSys.freeDiskSpace(tmp);
      }
      catch(Exception ex) {
	System.out.print(ex.getMessage() + "\n");
      }
      
      System.out.print(" Total = " + total + " (bytes)\n" + 
		       "  Free = " + free + " (bytes)\n\n");
    }

    /* try some illegal calls to test the exception handling */ 
    {
      System.out.print("-----------------------------------\n" + 
		       "Test Exceptions: \n\n");
      try {
	testSymlink(sometext, somelink);
      }
      catch(Exception ex) {
	System.out.print(ex.getMessage() + "\n");
      }

      System.out.print("-----------------------------------\n" + 
		       "Test Exceptions: \n\n");
      try {
	NativeFileSys.realpath(new File("nonexistant"));
      }
      catch(Exception ex) {
	System.out.print(ex.getMessage() + "\n");
      }
    }
  } 

  public void 
  testSymlink
  ( 
   File file, 
   File link
  ) 
    throws IOException
  {
    System.out.print("Create Symlink: \n" + 
		     "  target file = " + file + "\n" +
		     "         link = " + link + "\n\n");

    NativeFileSys.symlink(file, link);
  }

  public File
  testRealpath
  ( 
   File path
  ) 
    throws IOException
  {
    System.out.print("Realpath: \n" + 
		     "      path = " + path + "\n");

    File rpath = NativeFileSys.realpath(path);

    System.out.print("  resolved = " + rpath + "\n\n");

    return rpath;
  }

}
