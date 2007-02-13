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
 * This tool allows the user to build a render tree for a selected 
 * lighting node.<P>
 * Only works with Maya Ascii files, since it directly reads the text of the Maya
 * scene to determine what passes to build.  The passes that are built is determined
 * by the existance of sets in the Maya scene.  If certain sets exist, then it causes 
 * the associated passes to be built.
 * <P> This tool builds the following passes every time:
 * <ul>
 * <li> Motion Vector (mv)
 * <li> Normal (nrml)
 * <li> Z-depth (z)
 * <li> Sky (sky)
 * <li> Global Color Pass (safety)
 * </ul>
 * This tool builds the following passes based on sets:
 * <ul><li> Character (ch)
 * <li> Character Matte (chMatte)
 * <li> Character Occlusion (chOcc)
 * <li> Foreground (fg)
 * <li> Midground (mg)
 * <li> Background (bg)
 * <li> Environment Occlusion (envOcc)
 * <li> Holdout Matte (hoMatte)
 * <li> Fog (fog)
 * <li> Shadow (shdw)
 * <li> Special (special)
 * </ul>
 * <p>
 * All the render settings for the passes are hard coded into this file, as are the paths.
 * They are all lair specific and this tool will not be useful on other projects without some
 * substanstial modification.
 *  
 * 
 * @author Ifedayo O. Ojomo
 * @author Jesse Clemens
 * @version 1.0.0
 * 
 */
public class BuildRenderTreeTool extends BaseTool{

	/*-----------------------------------------------*/
	/*             STATIC INTERNAL VARS              */
	/*-----------------------------------------------*/
	private static final long serialVersionUID = 9218740239757218172L;

	/*-----------------------------------------------*/
	/*                  INTERNALS                    */
	/*-----------------------------------------------*/

	/**
	 * list of all the layers to be built
	 */
	private TreeSet<String> pLayerNames;
	
	/**
	 * Hashed on the pass name.  Contains mel scripts that combine the render globals
	 * with the custom shader mels.  Only exists for nodes that match custRndrPattern.
	 */
	private TreeMap<String, NodeMod> custRndrMel;
	
	/**
	 * Hashed on the pass name.  Contains mel scripts to set all the shader information for
	 * a custom pass.  Only exists for nodes that match custRndrPattern.
	 */
	private TreeMap<String, NodeMod> custShdrMel;

	/**
	 * The image nodes, hashed on pass name.
	 */
	private TreeMap<String, NodeMod> pImgNodes;
	
	/**
	 * The mel script that is going to be linked to the images node.  Its value varies based
	 * on whether the node is one of the special ones that has it's own custRndrMel or one
	 * of the unspecial ones that uses the scripts found in the rndrPattern directory.
	 */
	private TreeMap<String, String> pRndrMel;	


	/**
	 * The current working area user|view|toolset.
	 */
	private String pUser;
	private String pView;
	private String pToolset;
	
	/**
	 * The passes that use the maya to mental ray rendering solution<br>
	 * chMatte<br>
	 * hoMatte<br>
	 * sky<br>
	 * z
	 **/
	private TreeSet<String> mayaMR;

	/**
	 * A string to search for 4 different passes.<br>
	 * mv<br>
	 * chOcc<br>
	 * envOcc<br>
	 * z<br>
	 */
	private final String specialPattern = "(mv|chOcc|envOcc|z)";
	/**
	 * Path to the directory where the render mels are located
	 *  /projects/lr/assets/tools/render/render-
	 */
	private final String rndrPattern = "/projects/lr/assets/tools/render/render-";
	
	private final String cleanAllMEL = "/projects/lr/assets/tools/render/sceaCleanAll";
	private final String basicCleanMEL = "/projects/lr/assets/tools/render/sceaBasicClean";
	
	/**
	 *  The render globals scene for all the shots. 
	 */
	private final String rndrGlob = rndrPattern + "settings";
	
	/**
	 *  RegExp that matches lighting shots. 
	 **/
	private final String lgtPattern = ".*/production/.*/lgt/.*_lgt";

	private int startFrame;
	private int endFrame;
	private int byFrame;
	private int pad;
	private String extension;

	/**
	 * This is just a temp file to write debug information to.
	 */ 
	private PrintWriter err = null;
	
	private PluginMgrClient plug;
	public static final CheckOutMode over = CheckOutMode.OverwriteAll;
	public static final CheckOutMethod froz = CheckOutMethod.AllFrozen;
	public static final LinkPolicy REF = LinkPolicy.Reference;
	public static final LinkPolicy DEP = LinkPolicy.Dependency;
	public static final LinkRelationship LINKALL = LinkRelationship.All;


