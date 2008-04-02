// $Id: NodePurpose.java,v 1.2 2008/04/02 20:56:16 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

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
}
