// $Id: LinkCatagoryDesc.java,v 1.1 2004/06/28 23:39:45 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   L I N K   C A T A G O R Y   D E S C                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link LinkCatagory LinkCatagory} augmented with a short text description.
 */
public
class LinkCatagoryDesc
  extends LinkCatagory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  LinkCatagoryDesc() 
  {
    super();
  }

  /**
   * Construct a link catagory entry.
   * 
   * @param name 
   *   The name of the link catagory.
   * 
   * @param policy 
   *   The node state propogation policy.
   * 
   * @param desc
   *   A short description of the link catagory.
   */ 
  public
  LinkCatagoryDesc
  (
   String name,  
   LinkPolicy policy, 
   String desc
  ) 
  {
    super(name, policy);

    if(desc == null) 
      throw new IllegalArgumentException("The description cannot be (null)!");
    pDescription = desc;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the description text. 
   */ 
  public String
  getDescription()
  {
    return pDescription;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    encoder.encode("Description", pDescription);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    String desc = (String) decoder.decode("Description");
    if(desc == null) 
      throw new GlueException("The \"Description\" entry was missing or (null)!");
    pDescription = desc;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7730867073858384841L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A short message which describes the class. 
   */     
  protected String  pDescription;  
}



