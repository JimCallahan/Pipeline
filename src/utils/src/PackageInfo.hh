// $Id: PackageInfo.hh,v 1.5 2003/12/17 04:47:59 jim Exp $

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


  /* server hostname */ 
  static const char* sServer;

  /* server port number */ 
  static const int sPort;

  
  /* pipeline user ID */ 
  static const int sPipelineUID;

  /* pipeline group ID */ 
  static const int sPipelineGID;


  /* root directory of the production heirarchy */ 
  static const char* sBaseDir;

  /* base repository directory */ 
  static const char* sRepoDir;
  
  /* base working area directory */ 
  static const char* sWorkDir;
  
  /* installed toolset directory */ 
  static const char* sToolsetDir;
  
  /* installed location of the toolset-exec program */ 
  static const char* sToolsetExec;


  /* installed documentation directory */ 
  static const char* sDocsDir;

  /* location of the Mozilla web browser */ 
  static const char* sMozilla;


  /* installed location of the GNU Bourne Again SHell */ 
  static const char* sBash;
  
  /* installed location of the GNU make program */ 
  static const char* sMake;
  
  /* installed location of the md5sum program */ 
  static const char* sMd5sum;
  
  /* installed location of the diff program */ 
  static const char* sDiff;
  
  /* installed location of the kill program */ 
  static const char* sKill;
  
  /* installed location of the SSH program */ 
  static const char* sSsh;

  /* installed location of the YPCAT program */ 
  static const char* sYpCat;


  /* copyright message */ 
  static const char* sCopyright;

};

} // namespace Pipeline

#endif