	/**
	 * Builds the render tree for a selected lighting node.
	 */
	public BuildRenderTreeTool()
	{
		super("BuildRenderTree", new VersionID("1.0.0"), "SCEA",
		"Builds the render tree for a selected lighting node.");

		underDevelopment();

		addSupport(OsType.MacOS);
		addSupport(OsType.Windows);

		plug = PluginMgrClient.getInstance();
		err = null;
		
		pLayerNames = new TreeSet<String>();
		pImgNodes = new TreeMap<String, NodeMod>();
		pRndrMel = new TreeMap<String, String>();

		custShdrMel = new TreeMap<String, NodeMod>();
		custRndrMel = new TreeMap<String, NodeMod>();
		
		mayaMR = new TreeSet<String>();
		mayaMR.add("chMatte");
		mayaMR.add("hoMatte");
		mayaMR.add("sky");
		mayaMR.add("z");
	}//end constructor


	/**
	 * Check that the user has properly selected a target node for this tool
	 * <P>
	 * 
	 * @return The phase progress message or <CODE>null</CODE> to abort early.
	 * @throws PipelineException
	 *         If unable to validate the given user input.
	 */
	public synchronized String collectPhaseInput() throws PipelineException {

		if (pPrimary == null )
			throw new PipelineException("The primary selection must be the Target Node!");

		if(pSelected.size()!=1)
			throw new PipelineException("You can only run this tool on one node!");

		if(!pPrimary.matches(lgtPattern))
			throw new PipelineException("This tool will only work on a lgt node!");


		/* DO GUI DRAWING STUFF*/
		JScrollPane scroll = null;
		JIntegerField startField;
		JIntegerField endField;
		JIntegerField byField;
		JIntegerField padField;
		JTextField extField;
		{
			int twidth = 180;
			int vwidth = 80;
			Box hbox = new Box(BoxLayout.X_AXIS);
			Component comps[] = UIFactory.createTitledPanels();
			JPanel tpanel = (JPanel) comps[0];
			JPanel vpanel = (JPanel) comps[1];
			JDrawer drawer = new JDrawer("Frame Info:" , (JComponent) comps[2], true);
			hbox.add(drawer);
			{
				extField = UIFactory.createTitledTextField(tpanel, 
						"By Frame", twidth, vpanel, "iff", vwidth);
				startField = UIFactory.createTitledIntegerField(tpanel, 
						"Start Frame", twidth, vpanel, 1, vwidth);
				endField = UIFactory.createTitledIntegerField(tpanel, 
						"End Frame", twidth, vpanel, 2, vwidth);
				byField = UIFactory.createTitledIntegerField(tpanel, 
						"By Frame", twidth, vpanel, 1, vwidth);
				padField = UIFactory.createTitledIntegerField(tpanel, 
						"Frame Padding", twidth, vpanel, 4, vwidth);
			}

			{
				JPanel spanel = new JPanel();
				spanel.setName("Spacer");

				spanel.setMinimumSize(new Dimension(twidth + vwidth, 7));
				spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
				spanel.setPreferredSize(new Dimension(twidth + vwidth, 7));

				hbox.add(spanel);
			}

			{
				scroll = new JScrollPane(hbox);
				scroll
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				scroll
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

				Dimension size = new Dimension(twidth + vwidth, 140);
				scroll.setMinimumSize(size);
				scroll.setPreferredSize(size);

				scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
			}
		}

		/* query the user */
		{
			JToolDialog diag = new JToolDialog("What is the frame range?", scroll,
			"Confirm");      
			diag.setVisible(true);
			diag.setResizable(false);

			if(diag.wasConfirmed()){
				extension = (extField.getText()==null)? "iff" : extField.getText();
				startFrame = (startField.getValue()==null)? 1: startField.getValue();
				endFrame = (endField.getValue()==null)? 2 : endField.getValue();
				byFrame = (byField.getValue()==null)? 1 : byField.getValue();
				pad = (padField.getValue()==null)? 4 : padField.getValue();

			} else {
				return null;
			}
		}
		
		NodeStatus status = pSelected.get(pPrimary);
		NodeID nodeID = status.getNodeID();
		pUser = nodeID.getAuthor();
		pView = nodeID.getView();
		pToolset = status.getDetails().getWorkingVersion().getToolset();

		
		File errFile = null;
		
		try {
			errFile = File.createTempFile("BuildRenderTree", ".err", 
					PackageInfo.sTempPath.toFile());
			err = new PrintWriter(errFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		NodeMod mod = status.getDetails().getWorkingVersion();
		FileSeq fseq = mod.getPrimarySequence();
		Path script = new Path(PackageInfo.sProdPath,
				nodeID.getWorkingParent() + "/" + fseq.getPath(0));
		String strFile = script.toOsString();
		getLayerNames(strFile);
		
		if(pLayerNames.isEmpty()){
			return null;
		}
		pLayerNames.add("z");
		pLayerNames.add("nrml");
		pLayerNames.add("mv");
		pLayerNames.add("safety");

		return ": La-da-di-da-da-da";
	}//end collectPhaseInput


	/**
	 * Perform execution of the tool.
	 * <P>
	 * 
	 * @param mclient
	 *        The network connection to the plmaster(1) daemon.
	 * @param qclient
	 *        The network connection to the plqueuemgr(1) daemon.
	 * @return Whether to continue and collect user input for the next phase of
	 *         the tool.
	 * @throws PipelineException
	 *         If unable to sucessfully execute this phase of the tool.
	 */
	public synchronized boolean executePhase(MasterMgrClient mclient, 
		QueueMgrClient qclient) throws PipelineException 
	{	
		Path nPath = new Path(pPrimary);
		String lgtNodeName = nPath.getName();
		/**
		 * the image directory for the node.
		 */
		String folder = nPath.getParentPath().getParent() + "/img/";
		err.println("The folder is " + folder);

		String filePrefix = lgtNodeName.replaceAll("seq","s");
		filePrefix = filePrefix.replaceAll("lgt","");
		err.println("The prefix is " + filePrefix);

		createRenderNodes(mclient, folder, filePrefix);
		buildNetwork(mclient, folder, filePrefix);

		err.close();
		return false;
	}//end executePhase

	
	/**
	 * Go through the lighting scene file and get the the names of the layers created.
	 * 
	 * @param strFile The name of the lighting file.
	 */
	public void getLayerNames(String strFile)
	{
		try {
			BufferedReader br = new BufferedReader(new FileReader(strFile));

			String curLine;
			while((curLine = br.readLine())!=null) {
				if(curLine.startsWith("createNode objectSet")) {
					StringTokenizer strTok = new StringTokenizer(curLine," ");

					String token;
					while(strTok.hasMoreTokens()){
						token = strTok.nextToken();
						if(token.equals("-n")){
							String ref = strTok.nextToken();

							ref = ref.replace("\"", "");
							ref = ref.replace(";", "");
							err.println("Found a node with value " + ref);

							if(ref.matches("CH[1-7]")){
								pLayerNames.add("ch");
								pLayerNames.add("chMatte");
								pLayerNames.add("chOcc");
								pLayerNames.add("hoMatte");
							} else if(ref.matches("FG")) {
								pLayerNames.add("fg");	
								pLayerNames.add("envOcc");
							} else if(ref.matches("MG")) {
								pLayerNames.add("mg");
								pLayerNames.add("envOcc");
							} else if(ref.matches("BG")) {
								pLayerNames.add("bg");
								pLayerNames.add("envOcc");
							} else if(ref.matches("SHADOW")) {
								pLayerNames.add("shdw");
							} else if(ref.matches("SKY")) {
								pLayerNames.add("sky");
							} else if(ref.matches("FOG[1-4]")){
								pLayerNames.add("fog");
							} else if(ref.matches("SPECIAL")) {
							  pLayerNames.add("special");
							}
						}
					}//end while
				}//end if
			}//end while

			br.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}//end catch		
	}//end getLayerNames


	/**
	 * Build the render network based on the layers found in the lighting file.
	 * 
	 * @param mclient
	 * @throws PipelineException
	 */
	private void buildNetwork(MasterMgrClient mclient, String folder, String filePrefix) 
	throws PipelineException {
		DoubleMap<String, String, TreeSet<VersionID>> plugs = mclient
		.getToolsetActionPlugins(pToolset);

		// not sure about what this if statement is checking.  I'm going
		// take it out and see what happens
		//if(!custShdrMel.isEmpty())
		jcheckOut(mclient,pUser, pView, rndrGlob, null, over, froz);

		for(String pass: pLayerNames){
			//link lgt node to the img
			err.println(pass);
			
			err.println("Trying to build layer "+pass);
			
			NodeMod imgMod = pImgNodes.get(pass);
			String rndrMel = pRndrMel.get(pass);

			mclient.link(pUser, pView, imgMod.getName(), pPrimary, DEP, LINKALL, null);
			mclient.link(pUser, pView, imgMod.getName(), rndrMel, DEP, LINKALL, null);

			{
				VersionID ver = null;
				BaseAction action = null;
				
				if(mayaMR.contains(pass)){
					ver = plugs.get("Temerity", "MayaRender").last();
					action = plug.newAction("MayaRender", ver, "Temerity");
					action.setSingleParamValue("PreRenderMEL", rndrMel);
					action.setSingleParamValue("Renderer", "Mental Ray");
					action.setSingleParamValue("Processors", 4);
				} else if(pass.equals("fog")) {
					ver = plugs.get("Temerity", "MayaRender").last();
					action = plug.newAction("MayaRender", ver, "Temerity");
					action.setSingleParamValue("PreRenderMEL", rndrMel);
					action.setSingleParamValue("Renderer", "Software");
					action.setSingleParamValue("Processors", 4);
				} else {
					ver = plugs.get("Temerity", "MayaMRayRender").last();
					action = plug.newAction("MayaMRayRender", ver, "Temerity");
					action.setSingleParamValue("PreExportMEL", rndrMel);
				}
				action.setSingleParamValue("MayaScene", pPrimary);
				imgMod.setAction(action);
				
				err.println("Built and set action for img sequence node "+imgMod.getName());

				JobReqs jreqs = imgMod.getJobRequirements();
				jreqs.setMaxLoad(2.5F);
				
				if(mayaMR.contains(pass)){
					jreqs.addSelectionKey("Maya2MR");
				} else if(!pass.equals("fog")) {
					jreqs.addLicenseKey("MentalRay");
					jreqs.addSelectionKey("MentalRay");
					jreqs.addSelectionKey("Layers");
					jreqs.setMaxLoad(4.5F);
				} 
				jreqs.addSelectionKey("Lair");
				jreqs.addSelectionKey("Lighting");
				jreqs.addSelectionKey("LinuxOnly");
				jreqs.setRampUp(15);
				jreqs.setMinDisk(536870912L);
				jreqs.setMinMemory(3221225472L);
				imgMod.setJobRequirements(jreqs);

				err.println("About to set the exec method.");
				imgMod.setExecutionMethod(ExecutionMethod.Parallel);
				imgMod.setBatchSize(3);
				err.println("it worked");
				mclient.modifyProperties(pUser, pView, imgMod);

				err.println("Connecting lgt node and "+rndrMel+" to the img node, "
						+imgMod.getName()+" with the action "+action);
			}

			if(pass.matches(specialPattern)){
				
				err.println("This pass is special and will need a mel node with an action");
				//connect the renderSettings and the shdrApplication mel to the render MEL.
				NodeMod rndrMod = custRndrMel.get(pass);
				NodeMod shdrMod = custShdrMel.get(pass);

				String shdrMel = shdrMod.getName();

				mclient.link(pUser, pView, rndrMel, rndrGlob, DEP, LINKALL, null);
				mclient.link(pUser, pView, rndrMel, shdrMel, DEP, LINKALL, null);
				mclient.link(pUser, pView, rndrMel, cleanAllMEL, DEP, LINKALL, null);
				{
					//create the Action for the shader mel.
					err.print("Creating the action for the pass: ");
					BaseAction shdrAction = null;
					if(pass.equals("mv")){
						VersionID vid = plugs.get("SCEA","MayaMVPass").last();
						shdrAction = plug.newAction("MayaMVPass", vid, "SCEA");
						err.println("mv");
					} else if(pass.equals("z")){
						VersionID vid = plugs.get("SCEA","MayaZPass").last();
						shdrAction = plug.newAction("MayaZPass", vid, "SCEA");
						err.println("z");
					} else if(pass.equals("chOcc")){					
						VersionID vid = plugs.get("SCEA","MayaOccPass").last();
						shdrAction = plug.newAction("MayaOccPass", vid, "SCEA");
						shdrAction.setSingleParamValue("RenderType", "Ch Occ");
						err.println("chOcc");
					} else if(pass.equals("envOcc")){					
						VersionID vid = plugs.get("SCEA","MayaOccPass").last();
						shdrAction = plug.newAction("MayaOccPass", vid, "SCEA");
						shdrAction.setSingleParamValue("RenderType", "Env Occ");
						shdrAction.setSingleParamValue("MaxDistance", 400.0d);
						err.println("envOcc");
					}
					shdrMod.setAction(shdrAction);
					JobReqs jreqs = shdrMod.getJobRequirements();
					jreqs.addSelectionKey("LinuxOnly");
					shdrMod.setJobRequirements(jreqs);
					mclient.modifyProperties(pUser, pView, shdrMod);
				}
				
				{
					err.println("Setting up the cat action.");
					VersionID catVer = plugs.get("Temerity", "CatFiles").last();
					BaseAction catAction = plug.newAction("CatFiles", catVer, "Temerity");
					catAction.initSourceParams(rndrGlob);
					catAction.setSourceParamValue(rndrGlob, "Order", 100);				
					catAction.initSourceParams(shdrMel);
					catAction.setSourceParamValue(shdrMel, "Order", 150);
					catAction.initSourceParams(cleanAllMEL);
					catAction.setSourceParamValue(cleanAllMEL, "Order", 200);
					rndrMod.setAction(catAction);

					JobReqs jreqs = rndrMod.getJobRequirements();
					jreqs.addSelectionKey("LinuxOnly");
					rndrMod.setJobRequirements(jreqs);
					mclient.modifyProperties(pUser, pView, rndrMod);
				}

				err.println("Done creating the cat node");
			} 
		}		
	}//end buildNetwork


	/**
	 * Builds the image nodes for all the passes and the mel scripts that are going to be used
	 * control the rendering.
	 * 
	 * @param mclient
	 * @param folder
	 * 	the top level image directory for the node.
	 * @param filePrefix
	 * 	The prefix for the image nodes.  in the format s(seqname)_(shotName)
	 * @throws PipelineException
	 */
	private void createRenderNodes(MasterMgrClient mclient, String folder, 
			String filePrefix) throws PipelineException 
	{		
		BaseEditor editor = mclient.getEditorForSuffix(extension);
		BaseEditor melEditor = mclient.getEditorForSuffix("mel");
		TreeSet<String> curPasses = new TreeSet<String>(pLayerNames);
		
		for(String pass: curPasses){
		  /**
		   * The name of the image node for the specified pass.
		   */
			String name = folder + pass + "/" + filePrefix + pass;
			err.println(name);
			if(doesNodeExists(mclient, name)){
				pLayerNames.remove(pass);
				continue;
			}
			
			NodeMod nMod = registerSequence(mclient, name, pad, 
					extension, editor, startFrame, endFrame, byFrame);
			pRoots.add(nMod.getName());
			pImgNodes.put(pass, nMod);
			
			jcheckOut(mclient,pUser, pView, basicCleanMEL, null, over, froz);
			jcheckOut(mclient,pUser, pView, cleanAllMEL, null, over, froz);

			if(pass.matches(specialPattern)){
				NodeMod shdrMod = registerNode(mclient, name+"-shdr",
						"mel", melEditor);

				NodeMod rndrMod = registerNode(mclient, name+"-rndr",
						"mel", melEditor);

				custRndrMel.put(pass, rndrMod);
				custShdrMel.put(pass, shdrMod);				

				pRndrMel.put(pass, rndrMod.getName());
			} else {
				jcheckOut(mclient,pUser, pView, rndrPattern+pass, null, over, froz);
				err.println("Checking out: " + rndrPattern+pass);
				pRndrMel.put(pass, rndrPattern+pass );
			}//else
		}//end for
		

	}//end createRenderNodes


	public NodeMod registerNode(MasterMgrClient mclient, String name, 
			String ext, BaseEditor editor) throws PipelineException
	{
		File f = new File(name);
		FileSeq fSeq = new FileSeq(f.getName(), ext);
		NodeMod animNode = new NodeMod(name, fSeq, null, pToolset, editor);
		mclient.register(pUser, pView, animNode);
		return animNode;
	}//end registerNode

	
	public NodeMod registerSequence(MasterMgrClient mclient, String name, int pad, 
			String extention, BaseEditor editor, int startF, int endf, int byF) 
	throws PipelineException
	{
		Path p = new Path(name);
		FilePattern pat = new FilePattern(p.getName(), pad, extention);
		FrameRange range = new FrameRange(startF, endf, byF);
		FileSeq animSeq = new FileSeq(pat, range);
		NodeMod animNode = new NodeMod(name, animSeq, null, pToolset, editor);
		mclient.register(pUser, pView, animNode);
		return animNode;
	}//end registerSequence

	
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
			treeComps = treeComps.get(comp);
		}
		for (String s : treeComps.keySet())
		{
			toReturn.add(s);
		}
		return toReturn;
	}//end getChildrenNodes
	
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
	
	private static State getState(NodeTreeComp treeComps, String scene)
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
	
	  private void jcheckOut(MasterMgrClient mclient, String user, String view, String name,
	      VersionID id, CheckOutMode mode, CheckOutMethod method) throws PipelineException
	  {
	    if (id == null)
	      id = mclient.getCheckedInVersionIDs(name).last();
	    if (id == null)
	      throw new PipelineException("BAD BAD BAD");
	    mclient.checkOut(user, view, name, id, mode, method);
	  }

}//end class
