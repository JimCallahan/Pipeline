// $Id: AtomicTypes.hh,v 1.1 2003/09/22 16:44:37 jim Exp $

#ifndef PIPELINE_ATOMIC_TYPES_HH
#define PIPELINE_ATOMIC_TYPES_HH

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_MATH_H
#  include <math.h>
#endif

#ifdef HAVE_FLOAT_H
#  include <float.h>
#endif

#ifdef HAVE_CSTDLIB
#  include <cstdlib>
#else
#  ifdef HAVE_STDLIB_H
#    include <stdlib.h>
#  endif
#endif

#ifdef HAVE_ALGORITHM
#  include <algorithm>
#else
#  ifdef HAVE_ALGOBASE
#    include <algobase>
#  else
#    ifdef HAVE_ALGOBASE_H
#      include <algobase.h>
#    endif
#  endif
#endif

/*------------------------------------------------------------------------------------------*/
/*   A T O M I C   T Y P E S                                                                */
/*                                                                                          */
/*     All Phoenix code uses these typedefs when refering to the atomic C types.  The type  */
/*     names defined here are based on the literal bit sizes of the types.  This should     */
/*     help avoid any storage size related unpleasantness when the code is ported to a OS   */
/*     with weird sizes (like IRIX).                                                        */
/*------------------------------------------------------------------------------------------*/
											    
namespace Pipeline {								     	    

#if defined(SIZEOF_CHAR) && (SIZEOF_CHAR == 1)
  typedef signed char             Int8;
  typedef unsigned char          UInt8;
#endif  

#if defined(SIZEOF_SHORT) && (SIZEOF_SHORT == 2)
  typedef signed short           Int16;
  typedef unsigned short        UInt16;
#endif  

#if defined(SIZEOF_INT) && (SIZEOF_INT == 4)
  typedef signed int             Int32;
  typedef unsigned int          UInt32;
#endif  

#if defined(SIZEOF_LONG) && (SIZEOF_LONG == 8)
  typedef signed long             Int64;
  typedef unsigned long          UInt64;
#else 
#  if defined(SIZEOF_LONG_LONG) && (SIZEOF_LONG_LONG == 8)
     typedef signed long long     Int64;
     typedef unsigned long long  UInt64;
#  endif
#endif  

#if defined(SIZEOF_FLOAT) && (SIZEOF_FLOAT == 4)
  typedef float                  Real32;
#endif  

#if defined(SIZEOF_DOUBLE) && (SIZEOF_DOUBLE == 8)
  typedef double                 Real64;
#endif  

#if defined(SIZEOF_LONG_DOUBLE) && (SIZEOF_LONG_DOUBLE == 12)
  typedef long double            Real96;
#endif  

} // namespace Pipeline

#endif 
