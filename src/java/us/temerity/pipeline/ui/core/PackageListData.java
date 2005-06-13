// $Id: PackageListData.java,v 1.1 2005/06/13 16:05:01 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E   L I S T   D A T A                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * User data associated with the included toolset packages list in the JManageToolsetsDialog.
 */ 
public
class PackageListData
  extends PackageTreeData
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct the list data. 
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
  PackageListData
  ( 
   String pname, 
   OsType os, 
   VersionID vid 
  )
  {
    super(pname, os, vid);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns a string representation of the object. 
   */
  public String
  toString() 
  {
    return getName(); 
  }
}
