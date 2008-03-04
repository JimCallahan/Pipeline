// $Id: PassDependency.java,v 1.2 2008/03/04 08:15:14 jesse Exp $

/**
 * 
 */
package us.temerity.pipeline.builder;

import java.util.*;

import us.temerity.pipeline.builder.BaseBuilder.*;

public 
class PassDependency
{
  public
  PassDependency
  (
    BaseConstructPass target,
    LinkedList<BaseConstructPass> source
  )
  {
    pTarget = target;
    if (source != null) {
      for (BaseConstructPass pass : source)
        addSource(pass);
    }
    else
      pSources = new LinkedList<BaseConstructPass>(); 
  }
  
  public
  PassDependency
  (
    BaseConstructPass target,
    BaseConstructPass source
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
  
  public BaseConstructPass 
  getTarget()
  {
    return pTarget;
  }

  public LinkedList<BaseConstructPass> 
  getSources()
  {
    return pSources;
  }
  
  public void 
  addSource
  (
    BaseConstructPass pass
  )
  {
    if (pSources == null)
      pSources = new LinkedList<BaseConstructPass>();
    if (!pSources.contains(pass))
      pSources.add(pass);
  }
  
  @Override
  public String
  toString()
  {
    String message = pTarget.toString() + " Depends on: {";
    for (BaseConstructPass pass :  pSources) {
      message += pass.toString();
      if (!pSources.getLast().equals(pass))
        message += ", ";
    }
    message += "}";
    return message;
  }
  
  private BaseConstructPass pTarget;
  private LinkedList<BaseConstructPass> pSources;
}