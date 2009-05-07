// $Id: TemplateAOETool.java,v 1.1 2009/05/07 22:12:46 jesse Exp $

package us.temerity.pipeline.plugin.TemplateAOETool.v2_4_6;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   A O E   T O O L                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Tool for adding Action of Existence modes to nodes in templates.
 */
public 
class TemplateAOETool
  extends TaskToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateAOETool()
  {
    super("TemplateAOE", new VersionID("2.4.6"), "Temerity", 
          "Tool for adding Action of Existence modes to nodes in templates.");
    
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
    
    pModeFields = new ArrayList<JTextField>();
    pModeDefaultFields = new ArrayList<JCollectionField>();
    
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
      
      doAdd();
      
      pBody.add(UIFactory.createFiller(sTSize +sVSize*2 + 47));
      
      {
        scroll = new JScrollPane(pBody);

        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

        Dimension size = new Dimension(sTSize +sVSize*2 + 47, 300);
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
    TreeMap<String, ActionOnExistence> modes = new TreeMap<String, ActionOnExistence>();
    for (int i = 0; i < pModeFields.size(); i++) {
      String mode = pModeFields.get(i).getText();
      if (mode != null && !mode.equals("")) {
        ActionOnExistence aoe = ActionOnExistence.valueFromKey(pModeDefaultFields.get(i).getSelectedIndex());
        modes.put(mode, aoe);
      }
    }
    
    if (modes.isEmpty())
      return false;
    
    for (String node : pSelected.keySet()) {
      
      TreeMap<String, String> existing = new TreeMap<String, String>();
      TreeMap<String, String> existingReverse = new TreeMap<String, String>();
      
      NodeMod mod = mclient.getWorkingVersion(getAuthor(), getView(), node);
      
      TreeMap<String, BaseAnnotation> annots = mod.getAnnotations();
      
      // Get the existing modes.
      for (String aName : annots.keySet()) {
        if (aName.startsWith("TemplateAOE")) {
          String mode = (String) annots.get(aName).getParamValue(aModeName);
          existing.put(aName, mode);
          existingReverse.put(mode, aName);
        }
      }
      int newNum = 0;
      if (!existing.isEmpty())
        newNum = Integer.valueOf(existing.lastKey().replaceAll("TemplateAOE", "")) + 1;
      for (String mode : modes.keySet()) {
        ActionOnExistence aoe = modes.get(mode);
        BaseAnnotation annot = 
          plug.newAnnotation("TemplateAOE", new VersionID("2.4.6"), "Temerity");
        annot.setParamValue(aModeName, mode);
        annot.setParamValue(aActionOnExistence, aoe.toTitle());

        // If mode already exists
        if (existingReverse.keySet().contains(mode)) {
          String aName = existingReverse.get(mode);
          mod.addAnnotation(aName, annot);
        }
        else {
          String aName = "TemplateAOE" + pad(newNum);
          mod.addAnnotation(aName, annot);
        }
      }
      
      mclient.modifyProperties(getAuthor(), getView(), mod);
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
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  G U I   M E T H O D S                                                                 */
  /*----------------------------------------------------------------------------------------*/

  private void
  doAdd()
  {
    UIFactory.addVerticalSpacer(pTpanel, pVpanel, 6);
    pTpanel.add(UIFactory.createFixedLabel("AoE Mode:", sTSize, JLabel.RIGHT,
      "The name and default value for the AoE mode to assign to the selected nodes."));
    Box hbox = new Box(BoxLayout.X_AXIS);
    {
      JTextField mode = UIFactory.createEditableTextField(null, sVSize, JLabel.CENTER);
      hbox.add(mode);
      pModeFields.add(mode);
    }
    hbox.add(Box.createHorizontalStrut(12));
    {
      JCollectionField aoe = 
        UIFactory.createCollectionField(ActionOnExistence.titles(), pDialog, sVSize);
      pModeDefaultFields.add(aoe);
      hbox.add(aoe);
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
      super("Template AOE Tool", body, "Confirm", null, getButtons() );
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
    
    private static final long serialVersionUID = -8359443970497592566L;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4775606507292392901L;
  private static final int sVSize = 120;
  private static final int sTSize = 150;
  
  public static final String aModeName = "ModeName";
  public static final String aActionOnExistence = "ActionOnExistence";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private ArrayList<JTextField> pModeFields;
  private ArrayList<JCollectionField> pModeDefaultFields;
  private JTemplateDialog pDialog;
  private Box pBody;
  private JPanel pTpanel;
  private JPanel pVpanel;
}
