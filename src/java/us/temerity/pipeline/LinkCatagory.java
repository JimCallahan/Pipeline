// $Id: LinkCatagory.java,v 1.5 2004/06/28 23:00:38 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   L I N K   C A T A G O R Y                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The named classification of the node state propogation policy 
 * ({@link LinkPolicy LinkPolicy}) of a node link.
 */
public
class LinkCatagory
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  LinkCatagory() 
  {
    super();
  }

  /**
   * Construct a new node state propogation policy catagory with the given name.
   * 
   * @param name 
   *   The name of the link catagory.
   * 
   * @param policy 
   *   The node state propogation policy.
   */ 
  public
  LinkCatagory
  (
   String name,  
   LinkPolicy policy
  ) 
  {
    super(name);

    if(policy == null) 
      throw new IllegalArgumentException("The policy cannot be (null)!");
    pPolicy = policy;
  }

  /**
   * Copy constructor. 
   */ 
  public
  LinkCatagory
  (
   LinkCatagory lcat
  ) 
  {
    super(lcat.getName());

    pPolicy = lcat.getPolicy();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the link's {@link OverallNodeState OverallNodeState} and
   * {@link OverallQueueState OverallQueueState} propagation policy.
   */ 
  public LinkPolicy
  getPolicy() 
  {
    return pPolicy;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj
  )
  {
    if((obj != null) && (obj instanceof LinkCatagory)) {
      LinkCatagory link = (LinkCatagory) obj;
      return (super.equals(obj) && 
	      (pPolicy.equals(link.pPolicy)));
    }
    return false;
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

    encoder.encode("Policy", pPolicy);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    LinkPolicy policy = (LinkPolicy) decoder.decode("Policy");
    if(policy == null) 
      throw new GlueException("The \"Policy\" entry was missing or (null)!");
    pPolicy = policy;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4748760727596434882L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The link's {@link OverallNodeState OverallNodeState} and 
   * {@link OverallQueueState OverallQueueState} propagation policy.
   */
  private LinkPolicy  pPolicy; 

}



