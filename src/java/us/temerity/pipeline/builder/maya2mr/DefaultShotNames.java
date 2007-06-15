package us.temerity.pipeline.builder.maya2mr;

import java.util.*;

import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.BaseNames;
import us.temerity.pipeline.builder.UtilContext;


public class DefaultShotNames
  extends BaseNames
  implements BuildsShotNames
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  DefaultShotNames
  (
    String project,
    AnswersBuilderQueries builderInfo  
  ) 
    throws PipelineException
  {
    super("DefaultShotNames", 
          "The basic naming class for a shot provided by Temerity");
    
    pBuilderInfo = builderInfo;
    pProject = project;
    
    
    
  }
  
  protected TreeMap<String, TreeSet<String>>
  getMoviesAndSequences
  (
    String project
  ) 
    throws PipelineException
  {
    TreeMap<String, TreeSet<String>> toReturn = new TreeMap<String, TreeSet<String>>();
    for (String movie : pBuilderInfo.getMovieList(project)) {
      TreeSet<String> seqs = new TreeSet<String>();
      for (String seq : pBuilderInfo.getSequenceList(project, movie))
	seqs.add(seq);
      toReturn.put(movie, seqs);
    }
    return toReturn;
  }
  
  @Override
  public void generateNames()
    throws PipelineException
  {
    setGlobalContext((UtilContext) getParamValue(aUtilContext));
  }

  public String getAnimExportNodeName()
  {
    return null;
  }

  public String getAnimNodeName()
  {
    return null;
  }

  public ArrayList<String> getAssetNames()
  {
    return null;
  }

  public String getBlastNodeName()
  {
    return null;
  }
  
  public String
  getCameraNodeName()
  {
    return null;
  }

  public String getCamOverMiNodeName(
    String passName)
  {
    return null;
  }

  public String getCameraMiNodeName()
  {
    return null;
  }

  public String getCollateNodeName()
  {
    return null;
  }

  public String getCollateNodeName(
    String assetName)
  {
    return null;
  }

  public String getGeoInstMiNodeName(
    String assetName)
  {
    return null;
  }

  public String getGeoMiNodeName(
    String assetName)
  {
    return null;
  }

  public String getImageNodeName()
  {
    return null;
  }

  public String getImageNodeName(
    String passName)
  {
    return null;
  }

  public String getImageNodeName(
    String passName,
    String assetName)
  {
    return null;
  }

  public String getLayoutExportNodeName()
  {
    return null;
  }

  public String getLayoutNodeName()
  {
    return null;
  }

  public String getLightMiNodeName
  (
    String passName
  )
  {
    return null;
  }

  public String 
  getLightShaderDefMiNodeName()
  {
    return null;
  }

  public String getLightingNodeName()
  {
    return null;
  }

  public String getMovieName()
  {
    return null;
  }

  public String getOptionMiNodeName(
    String passName)
  {
    return null;
  }

  public String getSequenceName()
  {
    return null;
  }

  public String getShaderDefMiNodeName(
    String passName,
    String assetName)
  {
    return null;
  }

  public String getShaderMiNodeName(
    String passName,
    String assetName)
  {
    return null;
  }

  public String getShotName()
  {
    return null;
  }

  public String getSwitchNodeName()
  {
    return null;
  }

  public String getTopNodeName()
  {
    return null;
  }

  private AnswersBuilderQueries pBuilderInfo;
  
  private String pProject;
  
}
