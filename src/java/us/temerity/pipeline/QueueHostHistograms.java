// $Id: QueueHostHistograms.java,v 1.1 2006/12/05 18:23:30 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   H O S T   H I S T O G R A M S                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The frequency distribution data for significant catagories of information shared 
 * by all job server hosts.
 */
public
class QueueHostHistograms
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public
  QueueHostHistograms()
  {}

  /**
   * Construct job server histograms from the given specifications.
   */
  public 
  QueueHostHistograms
  (
   QueueHostHistogramSpecs specs
  ) 
  {
    pStatus  = new Histogram(specs.getStatusSpec()); 
    pOsType  = new Histogram(specs.getOsTypeSpec()); 
    pLoad    = new Histogram(specs.getLoadSpec()); 
    pMemory  = new Histogram(specs.getMemorySpec()); 
    pDisk    = new Histogram(specs.getDiskSpec()); 
    pNumJobs = new Histogram(specs.getNumJobsSpec()); 
    pSlots   = new Histogram(specs.getSlotsSpec()); 
    pReserve = new Histogram(specs.getReservationSpec()); 
    pOrder   = new Histogram(specs.getOrderSpec()); 
    pGroups  = new Histogram(specs.getGroupsSpec()); 
    pScheds  = new Histogram(specs.getSchedulesSpec()); 
  }




  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the histogram of server status.
   */ 
  public Histogram
  getStatusHist() 
  {
    return pStatus;
  }
  
  /**
   * Get the histogram of operating system type.
   */ 
  public Histogram
  getOsTypeHist() 
  {
    return pOsType; 
  }
  
  /**
   * Get the histogram of system load.
   */ 
  public Histogram
  getLoadHist() 
  {
    return pLoad; 
  }
  
  /**
   * Get the histogram of free memory.
   */ 
  public Histogram
  getMemoryHist() 
  {
    return pMemory; 
  }
  
  /**
   * Get the histogram of free temporary disk space.
   */ 
  public Histogram
  getDiskHist() 
  {
    return pDisk; 
  }

  /**
   * Get the histogram of the number of jobs running on the host.
   */ 
  public Histogram
  getNumJobsHist() 
  {
    return pNumJobs;
  }

  /**
   * Get the histogram of the number of job server slots. 
   */ 
  public Histogram
  getSlotsHist() 
  {
    return pSlots;
  }
  
  /**
   * Get the histogram of server reservations. 
   */ 
  public Histogram
  getReservationsHist() 
  {
    return pReserve;
  }
  
  /**
   * Get the histogram of server dispatch order. 
   */ 
  public Histogram
  getOrderHist() 
  {
    return pOrder;
  }
  
  /**
   * Get the histogram of selection groups. 
   */ 
  public Histogram
  getGroupsHist() 
  {
    return pGroups;
  }
  
  /**
   * Get the histogram of selection schedules. 
   */ 
  public Histogram
  getSchedulesHist() 
  {
    return pScheds;
  }
  
  



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Clear all catagory counts.
   */ 
  public void 
  clearCounts()
  {
    pStatus.clearCounts();
    pOsType.clearCounts();
    pLoad.clearCounts();
    pMemory.clearCounts();
    pDisk.clearCounts();
    pNumJobs.clearCounts();
    pSlots.clearCounts();
    pReserve.clearCounts();
    pOrder.clearCounts();
    pGroups.clearCounts();
    pScheds.clearCounts();
  }

  /**
   * Increment the item counts of all histograms based on the given job server information.
   */ 
  public void 
  catagorize
  (
   QueueHostInfo qinfo
  ) 
  {
    pStatus.catagorize(qinfo.getStatus());

    OsType os = qinfo.getOsType(); 
    if(os != null) 
      pOsType.catagorize(os);

    ResourceSample sample = qinfo.getLatestSample(); 
    if(sample != null) {
      pLoad.catagorize(sample.getLoad());  
      pMemory.catagorize(sample.getMemory());  
      pDisk.catagorize(sample.getDisk());  
      pNumJobs.catagorize(sample.getNumJobs());        
    }

    pSlots.catagorize(qinfo.getJobSlots()); 

    {
      String res = qinfo.getReservation();
      if(res == null) 
	res = "-";
      pReserve.catagorize(res); 
    }

    pOrder.catagorize(qinfo.getOrder()); 

    {
      String group = qinfo.getSelectionGroup();
      if(group == null) 
	group = "-";
      pGroups.catagorize(group); 
    }

    {
      String sched = qinfo.getSelectionSchedule();
      if(sched == null) 
	sched = "-";
      pScheds.catagorize(sched); 
    }
  }


    
  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    encoder.encode("Status", pStatus);
    encoder.encode("OsType", pOsType);
    encoder.encode("Load", pLoad);
    encoder.encode("Memory", pMemory);
    encoder.encode("Disk", pDisk);
    encoder.encode("NumJobs", pNumJobs);
    encoder.encode("Slots", pSlots);
    encoder.encode("Reservations", pReserve);
    encoder.encode("Order", pOrder);
    encoder.encode("Groups", pGroups);
    encoder.encode("Schedules", pScheds);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    pStatus  = (Histogram) decoder.decode("Status"); 
    pOsType  = (Histogram) decoder.decode("OsType");   
    pLoad    = (Histogram) decoder.decode("Load"); 
    pMemory  = (Histogram) decoder.decode("Memory"); 
    pDisk    = (Histogram) decoder.decode("Disk"); 
    pNumJobs = (Histogram) decoder.decode("NumJobs"); 
    pSlots   = (Histogram) decoder.decode("Slots");   
    pReserve = (Histogram) decoder.decode("Reservations");   
    pOrder   = (Histogram) decoder.decode("Order");   
    pGroups  = (Histogram) decoder.decode("Groups");   
    pScheds  = (Histogram) decoder.decode("Schedules");   
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2574447696399334072L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The histograms.
   */
  private Histogram  pStatus; 
  private Histogram  pOsType; 
  private Histogram  pLoad; 
  private Histogram  pMemory; 
  private Histogram  pDisk; 
  private Histogram  pNumJobs; 
  private Histogram  pSlots;
  private Histogram  pOrder;
  private Histogram  pReserve;
  private Histogram  pGroups;
  private Histogram  pScheds;

}

