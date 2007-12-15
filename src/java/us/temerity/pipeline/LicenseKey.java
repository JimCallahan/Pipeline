// $Id: LicenseKey.java,v 1.7 2007/12/15 07:14:57 jesse Exp $

package us.temerity.pipeline;

import java.util.TreeMap;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   L I C E N S E   K E Y                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A symbolic key which represents a limited number of floating software licenses.
 * 
 * License Keys can have an optional plugin which determine when they are turned on for jobs.
 * Under normal operation, a user selects which keys are on for each node that is submitted
 * for regeneration. However, if there a plugin associated with the license key, the user will
 * not be able to specify that license key. Instead the plugin will be used to calculate
 * whether the key should be on or off for the given node at the time of job submission.
 * 
 * @see JobReqs
 */
public
class LicenseKey
  extends BaseKey
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */ 
  public
  LicenseKey() 
  {
    super();
    pUsedPerHost = new TreeMap<String,Integer>();
  }

  /** 
   * Construct a new license key.
   * 
   * @param name 
   *   The name of the license key.
   * 
   * @param desc 
   *   A short description of the license key.
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
  LicenseKey
  (
   String name,  
   String desc, 
   LicenseScheme scheme, 
   Integer maxSlots, 
   Integer maxHosts, 
   Integer maxHostSlots
  ) 
  {
    super(name, desc);

    pUsedPerHost = new TreeMap<String,Integer>();

    setScheme(scheme);
    setMaxSlots(maxSlots);
    setMaxHosts(maxHosts);
    setMaxHostSlots(maxHostSlots);
  }
  
  /** 
   * Construct a new license key.
   * 
   * @param name 
   *   The name of the license key.
   * 
   * @param desc 
   *   A short description of the license key.
   * 
   * @param plugin
   *   The plugin that will be used to determine when this key is on.
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
  LicenseKey
  (
   String name,  
   String desc,
   BaseKeyChooser plugin,
   LicenseScheme scheme, 
   Integer maxSlots, 
   Integer maxHosts, 
   Integer maxHostSlots
  ) 
  {
    super(name, desc, plugin);

    pUsedPerHost = new TreeMap<String,Integer>();

    setScheme(scheme);
    setMaxSlots(maxSlots);
    setMaxHosts(maxHosts);
    setMaxHostSlots(maxHostSlots);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the licensing scheme
   */ 
  public synchronized LicenseScheme
  getScheme()
  {
    return pScheme;
  }

  /**
   * Set the licensing scheme.
   */ 
  public synchronized void 
  setScheme
  (
   LicenseScheme scheme
  ) 
  {
    pScheme = scheme;

    switch(scheme) {
    case PerSlot:
      if(pMaxSlots == null) 
	pMaxSlots = 0;
      pMaxHosts = null;
      pMaxHostSlots = null;
      break;

    case PerHost:
      pMaxSlots = null;
      if(pMaxHosts == null) 
	pMaxHosts = 0; 
      pMaxHostSlots = null;
      break;

    case PerHostSlot:
      pMaxSlots = null;
      if(pMaxHosts == null) 
	pMaxHosts = 0; 
      if(pMaxHostSlots == null)
	pMaxHostSlots = 0;
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the maximum number of slots running a job which requires the license key.
   * 
   * @return 
   *   The number of slots or <CODE>null</CODE> if the license scheme is not PerSlot.
   */ 
  public synchronized Integer
  getMaxSlots() 
  {
    return pMaxSlots; 
  }

  /**
   * Set the maximum number of slots running a job which requires the license key.
   */ 
  public synchronized void
  setMaxSlots
  (
   Integer slots
  ) 
  {
    switch(pScheme) {
    case PerSlot:
      if(slots == null) 
	throw new IllegalArgumentException
	  ("The maximum number of licensed slots cannot be (null)!");
      if(slots < 0) 
	throw new IllegalArgumentException
	  ("The maximum number of licensed slots cannot be negative!");
      break;
      
    default:
      if(slots != null) 
	throw new IllegalArgumentException
	  ("The maximum number of licensed slots is not valid for the " + pScheme.toTitle() +
	   " license scheme!");
    }      

    pMaxSlots = slots;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the maximum number of hosts which may run a job which requires the license key.
   * 
   * @return 
   *   The number of hosts or <CODE>null</CODE> if the license scheme is not PerSlot.
   */ 
  public synchronized Integer
  getMaxHosts()
  {
    return pMaxHosts;
  }

  /**
   * Set the maximum number of hosts which may run a job which requires the license key.
   */ 
  public synchronized void
  setMaxHosts
  (
   Integer hosts
  ) 
  {
    switch(pScheme) {
    case PerHost: 
    case PerHostSlot:
      if(hosts == null)
	throw new IllegalArgumentException
	  ("The maximum number of licensed hosts cannot be (null)!");
      if(hosts < 0) 
	throw new IllegalArgumentException
	  ("The maximum number of licensed hosts cannot be negative!");
      break;
      
    default:
      if(hosts != null) 
	throw new IllegalArgumentException
	  ("The maximum number of licensed hosts is not valid for the " + pScheme.toTitle() +
	   " license scheme!");
    }      
    
    pMaxHosts = hosts;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the maximum number of slots which may run a job requiring the license key on a 
   * single host.
   * 
   * @return
   *   The number of host slots or <CODE>null</CODE> if the license scheme is not PerHostSlot.
   */ 
  public synchronized Integer
  getMaxHostSlots()
  {
    return pMaxHostSlots;
  }
  
  /**
   * Set the maximum number of slots which may run a job requiring the license key on a 
   * single host.
   */ 
  public synchronized void
  setMaxHostSlots
  (
   Integer slots
  ) 
  {
    switch(pScheme) {
    case PerHostSlot:
      if(slots == null)
	throw new IllegalArgumentException
	  ("The maximum number of licensed slots per host cannot be (null)!");
      if(slots < 0) 
	throw new IllegalArgumentException
	  ("The maximum number of licensed slots per host cannot be negative!");
      break;
      
    default:
      if(slots != null) 
	throw new IllegalArgumentException
	  ("The maximum number of licensed slots per host is not valid for the " + 
	   pScheme.toTitle() + " license scheme!");
    }      

    pMaxHostSlots = slots;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the number of licenses currently available number of licenses.
   */ 
  public synchronized int 
  getAvailable() 
  {
    int avail = 0;
    switch(pScheme) {
    case PerSlot:
      {
	int totalUsed = 0;
	for(Integer cnt : pUsedPerHost.values()) 
	  totalUsed += cnt; 
	
	avail = Math.max((pMaxSlots - totalUsed), 0);
      }
      break;

    case PerHost:
    case PerHostSlot:
      {
	int hostsUsed = 0; 
	for(Integer cnt : pUsedPerHost.values()) {
	  if(cnt > 0) 
	    hostsUsed++;
	}
	
	avail = Math.max((pMaxHosts - hostsUsed), 0);
      }
    }
    
    return avail;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Attempt to acquire a license.
   * 
   * @param hostname
   *   The name of the host where the job acquiring the license will run.
   * 
   * @return 
   *   Whether a license was available.
   */ 
  public synchronized boolean
  acquire
  (
   String hostname
  ) 
  {
    Integer slotsUsed = pUsedPerHost.get(hostname);

    boolean available = false;
    switch(pScheme) {
    case PerSlot:
      {
	int totalUsed = 0;
	for(Integer cnt : pUsedPerHost.values()) 
	  totalUsed += cnt; 
	
	if(totalUsed < pMaxSlots) 
	  available = true;
      }
      break;

    case PerHost:
    case PerHostSlot:
      {
	int hostsUsed = 0;
	for(Integer cnt : pUsedPerHost.values()) {
	  if(cnt > 0) 
	    hostsUsed++; 
	}

	switch(pScheme) {
	case PerHost:
	  available = 
	    ((hostsUsed < pMaxHosts) || 
	     ((hostsUsed == pMaxHosts) && 
	      (slotsUsed != null) && (slotsUsed > 0)));
	  break;

	case PerHostSlot:
	  available = 
	    (((hostsUsed < pMaxHosts) && 
	      ((slotsUsed == null) || (slotsUsed < pMaxHostSlots))) || 
	     ((hostsUsed == pMaxHosts) && 
	      (slotsUsed != null) && (slotsUsed > 0) && (slotsUsed < pMaxHostSlots)));
	}
      }
    }

    if(available) {
      if(slotsUsed == null) 
	pUsedPerHost.put(hostname, 1);
      else 
	pUsedPerHost.put(hostname, slotsUsed+1);

      return true;
    }

    return false;
  }

  /**
   * Release a previously acquired license.
   *
   * @param hostname
   *   The name of the host where the job which acquired the license was run.
   */ 
  public synchronized void
  release
  (
   String hostname
  )
  {
    Integer slotsUsed = pUsedPerHost.get(hostname);
    if((slotsUsed != null) && (slotsUsed > 0)) 
      pUsedPerHost.put(hostname, slotsUsed-1);
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    super.toGlue(encoder);
    
    encoder.encode("LicenseScheme", pScheme);    
    
    switch(pScheme) {
    case PerSlot:
      encoder.encode("MaxSlots", pMaxSlots);
      break;

    case PerHostSlot:
      encoder.encode("MaxHostSlots", pMaxHostSlots);

    case PerHost:
      encoder.encode("MaxHosts", pMaxHosts);
    }
  }
  
  @Override
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    LicenseScheme scheme = (LicenseScheme) decoder.decode("LicenseScheme"); 
    if(scheme == null) 
      throw new GlueException("The \"LicenseScheme\" was missing!");
    setScheme(scheme);

    switch(pScheme) {
    case PerSlot:
      {
	Integer maxSlots = (Integer) decoder.decode("MaxSlots");
	if(maxSlots == null)
	  throw new GlueException("The \"MaxSlots\" was missing!");
	setMaxSlots(maxSlots);
      }
      break;
      
    case PerHostSlot:
      {
	Integer maxHostSlots = (Integer) decoder.decode("MaxHostSlots");
	if(maxHostSlots == null)
	  throw new GlueException("The \"MaxHostSlots\" was missing!");
	setMaxHostSlots(maxHostSlots);
      }

    case PerHost:
      {
	Integer maxHosts = (Integer) decoder.decode("MaxHosts");
	if(maxHosts == null)
	  throw new GlueException("The \"MaxHosts\" was missing!");
	setMaxHosts(maxHosts);
      }      
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7616282979518347032L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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


  /*----------------------------------------------------------------------------------------*/

  /**
   * The number of licenses currently in use by each job server host.
   */ 
  private TreeMap<String,Integer>  pUsedPerHost; 

}



