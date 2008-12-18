// $Id: TestNativeFileSysApp.java,v 1.7 2008/12/18 00:46:25 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;

import java.io.*; 
import java.util.*; 

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
    LogMgr.getInstance().setLevel(LogMgr.Kind.Sub, LogMgr.Level.Finest);

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
    File moretext    = new File(dir, "moretext");
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

    /* try out NativeFileStat for the files */ 
    {
      NativeFileStat crapstat    = testStat(new File(dir, "crap")); 
      NativeFileStat textstat    = testStat(new File(dir, "sometext")); 
      NativeFileStat morestat    = testStat(moretext); 
      NativeFileStat linkstat    = testStat(somelink);
      NativeFileStat anotherstat = testStat(anotherlink);
      NativeFileStat dirstat     = testStat(dir);

      System.out.print
        ("-----------------------------------\n" + 
         "I-Node Alias Checks:\n\n" + 
         "textstat vs. morestat    = " + textstat.isAlias(morestat) + "\n" + 
         "textstat vs. linkstat    = " + textstat.isAlias(linkstat) + "\n" + 
         "linkstat vs. anotherstat = " + linkstat.isAlias(anotherstat) + "\n" + 
         "anotherstat vs. linkstat = " + anotherstat.isAlias(linkstat) + "\n" +
         "textstat vs. dirstat     = " + textstat.isAlias(dirstat) + "\n\n");

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

  public NativeFileStat
  testStat
  ( 
   File file
  ) 
    throws IOException
  {
    Path path = new Path(file.getPath());
    NativeFileStat stat = new NativeFileStat(path); 

    System.out.print
      ("File Status: \n" + 
       "  target path         = " + path + "\n" +
       "  isValid             = " + stat.isValid() + "\n" + 
       "  isFile              = " + stat.isFile() + "\n" + 
       "  isDirectory         = " + stat.isDirectory() + "\n" + 
       "  fileSize            = " + stat.fileSize() + "\n" + 
       "  lastAccess          = " + stat.lastAccess() + "\n" + 
       "  lastModification    = " + stat.lastModification() + "\n" + 
       "  lastChange          = " + stat.lastChange() + "\n" + 
       "  lastModOrChange     = " + stat.lastModOrChange() + "\n" + 
       "  lastCriticalChange  = " + stat.lastCriticalChange(TimeStamps.now()) + "\n\n"); 

    return stat;
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
