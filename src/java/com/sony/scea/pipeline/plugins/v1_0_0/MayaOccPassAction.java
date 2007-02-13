package com.sony.scea.pipeline.plugins.v1_0_0;

import java.io.*;
import java.util.ArrayList;

import us.temerity.pipeline.*;

/**
 * Creates a mel script that can be run on a scene to have it generate an ambient
 * occlusion pass.
 * <p>
 * This script depends on the same group of sets that the {@link BuildRenderTreeTool} 
 * does.  So it should only be used on projects using the same setup.
 * <p>
 * Consult the mental ray documentation on its ambient occlusion shader for an
 * explanation of what the parameters control.
 * <DIV style="margin-left: 40px;">
 *   RenderType<BR>
 *   <DIV style="margin-left: 40px;">
 *    Is this a character or environment occlusion pass.
 *   </DIV> <BR>
 *   Samples<BR>
 *   <DIV style="margin-left: 40px;">
 *    The samples setting on the mental ray ambient occlusion shader.
 *   </DIV> <BR>
 *   Spread<BR>
 *   <DIV style="margin-left: 40px;">
 *    The spread setting on the mental ray ambient occlusion shader.
 *   </DIV> <BR>
 *   MaxDistance<BR>
 *   <DIV style="margin-left: 40px;">
 *    The max distance setting on the mental ray ambient occlusion shader.
 *   </DIV> <BR>
 * </DIV> <P> 
 * @author Ifedayo O. Ojomo
 */
public class MayaOccPassAction extends BaseAction {

	/*----------------------------------------------------------------------------------------*/
	/*   S T A T I C   I N T E R N A L S                                                      */
	/*----------------------------------------------------------------------------------------*/
	private static final long serialVersionUID = 6714621639097140455L;
	
