package com.sony.scea.pipeline.tools;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.plaf.synth.SynthLookAndFeel;

import com.sony.scea.pipeline.tools.SonyAsset.AssetType;

import us.temerity.pipeline.*;
import us.temerity.pipeline.bootstrap.BootApp;
import us.temerity.pipeline.laf.LookAndFeelLoader;
import us.temerity.pipeline.ui.*;

/**
 * This tool allows the user to replace assets on a shot-by-shot basis.
 * 
 * @author Ifedayo O. Ojomo
 * @version 1.0.0
 * 
 */
public class UpdateAssetGUI extends BootApp implements ActionListener
{

	MasterMgrClient mclient;
	PluginMgrClient plug;
	QueueMgrClient queue;
	LogMgr log;


	/*------------------------------------------------------------------------*/
	/*   INTERNAL VARIABLES                                                   */
	/*------------------------------------------------------------------------*/

	private Wrapper w;  
	private String shotPattern = ".*/production/.*/(anim|lgt)/.*";
	private String lgtPattern = ".*/production/.*/lgt/.*switchLgt"; 
	private String lr = "_lr";
	private String project;
	private boolean verbose = true;

	private Box top,list;
	private JDrawer test;
	private int sTSize = 100;
	private int sVSize = 200;
	private JToolDialog diag;   
	private JCollectionField userField;
	private JCollectionField viewField;
	private JCollectionField toolsetField;
	private JCollectionField projectField;

	private TreeMap<String, TreeSet<String>> areas;
	private TreeMap<String, String> propsList;
	private TreeMap<String, String> setsList;
	private TreeMap<String, String> charList;

	private TreeMap<String, LinkedList<JBooleanField>> pSubstituteFields;
	private TreeSet<String> potentialUpdates;
	
	private TreeMap<String, AssetInfo> pAssetManager;

	public final CheckOutMode over = CheckOutMode.OverwriteAll;
	public final CheckOutMode keep = CheckOutMode.KeepModified;
	public final CheckOutMethod modi = CheckOutMethod.Modifiable;
	public final CheckOutMethod froz = CheckOutMethod.AllFrozen;
	public final CheckOutMethod frozU = CheckOutMethod.FrozenUpstream;
	public final CheckOutMethod pFroz = CheckOutMethod.PreserveFrozen;



	/*------------------------------------------------------------------------*/
	/*   CONSTRUCTOR                                                          */
	/*------------------------------------------------------------------------*/


	/**
	 * 
	 */
	public UpdateAssetGUI()
	{
		try
		{
			PluginMgrClient.init();
			mclient = new MasterMgrClient();
			queue = new QueueMgrClient();
			plug = PluginMgrClient.getInstance();
			log = LogMgr.getInstance();

			pAssetManager = new TreeMap<String, AssetInfo>();

			project = "lr";
			charList = new TreeMap<String, String>();
			setsList = new TreeMap<String, String>();
			propsList = new TreeMap<String, String>();

			potentialUpdates = new TreeSet<String>();
			pSubstituteFields = new TreeMap<String, LinkedList<JBooleanField>>();

			/* load the look-and-feel */
			{
				try
				{
					SynthLookAndFeel synth = new SynthLookAndFeel();
					synth.load(LookAndFeelLoader.class.getResourceAsStream("synth.xml"),
							LookAndFeelLoader.class);
					UIManager.setLookAndFeel(synth);
				} catch ( java.text.ParseException ex )
				{
					log.log(LogMgr.Kind.Ops, LogMgr.Level.Severe,
							"Unable to parse the look-and-feel XML file (synth.xml):\n" + "  "
							+ ex.getMessage());
					System.exit(1);
				} catch ( UnsupportedLookAndFeelException ex )
				{
					log.log(LogMgr.Kind.Ops, LogMgr.Level.Severe,
							"Unable to load the Pipeline look-and-feel:\n" + "  " + ex.getMessage());
					System.exit(1);
				}
			}

			/* application wide UI settings */
			{
				JPopupMenu.setDefaultLightWeightPopupEnabled(false);
				ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
			}
		} catch ( PipelineException ex ) {
			ex.printStackTrace();
		}//end try/catch

	}//end constructor



