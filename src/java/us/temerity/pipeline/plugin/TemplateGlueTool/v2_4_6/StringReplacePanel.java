// $Id: StringReplacePanel.java,v 1.1 2009/06/11 05:35:08 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_6;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_3.*;
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
    
    {
      pButtonBox = new Box(BoxLayout.LINE_AXIS);
      
      JButton add = UIFactory.createDialogButton("Add", "add", this, "Add a Replacement");
      pButtonBox.add(add);
      pButtonBox.add(Box.createHorizontalGlue());
      pBox.add(pButtonBox);
      pBox.add(Box.createVerticalStrut(4));
    }
    
    {
      pHeaderBox = new Box(BoxLayout.LINE_AXIS);
      Dimension dim = new Dimension(150, 19);
      {
        JLabel label = UIFactory.createLabel("Replacement", dim.width, SwingConstants.LEFT);
        label.setMaximumSize(dim);
        pHeaderBox.add(label);
        pHeaderBox.add(Box.createHorizontalStrut(8));
      }
      {
        JLabel label = UIFactory.createLabel("Param Name", dim.width, SwingConstants.LEFT);
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
     ListSet<String> replace = oldSettings.getReplacements();
     TreeMap<String, String> defaults = oldSettings.getReplacementDefaults();
     TreeMap<String, String> params = oldSettings.getReplacementParamNames();
     
     for (String rep : replace) {
       String def = defaults.get(rep);
       String param = params.get(rep);
       createEntry(rep, def, param);
     }
    }
    createEntry(null, null, null);
    
    
    pBox.add(UIFactory.createFiller(100));
    
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
    pBox.add(pButtonBox);
    pBox.add(Box.createVerticalStrut(4));
    pBox.add(pHeaderBox);
    pBox.add(Box.createVerticalStrut(4));
    for (int i : pOrder) {
      ReplaceEntry entry = pEntries.get(i);
      pBox.add(entry);
      pBox.add(Box.createVerticalStrut(2));
    }
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
     pBox.add(entry);
     pBox.add(Box.createVerticalStrut(2));
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
      pEntries.remove(idx);
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
      
      pReplace = UIFactory.createEditableTextField(replace, 150, SwingConstants.LEFT);
      pReplace.setMaximumSize(pReplace.getPreferredSize());
      this.add(pReplace);
      this.add(Box.createHorizontalStrut(8));
      pParamName = UIFactory.createParamNameField(paramName, 150, SwingConstants.LEFT);
      pParamName.setMaximumSize(pParamName.getPreferredSize());
      this.add(pParamName);
      this.add(Box.createHorizontalStrut(8));
      pDefaultValue = 
        UIFactory.createEditableTextField(defaultValue, 150, SwingConstants.LEFT);
      pDefaultValue.setMaximumSize(pDefaultValue.getPreferredSize());
      this.add(pDefaultValue);
      this.add(Box.createHorizontalStrut(8));
      {
        JButton but = 
          UIFactory.createDialogButton("Up", "up-" + id, parent, "Move the replacement up.");
        this.add(but);
        this.add(Box.createHorizontalStrut(8));
      }
      {
        JButton but = 
          UIFactory.createDialogButton("Down", "down-" + id, parent, "Move the replacement down.");
        this.add(but);
        this.add(Box.createHorizontalStrut(8));
      }
      {
        JButton but = 
          UIFactory.createDialogButton("Remove", "remove-" + id, parent, "remove the replacement");
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
}
