// $Id: ToolsetTreeData.java,v 1.1 2005/06/13 16:05:01 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T O O L S E T   T R E E   D A T A                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * User data associated with toolset tree nodes in the JManageToolsetsDialog.
 */ 
public
class ToolsetTreeData
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct the data for the root node.
   */
  public 
  ToolsetTreeData() 
  {}

  /**
   * Construct the data for a toolset name node.
   */ 
  public 
  ToolsetTreeData
  ( 
   String tname
  )
  {
    pName = tname;
  }

  /**
   * Construct the OS specific toolset node. 
   */ 
  public 
  ToolsetTreeData
  ( 
   String tname, 
   OsType os   
  )
  {
    pName   = tname;
    pOsType = os;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the toolset name.
   */ 
  public String
  getName() 
  {
    return pName;
  }

  /**
   * Get the toolset operating system type.
   */ 
  public OsType
  getOsType() 
  {
    return pOsType; 
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
    if((obj != null) && (obj instanceof ToolsetTreeData)) {
      ToolsetTreeData data = (ToolsetTreeData) obj; 
      return (pName.equals(data.pName) && 
	      (((pOsType == null) && (data.pOsType == null)) || 
	       ((pOsType != null) && pOsType.equals(data.pOsType))));
    }

    return false;
  }

  /**
   * Returns a string representation of the object. 
   */
  public String
  toString() 
  {
    if(pOsType != null)
      return pOsType.toString();
    else if(pName != null) 
      return pName;

    return null;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The toolset name.
   */ 
  private String  pName;

  /**
   * The toolset operating system type.
   */ 
  private OsType  pOsType;

}
