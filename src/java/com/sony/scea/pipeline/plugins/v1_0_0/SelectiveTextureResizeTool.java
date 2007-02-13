package com.sony.scea.pipeline.plugins.v1_0_0;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;

import us.temerity.pipeline.*;
import us.temerity.pipeline.NodeTreeComp.State;
import us.temerity.pipeline.ui.JCollectionField;
import us.temerity.pipeline.ui.JDrawer;
import us.temerity.pipeline.ui.JToolDialog;
import us.temerity.pipeline.ui.UIFactory;

/**
 * This tool was never finished and was is largely untested.
 * <p>
 * The goal of the tool was to allow users to resize certain textures
 * on a per-shot basis.  It was pointed out that this somewhat defeats the 
 * entire purpose of using pyramdial map files, so development was never
 * finished.  It would be a very useful tool on a production that was not using
 * pyramidal files, allowing for per-shot LOD on textures.  
 */
public class SelectiveTextureResizeTool extends BaseTool {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5676498185274157649L;
	
	private String pUser;
	private String pView;

	private static String lgtPattern = ".*/production/.*/lgt/.*_lgt";
	private static String switchPattern = ".*/production/.*/lgt/.*_switch(Lgt)";
	private static String assetPattern = ".*/assets/(character|set|prop)/.*/[a-zA-Z0-9]+";

	private static String twoFiveSix = "256m";
	private static String fiveTwelve = "512m";
	private static String oneK = "1k";
	private static String twoK = "2k";
	private static String fourK = "4k";


	private TreeSet<String> pAssetList;
	private TreeMap<String, TreeSet<String>> pTextureList;
	private TreeMap<String, TreeMap<String,String>> pTextureChanges;
	private TreeMap<String, TreeMap<String,JCollectionField>> texChangeFields;

	private Box top;
	private int sTSize = 250;
	private int sVSize = 150;
	private JToolDialog diag;

	private ArrayList<String> textureSizes;

	private int pPhase;

	private PluginMgrClient plug;

	private String texNodeName;

	private String pToolset;

