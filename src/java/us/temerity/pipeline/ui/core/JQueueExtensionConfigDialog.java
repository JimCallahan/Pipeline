// $Id: JQueueExtensionConfigDialog.java,v 1.3 2009/03/24 01:21:21 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   E X T E N S I O N   C O N F I G   D I A L O G                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Create/modify a queue manager extension configuration.
 */ 
public 
class JQueueExtensionConfigDialog
  extends JBaseExtensionConfigDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JQueueExtensionConfigDialog
  (
   Frame owner
  )
  {
    super(owner, "Queue"); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new extension configuration.
   */ 
  public void
  newQueueExtensionConfig() 
  {
    setQueueExtensionConfig(null);
  }

  /**
   * Set the extension configuration.
   */ 
  public void
  setQueueExtensionConfig
  (
   QueueExtensionConfig config
  ) 
  {
    pExtension = null;
    if(config != null) {
      try {
	pExtension = config.getQueueExt(); 
      }
      catch(PipelineException ex) {
      }
    }
    
    super.setExtensionConfig(config);
  }

  /**
   * Get the new extension configuration.
   */ 
  public QueueExtensionConfig
  getQueueExtensionConfig() 
  {
    String cname = getConfigName();
    BaseQueueExt ext = (BaseQueueExt) getExtension();
    if((cname == null) || (pToolset == null) || (ext == null)) 
      return null;

    return new QueueExtensionConfig(cname, pToolset, ext, pIsEnabled); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create the field for editing the server extension plugin.
   */ 
  @Override
  protected JPluginSelectionField
  createExtPluginField()
  {
    UIMaster master = UIMaster.getInstance(); 
    master.clearExtPluginCaches();
    return master.createQueueExtSelectionField(getChannel(), sVSize);
  }
  
  /**
   * Create the field for editing the server extension plugin.
   */ 
  @Override
  protected void 
  updateExtPluginField()
  {
    UIMaster master = UIMaster.getInstance(); 
    master.clearExtPluginCaches();
    master.updateQueueExtPluginField(pToolset, pExtensionField);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get a new instance of the given extension plugin.
   */ 
  @Override
  protected BaseExt
  newExtPlugin
  (
   String ename, 
   VersionID evid, 
   String evendor
  )
    throws PipelineException
  {
    return PluginMgrClient.getInstance().newQueueExt(ename, evid, evendor);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4919043782110648561L;

}
