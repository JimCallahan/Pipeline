// $Id: GUIExecution.java,v 1.18 2010/01/08 20:42:25 jesse Exp $

package us.temerity.pipeline.builder.execution;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.MultiMap.*;
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
  {
    super(builder);
    pRunning = false;
  }

  @Override
  protected void 
  handleException
  (
    Throwable ex
  )
  {
    ExecutionPhase phase = getPhase();
    if (phase != ExecutionPhase.Release && phase != ExecutionPhase.Error &&
        phase != ExecutionPhase.ReleaseView || phase != ExecutionPhase.Finished) {
      String header = "The builder was unable to successfully complete.";
      
      String message;
      if (ex instanceof PipelineException)
        message = header + "\n" + ex.getMessage();
      else
        message = Exceptions.getFullMessage(header, ex);

    
      if (getRunningBuilder() != null) {
        if (getRunningBuilder().releaseOnError()) {
          if (phase.haveNodesBeenMade())
            message+= "\nAll registered nodes are scheduled to be released and " +
                      "will be if (Yes) is pressed.";
          else if (phase.isEndingPhase())
            message += "\nSince the check-in operation has begun, the Builder will not attempt to " +
                      "release registered nodes.";
        }
      }
      else
        if (phase.haveNodesBeenMade())
          message +=
            "\nThe registered nodes are not scheduled to be released.  " +
            "Do you wish to release them anyway?";
      
      setPhase(ExecutionPhase.Error);

      pLog.logAndFlush(Kind.Ops, Level.Severe, message);
      if (getBuilder().releaseOnError() && phase.haveNodesBeenMade()) {
        SwingUtilities.invokeLater(pDialog.new AskAboutReleaseTask(message));
      }
      if (phase == ExecutionPhase.SetupPass)
        SwingUtilities.invokeLater(pDialog.new ShowErrorTask(message));
    }
    else if (phase == ExecutionPhase.Release) {
      String header = 
        "The builder was not able to successfully release the nodes: ";
      String message = Exceptions.getFullMessage(header, ex);
      pLog.logAndFlush(Kind.Ops, Level.Severe, message);
      setPhase(ExecutionPhase.Error);
      SwingUtilities.invokeLater(pDialog.new ShowErrorTask(message));
    }
    else if (phase == ExecutionPhase.ReleaseView) {
      String header = 
        "A problem occurred after execution when attempting to release the working area: ";
      String message = Exceptions.getFullMessage(header, ex);
      pLog.logAndFlush(Kind.Ops, Level.Severe, message);
      setPhase(ExecutionPhase.Error);
      SwingUtilities.invokeLater(pDialog.new ShowErrorTask(message));
    }
    else if (phase == ExecutionPhase.Error) {
      String header = 
        "An additional problem occurred while handling the first problem: ";
      String message = Exceptions.getFullMessage(header, ex);
      pLog.logAndFlush(Kind.Ops, Level.Severe, message);
      SwingUtilities.invokeLater(pDialog.new ShowErrorTask(message));
    }
    else if (phase == ExecutionPhase.Finished) {
      String header =
        "An error occurred when trying to relaunch the builder.";
      String message = Exceptions.getFullMessage(header, ex);
      pLog.logAndFlush(Kind.Ops, Level.Severe, message);
      SwingUtilities.invokeLater(pDialog.new ShowErrorTask(message));
    }
  }
  
  private void
  handleRelaunchException
  (
    Throwable ex  
  )
  {
    String header =
      "An error occurred when trying to relaunch the builder.";
    String message = Exceptions.getFullMessage(header, ex);
    pLog.logAndFlush(Kind.Ops, Level.Severe, message);
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
    catch (LinkageError er ) {
      handleException(er);
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
    catch (LinkageError er ) {
      handleException(er);
    }
  }
  
  private 
  enum ButtonState
  {
    DisableAll,
    DisableQuit,
    EnableQuit,
    SetupPhaseEnable,
    ConstructPhaseEnable,
    EnableRelaunchButton
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
      JButton buttons[] = initUIHelper(header, pTopPanel, null, null, extras, cancel);
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
    
    /**
     * Initialize the common user interface components. <P> 
     * 
     * The button title arguments <CODE>confirm</CODE>, <CODE>apply</CODE> and 
     * <CODE>cancel</CODE> may be <CODE>null</CODE> to omit the button(s) from the dialog. 
     * 
     * @param header
     *   The text displayed in the dialog header. 
     * 
     * @param body
     *   The component containing the body of the dialog.
     * 
     * @param confirm
     *   The title of the confirm button.
     * 
     * @param apply
     *   The title of the apply button.
     * 
     * @param extra
     *   An array of title/action-command strings pairs used to create extra buttons.
     * 
     * @param cancel
     *   The title of the cancel button.
     * 
     * @param extraHeader
     *   Optional text which will be displayed right justified in the dialog header.  Can only 
     *   be used if the header is not set to null.
     * 
     * @return 
     *   The array of created extra buttons or <CODE>null</CODE> if extra was <CODE>null</CODE>.
     */ 
    @SuppressWarnings("incomplete-switch")
    private JButton[]
    initUIHelper
    (
     String header, 
     JComponent body, 
     String confirm, 
     String apply, 
     String[][] extra,
     String cancel
    ) 
    {
      setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      addWindowListener(this);

      JPanel root = new JPanel();
      root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));  

      if(header != null) {
        JPanel panel = new JPanel();
        panel.setName("DialogHeader");    
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        
        {
          JLabel label = new JLabel(header);

          label.setName("DialogHeaderLabel");     

          panel.add(label);         
        }
        
        panel.add(Box.createHorizontalGlue());
        
        {
          JButton btn = new JButton();          
          pRelaunchButton = btn;
          btn.setName("RelaunchButton");
            
          btn.setSelected(true);
            
          Dimension size = new Dimension(19, 19);
          btn.setMinimumSize(size);
          btn.setMaximumSize(size);
          btn.setPreferredSize(size);
            
          btn.setActionCommand("relaunch");
          btn.addActionListener(this);
          
          btn.setEnabled(false);
            
          panel.add(btn);
        } 
          
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
        
        {
          JButton btn = new JButton();          
          pRelaunchOptionsButton = btn;
          btn.setName("RelaunchOptionsButton");
            
          btn.setSelected(true);
            
          Dimension size = new Dimension(19, 19);
          btn.setMinimumSize(size);
          btn.setMaximumSize(size);
          btn.setPreferredSize(size);
            
          btn.setActionCommand("relaunch-options");
          btn.addActionListener(this);
          
          btn.setEnabled(false);
            
          panel.add(btn);
        }
        
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
        
        {
          JButton btn = new JButton();          
          btn.setName("CMDLineButton");
            
          btn.setSelected(true);
            
          Dimension size = new Dimension(19, 19);
          btn.setMinimumSize(size);
          btn.setMaximumSize(size);
          btn.setPreferredSize(size);
            
          btn.setActionCommand("print-cmdline");
          btn.addActionListener(this);
          
          btn.setEnabled(true);
            
          panel.add(btn);
        }
        
        root.add(panel);
      }     
      
      if(body != null) 
        root.add(body);

      JButton[] extraBtns = null;
      {
        JPanel panel = new JPanel();
        
        String mac = "";
        switch(PackageInfo.sOsType) {
        case MacOS:
          mac = "Mac";
        }
        panel.setName(mac + ((body != null) ? "DialogButtonPanel" : "DialogButtonPanel2"));

        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        
        panel.add(Box.createHorizontalGlue());
        panel.add(Box.createRigidArea(new Dimension(20, 0)));

        if(confirm != null) {
          JButton btn = UIFactory.createConfirmButton(confirm, "confirm", this, null); 
          pConfirmButton = btn;

          panel.add(btn);   
        }
          
        if((confirm != null) && (apply != null))
          panel.add(Box.createRigidArea(new Dimension(20, 0)));
       
        if(apply != null) {
          JButton btn = UIFactory.createDialogButton(apply, "apply", this, null); 
          pApplyButton = btn;

          panel.add(btn);   
        }

        if(((confirm != null) || (apply != null)) && (extra != null)) 
          panel.add(Box.createRigidArea(new Dimension(20, 0)));

        if(extra != null) {
          extraBtns = new JButton[extra.length];

          int wk;
          for(wk=0; wk<extra.length; wk++) {
            if(extra[wk] != null) {
              JButton btn = 
                UIFactory.createDialogButton(extra[wk][0], extra[wk][1], this, null); 
              extraBtns[wk] = btn;

              panel.add(btn);       
            }

            if(wk<(extra.length-1)) 
              panel.add(Box.createRigidArea(new Dimension(20, 0)));            
          }
        }

        if(((confirm != null) || (apply != null) || (extra != null)) && (cancel != null))
          panel.add(Box.createRigidArea(new Dimension(40, 0)));
       
        if(cancel != null) {
          JButton btn = UIFactory.createCancelButton(cancel, "cancel", this, null); 
          pCancelButton = btn;

          panel.add(btn);   
        }
        
        panel.add(Box.createRigidArea(new Dimension(20, 0)));
        panel.add(Box.createHorizontalGlue());
        
        root.add(panel);
      }

      setContentPane(root);

      pack();

      {
        Rectangle bounds = getGraphicsConfiguration().getBounds();
        setLocation(bounds.x + bounds.width/2 - getWidth()/2, 
                    bounds.y + bounds.height/2 - getHeight()/2);              
      }

      return extraBtns;
    }



    /*---------------------------------------------------------------------------------------*/
    /*   B U T T O N S                                                                       */
    /*---------------------------------------------------------------------------------------*/

    /**
     * Disable all the buttons except the cancel button.
     */
    private void
    disableAllButtons()
    {
      pNextButton.setEnabled(false);
      pNextActionButton.setEnabled(false);
      pRunAllButton.setEnabled(false);
    }

    /**
     * Disable the cancel button.
     */
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
    enableRelaunchButtons()
    {
      pRelaunchButton.setEnabled(true);
      pRelaunchOptionsButton.setEnabled(true);
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
      else if (cmd.equals("relaunch"))
        doRelaunch();
      else if (cmd.equals("relaunch-options"))
        doRelaunchOptions();
      else if (cmd.equals("print-cmdline"))
        doCmdLineOptions();
    }

    /*-- TREE SELECTION LISTENER METHODS ---------------------------------------------------*/

    public void 
    valueChanged
    (
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
        new KillQueueJobsTask().start();
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
        cleanupConnections();
        System.exit(exitCode);
      }
      else 
        this.setVisible(false);
    }
    
    private void
    doRelaunch()
    {
      new RelaunchBuilderTask().start();
    }
    
    private void
    doRelaunchOptions()
    {
     new RelaunchBuilderOptionsTask().start(); 
    }
    
    private void
    doCmdLineOptions()
    {
      new DoCmdLineTask().start();
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
        case EnableRelaunchButton:
          enableRelaunchButtons();
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
          
          /*
           * If all the params are ready to go immediately, just run the pass and 
           * keep going.
           */
          if (!pTopPanel.hasParams()) {
            disableAllButtons();
            new RunSetupPassTask().start();
          }
          else if (pTopPanel.allParamsReady())
            setupPhaseEnableButtons();
          else
            disableAllButtons();
        }
        catch (Exception ex) {
          handleException(ex);
        }
        catch (LinkageError er ) {
          handleException(er);
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
          else {
            if (getReleaseView(false))
              SwingUtilities.invokeLater(new AskAboutReleaseViewTask(false));
            else
              SwingUtilities.invokeLater(new FinishedTask(false));
          }
        }
        catch (Exception ex) {
          handleException(ex);
        }
        catch (LinkageError er ) {
          handleException(er);
        }
      }
    }
    
    private 
    class AskAboutReleaseViewTask
      extends Thread
    {
      public
      AskAboutReleaseViewTask
      (
        boolean error  
      )
      {
        pError = error;
      }
      
      @Override
      public void
      run()
      {
        setPhase(ExecutionPhase.ReleaseView);
        disableAllButtons();
        JConfirmDialog dialog = 
          new JConfirmDialog(pDialog, "Builder Error: Release View?", 
            "Release View was selected for this builder.  Do you want to release the view?");
        dialog.setVisible(true);
        if (dialog.wasConfirmed()) {
          disableCancelButton();
          new ReleaseViewTask(pError).start();
        }
        else
          pLog.logAndFlush(Kind.Ops, Level.Severe, 
            "Builder execution has ended with an error.  " +
            "The working area was not cleaned up.");
      }

      boolean pError;
    }

    private
    class FinishedTask
      extends Thread
    {
      public
      FinishedTask
      (
        boolean error  
      )
      {
        pError = error;
      }
      
      @Override
      public void
      run()
      {
        if (!pError)
          setPhase(ExecutionPhase.Finished);
        else
          setPhase(ExecutionPhase.Error);
        disableAllButtons();
        makeQuitButton();
        enableCancelButton();
        pLog.logAndFlush(Kind.Ops, Level.Info, "Execution is now complete");
      }
      
      boolean pError;
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
          handleException(ex);
        }
        catch (LinkageError er ) {
          handleException(er);
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
        catch (LinkageError er ) {
          handleException(er);
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
            new JConfirmDialog(pDialog, "Builder Error: Release Nodes?", pMessage);
          dialog.setVisible(true);
          if (dialog.wasConfirmed()) {
            disableCancelButton();
            new ReleaseNodesTask().start();
          }
          else {
            pLog.logAndFlush(Kind.Ops, Level.Severe, 
              "Builder execution has ended with an error.  " +
            "The created nodes were not cleaned up.");
          if (getReleaseView(true)) 
            SwingUtilities.invokeLater(new AskAboutReleaseViewTask(true));
          else
            SwingUtilities.invokeLater(new FinishedTask(true));
          }
        }
        catch (Exception ex) {
          handleException(ex);
        }
        catch (LinkageError er ) {
          handleException(er);
        }
      }
      private String pMessage;
    }

    private
    class RelaunchGUITask
      extends Thread
    {
      private 
      RelaunchGUITask
      (
        MultiMap<String, TreeSet<String>> paramNames  
      )
      {
        pParamNames = paramNames;
      }
      
      @Override
      public void 
      run()
      {
        Box vbox = Box.createVerticalBox();
        Component comps[] = UIFactory.createTitledPanels();
        JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
        
        MultiMap<String, JBooleanField> fieldsMap = new MultiMap<String, JBooleanField>();
        
        for (String firstKey : pParamNames.keySet()) {
          MultiMap<String, TreeSet<String>> mmap = pParamNames.get(firstKey);
          if (mmap.hasLeafValue()) {
            JBooleanField field = 
              UIFactory.createTitledBooleanField(tpanel, firstKey, sTSize, vpanel, sVSize);
            UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
            field.setValue(true);
            fieldsMap.putValue(firstKey, field);
          }
          else {
            for (String secondKey : mmap.keySet()) {
              String title = firstKey + " - " + secondKey;
              JBooleanField field = 
                UIFactory.createTitledBooleanField(tpanel, title, sTSize, vpanel, sVSize);
              UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
              LinkedList<String> keys = new LinkedList<String>();
              keys.add(firstKey);
              keys.add(secondKey);
              field.setValue(true);
              fieldsMap.putValue(keys, field);
            }
          }
        }
        
        vbox.add(comps[2]);
        
        vbox.add(UIFactory.createFiller(sTSize + sVSize));
        
        JScrollPane scroll;
        {
          scroll = new JScrollPane(vbox);

          scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
          scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

          scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

          Dimension size = new Dimension(sTSize + sVSize, 500);
          scroll.setMinimumSize(size);
        }
        JToolDialog dialog = 
          new JToolDialog("Relaunch Options", scroll, "Relaunch");
        dialog.setVisible(true);
        if (dialog.wasConfirmed()) {
          new RelaunchBuilderFinalTask(pParamNames, fieldsMap).start();
        }
        
      }
      
      private static final int sVSize = 80;
      private static final int sTSize = 500;
      
      private MultiMap<String, TreeSet<String>> pParamNames;
    }
    
    private
    class ShowErrorTask
      extends Thread
    {
      private 
      ShowErrorTask
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
        catch (LinkageError er ) {
          handleException(er);
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

    /**
     * Top panel button
     */
    private JButton pRelaunchButton;
    private JButton pRelaunchOptionsButton;

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
      catch (LinkageError er ) {
        handleException(er);
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
      catch (LinkageError er ) {
        handleException(er);
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
          SwingUtilities.invokeAndWait(
            pDialog.new AdjustButtonsTask(ButtonState.EnableRelaunchButton));
          checkActions();
          buildSecondLoopExecutionOrder();
          SwingUtilities.invokeLater(pDialog.new PrepareConstructPassesTask());
        }
      }
      catch (Exception ex) {
        handleException(ex);
      }
      catch (LinkageError er ) {
        handleException(er);
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
          if (pAbort)
            throw new PipelineException("Execution halted by user");
          pRunning = true;
          executeNextConstructPass();
          pRunning = false;
          SwingUtilities.invokeAndWait(pDialog.new DuringAllConstructPassTask());
        }
        catch (Exception ex) {
          handleException(ex);
          error = true;
        }
        catch (LinkageError er ) {
          handleException(er);
          error = true;
        }
      }
      if (!error) {
        if (getReleaseView(false))
          SwingUtilities.invokeLater(pDialog.new AskAboutReleaseViewTask(false));
        else
          SwingUtilities.invokeLater(pDialog.new FinishedTask(false));
      }
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
        handleException(ex);
      }
      catch (LinkageError er ) {
        handleException(er);
      }
    }
  }

  private
  class ReleaseViewTask
    extends Thread
  {
    public
    ReleaseViewTask
    (
      boolean error  
    )
    {
      pError = error;
    }
    
    @Override
    public void
    run()
    {
      try {
        releaseView(pError);
        SwingUtilities.invokeLater(pDialog.new FinishedTask(pError));
      }
      catch (Exception ex) {
        SwingUtilities.invokeLater(pDialog.new AdjustButtonsTask(ButtonState.EnableQuit));
        handleException(ex);
      }
      catch (LinkageError er ) {
        SwingUtilities.invokeLater(pDialog.new AdjustButtonsTask(ButtonState.EnableQuit));
        handleException(er);
      }
    }
    
    boolean pError;
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
        LogMgr log = getBuilder().getBuilderInformation().getLogMgr(); 
        log.log(Kind.Ops, Level.Warning, 
          "All the nodes that were registered will now be released.");
        getRunningBuilder().releaseNodes();
        log.log
          (Kind.Ops, Level.Info, "Finished releasing all the nodes.");
        if (getReleaseView(true))
          SwingUtilities.invokeLater(pDialog.new AskAboutReleaseViewTask(true));
        else
          SwingUtilities.invokeLater(pDialog.new FinishedTask(true));
      }
      catch (Exception ex) {
        SwingUtilities.invokeLater(pDialog.new AdjustButtonsTask(ButtonState.EnableQuit));
        handleException(ex);
      }
      catch (LinkageError er ) {
        SwingUtilities.invokeLater(pDialog.new AdjustButtonsTask(ButtonState.EnableQuit));
        handleException(er);
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
        validateCommandLineParams(getBuilder(), getBuilder().getBuilderInformation());
        initialSetupPases();
        SwingUtilities.invokeLater(pDialog.new NextParameterPassTask());
      }
      catch (Exception ex) {
        handleException(ex);
      }
      catch (LinkageError er ) {
        handleException(er);
      }
    }
  }
  
  private
  class KillQueueJobsTask
    extends Thread
  {
    @Override
    public void 
    run()
    {
      try {
        BaseBuilder current = getRunningBuilder();
        current.killJobs();
      }
      catch (Exception ex) {
        handleException(ex);
      }
      catch (LinkageError er ) {
        handleException(er);
      }
    }
  }
  
  private
  class DoCmdLineTask
    extends Thread
  {
   
    @Override
    public void 
    run()
    {
      try {
        BaseBuilder top = getBuilder();
        MultiMap<String, String> allParams = top.getAllParamValues();
        StringBuilder build = new StringBuilder();
        
        build.append("\n\n**************************\n");
        for (String builderName : allParams.keySet()) {
          build.append("--builder=");
          build.append(builderName.replaceAll(" ", ""));
          build.append(" ");
          MultiMap<String, String> params = allParams.get(builderName); 
          for (MultiMapEntry<String, String> entry : params.entries()) {
            build.append("-");
            for (String key : entry.getKeys()) {
              build.append("-");
              build.append(key);
            }
            build.append("=");
            build.append(entry.getValue());
            build.append(" ");
          }
        }
        build.append("\n**************************\n\n");
        top.getBuilderInformation().getLogMgr().logAndFlush
          (Kind.Ops, Level.Info, 
           build.toString());
      }
      catch (Exception ex) {
        handleException(ex);
      }
      catch (LinkageError er ) {
        handleException(er);
      }
    }
  }
  
  private 
  class RelaunchBuilderFinalTask
    extends Thread
  {

    public 
    RelaunchBuilderFinalTask
    (
      MultiMap<String, TreeSet<String>> paramNames,
      MultiMap<String, JBooleanField> fieldsMap
    )
    {
      pParamNames = paramNames;
      pFieldsMap = fieldsMap;
    }
    
    @Override
    public void 
    run()
    {
      BaseBuilder top = getBuilder();
      Class<? extends BaseBuilder> builderClass = top.getClass();
      Class args[] = {MasterMgrClient.class, QueueMgrClient.class, BuilderInformation.class};
      BaseBuilder newBuilder = null;
      try {
        Constructor<? extends BaseBuilder> construct = builderClass.getConstructor(args);
        MultiMap<String, String> params = top.getAllParamValues();
        
        for (String firstKey : pFieldsMap.keySet()) {
          MultiMap<String, JBooleanField> fmap = pFieldsMap.get(firstKey);
          if (fmap.hasLeafValue()) {
            boolean use = fmap.getLeafValue().getValue();
            if (!use) {
              TreeSet<String> paramNames = pParamNames.getValue(firstKey);
              for (String pName : paramNames) {
                LinkedList<String> keys = new LinkedList<String>();
                Collections.addAll(keys, firstKey, pName);
                params.removeBranch(keys);
              }
            }
          }
          else {
           for (String secondKey : fmap.keySet()) {
             boolean use = fmap.getValue(secondKey).getValue();
             if (!use) {
               TreeSet<String> paramNames = pParamNames.get(firstKey).getValue(secondKey);
               for (String pName : paramNames) {
                 LinkedList<String> keys = new LinkedList<String>();
                 Collections.addAll(keys, firstKey, pName);
                 params.removeBranch(keys);
               }
             }
           }
          }
        }
        
        
        BuilderInformation info = top.getBuilderInformation();
        BuilderInformation newInfo = 
          new BuilderInformation(info.getLoggerName(), info.usingGui(), 
                                 info.terminateAppOnQuit(), info.abortOnBadParam(), 
                                 info.useBuilderLogging(), params);

        MasterMgrClient mclient = new MasterMgrClient();
        QueueMgrClient qclient = new QueueMgrClient();
        newBuilder = construct.newInstance(mclient, qclient, newInfo);
      }
      catch (Exception ex) {
        handleRelaunchException(ex);
      }
      catch (LinkageError er ) {
        handleRelaunchException(er);
      }
      if (newBuilder != null)
        new GUIExecution(newBuilder).run();
    }
    
    private MultiMap<String, TreeSet<String>> pParamNames;
    private MultiMap<String, JBooleanField> pFieldsMap;
  }
  
  private 
  class RelaunchBuilderOptionsTask
    extends Thread
  {
    @Override
    public void 
    run()
    {
      try {
        BaseBuilder top = getBuilder();
        MultiMap<String, TreeSet<String>> paramNames = top.getAllParamNames();
        SwingUtilities.invokeLater(pDialog.new RelaunchGUITask(paramNames));
      }
      catch (Exception ex) {
        handleRelaunchException(ex);
      }
      catch (LinkageError er ) {
        handleRelaunchException(er);
      }
    }
  }
  
  private
  class RelaunchBuilderTask
    extends Thread
  {
    @SuppressWarnings("unchecked")
    @Override
    public void 
    run()
    {
      BaseBuilder top = getBuilder();
      Class<? extends BaseBuilder> builderClass = top.getClass();
      Class args[] = {MasterMgrClient.class, QueueMgrClient.class, BuilderInformation.class};
      BaseBuilder newBuilder = null;
      try {
        Constructor<? extends BaseBuilder> construct = builderClass.getConstructor(args);
        MultiMap<String, String> params = top.getAllParamValues();
        BuilderInformation info = top.getBuilderInformation();
        BuilderInformation newInfo = 
          new BuilderInformation(info.getLoggerName(), info.usingGui(), info.terminateAppOnQuit(), 
                                 info.abortOnBadParam(), info.useBuilderLogging(), params);
        MasterMgrClient mclient = new MasterMgrClient();
        QueueMgrClient qclient = new QueueMgrClient();
        newBuilder = construct.newInstance(mclient, qclient, newInfo);
      }
      catch (Exception ex) {
        handleRelaunchException(ex);
      }
      catch (LinkageError er ) {
        handleRelaunchException(er);
      }
      if (newBuilder != null)
        new GUIExecution(newBuilder).run();
    }
  }


 private JBuilderDialog pDialog;
 private boolean pRunning;
 private boolean pAbort;
}
