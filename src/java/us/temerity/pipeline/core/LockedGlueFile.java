// $Id: LockedGlueFile.java,v 1.2 2004/05/16 19:05:28 jim Exp $

package us.temerity.pipeline.core;

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
	throw new GlueLockException("Unable to aquire lock for Glue file (" + file + ")!");
      try {
	long size = chan.size();
	ByteBuffer buf = ByteBuffer.allocate((int) size);
	int read = chan.read(buf);
	GlueDecoder decoder = new GlueDecoderImpl(new String(buf.array()));
	return decoder.getObject();	
      }
      finally {
	lock.release();
	chan.close();
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw new GlueException("Unable to load Glue file (" + file + ")!", ex);
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
    Glueable obj 
  ) 
    throws GlueException, GlueLockException 
  {
    try {
      file.delete();
      FileChannel chan = new RandomAccessFile(file, "rw").getChannel();
      FileLock lock = chan.tryLock();
      if(lock == null) 
	throw new GlueLockException("Unable to aquire lock for Glue file (" + file + ")!");
      try {
	GlueEncoder ge = new GlueEncoderImpl(title, obj);	
	ByteBuffer buf = ByteBuffer.wrap(ge.getText().getBytes());
	chan.write(buf);
      }
      finally {
	lock.release();
	chan.close();
      }
    }
    catch (Exception ex) {
      throw new GlueException("Unable to save Glue file (" + file + "):\n" + 
			      "  " + ex);
    }
  }
}
