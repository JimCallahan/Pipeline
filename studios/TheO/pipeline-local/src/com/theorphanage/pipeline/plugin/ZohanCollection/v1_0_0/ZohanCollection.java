// $Id: ZohanCollection.java,v 1.3 2008/02/07 10:11:19 jesse Exp $

package com.theorphanage.pipeline.plugin.ZohanCollection.v1_0_0;

import java.util.TreeMap;

import us.temerity.pipeline.VersionID;
import us.temerity.pipeline.builder.BaseBuilderCollection;


public 
class ZohanCollection
  extends BaseBuilderCollection
{
  public 
  ZohanCollection()
  {
    super("ZohanBuilders", new VersionID("1.0.0"), "TheO", 
          "A collection of builders to make networks related to the ZohanCollection project");
    
    underDevelopment();
  }
  
  @Override
  public TreeMap<String, String> 
  getBuildersProvided()
  {
    TreeMap<String, String> toReturn = new TreeMap<String, String>();
    toReturn.put("Roto", "com.theorphanage.pipeline.plugin.ZohanCollection.v1_0_0.RotoBuilder");
    
    return toReturn;
  }
  private static final long serialVersionUID = -6297879651395440947L;
}
