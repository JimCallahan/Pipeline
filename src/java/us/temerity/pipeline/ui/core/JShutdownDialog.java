// $Id: JShutdownDialog.java,v 1.3 2005/03/28 04:17:12 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   S H U T D O W N   D I A L O G                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for server shutdown options.
 */ 
public 
class JShutdownDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JShutdownDialog() 
  {
    super("Shutdown", true);

    /* create dialog body components */ 
    {
      Box body = null;
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
	body = (Box) comps[2];
	
	pJobMgrsField = 
	  UIFactory.createTitledBooleanField
	  (tpanel, "Job Servers:", sTSize, 
	   vpanel, sVSize, 
	   "Shutdown all pljobmgr(1) daemons as well?");
	pJobMgrsField.setValue(true);

	UIFactory.addVerticalSpacer(tpanel, vpanel, 6);

	pPluginMgrField = 
	  UIFactory.createTitledBooleanField
	  (tpanel, "Plugin Manager:", sTSize, 
	   vpanel, sVSize, 
	   "Shutdown the plpluginmgr(1) daemon as well?");
	pPluginMgrField.setValue(true);

	UIFactory.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI("Shutdown Pipeline Servers?", true, body, "Yes", null, null, "No");
      pack();
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to command the queue manager to shutdown all job servers before exiting.
   */ 
  public boolean
  shutdownJobMgrs()
  {
    return pJobMgrsField.getValue();
  }

  /**
   * Whether to shutdown the plugin manager before exiting.
   */ 
  public boolean
  shutdownPluginMgr()
  {
    return pPluginMgrField.getValue();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4189100796087775748L;
  
  private static final int sTSize = 150;
  private static final int sVSize = 150;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to command the queue manager to shutdown all job servers before exiting.
   */
  private JBooleanField  pJobMgrsField; 

  /**
   * Whether to shutdown the plugin manager before exiting.
   */
  private JBooleanField  pPluginMgrField;

}
