package us.temerity.pipeline.builder.v2_4_1;

import java.util.*;

import us.temerity.pipeline.*;

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
   * A node that should be checked-in to signal that the task is ready for review.
   */ 
  Submit, 

  /**
   * Identifies the node that should be checked-in out together to provide a compatible 
   * set of Product nodes required as inputs into a Submit node network.
   */ 
  Prereq, 

  /**
   * Identifies the nodes that an artist should interactive modify in order to accomplish 
   * a task.
   */ 
  Edit, 

  /**
   * A node used in either the submit or approval networks as part of the process which 
   * will generates one or more of the other procedural types of nodes in the task but 
   * which has no direct utility to either artists or supervisors.
   */ 
  Prepare, 

  /**
   * A typically procedurally generated node that should be inspected by a  tasks 
   * supervisor part of the review process.
   */ 
  Focus, 

  /**
   * Identifies nodes associated with a single JPEG image suitable to represent a Focus 
   * node for external production tracking applications or web based systems.
   */ 
  Thumbnail, 

  /**
   * A node containing a procedurally generated post-approval product of the task used 
   * as input for tasks downstream in the production process.
   */ 
  Product, 

  /**
   * A node that should be checked-in to signal that the task has been approved.
   */ 
  Approve, 

  /**
   * A node containing QuickTime movies delivered for client or internal dailies.
   */ 
  Deliver; 



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
      case Approve:
      case Submit:
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