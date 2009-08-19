// $Id: JFileNoveltyTableCellRenderer.java,v 1.1 2009/08/19 23:49:20 jim Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

import us.temerity.pipeline.laf.LookAndFeelLoader;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   N O V E L T Y    T A B L E   C E L L   R E N D E R E R                       */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable} cells used in the JFileSeqPanel table.
 */ 
public
class JFileNoveltyTableCellRenderer
  extends JFastTableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JFileNoveltyTableCellRenderer
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
    FileSeqTableModel.IconState state = pModel.getIconState(row, col);
    if(state == null) 
      return this;

    boolean sel = isSelected && pModel.isEnabled(row); 

    switch(state) {
    case Check: 
      setIcon(sel ? sFileCheckIconSelected : sFileCheckIcon); 
      break;

    case CheckExtLeft: 
      setIcon(sel ? sFileCheckExtLeftIconSelected : sFileCheckExtLeftIcon);
      break;

    case CheckExtRight: 
      setIcon(sel ? sFileCheckExtRightIconSelected : sFileCheckExtRightIcon);
      break;

    case CheckExtBoth: 
      setIcon(sel ? sFileCheckExtBothIconSelected : sFileCheckExtBothIcon);
      break;


    case CheckPicked: 
      setIcon(sel ? sFileCheckPickedIconSelected : sFileCheckPickedIcon);
      break;

    case CheckPickedExtLeft: 
      setIcon(sel ? sFileCheckPickedExtLeftIconSelected : sFileCheckPickedExtLeftIcon);
      break;

    case CheckPickedExtRight: 
      setIcon(sel ? sFileCheckPickedExtRightIconSelected : sFileCheckPickedExtRightIcon);
      break;

    case CheckPickedExtBoth: 
      setIcon(sel ? sFileCheckPickedExtBothIconSelected : sFileCheckPickedExtBothIcon);
      break;

      
    case CheckDisabled: 
      setIcon(sel ? sFileCheckDisabledIconSelected : sFileCheckDisabledIcon);
      break;

    case CheckDisabledExtLeft:
      setIcon(sel ? sFileCheckDisabledExtLeftIconSelected : sFileCheckDisabledExtLeftIcon);
      break;

    case CheckDisabledExtRight:
      setIcon(sel ? sFileCheckDisabledExtRightIconSelected : sFileCheckDisabledExtRightIcon);
      break;

    case CheckDisabledExtBoth:
      setIcon(sel ? sFileCheckDisabledExtBothIconSelected : sFileCheckDisabledExtBothIcon);
      break;


    case Offline: 
      setIcon(sel ? sFileOfflineIconSelected : sFileOfflineIcon);
      break;

    case OfflineExtLeft:
      setIcon(sel ? sFileOfflineExtLeftIconSelected : sFileOfflineExtLeftIcon);
      break;

    case OfflineExtRight:
      setIcon(sel ? sFileOfflineExtRightIconSelected : sFileOfflineExtRightIcon);
      break;

    case OfflineExtBoth:
      setIcon(sel ? sFileOfflineExtBothIconSelected : sFileOfflineExtBothIcon);
      break;


    case BarExtRight: 
      setIcon(sel ? sFileBarExtRightIconSelected : sFileBarExtRightIcon); 
      break;

    case BarExtBoth: 
      setIcon(sel ? sFileBarExtBothIconSelected : sFileBarExtBothIcon); 
      break;


    case Missing:
      setIcon(null); 
    }
    
    return this;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3133267989226283845L;


  /*----------------------------------------------------------------------------------------*/

  private static final Icon sFileCheckIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckIcon.png"));

  private static final Icon sFileCheckExtLeftIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckExtLeftIcon.png"));

  private static final Icon sFileCheckExtRightIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckExtRightIcon.png"));

  private static final Icon sFileCheckExtBothIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckExtBothIcon.png"));


  private static final Icon sFileCheckPickedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckPickedIcon.png"));

  private static final Icon sFileCheckPickedExtLeftIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckPickedExtLeftIcon.png"));

  private static final Icon sFileCheckPickedExtRightIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckPickedExtRightIcon.png"));

  private static final Icon sFileCheckPickedExtBothIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckPickedExtBothIcon.png"));


  private static final Icon sFileCheckDisabledIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckDisabledIcon.png"));

  private static final Icon sFileCheckDisabledExtLeftIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckDisabledExtLeftIcon.png"));

  private static final Icon sFileCheckDisabledExtRightIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckDisabledExtRightIcon.png"));

  private static final Icon sFileCheckDisabledExtBothIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckDisabledExtBothIcon.png"));


  private static final Icon sFileOfflineIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileOfflineIcon.png"));

  private static final Icon sFileOfflineExtLeftIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileOfflineExtLeftIcon.png"));

  private static final Icon sFileOfflineExtRightIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileOfflineExtRightIcon.png"));

  private static final Icon sFileOfflineExtBothIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileOfflineExtBothIcon.png"));


  private static final Icon sFileBarExtRightIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileBarExtRightIcon.png"));

  private static final Icon sFileBarExtBothIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileBarExtBothIcon.png"));

  /*----------------------------------------------------------------------------------------*/

  private static final Icon sFileCheckIconSelected =  
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckIconSelected.png"));

  private static final Icon sFileCheckExtLeftIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckExtLeftIconSelected.png"));

  private static final Icon sFileCheckExtRightIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckExtRightIconSelected.png"));

  private static final Icon sFileCheckExtBothIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckExtBothIconSelected.png"));


  private static final Icon sFileCheckPickedIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckPickedIconSelected.png"));

  private static final Icon sFileCheckPickedExtLeftIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckPickedExtLeftIconSelected.png"));

  private static final Icon sFileCheckPickedExtRightIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckPickedExtRightIconSelected.png"));

  private static final Icon sFileCheckPickedExtBothIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckPickedExtBothIconSelected.png"));


  private static final Icon sFileCheckDisabledIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckDisabledIconSelected.png"));

  private static final Icon sFileCheckDisabledExtLeftIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckDisabledExtLeftIconSelected.png"));

  private static final Icon sFileCheckDisabledExtRightIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckDisabledExtRightIconSelected.png"));

  private static final Icon sFileCheckDisabledExtBothIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileCheckDisabledExtBothIconSelected.png"));


  private static final Icon sFileOfflineIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileOfflineIconSelected.png"));

  private static final Icon sFileOfflineExtLeftIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileOfflineExtLeftIconSelected.png"));

  private static final Icon sFileOfflineExtRightIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileOfflineExtRightIconSelected.png"));

  private static final Icon sFileOfflineExtBothIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileOfflineExtBothIconSelected.png"));


  private static final Icon sFileBarExtRightIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileBarExtRightIconSelected.png"));

  private static final Icon sFileBarExtBothIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource
                  ("FileBarExtBothIconSelected.png"));



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent talble model;
   */ 
  private FileSeqTableModel pModel; 

}
