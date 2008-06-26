// $Id: AssetNamer.java,v 1.2 2008/06/26 20:45:55 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/*------------------------------------------------------------------------------------------*/
/*   A S S E T   N A M E R                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Define the names of nodes used to construct Asset networks in the Nathan Love Base
 * Collection.
 */
public 
class AssetNamer
  extends BaseNames
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  AssetNamer
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient
  )
    throws PipelineException 
  {
    super("AssetNamer", 
          "Define the names of nodes used to construct Asset networks in " +
          "the Nathan Love Base Collection",
          mclient,
          qclient);
    {
      UtilityParam param =
        new StringUtilityParam
        (ParamNames.aProjectName,
         "The Name of the Project the asset should live in", 
         null);
      addParam(param);
    }
    {
      UtilityParam param = 
        new StringUtilityParam
        (ParamNames.aAssetName, 
         "The Name of the asset", 
         null);
      addParam(param);
    }
    {
      UtilityParam param = 
        new OptionalEnumUtilityParam
        (ParamNames.aAssetType, 
         "The Type of the asset", 
         AssetType.character.toTitle(),
         AssetType.commonTitles());
      addParam(param);
    }
  }
  
  @Override
  public void generateNames()
    throws PipelineException
  {
    setContext((UtilContext) getParamValue(aUtilContext));
    
    pProject = getStringParamValue(new ParamMapping(ParamNames.aProjectName));
    pAssetName =  getStringParamValue(new ParamMapping(ParamNames.aAssetName));
    pAssetType =  getStringParamValue(new ParamMapping(ParamNames.aAssetType));

    Path startPath = StudioDefinitions.getAssetPath(pProject, pAssetType, pAssetName);
    
    pStartPaths = new DoubleMap<Department, SubDir, Path>();
    pAssetStartPaths = new TreeMap<SubDir, Path>();
    
    for (Department department : Department.values()) {
      for (SubDir sub : SubDir.values()) {
        Path p = new Path(new Path(startPath, department.toString()), sub.dirName());
        pStartPaths.put(department, sub, p);
      }
    }
    for (SubDir sub : SubDir.values()) {
      Path p = new Path(startPath, sub.dirName());
      pAssetStartPaths.put(sub, p);
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  A C C E S S                                                                           */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the asset.
   */
  public String
  getAssetName()
  {
    return pAssetName;
  }
  
  /**
   * Get the type of the asset.
   */
  public AssetType
  getAssetType()
  {
    return AssetType.fromString(pAssetType);
  }
  
  /**
   * Get the name of the Pipeline task that will make this asset.
   */
  public String
  getTaskName()
  {
    return pAssetName + "_" + pAssetType;
  }
  
  /**
   * Get the name space which will be used to reference the scene.
   */
  public String
  getNamespace()
  {
    return pAssetName + "_" + pAssetType;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  M O D E L                                                                             */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The scene where the modeler works.
   */
  public String
  getModelEditScene()
  {
    return new Path(pStartPaths.get(Department.model, SubDir.edit), 
                    pAssetName + "_model_edit").toString();
  }
  
  /**
   * The node which the artist uses to submit the modeling task.
   */
  public String
  getModelSubmitNode()
  {
    return new Path(pStartPaths.get(Department.model, SubDir.submit), 
                    join(join(pAssetName, "model"), aSubmit)).toString();
  }
  
  /**
   * The node which the supervisor uses to approve the modeling task.
   */
  public String
  getModelApproveNode()
  {
    return new Path(pStartPaths.get(Department.model, SubDir.approve), 
                    join(join(pAssetName, "model"), aApprove)).toString();
  }
  
  /**
   * The scene which has the model verification MEL script run on it.
   */
  public String
  getModelVerifyScene()
  {
    return new Path(pStartPaths.get(Department.model, SubDir.prepare), 
                    pAssetName + "_model").toString();
  }
  
  /**
   * The scene delivered to rigging and shading. 
   */
  public String
  getModelProductScene()
  {
    return new Path(pStartPaths.get(Department.model, SubDir.product), 
                    pAssetName + "_model").toString();
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*  R I G                                                                                 */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The scene where the rigger works.
   */
  public String
  getRigEditScene()
  {
    return new Path(pStartPaths.get(Department.rig, SubDir.edit), 
                    pAssetName + "_rig_edit").toString();
  }
  
  /**
   * The node which the artist uses to submit the rigging task.
   */
  public String
  getRigSubmitNode()
  {
    return new Path(pStartPaths.get(Department.rig, SubDir.submit), 
                    join(join(pAssetName, "rig"), aSubmit)).toString();
  }
  
  /**
   * The node which the supervisor uses to approve the rigging task.
   */
  public String
  getRigApproveNode()
  {
    return new Path(pStartPaths.get(Department.rig, SubDir.approve), 
                    join(join(pAssetName, "rig"), aApprove)).toString();
  }
  
  /**
   * The scene which has the finalizeRig script run on it.
   */
  public String
  getRigVerifyScene()
  {
    return new Path(pStartPaths.get(Department.rig, SubDir.prepare), 
                    pAssetName + "_rig").toString();
  }
  
  /**
   * The scene delivered to animation. 
   */
  public String
  getRigProductScene()
  {
    return new Path(pStartPaths.get(Department.rig, SubDir.product), 
                    pAssetName + "_anim").toString();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  S H A D E                                                                             */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The scene where the shader works.
   */
  public String
  getShadeEditScene()
  {
    return new Path(pStartPaths.get(Department.shade, SubDir.edit), 
                    pAssetName + "_shade_edit").toString();
  }
  
  /**
   * The node which the artist uses to submit the shading task.
   */
  public String
  getShadeSubmitNode()
  {
    return new Path(pStartPaths.get(Department.shade, SubDir.submit), 
                    join(join(pAssetName, "shade"), aSubmit)).toString();
  }
  
  /**
   * The node which the supervisor uses to approve the shading task.
   */
  public String
  getShadeApproveNode()
  {
    return new Path(pStartPaths.get(Department.shade, SubDir.approve), 
                    join(join(pAssetName, "shade"), aApprove)).toString();
  }
  
  /**
   * The scene which contains the exported shaders.
   */
  public String
  getShaderExportScene()
  {
    return new Path(pStartPaths.get(Department.shade, SubDir.prepare), 
                    pAssetName + "_shadeExp").toString();
  }
  
  /**
   * The scene which contains the exported shaders put onto the rigged character.
   */
  public String
  getShadeFinalScene()
  {
    return new Path(pStartPaths.get(Department.shade, SubDir.prepare), 
                    pAssetName + "_shade").toString();
  }
  
  /**
   * The scene delivered to lighting. 
   */
  public String
  getShadeProductScene()
  {
    return new Path(pStartPaths.get(Department.shade, SubDir.product), 
                    pAssetName + "_lgt").toString();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  A S S E T                                                                             */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The scene where the artist works.
   */
  public String
  getAssetEditScene()
  {
    return new Path(pAssetStartPaths.get(SubDir.edit), 
                    pAssetName + "_edit").toString();
  }
  
  /**
   * The node which the artist uses to submit the asset task.
   */
  public String
  getAssetSubmitNode()
  {
    return new Path(pAssetStartPaths.get(SubDir.submit), 
                    join(pAssetName, aSubmit)).toString();
  }
  
  /**
   * The node which the supervisor uses to approve the asset task.
   */
  public String
  getAssetApproveNode()
  {
    return new Path(pAssetStartPaths.get(SubDir.approve), 
                    join(pAssetName, aApprove)).toString();
  }
  
  /**
   * The scene which has the finalizeAsset script run on it.
   */
  public String
  getAssetVerifyScene()
  {
    return new Path(pAssetStartPaths.get(SubDir.prepare), 
                    pAssetName + "_" + pAssetType).toString();
  }
  
  /**
   * The scene delivered to animation. 
   */
  public String
  getAssetProductScene()
  {
    return new Path(pAssetStartPaths.get(SubDir.product), 
                    pAssetName + "_" + pAssetType).toString();
  }
  
  /**
   * The shortened name of the product scene, used by Camera Assets.. 
   */
  public String
  getAssetProductShortScene()
  {
    return new Path(pAssetStartPaths.get(SubDir.product), 
                    pAssetName).toString();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  T E X T U R E                                                                         */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The grouping scene where textures are attached.
   */
  public String
  getTextureEditNode()
  {
    return new Path(pStartPaths.get(Department.texture, SubDir.edit), 
                    pAssetName + "_tex_edit").toString();
  }
  
  /**
   * The grouping scene where textures are attached.
   */
  public String
  getTextureProductNode()
  {
    return new Path(pStartPaths.get(Department.texture, SubDir.product), 
                    pAssetName + "_tex").toString();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  A S S E T   C L A S S                                                                 */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return the class of asset that this namer represents on disk.
   * <p>
   * This method will only work if the nodes have actually been constructed using an asset
   * builder. 
   * @return The class of the asset or <code>null</code> if no nodes exist in Pipeline.
   */
  public AssetClass
  getAssetClass()
    throws PipelineException
  {
    AssetClass toReturn = null;
    
    if (nodeExists(getShadeProductScene()))
      toReturn = AssetClass.Asset;
    else if (nodeExists(getAssetProductScene()))
      toReturn = AssetClass.SimpleAsset;
    else if (nodeExists(getAssetProductShortScene()))
      toReturn = AssetClass.Camera;
    
    return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  S T A T I C   H E L P E R S                                                           */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Build a generated AssetNamer with the pieces passed in.
   * 
   * @param mclient
   *   The Master manager instance to build the Namer with.
   * @param qclient
   *   The Queue manager instance to build the Namer with.
   * @param project
   *   The name of the project
   * @param assetName
   *   The name of the asset.
   * @param assetType
   *   The type of the asset.
   */
  public static AssetNamer
  getGeneratedNamer
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    String project,
    String assetName,
    AssetType assetType
  )
    throws PipelineException
  {
    AssetNamer namer = new AssetNamer(mclient, qclient);
    namer.setParamValue(ParamNames.aProjectName, project);
    namer.setParamValue(ParamNames.aAssetType, assetType.toTitle());
    namer.setParamValue(ParamNames.aAssetName, assetName);
    namer.run();
    return namer;
  }
  
  /**
   * Turn a node name into an AssetNamer instance.
   */
  public static AssetNamer
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
        ("The node name (" + nodeName + ") does not appear to be a valid asset node.");
    String project = getNextComponent(nodeName, projectParent);
    String assetParent = StudioDefinitions.getAssetParentPath(project).toString();
    if (!nodeName.startsWith(assetParent))
      throw new PipelineException
        ("The node name (" + nodeName + ") does not appear to be a valid asset node.");
    String assetType = getNextComponent(nodeName, assetParent);
    String assetTypeParent = 
      StudioDefinitions.getAssetTypeParentPath(project, assetType).toString();
    if (!nodeName.startsWith(assetTypeParent))
      throw new PipelineException
        ("The node name (" + nodeName + ") does not appear to be a valid asset node.");
    String assetName = getNextComponent(nodeName, assetTypeParent);
    AssetNamer namer = new AssetNamer(mclient, qclient);
    namer.setParamValue(ParamNames.aProjectName, project);
    namer.setParamValue(ParamNames.aAssetType, assetType);
    namer.setParamValue(ParamNames.aAssetName, assetName);
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
  /*  S T A T I C   I N T E R N A L S                                                       */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5038434892228456984L;
  
  private static final String aApprove = StudioDefinitions.aApproveName;
  private static final String aSubmit = StudioDefinitions.aSubmitName;


  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  private String pProject;

  private String pAssetName;

  private String pAssetType;
  
  private DoubleMap<Department, SubDir, Path> pStartPaths;

  private TreeMap<SubDir, Path> pAssetStartPaths;

}
