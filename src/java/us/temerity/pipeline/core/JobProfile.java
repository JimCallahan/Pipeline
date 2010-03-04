// $Id: JobProfile.java,v 1.5 2009/12/09 05:05:55 jesse Exp $

package us.temerity.pipeline.core;

import java.util.*; 

import us.temerity.pipeline.*;


/*------------------------------------------------------------------------------------------*/
/*   J O B   P R O F I L E                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The collection of information used to determine how to sort which jobs should run on a 
 * given host but is not dependent on factors which may change between slots.
 */ 
public class 
JobProfile
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new profile.
   * 
   * @param job 
   *   The job to profile. 
   * 
   * @param info
   *   Information about the current status of the job. 
   * 
   * @param selection
   *   The selection profile for this job.
   * 
   * @param hardware
   *   The hardware profile for this job.
   * 
   * @param toolsetSupports
   *   The OS typs supported by the toolset assigned to the job.
   */
  public
  JobProfile
  (
   QueueJob job, 
   QueueJobInfo info, 
   SelectionProfile selection, 
   HardwareProfile hardware, 
   Set<OsType> toolsetSupports
  ) 
  {    
    JobReqs jreqs = job.getJobRequirements();
    ActionAgenda jagenda = job.getActionAgenda();

    pSelectionProfile = selection;
    pHardwareProfile  = hardware;
    
    pPriority = jreqs.getPriority();
    pTimeStamp = info.getSubmittedStamp();

    for(OsType os : toolsetSupports) {
      switch(os) {
      case Unix:
        pHasUnixToolset = true;
        break;
        
      case Windows:
        pHasWindowsToolset = true;
        break;
        
      case MacOS:
        pHasMacOSToolset = true;
      }
    }

    for(OsType os : job.getAction().getSupports()) {
      switch(os) {
      case Unix:
        pHasUnixAction = true;
        break;
        
      case Windows:
        pHasWindowsAction = true;
        break;
        
      case MacOS:
        pHasMacOSAction = true;
      }
    }

    pAuthor = jagenda.getNodeID().getAuthor();
    
    pMaxLoad   = jreqs.getMaxLoad();
    pMinMemory = jreqs.getMinMemory();
    pMinDisk   = jreqs.getMinDisk();
    
    pJobGroupID = job.getJobGroupID();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the given host is eligible to run this job.
   */ 
  public boolean
  isEligible
  (
   ResourceSample sample, 
   OsType os, 
   String reservation,
   AdminPrivileges privs,
   TreeSet<String> usersOverMax, 
   boolean maxLoadEnabled
  )
  {
    switch(os) {
    case Unix:
      if(!pHasUnixToolset || !pHasUnixAction) 
        return false;
      break;

    case Windows:
      if(!pHasWindowsToolset || !pHasWindowsAction) 
        return false;
      break;

    case MacOS:
      if(!pHasMacOSToolset || !pHasMacOSAction) 
        return false;
    }      

    if((maxLoadEnabled && (sample.getLoad() > pMaxLoad)) ||
       (sample.getMemory() < pMinMemory) || 
       (sample.getDisk() < pMinDisk))
      return false;

    if((reservation != null) &&
       !(pAuthor.equals(reservation) || privs.isWorkGroupMember(pAuthor, reservation)))
      return false;
    
    if((usersOverMax != null) && usersOverMax.contains(pAuthor))
      return false;

    return true;
  }

  /**
   * Generate a messages suitable for the SEL logger which explains the reason why a 
   * job is eligible or not under this profile.
   */ 
  public String
  getEligibilityMsg
  ( 
   ResourceSample sample, 
   OsType os, 
   String reservation, 
   AdminPrivileges privs,
   TreeSet<String> usersOverMax
  )
  {
    boolean badToolset = false;
    boolean badAction  = false; 
    switch(os) {
    case Unix:
      if(!pHasUnixToolset) 
        badToolset = true;
      else if(!pHasUnixAction) 
        badAction = true;
      break;

    case Windows:
      if(!pHasWindowsToolset) 
        badToolset = true;
      else if(!pHasWindowsAction) 
        badAction = true;
      break;

    case MacOS:
      if(!pHasMacOSToolset) 
        badToolset = true;
      else if(!pHasMacOSAction) 
        badAction = true;
    }      

    if(badToolset) 
      return ("Host OS (" + os + ") not supported by job's toolset."); 

    if(badAction) 
      return ("Host OS (" + os + ") not supported by job's action."); 

    if(sample.getLoad() > pMaxLoad) 
      return ("System load (" + formatFloat(sample.getLoad()) + ") is greater than " +
              "required maximum (" + formatFloat(pMaxLoad) + ")."); 

    if(sample.getMemory() < pMinMemory) 
      return ("Free memory (" + formatLong(sample.getMemory()) + ") is less than required " + 
              "minimum (" +  formatLong(pMinMemory) + ")."); 

    if(sample.getDisk() < pMinDisk) 
      return ("Local disk space (" + formatLong(sample.getDisk()) + ") is less than " + 
              "required minimum (" +  formatLong(pMinDisk) + ")."); 

     if((reservation != null) &&
        !(pAuthor.equals(reservation) || privs.isWorkGroupMember(pAuthor, reservation))) 
       return ("Reservation (" + reservation + ") did not match job owner " + 
               "(" + pAuthor + ").");
    
     if (usersOverMax != null && usersOverMax.contains(pAuthor))
       return "The job owner (" + pAuthor + ") is over their Balance Group Max Share " +
       	      "for this machine.";

     return "QUALIFIED!"; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the selection profile for this job.
   */ 
  public SelectionProfile
  getSelectionProfile() 
  {
    return pSelectionProfile;
  }

  /**
   * Get the hardware profile for this job.
   */ 
  public HardwareProfile
  getHardwareProfile() 
  {
    return pHardwareProfile;
  }

  /**
   * Get the relative job priority (can be negative).
   */ 
  public int
  getPriority() 
  {
    return pPriority;
  }

  /**
   * Get the timestamp of when the job was submitted.
   */ 
  public long
  getTimeStamp() 
  {
    return pTimeStamp; 
  }
  
  /**
   * Get the id of the job group that this job belongs to.
   */
  public long
  getJobGroupID()
  {
    return pJobGroupID;
  }
  
  /**
   * Get the name of the user who submitted this job. 
   */
  public String
  getAuthor()
  {
    return pAuthor;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generates a formatted string representation of a floating point number.
   */ 
  private String
  formatFloat
  (
   float value
  ) 
  {
    return String.format("%1$.4f ", value);
  }

  
  /**
   * Generates a formatted string representation of a large integer number.
   */ 
  private String
  formatLong
  (
   Long value
  ) 
  {
    if(value < 1024) {
      return value.toString();
    }
    else if(value < 1048576) {
      double k = ((double) value) / 1024.0;
      return String.format("%1$.1fK", k);
    }
    else if(value < 1073741824) {
      double m = ((double) value) / 1048576.0;
      return String.format("%1$.1fM", m);
    }
    else {
      double g = ((double) value) / 1073741824.0;
      return String.format("%1$.1fG", g);
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The selection profile for this job.
   */ 
  private SelectionProfile pSelectionProfile;

  /**
   * The hardware profile for this job.
   */ 
  private HardwareProfile pHardwareProfile;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The relative job priority (can be negative).
   */ 
  private int pPriority;

  /**
   * The timestamp of when the job was submitted.
   */ 
  private long pTimeStamp; 
  
  /**
   * The id of the job group that this job belongs to.
   */
  private long pJobGroupID;

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether there exists a toolset with the name assigned to the job which supports
   * each of the OS types.
   */ 
  private boolean pHasUnixToolset; 
  private boolean pHasWindowsToolset; 
  private boolean pHasMacOSToolset; 

  /**
   * Whether the action assigned to the job supports each of the OS types.
   */ 
  private boolean pHasUnixAction; 
  private boolean pHasWindowsAction; 
  private boolean pHasMacOSAction; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the user which submitted the job.
   */ 
  private String pAuthor; 

  /**
   * The maximum allowable system load on an eligible host.
   */
  private float pMaxLoad;    
 
  /**
   * The minimum amount of free memory (in bytes) required on an eligible host.
   */      
  private long pMinMemory;  

  /**
   * The minimum amount of free temporary local disk space (in bytes) required on an 
   * eligible host.
   */       
  private long pMinDisk;           

}

