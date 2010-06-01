package us.temerity.pipeline.plugin.TemplateGenParamManifestTool.v2_4_27;

import java.awt.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;

import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_12.*;
import us.temerity.pipeline.ui.*;


public 
class AoEModePanel
  extends JPanel
{
  public 
  AoEModePanel
  (
    JTemplateGenParamDialog dialog,
    TemplateGlueInformation glueInfo,
    TemplateParamManifest oldManifest
  )
  {
    super();
    pDialog = dialog;
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    pBox = new Box(BoxLayout.PAGE_AXIS);
    
    pTitleBox = TemplateUIFactory.createTitleBox("AoE Modes:");
    
    pNextID = 0;
    
    pEntries = new TreeMap<Integer, AOEEntry>();
    pOrder = new LinkedList<Integer>();
    
    {
      pHeaderBox = TemplateUIFactory.createHorizontalBox();
      int width = 150;
      {
        JLabel label = UIFactory.createFixedLabel("AoE Mode:", width, SwingConstants.LEFT);
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
      TreeMap<String, ActionOnExistence> oldValues;
      if (oldManifest != null)
        oldValues = oldManifest.getAOEModes();
      else
        oldValues = new TreeMap<String, ActionOnExistence>();

      for (Entry<String, ActionOnExistence> entry: glueInfo.getAOEModes().entrySet()) {
        String mode = entry.getKey();
        ActionOnExistence aoe = oldValues.get(mode);
        if (aoe != null)
          createEntry(mode, aoe.toString());
        else
          createEntry(mode, entry.getValue().toString());
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
      AOEEntry entry = pEntries.get(i);
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
    AOEEntry entry = 
       new AOEEntry(rep, value);
     pEntries.put(pNextID, entry);
     pOrder.add(pNextID);
     pNextID++;
  }
  
  
  public TreeMap<String, ActionOnExistence>
  getAOEModes()
  {
    TreeMap<String, ActionOnExistence> toReturn = new TreeMap<String, ActionOnExistence>();
    for (int i : pOrder) {
      AOEEntry entry = pEntries.get(i);
      toReturn.put(entry.getAoEMode(), entry.getValue());
    }
    return toReturn;
  }

  
  private class
  AOEEntry
    extends Box
  {
    private
    AOEEntry
    (
      String replace,
      String oldValue
    )
    {
      super(BoxLayout.LINE_AXIS);
     
      this.add(TemplateUIFactory.createHorizontalIndent());
      pAoEMode = UIFactory.createTextField(replace, 150, SwingConstants.LEFT);
      pAoEMode.setMaximumSize(pAoEMode.getPreferredSize());
      this.add(pAoEMode);
      this.add(TemplateUIFactory.createHorizontalSpacer());
      ArrayList<String> values = ActionOnExistence.titles();
      pValue = UIFactory.createCollectionField(values, pDialog, 150);
      pValue.setMaximumSize(pValue.getPreferredSize());
      if (oldValue != null)
        pValue.setSelected(oldValue);
      this.add(pValue);
      this.add(Box.createHorizontalGlue());
    }
    
    private String
    getAoEMode()
    {
      return pAoEMode.getText();
    }
    
    private ActionOnExistence
    getValue()
    {
      int idx = pValue.getSelectedIndex();
      
      return ActionOnExistence.values()[idx];
    }

    private static final long serialVersionUID = -8120756096207527005L;

    
    private JTextField pAoEMode;
    private JCollectionField pValue;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6577844941263198815L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private int pNextID;
  private TreeMap<Integer, AOEEntry> pEntries;
  private LinkedList<Integer> pOrder;
  
  private Box pBox;
  private Box pHeaderBox;
  private Box pTitleBox;
  
  private JTemplateGenParamDialog pDialog;
}
