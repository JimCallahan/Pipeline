// $Id: GlueEncoder.java,v 1.1 2004/02/12 15:50:12 jim Exp $

package us.temerity.pipeline;

import java.lang.*;
import java.lang.reflect.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   G L U E   E N C O D E R                                                                */
/*                                                                                          */
/*    Converts a set of objects into a human readable text representation.  The format      */
/*    is flexible enough to handle adding, removing and renaming of fields.  All primitive  */
/*    types and well as most of the classes in java.lang and java.util are supported        */
/*    internally.  All other classes can add GLUE support by implementing the Glueable      */
/*    interface.                                                                            */
/*------------------------------------------------------------------------------------------*/

public
class GlueEncoder
{     
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /* Encode the given object. */ 
  public 
  GlueEncoder
  (
   String title,   /* IN: title of object */ 
   Glueable obj    /* IN: the object to encode */ 
  ) 
    throws GlueError
  {
    pNextID  = 1;
    pObjects = new HashMap<Integer,HashMap<Long,Object>>();  
    pBuf     = new StringBuffer();
    pLevel   = 0;

    writeEntity(title, obj);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /* The GLUE format text. */ 
  public String 
  getText() 
  {
    return (pBuf.toString());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I / O                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /* Encode an arbitrarily typed Object as GLUE format text. */ 
  public void 
  writeEntity
  (
   String title,      /* IN: title of object */ 
   Object obj         /* IN: object to write */  
  ) 
    throws GlueError
  {
    if(obj == null) {
      pBuf.append(indent());
      if(title.length() > 0) 
	pBuf.append(title + " "); 
      pBuf.append("<NULL>\n");
      return;
    }

    Long objID = lookupID(obj);
    if(objID != null) {
      writeRef(title, obj, objID);
      return;
    }
    
    objID = insertID(obj);
    writeObject(title, obj, objID);
  }



  /*-- I/O HELPERS -------------------------------------------------------------------------*/

  /* Encode a reference to a previously encoded Object. */ 
  protected void 
  writeRef
  (
   String title,      /* IN: title of object */ 
   Object obj,        /* IN: object to write */  
   Long objID         /* IN: unique object ID */ 
  ) 
  {
    pBuf.append(indent());
    if(title.length() > 0) 
      pBuf.append(title + " "); 
    pBuf.append("<REF> #" + objID + "\n");
  }


  /* Encode the first reference to an Object. */ 
  protected void 
  writeObject
  (
   String title,      /* IN: title of object */ 
   Object obj,        /* IN: object to write */  
   Long objID         /* IN: unique object ID */ 
  ) 
    throws GlueError
  {
    /* header */ 
    pBuf.append(indent());
    if(title.length() > 0) 
      pBuf.append(title + " "); 
    pBuf.append("<");

    /* primtive wrapper types */ 
    Class cls = obj.getClass();
    if((cls == sBooleanClass) ||
       (cls == sByteClass) ||
       (cls == sShortClass) ||
       (cls == sIntegerClass) ||
       (cls == sLongClass) ||
       (cls == sFloatClass) ||
       (cls == sDoubleClass)) {
      pBuf.append(cls.getName() + "> #" + objID + " { " + obj + " }\n");
    }
    else if(cls == sCharacterClass) {
      pBuf.append(cls.getName() + "> #" + objID + " { '" + obj + "' }\n");
    }
    else if(cls == sStringClass) {
      pBuf.append(cls.getName() + "> #" + objID + " { \"" + obj + "\" }\n");
    }
      
    /* arrays */ 
    else if(cls.isArray()) {
      int dim = Array.getLength(obj);

      /* header */ 
      Class primCls = null;
      String simple = null;
      {
	String cname = cls.getName();

	simple = null;
	int depth = 0;
	{
	  char cs[] = cname.toCharArray();
	  for(depth=0; (cs[depth] == '['); depth++);
	  
	  if(cs[depth] == 'L') {
	    simple = cname.substring(depth+1, cname.length()-1);
	  }
	  else {
	    switch(cs[depth]) {
	    case 'Z':
	      primCls = Boolean.TYPE;
	      simple = "boolean";
	      break;
	      
	    case 'B':
	      primCls = Byte.TYPE;
	      simple = "byte";
	      break;
	      
	    case 'C':
	      primCls = Character.TYPE;
	      simple = "char";
	      break;
		
	    case 'S':
	      primCls = Short.TYPE;
	      simple = "short";
	      break;
	      
	    case 'I':
	      primCls = Integer.TYPE;
	      simple = "int";
	      break;

	    case 'J':
	      primCls = Long.TYPE;
	      simple = "long";
	      break;
	      
	    case 'F':
	      primCls = Float.TYPE;
	      simple = "float";
	      break;
	      
	    case 'D':
	      primCls = Double.TYPE;
	      simple = "double";
	      break;
	      
	    default:
	      assert(false);
	    }
	  }
	
	  if(depth != 1) 
	    primCls = null;
	}

	pBuf.append(simple + "[" + dim + "]");
	
	int wk;
	for(wk=1; wk<depth; wk++) 
	  pBuf.append("[]");

	pBuf.append("> #" + objID + " {\n");
      }

      /* primitive type members */ 
      pLevel++;
      if(primCls != null) {
	String s = null;
	if((primCls == Boolean.TYPE) ||
	   (primCls == Byte.TYPE) ||
	   (primCls == Short.TYPE) ||
	   (primCls == Integer.TYPE) ||
	   (primCls == Long.TYPE) ||
	   (primCls == Float.TYPE) ||
	   (primCls == Double.TYPE)) 
	  s = "";
	else if(cls == Character.TYPE) 
	  s = "'";
	else
	  assert(false);
	
	int wk;
	for(wk=0; wk<dim; wk++) {
	  Object comp = Array.get(obj, wk);
	  assert(comp != null);
	  pBuf.append(indent() + wk + " <" + simple + "> { " + s + comp + s + " }\n");
	}
      }

      /* compound type members */ 
      else {
	int wk;
	for(wk=0; wk<dim; wk++) {
	  Object comp = Array.get(obj, wk);
	  writeEntity(String.valueOf(wk), comp);
	}
      }
      pLevel--;
      
      pBuf.append(indent() + "}\n");
    }
    
    /* compound objects */ 
    else {
      /* check supported interfaces */ 
      boolean isGlueable   = false;
      boolean isCollection = false;
      boolean isMap        = false;
      {
	assert(sGlueable != null);
	Class iface[] = cls.getInterfaces();
	int wk;
	for(wk=0; wk<iface.length; wk++) { 
	  if(sGlueable.isAssignableFrom(iface[wk])) 
	    isGlueable = true;
	  else if(sCollection.isAssignableFrom(iface[wk])) 
	    isCollection = true;
	  else if(sMap.isAssignableFrom(iface[wk])) 
	    isMap = true;
	}
      }
      
      /* Glueable objects */ 
      if(isGlueable) {
	pLevel++;
	{
	  pBuf.append(cls.getName() + "> #" + objID + " {\n");
	  
	  Glueable gobj = (Glueable) obj;
	  gobj.writeGlue(this);
	}
	pLevel--;

	pBuf.append(indent() + "}\n");
      }
      
      /* Collection objects */ 
      else if(isCollection) {
	pLevel++;
	{
	  pBuf.append(cls.getName() + "> #" + objID + " {\n");
	  
	  Collection col = (Collection) obj;
	  Iterator iter = col.iterator();
	  while(iter.hasNext()) {
	    writeEntity("", iter.next());
	  }
	}
	pLevel--;
	
	pBuf.append(indent() + "}\n");
      }

      /* Map objects */ 
      else if(isMap) {
	pLevel++;
	{
	  pBuf.append(cls.getName() + "> #" + objID + " {\n");
	  
	  Map map = (Map) obj;
	  Iterator iter = map.keySet().iterator();
	  while(iter.hasNext()) {
	    Object key = iter.next();
	    Object val = map.get(key);

	    pBuf.append(indent() + "{\n");
	    pLevel++;
	    {
	      writeEntity("Key", key);
	      writeEntity("Val", val);
	    }
	    pLevel--;
	    pBuf.append(indent() + "}\n");
	  }
	}
	pLevel--;
	
	pBuf.append(indent() + "}\n");
      }
    
      /* unsupported */ 
      else {
	throw new GlueError("Unsupported Class: " + cls.getName());
      }
    }
  }

  
  /* Generate an indentation string. */ 
  protected String 
  indent() 
  {
    assert(pLevel >= 0);

    StringBuffer buf = new StringBuffer();
    int wk;
    for(wk=0; wk<pLevel; wk++) 
      buf.append("  ");

    return (buf.toString());
  }



  /*-- OBJECT IDs --------------------------------------------------------------------------*/

  /* Lookup the unique object ID for the given object. 
       Returns (null) if the object has not been previously encountered. */ 
  protected Long
  lookupID
  (
   Object obj
  ) 
  {
    Integer key = new Integer(obj.hashCode());
    HashMap<Long,Object> objs = pObjects.get(key);
    if(objs == null) 
      return null;

    Iterator iter = objs.keySet().iterator();
    while(iter.hasNext()) {
      Long objID = (Long) iter.next();
      Object sobj = objs.get(objID);
      if(obj == sobj) 
	return objID;
    }
    
    return null;
  }


  /* Insert a new object into the object table and assign it a unique ID.
      Returns the ID for the just inserted object. */ 
  protected Long
  insertID 
  (
   Object obj
  ) 
  {
    Integer key = new Integer(obj.hashCode());
    HashMap<Long,Object> objs = pObjects.get(key);
    if(objs == null) {
      objs = new HashMap<Long,Object>();
      pObjects.put(key, objs);
    }

    Long objID = new Long(pNextID);
    pNextID++;

    objs.put(objID, obj);
    
    return objID;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N I T I A L I Z A T I O N                                            */
  /*----------------------------------------------------------------------------------------*/

  {
    try {
      sGlueable       = Class.forName("us.temerity.pipeline.Glueable");
      sCollection     = Class.forName("java.util.Collection");
      sMap            = Class.forName("java.util.Map");

      sBooleanClass   = Class.forName("java.lang.Boolean");
      sByteClass      = Class.forName("java.lang.Byte");
      sShortClass     = Class.forName("java.lang.Short");
      sIntegerClass   = Class.forName("java.lang.Integer");
      sLongClass      = Class.forName("java.lang.Long");
      sFloatClass     = Class.forName("java.lang.Float");
      sDoubleClass    = Class.forName("java.lang.Double");
      sCharacterClass = Class.forName("java.lang.Character");
      sStringClass    = Class.forName("java.lang.String");
    }
    catch (ClassNotFoundException ex) {
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  protected static Class sGlueable       = null;
  protected static Class sCollection     = null;
  protected static Class sMap            = null;
    
  protected static Class sBooleanClass   = null; 
  protected static Class sByteClass      = null;  
  protected static Class sShortClass     = null; 
  protected static Class sIntegerClass   = null; 
  protected static Class sLongClass      = null; 
  protected static Class sFloatClass     = null; 
  protected static Class sDoubleClass    = null; 
  protected static Class sCharacterClass = null; 
  protected static Class sStringClass    = null;  



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  protected long  pNextID;        /* A unique identifier for the next object processed. */ 

  /** 
   * The table of encoded/decoded object tables indexed by Object.hashCode().  Each 
   * object table contains the encoded/decoded objects with the same Object.hashCode()
   * indexed by unique object IDs. 
   */ 
  protected HashMap<Integer,HashMap<Long,Object>>  pObjects;    

  
  protected StringBuffer  pBuf;   /* The buffer holding the GLUE encoded text. */ 
 
  protected int  pLevel;          /* The current nesting level. */ 
 
}



