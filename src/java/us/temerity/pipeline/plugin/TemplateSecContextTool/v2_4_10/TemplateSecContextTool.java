// $Id: TemplateSecContextTool.java,v 1.1 2009/10/09 04:40:08 jesse Exp $

package us.temerity.pipeline.plugin.TemplateSecContextTool.v2_4_10;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   S E C   C O N T E X T   T O O L                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Tool for adding secondary context annotations to a group of nodes.
 */
public 
class TemplateSecContextTool
  extends TaskToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateSecContextTool()
  {
    super("TemplateSecContext", new VersionID("2.4.10"), "Temerity", 
          "Tool for adding context annotations to a group of nodes.");
    
    underDevelopment();
    
    addPhase(new FirstPass());
    addPhase(new SecondPass());
    
    addSupport(OsType.Windows);
    addSupport(OsType.MacOS);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  P H A S E S                                                                           */
  /*----------------------------------------------------------------------------------------*/

  private class
  FirstPass
    extends BaseTool.ToolPhase
  {
    @Override
    public String 
    collectInput()
      throws PipelineException
    {
      if (pSelected.size() != 1)
        throw new PipelineException
          ("You must have at one node selected to run this tool.");
      
      return " : Gathering info";
    }
    
    @Override
    public NextPhase 
    execute
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      pSequenceSet = new TreeSet<String>();
      
      NodeStatus stat = pSelected.values().iterator().next();
      
      NodeMod mod = mclient.getWorkingVersion(getAuthor(), getView(), stat.getName());
      
      SortedSet<FileSeq> seqs = mod.getSecondarySequences();
      if (seqs.isEmpty())
        throw new PipelineException
          ("(" + stat.getName() + ") has no secondary sequences, so it does not need a " +
           "secondary context");
      
      pSequenceSet.add(sALL);
      for (FileSeq seq : seqs)
        pSequenceSet.add(seq.getFilePattern().toString());
      
      return NextPhase.Continue;
    }
  }
  
  private class
  SecondPass
    extends BaseTool.ToolPhase
  {
    @Override
    public String 
    collectInput()
      throws PipelineException
    {
      pContextFields = new ArrayList<JTextField>();
      pSequenceFields = new ArrayList<JCollectionField>();
      
      /* create dialog body components */ 
      JScrollPane scroll;
      {
        pBody = new Box(BoxLayout.Y_AXIS);

        {
          Component comps[] = UIFactory.createTitledPanels();
          pTpanel = (JPanel) comps[0];
          pVpanel = (JPanel) comps[1];
          Box body = (Box) comps[2];
          
          pBody.add(body);
        }
        
        doAdd(null, null);
        
        pBody.add(UIFactory.createFiller(sTSize + sVSize * 2 + 47));
        
        {
          scroll = new JScrollPane(pBody);

          scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
          scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

          scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

          Dimension size = new Dimension(sTSize + sVSize * 2 + 47, 300);
          scroll.setMinimumSize(size);
          //scroll.setMaximumSize(size);
        }
      }

      pDialog = new JTemplateDialog(scroll);
      pDialog.setMinimumSize(new Dimension(pDialog.getSize().width, 300));
      pDialog.setVisible(true);
      if (pDialog.wasConfirmed())
        return ": adding Template Context";
      
      return null;
    }
    
    @Override
    public NextPhase 
    execute
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      PluginMgrClient plug = PluginMgrClient.getInstance();
      TreeMap<String, String> contexts = new TreeMap<String, String>();
      int i = 0;
      for (JTextField field : pContextFields) {
        String value = field.getText();
        if (value != null && !value.equals("")) {
          String seq = pSequenceFields.get(i).getSelected();
          contexts.put(value, seq);
        }
        i++;
      }
      
      if (contexts.isEmpty())
        return NextPhase.Finish;
      
      for (String node : pSelected.keySet()) {
        TreeMap<String, String> existing = new TreeMap<String, String>();
        
        NodeMod mod = mclient.getWorkingVersion(getAuthor(), getView(), node);
        TreeMap<String, BaseAnnotation> annots = mod.getAnnotations();
        
        // Get the existing contexts.
        for (String aName : annots.keySet()) {
          if (aName.startsWith("TemplateSecContext")) {
            String context = (String) annots.get(aName).getParamValue(aContextName);
            existing.put(aName, context);
          }
        }
        int newNum = 0;
        if (!existing.isEmpty())
          newNum = Integer.valueOf(existing.lastKey().replaceAll("TemplateSecContext", "")) + 1;
        for (Entry<String, String> entry : contexts.entrySet()) {
          String context = entry.getKey();
          String seq = entry.getValue();
          if (seq.equals(sALL))
            seq = null;
          if (!existing.values().contains(context)) {
            String aName = "TemplateSecContext" + pad(newNum); 
            BaseAnnotation annot = 
              plug.newAnnotation("TemplateSecContext", new VersionID("2.4.10"), "Temerity");
            annot.setParamValue(aContextName, context);
            annot.setParamValue(aSeqName, seq);
            mod.addAnnotation(aName, annot);
            newNum++;
          }
        }
        mclient.modifyProperties(getAuthor(), getView(), mod);
      }
      
      return NextPhase.Finish;
    }
  }
  

  
  private String 
  pad
  (
    int i
  )
  {
    String pad = String.valueOf(i);
    while(pad.length() < 4)
      pad = "0" + pad;
    return pad;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*  G U I   M E T H O D S                                                                 */
  /*----------------------------------------------------------------------------------------*/

  private void
  doAdd
  (
    String context,
    String fileSeq
  )
  {
    UIFactory.addVerticalSpacer(pTpanel, pVpanel, 6);
    pTpanel.add(UIFactory.createFixedLabel("Context:", sTSize, JLabel.RIGHT,
      "The name of the context to assign to the selected nodes."));
    Box hbox = new Box(BoxLayout.X_AXIS);
    {
      JTextField field = UIFactory.createEditableTextField(null, sVSize, JLabel.CENTER);
      field.setText(context);
      hbox.add(field);
      pContextFields.add(field);
    }
    hbox.add(Box.createHorizontalStrut(12));
    {
      JCollectionField seq = 
        UIFactory.createCollectionField(pSequenceSet, pDialog, sVSize);
      if (fileSeq != null) {
        if (pSequenceSet.contains(fileSeq)) 
          seq.setSelected(fileSeq);
        else
          seq.setSelected(sALL);
      }
      pSequenceFields.add(seq);
      hbox.add(seq);
    }

    pVpanel.add(hbox);
    pBody.revalidate();
  }
  
  private static
  String[][] getButtons()
  {
    String extra[][] = {
      { "Add",  "add" }
    };
    return extra;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L   C L A S S E S                                                       */
  /*----------------------------------------------------------------------------------------*/

  private class
  JTemplateDialog
    extends JToolDialog
  {
    private 
    JTemplateDialog
    (
      JComponent body  
    )
    {
      super("Template Sec Context Tool", body, "Confirm", null, getButtons() );
    }
    
    /*--------------------------------------------------------------------------------------*/
    /*   L I S T E N E R S                                                                  */
    /*--------------------------------------------------------------------------------------*/

    /*-- ACTION LISTENER METHODS -----------------------------------------------------------*/

    /** 
     * Invoked when an action occurs. 
     */ 
    @Override
    public void 
    actionPerformed
    (
     ActionEvent e
    ) 
    {
      String cmd = e.getActionCommand();
      if(cmd.equals("add")) 
        doAdd(null, null);
      else 
        super.actionPerformed(e);
    }
    private static final long serialVersionUID = -812105406854773228L;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6861392109225607718L;
  
  private static final int sVSize = 250;
  private static final int sTSize = 150;
  
  public static final String sALL = "[[ALL]]";
  
  public static final String aContextName = "ContextName";
  public static final String aSeqName = "SeqName";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private TreeSet<String> pSequenceSet;
  
  private ArrayList<JTextField> pContextFields;
  private ArrayList<JCollectionField> pSequenceFields;
  private JTemplateDialog pDialog;
  private Box pBody;
  private JPanel pTpanel;
  private JPanel pVpanel;
}
