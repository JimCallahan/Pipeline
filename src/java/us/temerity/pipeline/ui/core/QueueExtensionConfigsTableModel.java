// $Id: QueueExtensionConfigsTableModel.java,v 1.1 2006/10/11 22:45:41 jim Exp $

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
/*   Q U E U E   E X T E N S I O N   C O N F I G S   T A B L E   M O D E L                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel} which contains a set of {@link QueueExtensionConfig} 
 * instances.
 */ 
public
class QueueExtensionConfigsTableModel
  extends BaseExtensionConfigsTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  QueueExtensionConfigsTableModel
  (
   JManageServerExtensionsDialog parent
  ) 
  {
    super("Queue", parent);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the queue extension configuration at the given row.
   */
  public QueueExtensionConfig
  getQueueExtensionConfig
  (
   int row
  ) 
  {
    return (QueueExtensionConfig) pConfigs.get(pRowToIndex[row]); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the queue extension configurations which have been modified since the last update.
   */
  public TreeMap<String,QueueExtensionConfig> 
  getModifiedQueueExtensionConfigs() 
  {
    TreeMap<String,QueueExtensionConfig> table = new TreeMap<String,QueueExtensionConfig>();

    for(Integer idx : pModifiedIndices) {
      BaseExtensionConfig config = pConfigs.get(idx);
      table.put(config.getName(), (QueueExtensionConfig) config);
    }

    return table; 
  }

  /**
   * Get the fully specified queue extension configurations.
   */
  public TreeMap<String,QueueExtensionConfig> 
  getQueueExtensionConfigs() 
  {
    TreeMap<String,QueueExtensionConfig> table = new TreeMap<String,QueueExtensionConfig>();

    for(BaseExtensionConfig config : pConfigs) 
      table.put(config.getName(), (QueueExtensionConfig) config);

    return table; 
  }

  /**
   * Set the underlying queue extension configurations. <P> 
   * 
   * @param configs
   *   The extension configurations indexed by configuration name.
   * 
   * @param privileges
   *   The details of the administrative privileges granted to the current user. 
   */ 
  public void
  setQueueExtensionConfigs
  (
   TreeMap<String,QueueExtensionConfig> configs, 
   PrivilegeDetails privileges
  ) 
  {
    pConfigs.clear();
    pModifiedIndices.clear();

    for(QueueExtensionConfig config : configs.values()) 
      pConfigs.add(config);

    pPrivilegeDetails = privileges; 

    sort();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7454971971306328493L;

}
