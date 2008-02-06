// $Id: WtmCollection.java,v 1.2 2008/02/06 07:21:06 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import java.util.TreeMap;

import us.temerity.pipeline.VersionID;
import us.temerity.pipeline.builder.BaseBuilderCollection;


/*------------------------------------------------------------------------------------------*/
/*   W T M   C O L L E C T I O N                                                            */
/*------------------------------------------------------------------------------------------*/

public 
class WtmCollection
  extends BaseBuilderCollection
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  	
  public 
  WtmCollection()
  {
    super("WtmBuilders", new VersionID("1.0.0"), "ICVFX", 
          "A collection of builders to make networks related to the WTM project.");
    
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
    String pkg = "com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.";

    TreeMap<String, String> toReturn = new TreeMap<String, String>();
    toReturn.put("Plates", pkg + "PlatesBuilder");
    
    return toReturn;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -112789036519024010L;

}
