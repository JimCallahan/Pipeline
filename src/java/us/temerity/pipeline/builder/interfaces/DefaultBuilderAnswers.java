package us.temerity.pipeline.builder.interfaces;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.interfaces.AnswersBuilderQueries;

public 
class DefaultBuilderAnswers
  extends BaseUtil
  implements AnswersBuilderQueries
{
  public 
  DefaultBuilderAnswers
  (
    UtilContext context
  ) 
    throws PipelineException 
  {
    super("BasicBuilderImplemention", 
          "The basic implementation of the AnswersBuilderQueries, provided by Temerity", 
          context);
  }

  public ArrayList<String> 
  getProjectList() 
    throws PipelineException
  {
    return findChildBranchNames(new Path("/projects"));
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
  
  public void
  setContext
  (
    UtilContext context
  )
  {
    pContext = context;
  }

  private static final String sGlobalMelPath = "/global/assets/tools/mel";
  private static final String sProjectMelEndPath = "assets/tools/mel";
  
  private static final long serialVersionUID = 7387762854066534169L;
}
