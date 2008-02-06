// $Id: Maya2MRCollection.java,v 1.3 2008/02/06 05:11:29 jesse Exp $

package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2;

import java.util.TreeMap;

import us.temerity.pipeline.LayoutGroup;
import us.temerity.pipeline.VersionID;
import us.temerity.pipeline.builder.BaseBuilderCollection;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A 2 M R   C O L L E C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A collection of builders to make networks related to production pipelines using Maya and
 * rendering with Mental Ray
 */
public 
class Maya2MRCollection
  extends BaseBuilderCollection
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  Maya2MRCollection() 
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
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns a table of the fullly resolved node builder class names indexed by the short 
   * names of the builders provided by this collection.<P> 
   * 
   * All builder collections should override this method to return information about 
   * the specific builders they provide.  The key in the same should be identical to that 
   * returned by the {@link BaseBuilder.getName() BaseBuilder#getName} method.
   * 
   * @return
   *   The mapping of short builder names to the full class name of the builder.  By default
   *   an empty TreeMap is returned.
   */
  @Override
  public TreeMap<String, String> 
  getBuildersProvided()
  {
    String pkg = "us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.";

    TreeMap<String, String> toReturn = new TreeMap<String, String>();
    toReturn.put("Asset", pkg + "AssetBuilder");
    toReturn.put("AdvAsset", pkg + "AdvAssetBuilder");
    toReturn.put("NewAsset", pkg + "NewAssetBuilder");
    toReturn.put("Project", pkg + "ProjectBuilder");
    toReturn.put("ProjectScript", pkg + "ProjectScriptBuilder");
    toReturn.put("ProjectTurntable", pkg + "ProjectTurntableBuilder");
    toReturn.put("SimpleAsset", pkg + "SimpleAssetBuilder");
    toReturn.put("CurveShot", pkg + "CurveShotBuilder");
    
    return toReturn;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2016627948131568812L;
}
