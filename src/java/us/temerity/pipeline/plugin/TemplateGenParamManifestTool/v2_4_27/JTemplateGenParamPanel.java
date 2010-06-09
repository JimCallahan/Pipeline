package us.temerity.pipeline.plugin.TemplateGenParamManifestTool.v2_4_27;

import java.awt.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;
import javax.swing.tree.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.ui.*;
import us.temerity.pipeline.builder.v2_4_12.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   G E N   P A R A M   P A N E L                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Top level panel for tool.
 */
public 
class JTemplateGenParamPanel
  extends JPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  JTemplateGenParamPanel
  (
    JTemplateGenParamDialog parent,
    TemplateGlueInformation glueInfo,
    TemplateParamManifest oldManifest
  )
  {
    super();

    pFinished = false;
    pPanels = new TreeMap<ParamManifestStage, JPanel>();

    pParentDialog = parent;
    pGlueInfo = glueInfo;
    pOldManifest = oldManifest;
    
    pNewManifest = new TemplateParamManifest();
    
    pCurrentStage = ParamManifestStage.values()[0];
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    {
      pTreePanel = new JPanel();
      
      pTreePanel.setLayout(new BoxLayout(pTreePanel, BoxLayout.PAGE_AXIS));
      
      pTreePanel.add(Box.createRigidArea(new Dimension(0, 4)));
      
      /* Add the header. */
      {
        JPanel topBox = new JPanel();
        topBox.setLayout(new BoxLayout(topBox, BoxLayout.LINE_AXIS));
        JTextField field = UIFactory.createTextField("Manifest Step", 40, JLabel.CENTER);
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
      
      for (ParamManifestStage stage : ParamManifestStage.values()) {
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
      
      firstPass.setMinimumSize(new Dimension(740, 500));
      firstPass.setPreferredSize(new Dimension(740, 500));
      
      pSettingsPanel = firstPass;
      pSettingsLayouts = firstLayout;
    }
    
    prepareStage(pCurrentStage);
    
    this.add(pTreePanel);
    this.add(Box.createRigidArea(new Dimension(2, 0)));
    this.add(UIFactory.createSidebar());
    this.add(pSettingsPanel);   
  }
  
  public void
  doNext() 
    throws PipelineException
  {
    pParentDialog.disableButtons();
    ParamManifestStage nextStage = ParamManifestStage.getNextStage(pCurrentStage);
    finalizeStage(pCurrentStage);
    
    if (nextStage != null) {

      prepareStage(nextStage);

      pCurrentStage = nextStage;

      DefaultTreeModel model = (DefaultTreeModel) pSettingTree.getModel();
      ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setDone();
      model.nodeChanged(pActiveNode);
      pActiveNode = pActiveNode.getNextSibling();
      if (pActiveNode != null)
        ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setActive();
      model.nodeChanged(pActiveNode);
    }
    else {
      pFinished = true;
      pParentDialog.doFinal();
    }

    pParentDialog.enableButtons();
  }
  
  public void
  goBack()
  {
    pParentDialog.disableButtons();
    ParamManifestStage prevStage = ParamManifestStage.getPreviousStage(pCurrentStage);
    if (prevStage != null) {
      prepareStage(prevStage);
      pCurrentStage = prevStage;

      DefaultTreeModel model = (DefaultTreeModel) pSettingTree.getModel();
      ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setNotDone();
      model.nodeChanged(pActiveNode);
      pActiveNode = pActiveNode.getPreviousSibling();
      if (pActiveNode != null)
        ((BuilderTreeNodeInfo) pActiveNode.getUserObject()).setActive();
      model.nodeChanged(pActiveNode);
    }
    
    pParentDialog.enableButtons();
  }
  
  private void
  prepareStage
  (
    ParamManifestStage stage  
  )
  {
    if (stage == null)
      return;
    switch (stage) {
    case StringReplacement:
      {
        JPanel panel = pPanels.get(stage);
        if (panel == null) {
          panel = new StringReplacementPanel(pGlueInfo, pOldManifest);
          pPanels.put(stage, panel);
          pSettingsPanel.add(panel, stage.toString());
        }
        pSettingsLayouts.show(pSettingsPanel, stage.toString());
      }
      break;
    case AoEModes:
      {
        JPanel panel = pPanels.get(stage);
        if (panel == null) {
          panel = new AoEModePanel(pParentDialog, pGlueInfo, pOldManifest);
          pPanels.put(stage, panel);
          pSettingsPanel.add(panel, stage.toString());
        }
        pSettingsLayouts.show(pSettingsPanel, stage.toString());
      }
      break;
    case OptionalBranches:
      {
        JPanel panel = pPanels.get(stage);
        if (panel == null) {
          panel = new OptionalBranchPanel(pGlueInfo, pOldManifest);
          pPanels.put(stage, panel);
          pSettingsPanel.add(panel, stage.toString());
        }
        pSettingsLayouts.show(pSettingsPanel, stage.toString());
      }
      break;
    case Contexts:
      {
        JPanel panel = pPanels.get(stage);
        if (panel == null) {
          panel = new ContextsPanel(pGlueInfo, pOldManifest);
          pPanels.put(stage, panel);
          pSettingsPanel.add(panel, stage.toString());
        }
        pSettingsLayouts.show(pSettingsPanel, stage.toString());
      }
      break;
    case FrameRanges:
      {
        JPanel panel = pPanels.get(stage);
        if (panel == null) {
          TreeSet<String> allRanges = new TreeSet<String>();
          MappedSet<String, String> fRanges;
          if (pGlueInfo != null)
            fRanges = pGlueInfo.getFrameRanges();
          else 
            fRanges = pOldManifest.getFrameRangeContexts();
          
          pNewManifest.setFrameRangeContexts(fRanges);
          
          for (Entry<String, TreeSet<String>> entry : fRanges.entrySet()) {
            TreeSet<String> contexts = entry.getValue();
            if (contexts == null || contexts.isEmpty())
              allRanges.add(entry.getKey());
            else 
              for (TreeMap<String, String> map : allContextReplacements(contexts)) {
                allRanges.add(stringReplace(entry.getKey(), map));
              }
          }
          panel = new FrameRangePanel(allRanges, pGlueInfo, pOldManifest);
          pPanels.put(stage, panel);
          pSettingsPanel.add(panel, stage.toString());
        }
        pSettingsLayouts.show(pSettingsPanel, stage.toString());
      }
      break;
    case Offsets:
      {
        JPanel panel = pPanels.get(stage);
        if (panel == null) {
          TreeSet<String> allOffsets= new TreeSet<String>();
          MappedSet<String, String> offsets;
          if (pGlueInfo != null)
            offsets = pGlueInfo.getOffsets();
          else 
            offsets = pOldManifest.getOffsetContexts();
          
          pNewManifest.setOffsetContexts(offsets);
          
          for (Entry<String, TreeSet<String>> entry : offsets.entrySet()) {
            TreeSet<String> contexts = entry.getValue();
            if (contexts == null || contexts.isEmpty())
              allOffsets.add(entry.getKey());
            else 
              for (TreeMap<String, String> map : allContextReplacements(contexts)) {
                allOffsets.add(stringReplace(entry.getKey(), map));
              }
          }
          panel = new OffsetsPanel(allOffsets, pGlueInfo, pOldManifest);
          pPanels.put(stage, panel);
          pSettingsPanel.add(panel, stage.toString());
        }
        pSettingsLayouts.show(pSettingsPanel, stage.toString());
      }
      break;
    case Externals:
      {
        JPanel panel = pPanels.get(stage);
        if (panel == null) {
          TreeSet<String> allExternals = new TreeSet<String>();
          MappedSet<String, String> externals;
          if (pGlueInfo != null)
            externals = pGlueInfo.getExternals();
          else 
            externals = pOldManifest.getExternalContexts();
          
          pNewManifest.setExternalContexts(externals);
          
          for (Entry<String, TreeSet<String>> entry : externals.entrySet()) {
            TreeSet<String> contexts = entry.getValue();
            if (contexts == null || contexts.isEmpty())
              allExternals.add(entry.getKey());
            else 
              for (TreeMap<String, String> map : allContextReplacements(contexts)) {
                allExternals.add(stringReplace(entry.getKey(), map));
              }
          }
          panel = new ExternalsPanel(pParentDialog, allExternals, pOldManifest);
          pPanels.put(stage, panel);
          pSettingsPanel.add(panel, stage.toString());
        }
        pSettingsLayouts.show(pSettingsPanel, stage.toString());    
      }
      break;
    }
  }
  
  private void
  finalizeStage
  (
    ParamManifestStage stage  
  )
    throws PipelineException
  {
    if (stage == null)
      return;
    switch (stage) {
    case StringReplacement:
      {
        StringReplacementPanel panel = (StringReplacementPanel) pPanels.get(stage);
        pNewManifest.setReplacements(panel.getReplacementValues());
      }
      break;
    case AoEModes:
      {
        AoEModePanel panel = (AoEModePanel) pPanels.get(stage);
        pNewManifest.setAOEModes(panel.getAOEModes());
      }
      break;
    case OptionalBranches:
      {
        OptionalBranchPanel panel = (OptionalBranchPanel) pPanels.get(stage);
        pNewManifest.setOptionalBranches(panel.getOptionalBranchValues());
      }
      break;
    case Contexts:
      {
        ContextsPanel panel = (ContextsPanel) pPanels.get(stage);
        pContexts = panel.getContextValues();
        pNewManifest.setContexts(pContexts);
        pPanels.remove(ParamManifestStage.FrameRanges);
        pPanels.remove(ParamManifestStage.Externals);
        pPanels.remove(ParamManifestStage.Offsets);
      }
      break;
    case FrameRanges:
      {
        FrameRangePanel panel = (FrameRangePanel) pPanels.get(stage);
        pNewManifest.setFrameRanges(panel.getRangeValues());
      }
      break;
    case Offsets:
      {
        OffsetsPanel panel = (OffsetsPanel) pPanels.get(stage);
        pNewManifest.setOffsets(panel.getOffsetValues());
      }
      break;
    case Externals:
      {
        ExternalsPanel panel = (ExternalsPanel) pPanels.get(stage);
        pNewManifest.setExternals(panel.getExternalValues());
      }
      break;
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Replace each key occurrence with the its value in the source string.
   * 
   * @param source
   *   The string which will be replaced.
   * 
   * @param stringReplacements
   *   A map with the keys being the strings to be replaced and the values being what to 
   *   replace the  
   */
  protected String
  stringReplace
  (
    String source,
    TreeMap<String, String> stringReplacements
  )
  {
    String toReturn = source;
    for (String pattern : stringReplacements.keySet())
      toReturn = toReturn.replaceAll(pattern, stringReplacements.get(pattern));
    return toReturn;
  }
  
  /**
   * Get all the sets of replacements generated from the list of contexts. <p>

   * @param contexts
   *   The list of contexts.   
   * 
   * @return
   *   A list of all the maps of string replacements.
   */
  private LinkedList<TreeMap<String, String>>
  allContextReplacements
  (
    Set<String> contexts  
  )
  {
    LinkedList<TreeMap<String, String>> toReturn = new LinkedList<TreeMap<String,String>>();
    if (contexts.isEmpty()) {
      return toReturn;
    }
    allContextReplacementsHelper
      (new TreeSet<String>(contexts), new TreeMap<String, String>(), toReturn);
    return toReturn;
  }
  
  private void
  allContextReplacementsHelper
  (
    TreeSet<String> contexts,
    TreeMap<String,String> replacements, 
    LinkedList<TreeMap<String, String>> data
  )
  {
    String currentContext = contexts.pollFirst();
    ArrayList<TreeMap<String, String>> values = pContexts.get(currentContext);
    
    if (values == null || values.isEmpty()) {
      TreeMap<String, String> newReplace = new TreeMap<String, String>(replacements);

      if (contexts.isEmpty()) {  //bottom of the recursion
        data.add(newReplace);
      }
      else {
        allContextReplacementsHelper(new TreeSet<String>(contexts), newReplace, data);
      }
      return;
    }
    
    for (TreeMap<String, String> contextEntry : values) {
      TreeMap<String, String> newReplace = new TreeMap<String, String>(replacements);
      newReplace.putAll(contextEntry);
     
      if (contexts.isEmpty()) {  //bottom of the recursion
        data.add(newReplace);
      }
      else {
        allContextReplacementsHelper(new TreeSet<String>(contexts), newReplace, data);
      }
    }
  }
  
  public boolean
  isFinished()
  {
    return pFinished;
  }
  
  public TemplateParamManifest
  getNewManifest()
  {
    return pNewManifest;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -7719419025973696833L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private TemplateGlueInformation pGlueInfo;
  private TemplateParamManifest   pOldManifest;
  private TemplateParamManifest   pNewManifest;
  
  private ParamManifestStage      pCurrentStage;
  private boolean                 pFinished;
  
  MappedArrayList<String, TreeMap<String, String>> pContexts;
  
  private TreeMap<ParamManifestStage, JPanel> pPanels;
  
  /**
   * Tree that lists the different steps.
   */
  private JFancyTree pSettingTree;
  
  private JTemplateGenParamDialog pParentDialog;
  
  private DefaultMutableTreeNode pActiveNode;
  
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
}
