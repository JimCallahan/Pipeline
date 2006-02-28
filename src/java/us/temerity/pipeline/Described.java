// $Id: Described.java,v 1.7 2006/02/28 19:47:45 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder; 

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
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */ 
  protected
  Described() 
  {
    super();
  }

  /** 
   * Construct with a name and description. 
   * 
   * @param name 
   *   The short name of the plugin.  
   * 
   * @param desc 
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
  
  /**
   * Gets the description text. 
   */ 
  public String
  getDescription()
  {
    return pDescription;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj   
  )
  {
    if((obj != null) && (obj instanceof Described)) {
      Described desc = (Described) obj;
      return (super.equals(obj) && 
	      pDescription.equals(desc.pDescription));
    }
    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4356479666627868487L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A short message which describes the class. 
   */     
  protected String  pDescription;  
  
}



