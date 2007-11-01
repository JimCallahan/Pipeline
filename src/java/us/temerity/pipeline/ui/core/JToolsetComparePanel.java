// $Id: JToolsetComparePanel.java,v 1.1 2007/11/01 07:55:25 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 
import us.temerity.pipeline.toolset.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   T O O L S E T   C O M P A R E   P A N E L                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays a side-by-side comparison of the environmental variables from two toolsets.
 */ 
public 
class JToolsetComparePanel
  extends JPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new panel.
   * 
   * @param os
   *   The operating system type.
   */ 
  protected
  JToolsetComparePanel
  (
   OsType os
  ) 
  {
    /* initialize fields */ 
    {
      pOsType = os;
    }

    /* create dialog body components */ 
    {
      setLayout(new BorderLayout());
      
      JPanel mpanel = new JPanel();
      mpanel.setName("MainPanel");
      mpanel.setLayout(new BoxLayout(mpanel, BoxLayout.X_AXIS));  

      mpanel.add(Box.createRigidArea(new Dimension(20, 0)));

      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	
	vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	
        vbox.add(UIFactory.createPanelLabel(pOsType + " Environment:"));
	
	vbox.add(Box.createRigidArea(new Dimension(0, 4)));
	
        {
          Box hbox = new Box(BoxLayout.X_AXIS);
          
          {
            JPanel panel = new JPanel();
            pTitlePanel = panel;
            
            panel.setName("TitlePanel");
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            
            Dimension size = new Dimension(sTSize, 80);
            panel.setMinimumSize(size);
            panel.setPreferredSize(size);
            panel.setMaximumSize(new Dimension(sTSize, Integer.MAX_VALUE));

            hbox.add(panel);
          }
          
          {
            JPanel panel = new JPanel();
            pLeftValuePanel = panel;
            
            panel.setName("ValuePanel");
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            
            hbox.add(panel);
          }
          
          {
            JPanel panel = new JPanel();   
            pRightValuePanel = panel;
            
            panel.setName("ValuePanel");
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            
            hbox.add(panel);
          }
          
          JScrollPane scroll = 
            UIFactory.createVertScrollPane(hbox, sTSize+sVSize*2+52, 80);
          vbox.add(scroll);
	}

	vbox.add(Box.createRigidArea(new Dimension(0, 20)));

        mpanel.add(vbox);
      }

      mpanel.add(Box.createRigidArea(new Dimension(20, 0)));
      
      add(mpanel);
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the toolsets being displayed in the left and right portions of the panel.
   * 
   * @param left
   *   The versions of the toolset.
   * 
   * @param right
   *   The versions of the toolset.
   */ 
  public void 
  updateToolsets
  (
   Toolset left,
   Toolset right
  )
  { 
    pTitlePanel.removeAll();
    pLeftValuePanel.removeAll();
    pRightValuePanel.removeAll();

    TreeMap<String,String> lenv = null;
    if(left != null) 
      lenv = left.getEnvironment();
    else
      lenv = new TreeMap<String,String>();

    TreeMap<String,String> renv = null;
    if(right != null) 
      renv = right.getEnvironment();
    else
      renv = new TreeMap<String,String>();

    TreeSet<String> keys = new TreeSet<String>();
    keys.addAll(lenv.keySet());
    keys.addAll(renv.keySet());

    for(String key : keys) {
      String lvalue = lenv.get(key);
      String rvalue = renv.get(key);

      Color fg = Color.white;
      if(((lvalue == null) && (rvalue != null)) || 
         ((lvalue != null) && (rvalue == null)))
        fg = Color.yellow;
      else if((lvalue != null) && !lvalue.equals(rvalue))
        fg = Color.cyan;

      {
        JLabel label = UIFactory.createLabel(key + ":", sTSize, JLabel.RIGHT);
        label.setForeground(fg);
        pTitlePanel.add(label);

        pTitlePanel.add(Box.createRigidArea(new Dimension(0, 3)));
      }

      {
        String text = (lvalue != null) ? lvalue : "-";

        JTextField field = 
          UIFactory.createTextField
            (text, sVSize, (lvalue != null) ? JLabel.LEFT : JLabel.CENTER);
        field.setForeground(fg);   
        field.setEnabled(lvalue != null);
        pLeftValuePanel.add(field);

        pLeftValuePanel.add(Box.createRigidArea(new Dimension(0, 3)));
      }

      {
        String text = (rvalue != null) ? rvalue : "-";

        JTextField field = 
          UIFactory.createTextField
            (text, sVSize, (rvalue != null) ? JLabel.LEFT : JLabel.CENTER);
        field.setForeground(fg);   
        field.setEnabled(rvalue != null);
        pRightValuePanel.add(field);

        pRightValuePanel.add(Box.createRigidArea(new Dimension(0, 3)));
      }
    }

    pTitlePanel.add(Box.createVerticalGlue());
    pLeftValuePanel.add(Box.createVerticalGlue());
    pRightValuePanel.add(Box.createVerticalGlue());

    validate();
    repaint();    
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8476905541457079505L;

  private static final int sTSize = 200;
  private static final int sVSize = 300;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The target operating system type.
   */ 
  private OsType  pOsType; 

  /**
   * The environmental variable title and value container panels.
   */  
  private JPanel pTitlePanel;
  private JPanel pLeftValuePanel; 
  private JPanel pRightValuePanel; 


}
