// $Id: BaseArchiver.java,v 1.1 2004/11/16 03:56:36 jim Exp $

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
 * from some form of offline storage such as tapes, DVDs, worm drives, etc.
 */
public 
class BaseArchiver
  extends BasePlugin
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  protected
  BaseArchiver() 
  {
    super();
    
    pParams = new TreeMap<String,ArchiverParam>();
  }

  /** 
   * Construct with the given name, version and description. 
   * 
   * @param name 
   *   The short name of the archiver.
   * 
   * @param vid
   *   The archiver plugin revision number. 
   * 
   * @param desc 
   *   A short description of the archiver.
   */ 
  protected
  BaseArchiver
  (
   String name, 
   VersionID vid,
   String desc
  ) 
  {
    super(name, vid, desc);

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
    super(archiver.pName, archiver.pVersionID, archiver.pDescription);

    pParams = archiver.pParams;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the archiver requires manual confirmation before initiating an archive or 
   * restore operation. <P> 
   * 
   * Subclasses should override this method specify whether the archiver plugin is manual
   * or automatic. <P> 
   * 
   * This method should return <CODE>true</CODE> when human interaction is required to 
   * change tapes or other removable media before attempting to archive or restore files.  
   * Archivers which are capable of performing archive and restore operation without human 
   * intervention are considered automatic and should return <CODE>false</CODE>. <P> 
   * 
   * By default, this method returns <CODE>true</CODE>.
   */ 
  public boolean
  isManual()
  {
    return true;
  }

  /**
   * Get the capacity of the media (in bytes). <P> 
   * 
   * Subclasses should override this methods to return the size of the archive media used 
   * by the archiver plugin. <P> 
   * 
   * By default this method returns (0L).
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
  public boolean 
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
  protected void 
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
  public Comparable
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
  public ArchiverParam
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
  public Collection<ArchiverParam>
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
  public void 
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
	  Logs.ops.warning(ex.getMessage());
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
  protected void
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
  public Collection<String> 
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
   * Archives the given set of files.  <P> 
   * 
   * Subclasses are responsible for overriding this method to execute the whatever system
   * commands are neccessary to archive the given files. <P> 
   * 
   * This method will be run by the <B>plfilemgr</B>(1) daemon as the Pipeline 
   * administration user.  
   * 
   * @param name 
   *   The name of the backup.
   * 
   * @param files
   *   The names of the files to archive relative to the base production directory.
   * 
   * @param dir
   *   The base production directory.
   * 
   * @throws PipelineException
   *   If unable to successfully archive all of the given files.
   */  
  public void 
  archive
  (
   String name, 
   Collection<File> files, 
   File dir
  ) 
    throws PipelineException
  {
    throw new PipelineException
      ("The archive() method was not implemented by the Archiver (" + pName + ")!");
  }

  /** 
   * Restores the given set of files. <P> 
   * 
   * Subclasses are responsible for overriding this method to execute the whatever system
   * commands are neccessary to restore the given files. <P> 
   * 
   * This method will be run by the <B>plfilemgr</B>(1) daemon as the Pipeline 
   * administration user.  
   * 
   * @param name 
   *   The name of the backup.
   * 
   * @param files
   *   The names of the files to restore relative to the base production directory.
   * 
   * @param dir
   *   The base production directory.
   * 
   * @throws PipelineException
   *   If unable to successfully restore all of the given files.
   */  
  public void 
  restore
  (
   String name, 
   Collection<File> files, 
   File dir   
  ) 
    throws PipelineException
  {
    throw new PipelineException
      ("The restore() method was not implemented by the Archiver (" + pName + ")!");
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
  public boolean
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
  public Object 
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

    TreeMap<String,ArchiverParam> single = 
      (TreeMap<String,ArchiverParam>) decoder.decode("Params");   
    if(single != null) 
      pParams.putAll(single);
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



