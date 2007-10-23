// $Id: OPLibsMergePolicy.java,v 1.1 2007/10/23 01:47:37 jim Exp $

package us.temerity.pipeline.plugin.HfsOPLibsMergeAction.v2_3_14;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   O P   L I B S   M E R G E   P O L I C Y                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The policy for how OTLs listed in different OPlibraries files are merged to create a 
 * single unified OPlibraries file.
 */ 
public 
enum OPLibsMergePolicy
{
  /** 
   * Add the source OTLs to the output regardless of whether previous OTLs with the 
   * same names have already been encountered.
   */ 
  Append, 

  /**
   * Add the source OTLs to the output, but generate an error and have the Action fail 
   * if any OTLs with the same names have already been encountered.
   */ 
  Exclusive, 

  /**
   * Only add source OTLs to the output if no previous OTLs with the same names have 
   * been encountered.
   */ 
  Ignore, 

  /**
   * If any previous OTLs have been encountered with the same names as the the OTLs in 
   * this source, replace them with the source OTLs.
   */ 
  Override;



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible values.
   */ 
  public static ArrayList<OPLibsMergePolicy>
  all() 
  {
    OPLibsMergePolicy values[] = values();
    ArrayList<OPLibsMergePolicy> all = new ArrayList<OPLibsMergePolicy>(values.length);
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
    for(OPLibsMergePolicy policy : OPLibsMergePolicy.all()) 
      titles.add(policy.toTitle());
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

