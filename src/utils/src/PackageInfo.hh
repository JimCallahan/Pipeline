// $Id: PackageInfo.hh,v 1.12 2004/07/14 20:43:43 jim Exp $

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
   * The build mode: "opt" or "dbg".
   */ 
  static const char* sBuildMode;


  /**
   * The "pipeline" user ID 
   */ 
  static const int sPipelineUID;

  /**
   * The pipeline group ID 
   */ 
  static const int sPipelineGID;



  /**
   * The installed location of Java.
   */ 
  static const char* sJavaHome;

  /**
   * The runtime options to be passed to Java.
   */ 
  static const char* sJavaRuntimeFlags;
  

  /** 	
   * The root installation directory. <P>
   *
   * Set by the RootInstallDirectory customer profile entry.
   */ 
  static const char* sInstDir;

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
