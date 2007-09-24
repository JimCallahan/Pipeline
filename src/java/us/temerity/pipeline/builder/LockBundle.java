package us.temerity.pipeline.builder;

import java.util.*;


public 
class LockBundle
{
  public
  LockBundle()
  {
    pNodesToLock = new LinkedList<String>();
    pNodesToCheckin = new LinkedList<String>();
  }
  
  public void
  addNodeToLock
  (
    String node  
  )
  {
    if (!pNodesToLock.contains(node))
      pNodesToLock.add(node);
  }
  
  public List<String>
  getNodesToLock()
  {
    return Collections.unmodifiableList(pNodesToLock);
  }
  
  public void
  addNodeToCheckin
  (
    String node  
  )
  {
    if (!pNodesToCheckin.contains(node))
      pNodesToCheckin.add(node);
  }
  
  public List<String>
  getNodesToCheckin()
  {
    return Collections.unmodifiableList(pNodesToCheckin);
  }

  private LinkedList<String> pNodesToLock;
  private LinkedList<String> pNodesToCheckin;
}
