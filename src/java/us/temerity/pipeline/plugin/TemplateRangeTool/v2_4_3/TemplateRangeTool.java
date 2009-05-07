// $Id: TemplateRangeTool.java,v 1.2 2009/05/07 03:12:50 jesse Exp $

package us.temerity.pipeline.plugin.TemplateRangeTool.v2_4_3;

import java.awt.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   R A N G E   T O O L                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Tool for quickly adding TemplateRangeAnnotations to multiple nodes.
 */
public 
class TemplateRangeTool
  extends TaskToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateRangeTool()
  {
    super("TemplateRange", new VersionID("2.4.3"), "Temerity", 
          "Tool for adding range annotation to a group of nodes.");
    
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
      
        pRangeField = UIFactory.createTitledEditableTextField
          (tPanel, "Range:", sTSize, vPanel, "", sVSize, 
           "The name of the template range to assign to the selected nodes.");

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

    JToolDialog pDialog = new JToolDialog("Template Range Tool", scroll, "Confirm");
    pDialog.setMinimumSize(new Dimension(pDialog.getSize().width, 300));
    pDialog.setVisible(true);
    if (pDialog.wasConfirmed())
      return ": adding Template Range";
    
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
    String range = pRangeField.getText();
    if (range == null || range.equals(""))
      return false;

    for (String node : pSelected.keySet()) {
      BaseAnnotation annot = 
        plug.newAnnotation("TemplateRange", new VersionID("2.4.3"), "Temerity");
      annot.setParamValue(aRangeName, range);
      NodeMod mod = mclient.getWorkingVersion(getAuthor(), getView(), node);
      mod.addAnnotation("TemplateRange", annot);
      mclient.modifyProperties(getAuthor(), getView(), mod);
    }

    return false;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6054461332747859686L;
  
  private static final int sVSize = 250;
  private static final int sTSize = 150;
  
  public static final String aRangeName = "RangeName";

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
 
  private JTextField pRangeField;
}
