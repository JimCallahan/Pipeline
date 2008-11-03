// $Id: MultipleRenameTool.java,v 1.3 2008/11/03 23:47:37 jesse Exp $

package us.temerity.pipeline.plugin.MultipleRenameTool.v2_4_3;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   M U L T I P L E   R E N A M E   T O O L                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Rename a group of nodes using pattern substitution.
 * <p>
 * Only works if all selected nodes have never been checked-in.
 */
public 
class MultipleRenameTool
  extends CommonToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*  C O N S T R U C T O R                                                                 */
  /*----------------------------------------------------------------------------------------*/

  public 
  MultipleRenameTool()
  {
    super("MultipleRename", new VersionID("2.4.3"), "Temerity",
          "Renames a bunch of nodes that have never been checked-in.");

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);

    underDevelopment();
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*  P H A S E S                                                                           */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public synchronized String 
  collectPhaseInput() 
    throws PipelineException
  {
    {
      if(pSelected.size() == 0)
        throw new PipelineException
        ("The " + getName() + " tool requires at least one selected node!");

      NodeStatus status = pSelected.get(pSelected.keySet().toArray(new String[0])[0]);
      NodeID nodeID = status.getNodeID();
      
      pUser = nodeID.getAuthor();
      pView = nodeID.getView();
    }
    
    /* create the UI components */ 
    JScrollPane scroll = null;
    Box hbox = new Box(BoxLayout.X_AXIS);
    {
      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];
      
      pOldPattern = UIFactory.createTitledEditableTextField
        (tpanel, "Old Pattern:", sTSize, vpanel,
         "old", sVSize, 
         "The string to be replaced in the node names.");

      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

      pNewPattern = UIFactory.createTitledEditableTextField
        (tpanel, "New Pattern:", sTSize, vpanel,
         "new", sVSize, 
         "The string to be added in the node names.");
      
      hbox.add(comps[2]);
    }
    {
      scroll = new JScrollPane(hbox);

      scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

      scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

      Dimension size = new Dimension(sTSize + sVSize, 500);
      scroll.setMinimumSize(size);
    }
    
    JToolDialog diag = new JToolDialog("Multiple Rename", scroll, "Confirm");

    diag.setVisible(true);
    if(diag.wasConfirmed()) 
      return ": Renaming nodes";
    return null;
  }
  
  /**
   * Perform one phase in the execution of the tool. <P> 
   *    
   * @param mclient
   *   The network connection to the plmaster(1) daemon.
   * 
   * @param qclient
   *   The network connection to the plqueuemgr(1) daemon.
   * 
   * @return 
   *   Whether to continue and collect user input for the next phase of the tool.
   * 
   * @throws PipelineException 
   *   If unable to sucessfully execute this phase of the tool.
   */ 
  @Override
  public boolean 
  executePhase
  (
   MasterMgrClient mclient, 
   QueueMgrClient qclient
  )
    throws PipelineException
  {
    String oldP = pOldPattern.getText();
    String newP = pNewPattern.getText();
  
    TreeSet<String> bad = new TreeSet<String>(); 
    TreeMap<String, FilePattern> good = new TreeMap<String, FilePattern>();
    for (String node : pSelected.keySet()) {
      if (node.matches(oldP) || node.contains(oldP) ) {
        
        NodeStatus stat = mclient.status(pUser, pView, node, true, DownstreamMode.None);
        if (stat.getLightDetails().getBaseVersion() != null)
          bad.add(node);
        good.put(node, stat.getLightDetails().getWorkingVersion().
                       getPrimarySequence().getFilePattern() );
      }
    }
    if (bad.size() > 0)
        throw new PipelineException
          ("The nodes (" + bad +") cannot be renamed since it has been checked-in.  " +
           "Please deselect them before running this tool.");
    

    if (pRoots == null)
      pRoots = new TreeSet<String>();
    
    for (String node : good.keySet() ) {
      String newName = node.replaceAll(oldP, newP);
      FilePattern oldPat = good.get(node);
      FilePattern pat = new FilePattern(newName, oldPat.getPadding(), oldPat.getSuffix() );
      mclient.rename(pUser, pView, node, pat, true);
      pRoots.remove(node);
      pRoots.add(newName);
    }
    
    return false;
  }
  
  @Override
  public TreeSet<String> 
  rootsOnExit()
  {
    return pRoots;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final int sTSize = 150;
  private static final int sVSize = 300;
  
  private static final long serialVersionUID = -2158954294283299178L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private String pUser;
  private String pView;
  
  private JTextField pOldPattern;
  private JTextField pNewPattern;
}
