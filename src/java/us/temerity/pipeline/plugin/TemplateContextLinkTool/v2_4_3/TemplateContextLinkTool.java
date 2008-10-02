// $Id: TemplateContextLinkTool.java,v 1.1 2008/10/02 00:26:56 jesse Exp $

package us.temerity.pipeline.plugin.TemplateContextLinkTool.v2_4_3;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   C O N T E X T   L I N K   T O O L                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Tool for adding context link animation to a group of nodes.
 * <p>
 * First select the nodes that need to be in the context.  Then run the tool on the node that
 * is going to hold the context information.
 */
public 
class TemplateContextLinkTool
  extends TaskToolUtils
{

  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  public 
  TemplateContextLinkTool()
  {
    super("TemplateContextLink", new VersionID("2.4.3"), "Temerity", 
          "Tool for adding context link animation to a group of nodes.");
    
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
    if (pSelected.size() < 2)
      throw new PipelineException
        ("You must have at least twonode selected to run this tool.");
    
    if (pPrimary == null)
      throw new PipelineException
        ("You must have a target node when you run this tool.");
    
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
    
    TreeSet<String> sourceNodes = new TreeSet<String>(pSelected.keySet());
    sourceNodes.remove(pPrimary);
    
    TreeMap<String, BaseAnnotation> annots = mclient.getAnnotations(pPrimary);
    MappedSet<String, String> existing = new MappedSet<String, String>();
    
    TreeSet<String> aNames = new TreeSet<String>();
    for (String aName : annots.keySet()) {
      if (aName.startsWith("TemplateContextLink") ) {
        String context = (String) annots.get(aName).getParamValue(aContextName);
        String link = (String) annots.get(aName).getParamValue(aLinkName);
        existing.put(link, context);
        aNames.add(aName);
      }
    }
    
    int newNum = 0;
    if (!aNames.isEmpty())
      newNum = Integer.valueOf(aNames.last().replaceAll("TemplateContextLink", "")) + 1;
    
    for (String node : sourceNodes) {
      TreeSet<String> exists = existing.get(node);
      for (String context : contexts) {
        if (exists == null || !exists.contains(context)) {
          String aName = "TemplateContextLink" + pad(newNum); 
          BaseAnnotation annot = 
            plug.newAnnotation("TemplateContextLink", new VersionID("2.4.3"), "Temerity");
          annot.setParamValue(aContextName, context);
          annot.setParamValue(aLinkName, node);
          mclient.addAnnotation(pPrimary, aName, annot);
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

  private static final int sVSize = 250;
  private static final int sTSize = 150;
  private static final long serialVersionUID = -8965693742770436582L;
  
  public static final String aContextName = "ContextName";
  public static final String aLinkName = "LinkName";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private ArrayList<JTextField> pContextFields;
  private JTemplateDialog pDialog;
  private Box pBody;
  private JPanel pTpanel;
  private JPanel pVpanel;
}
