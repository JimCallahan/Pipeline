// $Id: AssetType.java,v 1.4 2008/02/06 18:17:43 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   A S S E T   T Y P E                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The types of assets shared by shots. 
 */ 
public  
enum AssetType
{
  /**
   * 
   */ 
  Rorschach, 

  /**
   *
   */ 
  Common; 



  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return the conventional name for a directory containing nodes with this asset type.
   */ 
  public String 
  toDirName() 
  {
    return super.toString().toLowerCase(); 
  }

  /**
   * Return the conventional name (as a Path) for a directory containing nodes with 
   * this asset type.
   */ 
  public Path 
  toDirPath() 
  {
    return new Path(toDirName()); 
  }
}
