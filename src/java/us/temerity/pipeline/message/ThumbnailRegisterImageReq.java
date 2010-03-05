// $Id: NodeDeleteReq.java,v 1.2 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T H U M B N A I L   R E G I S T E R   I M A G E   R E Q                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to register a thumbnail image.
 */
public
class ThumbnailRegisterImageReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param image
   *   The thumbnail image.
   */
  public
  ThumbnailRegisterImageReq
  (
   ThumbnailImage image
  )
  { 
    super();

    if(image == null) 
      throw new IllegalArgumentException
	("The thumbnail image image cannot be (null)!");
    pImage = image;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the thumbnail image.
   */
  public ThumbnailImage
  getImage() 
  {
    return pImage;
  }
  
    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1564713996838336095L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The thumbnail image.
   */ 
  private ThumbnailImage  pImage; 

}
  
