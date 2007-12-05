// $Id: HardwareGroup.java,v 1.2 2007/12/05 05:45:58 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   H A R D W A R E   G R O U P                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A named collection of hardware keys.
 */
public
class HardwareGroup
  extends Named
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
  HardwareGroup()
  { 
    init();
  }

  /**
   * Construct a new hardware group.
   * 
   * @param name
   *   The name of the group.
   */ 
  public
  HardwareGroup
  (
   String name
  ) 
  {
    super(name);
    init();
  }

  /**
   * Construct a new hardware group with the same hardware key values as the given group.
   * 
   * @param name
   *   The name of the new group.
   * 
   * @param group
   *   Copy hardware key values from this group.
   */ 
  public
  HardwareGroup
  (
   String name, 
   HardwareGroup group
  ) 
  {
    super(name);
    init();

    for(String key : group.getKeys()) 
      addKey(key);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the instance.
   */ 
  private void 
  init() 
  {
    pHardwareValues = new TreeSet<String>();
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the Hardware keys which are members of the group.
   */
  public synchronized Set<String>
  getKeys()
  {
    return Collections.unmodifiableSet(pHardwareValues);
  }

  /**
   * Whether the group contains the specified key.
   * 
   * @param key
   *   The name of the hardware key.
   */ 
  public synchronized Boolean
  hasKey
  (
   String key
  ) 
  {
    return pHardwareValues.contains(key);
  }
  
  /**
   * Add the given hardware key.
   * 
   * @param key
   *   The name of the hardware key.
   */ 
  public synchronized void 
  addKey
  (
   String key 
  ) 
  {
    if(key == null) 
      throw new IllegalArgumentException
	("The hardware key cannot be (null)!");
    pHardwareValues.add(key);
  }
  
  /** 
   * Removes the named hardware key.
   *
   * @param key 
   *    The name of the hardware key to remove.
   */
  public synchronized void
  removeKey
  (
   String key
  ) 
  {
    pHardwareValues.remove(key);
  }
  
  /** 
   * Remove all hardware keys.
    */
  public synchronized void
  removeAllValues() 
  {
    pHardwareValues.clear();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J O B   S E L E C T I O N                                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Determines if a job is eligible for the given job requirements. <P> 
   * 
   * This method will return <CODE>false</CODE> if the hardware group does not contain 
   * all of the hardware keys required.
   * 
   * @param jreqs
   *   The requirements that this host must meet in order to be eligible to run the job. 
   * 
   * @param keys 
   *   The names of the valid hardware keys.
   * 
   * @return 
   *   Whether this hardware group meets the requirements.
   */ 
  public synchronized boolean
  isEligible
  (
   JobReqs jreqs, 
   TreeSet<String> keys
  )
  {
    for(String key : jreqs.getHardwareKeys()) {
      if(keys.contains(key))
	if (!pHardwareValues.contains(key))
	  return false;
    }

    return true;
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
    super.toGlue(encoder); 
    
    if(!pHardwareValues.isEmpty()) 
      encoder.encode("HardwareValues", pHardwareValues);
    
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    TreeSet<String> values = 
      (TreeSet<String>) decoder.decode("HardwareValues"); 
    if(values != null) 
      pHardwareValues = values;

  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6235842136559531067L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The hardware keys that are activated in this group.
   */ 
  private TreeSet<String>  pHardwareValues; 

}
