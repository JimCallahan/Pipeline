/*
 * Created on Sep 21, 2006
 * Created by jesse
 * For Use in us.temerity.pipeline.builders
 * 
 */
package us.temerity.pipeline.builder;

import java.util.*;

import us.temerity.pipeline.PipelineException;

/*------------------------------------------------------------------------------------------*/
/*   S U B   B U I L D E R   M A P P I N G                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Class for use with Builders, allowing for the inheritence of Builder parameters 
 * between parent Builders and their children.
 * <P>
 * An instance of this class contains three things: <ul>
 * <li>  The name of the instance, which can be used by the parent Builder to identify 
 * this particular Sub-Builder.
 * <li>  The actual instance of the Sub-Builder
 * <li>  A table that maps the name of a parameter in the Sub-Builder to a 
 * parameter in the parent Builder.
 * </ul>
 *<P> 
 * These are used by Builders which have one or more Sub-Builder which need to run.  When a 
 * Sub-Builder is created, this class needs to be instantiated with the right linking values 
 * present for all the parameters that need to be controled.  For example, if the parent 
 * Builder is used to create multiple shots in a single project, then it might have a 
 * <code> Project </code> parameter which would be linked to a <code>Project</code> parameter 
 * in each Sub-Builder used to construct a single shot.  When a Builder actually begins its execution, 
 * it should use the method to initialize all the 
 * linked parameters of its Sub-Builders with the appropriate values. 
 * 
 * This class is also used to associated {@link BaseNames} classes with Builders.  Naming classes
 * are treated like any other Sub-Builder in terms of their parameters being set by their parent
 * Builder.  Builders are responsible for knowing which of their SubBuilderMappings represent a
 * Sub-Builder to be run and which represent naming classes that are used in the course of the builder.
 * Creating distinct a distinct <code>instanceName</code> for each type is one way this could be
 * accomplished. 
 * 
 * @author Jesse Clemens
 */
