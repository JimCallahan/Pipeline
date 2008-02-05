package com.theorphanage.pipeline.plugin.ZohanCollection.v1_0_0;

import java.util.ArrayList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.BaseUtil;

public 
class StudioDefinitions 
  extends BaseUtil 
{

  public 
  StudioDefinitions
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient
  ) 
    throws PipelineException 
  {
    super("StudioDefinitions",
          "Provides basic information about where things are located in the default Orphanage Pipeline setup.",
          mclient,
          qclient);
  }
  
  /**
   * Gets a list of all the projects that current exist
   */
  public ArrayList<String> 
  getProjectList() 
    throws PipelineException
  {
    return findChildBranchNames(aProjectStartPath);
  }

  /**
   * Returns the full Pipeline path to a project
   */
  public static Path
  getProjectPath
  (
    String project    
  )
  {
    return new Path(aProjectStartPath, project);
  }
  
  public static Path
  getStandardsPath
  (
    String project  
  )
  {
    return new Path(getProjectPath(project), aStandardsStart);
  }
  
  public static Path
  getSequencePath
  (
    String project,
    String sequence
  )
  {
    return new Path(getSequenceStartPath(project), sequence);
  }
  
  public static Path
  getSequenceStartPath
  (
    String project
  )
  {
    return new Path(getProjectPath(project), aSequenceStart);
  }
  
  public static Path
  getShotPath
  (
    String project,
    String sequence,
    String shot
  )
  {
    String fullName = sequence + shot;
    return new Path(getSequencePath(project, sequence), fullName);
  }
  
  public ArrayList<String>
  getSequenceList
  (
    String project
  )
    throws PipelineException
  {
    return findChildBranchNames(getSequenceStartPath(project));
  }
  
  public ArrayList<String>
  getShotList
  (
    String project,
    String sequence
  )
    throws PipelineException
  {
    return findChildBranchNames(getSequencePath(project, sequence));
  }
  
  public DoubleMap<String, String, ArrayList<String>>
  getAllProjectsAllNames() 
    throws PipelineException
  {
    DoubleMap<String, String, ArrayList<String>> toReturn = new DoubleMap<String, String, ArrayList<String>>();
    for (String project : getProjectList()) {
      for (String seq : getSequenceList(project)) {
        toReturn.put(project, seq, getShotList(project, seq));
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
      for (String seq : getSequenceList(project)) {
        ArrayList<String> shots = new ArrayList<String>();
        shots.add(aNEW);
        shots.addAll(getShotList(project, seq));
        toReturn.put(project, seq, shots);
      }
      ArrayList<String> shots = new ArrayList<String>();
      shots.add(aNEW);
      toReturn.put(project, aNEW, shots);
    }
    return toReturn;
  }
  

  public static Path aProjectStartPath = new Path("/Projects");
  public static String aSequenceStart  = "VFX";
  public static String aStandardsStart  = "Standards";
  
  public static String aNEW  = "[[NEW]]";
  private static final long serialVersionUID = -6164301048865508539L;

  
  
  @Override
  public int 
  getCurrentPass()
  {
    throw new IllegalArgumentException("Don't do this shit");
  }

}
