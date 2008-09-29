// $Id: BaseUtilityApp.java,v 1.6 2008/09/29 19:02:19 jim Exp $

package us.temerity.pipeline.utils;  

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;

import java.io.*;
import java.util.*;


/*------------------------------------------------------------------------------------------*/
/*   B A S E   U T I L I T Y   A P P                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * An abstract base class for Pipeline utility applications run by the JavaUtilityAction 
 * plugin. <P> 
 */ 
public abstract 
class BaseUtilityApp
{     
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a utility application. 
   */ 
  protected 
  BaseUtilityApp()
  {}



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * Get the calling action plugin instance. <P> 
   * 
   * The {@link #init} method needs to be called before this method will return a action. 
   * 
   * @return 
   *   The calling action or <CODE>null</CODE> if not yet initialized. 
   */
  public BaseAction
  getAction() 
  {
    return pAction;
  }
  
  /** 
   * Get the action agenda of the calling action plugin. <P> 
   * 
   * The {@link #init} method needs to be called before this method will return an agenda. 
   * 
   * @return 
   *   The action agenda or <CODE>null</CODE> if not yet initialized. 
   */
  public ActionAgenda
  getActionAgenda() 
  {
    return pAgenda; 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   R U N                                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Initialize the utility application from data passed as the command line arguments
   * to the application from the {@link JavaUtilityAction} plugin. <P> 
   * 
   * This method loads a GLUE format file generated by the JavaUtilityAction plugin and 
   * extracts the action and action agenda used to run the action.  These can be retrieved
   * using the {@link #getAction} and {@link #getActionAgenda} methods of this class. 
   * 
   * @param args
   *   The command line arguments passed to the <CODE>main</CODE> method of the application.
   * 
   * @throws PipelineException
   *   If unable to parse the command line arguments or GLUE input file.
   */ 
  protected void 
  init
  (
   String[] args
  ) 
    throws PipelineException
  {
    if(args.length != 1)
      throw new PipelineException
	("A Pipeline utility application should have exactly one argument containing the " +
	 "name of the input GLUE file.  This application had (" + args.length + ") " + 
	 "arguments!\n");

    String fileName = args[0];
    try {
      TreeMap<String, Object> table = 
        (TreeMap<String, Object>) GlueDecoderImpl.decodeFile("agenda", new File(fileName));

      pAction = (BaseAction) table.get("Action");
      pAgenda = (ActionAgenda) table.get("Agenda");
    }
    catch(GlueException ex) {
      throw new PipelineException(ex);
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The calling action plugin instance. <P> 
   */
  private BaseAction  pAction; 
  
  /** 
   * The action agenda of the calling action plugin. <P> 
   */
  private ActionAgenda  pAgenda; 
  
}
