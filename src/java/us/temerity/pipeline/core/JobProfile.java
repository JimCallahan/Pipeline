// $Id: JobProfile.java,v 1.2 2009/06/07 23:21:06 jim Exp $

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
   AdminPrivileges privs
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

    if((sample.getLoad() > pMaxLoad) ||
       (sample.getMemory() < pMinMemory) || 
       (sample.getDisk() < pMinDisk))
      return false;

    if((reservation != null) &&
       !(pAuthor.equals(reservation) || privs.isWorkGroupMember(pAuthor, reservation)))
      return false;

    return true;
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

