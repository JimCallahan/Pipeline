// $Id: HardwareProfile.java,v 1.1 2009/06/04 09:45:12 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   H A R D W A R E   P R O F I L E                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The set of all hardware eligibilities for a particular set of required hardware keys. 
 */ 
public class 
HardwareProfile
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new profile.
   * 
   * @param keys
   *   The names of all valid hardware keys.
   * 
   * @param groups
   *   The set of hardware groups to process.
   * 
   * @param jreqs
   *   The job requirements of a prototypical job which matches this hardware profile.
   */
  public
  HardwareProfile
  (
   TreeSet<String> keys, 
   TreeMap<String,HardwareGroup> groups, 
   JobReqs jreqs
  ) 
  {
    pHasKeys = jreqs.hasHardwareKeys();

    pIsEligible = new TreeSet<String>(); 
    for(String gname : groups.keySet()) {
      HardwareGroup group = groups.get(gname); 
      if(group.isEligible(jreqs, keys))
        pIsEligible.add(gname);
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the hardware requirements have been met by the given named hardware group.
   *
   * @param gname
   *   The hardware group associated with the slot or 
   *   <CODE>null</CODE> if no is no hardware group for the slot.
   */ 
  public boolean
  isEligible
  (
   String gname
  ) 
  {
    if(gname == null) 
      return !pHasKeys;

    return pIsEligible.contains(gname); 
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the jobs with this profile require any hardware keys.
   */ 
  private boolean pHasKeys; 

  /**
   * The names of the hardware groups which are eligible to run jobs with this hardware 
   * profile.
   */ 
  private TreeSet<String> pIsEligible; 

}

