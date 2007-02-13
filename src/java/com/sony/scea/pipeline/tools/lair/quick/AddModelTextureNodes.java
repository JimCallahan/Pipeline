package com.sony.scea.pipeline.tools.lair.quick;

import static com.sony.scea.pipeline.tools.Globals.*;
import static com.sony.scea.pipeline.tools.lair.LairConstants.*;

import java.util.ArrayList;
import java.util.TreeMap;

import us.temerity.pipeline.*;

import com.sony.scea.pipeline.tools.Wrapper;
import com.sony.scea.pipeline.tools.lair.LairAsset;
import com.sony.scea.pipeline.tools.lair.LairConstants.AssetType;

public class AddModelTextureNodes
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
	String user = "pipeline";
	String view = "default";
	String toolset = "csg_rev13";
	MasterMgrClient mclient = new MasterMgrClient();
	Wrapper w;
	try
	{
	    PluginMgrClient.init();
	    w = new Wrapper(user, view, toolset, mclient);
	    TreeMap<String, String> chars = getLairList(w, AssetType.CHARACTER);
	    int pass = 2;
	    if ( pass == 1 )
	    {
		for (String charPrefix : chars.keySet())
		{
		    //if (!charPrefix.equals("attakai"))
		    {
			System.out.println(charPrefix);
			String nodeName = chars.get(charPrefix);
			LairAsset as = new LairAsset(new Path(nodeName).getName(),
			    AssetType.CHARACTER);
			System.out.println("Checking out the shader scene");
			getNewest(w, as.shdScene, keep, modi);
			System.out.println("Check out done");
			if ( !doesNodeExists(w, as.texGroup) )
			{
			    System.out.println("Creating the tex node");
			    NodeMod mod = registerNode(w, as.texGroup, null, editorKWrite());
			    BaseAction act = actionListSources();
			    mod.setAction(act);
			    mclient.modifyProperties(user, view, mod);
			    mclient.link(user, view, as.shdScene, as.texGroup, REF,
				LINKALL, null);
			    try
			    {
				mclient.submitJobs(user, view, as.texGroup, null);
			    } catch ( PipelineException ex )
			    {
				ex.printStackTrace();
			    }
			} else
			{
			    getNewest(w, as.texGroup, keep, modi);
			    NodeMod mod = mclient
				.getWorkingVersion(user, view, as.texGroup);
			    mod.setToolset(toolset);
			    BaseAction act = mod.getAction();
			    if ( act == null )
			    {
				System.out.println("No action");
				act = actionListSources();
				mod.setAction(act);
				mclient.modifyProperties(user, view, mod);
			    } else
			    {
				String actionName = act.getName();
				if ( !actionName.equals("ListSources") )
				{
				    System.out.println("moding action");
				    act = actionListSources();
				    mod.setAction(act);
				    mclient.modifyProperties(user, view, mod);
				}
			    }
			    ArrayList<LinkMod> links = mod.getSources();
			    if ( links != null )
			    {
				for (LinkMod link : links)
				{
				    System.out.println("fixing links");
				    if ( link.getRelationship().equals(REF) )
				    {
					mclient.link(user, view, as.texGroup, link
					    .getName(), DEP, LINKALL, null);
				    }
				}
			    }
			    try
			    {
				mclient.submitJobs(user, view, as.texGroup, null);
			    } catch ( PipelineException ex )
			    {
				ex.printStackTrace();
			    }
			}
		    }
		}
	    } else if ( pass == 2 )
	    {
		for (String charPrefix : chars.keySet())
		{
		    System.out.println(charPrefix);
		    String nodeName = chars.get(charPrefix);
		    LairAsset as = new LairAsset(new Path(nodeName).getName(),
			AssetType.CHARACTER);
		    NodeID nodeID = new NodeID(user, view, as.shdScene);
		    mclient.checkIn(nodeID, "Added the texture node to every model "
			    + "and fixed any texture links that already exist.",
			VersionID.Level.Minor);
		}
	    }
	} catch ( PipelineException e )
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }
}
