// $Id: GlueError.java,v 1.1 2004/02/12 15:50:12 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   G L U E   E R R O R                                                                    */
/*                                                                                          */
/*     Error events related to encoding/decoding GLUE format text.                          */
/*------------------------------------------------------------------------------------------*/

public
class GlueError
  extends Exception 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  public
  GlueError()
  { 
    super(); 
  }

  public
  GlueError
  (
   String s
  ) 
  { 
    super(s); 
  }

  public
  GlueError
  (
   Exception ex
  ) 
  { 
    super(ex.getMessage(), ex);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2514382753062563917L;

}
  
