//$Id: ProjectNamer.java,v 1.1 2008/06/11 12:48:23 jim Exp $

package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/*------------------------------------------------------------------------------------------*/
/*   P R O J E C T   N A M E R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Defines the names of nodes which are not connected to particular entities within the
 * project.
 */
public 
class ProjectNamer 
extends BaseNames 
{
	/*----------------------------------------------------------------------------------------*/
	/*   C O N S T R U C T O R                                                                */
	/*----------------------------------------------------------------------------------------*/

	public 
	ProjectNamer
	(
			MasterMgrClient mclient,
			QueueMgrClient qclient
	)
	throws PipelineException
	{
		super("ProjectNamer", 
				"The basic naming class for project specific files.",
				mclient,
				qclient);
		{
			UtilityParam param =
				new StringUtilityParam
				(ParamNames.aProjectName,
						"The Name of the Project the asset should live in", 
						null);
			addParam(param);
		}
	}



	/*----------------------------------------------------------------------------------------*/
	/*   A C T I O N                                                                          */
	/*----------------------------------------------------------------------------------------*/

	@Override
	public void 
	generateNames() 
	throws PipelineException
	{
		setContext((UtilContext) getParamValue(aUtilContext));

		pProject = getStringParamValue(new ParamMapping(ParamNames.aProjectName));

		Path scriptPath = StudioDefinitions.getScriptPath(pProject);
		Path templatePath = StudioDefinitions.getTemplatePath(pProject);

		pScriptPaths = new DoubleMap<Department, ScriptType, Path>();
		pTemplatePaths = new TreeMap<Department, Path>();
		for (Department department : Department.values()) {
			Path scrPath = new Path(scriptPath, department.toString() );
			for (ScriptType type : ScriptType.values()) {
				Path finalPath = new Path(scrPath, type.toString());
				pScriptPaths.put(department, type, finalPath);
			}
			Path temPath = new Path(templatePath, department.toString() );
			pTemplatePaths.put(department, temPath);
		}
	}



	/*----------------------------------------------------------------------------------------*/
	/*   A C C E S S                                                                          */
	/*----------------------------------------------------------------------------------------*/

	public String
	getProjectName()
	{
		return pProject;
	}

	/*----------------------------------------------------------------------------------------*/
	/*   G E N E R A L                                                                        */
	/*----------------------------------------------------------------------------------------*/

	/**
	 * The MEL script used to make turntable setups where the camera sweeps in a circle around
	 * a central point.
	 */
	public String
	getCircleTurntableMEL()
	{
		return new Path(pScriptPaths.get(Department.general, ScriptType.place), 
		"circleTT").toString();
	}

	/**
	 * The MEL script used to make turntable setups where the camera sits at the origin and
	 * rotates in place. 
	 */
	public String
	getCenterTurntableMEL()
	{
		return new Path(pScriptPaths.get(Department.general, ScriptType.place), 
		"centerTT").toString();
	}

	/**
	 * The MEL script that removes all references from a Maya scene file.
	 */
	public String
	getRemoveReferenceMEL()
	{
		return new Path(pScriptPaths.get(Department.general, ScriptType.mel), 
		"removeRefs").toString();
	}

	/**
	 * The MEL script used to verify the correctness of a simple asset file.
	 */
	public String
	getAssetVerificationMEL()
	{
		return new Path(pScriptPaths.get(Department.general, ScriptType.mel), 
		"assetVerify").toString();  
	}

	public String 
	getPlaceholderCameraScriptName()
	{
		return new Path(pScriptPaths.get(Department.general, ScriptType.place), 
		"camPlaceholder").toString();  
	}

	public String 
	getPlaceholderSkelScriptName()
	{
		return new Path(pScriptPaths.get(Department.general, ScriptType.place), "placeHolderSkel").toString();
	}

	/*----------------------------------------------------------------------------------------*/
	/*   M O D E L                                                                            */
	/*----------------------------------------------------------------------------------------*/

	/**
	 * The MEL script used to verify the correctness of a model file.
	 */
	public String
	getModelVerificationMEL()
	{
		return new Path(pScriptPaths.get(Department.model, ScriptType.mel), 
		"modelVerify").toString();  
	}

	/**
	 * The MEL script used to generate a placeholder model.
	 */
	public String
	getModelPlaceholderMEL()
	{
		return new Path(pScriptPaths.get(Department.model, ScriptType.place), 
		"modelPlaceholder").toString();  
	}

	/**
	 * The MEL script render globals used while rendering a turntable.
	 */
	public String
	getModelGlobalsMEL()
	{
		return new Path(pScriptPaths.get(Department.model, ScriptType.render), 
				Department.model + "Globals").toString();  
	}

	/**
	 * The Maya scene used to generate the model turntable.
	 */
	public String
	getModelTTSetup()
	{
		return new Path(pTemplatePaths.get(Department.model), "modelTT").toString();  
	}

	/*----------------------------------------------------------------------------------------*/
	/*   R I G                                                                                */
	/*----------------------------------------------------------------------------------------*/

	/**
	 * The MEL script run on the finished rig before it is sent to animation.
	 */
	public String
	getRigFinalizeMEL
	(
			AssetType type  
	)
	{
		return new Path(pScriptPaths.get(Department.rig, ScriptType.mel), 
				"finalizeRig_" + type.toString()).toString();
	}

	/**
	 * The MEL script used to verify the correctness of a rig file.
	 */
	public String
	getRigVerificationMEL()
	{
		return new Path(pScriptPaths.get(Department.rig, ScriptType.mel), 
		"rigVerify").toString();  
	}



	/*----------------------------------------------------------------------------------------*/
	/*   S H A D E                                                                            */
	/*----------------------------------------------------------------------------------------*/

	/**
	 * The MEL script run on the shaded character before it is sent to lighting.
	 */
	public String
	getShadeFinalizeMEL
	(
			AssetType type  
	)
	{
		return new Path(pScriptPaths.get(Department.shade, ScriptType.mel), 
				"finalizeShade_" + type.toString()).toString();
	}

	/**
	 * The MEL script run on the shading scene before shaders are exported.
	 */
	public String
	getShadeVerificationMEL()
	{
		return new Path(pScriptPaths.get(Department.shade, ScriptType.mel), 
		"shadeVerify").toString();  
	}

	/**
	 * The MEL script run on the shading scene before shaders are exported.
	 */
	public String
	getShaderCopyMEL()
	{
		return new Path(pScriptPaths.get(Department.shade, ScriptType.mel), 
		"shaderCopy").toString();  
	}

	public String
	getShadeGlobalsMEL()
	{
		return new Path(pScriptPaths.get(Department.shade, ScriptType.render), 
				Department.shade + "Globals").toString();  
	}

	//SHOT STUFF
	public String 
	getAnimGlobals()
	{
		return new Path(pScriptPaths.get(Department.anim, ScriptType.render), 
				Department.anim + "Globals").toString();  
	}

	public String 
	getLgtGlobals()
	{
		return new Path(pScriptPaths.get(Department.lgt, ScriptType.render), 
				Department.lgt + "MRayGlobals").toString();  
	}

	public String 
	getMayaLgtGlobals()
	{
		return new Path(pScriptPaths.get(Department.lgt, ScriptType.render), 
				Department.lgt + "MayaGlobals").toString();
	}

	/*----------------------------------------------------------------------------------------*/
	/*   S T A T I C   I N T E R N A L S                                                      */
	/*----------------------------------------------------------------------------------------*/

	/**
	 * new
	 */
	private static final long serialVersionUID = -3877941377789664957L;



	/*----------------------------------------------------------------------------------------*/
	/*   I N T E R N A L S                                                                    */
	/*----------------------------------------------------------------------------------------*/

	private String pProject;

	private DoubleMap<Department, ScriptType, Path> pScriptPaths;

	private TreeMap<Department, Path> pTemplatePaths;
}
