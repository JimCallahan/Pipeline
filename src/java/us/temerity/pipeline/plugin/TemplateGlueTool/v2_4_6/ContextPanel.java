// $Id: ContextPanel.java,v 1.1 2009/06/11 05:35:08 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_6;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_3.*;
import us.temerity.pipeline.ui.*;


public 
class ContextPanel
  extends JPanel
  implements ActionListener
{
  public
  ContextPanel
  (
    TemplateNetworkScan scan,
    TemplateGlueInformation oldSettings  
  )
  {
    super();
    
    pNextID = 0;
    pOrder = new LinkedList<Integer>();
    pEntries = new TreeMap<Integer, ContextEntry>();
    pAddEntries = new ArrayList<AddEntry>();
    
    pMissing = scan.getContexts().keySet();
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    pBox = new Box(BoxLayout.PAGE_AXIS);
    
    {
      pButtonBox = new Box(BoxLayout.LINE_AXIS);
      
      JButton add = UIFactory.createDialogButton("Add", "add", this, "Add a Context");
      pButtonBox.add(add);
      pButtonBox.add(Box.createHorizontalGlue());
      pBox.add(pButtonBox);
      pBox.add(Box.createVerticalStrut(4));
    }
    
    {
      pHeaderBox = new Box(BoxLayout.LINE_AXIS);
      Dimension dim = new Dimension(150, 19);
      {
        JLabel label = UIFactory.createLabel("Context Name", dim.width, SwingConstants.LEFT);
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
      
      pHeaderBox.add(Box.createHorizontalGlue());
      pBox.add(pHeaderBox);
      pBox.add(Box.createVerticalStrut(4));
    }
    
    if (oldSettings != null) {
     MappedListSet<String, String> contexts = oldSettings.getContexts();
     DoubleMap<String, String, String> paramNames = oldSettings.getContextParamNames();
     for (Entry<String, ListSet<String>> entry : contexts.entrySet()) {
       String context = entry.getKey();
       ListMap<String, String> map = new ListMap<String, String>();
       for (String replace : entry.getValue() ) {
         String paramName = paramNames.get(context, replace);
         map.put(replace, paramName);
       }
       createEntry(context, map);
     }
    }
    createEntry(null, null);
    
    pBox.add(Box.createVerticalStrut(20));
    
    {
      pButtonBox2 = new Box(BoxLayout.LINE_AXIS);
      
      JButton add = 
        UIFactory.createDialogButton("Add All", "addall", this, "Add all the found optional branches");
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
  
  
  /**
   * @param rep
   * @param def
   * @param param
   */
  private void 
  createEntry
  (
    String context,
    ListMap<String, String> replacements
  )
  {
    ContextEntry entry = 
       new ContextEntry(this, pNextID, context, replacements);
     pBox.add(entry);
     pBox.add(Box.createVerticalStrut(2));
     pEntries.put(pNextID, entry);
     pOrder.add(pNextID);
     pNextID++;
  }
  
  public MappedListSet<String, String>
  getContexts()
    throws PipelineException
  {
    MappedListSet<String, String> toReturn = new MappedListSet<String, String>();
    for (int i : pOrder) {
      ContextEntry entry = pEntries.get(i);
      String context = entry.getContext();
      if (context != null && !context .equals("")) {
        if (toReturn.keySet().contains(context))
          throw new PipelineException
            ("The context (" + context + ") appears more than once in the panel.  " +
             "Please correct this before continuing.");
        ListSet<String> replacements = entry.getContextReplacements();
        toReturn.put(context, replacements);
      }
    }
    return toReturn;
  }
  
  public DoubleMap<String, String, String>
  getContextParamNames()
  {
    DoubleMap<String, String, String> toReturn = new DoubleMap<String, String, String>();
    for (int i : pOrder) {
      ContextEntry entry = pEntries.get(i);
      String context = entry.getContext();
      if (context != null && !context .equals("")) {
        TreeMap<String, String> paramNames = entry.getContextReplacementParamNames();
        toReturn.put(context, paramNames);
      }
    }
    return toReturn;
  }
  
  private TreeSet<String>
  collectContexts()
  {
    TreeSet<String> toReturn = new TreeSet<String>();
    for (int i : pOrder) {
      ContextEntry entry = pEntries.get(i);
      String mode = entry.getContext();
      if (mode != null && !mode.equals("")) {
        toReturn.add(mode);
      }
    }
    return toReturn;
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
      TreeSet<String> existing = collectContexts();
      for (String entry : pMissing) {
        if (!existing.contains(entry)) {
          createEntry(entry, null);
        }
      }
      relayout();
    }
    else if (command.startsWith("add-")) {
      String name = command.replace("add-", "");
      TreeSet<String> existing = collectContexts();
      if (!existing.contains(name)) 
        createEntry(name, null);
      relayout();
    }
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
      ContextEntry entry = pEntries.get(i);
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

    private static final long serialVersionUID = -9016055398021822651L;
  }
  
  private class
  ContextEntry
    extends Box
    implements ActionListener
  {
    private
    ContextEntry
    (
      ActionListener parent,
      int id,
      String context,
      ListMap<String, String> replacements
    )
    {
      super(BoxLayout.LINE_AXIS);
      
      ListMap<String, String> oldValues = new ListMap<String, String>();
      if (replacements != null)
        oldValues.putAll(replacements);
      
      pReplacements = new TreeMap<Integer, ReplacementEntry>();
      pReplaceOrder = new LinkedList<Integer>();

      pReplaceID = 0;
      pInsideBox = new Box(BoxLayout.PAGE_AXIS);
      
      Box hbox = new Box(BoxLayout.LINE_AXIS);
      
      pContext = UIFactory.createParamNameField(context, 150, SwingConstants.LEFT);
      pContext.setMaximumSize(pContext.getPreferredSize());
      hbox.add(pContext);
      hbox.add(Box.createHorizontalStrut(8));
      
      {
        JButton but = 
          UIFactory.createDialogButton("Add", "add", this, "Add another Context Replacement");
        hbox.add(but);
      }

      hbox.add(Box.createHorizontalStrut(4));
      
      {
        JButton but = 
          UIFactory.createDialogButton("Remove", "remove-" + id, parent, "remove the context");
        hbox.add(but);
      }
      hbox.add(Box.createHorizontalGlue());
      
      pInsideBox.add(hbox);
      
      pHeader = hbox;
      
      pInsideBox.add(Box.createVerticalStrut(2));
      
      {
        pReplaceHeader = new Box(BoxLayout.LINE_AXIS);
        pReplaceHeader.add(Box.createHorizontalStrut(75));
        JLabel l1 = UIFactory.createFixedLabel("Replacement", 150, SwingConstants.LEFT);
        pReplaceHeader.add(l1);
        pReplaceHeader.add(Box.createHorizontalStrut(8));
        JLabel l2 = UIFactory.createFixedLabel("Param Name", 150, SwingConstants.LEFT);
        pReplaceHeader.add(l2);
        pReplaceHeader.add(Box.createHorizontalGlue());
      }
      
      pInsideBox.add(pReplaceHeader);
      pInsideBox.add(Box.createVerticalStrut(2));
      
      for (Entry<String, String> replace : oldValues.entrySet()) {
        addContextReplacement(replace.getKey(), replace.getValue());
      }
      addContextReplacement(null, null);
      
      this.add(pInsideBox);
    }
    
    private void
    addContextReplacement
    (
      String replace,
      String paramName
    )
    {
      ReplacementEntry entry = 
        new ReplacementEntry(this, pReplaceID, replace, paramName);
      pInsideBox.add(entry);
      pInsideBox.add(Box.createVerticalStrut(2));
      pReplacements.put(pReplaceID, entry);
      pReplaceOrder.add(pReplaceID);
      pReplaceID++;
    }
    
    public String
    getContext()
    {
      return pContext.getText();
    }
    
    public ListSet<String>
    getContextReplacements()
      throws PipelineException
    {
      String context = getContext();
      ListSet<String> toReturn = new ListSet<String>();
      for (int i : pReplaceOrder) {
        ReplacementEntry entry = pReplacements.get(i);
        String replace = entry.getReplaceValue();
        if (replace != null && !replace.equals("")) {
          if (toReturn.contains(replace))
            throw new PipelineException
              ("The Replacement (" + replace + ") appears more than once in the context " +
               "(" + context + ") in the panel.  Please correct this before continuing.");
          toReturn.add(replace);
        }
      }
      return toReturn;
    }
    
    public TreeMap<String, String>
    getContextReplacementParamNames()
    {
      TreeMap<String, String> toReturn = new TreeMap<String, String>();
      for (int i : pReplaceOrder) {
        ReplacementEntry entry = pReplacements.get(i);
        String replace = entry.getReplaceValue();
        if (replace != null && !replace.equals("")) {
          toReturn.put(replace, entry.getParamName());
        }
      }
      return toReturn;
    }
    
    private void
    relayout()
    {
      pInsideBox.removeAll();
      pInsideBox.add(pHeader);
      pInsideBox.add(Box.createVerticalStrut(2));
      pInsideBox.add(pReplaceHeader);
      pInsideBox.add(Box.createVerticalStrut(2));
      for (int i : pReplaceOrder) {
        ReplacementEntry entry = pReplacements.get(i);
        pInsideBox.add(entry);
        pInsideBox.add(Box.createVerticalStrut(2));
      }
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
      if (command.startsWith("up-")) {
        int id = Integer.valueOf(command.replace("up-", ""));
        int idx = pReplaceOrder.indexOf(id);
        if (idx == 0)
          return;
        pReplaceOrder.remove(idx);
        pReplaceOrder.add(idx-1, id);
        relayout();
      }
      else if (command.startsWith("down-")) {
        int id = Integer.valueOf(command.replace("down-", ""));
        int idx = pReplaceOrder.indexOf(id);
        if (idx == (pReplaceOrder.size() - 1))
          return;
        pReplaceOrder.remove(idx);
        pReplaceOrder.add(idx+1, id);
        relayout();
      }
      else if (command.startsWith("remove-")) {
        int id = Integer.valueOf(command.replace("remove-", ""));
        int idx = pReplaceOrder.indexOf(id);
        pReplaceOrder.remove(idx);
        pReplacements.remove(idx);
        relayout();
      }
      else if (command.equals("add")) {
        addContextReplacement(null, null);
        relayout();
      }
    }


    private class
    ReplacementEntry
      extends Box
    {
      private
      ReplacementEntry
      (
        ActionListener parent,
        int id,
        String replace,
        String paramName
      )
      {
        super(BoxLayout.LINE_AXIS);
        
        this.add(Box.createHorizontalStrut(75));
        pReplaceValue = UIFactory.createEditableTextField(replace, 150, SwingConstants.LEFT);
        pReplaceValue.setMaximumSize(pReplaceValue.getPreferredSize());
        this.add(pReplaceValue);
        this.add(Box.createHorizontalStrut(8));
        pParamName = UIFactory.createParamNameField(paramName, 150, SwingConstants.LEFT);
        pParamName.setMaximumSize(pParamName.getPreferredSize());
        this.add(pParamName);
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
      getReplaceValue()
      {
        return pReplaceValue.getText();
      }
      
      private String
      getParamName()
      {
        return pParamName.getText();
      }
      
      private JTextField pReplaceValue;
      private JParamNameField pParamName;
      private static final long serialVersionUID = 5307019263179395388L;
    }

    
    private static final long serialVersionUID = 5865562915153825516L;

    
    private Box pInsideBox;
    
    private Box pHeader;
    private Box pReplaceHeader;

    private int pReplaceID;
    
    private JParamNameField pContext;
    private TreeMap<Integer, ReplacementEntry> pReplacements;
    private LinkedList<Integer> pReplaceOrder;
  }
  
  
  private static final long serialVersionUID = -6015881409571109974L;

  private int pNextID;
  private TreeMap<Integer, ContextEntry> pEntries;
  private LinkedList<Integer> pOrder;
  
  private Set<String> pMissing;
  private ArrayList<AddEntry> pAddEntries;
  
  private Box pBox;
  private Box pButtonBox;
  private Box pHeaderBox;
  private Box pButtonBox2;
}
