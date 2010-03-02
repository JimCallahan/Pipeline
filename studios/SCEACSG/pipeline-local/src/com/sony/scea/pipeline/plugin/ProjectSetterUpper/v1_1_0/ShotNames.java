// $Id: ShotNames.java,v 1.1 2008/06/11 12:48:23 jim Exp $

package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0;

import java.text.DecimalFormat;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

public 
class ShotNames 
  extends BaseNames 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  ShotNames
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient
  )
    throws PipelineException
  {
    super("ShotNamer", 
          "The basic naming class for shots",
          mclient, qclient, null);
    
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
         "The Spot the shot is being created in..",
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
    
    pFullName = pSpotName + "_" + pShotName + "_";  
    
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
    return join(pProject, pFullName);
  }
  
  public String
  getTaskName(String taskType)
  {
    //return pSpotName + "_" + pShotName + "_" + taskType;

	  return join(pSpotName, pShotName);
  }
  
  private String
  getSubmitNode
  (
    Path path
  )
  {
    return new Path(path, "submitForQC").toString();
  }
  
  private String
  getApprovalNode
  (
    Path path
  )
  {
    return new Path(path, "approve").toString();
  }
  
  private String
  getApprovalNode
  (
    Path path,
    String name
  )
  {
    return new Path(path, join ("approve", name)).toString();
  }
  
  public String
  getAnimEditNodeName()
  {
    return new Path(pStartPaths.get(Department.anim, SubDir.edit), pFullName + "anim").toString();
  }
  
  public String
  getAnimExportPrepareNodeName(String assetName)
  {
    return new Path(new Path(pStartPaths.get(Department.anim, SubDir.prepare), "data"), assetName + "_curves").toString();
  }
  
  public String
  getAnimExportPrepareNodeName(String assetName, Integer i)
  {
	DecimalFormat df = new DecimalFormat("0000");
	
    return new Path(new Path(pStartPaths.get(Department.anim, SubDir.prepare), "data"), assetName + "_" + df.format(i) + "_curves").toString();
  }
  
  public String
  getAnimExportProductNodeName(String assetName)
  {
	  return new Path(new Path(pStartPaths.get(Department.anim, SubDir.product), "data"), assetName + "_curves").toString();
  }
  
  public String
  getAnimExportProductNodeName(String assetName, Integer i)
  {
	  DecimalFormat df = new DecimalFormat("0000");
	  
	  return new Path(new Path(pStartPaths.get(Department.anim, SubDir.product), "data"), assetName + "_" + df.format(i) + "_curves").toString();
  }
  
  public String
  getAnimProductNodeName()
  {
    return new Path(pStartPaths.get(Department.anim, SubDir.product), pFullName + "anim").toString();
  }
  
  public String
  getAnimBuildNodeName()
  {
    return new Path(pStartPaths.get(Department.anim, SubDir.prepare), pFullName + "anim").toString();
  }
  
  /**
   * A render (or playblast) of the animation that is submitted for approval. 
   */
  public String
  getAnimImgNodeName()
  {
    return new Path(pStartPaths.get(Department.anim, SubDir.prepare), pFullName + "img").toString();
  }
  
  /**
   * A thumbnail of the images.
   */
  public String
  getAnimThumbNodeName()
  {
    return new Path(pStartPaths.get(Department.anim, SubDir.thumb), pFullName + "thumb").toString();
  }
  
  /**
   *  The submit node for the animation task. 
   */
  public String
  getAnimSubmitNodeName()
  {
    return new Path(pStartPaths.get(Department.anim, SubDir.submit), pFullName + "submit").toString();
  }
  
  /**
   * A node built by applying exported animation to lighting models.  This node
   * is referenced by the lighting scene.
   */  
  public String
  getPreLightNodeName()
  {
    return new Path(pStartPaths.get(Department.anim, SubDir.product), pFullName + "preLgt").toString();
  }
  
  /**
   * A scene specific gathering MEL script that is run on the prelight as it is being 
   * built.  Allows shot specific overrides. 
   */
  public String
  getPreLightMELNodeName()
  {
    return new Path(pStartPaths.get(Department.anim, SubDir.product), pFullName + "preScript").toString();
  }
  
  /**
   *  The approve node for the animation task. 
   */
  public String
  getAnimApproveNodeName()
  {
    return new Path(pStartPaths.get(Department.anim, SubDir.approve), pFullName + "approve").toString();
  }
  
  public String
  getLightEditNodeName()
  {
    return new Path(pStartPaths.get(Department.lgt, SubDir.edit), pFullName + "lgt").toString();
  }
  
  /**
   *  The rendered images which are submitted for approval of the lighting scene. 
   */
  public String
  getLightImagesNodeName()
  {
    return new Path(pStartPaths.get(Department.lgt, SubDir.prepare), pFullName + "img").toString();
  }

  /**
   * The submit node for the lighting task.
   */
  public String
  getLightSubmitNodeName()
  {
    return new Path(pStartPaths.get(Department.lgt, SubDir.submit), pFullName + "submit").toString();
  }
  
  /**
   * The approve node for the lighting task.
   */
  public String
  getLightApproveNodeName()
  {
    return new Path(pStartPaths.get(Department.lgt, SubDir.approve), pFullName + "approve").toString();
  }
  
  /**
   * Generated on lighting approval.  This node is used to render all the final
   * render passes that depend upon lighting. 
   */
  public String
  getFinalLightNodeName()
  {
    return new Path(pStartPaths.get(Department.lgt, SubDir.product), pFullName + "lgt").toString();
  }
  
  /**
   * A thumbnail of the images.
   */
  public String
  getLightThumbNodeName()
  {
    return new Path(pStartPaths.get(Department.lgt, SubDir.thumb), pFullName + "thumb").toString();
  }
  
  /**
   * A node containing all of the exported animation for the layout. 
   */
  public String
  getLayoutExportPrepareNodeName
  (
    String assetName  
  )
  {
    return new Path(new Path(pStartPaths.get(Department.anim, SubDir.prepare), "data"), pFullName + assetName).toString();
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T I E S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Join two strings together, inserting an underscore between them.
   */
  public String
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
   * Static method to turn a node name into a ShotNames class that represents the shot
   * that the node is from.
   * <p>
   * The method will return <code>null</code> if the nodeName that is passed in is not a 
   * valid name of a node in a shot.
   */
  public static ShotNames
  getNamesFromNode
  (
    String nodeName,
    MasterMgrClient mclient,
    QueueMgrClient qclient
  )
  {
    Path p = new Path(nodeName);
    ArrayList<String> pieces = p.getComponents();
    if (pieces.size() < 5)
      return null;
    String start = pieces.remove(0);
    if (!new Path("/" + start).equals(StudioDefinitions.aProjectStartPath))
      return null;
    String project = pieces.remove(0);
    String spotStart = pieces.remove(0);
    if (!spotStart.equals(StudioDefinitions.aProdStart))
      return null;
    String sequence = pieces.remove(0);
    String shot = pieces.remove(0).replaceFirst(sequence, "");
    ShotNames namer = null;
    try {
      namer = new ShotNames(mclient, qclient);
      namer.setParamValue(ParamNames.aProjectName, project);
      namer.setParamValue(ParamNames.aSpotName, sequence);
      namer.setParamValue(ParamNames.aShotName, shot);
      namer.run();
    }
    catch (PipelineException ex) {
      ex.printStackTrace();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return namer;
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/  

  static final long serialVersionUID = 7986870668535250969L;

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private StudioDefinitions pDefs;
  
  private String pProject;
  
  private String pSpotName;
  
  private String pShotName;
  
  private String pFullName;
  
  private DoubleMap<Department, SubDir, Path> pStartPaths;
  
}
