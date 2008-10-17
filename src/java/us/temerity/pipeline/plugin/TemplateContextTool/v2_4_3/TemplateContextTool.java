// $Id: TemplateContextTool.java,v 1.2 2008/10/17 03:36:46 jesse Exp $

package us.temerity.pipeline.plugin.TemplateContextTool.v2_4_3;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   C O N T E X T   T O O L                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Tool for adding context animation to a group of nodes.
 */
public 
class TemplateContextTool
  extends TaskToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  public 
  TemplateContextTool()
  {
    super("TemplateContext", new VersionID("2.4.3"), "Temerity", 
          "Tool for adding context animation to a group of nodes.");
    
    addSupport(OsType.Windows);
    addSupport(OsType.MacOS);
    
    underDevelopment();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  O P S                                                                                 */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public synchronized String 
  collectPhaseInput()
    throws PipelineException
  {
    if (pSelected.size() == 0)
      throw new PipelineException
        ("You must have at least one node selected to run this tool.");
    
    pContextFields = new ArrayList<JTextField>();
    
    /* create dialog body components */ 
    JScrollPane scroll;
    {
      Box vbox = new Box(BoxLayout.Y_AXIS);

      {
        Component comps[] = UIFactory.createTitledPanels();
        pTpanel = (JPanel) comps[0];
        pVpanel = (JPanel) comps[1];
        pBody = (Box) comps[2];
        
        vbox.add(pBody);
      }
      { 
        JTextField field = UIFactory.createTitledEditableTextField
          (pTpanel, "Context:", sTSize, pVpanel, "", sVSize, 
           "The name of the context to assign to the selected nodes.");
        pContextFields.add(field);

      }
      
      vbox.add(UIFactory.createFiller(sTSize +sVSize + 35));
      
      {
        scroll = new JScrollPane(vbox);

        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

        Dimension size = new Dimension(sTSize + sVSize + 35, 300);
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
  public synchronized boolean 
  executePhase
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient
  )
    throws PipelineException
  {
    PluginMgrClient plug = PluginMgrClient.getInstance();
    TreeSet<String> contexts = new TreeSet<String>();
    for (JTextField field : pContextFields) {
      String value = field.getText();
      if (value != null && !value.equals(""))
        contexts.add(value);
    }
    
    if (contexts.isEmpty())
      return false;
    
    for (String node : pSelected.keySet()) {
      TreeMap<String, BaseAnnotation> annots = mclient.getAnnotations(node);
      TreeMap<String, String> existing = new TreeMap<String, String>();
      
      // Get the existing contexts.
      for (String aName : annots.keySet()) {
        if (aName.startsWith("TemplateContext") && !aName.startsWith("TemplateContextLink")) {
          String context = (String) annots.get(aName).getParamValue(aContextName);
          existing.put(aName, context);
        }
      }
      int newNum = 0;
      if (!existing.isEmpty())
        newNum = Integer.valueOf(existing.lastKey().replaceAll("TemplateContext", "")) + 1;
      for (String context : contexts) {
        if (!existing.values().contains(context)) {
          String aName = "TemplateContext" + pad(newNum); 
          BaseAnnotation annot = 
            plug.newAnnotation("TemplateContext", new VersionID("2.4.3"), "Temerity");
          annot.setParamValue(aContextName, context);
          mclient.addAnnotation(node, aName, annot);
          newNum++;
        }
      }
    }
    
    return false;
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



  private void
  doAdd()
  {
    UIFactory.addVerticalSpacer(pTpanel, pVpanel, 6);
    JTextField field = UIFactory.createTitledEditableTextField
      (pTpanel, "Context:", sTSize, pVpanel, "", sVSize, 
       "The name of the context to assign to the selected nodes.");
    pContextFields.add(field);
    pDialog.validate();
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
      super("Template Context Tool", body, "Confirm", null, getButtons() );
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
        doAdd();
      else 
        super.actionPerformed(e);
    }
    private static final long serialVersionUID = -415336365090863281L;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -104985977551664428L;
  private static final int sVSize = 250;
  private static final int sTSize = 150;
  
  public static final String aContextName = "ContextName";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private ArrayList<JTextField> pContextFields;
  private JTemplateDialog pDialog;
  private Box pBody;
  private JPanel pTpanel;
  private JPanel pVpanel;
  
}
