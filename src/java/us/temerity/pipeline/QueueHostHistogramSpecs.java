// $Id: QueueHostHistogramSpecs.java,v 1.5 2010/01/08 23:15:38 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   H O S T   H I S T O G R A M   S P E C S                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The specifications for all job server host histograms.
 */
public
class QueueHostHistogramSpecs
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
  QueueHostHistogramSpecs()
  {}

  /**
   * Construct a new spec from individual histogram specifications.
   * 
   * @param statusSpec
   *   The histogram specification for server status.
   * 
   * @param osSpec 
   *   The histogram specification for operating system type.
   * 
   * @param loadSpec
   *   The histogram specification for system load.
   * 
   * @param memorySpec
   *   The histogram specification for free memory.
   * 
   * @param diskSpec
   *   The histogram specification for free temporary disk space.
   * 
   * @param numJobsSpec
   *   The histogram specification for the number of jobs running on the host.
   * 
   * @param slotsSpec
   *   The histogram specification for the number of job server slots. 
   * 
   * @param reserveSpec
   *   The histogram specification for server reservations. 
   * 
   * @param orderSpec
   *   The histogram specification for server dispatch order. 
   * 
   * @param selectionGroupsSpec
   *   The histogram specification for selection groups. 
   * 
   * @param selectionSchedsSpec
   *   The histogram specification for selection schedules. 
   * 
   * @param hardwareGroupsSpec
   *   The histogram specification for hardware groups.
   *   
   * @param dispatchControlsSpec
   *   The histogram specification for dispatch controls.
   *   
   * @param favorMethodsSpec
   *   The histogram specification for job group favor methods.
   *   
   * @param userBalanceGroups
   *   The histogram specification for user balance groups.
   */
  public 
  QueueHostHistogramSpecs
  (
   HistogramSpec statusSpec, 
   HistogramSpec osSpec, 
   HistogramSpec loadSpec, 
   HistogramSpec memorySpec, 
   HistogramSpec diskSpec, 
   HistogramSpec numJobsSpec,
   HistogramSpec slotsSpec, 
   HistogramSpec reserveSpec,   
   HistogramSpec orderSpec, 
   HistogramSpec selectionGroupsSpec, 
   HistogramSpec selectionSchedsSpec,
   HistogramSpec hardwareGroupsSpec,
   HistogramSpec dispatchControlsSpec,
   HistogramSpec favorMethodsSpec,
   HistogramSpec userBalanceGroups
  ) 
  {
    pStatus  = statusSpec; 
    pOsType  = osSpec;
    pLoad    = loadSpec; 
    pMemory  = memorySpec; 
    pDisk    = diskSpec; 
    pNumJobs = numJobsSpec; 
    pSlots   = slotsSpec; 
    pOrder   = orderSpec; 
    pReserve = reserveSpec; 
    pSelectionGroups   = selectionGroupsSpec; 
    pSelectionScheds   = selectionSchedsSpec; 
    pHardwareGroups    = hardwareGroupsSpec;
    pDispatchControls  = dispatchControlsSpec;
    pFavorMethods      = favorMethodsSpec;
    pUserBalanceGroups = userBalanceGroups;
  }

  /**
   * Construct job server histogram specifications from existing histograms. 
   */
  public 
  QueueHostHistogramSpecs
  (
   QueueHostHistograms hist
  ) 
  {
    pStatus  = new HistogramSpec(hist.getStatusHist()); 
    pOsType  = new HistogramSpec(hist.getOsTypeHist()); 
    pLoad    = new HistogramSpec(hist.getLoadHist()); 
    pMemory  = new HistogramSpec(hist.getMemoryHist()); 
    pDisk    = new HistogramSpec(hist.getDiskHist()); 
    pNumJobs = new HistogramSpec(hist.getNumJobsHist()); 
    pSlots   = new HistogramSpec(hist.getSlotsHist()); 
    pReserve = new HistogramSpec(hist.getReservationsHist()); 
    pOrder   = new HistogramSpec(hist.getOrderHist()); 
    pSelectionGroups   = new HistogramSpec(hist.getSelectionGroupsHist()); 
    pSelectionScheds   = new HistogramSpec(hist.getSelectionSchedulesHist());
    pHardwareGroups    = new HistogramSpec(hist.getHardwareGroupsHist());
    pDispatchControls  = new HistogramSpec(hist.getDispatchControlsHist());
    pFavorMethods      = new HistogramSpec(hist.getFavorMethodsHist());
    pUserBalanceGroups = new HistogramSpec(hist.getUserBalanceGroupsHist());
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a default specification.
   */
  public static QueueHostHistogramSpecs
  getDefault() 
  {
    HistogramSpec statusSpec = null;
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      for(QueueHostStatus status : QueueHostStatus.all())
	ranges.add(new HistogramRange(status));

      statusSpec = new HistogramSpec("ServerStatus", ranges);
    }

    HistogramSpec osSpec = null;
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      for(OsType os : OsType.all())
	ranges.add(new HistogramRange(os));

      osSpec = new HistogramSpec("ServerOS", ranges);
    }

    HistogramSpec loadSpec = null;
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      ranges.add(new FloatRange(0.0f, 0.5f));
      ranges.add(new FloatRange(0.5f, 1.0f));
      ranges.add(new FloatRange(1.0f, 1.5f));
      ranges.add(new FloatRange(1.5f, 2.0f));
      ranges.add(new FloatRange(2.0f, 3.0f));
      ranges.add(new FloatRange(3.0f, 4.0f));
      ranges.add(new FloatRange(4.0f, 6.0f));
      ranges.add(new FloatRange(6.0f, null));

      loadSpec = new HistogramSpec("SystemLoad", ranges);
    }

    HistogramSpec memorySpec = null;
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      ranges.add(new ByteSizeRange(0L, 67108864L));
      ranges.add(new ByteSizeRange(67108864L, 134217728L));
      ranges.add(new ByteSizeRange(134217728L, 536870912L));
      ranges.add(new ByteSizeRange(536870912L, 1073741824L));
      ranges.add(new ByteSizeRange(1073741824L, 1610612736L));
      ranges.add(new ByteSizeRange(1610612736L, 2147483648L));
      ranges.add(new ByteSizeRange(2147483648L, null));

      memorySpec = new HistogramSpec("FreeMemory", ranges);
    }

    HistogramSpec diskSpec = null;
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      ranges.add(new ByteSizeRange(0L, 134217728L));
      ranges.add(new ByteSizeRange(134217728L, 536870912L));
      ranges.add(new ByteSizeRange(536870912L, 1073741824L));
      ranges.add(new ByteSizeRange(1073741824L, 4294967296L));
      ranges.add(new ByteSizeRange(4294967296L, 17179869184L));
      ranges.add(new ByteSizeRange(17179869184L, 68719476736L));
      ranges.add(new ByteSizeRange(68719476736L, null));

      diskSpec = new HistogramSpec("FreeDisk", ranges);
    }

    HistogramSpec numJobsSpec = null;
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      ranges.add(new HistogramRange(0));
      ranges.add(new HistogramRange(1));
      ranges.add(new HistogramRange(2));
      ranges.add(new HistogramRange(3));
      ranges.add(new HistogramRange(4));
      ranges.add(new HistogramRange(5));
      ranges.add(new HistogramRange(6, null));

      numJobsSpec = new HistogramSpec("JobCounts", ranges);
    }
    
    HistogramSpec slotsSpec = null;
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      ranges.add(new HistogramRange(0));
      ranges.add(new HistogramRange(1));
      ranges.add(new HistogramRange(2));
      ranges.add(new HistogramRange(3));
      ranges.add(new HistogramRange(4));
      ranges.add(new HistogramRange(5));
      ranges.add(new HistogramRange(6, null));

      slotsSpec = new HistogramSpec("JobSlots", ranges);
    }

    HistogramSpec reserveSpec = null;
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      ranges.add(new HistogramRange(null, null));

      reserveSpec = new HistogramSpec("Reserved", ranges);
    }

    HistogramSpec orderSpec = null;
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      ranges.add(new HistogramRange(null, null));

      orderSpec = new HistogramSpec("Order", ranges);
    }

    HistogramSpec selectionGroupsSpec = null;
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      ranges.add(new HistogramRange(null, null));

      selectionGroupsSpec = new HistogramSpec("SelectionGroups", ranges);
    }
    
    HistogramSpec selectionSchedsSpec = null;
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      ranges.add(new HistogramRange(null, null));

      selectionSchedsSpec = new HistogramSpec("SelectionSchedules", ranges);
    }
    
    HistogramSpec hardwareGroupsSpec = null;
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      ranges.add(new HistogramRange(null, null));

      hardwareGroupsSpec = new HistogramSpec("HardwareGroups", ranges);
    }
    
    HistogramSpec dispatchControlsSpec = null;
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      ranges.add(new HistogramRange(null, null));

      dispatchControlsSpec = new HistogramSpec("DispatchControls", ranges);
    }
    
    HistogramSpec favorMethodsSpec = null;
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      for(JobGroupFavorMethod favor : JobGroupFavorMethod.values())
        ranges.add(new HistogramRange(favor));

      favorMethodsSpec = new HistogramSpec("FavorMethods", ranges);
    }
    
    HistogramSpec userBalanceGroupsSpec = null;
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      ranges.add(new HistogramRange(null, null));

      userBalanceGroupsSpec = new HistogramSpec("UserBalance", ranges);
    }
    
    return new QueueHostHistogramSpecs
      (statusSpec, osSpec, 
       loadSpec, memorySpec, diskSpec, numJobsSpec, slotsSpec, 
       reserveSpec, orderSpec, selectionGroupsSpec, selectionSchedsSpec, hardwareGroupsSpec,
       dispatchControlsSpec, favorMethodsSpec, userBalanceGroupsSpec);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether all or none of the categories from all histograms are included in the 
   * matching set. 
   */ 
  public boolean 
  allIncluded() 
  {
    return (pStatus.allIncluded() && 
            pOsType.allIncluded() && 
            pLoad.allIncluded() && 
            pMemory.allIncluded() && 
            pDisk.allIncluded() && 
            pNumJobs.allIncluded() && 
            pSlots.allIncluded() && 
            pOrder.allIncluded() && 
            pReserve.allIncluded() && 
            pSelectionGroups.allIncluded() && 
            pSelectionScheds.allIncluded() &&
            pHardwareGroups.allIncluded() &&
            pDispatchControls.allIncluded() &&
            pFavorMethods.allIncluded() &&
            pUserBalanceGroups.allIncluded()); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the histogram specification for server status.
   */ 
  public HistogramSpec
  getStatusSpec() 
  {
    return pStatus;
  }
  
  /**
   * Set the histogram specification for server status.
   */ 
  public void 
  setStatusSpec
  (
   HistogramSpec spec
  ) 
  {
    pStatus = spec;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the histogram specification for operating system type.
   */ 
  public HistogramSpec
  getOsTypeSpec() 
  {
    return pOsType; 
  }
  
  /**
   * Set the histogram specification for operating system type.
   */ 
  public void 
  setOsTypeSpec
  (
   HistogramSpec spec
  ) 
  {
    pOsType = spec;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the histogram specification for system load.
   */ 
  public HistogramSpec
  getLoadSpec() 
  {
    return pLoad; 
  }

  /**
   * Set the histogram specification for system load.
   */ 
  public void 
  setLoadSpec
  (
   HistogramSpec spec
  ) 
  {
    pLoad = spec;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the histogram specification for free memory.
   */ 
  public HistogramSpec
  getMemorySpec() 
  {
    return pMemory; 
  }

  /**
   * Set the histogram specification for free memory.
   */ 
  public void 
  setMemorySpec
  (
   HistogramSpec spec
  ) 
  {
    pMemory = spec;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the histogram specification for free temporary disk space.
   */ 
  public HistogramSpec
  getDiskSpec() 
  {
    return pDisk; 
  }

  /**
   * Set the histogram specification for free temporary disk space.
   */ 
  public void 
  setDiskSpec
  (
   HistogramSpec spec
  ) 
  {
    pDisk = spec;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the histogram specification for number of jobs running on the host.
   */ 
  public HistogramSpec
  getNumJobsSpec() 
  {
    return pNumJobs; 
  }

  /**
   * Set the histogram specification for the number of jobs running on the host.
   */ 
  public void 
  setNumJobsSpec
  (
   HistogramSpec spec
  ) 
  {
    pNumJobs = spec;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the histogram specification for the the number of job server slots. 
   */ 
  public HistogramSpec
  getSlotsSpec() 
  {
    return pSlots; 
  }

  /**
   * Set the histogram specification for the number of job server slots. 
   */ 
  public void 
  setSlotsSpec
  (
   HistogramSpec spec
  ) 
  {
    pSlots = spec;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the histogram specification for the server reservations. 
   */ 
  public HistogramSpec
  getReservationSpec() 
  {
    return pReserve; 
  }

  /**
   * Set the histogram specification for the server reservations. 
   */ 
  public void 
  setReservationSpec
  (
   HistogramSpec spec
  ) 
  {
    pReserve = spec;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the histogram specification for server dispatch order. 
   */ 
  public HistogramSpec
  getOrderSpec() 
  {
    return pOrder; 
  }

  /**
   * Set the histogram specification for server dispatch order. 
   */ 
  public void 
  setOrderSpec
  (
   HistogramSpec spec
  ) 
  {
    pOrder = spec;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the histogram specification for selection groups. 
   */ 
  public HistogramSpec
  getSelectionGroupsSpec() 
  {
    return pSelectionGroups; 
  }

  /**
   * Set the histogram specification for selection groups. 
   */ 
  public void 
  setSelectionGroupsSpec
  (
   HistogramSpec spec
  ) 
  {
    pSelectionGroups = spec;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the histogram specification for selection schedules. 
   */ 
  public HistogramSpec
  getSelectionSchedulesSpec() 
  {
    return pSelectionScheds; 
  }

  /**
   * Set the histogram specification for selection schedules. 
   */ 
  public void 
  setSelectionSchedulesSpec
  (
   HistogramSpec spec
  ) 
  {
    pSelectionScheds = spec;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the histogram specification for hardware groups. 
   */ 
  public HistogramSpec
  getHardwareGroupsSpec() 
  {
    return pHardwareGroups; 
  }

  /**
   * Set the histogram specification for hardware groups. 
   */ 
  public void 
  setHardwareGroupsSpec
  (
   HistogramSpec spec
  ) 
  {
    pHardwareGroups = spec;
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the histogram specification for dispatch controls. 
   */ 
  public HistogramSpec
  getDispatchControlsSpec() 
  {
    return pDispatchControls; 
  }

  /**
   * Set the histogram specification for dispatch controls. 
   */ 
  public void 
  setDispatchControlsSpec
  (
   HistogramSpec spec
  ) 
  {
    pDispatchControls = spec;
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the histogram specification for job group favor methods. 
   */ 
  public HistogramSpec
  getFavorMethodsSpec() 
  {
    return pFavorMethods; 
  }

  /**
   * Set the histogram specification for job group favor methods. 
   */ 
  public void 
  setFavorMethodsSpec
  (
   HistogramSpec spec
  ) 
  {
    pFavorMethods = spec;
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the histogram specification for user balance groups. 
   */ 
  public HistogramSpec
  getUserBalanceGroupsSpec() 
  {
    return pUserBalanceGroups; 
  }

  /**
   * Set the histogram specification for user balance groups. 
   */ 
  public void 
  setUserBalanceGroupsSpec
  (
   HistogramSpec spec
  ) 
  {
    pUserBalanceGroups = spec;
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
    encoder.encode("SelectionGroups", pSelectionGroups);
    encoder.encode("SelectionScheds", pSelectionScheds);
    encoder.encode("HardwareGroups",  pHardwareGroups);
    encoder.encode("DispatchControls", pDispatchControls);
    encoder.encode("FavorMethods", pFavorMethods);
    encoder.encode("UserBalanceGroups", pUserBalanceGroups);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    pStatus  = (HistogramSpec) decoder.decode("Status"); 
    pOsType  = (HistogramSpec) decoder.decode("OsType");   
    pLoad    = (HistogramSpec) decoder.decode("Load");     
    pMemory  = (HistogramSpec) decoder.decode("Memory"); 
    pDisk    = (HistogramSpec) decoder.decode("Disk");   
    pNumJobs = (HistogramSpec) decoder.decode("NumJobs");   
    pSlots   = (HistogramSpec) decoder.decode("Slots");   
    pReserve = (HistogramSpec) decoder.decode("Reservations");   
    pOrder   = (HistogramSpec) decoder.decode("Order");   
    pSelectionGroups = (HistogramSpec) decoder.decode("SelectionGroups");   
    pSelectionScheds = (HistogramSpec) decoder.decode("SelectionScheds");  
    pHardwareGroups  = (HistogramSpec) decoder.decode("HardwareGroups");
    pDispatchControls = (HistogramSpec) decoder.decode("DispatchControls");
    pFavorMethods     = (HistogramSpec) decoder.decode("FavorMethods");
    pUserBalanceGroups = (HistogramSpec) decoder.decode("UserBalanceGroups");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2574447696399334072L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The histogram specifications.
   */
  private HistogramSpec  pStatus; 
  private HistogramSpec  pOsType; 
  private HistogramSpec  pLoad; 
  private HistogramSpec  pMemory; 
  private HistogramSpec  pDisk; 
  private HistogramSpec  pNumJobs; 
  private HistogramSpec  pSlots;
  private HistogramSpec  pOrder;
  private HistogramSpec  pReserve;
  private HistogramSpec  pSelectionGroups;
  private HistogramSpec  pSelectionScheds;  
  private HistogramSpec  pHardwareGroups;
  private HistogramSpec  pDispatchControls;
  private HistogramSpec  pFavorMethods;
  private HistogramSpec  pUserBalanceGroups;

}

