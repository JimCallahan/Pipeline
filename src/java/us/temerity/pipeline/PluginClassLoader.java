// $Id: PluginClassLoader.java,v 1.1 2005/01/15 02:56:32 jim Exp $

package us.temerity.pipeline;

import java.io.*;
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
   * @param bytes
   *   The raw plugin class bytes.
   */
  public 
  PluginClassLoader
  (
   byte[] bytes
  ) 
  {
    super();

    pBytes = bytes;
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
    return defineClass(cname, pBytes, 0, pBytes.length);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The raw plugin class bytes.
   */ 
  private byte[]  pBytes; 

}

