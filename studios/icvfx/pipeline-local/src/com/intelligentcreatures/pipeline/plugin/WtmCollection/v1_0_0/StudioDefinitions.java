// $Id: StudioDefinitions.java,v 1.3 2008/02/06 07:21:06 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import java.util.ArrayList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.BaseUtil;
import us.temerity.pipeline.builder.UtilContext;

/*------------------------------------------------------------------------------------------*/
/*   S T U D I O   D E F I N I T I O N S                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Provides a set of convienence methods used by virtually all Namers and Builders to lookup
 * or generate the common node name path components related to projects, sequences, shots 
 * and assets. <P> 
 * 
 * Instances of this class are passed to the constructors of Builders and by these builder
 * instances on to their internal sub-builders and namer instances. <P> 
 * 
 * NOTE: The node directory structure provided is based off the WTM project, but some or 
 * all of the builders in this collection could be reused and modified for other future 
 * projects with completely different conventions by subclassing and overriding methods 
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
          "Provides information about the top-level organization at the studio.", 
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
    return new Path("/" + aProjectsParent); 
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
    return new Path(getProjectsParentPath(), project);
  }
  
  /**
   * Returns the short names of the root directories of all existing projects. 
   */
  public ArrayList<String> 
  getProjectsList() 
    throws PipelineException
  {
    return findChildBranchNames(getProjectsParentPath());
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Returns the fully resolved node directory path to the parent directory of all existing
   * shot sequences for the given project.
   * 
   * @param project
   *   The short name of the project.
   */
  public static Path
  getSequencesParentPath
  (
   String project
  )
  {
    return new Path(getProjectPath(project), aSequencesParent);
  }

  /**
   * Returns the fully resolved node directory path to the root directory of the given 
   * shot sequence in a project.
   * 
   * @param project
   *   The short name of the project.
   * 
   * @param sequence
   *   The short name of the shot sequence.
   */ 
  public static Path
  getSequencePath
  (
   String project,
   String sequence
  )
  {
    return new Path(getSequencesParentPath(project), sequence);
  }

  /** 
   * Returns the short names of the root directories of all existing shot sequences for the 
   * given project.
   * 
   * @param project
   *   The short name of the project.
   */ 
  public ArrayList<String>
  getSequencesList
  (
   String project
  )
    throws PipelineException
  {
    return findChildBranchNames(getSequencesParentPath(project));
  }
  

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Returns the fully resolved node directory path to the parent directory of all existing
   * shots within a sequence for the given project.<P> 
   * 
   * In this case, its the same as the {@link #getSequencePath}.
   * 
   * @param project
   *   The short name of the project.
   * 
   * @param sequence
   *   The short name of the shot sequence.
   */
  public static Path
  getShotsParentPath
  (
   String project, 
   String sequence
  )
  {
    return getSequencePath(project, sequence);
  }

  /**
   * Returns the fully resolved node directory path to the root directory of the given 
   * shot within a sequence in a project.
   * 
   * @param project
   *   The short name of the project.
   * 
   * @param sequence
   *   The short name of the shot sequence.
   * 
   * @param shot
   *   The short name of the shot.
   */ 
  public static Path
  getShotPath
  (
   String project,
   String sequence, 
   String shot
  )
  {
    return new Path(getShotsParentPath(project, sequence), shot); 
  }

  /** 
   * Returns the short names of the root directories of all existing shots within a sequence 
   * for the given project.
   * 
   * @param project
   *   The short name of the project.
   * 
   * @param sequence
   *   The short name of the shot sequence.
   */ 
  public ArrayList<String>
  getShotsList
  (
   String project,
   String sequence
  )
    throws PipelineException
  {
    return findChildBranchNames(getShotsParentPath(project, sequence));
  }
  
  

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Returns the fully resolved node directory path to the parent directory of all existing
   * shared assets for the given project.
   * 
   * @param project
   *   The short name of the project.
   */
  public static Path
  getAssetsParentPath
  (
    String project
  )
  {
    return new Path(getProjectPath(project), aAssetsParent);
  }

  /**
   * Returns the fully resolved node directory path to the root directory of the given 
   * asset in a project.
   * 
   * @param project
   *   The short name of the project.
   * 
   * @param asset
   *   The short name of the asset.
   */ 
  public static Path
  getAssetPath
  (
   String project,
   String asset
  )
  {
    return new Path(getAssetsParentPath(project), asset);
  }

  /** 
   * Returns the short names of the root directories of all existing assets for the given 
   * project.
   * 
   * @param project
   *   The short name of the project.
   */ 
  public ArrayList<String>
  getAssetsList
  (
   String project
  )
    throws PipelineException
  {
    return findChildBranchNames(getAssetsParentPath(project));
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Returns a table of the short names of all existing projects, sequences and shots.
   */ 
  public DoubleMap<String, String, ArrayList<String>>
  getAllProjectsAllNames() 
    throws PipelineException
  {
    DoubleMap<String, String, ArrayList<String>> toReturn = 
      new DoubleMap<String, String, ArrayList<String>>();
    
    for(String project : getProjectsList()) {
      for(String seq : getSequencesList(project)) 
        toReturn.put(project, seq, getShotsList(project, seq));
    }

    return toReturn;
  }
  
  /** 
   * Returns a table of the short names of all existing projects, sequences and shots 
   * suitable for use when initializing the DoubleMapUtilityParam of builders. <P> 
   * 
   * This method preppends a "[[NEW]]" entrie to the existing sequence and shot entries 
   * to provide the user of the builder the opportunity to either reuse or create each of 
   * these entities when running the builder.
   */ 
  public DoubleMap<String, String, ArrayList<String>>
  getAllProjectsAllNamesForParam() 
    throws PipelineException
  {
    DoubleMap<String, String, ArrayList<String>> toReturn = 
      new DoubleMap<String, String, ArrayList<String>>();

    for(String project : getProjectsList()) {
      for(String seq : getSequencesList(project)) {
        ArrayList<String> shots = new ArrayList<String>(); 
        shots.add(aNEW);
	shots.addAll(getShotsList(project, seq));

        toReturn.put(project, seq, shots);
      }

      ArrayList<String> shots = new ArrayList<String>();
      shots.add(aNEW);
      toReturn.put(project, aNEW, shots);
    }

    return toReturn;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   O V E R R I D E S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Disable the lookup of the current pass since this class should never be used 
   * in this way.
   */ 
  @Override
  public int 
  getCurrentPass()
  {
    throw new IllegalArgumentException("This should never be called!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P U B L I C   S T A T I C S                                                          */
  /*----------------------------------------------------------------------------------------*/

  public final static String aProjectName  = "ProjectName";
  public final static String aSequenceName = "SequenceName";
  public final static String aShotName     = "ShotName";

  public final static String aNEW = "[[NEW]]";
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8108986071922344646L;

  private final static String aProjectsParent  = "projects";
  private final static String aSequencesParent = "shots";
  private final static String aAssetsParent    = "assets"; 
  
}
