// $Id: BaseTool.java,v 1.2 2005/01/15 02:50:46 jim Exp $

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
    setHeaderText(name + ":");
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
   * Whether the tool uses a dialog to interactively query the user for input.
   */ 
  public boolean
  isInteractive() 
  {
    return (pDialogBody != null);
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the text displayed in the header of the tool dialog.
   */ 
  public String
  getHeaderText()
  {
    return pHeaderText;
  }

  /**
   * Set the text displayed in the header of the tool dialog. 
   */ 
  protected void 
  setHeaderText
  (
   String text
  ) 
  {
    pHeaderText = text; 
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get UI components which make up the body of the dialog used to interactively query 
   * the user for input. <P> 
   * 
   * @return 
   *   The component containing the body of the dialog or <CODE>null</CODE> if 
   *   the tool is non-interactive.
   */ 
  public JComponent
  getDialogBody() 
  {
    return pDialogBody;
  }

  /**
   * Set UI components which make up the body of the dialog used to interactively query 
   * the user for input. <P> 
   * 
   * If the <CODE>body</CODE> argument is not <CODE>null</CODE>, the tool will become
   * interactive and display a dialog when triggered.
   * 
   * @param body
   *   The component containing the body of the dialog.
   */ 
  protected void 
  setDialogBody
  (
   JComponent body
  ) 
  {
    pDialogBody = body;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the currently selected nodes. <P> 
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
  setSelectedNodes
  (
   String primary, 
   TreeMap<String,NodeStatus> selected
  )
  {
    pPrimary  = primary;
    pSelected = selected;    
  }
   


  /*----------------------------------------------------------------------------------------*/
  /*  O P S                                                                                 */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Update the dialog components based on the current node selection. <P> 
   * 
   * For interactive tools, this method is called when the user selects the menu item for 
   * the tool.  The current node selections have already been updated before this method 
   * is called.  <P> 
   * 
   * This method is executed in the Swing event thread immediately after the tool menu item
   * is selected.  Interactive tool subclasses override this method to update any of the 
   * components used in the body of the tool dialog before it is shown to the user (see 
   * {@link #setDialogBody setDialogBody}). <P> 
   * 
   * This method is never called for non-interactive tools. <P> 
   * 
   * All exceptions, including runtime exceptions, thrown in this method should be caught
   * and rethrown as a {@link PipelineException PipelineException}. <P>
   * 
   * @throws PipelineException 
   *   If unable to update the dialog components based on the current node selection. 
   */  
  public synchronized void 
  update() 
    throws PipelineException 
  {}

  /**
   * Validate the node selections and any user input from the tool dialog components. <P> 
   * 
   * For non-interactive tools, this method is called when the user selects the menu item 
   * for the tool since there is no dialog to be shown or confirmed. <P> 
   * 
   * For interactive tools, this method is called when the tool dialog is confirmed.  If the 
   * dialog is closed without pressing the Confirm button or the Cancel button is pressed, 
   * this method and the {@link #execute execute} method will be skipped. <P> 
   * 
   * This method is executed in the Swing event thread.  Interactive tool subclasses should 
   * override this method to collect and validate any information needed from the dialog 
   * body components (see {@link #setDialogBody setDialogBody}).  Any information relevant 
   * to the execution of the tool (see {@link #execute execute}), should be stored in instance
   * fields by this method. <P> 
   * 
   * Both interactive and non-interactive tools should also check whether the set of selected
   * nodes are valid for the tool in this method. <P> 
   * 
   * All exceptions, including runtime exceptions, thrown in this method should be caught
   * and rethrown as a {@link PipelineException PipelineException}. <P>
   * 
   * @throws PipelineException 
   *   If unable to validate the current node selection or user input from dialog components.
   */ 
  public synchronized void 
  validate() 
    throws PipelineException 
  {}

  /**
   * Execute the tool. <P> 
   * 
   * Subclasses should implement this method to perform the actual functionality of the tool 
   * plugin. No user interface code should be executed from this method as it is run in its 
   * own thread to avoid locking the UI during the execution of the tool.  Any information 
   * needed by this method should be stored in the tool instance fields during execution 
   * of the {@link #validate validate} method. <P> 
   * 
   * The connections to the <B>plmaster</B>(1) and <B>plqueuemgr</B>(1) daemons passed as
   * arguments to this method can be used by the tool to perform any operation supported 
   * by the Pipeline API.  In addition, local OS level processes and be run and monitored 
   * using the {@link SubProcessLight SubProcessLight} and 
   * {@link SubProcessHeavy SubProcessHeavy} classes. <P>
   * 
   * When writing tools, it should be kept in mind that users will be prevented from 
   * performing other Pipeline operations in the UI until this method returns.  Tools with
   * very long execution times are probably better suited to being standalone programs
   * using the Pipeline API than interactive tools. <P> 
   * 
   * All exceptions, including runtime exceptions, thrown in this method should be caught
   * and rethrown as a {@link PipelineException PipelineException}. <P>
   * 
   * @param mclient
   *   The network connection to the plmaster(1) daemon.
   * 
   * @param qclient
   *   The network connection to the plqueuemgr(1) daemon.
   * 
   * @throws PipelineException 
   *   If unable to sucessfully execute the tool.
   */ 
  public synchronized void 
  execute
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {}
  
  

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


  /*----------------------------------------------------------------------------------------*/

  /**
   * The text displayed in the header of the dialog.
   */ 
  private String  pHeaderText;

  /**
   * The components which make up the dialog body of interactive tools.
   */ 
  private JComponent  pDialogBody; 

}



