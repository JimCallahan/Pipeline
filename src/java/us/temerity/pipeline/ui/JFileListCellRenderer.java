// $Id: JFileListCellRenderer.java,v 1.5 2005/06/14 13:38:33 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   L I S T   C E L L   R E N D E R E R                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for {@link JList JList} cells containing {@link File File} data
 * which displays the file name, last modified date and file size.
 */ 
public
class JFileListCellRenderer
  extends JPanel
  implements ListCellRenderer 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JFileListCellRenderer() 
  {
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    
    {
      pNameLabel = new JLabel();
      pNameLabel.setHorizontalAlignment(JLabel.LEFT);
      add(pNameLabel);
    }
    
    add(Box.createRigidArea(new Dimension(8, 0)));
    add(Box.createHorizontalGlue());
    
    {
      pSizeLabel = new JLabel("X");

      Dimension size = new Dimension(80, 23);
      pSizeLabel.setMinimumSize(size);
      pSizeLabel.setMaximumSize(size);
      pSizeLabel.setPreferredSize(size);

      pSizeLabel.setHorizontalAlignment(JLabel.RIGHT);

      add(pSizeLabel);
    }
    
    add(Box.createRigidArea(new Dimension(8, 0)));

    {
      pDateLabel = new JLabel("X");

      Dimension size = new Dimension(230, 23);
      pDateLabel.setMinimumSize(size);
      pDateLabel.setMaximumSize(size);
      pDateLabel.setPreferredSize(size);

      pDateLabel.setHorizontalAlignment(JLabel.RIGHT);

      add(pDateLabel);
    } 

    Dimension size = new Dimension(500, 23);
    setMinimumSize(size);
    setPreferredSize(size);

    pFormat = new DecimalFormat("###0.0");   
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generates the component to be displayed for JList cells.
   */ 
  public Component 
  getListCellRendererComponent
  (
   JList list,
   Object value,
   int index,
   boolean isSelected,
   boolean cellHasFocus
  )
  {
    File file = (File) value;
    boolean isDir = file.isDirectory();

    pNameLabel.setText(file.getName()); 

    if(!isDir) {
      long size = file.length();

      if(size > 1073741824) 
	pSizeLabel.setText(pFormat.format(((double) size) / 1073741824.0) + "G");
      else if(size > 1048576) 
	pSizeLabel.setText(pFormat.format(((double) size) / 1048576) + "M");
      else if(size > 1024) 
	pSizeLabel.setText(pFormat.format(((double) size) / 1024) + "K");
      else 
	pSizeLabel.setText(String.valueOf(size));
    }
    else {
      pSizeLabel.setText(null);
    }
    
    Date date = new Date(file.lastModified());
    pDateLabel.setText(Dates.format(date));

    if(isDir) 
      pNameLabel.setIcon(isSelected ? sDirSelectedIcon : sDirNormalIcon);
    else 
      pNameLabel.setIcon(isSelected ? sSelectedIcon : sNormalIcon);

    Color fg = (isSelected ? Color.yellow : Color.white);
    pNameLabel.setForeground(fg);
    pSizeLabel.setForeground(fg);
    pDateLabel.setForeground(fg);
    
    return this;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 9144374790563937303L;


  protected static final Icon sNormalIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellNormalIcon.png"));

  protected static final Icon sSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellSelectedIcon.png"));

  protected static final Icon sDirNormalIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("DirectoryNormalIcon.png"));

  protected static final Icon sDirSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("DirectorySelectedIcon.png"));

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The file/directory name label.
   */
  protected JLabel  pNameLabel;

  /**
   * The file size label.
   */
  protected JLabel  pSizeLabel;

  /**
   * The last modified timestamp label.
   */
  protected JLabel  pDateLabel;

  /**
   * The size formatter.
   */
  protected DecimalFormat  pFormat;

}
