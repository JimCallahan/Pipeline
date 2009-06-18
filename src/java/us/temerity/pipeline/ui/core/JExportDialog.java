// $Id: JExportDialog.java,v 1.12 2009/06/18 08:42:52 jlee Exp $

package us.temerity.pipeline.ui.core;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.TreeSet;

import javax.swing.*;


import us.temerity.pipeline.NodeCommon;
import us.temerity.pipeline.NodeTreeComp;
import us.temerity.pipeline.ui.JBaseDialog;
import us.temerity.pipeline.ui.UIFactory;

/*------------------------------------------------------------------------------------------*/
/*   E X P O R T   D I A L O G                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Export node parameters dialog.                                                  
 */ 
public 
class JExportDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param owner
   *   The parent frame.
   */ 
  public 
  JExportDialog
  (
   int channel, 
   Frame owner
  ) 
  {
    super(owner, "Export");

    pChannel = channel;

    /* create dialog body components */ 
    {
      Box vbox = new Box(BoxLayout.Y_AXIS);

      {
	JExportPanel panel = 
	  new JExportPanel(pChannel, "Export All Properties:", sTSize, sVSize);
	pExportPanel = panel;

	vbox.add(panel);
      }

      vbox.add(UIFactory.createFiller(sTSize+sVSize+30));
      vbox.add(Box.createVerticalGlue());
      
      JScrollPane scroll = UIFactory.createVertScrollPane(vbox, sTSize+sVSize+52, 150);
	
      super.initUI("X", scroll, "Export", null, null, "Cancel");
    }  
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S                                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to export the toolset parameter.
   */ 
  public boolean 
  exportToolset() 
  {
    return pExportPanel.exportToolset(); 
  }
    
  /**
   * Whether to export the editor parameter.
   */ 
  public boolean 
  exportEditor() 
  {
    return pExportPanel.exportEditor(); 
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to export the regeneration action.
   */ 
  public boolean 
  exportAction() 
  {
    return pExportPanel.exportAction();  
  }
  
  /**
   * Whether to export whether the regeneration action is enabled.
   */ 
  public boolean 
  exportActionEnabled() 
  {
    return pExportPanel.exportActionEnabled();  
  }

  /**
   * Whether to export the single valued regeneration action parameter with the given name.
   * 
   * @param pname
   *   The name of the single valued action parameter.
   */ 
  public boolean 
  exportActionSingleParam
  (
   String pname
  ) 
  {
    return pExportPanel.exportActionSingleParam(pname); 
  }
  
  /**
   * Whether to export per-source regeneration action parameters.
   */ 
  public boolean 
  exportActionSourceParams() 
  {
    return pExportPanel.exportActionSourceParams(); 
  }

  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to export the overflow policy.
   */ 
  public boolean 
  exportOverflowPolicy() 
  {
    return pExportPanel.exportOverflowPolicy();  
  }
  
  /**
   * Whether to export the execution method.
   */ 
  public boolean 
  exportExecutionMethod() 
  {
    return pExportPanel.exportExecutionMethod();  
  }
  
  /**
   * Whether to export the batch size.
   */ 
  public boolean 
  exportBatchSize() 
  {
    return pExportPanel.exportBatchSize();  
  }
  
  /**
   * Whether to export the job priority.
   */ 
  public boolean 
  exportPriority() 
  {
    return pExportPanel.exportPriority();  
  }
  
  /**
   * Whether to export the ramp up interval.
   */ 
  public boolean
  exportRampUpInterval()
  {
    return pExportPanel.exportRampUpInterval();
  }
  
  /**
   * Whether to export the max system load.
   */ 
  public boolean 
  exportMaxLoad() 
  {
    return pExportPanel.exportMaxLoad();  
  }
  
  /**
   * Whether to export the minimum free memory.
   */ 
  public boolean 
  exportMinMemory() 
  {
    return pExportPanel.exportMinMemory();  
  }
  
  /**
   * Whether to export the minimum free disk space.
   */ 
  public boolean 
  exportMinDisk() 
  {
    return pExportPanel.exportMinDisk();  
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The names of the exported selection keys. 
   */ 
  public TreeSet<String> 
  exportedSelectionKeys() 
  {
    return pExportPanel.exportedSelectionKeys(); 
  }

  /**
   * The names of the exported license keys. 
   */ 
  public TreeSet<String> 
  exportedLicenseKeys() 
  {
    return pExportPanel.exportedLicenseKeys();  
  }

  /**
   * The names of the exported hardware keys. 
   */ 
  public TreeSet<String> 
  exportedHardwareKeys() 
  {
    return pExportPanel.exportedHardwareKeys();  
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to export the upstream link information to the given source node.
   */ 
  public boolean
  exportSource
  (
   String sname
  ) 
  {
    return pExportPanel.exportSource(sname); 
  }

  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to export the per-node annotation to the given source node.
   */ 
  public boolean
  exportNodeAnnotation
  (
   String sname
  ) 
  {
    return pExportPanel.exportNodeAnnotation(sname); 
  }
  
  /**
   * Whether to export the per-version annotation to the given source node.
   */ 
  public boolean
  exportVersionAnnotation
  (
   String sname
  ) 
  {
    return pExportPanel.exportVersionAnnotation(sname); 
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the selection fields.
   */ 
  public void 
  updateNode
  (
   NodeCommon node, 
   NodeTreeComp workingSources
  )
  { 
    pHeaderLabel.setText("Export:  " + node);
    pExportPanel.updateNode(node, workingSources);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -4818946288161690402L;
  
  private static final int sTSize = 150;
  private static final int sVSize = 240;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The index of the update channel.
   */ 
  private int  pChannel; 

  /**
   * The export fields panel. 
   */ 
  private JExportPanel  pExportPanel; 

}
