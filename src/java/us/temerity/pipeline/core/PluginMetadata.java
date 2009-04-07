// $Id: PluginMetadata.java,v 1.2 2009/04/07 08:01:41 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;
import java.math.BigInteger; 

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   M E T A D A T A                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Object to store information about an installed plugin.  Currently the class name and 
 * resource information is stored.
 */
public
class PluginMetadata
  implements Glueable, Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A no-args constructor for GLUE decoding.
   */
  public
  PluginMetadata()
  {
    pClassName = null;
    pResources = new TreeMap<String,Long>();
    pChecksums = new TreeMap<String,byte[]>();
  }

  /**
   * Constructs a new PluginMetadata object.
   *
   * @param cname
   *   The Java class name of a plugin.
   *
   * @param resources
   *   A SortedMap of resource file sizes keyed by Jar path.
   *
   * @param checksums
   *   A SortedMap of resource checksums keyed by Jar path.
   */
  public
  PluginMetadata
  (
   String cname, 
   SortedMap<String,Long> resources, 
   SortedMap<String,byte[]> checksums
  )
  {
    pClassName = cname;

    pResources = new TreeMap<String,Long>();
    pChecksums = new TreeMap<String,byte[]>();

    if(resources.size() != checksums.size())
      throw new IllegalArgumentException
	("The number of entries in resources (" + resources.size() + ") " + 
	 "does not match the number of entries in checksums (" + checksums.size() + ")!");

    pResources.putAll(resources);
    pChecksums.putAll(checksums);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the Java class name for the installed plugin.
   */
  public String
  getClassName()
  {
    return pClassName;
  }

  /**
   * Get an immutable table of resource file sizes keyed by Jar path.
   * This will be empty if there are no resources for the installed plugin.
   */
  public SortedMap<String,Long>
  getResources()
  {
    return Collections.unmodifiableSortedMap(pResources);
  }

  /**
   * Get an immutable table of resource checksums keyed by Jar path,
   * This will be empty if there are no resources for the installed plugin.
   */
  public SortedMap<String,byte[]>
  getChecksums()
  {
    return Collections.unmodifiableSortedMap(pChecksums);
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
    encoder.encode("ClassName", pClassName);

    /* Only write to GLUE file if there are resources. */

    if(!pResources.isEmpty()) {
      encoder.encode("Resources", pResources);
    }

    /* store checksum bytes in a condensed big integer string format */ 
    if(!pChecksums.isEmpty()) {
      TreeMap<String,String> table = new TreeMap<String,String>();
      for(String key : pChecksums.keySet()) {
        BigInteger big = new BigInteger(pChecksums.get(key));
        table.put(key, big.toString());
      }
      encoder.encode("Checksums", table);
    }
  }

  public void
  fromGlue
  (
   GlueDecoder decoder
  )
    throws GlueException
  {
    pClassName = (String) decoder.decode("ClassName");
    if(pClassName == null)
      throw new GlueException("The \"ClassName\" was missing!");

    /* If there are no resources associated with the installed plugin it is OK 
       for the GLUE decoder to return null. */
    {
      pResources.clear();
      TreeMap<String,Long> resources = (TreeMap<String,Long>) decoder.decode("Resources");
      if(resources != null) {
	pResources.putAll(resources);
      }
    }

    /* read checksum bytes from a condensed big integer string format */ 
    {
      pChecksums.clear(); 
      TreeMap<String,String> table = (TreeMap<String,String>) decoder.decode("Checksums");
      if(table != null) {
        for(String key : table.keySet()) {
          BigInteger big = new BigInteger(table.get(key));
          pChecksums.put(key, big.toByteArray());
        }
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7213104339243605009L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The Java class name of an installed plugin.
   */
  private String  pClassName;

  /**
   * The resource file sizes keyed by Jar path.
   */
  private SortedMap<String,Long>  pResources;

  /**
   * The resource checksums keyed by Jar path.
   */
  private SortedMap<String,byte[]>  pChecksums;
}

