// $Id: CleanOldJobGroups.java,v 1.1 2009/11/17 20:42:18 jim Exp $

import java.io.*; 
import java.util.*; 
import java.math.*; 
import java.text.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;

/*------------------------------------------------------------------------------------------*/
/*   C L E A N   O L D   J O B   G R O U P S                                                */
/*------------------------------------------------------------------------------------------*/

public 
class CleanOldJobGroups
{  
  public static void 
  main
  (
   String[] args  
  )
  {
    boolean success = false;
    try {
      long interval = 86400000L;  /* 24-hours */ 
      boolean dryRun = false;
      switch(args.length) {
      case 0:
        break;

      case 1:
      case 2:
        for(String arg : args) {
          if(arg.startsWith("--interval=")) {
            Long i = null;
            try {
              i = new Long(arg.substring(11, arg.length()));
            }
            catch(NumberFormatException ex) {
              System.out.print("ERROR: The interval (" + args[0] + ") is not valid!\n");
              System.exit(1);
            }
            
            if(i > 0L) 
              interval = i*3600000L;  /* convert from hours to msec */ 
            else {
              
            }
          }
          else if(arg.equals("--dry-run")) {
            dryRun = true;
          }
        }
        break;

      default:
        throw new PipelineException
          ("usage: CleanOldJobGroups [--interval=hours]  [--dry-run] \n" + 
           "\n" +
           "Deletes completed (finished or failed) Jobs Groups from the queue which\n" + 
           "are older than a given interval.\n" + 
           "\n" + 
           "--interval=hours\n" +
           "\n" + 
           "  Sets the age at which a completed Job Group should be considered for\n" + 
           "  deletion. If not specified, the default interval is 24-hours.\n" + 
           "\n" + 
           "--dry-run\n" + 
           "\n" + 
           "  Prints information about what Job Groups would have been deleted but\n" + 
           "  does not delete any of them.  Useful for seeing what would have been\n" + 
           "  done before actually doing it.\n\n");            
      }

      {
        QueueMgrClient qclient = new QueueMgrClient(); 
        
        TreeMap<Long,String> doomed = new TreeMap<Long,String>();
        {
          TreeMap<Long,QueueJobGroup> allGroups = qclient.getJobGroups();
          long now = System.currentTimeMillis(); 
          for(QueueJobGroup group : allGroups.values()) {
            Long stamp = group.getCompletedStamp();
            if((stamp != null) && ((now-interval) > stamp)) {
              doomed.put(group.getGroupID(), group.getNodeID().getAuthor());
              LogMgr.getInstance().logAndFlush
                (LogMgr.Kind.Ops, LogMgr.Level.Info,
                 "Found Job Group (" + group.getGroupID() + "):" + 
                 " owned by (" + group.getNodeID().getAuthor() + ")" +
                 " completed on (" + TimeStamps.format(stamp) + ").");
            }
          }
          
          if(dryRun) {
            LogMgr.getInstance().logAndFlush
              (LogMgr.Kind.Ops, LogMgr.Level.Info,
               "Found (" + doomed.size() + ") Job Groups to Delete.\n" + 
               "Nothing Deleted (dry run).");
          }
          else if(!doomed.isEmpty()) {
            qclient.deleteJobGroups(doomed);
            LogMgr.getInstance().logAndFlush
              (LogMgr.Kind.Ops, LogMgr.Level.Info,
               "Deleted (" + doomed.size() + ") Job Groups.");
          }
          else {
            LogMgr.getInstance().logAndFlush
              (LogMgr.Kind.Ops, LogMgr.Level.Info,
               "Nothing Deleted."); 
          }
        }
        
        success = true;
      }
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
}
