// $Id: Pls.cc,v 1.2 2003/10/11 04:16:07 jim Exp $

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
#endif

#ifdef HAVE_DIRENT_H
#  include <dirent.h>
#endif

#ifdef HAVE_UNISTD_H
#  include <unistd.h>
#endif

#ifdef HAVE_CTYPE_H
#  include <ctype.h>
#endif

#ifdef HAVE_SYS_STAT_H
#  include <sys/stat.h>
#endif

#ifdef HAVE_ERRNO_H
#  include <errno.h>
#endif

#ifdef HAVE_LIST
#  include <list>
#endif

#include <PackageInfo.hh>
#include <FileSeq.hh>

using namespace Pipeline;

/*------------------------------------------------------------------------------------------*/
/*   P L S                                                                                  */
/*                                                                                          */
/*     A directory listing program which lists file sequences.                              */
/*------------------------------------------------------------------------------------------*/

/* usage message */ 
void
usage()
{
  std::cerr << "usage: pls [options][directory]\n"
	    << "\n" 
	    << "       pls --help\n"
	    << "       pls --html-help\n"
	    << "       pls --version\n"
	    << "       pls --release-date\n"
	    << "       pls --copyright\n" 
	    << "\n" 
	    << "OPTIONS:\n" 
	    << "  [--fcheck][--fcheck-exec][--zero][--size=bytes]\n" 
	    << "\n"
	    << "\n"
	    << "Use \"pls --html-help\" to browse the full documentation.\n" 
	    << std::flush;
}

int
main
(
 int argc, 
 char **argv
)
{
  char msg[2048];

  /* parse command line args */ 
  FB::init(std::cout);
  FB::setWarnings(false);
  FB::setStageStats(false);

  bool fcheck = false;
  bool exec = false;
  long int minSize = -1;
  char* dir = NULL;
  switch(argc) {
  case 1:
    break;

  case 2:
    {
      if(strcmp(argv[1], "--help") == 0) {
	usage();
	exit(EXIT_SUCCESS);
      }
      else if(strcmp(argv[1], "--html-help") == 0) {
	char buf[1024];
	sprintf(buf, "openURL(file:%s/pls.html, new-window)", 
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
      else if(strcmp(argv[1], "--fcheck") == 0) 
	fcheck = true;
      else if(strcmp(argv[1], "--fcheck-exec") == 0) {
	fcheck = true;
	exec = true;
      }
      else if(strcmp(argv[1], "--zero") == 0) 
	minSize = 0;
      else if(strncmp(argv[1], "--size=", 7) == 0) 
	minSize = atol(argv[1]+7);
      else if(strncmp(argv[1], "--", 2) == 0) {
	sprintf(msg, "Illegal option: %s", argv[1]);
	FB::error(msg);
      }
      else 
	dir = argv[1];
    }
    break;

  case 3:
    {
      if(strcmp(argv[1], "--fcheck") == 0)
	fcheck = true;
      else if(strcmp(argv[1], "--fcheck-exec") == 0) {
	fcheck = true;
	exec = true;
      }
      else if(strcmp(argv[1], "--zero") == 0) 
	minSize = 0;
      else if(strncmp(argv[1], "--size=", 7) == 0) 
	minSize = atol(argv[1]+7);
      else if(strncmp(argv[1], "--", 2) == 0) {
	sprintf(msg, "Illegal option: %s", argv[1]);
	FB::error(msg);
      }
      
      dir = argv[2];
    }
    break;

  default:
    usage();
    exit(EXIT_FAILURE);
  }


  /* if no directory was given, determine the current directory */ 
  bool isCwd = false;
  if(dir == NULL) {
    isCwd = true;
    dir = new char[2048];
    if(getcwd(dir, 2048) == NULL) { 
      switch(errno) {
      case EACCES:
	FB::error("No permission to read the current directory.");
	break;

      case ENOENT:
	FB::error("The current working directory has been unlinked.");
	break;
	 
      default:
	FB::error("Internal error!");
      }
    }
  }
  assert(dir != NULL);

	
  /* build a list of filenames the current diretory */ 
  typedef std::list<string*> FileList;
  FileList files;
  FileList bad;
  {
    struct dirent **namelist;
    int n;
    if((n = scandir(dir, &namelist, NULL, alphasort)) == -1) {
      sprintf(msg, "Unable to read the directory: %s", dir);
      FB::error(msg);
    }

    while(n--) {
      string file;
      if(!isCwd) {
	file += dir;
	file += "/";
      }
      file += namelist[n]->d_name;

      if(access(file.c_str(), F_OK) == -1) {  
	sprintf(msg, "Unable to access: %s", file.c_str());
	FB::error(msg);	
      }

      struct stat buf;
      if(stat(file.c_str(), &buf) == 0) {
	if(S_ISREG(buf.st_mode) || S_ISLNK(buf.st_mode)) {
	  files.push_back(new string(file));
	  if(buf.st_size <= minSize) 
	    bad.push_back(new string(file));
	}
      }

      free(namelist[n]);
    }
    free(namelist);
  }


  /* sort found files into file sequences and single files */ 
  typedef std::set<FileSeq*, ltFileSeq>  FileSeqSet;
  FileSeqSet fseqs;
  {
    FileList::iterator iter;
    for(iter=files.begin(); iter != files.end(); iter++) {
      string* str = (*iter);

      /* check if this is a bad frame */ 
      bool badframe = false;
      {
	FileList::iterator biter;
	for(biter=bad.begin(); biter != bad.end(); biter++) {
	  if((*(*biter)) == (*(*iter))) {
	    badframe = true;
	    break;
	  }
	}	
      }

      /* chop filename up into prefix, padding, suffix and frame number components */ 
      int wk;
      string::size_type size = str->size();
      for(wk=size-1; wk>=0; wk--) {
	if(isdigit((int) ((*str)[wk]))) 
	  break;
      }
      
      if(wk == -1) {
	FileSeq* fseq = new FileSeq(*str);
	fseqs.insert(fseq);
      }
      else {
	string suffix;
	if(wk < (size-1)) 
	  suffix = str->substr(wk+1, size - (wk+1));
	
	int frameEnd = wk;
	for(; wk>=0; wk--) {
	  if(!isdigit((int) ((*str)[wk]))) 
	    break;
	}

	int padding = frameEnd - wk;
	assert(padding > 0);

	string fr = str->substr(wk+1, padding);
	int frame = atoi(fr.c_str());
	
	string prefix;
	if(wk > 0) 
	  prefix = str->substr(0, wk+1);

	FileSeq* fseq = new FileSeq(prefix, padding, suffix, frame);
	{
	  FileSeqSet::iterator fiter = fseqs.find(fseq);
	  if(fiter == fseqs.end()) {
	    fseqs.insert(fseq);
	    if(badframe) 
	      fseq->addBadFrame(frame);
	  }
	  else {
	    (*fiter)->addFrame(frame);
	    if(badframe) 
	      (*fiter)->addBadFrame(frame);
	  }
	}

	delete str;
      }
    }
  }
  
  { 
    FileSeqSet::iterator iter;
    for(iter=fseqs.begin(); iter != fseqs.end(); iter++) {
      FileSeq* fseq = (*iter);

      string str;
      if(fcheck) {
	fseq->toFcheckCommand(str);
	if(exec) {
	  std::cout << "EXEC: " << str << "\n";
	  system(str.c_str());
	}
	else {
	  std::cout << str << "\n";
	}
      }
      else {
	fseq->toString(str);
	std::cout << str << "\n";
      }
    }
  }

  return EXIT_SUCCESS;
}
