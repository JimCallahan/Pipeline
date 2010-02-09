// $Id: ProjectNamer.java,v 1.1 2008/05/26 03:19:50 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/*------------------------------------------------------------------------------------------*/
/*   P R O J E C T   N A M E R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Defines the names of nodes which are not connected to particular entities within the
 * project.
 */
public 
class ProjectNamer 
extends BaseNames 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  ProjectNamer
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation info
  )
    throws PipelineException
  {
    super("ProjectNamer", 
          "The basic naming class for project specific files.",
          mclient,
          qclient,
          info);
    {
      UtilityParam param =
        new StringUtilityParam
        (ParamNames.aProjectName,
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
    
    pProject = getStringParamValue(new ParamMapping(ParamNames.aProjectName));
    
    Path scriptPath = StudioDefinitions.getScriptPath(pProject);
    Path templatePath = StudioDefinitions.getTemplatePath(pProject);
    
    pScriptPaths = new DoubleMap<Department, ScriptType, Path>();
    pTemplatePaths = new TreeMap<Department, Path>();
    for (Department department : Department.values()) {
      Path scrPath = new Path(scriptPath, department.toString() );
      for (ScriptType type : ScriptType.values()) {
        Path finalPath = new Path(scrPath, type.toString());
        pScriptPaths.put(department, type, finalPath);
      }
      Path temPath = new Path(templatePath, department.toString() );
      pTemplatePaths.put(department, temPath);
    }
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  public String
  getProjectName()
  {
    return pProject;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   G E N E R A L                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The MEL script used to make turntable setups where the camera sweeps in a circle around
   * a central point.
   */
  public String
  getCircleTurntableMEL()
  {
    return new Path(pScriptPaths.get(Department.general, ScriptType.place), 
                    "circleTT").toString();
  }
  
  /**
   * The MEL script used to make turntable setups where the camera sits at the origin and
   * rotates in place. 
   */
  public String
  getCenterTurntableMEL()
  {
    return new Path(pScriptPaths.get(Department.general, ScriptType.place), 
                    "centerTT").toString();
  }
  
  /**
   * The MEL script that removes all references from a Maya scene file.
   */
  public String
  getRemoveReferenceMEL()
  {
    return new Path(pScriptPaths.get(Department.general, ScriptType.mel), 
                    "removeRefs").toString();
  }
  
  /**
   * The MEL script used to verify the correctness of a simple asset file.
   */
  public String
  getAssetVerificationMEL()
  {
    return new Path(pScriptPaths.get(Department.general, ScriptType.mel), 
                    "assetVerify").toString();  
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   M O D E L                                                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The MEL script used to verify the correctness of a model file.
   */
  public String
  getModelVerificationMEL()
  {
    return new Path(pScriptPaths.get(Department.model, ScriptType.mel), 
                    "modelVerify").toString();  
  }
  
  /**
   * The MEL script used to generate a placeholder model.
   */
  public String
  getModelPlaceholderMEL()
  {
    return new Path(pScriptPaths.get(Department.model, ScriptType.place), 
                    "modelPlaceholder").toString();  
  }
  
  /**
   * The MEL script render globals used while rendering a turntable.
   */
  public String
  getModelGlobalsMEL()
  {
    return new Path(pScriptPaths.get(Department.model, ScriptType.render), 
                    "modelGlobals").toString();  
  }
  
  /**
   * The Maya scene used to generate the model turntable.
   */
  public String
  getModelTTSetup()
  {
    return new Path(pTemplatePaths.get(Department.model), "modelTT").toString();  
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   R I G                                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The MEL script run on the finished rig before it is sent to animation.
   */
  public String
  getRigFinalizeMEL
  (
    AssetType type  
  )
  {
    return new Path(pScriptPaths.get(Department.rig, ScriptType.mel), 
                    "finalizeRig_" + type.toString()).toString();
  }
  
  /**
   * The MEL script used to verify the correctness of a rig file.
   */
  public String
  getRigVerificationMEL()
  {
    return new Path(pScriptPaths.get(Department.rig, ScriptType.mel), 
                    "rigVerify").toString();  
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S H A D E                                                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The MEL script run on the shaded character before it is sent to lighting.
   */
  public String
  getShadeFinalizeMEL
  (
    AssetType type  
  )
  {
    return new Path(pScriptPaths.get(Department.shade, ScriptType.mel), 
                    "finalizeShade_" + type.toString()).toString();
  }
  
  /**
   * The MEL script run on the shading scene before shaders are exported.
   */
  public String
  getShadeVerificationMEL()
  {
    return new Path(pScriptPaths.get(Department.shade, ScriptType.mel), 
                    "shadeVerify").toString();  
  }
  
  /**
   * The MEL script run on the shading scene before shaders are exported.
   */
  public String
  getShaderCopyMEL()
  {
    return new Path(pScriptPaths.get(Department.shade, ScriptType.mel), 
                    "shaderCopy").toString();  
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   C A M E R A                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The MEL script used to generate a default camera.
   */
  public String
  getCameraPlaceholderMEL()
  {
    return new Path(pScriptPaths.get(Department.general, ScriptType.place), 
                    "cameraPlaceholder").toString();  
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   L I G H T I N G                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the Maya Render Globals for lighting tests.
   */
  public String
  getLightingMayaGlobalsMEL()
  {
    return new Path(pScriptPaths.get(Department.lgt, ScriptType.render),
                    "mayaGlobals").toString();
  }

  /**
   * Get the Mental Ray Render Globals for lighting tests.
   */
  public String
  getLightingMRayGlobalsMEL()
  {
    return new Path(pScriptPaths.get(Department.lgt, ScriptType.render),
                    "mrayGlobals").toString(); 
  }
  
  /**
   * Get the script used to build the lighting product scene, by importing all
   * the references except the characters.
   */
  public String
  getLightingProductMEL()
  {
    return new Path(pScriptPaths.get(Department.lgt, ScriptType.mel),
                    "lgtProduct").toString(); 
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  S T A T I C   H E L P E R S                                                           */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Build a generated ProjectNamer with the pieces passed in.
   * 
   * @param mclient
   *   The Master manager instance to build the Namer with.
   * 
   * @param qclient
   *   The Queue manager instance to build the Namer with.
   * 
   * @param info
   *   The Builder Information instance to build the Namer with.
   * 
   * @param project
   *   The name of the project
   */
  public static ProjectNamer
  getGeneratedNamer
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation info,
    String project
  )
    throws PipelineException
  {
    ProjectNamer namer = new ProjectNamer(mclient, qclient, info);
    namer.setParamValue(ParamNames.aProjectName, project);
    namer.run();
    return namer;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -3877941377789664957L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private String pProject;
  
  private DoubleMap<Department, ScriptType, Path> pScriptPaths;
  
  private TreeMap<Department, Path> pTemplatePaths;
}
