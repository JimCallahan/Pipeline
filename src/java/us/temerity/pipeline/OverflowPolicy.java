// $Id: OverflowPolicy.java,v 1.4 2004/06/28 23:00:16 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   O V E R F L O W   P O L I C Y                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The frame range overflow policy. <P> 
 * 
 * During generation of jobs to execute the regneration action for nodes with a 
 * {@link LinkRelationship LinkRelationship} of {@link LinkRelationship#OneToOne OneToOne}, 
 * it is possible that the frame index offset of the node link may overflow the frame range
 * of the source node.  In these cases, this class provides the policy Pipeline will adopt
 * in handling this frame range overflow.
 *
 * @see NodeMod
 * @see NodeVersion
 */
public
enum OverflowPolicy
{  
  /** 
   * Any frame range overflow will be silently ignored. 
   */
  Ignore, 

  /**
   * Frame range overflows will cause Pipeline to abort generation of overflowed jobs and 
   * report these overflows to the user.
   */
  Abort;



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible values.
   */ 
  public static ArrayList<OverflowPolicy>
  all() 
  {
    OverflowPolicy values[] = values();
    ArrayList<OverflowPolicy> all = new ArrayList<OverflowPolicy>(values.length);
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
    for(OverflowPolicy policy : OverflowPolicy.all()) 
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
    return toString();
  }
}
