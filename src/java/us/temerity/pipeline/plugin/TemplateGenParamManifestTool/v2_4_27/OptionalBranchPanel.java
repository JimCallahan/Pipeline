package us.temerity.pipeline.plugin.TemplateGenParamManifestTool.v2_4_27;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_12.*;
import us.temerity.pipeline.ui.*;


public 
class OptionalBranchPanel
  extends JPanel
{
  public 
  OptionalBranchPanel
  (
    TemplateGlueInformation glueInfo,
    TemplateParamManifest oldManifest
  )
  {
    super();
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    pBox = new Box(BoxLayout.PAGE_AXIS);
    
    pTitleBox = TemplateUIFactory.createTitleBox("Optional Branches:");
    
    pNextID = 0;
    
    pEntries = new TreeMap<Integer, BranchEntry>();
    pOrder = new LinkedList<Integer>();
    
    {
      pHeaderBox = TemplateUIFactory.createHorizontalBox();
      int width = 150;
      {
        JLabel label = UIFactory.createFixedLabel("Optional Branch:", width, SwingConstants.LEFT);
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
      TreeSet<String> branches = new TreeSet<String>();
      if (glueInfo != null)
        branches.addAll(glueInfo.getOptionalBranches().keySet());
      else
        branches.addAll(oldManifest.getOptionalBranches().keySet());
      
      TreeMap<String, Boolean> oldValues;
      if (oldManifest != null)
        oldValues = oldManifest.getOptionalBranches();
      else
        oldValues = new TreeMap<String, Boolean>();
      
      ListMap<String, Boolean> glueValues;
      if (glueInfo != null)
        glueValues = glueInfo.getOptionalBranches();
      else
        glueValues = new ListMap<String, Boolean>();
      
      for (String branch: branches) {
        Boolean value = oldValues.get(branch); 
        if (value != null)
         createEntry(branch, value);
        else
          createEntry(branch, glueValues.get(branch));
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
      BranchEntry entry = pEntries.get(i);
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
    Boolean value
  )
  {
    BranchEntry entry = 
       new BranchEntry(rep, value);
     pEntries.put(pNextID, entry);
     pOrder.add(pNextID);
     pNextID++;
  }
  
  
  public TreeMap<String, Boolean>
  getOptionalBranchValues()
  {
    TreeMap<String, Boolean> toReturn = new TreeMap<String, Boolean>();
    for (int i : pOrder) {
      BranchEntry entry = pEntries.get(i);
      toReturn.put(entry.getBranchName(), entry.getValue());
    }
    return toReturn;
  }

  
  private class
  BranchEntry
    extends Box
  {
    private
    BranchEntry
    (
      String branch,
      Boolean oldValue
    )
    {
      super(BoxLayout.LINE_AXIS);
     
      this.add(TemplateUIFactory.createHorizontalIndent());
      pBranchName = UIFactory.createTextField(branch, 150, SwingConstants.LEFT);
      pBranchName.setMaximumSize(pBranchName.getPreferredSize());
      this.add(pBranchName);
      this.add(TemplateUIFactory.createHorizontalSpacer());
      pValue = 
        UIFactory.createBooleanField(oldValue, 150);
      pValue.setMaximumSize(pValue.getPreferredSize());
      if (oldValue != null)
        pValue.setValue(oldValue);
      else
        pValue.setValue(false);
      this.add(pValue);
      this.add(Box.createHorizontalGlue());
    }
    
    private String
    getBranchName()
    {
      return pBranchName.getText();
    }
    
    private Boolean
    getValue()
    {
      return pValue.getValue();
    }

    private static final long serialVersionUID = 8028482878767393399L;
    
    private JTextField pBranchName;
    private JBooleanField pValue;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1184276373379798729L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private int pNextID;
  private TreeMap<Integer, BranchEntry> pEntries;
  private LinkedList<Integer> pOrder;
  
  private Box pBox;
  private Box pHeaderBox;
  private Box pTitleBox;
}
