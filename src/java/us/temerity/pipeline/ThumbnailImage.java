// $Id: NodeID.java,v 1.14 2008/06/15 01:59:49 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;
import java.net.*; 
import java.awt.image.*;
import java.awt.color.*; 

import javax.imageio.*;

/*------------------------------------------------------------------------------------------*/
/*   T H U M B N A I L   I M A G E                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A serializable PNG format thumbnail image with RGBA color model.
 */
public
class ThumbnailImage 
  implements Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct from an image file.
   */
  public
  ThumbnailImage
  ( 
   File image
  )
    throws IOException 
  {
    this(ImageIO.read(image));
  }

  /** 
   * Construct from an image URL. 
   */
  public
  ThumbnailImage
  ( 
   URL image
  )
    throws IOException 
  {
    this(ImageIO.read(image));
  }

  /** 
   * Construct from a buffered image. 
   */
  public
  ThumbnailImage
  ( 
   BufferedImage image
  ) 
  {
    if(image == null) 
      throw new IllegalArgumentException
        ("The thumbnail image cannot be (null)!");  
    
    if((image.getType() != BufferedImage.TYPE_CUSTOM) && 
       (image.getType() != BufferedImage.TYPE_4BYTE_ABGR)) 
      throw new IllegalArgumentException
        ("The thumbnail image type (" + image.getType() + ") is not supported!"); 
    
    ColorModel model = image.getColorModel();
    if(!model.hasAlpha()) 
      throw new IllegalArgumentException
        ("The thumbnail image must have an Alpha channel!"); 

    ColorSpace space = model.getColorSpace(); 
    if(space.getType() != ColorSpace.TYPE_RGB) 
      throw new IllegalArgumentException
        ("The thumbnail image must have an RGB color space!"); 

    pImage = image;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the image data.
   */ 
  public BufferedImage
  getImage()  
  {
    return pImage; 
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   S E R I A L I Z A B L E                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the serializable fields to the object stream. 
   */ 
  private void 
  writeObject
  (
   java.io.ObjectOutputStream out
  )
    throws IOException
  {
    ImageIO.write(pImage, "png", ImageIO.createImageOutputStream(out));
  }

  /**
   * Read the serializable fields from the object stream. 
   */ 
  private void 
  readObject
  (
   java.io.ObjectInputStream in
  )
    throws IOException, ClassNotFoundException
  {
    pImage = ImageIO.read(ImageIO.createImageInputStream(in)); 
  }
      
      

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7179942979626476692L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
      
  /**
   * The underlying image data.
   */ 
  private BufferedImage  pImage; 

}