	/*------------------------------------------------------------------------*/
	/*   HELPER METHODS                                                       */
	/*------------------------------------------------------------------------*/ 


	/**
	 * 
	 * @param type
	 * @return
	 */
	private Collection<String> getCorrectList(String type){
		if(type.equals("prop")){
			return propsList.keySet();
		}else if(type.equals("set")){
			return setsList.keySet();
		}else {
			return charList.keySet();
		}
	}    

	@SuppressWarnings("unused")
	private void log(String s)
	{
		if ( verbose )
		{
			System.err.print(s);
		}
	}//end log(String)

	private void logLine(String s)
	{
		if ( verbose  )
		{
			System.err.println(s);
			//log.logAndFlush(LogMgr.Kind.Ops, LogMgr.Level.Fine, s);
		}
	}//end logLine(String)

	private String getShortName(String s){
		Path sPath = new Path(s);
		return sPath.getName();
	}
	
	private String getShortAssetName(String s){
		Path sPath = new Path(s);
		return sPath.getName();
		//return s.replaceAll(".*/assets/.*/","");
	}//getShortAssetName(String)

	private String getShortShotName(String s){
		return s.replaceAll(".*/production/.*/(anim|lgt)/", "");
	}//getShortShotName(String)

	private TreeMap<String, TreeSet<String>> convertListToShotBased()
	{
		TreeMap<String, TreeSet<String>> toReturn = new TreeMap<String, TreeSet<String>>();
		for(String asset: pAssetManager.keySet()){
			TreeMap<String, String > shots = pAssetManager.get(asset).getLoHiResShots();
			System.err.println(shots);
			for(String loResShot: shots.keySet()) {
				if(!toReturn.containsKey(loResShot))
					toReturn.put(loResShot, new TreeSet<String>());
				toReturn.get(loResShot).add(asset);
				
				if(shots.get(loResShot)==null){
					logLine("There is no hi-res node for the anim node: "+loResShot);
					continue;
				}

				if(!toReturn.containsKey(shots.get(loResShot)))
					toReturn.put(shots.get(loResShot), new TreeSet<String>());
				String hrAsset = asset.replace(lr,"");
				toReturn.get(shots.get(loResShot)).add(hrAsset);
			}//end for
		}//end for
		return toReturn;
	}//end convertListToShotBased()

	/**
	 * @return The path of a node
	 */
	private Path getNodePath(String nodeName) throws PipelineException{

		NodeID targetID = new NodeID(w.user,w.view,nodeName);
		NodeStatus targetStat = mclient.status(targetID);
		NodeMod targetMod = targetStat.getDetails().getWorkingVersion();
		if(targetMod == null)
			throw new PipelineException("No working version of the Target Scene Node (" 
					+ nodeName + ") exists " + "in the (" + w.view + ") working area owned by (" 
					+ PackageInfo.sUser+ ")!");

		Path targetPath;

		FileSeq fseq = targetMod.getPrimarySequence();
		String suffix = fseq.getFilePattern().getSuffix();
		if(!fseq.isSingle() || (suffix == null)
				|| (!suffix.equals("ma") && !suffix.equals("mb")))
			throw new PipelineException("The target node (" + nodeName
					+ ") must be a maya scene!");
		targetPath =
			new Path(PackageInfo.sProdPath, targetID.getWorkingParent() + "/" + 
					fseq.getFile(0));

		//System.err.println("$WORKING"+nodeName+"."+suffix);
		return targetPath;
	}//end getNodePath(String)



