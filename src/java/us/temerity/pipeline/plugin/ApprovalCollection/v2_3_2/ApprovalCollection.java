// $Id: ApprovalCollection.java,v 1.2 2008/02/04 12:12:48 jim Exp $

package us.temerity.pipeline.plugin.ApprovalCollection.v2_3_2;

import java.util.TreeMap;

import us.temerity.pipeline.VersionID;
import us.temerity.pipeline.builder.BaseBuilderCollection;

/*------------------------------------------------------------------------------------------*/
/*   A P P R O V A L   C O L L E C T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The most simple approval builder.
 */
public class ApprovalCollection
  extends BaseBuilderCollection
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  ApprovalCollection()
  {
    super("Approval", new VersionID("2.3.2"), "Temerity", 
          "The most simple approval builder.");
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
  getBuildersProvided()
  {
    String pkg = "us.temerity.pipeline.plugin.ApprovalCollection.v2_3_2.";

    TreeMap<String, String> toReturn = new TreeMap<String, String>();
    toReturn.put("Approval", pkg + "ApprovalBuilder");
    
    return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3412227340562064347L;

}
