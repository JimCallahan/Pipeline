// $Id: MergePolicy.java,v 1.1 2008/07/22 21:36:07 jesse Exp $

package us.temerity.pipeline.toolset2;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M E R G E   P O L I C Y                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The policy for how identically named {@link PackageEntry PackageEntry} instances are 
 * merged with when toolset packages are combined to generate a toolset.
 */ 
public 
enum MergePolicy
{
  /** 
   * The <CODE>PackageEntry</CODE> must not be defined by any previous package.
   */ 
  Exclusive, 

  /**
   * The <CODE>PackageEntry</CODE> will replace any existing <CODE>PackageEntry</CODE> with 
   * the same name defined by any previous package.
   */ 
  Override, 

  /**
   * The <CODE>PackageEntry</CODE> will be ignored if any existing <CODE>PackageEntry</CODE> 
   * with the same name defined by any previous package.
   */ 
  Ignore, 

  /**
   * The <CODE>PackageEntry</CODE> is a colon seperated path who's value is appended to the 
   * value of any previously defined <CODE>PackageEntry</CODE> with the same name.
   */ 
  AppendPath,
  
  /**
   * The <CODE>PackageEntry</CODE> is a colon seperated path who's value is prepended to the 
   * value of any previously defined <CODE>PackageEntry</CODE> with the same name.
   */ 
  PrependPath;

  /**
   * Get the list of all possible values.
   */ 
  public static ArrayList<MergePolicy>
  all() 
  {
    MergePolicy values[] = values();
    ArrayList<MergePolicy> all = new ArrayList<MergePolicy>(values.length);
    int wk;
    for(wk=0; wk<values.length; wk++)
	all.add(values[wk]);
    return all;
  }

  /**
   * Get the default policy for the given environmental variable name. <P> 
   * 
   * Common path environmental variables will be assigned the <CODE>AppendPath</CODE>
   * policy, while all other variables will be assigned the <CODE>Exclusive</CODE> policy.
   */ 
  public static MergePolicy
  getDefaultPolicy
  (
   String name
  ) 
  {
    MergePolicy policy = Exclusive;
    if(name.endsWith("PATH")) 
      policy = AppendPath;

    return policy;
  }
}
