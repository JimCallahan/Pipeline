// $Id: FrameRangePanel.java,v 1.1 2009/10/14 18:11:43 jesse Exp $

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
class OffsetsPanel
  extends JPanel
  implements ActionListener
{
  public
  OffsetsPanel
  (
    JTemplateGlueDialog dialog,
    TemplateNetworkScan scan,
    TemplateGlueInformation oldSettings,
    Set<String> contexts
  )
  {
    super();
    
    pNextID = 0;
    pOrder = new LinkedList<Integer>();
    pEntries = new TreeMap<Integer, OffsetEntry>();
    pAddEntries = new ArrayList<AddEntry>();
    
    pAllContexts = contexts;
    pDialog = dialog;
    
    pMissing = scan.getOffsets().keySet();
    pHasMissing = !pMissing.isEmpty();
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    pBox = new Box(BoxLayout.PAGE_AXIS);
    
    pTitleBox = TemplateUIFactory.createTitleBox("Offsets:");
    
    {
      pButtonBox = TemplateUIFactory.createHorizontalBox();
      JButton add = TemplateUIFactory.createPanelButton
        ("Add Offset", "add", this, "Add an Offset");
      pButtonBox.add(add);
      pButtonBox.add(Box.createHorizontalGlue());
    }
    
    {
      pHeaderBox = TemplateUIFactory.createHorizontalBox();
      int width = 150;
      {
        JLabel label = UIFactory.createFixedLabel("Offset:", width, SwingConstants.LEFT);
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
     MappedSet<String, String> ranges = oldSettings.getOffsets();
     TreeMap<String, Integer> defaults = oldSettings.getOffsetDefaults();
     
     for (Entry<String, TreeSet<String>> entry : ranges.entrySet()) {
       Integer def = defaults.get(entry.getKey());
       createEntry(entry.getKey(), def, entry.getValue());
     }
    }
    
    {
      pButtonBox2 = TemplateUIFactory.createHorizontalBox();
      
      pButtonBox2.add(UIFactory.createFixedLabel
        ("Found in scan: ", 150, SwingConstants.LEFT));
      
      pButtonBox2.add(TemplateUIFactory.createHorizontalSpacer());

      JButton add = 
        TemplateUIFactory.createPanelButton
          ("Add All", "addall", this, "Add all the found offsets");
      pButtonBox2.add(add);
      pButtonBox2.add(Box.createHorizontalGlue());
    }
    
    for (String extra : pMissing) {
      AddEntry entry = new AddEntry(this, extra, "frame range");
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
  
  
  public MappedSet<String, String>
  getOffsets()
    throws PipelineException
  {
    MappedSet<String, String> toReturn = new MappedSet<String, String>();
    for (int i : pOrder) {
      OffsetEntry entry = pEntries.get(i);
      String offset = entry.getOffset();
      if (offset != null && !offset.equals("")) {
        if (toReturn.keySet().contains(offset))
          throw new PipelineException
            ("The Offset (" + offset + ") appears more than once in the panel.  " +
             "Please correct this before continuing.");
        TreeSet<String> contexts = entry.getRangeContexts();
        toReturn.put(offset, contexts);
      }
    }
    return toReturn;
  }
  
  private TreeSet<String>
  collectOffsets()
  {
    TreeSet<String> toReturn = new TreeSet<String>();
    for (int i : pOrder) {
      OffsetEntry entry = pEntries.get(i);
      String offset = entry.getOffset();
      if (offset != null && !offset.equals("")) {
        toReturn.add(offset);
      }
    }
    return toReturn;
  }
  
  public TreeMap<String, Integer>
  getOffsetDefaults()
    throws PipelineException
  {
    TreeMap<String, Integer> toReturn = new TreeMap<String, Integer>();
    for (int i : pOrder) {
      OffsetEntry entry = pEntries.get(i);
      String replace = entry.getOffset();
      if (replace != null && !replace.equals("")) {
        Integer value = entry.getValue();
        
        if (value != null)
          toReturn.put(replace, value);
      }
    }
    return toReturn;
  }
  
  private void 
  createEntry
  (
    String offset,
    Integer defaultValue,
    TreeSet<String> contexts
  )
  {
    OffsetEntry entry = 
       new OffsetEntry(this, pNextID, offset, defaultValue, contexts);
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
      OffsetEntry entry = pEntries.get(i);
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
      createEntry(null, null, null);
      relayout();
    }
    else if (command.equals("addall")) {
      TreeSet<String> existing = collectOffsets();
      for (String entry : pMissing) {
        if (!existing.contains(entry)) {
          createEntry(entry, null, null);
        }
      }
      relayout();
    }
    else if (command.startsWith("add-")) {
      String name = command.replace("add-", "");
      TreeSet<String> existing = collectOffsets();
      if (!existing.contains(name)) 
        createEntry(name, null, null);
      relayout();
    }
  }
  


  private class
  OffsetEntry
    extends Box
    implements ActionListener
  {
    private
    OffsetEntry
    (
      ActionListener parent,
      int id,
      String range,
      Integer defaultValue,
      TreeSet<String> contexts
    )
    {
      super(BoxLayout.LINE_AXIS);

      this.add(TemplateUIFactory.createHorizontalIndent());

      
      TreeSet<String> old = new TreeSet<String>();
      if (contexts != null)
        old.addAll(contexts);

      pContextFields = new TreeMap<Integer, OffsetContextEntry>();
      pReplaceOrder = new LinkedList<Integer>();

      pReplaceID = 0;
      pInsideBox = new Box(BoxLayout.PAGE_AXIS);

      {
        Box hbox = TemplateUIFactory.createHorizontalBox();
        
        pOffset = UIFactory.createParamNameField(range, 150, SwingConstants.LEFT);
        pOffset.setMaximumSize(pOffset.getPreferredSize());
        hbox.add(pOffset);
        hbox.add(TemplateUIFactory.createHorizontalSpacer());
        pValue = UIFactory.createIntegerField(defaultValue, 70, SwingConstants.LEFT);
        pValue.setMaximumSize(pValue.getPreferredSize());
        hbox.add(pValue);
        
        hbox.add(Box.createHorizontalGlue());
        pHeader = hbox;
      }
      
      {
        pAddBox = TemplateUIFactory.createHorizontalBox();
        pAddBox.add(TemplateUIFactory.createSecondLevelIndent());
        JButton but = 
          TemplateUIFactory.createPanelButton
            ("Add Context", "add", this, "Add another Offset context.");
        pAddBox.add(but);
        pAddBox.add(Box.createHorizontalGlue());
      }
      
      {
        pReplaceHeader = TemplateUIFactory.createHorizontalBox();
        pReplaceHeader.add(TemplateUIFactory.createSecondLevelIndent());
        JLabel l1 = UIFactory.createFixedLabel("Offset Context:", 150, SwingConstants.LEFT);
        pReplaceHeader.add(l1);
        pReplaceHeader.add(Box.createHorizontalGlue());
      }
      for (String context : old) {
        if (pAllContexts.contains(context)) {
          addOffsetContext(context); 
        }
      }
      
      this.add(pInsideBox);
      
      relayout();
      
    }
    
    private void
    addOffsetContext
    (
      String context
    )
    {
      if (!pAllContexts.isEmpty() ) {
        OffsetContextEntry entry =
          new OffsetContextEntry(this, pReplaceID, context);
        pInsideBox.add(entry);
        pInsideBox.add(Box.createVerticalStrut(2));
        pContextFields.put(pReplaceID, entry);
        pReplaceOrder.add(pReplaceID);
        pReplaceID++;
      }
    }

    private String
    getOffset()
    {
      return pOffset.getText();
    }
    
    private Integer
    getValue()
    {
      return pValue.getValue();
    }
    
    
    public TreeSet<String>
    getRangeContexts()
    {
      TreeSet<String> toReturn = new TreeSet<String>();
      for (OffsetContextEntry entry : pContextFields.values()) {
        toReturn.add(entry.getContext());
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
        OffsetContextEntry entry = pContextFields.get(i);
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
        pContextFields.remove(id);
        relayout();
      }
      else if (command.equals("add")) {
        addOffsetContext(null);
        relayout();
      }
    }
    
    
    private class
    OffsetContextEntry
      extends Box
    {
      private
      OffsetContextEntry
      (
        ActionListener parent,
        int id,
        String context
      )
      {
        super(BoxLayout.LINE_AXIS);
        
        this.add(TemplateUIFactory.createHorizontalIndent());
        this.add(TemplateUIFactory.createSecondLevelIndent());

        pContext = 
          UIFactory.createCollectionField(pAllContexts, pDialog, 150);
        pContext.setMaximumSize(pContext.getPreferredSize());
        if (context != null)
          pContext.setSelected(context);
        
        this.add(pContext);
        
        this.add(TemplateUIFactory.createHorizontalSpacer());
        
        {
          JButton but = 
            TemplateUIFactory.createRemoveButton(parent, "remove-" + id);
          this.add(but);
        }
        this.add(Box.createHorizontalGlue());
      }

      public String
      getContext()
      {
        return pContext.getSelected();
      }
      
      static final long serialVersionUID = 4152235116045509382L;
      
      private JCollectionField pContext;
    }


    static final long serialVersionUID = -7871348827246497842L;

    private JParamNameField pOffset;
    private JIntegerField pValue;
    
    private Box pInsideBox;
    
    private Box pHeader;
    private Box pReplaceHeader;
    private Box pAddBox;
    
    private int pReplaceID;
    
    private LinkedList<Integer> pReplaceOrder;

    private TreeMap<Integer, OffsetContextEntry> pContextFields;
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -2833137426045695684L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private int pNextID;
  private TreeMap<Integer, OffsetEntry> pEntries;
  private LinkedList<Integer> pOrder;
  
  private Set<String> pMissing;
  private ArrayList<AddEntry> pAddEntries;
  
  private Box pTitleBox;
  private Box pBox;
  private Box pButtonBox;
  private Box pHeaderBox;
  private Box pButtonBox2;
  
  private Set<String> pAllContexts;
  
  private JTemplateGlueDialog pDialog;
  
  private boolean pHasMissing;
}
