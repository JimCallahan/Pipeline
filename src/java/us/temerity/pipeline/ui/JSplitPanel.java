// $Id: JSplitPanel.java,v 1.1 2004/04/26 23:20:10 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   S P L I T   P L A N E   P L                                                            */
/*------------------------------------------------------------------------------------------*/

public 
class JSplitPanel
  extends JSplitPane
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with the specified orientation and components.   
   * 
   * @param newOrientation
   *   JSplitPane.HORIZONTAL_SPLIT or JSplitPane.VERTICAL_SPLIT
   * 
   * @param newLeftComponent
   *   The Component that will appear on the left of a horizontally-split pane, or at 
   *   the top of a vertically-split pane.
   * 
   * @param newRightComponent
   *   The Component that will appear on the right of a horizontally-split pane, or at 
   *   the bottom of a vertically-split pane
   */
  JSplitPanel
  (
   int newOrientation, 
   Component newLeftComponent, 
   Component newRightComponent
  )
  {
    super(newOrientation, true, newLeftComponent, newRightComponent);

    setOneTouchExpandable(true);
    setResizeWeight(0.5);
    setDividerSize(10);

    Container bar = (Container) getComponent(2); 
    switch(newOrientation) {
    case JSplitPane.HORIZONTAL_SPLIT:
      bar.getComponent(0).setName("SplitPaneHorizontalDivider.leftOneTouchButton");
      bar.getComponent(1).setName("SplitPaneHorizontalDivider.rightOneTouchButton");
      break;

    case JSplitPane.VERTICAL_SPLIT:
      bar.getComponent(0).setName("SplitPaneVerticalDivider.upOneTouchButton");
      bar.getComponent(1).setName("SplitPaneVerticalDivider.downOneTouchButton");
      break;

    default:
      assert(false);
    }
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  //private static final long serialVersionUID = -3122417485809218152L;

}
