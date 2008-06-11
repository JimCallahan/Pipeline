//$Id: AssetNamer.java,v 1.1 2008/06/11 12:48:23 jim Exp $

package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/*------------------------------------------------------------------------------------------*/
/*   A S S E T   N A M E R                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Define the names of nodes used to construct Asset networks in the Nathan Love Base
 * Collection.
 */
public 
class AssetNamer
extends BaseNames
{

	/*----------------------------------------------------------------------------------------*/
	/*   C O N S T R U C T O R                                                                */
	/*----------------------------------------------------------------------------------------*/

	public 
	AssetNamer
	(
			MasterMgrClient mclient,
			QueueMgrClient qclient
	)
	throws PipelineException 
	{
		super("AssetNamer", 
				"Define the names of nodes used to construct Asset networks in " +
				"the Nathan Love Base Collection",
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
		{
			UtilityParam param = 
				new StringUtilityParam
				(ParamNames.aAssetName, 
						"The Name of the asset", 
						null);
			addParam(param);
		}
		{
			UtilityParam param = 
				new EnumUtilityParam
				(ParamNames.aAssetType, 
						"The Type of the asset", 
						AssetType.character.toTitle(),
						AssetType.titles());
			addParam(param);
		}
	}

	@Override
	public void generateNames()
	throws PipelineException
	{
		setContext((UtilContext) getParamValue(aUtilContext));

		pProject = getStringParamValue(new ParamMapping(ParamNames.aProjectName));
		pAssetName =  getStringParamValue(new ParamMapping(ParamNames.aAssetName));
		pAssetType =  getStringParamValue(new ParamMapping(ParamNames.aAssetType));

		Path startPath = StudioDefinitions.getAssetPath(pProject, pAssetType, pAssetName);

		pStartPaths = new DoubleMap<Department, SubDir, Path>();
		pAssetStartPaths = new TreeMap<SubDir, Path>();

		for (Department department : Department.values()) {
			for (SubDir sub : SubDir.values()) {
				Path p = new Path(new Path(startPath, department.toString()), sub.dirName());
				pStartPaths.put(department, sub, p);
			}
		}
		for (SubDir sub : SubDir.values()) {
			Path p = new Path(startPath, sub.dirName());
			pAssetStartPaths.put(sub, p);
		}
	}



	/*----------------------------------------------------------------------------------------*/
	/*  A C C E S S                                                                           */
	/*----------------------------------------------------------------------------------------*/

	public AssetType
	getAssetType()
	{
		return AssetType.fromString(pAssetType);
	}

	public String
	getTaskName()
	{
		return pAssetName + "_" + pAssetType;
	}

	public String 
	getAssetName()
	{
		return pAssetName;
	}

	public String 
	getNameSpace()
	{
		return pAssetName;
	}

	/*----------------------------------------------------------------------------------------*/
	/*  M O D E L                                                                             */
	/*----------------------------------------------------------------------------------------*/

	/**
	 * The scene where the modeler works.
	 */
	public String
	getModelEditScene()
	{
		return new Path(pStartPaths.get(Department.model, SubDir.edit), 
				pAssetName + "_model_edit").toString();
	}

	/**
	 * The node which the artist uses to submit the modeling task.
	 */
	public String
	getModelSubmitNode()
	{
		return new Path(pStartPaths.get(Department.model, SubDir.submit), 
				pAssetName + "_model_submit").toString();
	}

	/**
	 * The node which the supervisor uses to approve the modeling task.
	 */
	public String
	getModelApproveNode()
	{
		return new Path(pStartPaths.get(Department.model, SubDir.approve), 
				pAssetName + "_model_approve").toString();
	}

	/**
	 * The scene which has the model verification MEL script run on it.
	 */
	public String
	getModelVerifyScene()
	{
		return new Path(pStartPaths.get(Department.model, SubDir.prepare), 
				pAssetName + "_model").toString();
	}

	/**
	 * The scene delivered to rigging and shading. 
	 */
	public String
	getModelProductScene()
	{
		return new Path(pStartPaths.get(Department.model, SubDir.product), 
				pAssetName + "_model").toString();
	}

//	wct
	public String
	getModelTTScene()
	{
		return new Path(pStartPaths.get(Department.model, SubDir.prepare), 
				pAssetName + "_tt").toString();
	}

	public String
	getModelTTRender()
	{
		return new Path(pStartPaths.get(Department.model, SubDir.prepare),
				"/img/" + pAssetName + "_tt_render").toString();
	}

	public String
	getModelTTMovie()
	{
		return new Path(pStartPaths.get(Department.model, SubDir.prepare), 
				pAssetName + "_tt_render").toString();
	}

	public String
	getModelTTThumbnail()
	{
		return new Path(pStartPaths.get(Department.model, SubDir.prepare), 
				pAssetName + "_tt_thumb").toString();
	}

	/*----------------------------------------------------------------------------------------*/
	/*  R I G                                                                                 */
	/*----------------------------------------------------------------------------------------*/

	/**
	 * The scene where the rigger works.
	 */
	public String
	getRigEditScene()
	{
		return new Path(pStartPaths.get(Department.rig, SubDir.edit), 
				pAssetName + "_rig_edit").toString();
	}

	/**
	 * @return the skeletonNodeName
	 */
	public String 
	getSkeletonNodeName()
	{
		return new Path(pStartPaths.get(Department.rig, SubDir.edit),
				pAssetName + "_skel").toString();
	}

	/**
	 * The node which the artist uses to submit the rigging task.
	 */
	public String
	getRigSubmitNode()
	{
		return new Path(pStartPaths.get(Department.rig, SubDir.submit), 
				pAssetName + "_rig_submit").toString();
	}

	/**
	 * The node which the supervisor uses to approve the rigging task.
	 */
	public String
	getRigApproveNode()
	{
		return new Path(pStartPaths.get(Department.rig, SubDir.approve), 
				pAssetName + "_rig_approve").toString();
	}

	/**
	 * The scene which has the finalizeRig script run on it.
	 */
	public String
	getRigVerifyScene()
	{
		return new Path(pStartPaths.get(Department.rig, SubDir.prepare), 
				pAssetName + "_rig").toString();
	}

	/**
	 * The scene delivered to animation. 
	 */
	public String
	getRigProductScene()
	{
//		return new Path(pStartPaths.get(Department.rig, SubDir.product), 
//		pAssetName + "_anim").toString();
		return new Path(StudioDefinitions.getAssetPath(pProject, pAssetType, pAssetName), 
				pAssetName + "_forAnim").toString();
	}



	/*----------------------------------------------------------------------------------------*/
	/*  S H A D E                                                                             */
	/*----------------------------------------------------------------------------------------*/

	/**
	 * The scene where the shader works.
	 */
	public String
	getShadeEditScene()
	{
		return new Path(pStartPaths.get(Department.shade, SubDir.edit), 
				pAssetName + "_mat_edit").toString();
	}

	/**
	 * The node which the artist uses to submit the shading task.
	 */
	public String
	getShadeSubmitNode()
	{
		return new Path(pStartPaths.get(Department.shade, SubDir.submit), 
				pAssetName + "_mat_submit").toString();
	}

	/**
	 * The node which the supervisor uses to approve the shading task.
	 */
	public String
	getShadeApproveNode()
	{
		return new Path(pStartPaths.get(Department.shade, SubDir.approve), 
				pAssetName + "_mat_approve").toString();
	}

	/**
	 * The scene which contains the exported shaders.
	 */
	public String
	getShaderExportScene()
	{
		return new Path(pStartPaths.get(Department.shade, SubDir.prepare), 
				pAssetName + "_matExp").toString();
	}

	/**
	 * The scene which contains the exported shaders put onto the rigged character.
	 */
	public String
	getShadeFinalScene()
	{
		return new Path(pStartPaths.get(Department.shade, SubDir.prepare), 
				pAssetName + "_mat").toString();
	}

	/**
	 * The scene delivered to lighting. 
	 */
	public String
	getShadeProductScene()
	{
		//return new Path(pStartPaths.get(Department.shade, SubDir.product), 
		//                pAssetName + "_lgt").toString();
		return new Path(StudioDefinitions.getAssetPath(pProject, pAssetType, pAssetName), 
				pAssetName + "_forRender").toString();
	}

	//wct
	public String
	getShadeTTScene()
	{
		return new Path(pStartPaths.get(Department.shade, SubDir.prepare), 
				pAssetName + "_tt").toString();
	}

	public String
	getShadeTTRender()
	{
		return new Path(pStartPaths.get(Department.shade, SubDir.prepare), 
				"/img/" + pAssetName + "_tt_render").toString();
	}

	public String
	getShadeTTMovie()
	{
		return new Path(pStartPaths.get(Department.shade, SubDir.prepare), 
				pAssetName + "_tt_render").toString();
	}

	public String
	getShadeTTThumbnail()
	{
		return new Path(pStartPaths.get(Department.shade, SubDir.prepare), 
				pAssetName + "_tt_thumb").toString();
	}

	/*----------------------------------------------------------------------------------------*/
	/*  A S S E T                                                                             */
	/*----------------------------------------------------------------------------------------*/

	/**
	 * The scene where the artist works.
	 */
	public String
	getAssetEditScene()
	{
		return new Path(pAssetStartPaths.get(SubDir.edit), 
				pAssetName + "_edit").toString();
	}

	/**
	 * The node which the artist uses to submit the asset task.
	 */
	public String
	getAssetSubmitNode()
	{
		return new Path(pAssetStartPaths.get(SubDir.submit), 
				pAssetName + "_submit").toString();
	}

	/**
	 * The node which the supervisor uses to approve the asset task.
	 */
	public String
	getAssetApproveNode()
	{
		return new Path(pAssetStartPaths.get(SubDir.approve), 
				pAssetName + "_approve").toString();
	}

	/**
	 * The scene which has the finalizeAsset script run on it.
	 */
	public String
	getAssetVerifyScene()
	{
		return new Path(pAssetStartPaths.get(SubDir.prepare), 
				pAssetName + "_" + pAssetType).toString();
	}

	/**
	 * The scene delivered to animation. 
	 */
	public String
	getAssetProductScene()
	{
//		return new Path(pAssetStartPaths.get(SubDir.product), 
//		pAssetName + "_" + pAssetType).toString();
		return new Path(StudioDefinitions.getAssetPath(pProject, pAssetType, pAssetName), 
				pAssetName).toString();
	}



	/*----------------------------------------------------------------------------------------*/
	/*  T E X T U R E                                                                         */
	/*----------------------------------------------------------------------------------------*/

	/**
	 * The grouping scene where textures are attached.
	 */
	public String
	getTextureEditNode()
	{
		return new Path(pStartPaths.get(Department.texture, SubDir.edit), 
				pAssetName + "_tex_edit").toString();
	}

	/**
	 * The grouping scene where textures are attached.
	 */
	public String
	getTextureProductNode()
	{
		return new Path(pStartPaths.get(Department.texture, SubDir.product), 
				pAssetName + "_tex").toString();
	}


	/*----------------------------------------------------------------------------------------*/
	/*  S T A T I C   H E L P E R S                                                           */
	/*----------------------------------------------------------------------------------------*/

	/**
	 * Turns a node name into an AssetNamer instance.
	 */
	public static AssetNamer
	getNamerFromNodeName
	(
			String nodeName,
			MasterMgrClient mclient,
			QueueMgrClient qclient
	)
	throws PipelineException
	{
		String projectParent = StudioDefinitions.getProjectsParentPath().toString();
		if (!nodeName.startsWith(projectParent))
			throw new PipelineException
			("The node name (" + nodeName + ") does not appear to be a valid asset node.");
		String project = getNextComponent(nodeName, projectParent);
		String assetParent = StudioDefinitions.getAssetParentPath(project).toString();
		if (!nodeName.startsWith(assetParent))
			throw new PipelineException
			("The node name (" + nodeName + ") does not appear to be a valid asset node.");
		String assetType = getNextComponent(nodeName, assetParent);
		String assetTypeParent = 
			StudioDefinitions.getAssetTypeParentPath(project, assetType).toString();
		if (!nodeName.startsWith(assetTypeParent))
			throw new PipelineException
			("The node name (" + nodeName + ") does not appear to be a valid asset node.");
		String assetName = getNextComponent(nodeName, assetTypeParent);
		AssetNamer namer = new AssetNamer(mclient, qclient);
		namer.setParamValue(ParamNames.aProjectName, project);
		namer.setParamValue(ParamNames.aAssetType, assetType);
		namer.setParamValue(ParamNames.aAssetName, assetName);
		namer.run();
		return namer;
	}

	private static String
	getNextComponent
	(
			String full,
			String start
	)
	throws PipelineException
	{
		String replaced = full.replace(start, "");
		Path p = new Path(replaced);
		ArrayList<String> pieces = p.getComponents();
		if (pieces.isEmpty() || pieces.get(0) == null)
			throw new PipelineException
			("The node name (" + full+ ") does not appear to be a valid asset node.");
		return pieces.get(0);
	}



	/*----------------------------------------------------------------------------------------*/
	/*  S T A T I C   I N T E R N A L S                                                       */
	/*----------------------------------------------------------------------------------------*/

	private static final long serialVersionUID = -5038434892228456984L;



	/*----------------------------------------------------------------------------------------*/
	/*  I N T E R N A L S                                                                     */
	/*----------------------------------------------------------------------------------------*/

	private String pProject;

	private String pAssetName;

	private String pAssetType;

	private DoubleMap<Department, SubDir, Path> pStartPaths;

	private TreeMap<SubDir, Path> pAssetStartPaths;

}
