// $Id: RadarMaya2MRCollection.java,v 1.1 2008/01/30 20:00:13 jesse Exp $

package com.radar.pipeline.plugin.RadarMaya2MRCollection.v2_3_2;

import java.util.TreeMap;

import us.temerity.pipeline.LayoutGroup;
import us.temerity.pipeline.VersionID;
import us.temerity.pipeline.builder.BaseBuilderCollection;

/**
 * A collection of builders to make networks related to production pipelines using Maya and
 * rendering with Mental Ray writen for Radar films.
 */
public 
class RadarMaya2MRCollection
  extends BaseBuilderCollection
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  RadarMaya2MRCollection() 
  {
    super("Maya2MR", new VersionID("2.3.2"), "Radar", 
          "A collection of builders to make networks related to production pipelines " +
          "using Maya and rendering with Mental Ray writen for Radar Films");
    
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
      group.addSubGroup(assets);
    }
    
    {
      LayoutGroup assets = 
        new LayoutGroup("ShotBuilders", "Builders for shots", true);
      assets.addEntry("Shot");
      group.addSubGroup(assets);
    }
    setLayout(group);
    underDevelopment();
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns a list of all the builders that the this collection has in it, followed
   * by the full classpath to the class file that can be used to instantiate that builder.
   * <p>
   * All Builder Collections needs to override this method to return the list of builders
   * that they provide.
   * 
   * @return
   *   A mapping of Builder names to the classpath for the Builder.  By default, 
   *   this returns an empty TreeMap.
   */
  @Override
  public TreeMap<String, String> 
  getListOfBuilders()
  {
    String pkg = "us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.";

    TreeMap<String, String> toReturn = new TreeMap<String, String>();
    toReturn.put("Asset", pkg + "RadarAssetBuilder");
    toReturn.put("Project", pkg + "RadarProjectBuilder");
    toReturn.put("ProjectScript", pkg + "RadarProjectScriptBuilder");
    toReturn.put("ProjectTurntable", pkg + "RadarProjectTurntableBuilder");
    toReturn.put("SimpleAsset", pkg + "RadarSimpleAssetBuilder");
    toReturn.put("Shot", pkg + "RadarShotBuilder");
    
    return toReturn;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4480596783482271833L;

}
