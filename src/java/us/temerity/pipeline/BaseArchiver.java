// $Id: BaseArchiver.java,v 1.14 2007/03/28 19:31:03 jim Exp $

package us.temerity.pipeline;

import  us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   A R C H I V E R                                                              */
/*------------------------------------------------------------------------------------------*/

/** 
 * The superclass of all Pipeline archiver plugins. <P>
 * 
 * Archivers are responsible to copying the files associated with checked-in versions to and
 * from some form of offline storage such as tapes, DVDs, worm drives, etc.  See the 
 * {@link #archive archive} and {@link #restore restore} method for details on how this 
 * is accomplished. <P> 
 * 
 * While new plugin subclass versions are being modified and tested the 
 * {@link #underDevelopment underDevelopment} method should be called in the subclasses
 * constructor to enable the plugin to be dynamically reloaded.
 */
public 
class BaseArchiver
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
  BaseArchiver() 
  {
    super();
    
    pParams = new TreeMap<String,ArchiverParam>();
  }

  /** 
   * Construct with the given name, version, vendor and description. 
   * 
   * @param name 
   *   The short name of the archiver.
   * 
   * @param vid
   *   The archiver plugin revision number. 
   * 
   * @param vendor
   *   The name of the plugin vendor.
   * 
   * @param desc 
   *   A short description of the archiver.
   */ 
  protected
  BaseArchiver
  (
   String name, 
   VersionID vid,
   String vendor, 
   String desc
  ) 
  {
    super(name, vid, vendor, desc);

    pParams = new TreeMap<String,ArchiverParam>();
  }

  /**
   * Copy constructor. <P> 
   * 
   * Used internally to create a generic instances of plugin subclasses.  This constructor
   * should not be used in end user code! <P> 
   */ 
  public 
  BaseArchiver
  (
   BaseArchiver archiver
  ) 
  {
    super(archiver.pName, archiver.pVersionID, archiver.pVendor, archiver.pDescription);

    pParams = archiver.pParams;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get which general type of plugin this is. 
   */ 
  public final PluginType
  getPluginType()
  {
    return PluginType.Archiver;
  } 

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add support for execution under the given operating system type.<P> 
   * 
   * This method is disabled because Archiver plugins can only support the default 
   * Unix operating system.
   * 
   * @param os
   *   The operating system type.
   */ 
  protected final void 
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
	("Archiver plugins can only support the default Unix operating system.");
    }
  }

  /**
   * Remove support for execution under the given operating system type.<P> 
   * 
   * This method is disabled because Archiver plugins can only support the default 
   * Unix operating system.
   * 
   * @param os
   *   The operating system type.
   */ 
  protected final void 
  removeSupport
  (
   OsType os
  ) 
  {
    switch(os) {
    case Unix:
      throw new IllegalArgumentException
	("Unix support cannot be removed from Archiver plugins!");

    default:
      super.removeSupport(os);
    }
  }

  /**
   * Copy the OS support flags from the given plugin.<P> 
   * 
   * This method is disabled because Archiver plugins can only support the default 
   * Unix operating system.
   */ 
  protected final void
  setSupports
  (
   SortedSet<OsType> oss
  ) 
  {
    if(oss.contains(OsType.MacOS) || oss.contains(OsType.Windows)) 
      throw new IllegalArgumentException
	("Archiver plugins can only support the default Unix operating system.");

    super.setSupports(oss);
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the archiver requires manual confirmation before initiating an archive or 
   * restore operation. <P> 
   * 
   * Subclasses should override this method to return <CODE>true</CODE> when human 
   * interaction is required to change tapes or other removable media before attempting to 
   * archive or restore files.  Archivers which are capable of performing archive and 
   * restore operation without human intervention are considered automatic and may use the
   * default implementation which returns <CODE>false</CODE>.
   * 
   * If this method returns <CODE>true</CODE>, Pipeline will query the user for confirmation
   * before executing the {@link #archive archive} or {@link #restore restore} methods. <P> 
   * 
   * By default, this method returns <CODE>false</CODE>.
   */ 
  public boolean
  isManual()
  {
    return false;
  }

  /**
   * Get the capacity of the media volume (in bytes). <P> 
   * 
   * Subclasses must override this methods to return the size of the archive media used 
   * by the archiver plugin. <P> 
   * 
   * This size is used assign the versions to be archived to one or more archive volumes 
   * which do not exceed this capacity.  <P> 
   * 
   * By default this method returns (OL). 
   */ 
  public long
  getCapacity()
  {
    return 0L;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the action have any parameters?
   */ 
  public final boolean 
  hasParams()
  {
    return (!pParams.isEmpty());
  }

  /**
   * Add a parameter to this Archiver. <P>
   *
   * This method is used by subclasses in their constructors initialize the set of 
   * parameters that they support.
   *
   * @param param  
   *   The parameter to add.
   */
  protected final void 
  addParam
  (
   ArchiverParam param 
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
  public final Comparable
  getParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    ArchiverParam param = getParam(name); 
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
  public final ArchiverParam
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
   * The returned ArrayList may be empty if the archiver does not have any parameters.
   * 
   * @return 
   *   The set of parameters for this archiver.  
   */ 
  public final Collection<ArchiverParam>
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
  public final void 
  setParamValue
  (
   String name, 
   Comparable value
  ) 
  {
    if(name == null)
      throw new IllegalArgumentException("The parameter name cannot be (null)!");

    ArchiverParam param = pParams.get(name);
    if(param == null) 
      throw new IllegalArgumentException
	("No parameter named (" + param.getName() + ") exists for this archiver!");

    param.setValue(value);
  }

  /** 
   * Copy the values of all of the parameters from the given archiver. <P> 
   * 
   * Note that there is no requirement that the given archiver be the same plugin type or 
   * version.  Any incompatible parameters will simply be ignored by the copy operation.
   * 
   * @param archiver  
   *   The archiver to use as the source of parameter values.
   */
  public final void 
  setParamValues
  (
   BaseArchiver archiver   
  ) 
  {
    for(String name : pParams.keySet()) {
      ArchiverParam aparam = archiver.getParam(name);
      if(aparam != null) {
	ArchiverParam param = pParams.get(name);
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
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the layout of parameters in the user interface. <P> 
   * 
   * The <CODE>layout</CODE> argument must contain the name of each parameter exactly once, 
   * but may also contain <CODE>null</CODE> values.  The order of the parameter names in this 
   * layout list determines that order that the parameters are listed in the user interface.  
   * Extra space will be added between parameters for each <CODE>null</CODE> value 
   * encountered.  In this way parameters can be grouped by inserting <CODE>null</CODE>
   * entries between parameter names. <P> 
   * 
   * This method should be called by subclasses in their constructor after intializing all 
   * parameters with the {@link #addParam addParam} method.
   * 
   * @param layout
   *   The names of the parameters.
   */
  protected final void
  setLayout
  (
   Collection<String> layout
  ) 
  {
    for(String name : layout)
      if((name != null) && !pParams.containsKey(name)) 
	throw new IllegalArgumentException
	  ("There is no parameter (" + name + ") defined for this Archiver!");

    for(String pname : pParams.keySet()) {
      int cnt = 0;
      for(String name : layout) 
	if((name != null) && name.equals(pname)) 
	  cnt++;
      
      switch(cnt) {
      case 0:
	throw new IllegalArgumentException
	  ("The parameter (" + pname + ") was not specified in the layout!");
	
      case 1:
	break;

      default:
	throw new IllegalArgumentException
	  ("The parameter (" + pname + ") was specified (" + cnt + ") times " +
	   "by the layout!  Each parameter may only be specified once.");
      }
    }

    pLayout = new ArrayList<String>(layout);    
  }

  /**
   * Get the layout of parameters in the user interface. <P> 
   * 
   * The returned parameter names will include all single valued parameters exactly 
   * once.  The returned names may also contain <CODE>null</CODE> values, which should
   * be interpreted as delimeters between groupings of parameters.
   * 
   * @return 
   *   The names of each parameter in the order of layout.
   */ 
  public final Collection<String> 
  getLayout() 
  {
    if(pLayout == null) 
      pLayout = new ArrayList<String>(pParams.keySet());
    
    return Collections.unmodifiableCollection(pLayout);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Creates a new archive volume containing the given set of files.  <P> 
   * 
   * Subclasses are responsible for overriding this method to execute the whatever system
   * commands are neccessary to archive the given files. <P> 
   *
   * The archive volume should store the paths of archived files relative to the base 
   * repository directory.  In other words, exactly as given in the <CODE>files</CODE>
   * parameter.  The full path to the repsitory files can be constructed by prepending the 
   * path to the repository directory given by the <CODE>dir</CODE> parameter. <P>
   *
   * This method will be run by the <B>plfilemgr</B>(1) daemon as the Pipeline 
   * administration user.  
   * 
   * @param name 
   *   The name of the archive volume to create.
   * 
   * @param files
   *   The names of the files to archive relative to the base repository directory.
   * 
   * @param env
   *   The cooked toolset environment.
   * 
   * @param dir
   *   The base repository directory.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   * 
   * @return 
   *   The SubProcess which will create the archive volume containing the given files.
   * 
   * @throws PipelineException
   *   If unable to successfully archive all of the given files.
   */  
  public SubProcessHeavy
  archive
  (
   String name, 
   Collection<File> files, 
   Map<String,String> env, 
   File dir, 
   File outFile, 
   File errFile
  ) 
    throws PipelineException
  {
    throw new PipelineException
      ("The archive() method was not implemented by the Archiver (" + pName + ")!");
  }

  /** 
   * Restores the given set of files from an archive volume. <P> 
   * 
   * Subclasses are responsible for overriding this method to execute the whatever system
   * commands are neccessary to restore the given files. <P> 
   * 
   * It is very important that the files are restored relative to the directory given 
   * by the <CODE>dir</CODE> parameter.  Nothing should be assumed about the location of 
   * this directory which is created by Pipeline in a temporary location for each restore 
   * operation.  Once all files have been restores to this temporary location Pipeline 
   * checks the restored files for consistency with their pre-offlined contents using 
   * checksums.  Pipeline also verifies that all files which should have been restored 
   * be the Archiver plugin actually where restored.  Finally, each file is either moved
   * into the repository or symbolic links are created in the repository for the file 
   * depending on the current contents of the repository and the novelty of the file 
   * contents. <P> 
   * 
   * This method will be run by the <B>plfilemgr</B>(1) daemon as the Pipeline 
   * administration user.  
   * 
   * @param name 
   *   The name of the archive volume to restore.
   * 
   * @param stamp
   *   The timestamp (milliseconds since midnight, January 1, 1970 UTC) of the start of 
   *   the restore operation.
   * 
   * @param files
   *   The names of the files to restore relative to the base repository directory.
   * 
   * @param env
   *   The cooked toolset environment.
   * 
   * @param dir
   *   The base repository directory.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   * 
   * @return 
   *   The SubProcess which will restore the given file from the archive volume.
   * 
   * @throws PipelineException
   *   If unable to prepare a SubProcess due to illegal archiver pararameters.
   */  
  public SubProcessHeavy
  restore
  (
   String name, 
   long stamp, 
   Collection<File> files, 
   Map<String,String> env, 
   File dir,
   File outFile, 
   File errFile  
  ) 
    throws PipelineException
  {
    throw new PipelineException
      ("The restore() method was not implemented by the Archiver (" + pName + ")!");
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
  protected final void 
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
  public static final void 
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
   * created by the the archive process. <P> 
   * 
   * @param name 
   *   The name of the archive volume.
   */
  public final Path
  getArchiveTempPath
  (
   String name
  )
  {
    return new Path(PackageInfo.sTempPath, 
		    "plfilemgr/archive/" + name + "/scratch");
  }

  /**
   * Get the root directory used to store temporary files created by the the 
   * archive process. <P> 
   * 
   * @param name 
   *   The name of the archive volume.
   */
  public final File
  getArchiveTempDir
  (
   String name
  )
  {
    return getArchiveTempPath(name).toFile();
  }


  /**
   * Get the abstract pathname of the root directory used to store temporary files created 
   * by the the restore process. <P> 
   * 
   * @param name 
   *   The name of the archive volume.
   * 
   * @param stamp
   *   The timestamp (milliseconds since midnight, January 1, 1970 UTC) of the start of 
   *   the restore operation.
   */
  public final Path
  getRestoreTempPath
  (
   String name, 
   long stamp
  )
  {
    return new Path(PackageInfo.sTempPath, 
                    "plfilemgr/restore/" + name + "-" + stamp + "/scratch");
  }

  /**
   * Get the root directory used to store temporary files created by the the 
   * restore process. <P> 
   * 
   * @param name 
   *   The name of the archive volume.
   * 
   * @param stamp
   *   The timestamp (milliseconds since midnight, January 1, 1970 UTC) of the start of 
   *   the restore operation.
   */
  public final File
  getRestoreTempDir
  (
   String name, 
   long stamp
  )
  {
    return getRestoreTempPath(name, stamp).toFile();
  }


  /** 
   * Create a unique temporary file for the archive process with the given suffix and access 
   * permissions. <P> 
   * 
   * If successful, the temporary file will be added to the set of files which will be 
   * removed upon termination of the Java runtime (see @{link #cleanupLater cleanupLater}).
   * 
   * @param name 
   *   The name of the archive volume.
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
  public final File
  createArchiveTemp
  (
   String name, 
   int mode, 
   String suffix
  ) 
    throws PipelineException 
  {
    File tmp = null;
    try {
      tmp = File.createTempFile(pName + "-", "." + suffix, 
				getArchiveTempDir(name));
      chmod(mode, tmp);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to create temporary file for the archive of (" + name + "):\n\n" + 
	 ex.getMessage());
    }
    
    cleanupLater(tmp);
    
    return tmp;
  }

  /** 
   * Create a unique temporary file for the restore process with the given suffix and access 
   * permissions. <P> 
   * 
   * If successful, the temporary file will be added to the set of files which will be 
   * removed upon termination of the Java runtime (see @{link #cleanupLater cleanupLater}).
   * 
   * @param name 
   *   The name of the archive volume.
   * 
   * @param stamp
   *   The timestamp (milliseconds since midnight, January 1, 1970 UTC) of the start of 
   *   the restore operation.
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
  public final File
  createRestoreTemp
  (
   String name, 
   long stamp,  
   int mode, 
   String suffix
  ) 
    throws PipelineException 
  {
    File tmp = null;
    try {
      tmp = File.createTempFile(pName + "-", "." + suffix, getRestoreTempDir(name, stamp));
      chmod(mode, tmp);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to create temporary file for the restore of (" + name + "):\n\n" + 
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
  public final boolean
  equals
  (
   Object obj
  )
  {
    if((obj != null) && (obj instanceof BaseArchiver)) {
      BaseArchiver archiver = (BaseArchiver) obj;
      if(super.equals(obj) && 
	 equalParams(archiver)) 
	return true;
    }

    return false;
  }

  /**
   * Indicates whether the parameters of the given archiver equal to this archiver's 
   * parameters.
   */ 
  public final boolean
  equalParams
  (
   BaseArchiver archiver
  )
  {
    return pParams.equals(archiver.pParams);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public final Object 
  clone()
  {
    BaseArchiver clone = (BaseArchiver) super.clone();
    
    clone.pParams = new TreeMap<String,ArchiverParam>();
    for(ArchiverParam param : pParams.values()) {
      ArchiverParam pclone = (ArchiverParam) param.clone();
      clone.pParams.put(pclone.getName(), pclone);
    }

    return clone;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final void 
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
  
  public final void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    TreeMap<String,ArchiverParam> single = 
      (TreeMap<String,ArchiverParam>) decoder.decode("Params");   
    if(single != null) {
      for(ArchiverParam param : single.values()) 
	pParams.put(param.getName(), param); 
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2507783131617402235L;


 
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The table of archiver parameters.
   */
  private TreeMap<String,ArchiverParam>  pParams;    

  /**
   * Used to determing the order and grouping of parameters in the graphical user interface. 
   */ 
  private ArrayList<String>  pLayout;

}



