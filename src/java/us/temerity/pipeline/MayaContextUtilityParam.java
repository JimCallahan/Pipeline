// $Id: MayaContextUtilityParam.java,v 1.1 2007/06/15 22:29:47 jesse Exp $

package us.temerity.pipeline;

import java.util.ArrayList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   C O N T E X T   U T I L I T Y   P A R A M                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A utility parameter with an MayaContext value. <P> 
 */
public 
class MayaContextUtilityParam
  extends ComplexUtilityParam
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
  MayaContextUtilityParam() 
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
  MayaContextUtilityParam
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
      pLinearParam = new EnumUtilityParam
	(aLinearUnits, 
	 "The Linear Units value for Maya scenes.", 
	 value.getLinearUnit(),
	 new ArrayList<String>(MayaContext.getLinearUnits()));
      addParam(pLinearParam);
    }
    
    {
      pAngularParam = new EnumUtilityParam
	(aAngularUnits, 
	 "The Angular Units value for Maya scenes.", 
	 value.getAngularUnit(),
	 new ArrayList<String>(MayaContext.getAngularUnits()));
      addParam(pAngularParam);
    }
    
    {
      pTimeParam = new EnumUtilityParam
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
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U P D A T E                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  protected boolean
  needsUpdating()
  {
    return false;
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
  
  private EnumUtilityParam pLinearParam;
  private EnumUtilityParam pAngularParam;
  private EnumUtilityParam pTimeParam;
}



