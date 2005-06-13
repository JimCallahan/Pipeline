// $Id: PackageTreeData.java,v 1.1 2005/06/13 16:05:01 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E   T R E E   D A T A                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * User data associated with toolset packages tree nodes in the JManageToolsetsDialog.
 */ 
public
class PackageTreeData
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct the data for the root node.
   */
  public 
  PackageTreeData() 
  {}

  /**
   * Construct the data for a package name node.
   * 
   * @param pname
   *   The package name.
   */ 
  public 
  PackageTreeData
  ( 
   String pname
  )
  {
    pName = pname;
  }

  /**
   * Construct the package operating system node. 
   * 
   * @param pname
   *   The package name.
   * 
   * @param os
   *   The operation system type.
   */ 
  public 
  PackageTreeData
  ( 
   String pname, 
   OsType os   
  )
  {
    pName   = pname;
    pOsType = os;
  }

  /**
   * Construct the data for a package node. 
   * 
   * @param pname
   *   The package name.
   * 
   * @param os
   *   The operation system type.
   * 
   * @param vid 
   *   The package revision number or <CODE>null</CODE> for working version.
   */ 
  public 
  PackageTreeData
  ( 
   String pname, 
   OsType os, 
   VersionID vid 
  )
  {
    pName      = pname;
    pOsType    = os;
    pIsPackage = true;
    pVersionID = vid; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the package name.
   */ 
  public String
  getName() 
  {
    return pName;
  }

  /**
   * Get the package operating system type.
   */ 
  public OsType
  getOsType() 
  {
    return pOsType; 
  }

  /**
   * Whether this data is associate with an actual package node.
   */ 
  public boolean
  isPackage() 
  {
    return pIsPackage;
  }

  /**
   * Get the package revision number or <CODE>null</CODE> for working version.
   */ 
  public VersionID 
  getVersionID() 
  {
    return pVersionID; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj   
  )
  {
    if((obj != null) && (obj instanceof PackageTreeData)) {
      PackageTreeData data = (PackageTreeData) obj; 
      return (pName.equals(data.pName) && 
	      (((pOsType == null) && (data.pOsType == null)) || 
	       ((pOsType != null) && pOsType.equals(data.pOsType))) &&
	      (pIsPackage == data.pIsPackage) && 
	      (((pVersionID == null) && (data.pVersionID == null)) || 
	       ((pVersionID != null) && pVersionID.equals(data.pVersionID))));
    }

    return false;
  }

  /**
   * Returns a string representation of the object. 
   */
  public String
  toString() 
  {
    if(pIsPackage) {
      if(pVersionID != null) 
	return pVersionID.toString();
      else 
	return "working";
    }
    else if(pOsType != null)
      return pOsType.toString();
    else if(pName != null) 
      return pName;
      
    return null;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The package name. 
   */ 
  private String  pName;

  /**
   * The package operating system type.
   */ 
  private OsType  pOsType;

  /**
   * Whether this data is associate with an actual package node.
   */ 
  private boolean pIsPackage;

  /**
   * The package revision number or <CODE>null</CODE> for working version.
   */ 
  private VersionID pVersionID; 

}
