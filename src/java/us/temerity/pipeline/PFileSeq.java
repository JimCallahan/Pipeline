// $Id: PFileSeq.java,v 1.1 2004/02/12 15:50:12 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.util.logging.*;
import java.sql.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P F I L E   S E Q                                                                      */
/*                                                                                          */
/*                                                                                          */
/*------------------------------------------------------------------------------------------*/

public
class PFileSeq
  implements Glueable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /* Default constructor. */ 
  public 
  PFileSeq() 
  {}


  /* Construct a single file sequence. */ 
  public 
  PFileSeq
  (
   String prefix         /* IN: filename prefix */ 
  ) 
  {
    assert(prefix != null);
    pPattern = new FilePattern(prefix, -1, null);

    pFrameRange = null;
    pNumFiles   = 1;
  }

  public 
  PFileSeq
  (
   String prefix,        /* IN: filename prefix */ 
   String suffix         /* IN: filename suffix */ 
  ) 
  {
    assert(prefix != null);
    if((suffix != null) && (suffix.length() == 0))
      pPattern = new FilePattern(prefix, -1, null);
    else 
      pPattern = new FilePattern(prefix, -1, suffix);

    pFrameRange = null;
    pNumFiles   = 1;
  }


  /* Construct a padded (possibly) multi-frame file sequence. */ 
  public 
  PFileSeq
  (
   String prefix,        /* IN: filename prefix */ 
   FrameRange range,     /* IN: frame number */ 
   int padding,          /* IN: zero padding of frame number */ 
   String suffix         /* IN: filename suffix */ 
  ) 
  {
    assert(prefix != null);
    assert(padding >= 0);
    pPattern = new FilePattern(prefix, padding, suffix);

    assert(range != null);
    pFrameRange = new FrameRange(range);

    pNumFiles = pFrameRange.getNumFrames();
  }


  /* Construct from file pattern and frame range. */ 
  public 
  PFileSeq
  (
   FilePattern pattern,  /* IN: filename pattern */ 
   FrameRange range      /* IN: frame number */ 
  ) 
  {
    assert(pattern != null);
    pPattern = new FilePattern(pattern);

    if(range != null) {
      pFrameRange = new FrameRange(range);
      pNumFiles = pFrameRange.getNumFrames();
    }      
    else {
      pFrameRange = null;
      pNumFiles = 1;
    }
  }


  /* Construct a file sequence which contains only a single file 
       from the given file sequence. */ 
  public
  PFileSeq
  (
   PFileSeq fseq,  /* IN: the file sequence */ 
   int idx         /* IN: file index */ 
  ) 
  {
    pPattern = fseq.getFilePattern();
    if(fseq.getFrameRange() != null) 
      pFrameRange = new FrameRange(fseq.getFrameRange().indexToFrame(idx));
    else 
      pFrameRange = null;
    pNumFiles = 1;
  }

  
  /* Copy constructor. */ 
  public
  PFileSeq
  (
   PFileSeq fseq   /* IN: the file sequence */ 
  ) 
  {
    pPattern    = fseq.getFilePattern();
    pFrameRange = fseq.getFrameRange();
    pNumFiles   = fseq.numFiles();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /* Does the associated filename (or filenames) have a frame number component? */ 
  public boolean 
  hasFrameNumbers() 
  {
    return (pFrameRange != null);
  }

  
  /* Is this a single frame sequence? */ 
  public boolean 
  isSingle()
  {
    return ((pFrameRange == null) || (pFrameRange.isSingle()));
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /* The filenaming pattern. */ 
  public FilePattern
  getFilePattern()
  {
    assert(pPattern != null);
    return new FilePattern(pPattern);
  }
  
  /* The filename prefix. */ 
  public String
  getPrefix() 
  {
    return pPattern.getPrefix();
  }

  /* The number of digits to pad frame numbers.  */
  public int
  getPadding() 
  {
    return pPattern.getPadding();
  }
  
  /* The filename suffix. */ 
  public String
  getSuffix() 
  {
    return pPattern.getSuffix();
  }


  /* The file sequence frame range. */ 
  public FrameRange
  getFrameRange() 
  {
    if(pFrameRange != null) 
      return new FrameRange(pFrameRange);
    return null;
  }


  /* The total number of frames. */ 
  public int
  numFiles() 
  {
    return pNumFiles;
  }

  /* Generate the filename based on the given file index. */ 
  public String 
  getFilename
  (
   int idx   /* IN: file index [0, numFiles()-1] */  
  )  
  {
    assert(pPattern != null);
    if(pFrameRange != null) 
      return pPattern.getFilename(pFrameRange.indexToFrame(idx));
    else 
      return pPattern.getFilename(0);
  }

  /* Generate the filename of the first (possible only) file index. */ 
  public String 
  getFilename()
  {
    return getFilename(0);
  }

  /* Generate a list of ALL of the filenames. */ 
  public ArrayList 
  getFilenames() 
  {
    ArrayList<String> files = new ArrayList<String>();
    int wk;
    for(wk=0; wk<pNumFiles; wk++)
      files.add(getFilename(wk));
    return files;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   F I L E S Y T E M                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /* Delete any pre-exising files from this sequence under the given directory. */ 
  public void 
  delete
  (
   File dir   /* IN: parent directory */ 
  )
    throws PError
  {
    File file = null;
    try {
      int wk;
      for(wk=0; wk<pNumFiles; wk++) {
	file = new File(dir, getFilename(wk));
	if(file.isFile()) {
	  Logs.sub.finest("Deleting file: " + file);
	  file.delete();
	}
      }
    }
    catch (SecurityException ex) {
      throw new PError("Unable to delete file \"" + file + "\" from file sequence " + 
		       toString() + " due to the Java Security Manager!");
    }
  }


  /* Change the permissions to read-only for any existing files from the sequence 
       in the given directory. */ 
  public void 
  setReadOnly
  (
   File dir   /* IN: parent directory */ 
  )
    throws PError
  {
    File file = null;
    try {
      int wk;
      for(wk=0; wk<pNumFiles; wk++) {
	file = new File(dir, getFilename(wk));
	if(file.isFile()) {
	  Logs.sub.finest("Making file read-only: " + file);
	  file.setReadOnly();
	}
      }
    }
    catch (SecurityException ex) {
      throw new PError("Unable to change the permissions to read-only for file \"" + 
		       file + "\" from file sequence " + 
		       toString() + " due to the Java Security Manager!");
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R I S O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /* are the file sequences equivalent? */ 
  public boolean
  equals
  (
   Object obj   
  )
  {
    PFileSeq fseq = (PFileSeq) obj;

    if((fseq == null) || 
       !pPattern.equals(fseq.getFilePattern()))
      return false;

    if((pFrameRange == null) && (fseq.getFrameRange() == null)) 
      return true;
    else if((pFrameRange != null) && (fseq.getFrameRange() != null) &&
	    pFrameRange.equals(fseq.getFrameRange()))
      return true;

    return false;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  public String
  toString() 
  {
    if(pFrameRange != null) {
      if(pFrameRange.isSingle())
	return pPattern.getFilename(pFrameRange.getStart());
      else 
	return (pPattern.toString() + ", " + pFrameRange.toString());
    }
    else 
      return pPattern.toString();
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
    ge.writeEntity("FilePattern", pPattern);
    if(pFrameRange != null) 
      ge.writeEntity("FrameRange",  pFrameRange);
  }

  public void 
  readGlue
  (
   GlueDecoder gd  /* IN: the current GLUE decoder */ 
  ) 
    throws GlueError
  {
    pPattern    = (FilePattern) gd.readEntity("FilePattern");
    pFrameRange = (FrameRange)  gd.readEntity("FrameRange");
    pNumFiles   = (pFrameRange != null) ? pFrameRange.getNumFrames() : -1;
  }




  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  protected FilePattern  pPattern;     /* The filename pattern. */
  protected FrameRange   pFrameRange;  /* The file sequence frame range (null if none). */ 
  protected int          pNumFiles;    /* The number of files in the sequence. */ 

}


