// $Id: TestTool.java,v 1.2 2005/01/15 02:49:43 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T E S T   T O O L                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A testing tool.
 */
public
class TestTool
  extends BaseTool
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  TestTool()
  {
    super("Test", new VersionID("1.0.0"),
	  "A dummy tool strictly for testing tool plugins.");

    setHeaderText("A Test of Tools:");

    pField = new JPathField();
    setDialogBody(pField);

    underDevelopment();
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*  O P S                                                                                 */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Update the dialog components based on the current node selection.
   * 
   * @throws PipelineException 
   *   If unable to update the dialog components based on the current node selection. 
   */  
  public synchronized void 
  update() 
    throws PipelineException 
  {
    if(pPrimary != null) 
      pField.setText(pPrimary);
  }

  /**
   * Validate the node selections and any user input from the tool dialog components. <P> 
   * 
   * @throws PipelineException 
   *   If unable to validate the current node selection or user input from dialog components.
   */ 
  public synchronized void 
  validate() 
    throws PipelineException 
  {
    pNodeName = pField.getText();
    if((pNodeName == null) || (pNodeName.length() == 0))
      throw new PipelineException
	("Illegal node name given!");
  }

  /**
   * Execute the tool. <P> 
   * 
   * @param mclient
   *   The network connection to the plmaster(1) daemon.
   * 
   * @param qclient
   *   The network connection to the plqueuemgr(1) daemon.
   * 
   * @throws PipelineException 
   *   If unable to sucessfully execute the tool.
   */ 
  public void 
  execute
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {
    System.out.print("Test Tool:\n" + 
		     "  Node Name = " + pNodeName + "\n" + 
		     "  Primary = " + pPrimary + "\n");

    for(String name : pSelected.keySet()) 
      System.out.print("  Selected = " + name + "\n");

    System.out.print("\n");
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7830454485918743724L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * 
   */ 
  private JPathField pField;

  /**
   * The fully resolved name of a node.
   */ 
  private String  pNodeName; 


}


