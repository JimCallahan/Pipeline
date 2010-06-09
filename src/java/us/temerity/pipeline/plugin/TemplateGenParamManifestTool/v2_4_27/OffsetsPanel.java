package us.temerity.pipeline.plugin.TemplateGenParamManifestTool.v2_4_27;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.builder.v2_4_12.*;
import us.temerity.pipeline.ui.*;


public 
class OffsetsPanel
  extends JPanel
{
  public 
  OffsetsPanel
  (
    TreeSet<String> offsets,
    TemplateGlueInformation glueInfo,
    TemplateParamManifest oldManifest
  )
  {
    super();
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    pBox = new Box(BoxLayout.PAGE_AXIS);
    
    pTitleBox = TemplateUIFactory.createTitleBox("Offsets:");
    
    pNextID = 0;
    
    pEntries = new TreeMap<Integer, OffsetEntry>();
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
      TreeMap<String, Integer> oldValues;
      if (oldManifest != null)
        oldValues = oldManifest.getOffsets();
      else
        oldValues = new TreeMap<String, Integer>();
      
      TreeMap<String, Integer> glueValues;
      if (glueInfo != null)
        glueValues = glueInfo.getOffsetDefaults();
      else
        glueValues = new TreeMap<String, Integer>();
      
      for (String offset: offsets) {
        Integer value = oldValues.get(offset);
        if (value != null)
         createEntry(offset, value);
        else
          createEntry(offset, glueValues.get(offset));
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
      OffsetEntry entry = pEntries.get(i);
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
    Integer value
  )
  {
    OffsetEntry entry = 
       new OffsetEntry(rep, value);
     pEntries.put(pNextID, entry);
     pOrder.add(pNextID);
     pNextID++;
  }
  
  
  public TreeMap<String, Integer>
  getOffsetValues()
  {
    TreeMap<String, Integer> toReturn = new TreeMap<String, Integer>();
    for (int i : pOrder) {
      OffsetEntry entry = pEntries.get(i);
      String offset = entry.getOffsetName();
      if (offset != null && !offset.equals("")) {
        Integer offsetValue = entry.getOffset();
        if (offsetValue == null)
          offsetValue = 0;
        
        toReturn.put(offset, offsetValue);
      }
    }
    return toReturn;
  }

  
  private class
  OffsetEntry
    extends Box
  {
    private
    OffsetEntry
    (
      String offset,
      Integer oldValue
    )
    {
      super(BoxLayout.LINE_AXIS);
     
      this.add(TemplateUIFactory.createHorizontalIndent());
      
      pOffsetName = UIFactory.createTextField(offset, 150, SwingConstants.LEFT);
      pOffsetName.setMaximumSize(pOffsetName.getPreferredSize());
      this.add(pOffsetName);
      this.add(TemplateUIFactory.createHorizontalSpacer());
      pOffsetValue = UIFactory.createIntegerField(oldValue, 70, SwingConstants.LEFT);
      pOffsetValue.setMaximumSize(pOffsetValue.getPreferredSize());
      this.add(pOffsetValue);
      this.add(Box.createHorizontalGlue());
    }
    
    private String
    getOffsetName()
    {
      return pOffsetName.getText();
    }
    
    private Integer
    getOffset()
    {
      return pOffsetValue.getValue();
    }
    
    private static final long serialVersionUID = -6123236253558280449L;
    
    private JTextField pOffsetName;
    private JIntegerField pOffsetValue;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6947858103230001956L;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private int pNextID;
  private TreeMap<Integer, OffsetEntry> pEntries;
  private LinkedList<Integer> pOrder;
  
  private Box pBox;
  private Box pHeaderBox;
  private Box pTitleBox;
}
