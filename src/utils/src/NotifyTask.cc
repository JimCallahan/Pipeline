// $Id: NotifyTask.cc,v 1.1 2004/04/05 06:27:55 jim Exp $

#include <NotifyTask.hh>

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y   T A S K                                                                  */
/*                                                                                          */
/*    Monitors a set of local directories for file creation, modification, removal and      */
/*    renaming events which occur within the watched directories.                           */
/*------------------------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------------------*/
/*   H E L P E R S                                                                        */
/*----------------------------------------------------------------------------------------*/
  
/**
 * Report the modified directory to the parent NodeMgr.
 */ 
void
NotifyTask::modified
(
 const char* dir
)
{
  pLock.lock();
  {
    
    // DEBUG
    printf("[%d]: Modified: %s/%s\n", 
	   pPID, pRootDir, dir);
    // DEBUG
    
    // ... push onto the queue to be sent over the network...
    
    pMgr->modified(dir);
  }
  pLock.unlock();
}


} // namespace Pipeline
