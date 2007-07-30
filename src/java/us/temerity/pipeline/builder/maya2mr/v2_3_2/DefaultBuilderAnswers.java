package us.temerity.pipeline.builder.maya2mr.v2_3_2;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

public 
class DefaultBuilderAnswers
  extends BaseUtil
  implements AnswersBuilderQueries
{
  public 
  DefaultBuilderAnswers
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    UtilContext context
  ) 
    throws PipelineException 
  {
    super("BasicBuilderImplemention",
      	  new VersionID("2.3.2"),
      	  "Temerity",
          "The basic implementation of the AnswersBuilderQueries, provided by Temerity",
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
    Path search = new Path(new Path(new Path("/projects"), project), "prod");
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
      search = new Path(new Path(new Path("/projects"), project), "prod");
    else
      search = new Path(new Path(new Path(new Path("/projects"), project), "prod"), movie);
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
      search = new Path(new Path(new Path(new Path("/projects"), project), "prod"), sequence);
    else
      search = new Path(new Path(new Path(new Path(new Path("/projects"), project), "prod"), movie), sequence);
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
      ArrayList<String> globalMel = findAllChildNodeNames(sGlobalMelPath);

      for(String path : globalMel) {
	Path p = new Path(path);
	toReturn.put("global: " + p.getName(), path);
      }
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
    String searchPath = "/projects/" + project + "/assets/" + assetType;
    return findChildBranchNames(new Path(searchPath));
  }

  private static final String sGlobalMelPath = "/global/assets/tools/mel";
  private static final String sProjectMelEndPath = "assets/tools/mel";
  
  private static final long serialVersionUID = 7387762854066534169L;

  @Override
  public int getCurrentPass()
  {
    throw new IllegalArgumentException("Don't do this shit");
  }
}
