// $Id: TemplateExternalTool.java,v 1.2 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.TemplateExternalTool.v2_4_6;

import java.awt.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   E X T E R N A L   T O O L                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Tool for quickly adding a TemplateExternalAnnotations to a node.
 */
public 
class TemplateExternalTool
  extends CommonToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateExternalTool()
  {
    super("TemplateExternal", new VersionID("2.4.6"), "Temerity", 
          "Tool for adding a template external annotation to a node.");
    
    addSupport(OsType.Windows);
    addSupport(OsType.MacOS);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  O P S                                                                                 */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public synchronized String 
  collectPhaseInput()
    throws PipelineException
  {
    if (pSelected.size() != 1)
      throw new PipelineException
        ("You must have one and only one node selected to run this tool.");
    
    
    /* create dialog body components */ 
    JScrollPane scroll;
    {
      Box vBox = new Box(BoxLayout.Y_AXIS);

      {
        Component comps[] = UIFactory.createTitledPanels();
        JPanel tPanel = (JPanel) comps[0];
        JPanel vPanel = (JPanel) comps[1];
        Box body = (Box) comps[2];
        
        vBox.add(body);
      
        pExternalField = UIFactory.createTitledEditableTextField
          (tPanel, "External:", sTSize, vPanel, "", sVSize, 
           "The name of the template external to assign to the selected node.");

      }
      vBox.add(UIFactory.createFiller(sTSize +sVSize + 35));
      
      {
        scroll = new JScrollPane(vBox);

        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

        Dimension size = new Dimension(sTSize + sVSize + 35, 300);
        scroll.setMinimumSize(size);
      }
    }

    JToolDialog pDialog = new JToolDialog("Template External Tool", scroll, "Confirm");
    pDialog.setMinimumSize(new Dimension(pDialog.getSize().width, 300));
    pDialog.setVisible(true);
    if (pDialog.wasConfirmed())
      return ": adding Template External";
    
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
    String external = pExternalField.getText();
    if (external == null || external.equals(""))
      return false;

    for (String node : pSelected.keySet()) {
      BaseAnnotation annot = 
        plug.newAnnotation("TemplateExternal", new VersionID("2.4.6"), "Temerity");
      annot.setParamValue(aExternalName, external);
      NodeMod mod = mclient.getWorkingVersion(getAuthor(), getView(), node);
      mod.addAnnotation("TemplateExternal", annot);
      mclient.modifyProperties(getAuthor(), getView(), mod);
    }

    return false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5216382610279303884L;
  
  private static final int sVSize = 250;
  private static final int sTSize = 150;
  
  public static final String aExternalName = "ExternalName";

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
 
  private JTextField pExternalField;
}
