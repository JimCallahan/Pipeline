package com.sony.scea.pipeline.plugins.v1_0_0;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
/**
 * A generic pass builder that takes one or more lighting and animation scene 
 * and creates a variety of passes that branch off of them.<P>
 * This tool was designed to be flexible and probably has many of the more recent
 * ideas about multi-project flexibility built into it.  It does require modification
 * each time it needs to be used with a new project, but it should be minimal and simple.
 * <p>
 * This tool presents the user with two dialog boxes to input their settings into.  The
 * first allows them to specify exactly how many passes they wish to build.  There are
 * several options.
 * <ul><li><b>Same Passes for All Nodes?</b>: Since the tool allows you to select multiple
 * nodes, this option allows the user the choice of creating the same group of passes for 
 * every selected node or creating different passes for each node.  If the first option is
 * selected, then the integer fields in the bottom part of the dialog which allow the user
 * to specify the number of passes on a per-node basis will be disabled.  Otherwise, the 
 * <b>Number For All</b> field will be disabled.
 * <li><b>Number For All</b>: The number of passes to be constructed for each node, if 
 * the same passes are going to be created for all the nodes.
 * <li><b>Using Mel Scripts?</b>: Will mel scripts be used to help generate the passes.
 * If this is set to <code>YES</code> then the tool will scan the project and global mel
 * directories to find all possible mel scripts.
 * <li> A section that has an integer field for each selected node.  If different passes
 * are being built for each selected node, then the number of desired passes should be
 * inputed here.
 * </ul>
 * <p>
 * Once these options are set, a second window will appear to collect information about the 
 * passes that are going to be built.  Each pass will present the following set of options.
 * <ul><li><b>Name</b>: The name the pass will have.  There should be no spaces in this name.
 * <li><b>Basis</b>: This determines what node the pass will be based off of.  Currently there
 * are only two options for this: <code>Lighting</code> and <code>Animation</code>.  If it 
 * becomes desirable to branch off passes from other parts of shot setups, then the code could
 * be extended to permit that.  That would require modification of the collectFirstPhase 
 * method to build the correct names, the addition of extra maps to store this information 
 * (like nAnimNameMap), addition to oBasisOptions and logic in the code that actually builds 
 * the passes to make sure the right things get connected.
 * <li><b>Make What For</b>: A list of five options of things that can be built. <ul>
 * 	<li><b>Scene Only</b>: Only builds a single maya scene that branches off the 
 * basis.  If there is a mel script associated with the pass, it plugs into the maya scene.
 * 	<li><b>Dual Scene Only</b>: Builds two scenes, the first that branches off the basis,
 * and the second that comes off the first.  This is useful for setups where it is 
 * desired to have Pipeline always build the first node, which is then referenced by the second
 * node, allowing a user to layer their changes on top of the automatically generated changes.
 * 	<li><b>Scene + Images</b>: Adds an image node to the first setup.
 * 	<li><b>Dual Scene + Images</b>: Adds an image node to the second setup.
 * 	<li><b>Images Only</b>: Builds only an image node that branches off the basis.
 * </ul>
 * <li><b>Build type for Gen?</b>: Controls how the gen node (the first node in the dual
 * scene setup) is built.  There are three options. <ul>
 * 	<li><b>Copy</b>:  The basis scene is opened.  The mel script (assuming there is one) is 
 * run on the scene, and the scene is then saved.  Except for the changes the script might have
 * made it is identical to the basis scene.
 * 	<li><b>Import</b>: A new maya scene is created and the basis scene is imported in (with no
 * namespace, by default).
 * 	<li><b>Reference</b>: A new maya scene is created and the basis scene is reference in
 * (with a namespace, by default).  
 * </ul>
 * <li><b>Type of Pass</b>: Either "Lgt" or "Vfx", right now.  Determines the paths where nodes
 * will live.
 * <li><b>RenderType</b>: What sort of rendering is the images node going to use (assuming
 * that an images node was created).
 * <ul>
 * 	<li><b>MayaSoftware</b>: Renders using the MayaRender Action, set to "Maya Software"
 * 	<li><b>Maya2MR</b>: Renders using the MayaRender Action, set to "Mental Ray"
 * 	<li><b>MentalRay</b>: Renders using the MayaMRayRender
 * </ul>
 * <li><b>Use a mel script?</b>: Should a mel script be used for this pass?
 * <li><b>What Mel Script?</b>: Which mel script should be used.  The names of all the mel
 * scripts have a prefix that indicates where they come from.  The <code>global</code> tag
 * indicates that the script it from the global mel location.  All other tags are the 
 * parent directory of the script.  Note that if passes are being built for multiple
 * projects and the <b>Same Passes for All</b> is selected, then only <code>global </code>
 * mels will show up.
 * <P>
 * @author jesse
 *
 */
public class BuildRenderScenesTool extends BaseTool implements ActionListener
{
  private static final long serialVersionUID = -6997725721690428108L;

  /**
   * This initializes all the Maps and Lists that the class uses.  If you're adding
   * things to any of the enum parameters, this is the place to do it.
   */
  public BuildRenderScenesTool()

  {
    super("BuildRenderScenes", new VersionID("1.0.0"), "SCEA",
    "Builds a group of render scenes based of a lighting scene or an animation scene");

    nImagesStartFieldMap = new TreeMap<String, JIntegerField>();
    nImagesEndFieldMap = new TreeMap<String, JIntegerField>();
    nPassNumberMap = new TreeMap<String, JIntegerField>();
    nLightingNameStartMap = new TreeMap<String, Path>();
    nVfxNameStartMap = new TreeMap<String, Path>();
    nImagesNameStartMap = new TreeMap<String, Path>();
    nSceneNameMap = new TreeMap<String, String>();
    nToolsetNameMap = new TreeMap<String, String>();
    nLgtNameMap = new TreeMap<String, String>();
    nAnimNameMap = new TreeMap<String, String>();
    nHasLightingSceneMap = new TreeMap<String, Boolean>();
    nMelScriptSearchPath = new TreeMap<String, TreeSet<Path>>();
    qMelScriptsMap = new TreeMap<String, TreeMap<String, String>>();
    nProjectNameMap = new TreeMap<String, String>();

    mBuildTypeFieldMap = new TreeMap<String, TreeMap<Integer, JCollectionField>>();
    mPassNameFieldMap = new TreeMap<String, TreeMap<Integer, JTextField>>();
    mBasisFieldMap = new TreeMap<String, TreeMap<Integer, JCollectionField>>();
    mMelScriptFieldMap = new TreeMap<String, TreeMap<Integer, JCollectionField>>();
    mUseMelScriptFieldMap = new TreeMap<String, TreeMap<Integer, JBooleanField>>();
    mPassTypeFieldMap = new TreeMap<String, TreeMap<Integer, JCollectionField>>();
    mDualTypeFieldMap = new TreeMap<String, TreeMap<Integer, JCollectionField>>();
    mUseMelScriptFieldMap = new TreeMap<String, TreeMap<Integer,JBooleanField>>();
    mRenderingOptionsFieldMap = new TreeMap<String, TreeMap<Integer,JCollectionField>>();

    pAllOneProject = false;

    oBasisOptions = new ArrayList<String>();
    oBasisOptions.add("Animation");
    oBasisOptions.add("Lighting");

    oBasisNoLightOptions = new ArrayList<String>();
    oBasisNoLightOptions.add("Animation");

    oTypeOptions = new ArrayList<String>();
    oTypeOptions.add("Scene Only");
    oTypeOptions.add("Dual Scene Only");
    oTypeOptions.add("Scene + Images");
    oTypeOptions.add("Dual Scene + Images");
    oTypeOptions.add("Images Only");

    oPassTypeOptions = new ArrayList<String>();
    oPassTypeOptions.add("Lgt");
    oPassTypeOptions.add("Vfx");

    oDualTypeOptions = new ArrayList<String>();
    oDualTypeOptions.add("Copy");
    oDualTypeOptions.add("Import");
    oDualTypeOptions.add("Reference");

    oRenderingOptions = new ArrayList<String>();
    oRenderingOptions.add("MayaSoftware");
    oRenderingOptions.add("Maya2MR");
    oRenderingOptions.add("MentalRay");

    underDevelopment();

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);

