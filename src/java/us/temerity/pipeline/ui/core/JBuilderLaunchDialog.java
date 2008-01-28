// $Id: JBuilderLaunchDialog.java,v 1.1 2008/01/28 11:58:51 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.*;
import javax.swing.tree.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.BaseBuilderCollection;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   L A U N C H   D I A L O G                                              */
/*------------------------------------------------------------------------------------------*/

public class JBuilderLaunchDialog
  extends JTopLevelDialog
  implements ActionListener
{

  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new dialog.
   */
  public JBuilderLaunchDialog()
  {
    super("Builder Launch");
    
    pCurrentCollection = null;

    JPanel topPanel= new JPanel();
    {
      topPanel.setName("MainPanel");
      topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    }
    
    {
      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];
      
      pPluginField = UIMaster.getInstance().createBuilderCollectionSelectionField(sVSize);
      
      tpanel.add(UIFactory.createFixedLabel("Builder Collection", sTSize, JLabel.RIGHT, "The selected Builer Collection"));
      vpanel.add(pPluginField);
      
      pPluginField.addActionListener(this);
      pPluginField.setActionCommand("plugin-changed");
      
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      
      pVersionField = 
        UIFactory.createTitledTextField(tpanel, "Plugin Version", sTSize, 
                                        vpanel, "-", sVSize, 
                                        "The version of the selected plugin");
      
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      
      pVendorField = 
        UIFactory.createTitledTextField(tpanel, "Plugin Vendor", sTSize, 
                                        vpanel, "-", sVSize, 
                                        "The vendor of the selected plugin");

      
      JDrawer drawer = new JDrawer("Selected Plugin", (JComponent) comps[2], true);
      topPanel.add(drawer);
    }
    
    topPanel.add(Box.createVerticalStrut(10));
    
    {
      DefaultMutableTreeNode root = new DefaultMutableTreeNode("Builders", true);
      DefaultTreeModel model = new DefaultTreeModel(root, true);

      JTree tree = new JFancyTree(model);
      pBuilderTree = tree;
      tree.setName("DarkTree");

      tree.setCellRenderer(new JUserPrefsTreeCellRenderer());
      tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      tree.setExpandsSelectedPaths(true);
      
      {
        Dimension size = new Dimension(sVSize + sTSize, sLHeight);

        JScrollPane scroll =
          UIFactory.createScrollPane
          (tree, 
           ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
           ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
           size, size, null);
           
        topPanel.add(scroll);
      }
    }
    
    topPanel.add(UIFactory.createFiller(sTSize + sVSize));
    super.initUI("Launch Builders:", topPanel, null, "Launch", null, "Close");
    doUpdate();
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Called to update the plugin field before displaying the dialog.
   */
  public void
  updateAll()
  {
    UIMaster master = UIMaster.getInstance();
    master.clearBuilderCollectionPluginCaches();
    master.updateBuilderCollectionPluginField(pPluginField);
  }
  
  /**
   * Method to allow an outside invoker to pass in params that the dialog will pass
   * to any builder which it launches.
   * <p>
   * @param topLevelParams
   *   Param values for the top level builder, whose name is not yet know.  This are in
   *   the standard format for command line params of a list of Keys and then the value
   *   of the parameter, given as a String.  Pass in <code>null</code> to clear out the
   *   params.
   * @param paramValues
   *   These are param values for Sub-Builders.  They are in a MultiMap format, with the
   *   map keys representing the names of the param keys and the MultiMap values being
   *   String representations of the parameter values.  Pass in <code>null</code> to clear 
   *   out the params.   
   */
  public void
  setParams
  (
    ListMap<LinkedList<String>, String> topLevelParams,
    MultiMap<String, String> paramValues
  )
  {
    if (topLevelParams == null)
      pTopLevelParams = new ListMap<LinkedList<String>, String>();
    else
      pTopLevelParams = new ListMap<LinkedList<String>, String>(topLevelParams);
    if (paramValues == null)
      pParamValues = new MultiMap<String, String>();
    else
      pParamValues = paramValues;
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run when the selected Builder Collection Changes.
   */
  private void
  doUpdate()
  {
    DefaultTreeModel model = (DefaultTreeModel) pBuilderTree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
    root.removeAllChildren();
    
    String pluginName = pPluginField.getPluginName();
    if (pluginName == null) {
      pVendorField.setText("-");
      pVersionField.setText("-");
      
      pCurrentCollection = null;
      model.setRoot(root);
    }
    else {
      String vendor = pPluginField.getPluginVendor();
      VersionID version = pPluginField.getPluginVersionID();
      try {
        pCurrentCollection = 
          PluginMgrClient.getInstance().newBuilderCollection(pluginName, version, vendor);
      } 
      catch (PipelineException ex) {
        pPluginField.setPlugin(null);
        pVendorField.setText("-");
        pVersionField.setText("-");
        UIMaster.getInstance().showErrorDialog(ex);
        return;
      }
      
      pVendorField.setText(vendor);
      pVersionField.setText(version.toString());
      
      LayoutGroup group = pCurrentCollection.getLayout();
      createBuilderTreeNodes(root, group);
      model.setRoot(root);
    }
  }
  
  /**
   * Recursive method for building a {@link JTree} from a {@link LayoutGroup}
   * @param parent
   *   The node that is going to be the parent of all nodes in the group.
   * @param group
   *   The group containing the entries.  The LayoutGroup must not contain <code>null</code>
   *   entries.
   */
  private void 
  createBuilderTreeNodes
  (
   DefaultMutableTreeNode parent,
   LayoutGroup group
  ) 
  {
    for (LayoutGroup sub : group.getSubGroups()) {
      if (sub.hasEntries()) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(sub.getName(), true);
        parent.add(node);
        createBuilderTreeNodes(node, sub);
      }
    }
    
    for (String entry : group.getEntries()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(entry, false);
      parent.add(node);      
    }
  }
  
  /**
   * Apply changes and continue. 
   */ 
  @Override
  public void 
  doApply()
  {
    TreePath path = pBuilderTree.getSelectionPath();
    if (path != null) {
      DefaultMutableTreeNode selected = (DefaultMutableTreeNode) path.getLastPathComponent();
      if (selected.isLeaf() && pCurrentCollection != null) {
        String builder = (String) selected.getUserObject();
        RunBuilder run = new RunBuilder(pCurrentCollection, builder);
        run.start();
      }
    }
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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
    if (cmd.equals("plugin-changed") ) {
      doUpdate();
    } 
    else {
      super.actionPerformed(e);
    }
    
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private 
  class RunBuilder
    extends Thread
  {
    private 
    RunBuilder
    (
      BaseBuilderCollection collection,
      String builderName
    )
    {
      pCollection = collection;
      pBuilderName = builderName;
    }
    
    @Override
    public void 
    run()
    {
      try {
        BaseBuilderCollection collection = 
          PluginMgrClient.getInstance().newBuilderCollection
            (pCollection.getName(), 
             pCollection.getVersionID(), 
             pCollection.getVendor());
       MultiMap<String, String> params = new MultiMap<String, String>(pParamValues);
       for (LinkedList<String> keys : pTopLevelParams.keySet()) {
         String value = pTopLevelParams.get(keys);
         keys.addFirst(pBuilderName);
         params.putValue(keys, value, true);
       }
        collection.instantiateBuilder(pBuilderName, null, null, true, true, false, params);
      } catch(Exception ex)
      {
        UIMaster.getInstance().showErrorDialog(ex);
      }
    }
    
    private BaseBuilderCollection pCollection;
    private String pBuilderName;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4017596445127372410L;
  
  private static final int sTSize  = 150;
  private static final int sVSize  = 150;
  
  protected static final int  sLWidth  = 240;
  protected static final int  sLHeight = 300;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private JTree pBuilderTree;
  
  private JPluginSelectionField pPluginField;
  private JTextField pVersionField;
  private JTextField pVendorField;
  
  private BaseBuilderCollection pCurrentCollection;
  
  private ListMap<LinkedList<String>, String> pTopLevelParams;
  private MultiMap<String, String>            pParamValues;

}
