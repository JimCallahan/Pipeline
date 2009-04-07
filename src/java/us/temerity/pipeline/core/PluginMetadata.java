// $Id: PluginMetadata.java,v 1.1 2009/04/07 01:44:42 jlee Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

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
    pClassName  = null;
    pResources  = new TreeMap<String,Long>();
    pChecksums  = new TreeMap<String,byte[]>();
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

    pResources  = new TreeMap<String,Long>();
    pChecksums  = new TreeMap<String,byte[]>();

    if(resources.size() != checksums.size())
      throw new IllegalArgumentException
	("The number of entries in resources (" + resources.size() + ") " + 
	 "does not match the number of entries in checksums (" + checksums.size() + ")!");

    pResources.putAll(resources);
    pChecksums.putAll(checksums);
  }

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

    if(!pChecksums.isEmpty()) {
      encoder.encode("Checksums", pChecksums);
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
      SortedMap<String,Long> resources = 
	(TreeMap<String,Long>) decoder.decode("Resources");

      if(resources != null)
	pResources.putAll(resources);
    }

    {
      SortedMap<String,byte[]> checksums = 
	(TreeMap<String,byte[]>) decoder.decode("Checksums");

      if(checksums != null)
	pChecksums.putAll(checksums);
    }
  }

  private static final long serialVersionUID = 7213104339243605009L;

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

