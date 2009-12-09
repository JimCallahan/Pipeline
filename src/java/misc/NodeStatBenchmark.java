// $Id: NodeStatBenchmark.java,v 1.1 2009/12/09 14:29:12 jim Exp $

import java.util.*;
import java.io.*;
import java.text.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   S T A T   B E N C H M A R K                                                  */
/*------------------------------------------------------------------------------------------*/

public class
NodeStatBenchmark
{
  public static void 
  main
  (
   String[] args  
  )
  {
    boolean success = false;
    try {
      if(args.length < 2) 
        usage(); 
      
      int runs = new Integer(args[0]);

      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Info,
         "Getting file paths for matching nodes...");

      LinkedList<Path> paths = new LinkedList<Path>();
      long numAreas = 0L;
      long numNodes = 0L;
      long numPaths = 0L;
      {
        PluginMgrClient.init(true);
        MasterMgrClient client = new MasterMgrClient();
        try {
          TreeMap<String,TreeSet<String>> areas = client.getWorkingAreas();

          int wk;
          for(wk=1; wk<args.length; wk++) {
            String pattern = args[wk]; 
            for(String author : areas.keySet()) {
              for(String view : areas.get(author)) {
                Path wpath = new Path(PackageInfo.sWorkPath, author + "/" + view);
                for(String name : client.getWorkingNames(author, view, pattern)) {
                  long num = 0L;
                  Path npath = new Path(wpath, name);
                  Path ppath = npath.getParentPath();
                  NodeMod mod = client.getWorkingVersion(author, view, name);
                  for(FileSeq fseq : mod.getSequences()) {
                    for(Path fpath : fseq.getPaths()) {
                      paths.add(new Path(ppath, fpath));
                      num++;
                    }
                  }

                  LogMgr.getInstance().log
                    (LogMgr.Kind.Ops, LogMgr.Level.Info,
                     "Found: " + name + " (" + num + " files)."); 

                  numNodes++;
                }
                numAreas++;
              }
            }
          }

          numPaths = paths.size();      
        }
        finally {
          client.disconnect();
          
          PluginMgrClient pclient = PluginMgrClient.getInstance();
          if(pclient != null) 
            pclient.disconnect();
        }
      }

      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Info,
         "Found (" + numPaths + ") files for (" + numNodes + ") nodes in " + 
         "(" + numAreas + ") working areas.\n" + 
         "Timing file stats..."); 

      /* time the stats of all files */ 
      int rk;
      for(rk=0; rk<runs; rk++) {
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
        
        long total = timer.getTotalDuration();
        double perFile = (total > 0) ? ((double) numPaths*1000) / ((double) total) : 0.0;
        
        DecimalFormat fmt = new DecimalFormat("#.##");
        
        LogMgr.getInstance().log
          (LogMgr.Kind.Ops, LogMgr.Level.Info,
           rk + ": Tested (" + numPaths + ") files in " + total + "msec (" + 
           TimeStamps.formatInterval(total) + "), for a rate of " + fmt.format(perFile) + 
           " files/s.");
      }

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
      ("usage: NodeStatBenchmark runs node-regex1 [node-regex2 ... node-regexN]" +
       "\n" +
       "Performs file metadata lookups (runs) number of times on all of the files " + 
       "associated with the working area nodes which match the given pattern.  " + 
       "Statistics are reported for all that match."); 
  }
}
