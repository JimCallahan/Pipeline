// $Id: TemplateOptionalBranchTool.java,v 1.1 2009/05/26 07:09:32 jesse Exp $

package us.temerity.pipeline.plugin.TemplateOptionalBranchTool.v2_4_6;

import java.awt.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.plugin.TemplateOptionalBranchAnnotation.v2_4_6.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   O P T I O N A L   B R A N C H   A N N O T A T I O N                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Tool to add a {@link TemplateOptionalBranchAnnotation} to nodes.
 */
public 
class TemplateOptionalBranchTool
  extends CommonToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateOptionalBranchTool()
  {
    super("TemplateOptionalBranch", new VersionID("2.4.6"), "Temerity", 
          "Tool to add a TemplateOptionalBranch Annotation to nodes.");
    
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
      
        pOptionField = UIFactory.createTitledEditableTextField
          (tPanel, "Option:", sTSize, vPanel, "", sVSize, 
           "The name of the template option to assign to the selected nodes.");

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

    JToolDialog pDialog = new JToolDialog("Template Optional Branch Tool", scroll, "Confirm");
    pDialog.setMinimumSize(new Dimension(pDialog.getSize().width, 300));
    pDialog.setVisible(true);
    if (pDialog.wasConfirmed())
      return ": adding Template Optional Branch Annotations";
    
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
    String option = pOptionField.getText();
    if (option == null || option.equals(""))
      return false;

    for (String node : pSelected.keySet()) {
      BaseAnnotation annot = 
        plug.newAnnotation("TemplateOptionalBranch", new VersionID("2.4.6"), "Temerity");
      annot.setParamValue(aOptionName, option);
      NodeMod mod = mclient.getWorkingVersion(getAuthor(), getView(), node);
      mod.addAnnotation("TemplateOptionalBranch", annot);
      mclient.modifyProperties(getAuthor(), getView(), mod);
    }

    return false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3173256588022059471L;
  
  private static final int sVSize = 250;
  private static final int sTSize = 150;
  
  public static final String aOptionName = "OptionName";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
 
  private JTextField pOptionField;
}
