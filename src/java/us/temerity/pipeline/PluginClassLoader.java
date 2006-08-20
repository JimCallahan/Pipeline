// $Id: PluginClassLoader.java,v 1.2 2006/08/20 05:46:51 jim Exp $

package us.temerity.pipeline;

import java.io.*;
import java.util.*;
import java.net.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   C L A S S   L O A D E R                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A class loader which loads plugin classes from an array of bytes.
 */
public
class PluginClassLoader
  extends ClassLoader
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new plugin loader.
   * 
   * @param contents
   *   The raw plugin class bytes indexed by class name.
   */
  public 
  PluginClassLoader
  (
   TreeMap<String,byte[]> contents
  ) 
  {
    super();

    pContents = contents; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C L A S S   L O A D E R   O V E R R I D E S                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Finds the specified class. 
   */
  protected Class 
  findClass
  (
   String cname
  ) 
    throws ClassNotFoundException
  {
    byte bs[] = pContents.get(cname);
    if(bs == null) 
      throw new ClassNotFoundException
	("Unable to find class (" + cname + ")!");

    return defineClass(cname, bs, 0, bs.length);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The raw plugin class bytes indexed by class name.
   */ 
  private TreeMap<String,byte[]>  pContents; 

}

