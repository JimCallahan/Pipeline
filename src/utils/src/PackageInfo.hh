// $Id: PackageInfo.hh,v 1.7 2004/03/10 11:44:39 jim Exp $

#ifndef PIPELINE_PACKAGE_INFO_HH
#define PIPELINE_PACKAGE_INFO_HH

#include <AtomicTypes.hh>
#include <FB.hh>

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E   I N F O                                                                */
/*                                                                                          */
/*    Information about this release of the pipeline tools.                                 */
/*------------------------------------------------------------------------------------------*/

class PackageInfo
{ 
public:
  /*----------------------------------------------------------------------------------------*/
  /*   P U B L I C   F I E L D S                                                            */
  /*----------------------------------------------------------------------------------------*/
 
  /* the version number */ 
  static const char* sVersion;

  /* when the package was released */ 
  static const char* sRelease;

  
  /* pipeline user ID */ 
  static const int sPipelineUID;

  /* pipeline group ID */ 
  static const int sPipelineGID;


  /* installed documentation directory */ 
  static const char* sDocsDir;

  /* location of the Mozilla web browser */ 
  static const char* sMozilla;


  /* copyright message */ 
  static const char* sCopyright;

};

} // namespace Pipeline

#endif
