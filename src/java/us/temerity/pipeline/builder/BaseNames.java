/*
 * Created on Nov 2, 2006
 * Created by jesse
 * For Use in us.temerity.pipeline.builders
 * 
 */
package us.temerity.pipeline.builder;

import us.temerity.pipeline.PassLayoutGroup;
import us.temerity.pipeline.PipelineException;

public abstract
class BaseNames
  extends HasBuilderParams
{
  protected 
  BaseNames
  (
    String name,
    String desc
  ) 
    throws PipelineException
  {
    super(name, desc, false);
  }
  
  public abstract void 
  generateNames() 
    throws PipelineException;

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
  
  
}
