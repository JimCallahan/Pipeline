// $Id: JConfirmFrameRangeDialog.java,v 1.2 2006/09/25 12:11:44 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.*;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   C O N F I R M   F R A M E   R A N G E   D I A L O G                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Asks whether a file sequence with a suspiciously large frame range should be created.
 */ 
public 
class JConfirmFrameRangeDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent frame.
   * 
   * @param range
   *   Thesuspicious frame range.
   */ 
  public 
  JConfirmFrameRangeDialog
  (
   Frame owner,
   FrameRange range
  )
  {
    super(owner, "Confirm");
    initUI(range);
  }

  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent dialog.
   * 
   * @param range
   *   Thesuspicious frame range.
   */ 
  public 
  JConfirmFrameRangeDialog
  (
   Dialog owner,
   FrameRange range
  )
  {
    super(owner, "Confirm");
    initUI(range);
  }


  /*----------------------------------------------------------------------------------------*/

  private void
  initUI
  (
   FrameRange range   
  ) 
  {
    /* create dialog body components */ 
    {
      JPanel body = new JPanel(new BorderLayout());
      body.setName("MainDialogPanel");

      body.setMinimumSize(new Dimension(300, 180));

      JTextArea area = null;
      {
	String text = 
	  ("Attempting to create a file sequence with a frame range (" + range + ") that " + 
	   "contains an unusually large number of frames (" + range.numFrames() + ")!\n\n" + 
	   "File sequences that are this large may require more memory than is available " + 
	   "on your system to be displayed in the File Details panel.");

	area = new JTextArea(text, 8, 35); 
	
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

      super.initUI("Are you sure?", body, "Yes", null, null, "No");
      pack();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3772936374210911536L;

}
