// $Id: KonquerorEditor.java,v 1.1 2008/06/19 02:57:32 jim Exp $

package com.radarfilm.pipeline.plugin.KonquerorEditor.v2_3_5;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T E R M I N A L   E D I T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
	Opens the Konqueror file manager
 */
public
class KonquerorEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  



public
  KonquerorEditor()
  {  
    super("Konqueror", new VersionID("2.3.5"), "Radar",
	  "An Konqueror file manager opens in the nodes working directory " + 
	  "toolset environment.", 
	  "Konqueror");
    
    underDevelopment();
    //addSupport(OsType.MacOS);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Gets the name of the editor executable.
   */
  public String
  getProgram() 
  {
	  return "konqueror";  
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a {@link SubProcessLight} instance which when executed will launch an editor
   * program to view the given file sequence as arguments. <P> 
   * 
   * @param author
   *   The name of the user owning the files. 
   * 
   * @param fseq  
   *   The file sequence to edit.
   * 
   * @param env  
   *   The environment under which the editor is run.  
   * 
   * @param dir  
   *   The working directory where the editor is run.
   *
   * @return 
   *   The controlling <CODE>SubProcessLight</CODE> instance. 
   * 
   * @throws PipelineException
   *   If unable to launch the editor.
   * 
   * @see SubProcessLight
   */  
  public SubProcessLight
  prep
  (
   String author, 
   FileSeq fseq,      
   Map<String, String> env,      
   File dir        
  ) 
    throws PipelineException
  {

	  ArrayList<String> args = new ArrayList<String>();
		
	  for(File file : fseq.getFiles()) 
	args.add(file.getParent());
	  
	  return new SubProcessLight(author, getName(), getProgram(), args, env, dir);
    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/


	private static final long serialVersionUID = -7745541770978084036L;

}


