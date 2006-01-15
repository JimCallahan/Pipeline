// $Id: QueueSetMaxLicensesReq.java,v 1.2 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   S E T   T O T A L   L I C E N S E S   R E Q                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request set the licensing scheme and maximum number of licenses associated with a 
 * license key. <P> 
 */
public
class QueueSetMaxLicensesReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param kname
   *   The name of the license key.
   * 
   * @param scheme
   *   The scheme used to determine the number of available licenses.
   * 
   * @param maxSlots
   *   The maximum number of slots running a job which requires the license key or 
   *   <CODE>null</CODE> if the license scheme is not PerSlot.
   * 
   * @param maxHosts
   *   The maximum number of hosts which may run a job which requires the license key or 
   *   <CODE>null</CODE> if the license scheme is PerSlot.
   * 
   * @param maxHostSlots
   *   The maximum number of slots which may run a job requiring the license key on a 
   *   single host or <CODE>null</CODE> if the license scheme is not PerHostSlot.
   */
  public
  QueueSetMaxLicensesReq
  (
   String kname, 
   LicenseScheme scheme, 
   Integer maxSlots, 
   Integer maxHosts, 
   Integer maxHostSlots
  )
  { 
    super();

    if(kname == null) 
      throw new IllegalArgumentException
	("The license key name cannot be (null)!");
    pKeyName = kname; 

    pScheme       = scheme;
    pMaxSlots     = maxSlots;
    pMaxHosts     = maxHosts;
    pMaxHostSlots = maxHostSlots;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the license key to remove. 
   */
  public String
  getKeyName() 
  {
    return pKeyName;
  }
  
  /**
   * Get the licensing scheme
   */ 
  public LicenseScheme
  getScheme()
  {
    return pScheme;
  }

  /**
   * Get the maximum number of slots running a job which requires the license key.
   * 
   * @return 
   *   The number of slots or <CODE>null</CODE> if the license scheme is not PerSlot.
   */ 
  public Integer
  getMaxSlots() 
  {
    return pMaxSlots; 
  }

  /**
   * Get the maximum number of hosts which may run a job which requires the license key.
   * 
   * @return 
   *   The number of hosts or <CODE>null</CODE> if the license scheme is not PerSlot.
   */ 
  public Integer
  getMaxHosts()
  {
    return pMaxHosts;
  }
  
  /**
   * Get the maximum number of slots which may run a job requiring the license key on a 
   * single host.
   * 
   * @return
   *   The number of host slots or <CODE>null</CODE> if the license scheme is not PerHostSlot.
   */ 
  public Integer
  getMaxHostSlots()
  {
    return pMaxHostSlots;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7686310935389256224L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the license key to remove.
   */ 
  private String  pKeyName;

  /**
   * The scheme used to determine the number of available licenses.
   */ 
  private LicenseScheme  pScheme; 

  /**
   * The maximum number of slots running a job which requires the license key. <P> 
   *
   * Only used when the license scheme is PerSlot. Disabled and set to <CODE>null</CODE> for
   * the other schemes.
   */ 
  private Integer  pMaxSlots; 

  /**
   * The maximum number of hosts which may run a job which requires the license key. <P> 
   * 
   * Used by both the PerHost and PerHostSlot schemes. Disabled and set to <CODE>null</CODE> 
   * when the license scheme is PerSlot.
   */ 
  private Integer  pMaxHosts; 

  /**
   * The maximum number of slots which may run a job requiring the license key on a single 
   * host. <P>
   * 
   * Only used by the PerHostSlot license scheme. Disabled and set to <CODE>null</CODE> to
   * all other schemes.
   */ 
  private Integer  pMaxHostSlots; 

}
  
