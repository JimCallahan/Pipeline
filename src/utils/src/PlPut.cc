// $Id: PlPut.cc,v 1.13 2003/10/09 00:26:42 jim Exp $

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_FSTREAM
#  include <fstream>
   using std::ifstream;
   using std::ofstream;
#else
#  ifdef HAVE_FSTREAM_H
#    include <fstream.h>
#  endif
#endif

#ifdef HAVE_LIST
#  include <list>
#endif

#ifdef HAVE_SET
#  include <set>
#endif

#ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
#endif

#ifdef HAVE_SYS_STAT_H
#  include <sys/stat.h>
#endif

#ifdef HAVE_UNISTD_H
#  include <unistd.h>
#endif

#ifdef HAVE_ERRNO_H
#  include <errno.h>
#endif

#ifdef HAVE_LIBGEN_H
#  include <libgen.h>
#endif

#include <PackageInfo.hh>
#include <PlCommon.hh>

using namespace Pipeline;

/*------------------------------------------------------------------------------------------*/
/*   P L P U T                                                                              */
/*                                                                                          */
/*     A utility program used internally by the pipeline(1) tool to copy files from a       */
/*     usr's working area into the repository.                                              */
/*------------------------------------------------------------------------------------------*/

/* usage message */ 
void
usage()
{
  std::cerr << "usage: plput [--verbose] listfile\n"
	    << "       plput --help\n"
	    << "       plput --html-help\n"
	    << "       plput --version\n"
	    << "       plput --release-date\n"
	    << "       plput --copyright\n" 
	    << std::flush;
}



/* create a single symlink pointing to a previous repository version, 
     returns false if link failed */ 
bool
linkRepo
(
 const char* prev,    /* IN: previous repository file (or link) */ 
 const char* repo     /* IN: repository link */ 
)
{
  if(symlink(prev, repo) != 0)
    return false;

  char msg[1024];
  sprintf(msg, "Linked: %s to %s", prev, repo);
  FB::stageMsg(msg);

  return true;
}



/* copy a single file to the repository, 
     returns false if copy failed */ 
