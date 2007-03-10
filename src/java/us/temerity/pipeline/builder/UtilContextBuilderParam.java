// $Id: UtilContextBuilderParam.java,v 1.1 2007/03/10 22:44:33 jesse Exp $

package us.temerity.pipeline.builder;

import java.util.TreeSet;

import us.temerity.pipeline.BaseParam;
import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   U T I L   C O N T E X T   B U I L D E R   P A R A M                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A builder parameter with an {@link UtilContext} value. <P> 
 */
public 
class UtilContextBuilderParam
  extends BaseParam
  implements ComplexBuilderParam
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
  UtilContextBuilderParam() 
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
  UtilContextBuilderParam
  (
   String name,  
   String desc, 
   UtilContext value 
  ) 
  {
    super(name, desc, value);
    if (value == null)
      throw new IllegalArgumentException("The value cannot be (null)!");
  }

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the value of the parameter. 
   */ 
  public UtilContext
  getUtilContextValue() 
  {
    return ((UtilContext) getValue());
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   V A L I D A T O R                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A method to confirm that the input to the param is correct.
   * <P>
   */
  @SuppressWarnings("unchecked")
  protected void 
  validate
  (
    Comparable value	  
  )
    throws IllegalArgumentException 
  {
    if ( ( value != null ) && !( value instanceof UtilContext ) )
      throw new IllegalArgumentException("The parameter (" + pName
          + ") only accepts (UtilContext) values!");
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T I E S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  public TreeSet<String> 
  listOfKeys()
  {
    TreeSet<String> toReturn = new TreeSet<String>();
    toReturn.add(pName + "-Author");
    toReturn.add(pName + "-View");
    toReturn.add(pName + "-Toolset");
    return toReturn;
  }

  public void 
  valueFromString
  (
    String key, 
    String value
  )
  {
    if (value == null)
      return;
    UtilContext context = getUtilContextValue();
    if (key.equals(pName + "-Author"))
      setValue(new UtilContext(value, context.getView(), context.getToolset()));
    else if (key.equals(pName + "-View"))
      setValue(new UtilContext(context.getAuthor(), value, context.getToolset()));
    else if (key.equals(pName + "-Toolset"))
      setValue(new UtilContext(context.getAuthor(), context.getView(), value));
    else
      assert(false);
      return;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/


  private static final long serialVersionUID = -1468438453624432149L;

}