	/**
	 * Using a list of shots and the assets to be changed per shot, gets each shot 
	 * node and modifies the references as needed.
	 *
	 */
	private void processNodes() throws PipelineException
	{      
		/*each asset, its replacement and the lo-res versions*/
		for(String asset: pAssetManager.keySet()){
			String newAsset = pAssetManager.get(asset).getNewAsset();
			String hrAsset = asset.replace(lr,"");
			String newHrAsset = newAsset.replace(lr,"");
			logLine("Checking out nodes:\n\t"+asset+"\n\t"
					+hrAsset+"\n\t"+newAsset+"\n\t"+newHrAsset);

			mclient.checkOut(w.user, w.view, asset, null, over, froz);
			mclient.checkOut(w.user, w.view, newAsset, null, over, froz);
			mclient.checkOut(w.user, w.view, hrAsset, null, over, froz);
			mclient.checkOut(w.user, w.view, newHrAsset, null, over, froz);
		}//end for

		TreeMap<String, TreeSet<String>> shotBased = convertListToShotBased();
		TreeMap<String, String> nameMap = SonyConstants.getCustomNamespaces(project);
		TreeSet<String> oldRef = new TreeSet<String>();
		TreeSet<String> newRef = new TreeSet<String>();
		
		for(String shot: shotBased.keySet()){
			//check out the shot

			if(shot.endsWith("anim"))
				mclient.checkOut(w.user, w.view, shot, null, keep, frozU);
			else {
				continue;
				//mclient.checkOut(w.user, w.view, shot, null, keep, pFroz);
			}
			logLine("Checking out: "+shot);
			
			NodeMod	targetMod = mclient.getWorkingVersion(w.user, w.view, shot);
			if(!shot.matches(lgtPattern)){

				if(targetMod.isActionEnabled()){
					System.err.println("Anim node with action enabled");
					FileSeq fseq = targetMod.getPrimarySequence();
					VersionID targetID = targetMod.getWorkingID();
					TreeMap<String, VersionID> files = new TreeMap<String, VersionID>();
					files.put(fseq.getFile(0).getPath(), targetID);
					mclient.revertFiles(w.user, w.view, shot, files);
					targetMod.setActionEnabled(false);
				}
				
			    w.mclient.modifyProperties(w.user, w.view, targetMod);
			}

			/*--checking the shot to be modified---*/

			for(String assetToReplace: shotBased.get(shot)){
				if(assetToReplace.endsWith(lr)){
					AssetInfo temp = pAssetManager.get(assetToReplace);
					oldRef.add(assetToReplace);
					newRef.add(temp.getNewAsset());
				} else {					
					String hiRes = assetToReplace.replace(lr,"");
					AssetInfo temp = pAssetManager.get(hiRes);
					if(temp==null)
						continue;
					oldRef.add(assetToReplace);
					newRef.add(temp.getNewAsset()+lr);
				}//end else
			}//end for

			if(oldRef.isEmpty()||newRef.isEmpty()){
				logLine("Shot "+shot+" somehow does not need any changes.");
				continue;
			}//end if

			editShotReferences(shot, targetMod, oldRef, newRef, nameMap);
		}//end for   

		/*for(String shot: shotBased.keySet()){
			//TODO: Check in nodes
			String msg = "Checked in with UpdateAssetGUI. Removed:\n\t";
			for(String asset: oldRef)
				msg+= (asset + " ");
			
			msg+="\nAdded:\n\t";
			for(String asset: newRef) 
				msg+= (asset + " ");
			
			mclient.checkIn(w.user, w.view,shot, msg, VersionID.Level.Major);
		}*/
		
	}//end processShots


