// $Id: PackageMap.java,v 1.1 2005/06/28 18:05:22 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E   M A P                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A three level deep TreeMap containing information related to toolset packages.
 */
public
class PackageMap<V> 
  extends TripleMap<String,OsType,VersionID,V>
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct an empty map.
   */ 
  public 
  PackageMap()
  {
    super();
  }  

  /**
   * Deep copy constructor. 
   */ 
  public 
  PackageMap
  (
   PackageMap<V> pmap
  )
  {
    super((TripleMap<String,OsType,VersionID,V>) pmap);
  }  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -456777046471942841L;

  
}
