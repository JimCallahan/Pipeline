// $Id: MiscCreateToolsetReq.java,v 1.5 2009/02/10 10:56:12 jim Exp $

package us.temerity.pipeline.message.env;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   C R E A T E   T O O L S E T   R E Q                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to create a new toolset from the given toolset packages.
 * 
 * @see MasterMgr
 */
public
class MiscCreateToolsetReq
  extends PrivilegedReq 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param author
   *   The name of the user creating the toolset.
   * 
   * @param name
   *   The name of the new toolset.
   * 
   * @param desc 
   *   The toolset description text.
   * 
   * @param packages
   *   The package names in order of evaluation.
   * 
   * @param versions
   *   The package revision numbers indexed by package name.
   * 
   * @param os
   *   The operating system type.
   */
  public
  MiscCreateToolsetReq
  (
   String author, 
   String name, 
   String desc, 
   Collection<String> packages, 
   TreeMap<String,VersionID> versions, 
   OsType os
  )
  {
    super();

    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");
    pAuthor = author;

    if(name == null) 
      throw new IllegalArgumentException
	("The toolset name cannot be (null)!");
    if(name.length() == 0) 
      throw new IllegalArgumentException
	("The toolset name must be specified!");
    pName = name;

    if(desc == null) 
      throw new IllegalArgumentException
	("The toolset description cannot be (null)!");
    if(desc.length() == 0) 
      throw new IllegalArgumentException
	("The toolset description must be specified!");
    pDescription = desc;

    if(packages == null) 
      throw new IllegalArgumentException
	("The packages cannot be (null)!");
    pPackages = packages;

    if(versions == null) 
      throw new IllegalArgumentException
	("The versions cannot be (null)!");
    pVersions = versions;
    
    if(os == null) 
      throw new IllegalArgumentException
	("The operating system cannot be (null)!");
    pOsType = os;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the name of user creating the toolset.
   */ 
  public String
  getAuthor() 
  {
    return pAuthor;
  }

  /**
   * Gets the name of the toolset.
   */ 
  public String
  getName() 
  {
    if(pName == null)
      throw new IllegalStateException(); 
    return pName;
  }
 
  /**
   * Get the toolset description text.
   */ 
  public String 
  getDescription() 
  {
    return pDescription;
  }
  
  /**
   * Get the package names in order of evaluation. 
   */ 
  public Collection<String> 
  getPackages() 
  {
    return pPackages;
  }
  
  /**
   * Get the package revision numbers indexed by package name.
   */ 
  public TreeMap<String,VersionID>
  getVersions() 
  {
    return pVersions;
  }

  /**
   * Gets the operating system type.
   */ 
  public OsType
  getOsType() 
  {
    return pOsType;
  }
 



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3298379886720173219L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The name of user creating the toolset.
   */
  private String  pAuthor;

  /**
   * The name of the toolset.
   */
  private String  pName;  

  /**
   * The toolset description text.
   */ 
  private String  pDescription;

  /**
   * The package names in order of evaluation. 
   */ 
  private Collection<String>  pPackages;
  
  /**
   * The package revision numbers indexed by package name.
   */ 
  private TreeMap<String,VersionID>  pVersions;

  /**
   * The operating system type.
   */
  private OsType  pOsType;  

}
  
