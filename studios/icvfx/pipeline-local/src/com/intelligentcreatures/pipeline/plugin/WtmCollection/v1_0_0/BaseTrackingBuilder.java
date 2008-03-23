// $Id: BaseTrackingBuilder.java,v 1.2 2008/03/23 05:09:58 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   T R A C K I N G   B U I L D E R                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Abstract builder base class that provides the functionality common to all tracking 
 * related builders. 
 */
public abstract
class BaseTrackingBuilder 
  extends BaseShotBuilder 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Provided to allow parent builders to create instances and share namers. 
   * 
   * @param name
   *   Name of the builder.
   * 
   * @param desc 
   *   A short description of the builder. 
   * 
   * @param mclient 
   *   The master manager connection.
   * 
   * @param qclient 
   *   The queue manager connection.
   * 
   * @param builderInfo
   *   Information that is shared among all builders in a given invocation.
   * 
   * @param studioDefs 
   *   Provides a set of studio-wide helpers for project, sequence and shot naming.
   * 
   * @param projectNamer
   *   Provides project-wide names of nodes and node directories.
   * 
   * @param shotNamer
   *   Provides the names of nodes and node directories which are shot specific.
   */ 
  protected
  BaseTrackingBuilder
  (
   String name,
   String desc,
   MasterMgrClient mclient,
   QueueMgrClient qclient,
   BuilderInformation builderInfo, 
   StudioDefinitions studioDefs,
   ProjectNamer projectNamer, 
   ShotNamer shotNamer
  )
    throws PipelineException
  {
    super(name, desc, mclient, qclient, builderInfo, studioDefs, 
	  projectNamer, shotNamer, TaskType.Tracking); 

    /* setup builder parameters */ 
    {
      /* selects the project, sequence and shot for the task */ 
      addLocationParam(); 
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E T U P   P A S S E S                                                              */
  /*----------------------------------------------------------------------------------------*/

  protected
  class BaseSetupTrackingEssentials
    extends SetupPass
  {
    public 
    BaseSetupTrackingEssentials()
    {
      super("Setup Tracking Essentials", 
	    "Lookup the names of nodes required by the tracking task."); 
    }

    public 
    BaseSetupTrackingEssentials
    (
     String name, 
     String description
    )
    {
      super(name, description); 
    }
   
    /**
     * Phase in which parameter values should be extracted from parameters and checked
     * for consistency and applicability.
     */
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      /* register the required (locked) nodes */ 
      pUndistorted2kPlateNodeName = pShotNamer.getUndistorted2kPlateNode(); 
      pRequiredNodeNames.add(pUndistorted2kPlateNodeName); 
    }
    
    private static final long serialVersionUID = 2821815874355053699L;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = 
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing the undistorted/linearized
   * ~2k plate images.
   */ 
  protected String pUndistorted2kPlateNodeName; 

}
