// $Id: BaseBuilderCollection.java,v 1.22 2009/04/16 21:18:38 jesse Exp $

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
   *   The short name of the plugin. 
   * 
   * @param vid
   *   The plugin revision number.
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
   * Construct with the given name, version, vendor and description. 
   * 
   * @param pluginID 
   *    The unique ID of the plugin. 
   * 
   * @param desc 
   *   A short description of the action.
   */ 
  protected
  BaseBuilderCollection
  (
   PluginID pluginID, 
   String desc
  ) 
  {
    super(pluginID, desc); 
  }
  
  /**
   * Copy constructor. 
   * <P> 
   * Used internally to create a generic instances of plugin subclasses.  This constructor
   * should not be used in end user code! 
   * <P> 
   * @param collect
   *   The builder collection to copy.
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
   * Return an instance of the named builder.
   * <p>
   * The Builder Collection will create a new connection to the MasterMgr and QueueMgr and
   * provide a new {@link BuilderInformation} for this Builder to use. 
   * 
   * @param builderName
   *   The short name of the Builder to instantiate.  This needs to be one of the names
   *   in the keySet of the TreeMap returned by the {@link #getBuildersProvided()} method.
   *   
   * @param mclient
   *   The instance of {@link MasterMgrClient} for the builder to use.
   *
   * @param qclient
   *   The instance of {@link QueueMgrClient} for the builder to use.
   *
   * @param terminateOnQuit
   *   Should be entire app quit when the builder execution ends.  This needs to be false
   *   if the builder is being launched from inside plui or from some other external app
   *   that needs to continue running after the builder has completed.  If this is set
   *   to false, then whatever program invokes the builder is responsible for making sure
   *   the JVM is terminated.
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
  public final BaseBuilder
  instantiateBuilder
  (
    String builderName,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    boolean terminateOnQuit,
    boolean useBuilderLogging
  )
    throws PipelineException
  {
    BuilderInformation info = 
      new BuilderInformation(true, terminateOnQuit, true, 
                             useBuilderLogging, new MultiMap<String, String>());
    return instantiateBuilder(builderName, mclient, qclient, info);
  }

  /**
   * Return an instance of the named builder.<p>
   * 
   * The Builder Collection will create a new connection to the MasterMgr and QueueMgr and
   * provide a new {@link BuilderInformation} for this Builder to use. 
   * 
   * @param builderName
   *   The short name of the Builder to instantiate.  This needs to be one of the names
   *   in the keySet of the TreeMap returned by the {@link #getBuildersProvided()} method.
   *   
   * @param mclient
   *   The instance of {@link MasterMgrClient} for the builder to use.
   *
   * @param qclient
   *   The instance of {@link QueueMgrClient} for the builder to use.
   *
   * @param useGui
   *   Should the builder be instantiated in GUI mode.
   *   
   * @param abortOnBadParam
   *   Should the builder abort when it contains a command line param value that does
   *   not match any existing parameters.
   *   
   * @param paramValues
   *   A MultiMap that consists of BuilderName, parameter key names and finally parameter
   *   values.  Each level into a Complex Parameter is a separate key into the multimap.
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
   * @throws PipelineException 
   *   Whenever anything goes wrong with instantiating the builder.
   *   This can be for a variety of reasons, including a misnamed builder, bad parameters,
   *   missing classes, etc.
   */
  public final BaseBuilder
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
    return instantiateBuilder(builderName, mclient, qclient, info);
  }
  
  /**
   * Return an instance of the named builder.<p>
   *
   * The Builder Collection will create a new connection to the MasterMgr and QueueMgr and
   * provide a new {@link BuilderInformation} for this Builder to use. 
   * 
   * @param builderName
   *   The short name of the Builder to instantiate.  This needs to be one of the names
   *   in the keySet of the TreeMap returned by the {@link #getBuildersProvided()} method.
   *
   * @param mclient
   *   The instance of {@link MasterMgrClient} for the builder to use.
   *
   * @param qclient
   *   The instance of {@link QueueMgrClient} for the builder to use.
   *
   * @param info
   *   The instance of {@link BuilderInformation} for the builder to use or <code>null</code>
   *   if a new information class should be created.
   *
   * @return
   *   The instance of the specified Builder or <code>null</code> if an error occurred while
   *   creating the Builder, but an Exception was not thrown (due to internal error handling).
   *
   * @throws PipelineException 
   *   Only if the builder is being called in a context where the error message will never 
   *   make it to the log.
   */
  @SuppressWarnings({ "unchecked", "incomplete-switch" })
  public final BaseBuilder
  instantiateBuilder
  (
    String builderName,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation info
  )
    throws PipelineException
  {
    if (mclient == null)
      throw new PipelineException("The MasterMgrClient cannot be (null)!"); 

    if (qclient == null)
      throw new PipelineException("The QueueMgrClient cannot be (null)!");

    if (info == null)
      info = new BuilderInformation(true, true, true, true, new MultiMap<String, String>());
    
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
    
    Level opLevel = LogMgr.getInstance().getLevel(Kind.Ops);
    switch(opLevel) {
    case Info:
    case Warning:
    case Severe:
      LogMgr.getInstance().setLevel(Kind.Ops, Level.Fine);
      break;
    }
    
    try {
      ClassLoader loader = this.getClass().getClassLoader();
      Class cls = loader.loadClass(builderClassLocation);
      Constructor construct = 
        cls.getConstructor
        (MasterMgrClient.class, 
          QueueMgrClient.class, 
          BuilderInformation.class);
      BaseBuilder builder = (BaseBuilder) construct.newInstance(mclient, qclient, info);
      PassLayoutGroup layout = builder.getLayout();
      if (layout == null)
        throw new PipelineException
          ("The instantiated Builder (" + builderName + ") from collection " +
           "("+ getName() + "), version (" + getVersionID() +") provided by vendor " +
           "(" + getVendor() + ") does not have a valid layout.");
      return builder;
    }
    catch (NoSuchMethodException ex) {
      String header = 
        "Was unable to instantiate the constructor for the specified Builder.  " +
        "This most likely means that the Builder was not meant to be run as a " +
        "standalone builder.\n";
      String message = Exceptions.getFullMessage(header, ex);
      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Severe,
         message);
      if (!info.useBuilderLogging() && !info.usingGui())
        throw new PipelineException(message); 
    }
    catch (InvocationTargetException ex) {
      /* intercept if a HostConfigException is the root cause */ 
      {
        Throwable cause = ex;
        while(true) {
          cause = cause.getCause();
          if(cause == null) 
            break;
          
          if(cause instanceof HostConfigException) 
            throw new PipelineException(cause); 
        }
      }

      String message = 
        Exceptions.getFullMessage
        ("An Invocation Target Exception has occured.  This most likely indicates that " +
         "the name of the builder being passed to BuilderApp is specified incorrectly or " +
         "that an error occured in the Builder's constructor.", ex);
      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Severe,
         message);
      if (!info.useBuilderLogging() && !info.usingGui())
        throw new PipelineException(message); 
    }
    catch (PipelineException ex) {
      String message = "An error has occured while instantiating the Builder\n" + 
        ex.getMessage();
      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Severe,
         message);
      if (!info.useBuilderLogging() && !info.usingGui())
        throw ex; 
    }
    catch(Exception ex) {
      String message = Exceptions.getFullMessage
        ("An error has occured while instantiating the Builder", ex);
      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Severe,
         message);
      if (!info.useBuilderLogging() && !info.usingGui())
        throw new PipelineException(message); 
    }
    return null;
  }
  
  
  /**
   * Get a non-standard builder constructor
   * <p>
   * Normal instantiate builder calls automatically fill in constructor arguments and call
   * the constructor themselves.  This method returns an uninstantiated instance of a 
   * constructor with the specified argument list.
   * 
   * @param builderName
   *   The name of the builder whose Constructor will be returned.
   *   
   * @param arguments
   *   An array representing the list of arguments that the desired constructor takes.  So if
   *   the constructor takes a String and an Integer, this array should look like <code>Class 
   *   arguments[] = {String.class, Integer.class};</code>.  All the arguments classes that 
   *   are specified must exist within the scope of the Builder Collection that this method
   *   is being called on.  If this method is being called in a builder from another builder 
   *   collection and it using a class contained in that collection, that same class much be
   *   included in this class as well.
   * 
   * @return
   *   An instance of the constructor.  To instantiate the builder, call the newInstance() 
   *   method on the constructor with an array of the actually arguments.
   *     
   * @throws PipelineException
   *   If no builder with the given name exists or if no constructor with the specified argument
   *   list exists.
   */
  @SuppressWarnings({ "unchecked", "incomplete-switch" })
  public final Constructor
  getBuilderConstructor
  (
    String builderName,
    Class[] arguments
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
    
    Level opLevel = LogMgr.getInstance().getLevel(Kind.Ops);
    switch(opLevel) {
    case Info:
    case Warning:
    case Severe:
      LogMgr.getInstance().setLevel(Kind.Ops, Level.Fine);
      break;
    }

    ClassLoader loader = this.getClass().getClassLoader();
    Class cls;
    Constructor construct = null;
    try {
      cls = loader.loadClass(builderClassLocation);
      construct = cls.getConstructor(arguments);
    }
    catch (ClassNotFoundException ex) {
      String header = 
        "A builder named (" + builderName + ") with a class file location of " +
        "(" + builderClassLocation +" ) does not appear to exist in this " +
        "BuilderCollection\n";
      String message = Exceptions.getFullMessage(header, ex);
      throw new PipelineException(message);
    }
    catch (NoSuchMethodException ex) {
      String header = 
        "Was unable to instantiate the constructor for the specified Builder.  " +
        "The arguments passed in do not represent a valid call to the builder .\n";
      throw new PipelineException(header);
    }
    
   return construct; 
  }
  
  @SuppressWarnings("unchecked")
  private static boolean 
  checkConstructorArgs
  (
    Class[] a1, 
    Class[] a2
  ) 
  {
    LogMgr.getInstance().log(Kind.Bld, Level.Finest, 
      "Compairing constructor arguments:\n\t" + a1 + "\n\t" + a2 + "\n");
    
    if (a1 == null) 
        return a2 == null || a2.length == 0;

    if (a2 == null) 
        return a1.length == 0;

    if (a1.length != a2.length) 
        return false;

    for (int i = 0; i < a1.length; i++) {
      String className1 = a1[i].getName();
      String className2 = a2[i].getName();
        if (!className1.equals(className2)) {
            return false;
        }
    }

    return true;
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
