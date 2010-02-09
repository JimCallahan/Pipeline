/*
 * Created on Nov 2, 2006
 * Created by jesse
 * For Use in us.temerity.pipeline.builders
 * 
 */
package us.temerity.pipeline.builder;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   N A M E S                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 *  The parent class for all Namers. <p>
 *  
 *  Namers are simple Utilities that exist to provide node names to Builders, which
 *  the Builders use to construct their node networks.  Namers are defined separately from
 *  Builders in order to make it easy to change the node name and directory structure that
 *  Builders make, without having to change any of the actual functionality of the 
 *  Builder. <p>
 *  
 *  Namers must implement a single method, {@link #generateNames()}, which should evaluate
 *  any parameters that the Namer has and then generate the names that are going to be
 *  returned by its access methods.
 */
public abstract
class BaseNames
  extends BaseUtil
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new namer.
   * 
   * @param name
   *   The name of the namer.
   * 
   * @param desc
   *   A brief description of what the namer is supposed to do.
   * 
   * @param mclient
   *   The instance of the Master Manager that the builder is going to use.
   * 
   * @param qclient
   *   The instance of the Queue Manager that the builder is going to use
   *   
   * @param info
   *   The instance of {@link BuilderInformation} that is being used by the parent Builder of
   *   this namer.
   */
  protected 
  BaseNames
  (
    String name,
    String desc,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation info
  ) 
    throws PipelineException
  {
    super(name, desc, mclient, qclient, info.getLoggerName());
    pGenerated = false;
  }
  
  /**
   * Evaluate any parameters that the Namer has and then generate the names that are going to
   * be returned by its access methods.
   * 
   * @throws PipelineException If any of the parameter values that have been set are 
   * incorrect or if names cannot be generated.
   */
  public abstract void 
  generateNames() 
    throws PipelineException;
  
  /**
   * Called during Builder execution, this method is responsible for calling
   * {@link #generateNames()} and for setting the generated flag to true.
   */
  public final void
  run()
    throws PipelineException
  {
    generateNames();
    pGenerated = true;
  }
  
  /**
   * Has generateNames() been run on this namer yet?
   */
  public final boolean
  isGenerated()
  {
    return pGenerated;
  }

  @Override
  protected final void 
  setLayout
  (
    PassLayoutGroup layout
  )
  {
    if (layout.getNumberOfPasses() != 1)
      throw new IllegalArgumentException
        ("A layout was specified for a BaseNames class that contains more than 1 pass.  " +
         "The Namer infrastructure does not support this.");
    super.setLayout(layout);
  }

  @Override
  public final int 
  getCurrentPass()
  {
    return 1;
  }
  
  /**
   * Get the instance of {@link BuilderInformation} that this namer is using.
   */
  public BuilderInformation
  getBuilderInformation()
  {
    return pBuilderInformation;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  private boolean pGenerated;
  
  private BuilderInformation pBuilderInformation;
}
