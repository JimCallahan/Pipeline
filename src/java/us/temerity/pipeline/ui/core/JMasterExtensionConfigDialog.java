// $Id: JMasterExtensionConfigDialog.java,v 1.2 2007/12/16 11:03:59 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   M A S T E R   E X T E N S I O N   C O N F I G   D I A L O G                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Create/modify a master manager extension configuration.
 */ 
public 
class JMasterExtensionConfigDialog
  extends JBaseExtensionConfigDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JMasterExtensionConfigDialog
  (
   Frame owner
  )
  {
    super(owner, "Master"); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new extension configuration.
   */ 
  public void
  newMasterExtensionConfig() 
  {
    setMasterExtensionConfig(null);
  }

  /**
   * Set the extension configuration.
   */ 
  public void
  setMasterExtensionConfig
  (
   MasterExtensionConfig config
  ) 
  {
    pExtension = null;
    if(config != null) {
      try {
	pExtension = config.getMasterExt(); 
      }
      catch(PipelineException ex) {
      }
    }
    
    super.setExtensionConfig(config);
  }

  /**
   * Get the new extension configuration.
   */ 
  public MasterExtensionConfig
  getMasterExtensionConfig() 
  {
    String cname = getConfigName();
    BaseMasterExt ext = (BaseMasterExt) getExtension();
    if((cname == null) || (pToolset == null) || (ext == null)) 
      return null;

    return new MasterExtensionConfig(cname, pToolset, ext, pIsEnabled); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create the field for editing the server extension plugin.
   */ 
  protected JPluginSelectionField
  createExtPluginField()
  {
    UIMaster master = UIMaster.getInstance(); 
    master.clearExtPluginCaches();
    return master.createMasterExtSelectionField(sVSize);
  }
  
  /**
   * Create the field for editing the server extension plugin.
   */ 
  protected void 
  updateExtPluginField()
  {
    UIMaster master = UIMaster.getInstance(); 
    master.clearExtPluginCaches();
    master.updateMasterExtPluginField(pToolset, pExtensionField);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get a new instance of the given extension plugin.
   */ 
  protected BaseExt
  newExtPlugin
  (
   String ename, 
   VersionID evid, 
   String evendor
  )
    throws PipelineException
  {
    return PluginMgrClient.getInstance().newMasterExt(ename, evid, evendor);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4103423556794887982L;

}
