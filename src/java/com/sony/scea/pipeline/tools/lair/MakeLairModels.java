package com.sony.scea.pipeline.tools.lair;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.bootstrap.BootApp;

public class MakeLairModels extends BootApp
{

	MasterMgrClient client;
	PluginMgrClient plug;
	String user = "sballard";
	String view = "build";
	String toolset = "csg-rev12";

	public MakeLairModels()
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

	@SuppressWarnings("unused")
	public void run(String[] arg0)
	{
		Path file = new Path("//Kronos/csg/Temp/jim/LairAssets.txt");
		String working = "//Kronos/csg/pipeline/working/sballard/default";
		String melScript = "/projects/lr/assets/tools/mel/finalize-character";
		String sphereScript = "/projects/lr/assets/tools/mel/character-placeholder";
		Path mayaDummy = new Path("//Kronos/csg/Temp/jim/dummy.mb");
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
					String assetStart = "/projects/lr/assets/character/" + assetName;

					String modScene = assetStart + "/model/" + assetName + "_mod";
					String rigScene = assetStart + "/rig/" + assetName + "_rig";
					String matScene = assetStart + "/material/" + assetName + "_mat";
					String finalScene = assetStart + "/" + assetName;

					String lrModScene = assetStart + "/model/" + assetName + "_mod_lr";
					String lrRigScene = assetStart + "/rig/" + assetName + "_rig_lr";
					String lrMatScene = assetStart + "/material/" + assetName + "_mat_lr";
					String lrFinalScene = assetStart + "/" + assetName + "_lr";

					BaseEditor maya = plug.newEditor("Maya", new VersionID("2.0.10"),
							"Temerity");

					// ArrayList<String> list = new ArrayList<String>();
					// list.add(modScene);
					// list.add(rigScene);
					// list.add(matScene);
					// list.add(finalScene);
					// list.add(lrModScene);
					// list.add(lrRigScene);
					// list.add(lrMatScene);
					// list.add(lrFinalScene);
					//
					// for (String scene : list)
					// {
					// NodeMod mod = client.getWorkingVersion(user, view, scene);
					// mod.setToolset(toolset);
					// client.modifyProperties(user, view, mod);
					// }
					//					
					//					
					//					
					// {
					//						
					// client.link(user, view, rigScene, modScene, DEP, LINKALL,
					// null);
					// client.link(user, view, lrRigScene, lrModScene, DEP, LINKALL,
					// null);
					//
					// }
					// {
					//						
					// }
					//
					// ArrayList<String> mods = new ArrayList<String>();
					// mods.add(lrModScene);
					// mods.add(modScene);
					// for (String scene : mods)
					// {
					// NodeMod mod = client.getWorkingVersion(user, view, scene);
					// BaseAction act = plug.newAction("MayaReference",
					// new VersionID("2.0.10"), "Temerity");
					// client.link(user, view, scene, sphereScript, DEP, LINKALL,
					// null);
					// act.setSingleParamValue("ModelMEL", sphereScript);
					// mod.setAction(act);
					// client.modifyProperties(user, view, mod);
					// }
					//
					// try
					// {
					// client.submitJobs(user, view, finalScene, null);
					// client.submitJobs(user, view, lrFinalScene, null);
					// } catch (Exception ex)
					// {
					// System.out.println(ex.getMessage());
					// }

					// Part 4

//					{
//						NodeID nodeID = new NodeID(user, view, finalScene);
//						client.checkIn(nodeID,
//								"Inital model tree with placeholder geometry in model scene.",
//								VersionID.Level.Minor);
//					}
//
//					{
//						NodeID nodeID = new NodeID(user, view, lrFinalScene);
//						client.checkIn(nodeID,
//								"Inital model tree with placeholder geometry in model scene.",
//								VersionID.Level.Minor);
//					}

					// Part 3
					// try
					// {
					// client.submitJobs(user, view, finalScene, null);
					// client.submitJobs(user, view, lrFinalScene, null);
					// } catch (PipelineException ex)
					// {}

					// Part2
					// {
					// NodeMod mod = client.getWorkingVersion(user, view, modScene);
					// mod.setAction(null);
					// client.unlink(user, view, modScene, sphereScript);
					// client.modifyProperties(user, view, mod);
					// }
					// {
					// NodeMod mod = client.getWorkingVersion(user, view, matScene);
					// mod.setActionEnabled(false);
					// client.modifyProperties(user, view, mod);
					// }

					// {
					// NodeMod mod = client.getWorkingVersion(user, view,
					// lrModScene);
					// mod.setAction(null);
					// client.unlink(user, view, lrModScene, sphereScript);
					// client.modifyProperties(user, view, mod);
					// }
					// {
					// NodeMod mod = client.getWorkingVersion(user, view,
					// lrMatScene);
					// mod.setActionEnabled(false);
					// client.modifyProperties(user, view, mod);
					// }

					// Pass 1
					// {
					// NodeMod mod = registerNode(modScene, "ma", maya);
					// BaseAction act = plug.newAction("MayaReference",
					// new VersionID("2.0.9"), "Temerity");
					// client.link(user, view, modScene, sphereScript, DEP, LINKALL,
					// null);
					// act.setSingleParamValue("ModelMEL", sphereScript);
					// mod.setAction(act);
					// client.modifyProperties(user, view, mod);
					// }
					// {
					// NodeMod mod = registerNode(rigScene, "ma", maya);
					// BaseAction act = plug.newAction("MayaImport", new
					// VersionID("2.0.9"),
					// "Temerity");
					// referenceNode(rigScene, modScene, act, REF, "mod");
					// mod.setAction(act);
					// client.modifyProperties(user, view, mod);
					// }
					// {
					// NodeMod mod = registerNode(matScene, "ma", maya);
					// BaseAction act = plug.newAction("MayaReference",
					// new VersionID("2.0.9"), "Temerity");
					// referenceNode(matScene, rigScene, act, REF, "rig");
					// mod.setAction(act);
					// client.modifyProperties(user, view, mod);
					// }
					// {
					// NodeMod mod = registerNode(finalScene, "ma", maya);
					// BaseAction act = plug.newAction("MayaImport", new
					// VersionID("2.0.9"),
					// "Temerity");
					// referenceNode(finalScene, matScene, act, DEP, "mat");
					// client.link(user, view, finalScene, melScript, DEP, LINKALL,
					// null);
					// act.setSingleParamValue("ModelMEL", melScript);
					// mod.setAction(act);
					// client.modifyProperties(user, view, mod);
					// }
					//
					// // lowrez
					// {
					// NodeMod mod = registerNode(lrModScene, "ma", maya);
					// BaseAction act = plug.newAction("MayaReference",
					// new VersionID("2.0.9"), "Temerity");
					// client.link(user, view, lrModScene, sphereScript, DEP,
					// LINKALL, null);
					// act.setSingleParamValue("ModelMEL", sphereScript);
					// mod.setAction(act);
					// client.modifyProperties(user, view, mod);
					// }
					// {
					// NodeMod mod = registerNode(lrRigScene, "ma", maya);
					// BaseAction act = plug.newAction("MayaImport", new
					// VersionID("2.0.9"),
					// "Temerity");
					// referenceNode(lrRigScene, lrModScene, act, REF, "mod");
					// mod.setAction(act);
					// client.modifyProperties(user, view, mod);
					// }
					// {
					// NodeMod mod = registerNode(lrMatScene, "ma", maya);
					// BaseAction act = plug.newAction("MayaReference",
					// new VersionID("2.0.9"), "Temerity");
					// referenceNode(lrMatScene, lrRigScene, act, REF, "rig");
					// mod.setAction(act);
					// client.modifyProperties(user, view, mod);
					// }
					// {
					// NodeMod mod = registerNode(lrFinalScene, "ma", maya);
					// BaseAction act = plug.newAction("MayaImport", new
					// VersionID("2.0.9"),
					// "Temerity");
					// referenceNode(lrFinalScene, lrMatScene, act, DEP, "mat");
					// client.link(user, view, lrFinalScene, melScript, DEP, LINKALL,
					// null);
					// act.setSingleParamValue("ModelMEL", melScript);
					// mod.setAction(act);
					// client.modifyProperties(user, view, mod);
					// }
					// client.submitJobs(user, view, finalScene, null);
					// client.submitJobs(user, view, lrFinalScene, null);

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

	public static void copy(File source, File dest) throws IOException
	{
		FileChannel in = null;
		FileChannel out = null;
		try
		{
			in = new FileInputStream(source).getChannel();
			out = new FileOutputStream(dest).getChannel();

			long size = in.size();
			MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);

			out.write(buf);

		} finally
		{
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		}
	}

	public static void main(String[] args)
	{
		MakeLairModels mod = new MakeLairModels();
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

}
