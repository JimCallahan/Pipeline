/*
 * Created on Nov 2, 2006
 * Created by jesse
 * For Use in us.temerity.pipeline.builders
 * 
 */
package us.temerity.pipeline.builder;

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
}
