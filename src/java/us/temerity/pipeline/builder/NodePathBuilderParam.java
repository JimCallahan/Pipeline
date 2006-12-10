// $Id: NodePathBuilderParam.java,v 1.2 2006/12/10 23:02:25 jesse Exp $

package us.temerity.pipeline.builder;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.GlueDecoder;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   P A T H   B U I L D E R   P A R A M                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * An Builder parameter with an fully resolved node name value.
 */
public 
class NodePathBuilderParam
  extends PathParam
  implements BuilderParam
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
  NodePathBuilderParam() 
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
  NodePathBuilderParam
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
  public void
  validate
  (
    Comparable value
  )
    throws IllegalArgumentException
  {
    IllegalArgumentException ex = 
      new IllegalArgumentException("Path (" + value + ") is not a valid node name");
    
    if(value== null || !(value instanceof Path))
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
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6004574702514003655L;

}



