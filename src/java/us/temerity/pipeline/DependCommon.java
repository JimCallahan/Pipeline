// $Id: DependCommon.java,v 1.1 2004/02/28 20:05:47 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   D E P E N D   C O M M O N                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The superclass of <CODE>DependVersion</CODE> and <CODE>DependMod</CODE> which provides
 * the common fields and methods needed by both classes. <P>
 * 
 * @see DependVersion
 * @see DependMod 
 * @see NodeVersion
 * @see NodeMod
 */
public
class DependCommon
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  protected
  DependCommon() 
  {
    super();
  }

  /**
   * Internal constructor used by <CODE>DependMod</CODE> to construct a one-to-one 
   * working version dependency on the given source node. 
   * 
   * @param name [<B>in</B>]
   *   The fully resolved name of the source node.
   * 
   * @param offset [<B>in</B>]
   *   The frame index offset.
   */ 
  protected
  DependCommon
  (
   String name,   
   int offset     
  ) 
  {
    super(name);

    pUseAllFrames = false;
    pFrameOffset = offset;
  }

  /**
   * Internal constructor used by <CODE>DependMod</CODE> to construct an all-to-all 
   * working version dependency on the given source node. 
   * 
   * @param name [<B>in</B>]
   *   The fully resolved name of the source node.
   */ 
  protected
  DependCommon
  (
   String name
  ) 
  {
    super(name);

    pUseAllFrames = true;
    pFrameOffset  = 0;
  }

  /** 
   * Internal copy constructor used by both <CODE>DependMod</CODE> and 
   * <CODE>DependVersion</CODE> when constructing instances based off an instance 
   * of the other subclass.
   */
  protected
  DependCommon
  (
   DependCommon mdep   
  ) 
  {
    super(mdep.getName());

    pUseAllFrames = mdep.useAllFrames();
    pFrameOffset  = mdep.getFrameOffset();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the target node depend on all frames from the source node? 
   */ 
  public boolean 
  useAllFrames()
  {
    return pUseAllFrames;
  }
  
  /**
   * Get the frame offset to be added to frame indices of files associated with the 
   * target node to determine the frame indices of files associated with the source node.
   */
  public int 
  getFrameOffset() 
  {
    assert(pUseAllFrames == false);
    return pFrameOffset;
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
    if((obj != null) && (obj instanceof DependCommon)) {
      DependCommon dep = (DependCommon) obj;
      return (super.equals(obj) && 
	      (pUseAllFrames == dep.pUseAllFrames) &&
	      (pFrameOffset == dep.pFrameOffset));
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

    encoder.encode("UseAllFrames", pUseAllFrames);

    if(!pUseAllFrames) 
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

    Boolean useAll = (Boolean) decoder.decode("UseAllFrames");
    if(useAll == null) 
      throw new GlueException("The \"UseAllFrames\" entry was missing or (null)!");
    pUseAllFrames = useAll;

    if(!pUseAllFrames) {
      Integer offset = (Integer) decoder.decode("FrameOffset");
      if(offset == null) 
	throw new GlueException
	  ("The \"FrameOffset\" was missing or (null), yet \"UseAllFrames\" was (false)!");
      pFrameOffset = offset;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2793253706094747704L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the target node depend on all frames from the source node?  <P> 
   * 
   * If this field is <CODE>false</CODE>, then there is a one-to-one dependency 
   * relationship between files associated with the target node and files associated 
   * with the source node.  If this field is <CODE>true</CODE>, then every frame of the 
   * targets node depends on every frame of the source node.
   */
  protected boolean  pUseAllFrames;  

  /**
  * Frame offset to be added to frame indices of files associated with the target node 
  * to determine the frame indices of files associated with the source node.  This field
  * only has meaning when <CODE>pUseAllFrames</CODE> is <CODE>true</CODE>.
  */
  protected int  pFrameOffset;  
}



