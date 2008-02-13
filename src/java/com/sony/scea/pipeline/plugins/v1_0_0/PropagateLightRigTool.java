package com.sony.scea.pipeline.plugins.v1_0_0;

import java.awt.Component;
import java.awt.Dimension;
import java.io.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.NodeTreeComp.State;
import us.temerity.pipeline.ui.*;

/**
 * 
 * @author Ifedayo O. Ojomo
 * @version 1.0.0
 */
public class PropagateLightRigTool extends BaseTool{

	private static final long serialVersionUID = 8972964285987913892L;

	private String lgtPattern = ".*/production/lair/seq.*/lgt/seq.*_lgt";

	/**
	 * The current working area user|view.
	 */
	private String pUser;
	private String pView;
	//private String folder;
	private String lightRigName;
	private String pToolset;

	private int pPhase;
	private File linkFile;
	private String hdrLoc;
	private TreeSet<String> seqLgtNodes;
	private TreeMap<String, JBooleanField> pSubstituteFields;
	private TreeSet<String> shotsToLight;

	private String suffix;

	public PropagateLightRigTool(){

		super("PropagateLightRigTool", new VersionID("1.0.0"), "SCEA",
		"Tool to propigate a light rig to other shots");

		pPhase = 1;
		linkFile = null;
		hdrLoc = null;
		seqLgtNodes = new TreeSet<String>();
		shotsToLight = new TreeSet<String>();
		pSubstituteFields = new TreeMap<String, JBooleanField>();

		//addSupport(OsType.MacOS);
		//addSupport(OsType.Windows);

		underDevelopment();
	}

	/**
	 * Check that the user has properly selected a target node for this tool <P>
	 * 
	 * @return 
	 *   The phase progress message or <CODE>null</CODE> to abort early.
	 * 
	 * @throws PipelineException
	 *   If unable to validate the given user input.
	 */
	public synchronized String collectPhaseInput() throws PipelineException
	{
		switch(pPhase){
		case 1:
			return collectFirstPhaseInput();
		case 2:
			return collectSecondPhaseInput();
		default:
			return null;
		}
	}//end collectPhaseInput


	public synchronized String collectFirstPhaseInput() throws PipelineException
	{
		if(pPrimary==null)
			throw new PipelineException("Please select something!");

		if(pSelected.size()!=1)
			throw new PipelineException("Only one node can be selected!");

		if(!pPrimary.matches(lgtPattern))
			throw new PipelineException("This tool only works on lgt nodes.");

		NodeStatus status = pSelected.get(pPrimary);
		NodeID nodeID = status.getNodeID();
		pUser = nodeID.getAuthor();
		pView = nodeID.getView();

		Path nPath = new Path(pPrimary);
		String lgtNodeName = nPath.getName();
		//String seqNum = lgtNodeName.replaceFirst("seq","");
		//seqNum = seqNum.replaceAll("_.*_lgt","");

		String seqNum = lgtNodeName.replaceFirst("_.*_lgt","");
		String shotNum = lgtNodeName.replaceFirst(seqNum+"_","").replaceFirst("_lgt", "");
		System.err.println("The sequence number is "+seqNum+ " shot is "+shotNum);

		//folder = "/projects/lr/assets/lights/" + seqNum + "/";
		lightRigName = "/projects/lr/assets/lights/" + seqNum + "/" + seqNum + "_" + shotNum +"_rig";

		return ": This is the song that doesn't end...";
	}//end collectPhaseInput()


