// $Id: PackageInfo.hh,v 1.10 2004/04/09 18:03:24 jim Exp $

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
 
  /**
   * The version identifier of this Pipeline release.
   */ 
  static const char* sVersion;

  /**
   * The date and time when this version of Pipeline was released.
   */ 
  static const char* sRelease;

  
  /**
   * The "pipeline" user ID 
   */ 
  static const int sPipelineUID;

  /**
   * The pipeline group ID 
   */ 
  static const int sPipelineGID;


  /**
   * The root installed documentation directory. 
   * 
   * Set by appending "/share/docs" to the value of RootInstallDirectory customer profile 
   * entry.
   */ 
  static const char* sDocsDir;


  
  /**
   * The copyright notice for Pipeline. 
   */
  static const char* sCopyright;


  /**
   * The license agreement for Pipeline. 
   */
  static const char* sLicense;

};

} // namespace Pipeline

#endif
