// $Id: GlueDecoder.java,v 1.1 2004/02/12 15:50:12 jim Exp $

package us.temerity.pipeline;

import java.lang.*;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   G L U E   D E C O D E R                                                                */
/*                                                                                          */
/*    Converts a set of objects from a human readable text representation.  The format      */
/*    is flexible enough to handle adding, removing and renaming of fields.  All primitive  */
/*    types and well as most of the classes in java.lang and java.util are supported        */
/*    internally.  All other classes can add GLUE support by implementing the Glueable      */
/*    interface.                                                                            */
/*------------------------------------------------------------------------------------------*/

public
class GlueDecoder
{     
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  GlueDecoder
  (
   String text   /* IN: the GLUE format text to decode */ 
  ) 
    throws GlueError
  {
    pObjects = new TreeMap<Long,Object>();

    try {
      GlueParser parser = new GlueParser(new StringReader(text));
      parser.setDecoder(this);
      pRoot = parser.Decode();
    }
    catch(ParseException ex) {
      throw new GlueError(ex);
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /* The Object decoded from the GLUE text. */ 
  public Object 
  getObject() 
  {
    return pRoot;
  }
  

  /* Lookup an decoded Object with the given title.  
     This method is used by Glueable objects to initialize their fields in readGlue(). 
       Returns (null) if the field cannot be found. */ 
  public Object
  readEntity
  ( 
   String title  /* IN: title of object */ 
  ) 
  {
    return pCurTable.get(title);
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   P A R S I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /* Used by the GLUE parser to initialize the table before readGlue() is called. */ 
  public void 
  setCurrentTable
  (
   HashMap table
  ) 
  {
    assert(table != null);
    pCurTable = table;
  }

  
  /* Used by the parser to set the decoded Object. */ 
  public void 
  setRootObject
  (
   Object obj
  ) 
  {
    assert(obj != null);
    pRoot = obj;
  }


  /* Create a new instance of the given class and store it in the objects table. */ 
  public Object 
  newInstance
  (
   Long objID,
   Class cls
  ) 
    throws ParseException
  {
    Object obj = null;
    try {
      obj = cls.newInstance();
    }
    catch(Exception ex) {
      throw new ParseException(ex.toString());
    }
    
    if(pObjects.containsKey(objID))
      throw new ParseException("Duplicate object ID (" + objID + " encountered!");
    pObjects.put(objID, obj);

    return obj;
  }

  public Object 
  newArrayInstance
  (
   Long objID,
   Class cls,
   int size
  ) 
    throws ParseException
  {
    Object obj = null;
    try {
      obj = Array.newInstance(cls, size);
    }
    catch(Exception ex) {
      throw new ParseException(ex.toString());
    }
    
    if(pObjects.containsKey(objID))
      throw new ParseException("Duplicate object ID (" + objID + " encountered!");
    pObjects.put(objID, obj);

    return obj;
  }


  /* Add the given object to the objects table. */ 
  public void 
  addObject
  (
   Long objID,
   Object obj
  ) 
    throws ParseException
  {
    assert(obj != null);
    if(pObjects.containsKey(objID)) 
      throw new ParseException("Duplicate object ID (" + objID + " encountered!");
    pObjects.put(objID, obj);
  }

  
  /* Lookup a previous instantiated object. */ 
  public Object 
  lookupObject
  (
   Long objID
  )
  {
    return pObjects.get(objID);
  }
  
    
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  protected  HashMap  pCurTable;   /* Current object table index by title (String) */ 
  protected  Object   pRoot;       /* The decoded Object. */ 
  
  protected TreeMap<Long,Object>  pObjects;  /* Created objects indexed by object ID. */ 
}



