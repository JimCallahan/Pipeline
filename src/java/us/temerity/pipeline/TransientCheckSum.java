// $Id: TransientCheckSum.java,v 1.1 2009/08/28 02:10:46 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T R A N S I E N T   C H E C K   S U M                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A read-only checksum of the data contained in a file which is augmented with a timestamp
 * of when the checksum was computed in order to validate if the underlying file has been
 * modified since that time.
 */
public 
class TransientCheckSum
  extends CheckSum 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public
  TransientCheckSum() 
  {
    super(); 
  }

  /** 
   * Construct by computing the checksum of the given file and update timestamp.
   */
  public 
  TransientCheckSum
  (
   Path path, 
   long stamp
  ) 
    throws IOException 
  {
    super(path); 
    pUpdatedOn = stamp;
  }

  /** 
   * Construct from the given checksum bytes and update timestamp. 
   */
  public 
  TransientCheckSum
  (
   byte[] bytes, 
   long stamp
  ) 
  {
    super(bytes); 
    pUpdatedOn = stamp; 
  }

  /** 
   * Copy a permanent checksum adding an update timestamp.
   */
  public  
  TransientCheckSum
  (
   CheckSum sum, 
   long stamp
  ) 
  {
    super(sum); 
    pUpdatedOn = stamp;
  }

  /** 
   * Copy constructor. 
   */
  public  
  TransientCheckSum
  (
   TransientCheckSum sum
  ) 
  {
    super(sum); 
    pUpdatedOn = sum.pUpdatedOn; 
  }

  
 
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the checksum was generated after the given timestamp (milliseconds since 
   * midnight, January 1, 1970 UTC). <P> 
   * 
   * Typically this used to test the last modification/change timestamp of the source file 
   * to see whether a new checksum needs to be computed.
   * 
   * @param stamp
   *   The timestamp being tested.  
   */ 
  public boolean
  isValidAfter
  (
   long stamp
  ) 
  {
    return (pUpdatedOn > stamp);
  }

  /**
   * Whether the given checksum was generated after the this checksum.<P> 
   * 
   * @param stamp
   *   The timestamp being tested.  
   */ 
  public boolean
  isNewerThan
  (
   TransientCheckSum sum
  ) 
  {
    return (pUpdatedOn > sum.pUpdatedOn); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the timestamp of when the checksum was generated (milliseconds since 
   * midnight, January 1, 1970 UTC). <P> 
   */ 
  public long
  getUpdatedOn() 
  {
    return pUpdatedOn; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  @Override
  public Object 
  clone()
  {
    return new TransientCheckSum(this); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder); 
    encoder.encode("UpdatedOn", pUpdatedOn); 
  }

  @Override
  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder); 
    
    Long stamp = (Long) decoder.decode("UpdatedOn"); 
    if(stamp == null) 
      throw new GlueException("The \"UpdatedOn\" was missing!");
    pUpdatedOn = stamp; 
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -9143337677322235865L;
 
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the checksum
   * was generated.
   */ 
  private long  pUpdatedOn; 

}
