// $Id: BaseActionParam.java,v 1.1 2004/06/14 22:43:27 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   A C T I O N   P A R A M                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The abstract base class for all parameters associated with node Actions. <P> 
 */
public abstract
class BaseActionParam
  extends Described
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
    
  /** 
   * Construct a parameter with the given name, description and default value.
   * 
   * @param name 
   *   The short name of the editor.  
   * 
   * @param desc 
   *   A short description used in tooltips.
   * 
   * @param value 
   *   The default value for this parameter.
   */ 
  protected 
  BaseActionParam
  (
   String name,  
   String desc, 
   Comparable value
  ) 
  {
    super(name, desc);
    pValue = value;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets a modified form of the name of this instance with spaces inserted between 
   * each word. <P> 
   * 
   * This name is used in the UI to label fields and table columns in a more human 
   * friendly manner.
   * 
   * @see #getName
   */ 
  public String
  getNameUI()
  {
    StringBuffer buf = new StringBuffer();
    char c[] = getName().toCharArray();
    int wk;
    buf.append(c[0]);
    for(wk=1; wk<(c.length-1); wk++) {
      if(Character.isUpperCase(c[wk]) && 
	 (Character.isLowerCase(c[wk-1]) ||
	  Character.isLowerCase(c[wk+1])))
	  buf.append(" ");

      buf.append(c[wk]);
    }
    buf.append(c[wk]);

    return (buf.toString());
  }


  /**
   * Gets the value of the parameter. 
   */ 
  public Comparable
  getValue() 
  {
    return pValue;
  }
  
  /**
   * Sets the value of the parameter. 
   */
  public abstract void 
  setValue
  (
   Comparable value  
  );
   


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
    if((obj != null) && (obj instanceof BaseActionParam)) {
      BaseActionParam param = (BaseActionParam) obj;
    
      return (super.equals(obj) && 
	      pValue.equals(param.pValue));
    }

    return false;
  }

  /**
   * Returns a hash code value for the object.
   */
  public int 
  hashCode() 
  {
    return pValue.hashCode();
  }

  /**
   * Returns a string representation of the object. 
   */
  public String
  toString() 
  {
    return pValue.toString();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -841968510233915472L;


   
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The value of the parameter.                
   */     
  protected Comparable  pValue;

}



