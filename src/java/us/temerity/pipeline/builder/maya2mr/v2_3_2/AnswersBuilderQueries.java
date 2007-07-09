package us.temerity.pipeline.builder.maya2mr.v2_3_2;

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
  
  public ArrayList<String>
  getMovieList
  (
    String project
  )
    throws PipelineException;
  
  public ArrayList<String>
  getSequenceList
  (
    String project,
    String movie
  )
    throws PipelineException;
  
  public ArrayList<String>
  getShotList
  (
    String project,
    String movie,
    String sequence
  )
    throws PipelineException;
  
  public TreeMap<String, String> 
  getScriptList
  (
    String project, 
    String scriptType
  ) 
    throws PipelineException;
  
  public ArrayList<String>
  getListOfAssets
  (
    String project, 
    String assetType
  )
    throws PipelineException;

}
