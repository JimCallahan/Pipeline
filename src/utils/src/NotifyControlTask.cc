// $Id: NotifyControlTask.cc,v 1.1 2004/04/09 17:55:12 jim Exp $

#include <NotifyControlTask.hh>
#include <NotifyMgr.hh>

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

/*----------------------------------------------------------------------------------------*/
/*   T A S K                                                                              */
/*----------------------------------------------------------------------------------------*/

/**
 * Run the task.
 */ 
int
NotifyControlTask::run()
{
  char msg[1024];
  char data[1033];
  data[1033] = '\0';
  while(!pMgr.isShutdown()) {
    int num = Network::read(pSocket, data, 1032);
    if((num == -1) || (num < 1032)) {
      FB::warn("Illegible message recieved!");
      break;
    }
    
    if(strncmp(data, "ADD_____", 8) == 0) {
      sprintf(msg, "Add Directory: %s", data+8);
      FB::threadMsg(msg, 4, pName, pPID);

      pMgr.addDir(data+8); 
    }
    else if(strncmp(data, "REMOVE__", 8) == 0) {
      sprintf(msg, "Remove Directory: %s", data+8);
      FB::threadMsg(msg, 4, pName, pPID);
      
      pMgr.removeDir(data+8); 
    }
    else if(strncmp(data, "CLOSE___", 8) == 0) {
      FB::threadMsg("Close", 4, pName, pPID);
      break;
    }
    else if(strncmp(data, "SHUTDOWN", 8) == 0) {
      FB::threadMsg("Shutdown", 4, pName, pPID);
      
      pMgr.shutdown();
    }
    else {
      FB::warn("Illegal message recieved!");
      break;
    }
  }

  FB::threadMsg("Connection Closed.", 3, pName, pPID);
  
  pIsFinished = true;
  return EXIT_SUCCESS;
}

} // namespace Pipeline
