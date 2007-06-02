package us.temerity.pipeline.builder.ui;


public 
class BuilderTreeNodeInfo
{
  public BuilderTreeNodeInfo
  (
    String text  
  )
  {
    pActive = false;
    pDone = false;
    pText = text;
  }
  
  public BuilderTreeNodeInfo
  (
    boolean active,
    String text
  )
  {
    pActive = active;
    pDone = false;
    pText = text;
  }
  
  public void
  setActive()
  {
    pActive = true;
    pDone = false;
  }
  
  public void
  setDone()
  {
    pActive = false;
    pDone = true;
  }
  
  public boolean
  isActive()
  {
    return pActive;
  }
  
  public boolean
  isDone()
  {
    return pDone;
  }
  
  public String
  getText()
  {
    return pText;
  }
  
  private boolean pActive;
  private boolean pDone;
  private String pText;
}