// $Id: JSplitPanel.java,v 1.3 2004/04/30 11:24:29 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   S P L I T   P L A N E L                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A <CODE>JSplitPane</CODE> which names its one-touch buttons so that the Synth 
 * look-and-feel can properly assign icons to these buttons.
 */ 
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
    setName("Split");

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
  
  private static final long serialVersionUID = 7336580649412749232L;


}
