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
import us.temerity.pipeline.builder.BaseBuilder.ConstructPass;
import us.temerity.pipeline.ui.*;

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
    
    pPhase = GUIPhase.SetupPass;
  }
  
  public void
  initUI()
    throws PipelineException
  {
    String header = pBuilder.getNameUI();
    String cancel = "Abort";
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
    pNextActionButton = buttons[1];
    pRunAllButton = buttons[2];

    pTopPanel.setupListeners();
    
    if (pTopPanel.allParamsReady())
      setupPhaseEnableButtons();
    else
      disableAllButtons();
    
    this.validate();
    this.pack();
    this.setSize(getWidth(), 640);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   B U T T O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  private void
  disableAllButtons()
  {
    pNextButton.setEnabled(false);
    pNextActionButton.setEnabled(false);
    pRunAllButton.setEnabled(false);
  }
  
  private void
  disableQuitButton()
  {
    pCancelButton.setEnabled(false);
  }
  
  private void
  enableQuitButton()
  {
   pCancelButton.setEnabled(true); 
  }
  
  private void
  setupPhaseEnableButtons()
  {
    pNextButton.setEnabled(true);
    pNextActionButton.setEnabled(false);
    pRunAllButton.setEnabled(false);
  }
  
  private void
  constructPhaseEnableButtons()
  {
    pNextButton.setEnabled(false);
    pNextActionButton.setEnabled(true);
    pRunAllButton.setEnabled(true);
  }
  
  private void
  queuePhaseEnableButtons()
  {
    pNextActionButton.setText("Queue");
    pNextActionButton.setActionCommand("queue");
    pNextButton.setEnabled(false);
    pNextActionButton.setEnabled(true);
    pRunAllButton.setEnabled(false);
  }
  
  private void
  checkinPhaseEnableButtons()
  {
    pNextActionButton.setText("Finalize");
    pNextActionButton.setActionCommand("check-in");
    pNextButton.setEnabled(false);
    pNextActionButton.setEnabled(true);
    pRunAllButton.setEnabled(false);
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- WINDOW LISTENER METHODS -------------------------------------------------------------*/

  /**
   * Invoked when the user attempts to close the window from the window's system menu.
   */ 
  @Override
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
  @Override
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
    else if(cmd.equals("queue"))
      doQueue();
    else if(cmd.equals("check-in"))
      doCheckin();
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
      setupPhaseEnableButtons();
  }
  
  
    
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Cancel changes and close.
   */ 
  @Override
  public synchronized void 
  doCancel()
  {
    if (pPhase == GUIPhase.Finished)
      quit(0);
    else if (pPhase == GUIPhase.SetupPass || pPhase == GUIPhase.Error)
      quit(1);
    else if (pRunning == false)
      handleException(new PipelineException("Execution halted by user!"));
    else
      pAbort = true;
  }
  
  /**
   * 
   */ 
  public void 
  doNextPass()
  {
    disableAllButtons();
    LinkedList<JBuilderParamPanel> panels = pTopPanel.getCurrentBuilderParamPanels();
    for (JBuilderParamPanel panel : panels) {
      panel.disableAllComponents();
      panel.assignValuesToBuilder();
    }
    new RunSetupPassTask().start();
  }
  
  private void 
  runAllPasses()
  {
    pRunning = true;
    disableAllButtons();
    new RunAllConstructPassTask().start();
  }

  private void 
  runNextPass()
  {
    pRunning = true;
    disableAllButtons();
    new RunOneConstructPassTask().start();
  }

  private void
  quit
  (
    int exitCode  
  )
  {
    if (pBuilder.terminateAppOnQuit()) {
      pTopPanel.disconnect();
      System.exit(exitCode);
    } else {
      this.setVisible(false);
    }
  }
  
  private void
  doQueue()
  {
    disableAllButtons();
    pBuilder.new QueueThread().start();
  }
  
  private void
  doCheckin()
  {
    disableAllButtons();
    pCancelButton.setText("Quit");
    pBuilder.new CheckinTask().start();
  }
  
  public void
  afterQueue(boolean didJobsFinish)
  {
    if (didJobsFinish) {
      pPhase = GUIPhase.Checkin;
      checkinPhaseEnableButtons();
      enableQuitButton();
    }
    else
      handleException(new PipelineException("The jobs did not complete successfully."));
    //TODO this should really be another handler that allows you to retry queuing or try the afterQueue method again.
    
  }
  
  private void
  queuePrep()
  {
    LogMgr.getInstance().logAndFlush(Kind.Ops, Level.Info, "Building is now complete.");
    pPhase = GUIPhase.Queue;
    queuePhaseEnableButtons();
    disableQuitButton();
  }
  
  public void
  reallyFinish()
  {
    LogMgr.getInstance().logAndFlush(Kind.Ops, Level.Info, "Everything is now complete.");
    if (pAbort)
      quit(0);
    pPhase = GUIPhase.Finished;
  }
  
  public void
  handleException
  (
    PipelineException ex  
  )
  {
    boolean releaseNodes = pBuilder.releaseOnError();  
    String message = "The builder did not finish succesfully.\n";
    message += ex.getMessage() + "\n";
    pPhase = GUIPhase.Error;
    pCancelButton.setText("Quit");
    if (releaseNodes) {
      message += "The builder is set to release all registered nodes.  " +
      		 "Do you wish to release these nodes?\n";
      JConfirmDialog dialog = new JConfirmDialog(this, "Release Nodes?", message);
      dialog.setVisible(true);
      if (dialog.wasConfirmed()) {
	disableAllButtons();
	disableQuitButton();
	new ReleaseNodesTask().start();
      }
      else {
	enableQuitButton();
      }
    }
    else {
      disableAllButtons();
      JErrorDialog dialog = new JErrorDialog(this);
      dialog.setMessage("Execution halted", message);
      dialog.setVisible(true);
    }
  }
  
  private void
  handleErrorReleasing
  (
    PipelineException ex
  )
  {
    JErrorDialog dialog = new JErrorDialog(this);
    dialog.setMessage("Error Releasing Nodes", ex.getMessage());
    dialog.setVisible(true);
    disableAllButtons();
    pCancelButton.setEnabled(true);
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private
  class NextParameterPassTask
    extends Thread
  {
    @Override
    public void 
    run()
    {
      try {
	pTopPanel.addNextSetupPass();
	if (pTopPanel.allParamsReady())
	  setupPhaseEnableButtons();
	else
	  disableAllButtons();
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
	pPhase = GUIPhase.ConstructPass;
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
      constructPhaseEnableButtons();
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
      boolean error = false;
      for (ConstructPass pass : run) {
	if (error)
	  break;
	try {
	  pass.run();
	  pExecutionOrder.remove(pass);
	  if (pAbort)
	    throw new PipelineException("Execution halted by user!");
	  SwingUtilities.invokeLater(new DuringAllConstructPassTask());
	}
	catch (PipelineException ex) {
	  handleException(ex);
	  error = true;
	}
	catch (Exception ex) {
	  ex.printStackTrace();
	  handleException(new PipelineException(ex));
	  error = true;
	}
      }
      pRunning =  false;
      if (!error)
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
      queuePrep();
      doQueue();
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
	catch (Exception ex) {
	  handleException(new PipelineException(ex));
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
	constructPhaseEnableButtons();
      else
	queuePrep();
    }
  }
  
  private
  class ReleaseNodesTask
    extends Thread
  {
    @Override
    public void 
    run()
    {
      try {
	LogMgr.getInstance().log(Kind.Ops, Level.Warning, 
	  "All the nodes that were registered will now be released.");
	pTopPanel.releaseNodes();
	SwingUtilities.invokeLater(new AfterReleaseNodesTask());
      }
      catch (PipelineException ex) {
	SwingUtilities.invokeLater(new ProblemReleaseNodesTask(ex));
      }    
    }
  }
  
  private
  class AfterReleaseNodesTask
    extends Thread
  {
    @Override
    public void 
    run()
    {
      LogMgr.getInstance().log(Kind.Ops, Level.Info, 
        "Finished releasing all the nodes.");
      enableQuitButton();
    }
  }
  
  private
  class ProblemReleaseNodesTask
    extends Thread
  {
    public ProblemReleaseNodesTask
    (
      PipelineException ex
    )
    {
      pException = ex;
    }
    
    @Override
    public void 
    run()
    {
      handleErrorReleasing(pException);
    }
    private PipelineException pException;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 1418949118885334316L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private JButton pNextButton;
  private JButton pNextActionButton;
  private JButton pRunAllButton;
  
  private boolean pMorePasses;
  
  private BaseBuilder pBuilder;
  
  private boolean pAbort;
  
  private JBuilderTopPanel pTopPanel;
  
  private LinkedList<ConstructPass> pExecutionOrder;
  
  private boolean pRunning;
  
  private GUIPhase pPhase;
  
  enum GUIPhase
  {
    SetupPass, ConstructPass, Queue, Checkin, Finished, Error
  }
}
