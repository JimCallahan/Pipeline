// $Id: BaseTool.java,v 1.3 2005/02/20 20:53:57 jim Exp $

package us.temerity.pipeline;

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
 
  protected
  BaseTool() 
  {
    super();
  }

  /** 
   * Construct with the given name, version and description. 
   * 
   * @param name 
   *   The short name of the tool.
   * 
   * @param vid
   *   The tool plugin revision number. 
   * 
   * @param desc 
   *   A short description of the tool.
   */ 
  protected
  BaseTool
  (
   String name, 
   VersionID vid,
   String desc
  ) 
  {
    super(name, vid, desc);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the name of the catagory of this plugin.
   */ 
  public String 
  getPluginCatagory() 
  {
    return "Tool";
  }

 
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Initialize the execution phases with the currently node selection. <P> 
   * 
   * The primary selected node is the node under the mouse when the tool menu was shown 
   * (highlighted cyan).  The selected nodes include the primary selected node as well as 
   * any previously selected nodes (highlighted yellow). <P> 
   * 
   * Selected nodes with the same name are only reported once.  If the 
   * {@link NodeStatus NodeStatus} associated with selected nodes does not contain a
   * {@link NodeDetails NodeDetails}, then only downstream (grey) nodes were selected. <P> 
   * 
   * @param primary 
   *   The name of the primary selected node or <CODE>null</CODE>.
   * 
   * @param selected
   *   The last known status of the selected nodes indexed by fully resolved node name.
   */
  public void 
  initExecution
  (
   String primary, 
   TreeMap<String,NodeStatus> selected
  )
  {
    pPrimary  = primary;
    pSelected = selected;    
  }


  /*----------------------------------------------------------------------------------------*/

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
   * of the main Pipeline window. 
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
    return "...";
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
   * Otherwise, execution of the tool will end successfully.
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
    return false;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7255319543522132244L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The name of the primary selected node or <CODE>null</CODE> if there is no primary 
   * node selection.
   */ 
  protected String  pPrimary; 

  /**
   * The last known status of the selected nodes indexed by fully resolved node name.
   */ 
  protected TreeMap<String,NodeStatus>  pSelected; 

}



