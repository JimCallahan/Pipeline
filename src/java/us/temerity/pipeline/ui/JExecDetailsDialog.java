// $Id: JExecDetailsDialog.java,v 1.1 2004/09/05 06:54:56 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*; 

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   E X E C   D E T A I L S   D I A L O G                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays the command-line, working directory and environment used to execute a job.
 */ 
public  
class JExecDetailsDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   */ 
  public 
  JExecDetailsDialog()
  {
    super("Execution Details", false);

    /* create dialog body components */ 
    {
      JPanel body = new JPanel();

      body.setName("MainDialogPanel");
      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

      /* command line */ 
      {
	body.add(UIMaster.createPanelLabel("Command Line:"));

	{
	  JTextArea area = new JTextArea(null, 4, 70);
	  pCommandLineArea = area; 

	  area.setName("TextArea");
	  area.setLineWrap(true);
	  area.setWrapStyleWord(true);
	  area.setEditable(false);
	}

	{
	  JScrollPane scroll = new JScrollPane(pCommandLineArea);
	  
	  scroll.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	  scroll.setVerticalScrollBarPolicy
	    (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	  
	  Dimension size = scroll.getPreferredSize();
	  scroll.setMinimumSize(new Dimension(100, size.height));
	  scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, size.height));

	  body.add(scroll);
	}
      }

      body.add(Box.createRigidArea(new Dimension(0, 20)));

      /* working directory */ 
      {
	body.add(UIMaster.createPanelLabel("Working Directory:"));

	body.add(Box.createRigidArea(new Dimension(0, 4)));
      
	JTextField field = UIMaster.createTextField(null, 100, JLabel.LEFT);
	pWorkingDirField = field;

	body.add(field);
      }

      body.add(Box.createRigidArea(new Dimension(0, 20)));

      /* environment */ 
      {      
	body.add(UIMaster.createPanelLabel("Environment:"));
	
	body.add(Box.createRigidArea(new Dimension(0, 4)));

	Component comps[] = UIMaster.createTitledPanels();
	{
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];
	
	  tpanel.add(Box.createRigidArea(new Dimension(sTSize, 0)));
	  vpanel.add(Box.createHorizontalGlue());
	}

	{
	  JScrollPane scroll = new JScrollPane(comps[2]);
	  pEnvScroll = scroll;
	  
	  scroll.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	  scroll.setVerticalScrollBarPolicy
	    (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	
	  scroll.setMinimumSize(new Dimension(100, 50));
	  scroll.setPreferredSize(new Dimension(100, 300));
  
	  body.add(scroll);
	}
      }
         
      super.initUI("X", false, body, null, null, null, "Close");
    }
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the dialog contents.
   * 
   * @param header
   *   The dialog header text.
   * 
   * @param command
   *   The OS level process command line arguments or <CODE>null</CODE> if not known.
   * 
   * @param dir
   *   The working directory of the OS level process.   
   * 
   * @param env
   *   The environment under which the OS level process is run.  
   */ 
  public void 
  updateContents
  (
   String header, 
   String command,
   String dir, 
   SortedMap<String,String> env
  ) 
  {
    pHeaderLabel.setText(header);
      
    pCommandLineArea.setText(command);
      
    pWorkingDirField.setText(dir);
    
    {
      Component comps[] = UIMaster.createTitledPanels();
      {
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];
	
	if(!env.isEmpty()) {
	  String last = env.lastKey();
	  for(String key : env.keySet()) {
	    String value = env.get(key);
	      
	    JTextField field = 
	      UIMaster.createTitledTextField(tpanel, key + ":", sTSize, 
					     vpanel, value, sVSize);
	    field.setHorizontalAlignment(JLabel.LEFT);
	    
	    if(!key.equals(last))
	      UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	  }
	}
	else {
	  tpanel.add(Box.createRigidArea(new Dimension(sTSize, 0)));
	  vpanel.add(Box.createHorizontalGlue());
	}
      }

      pEnvScroll.setViewportView(comps[2]);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1000045463020428620L;

  private static final int  sTSize = 200;
  private static final int  sVSize = 400;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The command line text area.
   */ 
  private JTextArea pCommandLineArea; 
  
  /**
   * The working directory field.
   */ 
  private JTextField pWorkingDirField; 

  /**
   * The environment scroll pane. 
   */ 
  private JScrollPane  pEnvScroll; 

}
