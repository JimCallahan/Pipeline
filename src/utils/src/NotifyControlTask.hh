// $Id: NotifyControlTask.hh,v 1.3 2004/04/09 17:55:12 jim Exp $

#ifndef PIPELINE_NOTIFY_CONTROL_TASK_HH
#define PIPELINE_NOTIFY_CONTROL_TASK_HH

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#include <NotifyConnectTask.hh>

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y   C O N T R O L   T A S K                                                  */
/*                                                                                          */
/*    Manages a network connection with the Java based NotifyControlClient class.           */
/*                                                                                          */
/*    The protocol consists one-way messages send from the NotifyControlClient to this      */
/*    class.  Each message is exactly 1032 bytes long and contains the following data:      */
/*                                                                                          */
/*    Add a Directory:                                                                      */
/*      0-7     "ADD_____"                                                                  */
/*      8-1031  dir  (all unused bytes == '\0')                                             */
/*                                                                                          */
/*    Remove a Directory:                                                                   */
/*      0-7     "REMOVE__"                                                                  */
/*      1-1031  dir  (all unused bytes == '\0')                                             */
/*                                                                                          */
/*    Shutdown the Server:                                                                  */
/*      0-7     "SHUTDOWN"                                                                  */
/*      1-1031  (all bytes == '\0')                                                         */
/*                                                                                          */
/*    All directory names will be relative to the root production directory.                */
/*------------------------------------------------------------------------------------------*/

class NotifyControlTask : public NotifyConnectTask
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
   * param sd
   *   The socket descriptor.
   */ 
  NotifyControlTask
  (
   NotifyMgr& mgr, 
   int sd
  ) :
    NotifyConnectTask("NotifyControlTask", mgr, sd)
  {}



  /*----------------------------------------------------------------------------------------*/
  /*   D E S T R U C T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  ~NotifyControlTask()
  {}



  /*----------------------------------------------------------------------------------------*/
  /*   T A S K                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the task.
   */ 
  virtual int
  run();


};


} // namespace Pipeline

#endif