public 
class SubBuilderMapping
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Constructs a SubBuilderMapping with the given name and parameter mapping
   * 
   * @param instanceName
   * 	The name of the Sub-Builder Mapping.  This should match the key in the 
   * 	{@link TreeMap} in the Builder class that contains the mapping.
   * 
   * @param subBuilder
   * 	The {@link BaseBuilder} that is the sub builder.
   * 
   * @param paramMapping
   * 	An initial Parameter Mapping in the format sub-builder parameter as the key and
   *    the master parameter as the value.
   */
  public 
  SubBuilderMapping
  (
    String instanceName,
    HasBuilderParams subBuilder,
    TreeMap<String, String> paramMapping
  ) 
  {
    pInstanceName = instanceName;
    pSubBuilder = subBuilder;
    if (paramMapping != null)
      pParamMapping = new TreeMap<String, String>(paramMapping);
    else
      pParamMapping = new TreeMap<String, String>();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the name of this Sub-Builder instance 
   * 
   * @return
   * 	The Instance Name
   */
  public String 
  getInstanceName()
  {
    return pInstanceName;
  }
  
  /**
   * Gets the Sub-Builder instance. 
   * 
   * @return
   * 	The Sub-Builder.
   */
  public HasBuilderParams 
  getSubBuilder()
  {
    return pSubBuilder;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P A R A M E T E R S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets a list of all the Parameters in the Sub-Builder that are not mapped to a Parameter 
   * in its parent Builder.
   * 
   * @return
   * 	The {@link TreeMap} containing all the unmapped {@link BuilderParam}s.
   */
  public TreeMap<String, BuilderParam>
  getUnmappedParams()
  {
    TreeMap<String, BuilderParam> toReturn = new TreeMap<String, BuilderParam>();
    TreeSet<String> mappedParams = new TreeSet<String>(pParamMapping.keySet());
    for (BuilderParam param : pSubBuilder.getParams())
    {
      String name = param.getName();
      if (!mappedParams.contains(name))
	toReturn.put(name, param);
    }
    return toReturn;
  }

  /**
   * Gets a list of all the Parameters in the Sub-Builder that are not mapped to a Parameter 
   * in its parent Builder.
   * 
   * @return
   * 	The {@link TreeSet} containing the names of all the unmapped parameters.
   */
  public TreeSet<String>
  getUnmappedParamNames()
  {
    TreeSet<String> toReturn = new TreeSet<String>();
    TreeSet<String> mappedParams = new TreeSet<String>(pParamMapping.keySet());
    for (BuilderParam param : pSubBuilder.getParams())
    {
      String name = param.getName();
      if (!mappedParams.contains(name))
	toReturn.add(name);
    }
    return toReturn;
  }
  
  /**
   * Gets a list of all the Parameters in the Sub-Builder.
   * 
   * @return
   * 	The {@link TreeMap} containing all the {@link BuilderParam}s.
   */
  public TreeSet<String>
  getAllParams()
  {
    TreeSet<String> params = new TreeSet<String>();
    for (BuilderParam param : pSubBuilder.getParams())
    {
      String name = param.getName();
      params.add(name);
    }
    return params;
  }
  
  /**
   * Gets the name of the parent Parameter that controls a Sub-Builder Param.
   * 
   * @param name
   * 	The name of the Sub-Builder Parameter.
   * @return
   * 	The name of the parent Parameter or <code>null</code> if no mapping exists.
   */
  public String 
  getMappedParam
  (
    String name
  )
  {
    return pParamMapping.get(name);
  }
  
  /**
   * Gets all the mapped parameters and the parameters that drive them.
   */
  public SortedMap<String, String> 
  getMappedParams()
  {
    return Collections.unmodifiableSortedMap(pParamMapping);
  }
  
  /**
   * Is a particular parameter of the parent Builder driving at least one parameter in
   * this Sub-Builder?
   * @param name
   * 	The name of the parent parameter
   */
  public boolean 
  isParamDriving
  (
    String name
  )
  {
    boolean toReturn = false;
    if(pParamMapping.containsValue(name))
      toReturn = true;
    return toReturn;
  }

  /**
   * Is a particular parameter of this Sub-Builder being driven by a parent parameter.
   * @param name
   * 	The parameter of the Sub-Builder to check.
   */
  public boolean 
  isParamDriven
  (
    String name
  )
  {
    boolean toReturn = false;
    if(pParamMapping.containsKey(name))
      toReturn = true;
    return toReturn;
  }
  
  /**
   * Creates a mapping between a parameter in this Sub-Builder and a parameter
   * in the parent Builder. 
   * <p>
   * It is important to not that there is not a huge amount of error checking going
   * on here.  There is no checking to make sure that the parent parameter actually
   * exists in the parent Builder.  There is no checking that the two parameters 
   * are actually the same type of parameter.  It does check that the parameter you are
   * trying to map exists in the Sub-Builder.  Any error-checking beyond that is the
   * responsibility of the coder.  (This could be changed at a later date, but is a 
   * limitation of the current design.)
   * 
   * @param subParamName
   * 	The name of the Sub-Builder parameter that is being driven.
   * @param masterParamName
   * 	The name of the parent Builder parameter that is driving the Sub-Builder parameter.
   * @throws PipelineException
   * 	Only in cases where the Sub-Builder parameter does not exist.
   */
  public void
  addMappedParam
  (
    String subParamName,
    String masterParamName
  )
    throws PipelineException
  {
    if (getAllParams().contains(subParamName))
      pParamMapping.put(subParamName, masterParamName);
    else
      throw new PipelineException
       ("Illegal attempt mapping a Builder parameter to a SubBuilder.  " +
	"The Parameter (" + subParamName + ") does not exist in the subBuilder identified " +
	"with (" + pInstanceName +  "), making the attempted mapping invalid. " +
	"The full attempted mapping was of (" + masterParamName + ") " +
	"in the master to ("+ subParamName + ") in the sub Builder." );
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
    
  private String pInstanceName;
  
  private HasBuilderParams pSubBuilder;
  
  /**
   *  Contains a mapping of the Sub-Builder Parameter name to the parent Parameter name.
   */
  private TreeMap<String, String> pParamMapping;
}