// $Id: BaseAction.java,v 1.9 2004/05/21 18:07:30 jim Exp $

package us.temerity.pipeline;

import  us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   A C T I O N                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The abstract superclass of all Pipelin node Actions. <P>
 * 
 * Actions are used by Pipeline to regenerate the files associated with non-leaf nodes
 * in a consistent and reliable manner.
 */
public abstract
class BaseAction
  extends Described
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Construct with a name and description. 
   * 
   * @param name 
   *   The short name of the editor.  
   * 
   * @param desc 
   *   A short description used in tooltips.
   */ 
  protected
  BaseAction
  (
   String name,  
   String desc  
  ) 
  {
    super(name, desc);

    pSingleParams = new TreeMap<String,ActionParam>();
    pDependParams = new TreeMap<String,TreeMap<String,ActionParam>>();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /*-- SINGLE VALUED PARAMETERS ------------------------------------------------------------*/
  
  /**
   * Add a single valued parameter to this Action. <P>
   *
   * This method is used by subclasses in thier constructors initialize the set of 
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
    if(pSingleParams.containsKey(param.getName())) 
      throw new IllegalArgumentException
	("A parameter named (" + param.getName() + ") already exists!");

    pSingleParams.put(param.getName(), param); 
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

    if(value == null)
      throw new IllegalArgumentException("The parameter value cannot be (null)!");

    ActionParam param = pSingleParams.get(name);
    if(param == null) 
      throw new IllegalArgumentException
	("No parameter named (" + param.getName() + ") exists for this action!");

    param.setValue(value);
  }

  /** 
   * Copy the values of all of the single valued parameters from the given action.
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
    if(getClass() != action.getClass()) 
      throw new IllegalArgumentException
	("Actions of type (" + action.getClass().getName() + ") cannot be used to set the " +
	 "parameters of Actions of type (" + getClass().getName() + ")!");

    for(ActionParam param : action.getSingleParams()) 
      setSingleParamValue(param.getName(), param.getValue());
  }

  

  /*-- PER DEPENDENCY PARAMETERS -----------------------------------------------------------*/
  
  /**
   * Does this action support per-dependency parameters?  <P> 
   * 
   * Subclasses MUST override this method to return <CODE>true</CODE> if per-dependency 
   * paramters are allowed to be added to the action. 
   */ 
  public boolean 
  supportsDependParams()
  {
    return false;
  }

  /** 
   * Does this action have per-dependency parameters for the given node? 
   * 
   * @param depend  
   *   The fully resolved dependency node name.
   */ 
  public boolean 
  hasDependParams
  (
   String depend      
  ) 
  {
    return pDependParams.containsKey(depend);
  }

  /**
   * Initialize a new set of parameters for the given dependency. 
   * 
   * @param depend  
   *   The fully resolved dependency node name.
   */ 
  protected void
  initDependParams
  (
   String depend      
  ) 
  {
    if(depend == null)
      throw new IllegalArgumentException("The dependency name cannot be (null)!");

    TreeMap<String,ActionParam> params = getInitialDependParams();
    assert(params != null);
    
    pDependParams.put(depend, params);
  }

  /**
   * Get an initial set of action parameters associated with a dependency. <P> 
   * 
   * Subclasses which support per-dependency parameters MUST override this method
   * to provide a means for initializing parameters for dependencies.  
   */ 
  protected TreeMap<String,ActionParam>
  getInitialDependParams()
  {
    return null;
  }



  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get node names of the dependencies with per-dependency parameters.
   */ 
  public Set<String>
  getDependNames()
  {
    return Collections.unmodifiableSet(pDependParams.keySet());
  }


  /**
   * Get the value of the named parameter for the given dependency. 
   *
   * @param depend  
   *   The fully resolved dependency node name.
   * 
   * @param name  
   *   The name of the parameter. 
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no parameter with the given name exists for the given dependency.
   */ 
  public Comparable
  getDependParamValue
  (
   String depend,
   String name  
  ) 
    throws PipelineException	
  {
    ActionParam param = getDependParam(depend, name);
    if(param == null)
      throw new PipelineException
	("Unable to determine the value of the (" + name + ") parameter for the (" + 
	 depend + ") dependency !");

    return param.getValue();
  }

  /**
   * Get the named parameter for the given dependency. 
   *
   * @param depend  
   *   The fully resolved dependency node name.
   * 
   * @param name  
   *   The name of the parameter. 
   * 
   * @return 
   *   The action parameter or <CODE>null</CODE> if no parameter with the given name exists
   *   for the given dependency.
   */ 
  public ActionParam
  getDependParam
  (
   String depend,
   String name  
  )
  {
    if(depend == null)
      throw new IllegalArgumentException("The dependency name cannot be (null)!");

    TreeMap<String,ActionParam> table = pDependParams.get(depend);
    if(table == null) 
      return null;

    return table.get(name);
  }

  /**
   * Get all of the parameters for the given dependency.  <P> 
   * 
   * The returned ArrayList may be empty if the given dependency does not have any
   * parameters.
   * 
   * @param depend  
   *   The fully resolved dependency node name. 
   * 
   * @return 
   *   The set of parameters for the given dependency.  
   */ 
  public Collection<ActionParam>
  getDependParams
  (
   String depend  
  ) 
  {    
    if(depend == null)
      throw new IllegalArgumentException("The dependency name cannot be (null)!");

    TreeMap<String,ActionParam> table = pDependParams.get(depend);
    if(table != null) 
      return Collections.unmodifiableCollection(table.values());
    else 
      return new ArrayList<ActionParam>();
  }


  /**
   * Set the value of parameter for the given dependency.
   *
   * @param depend  
   *   The fully resolved dependency node name. 
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param value  
   *   The new value of the parameter. 
   */ 
  public void 
  setDependParamValue
  (
   String depend,
   String name, 
   Comparable value      
  ) 
  {
    if(depend == null)
      throw new IllegalArgumentException("The dependency name cannot be (null)!");

    if(name == null)
      throw new IllegalArgumentException("The parameter name cannot be (null)!");

    if(value == null)
      throw new IllegalArgumentException("The parameter value cannot be (null)!");

    TreeMap<String,ActionParam> table = pDependParams.get(depend);
    if(table == null) 
      throw new IllegalArgumentException("The dependency does not have parameters!");

    ActionParam param = table.get(name);
    if(param == null) 
      throw new IllegalArgumentException
	("No parameter named (" + param.getName() + ") exists for the dependency (" +
	 depend + ")!");
    
    param.setValue(value);    
  }

  /** 
   * Copy the values of all of the per-dependency parameters from the given action.
   * 
   * @param action  
   *   The action to use as the source of single valued parameter values.
   */
  public void 
  setDependParamValues
  (
   BaseAction action   
  ) 
  {
    if(getClass() != action.getClass()) 
      throw new IllegalArgumentException
	("Actions of type (" + action.getClass().getName() + ") cannot be used to set the " +
	 "parameters of Actions of type (" + getClass().getName() + ")!");

    for(String depend : action.getDependNames()) {
      removeDependParams(depend);
      initDependParams(depend);

      for(ActionParam param : action.getDependParams(depend)) 
	setDependParamValue(depend, param.getName(), param.getValue());
    }
  }


  /**
   * Remove all of the per-dependency parameters associated with the given dependency. 
   * 
   * @param depend  
   *   The fully resolved dependency node name. 
   */ 
  public void 
  removeDependParams
  (
   String depend        /* IN: the fully resolved dependency node name */ 
  ) 
  {
    if(depend == null)
      throw new IllegalArgumentException("The dependency name cannot be (null)!");

    pDependParams.remove(depend);
  }   

  /**
   * Remove all per-dependency parameters from this action.
   */ 
  public void 
  removeAllDependParams()
  {
    pDependParams.clear();
  }  



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a {@link SubProcess SubProcess} instance which when executed will 
   * regenerate the given file sequences for the target node. <P>
   * 
   * @param jobID  
   *   A unique job identifier.
   * 
   * @param name  
   *   The fully resolved name of the target node. 
   * 
   * @param author  
   *   The name of the user which submitted the job.
   * 
   * @param primaryTarget  
   *   The primary file sequence to generate.
   *
   * @param secondaryTargets  
   *   The secondary file sequences to generate
   *
   * @param primarySources  
   *   A table of primary file sequences associated with each dependency.
   *
   * @param secondarySources  
   *   The table of secondary file sequences associated with each dependency.
   *
   * @param env  
   *   The environment under which the action is run.  
   * 
   * @param dir  
   *   The working directory where the action is run.
   * 
   * @return 
   *   The SubProcess which will regenerate the target file sequences.
   * 
   * @throws PipelineException 
   *   If unable to prepare a SubProcess due to illegal, missing or imcompatable 
   *   file sequence arguments.
   */
  public abstract SubProcess
  prep
  (
   int jobID,                
   String name,              
   String author,            
   FileSeq primaryTarget,    
   ArrayList<FileSeq> secondaryTargets,
   Map<String,FileSeq> primarySources,        
   Map<String,ArrayList> secondarySources,   // should be: Map<String,ArrayList<FileSeq>> 
   Map<String,String> env, 
   File dir                 
  )
    throws PipelineException;



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

  /**
   * Get the directory used to store temporary files. 
   */
  protected File
  getTempDir()
  {
    return PackageInfo.sTempDir;
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
    if((obj != null) && (getClass() == obj.getClass())) {
      BaseAction action = (BaseAction) obj;

      if(super.equals(obj) && 
	 pSingleParams.equals(action.pSingleParams) && 
	 pDependParams.equals(action.pDependParams))
	return true;
    }

    return false;
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

    clone.pDependParams = new TreeMap<String,TreeMap<String,ActionParam>>();
    for(String depend : pDependParams.keySet()) {
      TreeMap<String,ActionParam> params = new TreeMap<String,ActionParam>();

      for(ActionParam param : pDependParams.get(depend).values()) {
	ActionParam pclone = (ActionParam) param.clone();
	params.put(pclone.getName(), pclone);
      }

      clone.pDependParams.put(depend, params);
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

    {
      TreeMap<String,Comparable> params = new TreeMap<String,Comparable>();
      for(ActionParam param : getSingleParams()) 
	params.put(param.getName(), param.getValue());

      if(!params.isEmpty()) 
	encoder.encode("SingleParams", params);
    }

    {
      TreeMap<String,TreeMap<String,Comparable>> dparams = 
	new TreeMap<String,TreeMap<String,Comparable>>();

      for(String depend : getDependNames()) {
	TreeMap<String,Comparable> params = new TreeMap<String,Comparable>();

	for(ActionParam param : getDependParams(depend)) 
	  params.put(param.getName(), param.getValue());
	
	dparams.put(depend, params);
      }

      if(!dparams.isEmpty()) 
	encoder.encode("DependParams", dparams);
    }
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    try {
      {
	TreeMap<String,Comparable> params = 
	  (TreeMap<String,Comparable>) decoder.decode("SingleParams");   
	if(params != null) {
	  for(String name : params.keySet()) 
	    setSingleParamValue(name, params.get(name));	
	}
      }
      
      {
	TreeMap<String,TreeMap<String,Comparable>> dparams = 
	  (TreeMap<String,TreeMap<String,Comparable>>) decoder.decode("DependParams");   
	if(dparams != null) {
	  for(String depend : dparams.keySet()) {
	    initDependParams(depend);
	    
	    TreeMap<String,Comparable> params = dparams.get(depend);
	    for(String name : params.keySet()) 
	      setDependParamValue(depend, name, params.get(name));	
	  }
	}
      }
    }
    catch (IllegalArgumentException ex) {
      throw new GlueException(ex);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The table of single valued action parameters.
   */
  private TreeMap<String,ActionParam>  pSingleParams;    

  /** 
   * The table of per-dependency action parameters.
   */
  private TreeMap<String,TreeMap<String,ActionParam>>  pDependParams;    

}



