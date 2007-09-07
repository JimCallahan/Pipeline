// $Id: JUnsavedChangesDialog.java,v 1.3 2007/09/07 18:52:38 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   U N S A V E D   C H A N G E S   D I A L O G                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Asks a simple question.
 */ 
public 
class JUnsavedChangesDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   *
   * @param panel
   *   The panel with unsaved changes.
   * 
   * @param msg
   *   The initial message displayed before the list of unsaved changes.
   */
  public 
  JUnsavedChangesDialog
  (
   JTopLevelPanel panel,
   String msg
  )
  {
    super(panel.getTopFrame(), "UnsavedChanges");

    {
      JPanel body = new JPanel(new BorderLayout());
      body.setName("MainDialogPanel");

      body.setMinimumSize(new Dimension(300, 300));

      {
        StringBuilder buf = new StringBuilder();
        {
          buf.append
            (msg + "\n\n" + 
             "The unsaved changes include:\n");
          
          for(String change : panel.unsavedChanges())
            buf.append("  " + change + "\n");
          
          buf.append
            ("\n" + 
             "Before continuing, please decide whether to Apply these changes now and " + 
             "abort the planned operation or to Ignore this warning and proceed normally " +
             "abandoning these unsaved changes.");
        }

	JTextArea area = new JTextArea(buf.toString(), 12, 35); 
	area.setName("TextArea");

	area.setLineWrap(true);
	area.setWrapStyleWord(true);
	area.setEditable(false);

	area.setFocusable(true);
      
	{
	  JScrollPane scroll = 
            UIFactory.createScrollPane
            (area, 
             JScrollPane.HORIZONTAL_SCROLLBAR_NEVER, 
             JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
             null, null, null);
	  
	  body.add(scroll);
	}
      }

      super.initUI("Warning:  Unsaved Changes", 
                   body, "Apply Now", null, null, "Ignore");
      pack();
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5390818847538618950L;

}
