// $Id: GlueDecoder.java,v 1.5 2004/02/25 02:59:28 jim Exp $

package us.temerity.pipeline;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   G L U E   D E C O D E R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Intantiates a set of objects read from a human readable text format called Glue. <P> 
 * 
 * The format is flexible enough to handle adding, removing and renaming of fields.  
 * All primitive types and well as most of the classes in java.lang and java.util are 
 * supported natively. All other classes can add Glue support by implementing the 
 * {@link Glueable Glueable} interface.
 * 
 * @see Glueable
 * @see GlueEncoder
 */
public
class GlueDecoder
{     
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Decode objects from a <CODE>String</CODE> containing Glue text.
   * 
   * @param text [<B>in</B>]
   *   The Glue format text to be decoded.
   */
  public 
  GlueDecoder
  (
   String text   
  ) 
    throws GlueException
  {
    pState = new GlueParserState();

    StringReader in = null;
    try {
      in = new StringReader(text);
      GlueParser parser = new GlueParser(in);
      pRoot = parser.Decode(this, pState);
    }
    catch(ParseException ex) {
      throw new GlueException(ex);
    }
    finally {
      in.close();
    }
  }

  /** 
   * Decode objects from an input stream of bytes containing Glue text.
   * 
   * @param stream [<B>in</B>]
   *   The input stream of bytes containing the Glue text to be decoded.
   */
  public 
  GlueDecoder
  (
   InputStream stream
  ) 
    throws GlueException
  {
    pState = new GlueParserState();

    try {
      GlueParser parser = new GlueParser(stream);
      pRoot = parser.Decode(this, pState);
    }
    catch(ParseException ex) {
      throw new GlueException(ex);
    }
    finally {
      try {
	stream.close(); 
      }
      catch(IOException ex) {
	throw new GlueException(ex);
      }
    }
  }

  /** 
   * Decode objects read from a character stream containing Glue text.
   * 
   * @param reader [<B>in</B>]
   *   The character stream reader providing the Glue text to be decoded.
   */
  public 
  GlueDecoder
  (
   Reader reader
  ) 
    throws GlueException
  {
    pState = new GlueParserState();

    try {
      GlueParser parser = new GlueParser(reader);
      pRoot = parser.Decode(this, pState);
    }
    catch(ParseException ex) {
      throw new GlueException(ex);
    }
    finally {
      try {
	reader.close();  
      }
      catch(IOException ex) {
	throw new GlueException(ex);
      }
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the top-level decoded <CODE>Object</CODE>.
   * 
   * @return
   *   The <CODE>Object</CODE> at the highest level scope within the Glue format text.
   */
  public Object 
  getObject() 
  {
    return pRoot;
  }
  

  /** 
   * Lookup an decoded <CODE>Object</CODE> with the given title from the current 
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


    
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The root decoded Object. 
   */ 
  private Object pRoot;       

  /** 
   * The parser helper class which maintains tables of objects used during decoding.  
   */
  private GlueParserState pState;
}



