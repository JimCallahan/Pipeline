// $Id: JNodeHistoryPanel.java,v 1.6 2004/09/27 04:54:35 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.core.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   H I S T O R Y   P A N E L                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays the check-in log messages associated with a node. <P> 
 */ 
public  
class JNodeHistoryPanel
  extends JTopLevelPanel
  implements MouseListener, KeyListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  public 
  JNodeHistoryPanel()
  {
    super();

    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JNodeHistoryPanel
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
    /* initialize the panel components */ 
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  

      /* header */ 
      {
	JPanel panel = new JPanel();	

	panel.setName("DialogHeader");	
	panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

	{
	  JLabel label = new JLabel("X");
	  pHeaderLabel = label;
	  
	  label.setName("DialogHeaderLabel");	       

	  panel.add(label);	  
	}

	panel.add(Box.createHorizontalGlue());
      
	add(panel);
      }

      add(Box.createRigidArea(new Dimension(0, 4)));

      /* full node name */ 
      {
	Box hbox = new Box(BoxLayout.X_AXIS);
	
	hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	{
	  JTextField field = UIMaster.createTextField(null, 100, JLabel.LEFT);
	  pNodeNameField = field;
	  
	  field.setFocusable(true);
	  field.addKeyListener(this);
	  field.addMouseListener(this); 

	  hbox.add(field);
	}

	hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	add(hbox);
      }
	
      add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	pMessageBox = vbox;
	
	{
	  JScrollPane scroll = new JScrollPane(vbox);
	  
	  scroll.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	  scroll.setVerticalScrollBarPolicy
	    (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	  scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

	  add(scroll);
	}
      }

      Dimension size = new Dimension(sSize+22, 120);
      setMinimumSize(size);
      setPreferredSize(size); 

      setFocusable(true);
      addKeyListener(this);
      addMouseListener(this); 
    }

    updateNodeStatus(null, null);
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

    PanelGroup<JNodeHistoryPanel> panels = master.getNodeHistoryPanels();

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
    PanelGroup<JNodeHistoryPanel> panels = UIMaster.getInstance().getNodeHistoryPanels();
    return panels.isGroupUnused(groupID);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the UI components to reflect the current check-in log messages.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param status
   *   The current node status.
   * 
   * @param history
   *   The check-in log messages.
   */
  public synchronized void 
  updateNodeStatus
  (
   String author, 
   String view, 
   NodeStatus status,
   TreeMap<VersionID,LogMessage> history
  ) 
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);

    updateNodeStatus(status, history);
  }

  /**
   * Update the UI components to reflect the current check-in log messages.
   * 
   * @param status
   *   The current node status.
   * 
   * @param history
   *   The check-in log messages.
   */
  public synchronized void 
  updateNodeStatus
  (
   NodeStatus status,
   TreeMap<VersionID,LogMessage> history
  ) 
  {
    pStatus  = status;
    pHistory = history;

    NodeDetails details = null;
    if(pStatus != null) 
      details = pStatus.getDetails();

    /* header */ 
    {
      {
	String name = "Blank-Normal";
	if(pStatus != null) {
	  if(details != null) {
	    if(details.getOverallNodeState() == OverallNodeState.NeedsCheckOut) {
	      VersionID wvid = details.getWorkingVersion().getWorkingID();
	      VersionID lvid = details.getLatestVersion().getVersionID();
	      switch(wvid.compareLevel(lvid)) {
	      case Major:
		name = ("NeedsCheckOutMajor-" + details.getOverallQueueState() + "-Normal");
		break;
		
	      case Minor:
		name = ("NeedsCheckOut-" + details.getOverallQueueState() + "-Normal");
		break;
		
	      case Micro:
		name = ("NeedsCheckOutMicro-" + details.getOverallQueueState() + "-Normal");
	      }
	    }
	    else {
	      name = (details.getOverallNodeState() + "-" + 
		      details.getOverallQueueState() + "-Normal");
	    }
	  }
	  
	  pHeaderLabel.setText(pStatus.toString());
	  pNodeNameField.setText(pStatus.getName());
	}
	else {
	  pHeaderLabel.setText(null);
	  pNodeNameField.setText(null);
	}

	try {
	  pHeaderLabel.setIcon(TextureMgr.getInstance().getIcon(name));
	}
	catch(IOException ex) {
	  Logs.tex.severe("Internal Error:\n" + 
			  "  " + ex.getMessage());
	  Logs.flush();
	  System.exit(1);
	} 
      }
    }

    /* check-in message history */ 
    {
      pMessageBox.removeAll();

      if(pHistory != null) {
	VersionID initial = new VersionID();
	
	ArrayList<VersionID> vids = new ArrayList<VersionID>(pHistory.keySet());
	Collections.reverse(vids);
	
	VersionID wvid = null;
	if((details != null) && (details.getWorkingVersion() != null)) 
	  wvid = details.getWorkingVersion().getWorkingID();	

	for(VersionID vid : vids) {
	  LogMessage msg = pHistory.get(vid);
	  
	  Color color = Color.white;
	  if((wvid != null) && wvid.equals(vid))
	    color = Color.cyan;

	  {
	    JPanel panel = new JPanel();
	    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));  

	    panel.setFocusable(true);
	    panel.addKeyListener(this);
	    panel.addMouseListener(this); 
	  
	    panel.add(Box.createRigidArea(new Dimension(0, 4)));
	  
	    {
	      Box hbox = new Box(BoxLayout.X_AXIS);
	      
	      {
		Box hbox2 = new Box(BoxLayout.X_AXIS);
		hbox2.add(Box.createRigidArea(new Dimension(6, 0)));
		
		{
		  JLabel label = new JLabel("v" + vid);
		  label.setForeground(color);
		  hbox2.add(label);
		}
		
		hbox2.add(Box.createHorizontalGlue());
		hbox2.add(Box.createRigidArea(new Dimension(4, 0)));
	      
		{
		  JLabel label = new JLabel(msg.getAuthor());
		  label.setForeground(color);
		  hbox2.add(label);
		}
		
		hbox2.add(Box.createHorizontalGlue());
		hbox2.add(Box.createRigidArea(new Dimension(4, 0)));
		
		{
		  JLabel label = new JLabel(Dates.format(msg.getTimeStamp()));
		  label.setForeground(color);
		  hbox2.add(label);
		}
		
		hbox2.add(Box.createRigidArea(new Dimension(6, 0)));
		
		Dimension size = new Dimension(sSize, 19);
		hbox2.setMinimumSize(size);
		hbox2.setMaximumSize(size);
		hbox2.setPreferredSize(size);
		
		hbox.add(hbox2);
	      }
	      
	      hbox.add(Box.createHorizontalGlue());
	      
	      panel.add(hbox);
	    }
	    
	    panel.add(Box.createRigidArea(new Dimension(0, 4)));
	    
	    {
	      Box hbox = new Box(BoxLayout.X_AXIS);
	      
	      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	      
	      {
		JTextArea area = new JTextArea(msg.getMessage(), 2, 42);
		area.setName("TextArea");
		
		area.setForeground(color);
		
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		
		area.setEditable(false);
		
		area.setFocusable(true);
		area.addKeyListener(this);
		area.addMouseListener(this); 
		
		Dimension size = area.getPreferredSize();
		area.setMinimumSize(size);
		area.setMaximumSize(size);
		
		hbox.add(area);
	      }
	      
	      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	      hbox.add(Box.createHorizontalGlue());
	      
	      panel.add(hbox);
	    }
	    
	    panel.add(Box.createRigidArea(new Dimension(0, 4)));

	    pMessageBox.add(panel);
	  }

	  if(!vid.equals(initial)) {
	    JPanel spanel = new JPanel();
	    spanel.setName("Spacer");
	    
	    spanel.setMinimumSize(new Dimension(sSize, 7));
	    spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));
	    spanel.setPreferredSize(new Dimension(sSize, 7));
	    
	    pMessageBox.add(spanel);
	  }
	}
      }
      
      {
	JPanel spanel = new JPanel();
	spanel.setName("Spacer");
	
	spanel.setMinimumSize(new Dimension(sSize, 7));
	spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	spanel.setPreferredSize(new Dimension(sSize, 7));
	
	pMessageBox.add(spanel);
      }
    }
      
    pMessageBox.revalidate();
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
    requestFocusInWindow();
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
  mousePressed
  (
   MouseEvent e
  )
  {
    pManagerPanel.handleManagerMouseEvent(e);
  }

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
    /* manager panel hotkeys */ 
    if(pManagerPanel.handleManagerKeyEvent(e)) 
      return;

    /* local hotkeys */ 
    switch(e.getKeyCode()) {
    case KeyEvent.VK_SHIFT:
    case KeyEvent.VK_ALT:
    case KeyEvent.VK_CONTROL:
      break;
      
    default:
      Toolkit.getDefaultToolkit().beep();
    }
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



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 7763696064974680919L;

  private static final int  sSize = 484;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The current node status.
   */ 
  private NodeStatus  pStatus;

  /**
   * The check-in log messages of the current node.
   */ 
  private TreeMap<VersionID,LogMessage>  pHistory;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The node name/state header.
   */ 
  private JLabel pHeaderLabel;
  
  /**
   * The fully resolved node name field.
   */ 
  private JTextField pNodeNameField;


  /**
   * The log message container.
   */ 
  private Box  pMessageBox;

}
