package us.temerity.pipeline.plugin.PatternCloneTool.v2_3_1;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   P A T T E R N   C L O N E   T O O L                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Clones a group of nodes, using pattern replacement to change their names.
 * <p>
 * It is important to note that this tool is not performing any modification to links, either
 * in Pipeline or inside files.  It cannot be used to create a clone of an existing network.
 * Instead it is designed to allow the copying of a group nodes that are not dependent on 
 * each other, but that share a similar naming convention.
 */
public 
class PatternCloneTool 
  extends BaseTool
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  PatternCloneTool()
  {
    super("PatternClone", new VersionID("2.3.1"), "Temerity",
	  "Clones a group of nodes, using pattern replacement to change their names.");
    
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*  O P S                                                                                 */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Create and show graphical user interface components to collect information from the 
   * user to use as input in the next phase of execution for the tool. <P> 
   * 
   * @return 
   *   The phase progress message or <CODE>null</CODE> to abort early.
   * 
   * @throws PipelineException 
   *   If unable to validate the given user input.
   */  
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
    {
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
	               "new ", sVSize, 
        	       "The string to be added in the node names.");
        
        UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
        
        pCloneActions = UIFactory.createTitledBooleanField
        		(tpanel, "Clone Actions:", sTSize, 
        		 vpanel, sVSize, 
        		 "Should the actions of each node be cloned.");
        pCloneActions.setValue(true);
        
        UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
        
        pCloneLinks = UIFactory.createTitledBooleanField
		      (tpanel, "Clone Links:", sTSize, 
		      vpanel, sVSize, 
		      "Should the links of each node be cloned.");
        pCloneLinks.setValue(true);
        
        UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
        
        pCopyFiles = UIFactory.createTitledBooleanField
		      (tpanel, "Copy Files:", sTSize, 
		      vpanel, sVSize, 
		      "Should the primary files of each node be copied.");
        pCopyFiles.setValue(false);
        
        UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
        
        pCopyAnnotations = UIFactory.createTitledBooleanField
                           (tpanel, "Copy Annotations:", sTSize, 
                            vpanel, sVSize, 
                            "Should the node annotations on each node be copied.");
        pCopyAnnotations.setValue(false);

        
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
    }
    
    JToolDialog diag = new JToolDialog("Pattern Clone Tool", scroll, "Confirm");

    diag.setVisible(true);
    if(diag.wasConfirmed()) 
      return ": Cloning Nodes";

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
  public boolean 
  executePhase
  (
   MasterMgrClient mclient, 
   QueueMgrClient qclient
  )
    throws PipelineException
  {
    boolean cloneAction = pCloneActions.getValue();
    boolean cloneLinks = pCloneLinks.getValue();
    boolean copyFiles = pCopyFiles.getValue();
    boolean copyAnnotations = pCopyAnnotations.getValue();
    
    String oldP = pOldPattern.getText();
    String newP = pNewPattern.getText();
    
    for (String node : pSelected.keySet()) {
      if (node.matches(oldP) || node.contains(oldP) ) {
	String newName = node.replaceAll(oldP, newP);
	NodeStatus stat = pSelected.get(node);
	NodeMod mod = stat.getDetails().getWorkingVersion();
	if (mod.getAction() == null)
	  cloneAction = false;
	System.out.println(pUser + "\t" + pView + "\t" + node + "\t" + newName);
	mclient.clone(pUser, pView, node, newName, cloneAction, cloneLinks, copyFiles, copyAnnotations);
      }
    }
    return false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -8453756469914500074L;
  
  private static final int sTSize = 150;
  private static final int sVSize = 300;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private String pUser;
  private String pView;
  
  private JTextField pOldPattern;
  private JTextField pNewPattern;
  
  private JBooleanField pCloneActions;
  private JBooleanField pCloneLinks;
  private JBooleanField pCopyFiles;
  private JBooleanField pCopyAnnotations;

}
