// $Id: ContextPanel.java,v 1.1 2009/10/14 18:11:43 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_12;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_12.*;
import us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_12.TemplateUIFactory.*;
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
    pHasMissing = !(pMissing.isEmpty());
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    pBox = new Box(BoxLayout.PAGE_AXIS);
    
    pTitleBox = TemplateUIFactory.createTitleBox("Contexts:");
    
    {
      pButtonBox = TemplateUIFactory.createHorizontalBox();
      
      JButton add = TemplateUIFactory.createPanelButton("Add Context", "add", this, "Add a Context");
      pButtonBox.add(add);
      pButtonBox.add(Box.createHorizontalGlue());
    }
    
    {
      pHeaderBox = TemplateUIFactory.createHorizontalBox();
      int width = 150;
      {
        JLabel label = UIFactory.createFixedLabel("Context Name:", width, SwingConstants.LEFT);
        pHeaderBox.add(label);
      }
      pHeaderBox.add(Box.createHorizontalGlue());
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
      AddEntry entry = new AddEntry(this, extra, "Context");
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
      if (context != null && !context.equals("")) {
        if (toReturn.keySet().contains(context))
          throw new PipelineException
            ("The context (" + context + ") appears more than once in the panel.  " +
             "Please correct this before continuing.");
        ListSet<String> replacements = entry.getContextReplacements();
        if (replacements.isEmpty())
          throw new PipelineException
            ("The context (" + context + ") contains no replacements.");
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
      pEntries.remove(id);
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
    pBox.add(pTitleBox);
    pBox.add(TemplateUIFactory.createLargeVerticalGap());
    pBox.add(pHeaderBox);
    pBox.add(TemplateUIFactory.createVerticalGap());
    for (int i : pOrder) {
      ContextEntry entry = pEntries.get(i);
      pBox.add(entry);
      pBox.add(TemplateUIFactory.createLargeVerticalGap());
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
      
      this.add(TemplateUIFactory.createHorizontalIndent());
      
      ListMap<String, String> oldValues = new ListMap<String, String>();
      if (replacements != null)
        oldValues.putAll(replacements);
      
      pReplacements = new TreeMap<Integer, ReplacementEntry>();
      pReplaceOrder = new LinkedList<Integer>();

      pReplaceID = 0;
      pInsideBox = new Box(BoxLayout.PAGE_AXIS);

      {
        Box hbox = TemplateUIFactory.createHorizontalBox();
        pContext = UIFactory.createParamNameField(context, 150, SwingConstants.LEFT);
        pContext.setMaximumSize(pContext.getPreferredSize());
        hbox.add(pContext);
        
        hbox.add(TemplateUIFactory.createHorizontalSpacer());
        {
          JButton but = TemplateUIFactory.createRemoveButton(parent, "remove-" + id); 
          hbox.add(but);
        }
        hbox.add(Box.createHorizontalGlue());
        
        pHeader = hbox;
      }
      
      {
        pAddBox = TemplateUIFactory.createHorizontalBox();
        pAddBox.add(TemplateUIFactory.createSecondLevelIndent());
        JButton but = 
          TemplateUIFactory.createPanelButton
            ("Add Replacement", "add", this, "Add another Context Replacement");
        pAddBox.add(but);
        pAddBox.add(Box.createHorizontalGlue());
      }

      
      {
        pReplaceHeader = TemplateUIFactory.createHorizontalBox();
        pReplaceHeader.add(TemplateUIFactory.createSecondLevelIndent());
        JLabel l1 = UIFactory.createFixedLabel("Replacement:", 150, SwingConstants.LEFT);
        pReplaceHeader.add(l1);
        pReplaceHeader.add(TemplateUIFactory.createHorizontalSpacer());
        JLabel l2 = UIFactory.createFixedLabel("Param Name:", 150, SwingConstants.LEFT);
        pReplaceHeader.add(l2);
        pReplaceHeader.add(Box.createHorizontalGlue());
      }
      
      for (Entry<String, String> replace : oldValues.entrySet()) {
        addContextReplacement(replace.getKey(), replace.getValue());
      }
      
      this.add(pInsideBox);
      relayout();
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
      pInsideBox.add(TemplateUIFactory.createVerticalGap());
      pInsideBox.add(pReplaceHeader);
      pInsideBox.add(TemplateUIFactory.createVerticalGap());
      for (int i : pReplaceOrder) {
        ReplacementEntry entry = pReplacements.get(i);
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
        pReplacements.remove(id);
        relayout();
      }
      else if (command.equals("add")) {
        addContextReplacement(null, null);
        relayout();
      }
    }

    @Override
    public String 
    toString()
    {
      return pContext.getText();
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
        
        this.add(TemplateUIFactory.createHorizontalIndent());
        this.add(TemplateUIFactory.createSecondLevelIndent());
        
        pReplaceValue = UIFactory.createEditableTextField(replace, 150, SwingConstants.LEFT);
        pReplaceValue.setMaximumSize(pReplaceValue.getPreferredSize());
        this.add(pReplaceValue);
        
        this.add(TemplateUIFactory.createHorizontalSpacer());
        
        pParamName = UIFactory.createParamNameField(paramName, 150, SwingConstants.LEFT);
        pParamName.setMaximumSize(pParamName.getPreferredSize());
        this.add(pParamName);
        
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
          JButton but = TemplateUIFactory.createRemoveButton(parent, "remove-" + id);
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
    private Box pAddBox;

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
  private Box pTitleBox;
  private Box pButtonBox;
  private Box pHeaderBox;
  private Box pButtonBox2;
  
  private boolean pHasMissing;
}
