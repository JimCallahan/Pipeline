/*
 * Created on Nov 2, 2006
 * Created by jesse
 * For Use in us.temerity.pipeline.builders
 * 
 */
package us.temerity.pipeline.builder;

import us.temerity.pipeline.*;

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
  
  protected void
  done()
  {
    pGenerated = true;
  }
  
  public boolean
  isGenerated()
  {
    return pGenerated;
  }

  @Override
  protected void 
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
