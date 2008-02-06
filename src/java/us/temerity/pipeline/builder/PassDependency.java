// $Id: PassDependency.java,v 1.1 2008/02/06 05:11:27 jesse Exp $

/**
 * 
 */
package us.temerity.pipeline.builder;

import java.util.LinkedList;

import us.temerity.pipeline.builder.BaseBuilder.ConstructPass;

public 
class PassDependency
{
  public
  PassDependency
  (
    ConstructPass target,
    LinkedList<ConstructPass> source
  )
  {
    pTarget = target;
    if (source != null) {
      for (ConstructPass pass : source)
        addSource(pass);
    }
    else
      pSources = new LinkedList<ConstructPass>(); 
  }
  
  public
  PassDependency
  (
    ConstructPass target,
    ConstructPass source
  )
  {
    pTarget = target;
    if (source != null) 
      addSource(source);
  }
  
  public boolean
  hasSources()
  {
    if (pSources == null || pSources.isEmpty())
      return false;
    return true;
  }
  
  public ConstructPass 
  getTarget()
  {
    return pTarget;
  }

  public LinkedList<ConstructPass> 
  getSources()
  {
    return pSources;
  }
  
  public void 
  addSource
  (
    ConstructPass pass
  )
  {
    if (pSources == null)
      pSources = new LinkedList<ConstructPass>();
    if (!pSources.contains(pass))
      pSources.add(pass);
  }
  
  @Override
  public String
  toString()
  {
    String message = pTarget.toString() + " Depends on: {";
    for (ConstructPass pass :  pSources) {
      message += pass.toString();
      if (!pSources.getLast().equals(pass))
        message += ", ";
    }
    message += "}";
    return message;
  }
  
  private ConstructPass pTarget;
  private LinkedList<ConstructPass> pSources;
}