// $Id: ExternalCollection.java,v 1.1 2009/04/16 17:57:25 jesse Exp $

package com.nathanlove.pipeline.plugin.ExternalCollection.v1_0_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;


public 
class ExternalCollection
  extends BaseBuilderCollection
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  ExternalCollection()
  {
    super("ExternalCollection", new VersionID("1.0.0"), "NathanLove", 
          "A test collection for constructor instantiation");
    
    LayoutGroup group = new LayoutGroup(true);
    group.addEntry("External");
    
    
    setLayout(group);
    
    underDevelopment();
  }

  @Override
  public TreeMap<String, String> 
  getBuildersProvided()
  {
    TreeMap<String, String> toReturn = new TreeMap<String, String>();
    toReturn.put("External", "com.nathanlove.pipeline.plugin.ExternalCollection.v1_0_0.ExternalBuilder");
    
    return toReturn;
  }

  private static final long serialVersionUID = 2157200429412917668L;
}
