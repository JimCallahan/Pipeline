// $Id: BaseAction.java,v 1.50 2009/10/30 18:58:29 jim Exp $

package us.temerity.pipeline;

import  us.temerity.pipeline.glue.*;
import  us.temerity.pipeline.plugin.CommonActionUtils;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   T H U M B N A I L                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The superclass of all Pipeline thumbnail generation plugins. <P>
 */
public 
class BaseThumbnail 
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
  BaseThumbnail() 
  {
    super();

    pParams = new TreeMap<String,ThumbnailParam>();
    pEnvironment = new TreeMap<String,String>();
    pAnnotations = new TreeMap<String,BaseAnnotation>();
  }

  /** 
   * Construct with the given name, version, vendor and description. 
   * 
   * @param name 
   *   The short name of the editor.
   * 
   * @param vid
   *   The editor plugin revision number. 
   * 
   * @param vendor
   *   The name of the plugin vendor.
   * 
   * @param desc 
   *   A short description of the editor.
   */ 
  protected
  BaseThumbnail
  (
   String name, 
   VersionID vid,
   String vendor, 
   String desc
  ) 
  {
    super(name, vid, vendor, desc);

    pParams = new TreeMap<String,ThumbnailParam>();
    pEnvironment = new TreeMap<String,String>();
    pAnnotations = new TreeMap<String,BaseAnnotation>();
  }

  /**
   * Copy constructor. <P> 
   * 
   * Used internally to create a generic instances of plugin subclasses.  This constructor
   * should not be used in end user code! <P> 
   */ 
  public 
  BaseThumbnail
  (
   BaseThumbnail thumbnail
  ) 
  {
    super(thumbnail.pName, thumbnail.pVersionID, thumbnail.pVendor, thumbnail.pDescription);

    pParams = thumbnail.pParams;
    pEnvironment = thumbnail.pEnvironment;
    pAnnotations = thumbnail.pAnnotations; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get which general type of plugin this is. 
   */ 
  @Override
  public final PluginType
  getPluginType()
  {
    return PluginType.Thumbnail;
  }
    
 
  
  /*----------------------------------------------------------------------------------------*/
  /*   P A R A M E T E R S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Add a parameter to this Thumbnail. <P>
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
   ThumbnailParam param 
  ) 
  {
    if(pParams.containsKey(param.getName())) 
      throw new IllegalArgumentException
	("A parameter named (" + param.getName() + ") already exists!");

    pParams.put(param.getName(), param); 
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Whether this thumbnail has any parameters.
   */ 
  public final boolean
  hasParams() 
  {
    return (!pParams.isEmpty());
  }
  
  /** 
   * Whether this thumbnail has a parameter with the specified name.
   * 
   * @param name
   *   The name of the parameter.
   */
  public final boolean
  hasParam
  (
   String name  
  )
  {
    return pParams.containsKey(name);
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
    ThumbnailParam param = getParam(name); 
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
  public final ThumbnailParam
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
   * The returned ArrayList may be empty if the thumbnail does not have any parameters.
   * 
   * @return 
   *   The set of parameters for this thumbnail.  
   */ 
  public final Collection<ThumbnailParam>
  getParams()
  {
    return Collections.unmodifiableCollection(pParams.values());
  }


  /*----------------------------------------------------------------------------------------*/

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

    ThumbnailParam param = pParams.get(name);
    if(param == null) 
      throw new IllegalArgumentException
	("No parameter named (" + param.getName() + ") exists for this thumbnail!");

    try {
      param.setValue(value);
    }
    catch(IllegalArgumentException ex) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Warning,
         "While attempting to set a parameter of the Thumbnail " +
         "(" + pName + " v" + pVersionID + ") from vendor (" + pVendor + "):\n  " + 
         ex.getMessage());
    }
  }

  /** 
   * Copy the values of all of the parameters from the given thumbnail. <P> 
   * 
   * Note that there is no requirement that the given thumbnail be the same plugin type or 
   * version.  Any incompatible parameters will simply be ignored by the copy operation.
   * 
   * @param thumbnail  
   *   The thumbnail to use as the source of parameter values.
   */
  public final void 
  setParamValues
  (
   BaseThumbnail thumbnail   
  ) 
  {
    for(String name : pParams.keySet()) {
      ThumbnailParam aparam = thumbnail.getParam(name);
      if(aparam != null) {
        ThumbnailParam param = pParams.get(name);
        try {
          param.setValue(aparam.getValue());
        }
        catch(IllegalArgumentException ex) {
          LogMgr.getInstance().log
            (LogMgr.Kind.Ops, LogMgr.Level.Warning,
             "While attempting to set a parameter of the Thumbnail " +
             "(" + pName + " v" + pVersionID + ") from vendor (" + pVendor + "):\n  " + 
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
	  ("There is no parameter (" + name + ") defined for this Thumbnail!");

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
  /*   T O O L S E T S                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether an subprocesses execution environment is requird by this thumbnail.
   */
  public boolean
  needsEnvironment()
    throws PipelineException
  {
    return false; 
  }

  /**
   * Sets the environment under which any subprocesses spawned by the thumbnail are 
   * executed.<P>
   * 
   * If the {@link #needsEnvironment} method returns <CODE>true</CODE>, a pre-cooked toolset
   * environment will be passed to this method before calling any of the thumbnail
   * generation methods. 
   * 
   * @param env
   *   The cooked toolset environment.
   */ 
  public final void 
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
   * Get the environment under which any subprocesses spawned by the thumbnail are 
   * executed.<P>
   * 
   * If you will be calling this method, make sure to override the {@link #needsEnvironment}
   * method to return <CODE>true</CODE> so that the environment will be pre-cooked before
   * calling any of the thumbnail generation methods. Otherwise, the environment returned by 
   * this method will be empty.
   * 
   * @throws PipelineException
   *   If the current operating system is not supported by the toolset.
   */ 
  public final SortedMap<String,String>
  getEnvironment()
    throws PipelineException
  {
    return pEnvironment; 
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   T H U M B N A I L   M G R   C L I E N T                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sets the {@link ThumbnailMgrClient} used to register and lookup thumbnail images. 
   * 
   * @param client
   *   The thumbnail manager client interface. 
   */ 
  private void 
  setThumbnailMgrClient
  (
   ThumbnailMgrClient client
  ) 
  {
    pThumbnailMgrClient = client; 
  }

  /**
   * Get the {@link ThumbnailMgrLightClient} used to register and lookup thumbnail images. 
   * 
   * @return 
   *   The client or <CODE>null</CODE> if unavailable.
   */ 
  public ThumbnailMgrClient
  getThumbnailMgrClient()
  {
    return pThumbnailMgrClient; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   I N F O R M A T I O N                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Initializes all data known to the thumbnail about the underlying node.<P> 
   * 
   * This method is called before any of the thumbnail generate methods which may access 
   * this information using the {@link #getNodeName getNodeName()}, 
   * {@link #getNodeID getNodeID()}, 
   * {@link #getNodeVersion getNodeVersion()}, 
   * {@link #getWorkingVersion getWorkingVersion()}, 
   * {@link #getLatestVersion getLatestVersion()} and 
   * {@link #getAnnotations getAnnotations()} methods.<P> 
   * 
   * @param nodeID
   *   The unique working version identifier or <CODE>null</CODE> if the node has not been 
   *   checked-out. 
   * 
   * @param work
   *   The working version of the node or <CODE>null</CODE> if the node has not been 
   *   checked-out. 
   * 
   * @param latest    
   *   The latest checked-in version of the node or <CODE>null</CODE> if this is an 
   *   initial working version which has never been checked-in. 
   *
   * @param annotations
   *   The table of node annotation plugin instances indexed by annotation name or
   *   <CODE>null</CODE> if there are no annotations.
   */ 
  public final void
  setNodeInformation
  (
   NodeID nodeID, 
   NodeMod work, 
   NodeVersion latest, 
   TreeMap<String,BaseAnnotation> annotations
  ) 
  {
    if(work != null) 
      pNodeVersion = work;
    else if(latest != null) 
      pNodeVersion = latest; 
    else 
      throw new IllegalArgumentException
        ("At least one of the working or latest node versions cannot be (null)!"); 

    pNodeID = nodeID; 
    pWorkingVersion = work;
    pLatestVersion = latest; 
    
    if(annotations != null) 
      pAnnotations = annotations;

    pIsInitialized = true;
  }

  /**
   * Whether the node information has been initialized by a previous call of the  
   * {@link #setNodeInformation setNodeInformation()} method.
   */ 
  public final boolean
  isInitialized() 
  {
    return pIsInitialized;
  }

  /**
   * Make sure the node information has been initialized by a previous call of the  
   * {@link #setNodeInformation setNodeInformation()} method.
   */ 
  private final void 
  checkInitialized() 
  {
    if(!pIsInitialized) 
      throw new IllegalStateException
        ("The node information cannot be accessed before its been initialized!"); 
  }

  /**
   * Gets the name of the node.
   */
  public final String
  getNodeName() 
  {
    checkInitialized();
    return pNodeVersion.getName();
  }

  /**
   * Gets the unique working version identifier or <CODE>null</CODE> if the node has not been 
   * checked-out. 
   */
  public final NodeID
  getNodeID() 
  {
    checkInitialized();
    return pNodeID;
  }

  /**
   * Get the common node properties of from the working version or latest checked-in version 
   * (if no working version exists). <P> 
   * 
   * This is a convenience for accessing shared node properties which don't depend on whether
   * the node is checked-in or not.
   * 
   * @return
   *   The node version. 
   */ 
  public final NodeCommon
  getNodeVersion()
  {
    checkInitialized();
    return pNodeVersion;
  }

  /**
   * Get the working version of the node.
   * 
   * @return
   *   The working version or <CODE>null</CODE> if none exists.
   */ 
  public final NodeMod
  getWorkingVersion()
  {
    checkInitialized();
    return pWorkingVersion;
  }

  /**
   * Get the latest checked-in version of the node.
   * 
   * @return
   *   The latest version or <CODE>null</CODE> if none exists.
   */ 
  public final NodeVersion
  getLatestVersion()
  {
    checkInitialized();
    return pLatestVersion;
  }

  /**
   * Get the table of node annotation plugin instances indexed by annotation name. 
   * 
   * @return
   *   The annotations (may be empty).
   */ 
  public final SortedMap<String,BaseAnnotation>
  getAnnotations()
  {
    checkInitialized();
    return Collections.unmodifiableSortedMap(pAnnotations); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*    T H U M B N A I L   G E N E R A T I O N                                             */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether this thumbnail will execute a subprocess in order to generate the target
   * thumbnail image.<P> 
   * 
   * This method should be overriden by subclasses to return <CODE>true</CODE> if an 
   * implementation of the {@link #prep prep()} method is provided.
   */ 
  public boolean
  hasPrep() 
  {
    return false; 
  }

  /**
   * Construct a {@link SubProcessHeavy} instance which when executed will generate the
   * target thumbnail image.<P> 
   * 
   * @param target
   *   The absolute file sytem path to the thumbnail file to generate.
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
   *   If unable to prepare a SubProcess.
   */
  public SubProcessHeavy
  prep
  (
   File target, 
   File outFile, 
   File errFile 
  )
    throws PipelineException
  {
    throw new PipelineException
      ("The prep() method was not implemented!");
  }
  
  /**
   * Determine the appropriate thumbnail for the current node.<P> 
   * 
   * This may simply involve looking up a previously generate thumbnail image's checksum, 
   * but may include registering the target image if it hasn't already been registered.
   * The image itself might have been generated by running the subprocess created by
   * the {@link #prep prep()} method or could be created by this method using Java only.<P> 
   * 
   * A return value of <CODE>null</CODE> from this method indicates that this plugin
   * was unable to determine the thumbnail image to use for the node and that the next
   * thumbnail plugin should be tried.<P> 
   * 
   * The default implementation of this method will register and return the MD5 checksum
   * of <CODE>target<CODE> image file, if {@ #hasPrep hasPrep()} is <CODE>true</CODE>.  
   * Otherwise it will do nothing and return <CODE>null</CODE>.  Subclasses should override
   * this method if they intend to make use of predefined icon images or generate the 
   * target image using Java only.<P> 
   * 
   * @param target
   *   The absolute file sytem path to the thumbnail file generated in the
   *   {@link #prep prep()} method or <CODE>null</CODE> if {@link #hasPrep hasPrep()} 
   *   returns <CODE>false</CODE>.
   * 
   * @return 
   *   The MD5 checksum of the registered thumbnail image for the node or 
   *   <CODE>null</CODE> if this plugin not capable of determining the thumbnail image.
   *
   * @throws PipelineException 
   *   If a failure occurs while trying to determine the thumbnail. 
   */ 
  public byte[]
  resolve 
  (
   File target   
  ) 
    throws PipelineException
  {
    if(hasPrep()) {
      try {
        return pThumbnailMgrClient.registerImage(new ThumbnailImage(target));
      }
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to load the target thumbnail image file (" + target + ")!"); 
      }
    }

    return null;
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
  @Override
  public final boolean
  equals
  (
   Object obj
  )
  {
    if((obj != null) && (obj instanceof BaseThumbnail)) {
      BaseThumbnail thumbnail = (BaseThumbnail) obj;
      if(super.equals(obj) && 
	 equalParams(thumbnail)) 
	return true;
    }

    return false;
  }

  /**
   * Indicates whether the parameters of the given thumbnail equal to this thumbnail's 
   * parameters.
   */ 
  public final boolean
  equalParams
  (
   BaseThumbnail thumbnail
  )
  {
    return pParams.equals(thumbnail.pParams);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  @Override
  public final Object 
  clone()
  {
    BaseThumbnail clone = (BaseThumbnail) super.clone();
    
    clone.pParams = new TreeMap<String,ThumbnailParam>();
    for(ThumbnailParam param : pParams.values()) {
      ThumbnailParam pclone = (ThumbnailParam) param.clone();
      clone.pParams.put(pclone.getName(), pclone);
    }

    return clone;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
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
  
  @Override
  public final void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    TreeMap<String,ThumbnailParam> params = 
      (TreeMap<String,ThumbnailParam>) decoder.decode("Params");   
    if(params != null) {
      for(ThumbnailParam param : params.values()) 
        pParams.put(param.getName(), param); 
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5255864747024338546L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The table of thumbnail parameters.
   */
  private TreeMap<String,ThumbnailParam>  pParams;    

  /**
   * Used to determine the order and grouping of parameters in the graphical user interface. 
   */ 
  private ArrayList<String>  pLayout;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The cached toolset environment used to execute any subprocesses spawned by the extension.
   */
  private TreeMap<String,String>  pEnvironment;
  
  /** 
   * The client instance used to register and lookup thumbnail images.
   */
  private ThumbnailMgrClient  pThumbnailMgrClient; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the node information has been initialized by a previous call of the  
   * {@link #setNodeInformation setNodeInformation()} method.
   */ 
  private boolean pIsInitialized; 
  
  /**
   * The unique working version identifier or <CODE>null</CODE> if the node has not been 
   * checked-out. 
   */ 
  private NodeID  pNodeID; 

  /**
   * The common node properties of from the working version or latest checked-in version 
   * (if no working version exists). 
   */ 
  private NodeCommon  pNodeVersion;
  
  /**
   * The working version of the node or <CODE>null</CODE> if none exists.
   */ 
  private NodeMod  pWorkingVersion;
  
  /**
   * The latest checked-in version of the node or <CODE>null</CODE> if none exists.
   */ 
  private NodeVersion  pLatestVersion;
  
  /**
   * The table of node annotation plugin instances indexed by annotation name. 
   */ 
  private TreeMap<String,BaseAnnotation>  pAnnotations; 

}


