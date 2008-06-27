// $Id: JWorkingSelectDialog.java,v 1.3 2008/06/27 00:12:13 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   W O R K I N G   S E L E C T   D I A L O G                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the Node Browser/Viewer panels in which a remotely specified
 * node should be selected.
 * 
 * The dialog is shown when a "working --select=..." request is received from plremote(1)
 * to enable the user to choose which Node Browser/Viewer panel should have its node 
 * selection modified.
 */ 
public 
class JWorkingSelectDialog
  extends JBaseDialog
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent frame.
   */ 
  public 
  JWorkingSelectDialog
  (
   Frame owner
  ) 
  {
    super(owner, "Remote Select");

    /* initialize fields */ 
    {
      pPostUpdateSelected  = new TreeSet<String>();
      pNodeBrowserChannels = new TreeSet<Integer>();
      pNodeViewerChannels  = new TreeSet<Integer>();
    }

    /* create dialog body components */ 
    {
      Box vbox = new Box(BoxLayout.Y_AXIS);

      vbox.add(Box.createRigidArea(new Dimension(0, 4)));

      /* full node name */ 
      {
	Box hbox = new Box(BoxLayout.X_AXIS);
	
	hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	{
	  JTextField field = UIFactory.createTextField(null, 100, JLabel.LEFT);
	  pNodeNameField = field;
	  
	  hbox.add(field);
	}

	hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	vbox.add(hbox);
      }
	
      vbox.add(Box.createRigidArea(new Dimension(0, 4)));

      {
	JPanel panel = new JPanel();
	panel.setName("HorizontalBar");

	Dimension size = new Dimension(100, 7);       
	panel.setPreferredSize(size);
	panel.setMinimumSize(size);
	panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));
	
	vbox.add(panel);
      }

      /* fields */ 
      {
        Component comps[] = UIFactory.createTitledPanels();
        {
          JPanel tpanel = (JPanel) comps[0];
          JPanel vpanel = (JPanel) comps[1];
          
          {
            ArrayList<String> choices = new ArrayList<String>();
            choices.add("-");
            int wk;
            for(wk=1; wk<10; wk++) 
              choices.add(String.valueOf(wk)); 

            pChannelField = 
              UIFactory.createTitledCollectionField
              (tpanel, "Update Channel:", sTSize, 
               vpanel, choices, this, sVSize, 
               "The panel update channel for the Node Browser/Viewer panels.");

            pChannelField.addActionListener(this);
            pChannelField.setActionCommand("channel-changed"); 
          }
          
          UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
          
          pBrowserField = 
            UIFactory.createTitledTextField
            (tpanel, "Node Browser:", sTSize, 
             vpanel, "-", sVSize);

          UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
          
          pViewerField = 
            UIFactory.createTitledTextField
            (tpanel, "Node Viewer:", sTSize, 
             vpanel, "-", sVSize);
        }
	
        vbox.add(comps[2]);
      }
      
      String extra[][] = {
	{ "Replace", "replace-selection" },
	{ "Add",     "add-selection" }
      };

      JButton btns[] = 
        super.initUI("Remote Node Select:", vbox, null, null, extra, "Cancel");

      pReplaceButton = btns[0];
      pReplaceButton.setEnabled(false);
      pReplaceButton.setToolTipText(UIFactory.formatToolTip
        ("Replace the currently selected nodes with this node.")); 

      pAddButton = btns[1];
      pAddButton.setEnabled(false);
      pAddButton.setToolTipText(UIFactory.formatToolTip
        ("Add the node to the set of currently selected nodes.")); 

      pack();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the panel.
   * 
   * @param channel 
   *   The default update channel index.
   *
   * @param name
   *   The fully resolved node name.
   * 
   * @param postUpdateSelected
   *   The names of the nodes which should be selected in the NodeViewer after an update.
   * 
   * @param browserChannels
   *   The update channel indices for all existing Node Browser panels.
   * 
   * @param viewerChannels
   *   The update channel indices for all existing Node Viewer panels.
   */ 
  public void 
  updateSelection
  (
   int channel, 
   String name,
   TreeSet<String> postUpdateSelected,
   TreeSet<Integer> browserChannels, 
   TreeSet<Integer> viewerChannels
  )
  {
    pNodeNameField.setText(name); 

    pPostUpdateSelected.clear();
    pPostUpdateSelected.addAll(postUpdateSelected);

    pNodeBrowserChannels.clear();
    pNodeBrowserChannels.addAll(browserChannels);

    pNodeViewerChannels.clear();
    pNodeViewerChannels.addAll(viewerChannels);

    pChannelField.setSelectedIndex(channel);
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
    if(cmd.equals("channel-changed"))
      doChannelChanged();
    else if(cmd.equals("add-selection")) 
      doAddSelection();
    else if(cmd.equals("replace-selection")) 
      doReplaceSelection();
    else
      super.actionPerformed(e);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the Node Browser/Viewer panel fields in response to a change in the selected 
   * update channel.
   */ 
  private void 
  doChannelChanged()
  {
    int idx = pChannelField.getSelectedIndex();
    switch(idx) {
    case -1:
    case 0:
      pBrowserField.setText("-");
      pViewerField.setText("-");
      pReplaceButton.setEnabled(false);
      pAddButton.setEnabled(false);
      break; 

    default:
      {
        boolean hasBrowser = pNodeBrowserChannels.contains(idx);
        boolean hasViewer  = pNodeViewerChannels.contains(idx);

        if(!hasBrowser && !hasViewer) {
          pBrowserField.setText("Create"); 
          pViewerField.setText("Create"); 
          pReplaceButton.setEnabled(true);
          pAddButton.setEnabled(false);
        }
        else if(hasBrowser && hasViewer) {
          pBrowserField.setText("Found"); 
          pViewerField.setText("Found"); 
          pReplaceButton.setEnabled(true);
          pAddButton.setEnabled(true);
        }
        else {
          pBrowserField.setText(hasBrowser ? "Found" : "Missing");
          pViewerField.setText(hasViewer ? "Found" : "Missing");
          pReplaceButton.setEnabled(false);
          pAddButton.setEnabled(false);
        }
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Performs the update the of Node Browser/Viewer panels to display a node network 
   * rooted at the given root by adding it to the current selection.
   */ 
  private void 
  doAddSelection() 
  {
    setVisible(false);
    
    UIMaster.getInstance().remoteAddSelection
      (pChannelField.getSelectedIndex(), pNodeNameField.getText(), pPostUpdateSelected);
  }

  /**
   * Performs the update the of Node Browser/Viewer panels to display a node network 
   * rooted at the given root by replacing the current selection.
   */ 
  private void 
  doReplaceSelection() 
  {
    setVisible(false);

    UIMaster.getInstance().remoteReplaceSelection
      (pChannelField.getSelectedIndex(), pNodeNameField.getText(), pPostUpdateSelected);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -877870299228988458L;
  
  private static final int sTSize = 150;
  private static final int sVSize = 200;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The existing Node Browser/Viewer panels.
   */ 
  private TreeSet<Integer>  pNodeBrowserChannels; 
  private TreeSet<Integer>  pNodeViewerChannels; 

  /**
   * The names of the nodes which should be selected in the NodeViewer panel after an update.
   */ 
  private TreeSet<String>  pPostUpdateSelected; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node name field.
   */ 
  private JTextField pNodeNameField;

  /**
   * The update channel field.
   */ 
  private JCollectionField  pChannelField;
  
  /**
   * The Node Browser/Viewer panel status fields.
   */ 
  private JTextField  pBrowserField;
  private JTextField  pViewerField;

  /**
   * The footer buttons.
   */ 
  private JButton  pReplaceButton; 
  private JButton  pAddButton; 

}
