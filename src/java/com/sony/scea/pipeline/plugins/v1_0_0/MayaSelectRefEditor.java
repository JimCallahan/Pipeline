package com.sony.scea.pipeline.plugins.v1_0_0;

import us.temerity.pipeline.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   P R O J E C T   E D I T O R                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The 3D modeling, animation and rendering program from Alias|Wavefront.
 * <p>
 * This opens a Maya scene with all the references unloaded.
 */
public class MayaSelectRefEditor extends SingleEditor {

	public MayaSelectRefEditor() 
	{
		super("MayaSelectRef", new VersionID("1.0.0"), "SCEA",
				"3D modeling and animation software from Alias|Wavefront. " +
				"Opens Maya and allows selective loading of references.", "maya");

		underDevelopment();
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
		Map<String, String> nenv = new TreeMap<String, String>(env);
		String midefs = env.get("PIPELINE_MI_SHADER_PATH");
		if (midefs != null) {
			Path dpath = new Path(new Path(dir), midefs);
			nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toOsString());
		}
		nenv.put("MAYA_CUSTOM_PROJECT_PATH", fseq.getPath(0).getParent());

		ArrayList<String> args = new ArrayList<String>();

		args.add("-command");
		if (PackageInfo.sOsType == OsType.Windows) {
			args.add("file -open -lnr \\\"" + fseq.getPath(0).toString() + "\\\"");
		} 
		else {
			args.add("file -open -lnr \"" + fseq.getPath(0).toString() + "\"");
		}

		SubProcessLight proc = new SubProcessLight(getName(), getProgram(), args, nenv, dir);
		proc.start();
		return proc;
	}



	/*----------------------------------------------------------------------------------------*/
	/*   S T A T I C   I N T E R N A L S                                                      */
	/*----------------------------------------------------------------------------------------*/
	private static final long serialVersionUID = 5430935099001524781L;
}
