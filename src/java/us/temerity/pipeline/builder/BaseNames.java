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
 *  The parent class for all Namers.
 *  <p>
 *  Namers are simply Utility classes that exist to provide node names to Builders, which
 *  the Builders use to construct their node networks.  Namers are defined separately from
 *  Builders in order to make it easy to change the node name and directory structure that
 *  Builders make, without having to change any of the actual functionality of the Builder.
 *  <p>
 *  Namers must implement a single method, {@link #generateNames()}, which should evaluate
 *  any parameters that the Namer has and then generate the names that are going to be
 *  returned by its access methods.
 */
public abstract
class BaseNames
  extends BaseUtil
{
  protected 
  BaseNames
  (
    String name,
    String desc,
    MasterMgrClient mclient,
    QueueMgrClient qclient
  ) 
    throws PipelineException
  {
    super(name, desc, mclient, qclient);
    pGenerated = false;
  }
  
  public abstract void 
  generateNames() 
    throws PipelineException;
  
  public final void
  run()
    throws PipelineException
  {
    generateNames();
    pGenerated = true;
  }
  
  /**
   * Has generateNames() been run on this builder yet?
s   */
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
  
  private boolean pGenerated;
}
