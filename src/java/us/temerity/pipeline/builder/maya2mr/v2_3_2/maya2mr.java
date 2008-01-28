// $Id: maya2mr.java,v 1.1 2008/01/28 11:46:11 jesse Exp $

package us.temerity.pipeline.builder.maya2mr.v2_3_2;

import java.util.TreeMap;

import us.temerity.pipeline.LayoutGroup;
import us.temerity.pipeline.VersionID;
import us.temerity.pipeline.builder.BaseBuilderCollection;

/*------------------------------------------------------------------------------------------*/
/*   m a y a 2 m r                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A collection of builders to make networks related to production pipelines using Maya and
 * rendering with Mental Ray
 */
public 
class maya2mr
  extends BaseBuilderCollection
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  maya2mr()
  {
    super("Maya2MR", new VersionID("2.3.2"), "Temerity", 
          "A collection of builders to make networks related to production pipelines " +
          "using Maya and rendering with Mental Ray");
    
    LayoutGroup group = new LayoutGroup(true);
    {
      LayoutGroup projects = 
        new LayoutGroup("ProjectBuilders", "Builders for global project construction", true);
      projects.addEntry("Project");
      projects.addEntry("ProjectScript");
      projects.addEntry("ProjectTurntable");
      group.addSubGroup(projects);
    }
    {
      LayoutGroup assets = 
        new LayoutGroup("AssetBuilders", "Builders for assets", true);
      assets.addEntry("SimpleAsset");
      assets.addEntry("Asset");
      assets.addEntry("AdvAsset");
      assets.addEntry("NewAsset");
      group.addSubGroup(assets);
    }
    
    {
      LayoutGroup assets = 
        new LayoutGroup("ShotBuilders", "Builders for shots", true);
      assets.addEntry("CurveShot");
      group.addSubGroup(assets);
    }
    
    setLayout(group);
    underDevelopment();
  }
  
  @Override
  public TreeMap<String, String> 
  getListOfBuilders()
  {
    TreeMap<String, String> toReturn = new TreeMap<String, String>();
    toReturn.put("Asset", "us.temerity.pipeline.builder.maya2mr.v2_3_2.AssetBuilder");
    toReturn.put("AdvAsset", "us.temerity.pipeline.builder.maya2mr.v2_3_2.AdvAssetBuilder");
    toReturn.put("NewAsset", "us.temerity.pipeline.builder.maya2mr.v2_3_2.NewAssetBuilder");
    toReturn.put("Project", "us.temerity.pipeline.builder.maya2mr.v2_3_2.ProjectBuilder");
    toReturn.put("ProjectScript", "us.temerity.pipeline.builder.maya2mr.v2_3_2.ProjectScriptBuilder");
    toReturn.put("ProjectTurntable", "us.temerity.pipeline.builder.maya2mr.v2_3_2.ProjectTurntableBuilder");
    toReturn.put("SimpleAsset", "us.temerity.pipeline.builder.maya2mr.v2_3_2.SimpleAssetBuilder");
    toReturn.put("CurveShot", "us.temerity.pipeline.builder.maya2mr.v2_3_2.CurveShotBuilder");
    
    return toReturn;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2016627948131568812L;
}
