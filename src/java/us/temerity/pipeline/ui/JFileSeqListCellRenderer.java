// $Id: JFileSeqListCellRenderer.java,v 1.2 2004/07/14 21:05:53 jim Exp $

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
/*   F I L E   S E Q   L I S T   C E L L   R E N D E R E R                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for the {@link JList JList} cells containing either {@link File File} 
 * or {@link FileSeq FileSeq} values.
 */ 
public
class JFileSeqListCellRenderer
  extends JFileListCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JFileSeqListCellRenderer() 
  {
    super();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the current directory.
   */ 
  public void 
  setDirectory
  (
   File dir
  ) 
  {
    pDir = dir;
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
    if(value instanceof File) {
      File file = (File) value;
      assert(file.isDirectory());

      pNameLabel.setText(file.getName()); 
      pNameLabel.setIcon(isSelected ? sDirSelectedIcon : sDirNormalIcon);
    
      pSizeLabel.setText(null);

      Date date = new Date(file.lastModified());
      pDateLabel.setText(Dates.format(date));
    }
    else if(value instanceof FileSeq) {
      FileSeq fseq = (FileSeq) value;

      pNameLabel.setText(fseq.toString());
      pNameLabel.setIcon(isSelected ? sSelectedIcon : sNormalIcon);

      long size = 0;
      long lastMod = 0;
      for(File file : fseq.getFiles()) {
	File path = new File(pDir, file.getName());
	if(path.exists() && path.isFile()) {
	  size += path.length();
	  lastMod = Math.max(lastMod, path.lastModified());
	}
      }

      if(size > 1073741824) 
	pSizeLabel.setText(pFormat.format(((double) size) / 1073741824.0) + "G");
      else if(size > 1048576) 
	pSizeLabel.setText(pFormat.format(((double) size) / 1048576) + "M");
      else if(size > 1024) 
	pSizeLabel.setText(pFormat.format(((double) size) / 1024) + "K");
      else
	pSizeLabel.setText(String.valueOf(size));
      
      if(lastMod > 0) {
	Date date = new Date(lastMod);
	pDateLabel.setText(Dates.format(date));
      }
      else {
	pDateLabel.setText(null);
      }
    }

    Color fg = (isSelected ? Color.yellow : Color.white);
    pNameLabel.setForeground(fg);
    pDateLabel.setForeground(fg);
    
    return this;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -463964161555323073L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The current directory.
   */ 
  private File  pDir;   

}
