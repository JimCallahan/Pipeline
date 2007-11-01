// $Id: JToolsetCompareDialog.java,v 1.1 2007/11/01 07:55:25 jim Exp $

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
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   T O O L S E T   C O M P A R E   D I A L O G                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Compares the environmental variable settings of two toolsets against each other.
 */ 
public 
class JToolsetCompareDialog
  extends JFullDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent frame.
   */ 
  public 
  JToolsetCompareDialog
  (
   Dialog owner
  ) 
  {
    super(owner, "Toolset Compare");

    /* create dialog body components */ 
    {
      pOsPanels = new ArrayList<JToolsetComparePanel>();

      JTabbedPanel tab = new JTabbedPanel();
      for(OsType os : OsType.all()) {
        JToolsetComparePanel panel = new JToolsetComparePanel(os);
        tab.add(panel);
        pOsPanels.add(panel);
      }

      JButton btns[] = super.initUI("", tab, null, null, null, "Close");

      updateToolsets(null, null);   
      pack();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the left toolset. 
   */ 
  public String
  getLeftToolsetName() 
  {
    return pLeftToolsetName;
  }

  /**
   * Get the name of the right toolset. 
   */ 
  public String
  getRightToolsetName() 
  {
    return pRightToolsetName;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the toolsets being displayed in the left and right portions of the dialog.
   * 
   * @param left
   *   The versions of the toolset specialized for each operating system type.
   * 
   * @param right
   *   The versions of the toolset specialized for each operating system type.
   */ 
  public void 
  updateToolsets
  (
   TreeMap<OsType,Toolset> left,
   TreeMap<OsType,Toolset> right
  )
  { 
    pLeftToolsetName = null;
    if(left != null) {
      for(OsType os : left.keySet()) {
        if(pLeftToolsetName == null) 
          pLeftToolsetName = left.get(os).getName();
        else if(!pLeftToolsetName.equals(left.get(os).getName()))
          throw new IllegalArgumentException
            ("Somehow the (left) toolsets supplied for each OS didn't all have " +
             "the same name!");
      }
    }

    pRightToolsetName = null;
    if(right != null) {
      for(OsType os : right.keySet()) {
        if(pRightToolsetName == null) 
          pRightToolsetName = right.get(os).getName();
        else if(!pRightToolsetName.equals(right.get(os).getName()))
          throw new IllegalArgumentException
            ("Somehow the (right) toolsets supplied for each OS didn't all have " +
             "the same name!");
      }
    }

    if((pLeftToolsetName != null) && (pRightToolsetName != null)) {
      pHeaderLabel.setText("Compare Toolsets:  " + 
                           pLeftToolsetName + " (bundled) vs. " + 
                           pRightToolsetName + " (local)"); 
    }
    else {
      pHeaderLabel.setText("Compare Toolsets:  (none)");
    }

    for(OsType os : OsType.all()) {
      Toolset lset = null;
      if(left != null) 
        lset = left.get(os);

      Toolset rset = null;
      if(right != null) 
        rset = right.get(os);

      pOsPanels.get(os.ordinal()).updateToolsets(lset, rset); 
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1151815522118112981L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the toolsets being compared.
   */ 
  private String pLeftToolsetName;
  private String pRightToolsetName;

  /**
   * The OS specific toolset comparison panels.
   */ 
  private ArrayList<JToolsetComparePanel>  pOsPanels;

}
