// $Id: Described.java,v 1.1 2004/02/25 01:25:17 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D E S C R I B E D                                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Provides short <CODE>String</CODE> description in addition to a simple name.
 */
public
class Described
  extends Named
  implements Glueable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  protected
  Described() 
  {
    super();
  }

  /** 
   * Construct with a name and description. 
   * 
   * @param name [<B>in</B>]
   *   The short name of the plugin.  
   * 
   * @param desc [<B>in</B>]
   *   A short description used in tooltips.
   */ 
  protected
  Described
  (
   String name,  
   String desc  
  ) 
  {
    super(name);
    
    if(desc == null) 
      throw new IllegalArgumentException("The description cannot be (null)!");
    pDescription = desc;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /* 
   * Gets the description text. 
   */ 
  public String
  getDescription()
  {
    return pDescription;
  }



  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A short message which describes the class. 
   */     
  protected String  pDescription;  
  
}