bool
copyFileToRepo
(
 const char* work,   /* IN: working area file */ 
 const char* repo    /* IN: repository file */ 
)
{
  char msg[1024];

  /* open work file */ 
  ifstream in(work);
  if(!in) {
    sprintf(msg, "Unable to open working file: %s", work);
    FB::warn(msg);
    return false;
  }
  
  /* create/open repository file */ 
  ofstream out(repo);
  if(!in) {
    sprintf(msg, "Unable to open repository file: %s", repo);
    FB::warn(msg);
    return false;
  }
  
  /* copy it byte-by-byte */   
  out << in.rdbuf(); 
  
  /* close the files */ 
  in.close();
  out.close();
  
  /* chmod 444 */ 
  if(chmod(repo, 0444) != 0) {
    sprintf(msg, "Unable change mode of repository file: %s", repo);
    FB::warn(msg);
    return false;
  }

  /* success! */ 
  sprintf(msg, "Copied: %s to %s", work, repo);
  FB::stageMsg(msg);

  return true;
}


  
int
main
(
 int argc, 
 char **argv
)
{
  /* parse command line args */ 
  FB::init(std::cout);
  FB::setWarnings(false);
  FB::setStageStats(false);

  switch(argc) {
  case 2:
    if(strcmp(argv[1], "--help") == 0) {
      usage();
      exit(EXIT_SUCCESS);
    }
    else if(strcmp(argv[1], "--html-help") == 0) {
      char buf[1024];
      sprintf(buf, "openURL(file:%s/plput.html, new-window)", 
	      PackageInfo::sDocsDir); 

      char* args[4]; 
      args[0] = strdup("mozilla");
      args[1] = strdup("-remote");
      args[2] = strdup(buf);
      args[3] = NULL;

      execv(PackageInfo::sMozilla, args);
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
    else {
      usage();
      exit(EXIT_FAILURE);      
    }
    break;

  case 3:
    if(strcmp(argv[1], "--verbose") == 0) {
      FB::setWarnings(true);
      FB::setStageStats(true);
    }
    else {
      usage();
      exit(EXIT_FAILURE);      
    }
    break;

  default:
    usage();
    exit(EXIT_FAILURE);
  }

  char* flist = argv[argc-1];

  FB::stageBegin("Working...");
  char msg[1024];
  
  /* read in the file list */ 
  int workDirSize = strlen(PackageInfo::sWorkDir);
  int repoDirSize = strlen(PackageInfo::sRepoDir);
  typedef std::list<PathPair*> Pairs;
  Pairs pairs;
  FB::stageBegin("Reading File List: ");
  {
    FB::stageMsg(flist);
    char mode[1024];
    char work[1024];
    char repo[1024];

    ifstream in(flist);
    if(!in) {
      char msg[1024];
      sprintf(msg, "Unable to read file list from: %s!", flist);
      FB::error(msg);
    }

    while(in) {
      in >> mode >> work >> repo;
      if(!in)
	break;

      bool isLink;
      if(strcmp(mode, "link") == 0)
	isLink = true;
      else if(strcmp(mode, "copy") == 0)
	isLink = false;
      else {
	sprintf(msg, "Illegal mode \"%s\" encountered!", mode);
	FB::error(msg);
      }

      if(isLink) {
	if(strncmp(work, PackageInfo::sRepoDir, repoDirSize) != 0) {
	  sprintf(msg, "Illegal repository filename \"%s\" encountered!", work);
	  FB::error(msg);	
	}
      }
      else {
	if(strncmp(work, PackageInfo::sWorkDir, workDirSize) != 0) {
	  sprintf(msg, "Illegal working area filename \"%s\" encountered!", work);
	  FB::error(msg);	
	}
      }
	  
      if(strncmp(repo, PackageInfo::sRepoDir, repoDirSize) != 0) {
	sprintf(msg, "Illegal repository filename \"%s\" encountered!", repo);
	FB::error(msg);	
      }

      pairs.push_back(new PathPair(isLink, work, repo));

      sprintf(msg, "%s: %s to %s", mode, work, repo);
      FB::stageMsg(msg);
    }
    in.close();
  }
  FB::stageEnd();


  /* make sure the working area files DO exist */ 
  FB::stageBegin("Verifying Source Files:");
  {
    Pairs::iterator iter;
    for(iter=pairs.begin(); iter != pairs.end(); iter++) {
      const char* work = (*iter)->uWork;
      FB::stageMsg(work);
      if(access(work, F_OK) != 0) {
	sprintf(msg, "Missing source file: %s", work);
	FB::error(msg);
      }
    }
  }
  FB::stageEnd();
  

  /* make sure the repository files DO NOT already exist */ 
  FB::stageBegin("Verifying Target Files:");
  {
    std::list<PathPair*>::iterator iter;
    for(iter=pairs.begin(); iter != pairs.end(); iter++) {
      const char* repo = (*iter)->uRepo;
      FB::stageMsg(repo);
      
      if(access(repo, F_OK) != 0) {
	struct stat buf;
	if(stat(repo, &buf) == 0) {
	  if(S_ISREG(buf.st_mode)) {
	    sprintf(msg, "Attempted to overwrite repository file: %s", repo);
	    FB::error(msg);
	  }
	  else if(S_ISDIR(buf.st_mode)) {
	    sprintf(msg, "Bad path, directory exists with the name: %s", repo);
	    FB::error(msg);	    
	  }
	  else if(S_ISLNK(buf.st_mode)) {
	    sprintf(msg, "Attempted to overwrite repository symlink: %s", repo);
	    FB::error(msg);	    
	  }	
	  else {
	    sprintf(msg, "Somehow the repostory file exists and is NOT a regular file: %s", 
		    repo);
	    FB::error(msg);	    
	  }
	}
      }
    }
  }
  FB::stageEnd();

  
  /* create any missing subdirectories */ 
  FB::stageBegin("Creating Repository Directories:");
  {
    /* build a list of unique directories needed by all paths */ 
    typedef std::set<const char*, StringCmp> DirSet;
    DirSet dirs;
    {
      Pairs::iterator iter;
      for(iter=pairs.begin(); iter != pairs.end(); iter++) {
	char* repo = strdup((*iter)->uRepo);
	char* p = repo;
	p += strlen(repo)-1;

	while(p > repo) {
	  if(*p == '/') {
	    char* q = p;
	    p--;
	    *q = '\0';
	    dirs.insert(strdup(repo));
	  }

	  p--;
	}
      }      
    }

    /* create them (subdirs will be created first due to set ordering) */
    {
      DirSet::iterator iter;
      for(iter=dirs.begin(); iter != dirs.end(); iter++) {
	const char* dir = *iter;
	assert(dir);
	assert(strlen(dir) > 0);

  	bool create = false;
  	struct stat buf;
  	if(stat(dir, &buf) != 0) {
  	  switch(errno) {
  	  case EBADF:
  	    sprintf(msg, "Illegal path: %s", dir);
  	    FB::error(msg);
  	    break;
	    
  	  case ENOENT:
  	    create = true;
  	    break;

  	  case ENOTDIR:
  	    sprintf(msg, "Bad directory path, file exists with the name: %s", dir);
  	    FB::error(msg);
  	    break;
	    
  	  case ELOOP:
  	    sprintf(msg, "Encountered too many symbolic links for: %s", dir);
  	    FB::error(msg);
  	    break;
	    
  	  default:
  	    sprintf(msg, "Internal error in stat(2): %s", dir);
  	    FB::error(msg);
  	  }
  	}
  	else {
  	  if(!S_ISDIR(buf.st_mode)) {
  	    sprintf(msg, "Bad path, file exists with the name: %s", dir);
  	    FB::error(msg);	    
  	  }
  	}

  	if(create) {
  	  if(mkdir(dir, 0755) == 0) {
  	    sprintf(msg, "Created: %s", dir);
  	    FB::stageMsg(msg);
  	  }
  	  else {
  	    sprintf(msg, "Failed to create: %s", dir);
  	    FB::error(msg);	    
  	  }
  	}	
      }
    }
  }
  FB::stageEnd();


  /* copy the files... */ 
  FB::stageBegin("Copying Files:");
  bool aborted = false;
  std::list<const char*> created;
  {
    Pairs::iterator iter;
    for(iter=pairs.begin(); iter!=pairs.end() && !aborted; iter++) {      
      if((*iter)->uIsLink) {
	if(!linkRepo((*iter)->uWork, (*iter)->uRepo)) {
	  aborted = true;
	  break;
	}     
      }
      else {
	if(!copyFileToRepo((*iter)->uWork, (*iter)->uRepo)) {
	  aborted = true;
	  break;
	}     
      }
      
      created.push_back((*iter)->uRepo);
    }

    /* a failure occured while copying, 
         remove any successfully created files */ 
    if(aborted) {
      FB::stageBegin("Copy Failed, Cleaning:");
      {
	std::list<const char*>::iterator citer;
	for(citer=created.begin(); citer!=created.end() && !aborted; citer++) {
	  const char* file = *citer;
	  if(unlink(file) != 0) {
	    sprintf(msg, "Unable to remove: %s", file);
	    FB::warn(msg);
	  }
	}
      }
      FB::stageEnd();
    }

  }
  FB::stageEnd();
  
  if(aborted) {
    FB::stageMsg("ABORTED!");
    exit(EXIT_FAILURE);
  }

  FB::stageMsg("Copy Succeeded!");

  FB::stageEnd();

  return EXIT_SUCCESS;
}
