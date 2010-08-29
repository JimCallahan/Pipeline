package us.temerity.pipeline.plugin.TaskCleanupExt.v2_4_28;

import java.util.*;
import java.util.Map.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;

/**
 *  Removes working areas that were left by failed verification or publishes after the job 
 *  which resulted in them being created is deleted. <p>
 * 
 *  It is highly recommend to use this extension to prevent an explosion of unnecessary 
 *  working areas. 
 */
public 
class TaskCleanupExt
  extends BaseQueueExt
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TaskCleanupExt()
  {
    super("TaskCleanup", new VersionID("2.4.28"), "Temerity",
          "Removes working areas that were left by failed verification or publishes after " +
          "the job which resulted in them being created is deleted.");
    
    underDevelopment();
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*  S E R V E R   O P S                                                                   */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  public boolean
  hasPostCleanupJobsTask() 
  {
    return true; 
  }
  
  @Override
  public void 
  postCleanupJobsTask
  (
    TreeMap<Long, QueueJob> jobs,
    TreeMap<Long, QueueJobInfo> infos
  )
  {
    for (Entry<Long, QueueJob> entry : jobs.entrySet()) {
      QueueJob job = entry.getValue();
      if (job.getAction().getPluginID().equals(sTaskRunBuilderID)) {
        Long jobID = entry.getKey();
        QueueJobInfo info = infos.get(jobID);
        /* If the job didn't fail, then it cleaned up the working area itself */
        if (info.getState() != JobState.Finished) {
          RemoveViewTask task = new RemoveViewTask(job.getNodeID().getAuthor(), jobID);
          task.start();
        }
      }
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   T H R E A D S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private class
  RemoveViewTask
    extends Thread
  {
    public
    RemoveViewTask
    (
      String userName,
      Long jobID
    )
    {
      pUserName = userName;
      pWorkingArea = String.valueOf(jobID);
    }
    
    @Override
    public void 
    run()
    {
      MasterMgrClient mclient = new MasterMgrClient();
      try {
        TreeSet<String> names = mclient.getWorkingNames(pUserName, pWorkingArea, null);
        if (names != null && !names.isEmpty())
          mclient.release(pUserName, pWorkingArea, names, true);
        mclient.removeWorkingArea(pUserName, pWorkingArea);
      }
      catch (PipelineException ex) {
        LogMgr.getInstance().logAndFlush
          (Kind.Ext, Level.Warning, 
           "Release view failed in TaskCleanupExt for working area (" + pUserName+ "|" + 
           pWorkingArea + ").\n" + ex.getMessage());
      }
      finally {
        mclient.disconnect();
      }
    }
    
    private String pUserName;
    private String pWorkingArea;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4161661185158300063L;
  
  private static final PluginID sTaskRunBuilderID = 
    new PluginID("TaskRunBuilder", new VersionID("2.4.28"), "Temerity");
}
