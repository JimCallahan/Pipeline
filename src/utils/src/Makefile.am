
instbindir  = @ROOT_INST_DIR@/bin

instbin_PROGRAMS = \
  pls \
  realpath

pls_SOURCES = FB.cc PackageInfo.cc Pls.cc

realpath_SOURCES = FB.cc PackageInfo.cc RealPath.cc 



instsbindir  = @ROOT_INST_DIR@/sbin

instsbin_PROGRAMS = \
  plrun \
  plnotify

plrun_SOURCES = FB.cc PackageInfo.cc PlRun.cc 

plnotify_SOURCES = FB.cc PackageInfo.cc NotifyTask.cc NotifyMgr.cc PlNotify.cc 
plnotify_LDADD = $(PTHREAD_LIB)



noinst_PROGRAMS = \
  test-plnotify \
  test-locks

test_plnotify_SOURCES = FB.cc PackageInfo.cc TestPlNotify.cc

test_locks_SOURCES = FB.cc PackageInfo.cc TestLocks.cc



EXTRA_DIST = \
  AtomicTypes.hh \
  FB.hh \
  FileSeq.hh \
  HtmlHelp.hh \
  Lock.hh \
  NotifyMgr.hh \
  NotifyTask.hh \
  PackageInfo.hh \
  Task.hh \
  Timer.hh


INCLUDES = 

