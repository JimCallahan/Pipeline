// $Id: ExecutionMethod.java,v 1.4 2005/04/04 08:35:59 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   E X E C U T I O N   M E T H O D                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The methodology for regenerating the files associated with nodes with regeneration
 * actions. <P> 
 * 
 * Note that there is no difference between the following execution policies if the
 * primary file sequence for the node only contains a single file.
 *
 * @see NodeMod
 * @see NodeVersion
 */
public
enum ExecutionMethod
{  
  /** 
   * All files associated with the node must be generated in a single execution of the 
   * regeneration action. <P> 
   * 
   * This means that if any of the files associated with a node need regeneration, then 
   * all files will be regenerated.  This is typically used for nodes which represent
   * some form of physical simulation such as dynamics, particle systems, cloth and 
   * hair simulations. 
   */
  Serial, 

  /** 
   * Each file associated with the node may be generated by an independent execution of 
   * the regeneration action. <P> 
   * 
   * This is similar to the Parallel method execept that the Batch Size will be ignored
   * and all jobs will regenerate only a single frame.  Furthermore, the order of execution
   * is determined by recursively subdividing the frame range rather than incrementing 
   * through the frame range.
   */
  Subdivided, 

  /**
   * Each file associated with the node may be generated by an independent execution of 
   * the regeneration action. <P> 
   * 
   * Multiple files may still be regenerated by a single execution of the regeneration 
   * action depending on the setting of the batch size property (see 
   * {@link NodeCommon#getBatchSize getBatchSize}) of the node.  However it is possible that
   * each frame may be generated by a seperate action execution.  Furthermore, there is 
   * no dependencies between the frames and they may be executed on the same or different 
   * hosts at simultaneously.  This policy is typically used for rendering and compositing 
   * actions which have no inter-frame dependencies.
   */
  Parallel;



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible values.
   */ 
  public static ArrayList<ExecutionMethod>
  all() 
  {
    ExecutionMethod values[] = values();
    ArrayList<ExecutionMethod> all = new ArrayList<ExecutionMethod>(values.length);
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
    for(ExecutionMethod method : ExecutionMethod.all()) 
      titles.add(method.toTitle());
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

};
