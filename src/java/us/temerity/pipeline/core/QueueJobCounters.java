// $Id: QueueJobCounters.java,v 1.3 2009/05/14 23:30:43 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B   C O U N T E R S                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Maintains a count of the Running, Finished and total number of jobs in each job group.
 */
public
class QueueJobCounters
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new set of counters.
   */ 
  public 
  QueueJobCounters()
  {
    pCountersByJob   = new TreeMap<Long,Counters>(); 
    pCountersByGroup = new TreeMap<Long,Counters>(); 
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new set of shared job state counters for all jobs in the given group.
   */ 
  public void 
  initCounters
  (
   TaskTimer timer, 
   QueueJobGroup group
  ) 
  {
    long groupID = group.getGroupID();
    LogMgr.getInstance().log
      (LogMgr.Kind.Dsp, LogMgr.Level.Finest, 
       "Init Job Counts for Group [" + groupID + "]");
    
    SortedSet<Long> jobIDs = group.getJobIDs();
    Counters counters = new Counters(jobIDs.size());
      
    timer.aquire();
    synchronized(pCountersByGroup) {
      timer.resume();
      
      if(pCountersByGroup.put(groupID, counters) != null)
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Dsp, LogMgr.Level.Warning, 
           "Somehow the job group (" + groupID + ") was already in the state " + 
           "counts table!");
    }
    
    timer.aquire();
    synchronized(pCountersByJob) {
      timer.resume();
      
      for(Long jobID : jobIDs) {
        if(pCountersByJob.put(jobID, counters) != null) 
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Dsp, LogMgr.Level.Warning, 
             "Somehow the job (" + jobID + ") was already in the state counts table!");
      }
    }
  }

  /**
   * Remove all counters for jobs in the given job group.
   */ 
  public void 
  removeCounters
  (
   TaskTimer timer, 
   QueueJobGroup group
  ) 
  {
    timer.aquire();
    synchronized(pCountersByGroup) {
      timer.resume();	
      pCountersByGroup.remove(group.getGroupID());
    }

    timer.aquire();
    synchronized(pCountersByJob) {
      timer.resume();

      for(Long jobID : group.getJobIDs())
	pCountersByJob.remove(jobID);
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the job state counts after a change to the job state information.
   * 
   * @param timer
   *   The operation timer.
   * 
   * @param prevState
   *   The previous job state before the change or
   *   <CODE>null</CODE> if the previous state is unknown.
   * 
   * @para info
   *   The current job info which includes the updated job state.
   */ 
  public void 
  update
  (
   TaskTimer timer, 
   JobState prevState, 
   QueueJobInfo info
  ) 
  {
    Counters counters = null;
    timer.aquire();
    synchronized(pCountersByJob) {
      timer.resume();
      counters = pCountersByJob.get(info.getJobID());
    }

    if(counters != null) 
      counters.update(prevState, info);
    else 
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Dsp, LogMgr.Level.Warning, 
         "Somehow the job (" + info.getJobID() + ") was not in the state counts table!");
  }

  /**
   * Get the percentage of jobs in the job group owning the given job which are currently 
   * Running or Finished.
   */ 
  public double 
  percentEngaged
  (
   TaskTimer timer,
   long jobID
  ) 
  {
    Counters counters = null;
    timer.aquire();
    synchronized(pCountersByJob) {
      timer.resume();
      counters = pCountersByJob.get(jobID);
    }

    if(counters != null) {
      double percent = counters.percentEngaged();
      LogMgr.getInstance().log
        (LogMgr.Kind.Dsp, LogMgr.Level.Finest, 
         "Percent Engaged [" + jobID + "]: " + percent);	      
      return percent;
    }
    else {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Dsp, LogMgr.Level.Warning, 
         "Somehow the job (" + jobID + ") was not in the state counts table!");
      return 0.0;
    }
  }

  /**
   * Get the percentage of jobs in the job group owning the given job which are currently 
   * Queued or Preempted. 
   */ 
  public double 
  percentPending
  (
   TaskTimer timer,
   long jobID
  ) 
  {
    Counters counters = null;
    timer.aquire();
    synchronized(pCountersByJob) {
      timer.resume();
      counters = pCountersByJob.get(jobID);
    }

    if(counters != null) {
      double percent = counters.percentPending();
      LogMgr.getInstance().log
        (LogMgr.Kind.Dsp, LogMgr.Level.Finest, 
         "Percent Pending [" + jobID + "]: " + percent);
      return percent;
    }
    else {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Dsp, LogMgr.Level.Warning, 
         "Somehow the job (" + jobID + ") was not in the state counts table!");
      return 0.0;
    }
  }

  /**
   * Get the distribution of job states for the jobs in the given group. 
   */ 
  public double[] 
  getDistribution
  (
   TaskTimer timer,
   long groupID
  ) 
  {
    Counters counters = null;
    timer.aquire();
    synchronized(pCountersByGroup) {
      timer.resume();
      counters = pCountersByGroup.get(groupID);
    }

    if(counters != null) {
      double dist[] = counters.distribution();

      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Dsp, LogMgr.Level.Finest)) {
        StringBuilder buf = new StringBuilder(); 
        buf.append("Job Group Distribution [" + groupID + "]:"); 
        for(JobState js : JobState.all()) 
          buf.append("\n" + js + " = " + dist[js.ordinal()]);
      
	LogMgr.getInstance().log(LogMgr.Kind.Dsp, LogMgr.Level.Finest, buf.toString());
      }

      return dist;
    }
    else {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Dsp, LogMgr.Level.Warning, 
         "Somehow the job group (" + groupID + ") was not in the state counts table!");
      
      return new double[JobState.all().size()];
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private
  class Counters
  {
    public 
    Counters
    (
     long total
    ) 
    {
      pCounts = new long[JobState.all().size()]; 
      pTotal = ((double) total);
    }

    public synchronized void
    update
    (
     JobState prevState, 
     QueueJobInfo info
    ) 
    {
      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Dsp, LogMgr.Level.Finest)) {
        StringBuilder buf = new StringBuilder(); 
        buf.append("Job Pre-Counts [" + info.getJobID() + "]: " + 
                   prevState + " -> " + info.getState()); 
        for(JobState js : JobState.all()) 
          buf.append("\n" + js + " = " + pCounts[js.ordinal()]);
      
	LogMgr.getInstance().log(LogMgr.Kind.Dsp, LogMgr.Level.Finest, buf.toString());
      }
      
      if(prevState != null) {
        if(pCounts[prevState.ordinal()] > 0) {
          pCounts[prevState.ordinal()]--;
        }
        else {
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Dsp, LogMgr.Level.Warning, 
             "Somehow the count of jobs with a " + prevState + " state was already " + 
             "when attempting to decrement the count after a change to a " + 
             info.getState() + " state for the job (" + info.getJobID() + ")!");
        }
      }

      pCounts[info.getState().ordinal()]++;

      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Dsp, LogMgr.Level.Finest)) {
        StringBuilder buf = new StringBuilder(); 
        buf.append("Job Post-Counts [" + info.getJobID() + "]: " + 
                   prevState + " -> " + info.getState()); 
        for(JobState js : JobState.all()) 
          buf.append("\n" + js + " = " + pCounts[js.ordinal()]);
	LogMgr.getInstance().log(LogMgr.Kind.Dsp, LogMgr.Level.Finest, buf.toString());
      }
    }
    
    public synchronized double 
    percentEngaged() 
    {
      return ((double) (pCounts[JobState.Running.ordinal()] + 
                        pCounts[JobState.Finished.ordinal()])) / pTotal;
    }

    public synchronized double 
    percentPending() 
    {
      return ((double) (pCounts[JobState.Queued.ordinal()] + 
                        pCounts[JobState.Preempted.ordinal()])) / pTotal;
    }

    public synchronized double[] 
    distribution() 
    {
      double dist[] = new double[pCounts.length];
      if(pTotal > 0.0) {
        int wk;
        for(wk=0; wk<pCounts.length; wk++) 
          dist[wk] = ((double) pCounts[wk]) / pTotal;
      }
      
      return dist;
    }

    private long   pCounts[]; 
    private double pTotal; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Job state counts indexed by unique job ID and job group ID. <P> 
   * 
   * There is only one underlying Counters instance but it is reachable via multiple paths
   * from each job ID in the group and from the group ID as well.  The Counters methods are
   * all synchronized, but access to the membership of these tables should be protected
   * with synchronized blocks as well.
   */ 
  private TreeMap<Long,Counters>  pCountersByJob; 
  private TreeMap<Long,Counters>  pCountersByGroup; 

}
