package com.radar.pipeline.plugin.RadarMaya2MRCollection.v2_3_2;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BaseUtil.ParamMapping;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.DefaultProjectNames;

/*------------------------------------------------------------------------------------------*/
/*   R A D A R   P R O J E C T   N A M E                                                    */
/*------------------------------------------------------------------------------------------*/


/**
 * Basic naming class for Radar scripts.
 * <p>
 * Note, due to the fact this class overrides the DefaultProjectNames class,
 * the name of this Namer class will appear as (DefaultProjectNames).  That is
 * the name that has to be used if specifying parameters on the commandline.
 */
public 
class RadarProjectNames
  extends DefaultProjectNames
{
  public RadarProjectNames
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient
  )
    throws PipelineException 
  {
    super(mclient, qclient);
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  
  @SuppressWarnings("unused")
  @Override
  public void generateNames() 
    throws PipelineException
  {
    setContext((UtilContext) getParamValue(aUtilContext));
    
    pProject = getStringParamValue(new ParamMapping(aProjectName));
    
    Path startPath = new Path("/projects/" + pProject);
    {
      Path scriptsPath =  new Path(startPath, "projectScripts");
      pMelScriptPath =  new Path(scriptsPath, "mel");
      pRenderScriptPath = new Path(scriptsPath, "render");
      pMelPlaceHolderScriptPath =  new Path(pMelScriptPath, "gen");
      pMelVerifyScriptPath = new Path(pMelScriptPath, "verify");
      pMelScriptSourcePath = new Path(pMelScriptPath, "source");
    }
    
    {
      Path globalsPath = new Path(startPath, "globals");
      pMayaGlobalsPath = new Path(globalsPath, "maya");
      pMRayGlobalsPath = new Path(globalsPath, "mray");
    }
    
    {
      Path setupPath = new Path(startPath, "projectSetups");
      pTurntablePath = new Path(setupPath, "tt");
    }
    done();
  }
  private static final long serialVersionUID = 4916962629028568427L;
}
