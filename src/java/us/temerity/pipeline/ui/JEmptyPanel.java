// $Id: JEmptyPanel.java,v 1.2 2004/04/30 11:24:29 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   E M P T Y   P A N E L                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * An placeholder panel used by {@link JManagerPanel JManagerPanel} when creating  
 * uninitialized child panels.
 */ 
public 
class JEmptyPanel
  extends JPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct an empty panel.
   */
  JEmptyPanel()
  {
    super(new BorderLayout());
    setName("Empty");
    setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 6067560279472521100L;

}
