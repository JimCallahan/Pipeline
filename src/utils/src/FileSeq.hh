// $Id: FileSeq.hh,v 1.2 2003/10/11 04:16:07 jim Exp $

#ifndef PIPELINE_FILE_SEQ_HH
#define PIPELINE_FILE_SEQ_HH

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef CSTRING_H
#  include <cstring>
#else 
#  ifdef HAVE_STRING_H
#    include <string.h>
#  endif
#endif

#ifdef HAVE_SET
#  include <set>
#endif

using namespace std;

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   F I L E   S E Q                                                                        */
/*                                                                                          */
/*                                                                                          */
/*------------------------------------------------------------------------------------------*/

class FileSeq
{
protected:
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L   T Y P E S                                                           */
  /*----------------------------------------------------------------------------------------*/
  
  struct ltint
  {
    bool 
    operator()
    (
     int a, 
     int b
    ) const
    {
      return (a < b);
    }
  };
  
  typedef std::set<int, ltint>   FrameSet; 
  
  
public:
  /*----------------------------------------------------------------------------------------*/
  /*  C O N S T R U C T O R                                                                 */
  /*----------------------------------------------------------------------------------------*/

  /* File sequence constructor. */ 
  FileSeq
  (
   string& prefix,   /* IN: file sequence prefix */ 
   int padding,      /* IN: minumum number of frame characters */ 
   string& suffix,   /* IN: file sequence suffix */
   int frame         /* IN: initial frame number */ 
  ) :
    pPrefix(prefix), 
    pPadding(padding), 
    pSuffix(suffix)
  {
    assert(pPadding > 0);
    addFrame(frame);
  }


  /* Single file constructor. */ 
  FileSeq
  (
   string& filename   /* IN: single file */ 
  ) :
    pPrefix(filename), 
    pPadding(-1), 
    pSuffix("")
  {}


  /*----------------------------------------------------------------------------------------*/
  /*  D E S T R U C T O R                                                                   */
  /*----------------------------------------------------------------------------------------*/

