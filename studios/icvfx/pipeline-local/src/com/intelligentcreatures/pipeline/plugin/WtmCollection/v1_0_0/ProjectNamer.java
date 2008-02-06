// $Id: ProjectNamer.java,v 1.1 2008/02/06 07:21:06 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.BaseNames;
import us.temerity.pipeline.builder.UtilContext;

import java.util.TreeMap;

/*------------------------------------------------------------------------------------------*/
/*   P R O J E C T   N A M E R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */
public 
class ProjectNamer 
  extends BaseNames 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public ProjectNamer
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient
  )
    throws PipelineException
  {
    super("ProjectNamer", 
          "The basic naming class for project specific files.",
          mclient, qclient);

    {
      UtilityParam param =
        new StringUtilityParam
        (StudioDefinitions.aProjectName,
         "The Name of the Project the asset should live in", 
         null);
      addParam(param);
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  @SuppressWarnings("unused")
  @Override
  public void 
  generateNames() 
    throws PipelineException
  {
    /* initialize the local util context (author, view and toolset) */ 
    setContext((UtilContext) getParamValue(aUtilContext)); 
    
    /* lookup the namer's parameter values */ 
    pProjectName = getStringParamValue(new ParamMapping(StudioDefinitions.aProjectName));
  

    // ...

  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The short project name. 
   */ 
  public String 
  getProjectName() 
  {
    return pProjectName;
  }

  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2625175852662491653L;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The short name of the project.
   */ 
  protected String pProjectName;

}
