package com.radar.pipeline.builder.maya2mr.v2_3_2;

import java.util.ArrayList;
import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.BaseUtil;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.AnswersBuilderQueries;


public 
class RadarBuilderAnswers
  extends BaseUtil
  implements AnswersBuilderQueries
{

  public 
  RadarBuilderAnswers
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    UtilContext context
  ) 
    throws PipelineException 
  {
    super("RadarImplemention",
      	  new VersionID("2.3.2"),
      	  "Radar",
          "The Radar implementation of the AnswersBuilderQueries",
          mclient,
          qclient,
          context);
  }
  
  public ArrayList<String> 
  getProjectList() 
    throws PipelineException
  {
    return findChildBranchNames(new Path("/projects"));
  }
  
  public ArrayList<String>
  getMovieList
  (
    String project
  )
    throws PipelineException
  {
    Path search = new Path(new Path(new Path("/projects"), project), "shotWork");
    return findChildBranchNames(search);
  }
  
  public ArrayList<String>
  getSequenceList
  (
    String project,
    String movie
  )
    throws PipelineException
  {
    Path search;
    if (movie == null)
      search = new Path(new Path(new Path("/projects"), project), "shotWork");
    else
      search = new Path(new Path(new Path(new Path("/projects"), project), "shotWork"), movie);
    return findChildBranchNames(search);
  }
  
  public ArrayList<String>
  getShotList
  (
    String project,
    String movie,
    String sequence
  )
    throws PipelineException
  {
    Path search;
    if (movie == null)
      search = new Path(new Path(new Path(new Path("/projects"), project), "shotWork"), sequence);
    else
      search = new Path(new Path(new Path(new Path(new Path("/projects"), project), "shotWork"), movie), sequence);
    return findChildBranchNames(search);
  }
  
  public TreeMap<String, String> 
  getScriptList
  (
    String project, 
    String scriptType
  ) 
    throws PipelineException
  {
    TreeMap<String, String> toReturn = new TreeMap<String, String>();
    if(scriptType.equals("mel")) {
//      ArrayList<String> globalMel = findAllChildNodeNames(sGlobalMelPath);
//
//      for(String path : globalMel) {
//	Path p = new Path(path);
//	toReturn.put("global: " + p.getName(), path);
//      }
      if(project != null) {
	String searchPath = "/projects/" + project + "/" + sProjectMelEndPath;
	ArrayList<String> projectMel = findAllChildNodeNames(searchPath);
	for(String path : projectMel) {
	  Path p = new Path(path);
	  toReturn.put("proj: " + p.getName(), path);
	}
      }
    }
    return toReturn;
  }
  
  public ArrayList<String>
  getListOfAssets
  (
    String project,
    String assetType
  ) 
    throws PipelineException
  {
    String searchPath = "/projects/" + project + "/buildOutput/" + assetType;
    return findChildBranchNames(new Path(searchPath));
  }

  @Override
  public int getCurrentPass()
  {
    throw new IllegalArgumentException("Don't do this shit");
  }

  //private static final String sGlobalMelPath = "/global/assets/tools/mel";
  private static final String sProjectMelEndPath = "projectScripts/mel";
  
  private static final long serialVersionUID = 8384935565757702273L;
}
