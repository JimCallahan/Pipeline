// $Id: PlShakeIO.cc,v 1.1 2005/10/09 17:28:20 jim Exp $

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
#endif

#include <NRiSFileIn.h>
#include <NRiFileOut.h>
#include <NRiScript.h>
#include <NRiSPlug.h>
#include <NRiRegistry.h>
#include <NRiSettingsHook.h>
#include <NRiGlobals.h>
#include <NRiProductInfo.h>
#include <NRiFx.h>
#include <NRiDir.h>
#include <NRiFile.h>

#include <PackageInfo.hh>
#include <FileSeq.hh>
#include <HtmlHelp.hh>

using namespace Pipeline;

/*------------------------------------------------------------------------------------------*/
/*   P   S H A K E   I O                                                                    */
/*                                                                                          */
/*     Lists information about the FileIn, SFileIn and FileOut nodes in a Shake script.     */
/*------------------------------------------------------------------------------------------*/

/* usage message */ 
void
usage()
{
  std::cerr << "USAGE:\n" 
	    << "  pshakeio script.shk\n"
	    << "\n" 
	    << "  pshakeio --help\n"
	    << "  pshakeio --html-help\n"
	    << "  pshakeio --version\n"
	    << "  pshakeio --release-date\n"
	    << "  pshakeio --copyright\n" 
	    << "  pshakeio --license\n" 
	    << "\n" 
	    << "OPTIONS:\n" 
	    << "  [--stats=LEVEL] [--warnings=LEVEL]\n" 
	    << "\n"
	    << "\n"
	    << "Use \"pshakeio --html-help\" to browse the full documentation.\n" 
	    << std::flush;
}

