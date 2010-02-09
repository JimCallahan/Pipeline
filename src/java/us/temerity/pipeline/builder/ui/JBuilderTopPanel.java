// $Id: JBuilderTopPanel.java,v 1.23 2009/03/26 00:10:01 jesse Exp $

package us.temerity.pipeline.builder.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BaseBuilder.*;
import us.temerity.pipeline.builder.execution.GUIExecution.*;
import us.temerity.pipeline.laf.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   T O P   P A N E L                                                      */
/*------------------------------------------------------------------------------------------*/

public 
class JBuilderTopPanel
  extends JPanel
  implements ComponentListener, TreeSelectionListener, ChangeListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  JBuilderTopPanel
  (
    BaseBuilder builder,
    JBuilderDialog parent
  ) 
  {
    super();
    pBuilder = builder;
    pParent = parent;
    
    pViewedYet = new ListMap<DefaultMutableTreeNode, Boolean>();
    pPanels = new ListMap<DefaultMutableTreeNode, JBuilderParamPanel>();
    pNeverViewed = new LinkedList<JBuilderParamPanel>();
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    JPanel rightPanel = new JPanel();
    rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
    

    {
      pTreePanel = new JPanel();
      pTreePanel.setLayout(new BoxLayout(pTreePanel, BoxLayout.LINE_AXIS));
      
      /* Creates the setup pass tree and adds it to the tab panel*/
      {
        DefaultMutableTreeNode root = 
          new DefaultMutableTreeNode(new BuilderTreeNodeInfo(""), true);
        DefaultTreeModel model = new DefaultTreeModel(root, true);
        
        pSetupTree = new JFancyTree(model); 
        pSetupTree.setName("DarkTree");
        
        pSetupTree.setCellRenderer(new JBuilderTreeCellRenderer());
        pSetupTree.getSelectionModel().setSelectionMode
          (TreeSelectionModel.SINGLE_TREE_SELECTION);
        pSetupTree.setExpandsSelectedPaths(true);
        
        JScrollPane scroll = 
          UIFactory.createScrollPane
          (pSetupTree, 
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
            new Dimension(300, 100), new Dimension(300, 250), null);
        
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
        topPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        
        {
          JPanel topBox = new JPanel();
          topBox.setLayout(new BoxLayout(topBox, BoxLayout.LINE_AXIS));
          JTextField field = UIFactory.createTextField("Setup Passes", 40, JLabel.CENTER);
          topBox.add(Box.createRigidArea(new Dimension(5, 0)));
          topBox.add(field);
          topBox.add(Box.createRigidArea(new Dimension(5, 0)));
          topPanel.add(topBox);
        }
        
        topPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        topPanel.add(scroll);
        
        pTreePanel.add(topPanel);
      }
      
      pTreePanel.add(UIFactory.createSidebar());
      
      /* Creates the construct pass tree and adds it to the tab panel*/
      {
        DefaultMutableTreeNode root = 
          new DefaultMutableTreeNode(new BuilderTreeNodeInfo(""), true);
        DefaultTreeModel model = new DefaultTreeModel(root, true);
        
        pConstructTree = new JFancyTree(model); 
        pConstructTree.setName("DarkTree");
        
        pConstructTree.setCellRenderer(new JBuilderTreeCellRenderer());
        pConstructTree.setSelectionModel(null);
        pConstructTree.setExpandsSelectedPaths(true);
        pConstructTree.addTreeSelectionListener(this);
        JScrollPane scroll = 
          UIFactory.createScrollPane
          (pConstructTree, 
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
            new Dimension(300, 100), new Dimension(300, 250), null);
        
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
        topPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        
        {
          JPanel topBox = new JPanel();
          topBox.setLayout(new BoxLayout(topBox, BoxLayout.LINE_AXIS));
          JTextField field = UIFactory.createTextField("Construct Passes", 40, JLabel.CENTER);
          topBox.add(Box.createRigidArea(new Dimension(5, 0)));
          topBox.add(field);
          topBox.add(Box.createRigidArea(new Dimension(5, 0)));
          topPanel.add(topBox);
        }
        
        topPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        topPanel.add(scroll);
        
        pTreePanel.add(topPanel);
        
      }
      rightPanel.add(pTreePanel);
    }

    {
      /* Creates the text area for logging. */
      {
	pLogArea = new JTextArea(0, 0);
	pLogArea.setWrapStyleWord(true);
	pLogArea.setEditable(false);
	pLogArea.setLineWrap(true);
	if (pBuilder.useBuilderLogging())
	  pBuilder.getBuilderInformation().getLogMgr().logToTextArea(pLogArea);
      }
      
      if (pBuilder.useBuilderLogging()) {
        JScrollPane scroll = 
          UIFactory.createScrollPane
          (pLogArea, 
           JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS, 
           JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
           new Dimension(300, 100), 
           new Dimension(600, 250), 
           null);

        //scroll.addComponentListener(this);
        rightPanel.add(Box.createVerticalStrut(14));
        rightPanel.add(scroll);
      }
    }
    {
      JPanel firstPass = new JPanel();
      CardLayout firstLayout = new CardLayout();
      firstPass.setLayout(firstLayout);
      JPanel empty = new JPanel();
      empty.add(UIFactory.createFiller(JBuilderParamPanel.returnWidth()));
      firstPass.add(empty, aEmpty);
      firstLayout.show(firstPass, aEmpty);
      
      firstPass.setMinimumSize(new Dimension(JBuilderParamPanel.returnWidth(), 500));
      firstPass.setPreferredSize(new Dimension(JBuilderParamPanel.returnWidth(), 500));
      firstPass.setMaximumSize(new Dimension(JBuilderParamPanel.returnWidth(), Integer.MAX_VALUE));
      
      this.add(firstPass);
      
      pFirstPassPanel = firstPass;
      pFirstPassLayouts = firstLayout;
    }

    this.add(UIFactory.createSidebar());
    

    if (builder.useBuilderLogging())
      this.add(rightPanel);
    else
      this.add(pTreePanel);
  }
  
  /**
   * Defers adding the Tree Selection Listioners until something else has happened.
   * <p>
   * It is currently called by the BuilderDialog that holds this panel and it adds the tree
   * listener to both this panel and to the top level dialog.  The dialog uses the tree 
   * listener to know when to change state on the buttons (it only activates the Next button
   * when all param panels have been viewed and set).  I think it is deferred here so that
   * the Dialog makes sure that these calls happen after the buttons it needs to modify have
   * already been created, avoiding null pointer errors.
   */
  public void
  setupListeners()
  {
    pSetupTree.addTreeSelectionListener((JBuilderDialog) this.getTopLevelAncestor());
    pSetupTree.addTreeSelectionListener(this);
  }
  
  /*-- SETUP PASSES ------------------------------------------------------------------------*/
  
  /**
   * Should make a new Builder Param Panel from the Setup Pass and the Builder passed in
   * and make it the current Active pass.
   * 
   * @param pass
   *   The current Setup Pass the parameters are for 
   * @param builder 
   *   The Builder the Setup Pass is from.
   * @throws PipelineException
   *   If bad things happen while creating the Builder Param Panel.  
   */
  public void
  addNextSetupPass
  (
    SetupPass pass,
    BaseBuilder builder
  ) 
    throws PipelineException
  {
    /* Marks the current panel as being done (which changes how it is displayed.*/
    //if (pActiveNode != null)
      //((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setDone();
    
    /* Marks the namer panels as done as well and clears the lists that were keeping
     * track of them.
     */
    {
      for (DefaultMutableTreeNode node : pViewedYet.keySet()) {
	((BuilderTreeNodeInfo) node.getUserObject()).setDone();
      }
      pViewedYet.clear();
      pNeverViewed.clear();
    }
    
    /* Gets the name and the pass number of the builder; uses that to make the param panel */
    //TODO, this is where I need to change the naming stuff.
    PrefixedName prefixName = new PrefixedName(builder.getPrefixedName(), pass.getName());
    PrefixedName realPrefixName = new PrefixedName(prefixName, "Pass Parameters");
    int passNum = builder.getCurrentPass();
    JBuilderParamPanel paramPanel = new JBuilderParamPanel(builder, passNum, pParent);
    
    /* Creates the tree node and marks it as active, but only if there are any params*/
    pActiveNode = createTreeNodes(realPrefixName.toString());
    if (paramPanel.numberOfParameters() > 0 && !paramPanel.allViewed())
      ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setActive();
    pPanels.put(pActiveNode, paramPanel);
    
    /* Creates panels for all the namers that have parameters. (actually we go ahead and make 
     * panels for all of them and just discard the ones that we aren't going to use.  A little
     * wasteful but such is life. */
    {
      Map<String, BaseNames> namers = builder.getNamers();
      for (String name : namers.keySet()) {
	BaseNames baseName = namers.get(name);

	PrefixedName prefixName2 = new PrefixedName(prefixName, name);
	JBuilderParamPanel namerPanel = new JBuilderParamPanel(baseName, 1, pParent);
	if (namerPanel.numberOfParameters() > 0) {
	  DefaultMutableTreeNode node = createTreeNodes(prefixName2.toString());
	  ((BuilderTreeNodeInfo) node.getUserObject()).setActive();
	  pFirstPassPanel.add(namerPanel, prefixName2.toString());
	  pViewedYet.put(node, false);
	  pPanels.put(node, namerPanel);
	  
	  /* Got to add them in the right order or it don't work*/
	  namerPanel.addChangeListener(pParent);
	  namerPanel.addChangeListener(this);
	  namerPanel.addChangeListener(namerPanel);
	}
	else
	  pNeverViewed.add(namerPanel);
      }
    }
    /* Makes sure the newly added node is the selection in the tree.
     * Actually, this needs to be a bit more complicated.  It should actually check
     * if the active node has any params, and if it doesn't, then you need to skip to
     * one of its children.  Finally, if there is nothing to select, you need to return
     * a signal that this pass should be skipped and the executor should advance to the
     * next pass.
     */
    {
      TreePath treePath = new TreePath(pActiveNode.getPath());
      pSetupTree.setSelectionPath(treePath);
    }
    pFirstPassPanel.add(paramPanel, realPrefixName.toString());
    pFirstPassLayouts.show(pFirstPassPanel, realPrefixName.toString());
    pViewedYet.put(pActiveNode, paramPanel.allViewed());
    
    /* Got to add them in the right order or it don't work*/
    paramPanel.addChangeListener(pParent);
    paramPanel.addChangeListener(this);
    paramPanel.addChangeListener(paramPanel);
  }
  
  /**
   * Returns a list of all the param panels which represent the current builder and 
   * its attendant namers.
   * @return
   *   The list of panels.
   */
  public LinkedList<JBuilderParamPanel>
  getCurrentBuilderParamPanels()
  {
    LinkedList<JBuilderParamPanel> toReturn = new LinkedList<JBuilderParamPanel>();
//    toReturn.add(pPanels.get(pActiveNode));
    
    for (DefaultMutableTreeNode node : pViewedYet.keySet())
      toReturn.add(pPanels.get(node));
    
    toReturn.addAll(pNeverViewed);
    
    return toReturn;
  }
  
  /**
   * Create the set of tree nodes corresponding to the given panel path.
   * 
   * @param path 
   *    The "-" separated panel title path.
   */ 
  @SuppressWarnings("unchecked")
  private DefaultMutableTreeNode 
  createTreeNodes
  (
   String path
  ) 
  {
    DefaultTreeModel model = (DefaultTreeModel) pSetupTree.getModel();
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) model.getRoot();

    String paths[] = path.split("-");
    int wk;
    for(wk=0; wk<paths.length; wk++) {
      DefaultMutableTreeNode next = null;
      {
	Enumeration e = parent.children();
	if(e != null) {
	  while(e.hasMoreElements()) {
	    DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) e.nextElement(); 
	    String name = ((BuilderTreeNodeInfo) tnode.getUserObject()).getText();
	    if(name.equals(paths[wk])) {
	      next = tnode;
	      break;
	    }
	  }
	}
      }

      if(next == null) {
	next = new DefaultMutableTreeNode(new BuilderTreeNodeInfo(paths[wk]), wk < (paths.length-1));
	model.insertNodeInto(next, parent, parent.getChildCount());
	TreePath treePath = new TreePath(next.getPath());
	pSetupTree.expandPath(treePath);
      }
      parent = next;
    }
    {
      TreePath treePath = new TreePath(parent.getPath());
      pSetupTree.setSelectionPath(treePath);
    }
    return parent;
  }
  
  /**
   * Determines if the user has looked at everything they're supposed to before advancing
   * to the next pass.
   * <p>
   * Looks at all the panels in the viewed yet data structure and checks if they have been
   * viewed.  As soon as it finds one that has not been viewed yet, it returns 
   * <code>false</code>.  Otherwise, it returns <code>true</code>.
   */
  public boolean
  allParamsReady()
  {
    for (boolean bool : pViewedYet.values())
      if (!bool)
	return bool;
    return true;
  }
  
  /**
   * Does the current set of panels have any parameters.
   * @return
   *   <code>true</code> if there are any parameters in the current set of panels.
   */
  public boolean
  hasParams()
  {
    for (DefaultMutableTreeNode node : pViewedYet.keySet() ) {
      JBuilderParamPanel panel = pPanels.get(node);
      if (panel.numberOfParameters() > 0)
        return true;
    }
    return false;
  }
  
  /*-- CONSTRUCT PASSES --------------------------------------------------------------------*/
  
  public void
  prepareConstructLoop
  (
    List<String> executionOrder
  )
  {
    //pFirstPassLayouts.show(pFirstPassPanel, aEmpty);
    DefaultTreeModel model = (DefaultTreeModel) pConstructTree.getModel();
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) model.getRoot();
    
    for (String name : executionOrder) {
      DefaultMutableTreeNode node = 
	new DefaultMutableTreeNode(new BuilderTreeNodeInfo(name), false);
      model.insertNodeInto(node, parent, parent.getChildCount());
    }
    pActiveNode = (DefaultMutableTreeNode) model.getChild(parent, 0);
    ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setActive();
  }
  
  public synchronized void
  makeNextActive()
  {
    DefaultTreeModel model = (DefaultTreeModel) pConstructTree.getModel();
    ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setDone();
    model.nodeChanged(pActiveNode);
    pActiveNode = pActiveNode.getNextSibling();
    if (pActiveNode != null)
      ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setActive();
    model.nodeChanged(pActiveNode);
  }
  
  public void
  releaseNodes()
    throws PipelineException
  {
    pBuilder.releaseNodes();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /*-- COMPONENT LISTENER METHODS ----------------------------------------------------------*/
  
  public void 
  componentHidden
  (
    ComponentEvent e
  )
  {}

  public void 
  componentMoved
  (
    ComponentEvent e
  )
  {}

  public void 
  componentResized
  (
    ComponentEvent e
  )
  {
    Component comp = e.getComponent();
    JScrollPane scroll = (JScrollPane) comp;
    Dimension size = scroll.getSize();
    Dimension logDim = pLogArea.getSize();
    Dimension newSize = new Dimension(size.width - 25, logDim.height);
    pLogArea.setSize(newSize);
    scroll.validate();
  }
  
  public void 
  componentShown
  (
    ComponentEvent e
  )
  {}
  
  /*-- TREE SELECTION LISTENER METHODS -----------------------------------------------------*/
  
  /**
   * Called whenever the value of the selection changes.
   */ 
  public void 
  valueChanged
  (
    TreeSelectionEvent e
  )
  {
    TreePath tpath = pSetupTree.getSelectionPath(); 
    DefaultMutableTreeNode newNode = null;
    if(tpath != null) {
      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
      if(tnode.isLeaf()) {
	String title = null;
	{
	  StringBuilder buf = new StringBuilder();
	  TreeNode path[] = tnode.getPath();
	  int wk;
	  for(wk=1; wk<path.length-1; wk++) {
	    BuilderTreeNodeInfo info = 
	      (BuilderTreeNodeInfo) ((DefaultMutableTreeNode) path[wk]).getUserObject(); 
	    buf.append(info.getText() + "-");
	  }
	  BuilderTreeNodeInfo info = 
	      (BuilderTreeNodeInfo) ((DefaultMutableTreeNode) path[wk]).getUserObject();
	  buf.append(info.getText());
	  title = buf.toString();
	  newNode = (DefaultMutableTreeNode) path[wk];
	}

	pFirstPassLayouts.show(pFirstPassPanel, title);
	LinkedList<DefaultMutableTreeNode> list = 
	  new LinkedList<DefaultMutableTreeNode>(pViewedYet.keySet()); 
	for (DefaultMutableTreeNode node : list) {
	  if (node.equals(newNode)) {
	    JBuilderParamPanel panel = pPanels.get(newNode); 
	    if (panel.allViewed()) {
	      pViewedYet.put(node, true);
	      ( (BuilderTreeNodeInfo) node.getUserObject()).setDone();
	    }
	  }
	}
	return;
      }
    }
    pFirstPassLayouts.show(pFirstPassPanel, " ");
  }
  
  /*-- CHANGE LISTENER METHODS -------------------------------------------------------------*/
  
  public void 
  stateChanged
  (
    ChangeEvent e
  )
  {
    JBuilderParamPanel panel = pPanels.get(pActiveNode);
    if (panel != null) {
      pViewedYet.put(pActiveNode, panel.allViewed());
      if (panel.allViewed()) {
        ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setDone();
        pSetupTree.repaint();
      }
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 634240817965602649L;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The instance of Base Builder which this GUI is representing.
   */
  private BaseBuilder pBuilder;

  /**
   * The Panel that contains the two trees displaying the execution order information.
   */
  private JPanel pTreePanel;
  
  /**
   * The panel which all the JBuilderParamPanels are added to.
   * <p>
   * This contains a Card Layout, the key into which is the prefixed name of the Builder
   * that is represented in the panel.
   */
  private JPanel pFirstPassPanel;
  
  /**
   * The text area the builder logs to when it is doing its own logging.
   */
  private JTextArea pLogArea;
  
  /**
   * The top level horizontal split pane which is always used.
   */
  //private JSplitPane pHorizSplitPane;
  
  /**
   * The interior split pane which is used when the Builder is doing its own logging.
   */
//  private JSplitPane pVertSplitPane;
  
  /**
   * The card layout for pFirstPassPanel, that new JBuilderParamPanels are added to.
   */
  private CardLayout pFirstPassLayouts;
  
  /**
   * The tree containing all the Setup Passes.
   */
  private JFancyTree pSetupTree;
  
  /**
   * The tree containing all the Construct Passes.
   */
  private JFancyTree pConstructTree;
  
  /**
   * The node in the Setup Tree that is currently selected.
   * <p>
   * It is set to <code>null</code> when the panel first initializes.
   */
  private DefaultMutableTreeNode pActiveNode;
  
  /**
   * A list of which BuilderParamPanels have been viewed yet, indexed by the
   * TreeNodes that represent each panel in the display
   */
  private ListMap<DefaultMutableTreeNode, Boolean> pViewedYet;
  
  /**
   * The list of BuilderParamPanels indexed by the tree node that represents them
   * in each panel
   */
  private ListMap<DefaultMutableTreeNode, JBuilderParamPanel> pPanels;
  
  /**
   * A list of Builder Param Panels for Namers with no parameters, which are never going 
   * to be viewed, which are returned with all the panels for the current Builder and 
   * has its parameters (none) assigned to the namer.  I don't think this is needed, but I
   * am not sure and do not want to take it out yet in case it breaks something.
   */
  private LinkedList<JBuilderParamPanel> pNeverViewed;
  
  /**
   * The parent dialog this is running in.
   */
  private JBuilderDialog pParent;
  
  /**
   * The name of the empty Card placed in the Card Layout upon initialization.
   */
  private static final String aEmpty = "Empty";
  
  private static final Icon sTabIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TabIcon.png"));
}
