// $Id: BaseTool.java,v 1.20 2009/09/21 22:30:14 jlee Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.glue.GlueDecoder; 

import java.util.*;
import java.io.*;

import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   T O O L                                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * The superclass of all Pipeline tool plugins. <P>
 * 
 * Tool plugins provide a mechanism for extending the functionality of the <B>plui</B>(1) 
 * graphical user interface to Pipeline.  The tool is triggered by a menu item displayed
 * in the Node Viewer panel similar to the built-in operations such as Check-In or Edit. <P>
 * 
 * Tools are responsible for creating any user interface components they require to collect
 * user input by overriding the {@link #collectPhaseInput collectPhaseInput} method.  The
 * {@link UIFactory UIFactory} class contains a large number of static methods for creating
 * UI component with a Pipeline look-and-feel.  The {@link JToolDialog JToolDialog} class
 * should be used as the container for presenting any created components to the user. <P> 
 * 
 * The functionality of tool subclasses is provided by overriding the 
 * {@link #executePhase executePhase} method. <P> 
 * 
 * These two methods are alternatively executed until the tool signifies that execution 
 * is complete or an error occurs.  In this way, the user interface components provided 
 * by the tool and the operations taken can be different during each phase of execution.
 * This allows for very flexible tools which may adapt during their execution based on 
 * additional user input. <P> 
 * 
 * Single phase tools which collect no user input execept for the selected nodes at the 
 * time the tool is run can be created by only overriding the <CODE>executePhase</CODE>
 * method and returning <CODE>true</CODE> on exit from this method. <P> 
 * 
 * While new plugin subclass versions are being modified and tested the 
 * {@link #underDevelopment underDevelopment} method should be called in the subclasses
 * constructor to enable the plugin to be dynamically reloaded.  
 */
public  
class BaseTool
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
  protected
  BaseTool() 
  {
    super();
    pPhases = new ArrayList<ToolPhase>();
  }

  /** 
   * Construct with the given name, version, vendor and description. 
   * 
   * @param name 
   *   The short name of the tool.
   * 
   * @param vid
   *   The tool plugin revision number. 
   * 
   * @param vendor
   *   The name of the plugin vendor.
   * 
   * @param desc 
   *   A short description of the tool.
   */ 
  protected
  BaseTool
  (
   String name, 
   VersionID vid,
   String vendor, 
   String desc
  ) 
  {
    super(name, vid, vendor, desc);
    pPhases = new ArrayList<ToolPhase>();
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
    return PluginType.Tool;
  }

 
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Initialize the execution phases with the currently node selection. <P> 
   * 
   * The primary selected node is the node under the mouse when the tool menu was shown 
   * (highlighted cyan).  The selected nodes include the primary selected node as well as 
   * any previously selected nodes (highlighted yellow). <P> 
   * 
   * It is possible that the same node may be selected more than once, however the status 
   * of any particular node will only be reported once.  In choosing among more then one 
   * selected copy of a node, nodes with heavyweight status details will be favored over 
   * those with lightweight details or downstream nodes with no details. <P> 
   * 
   * @param author 
   *   The name of the user which owns the working area where the tool is run.
   * 
   * @param view 
   *   The name of the user's working area view where the tool is run. 
   *
   * @param primary 
   *   The name of the primary selected node or <CODE>null</CODE>.
   *
   * @param prefix
   *   The name of the parent directory of the primary selected node or a branch 
   *   selected in the Node Browser or <CODE>null</CODE>.
   * 
   * @param selected
   *   The last known status of the selected nodes indexed by fully resolved node name.
   * 
   * @param roots
   *   The fully resolved names of the root nodes of the currently displayed node trees.
   */
  public final void 
  initExecution
  (
   String author, 
   String view, 
   String primary, 
   String prefix, 
   TreeMap<String,NodeStatus> selected, 
   TreeSet<String> roots
  )
  {
    pAuthor   = author; 
    pView     = view; 
    pPrimary  = primary;
    pPrefix   = prefix;
    pSelected = selected;    
    pRoots    = roots;
    pPhaseIdx = 0;
  }

  /**
   * Get the name of the user which owns the working area where the tool is run.
   */ 
  public String
  getAuthor()
  {
    return pAuthor;
  }

  /**
   * Get the name of the user's working area view where the tool is run. 
   */ 
  public String
  getView()
  {
    return pView;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Add an execution phase to the tool.<P> 
   * 
   * Tool plugin subclasses should call this method in their constructor to register 
   * instances of {@link ToolPhase} created by the tool which represent each phase of 
   * tool execution.<P> 
   * 
   * The default implementation of the {@link #collectPhaseInput collectPhaseInput} and 
   * {@link #executePhase executePhase} methods will iterate through these ToolPhase 
   * instances automatically.
   */ 
  protected final synchronized void 
  addPhase
  (
   ToolPhase phase
  ) 
  {
    pPhases.add(phase);
  }

  /** 
   * Create and show graphical user interface components to collect information from the 
   * user to use as input in the next phase of execution for the tool. <P> 
   * 
   * This method is executed by the Swing event thread.  Modal dialogs can be created and 
   * shown to collect user input.  The collected input should be validated and stored in 
   * fields of the tool subclass by this method before returning. <P> 
   * 
   * If this method return <CODE>null</CODE>, the execution of the tool will be immediately 
   * aborted without any notification to the user. Otherwise, the 
   * {@link #executePhase executePhase} method will be run in a seperate thread to perform 
   * action based on the input collected in this method.  The returned <CODE>String</CODE> 
   * will be used as the progress message shown in the operation status field at the bottom
   * of the main Pipeline window. <P> 
   * 
   * The default implementation of this method simply iterates through the ToolPhase 
   * instances previously registered using {@link #addPhase addPhase} calling the {@link
   * ToolPhase#collectInput ToolPhase.collectInput} method of each phase.<P> 
   * 
   * Note that older tool plugins created before the existance of {@link ToolPhase} override
   * this method and manually handle interating through tool execution phases.  This is no 
   * longer necessary or recommended.  New tools should create ToolPhase internal classes 
   * and register them with {@link #addPhase addPhase} instead of overriding this method. 
   * 
   * @return 
   *   The phase progress message or <CODE>null</CODE> to abort early.
   * 
   * @throws PipelineException 
   *   If unable to validate the given user input.
   */  
  public synchronized String
  collectPhaseInput() 
    throws PipelineException 
  {
    if(pPhaseIdx < pPhases.size()) 
      return pPhases.get(pPhaseIdx).collectInput();
    return null;
  }

  /**
   * Perform one phase in the execution of the tool. <P> 
   * 
   * This method is executed in a seperate thread from the Swing event thread.  No user
   * interface components should be created or queried by this method.  All information 
   * used to control the behaviour of this method should be stored in fields of the tool 
   * previously in the {@link #collectPhaseInput collectPhaseInput} method. <P> 
   * 
   * If this method returns <CODE>true</CODE>, another phase of execution will be initiated
   * and user input will again be collected by the <CODE>collectPhaseInput</CODE> method.
   * Otherwise, execution of the tool will end successfully.<P> 
   * 
   * The default implementation of this method simply iterates through the ToolPhase 
   * instances previously registered using {@link #addPhase addPhase} calling the {@link
   * ToolPhase#execute ToolPhase.execute} method of each phase.<P> 
   * 
   * Note that older tool plugins created before the existance of {@link ToolPhase} override
   * this method and manually handle interating through tool execution phases.  This is no 
   * longer necessary or recommended.  New tools should create ToolPhase internal classes 
   * and register them with {@link #addPhase addPhase} instead of overriding this method. 
   * 
   * @param mclient
   *   The network connection to the plmaster(1) daemon.
   * 
   * @param qclient
   *   The network connection to the plqueuemgr(1) daemon.
   * 
   * @return 
   *   Whether to continue and collect user input for the next phase of the tool.
   * 
   * @throws PipelineException 
   *   If unable to sucessfully execute this phase of the tool.
   */ 
  public synchronized boolean
  executePhase
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {
    if(pPhaseIdx >= pPhases.size()) 
      return false; 

    switch(pPhases.get(pPhaseIdx).execute(mclient, qclient)) {
    case Continue:
      pPhaseIdx++;
      return true;

    case Repeat:
      return true;

    default:
      return false;
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to perform a node status update upon successfully executing the tool. <P> 
   * 
   * By default this method returns <CODE>true</CODE>.  Subclasses which wish to skip
   * node status update should override this method to return <CODE>false</CODE>.
   */ 
  public boolean
  updateOnExit() 
  {
    return true;
  }

  /**
   * The fully resolved names of the root nodes to display on exit.
   * 
   * By default this method returns <CODE>pRoots</CODE>.  Subclasses which wish to modify
   * the set of nodes updated when the tool exits should modify the contents of 
   * <CODE>pRoots</CODE> or override this method to return the specific root nodes they wish 
   * to be displayed. <P> 
   * 
   * Note that a status update is required for this to take effect, so updateOnExit() must
   * also return <CODE>true</CODE>.
   */ 
  public TreeSet<String> 
  rootsOnExit() 
  {
    return pRoots; 
  }

  /**
   * The name of the user owning the current working area on exit.
   * 
   * By default this method returns <CODE>getAuthor()</CODE>.  Subclasses which wish to modify
   * the current working area when the tool exits should override this method to return an
   * alternative owning user.<P> 
   * 
   * Note that a status update is required for this to take effect, so updateOnExit() must
   * also return <CODE>true</CODE>.
   */ 
  public String
  authorOnExit() 
  {
    return pAuthor;
  }

  /**
   * The name of the user's working area view on exit.
   * 
   * By default this method returns <CODE>getView()</CODE>.  Subclasses which wish to modify
   * the current working area when the tool exits should override this method to return an
   * alternative working area view name.<P> 
   * 
   * Note that a status update is required for this to take effect, so updateOnExit() must
   * also return <CODE>true</CODE>.
   */ 
  public String
  viewOnExit() 
  {
    return pView;
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to display the Log History dialog when running this tool.
   * 
   * By default this method returns <CODE>false</CODE>.  Subclasses which wish to display
   * verbose progress messages in the log should override this method to return 
   * <CODE>true</CODE>.
   */ 
  public boolean 
  showLogHistory() 
  {
    return false;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The action to take after the completion of the execute portion of the current execute
   * phase of operation of the Tool plugin. 
   */
  public
  enum NextPhase
  {  
    /**
     * Continue on to the next phase of operation.
     */ 
    Continue, 

    /**
     * Repeat the current phase of operation.
     */ 
    Repeat, 
    
    /**
     * The Tool has completed all phases.
     */ 
    Finish;
  }

  /**
   * Base class for one phase of operation of the Tool plugin. <P> 
   * 
   * Tool plugins should create subclasses of ToolPhase for each of their phases of input
   * and register instances of these classes in their constructor using {@link #addPhase
   * addPhase}.  The default implementation of the {@link #collectPhaseInput 
   * collectPhaseInput} and {@link #executePhase executePhase} methods will iterate through
   * these ToolPhase instances automatically.
   */ 
  public 
  class ToolPhase
  {
    public
    ToolPhase() 
    {}

    /** 
     * Create and show graphical user interface components to collect information from the 
     * user to use as input for the {@link #execute execute} method in this phase.<P> 
     * 
     * This method is executed by the Swing event thread.  Modal dialogs can be created and 
     * shown to collect user input.  The collected input should be validated and stored in 
     * fields of the ToolPhase subclass by this method before returning. <P> 
     * 
     * If this method returns <CODE>null</CODE>, the execution of the tool will be 
     * immediately aborted without any notification to the user.  Otherwise, the 
     * {@link #execute execute} method will be run in a seperate thread to perform 
     * actions based on the input collected in this method.  The returned <CODE>String</CODE> 
     * will be used as the progress message shown in the operation status field at the bottom
     * of the main Pipeline window. 
     * 
     * @return 
     *   The phase progress message or <CODE>null</CODE> to abort early.
     * 
     * @throws PipelineException 
     *   If unable to validate the given user input.
     */  
    public String
    collectInput() 
      throws PipelineException 
    {
      return "...";
    }

    /**
     * Perform operations for this phase of the tool based on user input gathered by the 
     * {@link #collectInput collectInput} method. <P> 
     * 
     * This method is executed in a seperate thread from the Swing event thread.  No user
     * interface components should be created or queried by this method.  All information 
     * used to control the behaviour of this method should be stored in fields of this 
     * ToolPhase previously set by the {@link #collectInput collectInput} method. <P> 
     * 
     * If this method returns <CODE>true</CODE>, execution will proceed to the next 
     * ToolPhase for this tool plugin and begin collection more information from the user
     * by calling the {@link #collectInput collectInput} method of this next phase.<P> 
     * 
     * If this method returns <CODE>NextPhase.Finish</CODE>, execution of the entire tool 
     * will end successfully.
     * 
     * @param mclient
     *   The network connection to the plmaster(1) daemon.
     * 
     * @param qclient
     *   The network connection to the plqueuemgr(1) daemon.
     * 
     * @return 
     *   What to do next: Continue, Repeat or Finish?
     * 
     * @throws PipelineException 
     *   If unable to sucessfully execute this phase of the tool.
     */ 
    public NextPhase
    execute
    (
     MasterMgrClient mclient,
     QueueMgrClient qclient
    ) 
      throws PipelineException
    {
      return NextPhase.Finish;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7255319543522132244L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * The name of user which owns the working area where the tool is run.
   */
  private String  pAuthor;

  /** 
   * The name of the working area view where the tool is run. 
   */
  private String  pView;
  
  /**
   * The name of the primary selected node or <CODE>null</CODE> if there is no primary 
   * node selection.
   */ 
  protected String  pPrimary; 

  /**
   * The name of the parent directory of the primary selected node or the branch selected 
   * in the node browser.  This can be <CODE>null</CODE> if there is no primary node 
   * selection or no branch selected in the Node Browser.
   */
  protected String  pPrefix;

  /**
   * The last known status of the selected nodes indexed by fully resolved node name.
   */ 
  protected TreeMap<String,NodeStatus>  pSelected; 

  /**
   * The fully resolved names of the root nodes of the currently displayed node trees. <P> 
   * 
   * You can modify the contents of this set to change which nodes are displayed once 
   * the tool exits.  See the {@link #updateOnExit} and {@link #rootsOnExit} methods
   * for details.
   */ 
  protected TreeSet<String>  pRoots; 

  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The registered tool phases.
   */ 
  private ArrayList<ToolPhase>  pPhases;

  /**
   * The current phase index;
   */
  private int pPhaseIdx;

}



