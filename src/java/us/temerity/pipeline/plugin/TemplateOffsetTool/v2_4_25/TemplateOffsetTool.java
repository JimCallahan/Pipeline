// $Id: BaseBuilder.java,v 1.33 2007/11/01 19:08:53 jesse Exp $

package us.temerity.pipeline.plugin.TemplateOffsetTool.v2_4_25;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   O F F S E T   T O O L                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Tool for adding offset annotations to a group of nodes.
 * <p>
 * First select the nodes that will use the offset.  Then run the tool on the node that
 * is going to hold the offset information.
 */
public 
class TemplateOffsetTool
  extends TaskToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateOffsetTool()
  {
    super("TemplateOffset", new VersionID("2.4.25"), "Temerity", 
          "Tool for adding frame offset annotations to a group of nodes.");

    underDevelopment();
    
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
    if (pSelected.size() < 2)
      throw new PipelineException
        ("You must have at least two nodes selected to run this tool.");
    
    if (pPrimary == null)
      throw new PipelineException
        ("You must have a target node when you run this tool.");
    
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
      
        pOffsetField = UIFactory.createTitledEditableTextField
          (tPanel, "Offset:", sTSize, vPanel, "", sVSize, 
           "The name of the template offset to assign to the selected nodes.");

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

    JToolDialog pDialog = new JToolDialog("Template Offset Tool", scroll, "Confirm");
    pDialog.setMinimumSize(new Dimension(pDialog.getSize().width, 300));
    pDialog.setVisible(true);
    if (pDialog.wasConfirmed())
      return " : adding Template Offsets";
    
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
    String newOffset = pOffsetField.getText();
    if (newOffset == null || newOffset.equals(""))
      return false;
    
    TreeSet<String> sourceNodes = new TreeSet<String>(pSelected.keySet());
    sourceNodes.remove(pPrimary);

    TreeMap<String, BaseAnnotation> annots = 
      mclient.getAnnotations(getAuthor(), getView(), pPrimary);
    TreeMap<String, String> existing = new TreeMap<String, String>();

    TreeSet<String> aNames = new TreeSet<String>();
    for (String aName : annots.keySet()) {
      if (aName.startsWith("TemplateOffset") ) {
        String offset = (String) annots.get(aName).getParamValue(aOffsetName);
        String link = (String) annots.get(aName).getParamValue(aLinkName);
        existing.put(link, offset);
        aNames.add(aName);
      }
    }
    
    int newNum = 0;
    if (!aNames.isEmpty())
      newNum = Integer.valueOf(aNames.last().replaceAll("TemplateOffset", "")) + 1;

    NodeMod mod = mclient.getWorkingVersion(getAuthor(), getView(), pPrimary);
    Set<String> currentSources = mod.getSourceNames();

    for (String node : sourceNodes) {
      if (currentSources.contains(node)) {
        String aName = existing.get(node);
        if (aName == null ) { 
          aName = "TemplateOffset" + pad(newNum);
          newNum++;
        }
           
        BaseAnnotation annot = 
          plug.newAnnotation("TemplateOffset", new VersionID("2.4.25"), "Temerity");
        annot.setParamValue(aOffsetName, newOffset);
        annot.setParamValue(aLinkName, node);
        mod.addAnnotation(aName, annot);
      }
    }
  
    mclient.modifyProperties(getAuthor(), getView(), mod);
    
    return false;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
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
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4546228421274185473L;
  
  private static final int sVSize = 250;
  private static final int sTSize = 150;
  
  public static final String aOffsetName = "OffsetName";
  public static final String aLinkName   = "LinkName";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private JTextField pOffsetField;
}
