// $Id: PlPut.cc,v 1.2 2002/11/06 00:26:31 jim Exp $

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_FSTREAM
#  include <fstream>
#else
#  ifdef HAVE_FSTREAM_H
#    include <fstream.h>
#  endif
#endif

#ifdef HAVE_FSTREAM
  using std::ifstream;
  using std::ofstream;
#endif

/* REQUIRED by configure */ 
#include <list> 
#include <set>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <errno.h>
/* REQUIRED by configure */ 

#include <FB.hh>

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
  const char* uWork;
  const char* uRepo;
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


void
usage()
{
  cerr << "usage: plput [--verbose] < filelist\n"
       << "       plput --help\n"
       << "       plput --html-help\n"
       << "       plput --version\n"
       << "       plput --release\n";
}



void
main
(
 int argc, 
 char **argv
)
{
  /* parse command line args */ 
  FB::init(cout);
  FB::setWarnings(false);
  FB::setStageStats(false);

  if(argc == 2) {
    if(strcmp(argv[1], "--help") == 0) {
      usage();
      exit(EXIT_SUCCESS);
    }
    else if(strcmp(argv[1], "--html-help") == 0) {

      exit(EXIT_SUCCESS);
    }
    else if(strcmp(argv[1], "--version") == 0) {

      exit(EXIT_SUCCESS);
    }
    else if(strcmp(argv[1], "--release") == 0) {

      exit(EXIT_SUCCESS);
    }
    else if(strcmp(argv[1], "--verbose") == 0) {
      FB::setWarnings(true);
      FB::setStageStats(true);
    }
  }
  else if(argc != 1) {
    usage();
    exit(EXIT_FAILURE);
  }

  FB::stageBegin("Working...");
  char msg[1024];
  
  /* read in the file list from stdin */ 
  typedef list<PathPair*> Pairs;
  Pairs pairs;
  FB::stageBegin("Reading File List:");
  {
    char work[1024];
    char repo[1024];

    while(cin) {
      cin >> work >> repo;
      if(!cin)
	break;
      pairs.push_back(new PathPair(work, repo));

      sprintf(msg, "%s to %s", work, repo);
      FB::stageMsg(msg);
    }
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
    Pairs::iterator iter;
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
    typedef set<const char*, StringCmp> DirSet;
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


  /* make sure the repository files DO NOT already exist */ 
  FB::stageBegin("Rechecking Repository Files:");
  {
    Pairs::iterator iter;
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


  /* copy the files... */ 
  FB::stageBegin("Copying Files:");
  bool aborted = false;
  list<const char*> copied;
  {
    Pairs::iterator iter;
    for(iter=pairs.begin(); iter!=pairs.end() && !aborted; iter++) {
      const char* work = (*iter)->uWork;
      const char* repo = (*iter)->uRepo;
      
      /* open work file */ 
      ifstream in(work);
      if(!in) {
	sprintf(msg, "unable to open working file: %s", work);
	FB::warn(msg);
	aborted = true;
	break;
      }

      /* create/open repository file */ 
      ofstream out(repo, ios::out | ios::noreplace, 0444);
      if(!in) {
	sprintf(msg, "unable to open repository file: %s", repo);
	FB::warn(msg);
	aborted = true;
	break;
      }
      
      /* copy it byte-by-byte */   
      out << in.rdbuf(); 

      /* close the files */ 
      in.close();
      out.close();

      /* success! */ 
      sprintf(msg, "copied: %s to %s", work, repo);
      FB::stageMsg(msg);
    }


    /* a failure occured while copying, 
         remove any successfully copied files */ 
    if(aborted) {
      FB::stageBegin("Copy Failed, Cleaning:");
      {
	list<const char*>::iterator citer;
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