	/*----------------------------------------------------------------------------------------*/
	/*   C O N S T R U C T O R                                                                */
	/*----------------------------------------------------------------------------------------*/
	public MayaOccPassAction() {
		super("MayaOccPass", new VersionID("1.0.0"), "SCEA",
			"Creates a mel script for the ambient occlusion render pass.");
		
		{
			ArrayList<String> values = new ArrayList<String>();
			values.add("Env Occ");
			values.add("Ch Occ");
			ActionParam param = new EnumActionParam(
					"RenderType","", "Env Occ", values);
			addSingleParam(param);
		}
		{
			ActionParam param = new IntegerActionParam
			("Samples","", 128);
			addSingleParam(param);
		}
		{
			ActionParam param = new DoubleActionParam
			("Spread","", 0.7);
			addSingleParam(param);
		}
		{
			ActionParam param = new DoubleActionParam
			("MaxDistance","", 4000.0);
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
	 * @return nearDist
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
			("The MayaOccPass Action requires that primary target file sequence must " + 
			"be a single MEL script!"); 

		/* create a temporary shell script */ 
		File script = createTemp(agenda, 0644, "bash");
		File melScript = createTemp(agenda, 0644, "mel");
		try {      
		    PrintWriter bashOut = new PrintWriter(new BufferedWriter(new FileWriter(script)));
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(melScript)));
			Path target = new Path(PackageInfo.sProdPath, 
					nodeID.getWorkingParent() + "/" + fseq.getFile(0));


			EnumActionParam temp = (EnumActionParam)getSingleParam("RenderType");
			Integer renderType = temp.getIndex();
			if(renderType == null) 
				throw new PipelineException
				("The value of Max Distance (" + renderType + ") was illegal!");	      

			Integer samples = (Integer)getSingleParamValue("Samples"); 
			if((samples == null) || (samples < 0)) 
				throw new PipelineException
				("The value of Samples (" + samples + ") was illegal!");	  
			
			Double spread = (Double)getSingleParamValue("Spread"); 
			if((spread == null) || (spread < 0)) 
				throw new PipelineException
				("The value of Spread (" + spread + ") was illegal!");	  
			
			Double maxDist = (Double)getSingleParamValue("MaxDistance"); 
			if((maxDist == null) || (maxDist < 0)) 
				throw new PipelineException
				("The value of Max Distance (" + maxDist + ") was illegal!");	      

			bashOut.println("mv "+ melScript.getPath() + " " + target.toOsString());
			
		
			out.println("proc id_buildOCCPass(){\n" +
//				"	string $tempString = `file -q -sceneName`;\n" +
//				"	string $id_RPRootDir = dirname($tempString) + \"//\";\n" +
//				"	string $id_RPRoot = basenameEx($tempString);\n" +
				"	delete `ls -type light`;\n\n" +
					
				"	int $mode = "+ renderType+";\n"); 
			
			out.println("	/*-build and apply amb occ shader-*/\n" +
				"	string $surfShdr = `shadingNode -asShader surfaceShader`;\n" +
				"	string $surfShdrGrp = `sets -renderable true -noSurfaceShader true" +
				" -empty -name ($surfShdr+\"SG\")`;\n" +
				"	connectAttr -f ($surfShdr+\".outColor\") ($surfShdrGrp+\".surfaceShader\");\n\n" +
			
				"	string $shadeGrps[] = `ls -type shadingEngine`;\n" +
				"	string $shdGrp;");
			
			out.println("	for($shdGrp in $shadeGrps){\n" +
				"		string $mrShdr  = `connectionInfo -sfd ($shdGrp+\".miMaterialShader\")`;\n\n" +
		
				"		if($mrShdr!=\"\"){\n" +
				"			disconnectAttr $mrShdr ($shdGrp+\".miMaterialShader\");\n" +
				"		}");
			
			out.println("		string $dispShdr = `connectionInfo -sfd ($shdGrp+\".displacementShader\")`;\n" +
				"		if($dispShdr==\"\"){\n" +
				"			select -r $shdGrp;\n" +
				"			sets -e -forceElement $surfShdrGrp;\n" +
				"		} else {\n" +
				"			connectAttr -f ($surfShdr+\".outColor\") ($shdGrp+\".surfaceShader\");\n" +
				"		}\n" +"	}");
			
			out.println("	string $mrTex;");
			out.println("	print(\"Processing Ambient Occlusion Pass\\n\");");
			out.println("	$mrTex = `mrCreateCustomNode -asTexture \"\" mib_amb_occlusion`;");
			out.println("	setAttr ($mrTex+\".samples\") " + samples + ";");
			out.println("	setAttr ($mrTex+\".spread\") " + spread + ";");
			out.println("	setAttr ($mrTex+\".max_distance\") " + maxDist + ";");
			out.println("	connectAttr ($mrTex+\".outValue\") ($surfShdr+\".outColor\");");
			out.println("	//connectAttr ($surfShdr+\".outColor\") ($surfShdrGrp+\".surfaceShader\");");

			
			out.println("	if($mode==0){");
			out.println("		/*--ENV OCC--*/");
		
			out.println("		/*-prim vis off for characters-*/");
			out.println("		select `ls -sets \"CH*\"`;");
			out.println("		$temp = `ls -selection`;");
			out.println("		$temp = `listRelatives -f -ad $temp`;");
			
			out.println("		string $obj;");
			out.println("		for($obj in $temp){");
			out.println("			if (`attributeExists \"primaryVisibility\" $obj`) {");
			out.println("				setAttr ($obj+\".primaryVisibility\") 0;");
			out.println("			}");
			//out.println("			$attr = ($obj+\".primaryVisibility\");");
			//out.println("			catchQuiet(`setAttr $attr 0`);");
			out.println("		}");
			out.println("	} else {");
			out.println("		/*--CH OCC---*/");
			
			out.println("		/*---GET EYES AND MOUTH----*/");
			out.println("		select `ls -r true \"*r_EYES\" \"*r_TEETH\"`;");
			out.println("		string $selected[] = `ls -selection`;");
			out.println("		if(size($selected)>0){");
			out.println("			string $eyeSurfShdrGrp = \"\";");
			out.println("			string $eyeSurfShdr = `shadingNode -asShader surfaceShader -name WHITE_OCC`;");
			out.println("			$eyeSurfShdrGrp = `sets -renderable true -noSurfaceShader true ");
			out.println("				-empty -name ($eyeSurfShdr+\"SG\")`;");
			out.println("			connectAttr -f ($eyeSurfShdr+\".outColor\") ($eyeSurfShdrGrp+\".surfaceShader\");");
			out.println("			setAttr ($eyeSurfShdr+\".outColor\") -type double3 1 1 1;");
			out.println("			select $selected;");
			out.println("			sets -e -forceElement $eyeSurfShdrGrp;");
			out.println("		}\n");
			
			out.println("		/*--Hide nonessential geometry----*/");
			out.println("		select `ls -sets \"CH*\"`;");
			out.println("		select -add BLACKHOLE PRIMVIS;\n");
			
			out.println("		string $temp[] = `listUnselected`;");
			out.println("		select `listRelatives -f -ad $temp`;");
			out.println("		createDisplayLayer -name \"r_HIDE\" -number 1 -empty;");
			out.println("		editDisplayLayerMembers r_HIDE `ls -selection`;");
			//out.println("		layerEditorLayerButtonVisibilityChange r_HIDE;\n");
			out.println("		int $visible = false;");		
			out.println("		setAttr \"r_HIDE.visibility\" $visible;");
			
			out.println("		/*--create black hole shader and apply--*/");
			out.println("		select BLACKHOLE;");
			out.println("		$temp = `ls -selection`;");
			out.println("		if(size($temp)>0){");
			out.println("			string $holeSurfShdr = `shadingNode -asShader surfaceShader -name HOLE_MATTE`;");
			out.println("			$holeSurfShdrGrp = `sets -renderable true -noSurfaceShader true	-empty -name ($holeSurfShdr+\"SG\")`;");
			out.println("			connectAttr -f ($holeSurfShdr+\".outColor\") ($holeSurfShdrGrp+\".surfaceShader\");");
			out.println("			setAttr ($holeSurfShdr+\".outColor\") -type double3 0 0 0;");
			out.println("			setAttr ($holeSurfShdr+\".outMatteOpacity\") -type double3 0 0 0 ;");
			out.println("			select $temp;");
			out.println("			sets -e -forceElement $holeSurfShdrGrp;");
			out.println("		}");
			
			out.println("		/*--Primvis off geometry----*/");
			out.println("		select PRIMVIS;");
			out.println("		$temp = `ls -selection`;");
			out.println("		$temp = `listRelatives -f -ad $temp`;\n");
			
			out.println("		string $obj;");
			out.println("		for($obj in $temp){");
			out.println("			if (`attributeExists \"primaryVisibility\" $obj`) {");
			out.println("				setAttr ($obj+\".primaryVisibility\") 0;");
			out.println("			}");
			//out.println("			$attr = ($obj+\".primaryVisibility\");");
			//out.println("			catchQuiet(`setAttr $attr 0`);");
			out.println("		}");
			out.println("	}\n\n");
			
//			out.println("	/*---SET UP OUTPUT FILE-----*/\n" +			
//				"	setAttr defaultRenderGlobals.imageFilePrefix " +
//					"-type \"string\" ($id_RPRoot + \"_OCC\");\n" +
//				"	setAttr mentalrayGlobals.imageFilePrefix " +
//					"-type \"string\" ($id_RPRoot + \"_OCC\");\n");
			
			out.println("}");
			
			out.println("id_buildOCCPass();");

			out.close();
			bashOut.close();

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
