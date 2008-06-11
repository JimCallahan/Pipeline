// $Id: ProjectSetterUpper.java,v 1.1 2008/06/11 12:48:23 jim Exp $

package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   C O L L E C T I O N                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A collection of basic builders for the future of our children
 */
public 
class ProjectSetterUpper
  extends BaseBuilderCollection
{

/*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  ProjectSetterUpper()
  {
    super("ProjectSetterUpper", new VersionID("1.1.0"), "SCEA", 
          "A collection of basic builders for SCEA");
    
    LayoutGroup group = new LayoutGroup(true);
    group.addEntry("Project");
    group.addEntry("Asset");
    group.addEntry("SimpleAsset");
    group.addEntry("CurveShot");
    //group.addSeparator();
    
    setLayout(group);
    
    underDevelopment();
  }
  
  @Override
  public TreeMap<String, String> 
  getBuildersProvided()
  {
    TreeMap<String, String> toReturn = new TreeMap<String, String>();
    toReturn.put("Project", "com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.ProjectBuilder");
    toReturn.put("Asset", "com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.AssetBuilder");
    toReturn.put("SimpleAsset", "com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.SimpleAssetBuilder");
    toReturn.put("CurveShot", "com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.CurveShotBuilder");

    return toReturn;
  }
  
  private static final long serialVersionUID = -5410510192346720590L;
}
