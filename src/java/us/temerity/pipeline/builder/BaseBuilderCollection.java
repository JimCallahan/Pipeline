// $Id: BaseBuilderCollection.java,v 1.8 2008/02/06 07:53:23 jesse Exp $

package us.temerity.pipeline.builder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;
import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   B U I L D E R   C O L L E C T I O N                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The installable parent class of Builder Collections, which are groups of Builder and Namers
 * designed to work together.
 * <p>
 */
public 
class BaseBuilderCollection
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
  BaseBuilderCollection() 
  {
    super();
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
  BaseBuilderCollection
  (
   String name,  
   VersionID vid,
   String vendor, 
   String desc
  ) 
  {
    super(name, vid, vendor, desc);
  }
  
  /**
   * Copy constructor. 
   * <P> 
   * Used internally to create a generic instances of plugin subclasses.  This constructor
   * should not be used in end user code! 
   * <P> 
   * @param collect
   *   The source to copy from 
   */ 
  public 
  BaseBuilderCollection
  (
   BaseBuilderCollection collect
  ) 
  {
    super(collect.pName, collect.pVersionID, collect.pVendor, collect.pDescription);
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
    return PluginType.BuilderCollection;
  }
  
  /**
   * Returns a table of the fullly resolved node builder class names indexed by the short 
   * names of the builders provided by this collection.<P> 
   * 
   * All builder collections should override this method to return information about 
   * the specific builders they provide.  The key in the same should be identical to that 
   * returned by the {@link BaseBuilder#getName BaseBuilder.getName()} method.
   * 
   * @return
   *   The mapping of short builder names to the full class name of the builder.  By default
   *   an empty TreeMap is returned.
   */
  public TreeMap<String, String>
  getBuildersProvided()
  {
    return new TreeMap<String, String>();
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   B U I L D E R   L A Y O U T                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Sets the hierarchical grouping of builder which determine the layout of UI components.
   * <P>
   * The given layouts must contain one and only entry for all builders in the collection. 
   * <code>Null</code> entries will create seperators in the menu system.
   * <P>
   * This method should be called by subclasses in their constructor.
   * 
   * @param group
   *   The layout group.
   */
  protected final void
  setLayout
  (
   LayoutGroup group
  ) 
  {
    TreeSet<String> names = new TreeSet<String>();
    collectLayoutNames(group, names);
    
    Set<String> builderNames = getBuildersProvided().keySet();
    
    for(String name : names) {
      if(!builderNames.contains(name) )
        throw new IllegalArgumentException
        ("The entry (" + name + ") specified by the builder layout group " + 
         "(" + group.getName() + ") does not match any builder defined in the Collection!");
    }

    for(String name : builderNames) {
      if(!names.contains(name))
        throw new IllegalArgumentException
        ("The builder (" + name + ") defined in this Collection was not " + 
         "specified by any the builder layout group!");
    }
    pLayout = group;
  }
  
  /**
   * Recursively search the parameter groups to collect the builder names and verify
   * that no builder is specified more than once.
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
            ("The builder (" + name + ") was specified more than once " +
             "in the given parameter group!");
        names.add(name);
      }
    }
      
    for(LayoutGroup sgroup : group.getSubGroups()) 
      collectLayoutNames(sgroup, names);
  }
  
  /**
   * Get the grouping of builders used by layout components which represent 
   * the builders in the user interface. <P> 
   * 
   * If no builder layout group has been previously specified, a group will 
   * be created which contains all builders in alphabetical order.
   * 
   * @return
   *   The Layout Group. 
   */ 
  public final LayoutGroup
  getLayout()
  {
    if(pLayout == null) {
      pLayout = new LayoutGroup("Builders", 
                                "The builders in this collection.", true);
      for(String name : getBuildersProvided().keySet()) 
        pLayout.addEntry(name);
    }
    
    return pLayout; 
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N S T A N T I A T E                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Creates and runs an instance of the named builder.
   * <p>
   * The Builder Collection will create a new connection to the MasterMgr and QueueMgr and
   * provide a new {@link BuilderInformation} for this Builder to use. 
   * 
   * @param builderName
   *   The short name of the Builder to instantiate.  This needs to be one of the names
   *   in the keySet of the TreeMap returned by the {@link #getBuildersProvided()} method.
   *   
   * @param terminateOnQuit
   *   Should be entire app quit when the builder execution ends.  This needs to be false
   *   if the builder is being launched from inside plui or from some other external app
   *   that needs to continue running after the builder has completed.  If this is set
   *   to false, then whatever program invokes the builder is responsible for making sure
   *   the jvm is terminated.
   *   
   * @param useBuilderLogging
   *   Should the builder use its own internal log panel or should it use the built in
   *   Log History panel in plui.  Setting this to false when not running the builder
   *   through plui will result in no logging output.
   *
   * @throws PipelineException whenever anything goes wrong with instantiating the builder.
   *   This can be for a variety of reasons, including a misnamed builder, bad parameters,
   *   missing classes.
   */
  public final void
  instantiateBuilder
  (
    String builderName,
    boolean terminateOnQuit,
    boolean useBuilderLogging
  )
    throws PipelineException
  {
    BuilderInformation info = 
      new BuilderInformation(true, terminateOnQuit, true, useBuilderLogging, new MultiMap<String, String>());
    instantiateBuilder(builderName, null, null, info);
  }

  /**
   * Creates and runs an instance of the named builder.
   * <p>
   * The Builder Collection will create a new connection to the MasterMgr and QueueMgr and
   * provide a new {@link BuilderInformation} for this Builder to use. 
   * 
   * @param builderName
   *   The short name of the Builder to instantiate.  This needs to be one of the names
   *   in the keySet of the TreeMap returned by the {@link #getBuildersProvided()} method.
   * @param mclient
   *   The instance of {@link MasterMgrClient} for the builder to use or <code>null</code>
   *   if a new connection should be created.
   * @param qclient
   *   The instance of {@link QueueMgrClient} the builder to use or <code>null</code> if
   *   a new connection should be created.
   * @param useGui
   *   Should the builder be instantiated in GUI mode.
   * @param abortOnBadParam
   *   Should the builder abort when it contains a command line param value that does
   *   not match any existing parameters.
   * @param paramValues
   *   A MultiMap that consists of BuilderName, parameter key names and finally parameter
   *   values.  Each level into a Complex Parameter is a separate key into the multimap.
   * @param terminateOnQuit
   *   Should be entire app quit when the builder execution ends.  This needs to be false
   *   if the builder is being launched from inside plui or from some other external app
   *   that needs to continue running after the builder has completed.  If this is set
   *   to false, then whatever program invokes the builder is responsible for making sure
   *   the jvm is terminated.
   * @throws PipelineException whenever anything goes wrong with instantiating the builder.
   *   This can be for a variety of reasons, including a misnamed builder, bad parameters,
   *   missing classes, etc.
   */
  public final void
  instantiateBuilder
  (
    String builderName,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    boolean useGui,
    boolean abortOnBadParam,
    boolean terminateOnQuit,
    boolean useBuilderLogging,
    MultiMap<String, String> paramValues
  )
    throws PipelineException
  {
    BuilderInformation info = 
      new BuilderInformation(useGui, terminateOnQuit, abortOnBadParam, 
                             useBuilderLogging, paramValues);
    instantiateBuilder(builderName, mclient, qclient, info);
  }
  
  /**
   * Creates and runs an instance of the named builder.
   * <p>
   * The Builder Collection will create a new connection to the MasterMgr and QueueMgr and
   * provide a new {@link BuilderInformation} for this Builder to use. 
   * 
   * @param builderName
   *   The short name of the Builder to instantiate.  This needs to be one of the names
   *   in the keySet of the TreeMap returned by the {@link #getBuildersProvided()} method.
   * @param mclient
   *   The instance of {@link MasterMgrClient} for the builder to use or <code>null</code>
   *   if a new connection should be created.
   * @param qclient
   *   The instance of {@link QueueMgrClient} the builder to use or <code>null</code> if
   *   a new connection should be created.
   * @param info
   *   The instance of {@link BuilderInformation} for the builder to use or <code>null</code>
   *   if a new information class should be created.
   * @throws PipelineException whenever anything goes wrong with instantiating the builder.
   *   This can be for a variety of reasons, including a misnamed builder, bad parameters,
   *   missing classes.
   */
  @SuppressWarnings("unchecked")
  public final void
  instantiateBuilder
  (
    String builderName,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation info
  )
    throws PipelineException
  {
    TreeMap<String, String> list = getBuildersProvided();
    if (!list.keySet().contains(builderName))
      throw new PipelineException
        ("There is no Builder named (" + builderName + ") defined in the BuilderCollection " +
         "(" + pName + "), version (" + pVersionID.toString() +  ") from the " +
         "(" + pVendor + ") vendor.");
    
    String builderClassLocation = list.get(builderName);
    if (builderClassLocation == null || builderClassLocation.equals(""))
      throw new PipelineException
        ("A valid string must be provided for the name of the builder class.  " +
         "The builder (" + builderName + ") does not return a valid builder class path.");
    
    if (mclient == null)
      mclient = new MasterMgrClient();
    if (qclient == null)
      qclient = new QueueMgrClient();
    if (info == null)
      info = new BuilderInformation(true, false, true, false, new MultiMap<String, String>());
    
    LogMgr.getInstance().setLevel(Kind.Ops, Level.Fine);
    
    try {
      ClassLoader loader = this.getClass().getClassLoader();
      Class cls = loader.loadClass(builderClassLocation);
      Constructor construct = 
        cls.getConstructor
        (MasterMgrClient.class, 
          QueueMgrClient.class, 
          BuilderInformation.class);
      BaseBuilder builder = (BaseBuilder) construct.newInstance(mclient, qclient, info);
      builder.run();
    }
    catch (PipelineException ex) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Severe,
         ex.getMessage());
    }
    catch (NoSuchMethodException ex) {
      String message = 
        "Was unable to instantiate the constructor for the specified Builder.  " +
        "This most likely means that the Builder was not meant to be run as a " +
        "standalone builder.\n";
      message += ex.getMessage();
      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Severe,
         message);
      ex.printStackTrace();
    }
    catch (InvocationTargetException ex) {
      Throwable th = ex.getTargetException();
      String message = 
        "An Invocation Target Exception has occured.  This most likely indicates that " +
        "the name of the builder being passed to BuilderApp is specified incorrectly or " +
        "that an error occured in the Builder's constructor.\n";
      message += getFullMessage(th);
      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Severe,
         message);
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Severe,
         getFullMessage(ex));
        ex.printStackTrace();
    }
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -4676341676179041427L;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Specifies the grouping of builders used to layout components which 
   * represent the builders in the user interface. 
   */ 
  private LayoutGroup  pLayout;
}
