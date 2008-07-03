// $Id: ShotNamer.java,v 1.3 2008/07/03 19:52:48 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/*------------------------------------------------------------------------------------------*/
/*   S H O T   N A M E R                                                                    */
/*------------------------------------------------------------------------------------------*/


public 
class ShotNamer 
  extends BaseNames 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  ShotNamer
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient
  )
    throws PipelineException
  {
    super("ShotNamer", 
          "The basic naming class for shots in the Nathan Love Base Collection",
          mclient,
          qclient);
    
    {
      UtilityParam param =
        new StringUtilityParam
        (ParamNames.aProjectName,
         "The Name of the Project the shot is being added to.", 
         null);
      addParam(param);
    }

    {
      UtilityParam param =
        new StringUtilityParam
        (ParamNames.aSpotName,
         "The Spot the shot is being created in.",
         null);
      addParam(param);
    }
    {
      UtilityParam param =
        new StringUtilityParam
        (ParamNames.aShotName,
         "The name of the shot to be created.",
         null);
      addParam(param);
    }
    
    LayoutGroup group = new LayoutGroup("AssetInfo", "Information about the asset.", true);
    group.addEntry(aUtilContext);
    group.addEntry(ParamNames.aProjectName);
    group.addEntry(ParamNames.aSpotName);
    group.addEntry(ParamNames.aShotName);
    PassLayoutGroup layout = new PassLayoutGroup("AssetInfo", group);
    setLayout(layout);
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
    
    pStartPaths = new DoubleMap<Department, SubDir, Path>();

    pProject = getStringParamValue(new ParamMapping(ParamNames.aProjectName));
    pSpotName = getStringParamValue(new ParamMapping(ParamNames.aSpotName));
    pShotName = getStringParamValue(new ParamMapping(ParamNames.aShotName));
    
    pFullName = pSpotName + pShotName;  
    
    Path startPath = StudioDefinitions.getShotPath(pProject, pSpotName, pShotName);
    for (Department discipline : Department.values()) {
      Path disPath = new Path(startPath, discipline.toString() );
      for (SubDir dir : SubDir.values()) {
        Path finalPath = new Path(disPath, dir.toString());
        pStartPaths.put(discipline, dir, finalPath);
      }
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   G L O B A L S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  public String
  getProjectName()
  {
    return pProject;
  }
  
  public String
  getSpotName()
  {
    return pSpotName;
  }
  
  public String
  getShotName()
  {
    return pShotName;
  }
  
  public String
  getTaskName()
  {
    return pFullName;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A N I M A T I O N                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The scene where the animator works.
   */
  public String
  getAnimEditScene()
  {
    return new Path(pStartPaths.get(Department.anim, SubDir.edit), 
                    join(join(pFullName, "anim"), "edit")).toString();
  }
  
  /**
   * Node containing the exported animation curves for a specific asset.
   * 
   * @param assetName
   *   The name of the asset.
   *   
   * @param assetType
   *   The type of the asset.
   */
  public String
  getAnimCurveExportNode
  (
    String assetName,
    AssetType assetType
  )
  {
    return new Path(new Path(pStartPaths.get(Department.anim, SubDir.prepare), 
                    assetType.toTitle()), join(pFullName, assetName)).toString();
  }
  
  /**
   * The scene where the animation is reapplied to the models to verify its correctness.
   */
  public String
  getAnimVerifyScene()
  {
    return new Path(pStartPaths.get(Department.anim, SubDir.prepare), 
                    join(pFullName, "anim")).toString();
  }
  
  /**
   * Node containing the approved animation curves for a specific asset.
   * 
   * @param assetName
   *   The name of the asset.
   *   
   * @param assetType
   *   The type of the asset.
   */
  public String
  getAnimCurveProductNode
  (
    String assetName,
    AssetType assetType
  )
  {
    return new Path(new Path(pStartPaths.get(Department.anim, SubDir.product), 
                    assetType.toTitle()), join(pFullName, assetName)).toString();
  }
  
  /**
   * The node an artist uses to submit the animation task.
   */
  public String
  getAnimSubmitNode()
  {
    return new Path(pStartPaths.get(Department.anim, SubDir.submit), 
                    join(join(pFullName, "anim"), aSubmit)).toString();
  }
  
  /**
   * The node a supervisor uses to approve the animation task.
   */
  public String
  getAnimApproveNode()
  {
    return new Path(pStartPaths.get(Department.anim, SubDir.approve), 
                    join(join(pFullName, "anim"), aApprove)).toString();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   L I G H T I N G                                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The generated scene which combines the approved animation with the lighting models.
   */
  public String
  getPreLightScene()
  {
    return new Path(new Path(pStartPaths.get(Department.lgt, SubDir.edit), "src"),
                             join(pFullName, "preLgt")).toString();
  }
  
  /**
   * The grouping node for all the textures being used in the lighting scene.
   */
  public String
  getLightingTextureNode()
  {
    return new Path(new Path(pStartPaths.get(Department.lgt, SubDir.edit), "src"),
                             join(pFullName, "tex")).toString();
  }
  
  /**
   * The scene where the artist lights.
   */
  public String
  getLightingEditScene()
  {
    return new Path(pStartPaths.get(Department.lgt, SubDir.edit),
                    join(join(pFullName, "lgt"), "edit")).toString();
  }
  
  /**
   * The render of the lighting scene used to approve the lighting.
   */
  public String
  getLightingRenderNode()
  {
    return new Path(pStartPaths.get(Department.lgt, SubDir.focus),
                    join(join(pFullName, "lgt"), "render")).toString();
  }
  
  /**
   * The final lighting scene passed onto rendering.
   */
  public String
  getLightingProductScene()
  {
    return new Path(pStartPaths.get(Department.lgt, SubDir.product),
                    join(pFullName, "lgt")).toString();
  }
  
  /**
   * The final texture node passed onto rendering.
   */
  public String
  getLightingTextureProductNode()
  {
    return new Path(pStartPaths.get(Department.lgt, SubDir.product),
                    join(pFullName, "tex")).toString();
  }
  
  
  /**
   * The node an artist uses to submit the lighting task.
   */
  public String
  getLightingSubmitNode()
  {
    return new Path(pStartPaths.get(Department.lgt, SubDir.submit), 
                    join(join(pFullName, "lgt"), aSubmit)).toString();
  }
  
  /**
   * The node a supervisor uses to approve the lighting task.
   */
  public String
  getLightingApproveNode()
  {
    return new Path(pStartPaths.get(Department.lgt, SubDir.approve), 
                    join(join(pFullName, "lgt"), aApprove)).toString();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The name of a render image node.
   * 
   * @param layerName
   *   The name of the layer that the render belongs to.  Layers are physical breakdowns of
   *   elements, so this might be something like foreground or background.  This can be set
   *   to <code>null</code> if there is no layer break out.
   * @param passName
   *   The name of the pass that the render belongs to.  Passes are different looks of the 
   *   same physical elements, so these might include beauty, specular, matte passes, 
   *   occlusion, etc.  This can be set to <code>null</code> if there are no passes being used.
   */
  public String
  getRenderingImageNode
  (
    String layerName,
    String passName  
  )
  {
    Path startPath = pStartPaths.get(Department.render, SubDir.focus);
    String filename = null;
    if (layerName != null) {
      startPath = new Path(startPath, layerName);
      filename = layerName;
    }
    if (passName != null) {
      startPath = new Path(startPath, passName);
      if (filename != null)
        filename = join(filename, passName);
      else
        filename = passName;
    }
    
    if (filename != null)
      filename = join(pFullName, filename);
    else
      filename = pFullName;
    
    startPath = new Path(startPath, filename);
    
    return startPath.toString();
  }
  
  /**
   * The name of a render scene.
   * 
   * @param layerName
   *   The name of the layer that the scene belongs to.  Layers are physical breakdowns of
   *   elements, so this might be something like foreground or background.  This can be set
   *   to <code>null</code> if there is no layer break out.
   * @param passName
   *   The name of the pass that the scene belongs to.  Passes are different looks of the 
   *   same physical elements, so these might include beauty, specular, matte passes, 
   *   occlusion, etc.  This can be set to <code>null</code> if there are no passes being used.
   */
  public String
  getRenderingScene
  (
    String layerName,
    String passName  
  )
  {
    Path startPath = pStartPaths.get(Department.render, SubDir.edit);
    String filename = null;
    if (layerName != null) {
//      startPath = new Path(startPath, layerName);
      filename = layerName;
    }
    if (passName != null) {
//      startPath = new Path(startPath, passName);
      if (filename != null)
        filename = join(filename, passName);
      else
        filename = passName;
    }
    
    if (filename != null)
      filename = join(pFullName, filename);
    else
      filename = pFullName;
    
    startPath = new Path(startPath, filename);
    
    return startPath.toString();
  }
  
  /**
   * The name of a render image node passed on to compositing.
   * 
   * @param layerName
   *   The name of the layer that the render belongs to.  Layers are physical breakdowns of
   *   elements, so this might be something like foreground or background.  This can be set
   *   to <code>null</code> if there is no layer break out.
   * @param passName
   *   The name of the pass that the render belongs to.  Passes are different looks of the 
   *   same physical elements, so these might include beauty, specular, matte passes, 
   *   occlusion, etc.  This can be set to <code>null</code> if there are no passes being used.
   */
  public String
  getRenderingProductImageNode
  (
    String layerName,
    String passName  
  )
  {
    Path startPath = pStartPaths.get(Department.render, SubDir.product);
    String filename = null;
    if (layerName != null) {
      startPath = new Path(startPath, layerName);
      filename = layerName;
    }
    if (passName != null) {
      startPath = new Path(startPath, passName);
      if (filename != null)
        filename = join(filename, passName);
      else
        filename = passName;
    }
    
    if (filename != null)
      filename = join(pFullName, filename);
    else
      filename = pFullName;
    
    startPath = new Path(startPath, filename);
    
    return startPath.toString();
  }
  
  /**
   * The compositing scene used to slap comp the renders for approval.
   */
  public String
  getRenderingCompScene()
  {
    return new Path(pStartPaths.get(Department.render, SubDir.prepare), 
                    join(join(pFullName, "render"), "comp")).toString();
  }
  
  /**
   * The rendering of the composite scene used to slap comp 
   * the renders for approval.
   */
  public String
  getRenderingFinalImageNode()
  {
    return new Path(pStartPaths.get(Department.render, SubDir.focus), 
                    join(pFullName, "render")).toString();
  }
  
  /**
   * The node an artist uses to submit the rendering task.
   */
  public String
  getRenderingSubmitNode()
  {
    return new Path(pStartPaths.get(Department.render, SubDir.submit), 
                    join(join(pFullName, "render"), aSubmit)).toString();
  }
  
  /**
   * The node a supervisor uses to approve the rendering task.
   */
  public String
  getRenderingApproveNode()
  {
    return new Path(pStartPaths.get(Department.render, SubDir.approve), 
                    join(join(pFullName, "render"), aApprove)).toString();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T I E S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Join two strings together, inserting an underscore between them.
   */
  private String
  join
  (
    String a,
    String b
  )
  {
    return a + "_" + b;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   U T I L I T I E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Build a generated ShotNamer with the pieces passed in.
   * 
   * @param mclient
   *   The Master manager instance to build the Namer with.
   * @param qclient
   *   The Queue manager instance to build the Namer with.
   * @param project
   *   The name of the project
   * @param spotName
   *   The name of the spot.
   * @param shotName
   *   The name of the shot.
   */
  public static ShotNamer
  getGeneratedNamer
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    String project,
    String spotName,
    String shotName
  )
    throws PipelineException
  {
    ShotNamer namer = new ShotNamer(mclient, qclient);
    namer.setParamValue(ParamNames.aProjectName, project);
    namer.setParamValue(ParamNames.aSpotName, spotName);
    namer.setParamValue(ParamNames.aShotName, shotName);
    namer.run();
    return namer;
  }
  
  /**
   * Static method to turn a node name into a ShotNamer class that represents the shot
   * that the node is from.
   * <p>
   * The method will return <code>null</code> if the nodeName that is passed in is not a 
   * valid name of a node in a shot.
   */
  public static ShotNamer
  getNamerFromNodeName
  (
    String nodeName,
    MasterMgrClient mclient,
    QueueMgrClient qclient
  )
    throws PipelineException
  {
    String projectParent = StudioDefinitions.getProjectsParentPath().toString();
    if (!nodeName.startsWith(projectParent))
      throw new PipelineException
        ("The node name (" + nodeName + ") does not appear to be a valid shot node.");
    String project = getNextComponent(nodeName, projectParent);
    String spotParent = StudioDefinitions.getSpotStartPath(project).toString();
    if (!nodeName.startsWith(spotParent))
      throw new PipelineException
        ("The node name (" + nodeName + ") does not appear to be a valid shot node.");
    String spotName = getNextComponent(nodeName, spotParent);
    String shotParent = StudioDefinitions.getSpotPath(project, spotName).toString();
    if (!nodeName.startsWith(shotParent))
      throw new PipelineException
        ("The node name (" + nodeName + ") does not appear to be a valid shot node.");
    String shotName = getNextComponent(nodeName, shotParent);
    
    ShotNamer namer = new ShotNamer(mclient, qclient);
    namer.setParamValue(ParamNames.aProjectName, project);
    namer.setParamValue(ParamNames.aSpotName, spotName);
    namer.setParamValue(ParamNames.aShotName, shotName);
    namer.run();
    return namer;
    
  }
  
  private static String
  getNextComponent
  (
    String full,
    String start
  )
    throws PipelineException
  {
    String replaced = full.replace(start, "");
    Path p = new Path(replaced);
    ArrayList<String> pieces = p.getComponents();
    if (pieces.isEmpty() || pieces.get(0) == null)
      throw new PipelineException
      ("The node name (" + full+ ") does not appear to be a valid asset node.");
    return pieces.get(0);
  }
  
  
  private static final long serialVersionUID = -2462115806853614993L;
  
  private static final String aApprove = StudioDefinitions.aApproveName;
  private static final String aSubmit = StudioDefinitions.aSubmitName;
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private String pProject;
  
  private String pSpotName;
  
  private String pShotName;
  
  private String pFullName;
  
  private DoubleMap<Department, SubDir, Path> pStartPaths;
  
}
