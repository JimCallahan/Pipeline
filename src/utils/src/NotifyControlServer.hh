// $Id: NotifyControlServer.hh,v 1.3 2004/04/09 17:55:12 jim Exp $

#ifndef PIPELINE_NOTIFY_CONTROL_SERVER_HH
#define PIPELINE_NOTIFY_CONTROL_SERVER_HH

#include <NotifyServerGn.hh>
#include <NotifyControlTask.hh>

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y   C O N T R O L   S E R V E R                                              */
/*                                                                                          */
/*    Listens for notify control network connections and spawns a NotifyControlTask task    */
/*    for each incoming connection.                                                         */
/*------------------------------------------------------------------------------------------*/

class NotifyControlServer : public NotifyServerGn<NotifyControlTask>
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
  NotifyControlServer
  (
   NotifyMgr& mgr, 
   int port
  ) :
    NotifyServerGn<NotifyControlTask>("NotifyControlServer", "NotifyControlTask", mgr, port)
  {}

};


} // namespace Pipeline

#endif
