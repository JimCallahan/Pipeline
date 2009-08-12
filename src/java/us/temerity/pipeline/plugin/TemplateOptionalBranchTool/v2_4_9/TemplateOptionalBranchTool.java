// $Id: TemplateOptionalBranchTool.java,v 1.1 2009/08/12 20:33:05 jesse Exp $

package us.temerity.pipeline.plugin.TemplateOptionalBranchTool.v2_4_9;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
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
    super("TemplateOptionalBranch", new VersionID("2.4.9"), "Temerity", 
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
      
        pOptionNameField = UIFactory.createTitledEditableTextField
          (tPanel, "Option Name:", sTSize, vPanel, "", sVSize, 
           "The name of the template option to assign to the selected nodes.");
        
        UIFactory.addVerticalSpacer(tPanel, vPanel, 3);
        
        ArrayList<String> values = new ArrayList<String>(OptionalBranchType.titles());
        pOptionTypeField = 
          UIFactory.createTitledCollectionField
          (tPanel, "Option Type:", sTSize, 
           vPanel, values, sVSize, 
           "The type of the template option to assign to the selected nodes.");
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
    String optionName = pOptionNameField.getText();
    String optionType = pOptionTypeField.getSelected();
    if (optionName == null || optionName.equals(""))
      return false;

    for (String node : pSelected.keySet()) {
      BaseAnnotation annot = 
        plug.newAnnotation("TemplateOptionalBranch", new VersionID("2.4.9"), "Temerity");
      annot.setParamValue(aOptionName, optionName);
      annot.setParamValue(aOptionType, optionType);
      NodeMod mod = mclient.getWorkingVersion(getAuthor(), getView(), node);
      mod.addAnnotation("TemplateOptionalBranch", annot);
      mclient.modifyProperties(getAuthor(), getView(), mod);
    }

    return false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6709071710312517847L;
  
  private static final int sVSize = 250;
  private static final int sTSize = 150;
  
  public static final String aOptionName = "OptionName";
  public static final String aOptionType = "OptionType";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
 
  private JTextField pOptionNameField;
  private JCollectionField pOptionTypeField;
}
