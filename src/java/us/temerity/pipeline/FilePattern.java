// $Id: FilePattern.java,v 1.1 2004/02/12 15:50:12 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   P A T T E R N                                                                */
/*                                                                                          */
/*    Generates literal filename from prefix, frame and suffix components.                  */
/*------------------------------------------------------------------------------------------*/

public
class FilePattern
  implements Glueable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /* Default constructor. */ 
  public 
  FilePattern()
  {}

  /* Construct from components. */ 
  public 
  FilePattern
  (
   String prefix,  /* IN: filename prefix */ 
   int padding,    /* IN: zero padding of frame number (-1 for none) */ 
   String suffix   /* IN: filename extension (null for none) */ 
  ) 
  {
    assert(prefix != null);
    pPrefix = prefix;

    if(suffix != null) 
      assert((padding == -1) || (padding >= 0));
    else 
      assert(padding == -1);

    pPadding = padding;
    pSuffix  = suffix;
  }

  /* Copy constructor. */ 
  public 
  FilePattern
  (
   FilePattern pat   /* IN: pattern to copy */ 
  ) 
  {
    pPrefix  = pat.getPrefix();
    pPadding = pat.getPadding();
    pSuffix  = pat.getSuffix();
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  public String
  getPrefix()
  {
    return pPrefix;
  }

  public int 
  getPadding() 
  {
    return pPadding;
  }

  public String
  getSuffix()
  {
    return pSuffix;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   F I L E N A M E   G E N E R A T I O N                                                */
  /*----------------------------------------------------------------------------------------*/

  /* The literal filename for the given frame number. */ 
  public String
  getFilename
  (
   int frame  /* IN: frame number */ 
  ) 
  {
    StringBuffer buf = new StringBuffer();
    buf.append(pPrefix);  

    if(pSuffix != null) {
      buf.append(".");

      String fstr = String.valueOf(frame);
      if(pPadding > 0) {
	int pad = pPadding - fstr.length();
	int wk; 
	for(wk=0; wk<pad; wk++) 
	  buf.append("0");
	buf.append(fstr + ".");
      }
      else if(pPadding == 0) {
	buf.append(fstr + ".");
      }
      
      buf.append(pSuffix);
    }
    else {
      assert(pPadding == -1);
    }

    /* 
       THIS LOOKS WRONG! 

       If (pSuffix == null), the frame number is completely ignored... 
    */ 

    return (buf.toString());
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R I S O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /* Are the frame patterns equivalent? */ 
  public boolean
  equals
  (
   Object obj
  )
  {
    FilePattern pat = (FilePattern) obj;

    return ((pat != null) && 
	    pPrefix.equals(pat.pPrefix) && 
	    (pPadding == pat.pPadding) && 
	    (((pSuffix == null) && (pat.getSuffix() == null)) || 
	     pSuffix.equals(pat.pSuffix)));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /* Convert to a string representation. */ 
  public String
  toString() 
  {
    StringBuffer buf = new StringBuffer();
    buf.append(pPrefix);
    
    if(pSuffix != null) {
      buf.append(".");

      if(pPadding == 4) 
	buf.append("#.");
      else if(pPadding > 0) {
	int wk; 
	for(wk=0; wk<pPadding; wk++) 
	  buf.append("@");
	buf.append(".");
      }
      else if(pPadding == 0) {
	buf.append("@.");
      }

      buf.append(pSuffix);
    }
    else {
      assert(pPadding == -1);
    }
	
    /* 
       THIS LOOKS WRONG! 

       If (pSuffix == null), the frame number is completely ignored... 
    */ 
	     
    return (buf.toString());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  writeGlue
  ( 
   GlueEncoder ge   /* IN: the current GLUE encoder */ 
  ) 
    throws GlueError
  {
    ge.writeEntity("Prefix", pPrefix);
    ge.writeEntity("Padding", new Integer(pPadding));
    ge.writeEntity("Suffix", pSuffix);
  }

  public void 
  readGlue
  (
   GlueDecoder gd  /* IN: the current GLUE decoder */ 
  ) 
    throws GlueError
  {
    pPrefix  = (String) gd.readEntity("Prefix");
    pPadding = ((Integer) gd.readEntity("Padding")).intValue();
    pSuffix  = (String) gd.readEntity("Suffix");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  protected String  pPrefix;   /* Filename prefix. */ 				   
  protected int     pPadding;  /* Zero padding of frame number (-1 for none). */    
  protected String  pSuffix;   /* Filename extension (null for none). */          

}

