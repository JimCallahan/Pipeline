package us.temerity.pipeline;

import java.util.ArrayList;
import java.util.List;

import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   T R I P L E   M A P   P A R A M                                                        */
/*------------------------------------------------------------------------------------------*/

public abstract  
class TripleMapParam<E>
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
  TripleMapParam()
  {
    super();
  }
  
  protected
  TripleMapParam
  (
    String name,
    String desc,
    TripleMap<String, String, String, ArrayList<String>> values
  )
  {
    this(name, desc, 
         "First", "The First Parameter", 
         "Second", "The Second Parameter",
         "Third", "The Third Parameter",
         "Fourth", "The Fourth Parameter",
         values);
  }
  
  protected 
  TripleMapParam
  (
    String name,  
    String desc,
    String firstName,
    String firstDesc,
    String secondName,
    String secondDesc,
    String thirdName,
    String thirdDesc,
    String fourthName,
    String fourthDesc,
    TripleMap<String, String, String, ArrayList<String>> values
  )
  {
    super(name, desc);
    aFirst = firstName;
    aSecond = secondName;
    aFirstDesc = firstDesc;
    aSecondDesc = secondDesc;
    aThird = thirdName;
    aThirdDesc = thirdDesc;
    aFourth = fourthName;
    aFourthDesc = fourthDesc;
    pValues = new TripleMap<String, String, String, ArrayList<String>>(values);
    
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
    String secondValue;
    {
      ArrayList<String> paramValues = new ArrayList<String>(pValues.get(firstValue).keySet());
      secondValue = paramValues.get(0);
      E param = 
	createEnumParam
	(aSecond, 
	 aSecondDesc, 
	 secondValue, 
	 paramValues);
      addParam(param);
    }
    String thirdValue;
    {
      ArrayList<String> paramValues = 
	new ArrayList<String>(pValues.get(firstValue).get(secondValue).keySet());
      thirdValue = paramValues.get(0);
      E param = 
	createEnumParam
	(aThird, 
	 aThirdDesc, 
	 thirdValue, 
	 paramValues);
      addParam(param);
    }
    {
      ArrayList<String> paramValues = pValues.get(firstValue, secondValue, thirdValue);
      String fourthValue = paramValues.get(0);
      E param = 
	createEnumParam
	(aFourth, 
	 aFourthDesc, 
	 fourthValue, 
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
    List<String> paramName
  )
  {
    boolean toReturn = false;
    String name = paramName.get(0);
    if (name.equals(aFirst)) {
      String firstValue = (String) getValue(aFirst);
      String secondValue = (String) getValue(aSecond);
      ArrayList<String> paramValues = new ArrayList<String>(pValues.get(firstValue).keySet());
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
      toReturn = true;
    }
    if (name.equals(aFirst) || name.equals(aSecond)) {
      String firstValue = (String) getValue(aFirst);
      String secondValue = (String) getValue(aSecond);
      String thirdValue = (String) getValue(aThird);
      ArrayList<String> paramValues = 
	new ArrayList<String>(pValues.get(firstValue).get(secondValue).keySet());
      String value = null;
      if (paramValues.contains(thirdValue))
	value = thirdValue;
      else
	value = paramValues.get(0);
      E param = 
	createEnumParam
	(aThird, 
	 aThirdDesc, 
	 value, 
	 paramValues);
      replaceParam(param);
      toReturn = true;
    }
    if (name.equals(aFirst) || name.equals(aSecond) || name.equals(aThird)) {
      String firstValue = (String) getValue(aFirst);
      String secondValue = (String) getValue(aSecond);
      String thirdValue = (String) getValue(aThird);
      String fourthValue = (String) getValue(aFourth);
      
      ArrayList<String> paramValues = pValues.get(firstValue, secondValue, thirdValue);
      String value = null;
      if (paramValues.contains(fourthValue))
	value = fourthValue;
      else
	value = paramValues.get(0);
      E param = 
	createEnumParam
	(aFourth, 
	 aFourthDesc, 
	 value, 
	 paramValues);
      replaceParam(param);
      toReturn = true;
    }
    return toReturn;
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

  private static final long serialVersionUID = 3213903793864433084L;
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private TripleMap<String, String, String, ArrayList<String>> pValues;
  private String aFirst;
  private String aSecond;
  private String aThird;
  private String aFourth;
  private String aFirstDesc;
  private String aSecondDesc;
  private String aThirdDesc;
  private String aFourthDesc;
}
