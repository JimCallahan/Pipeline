// $Id: MayaEditor.java,v 1.1 2006/06/21 05:25:10 jim Exp $

package us.temerity.pipeline.plugin.v2_0_10;

import us.temerity.pipeline.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   E D I T O R                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The 3D modeling, animation and rendering program from Alias|Wavefront.
 */
public 
class MayaEditor 
  extends SingleEditor {

  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  public 
  MayaEditor() 
  {
    super("Maya", new VersionID("2.0.10"), "Temerity",
	  "3D modeling and animation software from Alias|Wavefront.",
	  "maya");
	
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Launch the editor program (obtained with {@link #getName getName}) under the given 
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
   *   The controlling <CODE>SubProcess</CODE> instance. 
   * 
   * @throws PipelineException
   *   If unable to launch the editor.
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
    Map<String, String> nenv = env; 
    String midefs = env.get("PIPELINE_MI_SHADER_PATH");
    if(midefs != null) {
      nenv = new TreeMap<String, String>(env);
      Path dpath = new Path(new Path(dir), midefs);
      nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toOsString());
    }

    ArrayList<String> args = new ArrayList<String>();
    args.add(fseq.getPath(0).toOsString());

    SubProcessLight proc = new SubProcessLight(getName(), getProgram(), args, nenv, dir);
    proc.start();
    return proc;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -7946703346554188617L;

}
