// $Id: BaseAction.java,v 1.18 2004/10/28 15:55:23 jim Exp $

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
 * instead.
 */
public 
class BaseAction
  extends BasePlugin
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  BaseAction() 
  {
    super();

    pSingleParams = new TreeMap<String,BaseActionParam>();
    pSourceParams = new TreeMap<String,TreeMap<String,BaseActionParam>>();
  }

  /** 
   * Construct with the given name, version and description. 
   * 
   * @param name 
   *   The short name of the action.  
   * 
   * @param vid
   *   The action plugin revision number.
   * 
   * @param desc 
   *   A short description of the action.
   */ 
  protected
  BaseAction
  (
   String name,  
   VersionID vid,
   String desc
  ) 
  {
    super(name, vid, desc);

    pSingleParams = new TreeMap<String,BaseActionParam>();
    pSourceParams = new TreeMap<String,TreeMap<String,BaseActionParam>>();
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
    super(action.pName, action.pVersionID, action.pDescription);

    pVersionID    = action.pVersionID; 
    pSingleParams = action.pSingleParams;
    pSourceParams = action.pSourceParams; 
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
      BaseActionParam aparam = action.getSingleParam(name);
      if(aparam != null) {
	BaseActionParam param = pSingleParams.get(name);
	try {
	  param.setValue(aparam.getValue());
	}
	catch(IllegalArgumentException ex) {
	  Logs.ops.warning(ex.getMessage());
	}
      }
    }
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

    for(String source : action.getSourceNames()) {
      removeSourceParams(source);
      initSourceParams(source);

      TreeMap<String,BaseActionParam> params = pSourceParams.get(source);
      if(params != null) {
	for(BaseActionParam aparam : action.getSourceParams(source)) {
	  BaseActionParam param = params.get(aparam.getName()); 
	  if(param != null) {
	    try {
	      param.setValue(aparam.getValue());
	    }
	    catch(IllegalArgumentException ex) {
	      Logs.ops.warning(ex.getMessage());
	    }
	  }
	}
      }
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
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the layout of single valued parameters in the user interface. <P> 
   * 
   * The <CODE>layout</CODE> argument must contain the name of each single valued parameter
   * exactly once, but may also contain <CODE>null</CODE> values.  The order of the parameter
   * names in this layout list determines that order that the parameters are listed in the 
   * user interface.  Extra space will be added between parameters for each <CODE>null</CODE>
   * value encountered.  In this way parameters can be grouped by inserting <CODE>null</CODE>
   * entries between parameter names. <P> 
   * 
   * This method should be called by subclasses in their constructor after intializing all 
   * single valued parameters with the {@link #addSingleParam addSingleParam} method.
   * 
   * @param layout
   *   The names of the single valued parameters.
   */
  protected void
  setSingleLayout
  (
   Collection<String> layout
  ) 
  {
    for(String name : layout)
      if((name != null) && !pSingleParams.containsKey(name)) 
	throw new IllegalArgumentException
	  ("There is no single valued parameter (" + name + ") defined for this Action!");

    for(String pname : pSingleParams.keySet()) {
      int cnt = 0;
      for(String name : layout) 
	if((name != null) && name.equals(pname)) 
	  cnt++;
      
      switch(cnt) {
      case 0:
	throw new IllegalArgumentException
	  ("The single valued parameter (" + pname + ") was not specified in the layout!");
	
      case 1:
	break;

      default:
	throw new IllegalArgumentException
	  ("The single valued parameter (" + pname + ") was specified (" + cnt + ") times " +
	   "by the layout!  Each parameter may only be specified once.");
      }
    }

    pSingleLayout = new ArrayList<String>(layout);    
  }

  /**
   * Get the layout of single valued parameters in the user interface. <P> 
   * 
   * The returned parameter names will include all single valued parameters exactly 
   * once.  The returned names may also contain <CODE>null</CODE> values, which should
   * be interpreted as delimeters between groupings of parameters.
   * 
   * @return 
   *   The names of each single valued parameter in the order of layout.
   */ 
  public Collection<String> 
  getSingleLayout() 
  {
    if(pSingleLayout == null) 
      pSingleLayout = new ArrayList<String>(pSingleParams.keySet());
    
    return Collections.unmodifiableCollection(pSingleLayout);
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the layout of per-source parameters in the user interface. <P> 
   * 
   * The <CODE>layout</CODE> argument must contain the name of each per-source parameter
   * exactly once.  The order of the parameter names in this layout list determines that 
   * order that the parameters are listed in the user interface. <P> 
   * 
   * This method should be called by subclasses in their constructor if they have overriden
   * the {@link #supportsSourceParams supportsSourceParams} to return <CODE>true</CODE> and
   * have implemented the {@link #getInitialSourceParams getInitialSourceParams} method. 
   * All parameters defined by the <CODE>getInitialSourceParams</CODE> method must be
   * a member of the <CODE>layout</CODE> argument.
   * 
   * @param layout
   *   The names of the per-source parameters.
   */
  protected void
  setSourceLayout
  (
   Collection<String> layout
  ) 
  {
    if(!supportsSourceParams()) 
      throw new IllegalArgumentException
	("This action does not have per-source parameters!");
    
    TreeMap<String,BaseActionParam> params = getInitialSourceParams();
    if(params == null) 
      throw new IllegalArgumentException
	("This action does not have per-source parameters!");

    for(String name : layout)
      if((name != null) && !params.containsKey(name)) 
	throw new IllegalArgumentException
	  ("There is no per-source parameter (" + name + ") defined for this Action!");

    for(String pname : params.keySet()) {
      int cnt = 0;
      for(String name : layout) 
	if((name != null) && name.equals(pname)) 
	  cnt++;
      
      switch(cnt) {
      case 0:
	throw new IllegalArgumentException
	  ("The per-source parameter (" + pname + ") was not specified in the layout!");
	
      case 1:
	break;

      default:
	throw new IllegalArgumentException
	  ("The per-source parameter (" + pname + ") was specified (" + cnt + ") times " +
	   "by the layout!  Each parameter may only be specified once.");
      }
    }

    pSourceLayout = new ArrayList<String>(layout);    
  }

  /**
   * Get the layout of per-source parameters in the user interface. <P> 
   * 
   * The returned parameter names will include all per-source parameters exactly 
   * once. 
   * 
   * @return 
   *   The names of each per-source parameter in the order of layout.
   */ 
  public Collection<String> 
  getSourceLayout() 
  {
    if(!supportsSourceParams()) 
      throw new IllegalArgumentException
	("This action does not have per-source parameters!");

    if(pSourceLayout == null) {
      TreeMap<String,BaseActionParam> params = getInitialSourceParams();
      if(params == null) 
	throw new IllegalArgumentException
	  ("This action does not have per-source parameters!");
      
      pSourceLayout = new ArrayList<String>(params.keySet());
    }
    
    return Collections.unmodifiableCollection(pSourceLayout);
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
    return new File(PackageInfo.sTempDir, "pljobmgr/" + agenda.getJobID() + "/scratch");
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
      tmp = File.createTempFile(pName + "-" + agenda.getJobID(), "." + suffix, 
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
   * Make sure the target working directory exists. <P> 
   */ 
  public void
  makeTargetDir
  (
    ActionAgenda agenda
  )
    throws PipelineException
  {
    File dir = agenda.getWorkingDir();
    if(dir.isDirectory()) 
      return;

    ArrayList<String> args = new ArrayList<String>();
    args.add("--parents");
    args.add("--mode=755");
    args.add(dir.getPath());

    SubProcessLight proc = 
      new SubProcessLight(agenda.getNodeID().getAuthor(), 
			  "MakeWorkingDir-" + agenda.getJobID(), 
			  "mkdir", args, agenda.getEnvironment(), PackageInfo.sTempDir);
    try {
      proc.start();
      proc.join();
      if(!proc.wasSuccessful()) 
	throw new PipelineException
	  ("Unable to create the working area directory (" + dir + "):\n\n" + 
	   "  " + proc.getStdErr());
    }
    catch(InterruptedException ex) {
      throw new PipelineException
	("Interrupted while creating working area directory (" + dir + ")!");
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
    
    if(!pSingleParams.isEmpty()) 
      encoder.encode("SingleParams", pSingleParams);
    
    if(!pSourceParams.isEmpty()) 
      encoder.encode("SourceParams", pSourceParams);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    TreeMap<String,BaseActionParam> single = 
      (TreeMap<String,BaseActionParam>) decoder.decode("SingleParams");   
    if(single != null) 
      pSingleParams.putAll(single);

    TreeMap<String,TreeMap<String,BaseActionParam>> source = 
      (TreeMap<String,TreeMap<String,BaseActionParam>>) decoder.decode("SourceParams");   
    if(source != null) 
      pSourceParams.putAll(source);
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
  private TreeMap<String,BaseActionParam>  pSingleParams;    

  /** 
   * The table of action parameters associated with each linked upstream node.
   */
  private TreeMap<String,TreeMap<String,BaseActionParam>>  pSourceParams;    


  /*----------------------------------------------------------------------------------------*/

  /**
   * Used to determing the order and grouping of single valued parameters in the 
   * graphical user interface. 
   */ 
  private ArrayList<String>  pSingleLayout;

  /**
   * Used to determing the order and grouping of per-source parameters in the 
   * graphical user interface. 
   */ 
  private ArrayList<String>  pSourceLayout;

}



