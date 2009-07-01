// $Id: QueueHostStatus.java,v 1.3 2009/07/01 16:43:14 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   H O S T   S T A T U S                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The operational status of a job server host.
 */
public
enum QueueHostStatus
{  
  /**
   * Indicates that the Job Manager is in the process of being enabled.
   */
  Enabling, 

  /**
   * Communication is currenty established with the Job Manager running on the host and 
   * is available to run any new jobs which meet the selection criteria for the host.
   */ 
  Enabled, 


  /**
   * Indicates that the Job Manager is in the process of being disabled.
   */
  Disabling, 

  /**
   * Communication is currenty established with the Job Manager running on the host but
   * it has been temporarily disabled from starting new jobs. <P> 
   * 
   * Jobs previously assigned to the host may continue running until they complete, but no 
   * new jobs will be assigned to this host.  The host will respond to requests to kill 
   * jobs currently running on the host. 
   */ 
  Disabled, 


  /**
   * Indicates that a shutdown of the Job Manager is underway.<P> 
   * 
   * If communication can be establshed, the Job Manager will be instructed to peform a 
   * clean Shutodown operation and exit.  However, even if the Job Manager cannot be 
   * contacted it will become marked as Offline.
   */
  Terminating, 

  /**
   * There is no communication currently established with the Job Manager on this host 
   * nor will any effort be made to reestablish communication.<P> 
   * 
   * Note that this state only indicates that the Queue Manager will ignore this host
   * and not whether a Job Manager is actually running on the host.
   */ 
  Shutdown,


  /*----------------------------------------------------------------------------------------*/

  /**
   * Communication with the Job Manager has unexpectedly been lost and it has become
   * unresponsive to requests for network communication. <P> 
   *  
   * The Queue Manager will periodically attempt to restore communication and if successful
   * the Job Manager will return to an Enabled state.  Users may also manually attempt
   * to restore contact by Enabling the host. <P> 
   * 
   * Note that there are a variety of reasons for while a Job Manager may become Limbo
   * including: <P> 
   * <DIV style="margin-left: 40px;">
   *   The host is not powered on. <P> 
   *   There is some network communication failure or misconfiguration.<P> 
   *   The host operating system has gone down or is under extremely high load.<P> 
   *   The Job Manager has been killed and has not been restarted.<P> 
   * </DIV>
   */ 
  Limbo;



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<QueueHostStatus> 
  all() 
  {
    QueueHostStatus values[] = values();
    ArrayList<QueueHostStatus> all = new ArrayList<QueueHostStatus>(values.length);
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
    for(QueueHostStatus status : QueueHostStatus.all()) 
      titles.add(status.toTitle());
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
  
  /**
   * Converts this data into a QueueHostStatusChange for use in a {@link QueueHostMod}.
   * <p>
   * This convenience method is intended to make it easy to take the {@link QueueHostStatus}
   * found in a {@link SelectionRule} and translate it into a form which a
   * {@link QueueHostMod} can easily work with. As a result, it is only concerned with the
   * {@link #Disabled} and {@link #Enabled} fields in this enumeration, since those are the
   * only two that are actually used in {@link SelectionRule}. Any other value will return
   * <code>null</code>.
   */
  public QueueHostStatusChange
  toQueueHostStatusChange()
  {
     switch(this) {
     case Disabled:
       return QueueHostStatusChange.Disable;
     case Enabled:
       return QueueHostStatusChange.Enable;
     default:
       return null;
     }
  }


}
