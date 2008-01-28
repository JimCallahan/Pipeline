// $Id: approval.java,v 1.1 2008/01/28 11:46:11 jesse Exp $

package us.temerity.pipeline.builder.approval.v2_3_2;

import java.util.TreeMap;

import us.temerity.pipeline.VersionID;
import us.temerity.pipeline.builder.BaseBuilderCollection;

/*------------------------------------------------------------------------------------------*/
/*   a p p r o v a l                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The most simple approval builder.
 */
public class approval
  extends BaseBuilderCollection
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  approval()
  {
    super("Approval", new VersionID("2.3.2"), "Temerity", 
          "The most simple approval builder.");
  }
  
  @Override
  public TreeMap<String, String> 
  getListOfBuilders()
  {
    TreeMap<String, String> toReturn = new TreeMap<String, String>();
    toReturn.put("Approval", "us.temerity.pipeline.builder.approval.v2_3_2.ApprovalBuilder");
    
    return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3412227340562064347L;

}
