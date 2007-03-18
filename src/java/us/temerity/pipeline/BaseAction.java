// $Id: BaseAction.java,v 1.41 2007/03/18 02:33:18 jim Exp $

package us.temerity.pipeline;

import  us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   A C T I O N                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The superclass of all Pipelin node action plugins. <P>
 * 
 * Actions are used by Pipeline to regenerate the files associated with non-leaf nodes
 * in a consistent and reliable manner. <P> 
 * 
 * New kinds of actions can be written by subclassing this class.  Due to the way plugins
 * are loaded and communicated between applications, any fields added to a subclass will
 * be reinitialized when the action is stored to disk or when it is sent over the network.
 * Any data which must be retained by the action should be stored in an action parameter
 * instead. <P> 
 * 
 * While new plugin subclass versions are being modified and tested the 
 * {@link #underDevelopment underDevelopment} method should be called in the subclasses
 * constructor to enable the plugin to be dynamically reloaded.  Nodes which use one of 
 * these dynamically reloadable Action plugins cannot be Checked-In until the plugin is
 * no longer under development.  
 */
public 
class BaseAction
  extends BasePlugin
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
  BaseAction() 
  {
    super();

    pSingleParams    = new TreeMap<String,ActionParam>();
    pSourceParams    = new TreeMap<String,TreeMap<String,ActionParam>>();
    pSecondaryParams = new TreeMap<String,TreeMap<FilePattern,TreeMap<String,ActionParam>>>();
    pPresetChoices   = new TreeMap<String,ArrayList<String>>();
    pPresetValues    = new TreeMap<String,TreeMap<String,TreeMap<String,Comparable>>>();
  }

  /** 
   * Construct with the given name, version, vendor and description. 
   * 
   * @param name 
   *   The short name of the action.  
   * 
   * @param vid
   *   The action plugin revision number.
   * 
   * @param vendor
   *   The name of the plugin vendor.
   * 
   * @param desc 
   *   A short description of the action.
   */ 
  protected
  BaseAction
  (
   String name,  
   VersionID vid,
   String vendor, 
   String desc
  ) 
  {
    super(name, vid, vendor, desc);

    pSingleParams    = new TreeMap<String,ActionParam>();
    pSourceParams    = new TreeMap<String,TreeMap<String,ActionParam>>();
    pSecondaryParams = new TreeMap<String,TreeMap<FilePattern,TreeMap<String,ActionParam>>>();
    pPresetChoices   = new TreeMap<String,ArrayList<String>>();
    pPresetValues    = new TreeMap<String,TreeMap<String,TreeMap<String,Comparable>>>();
  }

  /**
   * Copy constructor. <P> 
   * 
   * Used internally to create a generic instances of plugin subclasses.  This constructor
   * should not be used in end user code! <P> 
   */ 
  public 
  BaseAction
  (
   BaseAction action
  ) 
  {
    super(action.pName, action.pVersionID, action.pVendor, action.pDescription);

    setSupports(action.getSupports());

    pSingleParams    = action.pSingleParams;
    pSourceParams    = action.pSourceParams; 
    pSecondaryParams = action.pSecondaryParams; 
    pPresetChoices   = action.pPresetChoices;
    pPresetValues    = action.pPresetValues;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get which general type of plugin this is. 
   */ 
  public PluginType
  getPluginType()
  {
    return PluginType.Action;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S I N G L E   V A L U E D   P A R A M E T E R S                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does the action have any single valued parameters?
   */ 
  public boolean 
  hasSingleParams()
  {
    return (!pSingleParams.isEmpty());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Add a single valued parameter to this Action. <P>
   *
   * This method is used by subclasses in their constructors initialize the set of 
   * single valued parameters that they support.
   *
   * @param param  
   *   The parameter to add.
   */
  protected void 
  addSingleParam
  (
    ActionParam param 
  ) 
  {
    String name = param.getName();
    
    if(pSingleParams.containsKey(name)) 
      throw new IllegalArgumentException
	("A single valued parameter named (" + name + ") already exists!");

    if(pPresetChoices.containsKey(name)) 
      throw new IllegalArgumentException
	("The single valued parameter (" + name + ") cannot be added because a preset " + 
	 "already exists with the same name!");

    pSingleParams.put(name, param); 
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get all of the single valued parameters. <P> 
   * 
   * The returned ArrayList may be empty if the action does not have any single 
   * valued parameters.
   * 
   * @return 
   *   The set of single valued parameters for this action.  
   */ 
  public Collection<ActionParam>
  getSingleParams()
  {
    return Collections.unmodifiableCollection(pSingleParams.values());
  }  

  /** 
   * Get the single valued parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter or <CODE>null</CODE> if no parameter with the given name exists.
   */ 
  public ActionParam
  getSingleParam
  (
   String name   
  ) 
  {
    if(name == null)
      throw new IllegalArgumentException("The parameter name cannot be (null)!");
    return pSingleParams.get(name);
  }

  /** 
   * Get the value of the single valued parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists.
   */ 
  public Comparable
  getSingleParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    ActionParam param = getSingleParam(name); 
    if(param == null)
      throw new PipelineException
	("Unable to determine the value of the (" + name + ") parameter!");
    return param.getValue();
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the value of a single valued parameter. 
   * 
   * @param name  
   *   The name of the parameter. 
   * 
   * @param value  
   *   The new value of the parameter. 
   */ 
  public void 
  setSingleParamValue
  (
   String name, 
   Comparable value
  ) 
  {
    if(name == null)
      throw new IllegalArgumentException("The parameter name cannot be (null)!");

    ActionParam param = pSingleParams.get(name);
    if(param == null) 
      throw new IllegalArgumentException
	("No parameter named (" + name + ") exists for this action!");

    param.setValue(value);
  }

  /** 
   * Copy the values of all of the single valued parameters from the given action. <P> 
   * 
   * Note that there is no requirement that the given action be the same plugin type or 
   * version.  Any incompatible parameters will simply be ignored by the copy operation.
   * 
   * @param action  
   *   The action to use as the source of single valued parameter values.
   */
  public void 
  setSingleParamValues
  (
   BaseAction action   
  ) 
  {
    for(String name : pSingleParams.keySet()) {
      ActionParam aparam = action.getSingleParam(name);
      if(aparam != null) {
	ActionParam param = pSingleParams.get(name);
	try {
	  param.setValue(aparam.getValue());
	}
	catch(IllegalArgumentException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Warning,
	     ex.getMessage());
	}
      }
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   P E R - S O U R C E   P A R A M E T E R S                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether this action supports per-source parameters.  <P> 
   * 
   * Subclasses MUST override this method to return <CODE>true</CODE> if per-source 
   * paramters are allowed to be added to the action. 
   */ 
  public boolean 
  supportsSourceParams()
  {
    return false;
  }

  /** 
   * Whether this action has per-source parameters for the primary sequence of the 
   * given node.
   * 
   * @param source  
   *   The fully resolved dependency node name.
   */ 
  public boolean 
  hasSourceParams
  (
   String source      
  ) 
  {
    return pSourceParams.containsKey(source);
  }

  /** 
   * Whether this action has per-source parameters for a secondary sequence of the 
   * given node.
   * 
   * @param source  
   *   The fully resolved dependency node name.
   * 
   * @param fpat
   *   The file pattern of the secondary sequence of the upstream node.
   */ 
  public boolean 
  hasSecondarySourceParams
  (
   String source, 
   FilePattern fpat
  ) 
  {
    TreeMap<FilePattern,TreeMap<String,ActionParam>> ftable = pSecondaryParams.get(source);
    return ((ftable != null) && ftable.containsKey(fpat));
  }

  /**
   * Initialize a new set of parameters for the primary sequence of an upstream node.
   * 
   * @param source  
   *   The fully resolved node name of the upstream node.
   */ 
  public void
  initSourceParams
  (
   String source      
  ) 
  {
    if(source == null)
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    TreeMap<String,ActionParam> params = getInitialSourceParams();
    if(params == null)
      throw new IllegalStateException(); 
    
    pSourceParams.put(source, params);
  }

  /**
   * Initialize a new set of parameters for a secondary file sequence of an upstream node.
   * 
   * @param source  
   *   The fully resolved node name of the upstream node.
   * 
   * @param fpat
   *   The file pattern of the secondary sequence of the upstream node.
   */ 
  public void
  initSecondarySourceParams
  (
   String source, 
   FilePattern fpat
  ) 
  {
    if(source == null)
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    if(fpat == null) 
      throw new IllegalArgumentException
	("The secondary sequence file pattern cannot be (null)!");

    TreeMap<String,ActionParam> params = getInitialSourceParams();
    if(params == null)
      throw new IllegalStateException(); 
    
    TreeMap<FilePattern,TreeMap<String,ActionParam>> ftable = pSecondaryParams.get(source);
    if(ftable == null) {
      ftable = new TreeMap<FilePattern,TreeMap<String,ActionParam>>();
      pSecondaryParams.put(source, ftable);
    }
      
    ftable.put(fpat, params);
  }

  /**
   * Get an initial set of action parameters associated with an upstream node. <P> 
   * 
   * Subclasses which support per-source parameters MUST override this method
   * to provide a means for initializing parameters for dependencies.  
   */ 
  public TreeMap<String,ActionParam>
  getInitialSourceParams()
  {
    return null;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get node names of the upstream nodes with primary file sequence per-source parameters.
   */ 
  public Set<String>
  getSourceNames()
  {
    return Collections.unmodifiableSet(pSourceParams.keySet());
  }

  /** 
   * Get node names of the upstream nodes with secondary file sequence per-source parameters.
   */ 
  public Set<String>
  getSecondarySourceNames()
  {
    return Collections.unmodifiableSet(pSecondaryParams.keySet());
  }

  /** 
   * Get the file patterns of all secondary sequences with per-source parameters for the 
   * given node. 
   * 
   * @param source  
   *   The fully resolved node name of the upstream node.
   */ 
  public Set<FilePattern>
  getSecondarySequences
  (
   String source
  )
  {
    TreeMap<FilePattern,TreeMap<String,ActionParam>> ftable = pSecondaryParams.get(source);
    if(ftable != null) 
      return Collections.unmodifiableSet(ftable.keySet());
    return new TreeSet<FilePattern>();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the value of the named parameter for the primary file sequence of an upstream node. 
   *
   * @param source  
   *   The fully resolved node name of the upstream node.
   * 
   * @param name  
   *   The name of the parameter. 
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no parameter with the given name exists for the given upstream node.
   */ 
  public Comparable
  getSourceParamValue
  (
   String source,
   String name  
  ) 
    throws PipelineException	
  {
    ActionParam param = getSourceParam(source, name);
    if(param == null)
      throw new PipelineException
	("Unable to determine the value of the (" + name + ") parameter for the primary " + 
	 "file sequence of the upstream node (" + source + ")!");

    return param.getValue();
  }

  /**
   * Get the named parameter for the primary file sequence of an upstream node.
   *
   * @param source  
   *   The fully resolved node name of the upstream node.
   * 
   * @param name  
   *   The name of the parameter. 
   * 
   * @return 
   *   The action parameter or <CODE>null</CODE> if no parameter with the given name exists
   *   for the given source.
   */ 
  public ActionParam
  getSourceParam
  (
   String source,
   String name  
  )
  {
    if(source == null)
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    TreeMap<String,ActionParam> table = pSourceParams.get(source);
    if(table == null) 
      return null;

    return table.get(name);
  }

  /**
   * Get all of the per-source parameters associated with the primary file sequence of
   * an upstream node.  <P> 
   * 
   * The returned <CODE>Collection</CODE> may be empty if the upstream node does 
   * not have any parameters associated with its primary file sequence.
   * 
   * @param source  
   *   The fully resolved node name of the upstream node.
   * 
   * @return 
   *   The set of parameters for the given upstream node.  
   */ 
  public Collection<ActionParam>
  getSourceParams
  (
   String source  
  ) 
  {    
    if(source == null)
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    TreeMap<String,ActionParam> table = pSourceParams.get(source);
    if(table != null) 
      return Collections.unmodifiableCollection(table.values());
    else 
      return new ArrayList<ActionParam>();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the value of the named parameter for a secondary file sequence of an upstream node. 
   *
   * @param source  
   *   The fully resolved node name of the upstream node.
   * 
   * @param fpat
   *   The secondary sequence file pattern.
   * 
   * @param name  
   *   The name of the parameter. 
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no parameter with the given name exists for the given secondary file sequence of 
   *   the upstream node.
   */ 
  public Comparable
  getSecondarySourceParamValue
  (
   String source,
   FilePattern fpat, 
   String name  
  ) 
    throws PipelineException	
  {
    ActionParam param = getSecondarySourceParam(source, fpat, name);
    if(param == null)
      throw new PipelineException
	("Unable to determine the value of the (" + name + ") parameter for the secondary " + 
	 "file sequence (" + fpat + ") of the upstream node (" + source + ")!");

    return param.getValue();
  }

  /**
   * Get the named parameter for a secondary file sequence of an upstream node.
   *
   * @param source  
   *   The fully resolved node name of the upstream node.
   * 
   * @param fpat
   *   The secondary sequence file pattern.
   * 
   * @param name  
   *   The name of the parameter. 
   * 
   * @return 
   *   The action parameter or <CODE>null</CODE> if no parameter with the given name exists
   *   for given secondary file sequence of the upstream node.
   */ 
  public ActionParam
  getSecondarySourceParam
  (
   String source,
   FilePattern fpat, 
   String name  
  )
  {
    if(source == null)
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    TreeMap<FilePattern,TreeMap<String,ActionParam>> ftable = pSecondaryParams.get(source);
    if(ftable == null) 
      return null;

    TreeMap<String,ActionParam> table = ftable.get(fpat);
    if(table == null) 
      return null;

    return table.get(name);
  }

  /**
   * Get all of the per-source parameters associated with the given secondary file sequence
   * of an upstream node.  <P> 
   * 
   * The returned <CODE>Collection</CODE> may be empty if the upstream node does 
   * not have any parameters associated with the given secondary file sequence.
   * 
   * @param source  
   *   The fully resolved node name of the upstream node.
   * 
   * @param fpat
   *   The secondary sequence file pattern.
   * 
   * @return 
   *   The set of parameters for the given upstream node.  
   */ 
  public Collection<ActionParam>
  getSecondarySourceParams
  (
   String source,
   FilePattern fpat
  ) 
  {    
    if(source == null)
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    if(fpat == null) 
      throw new IllegalArgumentException
	("The secondary sequence file pattern cannot be (null)!");    

    TreeMap<FilePattern,TreeMap<String,ActionParam>> ftable = pSecondaryParams.get(source);
    if(ftable == null) 
      return new ArrayList<ActionParam>();

    TreeMap<String,ActionParam> table = ftable.get(fpat);
    if(table != null) 
      return Collections.unmodifiableCollection(table.values());
    else 
      return new ArrayList<ActionParam>();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the value of a per-source parameter for primary file sequence of an upstream node.
   *
   * @param source  
   *   The fully resolved node name of the upstream node.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param value  
   *   The new value of the parameter. 
   */ 
  public void 
  setSourceParamValue
  (
   String source,
   String name, 
   Comparable value      
  ) 
  {
    if(source == null)
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    if(name == null)
      throw new IllegalArgumentException("The parameter name cannot be (null)!");

    TreeMap<String,ActionParam> table = pSourceParams.get(source);
    if(table == null) 
      throw new IllegalArgumentException
	("The upstream node (" + source + ") does not have primary file sequence " + 
	 "parameters!");

    ActionParam param = table.get(name);
    if(param == null) 
      throw new IllegalArgumentException
	("No parameter named (" + param.getName() + ") exists for primary file sequence " + 
	 "of the upstream node (" + source + ")!");
    
    param.setValue(value);    
  }

  /**
   * Set the value of a per-source parameter for secondary file sequence of an upstream node.
   *
   * @param source  
   *   The fully resolved node name of the upstream node.
   * 
   * @param fpat
   *   The file pattern of the secondary sequence of the upstream node.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param value  
   *   The new value of the parameter. 
   */ 
  public void 
  setSecondarySourceParamValue
  (
   String source,
   FilePattern fpat,
   String name, 
   Comparable value      
  ) 
  {
    if(source == null)
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    if(fpat == null) 
      throw new IllegalArgumentException
	("The secondary sequence file pattern cannot be (null)!");    

    if(name == null)
      throw new IllegalArgumentException("The parameter name cannot be (null)!");

    TreeMap<FilePattern,TreeMap<String,ActionParam>> ftable = pSecondaryParams.get(source);
    if(ftable == null)
      throw new IllegalArgumentException
	("The upstream node (" + source + ") does not have secondary file sequence " + 
	 "parameters!");

    TreeMap<String,ActionParam> table = ftable.get(fpat);
    if(table == null) 
      throw new IllegalArgumentException
	("The upstream node (" + source + ") does not have parameters associated with the " + 
	 "secondary file sequence (" + fpat + ")!");     

    ActionParam param = table.get(name);
    if(param == null) 
      throw new IllegalArgumentException
	("No parameter named (" + param.getName() + ") exists for the secondary file " + 
	 "sequence (" + fpat + ") of the upstream node (" + source + ")!");    

    param.setValue(value);    
  }

  /** 
   * Copy the values of all of the per-source parameters from the given action. <P> 
   * 
   * For each source, if the given action has parameters for the source then this action will
   * reinitialize its per-source parameters for that source.  Any parameters are compatable
   * will then be copied from the given action to this action. <P> 
   * 
   * Note that there is no requirement that the given action be the same plugin type or 
   * version.  Any incompatible parameters will simply be ignored by the copy operation.
   * 
   * @param action  
   *   The action from which to copy per-source parameters.
   */
  public void 
  setSourceParamValues
  (
   BaseAction action   
  ) 
  {
    if(!supportsSourceParams()) 
      return; 

    /* primary file sequence parameters */ 
    for(String source : action.getSourceNames()) {
      removeSourceParams(source);
      initSourceParams(source);
      
      TreeMap<String,ActionParam> params = pSourceParams.get(source);
      if(params != null) {
	for(ActionParam aparam : action.getSourceParams(source)) {
	  ActionParam param = params.get(aparam.getName()); 
	  if(param != null) {
	    try {
	      param.setValue(aparam.getValue());
	    }
	    catch(IllegalArgumentException ex) {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Ops, LogMgr.Level.Warning,
		 ex.getMessage());
	    }
	  }
	}
      }
    }

    /* secondary file sequence parameters */ 
    for(String source : action.getSecondarySourceNames()) {
      for(FilePattern fpat : action.getSecondarySequences(source)) {
	removeSecondarySourceParams(source, fpat);
	initSecondarySourceParams(source, fpat);

	TreeMap<String,ActionParam> params = pSecondaryParams.get(source).get(fpat);
	if(params != null) {
	  for(ActionParam aparam : action.getSecondarySourceParams(source, fpat)) {
	    ActionParam param = params.get(aparam.getName()); 
	    if(param != null) {
	      try {
		param.setValue(aparam.getValue());
	      }
	      catch(IllegalArgumentException ex) {
		LogMgr.getInstance().log
		  (LogMgr.Kind.Ops, LogMgr.Level.Warning,
		   ex.getMessage());
	      }
	    }
	  }
	}
      }      
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Remove all of the per-source parameters associated with the primary file sequence of 
   * the given upstream node.
   * 
   * @param source 
   *   The fully resolved node name of the upstream node.
   */ 
  public void 
  removeSourceParams
  (
   String source        
  ) 
  {
    if(source == null)
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    pSourceParams.remove(source);
  }   

  /**
   * Remove all of the per-source parameters associated with a secondary file sequence of 
   * the given upstream node.
   * 
   * @param source 
   *   The fully resolved node name of the upstream node.
   * 
   * @param fpat
   *   The file pattern of the secondary sequence of the upstream node.
   */ 
  public void 
  removeSecondarySourceParams
  (
   String source,
   FilePattern fpat    
  ) 
  {
    if(source == null)
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    if(fpat == null) 
      throw new IllegalArgumentException
	("The secondary sequence file pattern cannot be (null)!");

    TreeMap<FilePattern,TreeMap<String,ActionParam>> ftable = pSecondaryParams.get(source);
    if(ftable != null) {
      ftable.remove(fpat);
      if(ftable.isEmpty()) 
	pSecondaryParams.remove(source);
    }
  }   

  /**
   * Remove all of the per-source parameters associated with all secondary file sequence of 
   * the given upstream node.
   * 
   * @param source 
   *   The fully resolved node name of the upstream node.
   */ 
  public void 
  removeSecondarySourceParams
  (
   String source
  ) 
  {
    if(source == null)
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    pSecondaryParams.remove(source);
  }   

  /**
   * Remove all per-source parameters from this action. <P> 
   * 
   * Parameters associated with both primary and secondary file sequences of all upstream
   * nodes will be removed.
   */ 
  public void 
  removeAllSourceParams()
  {
    pSourceParams.clear();
    pSecondaryParams.clear();
  }  



  /*----------------------------------------------------------------------------------------*/
  /*   P R E S E T S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add a named set of parameter preset choices to the action. <P> 
   * 
   * This method is used by subclasses in their constructors initialize the presets 
   * for single valued parameters that they support.
   * 
   * @param name
   *   The name of the preset.
   * 
   * @param choices
   *   The names of the possible preset choices.
   */ 
  protected void 
  addPreset
  (
   String name, 
   ArrayList<String> choices
  ) 
  {
    if(pPresetChoices.containsKey(name)) 
      throw new IllegalArgumentException
	("The preset (" + name + ") already exists!");
    
    if(pSingleParams.containsKey(name)) 
      throw new IllegalArgumentException
	("The preset (" + name + ") cannot be added because a single valued parameter " + 
	 "exists with the same name!");
    
    pPresetChoices.put(name, choices);
  }

  /**
   * Add the values of single valued parameters which should be set when a given preset 
   * choice is selected. <P> 
   * 
   * This method is used by subclasses in their constructors to specify the parameter
   * values set by a preset choice after first adding the preset using the 
   * {@link #addPreset addPreset} method.
   * 
   * @param name 
   *   The name of the preset.
   * 
   * @param choice
   *   The name of the preset choice which causes the parameter to be set.
   * 
   * @param values
   *   The value assiged to each singled valued parameter indexed by parameter name.
   */ 
  protected void 
  addPresetValues
  (
   String name, 
   String choice, 
   TreeMap<String,Comparable> values
  ) 
  {
    TreeMap<String,TreeMap<String,Comparable>> choices = pPresetValues.get(name);
    if(choices == null) {
      if(!pPresetChoices.containsKey(name)) 
	throw new IllegalArgumentException
	  ("No preset named (" + name + ") exists!");
      choices = new TreeMap<String,TreeMap<String,Comparable>>();
      pPresetValues.put(name, choices);
    }

    TreeMap<String,Comparable> pvalues = choices.get(choice);
    if(pvalues == null) {
      if(!pPresetChoices.get(name).contains(choice)) 
	throw new IllegalArgumentException
	  ("No choice (" + choice + ") exists for preset (" + name + ")!");
      pvalues = new TreeMap<String,Comparable>();
      choices.put(choice, pvalues);
    }

    pvalues.putAll(values);
  }
    

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the choices for the given preset.
   * 
   * @param name
   *   The name of the preset.
   * 
   * @return 
   *   The names of the choices or <CODE>null</CODE> if no preset exists.
   */ 
  public List<String> 
  getPresetChoices
  (
   String name
  ) 
  {
    ArrayList<String> choices = pPresetChoices.get(name);
    if(choices != null) 
      return Collections.unmodifiableList(choices);
    return null;
  }

  /**
   * Get the preset values of single valued parameters set by a given preset choice.
   * 
   * @param name
   *   The name of the preset.
   * 
   * @param choice
   *   The name of the preset choice which causes the parameter to be set.
   * 
   * @return 
   *   The parameter values indexed by parameter name or <CODE>null</CODE> if no 
   *   preset choice exists.
   */ 
  public SortedMap<String,Comparable> 
  getPresetValues
  (
   String name, 
   String choice
  ) 
  {
    TreeMap<String,TreeMap<String,Comparable>> choices = pPresetValues.get(name);
    if(choices != null) {
      TreeMap<String,Comparable> values = choices.get(choice);
      if(values != null) 
	return Collections.unmodifiableSortedMap(values);
    }
    return null;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   P A R A M E T E R    L A Y O U T                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sets the hierarchical grouping of parameters and presets which determine the layout of 
   * UI components. <P> 
   * 
   * The given layouts must contain an entry for all single valued parameters and presets
   * defined for the action exactly once.  A collapsible drawer component will be created 
   * for each layout group which contains a field for each parameter or preset entry in the 
   * order specified by the group.  All <CODE>null</CODE> entries will cause additional space 
   * to be added between the UI fields. Each layout subgroup will be represented by its own 
   * drawer nested within the drawer for the parent layout group. <P> 
   * 
   * This method should be called by subclasses in their constructor after intializing all 
   * single valued parameters with the {@link #addSingleParam addSingleParam} method and 
   * adding any preset choices with {@link #addPreset addPreset} method.
   * 
   * @param group
   *   The layout group.
   */
  protected void
  setSingleLayout
  (
   LayoutGroup group
  ) 
  {
    TreeSet<String> names = new TreeSet<String>();
    collectLayoutNames(group, names);
    
    for(String name : names) {
      if(!pSingleParams.containsKey(name) && !pPresetChoices.containsKey(name))
	throw new IllegalArgumentException
	  ("The entry (" + name + ") specified by the parameter layout group " + 
	   "(" + group.getName() + ") does not match any single valued parameter or " + 
	   "preset defined for this Action!");
    }

    for(String name : pSingleParams.keySet()) {
      if(!names.contains(name))
	throw new IllegalArgumentException
	  ("The single valued parameter (" + name + ") defined by this Action was not " + 
	   "specified by any the parameter layout group!");
    }

    for(String name : pPresetChoices.keySet()) {
      if(!names.contains(name))
	throw new IllegalArgumentException
	  ("The parameter preset (" + name + ") defined by this Action was not " + 
	   "specified by any the parameter layout group!");
    }

    pSingleLayout = new LayoutGroup("ActionParameters", 
				    "The single value Action plugin parameters.", group);
  }

  /**
   * Recursively search the parameter groups to collect the parameter names and verify
   * that no parameter is specified more than once.
   */ 
  private void 
  collectLayoutNames
  (
   LayoutGroup group, 
   TreeSet<String> names
  ) 
  {
    for(String name : group.getEntries()) {
      if(name != null) {
	if(names.contains(name)) 
	  throw new IllegalArgumentException
	    ("The single valued parameter (" + name + ") was specified more than once " +
	     "in the given parameter group!");
	names.add(name);
      }
    }
      
    for(LayoutGroup sgroup : group.getSubGroups()) 
      collectLayoutNames(sgroup, names);
  }

  /**
   * Get the grouping of single valued parameters used to layout components which represent 
   * the parameters in the user interface. <P> 
   * 
   * If no single valued parameter group has been previously specified, a group will 
   * be created which contains all single valued parameters in alphabetical order.
   */ 
  public LayoutGroup
  getSingleLayout()
  {
    if(pSingleLayout == null) {
      pSingleLayout = new LayoutGroup("ActionParameters", 
				      "The single value Action plugin parameters.", true);
      for(String name : pSingleParams.keySet()) 
	pSingleLayout.addEntry(name);
    }
    
    return pSingleLayout; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Specifies the ordering of per-source parameters in the user interface. <P> 
   * 
   * The specified list of parameters names must contains all per-source parameters defined 
   * for the action exactly once.  A table will be constructed for the per-source parameters
   * with a row for each source node and a column for each per-source parameter.  The order
   * of the parameters within the list will determine the ordering of the columns from 
   * left to right.  Parameter entries may not be <CODE>null</CODE> for per-source 
   * parameters. <P> 
   * 
   * This method should be called by subclasses in their constructor if they have overriden
   * the {@link #supportsSourceParams supportsSourceParams} to return <CODE>true</CODE> and
   * have implemented the {@link #getInitialSourceParams getInitialSourceParams} method. 
   * 
   * @param pnames
   *   The ordered per-source parameter names. 
   */
  protected void
  setSourceLayout
  (
   List<String> pnames
  ) 
  {
    if(!supportsSourceParams()) 
      throw new IllegalArgumentException
	("This action does not support per-source parameters!");

    TreeMap<String,ActionParam> params = getInitialSourceParams();

    for(String name : pnames) {
      if(!params.containsKey(name))
	throw new IllegalArgumentException
	  ("The per-source parameter (" + name + ") specified by the parameter group " + 
	   "was not defined for this Action!");
    }

    for(String name : params.keySet()) {
      if(!pnames.contains(name))
	throw new IllegalArgumentException
	  ("The per-source parameter (" + name + ") defined by this Action was not " + 
	   "specified by the parameter group!");
    }

    pSourceLayout = new ArrayList<String>(pnames);
  }

  /**
   * Get the grouping of per-source parameters used to layout components which represent 
   * the parameters in the user interface. <P>
   * 
   * If no per-source parameter group has been previously specified, a group will 
   * be created which contains all per-source parameters in alphabetical order.
   */ 
  public List<String>
  getSourceLayout()
  {
    if(pSourceLayout == null) {
      if(supportsSourceParams()) 
	pSourceLayout = new ArrayList<String>(getInitialSourceParams().keySet());
      else 
	pSourceLayout = new ArrayList<String>();
    }

    return Collections.unmodifiableList(pSourceLayout);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a {@link SubProcessHeavy} instance which when executed will fulfill the 
   * given action agenda. <P> 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   * 
   * @return 
   *   The SubProcess which will fulfill the agenda.
   * 
   * @throws PipelineException 
   *   If unable to prepare a SubProcess due to illegal, missing or imcompatable 
   *   information in the action agenda or a general failure of the prep method code.
   */
  public SubProcessHeavy
  prep
  (
   ActionAgenda agenda,
   File outFile, 
   File errFile 
  )
    throws PipelineException
  {
    throw new PipelineException
      ("The prep() method was not implemented by the Action (" + pName + ")!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   M I S C   F I L E   U T I L S                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract working area file system path to the first file in the given 
   * node file sequence. 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param name
   *   The unique node name.
   * 
   * @param fseq
   *   The file sequence associated with the node. 
   */
  public static Path
  getWorkingNodeFilePath
  (
   ActionAgenda agenda, 
   String name, 
   FileSeq fseq
  ) 
  {
    return getWorkingNodeFilePath(agenda, name, fseq.getPath(0)); 
  }

  /**
   * Get the abstract working area file system path to the given node file.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param name
   *   The unique node name.
   * 
   * @param file
   *   The name of the file relative to the directory containing the node.
   */
  public static Path
  getWorkingNodeFilePath
  (
   ActionAgenda agenda, 
   String name, 
   Path file
  ) 
  {
    return getWorkingNodeFilePath(new NodeID(agenda.getNodeID(), name), file); 
  }

  /**
   * Get the abstract working area file system path to the given node file.
   * 
   * @param nodeID
   *   The unique working version identifier. 
   * 
   * @param file
   *   The name of the file relative to the directory containing the node.
   */
  public static Path
  getWorkingNodeFilePath
  (
   NodeID nodeID,
   Path file
  ) 
  {
    return getWorkingNodeFilePath(nodeID, file.toString());
  }

  /**
   * Get the abstract working area file system path to the given node file.
   * 
   * @param nodeID
   *   The unique working version identifier. 
   * 
   * @param file
   *   The name of the file relative to the directory containing the node.
   */
  public static Path
  getWorkingNodeFilePath
  (
   NodeID nodeID,
   String file
  ) 
  {
    return new Path(PackageInfo.sProdPath, nodeID.getWorkingParent() + "/" + file);     
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Change file access permissions. <P> 
   * 
   * See the manpage for chmod(2) for details about the legal values for <CODE>mode</CODE>.
   *
   * @param mode 
   *   The access mode bitmask.
   *
   * @param file 
   *   The fully resolved path to the file to change.
   * 
   * @throws IOException 
   *   If unable to change the mode of the given file.
   */
  public static void 
  chmod
  (
   int mode, 
   File file
  ) 
    throws IOException
  {
    NativeFileSys.chmod(mode, file);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the given file to the set of files which will be removed upon termination of the
   * Java runtime.
   * 
   * @param file 
   *   The temporary file to cleanup.
   */
  protected void 
  cleanupLater
  (
   File file
  ) 
  {
    FileCleaner.add(file);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract pathname of the root directory used to store temporary files 
   * created by the job. <P> 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   */
  public static Path
  getTempPath
  (
   ActionAgenda agenda
  )
  {
    return (new Path(PackageInfo.sTempPath, 
		     "pljobmgr/" + agenda.getJobID() + "/scratch"));
  }

  /**
   * Get the root directory used to store temporary files created by the job. <P> 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   */
  public static File
  getTempDir
  (
   ActionAgenda agenda
  )
  {
    return getTempPath(agenda).toFile();
  }

  /** 
   * Create a unique temporary file for the job with the given suffix.<P> 
   * 
   * If successful, the temporary file will be added to the set of files which will be 
   * removed upon termination of the Java runtime (see @{link #cleanupLater cleanupLater}).
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param suffix
   *   The filename suffix of the temporary file.
   * 
   * @return 
   *   The temporary file.
   * 
   * @throws IOException 
   *   If unable to create the temporary file.
   */ 
  public File
  createTemp
  (
   ActionAgenda agenda, 
   String suffix
  ) 
    throws PipelineException 
  {
    File tmp = null;
    try {
      tmp = File.createTempFile(pName + "_" + agenda.getJobID(), "." + suffix, 
				getTempDir(agenda));
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to create temporary file for Job (" + agenda.getJobID() + "):\n\n" + 
	 ex.getMessage());
    }

    cleanupLater(tmp);

    return tmp;
  }

  /** 
   * Create a unique temporary file for the job with the given suffix and access 
   * permissions. <P> 
   * 
   * If successful, the temporary file will be added to the set of files which will be 
   * removed upon termination of the Java runtime (see @{link #cleanupLater 
   * cleanupLater}).<P> 
   * 
   * This method is not supported by the Windows operating system since it relies on the
   * {@link NativeFileSys#chmod NativeFileSys.chmod} method.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param mode 
   *   The access mode bitmask.
   * 
   * @param suffix
   *   The filename suffix of the temporary file.
   * 
   * @return 
   *   The temporary file.
   * 
   * @throws IOException 
   *   If unable to create the temporary file.
   */ 
  public File
  createTemp
  (
   ActionAgenda agenda, 
   int mode, 
   String suffix
  ) 
    throws PipelineException 
  {
    File tmp = null;
    try {
      tmp = File.createTempFile(pName + "_" + agenda.getJobID(), "." + suffix, 
				getTempDir(agenda));
      chmod(mode, tmp);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to create temporary file for Job (" + agenda.getJobID() + "):\n\n" + 
	 ex.getMessage());
    }

    cleanupLater(tmp);

    return tmp;
  }

  /**
   * Create a unique temporary script file appropriate for the current operating 
   * system type.<P> 
   * 
   * On Unix/MacOS this will be an bash(1) script, while on Windows the script will
   * be a BAT file suitable for evaluation by "Cmd".  The returned script file is suitable
   * for use as with the {@link #createScriptSubProcess createScriptSubProcess} method.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   */ 
  public File
  createTempScript
  (
   ActionAgenda agenda
  ) 
    throws PipelineException 
  {
    if(PackageInfo.sOsType == OsType.Windows)
      return createTemp(agenda, "bat");
    else 
      return createTemp(agenda, 0644, "bash");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P A R A M E T E R   C R E A T I O N                                                  */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Adds an additional command-line options parameter to the action.<P> 
   * 
   * The following single valued parameters is added: <BR>
   * 
   * <DIV style="margin-left: 40px;">
   *   Extra Options <BR>
   *   <DIV style="margin-left: 40px;">
   *     Additional command-line arguments. <BR> 
   *   </DIV> <BR>
   * </DIV> <P> 
   * 
   * This method should be called in the subclass constructor before specifying parameter
   * layouts.
   */
  protected void 
  addExtraOptionsParam() 
  {
    ActionParam param = 
      new StringActionParam
      (aExtraOptions,
       "Additional command-line arguments.", 
       null);
    addSingleParam(param);
  }

  /**
   * Add the parameter created by the {@link #addExtraOptionsParam addExtraOptionsParam} 
   * method to the given parameter layout group.
   */ 
  protected void 
  addExtraOptionsParamToLayout
  (
   LayoutGroup layout
  ) 
  {
    layout.addEntry(aExtraOptions);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   P A R A M E T E R   L O O K U P                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the selected index of the single valued Enum parameter with the given name.<P> 
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists.
   */ 
  public int
  getSingleEnumParamIndex
  (
   String name   
  ) 
    throws PipelineException
  {
    EnumActionParam param = (EnumActionParam) getSingleParam(name);
    if(param == null) 
      throw new PipelineException
        ("The required parameter (" + name + ") was not illegal!"); 
      
    return param.getIndex();
  }

  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null Boolean parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists.
   */ 
  public boolean
  getSingleBooleanParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    Boolean value = (Boolean) getSingleParamValue(name);  
    if(value == null) 
      throw new PipelineException
        ("The required parameter (" + name + ") was not set!"); 

    return value;
  }  

  /** 
   * Get the value of the single valued Boolean parameter with the given name.<P> 
   * 
   * If <CODE>null</CODE> value is treated as <CODE>false</CODE>.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists.
   */ 
  public boolean
  getSingleOptionalBooleanParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    Boolean value = (Boolean) getSingleParamValue(name); 
    return ((value != null) && value);
  }  


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null Long parameter with the given name.<P> 
   * 
   * This method can be used to retrieve ByteSizeActionParam values.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists or 
   *   the value is <CODE>null</CODE>.
   */ 
  public long
  getSingleLongParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    return getSingleLongParamValue(name, null, null);
  }

  /** 
   * Get the lower bounds checked value of the single valued non-null Long parameter with 
   * the given name. <P> 
   * 
   * Legal values must satisfy: (minValue <= value) <P> 
   * 
   * This method can be used to retrieve ByteSizeActionParam values.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param minValue
   *   The minimum (inclusive) legal value or <CODE>null</CODE> for no lower bounds.
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public long
  getSingleLongParamValue
  (
   String name, 
   Long minValue 
  ) 
    throws PipelineException
  {
    return getSingleLongParamValue(name, minValue, null);
  }

  /** 
   * Get the bounds checked value of the single valued non-null Long parameter with 
   * the given name. <P> 
   * 
   * Legal values must satisfy: (minValue <= value <= maxValue)<P> 
   * 
   * This method can be used to retrieve ByteSizeActionParam values.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param minValue
   *   The minimum (inclusive) legal value or <CODE>null</CODE> for no lower bounds.
   * 
   * @param maxValue
   *   The maximum (inclusive) legal value or <CODE>null</CODE> for no upper bounds.
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public long
  getSingleLongParamValue
  (
   String name, 
   Long minValue, 
   Long maxValue
  ) 
    throws PipelineException
  {
    Long value = (Long) getSingleParamValue(name); 
    if(value == null) 
      throw new PipelineException
        ("The required parameter (" + name + ") was not set!"); 

    if((minValue != null) && (value < minValue)) 
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + name + ") was less-than the " + 
         "minimum allowed value (" + minValue + ")!");
    
    if((maxValue != null) && (value > maxValue)) 
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + name + ") was greater-than the " + 
         "maximum allowed value (" + maxValue + ")!");

    return value;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null Integer parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists or 
   *   the value is <CODE>null</CODE>.
   */ 
  public int
  getSingleIntegerParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    return getSingleIntegerParamValue(name, null, null);
  }

  /** 
   * Get the lower bounds checked value of the single valued non-null Integer parameter with 
   * the given name. <P> 
   * 
   * Legal values must satisfy: (minValue <= value) 
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param minValue
   *   The minimum (inclusive) legal value or <CODE>null</CODE> for no lower bounds.
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public int
  getSingleIntegerParamValue
  (
   String name, 
   Integer minValue 
  ) 
    throws PipelineException
  {
    return getSingleIntegerParamValue(name, minValue, null);
  }

  /** 
   * Get the bounds checked value of the single valued non-null Integer parameter with 
   * the given name. <P> 
   * 
   * Legal values must satisfy: (minValue <= value <= maxValue)
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param minValue
   *   The minimum (inclusive) legal value or <CODE>null</CODE> for no lower bounds.
   * 
   * @param maxValue
   *   The maximum (inclusive) legal value or <CODE>null</CODE> for no upper bounds.
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public int
  getSingleIntegerParamValue
  (
   String name, 
   Integer minValue, 
   Integer maxValue
  ) 
    throws PipelineException
  {
    Integer value = (Integer) getSingleParamValue(name); 
    if(value == null) 
      throw new PipelineException
        ("The required parameter (" + name + ") was not set!"); 

    if((minValue != null) && (value < minValue)) 
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + name + ") was less-than the " + 
         "minimum allowed value (" + minValue + ")!");
    
    if((maxValue != null) && (value > maxValue)) 
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + name + ") was greater-than the " + 
         "maximum allowed value (" + maxValue + ")!");

    return value;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null Double parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists or 
   *   the value is <CODE>null</CODE>.
   */ 
  public double
  getSingleDoubleParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    return getSingleDoubleParamValue(name, null, null);
  }

  /** 
   * Get the lower bounds checked value of the single valued non-null Double parameter with 
   * the given name. <P> 
   * 
   * Legal values must satisfy: (lower < value)
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param lower
   *   The lower bounds (exclusive) of legal values or <CODE>null</CODE> for no lower bounds.
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists, 
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public double
  getSingleDoubleParamValue
  (
   String name, 
   Double lower
  ) 
    throws PipelineException
  {
    return getSingleDoubleParamValue(name, lower, null);
  }

  /** 
   * Get the bounds checked value of the single valued non-null Double parameter with 
   * the given name. <P> 
   * 
   * Legal values must satisfy: (lower < value < upper)
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param lower
   *   The lower bounds (exclusive) of legal values or <CODE>null</CODE> for no lower bounds.
   * 
   * @param upper
   *   The upper bounds (exclusive) of legal values or <CODE>null</CODE> for no upper bounds.
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists, 
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public double
  getSingleDoubleParamValue
  (
   String name, 
   Double lower, 
   Double upper
  ) 
    throws PipelineException
  {
    Double value = (Double) getSingleParamValue(name); 
    if(value == null) 
      throw new PipelineException
        ("The required parameter (" + name + ") was not set!"); 
    
    if((lower != null) && (value <= lower)) 
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + name + ") was not greater-than the " + 
         "the lower bounds (" + lower + ") for legal values!");
    
    if((upper != null) && (value >= upper)) 
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + name + ") was not less-than the " + 
         "the upper bounds (" + upper + ") for legal values!");

    return value;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued String parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value or 
   *   <CODE>null</CODE> if the value is null or the empty string. 
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists.
   */ 
  public String
  getSingleStringParamValue
  (
   String name   
  ) 
    throws PipelineException
  { 
    String value = (String) getSingleParamValue(name); 
    if((value != null) && (value.length() > 0))
      return value;

    return null;    
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   P A T H   G E N E R A T I O N                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract path to the single primary file associated with a target node. 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param desc
   *   A brief description of the type of file expected (used to generate error messages).
   * 
   * @return 
   *   The path to the target file. 
   */ 
  public Path
  getSinglePrimaryTargetPath
  (
   ActionAgenda agenda, 
   String desc
  ) 
    throws PipelineException 
  {
    return getSinglePrimaryTargetPath(agenda, new ArrayList<String>(), desc);
  }

  /**
   * Get the abstract path to the single primary file associated with a target node. 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param suffix
   *   The allowable filename suffix.
   * 
   * @param desc
   *   A brief description of the type of file expected (used to generate error messages).
   * 
   * @return 
   *   The path to the target file. 
   */ 
  public Path
  getSinglePrimaryTargetPath
  (
   ActionAgenda agenda, 
   String suffix, 
   String desc
  ) 
    throws PipelineException 
  {
    ArrayList<String> suffixes = new ArrayList<String>();
    suffixes.add(suffix);
    
    return getSinglePrimaryTargetPath(agenda, suffixes, desc);
  }

  /**
   * Get the abstract path to the single primary file associated with a target node. 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param suffixes
   *   The allowable filename suffixes.
   * 
   * @param desc
   *   A brief description of the type of file expected (used to generate error messages).
   * 
   * @return 
   *   The path to the target file. 
   */ 
  public Path
  getSinglePrimaryTargetPath
  (
   ActionAgenda agenda, 
   Collection<String> suffixes, 
   String desc
  ) 
    throws PipelineException 
  {
    FileSeq fseq = agenda.getPrimaryTarget();
    String suffix = fseq.getFilePattern().getSuffix();
    if(!fseq.isSingle() || 
       (!suffixes.isEmpty() && ((suffix == null) || !suffixes.contains(suffix)))) {
      throw new PipelineException
        ("The " + getName() + " Action requires that the primary target file sequence " + 
         "must be a single " + desc + "!");
    }

    return new Path(agenda.getTargetPath(), fseq.getPath(0));
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract path to the single primary file associated with a source node 
   * specified by the given parameter.
   * 
   * @param pname
   *   The name of the single valued parameter which names the source node.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param desc
   *   A brief description of the type of file expected (used to generate error messages).
   * 
   * @return 
   *   The path to the primary file of the source node or 
   *   <CODE>null</CODE> if none was specified.
   */ 
  public Path
  getSinglePrimarySourcePath
  (
   String pname, 
   ActionAgenda agenda, 
   String desc
  ) 
    throws PipelineException 
  {
    return getSinglePrimarySourcePath(pname, agenda, new ArrayList<String>(), desc);
  }

  /**
   * Get the abstract path to the single primary file associated with a source node 
   * specified by the given parameter.
   * 
   * @param pname
   *   The name of the single valued parameter which names the source node.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param suffix
   *   The allowable filename suffix.
   * 
   * @param desc
   *   A brief description of the type of file expected (used to generate error messages).
   * 
   * @return 
   *   The path to the primary file of the source node or 
   *   <CODE>null</CODE> if none was specified.
   */ 
  public Path
  getSinglePrimarySourcePath
  (
   String pname, 
   ActionAgenda agenda, 
   String suffix, 
   String desc
  ) 
    throws PipelineException 
  {
    ArrayList<String> suffixes = new ArrayList<String>();
    suffixes.add(suffix);

    return getSinglePrimarySourcePath(pname, agenda, suffixes, desc);
  }

  /**
   * Get the abstract path to the single primary file associated with a source node 
   * specified by the given parameter.
   * 
   * @param pname
   *   The name of the single valued parameter which names the source node.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param suffixes
   *   The allowable filename suffixes.
   * 
   * @param desc
   *   A brief description of the type of file expected (used to generate error messages).
   * 
   * @return 
   *   The path to the primary file of the source node or 
   *   <CODE>null</CODE> if none was specified.
   */ 
  public Path
  getSinglePrimarySourcePath
  (
   String pname, 
   ActionAgenda agenda, 
   Collection<String> suffixes, 
   String desc
  ) 
    throws PipelineException 
  {
    Path path = null; 

    ActionParam param = getSingleParam(pname);
    String title = param.getNameUI();

    String mname = (String) param.getValue();
    if(mname != null) {
      FileSeq fseq = agenda.getPrimarySource(mname);
      if(fseq == null) 
	throw new PipelineException
	  ("Somehow the " + title + " node (" + mname + ") was not one of the " + 
	   "source nodes!");
      
      String suffix = fseq.getFilePattern().getSuffix();
      if(!fseq.isSingle() || 
         (!suffixes.isEmpty() && ((suffix == null) || !suffixes.contains(suffix)))) {
	throw new PipelineException
	  ("The " + getName() + " Action requires that the source node specified by the " + 
	   title + " parameter (" + mname + ") must have a single " + desc + " as " + 
           "its primary file sequence!");
      }
      
      path = getWorkingNodeFilePath(agenda, mname, fseq); 
    }

    return path;	      
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get any additional command-line arguments specified using the action parameter created 
   * by the {@link #addExtraOptionsParam addExtraOptionsParam} method.
   */ 
  public ArrayList<String>
  getExtraOptionsArgs() 
    throws PipelineException
  {
    ArrayList<String> args = new ArrayList<String>();

    String extra = (String) getSingleParamValue(aExtraOptions);
    if(extra != null) {
      String parts[] = extra.split("\\p{Space}");
      int wk;
      for(wk=0; wk<parts.length; wk++) {
        if(parts[wk].length() > 0) 
          args.add(parts[wk]);
      }
    }

    return args;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S U B P R O C E S S   C R E A T I O N                                                */
  /*----------------------------------------------------------------------------------------*/
   
  /** 
   * A convienence method for creating the {@link SubProcessHeavy} instance to be returned
   * by the {@link #prep prep} method in a mostly OS-independent manner.<P> 
   * 
   * The caller is reponsible for handling any differences in program name and arguments 
   * between the different operating system types, but this method will handle specifying
   * the process owner, title, environment and working directory. <P> 
   * 
   * When run on a Unix or MacOS system, the working directory is the working area directory 
   * containing the target node.  On Windows, the working directory is always the local 
   * temporary directory since many Windows programs fail if the working directory in on a 
   * network share.  The caller is responsible for making any target file paths relative
   * to the working directory on Unix/MacOS and absolute on Windows.  The correct path for 
   * target files can be obtained using the {@link ActionAgenda#getTargetPath 
   * ActionAgenda.getTargetPath} method.
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   * 
   * @param program  
   *   The name of program to execute as an OS level subprocess.  
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   */
  public SubProcessHeavy
  createSubProcess
  (
   ActionAgenda agenda,
   String program, 
   File outFile, 
   File errFile    
  )  
    throws PipelineException
  {
    return createSubProcess(agenda, program, null, null, outFile, errFile);
  }

  /** 
   * A convienence method for creating the {@link SubProcessHeavy} instance to be returned
   * by the {@link #prep prep} method in a mostly OS-independent manner.<P> 
   * 
   * The caller is reponsible for handling any differences in program name and arguments 
   * between the different operating system types, but this method will handle specifying
   * the process owner, title, environment and working directory. <P> 
   * 
   * When run on a Unix or MacOS system, the working directory is the working area directory 
   * containing the target node.  On Windows, the working directory is always the local 
   * temporary directory since many Windows programs fail if the working directory in on a 
   * network share.  The caller is responsible for making any target file paths relative
   * to the working directory on Unix/MacOS and absolute on Windows.  The correct path for 
   * target files can be obtained using the {@link ActionAgenda#getTargetPath 
   * ActionAgenda.getTargetPath} method.
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   * 
   * @param program  
   *   The name of program to execute as an OS level subprocess.  
   * 
   * @param args  
   *   The command line arguments of the program to execute or 
   *   <CODE>null</CODE> for an empty argument list.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   */
  public SubProcessHeavy
  createSubProcess
  (
   ActionAgenda agenda,
   String program, 
   ArrayList<String> args,
   File outFile, 
   File errFile    
  )  
    throws PipelineException
  {
    return createSubProcess(agenda, program, args, null, outFile, errFile);
  }

  /** 
   * A convienence method for creating the {@link SubProcessHeavy} instance to be returned
   * by the {@link #prep prep} method in a mostly OS-independent manner.<P> 
   * 
   * The caller is reponsible for handling any differences in program name and arguments 
   * between the different operating system types, but this method will handle specifying
   * the process owner, title, environment and working directory.  The default Toolset 
   * generated environment can be overridden if specified using a non-null <CODE>env</CODE> 
   * parameter.<P> 
   * 
   * When run on a Unix or MacOS system, the working directory is the working area directory 
   * containing the target node.  On Windows, the working directory is always the local 
   * temporary directory since many Windows programs fail if the working directory in on a 
   * network share.  The caller is responsible for making any target file paths relative
   * to the working directory on Unix/MacOS and absolute on Windows.  The correct path for 
   * target files can be obtained using the {@link ActionAgenda#getTargetPath 
   * ActionAgenda.getTargetPath} method.
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   * 
   * @param program  
   *   The name of program to execute as an OS level subprocess.  
   * 
   * @param args  
   *   The command line arguments of the program to execute or 
   *   <CODE>null</CODE> for an empty argument list.
   * 
   * @param env  
   *   The environment under which the OS level process is run or 
   *   <CODE>null</CODE> to use the environment defined by the ActionAgenda.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   */
  public SubProcessHeavy
  createSubProcess
  (
   ActionAgenda agenda,
   String program, 
   ArrayList<String> args,
   Map<String,String> env,  
   File outFile, 
   File errFile    
  )  
    throws PipelineException
  {
    try {
      SubProcessHeavy proc = null;
      String owner = agenda.getSubProcessOwner();
      String title = getName() + "-" + agenda.getJobID(); 

      ArrayList<String> nargs = args;
      if(nargs == null) 
        nargs = new ArrayList<String>();

      Map<String,String> nenv = env;
      if(nenv == null) 
        nenv = agenda.getEnvironment();

      switch(PackageInfo.sOsType) {
      case Unix:
      case MacOS:
        proc = new SubProcessHeavy(owner, title, program, nargs, 
                                   nenv, agenda.getTargetPath().toFile(), 
                                   outFile, errFile);
        break;

      case Windows:
        proc = new SubProcessHeavy(owner, title, program, nargs, 
                                   nenv, PackageInfo.sTempPath.toFile(), 
                                   outFile, errFile);
      } 
      
      return proc;
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
    }
  }
  
  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * A convienence method for creating the {@link SubProcessHeavy} instance to be returned
   * by the {@link #prep prep} method when generating a temporary script to execute some 
   * OS-specific commands. <P>
   * 
   * If running on Unix or MacOS, the supplied script must be a bash(1) shell script while on
   * Windows the script must be an executable BAT/CMD file. The Action is reposible for 
   * making sure that the contents of these scripts are portable.  This method simply takes 
   * care of the common wrapper code needed to instantiate a {@link SubProcessHeavy} to run 
   * the script.
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   * 
   * @param script
   *   The temporary script file to execute.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   */ 
  public SubProcessHeavy
  createScriptSubProcess
  (
   ActionAgenda agenda,
   File script, 
   File outFile, 
   File errFile    
  ) 
    throws PipelineException
  {
    String program = null;
    ArrayList<String> args = new ArrayList<String>();

    switch(PackageInfo.sOsType) {
    case Unix:
    case MacOS:
      program = "bash";
      args.add(script.getPath());
      break;

    case Windows:
      program = script.getPath(); 
    }

    return createSubProcess(agenda, program, args, outFile, errFile);
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * A convienence method for creating the {@link SubProcessHeavy} instance to be returned
   * by the {@link #prep prep} method when simply copying a single temporary file created in 
   * the <CODE>prep</CODE> method to the target location.
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   * 
   * @param temp
   *   The temporary file to copy.
   * 
   * @param target
   *   The abtract path to the location of the target file.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   */ 
  public SubProcessHeavy
  createTempCopySubProcess
  (
   ActionAgenda agenda,
   File temp, 
   Path target, 
   File outFile, 
   File errFile    
  ) 
    throws PipelineException
  {
    String program = null;
    ArrayList<String> args = new ArrayList<String>();

    if(PackageInfo.sOsType == OsType.Windows) {
      program = "cmd.exe";
      
      args.add("/c");
      args.add("\"copy /y " + temp.getPath() + " " + target.toOsString() + "\"");
    }
    else {
      program = "cp";
      
      args.add(temp.getPath());
      args.add(target.toOsString());
    } 
    
    return createSubProcess(agenda, program, args, outFile, errFile);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P Y T H O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate the name of the Python interpreter to use based on the Toolset environment and 
   * current operating system type.<P> 
   * 
   * If the environmental variable PYTHON_BINARY is defined, its value will be used as the 
   * name of the python executable instead of the "python".  On Windows, this program name
   * should include the ".exe" extension.
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   */ 
  public static String
  getPythonProgram
  (
   ActionAgenda agenda   
  ) 
    throws PipelineException
  {
    return getPythonProgram(agenda.getEnvironment()); 
  }

  /**
   * Generate the name of the Python interpreter to use based on the Toolset environment and 
   * current operating system type.<P> 
   * 
   * If the environmental variable PYTHON_BINARY is defined, its value will be used as the 
   * name of the python executable instead of the "python".  On Windows, this program name
   * should include the ".exe" extension.
   * 
   * @param env  
   *   The environment used to lookup PYTHON_BINARY.
   */
  public static String
  getPythonProgram
  (
    Map<String,String> env
  ) 
    throws PipelineException
  {
    String python = env.get("PYTHON_BINARY");
    if((python != null) && (python.length() > 0)) 
      return python; 
    
    if(PackageInfo.sOsType == OsType.Windows) 
      return "python.exe";

    return "python";
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate a "launch" Python function declaration.<P> 
   * 
   * This launch function uses Python's os.spawn function to start an OS subprocess and 
   * wait for it complete.  If the subprocess returns an non-zero exit code, the launch
   * function calls sys.exit with an appropriate error message.<P> 
   * 
   * This method is provided as a convienence for writing dynamically generated Python 
   * scripts in a subclasses {@link #prep prep} method which run multiple subprocesses. 
   * By using "launch", you get standardized progress messages and error handling for 
   * free. <P> 
   * 
   * The usage is: <P> 
   * <CODE>
   *   launch(<I>program</I>, <I>args</I>)
   * </CODE><P> 
   * 
   * Where <I>program</I> is the executable name and <I>args</I> is the list of command
   * line arguments.  The program will be found using PATH from the Toolset environment
   * used to launch the Python interpretor.
   */ 
  public static String 
  getPythonLaunchHeader() 
  {
    return sPythonLaunchHeader;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * A convienence method for creating the {@link SubProcessHeavy} instance to be returned
   * by the {@link #prep prep} method which executes a temporary Python script.
   * 
   * This method will handle specifying the Python binary in an Toolset controlled and OS 
   * specific manner (see {@link #getPythonProgram getPythonProgram}).  This method also
   * properly specifies the process owner, title, environment and working directory for the 
   * Python process. 
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   * 
   * @param script
   *   The temporary Python script file to execute.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   */
  public SubProcessHeavy
  createPythonSubProcess
  (
   ActionAgenda agenda,
   File script, 
   File outFile, 
   File errFile    
  )  
    throws PipelineException
  {
    return createPythonSubProcess(agenda, script, null, null, outFile, errFile);
  }
  
  /** 
   * A convienence method for creating the {@link SubProcessHeavy} instance to be returned
   * by the {@link #prep prep} method which executes a temporary Python script.
   * 
   * This method will handle specifying the Python binary in an Toolset controlled and OS 
   * specific manner (see {@link #getPythonProgram getPythonProgram}).  This method also
   * properly specifies the process owner, title, environment and working directory for the 
   * Python process.  Additional command-line arguments for Python can be specified using a
   * non-null <CODE>args</CODE> parameter. The default Toolset generated environment can be 
   * overridden if specified using a non-null <CODE>env</CODE> parameter.<P> 
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   * 
   * @param script
   *   The temporary Python script file to execute.
   * 
   * @param args  
   *   Additional Python command line arguments to specify before the script or 
   *   <CODE>null</CODE> for no additional arguments.
   * 
   * @param env  
   *   The environment under which the OS level process is run or 
   *   <CODE>null</CODE> to use the environment defined by the ActionAgenda.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   */
  public SubProcessHeavy
  createPythonSubProcess
  (
   ActionAgenda agenda,
   File script, 
   ArrayList<String> args,
   Map<String,String> env,  
   File outFile, 
   File errFile    
  )  
    throws PipelineException
  {
    try {
      String owner = agenda.getSubProcessOwner();
      String title = getName() + "-" + agenda.getJobID(); 

      ArrayList<String> nargs = new ArrayList<String>();
      if(args != null) 
        nargs.addAll(args); 
      nargs.add(script.getPath());

      Map<String,String> nenv = env;
      if(nenv == null) 
        nenv = agenda.getEnvironment();

      return new SubProcessHeavy(owner, title, getPythonProgram(nenv), nargs, 
                                 nenv, agenda.getTargetPath().toFile(), 
                                 outFile, errFile);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj
  )
  {
    if((obj != null) && (obj instanceof BaseAction)) {
      BaseAction action = (BaseAction) obj;
      if(super.equals(obj) && 
	 equalSingleParams(action) && equalSourceParams(action)) 
	return true;
    }

    return false;
  }

  /**
   * Indicates whether the single valued parameters of the given action equal to this actions 
   * single valued parameters.
   */ 
  public boolean
  equalSingleParams
  (
   BaseAction action
  )
  {
    return pSingleParams.equals(action.pSingleParams);
  }

  /**
   * Indicates whether the per-source parameters of the given action equal to this actions 
   * per-source parameters for primary and secondary file sequences.
   */ 
  public boolean
  equalSourceParams
  (
   BaseAction action
  )
  {
    return (pSourceParams.equals(action.pSourceParams) && 
	    pSecondaryParams.equals(action.pSecondaryParams));
  }


  

  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public Object 
  clone()
  {
    BaseAction clone = (BaseAction) super.clone();
    
    clone.pSingleParams = new TreeMap<String,ActionParam>();
    for(ActionParam param : pSingleParams.values()) {
      ActionParam pclone = (ActionParam) param.clone();
      clone.pSingleParams.put(pclone.getName(), pclone);
    }

    clone.pSourceParams = new TreeMap<String,TreeMap<String,ActionParam>>();
    for(String source : pSourceParams.keySet()) {
      TreeMap<String,ActionParam> params = new TreeMap<String,ActionParam>();

      for(ActionParam param : pSourceParams.get(source).values()) {
	ActionParam pclone = (ActionParam) param.clone();
	params.put(pclone.getName(), pclone);
      }

      clone.pSourceParams.put(source, params);
    }

    clone.pSecondaryParams = 
      new TreeMap<String,TreeMap<FilePattern,TreeMap<String,ActionParam>>>();
    for(String source : pSecondaryParams.keySet()) {
      TreeMap<FilePattern,TreeMap<String,ActionParam>> sparams = 
	new TreeMap<FilePattern,TreeMap<String,ActionParam>>();

      for(FilePattern fpat : pSecondaryParams.get(source).keySet()) {
	TreeMap<String,ActionParam> params = new TreeMap<String,ActionParam>();

	for(ActionParam param : pSecondaryParams.get(source).get(fpat).values()) {
	  ActionParam pclone = (ActionParam) param.clone();
	  params.put(pclone.getName(), pclone);
	}

	sparams.put(fpat, params);
      }
    
      clone.pSecondaryParams.put(source, sparams); 
    }

    return clone;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    super.toGlue(encoder);
    
    if(!pSingleParams.isEmpty()) 
      encoder.encode("SingleParams", pSingleParams);
    
    if(!pSourceParams.isEmpty()) 
      encoder.encode("SourceParams", pSourceParams);

    if(!pSecondaryParams.isEmpty()) 
      encoder.encode("SecondarySourceParams", pSecondaryParams);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    TreeMap<String,ActionParam> single = 
      (TreeMap<String,ActionParam>) decoder.decode("SingleParams");   
    if(single != null) {
      for(ActionParam param : single.values()) 
	pSingleParams.put(param.getName(), param); 
    }

    TreeMap<String,TreeMap<String,ActionParam>> source = 
      (TreeMap<String,TreeMap<String,ActionParam>>) decoder.decode("SourceParams");   
    if(source != null) {
      for(String sname : source.keySet()) {
	TreeMap<String,ActionParam> params = new TreeMap<String,ActionParam>();
	for(ActionParam param : source.get(sname).values()) 
	  params.put(param.getName(), param);
	pSourceParams.put(sname, params); 
      }
    }

    TreeMap<String,TreeMap<FilePattern,TreeMap<String,ActionParam>>> secondary = 
      (TreeMap<String,TreeMap<FilePattern,TreeMap<String,ActionParam>>>) 
        decoder.decode("SecondarySourceParams");   
    if(secondary != null) {
      for(String sname : secondary.keySet()) {
	TreeMap<FilePattern,TreeMap<String,ActionParam>> otable = secondary.get(sname);
	TreeMap<FilePattern,TreeMap<String,ActionParam>> table = 
	  new TreeMap<FilePattern,TreeMap<String,ActionParam>>();
	for(FilePattern fpat : otable.keySet()) {
	  TreeMap<String,ActionParam> params = new TreeMap<String,ActionParam>();
	  for(ActionParam param : otable.get(fpat).values()) 
	    params.put(param.getName(), param);
	  table.put(fpat, params); 
	}
	pSecondaryParams.put(sname, table);
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8953612926185824947L;
  
  private static final String aExtraOptions = "ExtraOptions"; 

  private static final String sPythonLaunchHeader = 
    ("import os;\n" +
     "import sys;\n\n" +
     "def launch(program, args):\n" +
     "    a = [program] + args\n" +
     "    print('RUNNING: ' + ' '.join(a))\n" +
     "    sys.stdout.flush()\n" + 
     "    result = os.spawnvp(os.P_WAIT, program, a)\n" +
     "    if result != 0:\n" +
     "        sys.exit('  FAILED: Exit Code = ' + str(result));\n\n");
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The table of single valued action parameters.
   */
  private TreeMap<String,ActionParam>  pSingleParams;    

  /** 
   * The table of action parameters associated with the primary file sequence of an upstream 
   * node.  Indexed by fully resolved node name and action parameter name.
   */
  private TreeMap<String,TreeMap<String,ActionParam>>  pSourceParams;    

  /**
   * The table of action parameters associated with a secondary file sequence of an upstream
   * node.  Indexed by fully resolved node name, seconday sequence file pattern and action 
   * parameter name.
   */ 
  private TreeMap<String,TreeMap<FilePattern,TreeMap<String,ActionParam>>>  pSecondaryParams;


  /*----------------------------------------------------------------------------------------*/

  /**
   * A named set of parameter preset choices.
   */ 
  private TreeMap<String,ArrayList<String>>  pPresetChoices;

  /**
   * The preset values to assign parameters indexed by preset name, choice name and target   
   * parameter name.
   */ 
  private TreeMap<String,TreeMap<String,TreeMap<String,Comparable>>>  pPresetValues;
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Specifies the grouping of single valued parameters used to layout components which 
   * represent the parameters in the user interface. 
   */ 
  private LayoutGroup  pSingleLayout;

  /**
   * Specifies the grouping of per-source parameters used to layout components which 
   * represent the parameters in the user interface. <P> 
   */ 
  private ArrayList<String>  pSourceLayout;

}



