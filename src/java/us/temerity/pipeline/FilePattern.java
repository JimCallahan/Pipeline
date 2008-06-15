// $Id: FilePattern.java,v 1.15 2008/06/15 01:59:49 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   P A T T E R N                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A standardized representation of filenames associated with Pipeline nodes. <P>
 * 
 * All files associated with nodes have filenames which consist of the following 
 * components: <BR>
 * <DIV style="margin-left: 40px;"><I>prefix</I>[.<I>frame</I>][.<I>suffix</I>]</DIV><P>
 * 
 * The <I>prefix</I> component is required and must start with one of the characters 
 * ['<CODE>a</CODE>'-'<CODE>z</CODE>', '<CODE>A</CODE>'-'<CODE>Z</CODE>', '<CODE>/</CODE>'] 
 * followed by zero or more of the characters ['<CODE>a</CODE>'-'<CODE>z</CODE>', 
 * '<CODE>A</CODE>'-'<CODE>Z</CODE>', '<CODE>0</CODE>'-'<CODE>9</CODE>', '<CODE>_</CODE>', 
 * '<CODE>-</CODE>', '<CODE>/</CODE>']. <P>
 * 
 * The <I>frame</I> component is optional and represents a frame number consisting of the 
 * characters ['<CODE>0</CODE>'-'<CODE>9</CODE>'] which may be or may not be zero padded. <P>

 * Finally, the <I>suffix</I> component is optional and may only contain the characters 
 * ['<CODE>a</CODE>'-'<CODE>z</CODE>', '<CODE>A</CODE>'-'<CODE>Z</CODE>', 
 * '<CODE>0</CODE>'-'<CODE>9</CODE>', '<CODE>_</CODE>', '<CODE>-</CODE>']. <P>
 * 
 * This class is used by the {@link FileSeq FileSeq} class in combination with the 
 * {@link FrameRange FrameRange} class to represent the file sequences associated with 
 * nodes.
 * 
 * @see FrameRange
 * @see FileSeq
 */
