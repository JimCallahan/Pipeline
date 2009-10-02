// $Id: CommandLineExecution.java,v 1.10 2009/10/02 04:52:15 jesse Exp $

package us.temerity.pipeline.builder.execution;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;


/**
 * Executes a builder without displaying a GUI or accepting user input.
 */
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
    try {
      pLog.log(Kind.Ops, Level.Fine, "Beginning execution of SetupPasses.");
      executeFirstLoop();
      checkActions();
      buildSecondLoopExecutionOrder();
      executeSecondLoop();
      releaseView(false);
    }
    catch (Exception ex) {
      handleException(ex);
    }
    catch (LinkageError er ) {
      handleException(er);
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
    Throwable ex
  )
    throws PipelineException
  {
    ExecutionPhase phase = getPhase();
    if (phase != ExecutionPhase.Release && phase != ExecutionPhase.Error &&
        phase != ExecutionPhase.ReleaseView) {
      String header = "The builder was unable to successfully complete.";

      if (getBuilder().releaseOnError()) {
        if (phase.haveNodesBeenMade())
          header += "\nAll nodes registered in the current builder will now be released.";
        else if (phase.isEndingPhase())
          header += "\nSince the check-in operation began, the Builder will not attempt to " +
          "release registered nodes.";
      }

      setPhase(ExecutionPhase.Error);
      
      String message;
      if (ex instanceof PipelineException)
        message = header + "\n" + ex.getMessage();
      else
        message = Exceptions.getFullMessage(header, ex);

      pLog.logAndFlush(Kind.Ops, Level.Severe, message);
      if (getRunningBuilder() != null && getRunningBuilder().releaseOnError() && phase.haveNodesBeenMade()) {
        releaseNodes(getRunningBuilder());
      }
      releaseView(true);
      if (!getBuilder().useBuilderLogging())
        throw new PipelineException(message);
      if (getBuilder().terminateAppOnQuit()) {
        cleanupConnections();
        System.exit(1);
      }
    }
    else if (phase == ExecutionPhase.Release) {
      String header = 
        "The builder was not able to successfully release the nodes: ";
      String message;
      if (ex instanceof PipelineException)
        message = header + "\n" + ex.getMessage();
      else
        message = Exceptions.getFullMessage(header, ex);
      pLog.logAndFlush(Kind.Ops, Level.Severe, message);
    }
    else if (phase == ExecutionPhase.ReleaseView) {
      String header = 
        "A problem occured after execution when attempting to release the working area: ";
      String message;
      if (ex instanceof PipelineException)
        message = header + "\n" + ex.getMessage();
      else
        message = Exceptions.getFullMessage(header, ex);
      pLog.logAndFlush(Kind.Ops, Level.Severe, message);
    }
  }
}
