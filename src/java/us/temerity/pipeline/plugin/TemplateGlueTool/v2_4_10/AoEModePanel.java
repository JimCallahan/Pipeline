// $Id: AoEModePanel.java,v 1.1 2009/10/09 04:40:08 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_10;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_10.*;
import us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_10.TemplateUIFactory.*;
import us.temerity.pipeline.ui.*;


public 
class AoEModePanel
  extends JPanel
  implements ActionListener
{
  public
  AoEModePanel
  (
    JTemplateGlueDialog dialog,
    TemplateNetworkScan scan,
    TemplateGlueInformation oldSettings  
  )
  {
    super();
    
    pDialog = dialog;
    
    pNextID = 0;
    pOrder = new LinkedList<Integer>();
    pEntries = new TreeMap<Integer, AoEEntry>();
    pAddEntries = new ArrayList<AddEntry>();
    
    pMissing = scan.getAoEModes().keySet();
    pHasMissing = !pMissing.isEmpty();
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    pBox = new Box(BoxLayout.PAGE_AXIS);
    
    pTitleBox = TemplateUIFactory.createTitleBox("AoE Modes:");
    
    {
      pButtonBox = TemplateUIFactory.createHorizontalBox();
      
      JButton add = 
        TemplateUIFactory.createPanelButton("Add AoE Mode", "add", this, "Add an AoE Mode");
      pButtonBox.add(add);
      pButtonBox.add(Box.createHorizontalGlue());
    }
    
    {
      pHeaderBox = TemplateUIFactory.createHorizontalBox();
      int width = 150;
      {
        JLabel label = UIFactory.createFixedLabel("AoE Mode:", width, SwingConstants.LEFT);
        pHeaderBox.add(label);
      }
      pHeaderBox.add(TemplateUIFactory.createHorizontalSpacer());
      {
        JLabel label = UIFactory.createFixedLabel("Default Value:", width, SwingConstants.LEFT);
        pHeaderBox.add(label);
      }
      pHeaderBox.add(TemplateUIFactory.createHorizontalSpacer());      
      pHeaderBox.add(Box.createHorizontalGlue());
    }
    
    if (oldSettings != null) {
     TreeMap<String, ActionOnExistence> aoes = oldSettings.getAOEModes();
     
     for (Entry<String, ActionOnExistence> entry : aoes.entrySet()) {
       createEntry(entry.getKey(), entry.getValue().toTitle());
     }
    }
    
    {
      pButtonBox2 = TemplateUIFactory.createHorizontalBox();
      
      pButtonBox2.add(UIFactory.createFixedLabel
        ("Found in scan: ", 150, SwingConstants.LEFT));
      
      pButtonBox2.add(TemplateUIFactory.createHorizontalSpacer());

      JButton add = 
        TemplateUIFactory.createPanelButton("Add All", "addall", this, "Add all the found contexts");
      pButtonBox2.add(add);
      pButtonBox2.add(Box.createHorizontalGlue());
    }
    
    for (String extra : pMissing) {
      AddEntry entry = new AddEntry(this, extra, "AoE Mode");
      pAddEntries.add(entry);
    }
    
    
    Dimension dim = new Dimension(700, 500);
    
    JScrollPane scroll = UIFactory.createScrollPane
     (pBox, 
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
      ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
      dim, null, null);
    
    this.add(scroll);
    
    relayout();
  }
  
  public TreeMap<String, ActionOnExistence>
  getAoEModes()
    throws PipelineException
  {
    TreeMap<String, ActionOnExistence> toReturn = new TreeMap<String, ActionOnExistence>();
    for (int i : pOrder) {
      AoEEntry entry = pEntries.get(i);
      String aoe = entry.getAoEMode();
      if (aoe != null && !aoe.equals("")) {
        if (toReturn.keySet().contains(aoe))
          throw new PipelineException
            ("The AoE Mode (" + aoe + ") appears more than once in the panel.  " +
             "Please correct this before continuing.");
        
        toReturn.put(aoe, entry.getDefaultValue());
      }
    }
    return toReturn;
  }
  
  private TreeSet<String>
  collectAoEModes()
  {
    TreeSet<String> toReturn = new TreeSet<String>();
    for (int i : pOrder) {
      AoEEntry entry = pEntries.get(i);
      String mode = entry.getAoEMode();
      if (mode != null && !mode.equals("")) {
        toReturn.add(mode);
      }
    }
    return toReturn;
  }
  
  /**
   * @param rep
   * @param def
   * @param param
   */
  private void 
  createEntry
  (
    String range,
    String defaultValue
  )
  {
    AoEEntry entry = 
       new AoEEntry(this, pNextID, range, defaultValue);
     pEntries.put(pNextID, entry);
     pOrder.add(pNextID);
     pNextID++;
  }
  
  private void
  relayout()
  {
    pBox.removeAll();
    pBox.add(pTitleBox);
    pBox.add(TemplateUIFactory.createLargeVerticalGap());
    pBox.add(pHeaderBox);
    pBox.add(TemplateUIFactory.createVerticalGap());
    for (int i : pOrder) {
      AoEEntry entry = pEntries.get(i);
      pBox.add(entry);
      pBox.add(TemplateUIFactory.createVerticalGap());
    }
    
    pBox.add(TemplateUIFactory.createVerticalGap());
    pBox.add(pButtonBox);
    pBox.add(TemplateUIFactory.createVerticalGap());
    
    if (pHasMissing) {
      pBox.add(Box.createVerticalStrut(20));
      pBox.add(UIFactory.createPanelBreak());
      pBox.add(Box.createVerticalStrut(20));
      pBox.add(pButtonBox2);
      pBox.add(TemplateUIFactory.createVerticalGap());

      for (AddEntry entry : pAddEntries) {
        pBox.add(entry);
        pBox.add(TemplateUIFactory.createVerticalGap());
      }
    }
    
    pBox.add(TemplateUIFactory.createLargeVerticalGap());
    pBox.add(UIFactory.createFiller(100));
    pBox.revalidate();  
  }
  
  @Override
  public void 
  actionPerformed
  (
    ActionEvent e
  )
  {
    String command = e.getActionCommand();
    if (command.startsWith("remove-")) {
      int id = Integer.valueOf(command.replace("remove-", ""));
      int idx = pOrder.indexOf(id);
      pOrder.remove(idx);
      pEntries.remove(id);
      relayout();
    }
    else if (command.equals("add")) {
      createEntry(null, null);
      relayout();
    }
    else if (command.equals("addall")) {
      TreeSet<String> existing = collectAoEModes();
      for (String entry : pMissing) {
        if (!existing.contains(entry)) {
          createEntry(entry, null);
        }
      }
      relayout();
    }
    else if (command.startsWith("add-")) {
      String name = command.replace("add-", "");
      TreeSet<String> existing = collectAoEModes();
      if (!existing.contains(name)) 
        createEntry(name, null);
      relayout();
    }
  }
  
  private class
  AoEEntry
    extends Box
  {
    private
    AoEEntry
    (
      ActionListener parent,
      int id,
      String range,
      String defaultValue
    )
    {
      super(BoxLayout.LINE_AXIS);
      
      this.add(TemplateUIFactory.createHorizontalIndent());
      
      pAoEMode = UIFactory.createParamNameField(range, 150, SwingConstants.LEFT);
      pAoEMode.setMaximumSize(pAoEMode.getPreferredSize());
      this.add(pAoEMode);
      this.add(TemplateUIFactory.createHorizontalSpacer());
      
      ArrayList<String> values = ActionOnExistence.titles();
      pDefaultValue = UIFactory.createCollectionField(values, pDialog, 150);
      pDefaultValue.setMaximumSize(pDefaultValue.getPreferredSize());
      if (defaultValue != null)
        pDefaultValue.setSelected(defaultValue);
      this.add(pDefaultValue);
      
      this.add(TemplateUIFactory.createHorizontalSpacer());
      
      {
        JButton but = 
          TemplateUIFactory.createRemoveButton(parent, "remove-" + id);
        this.add(but);
      }
      
      this.add(Box.createHorizontalGlue());
    }
    
    private String
    getAoEMode()
    {
      return pAoEMode.getText();
    }
    
    private ActionOnExistence
    getDefaultValue()
    {
      int idx = pDefaultValue.getSelectedIndex();
      
      return ActionOnExistence.values()[idx];
    }

    
    private static final long serialVersionUID = 5788225325214683835L;

    private JParamNameField pAoEMode;
    private JCollectionField pDefaultValue;
  }

  
  private static final long serialVersionUID = -4295251517436450070L;

  
  private JTemplateGlueDialog pDialog;
  
  private int pNextID;
  private TreeMap<Integer, AoEEntry> pEntries;
  private LinkedList<Integer> pOrder;
  
  private Set<String> pMissing;
  private ArrayList<AddEntry> pAddEntries;
  
  private Box pBox;
  private Box pTitleBox;
  private Box pButtonBox;
  private Box pHeaderBox;
  private Box pButtonBox2;
  
  private boolean pHasMissing;
}
