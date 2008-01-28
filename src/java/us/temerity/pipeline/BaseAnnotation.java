// $Id: BaseAnnotation.java,v 1.3 2008/01/28 12:06:09 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   A N N O T A T I O N                                                          */
/*------------------------------------------------------------------------------------------*/

/** 
 * The superclass of all Pipeline node annotation plugins. <P>
 * 
 * Annotations are collections of arbitrary parameters globally associated with all 
 * checked-in and working versions of a node.  This means that annotations are independent
 * of node versions and modifications to annotations do not affect node status in any way.<P> 
 * 
 * A node may have zero or more annotations, each with possibly more than one parameter.
 * Annotation plugins are responsible for defining the names, types and descriptions of the
 * meanings of a collection of related annotation parameters.  Annotation plugins can only
 * be added or removed from a node by a user with Annotator privileges, but each plugin may 
 * specify its own parameter modification policies.<P> 
 * 
 * New kinds of annotations can be written by subclassing this class.  Due to the way plugins
 * are loaded and communicated between applications, any fields added to a subclass will
 * be reinitialized when the annotation is stored to disk or when it is sent over the 
 * network.<P>
 * 
 * While new plugin subclass versions are being modified and tested the 
 * {@link #underDevelopment() underDevelopment} method should be called in the subclasses
 * constructor to enable the plugin to be dynamically reloaded.
 */
