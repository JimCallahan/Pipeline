// $Id: BaseAction.java,v 1.12 2004/07/24 18:14:11 jim Exp $

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

    pSingleParams = new TreeMap<String,BaseActionParam>();
    pSourceParams = new TreeMap<String,TreeMap<String,BaseActionParam>>();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /*-- SINGLE VALUED PARAMETERS ------------------------------------------------------------*/
  
  /**
   * Does the action have any single valued parameters?
   */ 
  public boolean 
  hasSingleParams()
  {
    return (!pSingleParams.isEmpty());
  }

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
    BaseActionParam param 
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
    BaseActionParam param = getSingleParam(name); 
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
  public BaseActionParam
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
  public Collection<BaseActionParam>
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

    BaseActionParam param = pSingleParams.get(name);
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

    for(BaseActionParam param : action.getSingleParams()) 
      setSingleParamValue(param.getName(), param.getValue());
  }

  

  /*-- PER-SOURCE PARAMETERS ---------------------------------------------------------------*/
  
  /**
   * Does this action support per-source parameters?  <P> 
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
   * Does this action have per-source parameters for the given node? 
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
   * Initialize a new set of parameters for the given upstream node.
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

    TreeMap<String,BaseActionParam> params = getInitialSourceParams();
    assert(params != null);
    
    pSourceParams.put(source, params);
  }

  /**
   * Get an initial set of action parameters associated with an upstream node. <P> 
   * 
   * Subclasses which support per-source parameters MUST override this method
   * to provide a means for initializing parameters for dependencies.  
   */ 
  public TreeMap<String,BaseActionParam>
  getInitialSourceParams()
  {
    return null;
  }

  
  


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get node names of the upstream nodes with per-source parameters.
   */ 
  public Set<String>
  getSourceNames()
  {
    return Collections.unmodifiableSet(pSourceParams.keySet());
  }


  /**
   * Get the value of the named parameter for the given upstream node. 
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
    BaseActionParam param = getSourceParam(source, name);
    if(param == null)
      throw new PipelineException
	("Unable to determine the value of the (" + name + ") parameter for the upstream " + 
	 "node (" + source + ")!");

    return param.getValue();
  }

  /**
   * Get the named parameter for the given upstream node.
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
  public BaseActionParam
  getSourceParam
  (
   String source,
   String name  
  )
  {
    if(source == null)
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    TreeMap<String,BaseActionParam> table = pSourceParams.get(source);
    if(table == null) 
      return null;

    return table.get(name);
  }

  /**
   * Get all of the per-source parameters for the given upstream node.  <P> 
   * 
   * The returned ArrayList may be empty if the given upstream node does not have any
   * parameters.
   * 
   * @param source  
   *   The fully resolved node name of the upstream node.
   * 
   * @return 
   *   The set of parameters for the given upstream node.  
   */ 
  public Collection<BaseActionParam>
  getSourceParams
  (
   String source  
  ) 
  {    
    if(source == null)
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    TreeMap<String,BaseActionParam> table = pSourceParams.get(source);
    if(table != null) 
      return Collections.unmodifiableCollection(table.values());
    else 
      return new ArrayList<BaseActionParam>();
  }


  /**
   * Set the value of a per-source parameter for the given upstream node.
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

    TreeMap<String,BaseActionParam> table = pSourceParams.get(source);
    if(table == null) 
      throw new IllegalArgumentException("The upstream node does not have parameters!");

    BaseActionParam param = table.get(name);
    if(param == null) 
      throw new IllegalArgumentException
	("No parameter named (" + param.getName() + ") exists for the upstream node (" +
	 source + ")!");
    
    param.setValue(value);    
  }

  /** 
   * Copy the values of all of the per-source parameters from the given action.
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
    if(getClass() != action.getClass()) 
      throw new IllegalArgumentException
	("Actions of type (" + action.getClass().getName() + ") cannot be used to set the " +
	 "parameters of Actions of type (" + getClass().getName() + ")!");

    for(String source : action.getSourceNames()) {
      removeSourceParams(source);
      initSourceParams(source);

      for(BaseActionParam param : action.getSourceParams(source)) 
	setSourceParamValue(source, param.getName(), param.getValue());
    }
  }


  /**
   * Remove all of the per-source parameters associated with the given upstream node.
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
   * Remove all per-source parameters from this action.
   */ 
  public void 
  removeAllSourceParams()
  {
    pSourceParams.clear();
  }  



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a {@link SubProcess SubProcess} instance which when executed will fulfill
   * the given action agenda. <P> 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @return 
   *   The SubProcess which will fulfill the agenda.
   * 
   * @throws PipelineException 
   *   If unable to prepare a SubProcess due to illegal, missing or imcompatable 
   *   information in the action agenda.
   */
  public abstract SubProcess
  prep
  (
   ActionAgenda agenda
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
	 equalSingleParams(action) && 
	 equalSourceParams(action)) 
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
   * per-source parameters.
   */ 
  public boolean
  equalSourceParams
  (
   BaseAction action
  )
  {
    return pSourceParams.equals(action.pSourceParams);
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
    
    clone.pSingleParams = new TreeMap<String,BaseActionParam>();
    for(BaseActionParam param : pSingleParams.values()) {
      BaseActionParam pclone = (BaseActionParam) param.clone();
      clone.pSingleParams.put(pclone.getName(), pclone);
    }

    clone.pSourceParams = new TreeMap<String,TreeMap<String,BaseActionParam>>();
    for(String source : pSourceParams.keySet()) {
      TreeMap<String,BaseActionParam> params = new TreeMap<String,BaseActionParam>();

      for(BaseActionParam param : pSourceParams.get(source).values()) {
	BaseActionParam pclone = (BaseActionParam) param.clone();
	params.put(pclone.getName(), pclone);
      }

      clone.pSourceParams.put(source, params);
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
      for(BaseActionParam param : getSingleParams()) 
	params.put(param.getName(), param.getValue());

      if(!params.isEmpty()) 
	encoder.encode("SingleParams", params);
    }

    {
      TreeMap<String,TreeMap<String,Comparable>> dparams = 
	new TreeMap<String,TreeMap<String,Comparable>>();

      for(String source : getSourceNames()) {
	TreeMap<String,Comparable> params = new TreeMap<String,Comparable>();

	for(BaseActionParam param : getSourceParams(source)) 
	  params.put(param.getName(), param.getValue());
	
	dparams.put(source, params);
      }

      if(!dparams.isEmpty()) 
	encoder.encode("PerSourceParams", dparams);
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
	  (TreeMap<String,TreeMap<String,Comparable>>) decoder.decode("PerSourceParams");   
	if(dparams != null) {
	  for(String source : dparams.keySet()) {
	    initSourceParams(source);
	    
	    TreeMap<String,Comparable> params = dparams.get(source);
	    for(String name : params.keySet()) 
	      setSourceParamValue(source, name, params.get(name));	
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
  private TreeMap<String,BaseActionParam>  pSingleParams;    

  /** 
   * The table of action parameters associated with each linked upstream node.
   */
  private TreeMap<String,TreeMap<String,BaseActionParam>>  pSourceParams;    

}