    pPhase = 1;
  }

  public synchronized String collectPhaseInput() throws PipelineException
  {
    switch (pPhase)
    {
      case 1:
	return collectFirstPhaseInput();
      case 2:
	return collectSecondPhaseInput();
      default:
	assert ( false );
      return null;
    }
  }

  public synchronized boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient)
  throws PipelineException
  {
    switch (pPhase)
    {
      case 1:
	return executeFirstPhase(mclient, qclient);

      case 2:
	return executeSecondPhase(mclient, qclient);

      default:
	assert ( false );
      return false;
    }
  }

/**
 * Performs basic verification that the selected nodes match naming criteria
 * and displays the first GUI window, prompting for basic pass information.
 * <p>
 * If it is desired to let users generate passes without selecting either an
 * animation or a lighting node, then this method will need to be modified as
 * well, to expand the name checking and name generation code.
 * 
 * @return
 * @throws PipelineException
 */
  private String collectFirstPhaseInput() throws PipelineException
  {
    if ( pPrimary == null )
      throw new PipelineException("You've got to have something selected!");

    pPlugMgr = PluginMgrClient.getInstance();

    Box vbox = new Box(BoxLayout.Y_AXIS);

    JScrollPane scroll = new JScrollPane(vbox);
    Dimension size = new Dimension(sTSize + sVSize + 52, 300);
    scroll.setMinimumSize(size);
    scroll.setPreferredSize(size);

    scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

    pDiag = new JToolDialog("Build Lighting Scenes", scroll, "Confirm");

    {
      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];

      pSamePassesField = UIFactory.createTitledBooleanField(tpanel,
	"Same Passes for All Nodes?", sTSize, vpanel, sVSize,
      "Build the same group of passes for all the selected nodes?");
      pSamePassesField.setValue(true);
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      pSamePassesNumberField = UIFactory.createTitledIntegerField(tpanel, "Number for All",
	sTSize, vpanel, 1, sVSize, "The Number of Passes to build if All is selected");
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      pMelScriptField = UIFactory.createTitledBooleanField(tpanel, "Using Mel Scripts?",
	sTSize, vpanel, sVSize, "Check this if you plan to link mel scripts for "
	+ "some passes.  This will scan for existing scripts.");
      pMelScriptField.setValue(false);
      vbox.add(comps[2]);
      pSamePassesField.addActionListener(this);
      pSamePassesField.setActionCommand("samepasses");
    }

    {
      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];

      for (String name : pSelected.keySet())
      {
	NodeStatus status = pSelected.get(name);
	NodeID nodeID = status.getNodeID();
	Path namePath = new Path(name);

	pUser = nodeID.getAuthor();
	pView = nodeID.getView();

	{
	  String toolset = status.getDetails().getWorkingVersion().getToolset();
	  nToolsetNameMap.put(name, toolset);
	}

	int stype = NONE;

	if ( name.matches(animPattern) )
	  stype = ANIM;
	else if ( name.matches(lgtPattern) )
	  stype = LGT;
	else
	  throw new PipelineException("The Selected Node (" + name
	    + ") does not have a recognizable name");

	String projectName = namePath.getComponents().get(1);

	nProjectNameMap.put(name, projectName);

	String sceneName;
	Path lightingPath;
	Path animPath;
	Path vfxPath;
	{
	  String buffer[] = namePath.getName().split("_");
	  String seqName = buffer[0];
	  String shotName = buffer[1];

	  sceneName = seqName + "_" + shotName;

	  if ( !sceneName.matches("seq\\d\\d+[AB]?_.*") )
	    throw new PipelineException("The Selected Node (" + name
	      + ") does not have a recognizable name");

	  nSceneNameMap.put(name, sceneName);

	  buffer = name.split(seqName);

	  if ( buffer.length <= 1 )
	    throw new PipelineException("The Selected Node (" + name
	      + ") does not have a recognizable name.  Check 2. bl = " + buffer.length);
	  String sequenceStart = buffer[0];

	  Path scenePath = new Path(new Path(new Path(sequenceStart), seqName), shotName);

	  animPath = new Path(scenePath, "anim");
	  lightingPath = new Path(scenePath, "lgt");
	  vfxPath = new Path(scenePath, "vfx");
	  nLightingNameStartMap.put(name, lightingPath);
	  nVfxNameStartMap.put(name, vfxPath);

	  Path imagesPath = new Path(scenePath, "img");
	  nImagesNameStartMap.put(name, imagesPath);

	  TreeSet<Path> melScriptPath = new TreeSet<Path>();
	  melScriptPath.add(new Path(new Path(new Path("/projects"), projectName),"assets/tools/mel")); 
	  melScriptPath.add(new Path(new Path(new Path("/projects"), projectName),"assets/tools/render"));
//	  Path renderMelScriptPath = new Path(new Path(new Path("/projects"), projectName),
//	  "assets/tools/render");

	  nMelScriptSearchPath.put(name, melScriptPath);
	}

	String beautyScene;
	String animationScene;

	switch (stype)
	{
	  case ANIM:
	  {
	    nAnimNameMap.put(name, name);
	    animationScene = name;
	    beautyScene = new Path(lightingPath, sceneName + "_lgt").toString();
	    nLgtNameMap.put(name, beautyScene);
	    break;
	  }
	  case LGT:
	  {
	    nLgtNameMap.put(name, name);
	    beautyScene = name;
	    animationScene = new Path(animPath, sceneName + "_anim").toString();
	    nAnimNameMap.put(name, animationScene);
	    break;
	  }
	}

	JIntegerField intF = UIFactory.createTitledIntegerField(tpanel, name, sTSize,
	  vpanel, 1, sVSize, "The Number of Passes to build for this particular node.");
	intF.setEnabled(false);
	nPassNumberMap.put(name, intF);
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      } //      for(String name : pSelected.keySet())
      JDrawer drawer = new JDrawer("Number of Passes", (JComponent) comps[2], true);
      vbox.add(drawer);
    }
    {
      JPanel spanel = new JPanel();
      spanel.setName("Spacer");

      spanel.setMinimumSize(new Dimension(sTSize + sVSize, 7));
      spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
      spanel.setPreferredSize(new Dimension(sTSize + sVSize, 7));

      vbox.add(spanel);
    }

    pDiag.setVisible(true);

    /* This check might be a little confusing.  The idea is to see if all the nodes
     * being selected are in the same project.  If they are, then we only need to search
     * for mel scripts once.  Otherwise, we'll need to search for them for all the projects
     * that are represented. 
     */
    if ( pDiag.wasConfirmed() )
    {

      String projectName = nProjectNameMap.get(nProjectNameMap.firstKey());
      pAllOneProject = true;
      for (String name : nProjectNameMap.keySet())
      {
	String tempProj = nProjectNameMap.get(name);
	if ( !projectName.equals(tempProj) )
	{
	  pAllOneProject = false;
	  break;
	}
      }
      return ": Rub my lamp";
    }
    return null;

  }

  /**
   * Displays the second GUI window that gather specific information about the passes.
   * <p>
   * It makes use of the 
   * {@link #buildSecondPhaseConstruct(String, String, JPanel, JPanel, int, TreeMap, TreeMap, TreeMap, ArrayList, TreeMap, TreeMap, TreeMap, TreeMap, TreeMap, Set) : buildSecondPhaseConstruct}
   * method to do most of the heavy lifting.
   * @return
   */
  private String collectSecondPhaseInput()
  {
    boolean allSame = pSamePassesField.getValue();

    Box vbox = new Box(BoxLayout.Y_AXIS);

    JScrollPane scroll = new JScrollPane(vbox);
    Dimension size = new Dimension(sTSize + sVSize2 + 52, 300);
    scroll.setMinimumSize(size);
    scroll.setPreferredSize(size);

    scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
    pDiag = new JToolDialog("Build Lighting Scenes", scroll, "Confirm");

    /* 
     * These are going to be put into all the TreeMaps that start with the
     * 'n' prefix. 
     */
    TreeMap<Integer, JTextField> passNameMap = new TreeMap<Integer, JTextField>();
    TreeMap<Integer, JCollectionField> buildTypeMap = new TreeMap<Integer, JCollectionField>();
    TreeMap<Integer, JCollectionField> basisMap = new TreeMap<Integer, JCollectionField>();
    TreeMap<Integer, JCollectionField> melScriptMap = new TreeMap<Integer, JCollectionField>();
    TreeMap<Integer, JBooleanField> useMelScriptMap = new TreeMap<Integer, JBooleanField>();
    TreeMap<Integer, JCollectionField> passTypeMap = new TreeMap<Integer, JCollectionField>();
    TreeMap<Integer, JCollectionField> dualTypeMap = new TreeMap<Integer, JCollectionField>();
    TreeMap<Integer, JCollectionField> renderingOptionMap = new TreeMap<Integer, JCollectionField>();

    // If all the nodes are using the same passes.
    if ( allSame )
    {
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];

	int passNum = pSamePassesNumberField.getValue();
	for (int i = 0; i < passNum; i++)
	{
	  String titleStart = "Pass" + i;

	  TreeMap<String, String> melScripts = qMelScriptsMap.get("ALL");

	  buildSecondPhaseConstruct("ALL", titleStart, tpanel, vpanel, i, passNameMap,
	    buildTypeMap, basisMap, null, passTypeMap, melScriptMap, useMelScriptMap,
	    dualTypeMap, renderingOptionMap,  melScripts.keySet());
	  if ( i != passNum - 1 )
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 15);
	}

	mPassNameFieldMap.put("ALL", passNameMap);
	mBasisFieldMap.put("ALL", basisMap);
	mBuildTypeFieldMap.put("ALL", buildTypeMap);
	mPassTypeFieldMap.put("ALL", passTypeMap);
	mUseMelScriptFieldMap.put("ALL", useMelScriptMap);
	mMelScriptFieldMap.put("ALL", melScriptMap);
	mDualTypeFieldMap.put("ALL", dualTypeMap);
	mRenderingOptionsFieldMap.put("ALL", renderingOptionMap);

	JDrawer drawer = new JDrawer("All", (JComponent) comps[2], true);
	vbox.add(drawer);
      }

      // Finally allow each pass to have its own frame range.
      for (String name : pSelected.keySet())
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];

	JIntegerField infs = UIFactory.createTitledIntegerField(tpanel, "Start Frame",
	  sTSize2, vpanel, 1, sVSize2, "Start Frame For Images Node");
	nImagesStartFieldMap.put(name, infs);
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	JIntegerField infe = UIFactory.createTitledIntegerField(tpanel, "End Frame",
	  sTSize2, vpanel, 1, sVSize2, "End Frame For Images Node");
	nImagesEndFieldMap.put(name, infe);

	JDrawer drawer = new JDrawer(name, (JComponent) comps[2], true);
	vbox.add(drawer);
      }
    } else // If each passes gets its own passes
    {
      for (String name : pSelected.keySet())
      {
	// Got to reinit these.
	passNameMap = new TreeMap<Integer, JTextField>();
	buildTypeMap = new TreeMap<Integer, JCollectionField>();
	basisMap = new TreeMap<Integer, JCollectionField>();
	melScriptMap = new TreeMap<Integer, JCollectionField>();
	useMelScriptMap = new TreeMap<Integer, JBooleanField>();
	passTypeMap = new TreeMap<Integer, JCollectionField>();
	dualTypeMap = new TreeMap<Integer, JCollectionField>();
	renderingOptionMap = new TreeMap<Integer, JCollectionField>();
	
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];

	JIntegerField infs = UIFactory.createTitledIntegerField(tpanel, "Start Frame",
	  sTSize2, vpanel, 1, sVSize2, "Start Frame For Images Node");
	nImagesStartFieldMap.put(name, infs);
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	JIntegerField infe = UIFactory.createTitledIntegerField(tpanel, "End Frame",
	  sTSize2, vpanel, 1, sVSize2, "End Frame For Images Node");
	nImagesEndFieldMap.put(name, infe);

	UIFactory.addVerticalSpacer(tpanel, vpanel, 15);

	ArrayList<String> newBasis = null;
	if ( nHasLightingSceneMap.get(name) )
	{
	  newBasis = new ArrayList<String>(oBasisOptions);
	} else
	{
	  newBasis = new ArrayList<String>(oBasisNoLightOptions);
	}

	int numPasses = nPassNumberMap.get(name).getValue();
	for (int i = 0; i < numPasses; i++)
	{
	  String titleStart = "Pass" + i;

	  TreeMap<String, String> melScripts = qMelScriptsMap.get(nProjectNameMap.get(name));
	  if(melScripts == null)
	    melScripts = new TreeMap<String, String>();

	  buildSecondPhaseConstruct(name, titleStart, tpanel, vpanel, i, passNameMap,
	    buildTypeMap, basisMap, newBasis, passTypeMap, melScriptMap, useMelScriptMap,
	    dualTypeMap, renderingOptionMap, melScripts.keySet());
	  if ( i != numPasses - 1 )
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 15);
	}
	mPassNameFieldMap.put(name, passNameMap);
	mBasisFieldMap.put(name, basisMap);
	mBuildTypeFieldMap.put(name, buildTypeMap);
	mPassTypeFieldMap.put(name, passTypeMap);
	mUseMelScriptFieldMap.put(name, useMelScriptMap);
	mMelScriptFieldMap.put(name, melScriptMap);
	mDualTypeFieldMap.put(name, dualTypeMap);
	mRenderingOptionsFieldMap.put(name, renderingOptionMap);

	JDrawer drawer = new JDrawer(name, (JComponent) comps[2], true);
	vbox.add(drawer);
      }
    }
    {
      JPanel spanel = new JPanel();
      spanel.setName("Spacer");

      spanel.setMinimumSize(new Dimension(sTSize + sVSize, 7));
      spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
      spanel.setPreferredSize(new Dimension(sTSize + sVSize, 7));

      vbox.add(spanel);
    }

    pDiag.setVisible(true);

    if ( pDiag.wasConfirmed() )
    {
      return ": Building Scenes for you now";
    }
    return null;

  }

  /**
   * Builds all the necessary GUI components for one pass.
   * 
   * @param source
   * @param titleStart
   * @param tpanel
   * @param vpanel
   * @param i
   * @param passNameMap
   * @param buildTypeMap
   * @param basisMap
   * @param newBasis
   * @param passTypeMap
   * @param melScriptMap
   * @param useMelScriptMap
   * @param dualTypeMap
   * @param renderingOptionMap
   * @param melScripts
   */
  private void buildSecondPhaseConstruct
  (
      String source, 
      String titleStart, 
      JPanel tpanel,
      JPanel vpanel, 
      int i, 
      TreeMap<Integer, JTextField> passNameMap,
      TreeMap<Integer, JCollectionField> buildTypeMap,
      TreeMap<Integer, JCollectionField> basisMap, 
      ArrayList<String> newBasis,
      TreeMap<Integer, JCollectionField> passTypeMap,
      TreeMap<Integer, JCollectionField> melScriptMap,
      TreeMap<Integer, JBooleanField> useMelScriptMap,
      TreeMap<Integer, JCollectionField> dualTypeMap, 
      TreeMap<Integer, JCollectionField> renderingOptionMap,
      Set<String> melScripts
  )
  {
    JTextField nameF = UIFactory.createTitledEditableTextField(tpanel,
      titleStart + " Name", sTSize2, vpanel, titleStart, sVSize2,
      "The name for the new pass.  It can contain directory "
      + "information as well, if you wish " + "subdirectories of passes.");
    passNameMap.put(i, nameF);

    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

    if ( newBasis != null )
    {
      JCollectionField collF = UIFactory.createTitledCollectionField(tpanel, titleStart
	+ " Basis", sTSize2, vpanel, newBasis, sVSize2,
      "What node should be the basis for this pass?");
      basisMap.put(i, collF);
    } else
    {
      JCollectionField collF = UIFactory.createTitledCollectionField(tpanel, titleStart
	+ " Basis", sTSize2, vpanel, oBasisOptions, pDiag, sVSize2,
      "What node should be the basis for this pass?");
      basisMap.put(i, collF);
    }

    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

    JCollectionField collFT = UIFactory.createTitledCollectionField(tpanel,
      "Make what for " + titleStart, sTSize2, vpanel, oTypeOptions, pDiag, sVSize2,
    "What sort of construct do you want to make for this shot?");
    buildTypeMap.put(i, collFT);

    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

    JCollectionField dTF = UIFactory.createTitledCollectionField(tpanel,
      "Build Type for gen?", sTSize2, vpanel, oDualTypeOptions, pDiag, sVSize2,
    "Should the generation node reference or import its dependency or just be a copy of it?");
    dTF.setSelected("Copy");
    dualTypeMap.put(i, dTF);

    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

    JCollectionField ptF = UIFactory.createTitledCollectionField(tpanel, "Type of Pass",
      sTSize2, vpanel, oPassTypeOptions, pDiag, sVSize2,
    "What sort of pass is being built?");
    ptF.setSelected("Vfx");
    passTypeMap.put(i, ptF);

    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

    JCollectionField rtF = UIFactory.createTitledCollectionField(tpanel, "Render Type",
      sTSize2, vpanel, oRenderingOptions, pDiag, sVSize2,
    "What sort of Render do you want for this pass?");
    renderingOptionMap.put(i, rtF);

    collFT.setActionCommand("construct%" + source + "%" + i);
    collFT.addActionListener(this);

    if ( pMelScriptField.getValue() )
    {
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      JBooleanField useMF = UIFactory.createTitledBooleanField(tpanel, "Use a mel script?",
	sTSize2, vpanel, sVSize2,
      "Use a mel script plugged into the images to assist in rendering?");
      useMelScriptMap.put(i, useMF);

      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

      ArrayList<String> melList = new ArrayList<String>(melScripts);
      melList.add(0, "None");

      JCollectionField melF = UIFactory.createTitledCollectionField(tpanel,
	"What Mel Script?", sTSize2, vpanel, melList, pDiag, sVSize2,
      "What mel script do you want to use in this shot?");
      melScriptMap.put(i, melF);
    }
  }

  /**
   * Double checks and makes sure that the lighting and animation nodes exist.<p>
   * 
   * Also goes ahead and builds all the lists of mel scripts for everything.
   * 
   * @param mclient
   * @param qclient
   * @return
   * @throws PipelineException
   */
  private boolean executeFirstPhase(MasterMgrClient mclient, QueueMgrClient qclient)
  throws PipelineException
  {
    for (String name : pSelected.keySet())
    {
      String beautyScene = nLgtNameMap.get(name);
      NodeMod beautyMod = null;
      try
      {
	beautyMod = mclient.getWorkingVersion(pUser, pView, beautyScene);
      } catch ( PipelineException e )
      {}

      if ( beautyMod != null )
      {
	nHasLightingSceneMap.put(name, true);
      } else
      {
	nHasLightingSceneMap.put(name, false);
      }
      if ( !pAllOneProject )
      {
	String project = nProjectNameMap.get(name);
	if (qMelScriptsMap.get(project) == null)
	{
	  TreeSet<Path> melPath = nMelScriptSearchPath.get(name);
	  TreeMap<String, String> melscripts = getAllMelWithPrefix(mclient, melPath);
	  qMelScriptsMap.put(project, melscripts);
	}
      }
    }
    TreeMap<String, String> melscripts;
    if ( !pAllOneProject )
      melscripts = getAllMelWithPrefix(mclient, null);
    else
      melscripts = getAllMelWithPrefix(mclient, nMelScriptSearchPath.get(
	nMelScriptSearchPath.firstKey()));
    qMelScriptsMap.put("ALL", melscripts);
    pPhase++;
    return true;
  }

  /**
   * Actually builds all the node networks.
   * 
   * @param mclient
   * @param qclient
   * @return
   * @throws PipelineException
   */
  private boolean executeSecondPhase(MasterMgrClient mclient, QueueMgrClient qclient)
  throws PipelineException
  {
    boolean all = pSamePassesField.getValue();
    for (String name : pSelected.keySet())
    {
      if ( all )
	buildRenderPasses(mclient, name, "ALL");
      else
	buildRenderPasses(mclient, name, name);
    }
    return false;
  }

  /**
   * Actually does all the heavy lifting of building the passes
   * 
   * @param mclient
   * @param name
   * 	The name of the pass that is being built.
   * @param key
   * 	The key used to look up information.  It should be "ALL" if we're 
   * building the same passes for all the nodes.
   * @throws PipelineException
   */
  private void buildRenderPasses(MasterMgrClient mclient, String name, String key)
  throws PipelineException
  {

    TreeMap<Integer, JTextField> passNameMap = mPassNameFieldMap.get(key);
    TreeMap<Integer, JCollectionField> addImagesMap = mBuildTypeFieldMap.get(key);
    TreeMap<Integer, JCollectionField> basisMap = mBasisFieldMap.get(key);
    TreeMap<Integer, JCollectionField> passTypeMap = mPassTypeFieldMap.get(key);
    TreeMap<Integer, JBooleanField> userMelScriptMap = mUseMelScriptFieldMap.get(key);
    TreeMap<Integer, JCollectionField> melScriptMap = mMelScriptFieldMap.get(key);
    TreeMap<Integer, JCollectionField> dualTypeMap = mDualTypeFieldMap.get(key);
    TreeMap<Integer, JCollectionField> renderingOptionMap = mRenderingOptionsFieldMap.get(key);

    int startFrame = nImagesStartFieldMap.get(name).getValue();
    int endFrame = nImagesEndFieldMap.get(name).getValue();

    String toolset = nToolsetNameMap.get(name);
    String animNode = nAnimNameMap.get(name);
    String lgtNode = nLgtNameMap.get(name);

    String projectName = nProjectNameMap.get(name);

    BaseEditor mayaNormalEditor  = getEditor(mclient, "Temerity", "Maya", toolset);;
    BaseEditor mayaProjectEditor  = getEditor(mclient, "Temerity", "MayaProject", toolset);;
    Path lightingStart = nLightingNameStartMap.get(name);
    Path imagesStart = nImagesNameStartMap.get(name);
    Path vfxStart = nVfxNameStartMap.get(name);
    String sceneName = nSceneNameMap.get(name);

    TreeMap<String, String> melScripts = null;
    if ( !pAllOneProject )
      melScripts = qMelScriptsMap.get(projectName);
    else
      melScripts = qMelScriptsMap.get("ALL");

    int numPasses = passNameMap.size();

    for (int i = 0; i < numPasses; i++)
    {
      String passName = passNameMap.get(i).getText();

      String basis = basisMap.get(i).getSelected();

      String passType = passTypeMap.get(i).getSelected();

      String passNode = null;
      String dualNode = null;
      BaseEditor mayaEditor = null;
      /* 
       * Setups all the names based on the type of pass that is being built.
       * If you're adding new passTypes, you need to add them here as well.
       */ 
      if ( passType.equals("Lgt") )
      {
	passNode = new Path(lightingStart, ( sceneName + "_" + passName )).toString();
	dualNode = new Path(new Path(lightingStart, "gen"),
	  ( new Path(passNode).getName() + "_gen" )).toString();
	mayaEditor = mayaProjectEditor;
      } else if ( passType.equals("Vfx") )
      {
	passNode = new Path(vfxStart, ( sceneName + "_" + passName )).toString();
	dualNode = new Path(new Path(vfxStart, "gen"),
	  ( new Path(passNode).getName() + "_gen" )).toString();
	mayaEditor = mayaNormalEditor;
      } else
	throw new PipelineException(
	  "This should never happen.  Jesse is a bad programmer.  Go yell at him.");

      String buildType = addImagesMap.get(i).getSelected();

      // What exactly are we building.
      boolean doLighting = ( buildType.equals("Scene Only")
	  || buildType.equals("Scene + Images") || buildType.equals("Dual Scene Only") || buildType
	  .equals("Dual Scene + Images") );
      boolean doDualScene = ( buildType.equals("Dual Scene Only") || buildType
	  .equals("Dual Scene + Images") );
      boolean doImages = ( buildType.equals("Images Only")
	  || buildType.equals("Scene + Images") || buildType.equals("Dual Scene + Images") );

      boolean exists = false;

      String melScriptName = null;
      //System.err.println(userMelScriptMap);
      //System.err.println("\n\n"+userMelScriptMap.get(i));
      if(!userMelScriptMap.isEmpty()){
	if ( userMelScriptMap.get(i).getValue() )
	{
	  String melName = melScriptMap.get(i).getSelected();
	  //System.out.println("Using Mel: " + melName);
	  if ( !melName.equals("None") )
	  {
	    melScriptName = melScripts.get(melName);
	    //System.out.println("Mel Script: " + melScriptName);
	  }
	}
      }

      String dualType = dualTypeMap.get(i).getSelected();

      if ( doDualScene )
      {
	buildGenNode(mclient, mayaEditor, toolset, animNode, lgtNode, basis, dualNode,
	  dualType, melScriptName, startFrame, endFrame, projectName);
      }

      if ( doLighting && doDualScene )
	exists = buildPassNode(mclient, mayaEditor, toolset, dualNode, passNode,
	  startFrame, endFrame, projectName);
      else if ( doLighting )
	exists = buildSceneNode(mclient, mayaEditor, toolset, animNode, lgtNode, basis,
	  passNode, melScriptName, startFrame, endFrame, projectName);

      if ( doImages )
      {
	String imageNode = new Path(imagesStart, passName + "/" 
	  + sceneName.replaceFirst("seq","s") + "_" + passName).toString();

	BaseEditor passEditor = getEditor(mclient, "Temerity", "FCheck", toolset);

	try
	{
	  NodeMod passImageMod = registerSequence(mclient, imageNode, "iff", toolset,
	    passEditor, startFrame, endFrame);
	  String imgSource = null;
	  if ( doLighting )
	  {
	    mclient.link(pUser, pView, imageNode, passNode, DEP, LINKALL, null);
	    imgSource = passNode;
	  }
	  else
	  {
	    if ( basis.equals("Animation") )
	    {
	      mclient.link(pUser, pView, imageNode, animNode, DEP, LINKALL, null);
	      imgSource = animNode;
	    }
	    else if ( basis.equals("Lighting") )
	    {
	      mclient.link(pUser, pView, imageNode, lgtNode, DEP, LINKALL, null);
	      imgSource = lgtNode;
	    }
	    else
	      throw new PipelineException(
	      "bad basis here.  You may want to check for leaks, or even scallions");
	  }

	  String renderType = renderingOptionMap.get(i).getSelected();  

	  BaseAction imgAction;
	  if (renderType.equals("MentalRay"))
	  {
	    imgAction = getAction(mclient, "Temerity", "MayaMRayRender", toolset);

	  } else if (renderType.equals("MayaSoftware"))
	  {
	    imgAction = getAction(mclient, "Temerity", "MayaRender", toolset);
	    imgAction.setSingleParamValue("Renderer", "Software");
	    imgAction.setSingleParamValue("Processors", 0);
	    imgAction.setSingleParamValue("Processors", 0);
	  } else if (renderType.equals("Maya2MR"))
	  {
	    imgAction = getAction(mclient, "Temerity", "MayaRender", toolset);
	    imgAction.setSingleParamValue("Renderer", "Mental Ray");
	    imgAction.setSingleParamValue("Processors", 2);
	  }else
	    throw new PipelineException("Disgusting lack of code testing in these parts.  Something broke");

	  imgAction.setSingleParamValue("MayaScene", imgSource);

	  passImageMod.setAction(imgAction);
	  passImageMod.setExecutionMethod(ExecutionMethod.Parallel);
	  passImageMod.setBatchSize(3);

	  if ( passType.equals("Lgt") )
	    doLightingReqs(passImageMod, projectName, renderType);
	  else if ( passType.equals("Vfx") )
	    doEffectsReqs(passImageMod, projectName, renderType);
	  mclient.modifyProperties(pUser, pView, passImageMod);
	} catch ( PipelineException ex )
	{}
      }
      if ( !exists && doLighting )
	try
      {
	  mclient.submitJobs(new NodeID(pUser, pView, passNode), null);
      } catch ( PipelineException e )
      {}
    }

  }

  /**
   * Builds the gen node.
   * 
   * @param mclient
   * @param mayaEditor
   * @param toolset
   * @param animNode
   * @param lgtNode
   * @param basis
   * @param passNode
   * @param buildType
   * @param melScriptNode
   * @param start
   * @param end
   * @param project
   * @return
   * @throws PipelineException
   */
  private boolean buildGenNode(MasterMgrClient mclient, BaseEditor mayaEditor,
      String toolset, String animNode, String lgtNode, String basis, String passNode,
      String buildType, String melScriptNode, int start, int end, String project) throws PipelineException
      {
    boolean exists = false;
    {
      NodeMod passMod = null;

      try
      {
	passMod = registerNode(mclient, passNode, "ma", toolset, mayaEditor);
      } catch ( PipelineException ex )
      {
	try
	{
	  passMod = mclient.getWorkingVersion(pUser, pView, passNode);
	  exists = true;
	} catch ( PipelineException ex1 )
	{
	  throw new PipelineException("Could not create new gen node "
	    + "and couldn't find it in the repository.  "
	    + "Danger, danger will robinson.");
	}
      }

      if ( !exists )
      {
	BaseAction passAction;
	if (buildType.equals("Copy"))
	  passAction = getAction(mclient, "Temerity", "MayaMEL", toolset);
	else
	  passAction = getAction(mclient, "Temerity", "MayaBuild", toolset);
	String nameSpace = null;
	String source = null;
	if ( basis.equals("Animation") )
	{
	  mclient.link(pUser, pView, passNode, animNode, DEP, LINKALL, null);
	  source = animNode;
	  nameSpace = "anim";
	} else if ( basis.equals("Lighting") )
	{
	  mclient.link(pUser, pView, passNode, lgtNode, DEP, LINKALL, null);
	  source = lgtNode;
	  nameSpace = "lgt";
	} else
	  throw new PipelineException(
	    "Yeah, you really shouldn't have done that.  Invalid Basis!");

	if ( !buildType.equals("Copy") )
	{
	  passAction.initSourceParams(source);
	  if ( buildType.equals("Reference") )
	  {
	    passAction.setSourceParamValue(source, "PrefixName", nameSpace);
	    passAction.setSourceParamValue(source, "BuildType", "Reference");
	    passAction.setSourceParamValue(source, "NameSpace", true);
	  } else if ( buildType.equals("Import") )
	  {
	    passAction.setSourceParamValue(source, "PrefixName", null);
	    passAction.setSourceParamValue(source, "BuildType", "Import");
	    passAction.setSourceParamValue(source, "NameSpace", false);
	  } else
	    throw new PipelineException("Man, jesse is bad at this.  Invalid build type");

	  passAction.setSingleParamValue("TimeUnits", "NTSC (30 fps)");
	  passAction.setSingleParamValue("StartFrame", start);
	  passAction.setSingleParamValue("EndFrame", end);
	} else
	{
	  passAction.setSingleParamValue("MayaScene", source);
	  passAction.setSingleParamValue("SaveResult", true);
	}
	if ( melScriptNode != null )
	{
	  try
	  {
	    mclient.getWorkingVersion(pUser, pView, melScriptNode);
	  } catch ( PipelineException ex )
	  { // no working version
	    try
	    {
	      jcheckOut(mclient,pUser, pView, melScriptNode, null,
		CheckOutMode.KeepModified, CheckOutMethod.FrozenUpstream);
	    } catch ( PipelineException ex1 )
	    {
	      throw new PipelineException("can't find the node (" + melScriptNode
		+ ").  \n" + ex1.getMessage());
	    }
	  }
	  mclient.link(pUser, pView, passNode, melScriptNode, LinkPolicy.Dependency,
	    LinkRelationship.All, null);
	  if (buildType.equals("Copy"))
	  {
	    passAction.initSourceParams(melScriptNode);
	    passAction.setSourceParamValue(melScriptNode, "Order", 100);
	  } else
	    passAction.setSingleParamValue("ModelMEL", melScriptNode);
	}
	passMod.setAction(passAction);
	doReqs(passMod, project);
	mclient.modifyProperties(pUser, pView, passMod);
      }
    }
    return exists;
      }

  /**
   * Builds the scene node.  This only gets called if this is no dual node setup.
   * 
   * @param mclient
   * @param mayaEditor
   * @param toolset
   * @param animNode
   * @param lgtNode
   * @param basis
   * @param passNode
   * @param exists
   * @return
   * @throws PipelineException
   */
  private boolean buildSceneNode(MasterMgrClient mclient, BaseEditor mayaEditor,
      String toolset, String animNode, String lgtNode, String basis, String passNode,
      String melScriptNode, int start, int end, String project) throws PipelineException
      {
    boolean exists = false;
    {
      NodeMod passMod = null;

      try
      {
	passMod = registerNode(mclient, passNode, "ma", toolset, mayaEditor);
      } catch ( PipelineException ex )
      {
	try
	{
	  passMod = mclient.getWorkingVersion(pUser, pView, passNode);
	  exists = true;
	} catch ( PipelineException ex1 )
	{
	  throw new PipelineException("Could not create new pass node "
	    + "and couldn't find it in the repository.  "
	    + "Danger, danger will robinson.");
	}
      }

      if ( !exists )
      {
	BaseAction passAction = getAction(mclient, "Temerity", "MayaBuild", toolset);

	String nameSpace = null;
	String source = null;
	if ( basis.equals("Animation") )
	{
	  mclient.link(pUser, pView, passNode, animNode, REF, LINKALL, null);
	  source = animNode;
	  nameSpace = "anim";
	} else if ( basis.equals("Lighting") )
	{
	  mclient.link(pUser, pView, passNode, lgtNode, REF, LINKALL, null);
	  source = lgtNode;
	  nameSpace = "lgt";
	} else
	  throw new PipelineException(
	    "Yeah, you really shouldn't have done that.  Invalid Basis!");

	passAction.initSourceParams(source);
	passAction.setSourceParamValue(source, "PrefixName", nameSpace);
	passAction.setSourceParamValue(source, "BuildType", "Reference");
	passAction.setSourceParamValue(source, "NameSpace", true);
	passAction.setSingleParamValue("TimeUnits", "NTSC (30 fps)");
	passAction.setSingleParamValue("StartFrame", start);
	passAction.setSingleParamValue("EndFrame", end);
	if ( melScriptNode != null )
	{
	  try
	  {
	    mclient.getWorkingVersion(pUser, pView, melScriptNode);
	  } catch ( PipelineException ex )
	  { // no working version
	    try
	    {
	      jcheckOut(mclient,pUser, pView, melScriptNode, null,
		CheckOutMode.KeepModified, CheckOutMethod.FrozenUpstream);
	    } catch ( PipelineException ex1 )
	    {
	      throw new PipelineException("can't find the node (" + melScriptNode
		+ ").  \n" + ex1.getMessage());
	    }
	  }
	  mclient.link(pUser, pView, passNode, melScriptNode, LinkPolicy.Dependency,
	    LinkRelationship.All, null);
	  passAction.setSingleParamValue("ModelMEL", melScriptNode);
	}
	passMod.setAction(passAction);
	doReqs(passMod, project);
	mclient.modifyProperties(pUser, pView, passMod);
      }
    }
    return exists;
      }

  /**
   * This builds the scene node in the case when there is a dual node
   */
  private boolean buildPassNode(MasterMgrClient mclient, BaseEditor mayaEditor,
      String toolset, String dualNode, String passNode, int start, int end, String project)
  throws PipelineException
  {
    boolean exists = false;
    NodeMod passMod = null;

    try
    {
      passMod = registerNode(mclient, passNode, "ma", toolset, mayaEditor);
    } catch ( PipelineException ex )
    {
      try
      {
	passMod = mclient.getWorkingVersion(pUser, pView, passNode);
	exists = true;
      } catch ( PipelineException ex1 )
      {
	throw new PipelineException("Could not create new pass node "
	  + "and couldn't find it in the repository.  " + "Danger, danger will robinson.");
      }
    }
    if ( !exists )
    {
      BaseAction passAction = getAction(mclient, "Temerity", "MayaBuild", toolset);
      referenceNode(mclient, passNode, dualNode, passAction, REF, "gen");
      passAction.setSingleParamValue("TimeUnits", "NTSC (30 fps)");
      passAction.setSingleParamValue("StartFrame", start);
      passAction.setSingleParamValue("EndFrame", end);
      passMod.setAction(passAction);
      doReqs(passMod, project);
      mclient.modifyProperties(pUser, pView, passMod);
    }

    return exists;
  }

  private NodeMod registerNode(MasterMgrClient client, String name, String extention,
      String toolset, BaseEditor editor) throws PipelineException
      {
    File f = new File(name);
    FileSeq animSeq = new FileSeq(f.getName(), extention);
    NodeMod animNode = new NodeMod(name, animSeq, null, toolset, editor);
    client.register(pUser, pView, animNode);
    return animNode;
      }

  @SuppressWarnings("null")
  private void referenceNode(MasterMgrClient client, String target, String source,
      BaseAction action, LinkPolicy policy, String nameSpace) throws PipelineException
      {
    boolean reference = false;

    if ( action != null )
    {
      String actionType = action.getName();
      if ( actionType.equals("MayaReference") || actionType.equals("MayaImport")
	  || actionType.equals("MayaBuild") )
	reference = true;
    }

    client.link(pUser, pView, target, source, policy, LINKALL, null);
    if ( reference )
    {
      action.initSourceParams(source);
      action.setSourceParamValue(source, "PrefixName", nameSpace);
    }
      }

  private NodeMod registerSequence(MasterMgrClient client, String name, String extention,
      String toolset, BaseEditor editor, int start, int end) throws PipelineException
      {
    File f = new File(name);
    FilePattern pat = new FilePattern(f.getName(), 4, extention);
    FrameRange range = new FrameRange(start, end, 1);
    FileSeq animSeq = new FileSeq(pat, range);
    NodeMod animNode = new NodeMod(name, animSeq, null, toolset, editor);
    client.register(pUser, pView, animNode);
    return animNode;
      }

  /**
   * Adds the LinuxOnly and the project specific selection key to a node.<P>
   * 
   * Note that this does not run {@link MasterMgrClient#modifyProperties(String, String, NodeMod)}, 
   * which will still need to be run to apply these changes.
   */
  private void doReqs(NodeMod mod, String project) throws PipelineException
  {
    JobReqs req = mod.getJobRequirements();
    req.addSelectionKey(projectToKey(project));
    req.addSelectionKey("LinuxOnly");
    mod.setJobRequirements(req);
  }

  /**
   * Adds the LinuxOnly, a project specific, a renderer specific, and the Lighting 
   * Selection Key to a node.  Also will set the MentalRay License Key, if needed<P>
   * 
   * It also sets values for MinDisk and MinMemory. 
   * Note that this does not run {@link MasterMgrClient#modifyProperties(String, String, NodeMod)}, 
   * which will still need to be run to apply these changes.
   */
  private void doLightingReqs(NodeMod mod, String project, String renderType) throws PipelineException
  {
    JobReqs req = mod.getJobRequirements();
    req.addSelectionKey(projectToKey(project));
    req.addSelectionKey("Lighting");
    req.addSelectionKey(renderTypeToKey(renderType));
    req.addSelectionKey("LinuxOnly");
    req.setMinDisk(536870912L);
    req.setMinMemory(3221225472L);

    if (renderType.equals("MentalRay"))
      req.addLicenseKey("MentalRay");
    mod.setJobRequirements(req);
  }

  /**
   * Adds the LinuxOnly, a project specific, a renderer specific, and the Effects
   * Selection Key to a node.  Also will set the MentalRay License Key, if needed<P>
   * 
   * It also sets values for MinDisk and MinMemory. 
   * Note that this does not run {@link MasterMgrClient#modifyProperties(String, String, NodeMod)}, 
   * which will still need to be run to apply these changes.
   */
  private void doEffectsReqs(NodeMod mod, String project, String renderType) throws PipelineException
  {
    JobReqs req = mod.getJobRequirements();
    req.addSelectionKey(projectToKey(project));
    req.addSelectionKey("Effects");
    req.addSelectionKey(renderTypeToKey(renderType));
    req.addSelectionKey("LinuxOnly");
    req.setMinDisk(536870912L);
    req.setMinMemory(3221225472L);
    
    if (renderType.equals("MentalRay"))
      req.addLicenseKey("MentalRay");
    mod.setJobRequirements(req);
  }

  /**
   * Converts a project name into a selection key.<P>
   * 
   * This method can be extended to support more projects
   */
  private String projectToKey(String project)
  {
    if (project.equals("lr"))
      return "Lair";
    return "";
  }

  /**
   *  converts a rendering type into a selection key.
   */
  private String renderTypeToKey(String renderType)
  {
    if (renderType.equals("MentalRay"))
      return "MentalRay";
    else if (renderType.equals("Maya2MR"))
      return "Maya2MR";
    return "";
  }

  public void actionPerformed(ActionEvent e)
  {
    String command = e.getActionCommand();
    if ( command.equals("samepasses") )
    {
      JBooleanField field = (JBooleanField) e.getSource();
      boolean samePasses = field.getValue();
      pSamePassesNumberField.setEnabled(samePasses);
      for (JIntegerField iField : nPassNumberMap.values())
      {
	iField.setEnabled(!samePasses);
      }
    }
    // "construct_" + titleStart + "_" + i );
    else if ( command.startsWith("construct%") )
    {
      String buffer[] = command.split("%");
      String name = buffer[1];
      int key = Integer.parseInt(buffer[2]);
      String selected = ( (JCollectionField) e.getSource() ).getSelected();
      if ( selected.contains("Dual") )
      {
	TreeMap<Integer, JCollectionField> stuff = mDualTypeFieldMap.get(name);
	JCollectionField field = stuff.get(key);
	field.setEnabled(true);
      } else
      {
	TreeMap<Integer, JCollectionField> stuff = mDualTypeFieldMap.get(name);
	JCollectionField field = stuff.get(key);
	field.setEnabled(false);
      }
      if (selected.contains("Images"))
      {
	mRenderingOptionsFieldMap.get(name).get(key).setEnabled(true);
      } else
      {
	mRenderingOptionsFieldMap.get(name).get(key).setEnabled(false);
      }
    }
  }

  public BaseAction getAction(MasterMgrClient mclient, String vendor, String name,
      String toolset) throws PipelineException
      {
    DoubleMap<String, String, TreeSet<VersionID>> plugs = mclient
    .getToolsetActionPlugins(toolset);
    VersionID ver = plugs.get(vendor, name).last();

    return pPlugMgr.newAction(name, ver, vendor);
      }

  public BaseEditor getEditor(MasterMgrClient mclient, String vendor, String name,
      String toolset) throws PipelineException
      {
    DoubleMap<String, String, TreeSet<VersionID>> plugs = mclient
    .getToolsetEditorPlugins(toolset);
    VersionID ver = plugs.get(vendor, name).last();

    return pPlugMgr.newEditor(name, ver, vendor);
      }

  private TreeMap<String, String> getAllMelWithPrefix(MasterMgrClient mclient,
    TreeSet<Path> projectPath) throws PipelineException
    {
    ArrayList<String> globalMel = getChildNodes(mclient, globalMelPath);

    TreeMap<String, String> toReturn = new TreeMap<String, String>();
    for (String path : globalMel)
    {
      Path p = new Path(path);
      toReturn.put("global-" + p.getName(), globalMelPath + "/" + path);
    }
    if ( projectPath != null )
    {
      for (Path path : projectPath)
      {
	ArrayList<String> projectMel = getChildNodes(mclient, path.toString());
	String prefix = path.getName();
	for (String mel: projectMel)
	{
	  Path p = new Path(mel);
	  toReturn.put(prefix + "-" + p.getName(), path + "/" + mel);
	}
      }
    }
    return toReturn;
    }

  private ArrayList<String> getChildNodes(MasterMgrClient mclient, String start)
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

  private String pUser;

  private String pView;

  private boolean pAllOneProject;

  private PluginMgrClient pPlugMgr;

  //prefixes.
  // n means it is a per node mapping
  // m means it is a per pass mapping
  // o means they are lists used in JCollectionFields
  // q means a project specific mapping

  /**
   * The path to search for melscripts
   */
  private TreeMap<String, TreeSet<Path>> nMelScriptSearchPath;

  /**
   * The directory in which lighting nodes are stored
   */
  private TreeMap<String, Path> nLightingNameStartMap;

  /**
   * The directory in which image nodes are stored
   */
  private TreeMap<String, Path> nImagesNameStartMap;

  /**
   * The directory in which vfx nodes are stored
   */
  private TreeMap<String, Path> nVfxNameStartMap;

  /**
   * The short name for the scene 
   */
  private TreeMap<String, String> nSceneNameMap;

  /**
   * The number of passes for this particular node
   */
  private TreeMap<String, JIntegerField> nPassNumberMap;

  /**
   * The toolset that the selected node had.
   */
  private TreeMap<String, String> nToolsetNameMap;
  
  /**
   * The name of the lighting node for this particular scene
   */
  private TreeMap<String, String> nLgtNameMap;

  /**
   * The name of the animation node for this particular scene
   */
  private TreeMap<String, String> nAnimNameMap;

  /**
   * The project that the selected node was in.
   */
  private TreeMap<String, String> nProjectNameMap;

  /**
   * Does this scene has a lighting node?
   */
  private TreeMap<String, Boolean> nHasLightingSceneMap;
  
  /**
   * What frame should the image node start on for this scene
   */
  private TreeMap<String, JIntegerField> nImagesStartFieldMap;

  /**
   * What frame should the image node end on for this scene
   */
  private TreeMap<String, JIntegerField> nImagesEndFieldMap;
  
  // This is a project specific mapping

  /**
   * What are the mel scripts associated with this project
   */
  private TreeMap<String, TreeMap<String, String>> qMelScriptsMap;
  
  // These guys are settings that apply to all the pases. Hashed on pass name.

  /**
   * What is the build type for this pass.<br>
   * (Scene Only, Dual Scene Only, Scene + Images, Dual Scene + Images, Images Only).
   */
  private TreeMap<String, TreeMap<Integer, JCollectionField>> mBuildTypeFieldMap;

  /**
   * What is the dual gen type for this pass (Copy, Import, Reference)
   */
  private TreeMap<String, TreeMap<Integer, JCollectionField>> mDualTypeFieldMap;

  /**
   * What is the name of the pass
   */
  private TreeMap<String, TreeMap<Integer, JTextField>> mPassNameFieldMap;

  /**
   * What is the basis for this pass
   */
  private TreeMap<String, TreeMap<Integer, JCollectionField>> mBasisFieldMap;

  /**
   * What sort of pass is if (LGT or VFX)
   */
  private TreeMap<String, TreeMap<Integer, JCollectionField>> mPassTypeFieldMap;
  
  /**
   * What melscript does the pass use
   */
  private TreeMap<String, TreeMap<Integer, JCollectionField>> mMelScriptFieldMap;

  /**
   * Does the pass use a mel script
   */
  private TreeMap<String, TreeMap<Integer, JBooleanField>> mUseMelScriptFieldMap;

  /**
   * What renderer will the pass use.
   */
  private TreeMap<String, TreeMap<Integer, JCollectionField>> mRenderingOptionsFieldMap;

  /**
   * A list of all possible basisi
   * <p>
   * Animation or Lighting 
   */
  private ArrayList<String> oBasisOptions;

  /**
   * A smaller list of basisi
   * <p>
   * Animation
   */
  private ArrayList<String> oBasisNoLightOptions;

  /**
   * List of all possible network types.
   * <p> Scene Only
   * <br> Dual Scene Only
   * <br> Scene + Images
   * <br> Dual Scene + Images
   * <br> Images Only
   */
  private ArrayList<String> oTypeOptions;

  /**
   * The different sorts of passes that can be built. <P>
   * Vfx<br>
   * Lgt
   */
  private ArrayList<String> oPassTypeOptions;

  /**
   * Controls how the gen node (the first node in the dual scene setup) is built.<p>
   * <br> Copy
   * <br> Import
   * <br> Reference
   */
  private ArrayList<String> oDualTypeOptions;

  /**
   * List of all supported renderers<p>
   * MentalRay
   * <br>Maya2MR
   * <br>Maya Software
   */
  private ArrayList<String> oRenderingOptions;

  private JBooleanField pSamePassesField;

  private JIntegerField pSamePassesNumberField;

  private JBooleanField pMelScriptField;

  private JToolDialog pDiag;

  public static final LinkPolicy REF = LinkPolicy.Reference;

  public static final LinkPolicy DEP = LinkPolicy.Dependency;

  public static final LinkRelationship LINKALL = LinkRelationship.All;

  private static final int sTSize = 300;

  private static final int sVSize = 150;

  private static final int sTSize2 = 150;

  private static final int sVSize2 = 275;

  private final String animPattern = "/projects/.*/production/.*/anim/.*_anim";

  private final String lgtPattern = "/projects/.*/production/.*/lgt/.*_lgt";

  private static final String globalMelPath = "/global/assets/tools/mel";

  private int pPhase;

  private final int NONE = 0;

  private final int ANIM = 1;

  private final int LGT = 2;
}
