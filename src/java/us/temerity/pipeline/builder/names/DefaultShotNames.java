package us.temerity.pipeline.builder.names;

import java.util.ArrayList;

import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.BaseNames;


public class DefaultShotNames
  extends BaseNames
  implements BuildsShotNames
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  DefaultShotNames() 
    throws PipelineException
  {
    super("DefaultShotNames", 
          "The basic naming class for a shot provided by Temerity");
  }
  
  @Override
  public void generateNames()
    throws PipelineException
  {
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

  public String getLightMiNodeName(
    String passName)
  {
    return null;
  }

  public String getLightShaderDefMiNodeName()
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

}