	public synchronized String collectSecondPhaseInput() throws PipelineException
	{

		int sTSize = 120;
		int sVSize = 50;

		/* DO GUI DRAWING STUFF*/
		JScrollPane scroll = null;
		{
			Box ibox = new Box(BoxLayout.Y_AXIS);
			Component comps[] = UIFactory.createTitledPanels();
			JPanel tpanel = (JPanel) comps[0];
			JPanel vpanel = (JPanel) comps[1];
			String title = "Add Light Rig to Shots:";
			JDrawer shotListDrawer = new JDrawer(title , (JComponent) comps[2], true);
			ibox.add(shotListDrawer);

			if(seqLgtNodes.isEmpty()) {


				tpanel.add(Box.createRigidArea(new Dimension(sTSize - 7, 0)));
				vpanel.add(Box.createHorizontalGlue());

				ibox.add(comps[2]);
			} else {  
				for(String shot: seqLgtNodes){
					System.err.println(shot);
					if(shot.equals(pPrimary)){
						System.err.println("Match: Skipping "+shot);
						continue;
					}

					String shortShot = (new Path(shot)).getName();
					JBooleanField field = 
						UIFactory.createTitledBooleanField(tpanel, shortShot, sTSize, 
								vpanel, sVSize,"Whether to put the light rig in this lgt scene.");
					field.setName(shortShot);
					field.setValue(false);

					if(!pSubstituteFields.containsKey(shot))
						pSubstituteFields.put(shot, field);

					UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
				}
			}		      

			{
				JPanel spanel = new JPanel();
				spanel.setName("Spacer");

				spanel.setMinimumSize(new Dimension(sTSize + sVSize, 7));
				spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
				spanel.setPreferredSize(new Dimension(sTSize + sVSize, 7));

				ibox.add(spanel);
			}

			{
				scroll = new JScrollPane(ibox);
				scroll
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				scroll
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

				Dimension size = new Dimension(sTSize + sVSize + 52, 300);
				scroll.setMinimumSize(size);
				scroll.setPreferredSize(size);

				scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
			}
		}

		/* query the user */
		JToolDialog diag = new JToolDialog("Light These Shots", scroll,"Confirm");
		diag.setVisible(true);

		/* Process User Input */
		if(diag.wasConfirmed()) {

			for (String lgtShot : pSubstituteFields.keySet()) {
				JBooleanField field = pSubstituteFields.get(lgtShot);
				Boolean bUpdate = field.getValue();
				if(bUpdate) 
					shotsToLight.add(lgtShot);
			}
		} 
		return "...cos it goes on and on my friend!";
	}


	/**
	 * Perform execution of the tool.<P>
	 * 
	 * @param mclient
	 *   The network connection to the plmaster(1) daemon.
	 * 
	 * @param qclient
	 *   The network connection to the plqueuemgr(1) daemon.
	 * 
	 * @return 
	 *   Whether to continue and collect user input for the next phase of the tool.
	 * 
	 * @throws PipelineException
	 *   If unable to sucessfully execute this phase of the tool.
	 */
	public synchronized boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient)
	throws PipelineException{

		switch(pPhase){
		case 1:
			return executeFirstPhase(mclient, qclient);
		case 2:
			return executeSecondPhase(mclient, qclient);
		default:
			return false;
		}
	}//end executePhase


	public synchronized boolean executeFirstPhase(MasterMgrClient mclient, QueueMgrClient qclient)
	throws PipelineException
	{
		pToolset = mclient.getDefaultToolsetName();

		if(doesNodeExists(mclient, lightRigName)){
			System.err.println("The node "+lightRigName+ " already exists. Now do something with it.");
		} else {
			mclient.clone(pUser,pView, pPrimary, lightRigName, false, false, true, true);
		}

		try
		{
			linkFile = File.createTempFile("PropagateLightRigTool-Info.", ".txt",
					PackageInfo.sTempPath.toFile());
			FileCleaner.add(linkFile);
		} catch ( IOException ex )
		{
			throw new PipelineException(
					"Unable to create the temporary text file used to store the texture "
					+ "information collected from the Maya scene!");
		}

		File eScript;
		try
		{
			NodeID nodeID = new NodeID(pUser,pView, pPrimary);
			Path wdir = new Path(PackageInfo.sProdPath.toOsString());
			String workingArea = nodeID.getWorkingPath().toString().replaceAll(pPrimary,"");
			workingArea = wdir.toOsString()+workingArea;
			System.err.println("Mel: "+workingArea);

			eScript = File.createTempFile("PropagateLightRigTool-Export.", ".mel",
					PackageInfo.sTempPath.toFile());
			FileCleaner.add(eScript);


			System.err.println("Created the mel and text files in preparation for writing.");

			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(eScript)));

			out.println("{");
			out.println("	string $infoFile = \"" + linkFile.getAbsolutePath()+ "\";");
			out.println("	$fileId = `fopen $infoFile w`;");

			out.println("	string $lights[] =  `ls -l -type mentalrayIblShape`;");
			out.println("	if(size($lights)>0){");
			out.println("		string $hdr = `getAttr ($lights[0]+\".texture\")`;");
			out.println("		$hdr = `substitute \"$WORKING\" $hdr \"\"`;");
			out.println("		$hdr = `substring $hdr 1 (size($hdr)-4)`;");

			out.println("		fprint $fileId (\"hdr: \"+$hdr+\"\\n\");");			
			out.println("		fprint $fileId (\"ibl: \"+$lights[0]+\"\\n\");");
			out.println("	}");

			out.println("	$lights = `ls -l -type light`;");
			out.println("	string $lgt;");

			out.println("	for($lgt in $lights){");
			out.println("		string $temp[] = `listRelatives -pa -parent $lgt`;");
			out.println("		$lgt = $temp[0];");
			out.println("		fprint $fileId (\"lig: \"+$lgt+\"\\n\");");
			out.println("		$temp = `lightlink -q -shp false -sets false -light $lgt`;");
			out.println("		string $link;");
			out.println("		for ($link in $temp){");
			out.println("			fprint $fileId (\"obj: \"+$link+\"\\n\");");
			out.println("		}");
			out.println("	}");
			out.println("	fclose $fileId;");

			/*out.println("	select `ls -type light`;");
			out.println("	select -add `ls -type mentalrayIblShape`;");

			out.println("	file -type \"mayaAscii\" -namespace \"lgt\" -options \"v=0\" -er \"$WORKING"
					+ lightRigName + "\";");
			out.println("	file -save;");*/
			out.println("}");


			out.println("string $refs[] = `file -q -r`;");
			out.println("string $ref;");
			out.println("for ($ref in $refs) {");
			out.println("	file -rr $ref;");
			out.println("}");

			out.println("cleanUpScene 3;");
