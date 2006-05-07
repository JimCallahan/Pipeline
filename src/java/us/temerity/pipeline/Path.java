// $Id: Path.java,v 1.1 2006/05/07 21:30:07 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P A T H                                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Used to represent an abstract file system pathname which is independent of the 
 * underlying operating system. <P> 
 * 
 * This class provides methods for manipulating file system paths similar {@link File File} 
 * class, except that paths are always stored using UNIX file naming conventions regardless 
 * of the current operating system conventions.  This means that code which manipulates and 
 * constructs pathnames only has to consider one file naming convention. <P> 
 * 
 * Microsoft Windows paths are specified using the forward slash "/" in place of the 
 * back slash.  This means that in order to specify a native Windows path like "C:\foo\bar"
 * you will need to use the string "C:/foo/bar".  Similarly, a native UNC path such as 
 * "\\server\share\foo\bar will need to be specified as "//server/share/foo/bar". <P> 
 * 
 * In order to accomodate both UNIX and Windows conventions, each absolute path must 
 * begin with a prefix of either the UNIX root directory "/", the UNC root directory 
 * "//" or a drive letter specifier such as "C:/".  For relative paths, this prefix 
 * is optional. Following the prefix, a path may contain one or more path components 
 * seperated by the "/" character. <P>  
 * 
 * The abstract paths represented by this class can be converted into literal paths which 
 * conform to the conventions of the underlying operating system using the {@link #toFile}
 * and {@link #toOsString} methods. <P> 
 * 
 * The Pipeline API has been designed to prefer the usage of instances of this class over
 * the {@link File} class except in cases where the file will be simply used without 
 * modification as a parameter to a low-level I/O routine.  In cases, where a pathname is
 * used to construct a literal path, instances of this class are prefered in order to 
 * make the code involved more portable accross all operating systems supported by Pipeline.
 */
public
class Path
  implements Cloneable, Glueable, Comparable, Serializable
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
  Path() 
  {}

  /**
   * Copy constructor.
   * 
   * @param path
   *   The abstract pathname to copy.
   */ 
  public 
  Path
  (
   Path path
  )
  {
    this(null, path); 
  }

  /**
   * Creates a new Path instance from a parent abstract pathname and a child relative 
   * abstract path name. <P> 
   * 
   * If parent is <CODE>null</CODE> then the child Path instance is simply duplicated. 
   * 
   * @param parent
   *   The parent abstract pathname.
   * 
   * @param child
   *   The child abstract pathname relative to the parent path.
   */ 
  public 
  Path
  (
   Path parent, 
   Path child
  )
  {
    if(parent == null) {
      pPrefix = child.pPrefix;
      pComps  = child.pComps;
    }
    else {
      pPrefix = parent.pPrefix;

      pComps = new String[parent.pComps.length + child.pComps.length];
      int wk, ck;
      for(wk=0; wk<parent.pComps.length; wk++) 
	pComps[wk] = parent.pComps[wk];
      for(ck=0; ck<child.pComps.length; ck++, wk++) 
	pComps[wk] = child.pComps[ck];
    }
    
    buildCache();
  }

  /**
   * Creates a new Path instance from a parent abstract pathname and a child pathname 
   * string. <P> 
   * 
   * If parent is <CODE>null</CODE> then the new Path instance is created as if by 
   * invoking the single-argument Path constructor on the given child pathname string.
   * 
   * @param parent
   *   The parent abstract pathname.
   * 
   * @param child
   *   The child pathname string.
   */ 
  public 
  Path
  (
   Path parent, 
   String child
  )
  {
    initPath(parent, child);
  }
     
  /**
   * Creates a new Path instance by converting the given pathname string into an abstract 
   * pathname. <P> 
   * 
   * If the given string is the empty string, then the result is the empty 
   * abstract pathname.
   * 
   * @param pathname
   *   A pathname string.
   */ 
  public      
  Path
  (
   String pathname
  )
  {
    initPath(null, pathname);
  }

  /**
   * Creates a new Path instance by converting the given File into a Path. <P> 
   * 
   * In this case, the File is converted by replacing all instances of the system dependent
   * {@link File#separator seperator} with the "/" character in the path returned by the
   * {@link File#getPath getPath} method.
   * 
   * @param file
   *   The file to convert.
   */ 
  public      
  Path
  (
   File file 
  )
  {
    switch(PackageInfo.sOsType) {
    case Unix:
    case MacOS:
      initPath(null, file.getPath());
      break;

    case Windows:
      initPath(null, file.getPath().replace('\\', '/'));
      break;

    default:
      assert(false);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize a new Path instance from a parent abstract pathname and a child pathname 
   * string. <P> 
   * 
   * If parent is <CODE>null</CODE> then the new Path instance is created as if by 
   * invoking the single-argument Path constructor on the given child pathname string.
   * 
   * @param parent
   *   The parent abstract pathname.
   * 
   * @param child
   *   The child pathname string.
   */ 
  private void
  initPath
  (
   Path parent, 
   String child
  )
  {
    if(child == null) 
      throw new IllegalArgumentException
	("The child path cannot be (null)!");

    ArrayList<String> comps = new ArrayList<String>();
    String leftover = child;
    if(parent != null) {
      pPrefix = parent.pPrefix;

      int wk;
      for(wk=0; wk<parent.pComps.length; wk++) 
	comps.add(parent.pComps[wk]);
    }
    else {
      if(child.startsWith("//")) {
	pPrefix = "//";
	leftover = child.substring(2);
      }
      else if(child.startsWith("/")) {
	pPrefix = "/";
	leftover = child.substring(1);
      }
      else if((child.length() >= 3) && 
	      Character.isLetter(child.charAt(0)) && 
	      (child.charAt(1) == ':') && (child.charAt(2) == '/')) {
	pPrefix = child.substring(0, 3);
	leftover = child.substring(3);
      }
    }

    String parts[] = leftover.split("/");
    int wk; 
    for(wk=0; wk<parts.length; wk++) {
      if(parts[wk].length() > 0)
	comps.add(parts[wk]);
    }
     
    pComps = new String[comps.size()];
    comps.toArray(pComps);

    buildCache();
  }

  /**
   * Private constructor for creating a new Path from parts. 
   * 
   * @param prefix
   *   The path prefix or <CODE>null</CODE> for relative paths.
   * 
   * @param comps
   *   The pathname components.
   * 
   * @param count
   *   The number of pathname components to use.
   */ 
  private      
  Path
  (
   String prefix,
   String comps[], 
   int count
  )
  {
    pPrefix = prefix; 
    
    if(count > comps.length) 
      throw new IllegalArgumentException 
	("The component count (" + count + ") was greater than the number of components " +
	 "(" + comps.length + ")!");

    pComps = new String[count];
    int wk;
    for(wk=0; wk<pComps.length; wk++) 
      pComps[wk] = comps[wk];    

    buildCache();  
  }

  /**
   * Private constructor for creating a new Path from parts. 
   * 
   * @param prefix
   *   The path prefix or <CODE>null</CODE> for relative paths.
   * 
   * @param comps
   *   The pathname components.
   */ 
  private      
  Path
  (
   String prefix,
   String comps[]
  )
  {
    this(prefix, comps, comps.length);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The pathname prefix of this abstract pathname. <P> 
   * 
   * If the path is relative, the prefix will be <CODE>null</CODE>.
   */
  public String 
  getPrefix()
  {
    return pPrefix;
  }

  /**
   * Returns the name of the file or directory denoted by this abstract pathname. This is 
   * just the last name in the pathname's name sequence. If the pathname's name sequence 
   * is empty, then the empty string is returned.
   */
  public String 
  getName()
  {
    if(pComps.length > 0) 
      return pComps[pComps.length-1];
    return "";
  }
  
  /**
   * Returns the pathname string of this abstract pathname's parent, or <CODE>null</CODE> 
   * if this pathname does not name a parent directory.
   */
  public String 
  getParent()
  {
    Path parent = getParentPath();
    if(parent != null) 
      return parent.toString();
    return null;
  }

  /**
   * Returns the abstract pathname of this abstract pathname's parent, or <CODE>null</CODE> 
   * if this pathname does not name a parent directory.<P> 
   * 
   * The parent of an abstract pathname consists of the pathname's prefix, if any, and each 
   * name in the pathname's name sequence except for the last. If the name sequence is empty 
   * then the pathname does not name a parent directory. <P> 
   * 
   * @return 
   *   The abstract pathname of the parent directory named by this abstract pathname, or 
   *   <CODE>null</CODE> if this pathname does not name a parent
   */
  public Path 
  getParentPath()
  {
    switch(pComps.length) {
    case 0:
      return null;
      
    case 1:
      if(pPrefix != null) 
	return new Path(pPrefix); 
      else 
	return null;

    default:
      return new Path(pPrefix, pComps, pComps.length-1);
    }    
  }

  /**
   * Get the list of abstract pathname components.
   * 
   * @return
   *   May return an empty list if the path has no components.
   */ 
  public ArrayList<String> 
  getComponents() 
  {
    ArrayList<String> comps = new ArrayList<String>(pComps.length);

    int wk;
    for(wk=0; wk<pComps.length; wk++) 
      comps.add(pComps[wk]);

    return comps;
  }

 
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Tests whether this abstract pathname is absolute. <P> 
   * 
   * A path is absolute if it has one of the following valid path prefixes: the UNIX root 
   * directory "/", the UNC root directory "//" or a drive letter specifier such as "C:/".
   */
  public boolean 
  isAbsolute()
  {
    return (pPrefix != null);
  }

  /**
   * Tests whether this abstract pathname has a parent directory. 
   */
  public boolean 
  hasParent()
  {
    switch(pComps.length) {
    case 0:
      return false;
      
    case 1:
      return (pPrefix != null);

    default:
      return true; 
    }    
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Convert to a system dependent abstract pathname.
   */ 
  public File
  toFile() 
  {
    switch(PackageInfo.sOsType) {
    case Unix:
    case MacOS:
      return new File(toString());

    case Windows:
      return new File(pStringRep.replace('/', '\\'));

    default:
      assert(false);
      return null;
    }
  }

  /**
   * Convert to a system dependent abstract pathname string for the current operating system.
   */
  public String
  toOsString() 
  {
    return toOsString(PackageInfo.sOsType);
  }

  /**
   * Convert to a system dependent abstract pathname string for the given operating system. 
   */
  public String
  toOsString
  (
   OsType os
  ) 
  {
    switch(os) {
    case Unix:
    case MacOS:
      return toString();

    case Windows:
      return pStringRep.replace('/', '\\');

    default:
      assert(false);
      return null;
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Tests this abstract pathname for equality with the given object.
   */ 
  public boolean 
  equals
  (
   Object obj
  )
  {
    if((obj != null) && (obj instanceof Path)) {
      Path path = (Path) obj;
      return ((pHashCode == path.pHashCode) && 
	      pStringRep.equals(path.pStringRep));
    }
    return false;
  }
   
  /**
   * Returns a hash code value for the object.
   */
  public int 
  hashCode() 
  {
    assert(pStringRep != null);
    return pHashCode;
  }

  /**
   * Returns a string representation of the object. 
   */
  public String
  toString() 
  {
    assert(pStringRep != null);
    return pStringRep;
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
    try {
      return super.clone();
    }
    catch(CloneNotSupportedException ex) {
      assert(false);
      return null;
    }
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
    encoder.encode("PathName", pStringRep);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    String path = (String) decoder.decode("PathName"); 
    if(path == null) 
      throw new GlueException("The \"PathName\" was missing!");
    initPath(null, path);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R A B L E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compares this object with the specified object for order.
   * 
   * @param obj 
   *   The <CODE>Object</CODE> to be compared.
   */
  public int
  compareTo
  (
   Object obj
  )
  {
    if(obj == null) 
      throw new NullPointerException();
    
    if(!(obj instanceof Path))
      throw new IllegalArgumentException("The object to compare was NOT a Path!");

    return compareTo((Path) obj);
  }

  /**
   * Compares this <CODE>Path</CODE> with the given <CODE>Path</CODE> for order.
   * 
   * @param path 
   *   The <CODE>Path</CODE> to be compared.
   */
  public int
  compareTo
  (
   Path path
  )
  {
    return pStringRep.compareTo(path.pStringRep);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compute the cached string representation and hash code for the abstract pathname.
   */
  private void
  buildCache() 
  {
    StringBuffer buf = new StringBuffer();
    if(pPrefix != null) 
      buf.append(pPrefix);

    int wk;
    for(wk=0; wk<pComps.length; wk++)
      buf.append(((wk > 0) ? "/" : "") + pComps[wk]);

    pStringRep = buf.toString();
    pHashCode  = pStringRep.hashCode();    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 8907101928999914285L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The path prefix.
   */ 
  private String pPrefix; 

  /**
   * The path components.
   */ 
  private String pComps[];

  /** 
   * The cached string representation.
   */
  private String  pStringRep;

  /** 
   * The cached hash code.
   */
  private int  pHashCode;
}
