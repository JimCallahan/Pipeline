// $Id: InternalCollection.java,v 1.1 2009/04/16 17:57:25 jesse Exp $

package com.nathanlove.pipeline.plugin.InternalCollection.v1_0_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;


public 
class InternalCollection
  extends BaseBuilderCollection
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  InternalCollection()
  {
    super("InternalCollection", new VersionID("1.0.0"), "NathanLove", 
          "A test collection for constructor instantiation");
    
    LayoutGroup group = new LayoutGroup(true);
    group.addEntry("Internal");
    
    
    setLayout(group);
    
    underDevelopment();
  }

  @Override
  public TreeMap<String, String> 
  getBuildersProvided()
  {
    TreeMap<String, String> toReturn = new TreeMap<String, String>();
    toReturn.put("Internal", "com.nathanlove.pipeline.plugin.InternalCollection.v1_0_0.InternalBuilder");
    
    return toReturn;
  }
  
  private static final long serialVersionUID = -2653665443516047871L;
}
