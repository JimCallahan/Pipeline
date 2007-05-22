/**
 * 
 */
package us.temerity.pipeline.builder.ui;

import java.awt.event.*;

import javax.swing.*;

import us.temerity.pipeline.LogMgr;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;
import us.temerity.pipeline.builder.BaseBuilder;
import us.temerity.pipeline.builder.BaseUtil;
import us.temerity.pipeline.ui.JTopLevelDialog;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   P A R A M   D I A L O G                                                */
/*------------------------------------------------------------------------------------------*/

/**
 *
 */
public 
class JBuilderParamDialog
  extends JTopLevelDialog
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * 
   */
  public 
  JBuilderParamDialog
  (
    BaseBuilder builder
  )
  {
    super("Builder Parameters");
    
    pBuilder = builder;
    
    pLoop = 1;
  }
  
  public void
  initUI()
    throws PipelineException
  {
    //this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    String header = pBuilder.getName();
    String cancel = "Quit";
    String[][] extras = new String[3][2];
    extras[0][0] = "Next";
    extras[0][1] = "next-pass";
    extras[1][0] = "Run Next";
    extras[1][1] = "run-next-pass";
    extras[2][0] = "Run All";
    extras[2][1] = "run-all-passes";
    
    pTopPanel = new JBuilderTopPanel(pBuilder);
    JButton buttons[] = super.initUI(header, pTopPanel, null, null, extras, cancel);
    pNextButton = buttons[0];
    pRunNextButton = buttons[1];
    pRunAllButton = buttons[2];
    
    pRunNextButton.setEnabled(false);
    pRunAllButton.setEnabled(false);
    
    this.validate();
    this.pack();
    pTopPanel.setLeftSplitDivider(.5);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   B U T T O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  private void
  disableAllButtons()
  {
    pNextButton.setEnabled(false);
    pRunNextButton.setEnabled(false);
    pRunAllButton.setEnabled(false);
  }
  
  private void
  firstPassEnableButtons()
  {
    pNextButton.setEnabled(true);
    pRunNextButton.setEnabled(false);
    pRunAllButton.setEnabled(false);
  }
  
  private void
  secondPassEnableButtons()
  {
    pNextButton.setEnabled(false);
    pRunNextButton.setEnabled(true);
    pRunAllButton.setEnabled(true);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    String cmd = e.getActionCommand();
    if(cmd.equals("next-pass")) 
      doNextPass();
    else if(cmd.equals("run-next-pass")) 
      ;
    else if(cmd.equals("run-all-passes"))
      ;
    else if(cmd.equals("cancel")) 
      doCancel();
  }
  
  /*-- WINDOW LISTENER METHODS -------------------------------------------------------------*/
  
  /**
   * Invoked when the user attempts to close the window from the window's system menu.
   */ 
  public void 	
  windowClosing
  (
   WindowEvent e
  ) 
  {
    doCancel();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Cancel changes and close.
   */ 
  public synchronized void 
  doCancel()
  {
    if (pLoop == 1)
      quit();
    else if (pLoop == 2)
      pBuilder.setAbort(true);
  }
  
  /**
   * Cancel changes and close.
   */ 
  public void 
  doNextPass()
  {
    LogMgr.getInstance().logAndFlush(Kind.Ops, Level.Warning, "The next button was pressed");
    disableAllButtons();
    new RunSetupPassTask().start();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private void
  quit()
  {
    BaseUtil.disconnectClients();
    System.exit(1);
  }
  
  private
  class NextParameterPassTask
    extends Thread
  {
    public void 
    run()
    {
      try {
	pTopPanel.addNextSetupPass();
	firstPassEnableButtons();
      }
      catch (PipelineException ex) {
	ex.printStackTrace();
	//TODO handle exception with a callback to GUI
      }
    }
  }
  
  private
  class RunSetupPassTask
    extends Thread
  {
    @Override
    public void 
    run()
    {
      try {
	pBuilder.runNextSetupPass();
	pMorePasses = pBuilder.getNextSetupPass(false);
      }
      catch (PipelineException ex) {
	ex.printStackTrace();
	//TODO handle exception with a callback to GUI 
      }
      if (pMorePasses)
	SwingUtilities.invokeLater(new NextParameterPassTask());
      else
	; //Need to move onto the second loop now.
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 1418949118885334316L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private JButton pNextButton;
  private JButton pRunNextButton;
  private JButton pRunAllButton;
  
  private boolean pMorePasses;
  
  private BaseBuilder pBuilder;
  
  private int pLoop;
  
  private JBuilderTopPanel pTopPanel;
  
}