//			out.println("string $cams[] = `ls -type camera`;");
//			out.println("$cams = `pickWalk -d up $cams`;");
			
			out.println("string $camList[] = `ls -l -type camera`;");
			out.println("string $cams[] = `pickWalk -d up $camList`;");
			out.println("select -cl;");
			out.println("for($cam in $cams){");
			out.println("	string $rels[] = `listRelatives -pa -c -type \"light\" $cam`;");
//			out.println("	/*for($rel in $rels){");
//			out.println("		print($cam + \" is parent of light \"+$rel+\"\n\");\");");
//			out.println("	}*/");
			out.println("	if(size($rels)==0){");
			out.println("		print(\"Deleting camera \"+$cam+\"\\n\");");
			out.println("		catch(`delete $cam`);");
			out.println("	}");
			out.println("}");
			
//			out.println("catch(`delete $cams`);");

			out.println("file -rn \"" + workingArea + lightRigName + "\";");
			out.println("file -f -save -options \"v=0\" -type \"mayaAscii\";");

			out.close();

			System.err.println("Wrote the mel file in "+eScript.getAbsolutePath());
		} catch ( IOException ex )
		{
			throw new PipelineException("Unable to create the temporary MEL script used "
					+ "to do the light rig export!");
		}


		try
		{	
			NodeMod targetMod = pSelected.get(pPrimary).getDetails().getWorkingVersion();
			NodeID targetID = new NodeID(pUser,pView,pPrimary);

			if(targetMod == null)
				throw new PipelineException("No working version of the Target" +
						" Scene Node ("	+ pPrimary + ") exists in the (" + pView + 
						") working area owned by ("	+ PackageInfo.sUser+ ")!");

			/*Get path*/
			FileSeq fseq = targetMod.getPrimarySequence();
			suffix = fseq.getFilePattern().getSuffix();
			if(!fseq.isSingle() || (suffix == null)
					|| (!suffix.equals("ma") && !suffix.equals("mb")))
				throw new PipelineException("The target node (" + pPrimary
						+ ") must be a maya scene!");

			Path targetPath =
				new Path(PackageInfo.sProdPath, targetID.getWorkingParent() + "/" + 
						fseq.getFile(0));

			System.err.println("Going to run maya.");

			ArrayList<String> args = new ArrayList<String>();
			args.add("-batch");
			args.add("-script");
			args.add(eScript.getAbsolutePath());
			args.add("-file");
			args.add(targetPath.toOsString());

			Path wdir =
				new Path(PackageInfo.sProdPath.toOsString() + 
						targetID.getWorkingParent());
			TreeMap<String, String> env =
				mclient.getToolsetEnvironment(pUser, pView, targetMod.getToolset(),
						PackageInfo.sOsType);
			Map<String, String> nenv = env;
			String midefs = env.get("PIPELINE_MI_SHADER_PATH");
			if ( midefs != null )
			{
				nenv = new TreeMap<String, String>(env);
				Path dpath = new Path(new Path(wdir, midefs));
				nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toOsString());
			}

			String command = "maya";
			if ( PackageInfo.sOsType.equals(OsType.Windows) )
				command += ".exe";

			System.err.println("Set args to "+ targetPath.toOsString() + " and " + eScript.getAbsolutePath());

			SubProcessLight proc = new SubProcessLight("PropagateLightRigTool", command, args,
					env, wdir.toFile());
			try
			{
				proc.start();
				proc.join();
				if ( !proc.wasSuccessful() )
				{
					throw new PipelineException(
							"Did not correctly export the light rig due to a maya error.!\n\n"
							+ proc.getStdOut() + "\n\n" + proc.getStdErr());
				}
			} catch ( InterruptedException ex )
			{
				throw new PipelineException(ex);
			}
		} catch ( Exception ex )
		{
			throw new PipelineException(ex);
		}



		{
			System.err.println("Looking for the hdr file to link up in Pipeline.");
			try {
				BufferedReader br = new BufferedReader(new FileReader(linkFile));
				String cur = null;
				while((cur=br.readLine())!=null){
					if(cur.startsWith("hdr: ")) {
						hdrLoc = cur.replaceAll("hdr: ", "");
						break;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.err.println("The hdr is "+hdrLoc);

		}

		{
			NodeMod turnMod = null;

			if(!doesNodeExists(mclient, lightRigName)){
				turnMod = registerNode(mclient, lightRigName,"ma", 
						mclient.getEditorForSuffix("ma"));
			}else{
				turnMod = mclient.getWorkingVersion(pUser, pView, lightRigName);
			}
			mclient.link(pUser, pView, pPrimary, turnMod.getName(), 
					LinkPolicy.Reference, LinkRelationship.All, null);

			if(hdrLoc!=null)
			{
				if(doesNodeExists(mclient, hdrLoc)){

					NodeMod hdrMod = null;
					try{
						hdrMod = mclient.getWorkingVersion(pUser, pView, hdrLoc);
					} catch (PipelineException e){
						jcheckOut(mclient, pUser, pView, hdrLoc, null, CheckOutMode.OverwriteAll, 
								CheckOutMethod.AllFrozen);
						hdrMod = mclient.getWorkingVersion(pUser, pView, hdrLoc);
					}
					mclient.link(pUser, pView, turnMod.getName(), hdrMod.getName(), 
							LinkPolicy.Reference, LinkRelationship.All, null);
				} else { 
					JToolDialog tool = new JToolDialog("Light Share", new JPanel(), "Continue");
					JConfirmDialog dialog = new JConfirmDialog(tool,
							"This is to let you know that you are using an HDRI," +
							hdrLoc + ", that is not in the Pipeline library. Nothing's broken, but you " +
					"may want to add that HDRI to the library eventually.");
					dialog.setVisible(true);

					hdrLoc = null;
				}
			}

		}

		Path lgtPath = new Path(pPrimary);
		String seqDir = lgtPath.getParentPath().getParentPath().getParent();
		ArrayList<String> shotList = getChildrenDirs(mclient, seqDir);

		for(String shot: shotList){
			//System.err.println(seqDir+":"+shot);
			ArrayList<String> lgtNodes = getChildrenNodes(mclient, seqDir+"/"+shot+"/lgt");
			for(String node: lgtNodes)
				if(node.endsWith("_lgt")){
					seqLgtNodes.add(seqDir+"/"+shot+"/lgt/"+node);
					//System.err.println(seqDir+"/"+shot+"/lgt/"+node);
				}
		}


		pPhase++;
		return true;	
	}//end executefirstPhase


	public synchronized boolean executeSecondPhase(MasterMgrClient mclient, 
			QueueMgrClient qclient)	throws PipelineException{

		File eScript;
		try
		{

			NodeID nodeID = new NodeID(pUser,pView, pPrimary);
			Path wdir = new Path(PackageInfo.sProdPath.toOsString());
			String workingArea = nodeID.getWorkingPath().toString().replaceAll(pPrimary,"");
			workingArea = wdir.toOsString()+workingArea;
			System.err.println("Mel: "+workingArea);

			eScript = File.createTempFile("PropagateLightRigTool-Import.", ".mel",
					PackageInfo.sTempPath.toFile());
			FileCleaner.add(eScript);


			System.err.println("Created the mel and text files in preparation for writing.");

			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(eScript)));

			out.println("{");
			out.println("file -import -type \"mayaAscii\" -options \"v=0;p=17\" -pr \"$WORKING"
					+ lightRigName + "." + suffix +"\"; ");
			out.println("file -save;");
			out.println("}");
			out.close();

		}catch(IOException io){
			throw new PipelineException("Unable to create the temporary MEL script used "
					+ "to do the light rig import!");
		}

		String error = null;

		for(String shot: shotsToLight){
			System.err.println("Going to light "+shot);

			try
			{	
				NodeMod targetMod = null;
				try{
					targetMod = mclient.getWorkingVersion(pUser, pView, shot);
				} catch (PipelineException e){
					jcheckOut(mclient, pUser, pView, shot, null, CheckOutMode.KeepModified, 
							CheckOutMethod.PreserveFrozen);
					targetMod = mclient.getWorkingVersion(pUser, pView, shot);
				}

				NodeID targetID = new NodeID(pUser,pView,shot);

				if(targetMod == null)
					throw new PipelineException("No working version of the Target" +
							" Scene Node ("	+ shot + ") exists in the (" + pView + 
							") working area owned by ("	+ PackageInfo.sUser+ ")!");

				/*Get path*/
				FileSeq fseq = targetMod.getPrimarySequence();
				String suffix = fseq.getFilePattern().getSuffix();
				if(!fseq.isSingle() || (suffix == null)
						|| (!suffix.equals("ma") && !suffix.equals("mb")))
					throw new PipelineException("The target node (" + shot
							+ ") must be a maya scene!");

				Path targetPath =
					new Path(PackageInfo.sProdPath, targetID.getWorkingParent() + "/" + 
							fseq.getFile(0));

				System.err.println("Going to run maya.");

				/*String cmd = "file -import -type \"mayaAscii\" -options \"v=0;p=17\" -pr \"$WORKING"
					+ lightRigName + "." + suffix +"\"; file -save;";*/
				ArrayList<String> args = new ArrayList<String>();
				args.add("-batch");
				args.add("-script");
				args.add(eScript.getAbsolutePath());
				args.add("-file");
				args.add(targetPath.toOsString());

				Path wdir =
					new Path(PackageInfo.sProdPath.toOsString() + 
							targetID.getWorkingParent());
				TreeMap<String, String> env =
					mclient.getToolsetEnvironment(pUser, pView, targetMod.getToolset(),
							PackageInfo.sOsType);
				Map<String, String> nenv = env;
				String midefs = env.get("PIPELINE_MI_SHADER_PATH");
				if ( midefs != null )
				{
					nenv = new TreeMap<String, String>(env);
					Path dpath = new Path(new Path(wdir, midefs));
					nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toOsString());
				}

				String command = "maya";
				if ( PackageInfo.sOsType.equals(OsType.Windows) )
					command += ".exe";

				SubProcessLight proc = new SubProcessLight("PropagateLightRigTool", command, args,
						env, wdir.toFile());
				try
				{
					proc.start();
					proc.join();
					//System.err.println(proc.getStdOut());
					if ( !proc.wasSuccessful() )
					{
						if(error==null)
							error = "";

						error += (shot + "\nDid not correctly import the light rig due to a maya error.!\n"
								+ proc.getStdOut() + "\n" + proc.getStdErr()+"\n\n");
						continue;
						/*throw new PipelineException(
								"Did not correctly import the light rig due to a maya error.!\n\n"
								+ proc.getStdOut() + "\n\n" + proc.getStdErr());*/
					}
				} catch ( InterruptedException ex )
				{
					throw new PipelineException(ex);
				}

				mclient.link(pUser, pView, shot, lightRigName, LinkPolicy.Reference, 
						LinkRelationship.All, null);
				pRoots.add(shot);
				if(hdrLoc!=null)
					mclient.link(pUser, pView, shot, hdrLoc, 
							LinkPolicy.Reference, LinkRelationship.All, null);
			} catch ( Exception ex )
			{
				throw new PipelineException(ex);
			}

			if(error!=null)
				throw new PipelineException(error);
		}
		return false;
	}