int
main
(
 int argc, 
 char **argv
)
{
  /* parse command line args */ 
  char msg[2048];
  int statsLevel = -1;
  int warningsLevel = -1;
  char* scriptPath = NULL;
  switch(argc) {
  case 1:
    break;

  case 2:
    if(strcmp(argv[1], "--help") == 0) {
      usage();
      exit(EXIT_SUCCESS);
    }
    else if(strcmp(argv[1], "--html-help") == 0) {
      HtmlHelp::launch("pshakeio");
    }
    else if(strcmp(argv[1], "--version") == 0) {
      std::cerr << PackageInfo::sVersion << "\n";
      exit(EXIT_SUCCESS);
    }
    else if(strcmp(argv[1], "--release-date") == 0) {
      std::cerr << PackageInfo::sRelease << "\n";
      exit(EXIT_SUCCESS);
    }
    else if(strcmp(argv[1], "--copyright") == 0) {
      std::cerr << PackageInfo::sCopyright << "\n";
      exit(EXIT_SUCCESS);
    }
    else if(strcmp(argv[1], "--license") == 0) {
      std::cerr << PackageInfo::sLicense << "\n";
      exit(EXIT_SUCCESS);
    }
  }

  {
    int i = 1;
    for(i=1; i<argc; i++) {
      if(strncmp(argv[i], "--stats=", 8) == 0) 
	statsLevel = atoi(argv[i]+8);
      else if(strncmp(argv[i], "--warnings=", 11) == 0) 
	warningsLevel = atoi(argv[i]+11);
      else if(strncmp(argv[i], "--", 2) == 0) {
	sprintf(msg, "Illegal option: %s", argv[i]);
	FB::error(msg);
      }
      else {
	scriptPath = argv[i];
      }
    }
  }

  /* initialize the loggers */ 
  {
    FB::init(std::cout);
    
    if(statsLevel > 0) 
      FB::setStageStats(true, statsLevel);
    else 
      FB::setStageStats(false);
    
    if(warningsLevel > 0) 
      FB::setWarnings(true, warningsLevel);
    else
      FB::setWarnings(false);
  }

  FB::stageBegin("Working...", 1);

  /* Shake init */ 
  NRiSys::initialize(argc, argv);

  /* create a script object */ 
  NRiScript *script = new NRiScript();
  NRiRegistry::activate(1);
  
  /* load the script from file */ 
  FB::stageBegin("Loading Script...", 2);
  {
    script->loadScript(NRiName(scriptPath));
    script->initialize(script->timePlug->asFloat());
  }
  FB::stageEnd(2);
  
  /* process the nodes */ 
  {
    int numNodes = script->getNbChildren();
    int wk;
    for(wk=0; wk<numNodes; wk++) {
      NRiNode *node = (NRiNode*) script->getNthChild(wk);
      
      NRiFileIn*  fi = dynamic_cast<NRiFileIn*>(node);
      NRiFileOut* fo = dynamic_cast<NRiFileOut*>(node);

      if(fi || fo) {

	const char* nodeName = node->getName().getString();
	const char* imageName = NULL;
	const char* inMode = NULL;
	const char* outMode = NULL;
	NRiName pprefix, psuffix, prange, pformat; 
	float speed, timeShift, inPoint, outPoint; 
	{
	  NRiPArray<NRiPlug> plugs;
	  node->getPublicPlugs(plugs);
	  int numPlugs = plugs.getNbItems();
	  int pk;
	  for(pk=0; pk<numPlugs; pk++) {
	    NRiPlug *p = plugs[pk];
	    NRiId type = p->getType();
	    NRiIPlug *ip = dynamic_cast<NRiIPlug*>(p);
	    
	    const char* pname = p->getFullName().getString();
	    if((strcmp(pname, "imageName") == 0) && (type == kString)) {
	      imageName = p->asString().getString();
	      NRiName offset, formatDesc;
	      NRiFile::splitFileName(imageName, pprefix, psuffix, prange, pformat, 
				     offset, formatDesc);
	      
// 	      std::cout << "--------------------------------------------\n";
//   	      std::cout << "  " << pname << " = " << imageName << "\n"
//   			<< "    prefix - " << (pprefix.getString()) << "\n"
//   			<< "    suffix - " << (psuffix.getString()) << "\n"
//   			<< "    range - " << (prange.getString()) << "\n"
//   			<< "    format - " << (pformat.getString()) << "\n"
//   			<< "    offset - " << (offset.getString()) << "\n"
//   			<< "    formatDesc - " << (formatDesc.getString()) << "\n";
// 	      std::cout << "--------------------------------------------\n";
	    }
	    else if(fi) {
	      if((strcmp(pname, "timeShift") == 0) && (type == kFloat)) 
		timeShift = p->asFloat();
	      else if((strcmp(pname, "speed") == 0) && (type == kFloat)) 
		speed = p->asFloat();
	      else if((strcmp(pname, "inPoint") == 0) && (type == kFloat)) 
		inPoint = p->asFloat();
	      else if((strcmp(pname, "outPoint") == 0) && (type == kFloat)) 
		outPoint = p->asFloat();
	      else if((strcmp(pname, "inMode") == 0) && (type == kString)) 
		inMode  = p->asString().getString();
	      else if((strcmp(pname, "outMode") == 0) && (type == kString)) 
		outMode  = p->asString().getString();
	    }
	  }
	}

	if(fi) 
	  std::cout << "FileIn ";
	else if(fo) 
	  std::cout << "FileOut ";

	std::cout << nodeName << " ";

	if(!pprefix.isNull() && (pprefix.getLength() > 1)) { 
	  if(prange.isNull() && psuffix.isNull()) {
	    std::cout << pprefix.getString();
	  }
	  else {
	    std::cout << strndup(pprefix.getString(), pprefix.getLength()-1);
	    
	    if(!prange.isNull() && !pformat.isNull()) {
	      const char* format = pformat.getString();
	      if(strcmp(format, "%d") == 0) 
		std::cout << ".@";
	      else {
		int padding = atoi(strndup(format+1, strlen(format)-2));
		if(padding == 4) 
		  std::cout << ".#";
		else {
		  std::cout << ".";
		  int i; 
		  for(i=0; i<padding; i++) 
		    std::cout << "@";
		}
	      }	    
	    }
	    
	    if(!psuffix.isNull()) 
	      std::cout << psuffix.getString();
	    
	    if(!prange.isNull() && !pformat.isNull()) 
	      std::cout << "," << prange.getString();
	  }
	}
	else {
	  std::cout << "-";
	}
	
	if(fi) {
	  std::cout << " " << timeShift << " " << speed << " " 
		    << inPoint << " " << inMode << " " << outPoint << " " << outMode;
	}

	std::cout << "\n";
      }
    }
  }
  FB::stageEnd(2);
  
  
  return EXIT_SUCCESS;
}
