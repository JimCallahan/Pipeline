// $Id: StudioDefinitions.java,v 1.1 2008/06/11 12:48:23 jim Exp $

package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/*------------------------------------------------------------------------------------------*/
/*   S T U D I O   D E F I N I T I O N S                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Provides a set of convenience methods used by virtually all Namers and Builders to lookup
 * or generate the common node name path components related to projects, sequences, shots and
 * assets.
 * <P>
 * Instances of this class are passed to the constructors of Builders and by these builder
 * instances on to their internal sub-builders and namer instances.
 * <P>
 * NOTE: The node directory structure provided is based off the Nathan Love Base project, but
 * some or all of the builders in this collection could be reused and modified for other
 * future projects with completely different conventions by subclassing and overriding methods
 * in this class.
 */ 
public 
class StudioDefinitions 
  extends BaseUtil 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
        
  /**
   * Construct a new instance. 
   * 
   * @param mclient
   *   The instance of the Master Manager that the utility will use to execute.
   * 
   * @param qclient
   *   The instance of the Queue Manager that the utility will use to execute.
   *
   * @param context
   *   The working area author|view and toolset environment.
   */
  public 
  StudioDefinitions
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    UtilContext context
  ) 
    throws PipelineException 
  {
    super("StudioDefinitions",
          "Provides basic information about where things are located in the Nathan Love Pipeline setup.",
          mclient, qclient, context);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   Q U E R I E S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns the fully resolved node directory path to the parent directory of all existing
   * projects.
   */
  public static Path
  getProjectsParentPath()
  {
    return aProjectStartPath;
  }
  
  /**
   * Returns the short names of all existing projects. 
   */
  public ArrayList<String> 
  getProjectList() 
    throws PipelineException
  {
    return findChildBranchNames(aProjectStartPath);
  }

  /**
   * Returns the fully resolved node directory path to the root of the given project.
   * 
   * @param project
   *   The short name of the project.
   */
  public static Path
  getProjectPath
  (
    String project    
  )
  {
    return new Path(aProjectStartPath, project);
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the fully resolved node path to the root directory that contains all the asset type
   * folders.
   * 
   * @param project
   *   The name of the project.
   */
  public static Path
  getAssetParentPath
  (
    String project
  )
  {
    return new Path(getProjectPath(project), aAssetStart);
  }
  
  /**
   * Get the fully resolved node path to the root directory that contains all the assets of
   * the given asset type.
   * 
   * @param project
   *   The name of the project
   *   
   * @param assetType
   *   The type of the asset.
   */
  public static Path
  getAssetTypeParentPath
  (
    String project,
    String assetType
  )
  {
    return new Path(getAssetParentPath(project), assetType);
  }
  
  /**
   * Get the fully resolved node path to the root directory of an asset.
   * 
   * @param project
   *   The name of the project
   * 
   * @param assetType
   *   The type of the asset.
   *   
   * @param assetName
   *   The name of the asset.
   */
  public static Path
  getAssetPath
  (
    String project,
    String assetType,
    String assetName
  )
  {
    return new Path(getAssetTypeParentPath(project, assetType), assetName); 
  }
  
  public ArrayList<String>
  getAssetTypeList
  (
    String project  
  )
    throws PipelineException
  {
    return findChildBranchNames(getAssetParentPath(project));
  }
  
  /**
   * Get a map of all the assets in the specified asset types, indexed by their asset type.

   * @param project
   *   The name of the project
   * 
   * @param assetTypes
   *   A list of asset types to search
   */
  public MappedArrayList<String, String>
  getAssetList
  (
    String project,
    Set<String> assetTypes
  )
    throws PipelineException
  {
    MappedArrayList<String, String> toReturn = new MappedArrayList<String, String>();
    for (String assetType : assetTypes) {
      for (String asset : findChildBranchNames(getAssetTypeParentPath(project, assetType))) {
        toReturn.put(assetType, asset);
      }
    }
    return toReturn;
  }

  /**
   * Get a list of all the assets in a particular asset type.
   * 
   * @param project
   *   The name of the project
   * 
   * @param assetType
   *   The type of the assets being searched for.
   */
  public ArrayList<String>
  getAssetList
  (
    String project,
    String assetType 
  )
    throws PipelineException
  {
    TreeSet<String> types = new TreeSet<String>();
    types.add(assetType);
    return (getAssetList(project, types)).get(assetType);
  }

  /**
   * Get a map of all the assets in a project, indexed by their asset type.

   * @param project
   *   The name of the project
   */
  public MappedArrayList<String, String>
  getCompleteAssetList
  (
    String project  
  )
    throws PipelineException
  {
    return getAssetList(project, new TreeSet<String>(getAssetTypeList(project)));
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the fully resolved node path to the root directory of the given prod.

   * @param project
   *   The name of the project the prod is in.
   *    
   * @param prod
   *   The name of the prod. 
   */
  public static Path
  getProdPath
  (
    String project,
    String prod
  )
  {
    return new Path(getProdStartPath(project), prod);
  }
  
  /**
   * Get the fully resolved node path to the directory that a project's prods are
   * stored in.
   *  
   * @param project
   *   The name of the project.
   */
  public static Path
  getProdStartPath
  (
    String project
  )
  {
    return new Path(getProjectPath(project), aProdStart);
  }

  /**
   * Get the fully resolved node path to a shot's root directory
   * 
   * @param project
   *   The name of the project the shot is in.
   * @param prod
   *   The name of the prod the shot is in.
   * @param shot
   *   The name of the shot.
   */
  public static Path
  getShotPath
  (
    String project,
    String prod,
    String shot
  )
  {
    return new Path(getProdPath(project, prod), shot);
  }

  /**
   * Get a list of the all the prods in a given project.
   * 
   * @param project
   *   The name of the project.
   */
  public ArrayList<String>
  getProdList
  (
    String project
  )
    throws PipelineException
  {
    return findChildBranchNames(getProdStartPath(project));
  }
  
  /**
   * Get a list of all the shots in a given prod in a given project
   * 
   * @param project
   *   The name of the project.
   * 
   * @param prod
   *   The name of the prod.
   */
  public ArrayList<String>
  getShotList
  (
    String project,
    String prod
  )
    throws PipelineException
  {
    return findChildBranchNames(getProdPath(project, prod));
  }
  
  public DoubleMap<String, String, ArrayList<String>>
  getAllProjectsAllNames() 
    throws PipelineException
  {
    DoubleMap<String, String, ArrayList<String>> toReturn = new DoubleMap<String, String, ArrayList<String>>();
    for (String project : getProjectList()) {
      for (String prod: getProdList(project)) {
        toReturn.put(project, prod, getShotList(project, prod));
      }
    }
    return toReturn;
  }
  
  public DoubleMap<String, String, ArrayList<String>>
  getAllProjectsAllNamesForParam() 
    throws PipelineException
  {
    DoubleMap<String, String, ArrayList<String>> toReturn = new DoubleMap<String, String, ArrayList<String>>();
    for (String project : getProjectList()) {
      for (String prod : getProdList(project)) {
        ArrayList<String> shots = new ArrayList<String>();
        shots.add(aNEW);
        shots.addAll(getShotList(project, prod));
        toReturn.put(project, prod, shots);
      }
      ArrayList<String> shots = new ArrayList<String>();
      shots.add(aNEW);
      toReturn.put(project, aNEW, shots);
    }
    return toReturn;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   *  Get the fully resolved node path to the root directory containing look development 
   *  targets.
   *  
   *  @param project
   *    The name of the project.
   */
  public static Path
  getLookDevParentPath
  (
    String project  
  )
  {
    return new Path(getProjectPath(project), aLookDevStart);
  }
  
  /**
   * Get the fully resolved node path to the root directory of a look development target.
   * 
   * @param project
   *   The name of the project.
   * 
   * @param target
   *   The name of the look development target.
   */
  public static Path
  getLookDevPath
  (
    String project,
    String target
  )
  {
    return new Path(getLookDevParentPath(project), target);
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the fully resolved node path to the extras directory in a given project.
   */

  public static Path
  getEtcPath
  (
    String project  
  )
  {
    return new Path(getProjectPath(project), aEtcStart);
  }
  
  /**
   * Get the fully resolved node path to the scripts directory in a given project.
   */
  public static Path
  getScriptPath
  (
    String project  
  )
  {
    return new Path(getEtcPath(project), aScriptStart);
  }
  
  /**
   * Get the fully resolved node path to the template directory in a given project.
   */
  public static Path
  getTemplatePath
  (
    String project  
  )
  {
    return new Path(getEtcPath(project), aTemplateStart);
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   O V E R R I D E S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Disable the lookup of the current pass since this class should never be used 
   * in this way.
   * 
   * @throws IllegalStateException If you actually try to call this method.
   */ 
  @Override
  public int 
  getCurrentPass()
  {
    throw new IllegalStateException("This should never be called!");
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/


  private static final long serialVersionUID = -4738201691401265904L;

  public static final Path aProjectStartPath = new Path("/projects");
  
  public static final String aAssetStart     = "assets";
  public static final String aProdStart      = "production";
  public static final String aLookDevStart   = "lookDev";
  public static final String aScriptStart    = "scripts";
  public static final String aTemplateStart  = "templates";
  public static final String aEtcStart       = "util";
  
  public static String aNEW  = "[[NEW]]";
  
}
