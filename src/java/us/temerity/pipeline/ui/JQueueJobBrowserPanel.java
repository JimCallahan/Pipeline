// $Id: JQueueJobBrowserPanel.java,v 1.1 2004/08/25 05:19:59 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.glue.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B   B R O W S E R   P A N E L                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A panel which displays the existing @{link QueueJobGroup QueueJobGroup}.
 */ 
public 
class JQueueJobBrowserPanel
  extends JTopLevelPanel
  implements MouseListener, KeyListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  public 
  JQueueJobBrowserPanel()
  {
    super();
    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JQueueJobBrowserPanel
  (
   JTopLevelPanel panel
  )
  {
    super(panel);
    initUI();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components.
   */ 
  private void 
  initUI()
  {
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  
      
      {
	JPanel panel = new JPanel();
	pHeaderPanel = panel;
	panel.setName("DialogHeader");	
	
	panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

	panel.addMouseListener(this); 
	panel.setFocusable(true);
	panel.addKeyListener(this);
      
	{
	  JLabel label = new JLabel("Job Browser:");
	  label.setName("DialogHeaderLabel");	
	  
	  panel.add(label);	  
	}
	
	panel.add(Box.createHorizontalGlue());
	
	{
	  JToggleButton btn = new JToggleButton();		
	  pFilterViewsButton = btn;
	  btn.setName("ViewsButton");

	  btn.setSelected(true);
		  
	  Dimension size = new Dimension(19, 19);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setActionCommand("filter-views");
	  btn.addActionListener(this);
	  
	  panel.add(btn);
	} 

	add(panel);
      }
      
      {
	JPanel panel = new JPanel();
	panel.setName("MainPanel");
	
	panel.setLayout(new BorderLayout());
	
	{
	  QueueJobGroupsTableModel model = new QueueJobGroupsTableModel();
	  pTableModel = model;
	  
	  JTablePanel tpanel =
	    new JTablePanel(model, model.getColumnWidths(), 
			    model.getRenderers(), model.getEditors());
	  pTablePanel = tpanel;

	  panel.add(tpanel);
	}
	
	add(panel);
      }
    }

    updateQueueJobs(null, null);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the group ID. <P> 
   * 
   * Group ID values must be in the range: [1-9]
   * 
   * @param groupID
   *   The new group ID or (0) for no group assignment.
   */ 
  public void
  setGroupID
  (
   int groupID
  )
  {
    UIMaster master = UIMaster.getInstance();

    PanelGroup<JQueueJobBrowserPanel> panels = master.getQueueJobBrowserPanels();

    if(pGroupID > 0)
      panels.releaseGroup(pGroupID);

    pGroupID = 0;
    if((groupID > 0) && panels.isGroupUnused(groupID)) {
      panels.assignGroup(this, groupID);
      pGroupID = groupID;
    }
  }

  /**
   * Is the given group currently unused for this type of panel.
   */ 
  public boolean
  isGroupUnused
  (
   int groupID
  ) 
  {
    PanelGroup<JQueueJobBrowserPanel> panels = 
      UIMaster.getInstance().getQueueJobBrowserPanels();
    return panels.isGroupUnused(groupID);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the job groups and states.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param groups
   *   The queue job groups indexe by job group ID.
   * 
   * @param states
   *   The job states indexed by job ID.
   */ 
  public synchronized void
  updateQueueJobs
  (
   String author, 
   String view, 
   TreeMap<Long,QueueJobGroup> groups, 
   TreeMap<Long,JobState> states
  ) 
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);

    updateQueueJobs(groups, states);
  }

  /**
   * Update the job groups and states.
   * 
   * @param groups
   *   The queue job groups indexe by job group ID.
   * 
   * @param states
   *   The job states indexed by job ID.
   */ 
  public synchronized void
  updateQueueJobs
  (
   TreeMap<Long,QueueJobGroup> groups, 
   TreeMap<Long,JobState> states
  ) 
  {
    TreeMap<Long,QueueJobGroup> gs = groups; 
    if((groups != null) && pFilterViewsButton.isSelected()) {
      gs = new TreeMap<Long,QueueJobGroup>(); 
      for(QueueJobGroup group : groups.values()) {
	NodeID nodeID = group.getNodeID();
	if(nodeID.getAuthor().equals(pAuthor) && nodeID.getView().equals(pView)) 
	  gs.put(group.getGroupID(), group);
      }
    }

    pTableModel.setQueueJobGroups(gs, states);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- MOUSE LISTENER METHODS --------------------------------------------------------------*/

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on a component. 
   */ 
  public void 
  mouseClicked(MouseEvent e) {}
   
  /**
   * Invoked when the mouse enters a component. 
   */
  public void 
  mouseEntered
  (
   MouseEvent e
  ) 
  {
    pHeaderPanel.requestFocusInWindow();
  }

  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited
  (
   MouseEvent e
  ) 
  {
    KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
  }
   
  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed(MouseEvent e) {}

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {}



  /*-- KEY LISTENER METHODS ----------------------------------------------------------------*/

  /**
   * invoked when a key has been pressed.
   */   
  public void 
  keyPressed
  (
   KeyEvent e
  )
  {
    UserPrefs prefs = UserPrefs.getInstance();

    if((prefs.getJobBrowserUpdate() != null) &&
       prefs.getJobBrowserUpdate().wasPressed(e))
      doUpdate();
  }

  /**
   * Invoked when a key has been released.
   */ 
  public void 	
  keyReleased(KeyEvent e) {}

  /**
   * Invoked when a key has been typed.
   */ 
  public void 	
  keyTyped(KeyEvent e) {} 



  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    String cmd = e.getActionCommand();
    if(cmd.equals("filter-views")) 
      doUpdate();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the status of all jobs and job groups.
   */ 
  private void
  doUpdate()
  { 
    GetJobsTask task = new GetJobsTask();
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public synchronized void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    encoder.encode("FilterView", pFilterViewsButton.isSelected());
  }

  public synchronized void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    Boolean filter = (Boolean) decoder.decode("FilterView");
    if(filter != null) 
      pFilterViewsButton.setSelected(filter);

    super.fromGlue(decoder);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the current job groups and job states.
   */ 
  private
  class GetJobsTask
    extends Thread
  {
    public 
    GetJobsTask() 
    {
      super("JQueueJobBrowserPanel:GetJobsTask");
    }
 
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();

      TreeMap<Long,QueueJobGroup> groups = null;
      TreeMap<Long,JobState> states = null;
      if(master.beginPanelOp("Updating Jobs...")) {
	try {
	  QueueMgrClient client = master.getQueueMgrClient();
	  
	  groups = client.getJobGroups(); 
	  states = client.getAllJobStates();
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }
	
      UpdateTask task = new UpdateTask(groups, states);
      SwingUtilities.invokeLater(task);
    }
  }
  
  /** 
   * Update the UI components with the new job group and job state information.
   */ 
  private
  class UpdateTask
    extends Thread
  {
    public 
    UpdateTask
    (
     TreeMap<Long,QueueJobGroup> groups, 
     TreeMap<Long,JobState> states
    ) 
    {
      super("JQueueJobBrowserPanel:UpdateTask");

      pGroups = groups;
      pStates = states; 
    }

    public void 
    run() 
    {
      updateQueueJobs(pGroups, pStates);

      //UIMaster master = UIMaster.getInstance(); 
//       {
// 	PanelGroup<JQueueJobViewerPanel> panels = master.getQueueJobViewerPanels();
// 	JQueueJobViewerPanel panel = panels.getPanel(pGroupID);
// 	if(panel != null) {
// 	  panel.updateQueueJobs(pAuthor, pView, pJobGroups, pJobStates);
// 	  panel.updateManagerTitlePanel();
// 	}
//       }
    }
    
    private TreeMap<Long,QueueJobGroup>  pGroups; 
    private TreeMap<Long,JobState>       pStates; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -507775432755443313L;

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The header panel.
   */ 
  private JPanel pHeaderPanel; 

  /**
   * Used to select whether job groups should be filtered by the current owner|view. 
   */ 
  private JToggleButton  pFilterViewsButton;

  /**
   * The job groups table model.
   */ 
  private QueueJobGroupsTableModel  pTableModel;

  /**
   * The job groups table panel.
   */ 
  private JTablePanel  pTablePanel;


}
