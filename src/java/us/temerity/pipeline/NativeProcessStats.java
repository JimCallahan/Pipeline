// $Id: NativeProcessStats.java,v 1.1 2004/11/09 06:01:32 jim Exp $

package us.temerity.pipeline;

import java.io.*; 
import java.util.*;
import java.util.concurrent.atomic.*;

/*------------------------------------------------------------------------------------------*/
/*   N A T I V E   P R O C E S S   S T A T S                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */
public
class NativeProcessStats
  extends Native
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * 
   */ 
  public 
  NativeProcessStats() 
  {
    loadLibrary();
    
    pStats = new TreeMap<Integer,ProcStats>();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Add the given process to the set of monitored processes.
   * 
   * @param pid
   *   The process ID to begin monitoring.
   */ 
  public void
  monitor
  (
   int pid
  ) 
  {
    synchronized(pStats) {
      if(!pStats.containsKey(pid)) 
	pStats.put(pid, null);
    }
  }

  /**
   * Remove the given process to the set of monitored processes.
   * 
   * @param pid
   *   The process ID to cease monitoring.
   */ 
  public void 
  unmonitor
  (
   int pid
  ) 
  {
    synchronized(pStats) {
      pStats.remove(pid);
    }    
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the process IDs of the processes currently being monitored.
   */ 
  public Set<Integer>
  getMonitored() 
  {
    synchronized(pStats) {
      return Collections.unmodifiableSet(pStats.keySet()); 
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the number of seconds the process and its children have been scheduled in 
   * user mode.
   * 
   * @param pid
   *   The process ID.
   * 
   * @return 
   *   The time in seconds or <CODE>null</CODE> if no statistics have been collected 
   *   for the given process.
   */
  public Double
  getUserTime
  (
   int pid
  ) 
  {
    synchronized(pStats) {
      ProcStats stats = pStats.get(pid);
      if(stats == null) 
	return null;

      return new Double(((double) stats.uUTime) / 100.0);
    }    
  }
    
  /**
   * Get the number of seconds the process and its children have been scheduled in 
   * kernel mode.
   * 
   * @param pid
   *   The process ID.
   * 
   * @return 
   *   The time in seconds or <CODE>null</CODE> if no statistics have been collected 
   *   for the given process.
   */
  public Double
  getSystemTime
  (
   int pid
  ) 
  {
    synchronized(pStats) {
      ProcStats stats = pStats.get(pid);
      if(stats == null) 
	return null;

      return new Double(((double) stats.uSTime) / 100.0);
    }    
  }
  
  /**
   * Get the number of major faults which occured for the process and its children which 
   * have required loading a memory page from disk.
   * 
   * @param pid
   *   The process ID.
   * 
   * @return 
   *   The number of faults or <CODE>null</CODE> if no statistics have been collected 
   *   for the given process.
   */
  public Long
  getPageFaults
  (
   int pid
  ) 
  {
    synchronized(pStats) {
      ProcStats stats = pStats.get(pid);
      if(stats == null) 
	return null;

      return stats.uPageFaults;
    }    
  }

  /**
   * Get the maximum virtual memory size of the process and its children in bytes.
   * 
   * @param pid
   *   The process ID.
   * 
   * @return 
   *   The size in bytes or <CODE>null</CODE> if no statistics have been collected 
   *   for the given process.
   */
  public Long
  getVirtualSize
  (
   int pid
  ) 
  {
    synchronized(pStats) {
      ProcStats stats = pStats.get(pid);
      if(stats == null) 
	return null;

      return stats.uVirtualSize;
    }    
  }

  /**
   * Get the maximum resident memory size of the process and its children in bytes.
   * 
   * @param pid
   *   The process ID.
   * 
   * @return 
   *   The size in bytes or <CODE>null</CODE> if no statistics have been collected 
   *   for the given process.
   */
  public Long
  getResidentSize
  (
   int pid
  ) 
  {
    synchronized(pStats) {
      ProcStats stats = pStats.get(pid);
      if(stats == null) 
	return null;

      return stats.uResidentSize;
    }    
  }

  /**
   * Get the cumilative amount of memory swapped by the process and its children in bytes.
   * 
   * @param pid
   *   The process ID.
   * 
   * @return 
   *   The size in bytes or <CODE>null</CODE> if no statistics have been collected 
   *   for the given process.
   */
  public Long
  getSwappedSize
  (
   int pid
  ) 
  {
    synchronized(pStats) {
      ProcStats stats = pStats.get(pid);
      if(stats == null) 
	return null;

      return stats.uSwappedSize;
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Collect process resource usage statistics for the monitored processes and their children.
   */ 
  public void 
  collect() 
  {
    /* collect stats for all running processes */ 
    TreeMap<Integer,ProcStats> stats = new TreeMap<Integer,ProcStats>(); 
    TreeMap<Integer,TreeSet<Integer>> children = new TreeMap<Integer,TreeSet<Integer>>();
    {
      File proc = new File("/proc");
      String[] procs = proc.list(new ProcFilter());
      int wk;
      for(wk=0; wk<procs.length; wk++) {
	Integer pid = new Integer(procs[wk]); 
	try {
	  if(collectStatsNative(pid)) {
	    stats.put(pid, new ProcStats(pUTime, pSTime, pPageFaults, 
					 pVirtualSize, pResidentSize, pSwappedSize));
	    
	    TreeSet<Integer> cset = children.get(pParentPID);
	    if(cset == null) {
	      cset = new TreeSet<Integer>();
	      children.put(pParentPID, cset);
	    }
	    cset.add(pid);
	  }
	}
	catch(IOException ex) {
	  Logs.ops.warning
	    ("Unable to collection statistics for process (" + pid + "): \n" + 
	     ex.getMessage());
	}
      }
    }
    
    /* update the statistics for the monitored processes */ 
    synchronized(pStats) {      
      for(Integer pid : pStats.keySet()) {
	if(stats.get(pid) != null) {
	  ProcStats cstats = new ProcStats();
	  collateStats(pid, cstats, stats, children);
	  
	  ProcStats pstats = pStats.get(pid);
	  if(pstats == null) 
	    pStats.put(pid, cstats);
	  else 
	    pstats.max(cstats);
	}
      }
    }
  }

  /**
   * Accumilate the statistics for the given process and all of its children.
   * 
   * @param pid
   *   The current process ID.
   * 
   * @param cstats
   *   The cumulative statistics.
   * 
   * @param stats
   *   The table of all process statisctics indexed by process ID.
   * 
   * @param children
   *   The table of child process IDs indexed by parent process ID.
   */ 
  private void 
  collateStats
  (
   Integer pid, 
   ProcStats cstats, 
   TreeMap<Integer,ProcStats> stats, 
   TreeMap<Integer,TreeSet<Integer>> children
  ) 
  {
    ProcStats pstats = stats.get(pid);
    if(pstats != null) 
      cstats.sum(pstats);
    
    TreeSet<Integer> cpids = children.get(pid);
    if(cpids != null) {
      for(Integer cpid : cpids) 
	collateStats(cpid, cstats, stats, children);
    }
  }    



  /*----------------------------------------------------------------------------------------*/
  /*   N A T I V E    H E L P E R S                                                         */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Collect resource usage statistics for the given process.
   * 
   * @param pid  
   *   The native process ID.
   * 
   * @return 
   *   Whether the process was still running.
   * 
   * @throws IOException 
   *   If unable to determine the process stats.
   */ 
  private native boolean
  collectStatsNative
  (
   int pid
  )
    throws IOException;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * A filter which selects process filenames in the /proc filesystem. 
   */
  private 
  class ProcFilter
    implements FilenameFilter
  {
    public boolean 
    accept
    (
     File dir, 
     String name
    ) 
    {
      File file = new File(dir, name);
      return (file.isDirectory() && name.matches("[0-9]+"));
    }
  }
  

  /**
   * The statistics collected for a single process.
   */
  private 
  class ProcStats
  {
    public 
    ProcStats()
    {}

    public 
    ProcStats
    (
     long utime, 
     long stime, 
     long pageFaults, 
     long virtualSize, 
     long residentSize, 
     long swappedSize         
    ) 
    {	   
      uUTime        = utime; 	   
      uSTime        = stime; 	   
      uPageFaults   = pageFaults; 	   
      uVirtualSize  = virtualSize; 	   
      uResidentSize = residentSize;    
      uSwappedSize  = swappedSize;  
    }

    public void
    sum
    (
     ProcStats stats
    ) 
    {
      uUTime        += stats.uUTime;
      uSTime        += stats.uSTime;
      uPageFaults   += stats.uPageFaults;
      uVirtualSize  += stats.uVirtualSize;
      uResidentSize += stats.uResidentSize;
      uSwappedSize  += stats.uSwappedSize;
    }

    public void 
    max
    (
     ProcStats stats
    ) 
    {
      uUTime        = Math.max(uUTime,        stats.uUTime);
      uSTime        = Math.max(uSTime,        stats.uSTime);
      uPageFaults   = Math.max(uPageFaults,   stats.uPageFaults);
      uVirtualSize  = Math.max(uVirtualSize,  stats.uVirtualSize);
      uResidentSize = Math.max(uResidentSize, stats.uResidentSize);
      uSwappedSize  = Math.max(uSwappedSize,  stats.uSwappedSize);
    }
     
    public long  uUTime;
    public long  uSTime;
    public long  uPageFaults;      
    public long  uVirtualSize;
    public long  uResidentSize;
    public long  uSwappedSize; 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The cumulative statistics for each monitored process and all of its children. 
   * 
   * Access to this field should be protected by a synchronized block.
   */
  private TreeMap<Integer,ProcStats>  pStats;


  /*----------------------------------------------------------------------------------------*/

  /* 
   * The following fields are used to return the process statistics from the 
   * collectStatsNative method.  These fields should only be referenced by the 
   * single CollectStatsTask thread and therefore do not require lock protection.
   */ 
  
  /**
   * The parent process ID.
   */ 
  private int  pParentPID;

  /**
   * The number of jiffies (1/100th of a second) the process has been scheduled in 
   * user mode.
   */
  private long  pUTime;

  /**
   * The number of jiffies (1/100th of a second) the process has been scheduled in 
   * kernel mode.
   */
  private long  pSTime;

  /**
   * The number of major faults the process has made which have required loading a 
   * memory page from disk.
   */
  private long  pPageFaults;      

  /**
   * The virtual memory size of the process in bytes.
   */ 
  private long  pVirtualSize;

  /**
   * The resident memory set size of the process in bytes.
   */ 
  private long pResidentSize;

  /**
   * The cumilative amount of memory swapped by the process in bytes.
   */ 
  private long pSwappedSize;

}
