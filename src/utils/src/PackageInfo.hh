// $Id: PackageInfo.hh,v 1.2 2003/02/10 16:11:12 jim Exp $

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

  /* root directory of the production heirarchy */ 
  static const char* sBaseDir;

  /* installed documentation directory */ 
  static const char* sDocsDir;

  /* location of the Mozilla web browser */ 
  static const char* sMozilla;

  /* installed location of the toolset-exec program */ 
  static const char* sToolsetExec;

  /* installed location of the java interpreter */ 
  static const char* sJava;
  
  /* installed location of the Java Database JAR file */ 
  static const char* sJDBC;

  /* installed location of the GNU make program */ 
  static const char* sMake;

  /* installed location of the diff program */ 
  static const char* sDiff;

  /* copyright message */ 
  static const char* sCopyright;

};

} // namespace Pipeline

#endif
