package us.temerity.pipeline.builder.ui;

import java.awt.event.*;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import us.temerity.pipeline.LogMgr;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;
import us.temerity.pipeline.builder.BaseBuilder;
import us.temerity.pipeline.builder.BaseUtil;
import us.temerity.pipeline.builder.BaseBuilder.ConstructPass;
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
  implements ActionListener, TreeSelectionListener
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
    
    pRunning = false;
    
    pLoop = 1;
  }
  
  public void
  initUI()
    throws PipelineException
  {
    String header = pBuilder.getNameUI();
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

    pTopPanel.setupListeners();
    
    if (pTopPanel.allParamsReady())
      firstPassEnableButtons();
    else
      disableAllButtons();
    
    this.validate();
    this.pack();
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

  /*-- WINDOW LISTENER METHODS -------------------------------------------------------------*/

  /**
   * Invoked when the user attempts to close the window from the window's system menu.
   */ 
  public void 	
  windowClosing
  (
    @SuppressWarnings("unused")
    WindowEvent e
  ) 
  {
    doCancel();
  }
  
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
      runNextPass();
    else if(cmd.equals("run-all-passes"))
      runAllPasses();
    else if(cmd.equals("cancel")) 
      doCancel();
  }
  
  /*-- TREE SELECTION LISTENER METHODS -----------------------------------------------------*/
  
  /**
   * Called whenever the value of the selection changes.
   */ 
  public void 
  valueChanged
  (
    @SuppressWarnings("unused")
    TreeSelectionEvent e
  )
  {
    if (pTopPanel.allParamsReady())
      firstPassEnableButtons();
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
    if (pLoop == 1 || pLoop == 3)
      quit();
    else if (pRunning == false)
      handleException(new PipelineException("Execution halted by user!"));
    else
      pAbort = true;
  }
  
  /**
   * Cancel changes and close.
   */ 
  public void 
  doNextPass()
  {
    disableAllButtons();
    JBuilderParamPanel panel = pTopPanel.getCurrentBuilderParamPanel();
    panel.disableAllComponents();
    panel.assignValuesToBuilder();
    new RunSetupPassTask().start();
  }
  
  private void runAllPasses()
  {
    pRunning = true;
    disableAllButtons();
    new RunAllConstructPassTask().start();
  }

  private void runNextPass()
  {
    pRunning = true;
    disableAllButtons();
    new RunOneConstructPassTask().start();
  }

  
  private void
  quit()
  {
    BaseUtil.disconnectClients();
    System.exit(1);
  }
  
  private void
  finish()
  {
    disableAllButtons();
    LogMgr.getInstance().logAndFlush(Kind.Ops, Level.Info, "Execution is now complete.");
    pBuilder.new CheckinTask().start();
  }
  
  public void
  reallyFinish()
  {
    if (pAbort)
      quit();
    pLoop = 3;    
  }
  
  public void
  handleException
  (
    PipelineException ex  
  )
  {
    ex.printStackTrace();
    //System.exit(1);
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private
  class NextParameterPassTask
    extends Thread
  {
    public void 
    run()
    {
      try {
	pTopPanel.addNextSetupPass();
	if (pTopPanel.allParamsReady())
	  firstPassEnableButtons();
      }
      catch (PipelineException ex) {
	handleException(ex);
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
	handleException(ex);
      }
      if (pMorePasses)
	SwingUtilities.invokeLater(new NextParameterPassTask());
      else {
	disableAllButtons();
	pLoop = 2;
	pBuilder.new ExecutionOrderThread().start();
      }
    }
  }
  
  public
  class PrepareConstructPassesTask
    extends Thread
  {
    @Override
    public void 
    run()
    {
      pExecutionOrder = new LinkedList<ConstructPass>(pBuilder.getExecutionOrder());
      pTopPanel.prepareConstructLoop(pExecutionOrder);
      secondPassEnableButtons();
    }
  }
  
  private 
  class RunAllConstructPassTask
    extends Thread
  {
    @Override
    public void 
    run()
    {
      LinkedList<ConstructPass> run = new LinkedList<ConstructPass>(pExecutionOrder);
      for (ConstructPass pass : run) {
	try {
	  pass.run();
	  pExecutionOrder.remove(pass);
	  if (pAbort)
	    throw new PipelineException("Execution halted by user!");
	  SwingUtilities.invokeLater(new DuringAllConstructPassTask());
	}
	catch (PipelineException ex) {
	  handleException(ex);
	}
      }
      pRunning =  false;
      SwingUtilities.invokeLater(new AfterAllConstructPassTask());
    }
  }
  
  private
  class DuringAllConstructPassTask
    extends Thread
  {
    @Override
    public void 
    run()
    {
      pTopPanel.makeNextActive();
    }
  }
  
  private
  class AfterAllConstructPassTask
    extends Thread
  {
    @Override
    public void 
    run()
    {
      finish();
    }
  }
  
  private 
  class RunOneConstructPassTask
    extends Thread
  {
    @Override
    public void 
    run()
    {
      ConstructPass pass = pExecutionOrder.poll();
      if (pass != null)
	try {
	  pass.run();
	  SwingUtilities.invokeLater(new AfterOneConstructPassTask());
	}
	catch (PipelineException ex) {
	  handleException(ex);
	}
    }
  }
  
  private
  class AfterOneConstructPassTask
    extends Thread
  {
    @Override
    public void 
    run()
    {
      if (pAbort)
	handleException(new PipelineException("Execution halted by user!"));
      pRunning = false;
      pTopPanel.makeNextActive();
      if (!pExecutionOrder.isEmpty())
	secondPassEnableButtons();
      else
	finish();
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
  
  private boolean pAbort;
  
  private int pLoop;
  
  private JBuilderTopPanel pTopPanel;
  
  private LinkedList<ConstructPass> pExecutionOrder;
  
  private boolean pRunning;
}
