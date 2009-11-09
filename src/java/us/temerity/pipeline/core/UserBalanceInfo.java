// $Id: UserBalanceInfo.java,v 1.7 2009/11/09 21:58:05 jesse Exp $

package us.temerity.pipeline.core;

import java.util.*;
import java.util.Map.*;
import java.util.concurrent.atomic.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.BaseOpMap.*;
import us.temerity.pipeline.LogMgr.*;

/*------------------------------------------------------------------------------------------*/
/*   U S E R   B A L A N C E   I N F O                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Contains information about user usage of the queue on a per-user balance group basis. <P>
 */
public 
class UserBalanceInfo
{       
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Constructor to set up the user balance info.
   * <p>
   * 
   */
  public 
  UserBalanceInfo()
  {
    pSamples = new TreeMap<String, ArrayDeque<UserBalanceSample>>();
    pCurrentUsage = new DoubleMap<String, String, Double>();
    pSliceStart = System.currentTimeMillis();
    
    pHostInfos = new TreeMap<String, HostInfo>();
    
    pHostChanges = new MappedLinkedList<String, HostChange>();
    pJobChanges = new MappedLinkedList<String, JobChange>();
    
    pHostChangesLock = new Object();
    pJobChangesLock = new Object();
    pCurrentUsageLock = new Object();
    
    pSamplesToKeep.set(30);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C A L C U L A T E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  public void
  calculateUsage()
  {
    TaskTimer timer = new TaskTimer("User Balance Info");
    
    MappedLinkedList<String, HostChange> hostChanges;
    MappedLinkedList<String, JobChange> jobChanges;
    long startTime;
    long endTime;
    double interval;
    
    timer.aquire();
    synchronized (pJobChangesLock) {
      timer.resume();
      
      startTime = pSliceStart;
      endTime = System.currentTimeMillis();
      pSliceStart = endTime + 1;
      interval = endTime - startTime;
      
      timer.aquire();
      synchronized (pHostChangesLock) {
        timer.resume();
        hostChanges = pHostChanges;
        pHostChanges = new MappedLinkedList<String, HostChange>();
      }
      
      jobChanges = pJobChanges;
      pJobChanges = new MappedLinkedList<String, JobChange>();
    }
    
    {
      timer.suspend();
      TaskTimer tm = new TaskTimer("User Balance Info [Apply Host Changes]");
      for (Entry<String, LinkedList<HostChange>> entry : hostChanges.entrySet()) {
        String hostName = entry.getKey();
        LinkedList<HostChange> changes = entry.getValue();
        HostInfo hostInfo = pHostInfos.get(hostName);
        if (hostInfo == null) {
          hostInfo = new HostInfo(false, null, 0);
          pHostInfos.put(hostName, hostInfo);
        }
        for (HostChange change : changes) {
          if (change.pIsEnabled != null)
            hostInfo.pIsEnabled = change.pIsEnabled;
          if (change.pUserBalanceName != null) {
            if (change.pUserBalanceName.equals(""))
              hostInfo.pUserBalanceName = null;
            else
              hostInfo.pUserBalanceName = change.pUserBalanceName;
          }
          if (change.pNumSlots != null)
            hostInfo.pNumSlots = change.pNumSlots;
        }
      }
      LogMgr.getInstance().logSubStage
        (LogMgr.Kind.Usr, LogMgr.Level.Finer,
         tm, timer);
    }

    /* Count of slots indexed by User Balance Name */ 
    DoubleOpMap<String> slotsPerBalanceGroup = new DoubleOpMap<String>(Op.Add);
    // Count of used slots indexed by User Balance Name and User Name
    TreeMap<String, DoubleOpMap<String>> userSlotsPerBalanceGroup = 
      new TreeMap<String, DoubleOpMap<String>>();
    {
      timer.suspend();
      TaskTimer tm = new TaskTimer("User Balance Info [Calculate Current Usage]");
      for (Entry<String, HostInfo> entry : pHostInfos.entrySet()) {
        String hostname = entry.getKey();
        HostInfo info = entry.getValue();

        DoubleOpMap<String> slotsPerUser = null; 

        if (info.pUserBalanceName != null) {
          if (info.pIsEnabled) 
            slotsPerBalanceGroup.apply(info.pUserBalanceName, (double) info.pNumSlots);

          slotsPerUser = userSlotsPerBalanceGroup.get(info.pUserBalanceName);
          if (slotsPerUser == null) {
            slotsPerUser = new DoubleOpMap<String>(Op.Add);
            userSlotsPerBalanceGroup.put(info.pUserBalanceName, slotsPerUser);
          }        
        }

        TreeMap<Long, Long> startTimeByJob = new TreeMap<Long, Long>();
        LinkedList<JobChange> jchanges = jobChanges.get(hostname);
        if (jchanges != null) {
          for (JobChange jchange : jchanges) {
            long id = jchange.pJobID;
            // Job Started
            if (jchange.pStartTime != null) {
              String oldAuthor= info.pJobs.put(id, jchange.pAuthor); 
              if (oldAuthor != null) {
                LogMgr.getInstance().log
                (Kind.Usr, Level.Warning, 
                  "A Start Time was registered for job (" + id + ") which was marked as " +
                "already running on the same host.");
              }
              startTimeByJob.put(id, jchange.pStartTime);
            }
            // Job Finished
            else {
              String author = info.pJobs.remove(id);
              if (slotsPerUser != null ) {
                Long jobEndTime = jchange.pEndTime;
                Long jobStartTime = startTime;
                // Short Duration job
                if (startTimeByJob.containsKey(id)) 
                  jobStartTime = startTimeByJob.get(id);

                double used = ((double) jobEndTime - jobStartTime) / interval;

                slotsPerUser.apply(author, used);
              }
            }
          }
        } //if (changes != null)

        /* 
         * Now include all jobs that weren't touched this go-round (only if in balance group). 
         */
        if (slotsPerUser != null) {
          for (Entry<Long, String> entry2 : info.pJobs.entrySet()) {
            Long jobID = entry2.getKey();
            String author = entry2.getValue();
            if (startTimeByJob.containsKey(jobID)) {
              double used = ((double) endTime - startTimeByJob.get(jobID)) / interval;
              slotsPerUser.apply(author, used);
            }
            else
              slotsPerUser.apply(author, 1d);
          }
        }
      } // Finished looping through all the hosts.
      LogMgr.getInstance().logSubStage
        (LogMgr.Kind.Usr, LogMgr.Level.Finer,
         tm, timer);
    }
    
    int numSamples = pSamplesToKeep.get();
    
    /* Now we add the newly created samples to the saved info. */
    for (Entry<String, Double> entry : slotsPerBalanceGroup.entrySet()) {
      String userBalanceGroup = entry.getKey();
      Double totalSlots = entry.getValue();
      if (totalSlots == null)
        totalSlots = 0d;
      DoubleOpMap<String> slotsPerUser = userSlotsPerBalanceGroup.get(userBalanceGroup);
      if (slotsPerUser == null)
        slotsPerUser = new DoubleOpMap<String>();
      UserBalanceSample sample = new UserBalanceSample(totalSlots, slotsPerUser);
      
      ArrayDeque<UserBalanceSample> samples = pSamples.get(userBalanceGroup);
      if (samples == null) {
        samples = new ArrayDeque<UserBalanceSample>(numSamples + 1);
        pSamples.put(userBalanceGroup, samples);
      }
      samples.addLast(sample);
      while (samples.size() > numSamples)
        samples.removeFirst();
    }
    
    /* finally we need to calculate the new values for the dispatcher to read. */
    
    /* UserBalance, User, Percent Used */
    DoubleMap<String, String, Double> currentUsage = new DoubleMap<String, String, Double>();
    for (Entry<String, ArrayDeque<UserBalanceSample>> entry : pSamples.entrySet()) {
      String userBalanceGroup = entry.getKey();
      DoubleOpMap<String> slotsPerUser = new DoubleOpMap<String>();
      double totalSlots = 0d;
      for (UserBalanceSample sample : entry.getValue()) {
        totalSlots += sample.pTotalSlots;
        for (Entry<String, Double> entry2 : sample.pUserSlotsUsed.entrySet()) {
          slotsPerUser.apply(entry2.getKey(), entry2.getValue());
        }
      }
      if (totalSlots == 0d)
        continue;
      for (String user : slotsPerUser.keySet()) {
        slotsPerUser.apply(user, totalSlots, Op.Divide);
      }
      currentUsage.put(userBalanceGroup, slotsPerUser);
    }

    synchronized (pCurrentUsageLock) {
      pCurrentUsage = currentUsage; 
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current usage information generated by the last call to 
   * {@link #calculateUsage()}.<P>
   * 
   * This is a live data-structure (it is not copied before returning) so any code that uses
   * this should not modify its contents.
   * 
   * @return
   *   A {@link DoubleMap} with the first key being the user balance group, the second key
   *   being the user name, and the value being the percentage of that user balance groups's
   *   resources that the user has used.
   */
  public DoubleMap<String, String, Double>
  getCurrentStatistics()
  {
    synchronized (pCurrentUsageLock) {
      return pCurrentUsage;   
    }
  }
  
  /**
   * Add a change to a host.
   * 
   * @param hostName
   *   The name of the host.  Required.
   * 
   * @param isEnabled
   *   Whether the host is current enabled or <code>null</code> to not change the status of 
   *   the host.
   * 
   * @param userBalanceName
   *   The UserBalanceGroup associated with the host or <code>null</code> to not change the 
   *   group.  Since <code>null</code> is being used to indicate no change, the empty string 
   *   should be passed in to indicate the removal of the user balance group. 
   * 
   * @param numSlots
   *   The number of total job slots the host has or <code>null</code> to not change the 
   *   number of slots.
   */
  public void
  addHostChange
  (
    String hostName,
    Boolean isEnabled,
    String userBalanceName,
    Integer numSlots
  )
  {
     HostChange change = new HostChange(isEnabled, userBalanceName, numSlots);
     synchronized (pHostChangesLock) {
       pHostChanges.put(hostName, change);
     }
     LogMgr.getInstance().log
       (Kind.Usr, Level.Finest, 
        "Adding a host change for host (" + hostName + ") with the values " + change + ".");
  }
  
  /**
   * Add a change to a job. <p>
   * 
   * All values are required to be not <code>null</code>.  The method is not doing any 
   * <code>null</code> checking, so all code that calls this method should be making sure that 
   * there are not any bad values being passed in.<p>
   * 
   * A time stamp is generated by this method which is the time stamp associated with this 
   * change.  By generating the time stamp here, it is possible to guarantee that the change 
   * is not happening outside the window of the current cycle.  If a time stamp was being 
   * passed in, it could be possible to have events in the change queue that happened before 
   * the current slice started.  This would never be desirable (and would make doing 
   * calculations more difficult) so it is avoided by generating the time stamp inside this 
   * method. 
   * 
   * @param hostName
   *   The hostname of the machine the job is running on.
   *   
   * @param jobID
   *   The jobID of the job that has been changed.
   *   
   * @param author
   *   The name of the user who owns the job.
   *   
   * @param start
   *   Whether the job has started.  There are only two states that a job change can have, 
   *   either started or finished. 
   */
  public void
  addJobChange
  (
    String hostName,
    Long jobID,
    String author,
    boolean start
  )
  {
    JobChange change;
    synchronized (pJobChangesLock) {
     long time = System.currentTimeMillis();
     if (start)
       change = new JobChange(author, jobID, time, null);
     else
       change = new JobChange(author, jobID, null, time);
     
     pJobChanges.put(hostName, change);
    }
    LogMgr.getInstance().log
      (Kind.Usr, Level.Finest, 
       "Adding a job change for host (" + hostName + ") with the values " + change + ".");
  }
  
  /**
   * Get the number of samples that are being saved for each user balance group.
   */
  public int
  getSamplesToKeep()
  {
    return pSamplesToKeep.get();
  }
  
  /**
   * Set the number of samples that are being saved for each user balance group. <p>
   * 
   * If the number of samples is lowered, the excess samples will be eliminated the next time
   * usage is calculated.  If the number of samples is raised, then no samples will be 
   * eliminated until the correct level is reached.
   * 
   * @param samplesToKeep
   *   The number of samples to keep.
   *   
   * @throws IllegalArgumentException
   *   If a non-positive integer is passed in.
   */
  public void
  setSamplesToKeep
  (
    int samplesToKeep  
  )
  {
    if (samplesToKeep < 1)
      throw new IllegalArgumentException
        ("setSamplesToKeep can only be called with positive integers");
    pSamplesToKeep.set(samplesToKeep);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S U B - C L A S S E S                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   *  A slice of user use of the queue in a particular user balance group.  
   */
  private
  class UserBalanceSample
  {
    /*--------------------------------------------------------------------------------------*/
    /*   C O N S T R U C T O R                                                              */
    /*--------------------------------------------------------------------------------------*/

    /**
     * Constructor.
     * 
     * @param totalSlots
     * 
     */
    private
    UserBalanceSample
    (
      Double totalSlots,
      DoubleOpMap<String> userSlotsUsed
    )
    {
      pTotalSlots = totalSlots;
      pUserSlotsUsed = userSlotsUsed;
    }
    
    private final Double pTotalSlots;
    private final DoubleOpMap<String> pUserSlotsUsed;
  }
  
  private
  class HostInfo
  {
    /*--------------------------------------------------------------------------------------*/
    /*   C O N S T R U C T O R                                                              */
    /*--------------------------------------------------------------------------------------*/

    /**
     * Constructor.
     * 
     */
    private
    HostInfo
    (
      boolean isEnabled,
      String userBalanceGroup,
      int numSlots
    )
    {
      pUserBalanceName = userBalanceGroup;
      pNumSlots = numSlots;
      pJobs = new TreeMap<Long, String>();
      pIsEnabled = isEnabled;
    }
    
    public boolean pIsEnabled;
    public String pUserBalanceName;
    public int pNumSlots;
    
    /**
     * JobID, UserName
     */
    public TreeMap<Long, String> pJobs;
  }
  
  /**
   * A change to the running state of job, either starting or stopping.
   */
  private
  class JobChange
  {
    /*--------------------------------------------------------------------------------------*/
    /*   C O N S T R U C T O R                                                              */
    /*--------------------------------------------------------------------------------------*/

    /**
     * Constructor. <P>
     * 
     * Only one of the startTime and endTime parameters should ever be set.
     * 
     * @param author
     *   The user that owns the job.
     *   
     * @param jobID
     *   The job id of the job.
     *   
     * @param startTime
     *   The start time of the job or <code>null</code> if the job is not starting.
     * 
     * @param endTime
     *   The end time of the job or <code>null</code> if the job is not ending.
     */
    private
    JobChange
    (
      String author,
      Long jobID,
      Long startTime,
      Long endTime
    )
    {
      pAuthor = author;
      pJobID = jobID;
      pStartTime = startTime;
      pEndTime = endTime;
    }
    
    @Override
    public String 
    toString()
    {
      StringBuffer buf = new StringBuffer();
      buf.append("JobID: [" + pJobID + "], Author: [" + pAuthor +"], ");
      if (pStartTime != null)
        buf.append("StartTime: [" + pStartTime +"]");
      else
        buf.append("EndTime: [" + pEndTime +"]");
      
      return buf.toString();
    }
    
    private Long pStartTime;
    private Long pEndTime;
    private String pAuthor;
    private long pJobID;
  }

  /**
   * A change to a host. <p>
   * 
   * If the host does not exist, then the host will be created.  Any of the values which are 
   * set to <code>null</code> will be ignored when applying the change to an existing host.
   */
  private
  class HostChange
  {
    /*--------------------------------------------------------------------------------------*/
    /*   C O N S T R U C T O R                                                              */
    /*--------------------------------------------------------------------------------------*/

    /**
     * Constructor.<P>
     * 
     * @param isEnabled
     *   Is the host enabled?  This should be set to false any time the host has its status 
     *   changed from Enabled to something else (including Limbo).
     *   
     * @param userBalanceName
     *   The user balance group the host is in.
     *   
     * @param numSlots
     *   The current number of slots the host has.
     */
    private
    HostChange
    (
      Boolean isEnabled,
      String userBalanceName,
      Integer numSlots
    ) 
    {
      pIsEnabled = isEnabled;
      pUserBalanceName = userBalanceName;
      pNumSlots = numSlots;
    }
    
    @Override
    public String 
    toString()
    {
      return "IsEnabled: [" + pIsEnabled +"], UserBalanceGroup: [" + pUserBalanceName +"], " +
      	     "NumSlots [" + pNumSlots +"]";
    }
    
    private Boolean pIsEnabled;
    private String pUserBalanceName;
    private Integer pNumSlots;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The number of samples that are being kept.
   */
  private AtomicInteger pSamplesToKeep;
  
  /**
   * Hostname, Host Changes
   */
  private MappedLinkedList<String, HostChange> pHostChanges;
  private Object pHostChangesLock;
  
  /**
   * Hostname, Job Changes
   */
  private MappedLinkedList<String, JobChange> pJobChanges;
  private Object pJobChangesLock;
  
  /**
   * All the current samples.
   * <p>
   * New information is added to the front of this. 
   */
  private TreeMap<String, ArrayDeque<UserBalanceSample>> pSamples;
  
  /**
   * Cached information about what the hosts were doing last slice.
   */
  private TreeMap<String, HostInfo> pHostInfos;
  
  private long pSliceStart;
  
  /**
   * UserBalanceGroup, User, Percent Usage 
   */
  private DoubleMap<String, String, Double> pCurrentUsage;
  private Object pCurrentUsageLock;
}
