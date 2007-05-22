/**
 * 
 */
package us.temerity.pipeline.builder.ui;

import java.awt.*;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.BaseBuilder;
import us.temerity.pipeline.ui.JFancyTree;
import us.temerity.pipeline.ui.UIFactory;
import us.temerity.pipeline.ui.core.JUserPrefsTreeCellRenderer;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   P A R A M   T R E E   P A N E L                                        */
/*------------------------------------------------------------------------------------------*/

/**
 *
 */
public class JBuilderParamTreePanel
  extends JPanel
  implements TreeSelectionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  JBuilderParamTreePanel()
  {
    super();
    this.setName("MainDialogPanel");
    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

      panel.add(UIFactory.createPanelLabel("Index:"));

      panel.add(Box.createRigidArea(new Dimension(0, 4)));

      {
	DefaultMutableTreeNode root = new DefaultMutableTreeNode("", true);
	DefaultTreeModel model = new DefaultTreeModel(root, true);

	JTree tree = new JFancyTree(model); 
	pTree = tree;
	tree.setName("DarkTree");

	tree.setCellRenderer(new JUserPrefsTreeCellRenderer());
	tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	tree.setExpandsSelectedPaths(true);
	tree.addTreeSelectionListener(this);

	{
	  JScrollPane scroll = new JScrollPane(pTree);

	  scroll.setMinimumSize(new Dimension(230, 120));
	  scroll.setPreferredSize(new Dimension(230, 250));

	  scroll.setHorizontalScrollBarPolicy
	  (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	  scroll.setVerticalScrollBarPolicy
	  (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	  panel.add(scroll);
	}
      }
      this.add(panel);
    }
    this.add(Box.createRigidArea(new Dimension(20, 0)));
    
    {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

      {
	Box hbox = new Box(BoxLayout.X_AXIS);

	hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	{
	  JLabel label = new JLabel(" ");
	  pCardLabel = label;
	  label.setName("PanelLabel");

	  hbox.add(label);
	}

	hbox.add(Box.createHorizontalGlue());

	Dimension size = new Dimension(410, hbox.getPreferredSize().height);
	hbox.setMinimumSize(size);
	hbox.setMaximumSize(size);

	panel.add(hbox);
      }

      panel.add(Box.createRigidArea(new Dimension(0, 4)));

      {
	CardLayout layout = new CardLayout();
	JPanel cpanel = new JPanel(layout);
	pCardPanel = cpanel;

	{ 
	  Component comps[] = UIFactory.createTitledPanels();
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];

	  tpanel.add(Box.createRigidArea(new Dimension(sTSize, 1)));
	  vpanel.add(Box.createRigidArea(new Dimension(sVSize, 1)));

	  UIFactory.addVerticalGlue(tpanel, vpanel);

	  cpanel.add(comps[2], " ");

	  layout.show(pCardPanel, " ");
	}
      }

      {
	JScrollPane scroll = new JScrollPane(pCardPanel);

	scroll.setMinimumSize(new Dimension(410, 120));
	scroll.setMaximumSize(new Dimension(410, Integer.MAX_VALUE));

	scroll.setHorizontalScrollBarPolicy
	  (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.setVerticalScrollBarPolicy
	  (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	scroll.getVerticalScrollBar().setUnitIncrement(23);

	panel.add(scroll);
      }

      this.add(panel);
    }
  }
  
  /**
   * Initialize the user interface components.
   * 
   * @param builder
   * 	The Builder having its interface initialized.
   */  
  public void initUI
  (
    BaseBuilder builder
  ) 
    throws PipelineException
  {
    pTree.removeAll();
    pCardPanel.removeAll();
    
    String treeName = builder.getName();
    String panelName = builder.getName();
    
    int pass = builder.getCurrentPass();
    
    createTreeNodes(treeName);
    JBuilderParamPanel panel = new JBuilderParamPanel(builder, pass);
    pCardPanel.add(panel, panelName);
    for(BaseBuilder hbp : builder.getSubBuilders().values()) {
      uiHelper(hbp, treeName, panelName);
    }
  }
  
  private void uiHelper
  (
    BaseBuilder hbp,
    String treeName,
    String panelName
  ) 
    throws PipelineException
  {
   String newTreeName = treeName + "|" + hbp.getName();
   String newPanelName = panelName + " - " + hbp.getName();
   
   int pass = hbp.getCurrentPass();
   
   createTreeNodes(newTreeName);
   JBuilderParamPanel panel = new JBuilderParamPanel(hbp, pass);
   pCardPanel.add(panel, newPanelName);
   if(hbp.allowsChildren()) {
      BaseBuilder builder = hbp;
      for(BaseBuilder hbp2 : builder.getSubBuilders().values()) {
	uiHelper(hbp2, treeName, panelName);
      }
    }
  }
  
  /**
   * Create the set of tree nodes corresponding to the given panel path.
   * 
   * @param path 
   *    The "|" seperated panel title path.
   */ 
  @SuppressWarnings("unchecked")
  protected void 
  createTreeNodes
  (
   String path
  ) 
  {
    DefaultTreeModel model = (DefaultTreeModel) pTree.getModel();
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) model.getRoot();

    String paths[] = path.split("\\|");
    int wk;
    for(wk=0; wk<paths.length; wk++) {
      DefaultMutableTreeNode next = null;
      {
	Enumeration e = parent.children();
	if(e != null) {
	  while(e.hasMoreElements()) {
	    DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) e.nextElement(); 
	    String name = (String) tnode.getUserObject();
	    if(name.equals(paths[wk])) {
	      next = tnode;
	      break;
	    }
	  }
	}
      }

      if(next == null) {
	next = new DefaultMutableTreeNode(paths[wk], wk < (paths.length-1));
	parent.add(next);
      }
      parent = next;
    }
    model.reload();
  }

  
  
  /*-- TREE SELECTION LISTENER METHODS -----------------------------------------------------*/
  
  /**
   * Called whenever the value of the selection changes.
   */ 
  public void valueChanged(TreeSelectionEvent e)
  {
    CardLayout layout = (CardLayout) pCardPanel.getLayout();

    TreePath tpath = pTree.getSelectionPath(); 
    if(tpath != null) {
      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
      if(tnode.isLeaf()) {
	String title = null;
	{
	  StringBuilder buf = new StringBuilder();
	  TreeNode path[] = tnode.getPath();
	  int wk;
	  for(wk=1; wk<path.length-1; wk++) 
	    buf.append(path[wk].toString() + " - ");
	  buf.append(path[wk].toString());
	  title = buf.toString();
	}

	pCardLabel.setText(title + ":");
	layout.show(pCardPanel, title);

	return;
      }
    }

    pCardLabel.setText(" ");
    layout.show(pCardPanel, " ");
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1527039533641872740L;

  protected static final int sTSize = 150;
  protected static final int sVSize = 210;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The builder index tree.
   */ 
  protected JTree pTree;

  /**
   * The title label of the builder panels.
   */ 
  protected JLabel pCardLabel;

  /**
   * The collection of builder panels.
   */ 
  protected JPanel pCardPanel;
}
