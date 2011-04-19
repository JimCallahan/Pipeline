// $Id: LockedGlueFile.java,v 1.5 2008/06/29 17:46:16 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

/*------------------------------------------------------------------------------------------*/
/*   L O C K E D   G L U E   F I L E                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A set of static methods which provide file locking for a Glue format file used to 
 * maintain persistent storage for a heirarchy of Objects.
 */ 
public
class LockedGlueFile
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Decode the <CODE>Object</CODE> heirarchy read from a locked Glue format file.
   * 
   * @param file
   *   The Glue format file.
   * 
   * @throws GlueException
   *   If unable to read and decode the file.
   * 
   * @throws GlueLockException
   *   If the file is currently locked by another process.
   */
  public static Object
  load
  (
   File file
  ) 
    throws GlueException, GlueLockException 
  {
    try {
      FileChannel chan = new RandomAccessFile(file, "rw").getChannel();
      FileLock lock = chan.tryLock();
      if(lock == null) 
	throw new GlueLockException("Unable to acquire lock for Glue file (" + file + ")!");

      try {
	long size = chan.size();
	ByteBuffer buf = ByteBuffer.allocate((int) size);
	int read = chan.read(buf);
        
        return GlueDecoderImpl.decodeString("???", new String(buf.array()));
      }
      finally {
	lock.release();
	chan.close();
      }
    }
    catch(GlueException ex) {
      throw ex;
    }
    catch(GlueLockException ex) {
      throw ex;
    }
    catch(IOException ex) {
      String msg = 
        ("I/O ERROR: \n" + 
         "  While reading from file (" + file + ").\n" +
         "    " + ex.getMessage());
      LogMgr.getInstance().log(LogMgr.Kind.Glu, LogMgr.Level.Severe, msg); 
      throw new GlueException(msg);
    }
    catch (Exception ex) {
      String msg = Exceptions.getFullMessage("INTERNAL ERROR:", ex, true, true);
      LogMgr.getInstance().log(LogMgr.Kind.Glu, LogMgr.Level.Severe, msg); 
      throw new GlueException(msg);  
    }
  }

  /** 
   * Encode and save the given <CODE>Object</CODE> heirarchy into a locked Glue format file.
   * 
   * @param file
   *   The Glue format file.
   * 
   * @param title 
   *   The name to be given to the object when encoded.
   * 
   * @param obj 
   *   The <CODE>Object</CODE> to be encoded.
   * 
   * @throws GlueException
   *   If unable to encode and save the file.
   * 
   * @throws GlueLockException
   *   If the file is currently locked by another process.
   */
  public static void 
  save
  (
    File file, 
    String title,  
    Object obj 
  ) 
    throws GlueException, GlueLockException 
  {
    try {
      file.delete();
      FileChannel chan = new RandomAccessFile(file, "rw").getChannel();
      FileLock lock = chan.tryLock();
      if(lock == null) 
	throw new GlueLockException("Unable to acquire lock for Glue file (" + file + ")!");

      try {
        String text = GlueEncoderImpl.encodeString(title, obj);
	ByteBuffer buf = ByteBuffer.wrap(text.getBytes());
	chan.write(buf);
      }
      finally {
	lock.release();
	chan.close();
      }
    }
    catch(GlueException ex) {
      throw ex;
    }
    catch(GlueLockException ex) {
      throw ex;
    }
    catch(IOException ex) {
      String msg = 
        ("I/O ERROR: \n" + 
         "  While writing to file (" + file + ").\n" +
         "    " + ex.getMessage());
      LogMgr.getInstance().log(LogMgr.Kind.Glu, LogMgr.Level.Severe, msg); 
      throw new GlueException(msg);
    }
    catch (Exception ex) {
      String msg = Exceptions.getFullMessage("INTERNAL ERROR:", ex, true, true);
      LogMgr.getInstance().log(LogMgr.Kind.Glu, LogMgr.Level.Severe, msg); 
      throw new GlueException(msg);  
    }
  }
}
