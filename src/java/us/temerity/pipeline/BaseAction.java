// $Id: BaseAction.java,v 1.34 2006/09/29 03:03:21 jim Exp $

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
   * Get the name of the catagory of this plugin.
   */ 
  public String 
  getPluginCatagory() 
  {
    return "Action";
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

    pSingleLayout = group; 
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
      pSingleLayout = new LayoutGroup(true);
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
   * Construct a {@link SubProcessHeavy SubProcessHeavy} instance which when executed will 
   * fulfill the given action agenda. <P> 
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
   * Get the abstract pathname of the root directory used to store temporary files 
   * created by the job. <P> 
   * 
   * @param agenda 
   *   The jobs action agenda.
   */
  public Path
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
   *   The jobs action agenda.
   */
  public File
  getTempDir
  (
   ActionAgenda agenda
  )
  {
    return getTempPath(agenda).toFile();
  }

  /** 
   * Create a unique temporary file for the job with the given suffix and access 
   * permissions. <P> 
   * 
   * If successful, the temporary file will be added to the set of files which will be 
   * removed upon termination of the Java runtime (see @{link #cleanupLater cleanupLater}).
   * 
   * @param agenda 
   *   The jobs action agenda.
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
    if(single != null) 
      pSingleParams.putAll(single);

    TreeMap<String,TreeMap<String,ActionParam>> source = 
      (TreeMap<String,TreeMap<String,ActionParam>>) decoder.decode("SourceParams");   
    if(source != null) 
      pSourceParams.putAll(source);

    TreeMap<String,TreeMap<FilePattern,TreeMap<String,ActionParam>>> secondary = 
      (TreeMap<String,TreeMap<FilePattern,TreeMap<String,ActionParam>>>) 
        decoder.decode("SecondarySourceParams");   
    if(secondary != null) 
      pSecondaryParams.putAll(secondary);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8953612926185824947L;
  



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



