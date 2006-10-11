// $Id: MasterExtensionConfigsTableModel.java,v 1.1 2006/10/11 22:45:41 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   M A S T E R   E X T E N S I O N   C O N F I G S   T A B L E   M O D E L                */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel} which contains a set of {@link MasterExtensionConfig} 
 * instances.
 */ 
public
class MasterExtensionConfigsTableModel
  extends BaseExtensionConfigsTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  MasterExtensionConfigsTableModel
  (
   JManageServerExtensionsDialog parent
  ) 
  {
    super("Master", parent);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the master extension configuration at the given row.
   */
  public MasterExtensionConfig
  getMasterExtensionConfig
  (
   int row
  ) 
  {
    return (MasterExtensionConfig) pConfigs.get(pRowToIndex[row]); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the master extension configurations which have been modified since the last update.
   */
  public TreeMap<String,MasterExtensionConfig> 
  getModifiedMasterExtensionConfigs() 
  {
    TreeMap<String,MasterExtensionConfig> table = new TreeMap<String,MasterExtensionConfig>();

    for(Integer idx : pModifiedIndices) {
      BaseExtensionConfig config = pConfigs.get(idx);
      table.put(config.getName(), (MasterExtensionConfig) config);
    }

    return table; 
  }

  /**
   * Get the fully specified master extension configurations.
   */
  public TreeMap<String,MasterExtensionConfig> 
  getMasterExtensionConfigs() 
  {
    TreeMap<String,MasterExtensionConfig> table = new TreeMap<String,MasterExtensionConfig>();

    for(BaseExtensionConfig config : pConfigs) 
      table.put(config.getName(), (MasterExtensionConfig) config);

    return table; 
  }

  /**
   * Set the underlying master extension configurations. <P> 
   * 
   * @param configs
   *   The extension configurations indexed by configuration name.
   * 
   * @param privileges
   *   The details of the administrative privileges granted to the current user. 
   */ 
  public void
  setMasterExtensionConfigs
  (
   TreeMap<String,MasterExtensionConfig> configs, 
   PrivilegeDetails privileges
  ) 
  {
    pConfigs.clear();    
    pModifiedIndices.clear();  

    for(MasterExtensionConfig config : configs.values()) 
      pConfigs.add(config);

    pPrivilegeDetails = privileges; 

    sort();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5771117695361630367L;

}
