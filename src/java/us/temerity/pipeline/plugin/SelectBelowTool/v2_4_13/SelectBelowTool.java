// $Id: SelectBelowTool.java,v 1.1 2009/11/03 17:47:01 jesse Exp $

package us.temerity.pipeline.plugin.SelectBelowTool.v2_4_13;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   S E L E C T   B E L O W   T O O L                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Select all the nodes underneath the targeted directory in the Node Browser.
 */
public 
class SelectBelowTool
  extends CommonToolUtils
{
  public
  SelectBelowTool()
  {
    super("SelectBelow", new VersionID("2.4.13"), "Temerity",
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
      
      return " : selecting nodes";
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
      String pattern = pPrefix + ".*";
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
      
      
      JToolDialog dialog = new JToolDialog("Null", null, "confirm");
      JConfirmDialog confirm = new JConfirmDialog(dialog, message);
      confirm.setVisible(true);
      if (confirm.wasConfirmed()) {
        pRoots.addAll(pNewRoots);
      }
      return null;
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private TreeSet<String> pNewRoots;
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6252276948766316845L;
}
