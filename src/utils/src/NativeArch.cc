// $Id: NativeArch.cc,v 1.1 2004/08/29 09:18:36 jim Exp $

#include <NativeArch.hh>

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   N A T I V E   A R C H                                                                  */
/*                                                                                          */
/*    Information about the native machine architecture.                                    */
/*------------------------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------------------*/
/*   P U B L I C   F I E L D S                                                            */
/*----------------------------------------------------------------------------------------*/
  
/**
 * If not (NULL), use native libraries and binaries from the subdirectory named after 
 * the given hostname.
 */ 
const char* NativeArch::sHostname = NULL;  

} // namespace Pipeline
