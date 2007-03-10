// $Id: MayaContextBuilderParam.java,v 1.1 2007/03/10 22:44:33 jesse Exp $

package us.temerity.pipeline.builder;

import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   C O N T E X T   B U I L D E R   P A R A M                                    */
/*------------------------------------------------------------------------------------------*/


/**
 * An plugin parameter with an MayaContext value. <P> 
 */
public 
class MayaContextBuilderParam
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
  MayaContextBuilderParam() 
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
  MayaContextBuilderParam
  (
   String name,  
   String desc, 
   MayaContext value 
  ) 
  {
    super(name, desc, value);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the value of the parameter. 
   */ 
  public MayaContext
  getMayaContextValue() 
  {
    return ((MayaContext) getValue());
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
    if((value != null) && !(value instanceof MayaContext))
      throw new IllegalArgumentException("The parameter (" + pName
	+ ") only accepts (MayaContext) values!");
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T I E S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  public TreeSet<String> 
  listOfKeys()
  {
    TreeSet<String> toReturn = new TreeSet<String>();
    toReturn.add(pName + "-Angular");
    toReturn.add(pName + "-Linear");
    toReturn.add(pName + "-Time");
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
    MayaContext context = getMayaContextValue();
    try {
    if (key.equals(pName + "-Angular"))
      setValue(new MayaContext(value, context.getLinearUnit(), context.getTimeUnit()));
    else if (key.equals(pName + "-Linear"))
      setValue(new MayaContext(context.getAngularUnit(), value, context.getTimeUnit()));
    else if (key.equals(pName + "-Time"))
      setValue(new MayaContext(context.getAngularUnit(), context.getLinearUnit(), value));
    else
      assert(false);
    } catch (PipelineException ex) {
      LogMgr.getInstance().logAndFlush(Kind.Arg, Level.Warning, 
	"Attempted to set an invalid MayaContext value with a command line parameter.  " +
	"Value is being ignored.\n" + ex.getMessage());
      return;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3809821423807521696L;
}



