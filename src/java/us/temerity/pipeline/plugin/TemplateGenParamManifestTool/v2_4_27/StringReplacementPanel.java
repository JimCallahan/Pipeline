package us.temerity.pipeline.plugin.TemplateGenParamManifestTool.v2_4_27;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.builder.v2_4_12.*;
import us.temerity.pipeline.ui.*;


public 
class StringReplacementPanel
  extends JPanel
{
  public 
  StringReplacementPanel
  (
    TemplateGlueInformation glueInfo,
    TemplateParamManifest oldManifest
  )
  {
    super();
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    pBox = new Box(BoxLayout.PAGE_AXIS);
    
    pTitleBox = TemplateUIFactory.createTitleBox("String Replacements:");
    
    pNextID = 0;
    
    pEntries = new TreeMap<Integer, ReplaceEntry>();
    pOrder = new LinkedList<Integer>();
    
    {
      pHeaderBox = TemplateUIFactory.createHorizontalBox();
      int width = 150;
      {
        JLabel label = UIFactory.createFixedLabel("Replacement:", width, SwingConstants.LEFT);
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
      TreeMap<String, String> oldValues;
      if (oldManifest != null)
        oldValues = oldManifest.getReplacements();
      else
        oldValues = new TreeMap<String, String>();
      
      TreeMap<String, String> defaults = glueInfo.getReplacementDefaults();

      for (String replace : glueInfo.getReplacements()) {
        String value = oldValues.get(replace);
        if (value != null)
         createEntry(replace, value);
        else
          createEntry(replace, defaults.get(replace));
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
      ReplaceEntry entry = pEntries.get(i);
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
    String value
  )
  {
    ReplaceEntry entry = 
       new ReplaceEntry(rep, value);
     pEntries.put(pNextID, entry);
     pOrder.add(pNextID);
     pNextID++;
  }
  
  
  public TreeMap<String, String>
  getReplacementValues()
  {
    TreeMap<String, String> toReturn = new TreeMap<String, String>();
    for (int i : pOrder) {
      ReplaceEntry entry = pEntries.get(i);
      toReturn.put(entry.getReplace(), entry.getValue());
    }
    return toReturn;
  }

  
  private class
  ReplaceEntry
    extends Box
  {
    private
    ReplaceEntry
    (
      String replace,
      String oldValue
    )
    {
      super(BoxLayout.LINE_AXIS);
     
      this.add(TemplateUIFactory.createHorizontalIndent());
      pReplace = UIFactory.createTextField(replace, 150, SwingConstants.LEFT);
      pReplace.setMaximumSize(pReplace.getPreferredSize());
      this.add(pReplace);
      this.add(TemplateUIFactory.createHorizontalSpacer());
      pValue = 
        UIFactory.createEditableTextField(oldValue, 150, SwingConstants.LEFT);
      pValue.setMaximumSize(pValue.getPreferredSize());
      this.add(pValue);
      this.add(Box.createHorizontalGlue());
    }
    
    private String
    getReplace()
    {
      return pReplace.getText();
    }
    
    private String
    getValue()
    {
      return pValue.getText();
    }

    private static final long serialVersionUID = -2422542291929967743L;
    
    private JTextField pReplace;
    private JTextField pValue;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4959524028017325948L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private int pNextID;
  private TreeMap<Integer, ReplaceEntry> pEntries;
  private LinkedList<Integer> pOrder;
  
  private Box pBox;
  private Box pHeaderBox;
  private Box pTitleBox;
}
