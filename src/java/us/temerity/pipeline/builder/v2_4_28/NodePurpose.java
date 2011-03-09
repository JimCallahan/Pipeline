package us.temerity.pipeline.builder.v2_4_28;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.TaskAnnotation.v2_4_28.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   P U R P O S E                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The purpose of each node within a task.
 */ 
public  
enum NodePurpose
{
  /**
   * A node that is checked-in to signal that the artist is done working on the task.
   */ 
  Submit,
  
  /**
   * A grouping node for any automated procedures that occur between submit and publish.
   * This can be used to verify that the data submitted by the artist is correct and also to
   * generate focus and thumbnail nodes (so that artists do not need to spend time waiting for
   * those jobs to finish.<p>
   * 
   * In some cases, the submit and verify nodes may be the same node.  In these cases, an 
   * artist submitting a node is also verifying it at the same time, making it immediately 
   * ready for publishing.
   */
  Verify,

  /**
   * A grouping node for all the product nodes that a task generates and which is queued and
   * checked-in after a task has been approved to make the products available to tasks
   * downstream.
   */ 
  Publish, 

  /**
   * Identifies the nodes that an artist should interactive modify in order to accomplish 
   * a task.
   */ 
  Edit,

  /**
   * A node used in a task network networks as part of the process which will generates one or
   * more of the other procedural types of nodes in the task but which has no direct utility
   * to either artists or supervisors.
   */ 
  Prepare, 

  /**
   * A node that that contains information that can be used to evaluate the task's submitted 
   * work.  Typically this is a movie file of some sort, either containing a playblast, or a
   * turntable, or a render, but it can also be a raw scene file if close inspection is 
   * required.  
   * <p>
   * Tasks can have more than one focus node, but it is often useful to specify one that is 
   * most important, which can be done by specifying it as a master focus node in its 
   * annotation.
   * 
   * @see TaskAnnotation
   */ 
  Focus, 

  /**
   * A node associated with a single image file (usually a jpg or png) suitable to represent 
   * the focus node in production tracking applications.
   */ 
  Thumbnail, 

  /**
   * A node containing the results of the task which will be used as input for tasks 
   * downstream in the production process. 
   */ 
  Product,
  
  /**
   * A node that runs a builder to further the execution of the task.  These nodes should not 
   * be checked-in with any other nodes upstream.
   */
  Execution,
  
  /**
   * Identifies the node that should be checked-in out together to provide a compatible 
   * set of Product nodes required as inputs into a Submit node network. <p>
   * 
   * Currently not being used in the default 2.4.28 task tools, but may be utilized in the 
   * future.
   */ 
  Prereq, 

  /**
   * A node that should be checked-in when the Unify nodes for a task have been updated. <p>
   * 
   * Currently not being used in the default 2.4.28 task tools, but may be utilized in the 
   * future.
   */ 
  Synch,
  
  /**
   * A node which may be procedurally or manually generated that has as sources 
   * product nodes from multiple tasks which need to be carefully coordinated to ensure
   * correctness. <p>
   * 
   * Currently not being used in the default 2.4.28 task tools, but may be utilized in the 
   * future.
   */ 
  Unify; 



  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return the conventional name for a directory containing nodes with this purpose.
   */ 
  public String 
  toDirName() 
  {
    switch(this) {
    case Prepare:
      return "prep"; 

    case Thumbnail:
      return "thumb"; 
      
    default:
      return super.toString().toLowerCase(); 
    }
  }

  /**
   * Return the conventional name (as a Path) for a directory containing nodes with this 
   * purpose.
   */ 
  public Path
  toDirPath() 
  {
    return new Path(toDirName());
  }
  
  /**
   * Convert to a more human friendly string representation.
   */ 
  public String
  toTitle()
  {
    return toString();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the list of human friendly string representation for all possible values.
   */ 
  public static ArrayList<String>
  titles()
  {
    ArrayList<String> toReturn = new ArrayList<String>();
    for (NodePurpose type : values()) {
      toReturn.add(type.toTitle());
    }
    return toReturn;
  }
  
  public static ArrayList<String>
  commonTitles()
  {
    ArrayList<String> toReturn = new ArrayList<String>();
    for (NodePurpose type : values()) {
      switch(type) {
      case Publish:
      case Submit:
      case Synch:
      case Verify:
        break;
      default:
        toReturn.add(type.toTitle());
      }
    }
    return toReturn;
    
  }
  
  /**
   * Get the list of all possible values.
   */ 
  public static ArrayList<NodePurpose>
  all() 
  {
    return new ArrayList<NodePurpose>(Arrays.asList(values()));
  }
}
