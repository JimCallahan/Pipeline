// $Id: Plugins.java,v 1.5 2004/06/14 22:44:26 jim Exp $
  
package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N S                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A set of static methods for instantiating Pipeline plugin classes.
 */
public
class Plugins
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  private 
  Plugins() 
  {}


  /*----------------------------------------------------------------------------------------*/
  /*   I N I T I A L I Z A T I O N                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Load all {@link BaseEditor BaseEditor}, {@link BaseAction BaseAction} and 
   * {@link BaseTool BaseTool} classes found in the installed Pipeline plugin directory.  <P> 
   * 
   * This method must be called before any of the plugin instantiation methods
   * ({@link #newEditor(String) newEditor}, {@link #newAction(String) newAction} or 
   * {@link #newTool(String) newTool}) can be used.
   */
  public static synchronized void 
  init() 
    throws PipelineException
  {
    sEditors = new TreeMap<String,Class>();
    sActions = new TreeMap<String,Class>();
    sTools   = new TreeMap<String,Class>();
    
    File dir = new File(PackageInfo.sInstDir + "/plugins/us/temerity/pipeline/plugin");
    File[] files = dir.listFiles();
    int wk;
    for(wk=0; wk<files.length; wk++) {
      if(files[wk].isFile()) {
	Logs.plg.finer("Found class file: " + files[wk]);
      
	String parts[] = files[wk].getName().split("\\.");
	if((parts.length == 2) && parts[1].equals("class")) {
	  String cname = ("us.temerity.pipeline.plugin." + parts[0]);

	  try {
	    Logs.plg.finer("Loading: " + cname);
	    Class cls = Class.forName(cname);
	    
	    if(BaseEditor.class.isAssignableFrom(cls)) {
	      Logs.plg.finest("Instantiating Editor: " + cname);
	      BaseEditor editor = (BaseEditor) cls.newInstance();
	      
	      sEditors.put(editor.getName(), cls);
	      Logs.plg.fine("Loaded Editor Plugin: " + editor.getName());
	    }
 	    else if(BaseAction.class.isAssignableFrom(cls)) {
 	      Logs.plg.finest("Instantiating Action: " + cname);
 	      BaseAction action = (BaseAction) cls.newInstance();
	      
 	      sActions.put(action.getName(), cls);
 	      Logs.plg.fine("Loaded Action Plugin: " + action.getName());
 	    }
	    // 	  else if(BaseTool.class.isAssignableFrom(cls)) {
	    // 	    Logs.plg.finest("Instantiating Tool: " + cname);
	    // 	    BaseTool tool = (BaseTool) cls.newInstance();
	    
	    // 	    sTools.put(tool.getName(), cls);
	    // 	    Logs.plg.fine("Loaded Tool Plugin: " + tool.getName());
	    // 	  }
	  } 
	  catch(LinkageError ex) {
	    throw new PipelineException("Unable to link plugin: " + cname + "\n\n" + 
					ex.getMessage());
	  }
	  catch(InstantiationException ex) {
	    throw new PipelineException("Unable to intantiate plugin: " + cname + "\n\n" +
					ex.getMessage());
	  }
	  catch(IllegalAccessException ex) {
	    throw new PipelineException("Unable to access plugin: " + cname + "\n\n" +
					ex.getMessage());
	  }
	  catch(ClassNotFoundException ex) {
	    throw new PipelineException("Unable to find class for plugin: " + cname + "\n\n" +
					ex.getMessage());
	  }	  
	}
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the names of the loaded editor plugins.
   */ 
  public static synchronized TreeSet<String>
  getEditorNames() 
  {
    return new TreeSet<String>(sEditors.keySet());
  }
  
  /**
   * Get the names of the loaded action plugins.
   */ 
  public static synchronized TreeSet<String>
  getActionNames() 
  {
    return new TreeSet<String>(sActions.keySet());
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N S T A N T I A T I O N                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new node editor instance based on the given name. <P> 
   * 
   * Note that <CODE>name</CODE> is not the name of the class, but rather the name obtained
   * by calling {@link BaseEditor#getName BaseEditor.getName} for the returned editor.
   * 
   * @param name 
   *   The name of the editor to instantiate.  
   * 
   * @throws  PipelineException
   *   If no node editor class can be found for the given <CODE>name</CODE> or instantiation 
   *   fails for the found class.
   */
  public static synchronized BaseEditor
  newEditor
  (
   String name
  ) 
    throws PipelineException
  {
    return (BaseEditor) newPlugin("Editor", sEditors, name);
  }
  
  /**
   * Create a new node action instance based on the given name. <P> 
   * 
   * Note that <CODE>name</CODE> is not the name of the class, but rather the name obtained
   * by calling {@link BaseAction#getName BaseAction.getName} for the returned action.
   * 
   * @param name 
   *   The name of the action to instantiate.  
   * 
   * @throws  PipelineException
   *   If no node action class can be found for the given <CODE>name</CODE> or instantiation 
   *   fails for the found class.
   */
  public static synchronized BaseAction
  newAction
  (
   String name
  ) 
    throws PipelineException
  {
    return (BaseAction) newPlugin("Action", sActions, name);
  }
  

  /**
   * Create a new tool instance based on the given name. <P> 
   * 
   * Note that <CODE>name</CODE> is not the name of the class, but rather the name obtained
   * by calling {@link BaseTool#getName BaseTool.getName} for the returned tool.
   * 
   * @param name 
   *   The name of the tool to instantiate.  
   * 
   * @throws  PipelineException
   *   If no tool class can be found for the given <CODE>name</CODE> or instantiation 
   *   fails for the found class.
   */
 //  public static synchronized BaseTool
//   newTool
//   (
//    String name
//   ) 
//     throws PipelineException
//   {
//     return (BaseTool) newPlugin("Tool", sTools, name);
//   }
  

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new plugin instance looked up from the given table of classes using the 
   * given name.
   * 
   * @param ptype 
   *   The kind of plugin being instantiated: Editor, Action or Tool.
   * 
   * @param table 
   *   The table of plugin classes to search for <CODE>name</CODE>.
   * 
   * @param name 
   *   The name of the plugin to instantiate.  
   * 
   * @throws  PipelineException
   *   If no plugin class can be found for the given <CODE>name</CODE> or instantiation 
   *   fails for the found class.
   */
  private static synchronized Object
  newPlugin
  (
   String ptype,
   TreeMap<String,Class> table,
   String name
  ) 
    throws PipelineException
  {
    if(table == null) 
      throw new PipelineException("The plugins have not been intialized!");

    Class cls = table.get(name);
    if(cls == null) {
      throw new PipelineException
	("Unable to find any " + ptype + " plugin named: " + name);
    }

    try {
      return cls.newInstance();  
    }
    catch (IllegalAccessException ex) {
      throw new PipelineException
	("Unable to access the constructor for plugin class: " + name);
    }
    catch (InstantiationException ex) { 
      throw new PipelineException
	("Unable to instantiate the plugin class: " + name + "\n\n" + 
	 ex.getMessage());
    }
  }  



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The table of loaded editors plugin classes.
   */
  private static TreeMap<String,Class>  sEditors;
  
  /** 
   * The table of loaded actions plugin classes.
   */
  private static TreeMap<String,Class>  sActions;
  
  /** 
   * The table of loaded tools plugin classes.
   */
  private static TreeMap<String,Class>  sTools;
  
}


