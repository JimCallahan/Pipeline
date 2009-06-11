// $Id: AoEModePanel.java,v 1.1 2009/06/11 05:35:08 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_6;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_3.*;
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
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    pBox = new Box(BoxLayout.PAGE_AXIS);
    
    {
      pButtonBox = new Box(BoxLayout.LINE_AXIS);
      
      JButton add = UIFactory.createDialogButton("Add", "add", this, "Add an AoE Mode");
      pButtonBox.add(add);
      pButtonBox.add(Box.createHorizontalGlue());
      pBox.add(pButtonBox);
      pBox.add(Box.createVerticalStrut(4));
    }
    
    {
      pHeaderBox = new Box(BoxLayout.LINE_AXIS);
      Dimension dim = new Dimension(150, 19);
      {
        JLabel label = UIFactory.createLabel("AoE Mode", dim.width, SwingConstants.LEFT);
        label.setMaximumSize(dim);
        pHeaderBox.add(label);
        pHeaderBox.add(Box.createHorizontalStrut(8));
      }
      {
        JLabel label = UIFactory.createLabel("Default Value", dim.width, SwingConstants.LEFT);
        label.setMaximumSize(dim);
        pHeaderBox.add(label);
        pHeaderBox.add(Box.createHorizontalStrut(8));
      }
      
      pHeaderBox.add(Box.createHorizontalGlue());
      pBox.add(pHeaderBox);
      pBox.add(Box.createVerticalStrut(4));
    }
    
    if (oldSettings != null) {
     TreeMap<String, ActionOnExistence> aoes = oldSettings.getAOEModes();
     
     for (Entry<String, ActionOnExistence> entry : aoes.entrySet()) {
       createEntry(entry.getKey(), entry.getValue().toTitle());
     }
    }
    createEntry(null, null);
    
    pBox.add(Box.createVerticalStrut(20));
    
    {
      pButtonBox2 = new Box(BoxLayout.LINE_AXIS);
      
      JButton add = 
        UIFactory.createDialogButton("Add All", "addall", this, "Add all the found AoE Modes.");
      pButtonBox2.add(add);
      pButtonBox2.add(Box.createHorizontalGlue());
      pBox.add(pButtonBox2);
      pBox.add(Box.createVerticalStrut(4));
    }
    
    for (String extra : pMissing) {
      AddEntry entry = new AddEntry(this, extra);
      pBox.add(entry);
      pBox.add(Box.createVerticalStrut(4));
      pAddEntries.add(entry);
    }
    
    pBox.add(UIFactory.createFiller(100));
    
    Dimension dim = new Dimension(700, 500);
    
    JScrollPane scroll = UIFactory.createScrollPane
     (pBox, 
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
      ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
      dim, null, null);
    
    this.add(scroll); 
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
     pBox.add(entry);
     pBox.add(Box.createVerticalStrut(2));
     pEntries.put(pNextID, entry);
     pOrder.add(pNextID);
     pNextID++;
  }
  
  private void
  relayout()
  {
    pBox.removeAll();
    pBox.add(pButtonBox);
    pBox.add(Box.createVerticalStrut(4));
    pBox.add(pHeaderBox);
    pBox.add(Box.createVerticalStrut(4));
    for (int i : pOrder) {
      AoEEntry entry = pEntries.get(i);
      pBox.add(entry);
      pBox.add(Box.createVerticalStrut(2));
    }
    
    pBox.add(Box.createVerticalStrut(20));
    pBox.add(pButtonBox2);
    pBox.add(Box.createVerticalStrut(4));
    
    for (AddEntry entry : pAddEntries) {
      pBox.add(entry);
      pBox.add(Box.createVerticalStrut(4));
    }
    
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
      pEntries.remove(idx);
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
  AddEntry
    extends Box
  {
    private 
    AddEntry
    (
      ActionListener parent,
      String range
    )
    {
      super(BoxLayout.LINE_AXIS);
      
      JParamNameField field = UIFactory.createParamNameField(range, 150, SwingConstants.LEFT);
      field.setMaximumSize(field.getPreferredSize());
      field.setEditable(false);
      this.add(field);
      this.add(Box.createHorizontalStrut(8));
      {
        JButton but = 
          UIFactory.createDialogButton("Add", "add-" + range, parent, "Add the missing replacement");
        this.add(but);
      }
      this.add(Box.createHorizontalGlue()); 
    }

    private static final long serialVersionUID = -4950780858342540005L;
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
      
      pAoEMode = UIFactory.createParamNameField(range, 150, SwingConstants.LEFT);
      pAoEMode.setMaximumSize(pAoEMode.getPreferredSize());
      this.add(pAoEMode);
      this.add(Box.createHorizontalStrut(8));
      
      ArrayList<String> values = ActionOnExistence.titles();
      pDefaultValue = UIFactory.createCollectionField(values, pDialog, 150);
      pDefaultValue.setMaximumSize(pDefaultValue.getPreferredSize());
      if (defaultValue != null)
        pDefaultValue.setSelected(defaultValue);
      this.add(pDefaultValue);
      
      this.add(Box.createHorizontalStrut(4));
      
      {
        JButton but = 
          UIFactory.createDialogButton("Remove", "remove-" + id, parent, "Remove the AoE Mode.");
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
  private Box pButtonBox;
  private Box pHeaderBox;
  private Box pButtonBox2;
}
