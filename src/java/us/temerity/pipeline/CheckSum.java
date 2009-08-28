// $Id: CheckSum.java,v 1.8 2009/08/28 02:10:46 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   C H E C K   S U M                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A permanent read-only checksum of the data contained in a file.
 */
public 
class CheckSum
  implements Cloneable, Glueable, Serializable
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
  CheckSum() 
  {}

  /** 
   * Construct by computing the checksum of the given file.
   */
  public 
  CheckSum
  (
   Path path
  ) 
    throws IOException 
  {
    if(path == null) 
      throw new IllegalArgumentException("The source file path cannot be (null)!");

    LogMgr.getInstance().log
      (LogMgr.Kind.Sum, LogMgr.Level.Finer,
       "Rebuilding checksum for: " + path);

    initBytes(NativeFileSys.md5sum(path)); 
  }

  /** 
   * Construct from the given checksum bytes.
   */
  public 
  CheckSum
  (
   byte[] bytes
  ) 
  {
    initBytes(bytes); 
  }

  /** 
   * Copy constructor. 
   */
  public  
  CheckSum
  (
   CheckSum sum
  ) 
  {
    initBytes(sum.pBytes); 
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Validate and copy the given checksum bytes into the internal array. 
   */
  private void
  initBytes
  (
   byte[] bytes
  ) 
  {
    if(bytes == null) 
      throw new IllegalArgumentException
        ("The checksum bytes cannot be (null)!"); 
    
    if(bytes.length != sByteSize) 
      throw new IllegalArgumentException
        ("The number of bytes given (" + bytes.length + ") was different than the number " + 
         "of bytes expected (" + sByteSize + ")!"); 

    pBytes = Arrays.copyOf(bytes, sByteSize); 
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
  @Override
  public boolean
  equals
  (
   Object obj
  )
  {
    if((obj != null) && (obj instanceof CheckSum)) {
      CheckSum sum = (CheckSum) obj;
      return Arrays.equals(pBytes, sum.pBytes);
    }
    return false;
  }

  /**
   * Returns a hash code value for the object.
   */
  @Override
  public int 
  hashCode() 
  {
    return Arrays.hashCode(pBytes);
  }
  
  /**
   * The string representation of the checksum.
   */ 
  @Override
  public String
  toString() 
  {
    return bytesToHex(pBytes); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public Object 
  clone()
  {
    return new CheckSum(this); 
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
    encoder.encode("Bytes", bytesToHex(pBytes)); 
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    String hex = (String) decoder.decode("Bytes"); 
    if(hex == null) 
      throw new GlueException("The \"Bytes\" were missing!");
    pBytes = hexToBytes(hex); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   M E T H O D S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Read the contents of an file containing a 16-bit binary checksum.
   */ 
  public static byte[]
  readBytes
  (
   Path path
  ) 
    throws PipelineException 
  {
    byte[] bytes = new byte[sByteSize];

    try {
      FileInputStream in = new FileInputStream(path.toFile()); 
      try {
        in.read(bytes);
      }
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to read the checksum file (" + path + ")!");
      }
      finally {
        try {
          in.close();
        }
        catch (IOException ex) {
        }
      }
    } 
    catch(FileNotFoundException ex) {
      throw new PipelineException
        ("The checksum file (" + path + ") did not exist!");
    }
    catch(SecurityException ex) {
      throw new PipelineException
	("No permission to read the checksum file (" + path + ")!");
    }   

    return bytes; 
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert the given byte array to a hexidecimal string.
   */ 
  private String
  bytesToHex
  (
   byte[] bytes
  ) 
  {
    int len = bytes.length;

    char[] cs = new char[len * 2];
    int i;
    for(i=0; i < len; i++) {
      int v = (bytes[i] + 256) % 256;
      int hi = v >> 4;
      int lo = v & 0x0f;
      cs[i * 2 + 0] = sHexDigits[hi];
      cs[i * 2 + 1] = sHexDigits[lo];
    }

    return new String(cs); 
  }

  /**
   * Convert the given hexidecimal string to a byte array.
   */ 
  private byte[] 
  hexToBytes
  (
   String str
  ) 
  {
    char[] cs = str.toCharArray();
    int len = cs.length / 2;

    byte[] bytes = new byte[len];
    int i;
    for(i=0; i < len; i++) {
      int hi = Character.digit(cs[i * 2], 16);
      int lo = Character.digit(cs[i * 2 + 1], 16);
      int v = (hi << 4) | lo;
      if(v > 127)
        v -= 256;
      bytes[i] = (byte) v;
    }

    return bytes;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6931475149050337062L;
                       
  /** 
   * Size (in bytes) of checksum data.
   */ 
  private static final int sByteSize = 16; 
                         
  /**
   * The hexidecimal characters.
   */
  private static final char[] sHexDigits = { 
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' 
  };


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The checksum data.
   */ 
  private byte[]  pBytes; 

}
