package us.temerity.pipeline.builder.interfaces;

import java.util.*;

import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.UtilContext;

public 
interface AnswersBuilderQueries
{
  public void
  setContext
  (
    UtilContext context
  );
  
  public ArrayList<String> 
  getProjectList() 
    throws PipelineException;
  
  public TreeMap<String, String> 
  getScriptList
  (
    String project, 
    String scriptType
  ) 
    throws PipelineException;
  

}
