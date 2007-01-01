// $Id: NodeEventOp.java,v 1.1 2007/01/01 16:09:51 jim Exp $

package us.temerity.pipeline.event;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   E V E N T   O P                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The type of operation recorded by a node event.
 */
public
enum NodeEventOp
{  
  /**
   * The registration of an initial working version of a node.
   */ 
  Registered, 

  /**
   * The release of the working version of a node.
   */ 
  Released, 

  /**
   * The check-in of a working version into the repository. 
   */ 
  CheckedIn, 

  /**
   * The check-out of node from the repository into a user's working area.
   */ 
  CheckedOut, 

  /**
   * The change of the checked-in version upon which the working version is based 
   * without modifying the working version properties, links or associated files.
   */ 
  Evolved, 

  /**
   * The change in one or more properties of a working version of a node.
   */ 
  PropsModified, 

  /**
   * The addition, removal or modification of the properties of existing upstream links of
   * a working version of a node.
   */ 
  LinksModified, 

  /**
   * The addition, removal or renumbering of the file sequences assoicated with a working
   * version of a node.
   */ 
  SeqsModified, 
  
  /**
   * The period of time in which an Editor plugin was launched which was capable of modifying
   * the files associated with a working version of a node.
   */ 
  Edited;

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<NodeEventOp>
  all() 
  {
    NodeEventOp values[] = values();
    ArrayList<NodeEventOp> all = new ArrayList<NodeEventOp>(values.length);
    int wk;
    for(wk=0; wk<values.length; wk++)
      all.add(values[wk]);
    return all;
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
    "Registered", 
    "Released", 
    "Checked-In", 
    "Checked-Out", 
    "Evolved", 
    "Props Modified", 
    "Links Modified", 
    "Seqs Modified", 
    "Edited"
  };
}
