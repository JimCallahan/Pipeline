// $Id: NotifyMonitorServer.hh,v 1.1 2004/04/09 17:55:12 jim Exp $

#ifndef PIPELINE_NOTIFY_MONITOR_SERVER_HH
#define PIPELINE_NOTIFY_MONITOR_SERVER_HH

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#include <NotifyServerGn.hh>
#include <NotifyMonitorTask.hh>
#include <NotifyMgr.hh>

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y   C O N T R O L   S E R V E R                                              */
/*                                                                                          */
/*    Listens for notify monitor network connections and spawns a NotifyMonitorTask task    */
/*    for each incoming connection.                                                         */
/*------------------------------------------------------------------------------------------*/

class NotifyMonitorServer : public NotifyServerGn< NotifyMonitorTask<64> >
{
public:
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new task.
   * 
   * param mgr
   *   The notify task manager.
   * 
   * param port
   *   The network port number.
   */ 
  NotifyMonitorServer
  (
   NotifyMgr& mgr, 
   int port
  ) :
    NotifyServerGn<NotifyMonitorTask <64> >
      ("NotifyMonitorServer", "NotifyMonitorTask", mgr, port)
  {}

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Add the given directory to the list of recently modified directories.
   */ 
  void 
  modified
  (
   const char* dir
  )
  {
    pLockSet.lock(pLockID);
    {
      TaskList::iterator iter;
      for(iter = pTasks.begin(); iter != pTasks.end(); iter++) 
	(*iter)->modified(dir);
    }
    pLockSet.unlock(pLockID);
  }

};

} // namespace Pipeline

#endif
