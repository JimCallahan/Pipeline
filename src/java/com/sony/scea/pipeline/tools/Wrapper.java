package com.sony.scea.pipeline.tools;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;

/**
 * @author Jesse Clemens
 * <P>
 * 
 * A general utility class for passing Pipeline information to static methods.
 * <P>
 * This class provides the essential information that needs to be passed into a
 * static method to allow that static method to perform an action. All the
 * fields are public and final, meaning they must be set through the
 * constructor, but can be accessed with out the overhead of method calls.
 * 
 */
public class Wrapper
{
  /**
   * The name of the user whose working area the action will be performed.
   */
  public final String user;

  /**
   * The name of the working area where the action will be performed
   */
  public final String view;

  /**
   * The toolset that will be assigned to any nodes created or modified.
   */
  public final String toolset;

  /**
   * The instance of MasterMgrClient that will be used to perform an action.
   */
  public final MasterMgrClient mclient;

  /**
   * The only constructor used for this class. All four values must be passed
   * in. A null value for any of the fields will result in a
   * {@link PipelineException PipelineException} being thrown.
   * 
   * @param user
   * @param view
   * @param toolset
   * @param mclient
   * @throws PipelineException
   */
  @SuppressWarnings("hiding")
  public Wrapper(final String user, final String view, final String toolset,
    final MasterMgrClient mclient) throws PipelineException
    {
    if (user == null)
      throw new PipelineException("You cannot pass a null user value "
	+ "into the Wrapper constructor");
    if (view == null)
      throw new PipelineException("You cannot pass a null view value "
	+ "into the Wrapper constructor");
    if (toolset == null)
      throw new PipelineException("You cannot pass a null toolset value "
	+ "into the Wrapper constructor");
    if (mclient == null)
      throw new PipelineException("You cannot pass a null mclient value "
	+ "into the Wrapper constructor");

    this.user = user;
    this.view = view;
    this.toolset = toolset;
    this.mclient = mclient;
    }
}
