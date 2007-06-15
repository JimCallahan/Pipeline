// $Id: UtilityParam.java,v 1.1 2007/06/15 22:29:47 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   U T I L I T Y   P A R A M                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A parameter of tool Builder classes. 
 */
public abstract
interface UtilityParam
  extends Glueable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the Utility parameter.
   */ 
  public String
  getName();

  /**
   * Gets a modified form of the name of this instance with spaces inserted between 
   * each word. <P> 
   * 
   * This name is used in the UI to label fields and table columns in a more human 
   * friendly manner.
   */ 
  public String
  getNameUI();

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the short description of the parameter used in tooltips.
   */ 
  public String
  getDescription();



  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public Object 
  clone();

}



