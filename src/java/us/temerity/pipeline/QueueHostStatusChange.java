// $Id: QueueHostStatusChange.java,v 1.1 2006/07/02 00:27:49 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   H O S T   S T A T U S   C H A N G E                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to chang the operational status of a job server host.
 */
public
enum QueueHostStatusChange
{  
  /**
   * Request that the <B>pljobmgr</B>(1) daemon be enabled so that it can begin to 
   * process jobs for the queue.
   */
  Enable, 

  /**
   * Request that the <B>pljobmgr</B>(1) daemon be temporarily disabled and not longer
   * accept new jobs.
   */
  Disable, 

  /**
   * Request that the <B>pljobmgr</B>(1) daemon shutdown gracefully and exit.
   */
  Terminate; 



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<QueueHostStatusChange> 
  all() 
  {
    QueueHostStatusChange values[] = values();
    ArrayList<QueueHostStatusChange> all = 
      new ArrayList<QueueHostStatusChange>(values.length);
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
    for(QueueHostStatusChange change : QueueHostStatusChange.all()) 
      titles.add(change.toTitle());
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
