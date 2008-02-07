// $Id: MultipleLinkTool.java,v 1.1 2008/02/07 10:17:54 jesse Exp $

package us.temerity.pipeline.plugin.MultipleLinkTool.v2_3_15;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.JPanel;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   M U L T I P L E   L I N K   T O O L                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Links one source node to multiple target nodes.
 * <p>
 * Select the target nodes and then execute the tool on the source node.
 */
public 
class MultipleLinkTool
  extends BaseTool
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  MultipleLinkTool()
  {
    super("MultipleLink", new VersionID("2.3.15"), "Temerity",
          "Links one source node to multiple target nodes.");

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  O P S                                                                                 */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public synchronized String 
  collectPhaseInput()
    throws PipelineException
  {
    if (pPrimary == null)
      throw new PipelineException
        ("A primary node must be selected.  This is the node that will be the source node");
    
    if (pSelected.size() < 1)
      throw new PipelineException
        ("At least one target node must be selected before this tool is " +
         "run on the source node.");
    
    Box body = null;
    {
      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];
      body = (Box) comps[2];
      
      {
        JCollectionField field = 
          UIFactory.createTitledCollectionField(tpanel, "Link Policy:", sTSize, 
                                                vpanel, LinkPolicy.titles(), sVSize);
        pPolicyField = field;

        field.addActionListener(this);
        field.setActionCommand("policy-changed");
      }
      
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      
      {
        ArrayList<String> values = new ArrayList<String>();
        values.add(LinkRelationship.All.toTitle());
        values.add(LinkRelationship.OneToOne.toTitle());

        JCollectionField field = 
          UIFactory.createTitledCollectionField(tpanel, "Link Relationship:", sTSize, 
                                                vpanel, values, sVSize);
        pRelationshipField = field;

        field.addActionListener(this);
        field.setActionCommand("relationship-changed");
      }
      
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

      pOffsetField = 
        UIFactory.createTitledIntegerField(tpanel, "Frame Offset:", sTSize, 
                                          vpanel, null, sVSize);

      UIFactory.addVerticalGlue(tpanel, vpanel);
    }
    
    JToolDialog dialog = new JToolDialog("Multiple Link", body, "Confirm");
    dialog.setVisible(true);
    if (!dialog.wasConfirmed())
      return null;
    
    return ": Making Links";
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
    TreeSet<String> targets = new TreeSet<String>(pSelected.keySet());
    targets.remove(pPrimary);
    
    LinkPolicy policy = getPolicy();
    LinkRelationship relationship = getRelationship();
    Integer offset = getFrameOffset();
    
    for (String target : targets) 
      mclient.link(getAuthor(), getView(), target, pPrimary, policy, relationship, offset);
    
    return false;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the link category.
   */
  public LinkPolicy
  getPolicy() 
  {
    return LinkPolicy.values()[pPolicyField.getSelectedIndex()];
  }
    
  /**
   * Get the link relationship
   */ 
  public LinkRelationship
  getRelationship() 
  {
    if(pRelationshipField.getSelected().equals("None"))
      return LinkRelationship.None; 
    else if(pRelationshipField.getSelected().equals("1:1")) 
      return LinkRelationship.OneToOne;
    else if(pRelationshipField.getSelected().equals("All")) 
      return LinkRelationship.All;

    assert(false);
    return null;
  }

  /**
   * Get the frame offset.
   */ 
  public Integer
  getFrameOffset() 
  {
    return pOffsetField.getValue();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    String cmd = e.getActionCommand();
    if(cmd.equals("policy-changed")) 
      doPolicyChanged();
    else if(cmd.equals("relationship-changed")) 
      doRelationshipChanged();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The link policy has been changed.
   */ 
  public void 
  doPolicyChanged()
  {
    LinkRelationship rel = getRelationship();
    
    pRelationshipField.removeActionListener(this);
    {
      ArrayList<String> values = new ArrayList<String>();
      switch(getPolicy()) {
      case Association:
         rel = LinkRelationship.None;
         values.add(LinkRelationship.None.toTitle());
         pRelationshipField.setEnabled(false);
         break;

      default:
        if(rel == LinkRelationship.None) 
          rel = LinkRelationship.All;
        values.add(LinkRelationship.All.toTitle());
        values.add(LinkRelationship.OneToOne.toTitle());
        pRelationshipField.setEnabled(true);
      }

      pRelationshipField.setValues(values);
    }
    pRelationshipField.addActionListener(this);
    
    pRelationshipField.setSelected(rel.toTitle());
  }

  /**
   * The link relationship has been changed.
   */ 
  public void 
  doRelationshipChanged()
  {
    switch(getRelationship()) {
    case OneToOne:
      {
        pOffsetField.setEnabled(true);
        Integer offset = pOffsetField.getValue();
        if(offset == null) 
          pOffsetField.setValue(0);
      }
      break;

    default:
      pOffsetField.setValue(null);
      pOffsetField.setEnabled(false);
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7072709886305699093L;

  private static final int sTSize = 120;
  private static final int sVSize = 180;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The link policy field.
   */
  private JCollectionField  pPolicyField; 

  /**
   * The link relationship field.
   */
  private JCollectionField  pRelationshipField;
   
  /**
   * The frame offset field.
   */
  private JIntegerField  pOffsetField;
}
