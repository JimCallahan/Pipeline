// $Id: BasePlugin.java,v 1.5 2006/02/27 17:57:29 jim Exp $

package us.temerity.pipeline;

import  us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   P L U G I N                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The superclass of all Pipeline plugins. <P>
 */
public 
class BasePlugin
  extends Described
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  protected
  BasePlugin() 
  {
    super();
  }

  /** 
   * Construct with the given name, version, vendor and description. 
   * 
   * @param name 
   *   The short name of the plugin
   * 
   * @param vid
   *   The plugin revision number.
   * 
   * @param vendor
   *   The name of the plugin vendor.
   * 
   * @param desc 
   *   A short description of the plugin.
   */ 
  protected
  BasePlugin
  (
   String name,  
   VersionID vid,
   String vendor, 
   String desc
  ) 
  {
    super(name, desc);

    if(vid == null) 
      throw new IllegalArgumentException("The plugin version cannot be (null)");
    pVersionID = vid;

    if(vendor == null) 
      throw new IllegalArgumentException("The plugin vendor cannot be (null)");
    pVendor = vendor;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the revision number of the plugin. 
   */ 
  public VersionID
  getVersionID()
  {
    return pVersionID;
  }
  
  /**
   * Get the name of the plugin vendor. 
   */ 
  public String
  getVendor()
  {
    return pVendor; 
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Mark this version of the plugin as currently being modified and tested by the 
   * plugin developer.  <P> 
   * 
   * Plugins with this flag set can be dynamically reloaded by the <B>plpluginmgr</B>(1) 
   * daemon and distributed to all running Pipeline programs. Subclasses should call this
   * method in their constructor during the creation and testing phase of developement.
   */ 
  protected void 
  underDevelopment()
  {
    pIsUnderDevelopment = true;
  }

  /** 
   * Whether this version of the plugin in currently being modified and tested by the 
   * plugin developer.  <P> 
   * 
   * Plugins with this flag set can be dynamically reloaded by the <B>plpluginmgr</B>(1) 
   * daemon and distributed to all running Pipeline programs.
   */ 
  public boolean
  isUnderDevelopment() 
  {
    return pIsUnderDevelopment;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the catagory of this plugin.
   */ 
  public String 
  getPluginCatagory() 
  {
    return "Plugin";
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
    if((obj != null) && (obj instanceof BasePlugin)) {
      BasePlugin plg = (BasePlugin) obj;
      return (super.equals(obj) && 
	      pVersionID.equals(plg.pVersionID) && 
	      pVendor.equals(plg.pVendor));
    }
    return false;
  }

  /**
   * Generate a string representation of this plugin.
   */ 
  public String
  toString()
  {
    return 
     ("Name        : " + getName() + "\n" + 
      "Version     : " + getVersionID() + "\n" + 
      "Vendor      : " + getVendor() + "\n" + 
      "Description : " + wordWrap(getDescription(), 14, 80) + "\n" + 
      "Catagory    : " + getPluginCatagory() + "\n" +
      "Status      : " + (isUnderDevelopment() ? "Under Development" : "Permanent") + "\n" + 
      "Class       : " + getClass().getName());
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
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
   
  /**
   * Generate a string consisting the the given character repeated N number of times.
   */ 
  private String
  repeat
  (
   char c,
   int size
  ) 
  {
    StringBuffer buf = new StringBuffer();
    int wk;
    for(wk=0; wk<size; wk++) 
      buf.append(c);
    return buf.toString();
  }

  /**
   * Line wrap the given String at word boundries.
   */ 
  private String
  wordWrap
  (
   String str,
   int indent, 
   int size
  ) 
  {
    if(str.length() + indent < size) 
      return str;

    StringBuffer buf = new StringBuffer();
    String words[] = str.split("\\p{Blank}");
    int cnt = indent;
    int wk;
    for(wk=0; wk<words.length; wk++) {
      int ws = words[wk].length();
      if(ws > 0) {
	if((size - cnt - ws) > 0) {
	  buf.append(words[wk]);
	  cnt += ws;
	}
	else {
	  buf.append("\n" + repeat(' ', indent) + words[wk]);
	  cnt = indent + ws;
	}

	if(wk < (words.length-1)) {
	  if((size - cnt) > 0) {
	    buf.append(' ');
	    cnt++;
	  }
	  else {
	    buf.append("\n" + repeat(' ', indent));
	    cnt = indent;
	  }
	}
      }
    }

    return buf.toString();
  }  



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5767914235352032286L;
  


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

  /**
   * Whether this version of the plugin in currently being modified and tested by the 
   * plugin developer.  Plugins with this flag set will be dynamically reloaded by the
   * plugin manager daemon whenever new copies of the plugin version are registered.
   */ 
  private boolean  pIsUnderDevelopment;
}
