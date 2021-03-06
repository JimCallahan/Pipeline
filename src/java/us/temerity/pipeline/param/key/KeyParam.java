// $Id: KeyParam.java,v 1.2 2007/12/16 11:12:16 jesse Exp $

package us.temerity.pipeline.param.key;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   K E Y   P A R A M                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A parameter of key chooser classes. 
 */
public abstract
interface KeyParam
  extends Glueable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the Selection Key parameter.
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



