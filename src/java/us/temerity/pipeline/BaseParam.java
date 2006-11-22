// $Id: BaseParam.java,v 1.4 2006/11/22 09:08:00 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   P A R A M                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The abstract base class for all parameters associated with plugins.
 */
public abstract
class BaseParam
  extends Described
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
  BaseParam() 
  {
    super();
  }

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
  BaseParam
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
    StringBuilder buf = new StringBuilder();
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
    if((obj != null) && (obj instanceof BaseParam)) {
      BaseParam param = (BaseParam) obj;
    
      return (super.equals(obj) && 
	      (((pValue == null) && (param.pValue == null)) ||  
	       ((pValue != null) && pValue.equals(param.pValue))));
    }

    return false;
  }

  /**
   * Returns a string representation of the object. 
   */
  public String
  toString() 
  {
    if(pValue != null) 
      return pValue.toString();
    return null;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    encoder.encode("Value", pValue);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    pValue = (Comparable) decoder.decode("Value"); 
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



