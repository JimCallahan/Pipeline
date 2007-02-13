package com.sony.scea.pipeline.plugins.v1_0_0;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/**
 * Updates the cameras for each of the selected anim nodes.
 * <p>
 * Since cameras were imported into the scenes in Lair, there was
 * no way to update them without destroying all the animation in the scene.
 * So this tool was written which would open up the maya scene, delete the existing
 * camera and then import the new camera.  In retrospect, there may have been 
 * better ways of handling this, but such is life.
 * 
 * @author Ifedayo O. Ojomo
 * @version 1.0.0
 */
public class UpdateCameraTool extends BaseTool{ 

	/*-----------------------------------------------*/
	/*             STATIC INTERNAL VARS              */
	/*-----------------------------------------------*/
	private static final long serialVersionUID = -1418896022565586770L;

	/*-----------------------------------------------*/
	/*                  INTERNALS                    */
	/*-----------------------------------------------*/

	/**
	 * The current working area user|view.
	 */
	private String pUser;
	private String pView;

	private String animPattern = ".*/production/.*/anim/.*_anim";
	private String camPattern = ".*/assets/camera/.*/cam_.*";

	public final CheckOutMode over = CheckOutMode.OverwriteAll;
	public final CheckOutMethod frozU = CheckOutMethod.FrozenUpstream;


	/**
	 * 
	 *
	 */
	public UpdateCameraTool()
	{
		super("UpdateCameraTool", new VersionID("1.0.0"), "SCEA",
		"Tool to replace an imported camera with the newest version of that camera");

		underDevelopment();

		addSupport(OsType.MacOS);
		addSupport(OsType.Windows);
	}

	
	
	/* (non-Javadoc)
	 * @see us.temerity.pipeline.BaseTool#collectPhaseInput()
	 */
	@Override
	public synchronized String collectPhaseInput() throws PipelineException  {
		if ( pPrimary == null )
			throw new PipelineException("Please select something!");

		if ( pSelected.size() < 1 )
			throw new PipelineException("Please select something!");

		NodeStatus status = pSelected.get(pPrimary);
		NodeID nodeID = status.getNodeID();
		pUser = nodeID.getAuthor();
		pView = nodeID.getView();

		return "...All is said...All is done";
	}

	/* (non-Javadoc)
	 * @see us.temerity.pipeline.BaseTool#executePhase(us.temerity.pipeline.MasterMgrClient, 
	 * us.temerity.pipeline.QueueMgrClient)
	 */
	@Override
	public synchronized boolean executePhase(MasterMgrClient mclient, 
			QueueMgrClient qclient) throws PipelineException {
		
		for(String anim: pSelected.keySet()){
			//System.err.println("Working on "+anim);
			if(!anim.matches(animPattern))
				System.err.println("Skipping...\n");

			Set<String> srcs = pSelected.get(anim).getSourceNames();
			for(String src: srcs){
				//System.err.println(src + " is the source");
				if(src.matches(camPattern)){					
					String shortCamName = src.replaceAll(".*/assets/camera/.*/", "");					
					mclient.checkOut(pUser, pView, src, null, over, frozU);
					//System.err.println("Checking out camera: "+src+" with short name "+shortCamName);
					
					/* writing the mel script */
					{
						File script = null;
						try {
							script =
								File.createTempFile("UpdateCameraTool.", ".mel", 
										PackageInfo.sTempPath.toFile());
							FileCleaner.add(script);
						} catch(IOException ex) {
							throw new PipelineException(
							"Unable to create the temporary MEL script!");
						}//end catch

						try {
							PrintWriter out = new PrintWriter(new BufferedWriter(new 
									FileWriter(script)));
							out.println("file -lnr -open \"$WORKING"+anim+".ma\";");
							out.println("delete `ls \""+shortCamName+":*\"`;");
							out.println("namespace -f -mv \""+shortCamName+"\" \":\";");	
							out.println("namespace -rm \""+shortCamName+"\";");						
							out.println("file -import -type \"mayaAscii\" " +
									"-namespace \""+shortCamName+"\" -options \"v=0\" "
									+ "-pr \"$WORKING" + src + ".ma\";");
							out.println("// SAVE");
							out.println("file -save;");

							out.close();
						}//end try
						catch(IOException ex) {
							throw new PipelineException("Unable to write the temporary MEL script(" + script
									+ ") used add the references!");
						}//end catch


						/* run Maya to collect the information */
						try {


							NodeID targetID = new NodeID(pUser,pView,anim);
							NodeStatus targetStat = pSelected.get(anim);
							NodeMod targetMod = targetStat.getDetails().getWorkingVersion();
							ArrayList<String> args = new ArrayList<String>();

							if(targetMod == null)
								throw new PipelineException("No working version of the Target" +
										" Scene Node ("	+ anim + ") exists in the (" + pView + 
										") working area owned by ("	+ PackageInfo.sUser+ ")!");

							/*Get path*/
							FileSeq fseq = targetMod.getPrimarySequence();
							String suffix = fseq.getFilePattern().getSuffix();
							if(!fseq.isSingle() || (suffix == null)
									|| (!suffix.equals("ma") && !suffix.equals("mb")))
								throw new PipelineException("The target node (" + anim
										+ ") must be a maya scene!");
							
							@SuppressWarnings("unused")
							Path targetPath =
								new Path(PackageInfo.sProdPath, targetID.getWorkingParent() + "/" + 
									fseq.getFile(0));

							args.add("-batch");
							args.add("-script");
							args.add(script.getPath());
							//args.add("-file");
							//args.add(targetPath.toOsString());
							Path wdir =
								new Path(PackageInfo.sProdPath.toOsString() + 
										targetID.getWorkingParent());
							TreeMap<String, String> env =
								mclient.getToolsetEnvironment(pUser, pView, targetMod.getToolset(),
										PackageInfo.sOsType);
							Map<String, String> nenv = env;
							String midefs = env.get("PIPELINE_MI_SHADER_PATH");
							if(midefs != null) {
								nenv = new TreeMap<String, String>(env);
								Path dpath = new Path(new Path(wdir, midefs));
								nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toOsString());
							}
							String command = "maya";
							if (PackageInfo.sOsType.equals(OsType.Windows))
								command += ".exe";
							SubProcessLight proc =
								new SubProcessLight("UpdateCameraTool", command, args, env, 
										wdir.toFile());
							try {
								proc.start();
								proc.join();
								if(!proc.wasSuccessful()) {
									throw new PipelineException(
											"Did not correctly edit the reference due to a maya error.!\n\n"
											+ proc.getStdOut() + "\n\n" + proc.getStdErr());
								}//end if
							}//end try
							catch(InterruptedException ex) {
								throw new PipelineException(ex);
							}//end catch
						}//end try
						catch(Exception ex) {
							throw new PipelineException(ex);
						}//end catch
					}
				}//end if
			}//end for

		}//end for		

		return false;
	}

}//end class
