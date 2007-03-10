package us.temerity.pipeline.builder.interfaces;

import java.util.*;

import us.temerity.pipeline.PipelineException;

public 
interface AnswersBuilderQueries
{
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
