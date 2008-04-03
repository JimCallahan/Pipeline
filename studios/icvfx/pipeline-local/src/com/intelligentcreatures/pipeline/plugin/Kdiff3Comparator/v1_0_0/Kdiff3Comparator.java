package com.intelligentcreatures.pipeline.plugin.Kdiff3Comparator.v1_0_0;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   K D I F F 3   C O M P A R A T O R                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A graphical text file comparator and merge tool. <P> 
 * 
 */
public
class Kdiff3Comparator
  extends BaseComparator
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  Kdiff3Comparator()
  {
    super("Kdiff3", new VersionID("1.0.0"), "ICVFX",
	  "A graphical text file comparator and merge tool.",
	  "kdiff3");
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  private static final long serialVersionUID = 6383909479080350477L;

}


