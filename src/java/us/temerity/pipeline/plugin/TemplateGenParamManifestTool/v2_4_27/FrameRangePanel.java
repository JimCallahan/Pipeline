package us.temerity.pipeline.plugin.TemplateGenParamManifestTool.v2_4_27;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_12.*;
import us.temerity.pipeline.ui.*;


public 
class FrameRangePanel
  extends JPanel
{
  public 
  FrameRangePanel
  (
    TreeSet<String> frameRanges,
    TemplateGlueInformation glueInfo,
    TemplateParamManifest oldManifest
  )
  {
    super();
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    pBox = new Box(BoxLayout.PAGE_AXIS);
    
    pTitleBox = TemplateUIFactory.createTitleBox("Frame Ranges:");
    
    pNextID = 0;
    
    pEntries = new TreeMap<Integer, RangeEntry>();
    pOrder = new LinkedList<Integer>();
    
    {
      pHeaderBox = TemplateUIFactory.createHorizontalBox();
      int width = 150;
      {
        JLabel label = UIFactory.createFixedLabel("Frame Range:", width, SwingConstants.LEFT);
        pHeaderBox.add(label);
      }
      pHeaderBox.add(TemplateUIFactory.createHorizontalSpacer());
      {
        JLabel label = UIFactory.createFixedLabel("Value:", width, SwingConstants.LEFT);
        pHeaderBox.add(label);
      }
      pHeaderBox.add(TemplateUIFactory.createHorizontalSpacer());
      
      pHeaderBox.add(Box.createHorizontalGlue());
    }
    
    {
      
    }

    {
      TreeMap<String, FrameRange> oldValues;
      if (oldManifest != null)
        oldValues = oldManifest.getFrameRanges();
      else
        oldValues = new TreeMap<String, FrameRange>();
      
      TreeMap<String, FrameRange> glueValues;
      if (glueInfo != null)
        glueValues = glueInfo.getFrameRangeDefaults();
      else
        glueValues = new TreeMap<String, FrameRange>();
      
      for (String range: frameRanges) {
        FrameRange value = oldValues.get(range);
        if (value != null)
         createEntry(range, value);
        else
          createEntry(range, glueValues.get(range));
      }
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
    pBox.add(UIFactory.createFiller(100));
    pBox.revalidate();
  }
  
  private void 
  createEntry
  (
    String rep,
    FrameRange value
  )
  {
    RangeEntry entry = 
       new RangeEntry(rep, value);
     pEntries.put(pNextID, entry);
     pOrder.add(pNextID);
     pNextID++;
  }
  
  
  public TreeMap<String, FrameRange>
  getRangeValues()
    throws PipelineException
  {
    TreeMap<String, FrameRange> toReturn = new TreeMap<String, FrameRange>();
    for (int i : pOrder) {
      RangeEntry entry = pEntries.get(i);
      String replace = entry.getRangeName();
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

  
  private class
  RangeEntry
    extends Box
  {
    private
    RangeEntry
    (
      String range,
      FrameRange oldValue
    )
    {
      super(BoxLayout.LINE_AXIS);
     
      this.add(TemplateUIFactory.createHorizontalIndent());
      
      Integer start = null;
      Integer end = null;
      Integer by = null;
      if (oldValue != null) {
        start = oldValue.getStart();
        end = oldValue.getEnd();
        by = oldValue.getBy();
      }
      
      pRangeName = UIFactory.createTextField(range, 150, SwingConstants.LEFT);
      pRangeName.setMaximumSize(pRangeName.getPreferredSize());
      this.add(pRangeName);
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
      this.add(Box.createHorizontalGlue());
    }
    
    private String
    getRangeName()
    {
      return pRangeName.getText();
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

    private static final long serialVersionUID = -7343729039904433114L;
    
    private JTextField pRangeName;
    private JIntegerField pStart;
    private JIntegerField pEnd;
    private JIntegerField pBy;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4959524028017325948L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private int pNextID;
  private TreeMap<Integer, RangeEntry> pEntries;
  private LinkedList<Integer> pOrder;
  
  private Box pBox;
  private Box pHeaderBox;
  private Box pTitleBox;
}
