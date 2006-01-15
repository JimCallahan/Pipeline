// $Id: MiscCreateToolsetPackageReq.java,v 1.3 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   C R E A T E   T O O L S E T   P A C K A G E   R E Q                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to create a new toolset package from the given modifiable package. <P> 
 * 
 * @see MasterMgr
 */
public
class MiscCreateToolsetPackageReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * The <CODE>level</CODE> argument may be <CODE>null</CODE> if this is the first 
   * revision of the package.
   * 
   * @param author
   *   The name of the user creating the package.
   * 
   * @param mod
   *   The source modifiable toolset package.
   * 
   * @param desc 
   *   The package description text.
   * 
   * @param level
   *   The revision number component level to increment.
   * 
   * @param os
   *   The operating system type.
   */
  public
  MiscCreateToolsetPackageReq
  (
   String author, 
   PackageMod mod, 
   String desc, 
   VersionID.Level level, 
   OsType os
  )
  {
    super();

    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");
    pAuthor = author;

    if(mod == null) 
      throw new IllegalArgumentException
	("The modifiable package cannot be (null)!");
    pMod = mod;

    if(desc == null) 
      throw new IllegalArgumentException
	("The description cannot be (null)!");
    pDescription = desc;

    pLevel = level;

    if(os == null) 
      throw new IllegalArgumentException
	("The operating system cannot be (null)!");
    pOsType = os;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the name of user creating the package.
   */ 
  public String
  getAuthor() 
  {
    return pAuthor;
  }

  /**
   * Gets the source modifiable package.
   */ 
  public PackageMod
  getPackage() 
  {
    return pMod;
  }
 
  /**
   * Get the package description text.
   */ 
  public String 
  getDescription() 
  {
    return pDescription;
  }
  
  /**
   * Get the revision number component level to increment.
   */ 
  public VersionID.Level
  getLevel() 
  {
    return pLevel;
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

  private static final long serialVersionUID = 6503456486328802431L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The name of user creating the package.
   */
  private String  pAuthor;

  /**
   * The source modifiable toolset.
   */
  private PackageMod pMod; 

  /**
   * The toolset description text.
   */ 
  private String  pDescription;

  /**
   * The revision number component level to increment.
   */ 
  private VersionID.Level  pLevel;

  /**
   * The operating system type.
   */
  private OsType  pOsType;  

}
  
