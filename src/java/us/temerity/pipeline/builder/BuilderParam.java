// $Id: BuilderParam.java,v 1.4 2007/03/10 22:44:33 jesse Exp $

package us.temerity.pipeline.builder;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   P A R A M                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A parameter of tool Builder classes. 
 */
public abstract
interface BuilderParam
  extends Glueable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the Builder parameter.
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

  /**
   * Gets the value of the parameter. 
   */ 
  @SuppressWarnings("unchecked")
  public Comparable
  getValue();
  
  /**
   * Sets the value of the parameter. 
   */
  @SuppressWarnings("unchecked")
  public void 
  setValue
  (
   Comparable value  
  );



  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public Object 
  clone();

}