public
class BaseAnnotation
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
  BaseAnnotation() 
  {
    super();
    pParams = new TreeMap<String,AnnotationParam>();
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
  BaseAnnotation
  (
   String name, 
   VersionID vid,
   String vendor, 
   String desc
  ) 
  {
    super(name, vid, vendor, desc);
    pParams = new TreeMap<String,AnnotationParam>();
  }

  /**
   * Copy constructor. <P> 
   * 
   * Used internally to create a generic instances of plugin subclasses.  This constructor
   * should not be used in end user code! <P> 
   */ 
  public 
  BaseAnnotation
  (
   BaseAnnotation annot
  ) 
  {
    super(annot.pName, annot.pVersionID, annot.pVendor, annot.pDescription);
    pParams = annot.pParams;
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
    return PluginType.Annotation;
  }
    

  /*----------------------------------------------------------------------------------------*/

  /**
   * Add support for execution under the given operating system type.<P> 
   * 
   * This method is disabled because Annotation plugins can only support the default 
   * Unix operating system.
   * 
   * @param os
   *   The operating system type.
   */ 
  @Override
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
	("Annotation plugins can only support the default Unix operating system.");
    }
  }

  /**
   * Remove support for execution under the given operating system type.<P> 
   * 
   * This method is disabled because Annotation plugins can only support the default 
   * Unix operating system.
   * 
   * @param os
   *   The operating system type.
   */ 
  @Override
  protected final void 
  removeSupport
  (
   OsType os
  ) 
  {
    switch(os) {
    case Unix:
      throw new IllegalArgumentException
	("Unix support cannot be removed from Annotation plugins!");

    default:
      super.removeSupport(os);
    }
  }

  /**
   * Copy the OS support flags from the given plugin.<P> 
   * 
   * This method is disabled because Annotation plugins can only support the default 
   * Unix operating system.
   */ 
  @Override
  protected final void
  setSupports
  (
   SortedSet<OsType> oss
  ) 
  {
    if(oss.contains(OsType.MacOS) || oss.contains(OsType.Windows)) 
      throw new IllegalArgumentException
	("Annotation plugins can only support the default Unix operating system.");

    super.setSupports(oss);
  }

  /**
   * Is the ability to add this Annotation to a node open to all users?
   * <p>
   * By default, only users with the Annotator privilege can add new Annotations to nodes. If
   * it is desirable to have any user able to add this Annotation to a node, this method
   * should be overridden to return <code>true</code>.
   * 
   * @return <code>true</code> if any user can add this annotation or <code>false</code>
   *         if only users with the Annotator privilege can add this annotation.
   */
  public boolean
  isUserAddable()
  {
    return false;
  }
  
  /**
   * Is the ability to remove this Annotation to a node open to all users?
   * <p>
   * By default, only users with the Annotator privilege can remove Annotations from nodes. If
   * it is desirable to have any user able to remove this Annotation from a node, this method
   * should be overridden to return <code>true</code>.
   * 
   * @return <code>true</code> if any user can remove this annotation or <code>false</code>
   *         if only users with the Annotator privilege can remove this annotation.
   */
  public boolean
  isUserRemovable()
  {
    return false;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P A R A M E T E R S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Add a parameter to this Annotation. <P>
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
   AnnotationParam param 
  ) 
  {
    if(pParams.containsKey(param.getName())) 
      throw new IllegalArgumentException
	("A parameter named (" + param.getName() + ") already exists!");

    pParams.put(param.getName(), param); 
  }


  /*----------------------------------------------------------------------------------------*/

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
    AnnotationParam param = getParam(name); 
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
  public final AnnotationParam
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
   * The returned ArrayList may be empty if the annotation does not have any parameters.
   * 
   * @return 
   *   The set of parameters for this annotation.  
   */ 
  public final Collection<AnnotationParam>
  getParams()
  {
    return Collections.unmodifiableCollection(pParams.values());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether a given user is allowed to modify a specific annotation parameter. <P> 
   * 
   * The default implementation only grants users with Annotator privileges the right to 
   * modify a parameter, but subclasses may override this method to implement their own 
   * more permissive modification policy.  Note that users with Annotator privileges will 
   * always be able to modify annoation parameters even if a subclass overrides this method 
   * to always return <CODE>false</CODE>.
   * 
   * @param pname  
   *   The name of the parameter. 
   * 
   * @param user
   *   The name of the user requesting access to modify the parameter.
   * 
   * @param privileges
   *   The details of the administrative privileges granted to the user. 
   */ 
  public boolean
  isParamModifiable
  (
   String pname,
   String user, 
   PrivilegeDetails privileges
  )
  {
    return privileges.isAnnotator();
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

    AnnotationParam param = pParams.get(name);
    if(param == null) 
      throw new IllegalArgumentException
	("No parameter named (" + param.getName() + ") exists for this annotation!");

    param.setValue(value);
  }

  /** 
   * Copy the values of all of the parameters from the given annotation. <P> 
   * 
   * Note that there is no requirement that the given annotation be the same plugin type or 
   * version.  Any incompatible parameters will simply be ignored by the copy operation.
   * 
   * @param annotation  
   *   The annotation to use as the source of parameter values.
   */
  public final void 
  setParamValues
  (
   BaseAnnotation annotation   
  ) 
  {
    for(String name : pParams.keySet()) {
      AnnotationParam aparam = annotation.getParam(name);
      if(aparam != null) {
	AnnotationParam param = pParams.get(name);
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

  /** 
   * Copy the values of all of the parameters from the given annotation, checking access
   * privileges for each parameter.<P> 
   * 
   * Note that there is no requirement that the given annotation be the same plugin type or 
   * version.  Any incompatible parameters will simply be ignored by the copy operation. <P> 
   * 
   * The {@link BaseAnnotation#isParamModifiable BaseAnnotation.isParamModifiable} method
   * of this annotation will be used to determine whether the user has permission to modify
   * each parameter.  Only parameters with values not identical to those in this
   * annotation will perform the access test.  Identical values will simply be ignored.
   * 
   * @param annotation  
   *   The annotation to use as the source of parameter values.
   * 
   * @param user
   *   The name of the user requesting access to modify the parameter.
   * 
   * @param privileges
   *   The details of the administrative privileges granted to the user. 
   * 
   * @throws PipelineException
   *   If an attempt to modify a parameter for which the user does not have sufficient
   *   privileges.
   */
  public final void 
  setParamValues
  (
   BaseAnnotation annotation,
   String user, 
   PrivilegeDetails privileges
  ) 
    throws PipelineException
  {
    for(String pname : pParams.keySet()) {
      AnnotationParam aparam = annotation.getParam(pname);
      if(aparam != null) {
	AnnotationParam param = pParams.get(pname);
	try {
          Comparable ovalue = param.getValue(); 
          Comparable nvalue = aparam.getValue();          
          if(((ovalue == null) && (nvalue != null)) ||  
             ((ovalue != null) && !ovalue.equals(nvalue))) {

            if(!isParamModifiable(pname, user, privileges) && !privileges.isAnnotator()) 
              throw new PipelineException
                ("You do not have sufficient privileges to modify the parameter named " + 
                 "(" + pname + ") for the annotation (" + getName() + ")!"); 

            param.setValue(nvalue); 
          }
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
	  ("There is no parameter (" + name + ") defined for this Annotation!");

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
    if((obj != null) && (obj instanceof BaseAnnotation)) {
      BaseAnnotation annotation = (BaseAnnotation) obj;
      if(super.equals(obj) && 
	 equalParams(annotation)) 
	return true;
    }

    return false;
  }

  /**
   * Indicates whether the parameters of the given annotation equal to this annotation's 
   * parameters.
   */ 
  public final boolean
  equalParams
  (
   BaseAnnotation annotation
  )
  {
    return pParams.equals(annotation.pParams);
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
    BaseAnnotation clone = (BaseAnnotation) super.clone();
    
    clone.pParams = new TreeMap<String,AnnotationParam>();
    for(AnnotationParam param : pParams.values()) {
      AnnotationParam pclone = (AnnotationParam) param.clone();
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

    TreeMap<String,AnnotationParam> single = 
      (TreeMap<String,AnnotationParam>) decoder.decode("Params");   
    if(single != null) {
      for(AnnotationParam param : single.values()) 
	pParams.put(param.getName(), param); 
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7114310224774654224L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The table of annotation parameters.
   */
  private TreeMap<String,AnnotationParam>  pParams;    

  /**
   * Used to determine the order and grouping of parameters in the graphical user interface. 
   */ 
  private ArrayList<String>  pLayout;
  
}



