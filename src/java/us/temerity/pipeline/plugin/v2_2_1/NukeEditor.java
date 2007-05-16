// $Id: NukeEditor.java,v 1.2 2007/05/16 13:12:53 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   E D I T O R                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The Nuke compositing application from The Foundry. <P> 
 * 
 * All Read/Write nodes should have absolute file paths relative to the root working 
 * directory which start with the string "WORKING" in order to support portability of 
 * Nuke scripts between artists and operation sytems.  To enable the "WORKING" prefix to 
 * be expanded to the value of the WORKING environmental variable in these file paths, 
 * a "init.tcl" script as been provided with Pipeline in the "app-extra/nuke" directory 
 * where Pipeline is installed at your site.  You must either copy this script into the 
 * "plugin/user" directory of your Nuke installation or add Pipeline's "app-extra/nuke" 
 * directory to the NUKE_PATH defined in the Toolset using this plugin.<P> 
 * 
 * By default, this Editor launches the "Nuke4.6" binary.  This can be overridden by 
 * specifying an alternate binary with the NUKE_BINARY environmental variable in the 
 * Toolset used to run this Editor plugin. On Windows, the Nuke binary name should 
 * include the ".exe" extension.<P> 
 */
public
class NukeEditor
  extends SimpleSingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  NukeEditor()
  {
    super("Nuke", new VersionID("2.2.1"), "Temerity",
	  "The Nuke compositing application from The Foundry.", 
	  "Nuke4.6");  

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
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
    if(!fseq.isSingle())
      throw new PipelineException
        ("The " + getName() + " Editor can only edit a single Nuke script at a time!");

    ArrayList<String> args = new ArrayList<String>();
    args.add("-g"); 
    args.add(fseq.getFile(0).toString());

    String nuke = NukeActionUtils.getNukeProgram(env); 

    return new SubProcessLight(author, getName(), nuke, args, env, dir);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6681279688714061174L;

}


