// $Id: NativeArch.hh,v 1.1 2004/08/29 09:18:36 jim Exp $

#ifndef PIPELINE_NATIVE_ARCH_HH
#define PIPELINE_NATIVE_ARCH_HH

#include <AtomicTypes.hh>

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   N A T I V E   A R C H                                                                  */
/*                                                                                          */
/*    Information about the native machine architecture.                                    */
/*------------------------------------------------------------------------------------------*/

class NativeArch
{ 
public:
  /*----------------------------------------------------------------------------------------*/
  /*   P U B L I C   F I E L D S                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * If not (NULL), use native libraries and binaries from the subdirectory named after 
   * the given hostname.
   */ 
  static const char* sHostname; 

};

} // namespace Pipeline

#endif
