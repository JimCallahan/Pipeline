// $Id: JBaseMonitorPanel.java,v 1.1 2004/10/28 15:55:24 jim Exp $

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
   * 
   * @param rows
   *   The preferred number of rows of text.
   * 
   * @param cols
   *   The preferred number of columns of text.
   */ 
  public 
  JBaseMonitorPanel
  (
   int rows, 
   int cols
  ) 
  {
    pFirstUpdate = new AtomicBoolean(false);

    {
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      
      {
	JMonitorTextArea area = new JMonitorTextArea(rows, cols);
	pTextArea = area;
	
	add(area);
      }
      
      add(Box.createRigidArea(new Dimension(4, 0)));
      
      {
	JScrollBar bar = new JScrollBar(JScrollBar.VERTICAL, 0, rows, 0, rows);
	pScrollBar = bar;
	
	bar.addAdjustmentListener(this);
	
	add(bar);
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
    int lines = getNumLines() - 1;
    if(lines != pScrollBar.getMaximum())
      pScrollBar.setMaximum(lines);

    pScrollBar.setValue(pos);

    updateContents();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the scroll bar maximum to match the number of lines which may potentially 
   * be viewed.
   */ 
  public void 
  updateScrollBar() 
  {
    GetScrollBarMaxTask task = new GetScrollBarMaxTask();
    task.start();
  }

  /**
   * Update the contents of the text area.
   */ 
  public void 
  updateContents() 
  {
    if(!pFirstUpdate.get()) {
      pTextArea.setText("(Loading...)");
    }
    else if(!pTextArea.isVisible() || (pTextArea.getVisibleRows() < 1)) {
      pTextArea.setText(null);
    }
    else {
      String lines = getLines(pScrollBar.getValue(), pTextArea.getVisibleRows());
      if(lines != null) 
	pTextArea.setText(lines);
      else 
	pTextArea.setText("(Nothing Yet...)");			
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the current number of lines which may potentially be viewed.
   */
  protected abstract int 
  getNumLines();

  /** 
   * Get the current text for the given region of lines. <P> 
   * 
   * @param start
   *   The line number of the first line of text.
   * 
   * @param lines
   *   The number of lines of text to retrieve. 
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
    JMonitorTextArea
    (
     int rows, 
     int cols
    ) 
    {
      super();
      
      setPreferredSize(new Dimension(cols * getColumnWidth(), rows * getRowHeight()));

      setName("MonitorTextArea");
      setLineWrap(true);
      setWrapStyleWord(true);
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
   * Get the maximum value for the scroll bar components.
   */
  private
  class GetScrollBarMaxTask
    extends Thread
  {
    public 
    GetScrollBarMaxTask() 
    {
      super("JBaseMonitorPanel:GetScrollBarMaxTask");
    }

    public void 
    run()
    {
      int numLines = getNumLines();
      pFirstUpdate.set(true);
      SwingUtilities.invokeLater(new UpdateScrollBarTask(numLines));
    }
  }

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