	/**
	 * Updates the asset references for a shot within Maya and then in pipeline.
	 *  
	 * @param shotName The name of the shot being processed.
	 * @param pRemoveRef The list of assets being dereferenced from the shot.
	 * @param pReplaceRef The list of assets being referenced into the shot.
	 * @param nameMap 
	 */
	private void editShotReferences(String shotName, NodeMod targetMod, 
			TreeSet<String> pRemoveRef,	TreeSet<String> pReplaceRef, 
			TreeMap<String, String> nameMap) throws PipelineException
	{      
		logLine("Editing shot: "+shotName);
		boolean anim = !shotName.matches(lgtPattern);
		
		/* writing the mel script */
		if(anim){
			File script = null;
			try {
				script =
					File.createTempFile("UpdateAssetGUI.", ".mel", 
							PackageInfo.sTempPath.toFile());
				FileCleaner.add(script);
			}//end try
			catch(IOException ex) {
				throw new PipelineException(
						"Unable to create the temporary MEL script used to collect "
						+ "texture information from the Maya scene!");
			}//end catch

			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(new 
						FileWriter(script)));	    

				for(String asset : pReplaceRef) {
					String nameSpace;
					if(asset.endsWith(lr))
						nameSpace = nameMap.get(getShortName(asset.substring(0, asset.length()-3)));
					else {
						System.err.println("This should not be happening, a hi res asset in a lo-res node.");
						continue;
						//nameSpace = nameMap.get(getShortName(asset));
					}
					out.println("print (\"referencing file: $WORKING" + asset + ".ma\");");
					out.println("file -reference -namespace \"" + nameSpace + "\" \"$WORKING"
							+asset+".ma\";");
				}//end for

				for(String asset : pRemoveRef) {
					out.println("print (\"dereferencing file: $WORKING" + asset + ".ma\");");
					out.println("file -rr \"$WORKING"+asset+".ma\";");
				}//end for

				out.println("// SAVE");
				out.println("file -save;");

				out.close();
			}//end try
			catch(IOException ex) {
				throw new PipelineException("Unable to write the temporary MEL script(" + script
						+ ") used add the references!");
			}//end catch



			NodeID targetID = new NodeID(w.user,w.view,shotName);
			//NodeStatus targetStat = mclient.status(targetID);
			

