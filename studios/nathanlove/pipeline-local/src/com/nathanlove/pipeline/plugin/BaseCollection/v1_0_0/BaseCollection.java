// $Id: BaseCollection.java,v 1.3 2009/05/12 03:22:29 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   C O L L E C T I O N                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A collection of basic builders for Nathan Love
 */
public 
class BaseCollection
  extends BaseBuilderCollection
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  BaseCollection()
  {
    super("BaseBuilders", new VersionID("1.0.0"), "NathanLove", 
          "A collection of basic builders for Nathan Love");
    
    LayoutGroup group = new LayoutGroup(true);
    group.addEntry("Project");
    group.addEntry("Asset");
    group.addEntry("SimpleAsset");
    group.addSeparator();
    group.addEntry("CurveShot");
    group.addSeparator();
    group.addEntry("ShadeApproveTask");
    group.addEntry("AnimApproveTask");
    group.addEntry("LgtApproveTask");
    group.addSeparator();
    group.addEntry("Test");
    
    
    setLayout(group);
    
    underDevelopment();
  }
  
  @Override
  public TreeMap<String, String> 
  getBuildersProvided()
  {
    TreeMap<String, String> toReturn = new TreeMap<String, String>();
    toReturn.put("Project", "com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.ProjectBuilder");
    toReturn.put("Asset", "com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.AssetBuilder");
    toReturn.put("SimpleAsset", "com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.SimpleAssetBuilder");
    toReturn.put("ShadeApproveTask", "com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.ShadeApproveTaskBuilder");
    toReturn.put("CurveShot", "com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.CurveShotBuilder");
    toReturn.put("LgtApproveTask", "com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.LgtApproveTaskBuilder");
    toReturn.put("AnimApproveTask", "com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.AnimApproveTaskBuilder");
    toReturn.put("Test", "com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.TestBuilder");
    
    return toReturn;
  }
  
  private static final long serialVersionUID = 4808141228748422417L;
}
