// $Id: JEmptyPanel.java,v 1.4 2004/08/25 05:18:21 jim Exp $

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
  extends JTopLevelPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  public 
  JEmptyPanel()
  {
    super();
    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JEmptyPanel
  (
   JTopLevelPanel panel
  )
  {
    super(panel);
    initUI();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components.
   */ 
  private void 
  initUI()
  {
    setLayout(new BorderLayout());
    setName("DarkPanel");
    setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 6067560279472521100L;

}
