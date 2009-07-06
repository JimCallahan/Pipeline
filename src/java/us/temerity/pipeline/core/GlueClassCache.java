// $Id: GlueClassCache.java,v 1.3 2009/07/06 10:25:26 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.parser.*;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   G L U E   C L A S S   C A C H E                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Maintains a cache of Java classes previous referenced by Glue format files to optimize
 * instantiation of objects by avoiding calling Class.forName() when possible.
 */
class GlueClassCache
{     
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct the sole instance.
   */
  private  
  GlueClassCache()
  {
    pClasses     = new TreeMap<String,Class>();
    pClassesLock = new ReentrantReadWriteLock();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the GlueClassCache instance.
   */ 
  public static GlueClassCache
  getInstance() 
  {
    return sGlueClassCache;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the Class based on its name. <P> 
   * 
   * Note that Glue allows class names to be specified in two ways.  Classes may always be 
   * specified using their full Java class names.  Classes prefixed by one of the 
   * "java.lang", "java.util" or "us.temerity.pipeline" packages can also be specified
   * using a short name omitting these prefixes.  If the class cannot be found using its 
   * entire name, each of these default prefixes will be tried as well in the order listed
   * above.
   * 
   * @param cname
   *   The name of the Java class.
   * 
   * @throws ParseException
   *   If unable to determine any Java class for the name specified in the Glue file.
   */ 
  public Class
  getClass
  (
   String cname
  ) 
    throws ParseException
  {
    /* first see if its already been defined */ 
    pClassesLock.readLock().lock();
    try {
      Class cls = pClasses.get(cname);
      if(cls != null) 
        return cls;
    }
    finally {
      pClassesLock.readLock().unlock();
    }
    
    /* otherwise, try to define it... */ 
    {
      Class cls = null;
      try {
        cls = Class.forName(cname);

//      System.out.print("Glue Class (core): " + cname + " [" + cname + "]\n");
//      System.out.flush();     
      }
      catch (ClassNotFoundException ex) {
        int wk;
        for(wk=0; wk<sPackages.length; wk++) {
          try {
            cls = Class.forName(sPackages[wk] + "." + cname);

//          System.out.print
//            ("Glue Class (core): " + cname + " [" + sPackages[wk] + "." + cname + "]\n");
//          System.out.flush();    

            break;
          }
          catch (ClassNotFoundException ex2) {
          }
        }
      }
    
      if(cls == null) 
        throw new ParseException("Unable to locate class: " + cname);

      pClassesLock.writeLock().lock();
      try {
        pClasses.put(cname, cls);
        return cls;
      }
      finally {
        pClassesLock.writeLock().unlock();
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The sole instance of this class.
   */ 
  private static GlueClassCache sGlueClassCache = new GlueClassCache();
  
  /**
   * The package prefixes to use when resolving simple class names. 
   */ 
  private static final String sPackages[] = {
    "java.lang", 
    "java.util", 
    "us.temerity.pipeline"
  };


    
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * The defined classes indexed by class name. <P> 
   *
   * Access to this field should be protected by the read/write lock below.
   */
  private TreeMap<String,Class>   pClasses;
  private ReentrantReadWriteLock  pClassesLock;

}



