// $Id: SelectBelowCustomTool.java,v 1.1 2009/11/03 17:47:01 jesse Exp $

package us.temerity.pipeline.plugin.SelectBelowCustomTool.v2_4_13;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   S E L E C T   B E L O W   T O O L                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Display a dialog allowing conditions to be inputed before selecting all the nodes 
 * underneath the targeted directory in the Node Browser.
 */
public 
class SelectBelowCustomTool
  extends CommonToolUtils
{
  public
  SelectBelowCustomTool()
  {
    super("SelectBelowCustom", new VersionID("2.4.13"), "Temerity",
          "Select all the nodes underneath the targeted directory in the Node Browser.");

    underDevelopment();
    
    addPhase(new FirstPass());
    addPhase(new SecondPass());
    
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   F I R S T   P A S S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  private
  class FirstPass
    extends BaseTool.ToolPhase
  {
    @Override
    public String 
    collectInput()
      throws PipelineException
    {
      if (pPrefix == null)
        throw new PipelineException
          ("This tool needs to be run on a directory in the node browser");
      if (pPrimary != null)
        throw new PipelineException
          ("This tool needs to be run on a directory in the node browser");
      
      /* create the UI components */ 
      JScrollPane scroll = null;
      {
        Box hbox = new Box(BoxLayout.X_AXIS);
        {
          Component comps[] = UIFactory.createTitledPanels();
          JPanel tpanel = (JPanel) comps[0];
          JPanel vpanel = (JPanel) comps[1];
          
          pPatternField = UIFactory.createTitledEditableTextField
                        (tpanel, "Node Name Pattern:", sTSize, vpanel,
                         ".*", sVSize, 
                         "The string to search node names under this directory for.");
          
          hbox.add(comps[2]);
        }
        {
          scroll = new JScrollPane(hbox);

          scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
          scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

          scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

          Dimension size = new Dimension(sTSize + sVSize, 200);
          scroll.setMinimumSize(size);
        }
      }
      
      JToolDialog diag = new JToolDialog("Select Below Custom", scroll, "Select");

      diag.setVisible(true);
      if(diag.wasConfirmed()) 
        return " : selecting nodes";
      
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
      String match = pPatternField.getText();
      String pattern = pPrefix + match;
      pNewRoots = mclient.getNodeNames(pattern);
      if (pNewRoots.size() < 100) {
        pRoots.addAll(pNewRoots);
        return NextPhase.Finish;
      }
      
      return NextPhase.Continue;
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E C O N D   P A S S                                                                */
  /*----------------------------------------------------------------------------------------*/

  private
  class SecondPass
    extends BaseTool.ToolPhase
  {
    @Override
    public String 
    collectInput()
      throws PipelineException
    {
      String message = 
        "You are about to select " + pNewRoots.size() + " nodes.  " +
        "Are you sure you want to continue?";
      
      
      JConfirmDialog confirm = new JConfirmDialog(pDialog, message);
      confirm.setVisible(true);
      if (confirm.wasConfirmed()) {
        pRoots.addAll(pNewRoots);
      }
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
      return NextPhase.Finish;
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private TreeSet<String> pNewRoots;
  private JToolDialog pDialog;
  
  private JTextField pPatternField;
  
  private static final int sTSize = 150;
  private static final int sVSize = 300;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4432717964840104559L;
}
