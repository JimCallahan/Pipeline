// $Id: JVertSplitPanel.java,v 1.1 2004/05/11 19:17:03 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.glue.*;

import java.awt.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   V E R T   S P L I T   P A N E L                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A vertical <CODE>JSplitPane</CODE> which implements {@link Glueable Glueable}. <P> 
 * 
 * In addition, this class names its one-touch buttons so that the Synth 
 * look-and-feel can properly assign icons to these buttons.
 */ 
public 
class JVertSplitPanel
  extends JSplitPane
  implements Glueable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  JVertSplitPanel() 
  {
    super(JSplitPane.VERTICAL_SPLIT, true, new JLabel(), new JLabel()); 
    initUI();
  }

  /**
   * Construct with the specified orientation and components.   
   * 
   * @param top
   *   The Component that will appear above the divider.
   * 
   * @param bottom
   *   The Component that will appear below the divider.
   */
  public
  JVertSplitPanel
  (
   Component top, 
   Component bottom
  )
  {
    super(JSplitPane.VERTICAL_SPLIT, true, top, bottom);
    initUI();    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components.
   */ 
  private void 
  initUI() 
  {
    setName("Split");

    setOneTouchExpandable(true);
    setResizeWeight(0.5);
    setDividerSize(10);

    Container bar = (Container) getComponent(2); 
    bar.getComponent(0).setName("SplitPaneVerticalDivider.upOneTouchButton");
    bar.getComponent(1).setName("SplitPaneVerticalDivider.downOneTouchButton");
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    encoder.encode("Top", (Glueable) getTopComponent());
    encoder.encode("Bottom", (Glueable) getBottomComponent());
    encoder.encode("DividerLocation", getDividerLocation());
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    Component top = (Component) decoder.decode("Top");
    if(top == null) 
      throw new GlueException("The \"Top\" was missing or (null)!");
    setTopComponent(top);
	
    Component bottom = (Component) decoder.decode("Bottom");
    if(bottom == null) 
      throw new GlueException("The \"Bottom\" was missing or (null)!");
    setBottomComponent(bottom);
    
    Integer div = (Integer) decoder.decode("DividerLocation");
    if(div == null) 
      throw new GlueException("The \"DividerLocation\" was missing or (null)!");
    setDividerLocation(div);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 3047669563591539312L;

}
