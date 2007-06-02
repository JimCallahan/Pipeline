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
import us.temerity.pipeline.MultiMap.MultiMapNamedEntry;

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
    
    if (name.contains(" "))
      throw new PipelineException
      ("A class with Builder Parameters cannot have a space in its name, " +
       "due to command-line parsing restrictions.  (" + name + ") is not a valid name.");
    
    if (name.contains("-"))
      throw new PipelineException
      ("Due to command line parsing requirements, you cannot include the '-' character " +
       "in the name of any class that uses Builder Parameters.");
    
    pParams = new TreeMap<String, BuilderParam>();
    pAllowsChildren = allowsChildren;
    pParamMapping = new TreeMap<ParamMapping, ParamMapping>();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /* G L O B A L   P A R A M E T E R S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the Class have any parameters?
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
    
    sLog.log(Kind.Bld, Level.Finest, 
      "Adding a parameter named (" + param.getName() + ") to a Builder " +
      "identified by (" + getName() + ")");
  }

  /**
   * Get the value of the parameter with the given name.
   * 
   * @param name
   *        The name of the parameter.
   * @return The parameter value.
   * @throws IllegalArgumentException if no parameter with the given name exists or if the
   * named parameter does not implement {@link SimpleParamAccess}.
   */
  @SuppressWarnings("unchecked")
  public Comparable 
  getParamValue
  (
    String name
  ) 
  {
    BuilderParam param = getParam(name);
    if ( param == null )
      throw new IllegalArgumentException
        ("Unable to determine the value of the (" + name + ") parameter!");
    if (! (param instanceof SimpleParamAccess))
      throw new IllegalArgumentException
        ("The parameter (" + name + ") in builder (" + getName() + ") does not implement " +
         "SimpleParamAccess.");
    return ((SimpleParamAccess) param).getValue();
  }
  
  /**
   * Get the value of the Simple Parameter located inside the named Complex Parameter and
   * identified by the list of keys.
   * 
   * @throws IllegalArgumentException if no parameter with the given name exists or if the
   * named parameter does not implement {@link SimpleParamAccess}.
   */
  @SuppressWarnings("unchecked")
  public Comparable
  getParamValue
  (
    String name,
    List<String> keys
  )
  {
    BuilderParam param = getParam(name);
    if ( param == null )
      throw new IllegalArgumentException
        ("Unable to determine the value of the (" + name + ") parameter!");
    
    if (keys == null || keys.isEmpty())
      return getParamValue(name);
    
    if (! (param instanceof ComplexParamAccess))
      throw new IllegalArgumentException
        ("The parameter (" + name + ") in builder (" + getName() + ") does not implement " +
         "ComplexParamAccess.");
    return ((ComplexParamAccess<BuilderParam>) param).getValue(keys); 
  }
  
  @SuppressWarnings("unchecked")
  public Comparable
  getParamValue
  (
    ParamMapping mapping
  )
  {
    if (mapping.hasKeys())
      return getParamValue(mapping.getParamName(), mapping.getKeys());
    return getParamValue(mapping.getParamName());
  }

  @SuppressWarnings("unchecked")
  public BuilderParam 
  getParam
  (
    String name,
    List<String> keys
  )
  {
    BuilderParam param = getParam(name);
    if ( param == null )
      throw new IllegalArgumentException
        ("Unable to determine the value of the (" + name + ") parameter!");
    
    if (keys == null || keys.isEmpty())
      return param;
    
    if (! (param instanceof ComplexParamAccess))
      throw new IllegalArgumentException
        ("The parameter (" + name + ") in builder (" + getName() + ") does not implement " +
         "ComplexParamAccess.");
    return ((ComplexParamAccess<BuilderParam>) param).getParam(keys); 
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
  
  @SuppressWarnings("unchecked")
  public BuilderParam
  getParam
  (
    ParamMapping mapping
  )
  {
    if (mapping.hasKeys())
      return getParam(mapping.getParamName(), mapping.getKeys());
    return getParam(mapping.getParamName());
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
   * Gets a sorted Map of all the Parameters in this builder, with the keys being
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
  public final boolean 
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
    if (! (param instanceof SimpleParamAccess))
      throw new IllegalArgumentException
        ("The parameter (" + name + ") in builder (" + getName() + ") does not implement " +
         "SimpleParamAccess.");

    ((SimpleParamAccess) param).setValue(value);
    return false;
  }

  @SuppressWarnings("unchecked")
  public final boolean
  setParamValue
  (
    String name,
    List<String> keys,
    Comparable value  
  )
  {
    BuilderParam param = getParam(name);
    if ( param == null )
      throw new IllegalArgumentException
        ("Unable to determine the value of the (" + name + ") parameter!");
    if (! (param instanceof ComplexParamAccess))
      throw new IllegalArgumentException
        ("The parameter (" + name + ") in builder (" + getName() + ") does not implement " +
         "ComplexParamAccess.");
    return ((ComplexParamAccess<BuilderParam>) param).setValue(keys, value);
  }
  
  @SuppressWarnings("unchecked")
  public final boolean
  setParamValue
  (
    ParamMapping mapping,
    Comparable value
  )
  {
    if (mapping.hasKeys())
      return setParamValue(mapping.getParamName(), mapping.getKeys(), value);
    
    return setParamValue(mapping.getParamName(), value);
  }
  
  public boolean
  hasParam
  (
    String name
  )
  {
    BuilderParam param = getParam(name);
    if ( param == null )
      return false;
    return true;
  }
  
  @SuppressWarnings("unchecked")
  public boolean
  hasParam
  (
    String name,
    List<String> keys
  )
  {
    BuilderParam param = getParam(name);
    if ( param == null )
      return false;
    
    if (keys == null || keys.isEmpty())
      return hasParam(name);
    
    if (! (param instanceof ComplexParamAccess))
      throw new IllegalArgumentException
        ("The parameter (" + name + ") in builder (" + getName() + ") does not implement " +
         "ComplexParamAccess.");
    
    return ((ComplexParamAccess<BuilderParam>) param).hasParam(keys); 
  }
  
  @SuppressWarnings("unchecked")
  public boolean
  hasParam
  (
    ParamMapping mapping
  )
  {
    if (mapping.hasKeys())
      return hasParam(mapping.getParamName(), mapping.getKeys());
    return hasParam(mapping.getParamName());
  }
  
  public boolean
  hasSimpleParam
  (
    String name
  )
  {
    BuilderParam param = getParam(name);
    if ( param == null )
      return false;
    if (param instanceof SimpleParamAccess)
      return true;
    return false;
  }
  
  @SuppressWarnings("unchecked")
  public boolean
  hasSimpleParam
  (
    String name,
    List<String> keys
  )
  {
    BuilderParam param = getParam(name);
    
    if ( param == null )
      return false;
    
    if (keys == null || keys.isEmpty())
      return hasSimpleParam(name);

    if (! (param instanceof ComplexParamAccess))
      throw new IllegalArgumentException
        ("The parameter (" + name + ") in builder (" + getName() + ") does not implement " +
         "ComplexParamAccess.");
    
    return ((ComplexParamAccess<BuilderParam>) param).hasSimpleParam(keys); 
  }
  
  @SuppressWarnings("unchecked")
  public boolean
  hasSimpleParam
  (
    ParamMapping mapping
  )
  {
    if (mapping.hasKeys())
      return hasSimpleParam(mapping.getParamName(), mapping.getKeys());
    return hasSimpleParam(mapping.getParamName());
  }
  
  public boolean
  canSetSimpleParamFromString
  (
    String name
  )
  {
    BuilderParam param = getParam(name);
    if ( param == null )
      return false;
    if (param instanceof SimpleParamFromString)
      return true;
    return false;
  }
  
  @SuppressWarnings("unchecked")
  public boolean
  canSetSimpleParamFromString
  (
    String name,
    List<String> keys
  )
  {
    BuilderParam param = getParam(name);
    
    if ( param == null )
      return false;
    
    if (keys == null || keys.isEmpty())
      return hasSimpleParam(name);

    if (! (param instanceof ComplexParamAccess))
      throw new IllegalArgumentException
        ("The parameter (" + name + ") in builder (" + getName() + ") does not implement " +
         "ComplexParamAccess.");
    
    return ((ComplexParamAccess<BuilderParam>) param).canSetSimpleParamFromString(keys); 
  }
  
  @SuppressWarnings("unchecked")
  public boolean
  canSetSimpleParamFromString
  (
    ParamMapping mapping
  )
  {
    if (mapping.hasKeys())
      return canSetSimpleParamFromString(mapping.getParamName(), mapping.getKeys());
    return canSetSimpleParamFromString(mapping.getParamName());
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
    pLayout = layout;
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

//  public AdvancedLayoutGroup
//  getEditedPassLayout
//  (
//    int pass
//  )
//  {
//    AdvancedLayoutGroup toReturn = new AdvancedLayoutGroup(getLayout().getPassLayout(pass));
//    TreeSet<String> mapped = getMappedParamNames(pass);
//    for(String param : mapped) 
//      toReturn.removeEntry(param);
//    return toReturn;
//  }
  
  
  
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
  
  protected final void
  addParamMapping
  (
    ParamMapping subParam,
    ParamMapping masterParam
  )
  {
    pParamMapping.put(subParam, masterParam);
  }

  /**
   * Gets all the mapped parameters and the parameters that drive them.
   */
  public final SortedMap<ParamMapping, ParamMapping> 
  getMappedParams()
  {
    return Collections.unmodifiableSortedMap(pParamMapping);
  }
  
  /**
   * Gets all the mapped parameters and the parameters that drive them.
   */
  public final Set<ParamMapping> 
  getMappedParamNames()
  {
    return Collections.unmodifiableSet(pParamMapping.keySet());
  }
  
  /**
   * Returns the prefix name for this builder, which includes the full path to this builder as
   * a '-' separate list.
   * 
   * If this method is called on something with Builder Parameters which is used as a
   * Sub-Builder, then it should only be used after
   * {@link BaseBuilder#addSubBuilder(HasBuilderParams)} is called. The addSubBuilder method
   * is responsible for setting the value returned by this function correctly. If this method
   * is called before the value is correctly set, it will just return the name of the builder.
   * 
   */
  public PrefixedName 
  getPrefixedName()
  {
    if (pPrefixName == null)
      return new PrefixedName(getName());
    return pPrefixName;
  }
  
  public void
  setPrefixedName
  (
    PrefixedName namedPrefix
  )
  {
    pPrefixName = new PrefixedName(namedPrefix);
  }
  
  public abstract int
  getCurrentPass();
  
  protected final void
  assignCommandLineParams()
    throws PipelineException
  {
    boolean abort = BaseBuilder.getAbortOnBadParam();
    
    int currentPass = getCurrentPass();
    TreeSet<String> passParams = getPassParamNames(currentPass);
    
    String prefixName = getPrefixedName().toString();
    
    MultiMap<String, String> specificEntrys = 
      BaseBuilder.getCommandLineParams().get(prefixName);
    
    sLog.log(Kind.Arg, Level.Fine, 
      "Assigning command line parameters for Builder identified by (" + prefixName + ") " +
      "for pass number (" + currentPass + ")");
    
    if (specificEntrys == null) {
      sLog.log(Kind.Arg, Level.Finer, 
	"No command line parameters for Builder with prefixName (" + prefixName + ")");
      return;
    }
    
    /* This creates a Mapped ArrayList with Parameter name as the key set and
     * the list of keys contained in the MultiMapNamedEntry as the key set.
     */
    MappedArrayList<String, MultiMapNamedEntry<String, String>> commandLineValues = 
      specificEntrys.namedEntries();
    
    
    if(commandLineValues != null) {

      Set<ParamMapping> mappedParams = pParamMapping.keySet();
      
      for (String paramName : commandLineValues.keySet()) {
	if (!passParams.contains(paramName))
	  continue;
	for (MultiMapNamedEntry<String, String> entry : commandLineValues.get(paramName)) {
	  List<String> keys = entry.getKeys();
	  ParamMapping mapping = new ParamMapping(paramName, keys);
	  if (!mappedParams.contains(mapping)) {
	    String value = entry.getValue();
	    if (canSetSimpleParamFromString(mapping)) {
	      SimpleParamFromString param = (SimpleParamFromString) getParam(mapping);
	      try {
		param.fromString(value);
	      } 
	      catch (IllegalArgumentException ex) {
		String message = "There was an error setting the value of a Parameter " +
		  "from a command line argument.\n" + ex.getMessage(); 
		if (abort)
    		  throw new PipelineException(message);
		sLog.log(Kind.Arg, Level.Warning, message);
	      }
	      sLog.log(Kind.Arg, Level.Finest, 
		"Setting command line parameter (" + mapping + ") from builder " +
		"(" + prefixName + ") with the value (" + value + ").");
	    }
	    else {
	      String message = "Cannot set command line parameter (" + mapping + ") " +
	      	"from builder (" + prefixName + ") with the value (" + value + ").\n" +
	        "Parameter is not a Simple Parameter"; 
	      if (abort)
		throw new PipelineException(message);
	      sLog.log(Kind.Arg, Level.Warning, message);
	    }
	  }
	}
      }
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P A R A M E T E R   L O O K U P                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the selected index of the single valued Enum parameter with the given name.<P> 
   * 
   * @param mapping
   *   The name and keys of the parameter. 
   *
   * @return 
   *   The index value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists.
   * @throws ClassCastException
   *   If the parameter is not an EnumParameter.
   */ 
  public int
  getEnumParamIndex
  (
   ParamMapping mapping
  ) 
    throws PipelineException
  {
    EnumBuilderParam param = (EnumBuilderParam) getParam(mapping);
    if(param == null) 
      throw new PipelineException
        ("The required parameter (" + mapping + ") does not exist!"); 
      
    return param.getIndex();
  }

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null Boolean parameter with the given name.
   * 
   * @param mapping
   *   The name and keys of the parameter. 
   *
   * @return 
   *   The parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists.
   */ 
  public boolean
  getBooleanParamValue
  (
    ParamMapping mapping
  ) 
    throws PipelineException
  {
    Boolean value = (Boolean) getParamValue(mapping);  
    if(value == null) 
      throw new PipelineException
        ("The required parameter (" + mapping + ") was not set!"); 

    return value;
  }  

  /** 
   * Get the value of the single valued Boolean parameter with the given name.<P> 
   * 
   * If <CODE>null</CODE> value is treated as <CODE>false</CODE>.
   * 
   * @param mapping
   *   The name and keys of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   */ 
  public boolean
  getOptionalBooleanParamValue
  (
    ParamMapping mapping
  ) 
  {
    Boolean value = (Boolean) getParamValue(mapping); 
    return ((value != null) && value);
  } 
  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null Long parameter with the given name.<P> 
   * 
   * This method can be used to retrieve ByteSizeBuilderParam values.
   * 
   * @param mapping
   *   The name and keys of the parameter. 
   *
   * @return 
   *   The parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists or 
   *   the value is <CODE>null</CODE>.
   */ 
  public long
  getLongParamValue
  (
    ParamMapping mapping
  ) 
    throws PipelineException
  {
    return getLongParamValue(mapping, null, null);
  }

  /** 
   * Get the lower bounds checked value of the single valued non-null Long parameter with 
   * the given name. <P> 
   * 
   * Legal values must satisfy: (minValue <= value) <P> 
   * 
   * This method can be used to retrieve ByteSizeBuilderParam values.
   * 
   * @param mapping
   *   The name and keys of the parameter. 
   *
   * @param minValue
   *   The minimum (inclusive) legal value or <CODE>null</CODE> for no lower bounds.
   * 
   * @return 
   *   The parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public long
  getLongParamValue
  (
    ParamMapping mapping,
    Long minValue 
  ) 
    throws PipelineException
  {
    return getLongParamValue(mapping, minValue, null);
  }

  /** 
   * Get the bounds checked value of the single valued non-null Long parameter with 
   * the given name. <P> 
   * 
   * Legal values must satisfy: (minValue <= value <= maxValue)<P> 
   * 
   * This method can be used to retrieve ByteSizeBuilderParam values.
   * 
   * @param mapping
   *   The name and keys of the parameter. 
   *
   * @param minValue
   *   The minimum (inclusive) legal value or <CODE>null</CODE> for no lower bounds.
   * 
   * @param maxValue
   *   The maximum (inclusive) legal value or <CODE>null</CODE> for no upper bounds.
   * 
   * @return 
   *   The parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public long
  getLongParamValue
  (
    ParamMapping mapping,
    Long minValue, 
    Long maxValue
  ) 
    throws PipelineException
  {
    Long value = (Long) getParamValue(mapping); 
    if(value == null) 
      throw new PipelineException
        ("The required parameter (" + mapping + ") was not set!"); 

    if((minValue != null) && (value < minValue)) 
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + mapping + ") was less-than the " + 
         "minimum allowed value (" + minValue + ")!");
    
    if((maxValue != null) && (value > maxValue)) 
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + mapping + ") was greater-than the " + 
         "maximum allowed value (" + maxValue + ")!");

    return value;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null Integer parameter with the given name.
   * 
   * @param mapping
   *   The name and keys of the parameter. 
   *
   * @return 
   *   The parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists or 
   *   the value is <CODE>null</CODE>.
   */ 
  public int
  getIntegerParamValue
  (
    ParamMapping mapping
  ) 
    throws PipelineException
  {
    return getIntegerParamValue(mapping, null, null);
  }

  /** 
   * Get the lower bounds checked value of the single valued non-null Integer parameter with 
   * the given name. <P> 
   * 
   * Legal values must satisfy: (minValue <= value) 
   * 
   * @param mapping
   *   The name and keys of the parameter. 
   *
   * @param minValue
   *   The minimum (inclusive) legal value or <CODE>null</CODE> for no lower bounds.
   * 
   * @return 
   *   The parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public int
  getIntegerParamValue
  (
    ParamMapping mapping,
    Integer minValue 
  ) 
    throws PipelineException
  {
    return getIntegerParamValue(mapping, minValue, null);
  }

  /** 
   * Get the bounds checked value of the single valued non-null Integer parameter with 
   * the given name. <P> 
   * 
   * Legal values must satisfy: (minValue <= value <= maxValue)
   * 
   * @param mapping
   *   The name and keys of the parameter. 
   *
   * @param minValue
   *   The minimum (inclusive) legal value or <CODE>null</CODE> for no lower bounds.
   * 
   * @param maxValue
   *   The maximum (inclusive) legal value or <CODE>null</CODE> for no upper bounds.
   * 
   * @return 
   *   The parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public int
  getIntegerParamValue
  (
    ParamMapping mapping,
    Integer minValue, 
    Integer maxValue
  ) 
    throws PipelineException
  {
    Integer value = (Integer) getParamValue(mapping); 
    if(value == null) 
      throw new PipelineException
        ("The required parameter (" + mapping + ") was not set!"); 

    if((minValue != null) && (value < minValue)) 
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + mapping + ") was less-than the " + 
         "minimum allowed value (" + minValue + ")!");
    
    if((maxValue != null) && (value > maxValue)) 
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + mapping + ") was greater-than the " + 
         "maximum allowed value (" + maxValue + ")!");

    return value;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null Double parameter with the given name.
   * 
   * @param mapping
   *   The name and keys of the parameter. 
   *
   * @return 
   *   The parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists or 
   *   the value is <CODE>null</CODE>.
   */ 
  public double
  getDoubleParamValue
  (
    ParamMapping mapping
  ) 
    throws PipelineException
  {
    return getDoubleParamValue(mapping, null, null);
  }

  /** 
   * Get the lower bounds checked value of the single valued non-null Double parameter with 
   * the given name. <P> 
   * 
   * Legal values must satisfy: (lower < value)
   * 
   * @param mapping
   *   The name and keys of the parameter. 
   *
   * @param lower
   *   The lower bounds (exclusive) of legal values or <CODE>null</CODE> for no lower bounds.
   * 
   * @return 
   *   The parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists, 
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public double
  getDoubleParamValue
  (
    ParamMapping mapping,
    Double lower
  ) 
    throws PipelineException
  {
    return getDoubleParamValue(mapping, lower, null);
  }

  /** 
   * Get the bounds checked value of the single valued non-null Double parameter with 
   * the given name. <P> 
   * 
   * Legal values must satisfy: (lower < value < upper)
   * 
   * @param mapping
   *   The name and keys of the parameter. 
   *
   * @param lower
   *   The lower bounds (exclusive) of legal values or <CODE>null</CODE> for no lower bounds.
   * 
   * @param upper
   *   The upper bounds (exclusive) of legal values or <CODE>null</CODE> for no upper bounds.
   * 
   * @return 
   *   The parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists, 
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public double
  getDoubleParamValue
  (
    ParamMapping mapping,
    Double lower, 
    Double upper
  ) 
    throws PipelineException
  {
    Double value = (Double) getParamValue(mapping); 
    if(value == null) 
      throw new PipelineException
        ("The required parameter (" + mapping + ") was not set!"); 
    
    if((lower != null) && (value <= lower)) 
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + mapping + ") was not greater-than the " + 
         "the lower bounds (" + lower + ") for legal values!");
    
    if((upper != null) && (value >= upper)) 
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + mapping + ") was not less-than the " + 
         "the upper bounds (" + upper + ") for legal values!");

    return value;
  }


  /*----------------------------------------------------------------------------------------*/


  /** 
   * Get the value of the single valued String parameter with the given name.
   * 
   * @param mapping  
   *   The name of the parameter. 
   *
   * @return 
   *   The parameter value or <CODE>null</CODE> if the value is null or the empty string. 
   * 
   */ 
  public String
  getStringParamValue
  (
    ParamMapping mapping   
  ) 
  { 
    String value = (String) getParamValue(mapping); 
    if((value != null) && (value.length() > 0))
      return value;

    return null;    
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Instance of the log manager for builder logging purposes.
   */
  protected static LogMgr sLog = LogMgr.getInstance();
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
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
  private TreeMap<ParamMapping, ParamMapping> pParamMapping;
  
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
  private PrefixedName pPrefixName = null;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   C L A S S E S                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  public static
  class ParamMapping
    implements Comparable<ParamMapping>
  {
    public
    ParamMapping
    (
      ParamMapping mapping
    )
    {
      this(mapping.getParamName(), mapping.getKeys());
    }
    
    public
    ParamMapping
    (
      String paramName
    )
    {
      this(paramName, null);
    }
    
    public
    ParamMapping
    (
      String paramName,
      List<String> keys
    )
    {
      if (paramName == null)
	throw new IllegalArgumentException("Cannot have a null parameter name");
      pParamName = paramName;
      if (keys == null)
	pKeys = null;
      else if (keys.isEmpty())
	pKeys = null;
      else
	pKeys = new LinkedList<String>(keys);
    }
    
    public String
    getParamName()
    {
      return pParamName;
    }
    
    public boolean
    hasKeys()
    {
      if (pKeys == null)
	return false;
      return true;
    }
    
    public List<String>
    getKeys()
    {
      if (pKeys == null)
	return null;
      return Collections.unmodifiableList(pKeys);
    }
    
    public void
    addKey
    (
      String key
    )
    {
      if (pKeys == null)
	pKeys = ComplexParam.listFromObject(key);
      else
	pKeys.add(key);
    }

    public int 
    compareTo
    (
      ParamMapping that
    )
    {
      int compare = this.pParamName.compareTo(that.pParamName);
      if (compare != 0)
	return compare;
      if (this.pKeys == null) {
	if (that.pKeys == null)
	  return 0;
	return -1;
      }
      if (that.pKeys == null)
	return 1;
      int thisSize = this.pKeys.size();
      int thatSize = that.pKeys.size();
      if (thisSize > thatSize)
	return 1;
      else if (thatSize < thisSize)
	return -1;
      for (int i = 0; i < thisSize; i++) {
	String thisKey = this.pKeys.get(i);
	String thatKey = that.pKeys.get(i);
	compare = thisKey.compareTo(thatKey);
	if (compare != 0)
	  return compare;
      }
      return 0;
    }
    
    @Override
    public boolean 
    equals
    (
      Object obj
    )
    {
      if (!(obj instanceof ParamMapping ) )
	return false;
      ParamMapping mapping = (ParamMapping) obj;
      int compare = this.compareTo(mapping);
      if (compare == 0)
	return true;
      return false;
    }

    @Override
    public String
    toString()
    {
      return "Param Name (" + pParamName + ") with Keys: " + pKeys;
    }
    
    private String pParamName;
    private LinkedList<String> pKeys;
  }
  
  public static 
  class PrefixedName
  {
    public
    PrefixedName
    (
      LinkedList<String> prefixes,
      String name
    )
    {
      if (prefixes == null)
	pPrefixes = new LinkedList<String>();
      else
	pPrefixes = new LinkedList<String>(prefixes);
      if (name != null)
	pPrefixes.add(name);
    }
    
    public PrefixedName
    (
      String name
    )
    {
      pPrefixes = ComplexParam.listFromObject(name);
    }
    
    public PrefixedName
    (
      PrefixedName prefixName,
      String name
    )
    {
      if (prefixName.pPrefixes == null)
	pPrefixes = new LinkedList<String>();
      else
	pPrefixes = new LinkedList<String>(prefixName.pPrefixes);
      if (name != null)
	pPrefixes.add(name);
    }
    
    public PrefixedName
    (
      PrefixedName prefixName
    )
    {
      if (prefixName.pPrefixes == null)
	pPrefixes = new LinkedList<String>();
      else
	pPrefixes = new LinkedList<String>(prefixName.pPrefixes);
    }
    
    @Override
    public String toString()
    {
      StringBuilder toReturn = new StringBuilder();
      for (String each : pPrefixes) {
	if (toReturn.length() > 0)
	  toReturn.append(" - ");
	toReturn.append(each);
      }
      return toReturn.toString();
    }
    
    @Override
    public boolean 
    equals
    (
      Object that
    )
    {
      if (!(that instanceof PrefixedName)) 
	return false;
      PrefixedName that1 = (PrefixedName) that;
      if (that1.toString().equals(this.toString()))
	return true;
      return false;
    }

    private LinkedList<String> pPrefixes;
  }
}
