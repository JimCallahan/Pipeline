// $Id: LinkMod.java,v 1.3 2004/07/07 13:17:40 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   L I N K   M O D                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Information about the relationship between a modifiable working version of a node 
 * (<CODE>NodeMod</CODE>) and the upstream node to which it is linked. <P> 
 */ 
public
class LinkMod
  extends LinkCommon
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  LinkMod() 
  {
    super();
  }

  /**
   * Construct a node link from the given source node. <P> 
   * 
   * If the <CODE>relationship</CODE> argument is <CODE>OneToOne</CODE> then the 
   * <CODE>offset</CODE> argument must not be <CODE>null</CODE>.  For all other 
   * link relationships, the <CODE>offset</CODE> argument must be <CODE>null</CODE>.
   * 
   * @param name 
   *   The fully resolved name of the source node.
   * 
   * @param policy 
   *   The node state propogation policy.
   * 
   * @param relationship 
   *   The nature of the relationship between files associated with the source and 
   *   target nodes. 
   * 
   * @param offset 
   *   The frame index offset.
   */ 
  public 
  LinkMod
  (
   String name, 
   LinkPolicy policy,
   LinkRelationship relationship,  
   Integer offset
  ) 
  {
    super(name, policy, relationship, offset);
  }

  /**
   * Construct from a checked-in node link.
   */ 
  public 
  LinkMod
  (
   LinkVersion link
  ) 
  {
    super(link);
  }

  /**
   * Copy constructor. 
   */ 
  public 
  LinkMod
  (
   LinkMod link   
  ) 
  {
    super(link);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the link's {@link OverallNodeState OverallNodeState} and
   * {@link OverallQueueState OverallQueueState} propagation policy.
   */ 
  public void 
  setPolicy
  (
   LinkPolicy policy
  ) 
  {
    if(policy == null) 
      throw new IllegalArgumentException("The policy cannot be (null)!");
    pPolicy = policy;
  }

  /**
   * Set the nature of the relationship between files associated with the source and 
   * target nodes. 
   */ 
  public void 
  setRelationship
  (
   LinkRelationship relationship
  )
  {
    if(relationship == null) 
      throw new IllegalArgumentException("The link relationship cannot be (null)!");
    pRelationship = relationship;
  }
  
  /**
   * Set the frame offset to be added to frame indices of files associated with the 
   * target node to determine the frame indices of files associated with the source node.
   * 
   * This method should only be called for links for which the 
   * {@link #getRelationship getRelationship} method returns 
   * {@link LinkRelationship#OneToOne OneToOne}.
   */
  public void 
  setFrameOffset
  (
   int offset 
  ) 
  {
    if(pRelationship != LinkRelationship.OneToOne) 
      throw new IllegalArgumentException
	("The frame index offset has no meaning links with a (" + pRelationship.name() +
	 ") relationship!");
    
    pFrameOffset = new Integer(offset);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1538507879490090450L;


}



