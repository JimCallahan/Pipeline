package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Map.*;
import java.util.concurrent.atomic.*;

import javax.swing.*;
import javax.swing.event.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.ui.core.JJobListCellRenderer.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   M O N I T O R   D I A L O G                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Dialog that allows users to monitor the jobs they have submitted to the queue.
 */
public 
class JJobMonitorDialog
  extends JTopLevelDialog
  implements ActionListener, MouseListener, ListSelectionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JJobMonitorDialog() 
  {
    super("Job Monitor");

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    pAllJobGroups = new TreeMap<Long, QueueJobGroup>();
    pAllJobGroupsLock = new Object();
    
    pFinishedJobGroupIDs = new TreeMap<Long, Long>();
    
    pJobDistributions = new TreeMap<Long, double[]>();
    pJobDistributionsLock = new Object();
    
    pActiveJobGroupIDs = new TreeSet<Long>();
    pActiveJobGroupsLock = new Object();
    
    pCurrentSelection = new TreeSet<Long>();
    pCurrentSelectionLock = new Object();
    
    /* Job Group Menu*/
    {
      JMenuItem item;
      
      pJobGroupMenu = new JPopupMenu();
      
      item = new JMenuItem("Show Nodes");
      pShowNodeItem = item;
      item.setActionCommand("show-node");
      item.addActionListener(this);
      pJobGroupMenu.add(item);
      
      item = new JMenuItem("Show Jobs");
      pShowJobItem = item;
      item.setActionCommand("show-job");
      item.addActionListener(this);
      pJobGroupMenu.add(item);
    }
    
    {
      pNodeChannelMenu = (JPopupMenu) UIFactory.createGroupMenu(this, "node")[0];
      pJobChannelMenu = (JPopupMenu) UIFactory.createGroupMenu(this, "job")[0];
    }
    
    
    {
      JPanel mainPanel = new JPanel();
      mainPanel.setName("MainPanel");
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.LINE_AXIS));
      
      mainPanel.add(Box.createRigidArea(new Dimension(8, 0)));
      
      {
        Box vbox = new Box(BoxLayout.Y_AXIS);   
        
        vbox.add(Box.createRigidArea(new Dimension(0, 8)));
        
        {
          Box headerBox = new Box(BoxLayout.X_AXIS);
          JLabel label = 
            UIFactory.createFixedLabel("Node Channel:", 100, SwingConstants.LEFT);
          headerBox.add(label);
          headerBox.add(Box.createHorizontalStrut(4));
          
          {
            GroupMenuAnchor anchor = new GroupMenuAnchor(pNodeChannelMenu);
            pNodeMenuAnchor = anchor;
            
            anchor.setToolTipText(UIFactory.formatToolTip("The node channel selector."));
            headerBox.add(anchor);    
          }
          
          headerBox.add(Box.createHorizontalStrut(16));
          label = 
            UIFactory.createFixedLabel("Job Channel:", 100, SwingConstants.LEFT);
          headerBox.add(label);
          headerBox.add(Box.createHorizontalStrut(4));
          
          {
            GroupMenuAnchor anchor = new GroupMenuAnchor(pJobChannelMenu);
            pJobMenuAnchor = anchor;
            
            anchor.setToolTipText(UIFactory.formatToolTip("The job channel selector."));
            headerBox.add(anchor);    
          }
          
          headerBox.add(Box.createHorizontalGlue());
          
          vbox.add(headerBox);
        }
        
        vbox.add(Box.createVerticalStrut(12));
        
        {
          JList lst = new JList(new DefaultListModel());
          pJobList = lst;

          lst.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
          lst.setCellRenderer(new JJobListCellRenderer());

//          lst.addListSelectionListener(this);
          lst.addMouseListener(this);
          
          {
            JScrollPane scroll = 
              UIFactory.createScrollPane
              (lst, 
               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
               ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
               new Dimension(150, 150), 
               new Dimension(375, 450), null);

            pListScrollPane = scroll;
            vbox.add(scroll);
          }
        }
        
        mainPanel.add(vbox);
      }
      
      mainPanel.add(Box.createRigidArea(new Dimension(8, 0)));
      super.initUI("Job Monitor:", mainPanel, null, null, null, null, null);
      doNodeGroup(1);
      doJobGroup(2);
    }
  }
  
  public static void 
  main
  (
    String[] args
  ) 
    throws PipelineException
  {
    PluginMgrClient.init();
    UIMaster.init(null, false, false, false, true, false, false, false);
    UIFactory.initializePipelineUI();
    LogMgr.getInstance().setLevel(Kind.Job, Level.Finest);
    JJobMonitorDialog dialog = new JJobMonitorDialog();
    QueueMgrClient client = new QueueMgrClient();
    QueueJobGroup group = client.getJobGroup(1322l);
    LinkedList<QueueJobGroup> groups = new LinkedList<QueueJobGroup>();
    groups.add(group);
    groups.add(client.getJobGroup(1323l));
    dialog.addJobGroups(groups);
    dialog.setVisible(true);
    PluginMgrClient.getInstance().disconnect();
  }
  
  /**
   * Add job groups that will be monitored by this panel.
   * 
   * @param jobGroups
   *   The list of Job Groups to add.
   */
  public void
  addJobGroups
  (
    List<QueueJobGroup> jobGroups  
  )
  {
    if (jobGroups != null) {
      synchronized (pAllJobGroupsLock) {
        for (QueueJobGroup group : jobGroups) {
          Long groupID = group.getGroupID();
          if (!pAllJobGroups.containsKey(groupID))
            pAllJobGroups.put(groupID, group);
        }        
      }
      SwingUtilities.invokeLater(new UpdateUIThread());
    }
  }
  
  /**
   * Method to update all the job group information.
   */
  public void
  updateJobGroupInfo()
  {
    UIMaster master = UIMaster.getInstance();
    QueueMgrClient qclient = master.acquireQueueMgrClient();
    LogMgr log = LogMgr.getInstance();
    
    TreeSet<Long> activeJobGroups = new TreeSet<Long>(Collections.reverseOrder());
    synchronized (pAllJobGroupsLock) {
      activeJobGroups.addAll(pAllJobGroups.keySet());
    }
    
    activeJobGroups.removeAll(pFinishedJobGroupIDs.keySet());
    
    synchronized (pActiveJobGroupsLock) {
      pActiveJobGroupIDs = activeJobGroups;
    }
    
    log.log
      (Kind.Job, Level.Fine, 
       "Updating job states for the following job groups: " + activeJobGroups);
    
    int finished = JobState.Finished.ordinal();
    int aborted = JobState.Aborted.ordinal();
    int failed = JobState.Failed.ordinal();
    
    TreeSet<Long> finishedGroups = new TreeSet<Long>();
    TreeSet<Long> garbageGroups = new TreeSet<Long>();
    try {
      TreeMap<Long, double[]> dists = qclient.getJobStateDistribution(activeJobGroups);
      
      for (Entry<Long, double[]> entry : dists.entrySet()) {
        double[] states = entry.getValue();
        Long key = entry.getKey();
        
        double totalJobs = 0;
        double doneJobs = 0;
        
        if (states.length == 0 ) {
          garbageGroups.add(key);
          log.log
            (Kind.Job, Level.Finer, 
             "No job state information found for job: " + key);
          break;
        }
        
        for (int i = 0 ; i < states.length; i++) {
          totalJobs += states[i];
          if (i == finished || i == aborted || i == failed)
            doneJobs += states[i];
        }
        if (doneJobs == totalJobs)
          finishedGroups.add(key);
      }
      
      synchronized (pJobDistributionsLock) {
        pJobDistributions.putAll(dists);
      }
      
      long time = System.currentTimeMillis();
      
      for (Entry<Long, Long> entry : pFinishedJobGroupIDs.entrySet()) {
        if (( time - entry.getValue()) >= sJobRetensionTime ) {
          Long key = entry.getKey();
          garbageGroups.add(key);
          log.log
            (Kind.Job, Level.Finer, 
             "The job group (" + key + ") has expired and is being removed.");

        }
      }
      
      synchronized (pAllJobGroupsLock) {
        for (Long groupID : garbageGroups) {
          pAllJobGroups.remove(groupID);
        }
      }
      
      synchronized (pJobDistributionsLock) {
        for (Long groupID : garbageGroups) {
          pJobDistributions.remove(groupID);
        }
      }

      for (Long groupID : garbageGroups) {
        pFinishedJobGroupIDs.remove(groupID);
      }
      
      for (Long groupID : finishedGroups)
        pFinishedJobGroupIDs.put(groupID, time);
    }
    catch (PipelineException ex ) {
      master.showErrorDialog(ex);
    }
    finally {
      master.releaseQueueMgrClient(qclient);
    }
  }
  
  /**
   * Redraw the UI. <p>
   * 
   * This should only be called from a Swing UI thread.
   */
  public void
  redrawDialog()
  {
    pJobList.removeListSelectionListener(this);

    /* Don't let the user change the selection while we're fiddling with the list.*/
    synchronized (pCurrentSelectionLock) {
      TreeSet<Long> selectedGroups = getSelectedGroups();
      
      DefaultListModel model = (DefaultListModel) pJobList.getModel();
      model.clear();

      LinkedList<Long> groupIDs = new LinkedList<Long>();
      
      TreeSet<Long> activeGroupIDs ;
      synchronized (pActiveJobGroupsLock) {
        activeGroupIDs = pActiveJobGroupIDs; 
      }
      
      TreeSet<Long> finishedGroupIDs = new TreeSet<Long>(Collections.reverseOrder());
      synchronized (pAllJobGroupsLock) {
        finishedGroupIDs.addAll(pAllJobGroups.keySet());
      }
      
      finishedGroupIDs.removeAll(activeGroupIDs);
      
      groupIDs.addAll(activeGroupIDs);
      groupIDs.addAll(finishedGroupIDs);

      int index = 0;
      LinkedList<Integer> selectedIndexes = new LinkedList<Integer>(); 
      for (Long groupID : groupIDs) {
        QueueJobGroup group;
        synchronized (pAllJobGroupsLock) {
          group = pAllJobGroups.get(groupID);
        }
        if (group == null)
          continue;
        
        double dist[];
        synchronized (pJobDistributionsLock) {
          dist = pJobDistributions.get(groupID);
        }
        if (dist == null)
          dist = new double[JobState.values().length];

        JJobListCellRenderer.JobListCellData data = 
          new JobListCellData(group, dist);

        model.addElement(data);
        
        if (selectedGroups.contains(group.getGroupID()))
          selectedIndexes.add(index);
        
        index++;
      }
     
      int[] array = new int[selectedIndexes.size()];
      int wrk = 0;
      for (int idx : selectedIndexes)
        array[wrk++] = idx;
      pJobList.setSelectedIndices(array);
      
      pJobList.addListSelectionListener(this);
      
      pListScrollPane.validate();  
    }
  }
  
  @Override
  public void 
  setVisible
  (
    boolean isVisible
  )
  {
    if (isVisible) {
      pUpdateThread = new JobGroupUpdator(this);
      pUpdateThread.start();
    }
    else {
      pUpdateThread.pUpdating.set(false);
    }
      
    super.setVisible(isVisible);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /*-- LIST SELECTION LISTENER METHODS -----------------------------------------------------*/

  /**
   * Called whenever the value of the selection changes.
   */ 
  public void   
  valueChanged
  (
   ListSelectionEvent e
  )
  {
    /* synchronize to prevent selection from updating while redrawing*/
    synchronized (pCurrentSelectionLock) {
      pCurrentSelection = getSelectedGroups();
    }
  }
  
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
  mouseEntered(MouseEvent e) {} 
  
  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited(MouseEvent e) {} 
  
  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed
  (
   MouseEvent e
  )
  {
    int mods = e.getModifiersEx();
    switch(e.getButton()) {
    case MouseEvent.BUTTON3:
      int on1  = (MouseEvent.BUTTON3_DOWN_MASK);
      
      int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
                  MouseEvent.BUTTON2_DOWN_MASK | 
                  MouseEvent.SHIFT_DOWN_MASK |
                  MouseEvent.ALT_DOWN_MASK |
                  MouseEvent.CTRL_DOWN_MASK);
     
      /* BUTTON3: popup menu */ 
      if((mods & (on1 | off1)) == on1) {
        Component comp = e.getComponent();
        if(comp == pJobList) {
          pJobGroupMenu.show(comp, e.getX(), e.getY());
        }
      }
    }
  }
  
  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e){}
  
  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  @Override
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    String cmd = e.getActionCommand();
    
    if (cmd.startsWith("node"))
      doNodeGroup(Integer.valueOf(cmd.substring(5)));
    else if (cmd.startsWith("job"))
      doJobGroup(Integer.valueOf(cmd.substring(4)));
    else if (cmd.equals("show-node"))
      doShowNodes();
    else if (cmd.equals("show-job"))
      doShowJobs();
    else
      super.actionPerformed(e);
  }
  
  /**
   * Change the node group.
   */ 
  private void 
  doNodeGroup
  (
    int groupID
  )
  {
    pNodeChannel = groupID;
    pNodeMenuAnchor.setIcon(UIFactory.sGroupIcons[groupID]);
  }
  
  /**
   * Change the job group.
   */ 
  private void 
  doJobGroup
  (
    int groupID
  )
  {
    pJobChannel = groupID;
    pJobMenuAnchor.setIcon(UIFactory.sGroupIcons[groupID]);
  }
  
  private void
  doShowNodes()
  {
    TreeSet<Long> select;
    synchronized (pCurrentSelectionLock) {
     select = new TreeSet<Long>(pCurrentSelection); 
    }
    if (select.size() > 0) {
      String author = null;
      String view = null;
      TreeSet<String> nodeNames = new TreeSet<String>();

      for (Long id : select) {
        QueueJobGroup group;
        synchronized (pAllJobGroupsLock) {
          group = pAllJobGroups.get(id);
        }
        if (group != null) {
          NodeID nodeID = group.getNodeID();
          if (author == null) {
            author = nodeID.getAuthor();
            view = nodeID.getView();
            nodeNames.add(nodeID.getName());
          }
          else 
            if (author.equals(nodeID.getAuthor()) && view.equals(nodeID.getView()))
              nodeNames.add(nodeID.getName());
        }
      }

      UIMaster.getInstance().selectAndShowNodes(pNodeChannel, author, view, nodeNames);
    }
  }

  private void
  doShowJobs()
  {
    TreeSet<Long> select;
    synchronized (pCurrentSelectionLock) {
     select = new TreeSet<Long>(pCurrentSelection); 
    }
    
    if (select.size() > 0) {
      UIMaster.getInstance().selectAndShowJobGroups(pJobChannel, select);
    }
  }
  
  /*----------------------------------------------------------------------------------------*/
  


  /**
   * Get the list of job group ids which are currently selected.
   */
  private TreeSet<Long>
  getSelectedGroups()
  {
    TreeSet<Long> toReturn = new TreeSet<Long>();
    
    Object selected[] = pJobList.getSelectedValues();
    for (Object o : selected) {
      long id = ((JobListCellData) o).getJobGroup().getGroupID();
      toReturn.add(id);
    }
    
    return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A anchor icon which shows the group popup menu when pressed.
   */ 
  private
  class GroupMenuAnchor
    extends JLabel
    implements MouseListener
  {
    GroupMenuAnchor
    (
      JPopupMenu groupMenu  
    )
    {
      super();
      
      pGroupPopup = groupMenu;

      setIcon(UIFactory.sGroupIcons[0]);

      Dimension size = new Dimension(19, 19);
      setMinimumSize(size);
      setMaximumSize(size);
      setPreferredSize(size);
      
      addMouseListener(this);
    }


    /*-- MOUSE LISTENER METHODS ------------------------------------------------------------*/

    /**
     * Invoked when the mouse button has been clicked (pressed and released) on a component. 
     */ 
    public void 
    mouseClicked(MouseEvent e) {}
    
    /**
     * Invoked when the mouse enters a component. 
     */
    public void 
    mouseEntered(MouseEvent e) {}

    /**
     * Invoked when the mouse exits a component. 
     */ 
    public void 
    mouseExited(MouseEvent e) {}

    /**
     * Invoked when a mouse button has been pressed on a component. 
     */
    public void 
    mousePressed
    (
     MouseEvent e
    )
    {
      int mods = e.getModifiersEx();
      
      int on1  = (MouseEvent.BUTTON1_DOWN_MASK);                  
      
      int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
                  MouseEvent.BUTTON2_DOWN_MASK | 
                  MouseEvent.SHIFT_DOWN_MASK | 
                  MouseEvent.ALT_DOWN_MASK |
                  MouseEvent.CTRL_DOWN_MASK);

      int on2  = (MouseEvent.BUTTON3_DOWN_MASK);                  
      
      int off2 = (MouseEvent.BUTTON1_DOWN_MASK | 
                  MouseEvent.BUTTON2_DOWN_MASK | 
                  MouseEvent.SHIFT_DOWN_MASK | 
                  MouseEvent.ALT_DOWN_MASK |
                  MouseEvent.CTRL_DOWN_MASK);
      
      /* BUTTON3: popup menu */ 
      if(((mods & (on1 | off1)) == on1) ||
         ((mods & (on2 | off2)) == on2)) {
        handleAnchorMouseEvent(e);
      }
    }

    /**
     * Invoked when a mouse button has been released on a component. 
     */ 
    public void 
    mouseReleased(MouseEvent e) {}

    /**
     * Handle popup anchor mouse events.
     */ 
    public void
    handleAnchorMouseEvent
    (
     MouseEvent e 
    ) 
    {
      int wk;
      
      pGroupPopup.show(e.getComponent(), e.getX(), e.getY()); 
    }

    
    /*-- STATIC INTERNALS ------------------------------------------------------------------*/

    private static final long serialVersionUID = 8811675052095600218L;

    
    /*-- INTERNALS -------------------------------------------------------------------------*/
    
    private JPopupMenu pGroupPopup;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  private
  class UpdateUIThread
    extends Thread
  {
    @Override
    public void run()
    {
      redrawDialog();
    }
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  private
  class JobGroupUpdator
    extends Thread
  {
    private
    JobGroupUpdator
    (
      JJobMonitorDialog dialog  
    )
    {
      pDialog = dialog;
      
      pUpdating = new AtomicBoolean(true);
    }
    
    @Override
    public void 
    run()
    {
      while (pUpdating.get()) {
        updateJobGroupInfo();
        SwingUtilities.invokeLater(new UpdateUIThread());
        try {
          sleep(sUpdateTime);
        }
        catch (InterruptedException ex) {
          LogMgr.getInstance().log
            (Kind.Ops, Level.Severe, 
             "An error occurred in the job monitor dialog updating thread");
        }
      }
    }
    
    private JJobMonitorDialog pDialog;
    
    public AtomicBoolean pUpdating;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 8370358388185079279L;
  
  /**
   * The maximum amount of time that a finished job group will be retained in milliseconds.
   * <p>
   * 
   * 20 minutes.
   */
  private static final long sJobRetensionTime = 1000 * 60 * 20;

  /**
   * The amount of time the dialog will sleep between updates. <p>
   * 
   * 5 seconds.
   */
  private static final long sUpdateTime = 1000 * 5;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*
   * So how to best design this.  Who needs access to info.
   * 
   * 1.  Drawing thread
   *   Job Groups
   *   Job Distributions
   * 2.  Add New Job Group
   *   Job Groups
   * 3.  Update Thread
   *   Job Groups
   *   Job Distributions
   *   
   * Data Structures
   * 
   * A. List of new job groups.
   *   Added to by (2)
   *   Removed by (1)
   * B. List of job groups
   *   Added to by (1)
   *   Read by (3)
   *   Removed from by (3)
   * C. List of finished job groups
   *   Added to by (3)
   *   Removed from by (3)
   * D. List of Job Distributions
   *   Added to by (3)
   *   Removed from by (3)
   *   
   * Locks need.
   *   New Jobs
   *   Current Job Groups  
   */
  
  /**
   * The set of job groups which were added since the last update.
   */
//  private LinkedList<QueueJobGroup> pNewJobGroups;
//  private Object pNewJobGroupsLock;

  /**
   * All the job groups with currently being monitored by this panel with the timestamp of the
   * last time they were updated.
   */
  private TreeMap<Long, QueueJobGroup> pAllJobGroups;
  private Object pAllJobGroupsLock;
  
  /**
   * The job groups which currently all there jobs finished and the timestamp when they 
   * completed.
   */
  private TreeMap<Long, Long> pFinishedJobGroupIDs;
  
  /**
   * Current list of the job groups which have active jobs on the farm. <p>
   * 
   * Updated bucket-brigade style by the updator thread, so that it can always be acquired 
   * and used cleanly.
   */
  private TreeSet<Long> pActiveJobGroupIDs;
  private Object pActiveJobGroupsLock;
  
  /**
   * A mapping of the current job states.
   */
  private TreeMap<Long, double[]> pJobDistributions;
  private Object pJobDistributionsLock;
  
  private Integer pNodeChannel;
  private Integer pJobChannel;

  /**
   * The thread running the job group updator.
   */
  private JobGroupUpdator pUpdateThread;
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U I   E L E M E N T S                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  private JPopupMenu pJobGroupMenu;
  
  private JMenuItem  pShowNodeItem;
  private JMenuItem  pShowJobItem;

  private JPopupMenu pNodeChannelMenu;
  private GroupMenuAnchor pNodeMenuAnchor;
  private JPopupMenu pJobChannelMenu;
  private GroupMenuAnchor pJobMenuAnchor;
  
  private JList pJobList;
  private JScrollPane pListScrollPane;
  
  /**
   * 
   */
  private TreeSet<Long> pCurrentSelection;
  private Object pCurrentSelectionLock;
}
