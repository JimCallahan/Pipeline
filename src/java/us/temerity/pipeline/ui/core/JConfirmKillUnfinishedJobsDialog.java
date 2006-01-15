// $Id: JConfirmKillUnfinishedJobsDialog.java,v 1.1 2006/01/15 17:42:27 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.*;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   C O N F I R M   K I L L   U N F I N I S H E D   J O B S   D I A L O G                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Asks whether a file sequence with a suspiciously large frame range should be created.
 */ 
public 
class JConfirmKillUnfinishedJobsDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param name
   *   The name of the root node of the check-out.
   * 
   * @param jobIDs
   *   The unfinished job IDs indexed by node name.
   */ 
  public 
  JConfirmKillUnfinishedJobsDialog
  ( 
   String root, 
   TreeMap<String,TreeSet<Long>> jobIDs
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
	buf.append("Check-Out Aborted!\n" + 
		   "\n" + 
		   "Unable to perform check-out of node (" + root + ") because unfinished " + 
		   "jobs are associated with downstream nodes which would likely be " + 
		   "be made Stale by the check-out.\n" + 
		   "\n" + 
		   "The following jobs must either be killed or allowed to finish " + 
		   "normally before the check-out can be performed:\n\n");

	for(String name : jobIDs.keySet()) {
	  buf.append("Node: " + name + ":\n" + 
		     "Jobs:");
	  for(Long jobID : jobIDs.get(name)) 
	    buf.append(" " + jobID);
	  buf.append("\n\n");
	}
	
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

      super.initUI("Kill Unfinished Jobs?", true, body, "Yes", null, null, "No");
      pack();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -729441957420008827L;

}
