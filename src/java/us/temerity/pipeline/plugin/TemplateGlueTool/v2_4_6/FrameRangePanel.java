// $Id: FrameRangePanel.java,v 1.2 2009/06/11 19:41:22 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_6;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_3.*;
import us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_6.TemplateUIFactory.*;
import us.temerity.pipeline.ui.*;


public 
class FrameRangePanel
  extends JPanel
  implements ActionListener
{
  public
  FrameRangePanel
  (
    TemplateNetworkScan scan,
    TemplateGlueInformation oldSettings  
  )
  {
    super();
    
    pNextID = 0;
    pOrder = new LinkedList<Integer>();
    pEntries = new TreeMap<Integer, RangeEntry>();
    pAddEntries = new ArrayList<AddEntry>();
    
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
     TreeSet<String> ranges= oldSettings.getFrameRanges();
     TreeMap<String, FrameRange> defaults = oldSettings.getFrameRangeDefaults();
     
     for (String range : ranges) {
       FrameRange def = defaults.get(range);
       createEntry(range, def);
     }
    }
    
    {
      pButtonBox2 = TemplateUIFactory.createHorizontalBox();
      
      pButtonBox2.add(UIFactory.createFixedLabel
        ("Found in scan: ", 150, SwingConstants.LEFT));
      
      pHeaderBox.add(TemplateUIFactory.createHorizontalSpacer());

      JButton add = 
        TemplateUIFactory.createPanelButton("Add All", "addall", this, "Add all the found frame ranges");
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
  
  
  public TreeSet<String>
  getFrameRanges()
    throws PipelineException
  {
    TreeSet<String> toReturn = new TreeSet<String>();
    for (int i : pOrder) {
      RangeEntry entry = pEntries.get(i);
      String range = entry.getRange();
      if (range != null && !range.equals("")) {
        if (toReturn.contains(range))
          throw new PipelineException
            ("The Range (" + range + ") appears more than once in the panel.  " +
             "Please correct this before continuing.");
        toReturn.add(range);
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
    FrameRange defaultValues
  )
  {
    RangeEntry entry = 
       new RangeEntry(this, pNextID, range, defaultValues);
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
      createEntry(null, null);
      relayout();
    }
    else if (command.equals("addall")) {
      TreeSet<String> existing = collectFrameRanges();
      for (String entry : pMissing) {
        if (!existing.contains(entry)) {
          createEntry(entry, null);
        }
      }
      relayout();
    }
    else if (command.startsWith("add-")) {
      String name = command.replace("add-", "");
      TreeSet<String> existing = collectFrameRanges();
      if (!existing.contains(name)) 
        createEntry(name, null);
      relayout();
    }
  }
  


  private class
  RangeEntry
    extends Box
  {
    private
    RangeEntry
    (
      ActionListener parent,
      int id,
      String range,
      FrameRange defaultValue
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
      
      pRange = UIFactory.createParamNameField(range, 150, SwingConstants.LEFT);
      pRange.setMaximumSize(pRange.getPreferredSize());
      this.add(pRange);
      this.add(TemplateUIFactory.createHorizontalSpacer());
      pStart = UIFactory.createIntegerField(start, 70, SwingConstants.LEFT);
      pStart.setMaximumSize(pStart.getPreferredSize());
      this.add(pStart);
      this.add(TemplateUIFactory.createHorizontalSpacer());
      {
        JLabel label = new JLabel("to");

        label.setName("DisableLabel");
        label.setMaximumSize(label.getPreferredSize());

        this.add(label);
      }
      this.add(TemplateUIFactory.createHorizontalSpacer());
      pEnd = UIFactory.createIntegerField(end, 70, SwingConstants.LEFT);
      pEnd.setMaximumSize(pEnd.getPreferredSize());
      this.add(pEnd);
      this.add(TemplateUIFactory.createHorizontalSpacer());
      {
        JLabel label = new JLabel("by");

        label.setName("DisableLabel");
        label.setMaximumSize(label.getPreferredSize());

        this.add(label);
      }
      this.add(TemplateUIFactory.createHorizontalSpacer());
      pBy = UIFactory.createIntegerField(by, 70, SwingConstants.LEFT);
      pBy.setMaximumSize(pBy.getPreferredSize());
      this.add(pBy);
      this.add(TemplateUIFactory.createHorizontalSpacer());
      
      {
        JButton but = TemplateUIFactory.createRemoveButton(parent, "remove-" +  id);
        this.add(but);
      }
      
      this.add(Box.createHorizontalGlue());
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

    private static final long serialVersionUID = -7515290741588276034L;
    
    private JParamNameField pRange;
    private JIntegerField pStart;
    private JIntegerField pEnd;
    private JIntegerField pBy;
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
  
  private boolean pHasMissing;
}
