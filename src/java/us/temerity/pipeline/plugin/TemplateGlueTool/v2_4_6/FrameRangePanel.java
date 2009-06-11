// $Id: FrameRangePanel.java,v 1.1 2009/06/11 05:35:08 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_6;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_3.*;
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
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    pBox = new Box(BoxLayout.PAGE_AXIS);
    
    {
      pButtonBox = new Box(BoxLayout.LINE_AXIS);
      
      JButton add = UIFactory.createDialogButton("Add", "add", this, "Add a Frame Range");
      pButtonBox.add(add);
      pButtonBox.add(Box.createHorizontalGlue());
      pBox.add(pButtonBox);
      pBox.add(Box.createVerticalStrut(4));
    }
    
    {
      pHeaderBox = new Box(BoxLayout.LINE_AXIS);
      Dimension dim = new Dimension(150, 19);
      {
        JLabel label = UIFactory.createLabel("Frame Range", dim.width, SwingConstants.LEFT);
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
     TreeSet<String> ranges= oldSettings.getFrameRanges();
     TreeMap<String, FrameRange> defaults = oldSettings.getFrameRangeDefaults();
     
     for (String range : ranges) {
       FrameRange def = defaults.get(range);
       createEntry(range, def);
     }
    }
    createEntry(null, null);
    
    pBox.add(Box.createVerticalStrut(20));
    
    {
      pButtonBox2 = new Box(BoxLayout.LINE_AXIS);
      
      JButton add = 
        UIFactory.createDialogButton("Add All", "addall", this, "Add all the found frame ranges");
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
      RangeEntry entry = pEntries.get(i);
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
    
    private static final long serialVersionUID = -3561925475151159308L;
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
      this.add(Box.createHorizontalStrut(8));
      pStart = UIFactory.createIntegerField(start, 70, SwingConstants.LEFT);
      pStart.setMaximumSize(pStart.getPreferredSize());
      this.add(pStart);
      this.add(Box.createHorizontalStrut(8));
      {
        JLabel label = new JLabel("to");

        label.setName("DisableLabel");
        label.setMaximumSize(label.getPreferredSize());

        this.add(label);
      }
      this.add(Box.createHorizontalStrut(8));
      pEnd = UIFactory.createIntegerField(end, 70, SwingConstants.LEFT);
      pEnd.setMaximumSize(pEnd.getPreferredSize());
      this.add(pEnd);
      this.add(Box.createHorizontalStrut(8));
      {
        JLabel label = new JLabel("by");

        label.setName("DisableLabel");
        label.setMaximumSize(label.getPreferredSize());

        this.add(label);
      }
      this.add(Box.createHorizontalStrut(8));
      pBy = UIFactory.createIntegerField(by, 70, SwingConstants.LEFT);
      pBy.setMaximumSize(pBy.getPreferredSize());
      this.add(pBy);
      this.add(Box.createHorizontalStrut(4));
      
      {
        JButton but = 
          UIFactory.createDialogButton("Remove", "remove-" + id, parent, "remove the replacement");
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
  
  private Box pBox;
  private Box pButtonBox;
  private Box pHeaderBox;
  private Box pButtonBox2;
}