			/* run Maya to collect the information */
			try {

				Path targetPath = getNodePath(shotName);
				ArrayList<String> args = new ArrayList<String>();
				args.add("-batch");
				args.add("-script");
				args.add(script.getPath());
				args.add("-file");
				args.add(targetPath.toOsString());

				Path wdir =
					new Path(PackageInfo.sProdPath.toOsString() + 
							targetID.getWorkingParent());

				TreeMap<String, String> env =
					mclient.getToolsetEnvironment(w.user, w.view, targetMod.getToolset(),
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
					new SubProcessLight("UpdateAssetGUI", command, args, env, 
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
		
		/*-edit the references in pipeline once they are done in the file-*/
		BaseAction targetAction = targetMod.getAction();
		for(String asset : pReplaceRef) {
			mclient.link(w.user, w.view, shotName, asset, LinkPolicy.Reference,
				LinkRelationship.All, null);			
			if(anim){
				/*Set the namespaces*/
				String nameSpace =
					nameMap.get(getShortName(asset.substring(0, asset.length()-3)));
				System.err.println(nameSpace);
				targetAction.initSourceParams(asset);
				targetAction.setSourceParamValue(asset, "PrefixName", nameSpace);
				targetMod.setAction(targetAction);
			}
		}
		w.mclient.modifyProperties(w.user, w.view, targetMod);
		
		for(String asset : pRemoveRef)
			mclient.unlink(w.user, w.view, shotName, asset);
		
		if(!anim){
			System.err.println("Queuing the switchLgt node "+shotName);
			mclient.submitJobs(w.user, w.view, shotName, null);
		}
	}//end editShotReferences 


	/**
	 * Present a GUI allowing the user to select shots in which each previously
	 * selected asset should be updated.
	 * 
	 * @return
	 * @throws PipelineException
	 */
	private String confirmShotsToUpdate() throws PipelineException
	{

		/* DO GUI DRAWING STUFF*/
		JScrollPane scroll = null;
		{
			Box ibox = new Box(BoxLayout.Y_AXIS);
			if(pAssetManager.isEmpty()) {
				Component comps[] = UIFactory.createTitledPanels();
				JPanel tpanel = (JPanel) comps[0];
				JPanel vpanel = (JPanel) comps[1];

				tpanel.add(Box.createRigidArea(new Dimension(sTSize - 7, 0)));
				vpanel.add(Box.createHorizontalGlue());

				ibox.add(comps[2]);
			} else {  

				for (String assetName : pAssetManager.keySet()) {
					String name = getShortName(assetName);
					AssetInfo info = pAssetManager.get(assetName);

					Component comps[] = UIFactory.createTitledPanels();
					JPanel tpanel = (JPanel) comps[0];
					JPanel vpanel = (JPanel) comps[1];
					String title = "Replace "+name+" with ";
					title+= getShortName(info.getNewAsset());

					JDrawer shotList = new JDrawer(title , (JComponent) comps[2], true);
					ibox.add(shotList);

					for(String shot: info.getLoHiResShots().keySet()){

						String shortShot = getShortName(shot);
						JBooleanField field = 
							UIFactory.createTitledBooleanField(tpanel, shortShot, sVSize, 
									vpanel, sTSize,"Whether to replace this asset source for the node.");
						field.setName(shot);
						field.setValue(true);

						if(!pSubstituteFields.containsKey(assetName))
							pSubstituteFields.put(assetName, new LinkedList<JBooleanField>());

						pSubstituteFields.get(assetName).add(field);
						UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
					}
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
		JToolDialog diag = new JToolDialog("Update Assets Per Shot", scroll,"Confirm");
		diag.setVisible(true);

		/* Process User Input */
		if(diag.wasConfirmed()) {

			for (String asset : pSubstituteFields.keySet()) {
				for(JBooleanField field : pSubstituteFields.get(asset)) {
					Boolean bUpdate = field.getValue();
					if((bUpdate == null) || !bUpdate) {
						pAssetManager.get(asset).getLoHiResShots().remove(field.getName());

						//logLine("\tRemoving: "+ getShortName(field.getName())
						//TODO		+" from list for "+ getShortName(asset));
					}
				}
			}
			return ": Modifying Nodes...";
		} 
		return null;
	}//end confirmShotsToUpdate



	/**
	 * Get list of each asset to be replaced, and the shots it's in.
	 *  
	 * @return
	 * @throws PipelineException
	 */
	private boolean getShotsUsingAssets() throws PipelineException
	{          
		ArrayList<ArchiveInfo> archive = mclient.archiveQuery(shotPattern , null);

		logLine("Looking for shots using lo-res assets.");
		for(ArchiveInfo curArc : archive){
			String name = curArc.getName();
			VersionID vid = curArc.getVersionID();
			TreeSet<VersionID> allVers = mclient.getCheckedInVersionIDs(name);
			if(!vid.equals(allVers.last()))
				continue;

			NodeVersion ver = mclient.getCheckedInVersion(name, vid);	 
			Set<String> srcs = ver.getSourceNames();

			for(String loResAsset: pAssetManager.keySet()){
				if(srcs.contains(loResAsset)){
					//TODO chec if latest.

					logLine("\t"+getShortName(loResAsset)+": "
							+ getShortName(name));

					AssetInfo tempInfo = pAssetManager.get(loResAsset);

					if(!tempInfo.getLoHiResShots().containsKey(name))
						tempInfo.getLoHiResShots().put(name, null);
				}//end if   
			}//end for
		}//end for

		logLine("Looking for shots using hi-res assets");
		/* - Populate lo-res */
		for(ArchiveInfo curArc : archive){
			String name = curArc.getName();
			VersionID vid = curArc.getVersionID();
			TreeSet<VersionID> allVers = mclient.getCheckedInVersionIDs(name);
			if(!vid.equals(allVers.last()))
				continue;
			
			NodeVersion ver = mclient.getCheckedInVersion(name, vid);
			Set<String> srcs = ver.getSourceNames();

			for(String updateAsset: pAssetManager.keySet()){
				String hiResAsset = updateAsset.replace(lr,"");
				if(srcs.contains(hiResAsset)){
					logLine("\t"+ getShortName(hiResAsset)+": "
							+getShortName(name));
					AssetInfo tempInfo = pAssetManager.get(updateAsset);

					String loRes = tempInfo.getMatchingLoResShot(name);
					if(loRes==null){
						logLine("!!!\nWARNING:"+getShortName(hiResAsset)+
								" is used in the " + getShortName(name) +
								" node which has no matching lo-res model in an anim node." +
								" So it will not be changed\n!!!");
						continue;
					}

					tempInfo.getLoHiResShots().put(loRes, name);
				}//end if
			}//end for
		}//end for(ArchiveInfo)

		logLine("");

		for(String updateAsset: potentialUpdates)
		{
			TreeMap<String,String> shots = pAssetManager.get(updateAsset).getLoHiResShots();
			if(shots.isEmpty()) {
				logLine(getShortName(updateAsset) + " is not used in any shots");
				pAssetManager.remove(updateAsset);
			}
			for(String loRes: shots.keySet()){
				if(shots.get(loRes)==null)
					logLine(getShortName(updateAsset) + " is in a hi-res shot, "
							+getShortName(loRes)+", but doesn't have a matching hi-res"
							+"model in the lgt node.");
			}
		}

		if(pAssetManager.isEmpty())
			return false;

		return true;
	}//end getShotsUsingAssets


	/**
	 * Draws the GUI that allows a user to select assets to be updated.
	 * 
	 * @return true if the user made a valid choice of assets to replace.
	 * @throws PipelineException
	 */
	private boolean buildUpdateGUI() throws PipelineException{
		Box finalBox = new Box(BoxLayout.Y_AXIS);
		top = new Box(BoxLayout.Y_AXIS);

		JScrollPane scroll;

		{
			scroll = new JScrollPane(finalBox);

			scroll
			.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

			Dimension size = new Dimension(sVSize + sVSize + sTSize + 52, 500);
			scroll.setMinimumSize(size);
			scroll.setPreferredSize(size);
			scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
		}

		/* query the user */
		diag = new JToolDialog("Propagate Asset", scroll, "Continue");

		areas = mclient.getWorkingAreas();
		{
			Box hbox = new Box(BoxLayout.X_AXIS);
			Component comps[] = UIFactory.createTitledPanels();
			JPanel tpanel = (JPanel) comps[0];
			JPanel vpanel = (JPanel) comps[1];
			{
				userField = UIFactory.createTitledCollectionField(tpanel, "User:", sTSize,
						vpanel, areas.keySet(), diag, sVSize,
				"The user whose area the node is being created in.");
				userField.setActionCommand("user");
				userField.setSelected(PackageInfo.sUser);
				userField.addActionListener(this);
			}
			UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
			{
				viewField = UIFactory.createTitledCollectionField(tpanel, "View:", sTSize,
						vpanel, areas.get(PackageInfo.sUser), diag, sVSize,
				"The working area to create the nodes in.");
				viewField.setActionCommand("wrap");
				viewField.addActionListener(this);
			}
			UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
			{

				toolsetField = UIFactory.createTitledCollectionField(tpanel, "Toolset:",
						sTSize, vpanel, mclient.getActiveToolsetNames(), diag, sVSize,
				"The toolset to set on all the nodes.");
				toolsetField.setSelected(mclient.getDefaultToolsetName());
				toolsetField.setActionCommand("wrap");
				toolsetField.addActionListener(this);
			}
			UIFactory.addVerticalSpacer(tpanel, vpanel, 3);


			w = new Wrapper(userField.getSelected(), viewField.getSelected(), toolsetField
					.getSelected(), mclient);

			charList = SonyConstants.getAssetList(w, project, AssetType.CHARACTER);
			setsList = SonyConstants.getAssetList(w, project, AssetType.SET);
			propsList = SonyConstants.getAssetList(w, project, AssetType.PROP);

			{
				projectField = UIFactory.createTitledCollectionField(tpanel, "Project:",
						sTSize, vpanel, Globals.getChildrenDirs(w, "/projects"), diag, sVSize,
				"All the projects in pipeline.");
				projectField.setActionCommand("proj");
				projectField.addActionListener(this);
			}
			hbox.add(comps[2]);
			top.add(hbox);
		}

		{
			Box vbox = new Box(BoxLayout.Y_AXIS);
			Box hbox = new Box(BoxLayout.X_AXIS);
			JButton button = new JButton("Propagate Additional Asset");
			button.setName("ValuePanelButton");
			button.setRolloverEnabled(false);
			button.setFocusable(false);
			Dimension d = new Dimension(sVSize, 25);
			button.setPreferredSize(d);
			button.setMinimumSize(d);
			button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

			vbox.add(Box.createRigidArea(new Dimension(0, 5)));
			hbox.add(button);
			hbox.add(Box.createRigidArea(new Dimension(4, 0)));
			vbox.add(hbox);
			vbox.add(Box.createRigidArea(new Dimension(0, 5)));

			button.setActionCommand("add");
			button.addActionListener(this);

			top.add(vbox);
		}      

		list = new Box(BoxLayout.Y_AXIS);
		test = new JDrawer("Propagate Additional Asset", list, false);

		top.add(test);
		list.add(assetChoiceBox());

		finalBox.add(top);

		{
			JPanel spanel = new JPanel();
			spanel.setName("Spacer");
			spanel.setMinimumSize(new Dimension(sTSize + sVSize, 7));
			spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
			spanel.setPreferredSize(new Dimension(sTSize + sVSize, 7));
			finalBox.add(spanel);
		}

		diag.setVisible(true);
		if ( diag.wasConfirmed() ){
			//get list of things to change.
			for(Component comp: list.getComponents()){
				if(comp instanceof Box){
					Box can = (Box) comp;
					JCollectionField oldOne = (JCollectionField) can.getComponent(2);
					JCollectionField newOne = (JCollectionField) can.getComponent(4);

					TreeMap<String, String> assetList = new TreeMap<String, String>();
					assetList.putAll(charList);
					assetList.putAll(propsList);
					assetList.putAll(setsList);

					String key = assetList.get(oldOne.getSelected()) + lr;
					String value = assetList.get(newOne.getSelected()) + lr;
					if(!key.equals(value)){
						potentialUpdates.add(key);	       
						pAssetManager.put(key, new AssetInfo(key,value));
					}
					//System.err.println("bUG: "+pAssetManager.get(key).getHiLoResShots());
				}
			}

			if(!pAssetManager.isEmpty())
				return true;
		}
		return false;
	}//end buildReplacementGUI


	public void actionPerformed(ActionEvent e)
	{
		String com = e.getActionCommand();
		if ( com.equals("user") )
		{
			String user1 = userField.getSelected();
			String view1 = viewField.getSelected();
			viewField.setValues(areas.get(user1));
			if ( areas.get(user1).contains(view1) )
				viewField.setSelected(view1);
			try
			{
				w = new Wrapper(user1, viewField.getSelected(), toolsetField.getSelected(),
						mclient);
			} catch ( PipelineException e1 )
			{
				e1.printStackTrace();
				System.exit(1);
			}
		} else if ( com.equals("wrap") )
		{
			try
			{
				w = new Wrapper(userField.getSelected(), viewField.getSelected(), 
						toolsetField.getSelected(), mclient);
			} catch ( PipelineException e1 )
			{
				e1.printStackTrace();
				System.exit(1);
			}
		} else if ( com.equals("proj") )
		{	 
			project = projectField.getSelected();
		} else if ( com.equals("add") )
		{
			list.add(assetChoiceBox());
			diag.validate();
		} else if ( com.equals("type") )
		{
			JCollectionField eventSrc = (JCollectionField) e.getSource();
			Collection<String> curList = getCorrectList(eventSrc.getSelected());
			Box can = (Box) eventSrc.getParent();
			JCollectionField oldList = ((JCollectionField) can.getComponent(2));
			oldList.setValues(curList);
			JCollectionField newList = (JCollectionField) can.getComponent(4);
			newList.setValues(curList);	 
		}
	}//end actionPerformed


	/**
	 * 
	 * @return A Box for selecting an asset type, the old asset and its replacement asset.
	 */
	private Box assetChoiceBox()
	{
		TreeSet<String> types = new TreeSet<String>();

		types.add(AssetType.CHARACTER.toString());
		types.add(AssetType.PROP.toString());
		types.add(AssetType.SET.toString());

		//JDrawer toReturn;
		Box hbox = new Box(BoxLayout.X_AXIS);
		{
			JCollectionField assetType = UIFactory.createCollectionField(types, diag, sTSize);
			assetType.setActionCommand("type");
			assetType.addActionListener(this);

			JCollectionField oldAsset 
			= UIFactory.createCollectionField(charList.keySet(), diag, sVSize);
			JCollectionField newAsset 
			= UIFactory.createCollectionField(charList.keySet(), diag, sVSize);
			hbox.add(assetType);
			hbox.add(Box.createHorizontalStrut(10));
			hbox.add(oldAsset);
			hbox.add(Box.createHorizontalStrut(5));
			hbox.add(newAsset);

			//pPotentials.put(oldAsset, newAsset);
		}
		list.add(Box.createVerticalStrut(5));

		return hbox;//toReturn;
	}//return assetChoiceBox


	/**
	 * @param args
	 */
	public void run(String[] arg0)
	{
		try
		{
			boolean chosen = buildUpdateGUI();
			if(!chosen)
				System.exit(0);

			if(getShotsUsingAssets())
				if(confirmShotsToUpdate()==null)
					System.exit(0);
			processNodes();
			System.err.println("DONE");
			System.exit(0);
		} catch ( PipelineException e )
		{
			e.printStackTrace();
		}

	}//end run(String)


	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		UpdateAssetGUI gui = new UpdateAssetGUI();
		try
		{
			boolean chosen = gui.buildUpdateGUI();
			if(!chosen)
				System.exit(0);

			if(gui.getShotsUsingAssets())
				if(gui.confirmShotsToUpdate()==null)
					System.exit(0);
			gui.processNodes();
			System.err.println("DONE");
			System.exit(0);
		} catch ( PipelineException e )
		{
			e.printStackTrace();
		}
	}//end main(args)


	/**
	 * Private inner class for storing information about each asset to be 
	 * replaced.
	 * 
	 * @author Ifedayo O. Ojomo
	 */
	private class AssetInfo{
		private String oldAsset;
		private String newAsset;      
		private TreeMap<String, String> loHiResShots;

		private AssetInfo(){}

		public void setLoHiResShots(TreeMap<String, String> name)
		{
			loHiResShots = name;	 
		}

		/**
		 * Returns the lo res shots that matched the lo-res shot passed in.
		 * 
		 * @param hiResName The name of the lo-res shot
		 * @return The name of the matching hi-res scene
		 */
		public String getMatchingLoResShot(String hiResName)
		{
			String shortHiRes = hiResName.replaceAll(".*/production/.*/(anim|lgt)/", "");
			shortHiRes = shortHiRes.replaceAll("switch.*", "");

			for(String loResName: loHiResShots.keySet()){
				String shortLoRes = loResName.replaceAll(".*/production/.*/(anim|lgt)/", "");
				if(shortLoRes.startsWith(shortHiRes)){
					//System.err.println("Matched: "+hiResName+ " with "+loResName );
					return loResName;
				}//end if
			}//end for			
			return null;
		}//end getMatchingLoResShot


		public AssetInfo(String oldOne, String newOne){
			oldAsset = oldOne;
			newAsset = newOne;
			loHiResShots = new TreeMap<String,String>();
		}

		/**
		 * @return the hiResShots
		 */
		public TreeMap<String,String> getLoHiResShots()
		{
			return loHiResShots;
		}

		/**
		 * @return the newAsset
		 */
		public String getNewAsset()
		{
			return newAsset;
		}

		/**
		 * @param newAsset the newAsset to set
		 */
		public void setNewAsset(String newAsset)
		{
			this.newAsset = newAsset;
		}

		/**
		 * @return the oldAsset
		 */
		public String getOldAsset()
		{
			return oldAsset;
		}

		/**
		 * @param oldAsset the oldAsset to set
		 */
		public void setOldAsset(String oldAsset)
		{
			this.oldAsset = oldAsset;
		}    
	}//end inner class AssetInfo   

}//end class UpdateAssetGUI