// $Id: PlPut.cc,v 1.4 2003/01/25 01:35:04 jim Exp $

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

    uWorkMd5 = md5path(uWork);
    uRepoMd5 = md5path(uRepo);
  }

  ~PathPair()
  {
    assert(uWork);
    delete[] uWork;

    assert(uRepo);
    delete[] uRepo;

    assert(uWorkMd5);
    delete[] uWorkMd5;

    assert(uRepoMd5);
    delete[] uRepoMd5;
  }

protected:
  /* generate paths to the MD5 checksum file from the file path */ 
  char* 
  md5path
  (
   const char* path
  ) 
  {
    char* tmp1 = strdup(path);
    char* tmp2 = strdup(path);
    char* dir  = dirname(tmp1);
    char* file = basename(tmp2);

    char md5[1024];
    sprintf(md5, "%s/.md5sum/%s", dir, file);

    delete[] tmp1;
    delete[] tmp2;

    return strdup(md5);
  }

public:
  const char* uWork;          /* absolute path to working area file */ 
  const char* uRepo;          /* absolute path to repository file */ 

  const char* uWorkMd5;       /* absolute path to working area MD5 checksum file */ 
  const char* uRepoMd5;       /* absolute path to repository MD5 checksum file */
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
  std::cerr << "usage: plput [--verbose] [--md5] listfile\n"
	    << "       plput --help\n"
	    << "       plput --html-help\n"
	    << "       plput --version\n"
	    << "       plput --release-date\n"
	    << "       plput --copyright\n" 
	    << std::flush;
}


/* make sure repository file does not exist (as a file or directory) */ 
void
checkRepoFiles
(
 list<PathPair*>& pairs,    /* IN: list of work/repo pairs */ 
 bool md5                         /* IN: check MD5 checksums as well? */ 
)
{
  char msg[1024];

  list<PathPair*>::iterator iter;
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
    
    if(md5) {
      const char* md5 = (*iter)->uRepoMd5;
      if(stat(md5, &buf) == 0) {
	if(S_ISDIR(buf.st_mode)) {
	  sprintf(msg, "bad path, directory exists with the name: %s", md5);
	  FB::error(msg);	    
	}
	else {
	  sprintf(msg, "attempted to overwrite repository MD5 file: %s", md5);
	  FB::error(msg);
	}
      }
    }
  }
}


/* copy a single file to the repository, 
     returns false if copy failed */ 
bool
copyFileToRepo
(
 const char* work,           /* IN: working area file */ 
 const char* repo,           /* IN: repository file */ 
 list<const char*>& copied   /* IN/OUT: list of succesfully copied files */ 
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
  ofstream out(repo, ios::out | ios::noreplace, 0444);
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
  
  /* success! */ 
  sprintf(msg, "copied: %s to %s", work, repo);
  FB::stageMsg(msg);

  return true;
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

  bool md5 = false;
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
      else if(strcmp(argv[wk], "--md5") == 0) {
	md5 = true;
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
  typedef list<PathPair*> Pairs;
  Pairs pairs;
  FB::stageBegin("Reading File List: ");
  {
    FB::stageMsg(flist);
    char work[1024];
    char repo[1024];

    ifstream in(flist);
    if(!in) {
      char msg[1024];
      sprintf(msg, "Unable to read file list from: \"%s\"!", flist);
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

      if(md5) {
	const char* md5 = (*iter)->uWorkMd5;
	FB::stageMsg(md5);
	if(access(md5, F_OK) != 0) {
	  sprintf(msg, "missing working area MD5 checksum file: %s", md5);
	  FB::error(msg);
	}
      }
    }
  }
  FB::stageEnd();
  

  /* make sure the repository files DO NOT already exist */ 
  FB::stageBegin("Checking Repository Files:");
  {
    checkRepoFiles(pairs, md5);
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
	char* repo = NULL;
	if(md5) 
	  repo = strdup((*iter)->uRepoMd5);
	else 
	  repo = strdup((*iter)->uRepo);

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


  /* make sure the repository files DO NOT already exist (just to be absolutely safe!) */ 
  FB::stageBegin("Rechecking Repository Files:");
  {
    checkRepoFiles(pairs, md5);
  }
  FB::stageEnd();


  /* copy the files... */ 
  FB::stageBegin("Copying Files:");
  bool aborted = false;
  list<const char*> copied;
  {
    Pairs::iterator iter;
    for(iter=pairs.begin(); iter!=pairs.end() && !aborted; iter++) {      
      if(!copyFileToRepo((*iter)->uWork, (*iter)->uRepo, copied)) {
	aborted = true;
	break;
      }     

      if(!copyFileToRepo((*iter)->uWorkMd5, (*iter)->uRepoMd5, copied)) {
	aborted = true;
	break;
      }
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
