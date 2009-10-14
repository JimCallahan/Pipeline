// $Id: StringReplacePanel.java,v 1.1 2009/10/14 18:11:43 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_12;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_12.*;
import us.temerity.pipeline.ui.*;


public 
class StringReplacePanel
  extends JPanel
  implements ActionListener
{
  public
  StringReplacePanel
  (
    TemplateGlueInformation oldSettings  
  )
  {
    super();
    
    pNextID = 0;
    pOrder = new LinkedList<Integer>();
    pEntries = new TreeMap<Integer, ReplaceEntry>();
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    pBox = new Box(BoxLayout.PAGE_AXIS);
    
    pTitleBox = TemplateUIFactory.createTitleBox("String Replacements:");
    
    {
      pHeaderBox = TemplateUIFactory.createHorizontalBox();
      int width = 150;
      {
        JLabel label = UIFactory.createFixedLabel("Replacement:", width, SwingConstants.LEFT);
        pHeaderBox.add(label);
      }
      pHeaderBox.add(TemplateUIFactory.createHorizontalSpacer());
      {
        JLabel label = UIFactory.createFixedLabel("Param Name:", width, SwingConstants.LEFT);
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
     ListSet<String> replace = oldSettings.getReplacements();
     TreeMap<String, String> defaults = oldSettings.getReplacementDefaults();
     TreeMap<String, String> params = oldSettings.getReplacementParamNames();
     
     for (String rep : replace) {
       String def = defaults.get(rep);
       String param = params.get(rep);
       createEntry(rep, def, param);
     }
    }
    
    {
      pButtonBox = TemplateUIFactory.createHorizontalBox();
      JButton add = TemplateUIFactory.createPanelButton("Add Replacement", "add", this, "Add a Replacement");
      pButtonBox.add(add);
      pButtonBox.add(Box.createHorizontalGlue());
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
  
  public ListSet<String>
  getReplacements()
    throws PipelineException
  {
    ListSet<String> toReturn = new ListSet<String>();
    for (int i : pOrder) {
      ReplaceEntry entry = pEntries.get(i);
      String replace = entry.getReplace();
      if (replace != null && !replace.equals("")) {
        if (toReturn.contains(replace))
          throw new PipelineException
            ("The Replacement (" + replace + ") appears more than once in the panel.  " +
             "Please correct this before continuing.");
        toReturn.add(replace);
      }
    }
    return toReturn;
  }
  
  public TreeMap<String, String>
  getReplacementDefaults()
  {
    TreeMap<String, String> toReturn = new TreeMap<String, String>();
    for (int i : pOrder) {
      ReplaceEntry entry = pEntries.get(i);
      String replace = entry.getReplace();
      if (replace != null && !replace.equals("")) {
        toReturn.put(replace, entry.getDefaultValue());
      }
    }
    return toReturn;
  }
  
  public TreeMap<String, String>
  getReplacementParamNames()
  {
    TreeMap<String, String> toReturn = new TreeMap<String, String>();
    for (int i : pOrder) {
      ReplaceEntry entry = pEntries.get(i);
      String replace = entry.getReplace();
      if (replace != null && !replace.equals("")) {
        toReturn.put(replace, entry.getParamName());
      }
    }
    return toReturn;
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
      ReplaceEntry entry = pEntries.get(i);
      pBox.add(entry);
      pBox.add(TemplateUIFactory.createVerticalGap());
    }
    pBox.add(TemplateUIFactory.createVerticalGap());
    pBox.add(pButtonBox);
    pBox.add(TemplateUIFactory.createLargeVerticalGap());
    pBox.add(UIFactory.createFiller(100));
    pBox.revalidate();
  }

  /**
   * @param rep
   * @param def
   * @param param
   */
  private void 
  createEntry
  (
    String rep,
    String def,
    String param
  )
  {
    ReplaceEntry entry = 
       new ReplaceEntry(this, pNextID, rep, param, def);
     pEntries.put(pNextID, entry);
     pOrder.add(pNextID);
     pNextID++;
  }
  
  @Override
  public void 
  actionPerformed
  (
    ActionEvent e
  )
  {
    String command = e.getActionCommand();
    if (command.startsWith("up-")) {
      int id = Integer.valueOf(command.replace("up-", ""));
      int idx = pOrder.indexOf(id);
      if (idx == 0)
        return;
      pOrder.remove(idx);
      pOrder.add(idx-1, id);
      relayout();
    }
    else if (command.startsWith("down-")) {
      int id = Integer.valueOf(command.replace("down-", ""));
      int idx = pOrder.indexOf(id);
      if (idx == (pOrder.size() - 1))
        return;
      pOrder.remove(idx);
      pOrder.add(idx+1, id);
      relayout();
    }
    else if (command.startsWith("remove-")) {
      int id = Integer.valueOf(command.replace("remove-", ""));
      int idx = pOrder.indexOf(id);
      pOrder.remove(idx);
      pEntries.remove(id);
      relayout();
    }
    else if (command.equals("add")) {
      createEntry(null, null, null);
      relayout();
    }
  }


  private class
  ReplaceEntry
    extends Box
  {
    private
    ReplaceEntry
    (
      StringReplacePanel parent,
      int id,
      String replace,
      String paramName,
      String defaultValue
    )
    {
      super(BoxLayout.LINE_AXIS);
     
      this.add(TemplateUIFactory.createHorizontalIndent());
      pReplace = UIFactory.createEditableTextField(replace, 150, SwingConstants.LEFT);
      pReplace.setMaximumSize(pReplace.getPreferredSize());
      this.add(pReplace);
      this.add(TemplateUIFactory.createHorizontalSpacer());
      pParamName = UIFactory.createParamNameField(paramName, 150, SwingConstants.LEFT);
      pParamName.setMaximumSize(pParamName.getPreferredSize());
      this.add(pParamName);
      this.add(TemplateUIFactory.createHorizontalSpacer());
      pDefaultValue = 
        UIFactory.createEditableTextField(defaultValue, 150, SwingConstants.LEFT);
      pDefaultValue.setMaximumSize(pDefaultValue.getPreferredSize());
      this.add(pDefaultValue);
      this.add(TemplateUIFactory.createHorizontalSpacer());
      
      {
        JButton but = TemplateUIFactory.createUpButton(parent, "up-" + id);
        this.add(but);
      }

      this.add(TemplateUIFactory.createButtonSpacer());
      
      {
        JButton but = TemplateUIFactory.createDownButton(parent, "down-" + id);
        this.add(but);
      }
      
      this.add(TemplateUIFactory.createButtonSpacer());
      
      {
        JButton but = TemplateUIFactory.createRemoveButton(parent, "remove-" +  id);
        this.add(but);
      }
      this.add(Box.createHorizontalGlue());
    }
    
    private String
    getReplace()
    {
      return pReplace.getText();
    }
    
    private String
    getParamName()
    {
      return pParamName.getText();
    }
    
    private String
    getDefaultValue()
    {
      return pDefaultValue.getText();
    }

    private static final long serialVersionUID = 2668979613773213493L;
    
    private JTextField pReplace;
    private JParamNameField pParamName;
    private JTextField pDefaultValue;
  }

  private static final long serialVersionUID = -5897971342160952077L;

  private int pNextID;
  private TreeMap<Integer, ReplaceEntry> pEntries;
  private LinkedList<Integer> pOrder;
  
  private Box pBox;
  private Box pButtonBox;
  private Box pHeaderBox;
  private Box pTitleBox;
}
