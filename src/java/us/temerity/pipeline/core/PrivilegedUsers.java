// $Id: PrivilegedUsers.java,v 1.1 2004/05/23 20:01:27 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   P R I V I L E G E D   U S E R S                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A Glueable set of privileged user names.
 */
public
class PrivilegedUsers
  extends TreeSet<String>
  implements Glueable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  protected 
  PrivilegedUsers() 
  {}


  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    TreeSet<String> users = new TreeSet<String>(this);
    encoder.encode("Users", users);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    clear();
    
    TreeSet<String> users = (TreeSet<String>) decoder.decode("Users");
    if(users == null) 
      throw new GlueException("The \"Users\" were missing!");

    addAll(users);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4846530913772078253L;

}
