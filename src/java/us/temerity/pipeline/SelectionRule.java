// $Id: SelectionRule.java,v 1.4 2009/09/16 03:54:40 jesse Exp $

package us.temerity.pipeline;

import java.io.Serializable;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   S E L E C T I O N   R U L E                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A selection schedule rule which is always active. <P> 
 * 
 * Can be used as a default rule, but is also the base class and common interface for more 
 * specialized selection rules.
 */
public 
class SelectionRule
  implements Cloneable, Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new selection schedule rule.
   */ 
  public
  SelectionRule()
  {
    
  }

  /**
   * Copy constructor. 
   */ 
  public 
  SelectionRule
  (
   SelectionRule rule
  )
  {
    pGroup = rule.pGroup;
    pServerStatus = rule.pServerStatus;
    pRemoveReservation = rule.pRemoveReservation;
    pOrder = rule.pOrder;
    pSlots = rule.pSlots;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the selection group which is activated by this rule.
   * 
   * @return
   *   The selection group, {@link #aNone} if the rule removes the selection group,
   *   or <CODE>null</CODE> if this rule is not affecting selection groups.
   */ 
  public String
  getGroup() 
  {
    return pGroup;
  }

  /**
   * Set the name of the selection group which is activated by this rule, {@link #aNone} 
   * to have it set no selection group or <CODE>null</CODE> to disable this rule.
   */ 
  public void
  setGroup
  (
    String name
  ) 
  {
    pGroup = name;
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the dispatch control which is activated by this rule.
   * 
   * @return
   *   The dispatch control, {@link #aNone} if the rule removes the dispatch control,
   *   or <CODE>null</CODE> if this rule is not affecting selection groups.
   */ 
  public String
  getDispatchControl() 
  {
    return pDispatchControl;
  }

  /**
   * Set the name of the dispatch control which is activated by this rule, {@link #aNone} 
   * to have it sets no control or <CODE>null</CODE> to disable this rule.
   */ 
  public void
  setDispatchControl
  (
    String name
  ) 
  {
    pDispatchControl = name;
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the user balance group which is activated by this rule.
   * 
   * @return
   *   The user balance group, {@link #aNone} if the rule removes the user balance group,
   *   or <CODE>null</CODE> if this rule is not affecting user balance groups.
   */ 
  public String
  getUserBalance() 
  {
    return pUserBalance;
  }

  /**
   * Set the name of the user balance group which is activated by this rule, {@link #aNone} 
   * to have it set no user balance or <CODE>null</CODE> to disable this rule.
   */ 
  public void
  setUserBalance
  (
    String name
  ) 
  {
    pUserBalance = name;
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the job group favor method which is activated by this rule.
   * 
   * @return
   *   The favor method or <CODE>null</CODE> if this rule is not affecting 
   *   user balance groups.
   */ 
  public JobGroupFavorMethod
  getFavorMethod() 
  {
    return pFavorMethod;
  }

  /**
   * Set the job group favor method which is activated by this rule or <CODE>null</CODE> to 
   * disable this rule.
   */ 
  public void
  setFavorMethod
  (
    JobGroupFavorMethod favor
  ) 
  {
    pFavorMethod = favor;
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the {@link QueueHostStatus} which is implemented by this rule.
   * 
   * @return
   *   The service status or <CODE>null</CODE> if this rule is not effecting 
   *   the server status.
   */ 
  public QueueHostStatus 
  getServerStatus()
  {
    return pServerStatus;
  }

  /**
   * Set the {@link QueueHostStatus} which is implemented by this rule or 
   * <CODE>null</CODE> if this rule is not effecting the server status.
   * 
   * @throws IllegalArgumentException if any value except <code>null</code>,
   * {@link QueueHostStatus#Disabled} or {@link QueueHostStatus#Enabled} is passed in. 
   */ 
  public void 
  setServerStatus
  (
    QueueHostStatus serverStatus
  )
  {
    if (serverStatus != null && 
       !(serverStatus == QueueHostStatus.Enabled || serverStatus == QueueHostStatus.Disabled))
      throw new IllegalArgumentException
        ("Only Enabled, Disabled, or null are valid values for this method.");
    pServerStatus = serverStatus;
  }

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does this rule remove all Reservations.
   * 
   * @return
   *   The remove reservation status.
   */ 
  public boolean
  getRemoveReservation()
  {
    return pRemoveReservation;
  }

  /**
   * Set the action of this rule with respect to removing reservations.
   */
  public void 
  setRemoveReservation
  (
    boolean removeReservations
  )
  {
    pRemoveReservation = removeReservations;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the Order which is implemented by this rule.
   * 
   * @return
   *   The order or <CODE>null</CODE> if this rule is not effecting the order.
   */ 
  public Integer
  getOrder()
  {
    return pOrder;
  }

  /**
   * Set the Order which is implemented by this rule or 
   * <CODE>null</CODE> if this rule is not effecting the order.
   */ 
  public void 
  setOrder
  (
    Integer order
  )
  {
    pOrder = order;
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the number of slots which is implemented by this rule.
   * 
   * @return
   *   The number of slot or <CODE>null</CODE> if this rule is not effecting slots.
   */ 
  public Integer
  getSlots()
  {
    return pSlots;
  }

  /**
   * Set the number of slots which is implemented by this rule or 
   * <CODE>null</CODE> if this rule is not effecting the slots.
   */ 
  public void 
  setSlots
  (
    Integer slots
  )
  {
    pSlots = slots;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the rule is active during the given point in time (milliseconds since 
   * midnight, January 1, 1970 UTC).
   */ 
  public boolean
  isActive
  (
    @SuppressWarnings("unused")
    long stamp
  )
  {
    return true;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  @Override
  public Object 
  clone()
  {
    return new SelectionRule(this);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    encoder.encode("Group", pGroup);
    encoder.encode("ServerStatus", pServerStatus);
    encoder.encode("RemoveReservation", pRemoveReservation);
    encoder.encode("Order", pOrder);
    encoder.encode("Slots", pSlots);
    encoder.encode("Dispatch", pDispatchControl);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    pGroup = (String) decoder.decode("Group"); 
    pServerStatus = (QueueHostStatus) decoder.decode("ServerStatus");
    Boolean tempValue = (Boolean) decoder.decode("RemoveReservation");
    if (tempValue == null)
      tempValue = false;
    pRemoveReservation = tempValue;
    pOrder = (Integer) decoder.decode("Order");
    pSlots = (Integer) decoder.decode("Slots");
    pDispatchControl = (String) decoder.decode("Dispatch");
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -9118929846011805641L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The name of the selection group which is activated by this rule.
   */ 
  protected String pGroup; 
  
  /**
   * The name of the dispatch control which is activated by this rule.
   */ 
  protected String pDispatchControl;
  
  /**
   * The name of the user balance group which is activated by this rule.
   */ 
  protected String pUserBalance; 
  
  /**
   * The favor method the machine should have or <code>null</code> if the rule should not
   * affect the favor method.
   */
  protected JobGroupFavorMethod pFavorMethod;
  
  /**
   * How the rule should manipulate the Status of machines.
   */
  protected QueueHostStatus pServerStatus;
  
  /**
   * Should the rule manipulate the reservation Status of machines.
   */
  protected boolean pRemoveReservation;
  
  /**
   * The order the machine should have or <code>null</code> if the rule should not
   * affect the order.
   */
  protected Integer pOrder;
  
  /**
   * The number of slots the machine should have or <code>null</code> if the rule should not
   * affect the number of slots.
   */
  protected Integer pSlots;
  
  public static final String aNone = "[None]";
}
