// $Id: SelectionProfile.java,v 1.1 2009/06/04 09:45:12 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   S E L E C T I O N   P R O F I L E                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The set of all selection scores for a particular set of required selection keys. 
 */ 
public class 
SelectionProfile
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new profile.
   * 
   * @param keys
   *   The names of all valid selection keys.
   * 
   * @param groups
   *   The set of selection groups to process.
   * 
   * @param jreqs
   *   The job requirements of a prototypical job which matches this selection profile.
   */
  public
  SelectionProfile
  (
   TreeSet<String> keys, 
   TreeMap<String,SelectionGroup> groups, 
   JobReqs jreqs
  ) 
  {
    pHasKeys = jreqs.hasSelectionKeys();

    pScores = new TreeMap<String,Integer>();
    for(String gname : groups.keySet()) {
      SelectionGroup group = groups.get(gname); 
      pScores.put(gname, group.computeSelectionScore(jreqs, keys));
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the selection score for the given named selection group.
   * 
   * @param gname
   *   The hardware group associated with the slot or 
   *   <CODE>null</CODE> if no is no hardware group for the slot.
   * 
   * @return 
   *   The combined selection bias or 
   *   <CODE>null</CODE> the selection key requirements are not met by the group.
   */ 
  public Integer
  getScore
  (
   String gname
  ) 
  {
    if(gname == null) {
      if(pHasKeys) 
        return null;
      else
        return 0;
    }

    return pScores.get(gname);  
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the jobs with this profile require any selection keys.
   */ 
  private boolean pHasKeys; 

  /**
   * The selection scores for each selection group.
   */ 
  private TreeMap<String,Integer> pScores; 

}

