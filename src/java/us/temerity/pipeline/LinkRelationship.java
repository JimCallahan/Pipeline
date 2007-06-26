// $Id: LinkRelationship.java,v 1.6 2007/06/26 18:22:50 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   L I N K   R E L A T I O N S H I P                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The nature of the relationship between files associated with the source and target  
 * nodes of a node link. <P> 
 *
 * @see LinkMod
 * @see LinkVersion
 */
public
enum LinkRelationship
{  
  /**
   * This is only used for Association links where there can be no relationship 
   * between source and target files.
   */ 
  None, 

  /**
   * There is a one-to-one relationship between files associated with the target and 
   * source nodes. <P> 
   * 
   * In other words, changes to an individual file associated with the source node 
   * only has the potential to affect a single file associated with the target node.
   */
  OneToOne,

  /**
   * There is a all-to-all relationship between files associated with the target and 
   * source nodes. <P> 
   * 
   * In other words, changes to any of the files associated with the source node has
   * the potential to affect all of the files associated with the target node.
   */
  All;



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible values.
   */ 
  public static ArrayList<LinkRelationship>
  all() 
  {
    LinkRelationship values[] = values();
    ArrayList<LinkRelationship> all = new ArrayList<LinkRelationship>(values.length);
    int wk;
    for(wk=0; wk<values.length; wk++)
      all.add(values[wk]);
    return all;
  }

  /**
   * Get the list of human friendly string representation for all possible values.
   */ 
  public static ArrayList<String>
  titles() 
  {
    ArrayList<String> titles = new ArrayList<String>();
    for(LinkRelationship rel : LinkRelationship.all()) 
      titles.add(rel.toTitle());
    return titles;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert to a more human friendly string representation.
   */ 
  public String
  toTitle() 
  {
    return sTitles[ordinal()];
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static String sTitles[] = {
    "None", 
    "1:1", 
    "All"
  };
}
