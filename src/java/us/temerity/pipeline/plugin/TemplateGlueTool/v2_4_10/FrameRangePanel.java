// $Id: FrameRangePanel.java,v 1.1 2009/10/09 04:40:08 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_10;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_10.*;
import us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_10.TemplateUIFactory.*;
import us.temerity.pipeline.ui.*;


public 
class FrameRangePanel
  extends JPanel
  implements ActionListener
{
  public
  FrameRangePanel
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
    pEntries = new TreeMap<Integer, RangeEntry>();
    pAddEntries = new ArrayList<AddEntry>();
    
    pAllContexts = contexts;
    pDialog = dialog;
    
    pMissing = scan.getFrameRanges().keySet();
    pHasMissing = !pMissing.isEmpty();
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    pBox = new Box(BoxLayout.PAGE_AXIS);
    
    pTitleBox = TemplateUIFactory.createTitleBox("Frame Ranges:");
    
    {
      pButtonBox = TemplateUIFactory.createHorizontalBox();
      JButton add = TemplateUIFactory.createPanelButton("Add Frame Range", "add", this, "Add a Frame Range");
      pButtonBox.add(add);
      pButtonBox.add(Box.createHorizontalGlue());
    }
    
    {
      pHeaderBox = TemplateUIFactory.createHorizontalBox();
      int width = 150;
      {
        JLabel label = UIFactory.createFixedLabel("Frame Range:", width, SwingConstants.LEFT);
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
     MappedSet<String, String> ranges= oldSettings.getFrameRanges();
     TreeMap<String, FrameRange> defaults = oldSettings.getFrameRangeDefaults();
     
     for (Entry<String, TreeSet<String>> entry : ranges.entrySet()) {
       FrameRange def = defaults.get(entry.getKey());
       createEntry(entry.getKey(), def, entry.getValue());
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
  getFrameRanges()
    throws PipelineException
  {
    MappedSet<String, String> toReturn = new MappedSet<String, String>();
    for (int i : pOrder) {
      RangeEntry entry = pEntries.get(i);
      String range = entry.getRange();
      if (range != null && !range.equals("")) {
        if (toReturn.keySet().contains(range))
          throw new PipelineException
            ("The Range (" + range + ") appears more than once in the panel.  " +
             "Please correct this before continuing.");
        TreeSet<String> contexts = entry.getRangeContexts();
        toReturn.put(range, contexts);
      }
    }
    return toReturn;
  }
  
  private TreeSet<String>
  collectFrameRanges()
  {
    TreeSet<String> toReturn = new TreeSet<String>();
    for (int i : pOrder) {
      RangeEntry entry = pEntries.get(i);
      String range = entry.getRange();
      if (range != null && !range.equals("")) {
        toReturn.add(range);
      }
    }
    return toReturn;
  }
  
  public TreeMap<String, FrameRange>
  getFrameRangeDefaults()
    throws PipelineException
  {
    TreeMap<String, FrameRange> toReturn = new TreeMap<String, FrameRange>();
    for (int i : pOrder) {
      RangeEntry entry = pEntries.get(i);
      String replace = entry.getRange();
      if (replace != null && !replace.equals("")) {
        Integer start = entry.getStart();
        Integer end = entry.getEnd();
        Integer by = entry.getBy();
        FrameRange range;
        try {
        if (start == null)
          range = null;
        else if (end == null)
          range = new FrameRange(start);
        else
          range = new FrameRange(start, end, by);
        }
        catch (Exception ex) {
          throw new PipelineException("Problem generating frame range.\n" + ex.getMessage());
        }
        
        if (range != null)
          toReturn.put(replace, range);
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
    FrameRange defaultValues,
    TreeSet<String> contexts
  )
  {
    RangeEntry entry = 
       new RangeEntry(this, pNextID, range, defaultValues, contexts);
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
      RangeEntry entry = pEntries.get(i);
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
      TreeSet<String> existing = collectFrameRanges();
      for (String entry : pMissing) {
        if (!existing.contains(entry)) {
          createEntry(entry, null, null);
        }
      }
      relayout();
    }
    else if (command.startsWith("add-")) {
      String name = command.replace("add-", "");
      TreeSet<String> existing = collectFrameRanges();
      if (!existing.contains(name)) 
        createEntry(name, null, null);
      relayout();
    }
  }
  


  private class
  RangeEntry
    extends Box
    implements ActionListener
  {
    private
    RangeEntry
    (
      ActionListener parent,
      int id,
      String range,
      FrameRange defaultValue,
      TreeSet<String> contexts
    )
    {
      super(BoxLayout.LINE_AXIS);

      this.add(TemplateUIFactory.createHorizontalIndent());

      Integer start = null;
      Integer end = null;
      Integer by = null;
      if (defaultValue != null) {
        start = defaultValue.getStart();
        end = defaultValue.getEnd();
        by = defaultValue.getBy();
      }

      TreeSet<String> old = new TreeSet<String>();
      if (contexts != null)
        old.addAll(contexts);

      pContextFields = new TreeMap<Integer, FrameRangeContextEntry>();
      pReplaceOrder = new LinkedList<Integer>();

      pReplaceID = 0;
      pInsideBox = new Box(BoxLayout.PAGE_AXIS);

      {
        Box hbox = TemplateUIFactory.createHorizontalBox();
        
        pRange = UIFactory.createParamNameField(range, 150, SwingConstants.LEFT);
        pRange.setMaximumSize(pRange.getPreferredSize());
        hbox.add(pRange);
        hbox.add(TemplateUIFactory.createHorizontalSpacer());
        pStart = UIFactory.createIntegerField(start, 70, SwingConstants.LEFT);
        pStart.setMaximumSize(pStart.getPreferredSize());
        hbox.add(pStart);
        hbox.add(TemplateUIFactory.createHorizontalSpacer());
        {
          JLabel label = new JLabel("to");

          label.setName("DisableLabel");
          label.setMaximumSize(label.getPreferredSize());

          hbox.add(label);
        }
        hbox.add(TemplateUIFactory.createHorizontalSpacer());
        pEnd = UIFactory.createIntegerField(end, 70, SwingConstants.LEFT);
        pEnd.setMaximumSize(pEnd.getPreferredSize());
        hbox.add(pEnd);
        hbox.add(TemplateUIFactory.createHorizontalSpacer());
        {
          JLabel label = new JLabel("by");

          label.setName("DisableLabel");
          label.setMaximumSize(label.getPreferredSize());

          hbox.add(label);
        }
        hbox.add(TemplateUIFactory.createHorizontalSpacer());
        pBy = UIFactory.createIntegerField(by, 70, SwingConstants.LEFT);
        pBy.setMaximumSize(pBy.getPreferredSize());
        hbox.add(pBy);
        hbox.add(TemplateUIFactory.createHorizontalSpacer());
        
        {
          JButton but = TemplateUIFactory.createRemoveButton(parent, "remove-" +  id);
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
            ("Add Context", "add", this, "Add another Frame Range context.");
        pAddBox.add(but);
        pAddBox.add(Box.createHorizontalGlue());
      }
      
      {
        pReplaceHeader = TemplateUIFactory.createHorizontalBox();
        pReplaceHeader.add(TemplateUIFactory.createSecondLevelIndent());
        JLabel l1 = UIFactory.createFixedLabel("Range Context:", 150, SwingConstants.LEFT);
        pReplaceHeader.add(l1);
        pReplaceHeader.add(Box.createHorizontalGlue());
      }
      for (String context : old) {
        if (pAllContexts.contains(context)) {
          addRangeContext(context); 
        }
      }
      
      this.add(pInsideBox);
      
      relayout();
      
    }
    
    private void
    addRangeContext
    (
      String context
    )
    {
      if (!pAllContexts.isEmpty() ) {
        FrameRangeContextEntry entry =
          new FrameRangeContextEntry(this, pReplaceID, context);
        pInsideBox.add(entry);
        pInsideBox.add(Box.createVerticalStrut(2));
        pContextFields.put(pReplaceID, entry);
        pReplaceOrder.add(pReplaceID);
        pReplaceID++;
      }
    }

    private String
    getRange()
    {
      return pRange.getText();
    }
    
    private Integer
    getStart()
    {
      return pStart.getValue();
    }
    
    private Integer
    getEnd()
    {
      return pEnd.getValue();
    }

    private Integer
    getBy()
    {
      return pBy.getValue();
    }
    
    public TreeSet<String>
    getRangeContexts()
    {
      TreeSet<String> toReturn = new TreeSet<String>();
      for (FrameRangeContextEntry entry : pContextFields.values()) {
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
        FrameRangeContextEntry entry = pContextFields.get(i);
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
        addRangeContext(null);
        relayout();
      }
    }
    
    
    private class
    FrameRangeContextEntry
      extends Box
    {
      private
      FrameRangeContextEntry
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
      
      private static final long serialVersionUID = -5649055113307335000L;
      
      private JCollectionField pContext;
    }

    private static final long serialVersionUID = -7515290741588276034L;
    
    private JParamNameField pRange;
    private JIntegerField pStart;
    private JIntegerField pEnd;
    private JIntegerField pBy;
    
    private Box pInsideBox;
    
    private Box pHeader;
    private Box pReplaceHeader;
    private Box pAddBox;
    
    private int pReplaceID;
    
    private LinkedList<Integer> pReplaceOrder;

    private TreeMap<Integer, FrameRangeContextEntry> pContextFields;
  }
  
  private static final long serialVersionUID = 261267973144247060L;
  
  private int pNextID;
  private TreeMap<Integer, RangeEntry> pEntries;
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
