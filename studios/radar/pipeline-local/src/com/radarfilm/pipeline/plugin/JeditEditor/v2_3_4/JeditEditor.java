package com.radarfilm.pipeline.plugin.JeditEditor.v2_3_4;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   JEDIT   E D I T O R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The GNU extensible, customizable, self-documenting text editor.
 */
public
class JeditEditor
  extends SimpleEditor
{  

/*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  JeditEditor()
  {
    super("Jedit", new VersionID("2.3.4"), "Radar", 
	  "The world best mel editor.", 
	  "Jedit");

    underDevelopment();
    addSupport(OsType.MacOS); 
    addSupport(OsType.Windows); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** a
   * Gets the name of the editor executable.
   */
  public String
  getProgram() 
  {
    if(PackageInfo.sOsType == OsType.Unix)  
      return "jedit";
    else if(PackageInfo.sOsType == OsType.Windows) 
      return "jedit.exe"; 
    return super.getProgram();
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
	args.add(file.getPath());
	  
	  return new SubProcessLight(author, getName(), getProgram(), args, env, dir);
    
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

	private static final long serialVersionUID = 1496479376683714862L;


}


