// $Id: GlueParserState.java,v 1.5 2004/05/08 23:28:49 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.glue.*;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   G L U E   P A R S E R   S T A T E                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * A helper class used by {@link us.temerity.pipeline.GlueDecoder GlueDecoder} to communicate 
 * with the parser during translation of Glue format text into Objects.
 */ 
class GlueParserState
{     
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  GlueParserState() 
  {
    pMasterTable = new TreeMap<Long,Object>();
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Lookup an decoded <CODE>Object</CODE> with the given title from the current object table.
   *
   * @param title  
   *   The name of the object to lookup.
   * 
   * @return 
   *   The decoded <CODE>Object</CODE> or <CODE>null</CODE> if no object with the given 
   *   title can be found. 
   */ 
  public Object
  lookupCurrent
  ( 
   String title 
  ) 
  {
    return pCurTable.get(title);
  }
 
  /**
   * Used by the parser to initialize the current object table before 
   * <CODE>Glueable.fromGlue()</CODE> is called. 
   * 
   * @param table  
   *   The <CODE>HashMap</CODE> containing the decoded objects at the current level 
   *   indexed by object title <CODE>String</CODE>.
   */ 
  public void 
  setCurrentTable
  (
   HashMap table
  ) 
  {
    assert(table != null);
    pCurTable = table;
  }

  /** 
   * Used by the parser to create a new instance of the given class and store it in 
   * the master objects table under the given ID.
   * 
   * @param objID 
   *   A unique ID used to index objects in the master object table.
   * 
   * @param cls 
   *   The class to instantiate.
   * 
   * @return 
   *   The newly instantiated object.
   */
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
    
    if(pMasterTable.containsKey(objID))
      throw new ParseException("Duplicate object ID (" + objID + " encountered!");
    pMasterTable.put(objID, obj);

    return obj;
  }

  /** 
   * Used by the parser to create a new array with elements of the given class and 
   * store it in the master objects table under the given ID.
   * 
   * @param objID 
   *   A unique ID used to index objects in the master object table.
   * 
   * @param cls 
   *   The class to instantiate.
   * 
   * @param size 
   *   The number of elements in the array. 
   * 
   * @param depth 
   *   The number of levels of indirection.
   * 
   * @return 
   *   The newly instantiated array.
   */
  public Object 
  newArrayInstance
  (
   Long objID,
   Class cls,
   int size, 
   int depth
  ) 
    throws ParseException
  {
    if(pMasterTable.containsKey(objID))
      throw new ParseException("Duplicate object ID (" + objID + " encountered!");

    Object obj = null;
    try {
      if(depth == 1) {
	obj = Array.newInstance(cls, size);
      }
      else if(depth > 1) {
	StringBuffer buf = new StringBuffer();

	int wk;
	for(wk=1; wk<depth; wk++) 
	  buf.append("[");

	if(cls.isPrimitive()) {
	  if(cls == Boolean.TYPE) 
	    buf.append("Z");
	  else if(cls == Byte.TYPE) 
	    buf.append("B");
	  else if(cls == Short.TYPE) 
	    buf.append("S");
	  else if(cls == Integer.TYPE) 
	    buf.append("I");
	  else if(cls == Long.TYPE) 
	    buf.append("J");
	  else if(cls == Float.TYPE) 
	    buf.append("F");
	  else if(cls == Double.TYPE) 
	    buf.append("D");
	  else if(cls == Character.TYPE) 
	    buf.append("C");
	  else 
	    assert(false);
	}
	else {
	  buf.append("L" + cls.getName() + ";");
	}

	Class acls = Class.forName(buf.toString());
	obj = Array.newInstance(acls, size);
      }
      else {
	throw new NegativeArraySizeException("Array depth (" + depth + ") must be positive!");
      }
    }
    catch(Exception ex) {
      throw new ParseException(ex.toString());
    }
    
    pMasterTable.put(objID, obj);

    return obj;
  }


  /** 
   * Used by the parser to insert a parser created object into the master objects 
   * table under the given ID.
   * 
   * @param objID 
   *   A unique ID used to index objects in the master object table.
   *   
   * @param obj 
   *   The object to insert.
   */
  public void 
  insertObject
  (
   Long objID,
   Object obj
  ) 
    throws ParseException
  {
    assert(obj != null);
    if(pMasterTable.containsKey(objID)) 
      throw new ParseException("Duplicate object ID (" + objID + " encountered!");
    pMasterTable.put(objID, obj);
  }

  
  /**
   * Used by the parser to lookup a previously instantiated objects from the master object 
   * table.
   * 
   * @param objID 
   *   A unique ID used to index objects in the master object table.
   */ 
  public Object 
  lookupObject
  (
   Long objID
  )
  {
    return pMasterTable.get(objID);
  }
      
    
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Current object table which contains decoded objects at the current level indexed
   * by title <CODE>String</CODE>
   */ 
  private HashMap  pCurTable;   

  /**
   * The master table of all objects instantiated during the decoding of the Glue text
   * indexed by unique object ID.
   */
  private TreeMap<Long,Object>  pMasterTable;  
}



