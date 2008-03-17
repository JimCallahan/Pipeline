// $Id: DjvActionUtils.java,v 1.1 2008/03/17 22:56:41 jim Exp $

package us.temerity.pipeline.plugin;

import  us.temerity.pipeline.*;

import java.util.*;
import java.util.regex.*;
import java.io.*;


/*------------------------------------------------------------------------------------------*/
/*   D J V   A C T I O N   U T I L S                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Superclass of node Action plugins related to DJV Imaging. <P> 
 * 
 * This class provides convenience methods which make it easier to write Action plugins 
 * which generate DJV command line options.
 */
public 
class DjvActionUtils
  extends CompositeActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Construct with the given name, version, vendor and description. 
   * 
   * @param name 
   *   The short name of the action.  
   * 
   * @param vid
   *   The action plugin revision number.
   * 
   * @param vendor
   *   The name of the plugin vendor.
   * 
   * @param desc 
   *   A short description of the action.
   */ 
  protected
  DjvActionUtils
  (
   String name,  
   VersionID vid,
   String vendor, 
   String desc
  ) 
  {
    super(name, vid, vendor, desc);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   F I L E   N A M I N G                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The supported QuickTime movie related extensions.               
   */ 
  public static final ArrayList<String> 
  getQtExtensions() 
  {
    ArrayList<String> suffixes = new ArrayList<String>();
    suffixes.add("qt");
    suffixes.add("mov");
    suffixes.add("avi");
    suffixes.add("mp4");
    
    return suffixes;
  }

  /**
   * All supported image and movie filename extensions.
   */ 
  public static final ArrayList<String> 
  getDjvExtensions() 
  {
    /* as of DJV Imaging (0.8.1)... */ 
    ArrayList<String> suffixes = new ArrayList<String>();
    suffixes.add("1dl");
    suffixes.add("avi");
    suffixes.add("bw");
    suffixes.add("cin");
    suffixes.add("dpx");
    suffixes.add("exr");
    suffixes.add("ifl");
    suffixes.add("jfif");
    suffixes.add("jpeg");
    suffixes.add("jpg");
    suffixes.add("lut");
    suffixes.add("mov");
    suffixes.add("mp4");
    suffixes.add("pbm");
    suffixes.add("pgm");
    suffixes.add("pic");
    suffixes.add("png");
    suffixes.add("pnm");
    suffixes.add("ppm");
    suffixes.add("qt");
    suffixes.add("rgb");
    suffixes.add("rgba");
    suffixes.add("rla");
    suffixes.add("rpf");
    suffixes.add("sgi");
    suffixes.add("tga");
    suffixes.add("tif");
    suffixes.add("tiff");
    suffixes.add("vlut");
    
    return suffixes;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert a Pipeline file sequence into a DJV file sequence string.
   */ 
  public static final String
  toDjvFileSeq
  (
   FileSeq fseq
  ) 
  {
    if(fseq.isSingle()) {
      return fseq.getPath(0).toOsString();
    }
    else {
      FilePattern fpat = fseq.getFilePattern(); 
      String suffix = fpat.getSuffix();
      FrameRange range = fseq.getFrameRange();
      return (fpat.getPrefix() + "." + 
              pad(range.getStart(), fpat.getPadding()) + "-" + 
              pad(range.getEnd(), fpat.getPadding()) + 
              ((suffix != null) ? ("." + suffix) : ""));
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate padded frame numbers.
   */ 
  private static String
  pad
  (
   int value,
   int padding
  ) 
  {
    StringBuilder buf = new StringBuilder(); 

    String str = String.valueOf(value);
    int wk;
    for(wk=str.length(); wk<padding; wk++) 
      buf.append("0");
    buf.append(str);

    return buf.toString();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3372976680709900543L;

}



