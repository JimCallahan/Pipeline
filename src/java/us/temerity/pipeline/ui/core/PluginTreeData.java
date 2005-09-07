// $Id: PluginTreeData.java,v 1.2 2005/09/07 21:11:17 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   T R E E   D A T A                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * User data associated with plugin version tree nodes.
 */ 
public
class PluginTreeData
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct the data for the root node.
   */
  public 
  PluginTreeData() 
  {}

  /**
   * Construct the data for a plugin name node.
   * 
   * @param name
   *   The plugin name.
   */ 
  public 
  PluginTreeData
  ( 
   String name
  )
  {
    pName = name; 
  }

  /**
   * Construct the plugin version node.
   * 
   * @param name
   *   The plugin name.
   * 
   * @param vid
   *   The revision number.
   * 
   * @param vendor
   *   The name of the plugin vendor.
   */ 
  public 
  PluginTreeData
  ( 
   String name, 
   VersionID vid, 
   String vendor
  )
  {
    pName      = name;
    pVersionID = vid;
    pVendor    = vendor;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the plugin name.
   */ 
  public String
  getName() 
  {
    return pName;
  }

  /**
   * Get the plugin revision number or <CODE>null</CODE> if name node.
   */ 
  public VersionID 
  getVersionID() 
  {
    return pVersionID; 
  }

  /**
   * Get the name of the plugin vendor. 
   * 
   * @return 
   *   The vendor name or <CODE>null</CODE> if name node.
   */ 
  public String
  getVendor()
  {
    return pVendor; 
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
    if((obj != null) && (obj instanceof PluginTreeData)) {
      PluginTreeData data = (PluginTreeData) obj; 
      return (pName.equals(data.pName) && 
	      (((pVersionID == null) && (data.pVersionID == null)) || 
	       ((pVersionID != null) && pVersionID.equals(data.pVersionID))) ||
	      (((pVendor == null) && (data.pVendor == null)) || 
	       ((pVendor != null) && pVendor.equals(data.pVendor))));
    }

    return false;
  }

  /**
   * Returns a string representation of the object. 
   */
  public String
  toString()
  {
    if(pVersionID != null) 
      return ("v" + pVersionID);
    else if(pName != null) 
      return pName; 
    else
      return null;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*
 
  /**
   * The plugin name. 
   */ 
  private String  pName;

  /**
   * The plugin revision number or <CODE>null</CODE> if name node.
   */ 
  private VersionID pVersionID; 

  /**
   * The name of the plugin vendor or <CODE>null</CODE> if name node.
   */
  private String  pVendor; 

}

