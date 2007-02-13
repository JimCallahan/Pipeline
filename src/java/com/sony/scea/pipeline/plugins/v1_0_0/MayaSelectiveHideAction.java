package com.sony.scea.pipeline.plugins.v1_0_0;

import java.io.*;
import java.util.ArrayList;

import us.temerity.pipeline.*;

/**
 * Creates a mel script that can be run on a scene to have it generate a MotionVector
 * pass.
 * 
 * <DIV style="margin-left: 40px;">
 *   Normalize<BR>
 *   <DIV style="margin-left: 40px;">
 *    The normalize value for the lm2dv shader.
 *   </DIV> <BR>
 * </DIV> <P> 
 * @author Ifedayo O. Ojomo
 */
public class MayaSelectiveHideAction extends BaseAction{

	/*----------------------------------------------------------------------------------------*/
	/*   S T A T I C   I N T E R N A L S                                                      */
	/*----------------------------------------------------------------------------------------*/

	private static final long serialVersionUID = -4802016946302850251L;

	/*----------------------------------------------------------------------------------------*/
	/*   C O N S T R U C T O R                                                                */
	/*----------------------------------------------------------------------------------------*/
	public MayaSelectiveHideAction() {
		super("MayaSelectiveHide", new VersionID("1.0.0"), "SCEA",
			"Creates a mel script for the motion vector render pass.");
		{
			ActionParam param = new StringActionParam
			("SetName","","");
			addSingleParam(param);
		}
		//underDevelopment();
	}//end constructor

	/**
	 * Construct a {@link SubProcessHeavy SubProcessHeavy} instance which when executed will 
	 * fulfill the given action agenda. <P> 
	 * 
	 * @param agenda
	 *   The agenda to be accomplished by the action.
	 * 
	 * @param outFile 
	 *   The file to which all STDOUT output is redirected.
	 * 
	 * @param errFile 
	 *   The file to which all STDERR output is redirected.
	 * 
	 * @return 
	 *   The SubProcess which will fulfill the agenda.
	 * 
	 * @throws PipelineException 
	 *   If unable to prepare a SubProcess due to illegal, missing or imcompatable 
	 *   information in the action agenda or a general failure of the prep method code.
	 */
	public SubProcessHeavy prep(ActionAgenda agenda, File outFile, File errFile)
	throws PipelineException
	{ 
		/* sanity checks */ 
		NodeID nodeID = agenda.getNodeID();
		FileSeq fseq = agenda.getPrimaryTarget();
		if(!fseq.isSingle() || !fseq.getFilePattern().getSuffix().equals("mel"))
			throw new PipelineException
			("The MayaSelectiveHide Action requires that primary target file sequence must " + 
			"be a single MEL script!");

		/* create a temporary shell script */ 
		File script = createTemp(agenda, 0644, "bash");
		File melScript = createTemp(agenda, 0644, "mel");
		try {      
		    PrintWriter bashOut = new PrintWriter(new BufferedWriter(new FileWriter(script)));
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(melScript)));


			Path target = new Path(PackageInfo.sProdPath, 
					nodeID.getWorkingParent() + "/" + fseq.getFile(0));

			String SetNames = (String)getSingleParamValue("SetName"); 
			if(SetNames == null)
				throw new PipelineException
				("The set name is illegal!");	      
			
			bashOut.println("mv "+ melScript.getPath() + " " + target.toOsString());
			bashOut.close();
			
			out.println("global proc selectiveHide(string $setName)");
			out.println("{");
			out.println("	string $stuff[] = `ls -dag -lf`;");
			out.println("	string $item;");

			out.println("	for ($item in $stuff)");
			out.println("	{");
			out.println("		string $temp = firstParentOf($item);");
				
			out.println("		if (`objExists $temp`)");
			out.println("		{");
			out.println("			if (!`sets -im $setName $temp`)");
			out.println("			{");
			out.println("				setAttr ($temp + \".visibility\") 0;");
			out.println("			}");
			out.println("			else");
			out.println("			{");
			out.println("				setAttr ($temp + \".visibility\") 1;");
			out.println("			}");
			out.println("		}");
			out.println("	}");
			out.println("}");

			out.println("selectiveHide(\"" + SetNames + "\");");
			
			out.close();
	      
		} catch(IOException ex) {
			throw new PipelineException
			("Unable to write the target MEL script file (" + script + ") for Job " + 
				"(" + agenda.getJobID() + ")!\n" +
				ex.getMessage());
		}//end catch

		/* create the process to run the action */ 
		try {
			ArrayList<String> args = new ArrayList<String>();
			args.add(script.getPath());

			return new SubProcessHeavy
			(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
				"bash", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
				outFile, errFile);
		} catch(Exception ex) {
			throw new PipelineException
			("Unable to generate the SubProcess to perform this Action!\n" +
				ex.getMessage());
		}//end catch
	  }//end prep

}//end class
