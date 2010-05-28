package us.temerity.pipeline.plugin.TemplateManifestTool.v2_4_27;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   M A N I F E S T   T O O L                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Tool to add TemplateManifest Annotations to nodes.
 */
public 
class TemplateManifestTool
  extends CommonToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructor.
   */
  public 
  TemplateManifestTool()
  {
    super("TemplateManifest", new VersionID("2.4.27"), "Temerity", 
          "Tool to add TemplateManifest Annotations to nodes.");
    
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
      
        ArrayList<String> values = new ArrayList<String>();
        Collections.addAll(values, aParam, aDesc);
        pManifestField = UIFactory.createTitledCollectionField
          (tPanel, "Manifest Type:", sTSize, vPanel, values, sVSize, 
           "The type of manifest to assign to the selected nodes.");

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

    JToolDialog pDialog = new JToolDialog("Template Manifest Tool", scroll, "Confirm");
    pDialog.setMinimumSize(new Dimension(pDialog.getSize().width, 300));
    pDialog.setVisible(true);
    if (pDialog.wasConfirmed())
      return ": adding Template Manifest";
    
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
    String manifest = pManifestField.getSelected();

    for (String node : pSelected.keySet()) {
      BaseAnnotation annot = 
        plug.newAnnotation("TemplateManifest", new VersionID("2.4.27"), "Temerity");
      annot.setParamValue(aManifestType, manifest);
      NodeMod mod = mclient.getWorkingVersion(getAuthor(), getView(), node);
      mod.addAnnotation("TemplateManifest", annot);
      mclient.modifyProperties(getAuthor(), getView(), mod);
    }

    return false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 7269710624651778576L;

  private static final int sVSize = 250;
  private static final int sTSize = 150;
  
  public static final String aManifestType = "ManifestType";
  public static final String aParam        = "Param";
  public static final String aDesc         = "Desc";
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private JCollectionField pManifestField;
}