//	from Globals
	public boolean doesNodeExists(MasterMgrClient mclient, String name) throws PipelineException
	{
		TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
		comps.put(name, false);
		NodeTreeComp treeComps = mclient.updatePaths(pUser, pView, comps);
		State state = getState(treeComps, name);
		if ( state == null || state.equals(State.Branch) )
			return false;
		return true;
	}

//	from Globals
	private State getState(NodeTreeComp treeComps, String scene)
	{
		State toReturn = null;
		Path p = new Path(scene);
		NodeTreeComp dest = null;
		for (String s : p.getComponents())
		{
			if ( dest == null )
				dest = treeComps.get(s);
			else
				dest = dest.get(s);

			if ( dest == null )
				break;
		}
		if ( dest != null )
			toReturn = dest.getState();
		return toReturn;
	}

	public NodeMod registerNode(MasterMgrClient mclient, String name, 
			String extension,BaseEditor editor) throws PipelineException
			{
		File f = new File(name);
		FileSeq fSeq = new FileSeq(f.getName(), extension);
		NodeMod animNode = new NodeMod(name, fSeq, null, pToolset, editor);
		mclient.register(pUser, pView, animNode);
		return animNode;
			}//end registerNode

	/**
	 * Returns all the directories that are located directly underneath a given path.
	 * From Globals
	 * 
	 * @param w
	 * 	Wrapper class.
	 * @param start
	 * 	The path to start the search underneath
	 * @return
	 * @throws PipelineException
	 */
	public ArrayList<String> getChildrenDirs(MasterMgrClient mclient, String start)
	throws PipelineException
	{
		ArrayList<String> toReturn = new ArrayList<String>();
		TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
		comps.put(start, false);
		NodeTreeComp treeComps = mclient.updatePaths(pUser, pView, comps);
		Path p = new Path(start);
		ArrayList<String> parts = p.getComponents();
		for (String comp : parts)
		{
			if ( treeComps == null )
				break;
			treeComps = treeComps.get(comp);
		}
		if ( treeComps != null )
		{
			for (String s : treeComps.keySet())
			{
				NodeTreeComp comp = treeComps.get(s);
				if ( comp.getState() == NodeTreeComp.State.Branch )
					toReturn.add(s);
			}
		}
		return toReturn;
	}


	/**
	 * Returns all the paths that are located directly underneath a given path.
	 * From Globals
	 * @param w
	 * 	Wrapper class.
	 * @param start
	 * 	The path to start the search underneath
	 * @return				
	 * @throws PipelineException
	 */
	public ArrayList<String> getChildrenNodes(MasterMgrClient mclient, String start)
	throws PipelineException
	{
		ArrayList<String> toReturn = new ArrayList<String>();
		TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
		comps.put(start, false);
		NodeTreeComp treeComps = mclient.updatePaths(pUser, pView, comps);
		Path p = new Path(start);
		ArrayList<String> parts = p.getComponents();
		for (String comp : parts)
		{
			if(treeComps!=null)
				treeComps = treeComps.get(comp);
		}
		for (String s : treeComps.keySet())
		{
			toReturn.add(s);
		}
		return toReturn;
	}
	  private void jcheckOut(MasterMgrClient mclient, String user, String view, String name,
	      VersionID id, CheckOutMode mode, CheckOutMethod method) throws PipelineException
	  {
	    if (id == null)
	      id = mclient.getCheckedInVersionIDs(name).last();
	    if (id == null)
	      throw new PipelineException("BAD BAD BAD");
	    mclient.checkOut(user, view, name, id, mode, method);
	  }
}//end PropagateLightRigTool
