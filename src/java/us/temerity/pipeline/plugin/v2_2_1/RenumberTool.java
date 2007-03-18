// $Id: RenumberTool.java,v 1.1 2007/03/18 02:43:56 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import java.awt.Component;
import java.awt.Dimension;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   R E N U M B E R   T O O L                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Renumbers the frame range of a target node and all nodes connected upstream
 * to the target node through links with a OneToOne relationship.
 * <P>
 * 
 * This tool operates identically to the built-in Renumber operation except that
 * it follows upstream links to also renumber all connected upstream nodes.
 */
public class 
RenumberTool 
  extends BaseTool
{
  /*----------------------------------------------------------------------------------------*/
  /*  C O N S T R U C T O R                                                                 */
  /*----------------------------------------------------------------------------------------*/

  public 
  RenumberTool()
  {
    super("Renumber", new VersionID("2.2.1"), "Temerity",
	  "Renumbers the frame range of a target node and all nodes connected upstream " + 
	  "to the target node through links with a OneToOne relationship.");

    pPotentialNames = new TreeSet<String>(); 

    addPhase(new PhaseOne());
    addPhase(new PhaseTwo());

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);

    underDevelopment();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   P H A S E   O N E                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private 
  class PhaseOne
    extends BaseTool.ToolPhase
  {
    public 
    PhaseOne() 
    {
      super();
    }
    
    /**
     * Specify the renumbering parameters of the target node as per the built-in
     * Renumber operation.
     * 
     * @return 
     *   The phase progress message or <CODE>null</CODE> to abort early.
     * 
     * @throws PipelineException
     *   If unable to validate the given user input.
     */
    public String
    collectInput() 
      throws PipelineException 
    {
      /* create dialog components */
      JComponent body = null;
      JPathField targetNodeField;
      JIntegerField startFrameField;
      JIntegerField endFrameField;
      JIntegerField byFrameField;
      JBooleanField removeFilesField;
      {
        Component comps[] = UIFactory.createTitledPanels();
        JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
        body = (JComponent) comps[2];

        {
          targetNodeField = 
            UIFactory.createTitledPathField
	    (tpanel, "Target Node:", sTSize, 
	     vpanel, new Path("/"), sVSize,
	     "The root of the tree of nodes being renumbered.");
        }
      
        UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

        {
          tpanel.add(UIFactory.createFixedLabel("Frame Range:", sTSize, JLabel.RIGHT));
	
          {
            Box hbox = new Box(BoxLayout.X_AXIS);
	  
            startFrameField = UIFactory.createIntegerField(null, sVSize1, JLabel.CENTER);
            hbox.add(startFrameField);
	  
            hbox.add(Box.createHorizontalGlue());
            hbox.add(Box.createRigidArea(new Dimension(8, 0)));
	  
            hbox.add(new JLabel("to")); 
	  
            hbox.add(Box.createRigidArea(new Dimension(8, 0)));
            hbox.add(Box.createHorizontalGlue());
	  
            endFrameField = UIFactory.createIntegerField(null, sVSize1, JLabel.CENTER);
            hbox.add(endFrameField);
	  
            hbox.add(Box.createHorizontalGlue());
            hbox.add(Box.createRigidArea(new Dimension(8, 0)));
	  
            hbox.add(new JLabel("by"));
	  
            hbox.add(Box.createRigidArea(new Dimension(8, 0)));
            hbox.add(Box.createHorizontalGlue());
	  
            byFrameField = UIFactory.createIntegerField(null, sVSize1, JLabel.CENTER);
            hbox.add(byFrameField); 
	  
            Dimension size = new Dimension(sVSize+1, 19);
            hbox.setMinimumSize(size);
            hbox.setMaximumSize(size);
            hbox.setPreferredSize(size);
	  
            vpanel.add(hbox);
          }
        }
      
        UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

        removeFilesField = 
          UIFactory.createTitledBooleanField(tpanel, "Remove Obsolete Files:", sTSize, 
                                             vpanel, sVSize);

        UIFactory.addVerticalGlue(tpanel, vpanel);
      }


      /* validate node selections and initialize dialog components */
      {
        if(pPrimary == null)
          throw new PipelineException("The primary selection must be the Target Node!");

        targetNodeField.setText(pPrimary);

        {
          NodeStatus status = pSelected.get(pPrimary);
          NodeID nodeID = status.getNodeID();
          pAuthor = nodeID.getAuthor();
          pView = nodeID.getView();

          NodeDetails details = status.getDetails();
          if(details == null)
            throw new PipelineException
              ("The target node must have an existing status in order to be renumbered!");

          NodeMod mod = details.getWorkingVersion();
          if(mod == null)
            throw new PipelineException
              ("The target node must be checked-out in order to be renumbered!");

          {
            FileSeq fseq = mod.getPrimarySequence();
            if(!fseq.hasFrameNumbers())
              throw new PipelineException
                ("The Target Node (" + pPrimary + ") must have frame numbers to be " + 
                 "renumbered!");
            pOrigFrameRange = fseq.getFrameRange();
            if(pOrigFrameRange == null)
              throw new PipelineException
                ("The target node must have frame numbers in order to be renumbered!");
          }

          startFrameField.setValue(pOrigFrameRange.getStart());
          endFrameField.setValue(pOrigFrameRange.getEnd());
          byFrameField.setValue(pOrigFrameRange.getBy());

          byFrameField.setEnabled(mod.hasIdenticalFrameRanges());

          removeFilesField.setValue(false);
        }

        if(pSelected.size() != 1)
          throw new PipelineException
            ("Only one Target Node may be selected to be renumbered.");
      }

      /* query the user */
      JToolDialog diag = new JToolDialog("Renumber:", body, "Continue");
      diag.setVisible(true);
      if(!diag.wasConfirmed())
        return null;

      pTargetNode = targetNodeField.getText();
      if((pTargetNode == null) || (pTargetNode.length() == 0))
        throw new PipelineException("Illegal Target Node name!");
      
      /* collect new frame range info */
      {
        {
          Integer frame = startFrameField.getValue();
          if(frame == null)
            throw new PipelineException
              ("Unable to renumber node with an unspecified start frame!");
          pStartFrame = frame;
        }
	
        {
          Integer frame = endFrameField.getValue();
          if(frame == null)
            throw new PipelineException
              ("Unable to renumber node with an unspecified end frame!");
          pEndFrame = frame;
        }
	
        if(pStartFrame > pEndFrame) {
          Integer tmp = pEndFrame;
          pEndFrame = pStartFrame;
          pStartFrame = tmp;
	  
          startFrameField.setValue(pStartFrame);
          endFrameField.setValue(pEndFrame);
        }
	
        {
          Integer frame = byFrameField.getValue();
          if(frame == null)
            throw new PipelineException
              ("Unable to renumber node with an unspecified frame increment!");
          pByFrame = frame;
        }
	
        pIsByEnabled = byFrameField.isEnabled();
	
        {
          Boolean tf = removeFilesField.getValue();
          pRemoveFiles = (tf != null) && tf;
        }
      }
      
      return ": Collecting Upstream Node Information...";
    }
    
    /**
     * Determine the nodes to be renumbered and collect any neeeded state information.
     * 
     * @param mclient
     *   The network connection to the plmaster(1) daemon.
     * 
     * @param qclient
     *   The network connection to the plqueuemgr(1) daemon.
     * 
     * @return 
     *    Whether to continue and collect user input for the next phase of the tool.
     * 
     * @throws PipelineException
     *    If unable to sucessfully execute this phase of the tool.
     */
    public boolean
    execute
    (
     MasterMgrClient mclient,
     QueueMgrClient qclient
    ) 
      throws PipelineException
    {
      /* get the current status of the target node */
      NodeID nodeID = new NodeID(pAuthor, pView, pTargetNode);
      NodeStatus status = mclient.status(nodeID);
      if(status.getDetails().getWorkingVersion() == null)
        throw new PipelineException
          ("No working version of the Target Node ("+ pTargetNode + ") exists " + 
           "in the (" + pView + ") working area owned by (" + pAuthor + ")!");

      /* validate the new frame range */
      {
        pTargetFrameRange = null;
        
        if(!pIsByEnabled
           && ((((pOrigFrameRange.getStart() - pStartFrame) % pByFrame) != 0) || 
               (((pOrigFrameRange.getStart() - pEndFrame) % pByFrame) != 0)))
          throw new PipelineException
            ("Unable to renumber node due to misalignment of the new frame range ("
             + pStartFrame + "-" + pEndFrame + "x" + pByFrame + ") with the original " + 
             "frame range (" + pOrigFrameRange + ")!");
        
        try {
          pTargetFrameRange = new FrameRange(pStartFrame, pEndFrame, pByFrame);
        } 
        catch (IllegalArgumentException ex) {
          throw new PipelineException("Unable to renumber node. " + ex.getMessage());
        }
        
        if(pOrigFrameRange.equals(pTargetFrameRange))
          throw new PipelineException
            ("Unecessary to renumber node when the frame range (" +
             pOrigFrameRange + " " + "has not been altered!");
      }

      /*
       * find all upstream nodes with the same number of frames connected via OneToOne link
       */
      pPotentialNames.clear();
      findNodesToRenumber(status);

      return true;
    }
    
    /**
     * Recursively search the upstream links for potential nodes to renumber.
     */
    private void 
    findNodesToRenumber
    (
     NodeStatus status
    )
    {
      NodeDetails details = status.getDetails();
      if(details == null)
        return;
      
      NodeMod mod = details.getWorkingVersion();
      if(mod == null)
        return;

      if(mod.getPrimarySequence().getFrameRange().equals(pOrigFrameRange)) {
        pPotentialNames.add(status.getName());
        
        for (NodeStatus lstatus : status.getSources()) {
          LinkMod link = mod.getSource(lstatus.getName());
          if(link.getRelationship() == LinkRelationship.OneToOne)
            findNodesToRenumber(lstatus);
        }
      }
    }

    /*--------------------------------------------------------------------------------------*/
    /*  I N T E R N A L S                                                                   */
    /*--------------------------------------------------------------------------------------*/

    /**
     * The root node being renumbered;
     */
    private String pTargetNode;
    
    /**
     * The original frame range of the primary sequence of the target node.
     */ 
    private FrameRange pOrigFrameRange;

    /**
     * New frame range information.
     */ 
    private int pStartFrame;
    private int pEndFrame;
    private int pByFrame;
    private boolean pIsByEnabled;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   P H A S E   T W O                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private 
  class PhaseTwo
    extends BaseTool.ToolPhase
  {
    public 
    PhaseTwo() 
    {
      super();
      pRenumberNames = new TreeSet<String>();
    }
    
    /**
     * Confirm the changes to be made to the nodes.
     * 
     * @return 
     *   The phase progress message or <CODE>null</CODE> to abort early.
     * 
     * @throws PipelineException
     *   If unable to validate the given user input.
     */
    public String
    collectInput() 
      throws PipelineException 
    {
      pRenumberNames.clear();
      pRenumberNames.addAll(pPotentialNames);

      /* create dialog components */
      TreeMap<String, JBooleanField> renumberFields = new TreeMap<String, JBooleanField>();
      JScrollPane scroll = null;
      {
        Box ibox = new Box(BoxLayout.Y_AXIS);

        if(pPotentialNames.isEmpty()) {
          Component comps[] = UIFactory.createTitledPanels();
          JPanel tpanel = (JPanel) comps[0];
          JPanel vpanel = (JPanel) comps[1];
	
          tpanel.add(Box.createRigidArea(new Dimension(sTSize - 7, 0)));
          vpanel.add(Box.createHorizontalGlue());
	
          ibox.add(comps[2]);
        } 
        else {
          for (String name : pPotentialNames) {
            Component comps[] = UIFactory.createTitledPanels();
            JPanel tpanel = (JPanel) comps[0];
            JPanel vpanel = (JPanel) comps[1];
	  
            JBooleanField field = 
              UIFactory.createTitledBooleanField(tpanel, "Renumber Node:", sTSize - 7, 
                                                 vpanel, sVSize2,
                                                 "Whether to renumber the given node.");
            field.setValue(true);
            renumberFields.put(name, field);
	  
            UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

            JDrawer drawer = new JDrawer("Node: " + name, (JComponent) comps[2], true);
            ibox.add(drawer);
          }
        }
      
        {
          JPanel spanel = new JPanel();
          spanel.setName("Spacer");

          spanel.setMinimumSize(new Dimension(sTSize + sVSize2, 7));
          spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
          spanel.setPreferredSize(new Dimension(sTSize + sVSize2, 7));

          ibox.add(spanel);
        }

        {
          scroll = new JScrollPane(ibox);

          scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
          scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

          Dimension size = new Dimension(sTSize + sVSize2 + 52, 500);
          scroll.setMinimumSize(size);
          scroll.setPreferredSize(size);

          scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
        }
      }

      /* query the user */
      JToolDialog diag = new JToolDialog("Renumber: Frame Range Changes", scroll, "Confirm");
      diag.setVisible(true);
      if(!diag.wasConfirmed()) 
        return null;

      for(String name : renumberFields.keySet()) {
        JBooleanField field = renumberFields.get(name);
        Boolean value = field.getValue();
        if((value == null) || !value)
          pRenumberNames.remove(name);
      }
      
      return ": Modifying Nodes...";
    }

    /**
     * Renumber the nodes.
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
    execute
    (
     MasterMgrClient mclient,
     QueueMgrClient qclient
    ) 
      throws PipelineException
    {
      for(String name : pRenumberNames)
        mclient.renumber(pAuthor, pView, name, pTargetFrameRange, pRemoveFiles);
      return false;
    }


    /*--------------------------------------------------------------------------------------*/
    /*  I N T E R N A L S                                                                   */
    /*--------------------------------------------------------------------------------------*/

    /**
     * The names of the nodes approved for renumbering.
     */
    private TreeSet<String> pRenumberNames;

  }



  /*----------------------------------------------------------------------------------------*/
  /*  S T A T I C   I N T E R N A L S                                                       */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 817359796226084513L;

  private static final int sTSize  = 150;
  private static final int sVSize  = 300;
  private static final int sVSize1 = 60;
  private static final int sVSize2 = 250; 



  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The current working area author|view.
   */
  private String pAuthor;
  private String pView;

  /**
   * The names of the nodes which may potentially be renumbered.
   */
  private TreeSet<String> pPotentialNames;

  /**
   * New frame range.
   */
  private FrameRange pTargetFrameRange;

  /**
   * Whether to remove obsolete files.
   */ 
  private boolean pRemoveFiles;

}
