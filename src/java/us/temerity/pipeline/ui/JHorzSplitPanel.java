// $Id: JHorzSplitPanel.java,v 1.1 2004/05/11 19:17:03 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.glue.*;

import java.awt.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   H O R Z   S P L I T   P A N E L                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A horizontal <CODE>JSplitPane</CODE> which implements {@link Glueable Glueable}. <P> 
 * 
 * In addition, this class names its one-touch buttons so that the Synth 
 * look-and-feel can properly assign icons to these buttons.
 */ 
public 
class JHorzSplitPanel
  extends JSplitPane
  implements Glueable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  JHorzSplitPanel() 
  {
    super(JSplitPane.HORIZONTAL_SPLIT, true, new JLabel(), new JLabel()); 
    initUI();
  }

  /**
   * Construct with the specified orientation and components.   
   * 
   * @param left
   *   The Component that will appear on the left of the divider.
   * 
   * @param right
   *   The Component that will appear on the right of the divider.
   */
  public
  JHorzSplitPanel
  (
   Component left, 
   Component right
  )
  {
    super(JSplitPane.HORIZONTAL_SPLIT, true, left, right);
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
    bar.getComponent(0).setName("SplitPaneHorizontalDivider.leftOneTouchButton");
    bar.getComponent(1).setName("SplitPaneHorizontalDivider.rightOneTouchButton");
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
    encoder.encode("Left", (Glueable) getLeftComponent());
    encoder.encode("Right", (Glueable) getRightComponent());
    encoder.encode("DividerLocation", getDividerLocation());
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    Component left = (Component) decoder.decode("Left");
    if(left == null) 
      throw new GlueException("The \"Left\" was missing or (null)!");
    setLeftComponent(left);
	
    Component right = (Component) decoder.decode("Right");
    if(right == null) 
      throw new GlueException("The \"Right\" was missing or (null)!");
    setRightComponent(right);
    
    Integer div = (Integer) decoder.decode("DividerLocation");
    if(div == null) 
      throw new GlueException("The \"DividerLocation\" was missing or (null)!");
    setDividerLocation(div);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 2810029111074533103L;


}
