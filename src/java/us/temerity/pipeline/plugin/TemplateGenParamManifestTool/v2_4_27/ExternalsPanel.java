package us.temerity.pipeline.plugin.TemplateGenParamManifestTool.v2_4_27;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_12.*;
import us.temerity.pipeline.ui.*;


public 
class ExternalsPanel
  extends JPanel
{
  public
  ExternalsPanel
  (
    JTemplateGenParamDialog parent,
    TreeSet<String> externals,
    TemplateParamManifest oldManifest
  )
  {
    super();
    
    pFileDialog = new JFileSeqSelectDialog(parent);
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    pBox = new Box(BoxLayout.PAGE_AXIS);
    
    pTitleBox = TemplateUIFactory.createTitleBox("Externals:");
    
    pNextID = 0;
    
    pEntries = new TreeMap<Integer, ExternalsEntry>();
    pOrder = new LinkedList<Integer>();
    
    {
      pHeaderBox = TemplateUIFactory.createHorizontalBox();
      int width = 150;
      {
        JLabel label = UIFactory.createFixedLabel("Externals:", width, SwingConstants.LEFT);
        pHeaderBox.add(label);
      }
      pHeaderBox.add(TemplateUIFactory.createHorizontalSpacer());
      {
        JLabel label = UIFactory.createFixedLabel
          ("File Sequence:", width, SwingConstants.LEFT);
        pHeaderBox.add(label);
      }
      pHeaderBox.add(TemplateUIFactory.createHorizontalSpacer());
      {
        JLabel label = UIFactory.createFixedLabel("Start Frame:", width, SwingConstants.LEFT);
        pHeaderBox.add(label);
      }
      pHeaderBox.add(TemplateUIFactory.createHorizontalSpacer());
      
      pHeaderBox.add(Box.createHorizontalGlue());
    }
    
    {
      TreeMap<String, TemplateExternalData> oldValues;
      if (oldManifest != null)
        oldValues = oldManifest.getExternals();
      else
        oldValues = new TreeMap<String, TemplateExternalData>();
      
      
      for (String external : externals) {
        TemplateExternalData value = oldValues.get(external);
        createEntry(external, value);
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
      ExternalsEntry entry = pEntries.get(i);
      pBox.add(entry);
      pBox.add(TemplateUIFactory.createVerticalGap());
    }
    pBox.add(UIFactory.createFiller(100));
    pBox.revalidate();
  }
  
  public TreeMap<String, TemplateExternalData>
  getExternalValues()
  {
    TreeMap<String, TemplateExternalData> toReturn = 
      new TreeMap<String, TemplateExternalData>();
    for (int i : pOrder) {
      ExternalsEntry entry = pEntries.get(i);
      String external = entry.getExternalsName();
      if (external != null && !external.equals("")) {
        TemplateExternalData externalValue = entry.getTemplateExternalData();
        
        toReturn.put(external, externalValue);
      }
    }
    return toReturn;
  }
  
  private void 
  createEntry
  (
    String external,
    TemplateExternalData data
  )
  {
    ExternalsEntry entry = 
       new ExternalsEntry(external, data);
     pEntries.put(pNextID, entry);
     pOrder.add(pNextID);
     pNextID++;
  }
  
  
  private class
  ExternalsEntry
    extends Box
    implements ActionListener
  {
    private
    ExternalsEntry
    (
      String external,
      TemplateExternalData oldValue
    )
    {
      super(BoxLayout.LINE_AXIS);
     
      this.add(TemplateUIFactory.createHorizontalIndent());
      
      Integer startFrame = null;
      FileSeq fileSeq = null;
      String fileSeqValue = null;
      if (oldValue != null) {
        startFrame = oldValue.getStartFrame();
        fileSeq = oldValue.getFileSeq();
        fileSeqValue = fileSeq.toString();
      }
      
      pExternalName = UIFactory.createTextField(external, 150, SwingConstants.LEFT);
      pExternalName.setMaximumSize(pExternalName.getPreferredSize());
      this.add(pExternalName);
      
      this.add(TemplateUIFactory.createHorizontalSpacer());
      JComponent[] comps = UIFactory.createBrowsableStringField
        (fileSeqValue, 150, SwingConstants.LEFT, this, "browse");
      pExternalValue = (JTextField) comps[0];
      pExternalValue.setEditable(false);
      Box valueBox = Box.createVerticalBox();
      valueBox.add(comps[2]);
      valueBox.setMaximumSize(comps[2].getPreferredSize());
      this.add(valueBox);

      this.add(TemplateUIFactory.createHorizontalSpacer());
      pStartValue = UIFactory.createIntegerField(startFrame, 150, SwingConstants.LEFT);
      pStartValue.setMaximumSize(pStartValue.getPreferredSize());
      this.add(pStartValue);
    }
    
    private String
    getExternalsName()
    {
      return pExternalName.getText();
    }
    
    private TemplateExternalData
    getTemplateExternalData()
    {
      String external = pExternalValue.getText();
      FileSeq fseq = FileSeq.fromString(external);
      Integer startValue = pStartValue.getValue();
      
      TemplateExternalData data = new TemplateExternalData(fseq, startValue);
      
      return data;
    }
    
    @Override
    public void 
    actionPerformed
    (
      ActionEvent e
    )
    {
      Path start = new Path("//");
      
      String value = pExternalValue.getText();
      if (value != null  && !value.equals(""))
        start = new Path(value).getParentPath();
      pFileDialog.setRootDir(new Path("//").toFile());
      pFileDialog.updateTargetDir(start.toFile());
      
      pFileDialog.setVisible(true);
      if (!pFileDialog.wasConfirmed()) 
        return;
      
      FileSeq selected = pFileDialog.getSelectedFileSeq();
      FileSeq expanded = new FileSeq("/" + pFileDialog.getDirectoryPath().toString(), selected);
      pExternalValue.setText(expanded.toString());
    }

    
    private static final long serialVersionUID = 2437037601299452537L;

    private JTextField pExternalName;
    private JTextField pExternalValue;
    private JIntegerField pStartValue;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8711076188282896829L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private int pNextID;
  private TreeMap<Integer, ExternalsEntry> pEntries;
  private LinkedList<Integer> pOrder;
  
  private JFileSeqSelectDialog pFileDialog;
  
  private Box pBox;
  private Box pHeaderBox;
  private Box pTitleBox;
}
