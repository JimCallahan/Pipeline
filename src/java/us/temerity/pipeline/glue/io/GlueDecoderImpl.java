// $Id: GlueDecoderImpl.java,v 1.7 2009/10/01 03:39:16 jlee Exp $

package us.temerity.pipeline.glue.io;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.parser.*;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   G L U E   D E C O D E R   I M P L                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Intantiates a set of objects read from Glue format text files. <P> 
 * 
 * The Glue format is flexible enough to handle adding, removing and renaming of fields.  
 * All primitive types and well as most of the classes in java.lang and java.util are 
 * supported natively. All other classes can add Glue support by implementing the 
 * {@link Glueable Glueable} interface.
 * 
 * @see Glueable
 * @see GlueEncoder
 */
public
class GlueDecoderImpl
  implements GlueDecoder
{     
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Initialize a new Glue decoder.
   */
  private  
  GlueDecoderImpl() 
  {
    pState = new GlueParserState();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   M E T H O D S                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * A static convience method for decoding an object from a String.<P> 
   * 
   * All exceptions are logged using LogMgr internally and then rethrown as GlueException
   * with the same message as written to the log.
   * 
   * @param title 
   *   The name to be given to the object when decoded.
   * 
   * @param text 
   *   The Glue format text to be decoded.
   * 
   * @throws GlueException 
   *   If unable to decode the string.
   */
  public static Object
  decodeString
  (
   String title,  
   String text
  ) 
    throws GlueException
  {
    try {
      GlueDecoderImpl gd = new GlueDecoderImpl();
      StringReader in = null;
      try {
        in = new StringReader(text);
        GlueParser parser = new GlueParser(in);
        return parser.Decode(gd, gd.getState());
      }
      catch(ParseException ex) {
        throw new GlueException(ex);
      }
      catch(TokenMgrError ex) {
        throw new GlueException(ex);        
      }
      finally {
        in.close();
      }
    }
    catch(GlueException ex) {
      String msg = 
        ("Unable to Glue decode: " + title + "\n" + 
         "  " + ex.getMessage());
      LogMgr.getInstance().log(LogMgr.Kind.Glu, LogMgr.Level.Severe, msg); 
      throw new GlueException(msg);
    }
    catch(Exception ex) {
      String msg = Exceptions.getFullMessage("INTERNAL ERROR:", ex, true, true);
      LogMgr.getInstance().log(LogMgr.Kind.Glu, LogMgr.Level.Severe, msg); 
      throw new GlueException(msg);      
    }
  }

  /** 
   * A static convience method for decoding an object from a String.<P> 
   * 
   * All exceptions are logged using LogMgr internally and then rethrown as GlueException
   * with the same message as written to the log.
   * 
   * @param title 
   *   The name to be given to the object when decoded.
   * 
   * @param bytes
   *   The Glue formated data to be decoded.
   * 
   * @throws GlueException 
   *   If unable to decode the string.
   */
  public static Object
  decodeBytes
  (
   String title,  
   byte bytes[]
  ) 
    throws GlueException
  {
    try {
      GlueDecoderImpl gd = new GlueDecoderImpl();
      InputStream in = null;
      try {
        in = new ByteArrayInputStream(bytes);
        GlueParser parser = new GlueParser(in);
        return parser.Decode(gd, gd.getState());
      }
      catch(ParseException ex) {
        throw new GlueException(ex);
      }
      catch(TokenMgrError ex) {
        throw new GlueException(ex);        
      }
      finally {
        in.close();
      }
    }
    catch(GlueException ex) {
      String msg = 
        ("Unable to Glue decode: " + title + "\n" + 
         "  " + ex.getMessage());
      LogMgr.getInstance().log(LogMgr.Kind.Glu, LogMgr.Level.Severe, msg); 
      throw new GlueException(msg);
    }
    catch(Exception ex) {
      String msg = Exceptions.getFullMessage("INTERNAL ERROR:", ex, true, true);
      LogMgr.getInstance().log(LogMgr.Kind.Glu, LogMgr.Level.Severe, msg); 
      throw new GlueException(msg);      
    }
  }

  /** 
   * A static convience method for decoding an object from the given file.<P> 
   * 
   * All exceptions are logged using LogMgr internally and then rethrown as GlueException
   * with the same message as written to the log.
   * 
   * @param title 
   *   The name to be given to the object when decoded.
   * 
   * @param file
   *   The Glue format file to be decoded.
   * 
   * @throws GlueException 
   *   If unable to decode the string.
   */
  public static Object
  decodeFile
  (
   String title,  
   File file
  ) 
    throws GlueException
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Glu, LogMgr.Level.Finest,
       "Reading " + title + ": " + file); 
    
    try {
      InputStream in = null;
      try {
        in = new BufferedInputStream(new FileInputStream(file));
      }
      catch(IOException ex) {
        String msg = 
          ("I/O ERROR: \n" + 
           "  Unable to open file (" + file + ") to decode: " + title + "\n" + 
           "    " + ex.getMessage());
        LogMgr.getInstance().log(LogMgr.Kind.Glu, LogMgr.Level.Severe, msg); 
        throw new GlueException(msg);
      }

      try {
        GlueDecoderImpl gd = new GlueDecoderImpl();
        GlueParser parser = new GlueParser(in, "UTF-8");
        return parser.Decode(gd, gd.getState());
      }
      catch(ParseException ex) {
        throw new GlueException(ex);
      }
      catch(TokenMgrError ex) {
        throw new GlueException(ex);        
      }
      finally {
        in.close(); 
      }
    }
    catch(IOException ex) {
      String msg = 
        ("I/O ERROR: \n" + 
         "  While reading from file (" + file + ") during Glue decoding of: " + title + "\n" +
         "    " + ex.getMessage());
      LogMgr.getInstance().log(LogMgr.Kind.Glu, LogMgr.Level.Severe, msg); 
      throw new GlueException(msg);
    }
    catch(GlueException ex) {
      String msg = 
        ("While reading from file (" + file + "), unable to Glue decode: " + title + "\n" + 
         "  " + ex.getMessage());
      LogMgr.getInstance().log(LogMgr.Kind.Glu, LogMgr.Level.Severe, msg); 
      throw new GlueException(msg);
    }
    catch(Exception ex) {
      String msg = Exceptions.getFullMessage("INTERNAL ERROR:", ex, true, true);
      LogMgr.getInstance().log(LogMgr.Kind.Glu, LogMgr.Level.Severe, msg); 
      throw new GlueException(msg);      
    }
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Lookup parser state.
   */ 
  private GlueParserState
  getState()
  {
    return pState;
  }
  
  /** 
   * Lookup a decoded <CODE>Object</CODE> with the given title from the current 
   * Glue scope. <P> 
   * 
   * This method is used by objects implementing the {@link Glueable Glueable} interface 
   * to initialize their fields from within 
   * {@link Glueable#fromGlue(GlueDecoder) Glueable.fromGlue}.
   * 
   * @return
   *   The decoded <CODE>Object</CODE> or <CODE>null</CODE> if no object with the given 
   *   title exists at the current Glue scope.
   */ 
  public Object
  decode
  ( 
   String title 
  ) 
  {
    return pState.lookupCurrent(title);
  }

  /** 
   * Check whether a decoded Object with the given title exists in the current 
   * Glue scope. <P>
   * 
   * This method is used by objects implementing the {@link Glueable Glueable} interface 
   * to initialize their fields from within 
   * {@link Glueable#fromGlue(GlueDecoder) Glueable.fromGlue}.
   * 
   * @return
   *   True  if an Object is available or 
   *   false if no object with the given title exists at the current Glue scope.
   */
  public boolean
  exists
  (
   String title
  )
  {
    return pState.checkCurrent(title);
  }


    
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * The parser helper class which maintains tables of objects used during decoding.  
   */
  private GlueParserState pState;
}



