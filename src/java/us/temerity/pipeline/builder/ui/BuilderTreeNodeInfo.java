package us.temerity.pipeline.builder.ui;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   T R E E   N O D E   I N F O                                            */
/*------------------------------------------------------------------------------------------*/

public 
class BuilderTreeNodeInfo
{
  public 
  BuilderTreeNodeInfo
  (
    String text  
  )
  {
    pActive = false;
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
  
  public void
  setNotDone()
  {
    pActive = false;
    pDone = false;
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
  
  @Override
  public String 
  toString()
  {
    return getText();
  }
  
  private boolean pActive;
  private boolean pDone;
  private String pText;
}
