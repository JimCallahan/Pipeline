// $Id: FileStatBenchmark.java,v 1.1 2009/12/02 12:19:33 jim Exp $

import java.util.*;
import java.io.*;
import java.text.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   S T A T   B E N C H M A R K                                                  */
/*------------------------------------------------------------------------------------------*/

public class
FileStatBenchmark
{
  public static void 
  main
  (
   String[] args  
  )
  {
    boolean success = false;
    try {
      if(args.length < 1) 
        usage(); 

      LinkedList<Path> paths = new LinkedList<Path>();
      for(String arg : args) {
        File file = new File(arg);
        if(file.isDirectory()) {
          for(File f : file.listFiles()) 
            paths.add(new Path(f));
        }
        else if(file.isFile()) {
          paths.add(new Path(file));
        }
      }
        
      TaskTimer timer = new TaskTimer();
      for(Path p : paths) {
        try {
          new NativeFileStat(p);
        }
        catch(IOException ex) {
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Ops, LogMgr.Level.Severe,
             "Unable to stat (" + p + ")!"); 
        }
      }      
      timer.suspend();

      int count = paths.size();
      long total = timer.getTotalDuration();
      double perFile = (total > 0) ? ((double) count*1000) / ((double) total) : 0.0;

      DecimalFormat fmt = new DecimalFormat("#.##");

      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Info,
         "Tested (" + count + ") files in " + total + "msec (" + 
         TimeStamps.formatInterval(total) + "), for a rate of " + fmt.format(perFile) + 
         " files/s.");

      success = true;
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 ex.getMessage());
    }
    catch(Exception ex) {
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 Exceptions.getFullMessage(ex));
    }
    finally {
      LogMgr.getInstance().cleanup();
    }

    System.exit(success ? 0 : 1);
  }

  private static void 
  usage()
    throws PipelineException
  {
    throw new PipelineException
      ("usage: FileStatBenchmark path1 [path2 ... pathN]\n" + 
       "\n" +
       "Performs file metadata lookups on the given paths in exactly the same way " + 
       "the File Manager does internally, but without contacting the Pipeline servers " + 
       "in any way.  If a directory is given as a path, then all files in the directory " + 
       "will be tested."); 
  }

  
}
