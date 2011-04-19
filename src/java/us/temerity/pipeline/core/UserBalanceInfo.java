// $Id: UserBalanceInfo.java,v 1.10 2009/12/16 04:13:33 jesse Exp $

package us.temerity.pipeline.core;

import java.util.*;
import java.util.Map.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

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
    pSampleStart = System.currentTimeMillis();
    pCachedSampleStart = pSampleStart;
    
    pHostInfos = new TreeMap<String, HostInfo>();
    
    pHostChanges = new MappedLinkedList<String, HostChange>();
    pJobChanges = new MappedLinkedList<String, JobChange>();
    pGroupChanges = new LinkedList<GroupChange>();
    
    pBalanceGroups = new TreeSet<String>();
    
    pHostChangesLock = new Object();
    pJobChangesLock = new Object();
    pGroupChangesLock = new Object();
    pCurrentUsageLock = new ReentrantLock();
    
    pSamplesToKeep = new AtomicInteger(30);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C A L C U L A T E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Calculate the actual usage of the queue during the last interval.
   * 
   * @param timer
   *   The timer.
   */
  public void
  calculateUsage
  (
    TaskTimer timer  
  )
  {
    MappedLinkedList<String, HostChange> hostChanges;
    MappedLinkedList<String, JobChange> jobChanges;
    LinkedList<GroupChange> groupChanges;
    long startTime;
    long endTime;
    double sampleDuration;
    
    timer.acquire();
    synchronized (pJobChangesLock) {
      timer.resume();
      
      startTime = pSampleStart;
      endTime = System.currentTimeMillis();
      pSampleStart = endTime + 1;
      sampleDuration = endTime - startTime;
      
      LogMgr.getInstance().log
        (Kind.Usr, Level.Finest, 
         "SampleStart: [" + startTime + "], SampleEnd: [" + endTime +"], " +
         "SampleDuration: [" + sampleDuration +"]");
      
      timer.acquire();
      synchronized (pHostChangesLock) {
        timer.resume();
        hostChanges = pHostChanges;
        pHostChanges = new MappedLinkedList<String, HostChange>();
      }
      
      timer.acquire();
      synchronized (pGroupChangesLock) {
        timer.resume();
        groupChanges = pGroupChanges;
        pGroupChanges = new LinkedList<GroupChange>();
      }
      
      jobChanges = pJobChanges;
      pJobChanges = new MappedLinkedList<String, JobChange>();
    }
    
    {
      for (GroupChange change : groupChanges) {
        if (change.pCreated)
          pBalanceGroups.add(change.pName);
        else
          pBalanceGroups.remove(change.pName);
      }
    }
    
    {
      timer.suspend();
      TaskTimer tm = new TaskTimer("User Balance Info [Apply Host Changes]");
      for (Entry<String, LinkedList<HostChange>> entry : hostChanges.entrySet()) {
        String hostName = entry.getKey();
        LinkedList<HostChange> changes = entry.getValue();
        HostInfo hostInfo = pHostInfos.get(hostName);
        if (hostInfo == null) {
          hostInfo = new HostInfo();
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
              String oldAuthor = info.pJobs.put(id, jchange.pAuthor); 
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

                double used = (jobEndTime - jobStartTime) / sampleDuration;
                
                LogMgr.getInstance().log
                (Kind.Usr, Level.Finest,
                  "Job (" + id +") used (" + (jobEndTime - jobStartTime) +") milliseconds " +
                   "of queue time during this sample");

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
              double time = endTime - startTimeByJob.get(jobID);
              LogMgr.getInstance().log
                (Kind.Usr, Level.Finest,
                 "Job (" + jobID +") used (" + time +") milliseconds " +
                 "of queue time during this sample");
              double used = (time) / sampleDuration;
              slotsPerUser.apply(author, used);
            }
            else {
              LogMgr.getInstance().log
                (Kind.Usr, Level.Finest,
                 "Job (" + jobID +") used (" + sampleDuration +") milliseconds " +
                 "of queue time during this sample");
              slotsPerUser.apply(author, 1d);
            }
          }
        }
      } // Finished looping through all the hosts.
      LogMgr.getInstance().logSubStage
        (LogMgr.Kind.Usr, LogMgr.Level.Finer,
         tm, timer);
    }
    
    /* Now we want to prune pSamples due to balance group destruction */
    {
      TreeSet<String> remove = new TreeSet<String>();
      for (String groupName : pSamples.keySet())
        if (!pBalanceGroups.contains(groupName)) {
          pSamples.remove(groupName);
          LogMgr.getInstance().log
            (Kind.Usr, Level.Finest, 
             "Removing all samples for deleted group (" + groupName+ ")");
        }
    }
    
    int numSamples = pSamplesToKeep.get();
    
    /* Now we add the newly created samples to the saved info. */
    {
      timer.suspend();
      TaskTimer tm = new TaskTimer("User Balance Info [Updating Samples]");
      for (String groupName : pBalanceGroups) {
        Double totalSlots = slotsPerBalanceGroup.get(groupName);
        if (totalSlots == null)
          totalSlots = 0d;
        DoubleOpMap<String> slotsPerUser = userSlotsPerBalanceGroup.get(groupName);
        if (slotsPerUser == null)
          slotsPerUser = new DoubleOpMap<String>();
        UserBalanceSample sample = new UserBalanceSample(totalSlots, slotsPerUser);
        
        LogMgr.getInstance().log
          (Kind.Usr, Level.Finest, 
           "User Balance Sample (" + groupName + "), " + sample);

        ArrayDeque<UserBalanceSample> samples = pSamples.get(groupName);
        if (samples == null) {
          samples = new ArrayDeque<UserBalanceSample>(numSamples + 1);
          pSamples.put(groupName, samples);
        }
        samples.addLast(sample);
        while (samples.size() > numSamples)
          samples.removeFirst();
      }
      LogMgr.getInstance().logSubStage
        (Kind.Usr, Level.Finer,
         tm, timer);
    }
    
    /* finally we need to calculate the new values for the dispatcher to read. */
    
    {
      timer.suspend();
      TaskTimer tm = new TaskTimer("User Balance Info [Calculating New Usage]");
      /* UserBalance, User, Percent Used */
      DoubleMap<String, String, Double> currentUsage = new DoubleMap<String, String, Double>();
      TreeMap<String, Double> slotWeight = new TreeMap<String, Double>();
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
        slotWeight.put(userBalanceGroup, 1/totalSlots);
      }
      
      tm.acquire();
      {
        pCurrentUsageLock.lock(); 
        tm.resume();
        pCurrentUsage = currentUsage;
        pSlotWeight = slotWeight;
        pCachedSampleStart = pSampleStart;
        pCurrentUsageLock.unlock();
      }
      LogMgr.getInstance().logSubStage
        (LogMgr.Kind.Usr, LogMgr.Level.Finer,
         tm, timer);
      
      LogMgr.getInstance().log
      (Kind.Usr, Level.Fine, 
       "Current Usage: " + currentUsage);
    }
    LogMgr.getInstance().logStage
      (LogMgr.Kind.Usr, LogMgr.Level.Fine,
       timer); 
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current usage information generated by the last call to 
   * {@link #calculateUsage(TaskTimer)}.<P>
   * 
   * This is a live data-structure (it is not copied before returning) so any code that uses
   * this should not modify its contents. <p>
   * 
   * {@link #lockCurrentUsage()} needs to be called before calling this method.
   * 
   * @return
   *   A {@link DoubleMap} with the first key being the user balance group, the second key
   *   being the user name, and the value being the percentage of that user balance groups's
   *   resources that the user has used.
   */
  public DoubleMap<String, String, Double>
  getActualShares()
  {
    synchronized (pCurrentUsageLock) {
      return pCurrentUsage;   
    }
  }
  
  /**
   * Get the estimated usage that getting a single slot in the queue will add to a user. <p>
   * 
   * This number is used to generate estimates of queue use in between balancer() runs. 
   * Balance groups that have had no slots assigned to them during the entire sample history 
   * will have <code>null</code> entries in this table. <p>
   * 
   * {@link #lockCurrentUsage()} needs to be called before calling this method.
   */
  public TreeMap<String, Double>
  getSlotWeight()
  {
    synchronized (pCurrentUsageLock) {
      return pSlotWeight;
    }
  }
  
  /*----------------------------------------------------------------------------------------*/
  
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
   * Add a change to a group. <p>
   * 
   * All values are required to be not null.
   * 
   * @param name
   *   The name of the balance group.
   *   
   * @param created
   *   If <code>true</code>, then the balance group was created.  Otherwise it was removed.
   */
  public void
  addGroupChange
  (
    String name,
    boolean created
  )
  {
    GroupChange change = new GroupChange(name, created);
    synchronized (pGroupChangesLock) {
     pGroupChanges.add(change); 
    }

    if (created)
      LogMgr.getInstance().log
        (Kind.Usr, Level.Finest, 
         "Adding the balance group (" + name + ").");
    else
      LogMgr.getInstance().log
        (Kind.Usr, Level.Finest, 
         "Removed the balance group (" + name + ").");
  }
  
  /*----------------------------------------------------------------------------------------*/
  
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
  
  /**
   * Get the time (in milliseconds) the current user balance sample started.<p>
   * 
   * {@link #lockCurrentUsage()} needs to be called before calling this method.
   */
  public long
  getSampleStart()
  {
    return pCachedSampleStart;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   L O C K I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Acquire the lock that protects the current usage information. <p>
   * 
   * This lock needs to be acquired before calling {@link #getActualShares()}, 
   * {@link #getSlotWeight()}, and {@link #getSampleStart()} if consistency between the 
   * results of those methods is needed.
   */
  public void 
  lockCurrentUsage()
  {
    pCurrentUsageLock.lock();
  }

  /**
   * Release the lock once data acquisition is done.
   */
  public void
  unlockCurrentUsage()
  {
    pCurrentUsageLock.unlock();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S U B - C L A S S E S                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   *  A sample of user use of the queue in a particular user balance group.  
   */
  private
  class UserBalanceSample
  {
    /*--------------------------------------------------------------------------------------*/
    /*   C O N S T R U C T O R                                                              */
    /*--------------------------------------------------------------------------------------*/

    /**
     * Constructor. <P>
     * 
     * @param totalSlots
     *   The total number of slots assigned to this user balance group during the sample.
     *   
     * @param userSlotsUsed
     *   The total number of slots (can be fractional) used by each user during the sample.
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
    
    @Override
    public String 
    toString()
    {
      return "TotalSlots: [" + pTotalSlots +"], UserSlotsUsed: " + pUserSlotsUsed;
    }
    
    private final Double pTotalSlots;
    private final DoubleOpMap<String> pUserSlotsUsed;
  }
  
  /**
   *  Cached information about a host from the last sample. 
   */
  private
  class HostInfo
  {
    /*--------------------------------------------------------------------------------------*/
    /*   C O N S T R U C T O R                                                              */
    /*--------------------------------------------------------------------------------------*/

    /**
     * Constructor.
     */
    private
    HostInfo()
    {
      pUserBalanceName = null;
      pNumSlots = 0;
      pIsEnabled = false;
      pJobs = new TreeMap<Long, String>();
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
     *   The user that owns the job.  This must be set when the change is a job start.  It is
     *   ignored in cases where it is a job end.
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
    
    public Long pStartTime;
    public Long pEndTime;
    public String pAuthor;
    public long pJobID;
  }
  
  /**
   * Used to track the creation and deletion of user balance groups.
   *
   */
  private
  class GroupChange
  {
    /*--------------------------------------------------------------------------------------*/
    /*   C O N S T R U C T O R                                                              */
    /*--------------------------------------------------------------------------------------*/
    
    /**
     * Constructor.
     * 
     * @param name
     *   The name of the balance group.
     *   
     * @param created
     *   If <code>true</code>, then the balance group was created.  Otherwise it was removed.
     */
    private 
    GroupChange
    (
      String name,
      boolean created
    )
    {
      pName = name;
      pCreated = created;
    }
    
    public String pName;
    public boolean pCreated;
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
    
    public Boolean pIsEnabled;
    public String pUserBalanceName;
    public Integer pNumSlots;
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
  
  private LinkedList<GroupChange> pGroupChanges;
  private Object pGroupChangesLock;
  
  
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
  
  /**
   * List of balance groups that currently exist.
   */
  private TreeSet<String> pBalanceGroups;
  
  private long pSampleStart;
  
  /**
   * UserBalanceGroup, User, Percent Usage 
   */
  private DoubleMap<String, String, Double> pCurrentUsage;
  private TreeMap<String, Double> pSlotWeight;
  private long pCachedSampleStart;
  private ReentrantLock pCurrentUsageLock;
}
