// $Id: DependMod.java,v 1.1 2004/02/28 20:05:47 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   D E P E N D   M O D                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Information about the dependency relationship between a modifiable working version 
 * of a node (<CODE>NodeMod</CODE>) and the upstream node upon which it depends. <P> 
 * 
 * @see Node 
 * @see DependMod 
 */ 
public
class DependMod
  extends DependCommon
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  DependMod() 
  {
    super();
  }

  /**
   * Construct a one-to-one working version dependency on the given source node. 
   * 
   * @param name [<B>in</B>]
   *   The fully resolved name of the source node.
   * 
   * @param offset [<B>in</B>]
   *   The frame index offset.
   */ 
  public 
  DependMod
  (
   String name,   
   int offset     
  ) 
  {
    super(name, offset);
  }

  /**
   * Construct an all-to-all working version dependency on the given source node. 
   * 
   * @param name [<B>in</B>]
   *   The fully resolved name of the source node.
   */ 
  public 
  DependMod
  (
   String name
  ) 
  {
    super(name);
  }

  /**
   * Construct from a checked-in version dependency.
   */ 
  public 
  DependMod
  (
   DependVersion dep
  ) 
  {
    super(dep);
  }

  /**
   * Copy constructor. 
   */ 
  public 
  DependMod
  (
   DependMod dep   
  ) 
  {
    super(dep);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Set whether the target node depends on all frames from the source node? 
   */ 
  public void 
  setUseAllFiles
  (
   boolean tf 
  ) 
  {
    pUseAllFrames = tf;
  }  

  /**
   * Set the frame offset to be added to frame indices of files associated with the 
   * target node to determine the frame indices of files associated with the source node.
   */
  public void 
  setFrameOffset
  (
   int offset 
  ) 
  {
    assert(pUseAllFrames == false);
    pFrameOffset = offset;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 9068213687675247437L;


}



