// $Id: PluginMenuLayout.java,v 1.7 2008/01/28 12:00:51 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   L A Y O U T                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Represents the layout of a hierarchical set of menus for selection of a specific 
 * Pipeline plugin version.
 */
public
class PluginMenuLayout
  extends LinkedList<PluginMenuLayout> 
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct the root menu. 
   */ 
  public 
  PluginMenuLayout() 
  {
    pTitle = "Plugin Menu";
  }  

  /**
   * Construct a new submenu. 
   * 
   * @param title 
   *   The title of the submenu.
   */ 
  public 
  PluginMenuLayout
  (
   String title
  ) 
  {
    if(title == null) 
      throw new IllegalArgumentException("The submenu title cannot be (null)!");
    pTitle = title;
  }

  /**
   * Construct a new menu item. 
   * 
   * @param title 
   *   The title of menu item which selects a particular plugin version.
   * 
   * @param name
   *   The name of the plugin version.
   * 
   * @param vid
   *   The revision number of the plugin version.
   * 
   * @param vendor
   *   The name of the plugin vendor.
   */ 
  public 
  PluginMenuLayout
  (
   String title, 
   String name, 
   VersionID vid, 
   String vendor
  ) 
  {
    if(title == null) 
      throw new IllegalArgumentException("The menu item title cannot be (null)!");
    pTitle = title;

    if(name == null) 
      throw new IllegalArgumentException("The plugin name cannot be (null)!");
    pName = name;
    
    if(vid == null) 
      throw new IllegalArgumentException("The plugin revision number cannot be (null)!");
    pVersionID = vid;

    if(vendor == null) 
      throw new IllegalArgumentException("The plugin vendor name cannot be (null)!");
    pVendor = vendor;
  }

  /**
   * Deep copy constructor.
   * 
   * @param layout
   *   The menu layout to copy.
   */ 
  public 
  PluginMenuLayout
  (
   PluginMenuLayout layout
  ) 
  {
    pTitle     = layout.getTitle();
    pName      = layout.getName();
    pVersionID = layout.getVersionID(); 
    pVendor    = layout.getVendor();

    for(PluginMenuLayout pml : layout) 
      add(new PluginMenuLayout(pml));
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the title of the submenu or menu item.
   */ 
  public String
  getTitle() 
  {
    return pTitle; 
  }

  /**
   * Set the title the submenu or menu item.
   */ 
  public void 
  setTitle
  (
   String title
  ) 
  {
    if(title == null) 
      throw new IllegalArgumentException("The menu title cannot be (null)!");
    pTitle = title;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether this is a menu item? 
   */ 
  public boolean
  isMenuItem() 
  {
    return (pName != null);
  }

  /**
   * Whether this is a submenu? 
   */ 
  public boolean
  isSubmenu() 
  {
    return !isMenuItem();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the plugin associated with the menu item.
   * 
   * @return 
   *   The name or <CODE>null</CODE> if this is not a menu item.
   */ 
  public String
  getName() 
  {
    return pName;
  }

  /**
   * Gets the revision number of the plugin associated with the menu item.
   * 
   * @return 
   *   The revision number or <CODE>null</CODE> if this is not a menu item.
   */ 
  public VersionID
  getVersionID() 
  {
    if(pVersionID != null) 
      return new VersionID(pVersionID);
    return null;
  }

  /**
   * Get the name of the plugin vendor. 
   * 
   * @return 
   *   The vendor name or <CODE>null</CODE> if this is not a menu item.
   */ 
  public String
  getVendor()
  {
    return pVendor; 
  }
  
  /**
   * Sets the name and version of the plugin associated with the menu item.
   */ 
  public void
  setPlugin
  (
   String name,
   VersionID vid, 
   String vendor
  ) 
  {
    if(name == null)
      throw new IllegalArgumentException
	("The plugin name cannot be (null)!");

    if(vid == null)
      throw new IllegalArgumentException
	("The plugin revision number cannot be (null)!");

    if(vendor == null)
      throw new IllegalArgumentException
	("The plugin vendor name cannot be (null)!");

    pName      = name;
    pVersionID = vid;
    pVendor    = vendor;
  }

  /**
   * Clear the plugin associated with the menu item.
   */ 
  public void 
  clearPlugin()
  { 
    pName      = null; 
    pVersionID = null;
    pVendor    = null;
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
    if((obj != null) && (obj instanceof PluginMenuLayout)) {
      PluginMenuLayout pml = (PluginMenuLayout) obj;
      return (pTitle.equals(pml.pTitle));
    }
    return false;
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
    encoder.encode("Title", pTitle);

    if(pName != null) {
      encoder.encode("Name", pName);
      encoder.encode("VersionID", pVersionID);
      encoder.encode("Vendor", pVendor);
    }
    
    if(!isEmpty()) 
      encoder.encode("Children", new LinkedList<PluginMenuLayout>(this));
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    String title = (String) decoder.decode("Title"); 
    if(title == null) 
      throw new GlueException("The \"Title\" was missing!");
    pTitle = title;

    String name = (String) decoder.decode("Name"); 
    if(name != null) {
      pName = name;

      VersionID vid = (VersionID) decoder.decode("VersionID"); 
      if(vid == null) 
	throw new GlueException("The \"VersionID\" was missing!");
      pVersionID = vid; 

      String vendor = (String) decoder.decode("Vendor"); 
      if(vendor == null) 
	throw new GlueException("The \"Vendor\" was missing!");
      pVendor = vendor; 
    }

    LinkedList<PluginMenuLayout> children = 
      (LinkedList<PluginMenuLayout>) decoder.decode("Children"); 
    if(children != null) 
      addAll(children);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8353824296871840447L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The title of the submenu or menu item.
   */
  private String  pTitle;

  /**
   * The name of the plugin version.
   */
  private String  pName; 

  /**
   * The revision number of the plugin version.
   */
  private VersionID  pVersionID; 

  /**
   * The name of the plugin vendor. 
   */
  private String  pVendor; 

}
