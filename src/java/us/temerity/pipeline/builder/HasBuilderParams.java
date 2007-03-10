/*
 * Created on Nov 2, 2006
 * Created by jesse
 * For Use in us.temerity.pipeline.builders.stages
 * 
 */
package us.temerity.pipeline.builder;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;

public abstract 
class HasBuilderParams
  extends BaseUtil
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  protected 
  HasBuilderParams
  (
    String name,
    String desc,
    boolean allowsChildren
  ) 
    throws PipelineException
  {
    super(name, desc);
    
    if (name.contains("-"))
      throw new PipelineException
      ("Due to command line parsing requirements, you cannot include the '-' character " +
       "in the name of any class that uses Builder Parameters.");
    
    pParams = new TreeMap<String, BuilderParam>();
    pAllowsChildren = allowsChildren;
    pParamMapping = new TreeMap<String, String>();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /* G L O B A L   P A R A M E T E R S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the Class have any global parameters?
   */
  public boolean 
  hasParams()
  {
      return ( !pParams.isEmpty() );
  }

  /**
   * Add a parameter to this Class.
   * <P>
   * This method is used by subclasses in their constructors to initialize the set of
   * global parameters that they support.
   * 
   * @param param
   *        The parameter to add.
   */
  protected void 
  addParam
  (
    BuilderParam param
  )
  {
    if ( pParams.containsKey(param.getName()) )
      throw new IllegalArgumentException
        ("A parameter named (" + param.getName() + ") already exists!");

    pParams.put(param.getName(), param);
  }

  /**
   * Get the value of the parameter with the given name.
   * 
   * @param name
   *        The name of the parameter.
   * @return The parameter value.
   * @throws PipelineException
   *         If no parameter with the given name exists.
   */
  @SuppressWarnings("unchecked")
  public Comparable 
  getParamValue
  (
    String name
  ) 
    throws PipelineException
  {
    BuilderParam param = getParam(name);
    if ( param == null )
      throw new PipelineException
        ("Unable to determine the value of the (" + name + ") parameter!");
    return param.getValue();
  }

  /**
   * Get the parameter with the given name.
   * 
   * @param name
   *        The name of the parameter.
   * @return The parameter or <CODE>null</CODE> if no parameter with the given name
   *         exists.
   */
  public BuilderParam 
  getParam
  (
    String name
  )
  {
    if ( name == null )
      throw new IllegalArgumentException("The parameter name cannot be (null)!");
    return pParams.get(name);
  }

  public Collection<BuilderParam> 
  getParams()
  {
    return Collections.unmodifiableCollection(pParams.values());
  }
  
  public Collection<String> 
  getParamNames()
  {
    return Collections.unmodifiableCollection(pParams.keySet());
  }
  
  /**
   * Gets a sorted Map of all the Parametesr in this builder, with the keys being
   * the name of the parameters and the values the actual parameters.
   */
  public SortedMap<String, BuilderParam> 
  getParamMap()
  {
    return Collections.unmodifiableSortedMap(pParams);
  }
  
  /**
   * Set the value of a parameter.
   * 
   * @param name
   *        The name of the parameter.
   * @param value
   *        The new value of the parameter.
   */
  @SuppressWarnings("unchecked")
  public void 
  setParamValue
  (
    String name, 
    Comparable value
  )
  {
    if ( name == null )
      throw new IllegalArgumentException("The parameter name cannot be (null)!");

    BuilderParam param = pParams.get(name);
    if ( param == null )
      throw new IllegalArgumentException
        ("No parameter named (" + param.getName() + ") exists for this extension!");

    param.setValue(value);
  }

  /**
   * Copy the values of all of the parameters from the given Class.
   * <P>
   * Note that there is no requirement that the given extension be the same plugin type or
   * version. Any incompatible parameters will simply be ignored by the copy operation.
   * 
   * @param extension
   *        The extension to use as the source of parameter values.
   */
  public void setParamValues
  (
    BaseBuilder builder
  )
  {
    for(String name : pParams.keySet()) {
      BuilderParam aparam = builder.getParam(name);
      if(aparam != null) {
	BuilderParam param = pParams.get(name);
	try {
	  param.setValue(aparam.getValue());
	}
	catch(IllegalArgumentException ex) {
	  LogMgr.getInstance().log(LogMgr.Kind.Ops, LogMgr.Level.Warning, ex.getMessage());
	}
      }
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P A R A M E T E R    L A Y O U T                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sets the hierarchical grouping of parameters which determine the layout of 
   * UI components in different passes. <P> 
   * 
   * The given layouts must contain an entry for all parameters 
   * defined for the action exactly once in all the passes.  A collapsible drawer component 
   * will be created for each layout group which contains a field for each parameter
   * entry in the order specified by the group.  All <CODE>null</CODE> entries will cause 
   * additional space to be added between the UI fields. Each layout subgroup will be represented 
   * by its own drawer nested within the drawer for the parent layout group. <P> 
   * 
   * This method should be called by subclasses in their constructor after building the appropriate 
   * {@link PassLayoutGroup}.
   * 
   * @param groups
   *   The layout group broken down by passes.
   */
  protected void
  setLayout
  (
    PassLayoutGroup layout
  ) 
  {
    TreeSet<String> names = new TreeSet<String>();
    for(AdvancedLayoutGroup advanced : layout.getAllLayouts().values()) {
      collectLayoutNames(advanced, names);
    }
    
    for(String name : names) {
      if(!pParams.containsKey(name))
	throw new IllegalArgumentException
	  ("The entry (" + name + ") specified by the builder parameter layout group " + 
	   "does not match any single valued parameter defined for this Builder!");
    }

    for(String name : pParams.keySet()) {
      if(!names.contains(name))
	throw new IllegalArgumentException
	  ("The single valued parameter (" + name + ") defined by this Builder was not " + 
	   "specified by any the parameter layout group!");
    }
  }
  
  /**
   * Returns a name list for all the parameters that are contained in a particular
   * pass of the layout.
   * 
   * @param pass Which pass the name list is generated for.
   */
  public TreeSet<String> 
  getPassParamNames
  (
    int pass 
  )
  {
    TreeSet<String> toReturn = new TreeSet<String>();
    collectLayoutNames(getLayout().getPassLayout(pass), toReturn);
    return toReturn;
  }
  
  
  /**
   * Recursively search the parameter groups to collect the parameter names and verify
   * that no parameter is specified more than once.
   */ 
  private void 
  collectLayoutNames
  (
   AdvancedLayoutGroup group, 
   TreeSet<String> names
  ) 
  {
    for (Integer column : group.getAllColumns()) {
      for(String name : group.getEntries(column)) {
	if(name != null) {
	  if(names.contains(name)) 
	    throw new IllegalArgumentException
    	      ("The single valued parameter (" + name + ") was specified more than once " +
  	       "in the given parameter group!");
	  names.add(name);
	}
      }
      for(LayoutGroup sgroup : group.getSubGroups(column)) 
	      collectLayoutNames(sgroup, names);
    }
  }

  /**
   * Get the grouping of parameters used to layout components which represent 
   * the parameters in the user interface. <P> 
   * 
   * If no parameter group has been previously specified, a group will 
   * be created which contains all parameters in alphabetical order, in a single pass.
   */ 
  public PassLayoutGroup
  getLayout()
  {
    if(pLayout == null) {
      AdvancedLayoutGroup layout = 
	new AdvancedLayoutGroup("Pass 1", "The first pass of the params.", "Column 1", true);
      for(String name : pParams.keySet()) 
	layout.addEntry(1, name);
      pLayout = 
	new PassLayoutGroup("BuilderParams", "The BuilderParams", layout.getName(), layout );
    }
    return pLayout; 
  }
  
  /**
   * Gets the layout for a particular pass.
   * 
   * @param pass
   * 	The number of the pass.
   */
  public AdvancedLayoutGroup
  getPassLayout
  (
    int pass
  )
  {
    return getLayout().getPassLayout(pass);
  }

  public AdvancedLayoutGroup
  getEditedPassLayout
  (
    int pass
  )
  {
    AdvancedLayoutGroup toReturn = new AdvancedLayoutGroup(getLayout().getPassLayout(pass));
    TreeSet<String> mapped = getMappedParamNames(pass);
    for(String param : mapped) 
      toReturn.removeEntry(param);
    return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S U B - B U I L D E R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Is this class allowed to have children?
   * <p>
   * This is important to know when doing things like recursing through nested Builders
   * to build layouts.
   * <p>
   * This value should be set by Abstract classes that inherit from this class and not by
   * the actual implementing classes that use the Abstract classes.  Those Abstract classes need
   * to define methods to deal with the complexities discussed above. 
   */
  public boolean
  allowsChildren()
  {
    return pAllowsChildren;
  }
  
  /**
   * Creates a mapping between a parameter in this Sub-Builder and a parameter
   * in the parent Builder. 
   * <p>
   * It is important to note that there is not a huge amount of error checking going
   * on here.  There is no checking to make sure that the parent parameter actually
   * exists in the parent Builder.  There is no checking that the two parameters 
   * are actually the same type of parameter.  It does check that the parameter you are
   * trying to map exists in the Sub-Builder.  
   * <p>
   * More extensive error checking is done at the point where the mapping is actually 
   * carried out, so you're going to throw an Exception eventually, just not necessarily here.
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
    if (getParamNames().contains(subParamName))
      pParamMapping.put(subParamName, masterParamName);
    else
      throw new PipelineException
        ("Illegal attempt mapping a Builder parameter to a SubBuilder.  " +
	 "The Parameter (" + subParamName + ") does not exist in the subBuilder identified " +
	 "with (" + getName() +  "), making the attempted mapping invalid. " +
	 "The full attempted mapping was of (" + masterParamName + ") " +
	 "in the master to ("+ subParamName + ") in the sub Builder." );
  }

  /**
   * Creates mappings between parameters in this Sub-Builder and parameters
   * in the parent Builder. 
   * <p>
   * It is important to note that there is not a huge amount of error checking going
   * on here.  There is no checking to make sure that the parent parameter actually
   * exists in the parent Builder.  There is no checking that the two parameters 
   * are actually the same type of parameter.  It does check that the parameter you are
   * trying to map exists in the Sub-Builder.   
   * <p>
   * More extensive error checking is done at the point where the mapping is actually 
   * carried out, so you're going to throw an Exception eventually, just not necessarily here.
   * 
   * @param paramMapping
   * 	A mapping with a keyset of Sub-Builder parameter names, each with a corresponding
   * 	value which is a parent Builder parameter name.
   * @throws PipelineException
   * 	Only in cases where a Sub-Builder parameter does not exist.
   */

  public void
  addMappedParams
  (
    TreeMap<String, String> paramMapping
  ) 
    throws PipelineException
  {
    for(String subParamName : paramMapping.keySet()) {
      String masterParamName = paramMapping.get(subParamName);
      addMappedParam(subParamName, masterParamName);
    }
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
   * Gets all the ummapped parameters, indexed by parameter name.
   */
  public TreeMap<String, BuilderParam> 
  getUnmappedParams()
  {
    TreeMap<String, BuilderParam> toReturn = new TreeMap<String, BuilderParam>(pParams);
    for (String mapped : pParamMapping.keySet())
      toReturn.remove(mapped);
    return toReturn;
  }
  
  /**
   * 
   * @param pass
   * @return
   */
  public TreeSet<String>
  getMappedParamNames
  (
    int pass
  )
  {
    TreeSet<String> paramNames = getPassParamNames(pass);
    TreeSet<String> toReturn = new TreeSet<String>();
    Set<String> mappedParams = getMappedParams().keySet();
    for(String mapped : mappedParams) {
      if (paramNames.contains(mapped))
	toReturn.add(mapped);
    }
    return toReturn;
  }
  
  public String 
  getNamedPrefix()
  {
    return pNamedPrefix;
  }
  
  public void
  setNamedPrefix
  (
    String namedPrefix
  )
  {
    pNamedPrefix = namedPrefix;
  }
  
  protected final void
  assignCommandLineParams()
  {
    String prefixName = getNamedPrefix();
    if(prefixName == null)
      prefixName = getName();
    TreeMap<String, String> commandLineParams = BaseBuilder.getCommandLineParams().get(prefixName);
    
    if(commandLineParams != null) {

      TreeMap<String, BuilderParam> unmappedParams = getUnmappedParams();

      TreeMap<String, String> searchMap = new TreeMap<String, String>();
      {
	for(String unmapped : unmappedParams.keySet()) {
	  BuilderParam param = unmappedParams.get(unmapped);
	  if(param instanceof PrimitiveBuilderParam)
	    searchMap.put(unmapped, unmapped);
	  else if(param instanceof ComplexBuilderParam) {
	    for(String key : (((ComplexBuilderParam) param).listOfKeys()))
	      searchMap.put(key, unmapped);
	  }
	}
      }

      Set<String> searchSet = searchMap.keySet();
      for(String paramKey : commandLineParams.keySet()) {
	if(searchSet.contains(paramKey)) {
	  String value = commandLineParams.get(paramKey);
	  String paramName = searchMap.get(paramKey);
	  BuilderParam param = unmappedParams.get(paramName);
	  if(param instanceof PrimitiveBuilderParam)
	    ((PrimitiveBuilderParam) param).valueFromString(value);
	  else if(param instanceof ComplexBuilderParam)
	    ((ComplexBuilderParam) param).valueFromString(paramKey, value);
	  else
  	    LogMgr.getInstance().log(Kind.Arg, Level.Warning, 
	      "The command line parameter (" + paramKey + ") from builder (" + prefixName + ") " +
	      "has an unknown parameter Interface.  It will be ignored.");
	}
	else
	  LogMgr.getInstance().log(Kind.Arg, Level.Warning, 
	    "The command line parameter (" + paramKey + ") from builder (" + prefixName + ") " +
	    "cannot be found in the list of unmapped parameters.  It will be ignored.");
      } // for(String paramKey : commandLineParams.keySet())
    } // if(commandLineParams != null) {
  }

  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The table of Builder parameters.
   */
  protected TreeMap<String, BuilderParam> pParams;
  
  /**
   * Specifies the grouping of parameters used to layout components which 
   * represent the parameters in the user interface, broken down by pass. 
   */ 
  private PassLayoutGroup pLayout;
  
  /**
   *  Contains a mapping of the Sub-Builder Parameter name to the parent Parameter name.
   */
  private TreeMap<String, String> pParamMapping;
  
  /**
   * Is the class that inherets from this class allowed to have children?
   * <p>
   * This is an important consideration, since having child {@link HasBuilderParams} means
   * that those classes have to be able to maange those children.  It also means that 
   * calculations of things like the maximum number of passes necessary to run or the collection
   * of layouts that much more difficult.
   * <p>
   * This value should be set by Abstract classes that inherit from this class and not by
   * the actual implementing classes that use the Abstract classes.  Those Abstract classes need
   * to define methods to deal with the complexities discussed above. 
   */
  private final boolean pAllowsChildren;
  
  /**
   * 
   */
  private String pNamedPrefix = null;
}
