// $Id: AdjustLinkage.java,v 1.1 2005/01/03 06:56:24 jim Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   A D J U S T   L I N K A G E                                                            */
/*------------------------------------------------------------------------------------------*/
  
/**
 * Links two viewports horizontal position.
 */ 
public 
class AdjustLinkage
  implements AdjustmentListener
{
  /**
   * Construct a new linkage.
   * 
   * @param source
   *   The source viewport.
   * 
   * @param target
   *   The viewport being adjusted.
   */ 
  public 
  AdjustLinkage
  (
   JViewport source, 
   JViewport target
  ) 
  {
    pSource = source;
    pTarget = target; 
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  public void
  adjustmentValueChanged
  (
   AdjustmentEvent e
  )
  { 
    Point spos = pSource.getViewPosition();    
    Point tpos = pTarget.getViewPosition();
    
    if(spos.x != tpos.x) {
      tpos.x = spos.x;
      pTarget.setViewPosition(tpos);
    }
  }    
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The source viewport.
   */ 
  private JViewport  pSource;

  /**
   * The viewport being adjusted.
   */ 
  private JViewport  pTarget;
}

