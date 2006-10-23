// $Id: BaseExt.java,v 1.2 2006/10/23 11:30:20 jim Exp $

package us.temerity.pipeline;

import  us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   E X T                                                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * The superclass of all server extension plugins. <P>
 * 
 * Extensions provide a mechanism for extending the functionality of the Pipeline server
 * daemons.  For each server operation which can be extended, this class will provide two 
 * methods which can be overloaded in user created subclasses to enhance the operation. <P> 
 * 
 * The first type of extension method is called a pre-operation test.  It is invoked before 
 * the operation is performed and if it throws an exception, the operation will be aborted.
 * These methods should be used to perform additional consistency or security related checks
 * which are not long in duration so that the interactive performance of the operation is not
 * impacted from the users point of view.<P> 
 * 
 * The second type of extension is called a post-operation task.  The server will create a 
 * new Thread to execute this task after the operation has successfully completed.  Because 
 * this operation is performed in its own Thread of execution, the normal operation of the 
 * Pipeline server is not held up waiting for the task to complete.  However, these tasks
 * will impact the overall load on the machine running the server daemon and could impact
 * overall performance if the tasks are not written with efficiency in mind. <P> 
 * 
 * To minimize unecessary overhead required to support pre-operation tests and post-operation 
 * tasks, there are also predicate methods which specify whether a given pre/post operation 
 * method has been implemented by the Extension subclass.  There may also be predicate methods
 * which control the amount of work required to prepare information passed as parameters to 
 * the pre/post operation methods. <P> 
 * 
 * For example, the following methods are associated with the CheckIn operation for extension 
 * plugins for plmaster(1):
 * <DIV style="margin-left: 40px;">
 * <CODE>
 * public boolean 
 * hasPreCheckInTest() 
 * 
 * public void 
 * preCheckInTest
 * (
 *   NodeID id, 
 *   String message, 
 *   VersionID.Level level
 * ) 
 *   throws PipelineException
 * 
 * public boolean
 * hasPostCheckInTask() 
 * 
 * public boolean
 * postCheckInTaskNeedsStatus() 
 * 
 * public void 
 * postCheckInTask
 * (
 *   NodeID id, 
 *   String message, 
 *   VersionID.Level level, 
 *   NodeStatus status
 * ) 
 *   throws PipelineException
 * </CODE>
 * </DIV> 
 * 
 * In this case, the postCheckInTaskNeedsStatus() method controls whether a full node status
 * operation on the root node of the CheckIn should be performed and passed to the 
 * postCheckInTask() method as its last parameter. <P> 
 * 
 * The number, types and names of these extension methods vary depending on the nature of 
 * the operation they are extending.  The documentation for each method will give details 
 * about the particular usage. <P> 
 * 
 * In addition to the per-method paraters passed to each extension method, each instance of 
 * an Extension plugin subclass may define and set an arbitrary collection of Extension 
 * Parameters.  These parameters can be used to control the behavior of the overloaded 
 * methods interactively by Administrators.  These controls can be accessed inside the 
 * predicate methods to temporarily enable/disable extension features as well as provide 
 * a wide range of control over the behavior of the Extension plugin pre/post operation
 * methods. <P> 
 * 
 * While new plugin subclass versions are being modified and tested the 
 * {@link #underDevelopment underDevelopment} method should be called in the subclasses
 * constructor to enable the plugin to be dynamically reloaded.
 */
public 
class BaseExt
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
  BaseExt() 
  {
    super();
    
    pParams      = new TreeMap<String,ExtensionParam>();
    pEnvironment = new TreeMap<String,String>();
  }

  /** 
   * Construct with the given name, version, vendor and description. 
   * 
   * @param name 
   *   The short name of the extension.
   * 
   * @param vid
   *   The extension plugin revision number. 
   * 
   * @param vendor
   *   The name of the plugin vendor.
   * 
   * @param desc 
   *   A short description of the extension.
   */ 
  protected
  BaseExt
  (
   String name, 
   VersionID vid,
   String vendor, 
   String desc
  ) 
  {
    super(name, vid, vendor, desc);

    pParams      = new TreeMap<String,ExtensionParam>();
    pEnvironment = new TreeMap<String,String>();
  }

  /**
   * Copy constructor. <P> 
   * 
   * Used internally to create a generic instances of plugin subclasses.  This constructor
   * should not be used in end user code! <P> 
   */ 
  public 
  BaseExt
  (
   BaseExt extension
  ) 
  {
    super(extension.pName, extension.pVersionID, extension.pVendor, extension.pDescription);

    pParams      = extension.pParams;
    pEnvironment = extension.pEnvironment;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add support for execution under the given operating system type.<P> 
   * 
   * This method is disabled because Extension plugins can only support the default 
   * Unix operating system.
   * 
   * @param os
   *   The operating system type.
   */ 
  protected void 
  addSupport
  (
   OsType os
  ) 
  {
    switch(os) {
    case Unix:
      super.addSupport(os);
      break;
      
    default:
      throw new IllegalArgumentException
	("Extension plugins can only support the default Unix operating system.");
    }
  }

  /**
   * Remove support for execution under the given operating system type.<P> 
   * 
   * This method is disabled because Extension plugins can only support the default 
   * Unix operating system.
   * 
   * @param os
   *   The operating system type.
   */ 
  protected void 
  removeSupport
  (
   OsType os
  ) 
  {
    switch(os) {
    case Unix:
      throw new IllegalArgumentException
	("Unix support cannot be removed from Extension plugins!");

    default:
      super.removeSupport(os);
    }
  }

  /**
   * Copy the OS support flags from the given plugin.<P> 
   * 
   * This method is disabled because Extension plugins can only support the default 
   * Unix operating system.
   */ 
  protected void
  setSupports
  (
   SortedSet<OsType> oss
  ) 
  {
    if(oss.contains(OsType.MacOS) || oss.contains(OsType.Windows)) 
      throw new IllegalArgumentException
	("Extension plugins can only support the default Unix operating system.");

    super.setSupports(oss);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P A R A M E T E R S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does the action have any parameters?
   */ 
  public boolean 
  hasParams()
  {
    return (!pParams.isEmpty());
  }

  /**
   * Add a parameter to this Extension. <P>
   *
   * This method is used by subclasses in their constructors initialize the set of 
   * parameters that they support.
   *
   * @param param  
   *   The parameter to add.
   */
  protected void 
  addParam
  (
   ExtensionParam param 
  ) 
  {
    if(pParams.containsKey(param.getName())) 
      throw new IllegalArgumentException
	("A parameter named (" + param.getName() + ") already exists!");

    pParams.put(param.getName(), param); 
  }

  /** 
   * Get the value of the parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The parameter value.
   * 
   * @throws PipelineException 
   *   If no parameter with the given name exists.
   */ 
  public Comparable
  getParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    ExtensionParam param = getParam(name); 
    if(param == null)
      throw new PipelineException
	("Unable to determine the value of the (" + name + ") parameter!");
    return param.getValue();
  }

  /** 
   * Get the parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The parameter or <CODE>null</CODE> if no parameter with the given name exists.
   */ 
  public ExtensionParam
  getParam
  (
   String name   
  ) 
  {
    if(name == null)
      throw new IllegalArgumentException("The parameter name cannot be (null)!");
    return pParams.get(name);
  }

  /** 
   * Get all of the parameters. <P> 
   * 
   * The returned ArrayList may be empty if the extension does not have any parameters.
   * 
   * @return 
   *   The set of parameters for this extension.  
   */ 
  public Collection<ExtensionParam>
  getParams()
  {
    return Collections.unmodifiableCollection(pParams.values());
  }

  /**
   * Set the value of a parameter. 
   * 
   * @param name  
   *   The name of the parameter. 
   * 
   * @param value  
   *   The new value of the parameter. 
   */ 
  public void 
  setParamValue
  (
   String name, 
   Comparable value
  ) 
  {
    if(name == null)
      throw new IllegalArgumentException("The parameter name cannot be (null)!");

    ExtensionParam param = pParams.get(name);
    if(param == null) 
      throw new IllegalArgumentException
	("No parameter named (" + param.getName() + ") exists for this extension!");

    param.setValue(value);
  }

  /** 
   * Copy the values of all of the parameters from the given extension. <P> 
   * 
   * Note that there is no requirement that the given extension be the same plugin type or 
   * version.  Any incompatible parameters will simply be ignored by the copy operation.
   * 
   * @param extension  
   *   The extension to use as the source of parameter values.
   */
  public void 
  setParamValues
  (
   BaseExt extension   
  ) 
  {
    for(String name : pParams.keySet()) {
      ExtensionParam aparam = extension.getParam(name);
      if(aparam != null) {
	ExtensionParam param = pParams.get(name);
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
  /*   P A R A M E T E R    L A Y O U T                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sets the hierarchical grouping of parameters and presets which determine the layout of 
   * UI components. <P> 
   * 
   * The given layouts must contain an entry for all parameters defined for the extension 
   * exactly once.  A collapsible drawer component will be created for each layout group 
   * which contains a field for each parameter or preset entry in the order specified by 
   * the group.  All <CODE>null</CODE> entries will cause additional space 
   * to be added between the UI fields. Each layout subgroup will be represented by its own 
   * drawer nested within the drawer for the parent layout group. <P> 
   * 
   * This method should be called by subclasses in their constructor after intializing all 
   * single valued parameters with the {@link #addParam} method.
   * 
   * @param group
   *   The layout group.
   */
  protected void
  setLayout
  (
   LayoutGroup group
  ) 
  {
    TreeSet<String> names = new TreeSet<String>();
    collectLayoutNames(group, names);
    
    for(String name : names) {
      if(!pParams.containsKey(name))
	throw new IllegalArgumentException
	  ("The entry (" + name + ") specified by the parameter layout group " + 
	   "(" + group.getName() + ") does not match any parameter defined for this " +
	   "Extension!");
    }

    for(String name : pParams.keySet()) {
      if(!names.contains(name))
	throw new IllegalArgumentException
	  ("The single valued parameter (" + name + ") defined by this Extension was not " + 
	   "specified by any the parameter layout group!");
    }

    pLayout = new LayoutGroup("ExtensionParameters", 
			      "The Server Extension plugin parameters.", group);
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
	    ("The parameter (" + name + ") was specified more than once in the " + 
	     "given parameter group!");
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
  getLayout()
  {
    if(pLayout == null) {
      pLayout = new LayoutGroup("ExtensionParameters", 
				"The Server Extension plugin parameters.", true);
      for(String name : pParams.keySet()) 
	pLayout.addEntry(name);
    }
    
    return pLayout; 
  }
  
  


  /*----------------------------------------------------------------------------------------*/
  /*   T O O L S E T S                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether an subprocesses execution environment is requird by this extension.
   */
  public boolean
  needsEnvironment()
    throws PipelineException
  {
    return false; 
  }

  /**
   * Sets the environment under which any subprocesses spawned by the extension are 
   * executed.<P>
   * 
   * If the {@link #needsEnvironment} method returns <CODE>true</CODE>, the server daemon
   * will pre-cooked the toolset environment passed to this method before calling any of 
   * the extension's pre/post operation methods.
   * 
   * @param env
   *   The cooked toolset environment.
   */ 
  public void 
  setEnvironment
  (
   SortedMap<String,String> env
  ) 
    throws PipelineException
  {
    pEnvironment.clear();
    pEnvironment.putAll(env);
  }

  /**
   * Get the environment under which any subprocesses spawned by the extension are 
   * executed.<P>
   * 
   * If you will be calling this method, make sure to override the {@link #needsEnvironment}
   * method to return <CODE>true</CODE> so that the environment will be pre-cooked before
   * calling any of the extension's pre/post operation methods.  Otherwise, the environment
   * returned by this method will be empty.
   * 
   * @throws PipelineException
   *   If the current operating system is not supported by the toolset.
   */ 
  public SortedMap<String,String>
  getEnvironment()
    throws PipelineException
  {
    return pEnvironment; 
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
   * created by the extension.
   */
  public Path
  getExtensionTempPath() 
  {
    return new Path(PackageInfo.sTempPath, "plextensions/scratch");
  }

  /**
   * Get the root directory used to store temporary files created by the extension. 
   */
  public File
  getExtensionTempDir() 
  {
    return getExtensionTempPath().toFile();
  }

  /** 
   * Create a unique temporary file for the extension with the given suffix and access 
   * permissions. <P> 
   * 
   * If successful, the temporary file will be added to the set of files which will be 
   * removed upon termination of the Java runtime (see @{link #cleanupLater cleanupLater}).
   * 
   * @param prefix  
   *   The filename prefix.
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
  createExtensionTemp
  (
   String prefix, 
   int mode, 
   String suffix
  ) 
    throws PipelineException 
  {
    File tmp = null;
    try {
      tmp = File.createTempFile(prefix + "-", "." + suffix, getExtensionTempDir());
      chmod(mode, tmp);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to create temporary file for the server extension (" + pName + "):\n\n" + 
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
    if((obj != null) && (obj instanceof BaseExt)) {
      BaseExt extension = (BaseExt) obj;
      if(super.equals(obj) && 
	 equalParams(extension)) 
	return true;
    }

    return false;
  }

  /**
   * Indicates whether the parameters of the given extension equal to this extension's 
   * parameters.
   */ 
  public boolean
  equalParams
  (
   BaseExt extension
  )
  {
    return pParams.equals(extension.pParams);
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
    BaseExt clone = (BaseExt) super.clone();
    
    clone.pParams = new TreeMap<String,ExtensionParam>();
    for(ExtensionParam param : pParams.values()) {
      ExtensionParam pclone = (ExtensionParam) param.clone();
      clone.pParams.put(pclone.getName(), pclone);
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
    
    if(!pParams.isEmpty()) 
      encoder.encode("Params", pParams);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    TreeMap<String,ExtensionParam> single = 
      (TreeMap<String,ExtensionParam>) decoder.decode("Params");   
    if(single != null) 
      pParams.putAll(single);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4551283348457035732L;


 
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The table of extension parameters.
   */
  protected TreeMap<String,ExtensionParam>  pParams;    

  /**
   * Specifies the grouping of parameters used to layout components which represent 
   * the parameters in the user interface. 
   */ 
  private LayoutGroup  pLayout;

  /**
   * The cached toolset environment used to execute any subprocesses spawned by the extension.
   */
  private TreeMap<String,String>  pEnvironment; 

}



