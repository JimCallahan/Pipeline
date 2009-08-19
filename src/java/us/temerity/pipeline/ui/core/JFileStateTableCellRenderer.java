// $Id: JFileStateTableCellRenderer.java,v 1.1 2009/08/19 23:49:20 jim Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

import us.temerity.pipeline.laf.LookAndFeelLoader;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   S T A T E    T A B L E   C E L L   R E N D E R E R                           */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable} cells used in the JFileSeqPanel table.
 */ 
public
class JFileStateTableCellRenderer
  extends JFastTableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JFileStateTableCellRenderer
  (
   FileSeqTableModel model
  )  
  {
    pModel = model;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/
          
  /**
   * Returns the component used for drawing the cell.
   */ 
  public Component 	
  getTableCellRendererComponent
  (
   JTable table, 
   Object value, 
   boolean isSelected, 
   boolean hasFocus, 
   int row, 
   int col
  )
  {
    String prefix = (String) value;
    
    TextureMgr mgr = TextureMgr.getInstance();
    try {
      String suffix = (pModel.isEnabled(row) && isSelected) ? "Selected" : "Normal"; 
      setIcon(mgr.getIcon21(prefix + suffix));
    }	
    catch(PipelineException ex) {
      setIcon(null); 
      UIMaster.getInstance().showErrorDialog(ex);
    }
    
    return this;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1026090129214149429L;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent table model.
   */ 
  private FileSeqTableModel pModel;
}