public
class FilePattern
  implements Comparable<FilePattern>, Cloneable, Glueable, Serializable
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
  FilePattern()
  {
    pPadding = -1;
  }

  /**
   * Construct a <CODE>FilePattern</CODE> without frame number or suffix components. 
   * 
   * @param prefix  
   *   The required filename prefix component. This argument cannot be <CODE>null</CODE>.
   * 
   * @throws IllegalArgumentException
   *   If the <CODE>prefix</CODE> is <CODE>null</CODE>.
   */
  public 
  FilePattern
  (
   String prefix
  ) 
  {
    if(prefix == null)
      throw new IllegalArgumentException("The prefix cannot be (null)!");
    pPrefix = prefix;
    
    pPadding = -1;

    buildCache();
  }

  /**
   * Construct a <CODE>FilePattern</CODE> without a frame number component.
   * 
   * @param prefix  
   *   The required filename prefix component. This argument cannot be <CODE>null</CODE>.
   * 
   * @param suffix  
   *   The filename extension (or suffix) component.  If <CODE>null</CODE> is passed for this 
   *   argument, then the filename will have no suffix component.
   * 
   * @throws IllegalArgumentException
   *   If the <CODE>prefix</CODE> is <CODE>null</CODE>. If the <CODE>suffix</CODE> is an 
   *   empty <CODE>String</CODE>.
   */
  public 
  FilePattern
  (
   String prefix,  
   String suffix   
  ) 
  {
    if(prefix == null)
      throw new IllegalArgumentException("The prefix cannot be (null)!");
    pPrefix = prefix;

    pPadding = -1;

    if((suffix != null) && (suffix.length() == 0)) 
      throw new IllegalArgumentException("The suffix was an empty string!");
    pSuffix = suffix;

    buildCache();
  }

  /**
   * Construct a <CODE>FilePattern</CODE> from general components. 
   * 
   * @param prefix  
   *   The required filename prefix component. This argument cannot be <CODE>null</CODE>.
   * 
   * @param padding 
   *   The number of digits of zero padding of the frame number component of the filename.
   *   If <CODE>0</CODE> is passed for this argument, then frame numbers will be unpadded.
   *   If <CODE>-1</CODE> is passed for this argument, then the constructed 
   *   <CODE>FilePattern</CODE> will have <I>no</I> frame number component.
   * 
   * @param suffix  
   *   The filename extension (or suffix) component.  If <CODE>null</CODE> is passed for this 
   *   argument, then the filename will have <I>no</I> suffix component.
   * 
   * @throws IllegalArgumentException
   *   If the <CODE>prefix</CODE> is <CODE>null</CODE>. If the <CODE>padding</CODE> is
   *   less-than <CODE>-2</CODE>.  If the <CODE>suffix</CODE> is an empty <CODE>String</CODE>.
   */ 
  public 
  FilePattern
  (
   String prefix,  
   int padding,    
   String suffix   
  ) 
  {
    if(prefix == null)
      throw new IllegalArgumentException("The prefix cannot be (null)!");
    pPrefix = prefix;

    if(padding < -1)
      throw new IllegalArgumentException
	("The frame number padding (" + padding + ") must (-1), (0) or a positive value!");
    pPadding = padding;

    if((suffix != null) && (suffix.length() == 0)) 
      throw new IllegalArgumentException("The suffix was an empty string!");
    pSuffix  = suffix;

    buildCache();
  }

  /**
   * Construct a <CODE>FilePattern</CODE> by prepend a path to an existing pattern.
   * 
   * @param path
   *   The path to prepend.
   * 
   * @param pattern  
   *   The <CODE>FilePattern</CODE> to prepend.
   */ 
  public 
  FilePattern
  (
   String path, 
   FilePattern pattern 
  )  
  {
    if(path == null) 
      throw new IllegalArgumentException("The prepend path cannot be (null)!");

    pPrefix  = path + "/" + pattern.getPrefix();
    pPadding = pattern.getPadding();
    pSuffix  = pattern.getSuffix();

    buildCache();
  }

  /**
   * Copy constructor. 
   * 
   * @param pattern  
   *    The <CODE>FilePattern</CODE> to copy. 
   */ 
  public 
  FilePattern
  (
   FilePattern pattern 
  )  
  {
    pPrefix  = pattern.getPrefix();
    pPadding = pattern.getPadding();
    pSuffix  = pattern.getSuffix();

    pStringRep = pattern.toString();
    pHashCode  = pattern.hashCode();
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Does this <CODE>FilePattern</CODE> have a frame number component.
   */
  public boolean 
  hasFrameNumbers() 
  {
    return (pPadding >= 0);
  }

  /** 
   * Does this <CODE>FilePattern</CODE> have a suffix component.
   */
  public boolean 
  hasSuffix() 
  {
    return (pSuffix != null);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * Gets the prefix component.
   */ 
  public String
  getPrefix()
  {
    if(pPrefix == null)
      throw new IllegalStateException(); 
    return pPrefix;
  }

  /** 
   * Gets the number of digits of of zero padding of the frame number component.
   * 
   * @return  
   *   The number of zero padded digits or <CODE>-1</CODE> if this <CODE>FilePattern</CODE>
   *   does not have a frame number component. 
   */ 
  public int 
  getPadding() 
  {
    return pPadding;
  }

  /** 
   * Gets the optional filename extension (or suffix) component.
   * 
   * @return  
   *   The suffix string or <CODE>null</CODE> if this <CODE>FilePattern</CODE> has no 
   *   suffix component.
   */
  public String
  getSuffix()
  {
    return pSuffix;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   F I L E N A M E   G E N E R A T I O N                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generated a literal filename for the given frame number. <P>
   * 
   * This method is only valid for <CODE>FilePattern</CODE> objects which have a frame 
   * number component.
   * 
   * @param frame  
   *   The frame number used to generate the filename.
   */ 
  public File
  getFile
  (
   int frame  
  ) 
  {
    if(!hasFrameNumbers())
      throw new IllegalStateException(); 

    StringBuilder buf = new StringBuilder();
    buf.append(pPrefix);

    String fstr = String.valueOf(frame);
    if(pPadding >= 0) {
      buf.append(".");
      int wk; 
      for(wk=0; wk < (pPadding - fstr.length()); wk++) 
	buf.append("0");
    }
    buf.append(fstr);
      
    if(pSuffix != null) 
      buf.append("." + pSuffix);

    return new File(buf.toString());
  }
  
  /**
   * Generated a literal filename. <P>
   * 
   * This method is only valid for <CODE>FilePattern</CODE> objects which have 
   * <I>no</I> frame number component.
   */
  public File
  getFile() 
  {
    if(hasFrameNumbers())
      throw new IllegalStateException(); 

    if(pSuffix != null) 
      return new File(pPrefix + "." + pSuffix);

    return new File(pPrefix);
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generated an abstract pathname for the given frame number. <P>
   * 
   * This method is only valid for <CODE>FilePattern</CODE> objects which have a frame 
   * number component.
   * 
   * @param frame  
   *   The frame number used to generate the filename.
   */ 
  public Path
  getPath
  (
   int frame  
  ) 
  {
    if(!hasFrameNumbers())
      throw new IllegalStateException(); 

    StringBuilder buf = new StringBuilder();
    buf.append(pPrefix);

    String fstr = String.valueOf(frame);
    if(pPadding >= 0) {
      buf.append(".");
      int wk; 
      for(wk=0; wk < (pPadding - fstr.length()); wk++) 
	buf.append("0");
    }
    buf.append(fstr);
      
    if(pSuffix != null) 
      buf.append("." + pSuffix);

    return new Path(buf.toString());
  }
  
  /**
   * Generated an abstract pathname. <P>
   * 
   * This method is only valid for <CODE>FilePattern</CODE> objects which have 
   * <I>no</I> frame number component.
   */
  public Path
  getPath() 
  {
    if(hasFrameNumbers())
      throw new IllegalStateException(); 

    if(pSuffix != null) 
      return new Path(pPrefix + "." + pSuffix);

    return new Path(pPrefix);
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
    if((obj != null) && (obj instanceof FilePattern)) {
      FilePattern pat = (FilePattern) obj;
      return ((pHashCode == pat.pHashCode) && 
	      pStringRep.equals(pat.pStringRep));
    }

    return false;
  }

  /**
   * Returns a hash code value for the object.
   */
  public int 
  hashCode() 
  {
    if(pStringRep == null)
      throw new IllegalStateException(); 
    return pHashCode;
  }

  /**
   * Returns a string representation of the object. 
   */
  public String
  toString() 
  {
    if(pStringRep == null)
      throw new IllegalStateException(); 
    return pStringRep;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R A B L E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compares this <CODE>FilePattern</CODE> with the given <CODE>FilePattern</CODE> for order.
   * 
   * @param fpat 
   *   The <CODE>FilePattern</CODE> to be compared.
   */
  public int
  compareTo
  (
   FilePattern fpat
  )
  {
    return pStringRep.compareTo(fpat.pStringRep);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public Object 
  clone()
  {
    try {
      return super.clone(); 
    }
    catch(CloneNotSupportedException ex) {
      throw new IllegalStateException(); 
    }
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
    encoder.encode("Prefix",  pPrefix);

    if(pPadding > -1)
      encoder.encode("Padding", pPadding);

    if(pSuffix != null)
      encoder.encode("Suffix",  pSuffix);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    String prefix = (String) decoder.decode("Prefix");
    if(prefix == null) 
      throw new GlueException("The \"Prefix\" was missing or (null)!");
    pPrefix = prefix;

    Integer padding = (Integer) decoder.decode("Padding");
    if(padding != null) {
      if(padding < -1)
	throw new GlueException
	  ("The frame number \"Padding\" (" + padding + ") must (-1), (0) " + 
	   "or a positive value!");
      pPadding = padding;
    }

    String suffix = (String) decoder.decode("Suffix"); 
    if((suffix != null) && (suffix.length() == 0)) 
      throw new GlueException("The \"Suffix\" was an empty string!");
    pSuffix = suffix;

    buildCache();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compute the cached string representation and hash code for the file pattern.
   */
  private void
  buildCache() 
  {
    {
      StringBuilder buf = new StringBuilder();
      buf.append(pPrefix);
      
      switch(pPadding) {
      case -1:
	break;
	
      case 0:
	buf.append(".@");
	break;
	
      case 4:
	buf.append(".#");
	break;
	
      default:
	{
	  buf.append(".");
	  int wk; 
	  for(wk=0; wk<pPadding; wk++) 
	    buf.append("@");
	}
      }
      
      if(pSuffix != null) 
	buf.append("." + pSuffix);
      
      pStringRep = buf.toString();
    }

    pHashCode = pStringRep.hashCode();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3005276181325424333L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The required filename prefix component. This component cannot be <CODE>null</CODE>.
   */
  private String pPrefix;  

  /** 
   * The number of digits of zero padding of the frame number component of the filename.
   * If <CODE>0</CODE>, then the frame numbers will be unpadded.  If <CODE>-1</CODE>, then 
   * the <CODE>FilePattern</CODE> will have <I>no</I> frame number component.
   */
  private int pPadding;  

  /**
   * The filename extension (or suffix) component.  If <CODE>null</CODE>, then the filename 
   * will have <I>no</I> suffix component.
   */ 
  private String pSuffix;  


  /** 
   * The cached string representation.
   */
  private String  pStringRep;
 
  /** 
   * The cached hash code.
   */
  private int  pHashCode;
 
}

