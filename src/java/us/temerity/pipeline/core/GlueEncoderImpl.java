// $Id: GlueEncoderImpl.java,v 1.9 2006/11/22 09:08:00 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.glue.*;

import java.lang.reflect.*;
import java.text.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   G L U E   E N C O D E R   I M P L                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Converts a set of objects into Glue format text files. <P> 
 * 
 * The Glue format is flexible enough to handle adding, removing and renaming of fields.  
 * All primitive types and well as most of the classes in java.lang and java.util are 
 * supported natively. All other classes can add Glue support by implementing the 
 * {@link Glueable Glueable} interface.
 * 
 * @see Glueable
 * @see GlueDecoder
 */
public
class GlueEncoderImpl
  implements GlueEncoder
{     
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Encode the heirarchy of objects reachable from the given object into Glue format text.
   * 
   * @param title 
   *   The name to be given to the object when encoded.
   * 
   * @param obj 
   *   The <CODE>Object</CODE> to be encoded.
   */
  public 
  GlueEncoderImpl
  (
   String title,  
   Object obj   
  ) 
    throws GlueException
  {
    pNextID  = 1;
    pObjects = new HashMap<Integer,HashMap<Long,Object>>();  
    pBuf     = new StringBuffer();
    pLevel   = 0;

    encode(title, obj);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Gets a <CODE>String</CODE> containing the Glue representation of the encoded objects.
   */
  public String 
  getText() 
  {
    return (pBuf.toString());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I / O                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Encode an arbitrarily typed Object as Glue format text at the current Glue scope. <P> 
   * 
   * This method is used by objects implementing the {@link Glueable Glueable} interface 
   * to encode their fields from within 
   * {@link Glueable#toGlue(GlueEncoder) Glueable.toGlue}.
   * 
   * @param title 
   *   The name to be given to the object when encoded.
   * 
   * @param obj 
   *   The <CODE>Object</CODE> to be encoded.
   */ 
  public void 
  encode
  (
   String title,    
   Object obj       
  ) 
    throws GlueException
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
      writeRef(title, objID);
      return;
    }
    
    objID = insertID(obj);
    writeObject(title, obj, objID);
  }



  /*-- I/O HELPERS -------------------------------------------------------------------------*/

  /**
   * Encode a reference to a previously encoded Object. 
   * 
   * @param title 
   *   The name to be given to the object when encoded.
   * 
   * @param objID 
   *   The unique ID of the object.
   */ 
  private void 
  writeRef
  (
   String title,  
   Long objID     
  ) 
  {
    pBuf.append(indent());
    if(title.length() > 0) 
      pBuf.append(title + " "); 
    pBuf.append("<REF> #" + objID + "\n");
  }

  /**
   * Encode the first reference to an Object. 
   * 
   * @param title 
   *   The name to be given to the object when encoded.
   * 
   * @param obj 
   *   The <CODE>Object</CODE> to be encoded.
   *
   * @param objID 
   *   The unique ID of the object.
   */ 
  private void 
  writeObject
  (
   String title, 
   Object obj,   
   Long objID    
  ) 
    throws GlueException
  {
    /* header */ 
    pBuf.append(indent());
    if(title.length() > 0) 
      pBuf.append(title + " "); 
    pBuf.append("<");

    /* primtive wrapper types */ 
    Class cls = obj.getClass();
    String cshort = shortName(cls.getName());
    if((cls == sBooleanClass) ||
       (cls == sByteClass) ||
       (cls == sShortClass) ||
       (cls == sIntegerClass) ||
       (cls == sLongClass)) {
      pBuf.append(cshort + "> #" + objID + " { " + obj + " }\n");
    }
    else if(cls == sFloatClass) {
      pBuf.append(cshort + "> #" + objID + " { " + sFloatFormat.format((Float) obj) + " }\n");
    }
    else if(cls == sDoubleClass) {
      pBuf.append(cshort + "> #" + objID + " { " + sDoubleFormat.format((Double) obj) + " }\n");
    }
    else if(cls == sCharacterClass) {
      Character c = (Character) obj;
      pBuf.append(cshort + "> #" + objID + " { " + ((int) c.charValue()) + " }\n");
    }
    else if(cls == sStringClass) {
      pBuf.append(cshort + "> #" + objID + " { \"");

      char cs[] = ((String) obj).toCharArray();
      int wk;
      for(wk=0; wk<cs.length; wk++) {
	if(cs[wk] == '"') 
	  pBuf.append("\\");
	pBuf.append(cs[wk]);
      }

      pBuf.append("\" }\n");
    }
    else if(cls.isEnum()) {
      Enum e = (Enum) obj;
      pBuf.append(cshort + "> #" + objID + " { :" + e.name() + ": }\n");
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
	    simple = shortName(cname.substring(depth+1, cname.length()-1));
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
	      throw new IllegalStateException("Unknown array type (" + cs[depth] + ")!"); 
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
	int wk;
	for(wk=0; wk<dim; wk++) {
	  Object comp = Array.get(obj, wk);
	  if(comp == null)
	    throw new IllegalStateException();

	  pBuf.append(indent() + wk + " <" + simple + "> { ");

	  if((primCls == Boolean.TYPE) ||
	     (primCls == Byte.TYPE) ||
	     (primCls == Short.TYPE) ||
	     (primCls == Integer.TYPE) ||
	     (primCls == Long.TYPE)) {
	    pBuf.append(comp.toString());
	  }
	  else if(primCls == Float.TYPE) {
	    pBuf.append(sFloatFormat.format((Float) comp));
	  }
	  else if(primCls == Double.TYPE) {
	    pBuf.append(sDoubleFormat.format((Double) comp));
	  }
	  else if(primCls == Character.TYPE) {
	    pBuf.append("'" + comp + "'");
	  }
	  else {
	    throw new IllegalStateException("Unknown primitive type (" + primCls + ")!");
	  }

	  pBuf.append(" }\n");
	}
      }

      /* compound type members */ 
      else {
	int wk;
	for(wk=0; wk<dim; wk++) {
	  Object comp = Array.get(obj, wk);
	  encode(String.valueOf(wk), comp);
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
	if(sGlueable.isAssignableFrom(cls))
	  isGlueable = true;
	else if(sCollection.isAssignableFrom(cls))
	  isCollection = true;
	else if(sMap.isAssignableFrom(cls))
	  isMap = true;
      }
      
      /* Glueable objects */ 
      if(isGlueable) {
	pLevel++;
	{
	  pBuf.append(cshort + "> #" + objID + " {\n");
	  
	  Glueable gobj = (Glueable) obj;
	  gobj.toGlue(this);
	}
	pLevel--;

	pBuf.append(indent() + "}\n");
      }
      
      /* Collection objects */ 
      else if(isCollection) {
	pLevel++;
	{
	  pBuf.append(cshort + "> #" + objID + " {\n");
	  
	  Collection col = (Collection) obj;
	  Iterator iter = col.iterator();
	  while(iter.hasNext()) {
	    encode("", iter.next());
	  }
	}
	pLevel--;
	
	pBuf.append(indent() + "}\n");
      }

      /* Map objects */ 
      else if(isMap) {
	pLevel++;
	{
	  pBuf.append(cshort + "> #" + objID + " {\n");
	  
	  Map map = (Map) obj;
	  Iterator iter = map.keySet().iterator();
	  while(iter.hasNext()) {
	    Object key = iter.next();
	    Object val = map.get(key);

	    pBuf.append(indent() + "{\n");
	    pLevel++;
	    {
	      encode("Key", key);
	      encode("Val", val);
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
	throw new GlueException("Unsupported Class: " + cls.getName());
      }
    }
  }

  
  /**
   * Generate an indentation string. 
   */ 
  private String 
  indent() 
  {
    if(pLevel < 0)
      throw new IllegalStateException("Indent level (" + pLevel + ") cannot be negative!"); 

    StringBuilder buf = new StringBuilder();
    int wk;
    for(wk=0; wk<pLevel; wk++) 
      buf.append("  ");

    return (buf.toString());
  }



  /*-- OBJECT IDs --------------------------------------------------------------------------*/

  /** 
   * Lookup the unique object ID for the given object. 
   * 
   * @return 
   *   The object's ID or <CODE>null</CODE> if the object has not been previously 
   *   encountered. 
   */ 
  private Long
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


  /**
   * Insert a new object into the object table and assign it a unique ID.
   * 
   * @param obj 
   *   The <CODE>Object</CODE> to be inserted.
   * 
   * @return 
   *   The ID assigned to the inserted object.
   */
  private Long
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

  static {
    try {
      sGlueable       = Class.forName("us.temerity.pipeline.glue.Glueable");
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
      throw new IllegalStateException
	("Unable to find atomic GLUE classes:\n  " + 
	 ex.toString());
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Strips the package prefix off of commonly used class names.
   * 
   * @param cname 
   *   The full name of the class.
   */
  private String 
  shortName
  (
   String cname
  ) 
  {
    int wk;
    for(wk=0; wk<sPackages.length; wk++) 
      if(cname.startsWith(sPackages[wk])) 
	return cname.substring(sPackages[wk].length() + 1);

    return cname;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Cached class objects for types and interfaces which are tested frequently.
   */
  private static Class sGlueable;       
  private static Class sCollection;     
  private static Class sMap;            
    
  private static Class sBooleanClass;   
  private static Class sByteClass;       
  private static Class sShortClass;     
  private static Class sIntegerClass;   
  private static Class sLongClass;      
  private static Class sFloatClass;     
  private static Class sDoubleClass;    
  private static Class sCharacterClass; 

  private static Class sStringClass;     

  /**
   * The package prefixes to use when resolving simple class names. 
   */ 
  private static final String sPackages[] = {
    "java.lang", 
    "java.util", 
    "us.temerity.pipeline"
  };

  /** 
   * Floating point text formatters.
   */ 
  private static final DecimalFormat sFloatFormat  = new DecimalFormat("0.000");
  private static final DecimalFormat sDoubleFormat = new DecimalFormat("0.00000");



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * A unique identifier for the next object processed. 
   */ 
  private long  pNextID;        

  /** 
   * The table of encoded/decoded object tables indexed by Object.hashCode().  Each 
   * object table contains the encoded/decoded objects with the same Object.hashCode()
   * indexed by unique object IDs. 
   */ 
  private HashMap<Integer,HashMap<Long,Object>>  pObjects;    

  /**
   * The string buffer holding the GLUE encoded text. 
   */ 
  private StringBuffer  pBuf;   
 
  /**
   * The current nesting level. 
   */
  private int  pLevel;          
 
}



