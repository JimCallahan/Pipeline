// $Id: BaseKeyChooser.java,v 1.3 2007/12/16 12:22:09 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.param.key.KeyParam;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   S E L E C T I O N   K E Y                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The superclass of all Pipeline key chooser plugins.
 * <P>
 * The plugins are used by Pipeline to determine which selection/hardware/license keys a node
 * will turn on when it is submitted as a job to the queue. See the sections on
 * {@link JobReqs} and {@link SelectionKey SelectionKeys} / {@link HardwareKey HardwareKeys} /
 * {@link LicenseKey LicenseKeys} for more information on how the different parts of the
 * queue dispatch process work work.
 * <P>
 * 
 * New kinds of key choosers can be written by subclassing this class. Due to the way
 * plugins are loaded and communicated between applications, any fields added to a subclass
 * will be reinitialized when the key is stored to disk or when it is sent over the network.
 * Any data which must be retained by the key should be stored in a key parameter instead.
 * <P>
 * 
 * While new plugin subclass versions are being modified and tested the
 * {@link #underDevelopment() underDevelopment} method should be called in the subclasses
 * constructor to enable the plugin to be dynamically reloaded.
 */
public 
class BaseKeyChooser
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
  BaseKeyChooser() 
  {
    super();

    pParams = new TreeMap<String, KeyParam>();
  }
  
  /** 
   * Construct with the given name, version, vendor and description. 
   * 
   * @param name 
   *   The short name of the selection key.  
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
  BaseKeyChooser
  (
   String name,  
   VersionID vid,
   String vendor, 
   String desc
  ) 
  {
    super(name, vid, vendor, desc);

    pParams = new TreeMap<String, KeyParam>();
  }
  
  /**
   * Copy constructor. 
   * <P> 
   * Used internally to create a generic instances of plugin subclasses.  This constructor
   * should not be used in end user code! 
   * <P> 
   * @param key
   *   The source to copy from 
   */ 
  public 
  BaseKeyChooser
  (
    BaseKeyChooser key
  ) 
  {
    super(key.pName, key.pVersionID, key.pVendor, key.pDescription);

    pParams = key.pParams;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O M P U T A T I O N                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a value indicating whether the given node meets the criteria for this key.
   * <P> 
   * @param job
   *   The QueueJob that the key is going to apply to.  This contains the BaseAction and
   *   the ActionAgenda that can be mined for information.
   *   
   * @param annots
   *   The list of annotations assigned to the node the job is being created for.
   * 
   * @return 
   *   Whether this key is active for the job being created by the given node.
   * 
   * @throws PipelineException 
   *   If unable to return a value due to illegal, missing or incompatible 
   *   information in the node information or a general failure of the isActive method code.
   */
  public boolean
  isActive
  (
    QueueJob job,
    TreeMap <String, BaseAnnotation> annots 
  )
    throws PipelineException
  {
    throw new PipelineException
      ("The isActive() method was not implemented by the KeyChooser (" + pName + ")!");
  }
  
  /**
   * Actual method that is called by the servers that provides exception wrapping
   * and handling. 
   * <p>
   * @param job
   *   The QueueJob that the key is going to apply to.  This contains the BaseAction and
   *   the ActionAgenda that can be mined for information.
   *   
   * @param annots
   *   The list of annotations assigned to the node the job is being created for.
   * 
   * @return 
   *   Whether this key is active for the job being created by the given node.
   * 
   * @throws PipelineException if any error occurs while running 
   *   {@link #isActive(QueueJob, TreeMap)}
   */
  public final boolean
  computeIsActive
  (
    QueueJob job,
    TreeMap <String, BaseAnnotation> annots 
  )
    throws PipelineException
  {
    try {
      return isActive(job, annots);  
    }
    catch (Exception e) {
      String msg = "An error occured in KeyChooser (" + getName() + ")  " +
      		   "version (" + getVersionID().toString() + "), " +
      		   "provided by (" + getVendor() + ").  \n" + e.getMessage();
      if (e instanceof PipelineException)
        LogMgr.getInstance().logAndFlush(Kind.Ops, Level.Warning, msg);
      else
        LogMgr.getInstance().logAndFlush(Kind.Ops, Level.Severe, msg);
      throw new PipelineException(msg);
    }
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
    return PluginType.KeyChooser;
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add support for execution under the given operating system type.<P> 
   * 
   * This method is disabled because Key Chooser plugins can only support the default 
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
        ("Selection Key plugins can only support the default Unix operating system.");
    }
  }

  /**
   * Remove support for execution under the given operating system type.<P> 
   * 
   * This method is disabled because Key Chooser plugins can only support the default 
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
        ("Unix support cannot be removed from Selection Key plugins!");

    default:
      super.removeSupport(os);
    }
  }

  /**
   * Copy the OS support flags from the given plugin.<P> 
   * 
   * This method is disabled because Selection Key plugins can only support the default 
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
        ("Selection Key plugins can only support the default Unix operating system.");

    super.setSupports(oss);
  }

  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P A R A M E T E R S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does the Key Chooser have any parameters?
   */ 
  public final boolean 
  hasParams()
  {
    return (!pParams.isEmpty());
  }

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Add a parameter to this Key Chooser. <P>
   *
   * This method is used by subclasses in their constructors to initialize the set of 
   * parameters that they support.
   *
   * @param param  
   *   The parameter to add.
   */
  protected final void 
  addParam
  (
    KeyParam param 
  ) 
  {
    String name = param.getName();
    
    if(pParams.containsKey(name)) 
      throw new IllegalArgumentException
        ("A parameter named (" + name + ") already exists!");

    pParams.put(name, param); 
  }
  

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get all of the parameters. <P> 
   * 
   * The returned ArrayList may be empty if the Key Chooser does not have any single 
   * valued parameters.
   * 
   * @return 
   *   The set of parameters for this Key Chooser.  
   */ 
  public final Collection<KeyParam>
  getParams()
  {
    return Collections.unmodifiableCollection(pParams.values());
  }  

  /** 
   * Get the parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The key parameter or <CODE>null</CODE> if no parameter with the given name exists.
   */ 
  public final KeyParam
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
   * Get the value of the parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The key parameter value.
   * 
   * @throws PipelineException 
   *   If no parameter with the given name exists.
   */ 
  @SuppressWarnings("unchecked")
  public final Comparable
  getParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    KeyParam param = getParam(name); 
    if(param == null)
      throw new PipelineException
        ("Unable to determine the value of the (" + name + ") parameter!");
    return param.getValue();
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
  @SuppressWarnings("unchecked")
  public final void 
  setParamValue
  (
   String name, 
   Comparable value
  ) 
  {
    if(name == null)
      throw new IllegalArgumentException("The parameter name cannot be (null)!");

    KeyParam param = pParams.get(name);
    if(param == null) 
      throw new IllegalArgumentException
        ("No parameter named (" + name + ") exists for this action!");

    param.setValue(value);
  }

  /** 
   * Copy the values of all of the parameters from the given selection key. <P> 
   * 
   * Note that there is no requirement that the given Key Chooser be the same plugin type or 
   * version.  Any incompatible parameters will simply be ignored by the copy operation.
   * 
   * @param key  
   *   The Key Chooser to use as the source of single valued parameter values.
   */
  public final void 
  setParamValues
  (
   BaseKeyChooser key   
  ) 
  {
    for(String name : pParams.keySet()) {
      KeyParam kparam = key.getParam(name);
      if(kparam != null) {
        KeyParam param = pParams.get(name);
        try {
          param.setValue(kparam.getValue());
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
   * Sets the hierarchical grouping of parameters and presets which determine the layout of UI
   * components.
   * <P>
   * 
   * The given layouts must contain an entry for all parameters defined for the selection key
   * exactly once. A collapsible drawer component will be created for each layout group which
   * contains a field for each parameter or preset entry in the order specified by the group.
   * All <CODE>null</CODE> entries will cause additional space to be added between the UI
   * fields. Each layout subgroup will be represented by its own drawer nested within the
   * drawer for the parent layout group.
   * <P>
   * 
   * This method should be called by subclasses in their constructor after initializing all
   * single valued parameters with the {@link #addParam addParam} method.
   * 
   * @param group
   *        The layout group.
   */
  protected final void
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
           "(" + group.getName() + ") does not match any parameter " + 
           "defined for this Selection Key!");
    }

    for(String name : pParams.keySet()) {
      if(!names.contains(name))
        throw new IllegalArgumentException
          ("The parameter (" + name + ") defined by this Selection Key was not " + 
           "specified by any the parameter layout group!");
    }

    pLayout = new LayoutGroup("KeyParameters", 
                              "The single value Selection Key plugin parameters.", group);
  }

  /**
   * Recursively search the parameter groups to collect the parameter names and verify
   * that no parameter is specified more than once.
   */ 
  private final void 
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
            ("The parameter (" + name + ") was specified more than once " +
             "in the given parameter group!");
        names.add(name);
      }
    }
      
    for(LayoutGroup sgroup : group.getSubGroups()) 
      collectLayoutNames(sgroup, names);
  }

  /**
   * Get the grouping of parameters used to layout components which represent 
   * the parameters in the user interface. <P> 
   * 
   * If no parameter group has been previously specified, a group will 
   * be created which contains all parameters in alphabetical order.
   */ 
  public final LayoutGroup
  getLayout()
  {
    if(pLayout == null) {
      pLayout = new LayoutGroup("KeyParameters", 
                                "The Selection Key plugin parameters.", true);
      for(String name : pParams.keySet()) 
        pLayout.addEntry(name);
    }
    
    return pLayout; 
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
  @SuppressWarnings("unchecked")
  public final void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    TreeMap<String, KeyParam> stuff = 
      (TreeMap<String, KeyParam>) decoder.decode("Params");   
    if(stuff != null) {
      for(KeyParam param : stuff.values()) 
        pParams.put(param.getName(), param); 
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5956823852589887118L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The table of key parameters.
   */
  private TreeMap<String, KeyParam>  pParams;   
  
  /**
   * Specifies the grouping of parameters used to layout components which 
   * represent the parameters in the user interface. 
   */ 
  private LayoutGroup  pLayout;

}
