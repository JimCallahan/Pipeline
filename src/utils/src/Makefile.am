
# instbindir  = @ROOT_INST_DIR@/bin

# instbin_PROGRAMS = \
#   pls \
#   realpath

# pls_SOURCES = LockSet.cc FB.cc PackageInfo.cc Pls.cc

# realpath_SOURCES = LockSet.cc FB.cc PackageInfo.cc RealPath.cc 



# instsbindir  = @ROOT_INST_DIR@/sbin

# instsbin_PROGRAMS = \
#   plrun \
#   plnotify

# plrun_SOURCES = LockSet.cc FB.cc PackageInfo.cc PlRun.cc 

# plnotify_SOURCES = \
#   LockSet.cc FB.cc PackageInfo.cc NotifyMgr.cc NotifyTask.cc NotifyControlTask.cc PlNotify.cc 
# plnotify_LDADD = $(PTHREAD_LIB)



noinst_PROGRAMS = \
  test-locks
#   test-plnotify-control \
#   test-plnotify-shutdown \
#   test-plnotify-monitor

# test_plnotify_control_SOURCES = LockSet.cc FB.cc PackageInfo.cc TestPlNotifyControl.cc

# test_plnotify_shutdown_SOURCES = LockSet.cc FB.cc PackageInfo.cc TestPlNotifyShutdown.cc

# test_plnotify_monitor_SOURCES = LockSet.cc FB.cc PackageInfo.cc TestPlNotifyMonitor.cc

test_locks_SOURCES = LockSet.cc FB.cc PackageInfo.cc TestLocks.cc



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

