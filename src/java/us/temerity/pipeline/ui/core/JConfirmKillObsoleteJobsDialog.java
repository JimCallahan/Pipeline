// $Id: JConfirmKillObsoleteJobsDialog.java,v 1.1 2006/01/16 04:11:12 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.*;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   C O N F I R M   K I L L   O B S O L E T E   J O B S   D I A L O G                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Asks whether jobs associated with obsolete frames after a renumber should be killed.
 */ 
public 
class JConfirmKillObsoleteJobsDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param name
   *   The name of the renumbered node. 
   * 
   * @param jobIDs
   *   The unfinished job IDs.
   */ 
  public 
  JConfirmKillObsoleteJobsDialog
  ( 
   String name, 
   TreeSet<Long> jobIDs
  ) 
  {
    super("Confirm", true);

    /* create dialog body components */ 
    {
      JPanel body = new JPanel(new BorderLayout());
      body.setName("MainDialogPanel");

      body.setMinimumSize(new Dimension(300, 180));

      JTextArea area = null;
      {
	StringBuffer buf = new StringBuffer();
	buf.append("The following unfinished jobs will regenerate frames which have been " +
		   "made obsolete by the renumber operation:\n" +
		   "\n");

	buf.append("Node: " + name + ":\n" + 
		   "Jobs:");
	for(Long jobID : jobIDs) 
	  buf.append(" " + jobID);
	buf.append("\n\n");
	
	area = new JTextArea(buf.toString(), 8, 35); 
	area.setName("TextArea");

	area.setLineWrap(true);
	area.setWrapStyleWord(true);
	area.setEditable(false);

	area.setFocusable(true);
      }
      
      {
	JScrollPane scroll = new JScrollPane(area);
	scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	
	body.add(scroll);
      }

      super.initUI("Kill Obsolete Jobs?", true, body, "Yes", null, null, "No");
      pack();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3614821228922667189L;

}
