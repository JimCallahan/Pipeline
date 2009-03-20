// $Id: AnnotationContext.java,v 1.1 2009/03/20 03:10:38 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   A N N O T A T I O N   C O N T E X T                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The contexts in which Annotation plugins are used.
 */
public
enum AnnotationContext
{  
  /**
   * Annotations associated with all versions of a node independent of any one version.
   */
  PerNode, 

  /**
   * Annotations which are part of the information contained within a node version.
   */
  PerVersion; 



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible values.
   */ 
  public static ArrayList<AnnotationContext>
  all() 
  {
    AnnotationContext values[] = values();
    ArrayList<AnnotationContext> all = new ArrayList<AnnotationContext>(values.length);
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
    for(AnnotationContext policy : AnnotationContext.all()) 
      titles.add(policy.toTitle());
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

  private static final String sTitles[] = {
    "Per-Node", 
    "Per-Version"
  };
  
}
