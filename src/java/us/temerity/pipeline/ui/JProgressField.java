// $Id: JProportionGraph.java,v 1.3 2009/05/14 23:30:43 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   P R O G R E S S    F I E L D                                                           */
/*------------------------------------------------------------------------------------------*/

/**
 * A component which displays a progress message, an optional timing message and an optional
 * bar showing completion percentage.
 */ 
public 
class JProgressField
  extends JPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new component.
   */
  public 
  JProgressField() 
  {
    super();

    setLayout(new BorderLayout());
    setName("JobStatesTableCellRenderer");  

    pBar = new JProgressBar();
    add(pBar);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * 
   * @param msg
   *   A message describing the progress.
   * 
   * @param timingMsg
   *   A short message describing the amount of time the operation has been or is expected
   *   to be running or <CODE>null</CODE> if no timing information is known.
   * 
   * @param percentage
   *   The completion percentage [0.0, 1.0] if known or <CODE>null</CODE> if unknown.
   */ 
  public void 
  update
  (
   String msg, 
   String timingMsg, 
   Float percentage 
  ) 
  {
    pBar.update(msg, timingMsg, percentage);
  } 


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L    C L A S S E S                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private 
  class JProgressBar 
    extends JComponent
  {
    /**
     * Construct a new component.
     */
    public 
    JProgressBar() 
    {
      super();

      pRunningColor  = new Color(0.35f, 0.35f, 0.35f);
      pFinishedColor = new Color(0.45f, 0.45f, 0.45f);
    }

    /**
     * Update the data being displayed. 
     */ 
    public void 
    update
    (
     String msg, 
     String timingMsg, 
     Float percentage
    ) 
    {
      pMessage    = msg; 
      pTimingMsg  = timingMsg; 
      pPercentage = percentage;
    } 

    /**
     * Performs any custom painting.
     */
    @Override
    protected void 
    paintComponent
    (
     Graphics graphics
    )
    {
      super.paintComponent(graphics);

      if((pMessage == null) && (pTimingMsg == null) && (pPercentage == null)) 
        return;

      Graphics2D gfx = (Graphics2D) graphics.create(); 
      int width = getWidth();
      int height = getHeight();

      if((width <= 0) || (height <= 0)) 
        return;

      /* draw the percentage complete meter */ 
      if(pPercentage != null) {
        int posX = Math.round(((float) width) * pPercentage);
        gfx.setColor(pFinishedColor);
        gfx.fill(new Rectangle(0, 0, posX, height));
        gfx.setColor(pRunningColor);
        gfx.fill(new Rectangle(posX, 0, width-posX, height));
      }

      Font font = gfx.getFont();
      FontRenderContext frc = gfx.getFontRenderContext();

      /* draw the progress message */ 
      Integer posY = null;
      if((pMessage != null) && (pMessage.length() > 0)) {
        gfx.setColor(Color.white);
        TextLayout layout = new TextLayout(pMessage, font, frc);
        Rectangle2D bounds = layout.getBounds();
        layout.draw(gfx, 6, height-4);
      }

      /* draw the timing message */ 
      if((pTimingMsg != null) && (pTimingMsg.length() > 0)) {
        gfx.setColor(Color.white);
        TextLayout layout = new TextLayout(pTimingMsg, font, frc);
        Rectangle2D bounds = layout.getBounds();
        layout.draw(gfx, width - 8 - ((int) bounds.getWidth()), height-4);
      }
    }

    private static final long serialVersionUID = -5415219722586916130L;

    /**
     * A message describing the progress.
     */
    private String pMessage; 

    /**
     * A short message describing the amount of time the operation has been or is expected
     * to be running or <CODE>null</CODE> if no timing information is known.
     */
    private String pTimingMsg;

    /**
     * The completion percentage [0.0, 1.0] if known or <CODE>null</CODE> if unknown.
     */
    private Float  pPercentage;

    /**
     * Progress bar colors.
     */ 
    private Color pRunningColor; 
    private Color pFinishedColor; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -6850977164653092798L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The progress bar.
   */ 
  private JProgressBar  pBar;
}
