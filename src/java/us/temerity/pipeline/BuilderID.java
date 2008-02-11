// $Id: BuilderID.java,v 1.1 2008/02/11 03:16:25 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   I D                                                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * A unique combination of bulder collection plugin name, version and vendor along with the 
 * name of a specific builder within the collection which identifies a builder. 
 */
public
class BuilderID
  extends PluginID
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
  BuilderID() 
  {
    super();
  }

  /** 
   * Construct from components. 
   * 
   * @param name 
   *   The short name of the builder collection plugin.
   * 
   * @param vid
   *   The builder collection plugin revision number.
   * 
   * @param vendor
   *   The name of the builder collection plugin vendor.
   * 
   * @param builderName 
   *   The short name of a specific builder within the collection. 
   */ 
  public 
  BuilderID
  (
   String name,  
   VersionID vid,
   String vendor, 
   String builderName
  ) 
  {
    super(name, vid, vendor);
    
    if(builderName == null) 
      throw new IllegalArgumentException("The builder name cannot be (null)!");
    pBuilderName = builderName; 
  }

  /** 
   * Construct from a builder collection ID and a builder name. 
   * 
   * @param collectionID
   *    The plugin ID of the builder collection.
   * 
   * @param builderName 
   *   The short name of a specific builder within the collection. 
   */ 
  public 
  BuilderID
  (
   PluginID collectionID, 
   String builderName
  ) 
  {
    super(collectionID);
    
    if(builderName == null) 
      throw new IllegalArgumentException("The builder name cannot be (null)!");
    pBuilderName = builderName; 
  }

  /** 
   * Copy constructor. 
   * 
   * @param builderID
   *   The builder ID to copy. 
   */ 
  public 
  BuilderID
  (
   BuilderID builderID
  ) 
  {
    this(builderID.getName(), builderID.getVersionID(), builderID.getVendor(), 
         builderID.getBuilderName());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the name of the specific builder within the collection. 
   */ 
  public final String
  getBuilderName()
  {
    return pBuilderName; 
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
    if((obj != null) && (obj instanceof BuilderID)) {
      BuilderID plg = (BuilderID) obj;
      return (super.equals(obj) && 
	      pBuilderName.equals(plg.pBuilderName));
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
    
    if(!(obj instanceof BuilderID))
      throw new IllegalArgumentException("The object to compare was NOT a BuilderID!");

    return compareTo((BuilderID) obj);
  }


  /**
   * Compares this <CODE>BuilderID</CODE> with the given <CODE>BuilderID</CODE> for order.
   * 
   * @param builderID 
   *   The <CODE>BuilderID</CODE> to be compared.
   */
  public int
  compareTo
  (
   BuilderID builderID 
  )
  {
    int pluginOrder = super.compareTo(builderID); 
    if(pluginOrder != 0) 
      return pluginOrder;

    return pBuilderName.compareTo(builderID.getBuilderName());
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
    
    encoder.encode("BuilderName", pBuilderName);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);
    
    String vendor = (String)  decoder.decode("BuilderName");
    if(vendor == null) 
      throw new GlueException("The \"BuilderName\" was missing!");
    pBuilderName = vendor;     
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3324093334519276311L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the plugin vendor.
   */ 
  protected String  pBuilderName; 

}



