// $Id: JOsSupportField.java,v 1.1 2006/05/07 21:34:00 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.laf.LookAndFeelLoader;
import us.temerity.pipeline.OsType;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   O S   S U P P O R T   F I E L D                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A field which represents operating system types supported by a plugin.
 */
public 
class JOsSupportField
  extends JPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   */ 
  public 
  JOsSupportField() 
  {
    super();  
    setName("OsSupportField");

    setAlignmentY(0.5f);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    {
      add(Box.createRigidArea(new Dimension(8, 0)));
      add(Box.createHorizontalGlue());

      pLabels = new JLabel[3];
      for(OsType os : OsType.all()) {
	JLabel label = new JLabel();
	pLabels[os.ordinal()] = label;
	label.setToolTipText(UIFactory.formatToolTip
	  ("The " + os.toTitle() + " operating system."));  
      }

      add(pLabels[OsType.Unix.ordinal()]);

      add(Box.createRigidArea(new Dimension(6, 0)));

      add(pLabels[OsType.MacOS.ordinal()]);

      add(Box.createRigidArea(new Dimension(6, 0)));

      add(pLabels[OsType.Windows.ordinal()]);
	
      add(Box.createHorizontalGlue());
      add(Box.createRigidArea(new Dimension(8, 0)));
    }

    pModifiedColor = false;

    setSupports(null);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Set the supported operating systems.
   */ 
  public void 
  setSupports
  (
   SortedSet<OsType> supports
  ) 
  {
    if(supports != null) 
      pSupports = new TreeSet<OsType>(supports);
    else 
      pSupports = null;

    updateFieldAppearance(); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J C O M P O N E N T   O V E R R I D E S                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sets the foreground color of this component.
   */ 
  public void 
  setForeground
  (
   Color fg
  )
  {
    pModifiedColor = !fg.equals(Color.white);
    if(pLabels != null) 
      updateFieldAppearance(); 
  }

       

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the appearance of the field.
   */
  private void 
  updateFieldAppearance()
  {
    if(pSupports != null) {
      for(OsType os : OsType.all()) {
	int idx = os.ordinal();

	Icon icon = sUnsupportedIcons[idx];
	if(pSupports.contains(os)) 
	  icon = pModifiedColor ? sModifiedIcons[idx] : sSupportedIcons[idx];
	pLabels[os.ordinal()].setIcon(icon);
      }
      pLabels[OsType.MacOS.ordinal()].setText(null);
    }
    else {
      for(OsType os : OsType.all()) 
	pLabels[os.ordinal()].setIcon(null);

      int idx = OsType.MacOS.ordinal();
      pLabels[idx].setText("-");
      pLabels[idx].setForeground(pModifiedColor ? Color.cyan : Color.white);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7769060376233827003L; 


  private static final Icon sSupportedIcons[] = {
    new ImageIcon(LookAndFeelLoader.class.getResource("UnixSupportedIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("WindowsSupportedIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("MacOSSupportedIcon.png"))
  };

  private static final Icon sUnsupportedIcons[] = {
    new ImageIcon(LookAndFeelLoader.class.getResource("UnixUnsupportedIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("WindowsUnsupportedIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("MacOSUnsupportedIcon.png"))
  };

  private static final Icon sModifiedIcons[] = {
    new ImageIcon(LookAndFeelLoader.class.getResource("UnixModifiedIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("WindowsModifiedIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("MacOSModifiedIcon.png"))
  };


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The supported operating systems.
   */ 
  private TreeSet<OsType> pSupports; 

  /**
   * Whether to use the modified color; 
   */ 
  private boolean pModifiedColor; 

  /**
   * The OS labels. 
   */ 
  private JLabel  pLabels[];



}
