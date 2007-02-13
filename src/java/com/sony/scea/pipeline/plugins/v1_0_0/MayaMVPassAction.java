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
public class MayaMVPassAction extends BaseAction{

	/*----------------------------------------------------------------------------------------*/
	/*   S T A T I C   I N T E R N A L S                                                      */
	/*----------------------------------------------------------------------------------------*/
	private static final long serialVersionUID = -4805377349622855505L;
	
	/*----------------------------------------------------------------------------------------*/
	/*   C O N S T R U C T O R                                                                */
	/*----------------------------------------------------------------------------------------*/
	public MayaMVPassAction() {
		super("MayaMVPass", new VersionID("1.0.0"), "SCEA",
			"Creates a mel script for the motion vector render pass.");
		{
			ActionParam param = new IntegerActionParam
			("Normalize","", 64);
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
			("The MayaMVPass Action requires that primary target file sequence must " + 
			"be a single MEL script!"); 

		/* create a temporary shell script */ 
		File script = createTemp(agenda, 0644, "bash");
		File melScript = createTemp(agenda, 0644, "mel");
		try {      
		    PrintWriter bashOut = new PrintWriter(new BufferedWriter(new FileWriter(script)));
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(melScript)));


			Path target = new Path(PackageInfo.sProdPath, 
					nodeID.getWorkingParent() + "/" + fseq.getFile(0));

			Integer normValue = (Integer)getSingleParamValue("Normalize"); 
			if((normValue == null) || (normValue <= 0)) 
				throw new PipelineException
				("The value of Normalize (" + normValue + ") was illegal!");	      
			
			bashOut.println("mv "+ melScript.getPath() + " " + target.toOsString());
			bashOut.close();
			
			out.println("proc id_buildMVPass(){");
//			out.println("	string $tempString = `file -q -sceneName`;");
//			out.println("	string $id_RPRootDir = dirname($tempString) + \"//\";");
//			out.println("	string $id_RPRoot = basenameEx($tempString);\n");
			
			out.println("	string $eyeSurfShdrGrp = \"\";");
			out.println("	string $surfShdr = `shadingNode -asShader surfaceShader`;");
			out.println("	string $surfShdrGrp = `sets -renderable true -noSurfaceShader true");
			out.println("		-empty -name ($surfShdr+\"SG\")`;");
			out.println("	connectAttr -f ($surfShdr+\".outColor\") " +
				"($surfShdrGrp+\".surfaceShader\");\n");
			
			out.println("	string $shadeGrps[] = `ls -type shadingEngine`;");
			out.println("	string $shdGrp;");
			out.println("	for($shdGrp in $shadeGrps){");
			out.println("		string $mrShdr  = " +
				"`connectionInfo -sfd ($shdGrp+\".miMaterialShader\")`;\n");
			
			out.println("		if($mrShdr!=\"\"){");
			out.println("			disconnectAttr $mrShdr ($shdGrp+\".miMaterialShader\");");
			out.println("		}\n");
			
			out.println("		string $dispShdr = " +
				"`connectionInfo -sfd ($shdGrp+\".displacementShader\")`;");
			out.println("		if($dispShdr==\"\"){");
			out.println("			select -r $shdGrp;");
			out.println("			sets -e -forceElement $surfShdrGrp;");
			out.println("		} else {");
			out.println("			connectAttr -f ($surfShdr+\".outColor\") " +
				"($shdGrp+\".surfaceShader\");");
			out.println("		}\n");
			out.println("	}");
			out.println("	print(\"Processing Motion Vector Pass\\n\");");
			out.println("	string $mrTex = `mrCreateCustomNode -asTexture \"\" lm2DMV_v2`;");
			out.println("	setAttr ($mrTex+\".Normalize\") " + normValue + ";\n");
			
			out.println("	setAttr miDefaultOptions.motionBlur 2;");
			out.println("	setAttr miDefaultOptions.motionBlurBy 0.5;");
			out.println("	setAttr miDefaultOptions.shutter 0.5;");
			out.println("	setAttr miDefaultOptions.shutterDelay 0.5;\n");
			
			out.println("	connectAttr ($mrTex+\".outValue\") ($surfShdr+\".outColor\");");
			out.println("	//connectAttr ($surfShdr+\".outColor\") " +
				"($surfShdrGrp+\".surfaceShader\");\n");
			
//			out.println("	/*---SET UP OUTPUT FILE-----*/\n");
//			out.println("	setAttr defaultRenderGlobals.imageFilePrefix " +
//				"-type \"string\" ($id_RPRoot + \"_MV\");");
//			out.println("	setAttr mentalrayGlobals.imageFilePrefix " +
//				"-type \"string\" ($id_RPRoot + \"_MV\");");
			out.println("}");
			out.println("id_buildMVPass();");
			
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
