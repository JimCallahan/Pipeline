package com.theorphanage.pipeline.plugin.ZohanCollection.v1_0_0;

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
    super("RotoNamer", 
        "The basic naming class for shots in Zohan",
        mclient,
        qclient);
    
    {
      UtilityParam param =
        new StringUtilityParam
        (aProjectName,
         "The Name of the Project the shot is being added to.", 
         null);
      addParam(param);
    }

    {
      UtilityParam param =
        new StringUtilityParam
        (aSequenceName,
         "The Sequence the shot is being created in..",
         null);
      addParam(param);
    }
    {
      UtilityParam param =
        new StringUtilityParam
        (aShotName,
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
    
    pStartPaths = new DoubleMap<Discipline, SubDir, Path>();

    pProject = getStringParamValue(new ParamMapping(aProjectName));
    pSequenceName = getStringParamValue(new ParamMapping(aSequenceName));
    pShotName = getStringParamValue(new ParamMapping(aShotName));
    
    pFullName = pSequenceName + pShotName;  
    
    Path startPath = StudioDefinitions.getShotPath(pProject, pSequenceName, pShotName);
    for (Discipline discipline : Discipline.values()) {
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
  getSequenceName()
  {
    return pSequenceName;
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
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S O U R C E                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the full Pipeline node name of a plate.
   * 
   * @param plateName
   *   The Element Type, Format, and Descriptor that should be appended to the plate  
   */
  public String
  getPlateName
  (
    String plateName
  )
  {
    return new Path(getPlatePath(), join(pFullName, plateName)).toString();
  }
  
  /**
   * Get the Pipeline node Path to the directory where plates are stored.
   */
  public Path
  getPlatePath()
  {
    return pStartPaths.get(Discipline.Source, SubDir.work);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   R O T O   P A I N T                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The silhouette scene for doing the roto.
   */
  public String
  getRotoScene()
  {
    return new Path(pStartPaths.get(Discipline.RotoPaint, SubDir.work), join(pFullName, "roto")).toString();
  }
  
  /**
   * A matte exported out of silhouette
   * @param descriptor
   *   5 Character max descriptor of what is in the matte.
   */
  public String
  getMatteName
  (
    String descriptor
  )
  {
    String matName = join(descriptor, "MAT");
    return new Path(new Path(pStartPaths.get(Discipline.RotoPaint, SubDir.output), matName), join(pFullName, matName) ).toString();
  }
  

  /**
   * A final matte, copied from one exported out of silhouette, to be used in comping.
   * @param descriptor
   *   5 Character max descriptor of what is in the matte.
   */
  public String
  getFinalMatteName
  (
    String descriptor
  )
  {
    String matName = join(descriptor, "MAT");
    return new Path(new Path(pStartPaths.get(Discipline.RotoPaint, SubDir.output), matName), join(join(pFullName, matName), "FINAL") ).toString();
  }
  
  /**
   * A comp node that combines the matte with the plate for supervisor verification.
   * @param descriptor
   *   5 Character max descriptor of what is in the matte.
   */
  public String
  getMatteTestCompScene
  (
    String descriptor    
  )
  {
    String matName = join(descriptor, "MAT");
    return new Path(pStartPaths.get(Discipline.RotoPaint, SubDir.assembly), join(join(pFullName, matName), "TEST") ).toString();
  }
  
  /**
   * A image node that contains a render of the test comp scene..
   * @param descriptor
   *   5 Character max descriptor of what is in the matte.
   */
  public String
  getMatteTestRenderName
  (
    String descriptor    
  )
  {
    String matName = join(descriptor, "MAT");
    return new Path(pStartPaths.get(Discipline.RotoPaint, SubDir.takes), join(join(pFullName, matName), "TEST") ).toString();
  }
  
  /**
   * Node for artists to submit their work with.
   */
  public String
  getRotoSubmitName()
  {
    return getSubmitNode(pStartPaths.get(Discipline.RotoPaint, SubDir.QC)); 
  }
  
  /**
   * Root of network to run when all mattes are approved. 
   * @return
   */
  public String
  getRotoApprovalName()
  {
    return getRotoApprovalName("ALL"); 
  }
  
  /**
   * Root of network to run when all mattes are approved. 
   * @return
   */
  public String
  getRotoApprovalName
  (
    String descriptor    
  )
  {
    return getApprovalNode(pStartPaths.get(Discipline.RotoPaint, SubDir.QC), descriptor); 
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O M P                                                                              */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The actual composite scene.
   */
  public String
  getCompScene()
  {
    return new Path(pStartPaths.get(Discipline.Comp, SubDir.work), join(pFullName, "CMP")).toString();
  }
  
  /**
   * A image node that contains a render of the comp scene.
   */
  public String
  getCompTestRenderName()
  {
    return new Path(pStartPaths.get(Discipline.Comp, SubDir.output), pFullName ).toString();
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
    String seqStart = pieces.remove(0);
    if (!seqStart.equals(StudioDefinitions.aSequenceStart))
      return null;
    String sequence = pieces.remove(0);
    String shot = pieces.remove(0).replaceFirst(sequence, "");
    ShotNames namer = null;
    try {
      namer = new ShotNames(mclient, qclient);
      namer.setParamValue(aProjectName, project);
      namer.setParamValue(aSequenceName, sequence);
      namer.setParamValue(aShotName, shot);
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
  
  public final static String aProjectName = "ProjectName";

  public final static String aSequenceName = "SequenceName";
  
  public final static String aShotName = "ShotName";
  
  private StudioDefinitions pDefs;
  
  private String pProject;
  
  private String pSequenceName;
  
  private String pShotName;
  
  private String pFullName;
  
  private DoubleMap<Discipline, SubDir, Path> pStartPaths;
  
}
