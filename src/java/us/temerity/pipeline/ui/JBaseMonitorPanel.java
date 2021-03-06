// $Id: JBaseMonitorPanel.java,v 1.6 2009/07/01 16:43:14 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*; 

import java.util.*;
import java.util.concurrent.atomic.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   M O N I T O R   P A N E L                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A panel containing a scrollable text area which displays a portion of a large 
 * amount of text. <P> 
 * 
 * The contents of the text area are dynamically updated from the source text whenever
 * the scroll bar is adjusted or the text area is resized.
 */ 
public abstract
class JBaseMonitorPanel 
  extends JPanel
  implements AdjustmentListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new panel.
   */ 
  public 
  JBaseMonitorPanel()
  {
    pFirstUpdate = new AtomicBoolean(false);

    {
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      
      {
	JMonitorTextArea area = new JMonitorTextArea();
	pTextArea = area;
	
	{
	  JScrollPane scroll = 
            UIFactory.createScrollPane
            (area, 
             ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS,
             ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, 	  
             new Dimension(150, 100), new Dimension(800, 500), null);
	  
	  scroll.setWheelScrollingEnabled(false);

	  add(scroll);
	}
      }
      
      {
	Box vbox = new Box(BoxLayout.Y_AXIS);

	{
	  JScrollBar bar = new JScrollBar(JScrollBar.VERTICAL, 0, 10, 0, 10);
	  pScrollBar = bar;

	  bar.addAdjustmentListener(this);

	  vbox.add(bar);
	}

	vbox.add(Box.createRigidArea(new Dimension(0, 14)));
      
	add(vbox);
      }
    }
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the line number of the first line displayed by the text area. 
   */ 
  public int 
  getStartPos() 
  {
    return pScrollBar.getValue();
  }

  /**
   * Set the line number of the first line displayed by the text area. 
   */ 
  public void 
  setStartPos
  (
   int pos
  ) 
  {
    Integer numLines = getNumLines();
    if(numLines != null) {
      int lines = numLines - 1;
      if(lines != pScrollBar.getMaximum())
        pScrollBar.setMaximum(lines);

      pScrollBar.setValue(pos);

      updateContents();
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the scroll bar maximum to match the number of lines which may potentially 
   * be viewed.<P> 
   *
   * This may block for network communiation.
   */ 
  public void 
  updateScrollBar() 
  {
    Integer numLines = getNumLines();
    if(numLines != null) {
      pFirstUpdate.set(true);
      SwingUtilities.invokeLater(new UpdateScrollBarTask(numLines));
    }
  }

  /**
   * Update the contents of the text area.
   */ 
  public void 
  updateContents() 
  {
    if(!pFirstUpdate.get()) {
      pTextArea.setText("(Contacting Job Manager...)");
    }
    else if(!pTextArea.isVisible() || (pTextArea.getVisibleRows() < 1)) {
      pTextArea.setText(null);
    }
    else {
      GetLinesTask task = 
        new GetLinesTask(pScrollBar.getValue(), pTextArea.getVisibleRows()); 
      task.start(); 
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the current number of lines which may potentially be viewed.
   * 
   * @return 
   *   The number of lines or <CODE>null</CODE> if unable to determine the number of lines.
   */
  protected abstract Integer 
  getNumLines();

  /** 
   * Get the current text for the given region of lines. <P> 
   * 
   * @param start
   *   The line number of the first line of text.
   * 
   * @param lines
   *   The number of lines of text to retrieve. 
   * 
   * @return 
   *   The text or <CODE>null</CODE> if unable to retreive the text. 
   */
  protected abstract String
  getLines
  (
   int start, 
   int lines
  ); 

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /*-- ADJUSTMENT LISTENER METHODS ---------------------------------------------------------*/

  /**
   * Invoked when the value of the adjustable has changed.
   */
  public void 	
  adjustmentValueChanged
  (
   AdjustmentEvent e
  )
  {
    if(e.getValueIsAdjusting()) 
      return;

    updateContents();
  }

 

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private 
  class JMonitorTextArea
    extends JTextArea
    implements ComponentListener
  {
    public 
    JMonitorTextArea() 
    {
      super();

      setName("CodeTextArea");
      setLineWrap(false);
      setEditable(false);

      addComponentListener(this);
    }

    
    public Dimension 
    getMinimumSize()
    {
      return (new Dimension());
    }

    public void 
    setMinimumSize
    (
     Dimension minimumSize
    )
    {}


    public int
    getVisibleRows() 
    {
      return pVisibleRows;
    }


    public void 	
    componentHidden(ComponentEvent e) {} 
    
    public void 	
    componentMoved(ComponentEvent e) {} 
    
    public void 	
    componentResized
    (
     ComponentEvent e
    )
    {
      int rows = getHeight() / getRowHeight();
      if(rows != pVisibleRows) {
	pVisibleRows = rows;
	updateContents();
      }
    }

    public void 	
    componentShown(ComponentEvent e) {} 


    private static final long serialVersionUID = -7565902548199869190L;

    private int        pVisibleRows;
    private Dimension  pMinSize; 
  }



  /*----------------------------------------------------------------------------------------*/

  /** 
   * Update the scroll bar components.
   */
  private
  class UpdateScrollBarTask
    extends Thread
  {
    public 
    UpdateScrollBarTask
    (
     int numLines
    ) 
    {
      super("JBaseMonitorPanel:UpdateScrollBarTask");
      pNumLines = numLines;
    }

    public void 
    run()
    {
      int lines = pNumLines - 1;
      boolean updated = (lines != pScrollBar.getMaximum());
      if(updated) 
	pScrollBar.setMaximum(lines);
      
      pScrollBar.setVisibleAmount(pTextArea.getVisibleRows());

      if(updated && ((pScrollBar.getValue() + pTextArea.getVisibleRows()) > lines))
        updateContents(); 
    }

    private int pNumLines; 
  }

  /**
   * Get the lines to display in the text area from the Job Manager.
   */ 
  private
  class GetLinesTask
    extends Thread
  {
    public 
    GetLinesTask
    (
     int start, 
     int lines
    ) 
    {
      super("JBaseMonitorPanel:GetLinesTask");

      pStartNum = start; 
      pNumLines = lines; 
    }

    public void 
    run()
    {
      String lines = null;
      try {
        lines = getLines(pStartNum, pNumLines); 
      }
      catch(Exception ex) {
      }

      if(lines != null) 
        SwingUtilities.invokeLater(new UpdateLinesTask(lines));       
    }

    private int pStartNum; 
    private int pNumLines; 
  }

  /**
   * Update the text area contents.
   */ 
  private
  class UpdateLinesTask
    extends Thread
  {
    public 
    UpdateLinesTask
    (
     String lines
    ) 
    {
      super("JBaseMonitorPanel:UpdateLinesTask");
      pLines = lines; 
    }

    public void 
    run()
    {
      if(pLines != null) {
	if(pLines.length() == 0) 
	  pTextArea.setText("(Nothing Output)");
	else 
	  pTextArea.setText(pLines);
      }
      else {
	pTextArea.setText("(Nothing Yet...)");			
      }
    }

    private String pLines; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the first scroll bar update task has completed.
   */ 
  private AtomicBoolean  pFirstUpdate;

  /**
   * The text area.
   */
  protected JMonitorTextArea pTextArea;

  /**
   * The scroll bar.
   */
  private JScrollBar  pScrollBar;

}
