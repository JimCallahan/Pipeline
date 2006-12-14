// $Id: QueueJobCounters.java,v 1.1 2006/12/14 02:39:05 jim Exp $

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
    pCountersLock = new ReentrantReadWriteLock();
    pCounters     = new TreeMap<Long,Counters>();
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
    timer.aquire();
    pCountersLock.writeLock().lock();
    try {
      timer.resume();	

      LogMgr.getInstance().log
	(LogMgr.Kind.Dsp, LogMgr.Level.Finest, 
	 "Init Job Counts for Group [" + group.getGroupID() + "]");

      SortedSet<Long> jobIDs = group.getJobIDs();
      Counters counters = new Counters(jobIDs.size());

      for(Long jobID : jobIDs) {
	if(pCounters.put(jobID, counters) != null) 
	  LogMgr.getInstance().logAndFlush
	    (LogMgr.Kind.Dsp, LogMgr.Level.Warning, 
	     "Somehow the job (" + jobID + ") was already in the state counts table!");
      }
    }
    finally {
      pCountersLock.writeLock().unlock();
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
    pCountersLock.writeLock().lock();
    try {
      timer.resume();	

      for(Long jobID : group.getJobIDs())
	pCounters.remove(jobID);
    }
    finally {
      pCountersLock.writeLock().unlock();
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the job state counts after a change to the job state information.
   */ 
  public void 
  update
  (
   TaskTimer timer, 
   QueueJobInfo info 
  ) 
  {
    timer.aquire();
    pCountersLock.readLock().lock();
    try {
      timer.resume();	

      Counters counters = pCounters.get(info.getJobID());
      if(counters != null) 
	counters.update(info);
      else 
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Dsp, LogMgr.Level.Warning, 
	   "Somehow the job (" + info.getJobID() + ") was not in the state counts table!");
    }
    finally {
      pCountersLock.readLock().unlock();
    }
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
    timer.aquire();
    pCountersLock.readLock().lock();
    try {
      timer.resume();	
    
      Counters counters = pCounters.get(jobID); 
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
    finally {
      pCountersLock.readLock().unlock();
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
    timer.aquire();
    pCountersLock.readLock().lock();
    try {
      timer.resume();	
    
      Counters counters = pCounters.get(jobID);
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
    finally {
      pCountersLock.readLock().unlock();
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
      pTotal = total; 
    }
    public synchronized void
    update
    (
     QueueJobInfo info
    ) 
    {
      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Dsp, LogMgr.Level.Finest)) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Dsp, LogMgr.Level.Finest, 
	   "Job Pre-Counts [" + info.getJobID() + "]: " + info.getState() + "\n" +
	   "   Pending = " + pPending + "\n" + 
	   "   Running = " + pRunning + "\n" + 
	   "  Finished = " + pFinished+ "\n" + 
	   "     TOTAL = " + pTotal);
      }

      switch(info.getState()) {
      case Queued:
	pPending++;
	break;
	
      case Preempted:
	pRunning = Math.max(pRunning-1, 0);
	pPending++;
	break;

      case Paused:
      case Aborted:
	pPending = Math.max(pPending-1, 0);
	break;

      case Running:
	pPending = Math.max(pPending-1, 0);
	pRunning++;
	break;

      case Finished:
	pFinished++;
	pRunning = Math.max(pRunning-1, 0);
	break;

      case Failed:
	pRunning = Math.max(pRunning-1, 0);
      }

      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Dsp, LogMgr.Level.Finest)) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Dsp, LogMgr.Level.Finest, 
	   "Job Post-Counts [" + info.getJobID() + "]: " + info.getState() + "\n" +
	   "   Pending = " + pPending + "\n" + 
	   "   Running = " + pRunning + "\n" + 
	   "  Finished = " + pFinished + "\n" + 
	   "     TOTAL = " + pTotal);
      }
    }
    
    public synchronized double 
    percentEngaged() 
    {
      return ((double) (pRunning+pFinished)) / ((double) pTotal);
    }

    public synchronized double 
    percentPending() 
    {
      return ((double) (pPending)) / ((double) pTotal);
    }
    

    private long  pPending; 
    private long  pRunning; 
    private long  pFinished; 
    private long  pTotal; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Job state counts indexed by unique job ID.
   */ 
  private ReentrantReadWriteLock  pCountersLock; 
  private TreeMap<Long,Counters>  pCounters; 

}
