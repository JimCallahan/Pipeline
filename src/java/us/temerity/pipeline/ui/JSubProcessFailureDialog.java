// $Id: JSubProcessFailureDialog.java,v 1.1 2004/06/28 00:22:06 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.toolset.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   S U B P R O C E S S   F A I L U R E   D I A L O G                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The dialog giving details of the failure of a subprocess.
 */ 
public 
class JSubProcessFailureDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JSubProcessFailureDialog()
  {
    super("Subprocess Failure", true);

    /* create dialog body components */ 
    {
      JPanel cpanel = new JPanel();
      {
	cpanel.setName("MainDialogPanel");
	cpanel.setLayout(new BoxLayout(cpanel, BoxLayout.Y_AXIS));

	cpanel.add(UIMaster.createPanelLabel("Command:"));
	
	cpanel.add(Box.createRigidArea(new Dimension(0, 4)));

	{
	  JTextArea area = new JTextArea(null, 2, 80);
	  pCommandArea = area;
	  area.setName("CodeTextArea");
	  area.setLineWrap(true);
	  area.setWrapStyleWord(true);
	  area.setEditable(false);
	}

	{
	  JScrollPane scroll = new JScrollPane(pCommandArea);
	  
	  scroll.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	  scroll.setVerticalScrollBarPolicy
	    (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	  
	  scroll.setMinimumSize(new Dimension(100, 50));

	  cpanel.add(scroll);
	}	
      }

      JSplitPane split = null;
      {
	JPanel opanel = new JPanel();
	{
	  opanel.setName("MainDialogPanel");
	  opanel.setLayout(new BoxLayout(opanel, BoxLayout.Y_AXIS));

	  opanel.add(UIMaster.createPanelLabel("Editor Output:"));
	  
	  opanel.add(Box.createRigidArea(new Dimension(0, 4)));
	  
	  {
	    JTextArea area = new JTextArea(null, 10, 80);
	    pStdOutArea = area;
	    area.setName("CodeTextArea");
	    area.setLineWrap(true);
	    area.setWrapStyleWord(true);
	    area.setEditable(false);
	  }
	  
	  {
	    JScrollPane scroll = new JScrollPane(pStdOutArea);
	    
	    scroll.setHorizontalScrollBarPolicy
	      (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    scroll.setVerticalScrollBarPolicy
	      (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    
	    scroll.setMinimumSize(new Dimension(100, 50));

	    opanel.add(scroll);
	  }
	}
	
	JPanel epanel = new JPanel();
	{
	  epanel.setName("MainDialogPanel");
	  epanel.setLayout(new BoxLayout(epanel, BoxLayout.Y_AXIS));
	  
	  epanel.add(UIMaster.createPanelLabel("Editor Errors:"));
	  
	  epanel.add(Box.createRigidArea(new Dimension(0, 4)));
	  
	  {
	    JTextArea area = new JTextArea(null, 10, 80);
	    pStdErrArea = area;
	    area.setName("CodeTextArea");
	    area.setLineWrap(true);
	    area.setWrapStyleWord(true);
	    area.setEditable(false);
	  }
	  
	  {
	    JScrollPane scroll = new JScrollPane(pStdErrArea);
	    
	    scroll.setHorizontalScrollBarPolicy
	      (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    scroll.setVerticalScrollBarPolicy
	      (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	    scroll.setMinimumSize(new Dimension(100, 50));
	    
	    epanel.add(scroll);
	  }
	}

	split = new JVertSplitPanel(opanel, epanel);
      }  
      
      JSplitPane body = new JVertSplitPanel(cpanel, split);

      body.setAlignmentX(0.5f);
      body.setMinimumSize(new Dimension(100, 350));

      super.initUI("X", true, body, null, null, null, "Close");
    }
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the UI components for the given subprocess.
   * 
   * @param header
   *   The header label text.
   * 
   * @param proc
   *   The failed subprocess.
   */ 
  public void 
  updateProc
  (
   String header, 
   SubProcess proc
  )
  {
    pHeaderLabel.setText(header);

    pCommandArea.setText(proc.getCommand());
    pStdOutArea.setText(proc.getStdOut());
    pStdErrArea.setText(proc.getStdErr());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5494894104009213275L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The command-line text area.
   */ 
  private JTextArea  pCommandArea;

  /**
   * The command output.
   */ 
  private JTextArea  pStdOutArea;

  /**
   * The command errors.
   */ 
  private JTextArea  pStdErrArea;
  
}
