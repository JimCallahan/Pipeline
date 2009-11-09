// $Id: UserBalanceInfo.java,v 1.1 2009/11/09 19:48:18 jesse Exp $

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
 * Contains information about user usage of the queue on a per-user balance group basis.
 * <p>
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
    pInfo = new TreeMap<String, ArrayDeque<UserBalanceSlice>>();
    pCurrentUsage = new DoubleMap<String, String, Double>();
    pSliceStart = System.currentTimeMillis();
    
    pHostInfos = new TreeMap<String, HostInfo>();
    
    pHostChanges = new MappedLinkedList<String, HostChange>();
    pJobChanges = new MappedLinkedList<String, JobChange>();
    
    pHostChangesLock = new Object();
    pJobChangesLock = new Object();
    pCurrentUsageLock = new Object();
    pTimeLock = new Object();
    
    pSamplesToKeep.set(30);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C A L C U L A T E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  public void
  calculateUsage()
  {
    TaskTimer timer = new TaskTimer("User Balance Info");
    
    MappedLinkedList<String, HostChange> sliceHostChanges;
    MappedLinkedList<String, JobChange> jobChanges;
    long startTime;
    long endTime;
    double interval;
    
    timer.aquire();
    synchronized (pTimeLock) {
      timer.resume();
      
      startTime = pSliceStart;
      endTime = System.currentTimeMillis();
      pSliceStart = endTime + 1;
      interval = endTime - startTime;
      
      timer.aquire();
      synchronized (pHostChangesLock) {
        timer.resume();
        sliceHostChanges = pHostChanges;
        pHostChanges = new MappedLinkedList<String, HostChange>();
      }
      
      synchronized (pJobChangesLock) {
        jobChanges = pJobChanges;
        pJobChanges = new MappedLinkedList<String, JobChange>();
      }
    }
    
    {
      timer.suspend();
      TaskTimer tm = new TaskTimer("User Balance Info [Apply Host Changes]");
      for (Entry<String, LinkedList<HostChange>> entry : sliceHostChanges.entrySet()) {
        String hostName = entry.getKey();
        LinkedList<HostChange> changes = entry.getValue();
        HostInfo info = pHostInfos.get(hostName);
        if (info == null) {
          info = new HostInfo(false, null, 0);
          pHostInfos.put(hostName, info);
        }
        for (HostChange change : changes) {
          if (change.pEnabled != null)
            info.pEnabled = change.pEnabled;
          if (change.pUserBalanceGroup != null) {
            if (change.pUserBalanceGroup.equals(""))
              info.pUserBalanceGroup = null;
            else
              info.pUserBalanceGroup = change.pUserBalanceGroup;
          }
          if (change.pNumSlots != null)
            info.pNumSlots = change.pNumSlots;
        }
      }
      LogMgr.getInstance().logSubStage
        (LogMgr.Kind.Usr, LogMgr.Level.Finer,
         tm, timer);
    }

    // UserBalance, Usage 
    DoubleOpMap<String> totalSlots = new DoubleOpMap<String>(Op.Add);
    // UserBalance, User, Usage
    TreeMap<String, DoubleOpMap<String>> userSlots = 
      new TreeMap<String, DoubleOpMap<String>>();

    {
      timer.suspend();
      TaskTimer tm = new TaskTimer("User Balance Info [Calculate Current Usage]");
      for (Entry<String, HostInfo> entry : pHostInfos.entrySet()) {
        String hostname = entry.getKey();
        HostInfo info = entry.getValue();

        DoubleOpMap<String> userUse = null; 

        if (info.pUserBalanceGroup != null) {
          if (info.pEnabled) 
            totalSlots.apply(info.pUserBalanceGroup, (double) info.pNumSlots);

          userUse = userSlots.get(info.pUserBalanceGroup);
          if (userUse == null) {
            userUse = new DoubleOpMap<String>(Op.Add);
            userSlots.put(info.pUserBalanceGroup, userUse);
          }        
        }
        TreeSet<Long> touchedJobs = new TreeSet<Long>();
        LinkedList<JobChange> changes = jobChanges.get(hostname);
        TreeMap<Long, Long> started = new TreeMap<Long, Long>();
        if (changes != null) {
          for (JobChange change : changes) {
            long id = change.pJobID;
            // Job Started
            if (change.pStartTime != null) {
              info.pJobs.put(id, change.pAuthor);
              touchedJobs.add(id);
              started.put(id, change.pStartTime);
            }
            // Job Finished
            else {
              String author = info.pJobs.remove(id);
              if (userUse != null ) {
                Long jobEndTime = change.pEndTime;
                Long jobStartTime = startTime;
                // Short Duration job
                if (started.containsKey(id)) {
                  jobStartTime = started.get(id);
                }
                double used = ((double) jobEndTime - jobStartTime) / interval;

                userUse.apply(author, used);
              }
            }
          }
        } //if (changes != null)

        // Now include all jobs that weren't touched this go-round (only if in balance group).
        if (userUse != null) {
          for (Entry<Long, String> entry2 : info.pJobs.entrySet()) {
            Long jobID = entry2.getKey();
            if (!touchedJobs.contains(jobID)) {
              String author = entry2.getValue();
              userUse.apply(author, 1d);
            }
          }
        }
      } // Finished looping through all the hosts.
      LogMgr.getInstance().logSubStage
        (LogMgr.Kind.Usr, LogMgr.Level.Finer,
         tm, timer);
    }
    
    int samples = pSamplesToKeep.get();
    
    // Now we update all the slices.
    for (Entry<String, Double> entry : totalSlots.entrySet()) {
      String userBalanceGroup = entry.getKey();
      Double slots = entry.getValue();
      if (slots == null)
        slots = 0d;
      DoubleOpMap<String> userUse = userSlots.get(userBalanceGroup);
      if (userUse == null)
        userUse = new DoubleOpMap<String>();
      UserBalanceSlice slice = new UserBalanceSlice(slots, userUse);
      ArrayDeque<UserBalanceSlice> info = pInfo.get(userBalanceGroup);
      if (info == null) {
        info = new ArrayDeque<UserBalanceSlice>(samples + 1);
        pInfo.put(userBalanceGroup, info);
      }
      info.add(slice);
      while (info.size() > samples)
        info.pop();
    }
    
    // finally we need to calculate the new values for the dispatcher to read.
    
    // UserBalance, User, Percent Used
    DoubleMap<String, String, Double> computed = new DoubleMap<String, String, Double>();
    
    for (Entry<String, ArrayDeque<UserBalanceSlice>> entry : pInfo.entrySet()) {
      String userBalanceGroup = entry.getKey();
      DoubleOpMap<String> sums = new DoubleOpMap<String>();
      double allSlices = 0d;
      for (UserBalanceSlice slice : entry.getValue()) {
        allSlices += slice.pTotalSlices;
        for (Entry<String, Double> entry2 : slice.pUserSlotsUsed.entrySet()) {
          sums.apply(entry2.getKey(), entry2.getValue());
        }
      }
      if (allSlices == 0d)
        continue;
      for (String user : sums.keySet()) {
        sums.apply(user, allSlices, Op.Divide);
      }
      computed.put(userBalanceGroup, sums);
    }

    synchronized (pCurrentUsageLock) {
     pCurrentUsage = computed; 
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current usage information generated by the last call to 
   * {@link #calculateUsage()}.
   * <p>
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
   * Add a change to a host
   * 
   * @param hostName
   *   The name of the host.  Required.
   * 
   * @param enabled
   *   Whether the host is current enabled or <code>null</code> to not change the status of 
   *   the host.
   * 
   * @param balanceGroup
   *   The UserBalanceGroup associated with the host or <code>null</code> to not change the 
   *   group.  Since <code>null</code> is being used to indicate no change, the empty string 
   *   should be passed in to indicate the removal of the user balance group. 
   * 
   * @param numSlots
   *   The number of total job slots the host has or <code>null</code> to not change the number 
   *   of slots.
   */
  public void
  addHostChange
  (
    String hostName,
    Boolean enabled,
    String balanceGroup,
    Integer numSlots
  )
  {
     HostChange change = new HostChange(enabled, balanceGroup, numSlots);
     synchronized (pHostChangesLock) {
       pHostChanges.put(hostName, change);
     }
     LogMgr.getInstance().log
       (Kind.Usr, Level.Finest, 
        "Adding a host change for host (" + hostName + ") with the values " + change + ".");
  }
  
  /**
   * Add a change to a job.
   * <p>
   * All values are required to be not <code>null</code>.  The method is not doing any 
   * <code>null</code> checking, so all code that calls this method should be making sure that 
   * there are not any bad values being passed in.
   * <p>
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
    synchronized (pTimeLock) {
     long time = System.currentTimeMillis();
     if (start)
       change = new JobChange(author, jobID, time, null);
     else
       change = new JobChange(author, jobID, null, time);
     
     synchronized(pJobChangesLock) {
       pJobChanges.put(hostName, change);
     }
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
   * Set the number of samples that are being saved for each user balance group.
   * <p>
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
  /*   U S E R   B A L A N C E   S L I C E                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   *  A slice of user use of the queue in a particular user balance group.  
   */
  private
  class UserBalanceSlice
  {
    /*--------------------------------------------------------------------------------------*/
    /*   C O N S T R U C T O R                                                              */
    /*--------------------------------------------------------------------------------------*/

    /**
     * Constructor.
     */
    private
    UserBalanceSlice
    (
      Double totalSlices,
      DoubleOpMap<String> userSlotsUsed
    )
    {
      pTotalSlices = totalSlices;
      pUserSlotsUsed = userSlotsUsed;
    }
    
    private Double pTotalSlices;
    private DoubleOpMap<String> pUserSlotsUsed;
  }
  
  private
  class HostInfo
  {
    private
    HostInfo
    (
      boolean enabled,
      String userBalanceGroup,
      int numSlots
    )
    {
      pUserBalanceGroup = userBalanceGroup;
      pNumSlots = numSlots;
      pJobs = new TreeMap<Long, String>();
      pEnabled = enabled;
    }
    
    private boolean pEnabled;
    private String pUserBalanceGroup;
    private int pNumSlots;
    
    /**
     * JobID, UserName
     */
    private TreeMap<Long, String> pJobs;
  }
  
  /**
   * A change to the running state of job, either starting or stopping.
   */
  private
  class JobChange
  {
    /**
     * Constructor.
     * <p>
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
   * A change to a host.
   * <p>
   * If the host does not exist, then the host will be created.  Any of the values which are 
   * set to <code>null</code> will be ignored when applying the change to an existing host.
   */
  private
  class HostChange
  {
    /**
     * Constructor
     * 
     * @param enabled
     *   Is the host enabled?  This should be set to false any time the host has its status 
     *   changed from Enabled to something else (including Limbo).
     *   
     * @param userBalanceGroup
     *   The user balance group the host is in.
     *   
     * @param numSlots
     *   The current number of slots the host has.
     */
    private
    HostChange
    (
      Boolean enabled,
      String userBalanceGroup,
      Integer numSlots
    ) 
    {
      pEnabled = enabled;
      pUserBalanceGroup = userBalanceGroup;
      pNumSlots = numSlots;
    }
    
    @Override
    public String 
    toString()
    {
      return "Enabled: [" + pEnabled +"], UserBalanceGroup: [" + pUserBalanceGroup +"], " +
      	     "NumSlots [" + pNumSlots +"]";
    }
    
    private Boolean pEnabled;
    private String pUserBalanceGroup;
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
   * All the current slices.
   * <p>
   * New information is added to the front of this 
   */
  private TreeMap<String, ArrayDeque<UserBalanceSlice>> pInfo;
  
  /**
   * Cached information about what the hosts were doing last slice.
   */
  private TreeMap<String, HostInfo> pHostInfos;
  
  private long pSliceStart;
  private Object pTimeLock;
  
  private DoubleMap<String, String, Double> pCurrentUsage;
  private Object pCurrentUsageLock;
}
