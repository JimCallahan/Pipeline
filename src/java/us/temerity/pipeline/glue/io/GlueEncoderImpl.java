// $Id: GlueEncoderImpl.java,v 1.8 2009/05/16 02:06:19 jim Exp $

package us.temerity.pipeline.glue.io;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.lang.reflect.*;
import java.io.*;
import java.text.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   G L U E   E N C O D E R   I M P L                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Converts a set of objects into Glue format text. <P> 
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
   * Initialize a new Glue encoder.
   * 
   * @param writer 
   *   The character stream into which the Glue encoded representation is written.
   */
  private 
  GlueEncoderImpl
  (
   Writer writer
  ) 
  {
    pNextID  = 1;
    pObjects = new TreeMap<Integer,TreeMap<Long,Object>>();  
    pWriter  = writer;
    pLevel   = 0;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   M E T H O D S                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * A static convience method for encoding an object into a String.<P> 
   * 
   * All exceptions are logged using LogMgr internally and then rethrown as GlueException
   * with the same message as written to the log.
   * 
   * @param title 
   *   The name to be given to the object when encoded.
   * 
   * @param obj 
   *   The <CODE>Object</CODE> to be encoded.
   */
  public static String
  encodeString
  (
   String title,  
   Object obj
  ) 
    throws GlueException
  {
    try {
      Writer writer = new StringWriter();  
      GlueEncoderImpl ge = new GlueEncoderImpl(writer); 
      ge.encode(title, obj);
      return writer.toString();
    }
    catch(GlueException ex) {
      String msg = 
        ("Unable to Glue encode: " + title + "\n" + 
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
   * A static convience method for encoding an object into the given file.<P> 
   * 
   * All exceptions are logged using LogMgr internally and then rethrown as GlueException
   * with the same message as written to the log.
   * 
   * @param title 
   *   The name to be given to the object when encoded.
   * 
   * @param obj 
   *   The <CODE>Object</CODE> to be encoded.
   * 
   * @param file
   *   The file into which the Glue encoded representation is written.
   */
  public static void 
  encodeFile
  (
   String title,  
   Object obj, 
   File file
  ) 
    throws GlueException
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Glu, LogMgr.Level.Finest,
       "Writing " + title + ": " + file); 

    try {
      Writer writer = null;
      try {
        writer = new BufferedWriter(new FileWriter(file));
      }
      catch(IOException ex) {
        String msg = 
          ("I/O ERROR: \n" + 
           "  Unable to open file (" + file + ") used to encode: " + title + "\n" + 
           "    " + ex.getMessage());
        LogMgr.getInstance().log(LogMgr.Kind.Glu, LogMgr.Level.Severe, msg); 
        throw new GlueException(msg);
      }

      try {
        try {
          GlueEncoderImpl ge = new GlueEncoderImpl(writer); 
          ge.encode(title, obj);
        }
        catch(GlueException ex) {
          String msg = 
            ("While writing to file (" + file + "), unable to Glue encode: " + title + "\n" + 
             "  " + ex.getMessage());
          LogMgr.getInstance().log(LogMgr.Kind.Glu, LogMgr.Level.Severe, msg); 
          throw new GlueException(msg);
        }
        finally {
          writer.close();
        }
      }
      catch(IOException ex) {
        String msg = 
          ("I/O ERROR: \n" + 
           "  While writing to file (" + file + ") during Glue encoding of: " + title + "\n" +
           "    " + ex.getMessage());
        LogMgr.getInstance().log(LogMgr.Kind.Glu, LogMgr.Level.Severe, msg); 
        throw new GlueException(msg);
      }
    }
    catch(GlueException ex) {
      throw ex;
    }
    catch(Exception ex) {
      String msg = Exceptions.getFullMessage("INTERNAL ERROR:", ex, true, true);
      LogMgr.getInstance().log(LogMgr.Kind.Glu, LogMgr.Level.Severe, msg); 
      throw new GlueException(msg);      
    }
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
    try {
      if(obj == null) {
        pWriter.append(indent());
        if(title.length() > 0) 
          pWriter.append(title + " "); 
        pWriter.append("<NULL>\n");
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
    catch(IOException ex) {
      throw new GlueException(ex);
    }
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
    throws IOException
  {
    pWriter.append(indent());
    if(title.length() > 0) 
      pWriter.append(title + " "); 
    pWriter.append("<REF> #" + objID + "\n");
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
    throws GlueException, IOException
  {
    /* header */ 
    pWriter.append(indent());
    if(title.length() > 0) 
      pWriter.append(title + " "); 
    pWriter.append("<");

    /* primtive wrapper types */ 
    Class cls = obj.getClass();
    String cshort = shortName(cls.getName());
    if((cls == sBooleanClass) ||
       (cls == sByteClass) ||
       (cls == sShortClass) ||
       (cls == sIntegerClass) ||
       (cls == sLongClass)) {
      pWriter.append(cshort + "> #" + objID + " { " + obj + " }\n");
    }
    else if(cls == sFloatClass) {
      pWriter.append(cshort + "> #" + objID + 
                     " { " + sFloatFormat.format((Float) obj) + " }\n");
    }
    else if(cls == sDoubleClass) {
      pWriter.append(cshort + "> #" + objID + 
                     " { " + sDoubleFormat.format((Double) obj) + " }\n");
    }
    else if(cls == sCharacterClass) {
      Character c = (Character) obj;
      pWriter.append(cshort + "> #" + objID + " { " + ((int) c.charValue()) + " }\n");
    }
    else if(cls == sStringClass) {
      pWriter.append(cshort + "> #" + objID + " { \"");

      char cs[] = ((String) obj).toCharArray();
      int wk;
      for(wk=0; wk<cs.length; wk++) {
	if(cs[wk] == '"') 
	  pWriter.append("\\");
	pWriter.append(cs[wk]);
      }

      pWriter.append("\" }\n");
    }
    else if(cls.isEnum()) {
      Enum e = (Enum) obj;
      pWriter.append(cshort + "> #" + objID + " { :" + e.name() + ": }\n");
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

	pWriter.append(simple + "[" + dim + "]");
	
	int wk;
	for(wk=1; wk<depth; wk++) 
	  pWriter.append("[]");

	pWriter.append("> #" + objID + " {\n");
      }

      /* primitive type members */ 
      pLevel++;
      if(primCls != null) {
	int wk;
	for(wk=0; wk<dim; wk++) {
	  Object comp = Array.get(obj, wk);
	  if(comp == null)
	    throw new IllegalStateException();

	  pWriter.append(indent() + wk + " <" + simple + "> { ");

	  if((primCls == Boolean.TYPE) ||
	     (primCls == Byte.TYPE) ||
	     (primCls == Short.TYPE) ||
	     (primCls == Integer.TYPE) ||
	     (primCls == Long.TYPE)) {
	    pWriter.append(comp.toString());
	  }
	  else if(primCls == Float.TYPE) {
	    pWriter.append(sFloatFormat.format((Float) comp));
	  }
	  else if(primCls == Double.TYPE) {
	    pWriter.append(sDoubleFormat.format((Double) comp));
	  }
	  else if(primCls == Character.TYPE) {
	    pWriter.append("'" + comp + "'");
	  }
	  else {
	    throw new IllegalStateException("Unknown primitive type (" + primCls + ")!");
	  }

	  pWriter.append(" }\n");
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
      
      pWriter.append(indent() + "}\n");
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
	  pWriter.append(cshort + "> #" + objID + " {\n");
	  
	  Glueable gobj = (Glueable) obj;
	  gobj.toGlue(this);
	}
	pLevel--;

	pWriter.append(indent() + "}\n");
      }
      
      /* Collection objects */ 
      else if(isCollection) {
	pLevel++;
	{
	  pWriter.append(cshort + "> #" + objID + " {\n");
	  
	  Collection col = (Collection) obj;
	  Iterator iter = col.iterator();
	  while(iter.hasNext()) {
	    encode("", iter.next());
	  }
	}
	pLevel--;
	
	pWriter.append(indent() + "}\n");
      }

      /* Map objects */ 
      else if(isMap) {
	pLevel++;
	{
	  pWriter.append(cshort + "> #" + objID + " {\n");
	  
	  Map map = (Map) obj;
	  Iterator iter = map.keySet().iterator();
	  while(iter.hasNext()) {
	    Object key = iter.next();
	    Object val = map.get(key);

	    pWriter.append(indent() + "{\n");
	    pLevel++;
	    {
	      encode("Key", key);
	      encode("Val", val);
	    }
	    pLevel--;
	    pWriter.append(indent() + "}\n");
	  }
	}
	pLevel--;
	
	pWriter.append(indent() + "}\n");
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
    throws IOException
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
    TreeMap<Long,Object> objs = pObjects.get(key);
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
    TreeMap<Long,Object> objs = pObjects.get(key);
    if(objs == null) {
      objs = new TreeMap<Long,Object>();
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
  private TreeMap<Integer,TreeMap<Long,Object>>  pObjects;  
  // private DoubleMap<Integer,Long,Object>  pObjects;    <-- Use this   

  /**
   * The character stream where GLUE encoded text is written. 
   */ 
  private Writer  pWriter;   
 
  /**
   * The current nesting level. 
   */
  private int  pLevel;          
 
}



