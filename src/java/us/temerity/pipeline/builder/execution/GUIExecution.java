// $Id: GUIExecution.java,v 1.4 2008/03/04 08:15:15 jesse Exp $

package us.temerity.pipeline.builder.execution;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BaseBuilder.*;
import us.temerity.pipeline.builder.ui.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   G U I   E X E C U T I O N                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Gui execution for builders.
 */
public 
class GUIExecution
  extends BaseBuilderExecution
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Default constructor
   * 
   * @param builder
   *   The Builder to execute.
   *   
   * @throws PipelineException
   *   If <code>null</code> is passed in for the Builder.
   */
  public
  GUIExecution
  (
    BaseBuilder builder  
  )
    throws PipelineException
  {
    super(builder);
    pRunning = false;
  }

  @Override
  protected void 
  handleException
  (
    Exception ex
  )
  {
    ExecutionPhase phase = getPhase();
    if (phase != ExecutionPhase.Release && phase != ExecutionPhase.Error) {
      String header = "An error occured during Builder Execution.";
    
      if (getRunningBuilder() != null) {
        if (getRunningBuilder().releaseOnError()) {
          if (phase.haveNodesBeenMade())
            header += "\nAll registered nodes are scheduled to be released and " +
                      "will be if (Yes) is pressed.";
          else if (phase.isEndingPhase())
            header += "\nSince the check-in operation has begun, the Builder will not attempt to " +
                      "release registered nodes.";
        }
      }
      
      else
        if (phase.haveNodesBeenMade())
          header +=
            "\nThe registered nodes are not scheduled to be released.  " +
            "Do you wish to release them anyway?";
      
      setPhase(ExecutionPhase.Error);

      String message;
      if (ex instanceof PipelineException)
        message = header + "\n" + ex.getMessage();
      else
        message = Exceptions.getFullMessage(header, ex);
      pLog.logAndFlush(Kind.Ops, Level.Severe, message);
      if (getBuilder().releaseOnError() && phase.haveNodesBeenMade()) {
        SwingUtilities.invokeLater(pDialog.new AskAboutReleaseTask(message));
      }
      if (phase == ExecutionPhase.SetupPass)
        SwingUtilities.invokeLater(pDialog.new ShowErrorTask(message));
    }
    else if (phase == ExecutionPhase.Release) {
      String header = 
        "Additionally, an error occured while attempting to release the nodes";
      String message = Exceptions.getFullMessage(header, ex);
      pLog.logAndFlush(Kind.Ops, Level.Severe, message);
    }
    else if (phase == ExecutionPhase.Error) {
      String header = 
        "Additionally, another error while dealing with the first error. ";
      String message = Exceptions.getFullMessage(header, ex);
      pLog.logAndFlush(Kind.Ops, Level.Severe, message);
    }
  }

  @Override
  public void 
  run()
  {
    try {
      SwingUtilities.invokeLater(new BuilderGuiThread());
    } 
    catch (Exception ex) {
      handleException(ex);  
    }
  }
  
  public void
  viewGui()
  {
    try {
      SwingUtilities.invokeLater(new BuilderGuiViewThread());
    }
    catch(Exception ex) {
      handleException(ex);
    }
  }
  
  private 
  enum ButtonState
  {
    DisableAll,
    DisableQuit,
    EnableQuit,
    SetupPhaseEnable,
    ConstructPhaseEnable
  }
  
  /**
   *
   */
  public 
  class JBuilderDialog
  extends JTopLevelDialog
  implements ActionListener, TreeSelectionListener, ChangeListener
  {

    /**
     * Constructor
     */
    private 
    JBuilderDialog()
    {
      super("Builder Parameters");

      pRunning = false;

    }

    public void
    initUI()
    {
      String header = "Node Builder:  " + getBuilder().getNameUI();
      String cancel = "Abort";
      String[][] extras = new String[3][2];
      extras[0][0] = "Next";
      extras[0][1] = "next-pass";
      extras[1][0] = "Run Next";
      extras[1][1] = "run-next-pass";
      extras[2][0] = "Run All";
      extras[2][1] = "run-all-passes";

      pTopPanel = new JBuilderTopPanel(getBuilder(), this);
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
    }



    /*---------------------------------------------------------------------------------------*/
    /*   B U T T O N S                                                                       */
    /*---------------------------------------------------------------------------------------*/

    private void
    disableAllButtons()
    {
      pNextButton.setEnabled(false);
      pNextActionButton.setEnabled(false);
      pRunAllButton.setEnabled(false);
    }

    private void
    disableCancelButton()
    {
      pCancelButton.setEnabled(false);
    }

    private void
    enableCancelButton()
    {
      pCancelButton.setEnabled(true); 
    }
    
    private void
    makeQuitButton()
    {
      pCancelButton.setText("Quit"); 
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



    /*--------------------------------------------------------------------------------------*/
    /*   L I S T E N E R S                                                                  */
    /*--------------------------------------------------------------------------------------*/

    /*-- WINDOW LISTENER METHODS -----------------------------------------------------------*/

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

    /*-- ACTION LISTENER METHODS -----------------------------------------------------------*/

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
    }

    /*-- TREE SELECTION LISTENER METHODS ---------------------------------------------------*/

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
      if (pTopPanel.allParamsReady() && getPhase() == ExecutionPhase.SetupPass &&
          peekNextSetupPass() != null)
        setupPhaseEnableButtons();
    }

    /*-- CHANGE LISTENER METHODS -------------------------------------------------------------*/
    
    public void 
    stateChanged
    (
      @SuppressWarnings("unused")
      ChangeEvent e
    )
    {
      if (pTopPanel.allParamsReady() && getPhase() == ExecutionPhase.SetupPass &&
          peekNextSetupPass() != null)
        setupPhaseEnableButtons();
    }


    /*---------------------------------------------------------------------------------------*/
    /*   A C T I O N S                                                                       */
    /*---------------------------------------------------------------------------------------*/

    /**
     * Cancel changes and close.
     */ 
    @Override
    public synchronized void 
    doCancel()
    {
      ExecutionPhase phase = getPhase();
      if (phase == ExecutionPhase.Finished)
        quit(0);
      else if (phase == ExecutionPhase.SetupPass || phase == ExecutionPhase.Error)
        quit(1);
      else if (!pRunning)
        handleException(new PipelineException("Execution halted by user!"));
      else {
        disableCancelButton();
        pAbort = true;
      }
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
      if (getBuilder().terminateAppOnQuit()) {
        pTopPanel.disconnect();
        System.exit(exitCode);
      } 
      else {
        this.setVisible(false);
      }
    }

    private
    class AdjustButtonsTask
    extends Thread
    {

      AdjustButtonsTask
      (
        ButtonState buttonState  
      )
      {
        pButtonState = buttonState;
      }

      @Override
      public void 
      run()
      {
        switch(pButtonState) {
        case ConstructPhaseEnable:
          constructPhaseEnableButtons();
          break;
        case DisableAll:
          disableAllButtons();
          break;
        case DisableQuit:
          disableCancelButton();
          break;
        case EnableQuit:
          enableCancelButton();
          break;
        case SetupPhaseEnable:
          setupPhaseEnableButtons();
          break;
        }
      }

      private ButtonState pButtonState;
    }

    private
    class NextParameterPassTask
    extends Thread
    {
      @Override
      public void 
      run()
      {
        try {
          initNextSetupPass();
          SetupPassBundle bundle = peekNextSetupPass();
          pTopPanel.addNextSetupPass(bundle.getPass(), bundle.getOwningBuilder());
          if (pTopPanel.allParamsReady())
            setupPhaseEnableButtons();
          else
            disableAllButtons();
        }
        catch (Exception ex) {
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
        try
        {
          if (pAbort)
            throw new PipelineException("Execution halted by user!");
          if (peekNextConstructPass() != null) {
            pTopPanel.makeNextActive();
            constructPhaseEnableButtons();
          }
          else
            SwingUtilities.invokeLater(new FinishedTask());
        }
        catch (Exception ex) {
          handleException(ex);
        }
      }
    }

    private
    class FinishedTask
    extends Thread
    {
      @Override
      public void
      run()
      {
        setPhase(ExecutionPhase.Finished);
        disableAllButtons();
        makeQuitButton();
        pLog.logAndFlush(Kind.Ops, Level.Info, "Execution is now complete");
      }
    }

    private
    class PrepareConstructPassesTask
    extends Thread
    {
      @Override
      public void 
      run()
      {
        try {
          pTopPanel.prepareConstructLoop(getExecutionOrderNames());
          constructPhaseEnableButtons();
        }
        catch (Exception ex) {
          handleException(PipelineException.getDetailedException(ex));
        }
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
        try {
          pTopPanel.makeNextActive();
        }
        catch (Exception ex) {
          handleException(ex);
        }
      }
    }

    private
    class AskAboutReleaseTask
    extends Thread
    {
      private 
      AskAboutReleaseTask
      (
        String message  
      )
      {
        pMessage = message;
      }

      @Override
      public void 
      run()
      {
        try {
          disableAllButtons();
          makeQuitButton();
          JConfirmDialog dialog = 
            new JConfirmDialog(pDialog, "Release Nodes", pMessage);
          dialog.setVisible(true);
          if (dialog.wasConfirmed()) {
            disableCancelButton();
            new ReleaseNodesTask().start();
          }
          else
            pLog.logAndFlush(Kind.Ops, Level.Severe, 
              "Builder execution has ended with an error.  " +
            "The created nodes were not cleaned up.");
        }
        catch (Exception ex) {
          handleException(ex);
        }
      }
      private String pMessage;
    }

    private
    class ShowErrorTask
    extends Thread
    {
      private ShowErrorTask
      (
        String message  
      )
      {
        pMessage = message;
      }

      @Override
      public void 
      run()
      {
        try {
          disableAllButtons();
          makeQuitButton();
          enableCancelButton();
          JErrorDialog dialog = new JErrorDialog(pDialog);
          dialog.setMessage("Error Dialog", pMessage);
          dialog.setVisible(true);
        }
        catch (Exception ex) {
          handleException(ex);
        }
      }
      private String pMessage;
    }



    /*--------------------------------------------------------------------------------------*/
    /*   S T A T I C   I N T E R N A L S                                                    */
    /*--------------------------------------------------------------------------------------*/

    private static final long serialVersionUID = 5795781998477466838L;



    /*--------------------------------------------------------------------------------------*/
    /*   I N T E R N A L S                                                                  */
    /*--------------------------------------------------------------------------------------*/

    private JButton pNextButton;
    private JButton pNextActionButton;
    private JButton pRunAllButton;

    private boolean pAbort;

    private JBuilderTopPanel pTopPanel;
  }

  /**
   * Thread for use with the Builder GUI code.
   */
  private
  class BuilderGuiThread
  extends Thread
  {
    @Override
    public void 
    run() 
    {  
      UIFactory.initializePipelineUI();
      try {
        pDialog = new JBuilderDialog();
        pDialog.initUI();
        pDialog.setVisible(true);
        new InitialSetupTask().start();
      }
      catch(Exception ex) {
        handleException(ex);
      }
    }
  }

  private
  class BuilderGuiViewThread
  extends Thread
  {
    @Override
    public void 
    run() 
    {  
      UIFactory.initializePipelineUI();
      try {
        pDialog = new JBuilderDialog();
        pDialog.initUI();
        pDialog.setVisible(true);
      }
      catch(Exception ex) {
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
      boolean morePasses = false;
      try {
        executeNextSetupPass();
        morePasses = (peekNextSetupPass() != null);
        if (morePasses)
          SwingUtilities.invokeLater(pDialog.new NextParameterPassTask());
        else {
          SwingUtilities.invokeAndWait(pDialog.new AdjustButtonsTask(ButtonState.DisableAll));
          checkActions();
          buildSecondLoopExecutionOrder();
          SwingUtilities.invokeLater(pDialog.new PrepareConstructPassesTask());
        }
      }
      catch (Exception ex) {
        handleException(ex);
      }
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
      boolean error = false;
      while (peekNextConstructPass() != null) {
        if (error)
          break;
        try {
          pRunning = true;
          executeNextConstructPass();
          pRunning = false;
          SwingUtilities.invokeAndWait(pDialog.new DuringAllConstructPassTask());
        }
        catch (Exception ex) {
          handleException(ex);
          error = true;
        }
      }
      if (!error)
        SwingUtilities.invokeLater(pDialog.new FinishedTask());
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
      BaseConstructPass pass = peekNextConstructPass();
      if (pass != null)
        try {
          pRunning = true;
          executeNextConstructPass();
          pRunning = false;
          SwingUtilities.invokeLater(pDialog.new AfterOneConstructPassTask());
        }
      catch (Exception ex) {
        handleException(PipelineException.getDetailedException(ex));
      }
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
        getRunningBuilder().releaseNodes();
        LogMgr.getInstance().log
        (Kind.Ops, Level.Info, "Finished releasing all the nodes.");
        SwingUtilities.invokeAndWait(pDialog.new AdjustButtonsTask(ButtonState.EnableQuit));
      }
      catch (Exception ex) {
        SwingUtilities.invokeLater(pDialog.new AdjustButtonsTask(ButtonState.EnableQuit));
        handleException(ex);
      }    
    }
  }

  private
  class InitialSetupTask
  extends Thread
  {
    @Override
    public void 
    run()
    {
      try {
        initialSetupPases();
        SwingUtilities.invokeLater(pDialog.new NextParameterPassTask());
      }
      catch (Exception ex) {
        handleException(ex);
      }    
    }
  }


 private JBuilderDialog pDialog;
 private boolean pRunning;
}
