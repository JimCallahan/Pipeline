// $Id: LinkCommon.java,v 1.7 2004/07/07 13:17:40 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   L I N K   C O M M O N                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The superclass of <CODE>LinkVersion</CODE> and <CODE>LinkMod</CODE> which provides
 * the common fields and methods needed by both classes. <P>
 * 
 * @see LinkMod
 * @see LinkVersion
 */
public
class LinkCommon
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  protected
  LinkCommon() 
  {
    super();
  }

  /**
   * Internal constructor used by <CODE>LinkMod</CODE> to construct a node link. <P> 
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
   *   The frame index offset of target frames from source frames.
   */ 
  protected
  LinkCommon
  (
   String name,  
   LinkPolicy policy,
   LinkRelationship relationship,  
   Integer offset     
  ) 
  {
    super(name);

    if(policy == null) 
      throw new IllegalArgumentException("The policy cannot be (null)!");
    pPolicy = policy;

    if(relationship == null) 
      throw new IllegalArgumentException("The link relationship cannot be (null)!");
    pRelationship = relationship;

    if(relationship == LinkRelationship.OneToOne) {
      if(offset == null) 
	throw new IllegalArgumentException
	  ("The frame index offset cannot be (null) for links with a " + 
	   "(OneToOne) relationship!");
      pFrameOffset = offset;
    }
    else if(offset != null) {
      throw new IllegalArgumentException
	("The frame index offset must be (null) for links with a (" + relationship.name() +
	 ") relationship!");
    }
  }

  /** 
   * Internal copy constructor used by both <CODE>LinkMod</CODE> and 
   * <CODE>LinkVersion</CODE> when constructing instances based off an instance 
   * of the other subclass.
   */
  protected
  LinkCommon
  (
   LinkCommon link
  ) 
  {
    super(link.getName());

    pPolicy       = link.getPolicy();
    pRelationship = link.getRelationship();
    pFrameOffset  = link.getFrameOffset();
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

  /**
   * Get the nature of the relationship between files associated with the source and 
   * target nodes. 
   */ 
  public LinkRelationship
  getRelationship()
  {
    return pRelationship;
  }
  
  /**
   * Get the frame offset to be added to frame indices of files associated with the 
   * target node to determine the frame indices of files associated with the source node. <P> 
   * 
   * @return
   *   The frame offset or <CODE>null</CODE> if the {@link #getRelationship getRelationship} 
   *   method returns anything other than {@link LinkRelationship#OneToOne OneToOne}.
   */
  public Integer
  getFrameOffset() 
  {
    if(pRelationship == LinkRelationship.OneToOne)
      return pFrameOffset;

    return null;
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
    if((obj != null) && (obj instanceof LinkCommon)) {
      LinkCommon link = (LinkCommon) obj;
      return (super.equals(obj) && 
	      (pPolicy.equals(link.pPolicy)) && 
	      (pRelationship == link.pRelationship) &&
	      (((pFrameOffset == null) && (link.pFrameOffset == null)) ||
	       ((pFrameOffset != null) && pFrameOffset.equals(link.pFrameOffset))));
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
    encoder.encode("Relationship", pRelationship);

    if(pRelationship == LinkRelationship.OneToOne)
      encoder.encode("FrameOffset", pFrameOffset);
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

    LinkRelationship relationship = (LinkRelationship) decoder.decode("Relationship");
    if(relationship == null) 
      throw new GlueException("The \"Relationship\" entry was missing or (null)!");
    pRelationship = relationship;

    if(pRelationship == LinkRelationship.OneToOne) {
      Integer offset = (Integer) decoder.decode("FrameOffset");
      if(offset == null) 
	throw new GlueException
	  ("The \"FrameOffset\" was missing or (null)!");
      pFrameOffset = offset;
    }
    else {
      pFrameOffset = null;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8234951904488951376L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The link's {@link OverallNodeState OverallNodeState} and 
   * {@link OverallQueueState OverallQueueState} propagation policy.
   */
  protected LinkPolicy  pPolicy; 

  /**
   * The nature of the relationship between files associated with the source and target 
   * nodes. 
   */
  protected LinkRelationship  pRelationship;

  /**
  * Frame offset to be added to frame indices of files associated with the target node 
  * to determine the frame indices of files associated with the source node.  This field
  * only has meaning when <CODE>pRelationship</CODE> is <CODE>OneToOne</CODE> and is 
  * <CODE>null</CODE> for all other relationships.
  */
  protected Integer  pFrameOffset;  
}



