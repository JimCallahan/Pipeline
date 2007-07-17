package us.temerity.pipeline.builder.ui;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.*;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.BaseBuilder;
import us.temerity.pipeline.builder.BaseNames;
import us.temerity.pipeline.builder.BaseBuilder.ConstructPass;
import us.temerity.pipeline.builder.BaseBuilder.SetupPass;
import us.temerity.pipeline.builder.BaseUtil.PrefixedName;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   T O P   P A N E L                                                      */
/*------------------------------------------------------------------------------------------*/

public 
class JBuilderTopPanel
  extends JPanel
  implements ComponentListener, TreeSelectionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public JBuilderTopPanel
  (
    BaseBuilder builder
  ) 
    throws PipelineException
  {
    super();
    pBuilder = builder;
    
    pViewedYet = new ListMap<DefaultMutableTreeNode, Boolean>();
    pPanels = new ListMap<DefaultMutableTreeNode, JBuilderParamPanel>();
    
    BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
    this.setLayout(layout);
    
    pSplitPane = new JHorzSplitPanel();
    pSplitPane.setDividerLocation(325);
    pSplitPane.setResizeWeight(0);
    
    pSecondSplitPane = new JVertSplitPanel();
    pSecondSplitPane.setDividerLocation(.5);
    pSecondSplitPane.setResizeWeight(.5);
    
    {
      {
	pLogArea = new JTextArea(0, 0);
	pLogArea.setWrapStyleWord(true);
	pLogArea.setEditable(false);
	pLogArea.setLineWrap(true);
	LogMgr.getInstance().logToTextArea(pLogArea);
      }
      
      JScrollPane scroll = new JScrollPane(pLogArea);
      scroll.addComponentListener(this);
      pSecondSplitPane.setBottomComponent(scroll);
    }
    {
      pTreeCardPanel = new JPanel();
      pTreeCardLayout = new CardLayout();
      pTreeCardPanel.setLayout(pTreeCardLayout);
      pTreeCardPanel.setMinimumSize(new Dimension(325, 0));
      pTreeCardPanel.setPreferredSize(new Dimension(325, 0));
      
      {
	DefaultMutableTreeNode root = new DefaultMutableTreeNode(new BuilderTreeNodeInfo(""), true);
	DefaultTreeModel model = new DefaultTreeModel(root, true);
	
	pTree = new JFancyTree(model); 
	pTree.setName("DarkTree");
	
	pTree.setCellRenderer(new JBuilderTreeCellRenderer());
	pTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	pTree.setExpandsSelectedPaths(true);
	JScrollPane scroll = new JScrollPane(pTree);
	scroll.setMinimumSize(new Dimension(325, 0));
	scroll.setPreferredSize(new Dimension(325, 0));
	pTreeCardPanel.add(scroll, aSetupPasses);
	pTreeCardLayout.show(pTreeCardPanel, aSetupPasses);
      }
      {
	DefaultMutableTreeNode root = new DefaultMutableTreeNode(new BuilderTreeNodeInfo(""), true);
	DefaultTreeModel model = new DefaultTreeModel(root, true);
	
	pSecondTree = new JFancyTree(model); 
	pSecondTree.setName("DarkTree");
	
	pSecondTree.setCellRenderer(new JBuilderTreeCellRenderer());
	pSecondTree.setSelectionModel(null);
	pSecondTree.setExpandsSelectedPaths(true);
	pSecondTree.addTreeSelectionListener(this);
	JScrollPane scroll = new JScrollPane(pSecondTree);
	pTreeCardPanel.add(scroll, aConstructPasses);
      }
      {
	JScrollPane scroll = new JScrollPane(pTreeCardPanel);
	scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	//pSplitPane.setLeftComponent(pTreeCardPanel);
	pSplitPane.setLeftComponent(scroll);
      }
    }
    {
      pFirstPassPanel = new JPanel();
      pFirstPassLayouts = new CardLayout();
      pFirstPassPanel.setLayout(pFirstPassLayouts);
      pFirstPassPanel.setAlignmentX(LEFT_ALIGNMENT);
      pFirstPassPanel.setAlignmentY(TOP_ALIGNMENT);
      //Dimension size = new Dimension(JBuilderParamPanel.returnWidth() * 3, 100);
      //pFirstPassPanel.setMinimumSize(size);
      //pFirstPassPanel.setPreferredSize(size);
      //pFirstPassPanel.setMaximumSize(new Dimension(size.width, Integer.MAX_VALUE ));      
    }
    {  
      pSecondSplitPane.setTopComponent(pFirstPassPanel);
    }
    {
      pSplitPane.setRightComponent(pSecondSplitPane);
    }
    {
      BaseBuilder theBuilder = pBuilder.getCurrentBuilder();
      SetupPass pass = pBuilder.getCurrentSetupPass();
      PrefixedName prefixName = new PrefixedName(theBuilder.getPrefixedName(), pass.getName());
      int passNum = theBuilder.getCurrentPass();
      
      JBuilderParamPanel paramPanel = new JBuilderParamPanel(theBuilder, passNum);
      pActiveNode = createTreeNodes(prefixName.toString());
      ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setActive();
      pFirstPassPanel.add(paramPanel, prefixName.toString());
      pPanels.put(pActiveNode, paramPanel);
      {
	Map<String, BaseNames> namers = theBuilder.getNamers();
	for (String name : namers.keySet()) {
	  BaseNames baseName = namers.get(name);
	  PrefixedName prefixName2 = new PrefixedName(theBuilder.getPrefixedName(), name);
	  JBuilderParamPanel namerPanel = new JBuilderParamPanel(baseName, 1);
	  DefaultMutableTreeNode node = createTreeNodes(prefixName2.toString());
	  ((BuilderTreeNodeInfo) node.getUserObject()).setActive();
	  pFirstPassPanel.add(namerPanel, prefixName2.toString());
	  pViewedYet.put(node, false);
	  pPanels.put(node, namerPanel);
	}
      }
      pFirstPassLayouts.show(pFirstPassPanel, prefixName.toString());
      {
	TreePath treePath = new TreePath(pActiveNode.getPath());
	pTree.setSelectionPath(treePath);
      }
      
      pFirstPassPanel.add(new JPanel(), aEmpty);
    }
    this.add(pSplitPane);
  }
  
  public void
  setupListeners()
  {
    pTree.addTreeSelectionListener((JBuilderParamDialog) this.getTopLevelAncestor());
    pTree.addTreeSelectionListener(this);
  }
  
  /*-- SETUP PASSES ------------------------------------------------------------------------*/
  
  public void
  addNextSetupPass() 
    throws PipelineException
  {
    ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setDone();
    {
      for (DefaultMutableTreeNode node : pViewedYet.keySet()) {
	((BuilderTreeNodeInfo) node.getUserObject()).setDone();
      }
      pViewedYet.clear();
    }
    
    BaseBuilder theBuilder = pBuilder.getCurrentBuilder();
    SetupPass pass = pBuilder.getCurrentSetupPass();
    PrefixedName prefixName = new PrefixedName(theBuilder.getPrefixedName(), pass.getName());
    int passNum = theBuilder.getCurrentPass();
    JBuilderParamPanel paramPanel = new JBuilderParamPanel(theBuilder, passNum);
    
    pActiveNode = createTreeNodes(prefixName.toString());
    ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setActive();
    pPanels.put(pActiveNode, paramPanel);
    {
      Map<String, BaseNames> namers = theBuilder.getNamers();
      for (String name : namers.keySet()) {
	BaseNames baseName = namers.get(name);
	PrefixedName prefixName2 = new PrefixedName(theBuilder.getPrefixedName(), name);
	JBuilderParamPanel namerPanel = new JBuilderParamPanel(baseName, 1);
	DefaultMutableTreeNode node = createTreeNodes(prefixName2.toString());
	((BuilderTreeNodeInfo) node.getUserObject()).setActive();
	pFirstPassPanel.add(namerPanel, prefixName2.toString());
	pViewedYet.put(node, false);
	pPanels.put(node, namerPanel);
      }
    }
    {
      TreePath treePath = new TreePath(pActiveNode.getPath());
      pTree.setSelectionPath(treePath);
    }
    setCurrentBuilderParamPanel(paramPanel, prefixName);
  }
  
  private void
  setCurrentBuilderParamPanel
  (
    JBuilderParamPanel paramPanel,
    PrefixedName prefixName
  )
  {
    pFirstPassPanel.add(paramPanel, prefixName.toString());
    pFirstPassLayouts.show(pFirstPassPanel, prefixName.toString());
  }
  
  public LinkedList<JBuilderParamPanel>
  getCurrentBuilderParamPanels()
  {
    LinkedList<JBuilderParamPanel> toReturn = new LinkedList<JBuilderParamPanel>();
    toReturn.add(pPanels.get(pActiveNode));
    
    for (DefaultMutableTreeNode node : pViewedYet.keySet())
      toReturn.add(pPanels.get(node));
    
    return toReturn;
  }
  
  /**
   * Create the set of tree nodes corresponding to the given panel path.
   * 
   * @param path 
   *    The "-" seperated panel title path.
   */ 
  @SuppressWarnings("unchecked")
  private DefaultMutableTreeNode 
  createTreeNodes
  (
   String path
  ) 
  {
    DefaultTreeModel model = (DefaultTreeModel) pTree.getModel();
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
	pTree.expandPath(treePath);
      }
      parent = next;
    }
    {
      TreePath treePath = new TreePath(parent.getPath());
      pTree.setSelectionPath(treePath);
    }
    return parent;
  }
  
  public boolean
  allParamsReady()
  {
    for (boolean bool : pViewedYet.values())
      if (!bool)
	return bool;
    return true;
  }
  
  /*-- CONSTRUCT PASSES --------------------------------------------------------------------*/
  
  public void
  prepareConstructLoop
  (
    LinkedList<ConstructPass> executionOrder
  )
  {
    pFirstPassLayouts.show(pFirstPassPanel, aEmpty);
    DefaultTreeModel model = (DefaultTreeModel) pSecondTree.getModel();
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) model.getRoot();
    
    for (ConstructPass pass : executionOrder) {
      String name = pass.toString();
      DefaultMutableTreeNode node = 
	new DefaultMutableTreeNode(new BuilderTreeNodeInfo(name), false);
      model.insertNodeInto(node, parent, parent.getChildCount());
    }
    pTreeCardLayout.show(pTreeCardPanel, aConstructPasses);
    pActiveNode = (DefaultMutableTreeNode) model.getChild(parent, 0);
    ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setActive();
  }
  
  public synchronized void
  makeNextActive()
  {
    DefaultTreeModel model = (DefaultTreeModel) pSecondTree.getModel();
    ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setDone();
    model.nodeChanged(pActiveNode);
    pActiveNode = pActiveNode.getNextSibling();
    if (pActiveNode != null)
      ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setActive();
    model.nodeChanged(pActiveNode);
  }
  
  public void
  disconnect()
  {
    pBuilder.disconnectClients();
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
    @SuppressWarnings("unused")
    ComponentEvent e
  )
  {}

  public void 
  componentMoved
  (
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    TreeSelectionEvent e
  )
  {
    TreePath tpath = pTree.getSelectionPath(); 
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
	  if (node.equals(newNode))
	    pViewedYet.put(node, true);
	}
	return;
      }
    }
    pFirstPassLayouts.show(pFirstPassPanel, " ");
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 634240817965602649L;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private BaseBuilder pBuilder;

  private JPanel pTreeCardPanel;
  private JPanel pFirstPassPanel;
  
  private JTextArea pLogArea;
  
  private JSplitPane pSplitPane;
  private JSplitPane pSecondSplitPane;
  
  private CardLayout pTreeCardLayout;
  private CardLayout pFirstPassLayouts;
  private JFancyTree pTree;
  private JFancyTree pSecondTree;
  
  private DefaultMutableTreeNode pActiveNode;
  
  private ListMap<DefaultMutableTreeNode, Boolean> pViewedYet;
  private ListMap<DefaultMutableTreeNode, JBuilderParamPanel> pPanels;
  
  private static final String aConstructPasses = "ConstructPasses";
  private static final String aSetupPasses = "SetupPasses";
  private static final String aEmpty = "Empty";
}
