// $Id: NodePathUtilityParam.java,v 1.2 2008/03/19 15:15:11 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   P A T H   U T I L I T Y   P A R A M                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A Utility parameter with an fully resolved node name value.
 */
public 
class NodePathUtilityParam
  extends PathParam
  implements UtilityParam
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */    
  public 
  NodePathUtilityParam() 
  {
    super();
  }

  /** 
   * Construct a parameter with the given name, description and default value.
   * 
   * @param name 
   *   The short name of the parameter.  
   * 
   * @param desc 
   *   A short description used in tooltips.
   * 
   * @param value 
   *   The default value for this parameter.
   */ 
  public
  NodePathUtilityParam
  (
   String name,  
   String desc, 
   Path value
  ) 
  {
    super(name, desc, value);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   V A L I D A T O R                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A method to confirm that the input to the param is correct.
   * <P>
   */
  @SuppressWarnings("unchecked")
  public void
  validate
  (
    Comparable value
  )
    throws IllegalArgumentException
  {
    IllegalArgumentException ex = 
      new IllegalArgumentException("Path (" + value + ") is not a valid node name");
    
    if((value == null) || !(value instanceof Path))
      throw ex;
    
    Path p = (Path) value;

    String text = p.toString();
    
    String comps[] = text.split("/", -1);
    if(comps.length > 0) {
      if(comps[0].length() > 0) 
	throw ex;
      
      int wk;
      for(wk=1; wk<(comps.length-1); wk++) {
	if(comps[wk].length() == 0) 
	  throw ex;
      }
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T I E S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Sets the value of this parameter from a string.
   */
  public void 
  valueFromString
  (
    String value
  )
  {
    if (value == null)
      return;
    setValue(new Path(value));
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6004574702514003655L;

}



