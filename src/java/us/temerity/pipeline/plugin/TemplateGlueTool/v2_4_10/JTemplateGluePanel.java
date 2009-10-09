// $Id: JTemplateGluePanel.java,v 1.1 2009/10/09 04:40:08 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_10;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.ui.*;
import us.temerity.pipeline.builder.v2_4_10.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   G L U E   P A N E L                                                  */
/*------------------------------------------------------------------------------------------*/

public 
class JTemplateGluePanel
  extends JPanel
{
  public
  JTemplateGluePanel
  (
    JTemplateGlueDialog parentDialog, 
    String rootNode,
    TemplateGlueInformation oldSettings, 
    TreeSet<String> nodesInTemplate, 
    MasterMgrClient mclient, 
    String author, 
    String view
  )
  {
    super();
    
    pFinished = false;
    
    pParentDialog = parentDialog;
    pOldSettings = oldSettings;
    pNodesInTemplate = nodesInTemplate;
    pClient = mclient;
    pRootNode = rootNode;
    pAuthor = author;
    pView = view;
    
    pPanels = new TreeMap<TemplateGlueStage, JPanel>();
    
    pCurrentStage = TemplateGlueStage.ScanNetwork;
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

    {
      pTreePanel = new JPanel();
      
      pTreePanel.setLayout(new BoxLayout(pTreePanel, BoxLayout.PAGE_AXIS));
      
      pTreePanel.add(Box.createRigidArea(new Dimension(0, 4)));
      
      /* Add the header. */
      {
        JPanel topBox = new JPanel();
        topBox.setLayout(new BoxLayout(topBox, BoxLayout.LINE_AXIS));
        JTextField field = UIFactory.createTextField("Template Step", 40, JLabel.CENTER);
        topBox.add(Box.createRigidArea(new Dimension(5, 0)));
        topBox.add(field);
        topBox.add(Box.createRigidArea(new Dimension(5, 0)));
        pTreePanel.add(topBox);
      }
      
      DefaultMutableTreeNode root = 
        new DefaultMutableTreeNode(new BuilderTreeNodeInfo(""), true);
      DefaultTreeModel model = new DefaultTreeModel(root, true);
      
      pSettingTree = new JFancyTree(model); 
      pSettingTree.setName("DarkTree");
      
      pSettingTree.setCellRenderer(new JBuilderTreeCellRenderer());
      pSettingTree.setSelectionModel(null);
      pSettingTree.setExpandsSelectedPaths(true);
      JScrollPane scroll = 
        UIFactory.createScrollPane
        (pSettingTree, 
          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
          ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
          new Dimension(190, 350), null , null);

      pTreePanel.add(Box.createRigidArea(new Dimension(0, 4)));
      pTreePanel.add(scroll);
      pTreePanel.setPreferredSize(new Dimension(190, 500));
      pTreePanel.setMaximumSize(new Dimension(190, 500));
      
      
      for (TemplateGlueStage stage : TemplateGlueStage.values()) {
        DefaultMutableTreeNode node = 
          new DefaultMutableTreeNode(new BuilderTreeNodeInfo(stage.toString()), false);
        model.insertNodeInto(node, root, root.getChildCount());
      }
      pActiveNode = (DefaultMutableTreeNode) model.getChild(root, 0);
      ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setActive();
    }
    
    {
      JPanel firstPass = new JPanel();
      CardLayout firstLayout = new CardLayout();
      firstPass.setLayout(firstLayout);
      JPanel p = new ScanNetworkPanel(pRootNode);
      pPanels.put(pCurrentStage, p);
      firstPass.add(p, pCurrentStage.toString());
      firstLayout.show(firstPass, pCurrentStage.toString());

      
      firstPass.setMinimumSize(new Dimension(740, 500));
      firstPass.setPreferredSize(new Dimension(740, 500));
//      firstPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
      
      pSettingsPanel = firstPass;
      pSettingsLayouts = firstLayout;
      
    }

    this.add(pTreePanel);
    this.add(Box.createRigidArea(new Dimension(2, 0)));
    this.add(UIFactory.createSidebar());
    this.add(pSettingsPanel);    
    
  }
  
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  public TemplateGlueInformation
  getNewSettings()
  {
    return pNewSettings;
  }
  
  public boolean
  isFinished()
  {
    return pFinished;
  }
  
  
  public void
  doNext() 
    throws PipelineException
  {
    pParentDialog.disableButtons();
    TemplateGlueStage nextStage = TemplateGlueStage.getNextStage(pCurrentStage);
    switch(pCurrentStage) {
    case ScanNetwork:
      {
        pScan = new TemplateNetworkScan(pClient, pAuthor, pView, pRootNode, pNodesInTemplate);
        pScan.scan();
        JPanel panel = new InitialSummaryPanel(pScan, pOldSettings);
        pPanels.put(nextStage, panel);
        pSettingsPanel.add(panel, nextStage.toString());
        pSettingsLayouts.show(pSettingsPanel, nextStage.toString());
        break;
      }
    case InitialSummary:
      {
        JPanel panel = pPanels.get(nextStage);
        if (panel == null) {
          panel = new StringReplacePanel(pOldSettings);
          pPanels.put(nextStage, panel);
          pSettingsPanel.add(panel, nextStage.toString());
        }
        pSettingsLayouts.show(pSettingsPanel, nextStage.toString());
        break;
      }
    case StringReplacement:
      {
        try {
         StringReplacePanel panel = (StringReplacePanel) pPanels.get(pCurrentStage);
         pReplacements = panel.getReplacements();
         pReplacementParamNames = panel.getReplacementParamNames();
         pReplacementDefaults = panel.getReplacementDefaults();
        }
        catch(PipelineException ex) {
          JErrorDialog edialog = new JErrorDialog(pParentDialog);
          edialog.setMessage("Problem detected", ex.getMessage());
          edialog.setVisible(true);
          pParentDialog.enableButtons();
          return;
        }
        
         JPanel panel = pPanels.get(nextStage);
         if (panel == null) {
           panel = new AoEModePanel(pParentDialog, pScan, pOldSettings);
           pPanels.put(nextStage, panel);
           pSettingsPanel.add(panel, nextStage.toString());
         }
         pSettingsLayouts.show(pSettingsPanel, nextStage.toString());
         break;
      }
    case AoEModes:
      {
        try {
          AoEModePanel panel = (AoEModePanel) pPanels.get(pCurrentStage);
          pAOEModes = panel.getAoEModes();
         }
         catch(PipelineException ex) {
           JErrorDialog edialog = new JErrorDialog(pParentDialog);
           edialog.setMessage("Problem detected", ex.getMessage());
           edialog.setVisible(true);
           pParentDialog.enableButtons();
           return;
         }
      
         JPanel panel = pPanels.get(nextStage);
         if (panel == null) {
           panel = new OptionalBranchPanel(pScan, pOldSettings);
           pPanels.put(nextStage, panel);
           pSettingsPanel.add(panel, nextStage.toString());
         }
         pSettingsLayouts.show(pSettingsPanel, nextStage.toString());
         break;
      }
    case OptionalBranches:
      {
        try {
          OptionalBranchPanel panel = (OptionalBranchPanel) pPanels.get(pCurrentStage);
          pOptionalBranches = panel.getOptionalBranches();
         }
         catch(PipelineException ex) {
           JErrorDialog edialog = new JErrorDialog(pParentDialog);
           edialog.setMessage("Problem detected", ex.getMessage());
           edialog.setVisible(true);
           pParentDialog.enableButtons();
           return;
         }
         JPanel panel = pPanels.get(nextStage);
         if (panel == null) {
           panel = new ContextPanel(pScan, pOldSettings);
           pPanels.put(nextStage, panel);
           pSettingsPanel.add(panel, nextStage.toString());
         }
         pSettingsLayouts.show(pSettingsPanel, nextStage.toString());
         break;
      }
    case Contexts:
      {
        try {
          ContextPanel panel = (ContextPanel) pPanels.get(pCurrentStage);
          pContexts = panel.getContexts();
          pContextParamNames = panel.getContextParamNames();
         }
         catch(PipelineException ex) {
           JErrorDialog edialog = new JErrorDialog(pParentDialog);
           edialog.setMessage("Problem detected", ex.getMessage());
           edialog.setVisible(true);
           pParentDialog.enableButtons();
           return;
         }
         JPanel panel = new ContextsDefaultsPanel(pOldSettings, pContexts);
         pPanels.put(nextStage, panel);
         pSettingsPanel.add(panel, nextStage.toString());
         pSettingsLayouts.show(pSettingsPanel, nextStage.toString());
         break;
      }
    case ContextDefaults:
      {
        {
          ContextsDefaultsPanel panel = (ContextsDefaultsPanel) pPanels.get(pCurrentStage);
          pContextDefaults = panel.getContextDefaults();
        }

        JPanel panel = new FrameRangePanel(pParentDialog, pScan, 
                                           pOldSettings, pContexts.keySet());
        pPanels.put(nextStage, panel);
        pSettingsPanel.add(panel, nextStage.toString());
        pSettingsLayouts.show(pSettingsPanel, nextStage.toString());
        break;
      }
    case FrameRanges:
      {
        try {
          FrameRangePanel panel = (FrameRangePanel) pPanels.get(pCurrentStage);
          pFrameRanges = panel.getFrameRanges();
          pFrameRangeDefaults = panel.getFrameRangeDefaults();
        }
        catch(PipelineException ex) {
          JErrorDialog edialog = new JErrorDialog(pParentDialog);
          edialog.setMessage("Problem detected", ex.getMessage());
          edialog.setVisible(true);
          pParentDialog.enableButtons();
          return;
        }
        JPanel panel = new ExternalsPanel(pParentDialog, pScan, pOldSettings, pContexts.keySet());
        pPanels.put(nextStage, panel);
        pSettingsPanel.add(panel, nextStage.toString());
        pSettingsLayouts.show(pSettingsPanel, nextStage.toString());
        break;
      }
    case Externals:
      {
        {
          ExternalsPanel panel = (ExternalsPanel) pPanels.get(pCurrentStage);
          pExternals = panel.getExternals();
        }
        
        TemplateGlueInformation info = new TemplateGlueInformation
          ("Template", "Built with the TemplateGlueTool");

        info.setNodesInTemplate(pNodesInTemplate);
        info.setReplacements(pReplacements);
        info.setReplacementParamNames(pReplacementParamNames);
        info.setReplacementDefaults(pReplacementDefaults);
        info.setContexts(pContexts);
        info.setContextParamNames(pContextParamNames);
        info.setContextDefaults(pContextDefaults);
        info.setFrameRanges(pFrameRanges);
        info.setFrameRangeDefaults(pFrameRangeDefaults);
        info.setAOEModes(pAOEModes);
        info.setExternals(pExternals);
        info.setOptionalBranches(pOptionalBranches);
        
        pNewSettings = info;
        
        FinalSummaryPanel panel = new FinalSummaryPanel(pScan, pOldSettings, pNewSettings);
        pPanels.put(nextStage, panel);
        pSettingsPanel.add(panel, nextStage.toString());
        pSettingsLayouts.show(pSettingsPanel, nextStage.toString());
        break;
      }
    case FinalSummary:
      {
        pFinished = true;
        pParentDialog.doFinal();
        break;
      }
    }
    
    pCurrentStage = nextStage;
    
    DefaultTreeModel model = (DefaultTreeModel) pSettingTree.getModel();
    ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setDone();
    model.nodeChanged(pActiveNode);
    pActiveNode = pActiveNode.getNextSibling();
    if (pActiveNode != null)
      ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setActive();
    model.nodeChanged(pActiveNode);
    
    pParentDialog.enableButtons();
  }
  
  public void
  goBack()
  {
    TemplateGlueStage previousStage = TemplateGlueStage.getPreviousStage(pCurrentStage);
    if (previousStage == null)
      return;
    pSettingsLayouts.show(pSettingsPanel, previousStage.toString());
    pCurrentStage = previousStage;
    
    pFinished = false;
    
    DefaultTreeModel model = (DefaultTreeModel) pSettingTree.getModel();
    ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setNotDone();
    model.nodeChanged(pActiveNode);
    pActiveNode = pActiveNode.getPreviousSibling();
    if (pActiveNode != null)
      ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setActive();
    model.nodeChanged(pActiveNode);
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 216823893784570949L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private TemplateGlueInformation pOldSettings;
  private TemplateGlueInformation pNewSettings;
  private TreeSet<String> pNodesInTemplate;
  private MasterMgrClient pClient;
  private String pRootNode;
  
  /**
   * Panel that contains the tree that lays out all the steps.
   */
  private JPanel pTreePanel;
  
  /**
   * Panel containing the different user inputed settings.
   */
  private JPanel pSettingsPanel;

  /**
   * Layout for the different input panels. 
   */
  private CardLayout pSettingsLayouts;
  
  private TreeMap<TemplateGlueStage, JPanel> pPanels;
  
  /**
   * Tree that lists the different steps.
   */
  private JFancyTree pSettingTree;
  
  /**
   * The current stage of the process.
   */
  private TemplateGlueStage pCurrentStage;
  
  private DefaultMutableTreeNode pActiveNode;
  
  private JTemplateGlueDialog pParentDialog;
  
  private String pAuthor;

  private String pView;
  
  private TemplateNetworkScan pScan;

  
  /**
   * Gather information.
   */
  private ListSet<String> pReplacements;
  private TreeMap<String, String> pReplacementParamNames;
  private TreeMap<String, String> pReplacementDefaults;
  private MappedListSet<String, String> pContexts;
  private DoubleMap<String, String, String> pContextParamNames;
  private MappedArrayList<String, TreeMap<String, String>> pContextDefaults;
  
  private MappedSet<String, String> pFrameRanges;
  private TreeMap<String, FrameRange> pFrameRangeDefaults;
  
  private TreeMap<String, ActionOnExistence> pAOEModes;
  
  private ListMap<String, Boolean> pOptionalBranches;
  
  private MappedSet<String, String> pExternals;
  
  private boolean pFinished;
}
