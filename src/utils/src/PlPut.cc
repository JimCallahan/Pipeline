// $Id: PlPut.cc,v 1.6 2003/01/30 01:59:08 jim Exp $

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

using namespace Phoenix;
using namespace Phoenix::Core;

/*------------------------------------------------------------------------------------------*/
/*   P L P U T                                                                              */
/*                                                                                          */
/*     A utility program used internally by the pipeline(1) tool to copy files from a       */
/*     usr's working area into the repository.                                              */
/*------------------------------------------------------------------------------------------*/

class PathPair
{
public:
  PathPair
  (
   char* work,
   char* repo
  ) : 
    uWork(strdup(work)),
    uRepo(strdup(repo))
  {
    assert(uWork);
    assert(uRepo);
  }

  ~PathPair()
  {
    assert(uWork);
    delete[] uWork;

    assert(uRepo);
    delete[] uRepo;
  }

public:
  const char* uWork;          /* absolute path to working area file */ 
  const char* uRepo;          /* absolute path to repository file */ 
};


struct StringCmp
{
  bool operator()
  (
   const char* a, 
   const char* b
  ) const
  {
    return strcmp(a, b) < 0;
  }
};



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



/* copy a single file to the repository, 
     returns false if copy failed */ 
bool
copyFileToRepo
(
 const char* work,                /* IN: working area file */ 
 const char* repo,                /* IN: repository file */ 
 std::list<const char*>& copied   /* IN/OUT: list of succesfully copied files */ 
)
{
  char msg[1024];

  /* open work file */ 
  ifstream in(work);
  if(!in) {
    sprintf(msg, "unable to open working file: %s", work);
    FB::warn(msg);
    return false;
  }
  
  /* create/open repository file */ 
  ofstream out(repo);
  if(!in) {
    sprintf(msg, "unable to open repository file: %s", repo);
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
    sprintf(msg, "unable change mode of repository file: %s", repo);
    FB::warn(msg);
    return false;
  }

  /* success! */ 
  sprintf(msg, "copied: %s to %s", work, repo);
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

  if(argc == 2) {
    if(strcmp(argv[1], "--help") == 0) {
      usage();
      exit(EXIT_SUCCESS);
    }
    else if(strcmp(argv[1], "--html-help") == 0) {
      char buf[1024];
      sprintf(buf, "openURL(file:%s/plput.html, new-window)", Pipeline::PackageInfo::sDocsDir); 

      char* args[4]; 
      args[0] = strdup("mozilla");
      args[1] = strdup("-remote");
      args[2] = strdup(buf);
      args[3] = NULL;

      execv(Pipeline::PackageInfo::sMozilla, args);
    }
    else if(strcmp(argv[1], "--version") == 0) {
      std::cerr << Pipeline::PackageInfo::sVersion << "\n";
      exit(EXIT_SUCCESS);
    }
    else if(strcmp(argv[1], "--release-date") == 0) {
      std::cerr << Pipeline::PackageInfo::sRelease << "\n";
      exit(EXIT_SUCCESS);
    }
    else if(strcmp(argv[1], "--copyright") == 0) {
      std::cerr << Pipeline::PackageInfo::sCopyright << "\n";
      exit(EXIT_SUCCESS);
    }
  }
  else if((argc > 2) && (argc < 5)) {
    int wk;
    for(wk=1; wk<3; wk++) {
      if(strcmp(argv[wk], "--verbose") == 0) {
	FB::setWarnings(true);
	FB::setStageStats(true);
      }
    }
  }
  else {
    usage();
    exit(EXIT_FAILURE);
  }

  char* flist = argv[argc-1];

  FB::stageBegin("Working...");
  char msg[1024];
  
  /* read in the file list from stdin */ 
  typedef std::list<PathPair*> Pairs;
  Pairs pairs;
  FB::stageBegin("Reading File List: ");
  {
    FB::stageMsg(flist);
    char work[1024];
    char repo[1024];

    ifstream in(flist);
    if(!in) {
      char msg[1024];
      sprintf(msg, "Unable to read file list from: %s!", flist);
      FB::error(msg);
    }

    while(in) {
      in >> work >> repo;
      if(!in)
	break;
      pairs.push_back(new PathPair(work, repo));

      sprintf(msg, "%s to %s", work, repo);
      FB::stageMsg(msg);
    }
    in.close();
  }
  FB::stageEnd();


  /* make sure the working area files DO exist */ 
  FB::stageBegin("Checking Working Files:");
  {
    Pairs::iterator iter;
    for(iter=pairs.begin(); iter != pairs.end(); iter++) {
      const char* work = (*iter)->uWork;
      FB::stageMsg(work);
      if(access(work, F_OK) != 0) {
	sprintf(msg, "missing working area file: %s", work);
	FB::error(msg);
      }
    }
  }
  FB::stageEnd();
  

  /* make sure the repository files DO NOT already exist */ 
  FB::stageBegin("Checking Repository Files:");
  {
    std::list<PathPair*>::iterator iter;
    for(iter=pairs.begin(); iter != pairs.end(); iter++) {
      const char* repo = (*iter)->uRepo;
      FB::stageMsg(repo);
      
      struct stat buf;
      if(stat(repo, &buf) == 0) {
	if(S_ISDIR(buf.st_mode)) {
	  sprintf(msg, "bad path, directory exists with the name: %s", repo);
	  FB::error(msg);	    
	}
	else {
	  sprintf(msg, "attempted to overwrite repository file: %s", repo);
	  FB::error(msg);
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
  	    sprintf(msg, "illegal path: %s", dir);
  	    FB::error(msg);
  	    break;
	    
  	  case ENOENT:
  	    create = true;
  	    break;

  	  case ENOTDIR:
  	    sprintf(msg, "bad directory path, file exists with the name: %s", dir);
  	    FB::error(msg);
  	    break;
	    
  	  case ELOOP:
  	    sprintf(msg, "encountered too many symbolic links for: %s", dir);
  	    FB::error(msg);
  	    break;
	    
  	  default:
  	    sprintf(msg, "internal error in stat(2): %s", dir);
  	    FB::error(msg);
  	  }
  	}
  	else {
  	  if(!S_ISDIR(buf.st_mode)) {
  	    sprintf(msg, "bad path, file exists with the name: %s", dir);
  	    FB::error(msg);	    
  	  }
  	}

  	if(create) {
  	  if(mkdir(dir, 0755) == 0) {
  	    sprintf(msg, "created: %s", dir);
  	    FB::stageMsg(msg);
  	  }
  	  else {
  	    sprintf(msg, "failed to create: %s", dir);
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
  std::list<const char*> copied;
  {
    Pairs::iterator iter;
    for(iter=pairs.begin(); iter!=pairs.end() && !aborted; iter++) {      
      if(!copyFileToRepo((*iter)->uWork, (*iter)->uRepo, copied)) {
	aborted = true;
	break;
      }     
    }

    /* a failure occured while copying, 
         remove any successfully copied files */ 
    if(aborted) {
      FB::stageBegin("Copy Failed, Cleaning:");
      {
	std::list<const char*>::iterator citer;
	for(citer=copied.begin(); citer!=copied.end() && !aborted; citer++) {
	  const char* file = *citer;
	  if(unlink(file) != 0) {
	    sprintf(msg, "unable to remove: %s", file);
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
