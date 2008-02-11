// $Id: PluginID.java,v 1.1 2008/02/11 03:16:25 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   I D                                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * A unique combination of name, version and vendor used to identify a specific plugin. 
 */
public
class PluginID
  extends Named
  implements Comparable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */ 
  public
  PluginID() 
  {
    super();
  }

  /** 
   * Construct with the given name, version and vendor.
   * 
   * @param name 
   *   The short name of the plugin
   * 
   * @param vid
   *   The plugin revision number.
   * 
   * @param vendor
   *   The name of the plugin vendor.
   */ 
  public 
  PluginID
  (
   String name,  
   VersionID vid,
   String vendor
  ) 
  {
    super(name);
    
    if(vid == null) 
      throw new IllegalArgumentException("The version ID cannot be (null)!");
    pVersionID = vid;
    
    if(vendor == null) 
      throw new IllegalArgumentException("The vendor cannot be (null)!");
    pVendor = vendor;
  }

  /** 
   * Copy constructor. 
   * 
   * @param pluginID
   *   The plugin ID to copy. 
   */ 
  public 
  PluginID
  (
   PluginID pluginID
  ) 
  {
    this(pluginID.getName(), pluginID.getVersionID(), pluginID.getVendor()); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the revision number of the plugin. 
   */ 
  public final VersionID
  getVersionID()
  {
    return pVersionID;
  }
  
  /**
   * Get the name of the plugin vendor. 
   */ 
  public final String
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
    if((obj != null) && (obj instanceof PluginID)) {
      PluginID plg = (PluginID) obj;
      return (super.equals(obj) && 
	      pVersionID.equals(plg.pVersionID) && 
	      pVendor.equals(plg.pVendor));
    }
    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R A B L E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compares this object with the specified object for order.
   * 
   * @param obj 
   *   The <CODE>Object</CODE> to be compared.
   */
  public int
  compareTo
  (
   Object obj
  )
  {
    if(obj == null) 
      throw new NullPointerException();
    
    if(!(obj instanceof PluginID))
      throw new IllegalArgumentException("The object to compare was NOT a PluginID!");

    return compareTo((PluginID) obj);
  }

  /**
   * Compares this <CODE>PluginID</CODE> with the given <CODE>PluginID</CODE> for order.
   * 
   * @param pluginID 
   *   The <CODE>PluginID</CODE> to be compared.
   */
  public int
  compareTo
  (
   PluginID pluginID 
  )
  {
    int nameOrder = pName.compareTo(pluginID.getName());
    if(nameOrder != 0) 
      return nameOrder;

    int versionOrder = pVersionID.compareTo(pluginID.getVersionID());
    if(versionOrder != 0) 
      return versionOrder;

    return pVendor.compareTo(pluginID.getVendor());
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
    super.toGlue(encoder);
    
    encoder.encode("VersionID", pVersionID);
    encoder.encode("Vendor", pVendor);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    VersionID vid = (VersionID) decoder.decode("VersionID");
    if(vid == null) 
      throw new GlueException("The \"VersionID\" was missing!");
    pVersionID = vid; 
    
    String vendor = (String)  decoder.decode("Vendor");
    if(vendor == null) 
      throw new GlueException("The \"Vendor\" was missing!");
    pVendor = vendor;     
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7719274771593783575L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The revision number of the plugin. 
   */ 
  protected VersionID  pVersionID;

  /**
   * The name of the plugin vendor.
   */ 
  protected String  pVendor; 

}



