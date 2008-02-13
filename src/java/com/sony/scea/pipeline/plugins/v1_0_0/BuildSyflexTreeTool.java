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
 * This tool is designed to be run on a single animation node and will bring
 * a node network to enable cloth simulation from it. <p>
 * <b>NOTE: There are assumptions made by this tool which I am not comfortable 
 * about shots being 'final' and never changing, which leads to connections being 
 * broken that I wouldn't break.  This was built to the specs of the Syflex users, 
 * and not to what I would consider safe specs.  This can lead to inconsistent 
 * behavior when animation or rigs change.  While these should not be common 
 * occurances, they should be taken into account before using this tool</b><p> 
 * 
 * This tool will open a Maya scene and search inside it for all the objects 
 * whose names end in "_clt" (This could, and probably should, be replaced with
 * a hardcoded list of objects that exist in each character that have cloth, since
 * that would make performance much faster).  Once it finds them, it prompts the user
 * to input certain parameters about the cloth objects and how they want the sims
 * built.  It then goes and builds the sims, queues all the built nodes, allows the jobs
 * to finish, and then disconnects the animation node from all the networks, isolating this
 * network from actual production.  Simulation scenes are built by switching out the
 * low-rez character files for separate syflex character files and then removing all
 * other characters from the scenes.<p>
 * 
 * <b> NOTE 2: This tool does not support being run multiple times on the same scene.
 * If you build a sim network and later need to add to it, it must either be done
 * manually or another tool would have to be writen to support this.  The logic that is used
 * to determine how sims are built and the order they are linked in is just too complicated
 * to make supporting multiple invocations of this tool on the same scene a nightmare.
 * </b><p>
 * 
 * There are two dialog boxes that prompt for user input.  The first dialog allows specification
 * of the frame range of the sim (it defaults to displaying the full range of animation
 * contained in the maya scene).  It also asks which characters that have cloth need to
 * have sims built for them.  There is also a Channel parameter for each character.  This only
 * needs to be used if certain characters have to collide with each other.  The default behavior
 * for the tool is to build a separate simulation scene for each character that needs to
 * be simmed.  If two characters are set to the same channel, then one simulation scene will
 * be built that contains both of the syflex characters.  This dialog also prompts whether the sims
 * should be queued after they are built.  This is useful if it is known that the sims
 * will require no human intervention to run.
 * <p>
 * Once these settings are made, the second dialog appears.  This dialog has one Drawer for each
 * simulation group.  By default, there is one simulation group for each character being
 * simulated.  However, if multiple characters were assigned to the same channel in the first
 * step, then they will both show up in a single simulation group in this step.  Each cloth object
 * has an option to disable it (when it doesn't need to be simmed) and also has a channel option.
 * Channels work different in this step.  Channels are used to indicate dependency between
 * different pieces of cloth.  If one cloth object is assigned to channel 1 and one to channel 2,
 * then the channel 1 object will be simulated first.  Once it is done simulating, its cache will
 * be applied back to the simulation scene, creating a new simulation scene.  This new simulation
 * scene is then used to generate the second object's cache.  For example, a shirt might be
 * assigned to channel 1 and a jacket to channel 2.  First the shirt is simulated and the results
 * cached.  Then a new simulation scene is created in which the shirt is driven by its cache.
 * The jacket can then be simulated using the shirt as a collider.
 * <p>
 * In order to avoid negative frame ranges, this tool uses a two node method of creating sims.
 * The first node contains all the frames for run-up (150 extra frames at the beginning) as well as
 * enough frames for the animation to start from its beginning (which may be different than the 
 * first frame that is being cached).  This frame range is offset 1000 frames into the future 
 * (the syflex simulation nodes are also offset the same way by a mel script).  This way, the 
 * simulation will also be able to have non-negative run-up frames.  The second node only contains
 * the frame range that the user specified as needing to be cached. 
 * 
 * @author Jesse Clemens
 *
 */
public class BuildSyflexTreeTool extends BaseTool
{

  public BuildSyflexTreeTool()
  {
    super("BuildSyflexTree", new VersionID("1.0.0"), "SCEA",
      "Builds a big old syflex sim tree.");

    underDevelopment();
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);

    namespaceToCloth = new TreeMap<String, TreeSet<String>>();
    namespaceToCharacter = new TreeMap<String, String>();
    namespaceToStart = new TreeMap<String, Integer>();
    namespaceToEnd = new TreeMap<String, Integer>();
    clothOrderFields = new TripleMap<Integer, String, String, JCollectionField>();
    clothSimFields = new TripleMap<Integer, String, String, JBooleanField>();
    multipleCharChanField = new TreeMap<String, JCollectionField>();
    namespaceToSimNode = new TreeMap<String, String>();
    namespacesToSim = new TreeMap<Integer, TreeSet<String>>();
    charSimFields = new TreeMap<String, JBooleanField>();
    clothOrderInfo = new TripleMap<Integer, String, String, Integer>();
    simNodesToQueue = new TreeSet<String>();
    jobGroups = new ArrayList<QueueJobGroup>();

    pPhase = 1;
  }

  public synchronized String collectPhaseInput() throws PipelineException
  {
    System.err.println("Collecting Phase " + pPhase);
    switch (pPhase)
    {
      case 1:
	try
	{
	  // creates a temporary file to print log messages to.
	  File info = File.createTempFile("BuildSyflexTreeTool-Debug.", ".txt",
	    PackageInfo.sTempPath.toFile());
	  FileCleaner.add(info);
	  log = new PrintWriter(new BufferedWriter(new FileWriter(info)));
	} catch ( IOException ex )
	{
	  throw new PipelineException(
	    "Unable to create the temporary text file used to store the "
		+ "debug information!");
	}
	return collectFirstPhaseInput();

      case 2:
	return collectSecondPhaseInput();
      default:
	throw new IllegalStateException();
    }
  }

  /**
   * makes sure the node selected meets all necessary criteria.
   * @return
   * 	Status string.
   * @throws PipelineException
   */
  private String collectFirstPhaseInput() throws PipelineException
  {
    if ( pPrimary == null )
      throw new PipelineException("You need to have a node selected");
    if ( pSelected.size() != 1 )
      throw new PipelineException("You can only select one node.");

    if ( !pPrimary.matches(animPattern) )
      throw new PipelineException(
	"Please select an animation node before running this tool.");

    NodeStatus stat = pSelected.get(pPrimary);
    NodeID id = stat.getNodeID();
    pAuthor = id.getAuthor();
    pView = id.getView();
    // pToolset = stat.getDetails().getWorkingVersion().getToolset();
    pToolset = "csg_rev23";
    pToolsetSyflexFarm = "Syflex-Farm";
    pToolsetSyflexClient = "Syflex-Client";
    //pToolset = pToolsetSyflexFarm = pToolsetSyflexClient = "061025"; 

    pRoots.remove(pPrimary);
    
    return " : the magic of pipeline.";
  }

  /**
   * Displays both dialogs to gather information about the node 
   * @return
   * 	Status string
   * @throws PipelineException
   */
  private String collectSecondPhaseInput() throws PipelineException
  {
    {
      JScrollPane scroll = null;
      Box vbox = new Box(BoxLayout.Y_AXIS);
      {
	Box hbox = new Box(BoxLayout.X_AXIS);
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];

	pStart = Integer.MAX_VALUE;
	pEnd = Integer.MIN_VALUE;

	for (String namespace : namespaceToStart.keySet())
	{
	  int tempStart = namespaceToStart.get(namespace);
	  int tempEnd = namespaceToEnd.get(namespace);
	  log.println("Finding Range: " + namespace + " " + tempStart + " " + tempEnd);
	  if ( tempStart < pStart )
	    pStart = tempStart;
	  if ( tempEnd > pEnd )
	    pEnd = tempEnd;
	}

	startField = UIFactory.createTitledIntegerField(tpanel, "StartFrame:", sTSize,
	  vpanel, pStart, sVSize);
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	endField = UIFactory.createTitledIntegerField(tpanel, "EndFrame", sTSize, vpanel,
	  pEnd, sVSize);
	UIFactory.addVerticalSpacer(tpanel, vpanel, 6);
	runSimField = UIFactory.createTitledBooleanField(tpanel, "QueueSim", sTSize, vpanel,
	  sVSize, "Should the simulation be queued after it is built");
	runSimField.setValue(false);
	hbox.add(comps[2]);
	JDrawer drawer = new JDrawer("Frame Range", hbox, true);
	vbox.add(drawer);

      }

      {
	int size = namespaceToCloth.size();
	ArrayList<String> options = buildList(size);

	Box hbox = new Box(BoxLayout.X_AXIS);
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];

	for (String namespace : namespaceToCloth.keySet())
	{
	  UIFactory.createTitledTextField(tpanel, "Character name:", sTSize, vpanel,
	    namespaceToCharacter.get(namespace), sVSize, "The Character to be simmed.");
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  {
	    JBooleanField field = UIFactory.createTitledBooleanField(tpanel, "Create Sim?",
	      sTSize, vpanel, sVSize);
	    field.setValue(true);
	    charSimFields.put(namespace, field);
	  }
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  {
	    JCollectionField field = UIFactory.createTitledCollectionField(tpanel,
	      "Character Channel", sTSize, vpanel, options, sVSize,
	      "Set all characters that need to "
		  + "collide with each other to the same channel.  "
		  + "If they do not need to collide with other charactesr, "
		  + "this should be set to None.");
	    multipleCharChanField.put(namespace, field);
	  }
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 16);
	}
	hbox.add(comps[2]);
	JDrawer drawer = new JDrawer("Cloth characters", hbox, true);
	vbox.add(drawer);
	{
	  JPanel spanel = new JPanel();
	  spanel.setName("Spacer");

	  spanel.setMinimumSize(new Dimension(sTSize + sVSize, 7));
	  spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	  spanel.setPreferredSize(new Dimension(sTSize + sVSize, 7));

	  vbox.add(spanel);
	}
      }
      {
	scroll = new JScrollPane(vbox);

	scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

	Dimension dim = new Dimension(sTSize + sVSize, 500);
	scroll.setMinimumSize(dim);
      }

      JToolDialog diag = new JToolDialog("Syflex Tool", scroll, "Confirm");
      diag.setVisible(true);
      if ( !diag.wasConfirmed() )
	return null;
    }

    for (String namespace : charSimFields.keySet())
    {
      JBooleanField simField = charSimFields.get(namespace);
      JCollectionField orderField = multipleCharChanField.get(namespace);
      if ( simField.getValue() )
      {
	String orderName = orderField.getSelected();
	int order;
	if ( orderName.equals("None") )
	  order = 0;
	else
	  order = Integer.parseInt(orderName);
	TreeSet<String> toSim = namespacesToSim.get(order);
	if ( toSim == null )
	  toSim = new TreeSet<String>();
	toSim.add(namespace);
	namespacesToSim.put(order, toSim);
      }
    }

    pUserStart = startField.getValue();
    pUserEnd = endField.getValue();

    {
      JScrollPane scroll = null;
      Box vbox = new Box(BoxLayout.Y_AXIS);
      TreeSet<String> individChar = namespacesToSim.get(0);
      if ( individChar != null )
      {
	for (String namespace : individChar)
	{
	  Box hbox = new Box(BoxLayout.X_AXIS);
	  Component comps[] = UIFactory.createTitledPanels();
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];
	  TreeSet<String> clothObjects = namespaceToCloth.get(namespace);
	  int clothsize = clothObjects.size();
	  ArrayList<String> clothOptions = buildList(clothsize);
	  for (String cloth : clothObjects)
	  {
	    UIFactory.createTitledTextField(tpanel, "Piece of Cloth:", sTSize, vpanel,
	      cloth, sVSize, "The Maya node that is going to be simmed.");
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    {
	      JBooleanField field = UIFactory.createTitledBooleanField(tpanel,
		"Create Sim?", sTSize, vpanel, sVSize);
	      field.setValue(true);
	      clothSimFields.put(0, namespace, cloth, field);
	    }
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    {
	      JCollectionField field = UIFactory.createTitledCollectionField(tpanel,
		"Cloth Channel", sTSize, vpanel, clothOptions, sVSize,
		"Set all cloth objects that need to collide with each "
		    + "other to the same channel. If they do not need to "
		    + "collide with other pieces of cloth, " + "this can be left blank.");
	      clothOrderFields.put(0, namespace, cloth, field);
	    }
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 16);
	  }
	  hbox.add(comps[2]);
	  JDrawer drawer = new JDrawer(namespaceToCharacter.get(namespace), hbox, true);
	  vbox.add(drawer);
	}
      }
      for (int group : namespacesToSim.keySet())
      {
	if ( group == 0 )
	  continue;
	TreeSet<String> chars = namespacesToSim.get(group);

	Box hbox = new Box(BoxLayout.X_AXIS);
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];
	int clothsize = 0;
	for (String namespace : chars)
	{
	  int size = namespaceToCloth.get(namespace).size();
	  clothsize += size;
	}
	ArrayList<String> clothOptions = buildList(clothsize);
	String allChars = "";
	for (String namespace : chars)
	{
	  UIFactory.createTitledTextField(tpanel, "Character :", sTSize, vpanel,
	    namespaceToCharacter.get(namespace), sVSize, "The Character the cloth is in.");
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  TreeSet<String> clothObjects = namespaceToCloth.get(namespace);
	  for (String cloth : clothObjects)
	  {
	    UIFactory.createTitledTextField(tpanel, "Piece of Cloth:", sTSize, vpanel,
	      cloth, sVSize, "The Maya node that is going to be simmed.");
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    {
	      JBooleanField field = UIFactory.createTitledBooleanField(tpanel,
		"Create Sim?", sTSize, vpanel, sVSize);
	      field.setValue(true);
	      clothSimFields.put(group, namespace, cloth, field);
	    }
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    {
	      JCollectionField field = UIFactory.createTitledCollectionField(tpanel,
		"Cloth Channel", sTSize, vpanel, clothOptions, sVSize,
		"Set all cloth objects that need to collide with each "
		    + "other to the same channel. If they do not need to "
		    + "collide with other pieces of cloth, " + "this can be left blank.");
	      clothOrderFields.put(group, namespace, cloth, field);
	    }
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 16);
	  }
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 16);
	  allChars += namespaceToCharacter.get(namespace) + "  ";
	}
	hbox.add(comps[2]);
	JDrawer drawer = new JDrawer(allChars, hbox, true);
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

      {
	scroll = new JScrollPane(vbox);

	scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

	Dimension dim = new Dimension(sTSize + sVSize, 500);
	scroll.setMinimumSize(dim);
      }

      JToolDialog diag = new JToolDialog("Syflex Tool", scroll, "Confirm");
      diag.setVisible(true);
      if ( !diag.wasConfirmed() )
	return null;
    }
    {
      for (int group : clothOrderFields.keySet())
	for (String namespace : clothOrderFields.keySet(group))
	  for (String clothObject : clothOrderFields.keySet(group, namespace))
	  {
	    boolean sim = clothSimFields.get(group, namespace, clothObject).getValue();
	    if ( sim )
	    {
	      String orderName = clothOrderFields.get(group, namespace, clothObject)
		.getSelected();
	      int order;
	      if ( orderName.equals("None") )
		order = 0;
	      else
		order = Integer.parseInt(orderName);
	      clothOrderInfo.put(group, namespace, clothObject, order);
	    }
	  }
    }

    log.println(clothOrderInfo);
    log.println("Done second collect phase");
    return " : patent pending, all rights reserved.";
  }

  public synchronized boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient)
    throws PipelineException
  {
    System.err.println("Execute Phase " + pPhase);
    switch (pPhase)
    {
      case 1:
	return executeFirstPhase(mclient, qclient);
      case 2:
	return executeSecondPhase(mclient, qclient);
      default:
	throw new IllegalStateException();
    }
  }

  /**
   * Opens up the maya scene and runs a mel script (generated by this phase as well) to
   * retrieve information about the scene, including the cloth objects and the 
   * frame ranges of all the animation.
   * 
   * @param mclient
   * @param qclient
   * @return
   * 	true
   * @throws PipelineException
   */
  private boolean executeFirstPhase(MasterMgrClient mclient, QueueMgrClient qclient)
    throws PipelineException
  {
    Path mayaScenePath = null;
    NodeID nodeID = null;
    NodeMod mod = null;
    {

      NodeStatus stat = pSelected.get(pPrimary);
      nodeID = stat.getNodeID();
      mod = stat.getDetails().getWorkingVersion();
      FileSeq fseq = mod.getPrimarySequence();
      mayaScenePath = new Path(PackageInfo.sProdPath, nodeID.getWorkingParent() + "/"
	  + fseq.getPath(0));
    }

    File script;
    File info;
    try
    {
      script = File.createTempFile("BuildSyflexTreeTool-Gather.", ".mel",
	PackageInfo.sTempPath.toFile());
      FileCleaner.add(script);
    } catch ( IOException ex )
    {
      throw new PipelineException("Unable to create the temporary MEL script used "
	  + "to get the information!");
    }
    try
    {
      info = File.createTempFile("BuildSyflexTreeTool-Info.", ".txt", PackageInfo.sTempPath
	.toFile());
      FileCleaner.add(info);
    } catch ( IOException ex )
    {
      throw new PipelineException(
	"Unable to create the temporary text file used to store the "
	    + "information collected from the Maya scene!");
    }

    // Writing the mel script.
    try
    {
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(script)));

      out.println("{");
      out.println("$out = `fopen \"" + fixPath(info.getAbsolutePath()) + "\" \"w\"`;");
      out.println("    string $clothz[] = `ls \"*:*_clt\"`;");
      out.println("    string $cloth;");
      out.println("    string $namespaces[];");
      out.println("    for ($cloth in $clothz)");
      out.println("    {");
      out.println("     		 string $file = 		 `reference -q -f $cloth`;");
      out.println("    		 string $buffer[];");
      out.println("     		 tokenize($file, \"/\", $buffer);");
      out.println("     		 tokenize($buffer[(size($buffer) -1)], \"_\", $buffer);");
      out.println("      		 string $char = $buffer[0];");
      out
	.println("   		 fprint($out, \"Cloth \" + $cloth + \" \" + $char + \" \" + $file + \"\\n\");");
      out.println("   		 tokenize($cloth, \":\", $buffer);");
      out.println("   		 $namespaces[size($namespaces)]		 = $buffer[0];");
      out.println("    }");
      out.println("    $namespaces = stringArrayRemoveDuplicates($namespaces);");
      out.println("    string $space;");
      out.println("    for ($space in $namespaces)");
      out.println("    {");
      //TODO changed this
      out.println("      string $objs[] = `ls -type \"joint\" ($space + \":*\")`;");
      //out.println("      string $objs[] = `ls ($space + \":*\")`;");
      out.println("      int $first = `findKeyframe -w \"first\" $objs`;");
      out.println("      int $end = `findKeyframe -w \"last\" $objs`;");
      out
	.println("      fprint($out, \"Range \" + $space + \" \" + $first + \" \" + $end + \"\\n\");");
      out.println("    }");
      out.println("fclose $out;");
      out.println("}");
      out.close();
    } catch ( IOException e )
    {
      throw new PipelineException("Unable to write the temporary MEL script (" + script
	  + ") used to export the shaders");
    }

    try
    {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-batch");
      args.add("-script");
      args.add(script.getPath());
      args.add("-file");
      args.add(mayaScenePath.toOsString());

      Path wdir = new Path(PackageInfo.sProdPath.toOsString() + nodeID.getWorkingParent());
      TreeMap<String, String> env = mclient.getToolsetEnvironment(nodeID.getAuthor(),
	nodeID.getView(), mod.getToolset(), PackageInfo.sOsType);

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

      SubProcessLight proc = new SubProcessLight("ShaderCopyTool", command, args, env, wdir
	.toFile());
      try
      {
	proc.start();
	proc.join();
	if ( !proc.wasSuccessful() )
	{
	  throw new PipelineException(
	    "Did not correctly export the shaders due to a maya error.!\n\n"
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

    BufferedReader in;

    // reads the results that the mel script should have written out from maya.
    try
    {
      in = new BufferedReader(new FileReader(info));
      String line = in.readLine();
      while ( line != null )
      {
	String buffer[];
	buffer = line.split(" ");
	String type = buffer[0];
	log.println(line);
	log.println(type);
	if ( type.equals("Cloth") )
	{
	  log.println("Running cloth code");
	  String clothObject = buffer[1];
	  String charName = buffer[2];
	  String fileName = buffer[3];
	  String simNode = charToSim(pathToNode(fileName));
	  log.println(simNode);
	  if ( doesNodeExists(mclient, simNode) )
	  {
	    String buffer2[];
	    buffer2 = clothObject.split(":");
	    if ( buffer2.length > 2 )
	      throw new PipelineException("Objects with multiple layers of "
		  + "namespaces are not supported by this tool.");
	    String namespace = buffer2[0];
	    log.println("Namespace = " + namespace);

	    TreeSet<String> clothSet = namespaceToCloth.get(namespace);
	    if ( clothSet == null )
	      clothSet = new TreeSet<String>();
	    clothSet.add(clothObject);

	    namespaceToCloth.put(namespace, clothSet);
	    namespaceToCharacter.put(namespace, charName);
	    namespaceToSimNode.put(namespace, simNode);
	  }
	} else if ( type.equals("Range") )
	{
	  log.println("Running range code");
	  String namespace = buffer[1];
	  log.println("Namespace = " + namespace);
	  Integer start = Integer.parseInt(buffer[2]);
	  Integer end = Integer.parseInt(buffer[3]);
	  namespaceToStart.put(namespace, start);
	  namespaceToEnd.put(namespace, end);
	}
	line = in.readLine();
      }
    } catch ( IOException e )
    {
      throw new PipelineException("Could not read from the info file. " + e.getMessage());
    }
    pPhase++;
    return true;
  }

  /**
   * Builds the switch and end node.  Then calls the other steps.
   * The switch node the scene where all the lowrez models are converted into hirez models.
   * This scene is used to generate the syflexEnd scene which is actually used to 
   * drive the final verification scene.  This node is later disconnected from the 
   * network.
   * 
   * The end node is the hirez scene with all its references imported.  This is the node
   * that will have all the caches applied to it in the verification step.
   * 
   * @param mclient
   * @param qclient
   * @return
   * @throws PipelineException
   */
  private boolean executeSecondPhase(MasterMgrClient mclient, QueueMgrClient qclient)
    throws PipelineException
  {
    syflexSwitchName = pPrimary.replaceAll("anim", "syf");
    syflexSwitchName = syflexSwitchName.replaceAll("_syf", "_switch");
    
    /*
     * This is the scene where all the lowrez models are converted into hirez models.
     * This scene is used to generate the syflexEnd scene which is actually used to 
     * drive the final verification scene.  This node is later disconnected from the 
     * network.
     */ 
    {
      NodeMod syflexSwitchMod =
	registerNode(mclient, syflexSwitchName, "ma", pToolset, getEditor(mclient, "Temerity",
	"MayaProject", pToolset));
      linkModels(mclient, namespaceToSimNode.values(), syflexSwitchName);
      mclient.link(pAuthor, pView, syflexSwitchName, pPrimary, LinkPolicy.Dependency, LinkRelationship.All, null);
      BaseAction act = getAction(mclient, "SCEA", "ModelReplace", pToolset);
      act.setSingleParamValue("Source", pPrimary);
      act.setSingleParamValue("Response", "Ignore");
      syflexSwitchMod.setAction(act);
      mclient.modifyProperties(pAuthor, pView, syflexSwitchMod);
    }
    
    /*
     * The hirez scene with all its references imported.
     */
    syflexEndName = pPrimary.replaceAll("anim", "syf");
    syflexEndName = syflexEndName.replaceAll("_syf", "_end");
    if(doesNodeExists(mclient, syflexEndName))
      throw new PipelineException("This tool was already run once, cannot be run again");

    {
      NodeMod syflexEndMod =
        registerNode(mclient, syflexEndName, "ma", pToolset, getEditor(mclient, "Temerity",
  	"MayaProject", pToolset));
      mclient.link(pAuthor, pView, syflexEndName, syflexSwitchName, LinkPolicy.Dependency,
        LinkRelationship.All, null);

      BaseAction act = getAction(mclient, "Temerity", "MayaBuild", pToolset);
      act.setSingleParamValue("TimeUnits", "NTSC (30 fps)");
      act.setSingleParamValue("StartFrame", pUserStart);
      act.setSingleParamValue("EndFrame", pUserEnd);
  
      act.initSourceParams(syflexSwitchName);
      act.setSourceParamValue(syflexSwitchName, "BuildType", "Import");
      act.setSourceParamValue(syflexSwitchName, "NameSpace", false);
      syflexEndMod.setAction(act);
      mclient.modifyProperties(pAuthor, pView, syflexEndMod);
    }

    jobGroup = mclient.submitJobs(pAuthor, pView, syflexEndName, null).getFirst();

    boolean next = false;
    while (!next)
    {
      next = queuePhase1(mclient, qclient);
    }
    
    buildPhase(mclient, qclient);
    
    next = false;
    while (!next)
    {
      next = queuePhase2(mclient, qclient);
    }
    
    boolean queueFinal = runSimField.getValue();
    if (queueFinal)
      mclient.submitJobs(pAuthor, pView, globalVerifyScene.toString() , null);
    
    return false;
  }

  /**
   * checks if the switch and end nodes have finished running.<p>
   * 
   * If they have, returns true.  Else, return false, which continues the loop.  
   * Sleeps for 5 second on every invocation to keep from hammering the queue manager.  
   * @param mclient
   * @param qclient
   * @return
   * 	if the jobs are done yet.
   * @throws PipelineException
   */
  private boolean queuePhase1(MasterMgrClient mclient, QueueMgrClient qclient)
    throws PipelineException
  {
    try {
      Thread.sleep(5000);
    }
    catch(InterruptedException e) {
      e.printStackTrace();
    }

    boolean done = false;
    boolean error = false;

    while(!done) {
      TreeSet<Long> stuff = new TreeSet<Long>();
      stuff.add(jobGroup.getGroupID());
      TreeMap<Long, JobStatus> statuses = qclient.getJobStatus(stuff);
      for(JobStatus status : statuses.values()) {
	System.err.println("JobID: " + status.getJobID());
	JobState state = status.getState();
	System.err.println("State: " + state);
	if(state.equals(JobState.Failed) || state.equals(JobState.Aborted)) {
	  error = true;
	  break;
	}
	if(!state.equals(JobState.Finished)) {
	  done = false;
	  break;
	}
	done = true;
      }
      System.err.println("Value of Done: " + done);
      System.err.println("Value of Error: " + done);
      if(error)
	throw new PipelineException("The job for the (" + syflexEndName
	  + ") did not complete correctly.");
      if(!done)
	break;
    }
    System.err.println();
    return done;
  }

  /**
   *  Builds the entire node network, based on the settings that the user gave. 
   * @param mclient
   * @param qclient
   * @throws PipelineException
   */
  private void buildPhase(MasterMgrClient mclient, QueueMgrClient qclient)
    throws PipelineException
  {

    /*
     * The realStart is based on the lower of the actual start of animation and
     * what the user stated was the start frame.
     */ 
    int realStart = ( ( pStart < pUserStart ) ? pStart : pUserStart ) + 1000;
    int realEnd = pUserEnd + 1000;
    log.println("starting second phase");

    /*
     * This mel script contains the logic that creates the bind pose for the characters
     * as well as offseting the animation.
     */
    String clothPoseMel = deriveMelStart(pPrimary) + "/clothBasePose";
    jcheckOut(mclient, pAuthor, pView, clothPoseMel, null, CheckOutMode.OverwriteAll,
      CheckOutMethod.PreserveFrozen);

    Path syflexStart = buildSyflexStart(pPrimary);
    String nameStart = buildNameStart(pPrimary);

    globalVerifyScene = new Path(syflexStart, nameStart + "_cache");
    pRoots.add(globalVerifyScene.toOsString());

    log.println("Building Global scene " + globalVerifyScene.toString());

    NodeMod globalVerifyMod = registerNode(mclient, globalVerifyScene.toString(), "ma",
      pToolset, getEditor(mclient, "Temerity", "MayaProject", pToolset));
    mclient.link(pAuthor, pView, globalVerifyScene.toString(), syflexEndName,
      LinkPolicy.Dependency, LinkRelationship.All, null);

    log.println(namespacesToSim);
    TreeSet<String> groupZero = namespacesToSim.get(0);
    /*
     * First we're going to do all the characters that don't depend on other characters.
     * Each character in here has a separate sim network built for it.
     */
    if ( groupZero != null )
    {
      log.println("There is a group zero");
      for (String namespace : groupZero)
      {
	log.println("AssetName: " + namespace);
	String assetSimName = namespaceToSimNode.get(namespace);
	jcheckOut(mclient, pAuthor, pView, assetSimName, null, CheckOutMode.KeepModified,
	  CheckOutMethod.PreserveFrozen);
	Path simStart = new Path(syflexStart, namespace);
	Path simScene = new Path(simStart, nameStart + "_syf_" + namespace);
	Path simCacheStart = new Path(simStart, "cache");
	Path simFilesStart = new Path(simStart, "sim");
	log.println("SimScene Being registered " + simScene.toString());

	// Creates the scene to do the simulation.
	NodeMod simSceneMod = registerNode(mclient, simScene.toString(), "ma",
	  pToolsetSyflexClient, getEditor(mclient, "Temerity", "MayaProject", pToolsetSyflexClient));
	{
	  BaseAction act = getAction(mclient, "SCEA", "ModelReplace", pToolsetSyflexClient);
	  mclient.link(pAuthor, pView, simScene.toString(), assetSimName,
	    LinkPolicy.Dependency, LinkRelationship.All, null);
	  mclient.link(pAuthor, pView, simScene.toString(), clothPoseMel,
	    LinkPolicy.Dependency, LinkRelationship.All, null);
	  mclient.link(pAuthor, pView, simScene.toString(), pPrimary,
	    LinkPolicy.Dependency, LinkRelationship.All, null);
	  act.setSingleParamValue("TargetSuffix", "syf");
	  // act.setSingleParamValue("SourceSuffix", "lr");
	  act.setSingleParamValue("Response", "Remove");
	  act.setSingleParamValue("Source", pPrimary);
	  act.setSingleParamValue("PostReplaceMEL", clothPoseMel);
	  simSceneMod.setAction(act);
	  doReqs(simSceneMod);
	  mclient.modifyProperties(pAuthor, pView, simSceneMod);
	}
	simNodesToQueue.add(simScene.toString());

	/*
	 * Builds a map of all the cloth pieces by channel.
	 */
	TreeMap<Integer, TreeSet<String>> piecesByOrder = new TreeMap<Integer, TreeSet<String>>();
	for (String piece : clothOrderInfo.keySet(0, namespace))
	{
	  int order = clothOrderInfo.get(0, namespace, piece);
	  TreeSet<String> pieces = piecesByOrder.get(order);
	  if ( pieces == null )
	    pieces = new TreeSet<String>();
	  pieces.add(piece);
	  piecesByOrder.put(order, pieces);
	}
	TreeSet<String> zeroPieces = piecesByOrder.get(0);
	
	/*
	 * First we're going to do all the pieces of cloth that don't depend on any
	 * other pieces of cloth. 
	 */  
	if ( zeroPieces != null )
	{
	  piecesByOrder.remove(0);
	  for (String piece : zeroPieces)
	  {
	    String buffer[] = piece.split(":");
	    String pieceName = buffer[buffer.length - 1];
	    Path cacheNodeName = new Path(simStart, pieceName);
	    Path simCacheNodeName = new Path(simCacheStart, pieceName);
	    buildCacheNodes(mclient, realStart, realEnd, simScene, piece, cacheNodeName,
	      simCacheNodeName);
	    mclient.link(pAuthor, pView, globalVerifyScene.toString(), cacheNodeName
	      .toString(), LinkPolicy.Dependency, LinkRelationship.All, null);
	  }
	} // if (zeroPieces != null)
	/*
	 * Now we do everything else, starting with the first channel and working out
	 * way along. 
	 */
	if ( piecesByOrder.size() > 0 )
	{
	  Path previousSimScene = simScene;
	  int simSceneNumber = 1;

	  for (int order : piecesByOrder.keySet())
	  {
	    log.println("Current Order: " + order);
	    log.println("Previous Sim: " + previousSimScene);
	    
	    // Is there another channel after this one.
	    boolean isAnotherStep = true;
	    if ( order == piecesByOrder.lastKey() )
	      isAnotherStep = false;

	    /*
	     * This will be the name of the node where we apply all the caches from
	     * this run to create a simulation scene for the next step.
	     */
	    Path nextStep = null;
	    NodeMod nextStepMod;
	    // If there isn't a next step, then we don't need to build this node.
	    if ( isAnotherStep )
	    {
	      log.println("There is another step");
	      nextStep = new Path(simFilesStart, nameStart + "_syf_" + namespace + "_s"
		  + simSceneNumber);
	      log.println("Next Step: " + nextStep.toString());
	      nextStepMod = registerNode(mclient, nextStep.toString(), "ma", pToolsetSyflexClient,
		getEditor(mclient, "Temerity", "MayaProject", pToolsetSyflexClient));
	      mclient.link(pAuthor, pView, nextStep.toString(),
		previousSimScene.toString(), LinkPolicy.Dependency, LinkRelationship.All,
		null);
	      BaseAction act = getAction(mclient, "Temerity", "SyflexApplyCache", pToolsetSyflexClient);
	      act.setSingleParamValue("MayaScene", previousSimScene.toString());
	      nextStepMod.setAction(act);
	      doReqs(nextStepMod);
	      mclient.modifyProperties(pAuthor, pView, nextStepMod);
	    }
	    // Now loop over all the pieces in this step.
	    for (String piece : piecesByOrder.get(order))
	    {
	      String buffer[] = piece.split(":");
	      String pieceName = buffer[buffer.length - 1];
	      Path cacheNodeName = new Path(simStart, pieceName);
	      Path simCacheNodeName = new Path(simCacheStart, pieceName);
	      log.println("Cache Node Name: " + cacheNodeName.toString());
	      log.println("Sim Cache Node Name: " + simCacheNodeName.toString());
	      try
	      {
		buildCacheNodes(mclient, realStart, realEnd, previousSimScene, piece,
		  cacheNodeName, simCacheNodeName);
	      } catch ( Exception ex )
	      {
		log.close();
		throw new PipelineException(ex.getStackTrace().toString());
	      }
	      mclient.link(pAuthor, pView, globalVerifyScene.toString(), cacheNodeName
		.toString(), LinkPolicy.Dependency, LinkRelationship.All, null);
	      // Link to the new simulation scene if there is another step
	      if ( isAnotherStep )
	      {
		mclient.link(pAuthor, pView, nextStep.toString(), simCacheNodeName
		  .toString(), LinkPolicy.Dependency, LinkRelationship.All, null);
		simSceneNumber++;
	      }
	    } // for (String piece : piecesByOrder.get(order))
	    previousSimScene = nextStep;
	  } // for (int order : piecesByOrder.keySet())
	} // if ( piecesByOrder.size() > 0 )
      } // for (String namespace : groupZero)

      namespacesToSim.remove(0);
      // namespaceToSimNode.remove(0);
    } // if ( groupZero != null )

    log.println("Done with all the Zero case characters");
    
    /* 
     * Now do the exact same thing, but with multiple characters in each sim scene.
     * I'm not going to comment out the logic here.  It's basically exactly the same
     * as everything that was done above, just with more than one character per sim
     * scene.  Consult the comments for the single character section to get an 
     * explanation of what I'm doing.
    */ 
    for (int charGrouping : namespacesToSim.keySet())
    {
      log.println("doing character grouping " + charGrouping);
      TreeSet<String> allGroupNamespaces = namespacesToSim.get(charGrouping);
      TreeSet<String> assetSims = new TreeSet<String>();
      for (String namespace : allGroupNamespaces)
      {
	log.println("AssetName: " + namespace);
	String assetSimName = namespaceToSimNode.get(namespace);
	assetSims.add(assetSimName);
	jcheckOut(mclient, pAuthor, pView, assetSimName, null, CheckOutMode.KeepModified,
	  CheckOutMethod.PreserveFrozen);
      }
      String simName = "sim" + charGrouping;
      Path simGlobalStart = new Path(syflexStart, simName);
      Path simScene = new Path(simGlobalStart, nameStart + "_syf_" + simName);
      Path simFilesStart = new Path(simGlobalStart, "sim");
      log.println("SimScene Being registered " + simScene.toString());

      NodeMod simSceneMod = registerNode(mclient, simScene.toString(), "ma",
	pToolsetSyflexClient, getEditor(mclient, "Temerity", "MayaProject", pToolsetSyflexClient));
      {
	BaseAction act = getAction(mclient, "SCEA", "ModelReplace", pToolsetSyflexClient);
	// linking all the asset syflex nodes for this channel
	for (String assetSimName : assetSims)
	{
	  mclient.link(pAuthor, pView, simScene.toString(), assetSimName,
	    LinkPolicy.Dependency, LinkRelationship.All, null);
	}
	mclient.link(pAuthor, pView, simScene.toString(), clothPoseMel,
	  LinkPolicy.Dependency, LinkRelationship.All, null);
	mclient.link(pAuthor, pView, simScene.toString(), pPrimary, LinkPolicy.Dependency,
	  LinkRelationship.All, null);
	act.setSingleParamValue("TargetSuffix", "syf");
	// act.setSingleParamValue("SourceSuffix", "lr");
	act.setSingleParamValue("Response", "Remove");
	act.setSingleParamValue("Source", pPrimary);
	act.setSingleParamValue("PostReplaceMEL", clothPoseMel);
	simSceneMod.setAction(act);
	doReqs(simSceneMod);
	mclient.modifyProperties(pAuthor, pView, simSceneMod);
      }
      simNodesToQueue.add(simScene.toString());

      DoubleMap<Integer, String, TreeSet<String>> piecesBySpaceAndOrder = new DoubleMap<Integer, String, TreeSet<String>>();
      for (String namespace : clothOrderInfo.keySet(charGrouping))
	for (String piece : clothOrderInfo.keySet(charGrouping, namespace))
	{
	  int order = clothOrderInfo.get(charGrouping, namespace, piece);
	  TreeSet<String> pieces = piecesBySpaceAndOrder.get(order, namespace);
	  if ( pieces == null )
	    pieces = new TreeSet<String>();
	  pieces.add(piece);
	  piecesBySpaceAndOrder.put(order, namespace, pieces);
	}

      TreeMap<String, TreeSet<String>> zeroPieces = piecesBySpaceAndOrder.get(0);
      if ( zeroPieces != null )
      {
	piecesBySpaceAndOrder.remove(0);
	for (String namespace : zeroPieces.keySet())
	  for (String piece : zeroPieces.get(namespace))
	  {
	    Path simIndividualStart = new Path(syflexStart, namespace);
	    Path simCacheStart = new Path(simIndividualStart, "cache");
	    String buffer[] = piece.split(":");
	    String pieceName = buffer[buffer.length - 1];
	    Path cacheNodeName = new Path(simIndividualStart, pieceName);
	    Path simCacheNodeName = new Path(simCacheStart, pieceName);
	    buildCacheNodes(mclient, realStart, realEnd, simScene, piece, cacheNodeName,
	      simCacheNodeName);
	    mclient.link(pAuthor, pView, globalVerifyScene.toString(), cacheNodeName
	      .toString(), LinkPolicy.Dependency, LinkRelationship.All, null);
	  }
      } // if (zeroPieces != null)
      if ( piecesBySpaceAndOrder.size() > 0 )
      {
	Path previousSimScene = simScene;
	int simSceneNumber = 1;

	for (int order : piecesBySpaceAndOrder.keySet())
	{
	  TreeMap<String, TreeSet<String>> groupPieces = piecesBySpaceAndOrder.get(order);
	  log.println("Current Order: " + order);
	  log.println("Previous Sim: " + previousSimScene);
	  boolean isAnotherStep = true;
	  if ( order == piecesBySpaceAndOrder.lastKey() )
	    isAnotherStep = false;
	  Path nextStep = null;
	  NodeMod nextStepMod;
	  if ( isAnotherStep )
	  {
	    log.println("There is another step");
	    nextStep = new Path(simFilesStart, nameStart + "_syf_" + simName + "_s"
		+ simSceneNumber);
	    log.println("Next Step: " + nextStep.toString());
	    nextStepMod = registerNode(mclient, nextStep.toString(), "ma", pToolsetSyflexClient,
	      getEditor(mclient, "Temerity", "MayaProject", pToolsetSyflexClient));
	    mclient.link(pAuthor, pView, nextStep.toString(), previousSimScene.toString(),
	      LinkPolicy.Dependency, LinkRelationship.All, null);
	    BaseAction act = getAction(mclient, "Temerity", "SyflexApplyCache", pToolsetSyflexClient);
	    act.setSingleParamValue("MayaScene", previousSimScene.toString());
	    nextStepMod.setAction(act);
	    doReqs(nextStepMod);
	    mclient.modifyProperties(pAuthor, pView, nextStepMod);
	  }
	  for (String namespace : groupPieces.keySet())
	    for (String piece : groupPieces.get(namespace))
	    {
	      Path simIndividualStart = new Path(syflexStart, namespace);
	      Path simCacheStart = new Path(simIndividualStart, "cache");
	      String buffer[] = piece.split(":");
	      String pieceName = buffer[buffer.length - 1];
	      Path cacheNodeName = new Path(simIndividualStart, pieceName);
	      Path simCacheNodeName = new Path(simCacheStart, pieceName);
	      log.println("Cache Node Name: " + cacheNodeName.toString());
	      log.println("Sim Cache Node Name: " + simCacheNodeName.toString());
	      try
	      {
		buildCacheNodes(mclient, realStart, realEnd, previousSimScene, piece,
		  cacheNodeName, simCacheNodeName);
	      } catch ( Exception ex )
	      {
		log.close();
		throw new PipelineException(ex.getStackTrace().toString());
	      }
	      mclient.link(pAuthor, pView, globalVerifyScene.toString(), cacheNodeName
		.toString(), LinkPolicy.Dependency, LinkRelationship.All, null);
	      if ( isAnotherStep )
	      {
		mclient.link(pAuthor, pView, nextStep.toString(), simCacheNodeName
		  .toString(), LinkPolicy.Dependency, LinkRelationship.All, null);
		simSceneNumber++;
	      }
	    } // for (String piece : piecesByOrder.get(order))
	  previousSimScene = nextStep;
	} // for (int order : piecesByOrder.keySet())
      } // if ( piecesByOrder.size() > 0 )
    } // for (int charGrouping : namespacesToSim.keySet())

    buildCacheVerifyNode(mclient, new TreeSet<String>(namespaceToSimNode.values()),
      globalVerifyMod, globalVerifyScene);
    
    // Now queue everything
    for (String simNode : simNodesToQueue)
    {
      jobGroups.add(mclient.submitJobs(pAuthor, pView, simNode, null).getFirst());
    }
  }

  /**
   * Waits for all the simulation scenes to be built.<P>
   * Once they are built, it disconnects the animation nodes from them
   * so that they are self-contained away from the rest of the scene tree.
   * @param mclient
   * @param qclient
   * @return
   * @throws PipelineException
   */
  private boolean queuePhase2(MasterMgrClient mclient, QueueMgrClient qclient)
  throws PipelineException
{

  try
  {
    Thread.sleep(5000);
  } catch ( InterruptedException e )
  {
    e.printStackTrace();
  }

  boolean done = false;
  boolean error = false;

  
  while ( !done )
  {
    TreeSet<Long> stuff = new TreeSet<Long>();
    for (QueueJobGroup group : jobGroups)
    {
      stuff.add(group.getGroupID());
    }
    TreeMap<Long, JobStatus> statuses = qclient.getJobStatus(stuff);
    for (JobStatus status : statuses.values())
    {
	JobState state = status.getState();
	if ( state.equals(JobState.Failed) || state.equals(JobState.Aborted) )
	{
	  error = true;
	  break;
	}
	if ( !state.equals(JobState.Finished) )
	{
	  done = false;
	  break;
	}
	done = true;
    }
    if ( error )
	throw new PipelineException("The jobs did not complete correctly.");
    if ( !done )
	break;
  }
  if ( done )
  {
    for (String simNode : simNodesToQueue)
    {
      mclient.unlink(pAuthor, pView, simNode, pPrimary);
      NodeMod simNodeMod = mclient.getWorkingVersion(pAuthor, pView, simNode);
      simNodeMod.setAction(null);
      mclient.modifyProperties(pAuthor, pView, simNodeMod);
    }
    log.close();
    return true;
  }
  System.err.println();
  return false;
}
 
  /**
   * Links all the rig nodes to the switch node.
   * @param mclient
   * @param syfSrcs
   * @param syflexSwitchNode
   * @throws PipelineException
   */
  private void linkModels(MasterMgrClient mclient, Collection<String> syfSrcs, String syflexSwitchNode)
    throws PipelineException
  {
    for(String syfSrc : syfSrcs) {
      System.err.println("\n" + syfSrc);
      if(syfSrc.matches(syfRigPattern)) {
	String rigName = syfSrc.replaceAll("_syf", "_rig");
	mclient.link(pAuthor, pView, syflexSwitchNode, rigName, 
	  LinkPolicy.Dependency, LinkRelationship.All, null);
      }
    }
  }
  
  /**
   * Sets the action and the params for the final cache verification node.
   * 
   * @param mclient
   * @param syfSrcs
   * @param globalVerifyMod
   * @param globalVerifyScene
   * @throws PipelineException
   */
  private void buildCacheVerifyNode(
    MasterMgrClient mclient,
    TreeSet<String> syfSrcs,
    NodeMod globalVerifyMod,
    Path globalVerifyScene) throws PipelineException
  {
    BaseAction action = getAction(mclient, "Temerity", "SyflexApplyCache", pToolset);

    action.setSingleParamValue("MayaScene", syflexEndName);
    globalVerifyMod.setAction(action);
    doReqs(globalVerifyMod);
    mclient.modifyProperties(pAuthor, pView, globalVerifyMod);
  }

  /**
   * Builds the cache nodes. <P>
   * First builds the actual sim node, which is has its frame range offset into the future.
   * Then builds the final cache node which has the actual correct frame range.
   * 
   * @param mclient
   * @param realStart
   * @param realEnd
   * @param simScene
   * @param piece
   * @param cacheNodeName
   * @param simCacheNodeName
   * @throws PipelineException
   */
  private void buildCacheNodes(MasterMgrClient mclient, int realStart, int realEnd,
      Path simScene, String piece, Path cacheNodeName, Path simCacheNodeName)
    throws PipelineException
  {
    log.println("Running BuildCacheNodes");
    log.println("Cache Node: " + cacheNodeName.toString());
    log.println("SimScene: " + simScene.toString());

    NodeMod simCacheNodeMod = registerSequence(mclient, pToolsetSyflexFarm,
      simCacheNodeName.toString(), 4, null, null, realStart - 150, realEnd, 1);
    {
      BaseAction act = getAction(mclient, "Temerity", "SyflexCache", pToolsetSyflexFarm);
      mclient.link(pAuthor, pView, simCacheNodeName.toString(), simScene.toString(),
	LinkPolicy.Dependency, LinkRelationship.All, null);
      act.setSingleParamValue("MayaScene", simScene.toString());
      act.setSingleParamValue("ClothObject", piece);
      simCacheNodeMod.setAction(act);
      doSimReqs(simCacheNodeMod);
      mclient.modifyProperties(pAuthor, pView, simCacheNodeMod);
    }
    NodeMod cacheNodeMod = registerSequence(mclient, pToolset, cacheNodeName
      .toString(), 4, null, null, pUserStart, pUserEnd, 1);
    {
      BaseAction act = getAction(mclient, "Temerity", "SyflexCacheCopy", pToolset);
      int offset = 150 + ( pUserStart - pStart );
      mclient.link(pAuthor, pView, cacheNodeName.toString(), simCacheNodeName.toString(),
	LinkPolicy.Dependency, LinkRelationship.OneToOne, offset);
      act.setSingleParamValue("ClothObject", piece);
      cacheNodeMod.setAction(act);
      doReqs(cacheNodeMod);
      cacheNodeMod.setExecutionMethod(ExecutionMethod.Parallel);
      cacheNodeMod.setBatchSize(25);
      mclient.modifyProperties(pAuthor, pView, cacheNodeMod);
    }
  }

  /**
   * generates the name of the basic syflex node.
   */
  private static String buildNameStart(String primary)
  {
    String toReturn = new Path(primary).getName();
    toReturn = toReturn.replace("_anim", "");
    toReturn = toReturn.replace("_syf", "");
    return toReturn;
  }

  /**
   * Generates the directory in which all syflex nodes will live.
   */
  private static Path buildSyflexStart(String primary)
  {
    Path p = new Path(primary);
    p = p.getParentPath();
    p = new Path(p.getParentPath(), "syf");
    return p;
  }
  
  /**
   * Generates the list of channels.  Starting with None and
   * then containing a one based list up to and including size.
   * @param size
   * 	The number of channels to generate.
   * @return
   */
  private ArrayList<String> buildList(int size)
  {
    ArrayList<String> toReturn = new ArrayList<String>();
    toReturn.add("None");
    for (int i = 1; i <= size; i++)
    {
      toReturn.add(String.valueOf(i));
    }
    return toReturn;
  }

  /**
   * turns an absoulte path into a node name.
   */
  private String pathToNode(String path)
  {
    path = path.replace(PackageInfo.sWorkPath.toString(), "");
    path = path.replace(".ma", "");
    Path p = new Path(path);
    ArrayList<String> list = p.getComponents();
    int size = list.size();
    Path newPath = new Path("/");
    for (int i = 2; i < size; i++)
      newPath = new Path(newPath, list.get(i));
    return newPath.toString();
  }

  /**
   * Uses the project and other information contained in the animation node
   * path to determine where the mel script directory for this project lives.
   */
  private Path deriveMelStart(String path)
  {
    Path toReturn = new Path("/");
    ArrayList<String> parts = new Path(path).getComponents();
    for (String part : parts)
    {
      if ( part.equals("production") )
	break;
      toReturn = new Path(toReturn, part);

    }
    toReturn = new Path(toReturn, "assets");
    toReturn = new Path(toReturn, "tools");
    toReturn = new Path(toReturn, "mel");
    return toReturn;
  }

  /**
   * Sets the Lair key.<p>
   * This method should be replaced with one that is multi-project friendly.
   */
  private void doReqs(NodeMod mod) throws PipelineException
  {
    JobReqs req = mod.getJobRequirements();
    req.addSelectionKey("Lair");
    mod.setJobRequirements(req);
  }
  
  /**
   * Sets the Lair selection key and the SyflexClient License key.<p>
   * This method should be replaced with one that is multi-project friendly.
   */
  private void doSimSceneReqs(NodeMod mod) throws PipelineException
  {
    JobReqs req = mod.getJobRequirements();
    req.addSelectionKey("Lair");
    req.addLicenseKey("SyflexClient");
    mod.setJobRequirements(req);
  }

  /**
   * Sets the Lair and Animation selection keys and the SyflexFarm License key.<p>
   * This method should be replaced with one that is multi-project friendly.
   */
  private void doSimReqs(NodeMod mod) throws PipelineException
  {
    JobReqs req = mod.getJobRequirements();
    req.addSelectionKey("Lair");
    req.addSelectionKey("Animation");
    req.addLicenseKey("SyflexFarm");
    mod.setJobRequirements(req);
  }

  /**
   * Converts a low-rez character node name in to a node name for the 
   * syflex simulation scene.
   */
  private static String charToSim(String node)
  {
    Path p = new Path(node);
    String name = p.getName().replace("_lr", "_syf");
    Path newPath = new Path(p.getParentPath(), "rig");
    newPath = new Path(newPath, name);
    return newPath.toString();
  }

  private static final String fixPath(String path)
  {
    return path.replaceAll("\\\\", "/");
  }

  public boolean doesNodeExists(MasterMgrClient mclient, String name)
    throws PipelineException
  {
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(name, false);
    NodeTreeComp treeComps = mclient.updatePaths(pAuthor, pView, comps);
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

  private NodeMod registerNode(MasterMgrClient client, String name, String extention,
      String toolset, BaseEditor editor) throws PipelineException
  {
    File f = new File(name);
    FileSeq animSeq = new FileSeq(f.getName(), extention);
    NodeMod animNode = new NodeMod(name, animSeq, null, toolset, editor);
    client.register(pAuthor, pView, animNode);
    return animNode;
  }

  private NodeMod registerSequence(MasterMgrClient client, String toolset, String name,
      int pad, String extention, BaseEditor editor, int startF, int endf, int byF)
    throws PipelineException
  {
    Path p = new Path(name);
    FilePattern pat = new FilePattern(p.getName(), pad, extention);
    FrameRange range = new FrameRange(startF, endf, byF);
    FileSeq animSeq = new FileSeq(pat, range);
    NodeMod animNode = new NodeMod(name, animSeq, null, toolset, editor);
    client.register(pAuthor, pView, animNode);
    return animNode;
  }

  public BaseAction getAction(MasterMgrClient mclient, String vendor, String name, String toolset)
    throws PipelineException
  {
    DoubleMap<String, String, TreeSet<VersionID>> plugs = mclient
      .getToolsetActionPlugins(toolset);
    VersionID ver = plugs.get(vendor, name).last();

    return plug.newAction(name, ver, vendor);
  }

  public BaseEditor getEditor(MasterMgrClient mclient, String vendor, String name, String toolset)
    throws PipelineException
  {
    DoubleMap<String, String, TreeSet<VersionID>> plugs = mclient
      .getToolsetEditorPlugins(toolset);
    VersionID ver = plugs.get(vendor, name).last();

    return plug.newEditor(name, ver, vendor);
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

  private static String animPattern = ".*/production/.*/anim/.*_anim";

  // private static String syfPattern = ".*/production/.*/syf/.*_syf";
  private static String syfRigPattern = ".*/assets/(character|set|prop)/.*/rig/.*_syf";

  // private static String loresPattern =
  // ".*/assets/(character|set|prop)/.*/[a-zA-Z0-9]+_lr";

  private int pPhase;

  private TreeMap<String, TreeSet<String>> namespaceToCloth;

  private TreeMap<String, String> namespaceToCharacter;

  private TreeMap<String, Integer> namespaceToStart;

  private TreeMap<String, String> namespaceToSimNode;

  private TreeMap<String, Integer> namespaceToEnd;

  private TripleMap<Integer, String, String, JCollectionField> clothOrderFields;

  private TripleMap<Integer, String, String, JBooleanField> clothSimFields;

  private TripleMap<Integer, String, String, Integer> clothOrderInfo;

  private TreeMap<String, JCollectionField> multipleCharChanField;

  private TreeMap<String, JBooleanField> charSimFields;

  private TreeMap<Integer, TreeSet<String>> namespacesToSim;
  
  private TreeSet<String> simNodesToQueue;

  //private String syflexStartName;
  /**
   * A scene which represents the animation.  Has all the rigs and
   * animation imported, so that is stands alone from the regular shot network.
   */
  private String syflexEndName;
  /**
   * A scene which replaces the low rez models in the animation scene with
   * the rig models.  This is used to build the end node.  
   * <p>
   * This is necessary since the low rez models have the cloth removed from 
   * them for speed purposes, while the rig scenes have the cloth, but not the
   * shaders and materials of the high-rez models.
   */
  private String syflexSwitchName;
  /**
   * The scene that the caches are applied to at the end to make sure they all work.
   */
  private Path globalVerifyScene;

  private JIntegerField startField;

  private JIntegerField endField;
  
  private JBooleanField runSimField;

  /**
   * The start frame of animation from the Maya scene
   */
  private int pStart;

  /**
   * The end frame of animation from the Maya scene
   */
  private int pEnd;

  /**
   * The start frame that the user inputed
   */
  private int pUserStart;

  /**
   * The end frame the user inputed.
   */
  private int pUserEnd;

  private QueueJobGroup jobGroup;
  private ArrayList<QueueJobGroup> jobGroups;
  
  private PrintWriter log;

  private static final int sTSize = 150;

  private static final int sVSize = 300;

  private static final long serialVersionUID = -9053053360342889270L;

  private static final PluginMgrClient plug = PluginMgrClient.getInstance();

  private String pAuthor;

  private String pView;

  private String pToolset;

  private String pToolsetSyflexFarm;

  private String pToolsetSyflexClient;

  // private String syfPrim = null;
  // private String switchName = null;
//
//  private static final String aPreReplace = "PreReplaceMEL";
//
//  private static final String aSource = "Source";
//
//  private static final String aPostReplace = "PostReplaceMEL";
//
//  private static final String aResponse = "Response";
//
//  private static final String aRemove = "Remove";
//
//  private static final String aApplyCache = "ApplyCache";

}