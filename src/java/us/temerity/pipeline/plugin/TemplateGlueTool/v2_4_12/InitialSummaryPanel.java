// $Id: InitialSummaryPanel.java,v 1.1 2009/10/14 18:11:43 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_12;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_12.*;
import us.temerity.pipeline.ui.*;


public 
class InitialSummaryPanel
  extends JPanel
{
  public
  InitialSummaryPanel
  (
    TemplateNetworkScan scan,
    TemplateGlueInformation oldSettings
  )
  {
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    pScan = scan;
    pOldSettings = oldSettings;
    
    JPanel vbox = new JPanel();
    vbox.setLayout(new BoxLayout(vbox, BoxLayout.PAGE_AXIS));
 
    vbox.add(UIFactory.createPanelLabel("The following nodes were found"));
    vbox.add(Box.createVerticalStrut(8));
    createNodeTable(vbox);
    
    vbox.add(Box.createVerticalStrut(12));
    vbox.add(UIFactory.createPanelLabel("The following contexts were found"));
    vbox.add(Box.createVerticalStrut(8));
    createContextTable(vbox);
    
    vbox.add(Box.createVerticalStrut(12));
    vbox.add(UIFactory.createPanelLabel("The following frame ranges were found"));
    vbox.add(Box.createVerticalStrut(8));
    createFrameRangeTable(vbox);
    
    vbox.add(Box.createVerticalStrut(12));
    vbox.add(UIFactory.createPanelLabel("The following offsets were found"));
    vbox.add(Box.createVerticalStrut(8));
    createOffsetTable(vbox);

    vbox.add(Box.createVerticalStrut(12));
    vbox.add(UIFactory.createPanelLabel("The following AoE modes were found"));
    vbox.add(Box.createVerticalStrut(8));
    createAoEModeTable(vbox);
    
    vbox.add(Box.createVerticalStrut(12));
    vbox.add(UIFactory.createPanelLabel("The following optional branches were found"));
    vbox.add(Box.createVerticalStrut(8));
    createOptionalBranchTable(vbox);
    
    vbox.add(Box.createVerticalStrut(12));
    vbox.add(UIFactory.createPanelLabel("The following conditional builds were found"));
    vbox.add(Box.createVerticalStrut(8));
    createConditionalBuildTable(vbox);
    
    vbox.add(Box.createVerticalStrut(12));
    vbox.add(UIFactory.createPanelLabel("The following external sequences were found"));
    vbox.add(Box.createVerticalStrut(8));
    createExternalTable(vbox);
    
    vbox.add(Box.createVerticalStrut(12));
    vbox.add(UIFactory.createPanelLabel
      ("The following product nodes were found."));
    vbox.add(UIFactory.createPanelLabel
      ("The third column indicates if they are ever flagged as an Ignorable Product."));
    vbox.add(UIFactory.createPanelLabel(
      "The fourth column indicates that they are flagged as an Ignorable Product every time they appear."));
    vbox.add(UIFactory.createPanelLabel(
      "If the third and forth columns are different, there a problem in the template."));
    vbox.add(Box.createVerticalStrut(8));
    createProductTable(vbox);
    
    
    
    Dimension dim = new Dimension(700, 500);
    
    JScrollPane scroll = UIFactory.createScrollPane
     (vbox, 
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
      ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
      dim, null, null);
    
    this.add(scroll);
    
  }
  
  /**
   * @param vbox
   */
  private void 
  createNodeTable
  (
    JPanel vbox
  )
  {
    TreeSet<String> inTemplate = pScan.getNodesInTemplate();
    TreeSet<String> old = new TreeSet<String>();
    if (pOldSettings != null)
      old.addAll(pOldSettings.getNodesInTemplate());
    TreeSet<String> allNodes = new TreeSet<String>(inTemplate);
    allNodes.addAll(old);
    makeTable(vbox, inTemplate, old, allNodes, "Node Name");
  }

  private void 
  createContextTable
  (
    JPanel vbox
  )
  {
    Set<String> inTemplate = pScan.getContexts().keySet();
    TreeSet<String> old = new TreeSet<String>();
    if (pOldSettings != null)
      old.addAll(pOldSettings.getContexts().keySet());
    TreeSet<String> allContexts = new TreeSet<String>(inTemplate);
    allContexts.addAll(old);
    makeTable(vbox, inTemplate, old, allContexts, "Context Name");
  }
  
  private void 
  createFrameRangeTable
  (
    JPanel vbox
  )
  {
    Set<String> inTemplate = pScan.getFrameRanges().keySet();
    TreeSet<String> old = new TreeSet<String>();
    if (pOldSettings != null)
      old.addAll(pOldSettings.getFrameRanges().keySet());
    TreeSet<String> all = new TreeSet<String>(inTemplate);
    all.addAll(old);
    makeTable(vbox, inTemplate, old, all, "Range Name");
  }
  
  private void 
  createOffsetTable
  (
    JPanel vbox
  )
  {
    Set<String> inTemplate = pScan.getOffsets().keySet();
    TreeSet<String> old = new TreeSet<String>();
    if (pOldSettings != null)
      old.addAll(pOldSettings.getOffsets().keySet());
    TreeSet<String> all = new TreeSet<String>(inTemplate);
    all.addAll(old);
    makeTable(vbox, inTemplate, old, all, "Offset Name");
  }
  
  private void 
  createAoEModeTable
  (
    JPanel vbox
  )
  {
    Set<String> inTemplate = pScan.getAoEModes().keySet();
    TreeSet<String> old = new TreeSet<String>();
    if (pOldSettings != null)
      old.addAll(pOldSettings.getAOEModes().keySet());
    TreeSet<String> all = new TreeSet<String>(inTemplate);
    all.addAll(old);
    makeTable(vbox, inTemplate, old, all, "AoE Mode Name");
  }
  
  private void 
  createExternalTable
  (
    JPanel vbox
  )
  {
    Set<String> inTemplate = pScan.getExternals().keySet();
    TreeSet<String> old = new TreeSet<String>();
    if (pOldSettings != null)
      old.addAll(pOldSettings.getExternals().keySet());
    TreeSet<String> all = new TreeSet<String>(inTemplate);
    all.addAll(old);
    makeTable(vbox, inTemplate, old, all, "External Name");
  }
  
  private void 
  createOptionalBranchTable
  (
    JPanel vbox
  )
  {
    MappedSet<String, String> inTemplate = pScan.getOptionalBranchValues();
    Set<String> keySet = inTemplate.keySet();
    TreeSet<String> old = new TreeSet<String>();
    if (pOldSettings != null)
      old.addAll(pOldSettings.getOptionalBranches().keySet());
    TreeSet<String> all = new TreeSet<String>(inTemplate.keySet());
    all.addAll(old);
    
    int size = all.size();
    String names[] = new String[size];
    names = all.toArray(names);
    Integer num[] = new Integer[size];
    Boolean inT[] = new Boolean[size];
    Boolean selected[] = new Boolean[size];

    for (int i = 0; i < size; i ++) {
      String name = names[i];
      inT[i] = keySet.contains(name);
      if (inT[i]) {
        num[i] = inTemplate.get(name).size();
      }
      else
        num[i] = 0;
      selected[i] = old.contains(name);
    }
      
    ArrayList<Comparable[]> list = new ArrayList<Comparable[]>();
    list.add(inT);
    list.add(num);
    list.add(selected);

    ArrayList<String> headers = new ArrayList<String>();
    Collections.addAll(headers, "Optional Branches", "Node Scan", "Times Used", "Glue File");

    TemplateGlueNodeTableModel tableModel = 
      new TemplateGlueNodeTableModel(names,  list, headers);


    JTablePanel tpanel =
      new JTablePanel(tableModel, 
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

    JTable table = tpanel.getTable();
    Dimension dim = table.getPreferredScrollableViewportSize();
    table.setPreferredScrollableViewportSize(new Dimension(dim.width, Math.min(25 * size, 250)));

    vbox.add(tpanel);
  }
  
  private void 
  createProductTable
  (
    JPanel vbox
  )
  {
    DoubleMap<String, String, Boolean> inTemplate = pScan.getProductNodes();
    
    int size = inTemplate.size();
    String products[] = new String[size];
    products = inTemplate.keySet().toArray(products);
    Integer num[] = new Integer[size];
    Boolean any[] = new Boolean[size];
    Boolean all[] = new Boolean[size];
    
    for (int i = 0; i < size; i++) {
      String name = products[i];
      TreeMap<String, Boolean> targets = inTemplate.get(name);
      num[i] = targets.size();
      
      boolean anyTest = false;
      boolean allTest = true;
      
      for (Boolean bool: targets.values()) {
        if (bool)
          anyTest = true;
        else
          allTest = false;
      }
      any[i] = anyTest;
      all[i] = allTest;
    }

    ArrayList<Comparable[]> list = new ArrayList<Comparable[]>();
    list.add(num);
    list.add(any);
    list.add(all);
    
    ArrayList<String> headers = new ArrayList<String>();
    Collections.addAll(headers, "Product Nodes", "Times Used", "Some Ignore", "All Ignore");
    
    TemplateGlueNodeTableModel tableModel = 
      new TemplateGlueNodeTableModel(products, list, headers);
    
    
    JTablePanel tpanel =
      new JTablePanel(tableModel, 
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    
    JTable table = tpanel.getTable();
    Dimension dim = table.getPreferredScrollableViewportSize();
    table.setPreferredScrollableViewportSize(new Dimension(dim.width, Math.min(25 * size, 250)));
    
    vbox.add(tpanel);
    
  }
  
  private void 
  createConditionalBuildTable
  (
    JPanel vbox
  )
  {
    TreeMap<String, String> inTemplate = pScan.getConditionalBranches();
    
    int size = inTemplate.size();
    String targets[] = new String[size];
    String sources[] = new String[size];
    targets = inTemplate.keySet().toArray(targets);
    
    for (int i = 0; i < size; i++) {
      String name = targets[i];
      String source = inTemplate.get(name);
      sources[i] = source;
    }

    ArrayList<String[]> list = new ArrayList<String[]>();
    list.add(targets);
    list.add(sources);
    
    ArrayList<String> headers = new ArrayList<String>();
    Collections.addAll(headers, "Target Nodes", "Condition");
    
    TemplateGlueNodeTableModel tableModel = 
      new TemplateGlueNodeTableModel(list, new ArrayList<Comparable[]>(), headers);
    
    
    JTablePanel tpanel =
      new JTablePanel(tableModel, 
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    
    JTable table = tpanel.getTable();
    Dimension dim = table.getPreferredScrollableViewportSize();
    table.setPreferredScrollableViewportSize(new Dimension(dim.width, Math.min(25 * size, 250)));
    
    vbox.add(tpanel);
    
  }

  /**
   * @param vbox
   * @param inTemplate
   * @param old
   * @param all
   * @param header
   */
  @SuppressWarnings("unchecked")
  private void 
  makeTable
  (
    JPanel vbox,
    Set<String> inTemplate,
    TreeSet<String> old,
    TreeSet<String> all,
    String header
  )
  {
    int size = all.size();
    String names[] = new String[size];
    names = all.toArray(names);
    Boolean inT[] = new Boolean[size];
    Boolean selected[] = new Boolean[size];

    for (int i = 0; i < size; i ++) {
      String name = names[i];
      inT[i] = inTemplate.contains(name);
      selected[i] = old.contains(name);
    }
      
      ArrayList<Comparable[]> list = new ArrayList<Comparable[]>();
      list.add(inT);
      list.add(selected);
      
      ArrayList<String> headers = new ArrayList<String>();
      Collections.addAll(headers, header, "Node Scan", "Glue File");
      
      TemplateGlueNodeTableModel tableModel = 
        new TemplateGlueNodeTableModel(names, list, headers);
      
      
      JTablePanel tpanel =
        new JTablePanel(tableModel, 
          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
          ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      
      JTable table = tpanel.getTable();
      Dimension dim = table.getPreferredScrollableViewportSize();
      table.setPreferredScrollableViewportSize(new Dimension(dim.width, Math.min(25 * size, 250)));
      
      vbox.add(tpanel);
  }
  
  
  
  private static final long serialVersionUID = -1762075422472823007L;
  
  
  private TemplateGlueInformation pOldSettings;
  private TemplateNetworkScan pScan;
}
