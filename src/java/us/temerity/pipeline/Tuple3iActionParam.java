// $Id: Tuple3iActionParam.java,v 1.1 2007/07/31 14:58:14 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.math.*;
import us.temerity.pipeline.glue.GlueDecoder; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E  3 I   A C T I O N   P A R A M                                               */
/*------------------------------------------------------------------------------------------*/

/**
 * An Action parameter with an Tuple3i value. <P> 
 */
public 
class Tuple3iActionParam
  extends TupleParam<Tuple3i>
  implements ActionParam
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
  Tuple3iActionParam() 
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
  Tuple3iActionParam
  (
   String name,  
   String desc, 
   Tuple3i value
  ) 
  {
    super(name, desc, value);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6357420531207180914L;

}



