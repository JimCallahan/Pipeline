// $Id: JLogPanel.java,v 1.2 2004/04/30 11:24:29 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   L O G   A R E A                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
public 
class JLogPanel
  extends JPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a log panel. 
   */
  JLogPanel()
  {
    super();

    setName("LogPanel");
    setLayout(new BorderLayout());

    JTextArea area = null;
    {
      area = new JTextArea(3, 80);
      area.append("Testing...\n\n");

      TextAreaLogHandler handler = new TextAreaLogHandler(area);
      Logs.addHandler(handler);

      area.setLineWrap(true);
      area.setWrapStyleWord(true);
      area.setEditable(false);
    }

    JScrollPane scroll = null;
    {
      scroll = new JScrollPane(area);
      scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

      add(scroll);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -8315838729409670369L;


  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

}
