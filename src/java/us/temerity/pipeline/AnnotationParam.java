// $Id: AnnotationParam.java,v 1.1 2007/06/15 00:27:31 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   A N N O T A T I O N   P A R A M                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A parameter of Annotation plugins.
 */
public 
interface AnnotationParam
  extends Glueable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the Annotation parameter.
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
  public Comparable
  getValue();
  
  /**
   * Sets the value of the parameter. 
   */
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



