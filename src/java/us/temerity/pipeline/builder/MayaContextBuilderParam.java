// $Id: MayaContextBuilderParam.java,v 1.2 2007/03/28 20:43:45 jesse Exp $

package us.temerity.pipeline.builder;

import java.util.ArrayList;

import us.temerity.pipeline.SimpleParamAccess;
import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   C O N T E X T   B U I L D E R   P A R A M                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * An plugin parameter with an MayaContext value. <P> 
 */
public 
class MayaContextBuilderParam
  extends ComplexBuilderParam
  implements SimpleParamAccess
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
    super(name, desc);
    
    if (value == null)
      value = new MayaContext();
    
    {
      pLinearParam = new EnumBuilderParam
	(aLinearUnits, 
	 "The Linear Units value for Maya scenes.", 
	 value.getLinearUnit(),
	 new ArrayList<String>(MayaContext.getLinearUnits()));
      addParam(pLinearParam);
    }
    
    {
      pAngularParam = new EnumBuilderParam
	(aAngularUnits, 
	 "The Angular Units value for Maya scenes.", 
	 value.getAngularUnit(),
	 new ArrayList<String>(MayaContext.getAngularUnits()));
      addParam(pAngularParam);
    }
    
    {
      pTimeParam = new EnumBuilderParam
	(aTimeUnits, 
	 "The Time Units value for Maya scenes.", 
	 value.getTimeUnit(),
	 new ArrayList<String>(MayaContext.getTimeUnits()));
      addParam(pTimeParam);
    }
    
    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add(aAngularUnits);
      layout.add(aLinearUnits);
      layout.add(aTimeUnits);
      setLayout(layout);
    }
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
    String linear = pLinearParam.getStringValue();
    String angular = pAngularParam.getStringValue();
    String time = pTimeParam.getStringValue();
    
    MayaContext value = new MayaContext(angular, linear, time);
    return value;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S I M P L E   P A R A M E T E R   A C C E S S                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Implemented to allow this Complex Parameter to be used a Simple Parameter.  Returns
   * a {@link MayaContext};
   */
  @SuppressWarnings("unchecked")
  public Comparable
  getValue()
  {
    return getMayaContextValue();
  }
  
  /**
   * Sets the value of the parameter from a MayaContext.
   */
  @SuppressWarnings("unchecked")
  public void
  setValue
  (
    Comparable value
  )
  {
    if ( ( value != null ) && !( value instanceof MayaContext ) )
      throw new IllegalArgumentException("The parameter (" + pName
          + ") only accepts (MayaContext) values!");
    MayaContext context = (MayaContext) value;
    
    setValue(aAngularUnits, context.getAngularUnit());
    setValue(aLinearUnits, context.getLinearUnit());
    setValue(aTimeUnits, context.getTimeUnit());
  }
  
  /**
   * Sets the value from a single String.  Used for command line argument parsing.
   */
  public void 
  setValueFromString
  (
    String key 
  )
  {
    String buffer[] = key.split(",");
    if (buffer.length != 3) {
      throw new IllegalArgumentException
        ("The string that was passed in is not valid.  To set the MayaContext value, " +
         "it needs three comma-separated string values in the form angular,linear,time");
    }
    setValue(aAngularUnits, buffer[0]);
    setValue(aLinearUnits, buffer[1]);
    setValue(aTimeUnits, buffer[2]);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3809821423807521696L;
  
  public static final String aLinearUnits = "LinearUnits";
  public static final String aAngularUnits = "AngularUnits";
  public static final String aTimeUnits = "TimeUnits";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private EnumBuilderParam pLinearParam;
  private EnumBuilderParam pAngularParam;
  private EnumBuilderParam pTimeParam;
}



