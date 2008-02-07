package com.theorphanage.pipeline.plugin.ZohanCollection.v1_0_0;

import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;


public 
class ProjectNames 
extends BaseNames 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  ProjectNames
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient
  )
    throws PipelineException
  {
    super("ProjectNames", 
          "The basic naming class for project specific files.",
          mclient,
          qclient);
    {
      UtilityParam param =
        new StringUtilityParam
        (aProjectName,
         "The Name of the Project the asset should live in", 
         null);
      addParam(param);
    }
  }

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  
  @Override
  public void 
  generateNames() 
    throws PipelineException
  {
    setContext((UtilContext) getParamValue(aUtilContext));
    
    pProject = getStringParamValue(new ParamMapping(aProjectName));
    
    Path startPath = StudioDefinitions.getStandardsPath(pProject);
    
    pStartPaths = new TreeMap<Discipline, Path>();
    for (Discipline discipline : Discipline.values()) {
      Path disPath = new Path(startPath, discipline.toString() );
        pStartPaths.put(discipline, disPath);
    }
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   R O T O                                                                              */
  /*----------------------------------------------------------------------------------------*/
  
  public String
  getRotoCompTemplateName()
  {
    return new Path(pStartPaths.get(Discipline.RotoPaint), "Comp_Template").toString();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O M P                                                                              */
  /*----------------------------------------------------------------------------------------*/
  
  public String
  getCompTemplateName()
  {
    return new Path(pStartPaths.get(Discipline.Comp), "Comp_Template").toString();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aProjectName = "ProjectName";

  private static final long serialVersionUID = 6275876096707928367L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  protected String pProject;
  
  private TreeMap<Discipline, Path> pStartPaths;
}
