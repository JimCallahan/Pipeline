package com.sony.scea.pipeline.tools.lair;

import static com.sony.scea.pipeline.tools.Globals.setPresets;
import static com.sony.scea.pipeline.tools.lair.LairConstants.*;

import java.io.*;
import java.util.ArrayList;
import java.util.SortedMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.bootstrap.BootApp;

@SuppressWarnings("unused")
public class MakeLairTurnTables extends BootApp
{

	MasterMgrClient client;
	PluginMgrClient plug;
	String user = "sballard";
	String view = "build";
	String toolset = "csg-rev12";
	int start = 0;
	int end = 720;
	int by = 90;

	public MakeLairTurnTables()
	{
		try
		{
			PluginMgrClient.init();
			client = new MasterMgrClient();
			plug = PluginMgrClient.getInstance();
		} catch (PipelineException ex)
		{
			ex.printStackTrace();
		}
	}

	// @SuppressWarnings("unused")
	public void run(String[] arg0)
	{
		Path file = new Path("//Kronos/csg/Temp/jim/LairAssets.txt");
		// String working = "//Kronos/csg/pipeline/working/sballard/default";
		// String melScript = "/projects/lr/assets/tools/mel/finalize-character";
		// String sphereScript = "/projects/lr/assets/tools/mel/sphere";
		// Path mayaDummy = new Path("//Kronos/csg/Temp/jim/dummy.mb");
		String turntableScript = "/projects/lr/assets/tools/mel/turntable-import";
		String loadMIScript = "/projects/lr/assets/tools/mel/mr-init";
		ArrayList<String> passes = new ArrayList<String>();
		passes.add("beau");
		int proc = 3;
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(file.toFile()));
			boolean read = true;
			String line = null;
			while (read)
			{
				line = in.readLine();
				if (line != null)
				{
					System.out.println(line);
					String assetName = line;
					String turntableName = "circ360";
					String assetStart = "/projects/lr/assets/character/" + assetName;
					String turntableStart = "/projects/lr/production/tt/character/"
							+ assetName + "/" + turntableName;

					// String modScene = assetStart + "/model/" + assetName + "_mod";
					// String rigScene = assetStart + "/rig/" + assetName + "_rig";
					// String matScene = assetStart + "/material/" + assetName +
					// "_mat";
					String finalScene = assetStart + "/" + assetName;

					String lrModScene = assetStart + "/model/" + assetName + "_mod_lr";
					// String lrRigScene = assetStart + "/rig/" + assetName +
					// "_rig_lr";
					// String lrMatScene = assetStart + "/material/" + assetName +
					// "_mat_lr";
					// String lrFinalScene = assetStart + "/" + assetName + "_lr";

					String namePrefix = assetName + "_" + turntableName + "_";
					String shdScene = assetStart + "/shader/" + assetName + "_shd";
					String turntableScene = turntableStart + "/lgt/" + namePrefix + "lgt";
					String shdIncScene = assetStart + "/shader/shaders/" + assetName
							+ "_shdinc";
					FileSeq shdIncSecSeq = new FileSeq("mi_shader_defs", "mi");

					// System.out.println(shdScene);
					// System.out.println(turntableShot);
					// /tests/projects/proto/production/turntables/character/spikey/circular360/mi/spikey/beauty/shd

					BaseEditor maya = plug.newEditor("Maya", new VersionID("2.0.10"),
							"Temerity");
					BaseEditor scite = plug.newEditor("SciTE", new VersionID("2.0.10"),
							"Temerity");
					BaseEditor fcheck = plug.newEditor("FCheck", new VersionID("2.0.9"),
							"Temerity");

					String baseTurntableShot = "/projects/lr/assets/tt/setups/"
							+ turntableName;

					for (String pass : passes)
					{

						String baseTTOptionsMI = "/projects/lr/assets/tt/opt/" + pass;

						String ttShadeMI = turntableStart + "/mi/" + assetName + "/" + pass
								+ "/shd";
						String ttLightMI = turntableStart + "/mi/com/" + pass + "/lgt";
						String ttGeoMI = turntableStart + "/mi/" + assetName + "/com/mod";
						String ttCamMI = turntableStart + "/mi/com/cam";
						String ttCamOverMI = turntableStart + "/mi/com/" + pass + "/camOpt";
						String ttOptionsMI = turntableStart + "/mi/com/" + pass + "/opt";
						String ttImages = turntableStart + "/img/" + assetName + "/" + pass
								+ "/" + namePrefix + pass;
						String ttShaderIncMI = turntableStart + "/mi/" + assetName + "/" + pass
								+ "/shaders/mi_shader_defs";

						/*
						 * System.out.println(shdIncScene);
						 * System.out.println(shdScene);
						 * System.out.println(ttShadeMI);
						 * System.out.println(ttShaderIncMI);
						 * System.out.println(ttCamMI);
						 * System.out.println(ttCamOverMI);
						 * System.out.println(ttLightMI);
						 * System.out.println(ttOptionsMI);
						 * System.out.println(ttShadeMI); System.out.println(ttGeoMI);
						 * System.out.println(ttImages);
						 */
						ArrayList<String> addedNodes = new ArrayList<String>();

						if (proc == 1)
						{
							try
							{
								{
									NodeMod mod = registerNode(shdIncScene, "mi", scite);
									addedNodes.add(shdIncScene);
									mod.addSecondarySequence(shdIncSecSeq);
									BaseAction act = actionMRayShaderInclude();
									mod.setAction(act);
									client.modifyProperties(user, view, mod);
								}

								{
									NodeMod mod = registerNode(ttShaderIncMI, "mi", scite);
									addedNodes.add(ttShaderIncMI);
									client.link(user, view, ttShaderIncMI, shdIncScene, DEP,
											LINKALL, null);
									BaseAction act = actionCatFiles();
									act.initSecondarySourceParams(shdIncScene, shdIncSecSeq
											.getFilePattern());
									act.setSecondarySourceParamValue(shdIncScene, shdIncSecSeq
											.getFilePattern(), "Order", 100);
									mod.setAction(act);
									client.modifyProperties(user, view, mod);
								}

								{
									NodeMod mod = registerNode(shdScene, "ma", maya);
									addedNodes.add(shdScene);
									BaseAction act = actionMayaReference();
									referenceNode(shdScene, finalScene, act, REF, "final");
									client.link(user, view, shdScene, shdIncScene, REF, LINKALL,
											null);
									client.link(user, view, shdScene, loadMIScript, DEP, LINKALL,
											null);
									act.setSingleParamValue("InitialMEL", loadMIScript);
									mod.setAction(act);
									client.modifyProperties(user, view, mod);
								}
								{
									cloneNode(ttOptionsMI, baseTTOptionsMI);
									addedNodes.add(ttOptionsMI);
								}
								{ 
									NodeMod mod = registerNode(turntableScene, "ma", maya);
									addedNodes.add(turntableScene);
									BaseAction act = actionMayaReference();
									referenceNode(turntableScene, lrModScene, act, REF, "mod");
									referenceNode(turntableScene, baseTurntableShot, act, DEP,
											"turn");
									client.link(user, view, turntableScene, turntableScript, DEP,
											LINKALL, null);
									act.setSingleParamValue("ModelMEL", turntableScript);
									mod.setAction(act);
									client.modifyProperties(user, view, mod);
								}
								{
									NodeMod mod = registerSequence(ttLightMI, 4, "mi", scite,
											start, end, by);
									addedNodes.add(ttLightMI);
									client.link(user, view, ttLightMI, turntableScene, DEP,
											LINKALL, null);
									BaseAction act = actionMayaMiExport();
									SortedMap<String, Comparable> preset = act.getPresetValues(
											"EntityPresets", preset_MRLIGHTS);
									setPresets(act, preset);
									act.setSingleParamValue("MayaScene", turntableScene);
									act.setSingleParamValue("ExportSet", "LIGHT");
									mod.setAction(act);
									JobReqs req = mod.getJobRequirements();
									req.addSelectionKey("MentalRay");
									mod.setJobRequirements(req);
									mod.setExecutionMethod(ExecutionMethod.Parallel);
									mod.setBatchSize(100);
									client.modifyProperties(user, view, mod);
								}
								{
									NodeMod mod = registerSequence(ttCamMI, 4, "mi", scite, start,
											end, by);
									addedNodes.add(ttCamMI);
									client.link(user, view, ttCamMI, turntableScene, DEP, LINKALL,
											null);
									BaseAction act = actionMayaMiExport();
									SortedMap<String, Comparable> preset = act.getPresetValues(
											"EntityPresets", preset_CAMERAS);
									setPresets(act, preset);
									act.setSingleParamValue("MayaScene", turntableScene);
									act.setSingleParamValue("ExportSet", "CAMERA");
									mod.setAction(act);
									JobReqs req = mod.getJobRequirements();
									req.addSelectionKey("MentalRay");
									mod.setJobRequirements(req);
									mod.setExecutionMethod(ExecutionMethod.Parallel);
									mod.setBatchSize(100);
									client.modifyProperties(user, view, mod);
								}
								{
									NodeMod mod = registerNode(ttCamOverMI, "mi", scite);
									addedNodes.add(ttCamOverMI);
									BaseAction act = actionMRayCamOverride();
									mod.setAction(act);
									client.modifyProperties(user, view, mod);
								}
								{
									NodeMod mod = registerNode(ttGeoMI, "mi", scite);
									addedNodes.add(ttGeoMI);
									client.link(user, view, ttGeoMI, finalScene, DEP, LINKALL,
											null);
									BaseAction act = actionMayaMiExport();
									SortedMap<String, Comparable> preset = act.getPresetValues(
											"EntityPresets", preset_GEOALL);
									setPresets(act, preset);
									act.setSingleParamValue("MayaScene", finalScene);
									act.setSingleParamValue("ExportSet", "GEOMETRY");
									mod.setAction(act);
									client.modifyProperties(user, view, mod);
								}
								{
									NodeMod mod = registerNode(ttShadeMI, "mi", scite);
									addedNodes.add(ttShadeMI);
									client.link(user, view, ttShadeMI, shdScene, DEP, LINKALL,
											null);
									client.link(user, view, ttShadeMI, ttShaderIncMI, DEP,
											LINKALL, null);
									BaseAction act = actionMayaMiShader();
									act.setSingleParamValue("MayaScene", shdScene);
									act.setSingleParamValue("MaterialNamespace", "final");
									mod.setAction(act);
									client.modifyProperties(user, view, mod);
								}
								{
									NodeMod mod = registerSequence(ttImages, 4, "iff", fcheck,
											start, end, by);
									addedNodes.add(ttImages);
									client.link(user, view, ttImages, ttShadeMI, DEP, LINKALL,
											null);
									client.link(user, view, ttImages, ttOptionsMI, DEP, LINKALL,
											null);
									client.link(user, view, ttImages, ttLightMI, DEP, LINKONE, 0);
									client.link(user, view, ttImages, ttCamMI, DEP, LINKONE, 0);
									client.link(user, view, ttImages, ttGeoMI, DEP, LINKALL, null);
									client.link(user, view, ttImages, ttCamOverMI, DEP, LINKALL,
											null);
									client.link(user, view, ttImages, shdIncScene, DEP, LINKALL,
											null);
									BaseAction act = actionMRayRender();
									act.initSourceParams(ttShadeMI);
									act.initSourceParams(ttOptionsMI);
									act.initSourceParams(ttLightMI);
									act.initSourceParams(ttCamMI);
									act.initSourceParams(ttGeoMI);
									act.initSourceParams(shdIncScene);
									act.setSourceParamValue(shdIncScene, "Order", 100);
									act.setSourceParamValue(ttOptionsMI, "Order", 200);
									act.setSourceParamValue(ttShadeMI, "Order", 300);
									act.setSourceParamValue(ttGeoMI, "Order", 400);
									act.setSourceParamValue(ttLightMI, "Order", 500);
									act.setSourceParamValue(ttCamMI, "Order", 600);
									mod.setAction(act);
									JobReqs req = mod.getJobRequirements();
									req.addSelectionKey("MentalRay");
									mod.setJobRequirements(req);
									mod.setExecutionMethod(ExecutionMethod.Parallel);
									mod.setBatchSize(1);
									client.modifyProperties(user, view, mod);
								}
								client.submitJobs(user, view, ttImages, null);

							} catch (PipelineException ex)
							{
								for (String node : addedNodes)
								{
									client.release(user, view, node, true);
								}
								ex.printStackTrace();
							} catch (Exception ex)
							{
								for (String node : addedNodes)
								{
									client.release(user, view, node, true);
								}
								ex.printStackTrace();
							}
						} else if (proc == 2)
						{
							disableAction(turntableScene);
							disableAction(shdScene);
						} else if (proc == 3)
						{
							{
								NodeID nodeID = new NodeID(user, view, ttImages);
								client.checkIn(nodeID, "Inital turntable tree with "
										+ "placeholder geometry in model scene, no shaders.",
										VersionID.Level.Minor);
							}

						}
					}

				} else
				{
					read = false;
				}

			}
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (PipelineException e)
		{
			e.printStackTrace();
		}

	}

	public NodeMod cloneNode(String newName, String oldName) throws PipelineException
	{
		Path p = new Path(newName);
		String name = p.getName();

		NodeMod oldMod = client.getWorkingVersion(user, view, oldName);
		FileSeq oldSeq = oldMod.getPrimarySequence();
		FilePattern oldPat = oldSeq.getFilePattern();

		FrameRange range = null;
		FilePattern pat = null;

		if (oldSeq.hasFrameNumbers())
		{
			range = oldSeq.getFrameRange();
			pat = new FilePattern(name, oldPat.getPadding(), oldPat.getSuffix());
		} else
		{
			range = null;
			pat = new FilePattern(name, oldPat.getSuffix());
		}
		FileSeq newSeq = new FileSeq(pat, range);
		NodeMod newMod = new NodeMod(newName, newSeq, oldMod.getSecondarySequences(),
				oldMod.getToolset(), oldMod.getEditor());
		client.register(user, view, newMod);
		NodeID source = new NodeID(user, view, oldName);
		NodeID target = new NodeID(user, view, newName);
		client.cloneFiles(source, target);
		return newMod;
	}

	public void disableAction(String name) throws PipelineException
	{
		NodeID nodeID = new NodeID(user, view, name);
		NodeMod nodeMod = client.getWorkingVersion(nodeID);
		nodeMod.setActionEnabled(false);
		client.modifyProperties(user, view, nodeMod);
	}

	public NodeMod registerSequence(String name, int pad, String extention,
			BaseEditor editor, int startF, int endf, int byF) throws PipelineException
	{
		Path p = new Path(name);
		FilePattern pat = new FilePattern(p.getName(), pad, extention);
		FrameRange range = new FrameRange(startF, endf, byF);
		FileSeq animSeq = new FileSeq(pat, range);
		NodeMod animNode = new NodeMod(name, animSeq, null, toolset, editor);
		client.register(user, view, animNode);
		return animNode;
	}

	public NodeMod registerNode(String name, String extention, BaseEditor editor)
			throws PipelineException
	{
		File f = new File(name);
		FileSeq animSeq = new FileSeq(f.getName(), extention);
		NodeMod animNode = new NodeMod(name, animSeq, null, toolset, editor);
		client.register(user, view, animNode);
		return animNode;
	}

	public void referenceNode(String target, String source, BaseAction action,
			LinkPolicy policy, String nameSpace) throws PipelineException
	{
		boolean reference = false;

		String actionType = action.getName();
		if (actionType.equals("MayaReference") || actionType.equals("MayaImport"))
			reference = true;

		client.link(user, view, target, source, policy, LINKALL, null);
		if (reference)
		{
			action.initSourceParams(source);
			action.setSourceParamValue(source, "PrefixName", nameSpace);
		}
	}

	public static void main(String[] args)
	{
		MakeLairTurnTables mod = new MakeLairTurnTables();
		mod.run(new String[1]);
	}
	public static final LinkPolicy REF = LinkPolicy.Reference;
	public static final LinkRelationship LINKALL = LinkRelationship.All;
	public static final LinkRelationship LINKONE = LinkRelationship.OneToOne;
	public static final LinkPolicy DEP = LinkPolicy.Dependency;
	public static final CheckOutMode over = CheckOutMode.OverwriteAll;
	public static final CheckOutMode keep = CheckOutMode.KeepModified;
	public static final CheckOutMethod modi = CheckOutMethod.Modifiable;
	public static final CheckOutMethod froz = CheckOutMethod.AllFrozen;
	public static final CheckOutMethod frozU = CheckOutMethod.FrozenUpstream;
	public static final CheckOutMethod pFroz = CheckOutMethod.PreserveFrozen;

	// From MayaMiExportAction
	private static final String preset_MRSHADE = "Mental Ray Shaders and Material";
	private static final String preset_GEOINST = "Geometry Instances";
	private static final String preset_GEODEC = "Geometry Definition (Stub Materials/No Instances)";
	private static final String preset_GEOALL = "Geometry (Including Instances/Stub Materials)";
	private static final String preset_OPTIONS = "Options (Render Globals)";
	private static final String preset_CAMERAS = "Camera Declarations and Instances";
	private static final String preset_MRLIGHTS = "Mental Ray Lights";

}
