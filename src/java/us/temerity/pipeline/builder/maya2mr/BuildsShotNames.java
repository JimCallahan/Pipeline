package us.temerity.pipeline.builder.maya2mr;

import java.util.ArrayList;

public 
interface BuildsShotNames
{
  public String
  getMovieName();
  
  public String
  getSequenceName();
  
  public String
  getShotName();
  
  public String
  getCameraNodeName();

  public String
  getAnimNodeName();
  
  public String
  getAnimExportNodeName();
  
  public String
  getSwitchNodeName();
  
  public String
  getCollateNodeName();
  
  public String
  getLayoutNodeName();
  
  public String
  getLayoutExportNodeName();

  public String
  getLightingNodeName();
  
  public String
  getImageNodeName();
  
  public String
  getBlastNodeName();
  
  public String
  getTopNodeName();
  
  public ArrayList<String>
  getAssetNames();

  public String
  getCameraMiNodeName();
  
  public String
  getLightShaderDefMiNodeName();
  
  public String
  getGeoMiNodeName
  (
    String assetName
  );
  
  public String
  getGeoInstMiNodeName
  (
    String assetName
  );
  
  public String
  getCollateNodeName
  (
    String assetName
  );

  public String
  getImageNodeName
  (
    String passName
  );
  
  public String
  getLightMiNodeName
  (
    String passName
  );
  
  public String
  getOptionMiNodeName
  (
    String passName
  );
  
  public String
  getCamOverMiNodeName
  (
    String passName
  );
  
  public String
  getShaderMiNodeName
  (
    String passName,
    String assetName
  );

  public String
  getShaderDefMiNodeName
  (
    String passName,
    String assetName
  );

  public String
  getImageNodeName
  (
    String passName,
    String assetName
  );
}