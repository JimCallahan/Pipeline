package us.temerity.pipeline;

import java.util.*;

import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   T R E E   M A P   P A R A M                                                            */
/*------------------------------------------------------------------------------------------*/

public abstract 
class TreeMapParam<E>
  extends ComplexParam<E>
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
  TreeMapParam()
  {
    super();
  }
  
  protected
  TreeMapParam
  (
    String name,
    String desc,
    Map<String, ArrayList<String>> values
  )
  {
    this(name, desc, 
         "First", "The First Parameter", 
         "Second", "The Second Parameter", 
         values);
  }
  
  protected 
  TreeMapParam
  (
    String name,  
    String desc,
    String firstName,
    String firstDesc,
    String secondName,
    String secondDesc,
    Map<String, ArrayList<String>> values
  )
  {
    super(name, desc);
    aFirst = firstName;
    aSecond = secondName;
    aFirstDesc = firstDesc;
    aSecondDesc = secondDesc;
    pValues = new TreeMap<String, ArrayList<String>>(values);
    
    String firstValue;
    {
      ArrayList<String> paramValues = new ArrayList<String>(pValues.keySet());
      firstValue = paramValues.get(0);
      E param = 
	createEnumParam
	(aFirst, 
	 aFirstDesc, 
	 firstValue, 
	 paramValues);
      addParam(param);
    }
    {
      ArrayList<String> paramValues = pValues.get(firstValue);
      String value = paramValues.get(0);
      E param = 
	createEnumParam
	(aSecond, 
	 aSecondDesc, 
	 value, 
	 paramValues);
      addParam(param);
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U P D A T E                                                                          */
  /*----------------------------------------------------------------------------------------*/

  @Override
  protected boolean 
  needsUpdating()
  {
    return true;
  }
  
  /**
   * Called when setting a param value.  <p>
   */
  @Override
  protected boolean
  valueUpdated
  (
    String paramName
  )
  {
    if (paramName.equals(aFirst)) {
      String firstValue = (String) getValue(aFirst);
      String secondValue = (String) getValue(aSecond);
      ArrayList<String> paramValues = pValues.get(firstValue);
      String value = null;
      if (paramValues.contains(secondValue))
	value = secondValue;
      else
	value = paramValues.get(0);
      E param = 
	createEnumParam
	(aSecond, 
	 aSecondDesc, 
	 value, 
	 paramValues);
      replaceParam(param);
      return true;
    }
    return false;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   G E N E R I C S   S U P P O R T                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  protected abstract E
  createEnumParam
  (
    String name, 
    String desc,
    String value,
    ArrayList<String> values
  );
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7433909038574101329L;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private TreeMap<String, ArrayList<String>> pValues;
  private String aFirst;
  private String aSecond;
  private String aFirstDesc;
  private String aSecondDesc;
}
