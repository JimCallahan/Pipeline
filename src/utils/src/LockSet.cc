// $Id: LockSet.cc,v 1.1 2004/04/09 17:55:12 jim Exp $

// $Id: LockSet.cc,v 1.1 2004/04/09 17:55:12 jim Exp $

#include <LockSet.hh>

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   L O C K   S E T                                                                        */
/*                                                                                          */
/*     A manager of Locks.                                                                  */
/*------------------------------------------------------------------------------------------*/

LockSet::SemSet** LockSet::sSemSets     = NULL;
LockSet::Lock*    LockSet::sMasterLock  = NULL;
LockSet::Lock*    LockSet::sCleanupLock = NULL;
int               LockSet::sCleanupPID  = -1;

} // namespace Pipeline

