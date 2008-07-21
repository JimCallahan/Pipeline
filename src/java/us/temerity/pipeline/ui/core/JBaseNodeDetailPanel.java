// $Id: JBaseNodeDetailPanel.java,v 1.1 2008/07/21 17:31:10 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.glue.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   N O D E   D E T A I L   P A N E L                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Common base class for all panels which display detailed node status information. 
 */ 
public  
class JBaseNodeDetailPanel
  extends JTopLevelPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  protected 
  JBaseNodeDetailPanel() 
  {
    super();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  protected
  JBaseNodeDetailPanel
  (
   JTopLevelPanel panel
  )
  {
    super(panel);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the panel header user interface components.
   * 
   * @param panel
   *   The parent panel which will contain the header components.
   */ 
  protected void
  initHeader
  (
   JPanel panel
  )
  {
    panel.setName("DialogHeader");	
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    
    {
      pHeaderIcon = new JLabel();
      panel.add(pHeaderIcon);	  
    }
    
    panel.add(Box.createRigidArea(new Dimension(3, 0)));
    
    {
      pHeaderLabel = new JLabel("X");
      pHeaderLabel.setName("DialogHeaderLabel");	       
      panel.add(pHeaderLabel);	  
    }
  }

  /**
   * Initialize the full node name user interface components.
   * 
   * @param panel
   *   The parent panel which will contain the name components.
   */ 
  protected void
  initNameField
  (
   JPanel panel
  )
  {
    Box hbox = new Box(BoxLayout.X_AXIS);
	
    hbox.add(Box.createRigidArea(new Dimension(4, 0)));
    
    {
      pNodeNameField = UIFactory.createTextField(null, 100, JLabel.LEFT);
      hbox.add(pNodeNameField);
    }
    
    hbox.add(Box.createRigidArea(new Dimension(4, 0)));
    
    panel.add(hbox);
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components to reflect the current node status.
   * 
   * @param status
   *   The current node status.
   */
  protected synchronized void 
  updateNodeStatus
  (
   NodeStatus status
  ) 
  {
    updatePrivileges();

    pStatus = status;

    /* header */ 
    {
      String iname = "Blank-Normal"; 
      if(pStatus != null) {	
        if(pStatus.hasHeavyDetails()) {
          NodeDetailsHeavy details = pStatus.getHeavyDetails();
          if(details.getOverallNodeState() == OverallNodeState.NeedsCheckOut) {
            VersionID wvid = details.getWorkingVersion().getWorkingID();
            VersionID lvid = details.getLatestVersion().getVersionID();
            switch(wvid.compareLevel(lvid)) {
            case Major:
              iname = ("NeedsCheckOutMajor-" + details.getOverallQueueState());
              break;
              
            case Minor:
              iname = ("NeedsCheckOut-" + details.getOverallQueueState());
              break;
              
            case Micro:
              iname = ("NeedsCheckOutMicro-" + details.getOverallQueueState());
            }
          }
          else {
            iname = (details.getOverallNodeState() + "-" + details.getOverallQueueState());
          }
          
          NodeMod mod = details.getWorkingVersion();
          if((mod != null) && mod.isFrozen()) 
            iname = (iname + "-Frozen-Normal");
          else 
            iname = (iname + "-Normal");
        }
        else if(pStatus.hasLightDetails()) {
          NodeDetailsLight details = pStatus.getLightDetails();
          switch(details.getVersionState()) {
          case CheckedIn:
            iname = "CheckedIn-Undefined-Normal"; 
            break;
            
          default:
            iname = "Lightweight-Normal";
          }
        }

        pHeaderLabel.setText(pStatus.toString());
        pNodeNameField.setText(pStatus.getName());
      }
      else {
        pHeaderLabel.setText(null);
        pNodeNameField.setText(null);
      }
      
      try {
        pHeaderIcon.setIcon(TextureMgr.getInstance().getIcon32(iname));
      }
      catch(PipelineException ex) {
        pHeaderIcon.setIcon(null); 
        UIMaster.getInstance().showErrorDialog(ex);
      }
    }
  }

   

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -1027548213455199580L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The current node status.
   */ 
  protected NodeStatus  pStatus;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The node name/state header.
   */ 
  protected JLabel  pHeaderIcon;
  protected JLabel  pHeaderLabel;
  
  /**
   * The fully resolved node name field.
   */ 
  protected JTextField  pNodeNameField;

}
