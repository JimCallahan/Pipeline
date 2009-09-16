// $Id: QueueAddByNameReq.java,v 1.1 2009/09/16 03:54:40 jesse Exp $

package us.temerity.pipeline.message;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   A D D   B Y   N A M E   R E Q                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to add a new queue control to the currently defined set of controls. <P> 
 */
public
class QueueAddByNameReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param name
   *   The name of the new queue control.
   *   
   * @param controlType
   *   The type of the control, used for constructing error messages.
   */
  public
  QueueAddByNameReq
  (
    String name,
    String controlType
  )
  { 
    super();

    if(name == null) 
      throw new IllegalArgumentException
        ("The " + controlType + " name cannot be (null)!");
    pName = name;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the new queue control. 
   */
  public String
  getName() 
  {
    return pName; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 9109409213635594760L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the queue control to add.
   */ 
  private String pName;
}
  
