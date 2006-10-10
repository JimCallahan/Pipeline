package us.temerity.pipeline.plugin.v2_0_10;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.util.*;
import java.io.*;
import java.awt.*;

import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   A D D   R E F E R E N C E   T O O L                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Adds selected source models to the target scene as references in both Pipline and Maya.
 * <p>
 * This tool will also go ahead and add the appropriate values to the MayaBuild Action (if
 * that is the Action for the node being modified) so that the MayaBuild Action will
 * correctly rebuild the scene if it is enabled.
 */
public 
class MayaAddReferenceTool
  extends BaseTool
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public MayaAddReferenceTool() 
  {
    super("MayaAddReference", new VersionID("2.0.10"), "Temerity",
      "Adds references to a Maya scene and makes the correct links in pipeline");

    pNameSpaceMap = new TreeMap<String, JTextField>();
    pSourceFiles = new TreeMap<String, Path>();

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);

    underDevelopment();
  }



  /*----------------------------------------------------------------------------------------*/
  /*  O P S                                                                                 */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Create and show graphical user interface components to collect information from the 
   * user to use as input in the next phase of execution for the tool. <P> 
   * 
   * @return 
   *   The phase progress message or <CODE>null</CODE> to abort early.
   * 
   * @throws PipelineException 
   *   If unable to validate the given user input.
   */  
  public String 
  collectPhaseInput() 
    throws PipelineException
  {
    { // error checking
      int size = pSelected.keySet().size();

      if(pPrimary == null)
	throw new PipelineException("Must have a target node.");

      if(size < 2)
	throw new PipelineException("Must have at least two nodes selected");

      NodeStatus stat = pSelected.get(pPrimary);
      Set<String> allSources = stat.getSourceNames();
      for(String s : pSelected.keySet()) {
	if(s.equals(pPrimary))
	  continue;
	if(allSources.contains(s))
	  throw new PipelineException("The selected node (" + s
	    + ") is already a source node of" + pPrimary);
      }
    }

    NodeID id = pSelected.get(pSelected.firstKey()).getNodeID();
    pAuthor = id.getAuthor();
    pView = id.getView();

    JScrollPane scroll = null;
    {
      Box vbox = new Box(BoxLayout.Y_AXIS);

      for(String sourceName : pSelected.keySet()) {
	if(!sourceName.equals(pPrimary)) {
	  Box hbox = new Box(BoxLayout.X_AXIS);
	  Component comps[] = UIFactory.createTitledPanels();
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];

	  UIFactory.createTitledTextField(tpanel, "Selected Node:", sTSize, vpanel,
	    sourceName, sVSize, "The node that is going to be added as a reference.");

	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	  JTextField field =
	    UIFactory.createTitledEditableTextField(tpanel, "Namespace:", sTSize, vpanel,
	      null, sVSize, "The Maya Namespace for each field.");

	  pNameSpaceMap.put(sourceName, field);

	  hbox.add(comps[2]);
	  JDrawer drawer = new JDrawer("Source Node: " + sourceName, hbox, true);
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

      {
	scroll = new JScrollPane(vbox);

	scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

	Dimension size = new Dimension(sTSize + sVSize, 500);
	scroll.setMinimumSize(size);
      }
    }

    JToolDialog diag = new JToolDialog("Maya Reference Tool", scroll, "Confirm");

    diag.setVisible(true);
    if(diag.wasConfirmed()) {
      return ": Adding References";
    }
    return null;
  }
 
  /**
   * Perform one phase in the execution of the tool. <P> 
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
  public boolean 
  executePhase
  (
   MasterMgrClient mclient, 
   QueueMgrClient qclient
  )
    throws PipelineException
  {

    NodeID targetID = new NodeID(pAuthor, pView, pPrimary);
    NodeMod targetMod = pSelected.get(pPrimary).getDetails().getWorkingVersion();

    Path targetPath;

    if(targetMod == null)
      throw new PipelineException("No working version of the Target Scene Node (" + pPrimary
	+ ") exists " + "in the (" + pView + ") working area owned by (" + PackageInfo.sUser
	+ ")!");

    {
      FileSeq fseq = targetMod.getPrimarySequence();
      String suffix = fseq.getFilePattern().getSuffix();
      if(!fseq.isSingle() || (suffix == null)
	|| (!suffix.equals("ma") && !suffix.equals("mb")))
	throw new PipelineException("The target node (" + pPrimary
	  + ") must be a maya scene!");
      targetPath =
	new Path(PackageInfo.sProdPath, targetID.getWorkingParent() + "/" + fseq.getFile(0));
    }

    {
      for(String sourceName : pSelected.keySet()) {
	if(!sourceName.equals(pPrimary)) {
	  NodeMod sourceMod = pSelected.get(sourceName).getDetails().getWorkingVersion();

	  FileSeq fseq = sourceMod.getPrimarySequence();
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null)
	    || !(suffix.equals("ma") || suffix.equals("mb")))
	    throw new PipelineException("The source node (" + sourceName
	      + ") must be a maya scene!");
	  Path p = new Path(sourceName);
	  Path sourcePath = new Path(p.getParent() + "/" + fseq.getPath(0));
	  pSourceFiles.put(sourceName, sourcePath);
	}
      }
    }

    /* writing the mel script */

    {
      File script = null;
      try {
	script =
	  File.createTempFile("AddReferenceTool.", ".mel", PackageInfo.sTempPath.toFile());
	FileCleaner.add(script);
      }
      catch(IOException ex) {
	throw new PipelineException(
	  "Unable to create the temporary MEL script used to collect "
	    + "texture information from the Maya scene!");
      }

      try {
	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(script)));

	for(String sourceName : pSourceFiles.keySet()) {
	  Path path = pSourceFiles.get(sourceName);

	  String nameSpace = pNameSpaceMap.get(sourceName).getText();

	  out.println("// MODEL: \" + sourceName");
	  out.println("print (\"referencing file: " + path + "\");");
	  out.println("file -reference -namespace \"" + nameSpace + "\" \"$WORKING"
	    + path.toOsString(OsType.Unix) + "\";");
	}

	out.println("// SAVE");
	out.println("file -save;");

	out.close();
      }
      catch(IOException ex) {
	throw new PipelineException("Unable to write the temporary MEL script (" + script
	  + ") used add the references!");
      }

      /* run Maya to add the reference */

      try {
	ArrayList<String> args = new ArrayList<String>();
	args.add("-batch");
	args.add("-script");
	args.add(script.getPath());
	args.add("-file");
	args.add(targetPath.toOsString());

	Path wdir =
	  new Path(PackageInfo.sProdPath.toOsString() + targetID.getWorkingParent());
	TreeMap<String, String> env =
	  mclient.getToolsetEnvironment(pAuthor, pView, targetMod.getToolset(),
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
	  new SubProcessLight("AddReferenceTool", command, args, env, wdir.toFile());
	try {
	  proc.start();
	  proc.join();
	  if(!proc.wasSuccessful()) {
	    throw new PipelineException(
	      "Did not correctly add the reference due to a maya error.!\n\n"
		+ proc.getStdOut() + "\n\n" + proc.getStdErr());
	  }
	}
	catch(InterruptedException ex) {
	  throw new PipelineException(ex);
	}
      }
      catch(Exception ex) {
	throw new PipelineException(ex);
      }
    }

    for(String sourceName : pSelected.keySet()) {
      if(sourceName != pPrimary)
	mclient.link(pAuthor, pView, pPrimary, sourceName, LinkPolicy.Reference,
	  LinkRelationship.All, null);
    }
    {
       BaseAction act = targetMod.getAction();
       if (act.getName().equals("MayaBuild"));
       {
	  for (String sourceName : pSourceFiles.keySet())
	  {
	     String nameSpace =  pNameSpaceMap.get(sourceName).getText();
	     act.initSourceParams(sourceName);
	     act.setSourceParamValue(sourceName, "PrefixName", nameSpace);
	  }
	  targetMod.setAction(act);
	  mclient.modifyProperties(pAuthor, pView, targetMod);
       }
    }

    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -80836529612490389L;

  private static final int sTSize = 150;
  private static final int sVSize = 300;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private String pAuthor;

  private String pView;

  private TreeMap<String, JTextField> pNameSpaceMap;

  private TreeMap<String, Path> pSourceFiles;


}
