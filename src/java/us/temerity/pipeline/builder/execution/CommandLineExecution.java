// $Id: CommandLineExecution.java,v 1.1 2008/02/25 05:03:05 jesse Exp $

package us.temerity.pipeline.builder.execution;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;


public 
class CommandLineExecution
  extends BaseBuilderExecution
{
  public
  CommandLineExecution
  (
    BaseBuilder builder  
  )
    throws PipelineException
  {
    super(builder);
  }
  
  @Override
  public void 
  run()
    throws PipelineException
  {
    pLog.log(Kind.Ops, Level.Fine, "Starting the command line execution.");
    boolean finish = false;
    try {
      pLog.log(Kind.Ops, Level.Fine, "Beginning execution of SetupPasses.");
      executeFirstLoop();
      checkActions();
      buildSecondLoopExecutionOrder();
      executeSecondLoop();
      finish = queueAndWait();
      if (!finish)
        throw new PipelineException("Execution halted.  Jobs didn't finish correctly");
    }
    catch (Exception ex) {
      handleException(ex);
    }
    try {
      if (finish)
        executeCheckIn();
    }
    catch (Exception ex) {
      handleException(ex);
    }
  }
  
  /**
   * Handles an exception thrown during command-line execution.
   * 
   * @param ex
   *   The Exception that was thrown.
   *   
   * @param checkInStarted
   *   Was the check-in operation started before this exception was thrown.
   * 
   * @throws PipelineException
   *   If the Builder is not using its own logging facilities, it will throw
   *   a PipelineException containing the Full Message from whatever exception was
   *   caught.  This is to allow whatever application has called the Builder and is capturing
   *   its logging to respond appropriately. 
   */
  @Override
  protected void
  handleException
  (
    Exception ex
  )
    throws PipelineException
  {
    ExecutionPhase phase = getPhase();
    if (phase != ExecutionPhase.Release && phase != ExecutionPhase.Error) {
      String header = "An error occured during Builder Execution.";

      if (getBuilder().releaseOnError()) {
        if (phase.haveNodesBeenMade())
          header += "\nAll registered nodes will now be released.";
        else if (phase.isEndingPhase())
          header += "\nSince the check-in operation began, the Builder will not attempt to " +
          "release registered nodes.";
      }

      setPhase(ExecutionPhase.Error);

      String message = Exceptions.getFullMessage(header, ex);
      pLog.logAndFlush(Kind.Ops, Level.Severe, message);
      if (getBuilder().releaseOnError() && phase.haveNodesBeenMade()) {
        releaseNodes();
      }
      if (!getBuilder().useBuilderLogging())
        throw new PipelineException(message);
      if (getBuilder().terminateAppOnQuit())
        System.exit(1);
    }
    else if (phase == ExecutionPhase.Release) {
      String header = 
        "Additionally, an error occured while attempting to release the nodes";
      String message = Exceptions.getFullMessage(header, ex);
      pLog.logAndFlush(Kind.Ops, Level.Severe, message);
    }

  }
  
}