	public SelectiveTextureResizeTool(){
		super("SelectiveTextureResize", new VersionID("1.0.0"), "SCEA",
		"Allows textures to be resize on a shot-by-shot basis.");
		
		pAssetList = new TreeSet<String>();
		pTextureList = new TreeMap<String, TreeSet<String>>();
		texChangeFields = new TreeMap<String, TreeMap<String, JCollectionField>>();
		pTextureChanges = new TreeMap<String, TreeMap<String,String>>();

		textureSizes = new ArrayList<String>();

		pPhase = 1;
		
		plug = PluginMgrClient.getInstance();

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
			throw new IllegalStateException();
		}

	}


	public synchronized String collectFirstPhaseInput() throws PipelineException
	{
		if(pPrimary==null)
			throw new PipelineException("Please select something!");

		if(pSelected.size()!=1)
			throw new PipelineException("Only one node can be selected!");

		if(!pPrimary.matches(lgtPattern))
			throw new PipelineException("This tool only works on lgt nodes.");

		textureSizes.add(twoFiveSix);
		textureSizes.add(fiveTwelve);
		textureSizes.add(oneK);
		textureSizes.add(twoK);
		textureSizes.add(fourK);
		
		NodeStatus status = pSelected.get(pPrimary);
		NodeID nodeID = status.getNodeID();
		pUser = nodeID.getAuthor();
		pView = nodeID.getView();

	
		Path nPath = new Path(pPrimary);
		String lgtNodeName = nPath.getName();
		String shotPrefix = lgtNodeName.replaceAll("lgt","");
		texNodeName = nPath.getParent()+"/"+shotPrefix+"tex";	

		return "...The eensy weensy spider comes down the water spout.";
	}

	public synchronized String collectSecondPhaseInput() throws PipelineException
	{
		if(!buildGUI())
			return null;

		ArrayList<String> assetList = new ArrayList<String>(texChangeFields.keySet()); 
		
		for(String asset: assetList){
			TreeMap<String, JCollectionField> fields = texChangeFields.get(asset);
			TreeMap<String, String> changes = new TreeMap<String, String>();
			
			for(String texture: fields.keySet()){
				String res = fields.get(texture).getSelected();
				if(!res.equals(oneK)){
					Path p = new Path(texture);
					String newName = p.getParent()+"/"+res+"/"+p.getName();
					changes.put(texture, newName);
					System.err.println(newName);
				}
			}
			if(!changes.isEmpty())
				pTextureChanges.put(asset, changes);
		}

		return "...Just like a star";
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
		switch (pPhase) {
		case 1:
			return executeFirstPhase(mclient, qclient);
		case 2:
			return executeSecondPhase(mclient, qclient);
		default:
			return false;
		}

	}

	/*
	 * Get a list of all the textures used by an asset.
	 */
	public synchronized boolean executeFirstPhase(MasterMgrClient mclient, QueueMgrClient qclient)
	throws PipelineException{
		pToolset = mclient.getDefaultToolsetName();
		
		NodeMod lgtMod = mclient.getWorkingVersion(pUser, pView, pPrimary);
		TreeSet<String> srcs = new TreeSet<String>(lgtMod.getSourceNames());
		String switchName = null;
		for(String src: srcs){
			if(src.matches(switchPattern )){
				switchName = src;
				break;
			}
		}

		if(switchName==null)
			throw new PipelineException("This node does not have a switch(Lgt) node source!!");

		NodeMod switchMod = mclient.getWorkingVersion(pUser, pView, switchName);

		pAssetList.addAll(switchMod.getSourceNames());
		for(String src: switchMod.getSourceNames()){
			if(!src.matches(assetPattern))
				pAssetList.remove(src);
			else 
				pTextureList.put(src, new TreeSet<String>());
		}

		//System.err.println("The assets for "+ switchName+" are:\n"+ pAssetList.toString());

		for(String src: pAssetList){
			Path p = new Path(src);
			String assetFolder = p.getParent();
			if(p.getName().equals("burntMan"))
				continue;
			String texFolder = assetFolder+"/textures/map";
			//System.err.println(texFolder);
			ArrayList<String> maps = null;
			try {
				maps = getChildrenNodes(mclient, texFolder);
			}catch(NullPointerException e){
				e.printStackTrace();
				continue;
			}

			if(maps.isEmpty())
				pTextureList.remove(src);

			for(String file: maps){
				if(textureSizes.contains(file))
					continue;
				String fileName = texFolder+"/" +file;
				pTextureList.get(src).add(fileName);
			}
			//System.err.println("The maps for "+p.getName()+ " are:\n" + pTextureList.get(src));
		}

		pPhase++;
		return true;
	}

	/*
	 * Register new up/down res maps where necessary. Hook them up to a texture node for the light scene.
	 */
	public synchronized boolean executeSecondPhase(MasterMgrClient mclient, QueueMgrClient qclient)
	throws PipelineException{
		
		BaseEditor editor = mclient.getEditorForSuffix("tga");
		
		System.err.println("The shot tex node is "+ texNodeName);
		NodeMod texMod = null;
		if(!doesNodeExists(mclient,texNodeName)){
		    BaseEditor kwEditor = plug.newEditor("KWrite", null, "Temerity");
			texMod = registerNode(mclient, texNodeName, null, kwEditor);
			mclient.link(pUser, pView, pPrimary, texNodeName, LinkPolicy.Reference, LinkRelationship.All, null);
			
			BaseAction action = plug.newAction("List Sources", null, "Temerity");
			texMod.setAction(action);
			
			JobReqs jreqs = texMod.getJobRequirements();
			jreqs.addSelectionKey("Lair");
			texMod.setJobRequirements(jreqs);
			
			mclient.modifyProperties(pUser, pView, texMod);
		}
			
		if(texMod==null)
			texMod = mclient.getWorkingVersion(pUser, pView, texNodeName);
		
		Set<String> texSrcs = texMod.getSourceNames();
		
		Set<String> assetList = pTextureChanges.keySet();
		for(String asset: assetList){
			TreeMap<String,String> texChanges = pTextureChanges.get(asset);
			
			Path p = new Path(asset);
			String assetName = p.getName();
			String charTexNode = p.getParent() +"/textures/"+ assetName+"_tex";
			String matNode = p.getParent()+"/material/"+ assetName+"_mat";

			if(doesNodeExists(mclient, charTexNode)){
				mclient.checkOut(pUser, pView, charTexNode, null, CheckOutMode.KeepModified, CheckOutMethod.Modifiable);
				System.err.println("\n" + charTexNode + " exists");
			}
			
			TreeSet<String> texList = new TreeSet<String>(texChanges.keySet());
			for(String texture: texList){
				String newTex = texChanges.get(texture);		
				//System.err.println(texture + " will be " + newTex);
			
				if(!doesNodeExists(mclient,newTex)){
					registerMap(mclient, texture, newTex, charTexNode, editor);
				}
			
				if(!texSrcs.contains(newTex)){
					String shortTexName = (new Path(newTex)).getName();
					for(String src: texSrcs){
						String shortSrcName = (new Path(src)).getName();
						if(shortSrcName.matches(shortTexName)){
							System.err.println("Match!!! "+ shortSrcName);
							mclient.unlink(pUser, pView, texNodeName, src);
							texChanges.put(src,newTex);
							texChanges.remove(texture);
							break;
						}
					}
					mclient.link(pUser, pView, texNodeName, newTex, LinkPolicy.Dependency, 
							LinkRelationship.All, null);
				}
				else{
					System.err.println("This texture is already linked.");
				}
			}
			
			if(doesNodeExists(mclient, matNode)){
				mclient.checkOut(pUser, pView, matNode, null, CheckOutMode.KeepModified, CheckOutMethod.Modifiable);
				System.err.println("\n" + matNode + " exists");
			}
			if(doesNodeExists(mclient, matNode+"Exp")){
				mclient.checkOut(pUser, pView, matNode+"Exp", null, CheckOutMode.KeepModified, CheckOutMethod.Modifiable);
				System.err.println("\n" + matNode+"Exp" + " exists");
			}
			if(doesNodeExists(mclient, asset)){
				mclient.checkOut(pUser, pView, asset, null, CheckOutMode.KeepModified, CheckOutMethod.Modifiable);
				System.err.println("\n" + asset + " exists");
			}
			mclient.submitJobs(pUser, pView, asset, null);
		}
		
		mclient.submitJobs(pUser, pView, texNodeName, null);
		pPhase++;
		return false;
	}


	private NodeMod registerMap(MasterMgrClient mclient, String oldMap, String newMap, String charTexNode,
			BaseEditor editor) 
	throws PipelineException
	{
		Path p = new Path(newMap);
		String res = p.getParentPath().getName();
		//String texFolder = p.getParentPath().getParentPath().getParent();
		//String assetName = (new Path(texFolder)).getParentPath().getName();
		//String charTexNode = texFolder+"/"+ assetName+"_tex";
		
		/*if(doesNodeExists(mclient, charTexNode)){
			mclient.checkOut(pUser, pView, charTexNode, null, CheckOutMode.KeepModified, CheckOutMethod.Modifiable);
			System.err.println("\n" + charTexNode + " exists");
		}*/
		
		//System.err.println("res is "+res+", folder is "+texFolder);
		NodeMod oldMapMod = null;
		try {
			oldMapMod = mclient.getWorkingVersion(pUser, pView, oldMap);
		}catch(PipelineException e){
			mclient.checkOut(pUser, pView, oldMap, null, CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
			oldMapMod = mclient.getWorkingVersion(pUser, pView, oldMap);
		}
		
		System.err.println("Going to register map: "+newMap);
		BaseAction mapAct = oldMapMod.getAction();
		String src = (String) mapAct.getSingleParamValue("ImageSource");
		
		if(src == null)
			throw new PipelineException("There is no source for map "+ oldMap);
		System.err.println(src);		
		
		NodeMod mapMod = null;
		{
			mapMod = registerNode(mclient, newMap, "map", editor);
			
			mclient.link(pUser, pView, charTexNode, newMap, LinkPolicy.Dependency, LinkRelationship.All, null);
			mclient.link(pUser, pView, newMap, src, LinkPolicy.Dependency, LinkRelationship.All, null);
			BaseAction action = plug.newAction("MRayTextureResize", null, "SCEA");
			action.setSingleParamValue("ImageSource", src);
			action.setSingleParamValue("ResizeAmount", res);
			action.setSingleParamValue("TexelLayout", "Scanlines");
			mapMod.setAction(action);

			JobReqs jreqs = mapMod.getJobRequirements();
			jreqs.addSelectionKey("Lair");
			mapMod.setJobRequirements(jreqs);

			mclient.modifyProperties(pUser, pView, mapMod);
			//mclient.submitJobs(pUser, pView, mapMod.getName(), null);
			//mclient.submitJobs(pUser, pView, charTexNode, null);
			
		}
		
		return mapMod;
		
	}
	
	/**
	 * Displays a list of all textures and the possible resolution sizes for each.
	 * 
	 * @return true if the user confirms a selection.
	 * @throws PipelineException
	 */
	private boolean buildGUI() throws PipelineException
	{
		Box finalBox = new Box(BoxLayout.Y_AXIS);
		top = new Box(BoxLayout.Y_AXIS);

		JScrollPane scroll;

		{
			scroll = new JScrollPane(finalBox);

			scroll
			.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

			Dimension size = new Dimension(sTSize + sVSize + 52, 500);
			scroll.setMinimumSize(size);
			scroll.setPreferredSize(size);

			scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
		}

		/* query the user */
		diag = new JToolDialog("Selective Texture Resize Tool", scroll, "Continue");

		JTextField userField = null;
		JTextField viewField = null;
		JTextField toolsetField = null;
		{
			Box hbox = new Box(BoxLayout.X_AXIS);
			Component comps[] = UIFactory.createTitledPanels();
			JPanel tpanel = (JPanel) comps[0];
			JPanel vpanel = (JPanel) comps[1];
			{
				userField = UIFactory.createTitledTextField(tpanel, "User:", sTSize,
						vpanel, pUser, sVSize);
				UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

				viewField = UIFactory.createTitledTextField(tpanel, "View:", sTSize,
						vpanel, pView, sVSize);
				UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

				toolsetField = UIFactory.createTitledTextField(tpanel, "Toolset:",
						sTSize, vpanel, pToolset, sVSize);
				UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
			}

			hbox.add(comps[2]);
			top.add(hbox);
		}

		{
			for(String asset: pTextureList.keySet()){
				Box hbox = new Box(BoxLayout.X_AXIS);
				Component comps[] = UIFactory.createTitledPanels();
				JPanel tpanel = (JPanel) comps[0];
				JPanel vpanel = (JPanel) comps[1];

				TreeSet<String> texs = pTextureList.get(asset);
				TreeMap<String, JCollectionField> list = new TreeMap<String, JCollectionField>();
				
				for (String texture : texs)
				{
					Path p = new Path(texture);
					JCollectionField field = UIFactory.createTitledCollectionField(tpanel, p.getName(), sTSize, 
							vpanel, textureSizes, diag, sVSize, "The map size to use");
					field.setSelected(oneK);
					list.put(texture, field);
					UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
				}
				texChangeFields.put(asset, list);
				hbox.add(comps[2]);
				JDrawer draw = new JDrawer(asset, hbox, false);
				top.add(draw);
			}		
		}

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
			return true;
		}
		return false;
	}//end buildGUI()

	
	
	/**
	 * Returns all the paths that are located directly underneath a given path.
	 * 
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
			treeComps = treeComps.get(comp);
		}
		try {
		for (String s : treeComps.keySet())
		{
			toReturn.add(s);
		}
		} catch (NullPointerException e){}
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

	public NodeMod registerNode(MasterMgrClient mclient, String name, String extention,
			BaseEditor editor) throws PipelineException
	{
		File f = new File(name);
		FileSeq animSeq = new FileSeq(f.getName(), extention);
		NodeMod animNode = new NodeMod(name, animSeq, null, pToolset, editor);
		mclient.register(pUser, pView, animNode);
		return animNode;
	}

	
	/*
	 * Test Main
	 */  
	/*public static void main(String[] args)
	{

		try {
			mclient = new MasterMgrClient();
			QueueMgrClient qclient = new QueueMgrClient();
			PluginMgrClient.init();

			pSelected = new TreeMap<String, NodeStatus>();
			SelectiveTextureResizeTool test = new SelectiveTextureResizeTool();
			//test.mclient = mclient;

			String author = "iojomo";
			String view = "build";

			pPrimary = "/projects/lr/production/lair/seq029/001/lgt/seq029_001_lgt";
			NodeStatus value = mclient.status(author,view, pPrimary);
			pSelected.put(pPrimary, value);			

			while(test.collectPhaseInput()!=null){
				if(!test.executePhase(mclient,qclient))
					break;
			}

			//System.err.println("DONE");

		} catch(PipelineException e){
			e.printStackTrace();
		}
		System.exit(0);
	}

	private static String pPrimary = null;
	private static TreeMap<String,NodeStatus> pSelected;
	private static MasterMgrClient mclient = null;
	*/
}
