// $Id: TestUIClientTool.java,v 1.1 2009/12/18 07:20:14 jim Exp $

package us.temerity.pipeline.plugin.TestUIClientTool.v2_4_18;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;


/*------------------------------------------------------------------------------------------*/
/*   C L E A N   U P   T O O L                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Analyses the nodes and associated jobs in a working area and optionally performs 
 * preparatory steps required for removal of the working area. 
 */
public 
class TestUIClientTool
  extends BaseTool
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  TestUIClientTool()
  {
    super("TestUIClient", new VersionID("2.4.18"), "Temerity",
          ""); 
    
    addPhase(new PhaseOne());

    underDevelopment(); 
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   P H A S E   O N E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private 
  class PhaseOne
    extends BaseTool.ToolPhase
  {
    public 
    PhaseOne() 
    {
      super();
    }
    
    /**
     * Just shows message.
     * 
     * @return 
     *   The phase progress message or <CODE>null</CODE> to abort early.
     * 
     * @throws PipelineException
     *   If unable to validate the given user input.
     */
    @Override
    public String
    collectInput() 
      throws PipelineException 
    {
      return ": Running Tests.";
    }
    
    /**
     * Search for unfinished jobs associated with the nodes in the working area...
     */
    @Override
    public NextPhase 
    execute
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      UIClient client = UIFactory.getUIClient(); 
      for(Path p : client.getSavedLayoutPaths()) 
        System.out.print("Layout = " + p + "\n"); 

      return NextPhase.Finish;
    }
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -621988252595856310L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/


}
