// $Id: JNodeHistoryPanel.java,v 1.35 2009/10/30 04:56:31 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   H I S T O R Y   P A N E L                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays the check-in log messages associated with a node. <P> 
 */ 
public  
class JNodeHistoryPanel
  extends JBaseNodeDetailPanel
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
    /* initialize the popup menus */ 
    {
      initBasicMenus(false, false); 
      updateMenuToolTips();
    }

    /* initialize the panel components */ 
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  

      /* header */ 
      {
	JPanel panel = initHeader(false); 
	add(panel);
      }

      add(Box.createRigidArea(new Dimension(0, 4)));

      /* full node name */ 
      {
        initNameField(this);
        pNodeNameField.setFocusable(true);     
        pNodeNameField.addKeyListener(this);   
        pNodeNameField.addMouseListener(this); 
      }

      add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	pMessageBox = vbox;

	pTextAreas = new ArrayList<JTextArea>();

	{
	  JScrollPane scroll = UIFactory.createVertScrollPane(vbox);
	  pScroll = scroll;
	  
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

    updateNodeStatus(null, null, null);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the title of this type of panel.
   */
  @Override
  public String 
  getTypeName() 
  {
    return "Node History";
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the group ID. <P> 
   * 
   * Group ID values must be in the range: [1-9]
   * 
   * @param groupID
   *   The new group ID or (0) for no group assignment.
   */ 
  @Override
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

    master.updateOpsBar();
  }

  /**
   * Is the given group currently unused for this type of panel.
   */ 
  @Override
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

  /**
   * Are the contents of the panel read-only. <P> 
   */ 
  @Override
  public boolean
  isLocked() 
  {
    return (super.isLocked() && !pPrivilegeDetails.isNodeManaged(pAuthor));
  }

  /**
   * Set the author and view.
   */ 
  @Override
  public synchronized void 
  setAuthorView
  (
   String author, 
   String view 
  ) 
  {
    super.setAuthorView(author, view);    
    updatePanels();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update all panels which share the current update channel.
   */
  @Override
  public void 
  updatePanels() 
  {
    if (pGroupID != 0) {
      PanelUpdater pu = new PanelUpdater(this);
      pu.execute();
    }
  }

  /**
   * Apply the updated information to this panel.
   * 
   * @param author
   *   Owner of the current working area.
   * 
   * @param view
   *   Name of the current working area view.
   * 
   * @param status
   *   The current status for the node being displayed. 
   * 
   * @param history
   *   The check-in log messages.
   *
   * @param offline
   *   The revision numbers of the offline checked-in versions.
   */
  public synchronized void 
  applyPanelUpdates
  (
   String author, 
   String view,
   NodeStatus status,
   TreeMap<VersionID,LogMessage> history,
   TreeSet<VersionID> offline
  )
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);    

    updateNodeStatus(status, history, offline);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components to reflect the current check-in log messages.
   * 
   * @param status
   *   The current node status.
   * 
   * @param history
   *   The check-in log messages.
   *
   * @param offline
   *   The revision numbers of the offline checked-in versions.
   */
  private synchronized void 
  updateNodeStatus
  (
   NodeStatus status,
   TreeMap<VersionID,LogMessage> history,
   TreeSet<VersionID> offline
  ) 
  {
    super.updateNodeStatus(status, false);

    pHistory = history;

    NodeDetailsLight details = null;
    if(pStatus != null) 
      details = pStatus.getLightDetails();

    /* check-in message history */ 
    {
      pMessageBox.removeAll();
      pTextAreas.clear();

      if(pHistory != null) {
	VersionID initial = new VersionID();
	
	ArrayList<VersionID> vids = new ArrayList<VersionID>(pHistory.keySet());
	Collections.reverse(vids);
	
	VersionID wvid = null;
	if((details != null) && (details.getWorkingVersion() != null)) 
	  wvid = details.getWorkingVersion().getWorkingID();	

	for(VersionID vid : vids) {
	  LogMessage msg = pHistory.get(vid);
	  
	  Color color  = Color.white;
	  if((wvid != null) && wvid.equals(vid)) 
	    color  = Color.cyan;

	  String rootName = msg.getRootName();
	  
	  boolean isLeaf = ((rootName == null) || 
			    ((rootName != null) && (!rootName.equals(pStatus.getName()))));

	  {
	    JPanel panel = new JPanel();
	    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));  

	    panel.setFocusable(true);
	    panel.addKeyListener(this);
	    panel.addMouseListener(this); 
	  
	    panel.add(Box.createRigidArea(new Dimension(0, 3)));
	  
	    {
	      Box hbox = new Box(BoxLayout.X_AXIS);
	      
	      {
		Box hbox2 = new Box(BoxLayout.X_AXIS);
		hbox2.add(Box.createRigidArea(new Dimension(10, 0)));
		
		{
		  JLabel label = new JLabel("v" + vid);
		  label.setForeground(color);
		  label.setToolTipText(UIFactory.formatToolTip
                    ("The revision number of the checked-in version."));
		  hbox2.add(label);
		}
		
		hbox2.add(Box.createHorizontalGlue());
		hbox2.add(Box.createRigidArea(new Dimension(4, 0)));
	      
		{
		  String author = msg.getAuthor();
		  String impostor = msg.getImpostor();
		  
		  JLabel label;
		  if (impostor == null)
		    label = new JLabel(author);
		  else
		    label = new JLabel(author + " (" + impostor + ")");
		  label.setForeground(color);
		  label.setToolTipText(UIFactory.formatToolTip
                    ("The name of the user whose working area the version was created from " +
                     "followed by the name of the user who requested the check-in if it is " +
                     "different from the first name."));
		  hbox2.add(label);
		}
		
		hbox2.add(Box.createRigidArea(new Dimension(4, 0)));
		hbox2.add(Box.createHorizontalGlue());
		
		{
		  JLabel label = new JLabel(TimeStamps.format(msg.getTimeStamp()));
		  label.setForeground(color);
		  label.setToolTipText(UIFactory.formatToolTip
                    ("When the version was checked-in."));
		  hbox2.add(label);
		}
		
		hbox2.add(Box.createRigidArea(new Dimension(10, 0)));
		
		Dimension size = new Dimension(sSize, 19);
		hbox2.setMinimumSize(size);
		hbox2.setMaximumSize(size);
		hbox2.setPreferredSize(size);
		
		hbox.add(hbox2);
	      }
	      
	      hbox.add(Box.createHorizontalGlue());
	      
	      panel.add(hbox);
	    }
	    
	    panel.add(Box.createRigidArea(new Dimension(0, 3)));
	    
	    {
	      Box hbox = new Box(BoxLayout.X_AXIS);
	      
	      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	      
	      {
		JTextArea area = new JTextArea(msg.getMessage(), 0, 39);
		pTextAreas.add(area);
		
		area.setName(isLeaf ? "HistoryTextArea" : "HistoryTextAreaDark");
		area.setForeground(color);
		
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		
		area.setEditable(false);
		
		area.setFocusable(true);
		area.addKeyListener(this);
		area.addMouseListener(this); 
		
		hbox.add(area);
	      }
	      
	      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	      hbox.add(Box.createHorizontalGlue());
	      
	      panel.add(hbox);
	    }
	    
	    panel.add(Box.createRigidArea(new Dimension(0, 3)));

	    {
	      Box hbox = new Box(BoxLayout.X_AXIS);
	      
	      {
		Box hbox2 = new Box(BoxLayout.X_AXIS);
		
		hbox2.add(Box.createRigidArea(new Dimension(10, 0)));

		if(offline.contains(vid)) {
		  JLabel label = new JLabel("Offline");
		  label.setForeground(new Color(0.75f, 0.75f, 0.75f));
		  label.setToolTipText(UIFactory.formatToolTip
		    ("The checked-in version is currently offline."));
		  hbox2.add(label);
		}

		hbox2.add(Box.createHorizontalGlue());
		
		{
		  String text = null;
		  String tooltip = null;
		  if(rootName == null) {
		    text = "???";
		    tooltip = "The root check-in node is unknown.";
		  }
		  else if(rootName.equals(pStatus.getName())) {
		    text = "Check-In Root"; 
		    tooltip = "This node was the root of the check-in.";
		  }
		  else {
		    text = (rootName + "  v" + msg.getRootVersionID());
		    tooltip = "The name and revision number of the root check-in node.";
		  }
		  
		  JLabel label = new JLabel(text);
		  label.setForeground(new Color(0.75f, 0.75f, 0.75f));

		  label.setToolTipText(UIFactory.formatToolTip(tooltip));                    

		  hbox2.add(label);
		}
		
		hbox2.add(Box.createRigidArea(new Dimension(10, 0)));
		
		Dimension size = new Dimension(sSize, 19);
		hbox2.setMinimumSize(size);
		hbox2.setMaximumSize(size);
		hbox2.setPreferredSize(size);
		
		hbox.add(hbox2);
	      }
	      
	      hbox.add(Box.createHorizontalGlue());
	      
	      panel.add(hbox);
	    }
	  
	    panel.add(Box.createRigidArea(new Dimension(0, 3)));
	    
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
      
      pMessageBox.add(UIFactory.createFiller(sSize));
    }
      
    pMessageBox.revalidate();

    SwingUtilities.invokeLater(new ScrollTask());
  }




  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Scrolls to the top check-in message.
   */ 
  private
  class ScrollTask
    extends Thread
  {
    public 
    ScrollTask()
    {
      super("JBaseNodeHistoryPanel:ScrollTask");
    }

    @Override
    public void 
    run() 
    {    
      for(JTextArea area : pTextAreas) {
	area.setRows(area.getLineCount());		
	
	Dimension size = area.getPreferredSize();
	area.setMinimumSize(size);
	area.setMaximumSize(size);
      }

      pScroll.getViewport().setViewPosition(new Point());
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 7763696064974680919L;
  
  private static final int  sSize = 564;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The check-in log messages of the current node.
   */ 
  private TreeMap<VersionID,LogMessage>  pHistory;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The log message container.
   */ 
  private Box  pMessageBox;

  /**
   * The log message text areas.
   */
  private ArrayList<JTextArea>  pTextAreas; 

  /**
   * The scroll panel containing the messages.
   */ 
  private JScrollPane  pScroll; 

}
