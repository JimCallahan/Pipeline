// $Id: ContextsDefaultsPanel.java,v 1.1 2009/10/09 04:40:08 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_10;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_10.*;
import us.temerity.pipeline.ui.*;

public 
class ContextsDefaultsPanel
  extends JPanel
{
  public
  ContextsDefaultsPanel
  (
    TemplateGlueInformation oldSettings,
    MappedListSet<String, String> contexts
  )
  {
    super();

    pNextID = 0;
    pOrder = new LinkedList<Integer>();
    pEntries = new TreeMap<Integer, ContextDefaultEntry>();
    pEntryByContext = new TreeMap<String, ContextDefaultEntry>();
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    pBox = new Box(BoxLayout.PAGE_AXIS);
    
    Box titleBox = TemplateUIFactory.createTitleBox("Context Defaults:");
    pBox.add(titleBox);
    pBox.add(TemplateUIFactory.createLargeVerticalGap());
    
    
    TreeMap<String, ArrayList<TreeMap<String, String>>> olds = 
      new TreeMap<String, ArrayList<TreeMap<String,String>>>();
    if (oldSettings != null) {
      olds.putAll(oldSettings.getContextDefaults());
    }
    
    for (Entry<String, ListSet<String>> entry : contexts.entrySet()) {
      String context = entry.getKey();
      ListSet<String> replacements = entry.getValue();
      ArrayList<TreeMap<String, String>> defaults = olds.get(context);
      createEntry(context, replacements, defaults);
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
  
  private void 
  createEntry
  (
    String context,
    ListSet<String> replacements,
    ArrayList<TreeMap<String, String>> defaults
  )
  {
    ContextDefaultEntry entry = 
       new ContextDefaultEntry(context, replacements, defaults);
     pBox.add(entry);
     pBox.add(TemplateUIFactory.createLargeVerticalGap());
     pEntries.put(pNextID, entry);
     pEntryByContext.put(context, entry);
     pOrder.add(pNextID);
     pNextID++;
  }
  
  public MappedArrayList<String, TreeMap<String, String>> 
  getContextDefaults()
  {
    MappedArrayList<String, TreeMap<String, String>>toReturn = 
      new MappedArrayList<String, TreeMap<String, String>>();
    
    for (Entry<String, ContextDefaultEntry> entry : pEntryByContext.entrySet()) {
      String context = entry.getKey();
      ArrayList<TreeMap<String, String>> defaults = entry.getValue().getContextDefaults();
      if (defaults.size() > 0)
        toReturn.put(context, defaults);
    }
    return toReturn;
  }
  
  private class
  ContextDefaultEntry
    extends Box
    implements ActionListener
  {
    private
    ContextDefaultEntry
    (
      String context,
      ListSet<String> replacements,
      ArrayList<TreeMap<String, String>> defaults
    ) 
    {
      super(BoxLayout.LINE_AXIS);
      
      pReplacements = new ListSet<String>();
      if (replacements != null)
        pReplacements.addAll(replacements);
      
      ArrayList<TreeMap<String, String>> oldValues = 
        new ArrayList<TreeMap<String,String>>();
      if (defaults != null)
        oldValues.addAll(defaults);
      
      pReplaceOrder = new LinkedList<Integer>();
      pDefaults = new TreeMap<Integer, DefaultEntry>();
      
      pInsideBox = new Box(BoxLayout.PAGE_AXIS);
      
      {
        Box hbox = TemplateUIFactory.createHorizontalBox();

        pContextName = UIFactory.createTextField(context, 150, SwingConstants.LEFT);
        pContextName.setMaximumSize(pContextName.getPreferredSize());
        hbox.add(pContextName);
        hbox.add(Box.createHorizontalGlue());
        pHeader = hbox;
      }
      
      
      {
        pAddBox = TemplateUIFactory.createHorizontalBox();
        pAddBox.add(TemplateUIFactory.createSecondLevelIndent());
        JButton but = 
          TemplateUIFactory.createPanelButton
            ("Add Defaults", "add", this, "Add another Context Default");
        pAddBox.add(but);
        pAddBox.add(Box.createHorizontalGlue());
      }
      
      {
        pReplaceHeader = new Box(BoxLayout.LINE_AXIS);
        pReplaceHeader.add(Box.createHorizontalStrut(75));
        for (String replace : pReplacements) {
          JLabel l1 = UIFactory.createFixedLabel(replace + ":", 150, SwingConstants.LEFT);
          pReplaceHeader.add(l1);
        }
        pReplaceHeader.add(TemplateUIFactory.createHorizontalSpacer());
        pReplaceHeader.add(Box.createHorizontalGlue());
      }

      for (TreeMap<String, String> each : oldValues) {
        if (!each.isEmpty())
          addContextDefault(pReplacements, each);
      }
      
      this.add(pInsideBox);
      relayout();
    }
    
    public ArrayList<TreeMap<String, String>>
    getContextDefaults()
    {
      ArrayList<TreeMap<String, String>> toReturn = new ArrayList<TreeMap<String,String>>();
      
      for (DefaultEntry entry : pDefaults.values()) {
        TreeMap<String, String> val = entry.getDefaultValues();
        if (!val.isEmpty())
          toReturn.add(val);
      }
      return toReturn;
    }
    
    private void
    addContextDefault
    (
      ListSet<String> sorted,
      TreeMap<String, String> values
    )
    {
      DefaultEntry entry = 
        new DefaultEntry(this, pReplaceID, sorted, values);
      pDefaults.put(pReplaceID, entry);
      pReplaceOrder.add(pReplaceID);
      pReplaceID++;
    }
    
    private void
    relayout()
    {
      pInsideBox.removeAll();
      pInsideBox.add(pHeader);
      pInsideBox.add(TemplateUIFactory.createVerticalGap());
      pInsideBox.add(pReplaceHeader);
      pInsideBox.add(TemplateUIFactory.createVerticalGap());
      for (int i : pReplaceOrder) {
        DefaultEntry entry = pDefaults.get(i);
        pInsideBox.add(entry);
        pInsideBox.add(TemplateUIFactory.createVerticalGap());
      }
      pInsideBox.add(TemplateUIFactory.createVerticalGap());
      pInsideBox.add(pAddBox);
      pInsideBox.revalidate();
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
        int idx = pReplaceOrder.indexOf(id);
        pReplaceOrder.remove(idx);
        pDefaults.remove(id);
        relayout();
      }
      else if (command.equals("add")) {
        addContextDefault(pReplacements, null);
        relayout();
      }
    }
    
    private class
    DefaultEntry
      extends Box
    {
      private
      DefaultEntry
      (
        ActionListener parent,
        int id,
        ListSet<String> replacements,
        TreeMap<String, String> defaults
      )
      {
        super(BoxLayout.LINE_AXIS);
        
        this.add(TemplateUIFactory.createHorizontalIndent());
        this.add(TemplateUIFactory.createSecondLevelIndent());
        
        pDefaultValues = new TreeMap<String, JTextField>();
        
        for (String replace : replacements) {
          String dv = null;
          if (defaults != null)
            dv = defaults.get(replace);
          JTextField field = UIFactory.createEditableTextField(dv, 150, SwingConstants.LEFT);
          field.setMaximumSize(field.getPreferredSize());
          this.add(field);
          this.add(TemplateUIFactory.createHorizontalSpacer());
          pDefaultValues.put(replace, field);
        }
        
        {
          JButton but = TemplateUIFactory.createRemoveButton(parent, "remove-" + id);
          this.add(but);
        }

        this.add(Box.createHorizontalGlue());
      }
      
      private TreeMap<String, String>
      getDefaultValues()
      {
        TreeMap<String, String> toReturn = new TreeMap<String, String>();
        for (Entry<String, JTextField> entry : pDefaultValues.entrySet()) {
          String value = entry.getValue().getText();
          if (value != null && !value.equals(""))
            toReturn.put(entry.getKey(), value);
        }
        
        return toReturn;
      }

      private static final long serialVersionUID = -2642960639528864716L;
      
      private TreeMap<String, JTextField> pDefaultValues;
    }

    private static final long serialVersionUID = -5781238900790683836L;

    
    private Box pInsideBox;
    
    private Box pHeader;
    private Box pAddBox;
    private Box pReplaceHeader;

    private int pReplaceID;
    
    private ListSet<String> pReplacements;
    
    private LinkedList<Integer> pReplaceOrder;
    private TreeMap<Integer, DefaultEntry> pDefaults;
    private JTextField pContextName;
  }
  
  private static final long serialVersionUID = 2806258744745045971L;
  
  private int pNextID;
  private TreeMap<Integer, ContextDefaultEntry> pEntries;
  private TreeMap<String, ContextDefaultEntry> pEntryByContext;
  private LinkedList<Integer> pOrder;
  
  private Box pBox;
}
