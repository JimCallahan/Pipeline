package us.temerity.pipeline.plugin.v2_0_9;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import us.temerity.pipeline.*;

/**
 * The UltraEdit text editor for Windows.
 */
public 
class UltraEditEditor 
extends BaseEditor
{
  /*----------------------------------------------------------------------------------------*/
  /* C O N S T R U C T O R */
  /*----------------------------------------------------------------------------------------*/
  
  public UltraEditEditor()
  {
    super("UltraEdit", new VersionID("2.0.9"), "Temerity",
	  "The Ultra Edit text editor.", "uedit32.exe");
    
    removeSupport(OsType.Unix);
    addSupport(OsType.Windows);
  }


	
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Launch the editor program (obtained with {@link #getProgram getProgram}) under the given 
   * environmant with all of the files which comprise the given file sequence as 
   * arguments. The environment <CODE>env</CODE> consists of a table of environmental 
   * variable name/value pairs.  Typically, this environment is corresponds to a Toolset. <P>
   * 
   * Subclasses should override this method if more specialized behavior or different 
   * command line arguments are needed in order to launch the editor for the given file 
   * sequence.
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
  launch
  (
   FileSeq fseq,      
   Map<String, String> env,      
   File dir        
  ) 
    throws PipelineException
  {
    if(PackageInfo.sOsType == OsType.Windows) {
      ArrayList<String> args = new ArrayList<String>();
      for(File file : fseq.getFiles()) 
	args.add(file.getPath());

      /* ZBrush wants to be launched from its installed location */
      File edir = null;
      {
	String path = env.get("PATH");
	if(path == null) 
	  throw new PipelineException
	    ("Unable to determine the value of the PATH environmental variable!");
	ExecPath epath = new ExecPath(path);
	File which = epath.which(getProgram());
	if(which == null) 
	  throw new PipelineException
	    ("Unable to locate the ZBrush executable!");
	edir = which.getParentFile();
      }

      SubProcessLight proc = new SubProcessLight(getName(), getProgram(), args, env, edir);
      proc.start();
      return proc;
    }
    else {
      throw new PipelineException
	("This plugin only supports the Windows operating system!");
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4150117853252389967L;

}
