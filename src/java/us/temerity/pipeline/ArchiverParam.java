// $Id: ArchiverParam.java,v 1.1 2004/11/11 00:40:09 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   A R C H I V E R   P A R A M                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A parameter of Archiver plugins.
 */
public 
interface ArchiverParam
  extends Glueable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the Archiver parameter.
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