  ~FileSeq()
  {
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*  A C C E S S                                                                           */
  /*----------------------------------------------------------------------------------------*/

  /* File sequence prefix */ 
  const string& 
  getPrefix() const
  {
    return pPrefix;
  }

  /* Minumum number of frame characters. */ 
  int 
  getPadding() const 
  {
    return pPadding;
  }

  /* File sequence suffix */ 
  const string& 
  getSuffix() const
  {
    return pSuffix;
  }


  /*----------------------------------------------------------------------------------------*/
  /*  F R A M E   N U M B E R S                                                             */
  /*----------------------------------------------------------------------------------------*/

  /* Add a frame number to the sequence. */ 
  void 
  addFrame
  (
   int frame  /* IN: frame number */ 
  ) 
  {
    pFrames.insert(frame);
  }
  

  /* Add a bad frame number to the sequence. */ 
  void 
  addBadFrame
  (
   int frame  /* IN: frame number */ 
  ) 
  {
    pBadFrames.insert(frame);
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*  C O N V E R S I O N                                                                   */
  /*----------------------------------------------------------------------------------------*/

  /* Convert to a string representation. */ 
  void
  toString
  (
   string& out   /* OUT: returned string representation */ 
  ) const 
  {
    /* single filename */ 
    if(pPadding == -1) {
      out = pPrefix;
      return;
    }

    /* single file sequence */ 
    if(pFrames.size() == 1) {
      char format[1024];
      sprintf(format, "%%s%%0%dd%%s", pPadding);

      FrameSet::iterator iter = pFrames.begin();
      int frame = (*iter);

      char tmp[1024];
      sprintf(tmp, format, pPrefix.c_str(), frame, pSuffix.c_str());

      out = tmp;

      return;
    }

    /* file sequence pattern */ 
    {
      out = pPrefix;

      if(pPadding == 4) 
	out += "#";
      else {
	int wk;
	for(wk=0; wk<pPadding; wk++)
	  out += "@";
      }
      
      out += (pSuffix + " ");
    }


    /* the frame range and increment */ 
    int by;
    {
      int start, end;
      compFrameRange(start, end, by);

      char range[1024];
      sprintf(range, "(%d-%dx%d", start, end, by);
      out += range;
    }

    
    /* the contiguous sequences in range */ 
    {
      FrameSet::iterator iter = pFrames.begin();
      int last  = (*iter);
      int start = last;
      iter++;

      char tmp[1024];
      sprintf(tmp, "%d", last);
      string seq = tmp;

      bool missing = false;
      for(; iter != pFrames.end(); iter++) {
	if(((*iter) - last) > by) {
	  missing = true;

	  if(last > start) 
	    sprintf(tmp, "-%d,%d", last, *iter);
	  else 
	    sprintf(tmp, ",%d", *iter);
	  seq += tmp;

	  start = *iter;
	}
	
	last = (*iter);
      }

      if(last > start) {
	sprintf(tmp, "-%d", last);
	seq += tmp;
      }
	  
      if(missing) 
	out += (": " + seq + ")");
      else 
	out += ")";
    }


    /* bad frames */ 
    if(pBadFrames.size() > 0) {
      FrameSet::iterator iter = pBadFrames.begin();
      int last  = (*iter);
      int start = last;
      iter++;

      char tmp[1024];
      sprintf(tmp, "%d", last);
      string seq = tmp;

      for(; iter != pBadFrames.end(); iter++) {
	if(((*iter) - last) > by) {
	  if(last > start) 
	    sprintf(tmp, "-%d,%d", last, *iter);
	  else 
	    sprintf(tmp, ",%d", *iter);
	  seq += tmp;

	  start = *iter;
	}
	
	last = (*iter);
      }

      if(last > start) {
	sprintf(tmp, "-%d", last);
	seq += tmp;
      }
	  
      out += (" <BAD: " +seq + ">");
    }
  }

 
  /* Convert to a fcheck command-line representation. */ 
  void
  toFcheckCommand
  (
   string& out   /* OUT: command-line */ 
  ) const 
  {
    out = "fcheck ";

    /* single filename */ 
    if(pPadding == -1) {
      out += pPrefix;
      return;
    }

    /* single file sequence */ 
    if(pFrames.size() == 1) {
      char format[1024];
      sprintf(format, "%%s%%0%dd%%s", pPadding);

      FrameSet::iterator iter = pFrames.begin();
      int frame = (*iter);

      char tmp[1024];
      sprintf(tmp, format, pPrefix.c_str(), frame, pSuffix.c_str());

      out += tmp;

      return;
    }

    /* the frame range and increment */ 
    {
      int start, end, by;
      compFrameRange(start, end, by);

      char range[1024];
      sprintf(range, "-n %d %d %d ", start, end, by);
      out += range;
    }

    /* file pattern */ 
    {
      out += pPrefix;

      if(pPadding == 4) 
	out += "#";
      else 
	out += "@";

      out += pSuffix;
    }    
  }



protected:
  /*----------------------------------------------------------------------------------------*/
  /*  H E L P E R S                                                                         */
  /*----------------------------------------------------------------------------------------*/

  /* Compute the frame range and increment. */ 
  void 
  compFrameRange
  (
   int& start,   /* OUT: start frame */ 
   int& end,     /* OUT: end frame */ 
   int& by       /* OUT: frame increment */ 
  ) const 
  {    
    assert(pFrames.size() > 1);

    FrameSet::iterator iter = pFrames.begin();
    start = (*iter);
    int last = (*iter);
    iter++;

    by = 0;    
    for(; iter != pFrames.end(); iter++) {
      if(by == 0)
	by = (*iter) - last;
      else 
	by = min(by, (*iter) - last);
      
      last = (*iter);
    }
    
    end = last;
  }
      


protected:
  const string  pPrefix;     /* File sequence prefix. */ 
  int           pPadding;    /* Minumum number of frame characters. */ 
  const string  pSuffix;     /* File sequence suffix. */ 

  FrameSet      pFrames;     /* Existing frame numbers. */ 
  FrameSet      pBadFrames;  /* Bad frame numbers. */ 

};
  

/* Less-than operator for FileSeqs */ 
struct ltFileSeq
{
  bool operator()(const FileSeq* a, const FileSeq* b) const
  {
    if(a->getPrefix() < b->getPrefix())
      return true;
    else if(a->getPrefix() > b->getPrefix())
      return false;
    else {
      int padding = a->getPadding() - b->getPadding();
      if(padding < 0)
	return true;
      else if(padding > 0) 
	return false;
      else 
	return (a->getSuffix() < b->getSuffix());	
    }
  }
};
  

} // namespace Pipeline

#endif
