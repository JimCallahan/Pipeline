// $Id: MayaTextureSyncTool.java,v 1.2 2005/02/21 00:12:19 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*; 

/*------------------------------------------------------------------------------------------*/
/*   M A Y A    T E X T U R E   T O O L                                                     */
/*------------------------------------------------------------------------------------------*/

/**
 * Synchronizes the texture files referenced by a Maya scene with the texture nodes
 * upstream of the target Maya scene node.
 */
public
class MayaTextureSyncTool
  extends BaseTool
  implements ActionListener
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaTextureSyncTool()
  {
    super("MayaTextureSync", new VersionID("1.0.0"),
	  "Synchronizes the texture files referenced by a Maya scene with the texture " + 
	  "nodes upstream of the target Maya scene node.");

    pFirstPhase = true; 

    pTextureInfo    = new TreeMap<String,String[]>();
    pTextureNodes   = new TreeSet<String>();
    pTextureToNode  = new TreeMap<File,NodeMod>();
    pShaderToNode   = new TreeMap<String,String>();

    pRegisterNodes = new TreeMap<String,Boolean>();
    pCheckOutNodes = new TreeMap<String,Boolean>();  
    pLinkNodes 	   = new TreeMap<String,Boolean>();  
    
    pCopyTextures    = new TreeMap<String,Boolean>();  
    pFixTexturePaths = new TreeMap<String,Boolean>();  

    pSkippedReason = new TreeMap<String,String>();

    pTextureFormats = new ArrayList<String>(); 
    pTextureFormats.add("iff");
    pTextureFormats.add("gif");
    pTextureFormats.add("jpg");
    pTextureFormats.add("jpeg");
    pTextureFormats.add("png");
    pTextureFormats.add("ppm");
    pTextureFormats.add("rgb");
    pTextureFormats.add("sgi");
    pTextureFormats.add("bw");
    pTextureFormats.add("tga");
    pTextureFormats.add("tif");
    pTextureFormats.add("tiff");

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
  public synchronized String
  collectPhaseInput() 
    throws PipelineException 
  {
    if(pFirstPhase) 
      return collectFirstPhaseInput();
    else 
      return collectSecondPhaseInput();
  }


  /**
   * Select the target nodes and relocation directory.
   * 
   * @return 
   *   The phase progress message or <CODE>null</CODE> to abort early.
   * 
   * @throws PipelineException 
   *   If unable to validate the given user input.
   */ 
  private String
  collectFirstPhaseInput() 
    throws PipelineException 
  {
    /* create dialog components */ 
    JComponent body = null;
    {
      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];
      body = (JComponent) comps[2];
      
      pTargetSceneField = 
	UIFactory.createTitledPathField
	(tpanel, "Target Scene Node:", sTSize, 
	 vpanel, null, sVSize, 
	 "The node associated with the Maya scene being checked.");
      
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      
      pTargetLinkField = 
	UIFactory.createTitledPathField
	(tpanel, "Target Link Node:", sTSize, 
	 vpanel, null, sVSize, 
	 "The node on the downstream side of all linked texture nodes being checked.");
      
      UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
      
      pRelocateField = 
	UIFactory.createTitledBooleanField
	(tpanel, "Relocate Path:", sTSize, 
	 vpanel, sVSize, 
	 "Whether to copy texture image files outside of the working area to a " +
	 "directory specified by the Relocate Path.");
      pRelocateField.setValue(true);
      pRelocateField.addActionListener(this);
      pRelocateField.setActionCommand("relocate-changed");
      
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      
      {
	JComponent rcomps[] = 
	  UIFactory.createTitledBrowsablePathField
	  (tpanel, "Relocate Path:", sTSize, 
	   vpanel, null, sVSize, 
	   this, "relocate-browse", 
	   "The fully resolved working area destination to copy relocated " + 
	   "texture files.");
	
	pRelocatePathField  = (JPathField) rcomps[0];	   
	pRelocatePathButton = (JButton) rcomps[1];
      }
      
      UIFactory.addVerticalGlue(tpanel, vpanel);
    }
    
    /* validate node selections and initialize dialog components */ 
    {
      if(pPrimary == null)
	throw new PipelineException
	  ("The primary selection must be the Target Scene Node!");
      pTargetSceneField.setText(pPrimary);
      
      {
	NodeStatus status = pSelected.get(pPrimary);
	NodeID nodeID = status.getNodeID();
	if(!nodeID.getAuthor().equals(PackageInfo.sUser)) 
	  throw new PipelineException 
	    ("Only nodes owned by the current user (" + PackageInfo.sUser + ") can " +
	     "be used as the target of the Maya Texture Sync tool!");
	
	pView = nodeID.getView(); 
      }
      
      if(pSelected.size() == 1) {
	pTargetLinkField.setText(pPrimary);
	
	File path = new File(pPrimary);
	pRelocatePathField.setText(path.getParent());
      }
      else if(pSelected.size() == 2) {
	for(String name : pSelected.keySet()) {
	  if(!name.equals(pPrimary)) {
	    pTargetLinkField.setText(name);
	    
	    File path = new File(name);
	    pRelocatePathField.setText(path.getParent());
	  }
	}
      }
      else {
	throw new PipelineException
	  ("Only one Target Link Node may be selected as the secondary selection!");
      }
    }
    
    /* query the user */ 
    JToolDialog diag = 
      new JToolDialog("Maya Texture Synchronizer:", body, "Continue");
    
    pRelocateDialog = 
      new JFileSelectDialog(diag, "Select Directory", 
			    "Select Texture Directory:", "Select");
    
    diag.setVisible(true);
    if(diag.wasConfirmed()) {
      pTargetScene = pTargetSceneField.getText();
      if((pTargetScene == null) || (pTargetScene.length() == 0))
	throw new PipelineException("Illegal Target Scene Node name!");
      
      pTargetLink = pTargetLinkField.getText();
      if((pTargetLink == null) || (pTargetLink.length() == 0))
	throw new PipelineException("Illegal Target Link Node name!");
      
      pRelocate = pRelocateField.getValue();
      if(pRelocate) {
	pRelocatePath = pRelocatePathField.getText();
	if((pRelocatePath == null) || (pRelocatePath.length() == 0))
	  throw new PipelineException("Illegal Relocate Path name!");
      }
      else {
	pRelocatePath = null;
      }
	
      return ": Collecting Texture Information...";
    }
    else {
      return null;
    }	
  }

  /**
   * Confirm the changes to be made to the nodes, textures files and Maya scene.
   * 
   * @return 
   *   The phase progress message or <CODE>null</CODE> to abort early.
   * 
   * @throws PipelineException 
   *   If unable to validate the given user input.
   */ 
  private String
  collectSecondPhaseInput() 
    throws PipelineException 
  {
    /* create dialog components */ 
    JComponent body = null;
    {
      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];
      body = (JComponent) comps[2];
      
      
      // ...
      

      UIFactory.addVerticalGlue(tpanel, vpanel);
    }
    
    /* query the user */ 
    JToolDialog diag = 
      new JToolDialog("Maya Texture Synchronizer:", body, "Confirm");
    diag.setVisible(true);
    if(diag.wasConfirmed()) {
      
      
      // ...
      
      
      return ": Modifying Nodes and Scene...";
    }
    else {
      return null;
    }
  }


  /*----------------------------------------------------------------------------------------*/

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
  public synchronized boolean
  executePhase
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {
    if(pFirstPhase) 
      return executeFirstPhase(mclient, qclient);
    else 
      return executeSecondPhase(mclient, qclient);
  }

  /**
   * Collect texture information from the Maya scene.
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
  private boolean
  executeFirstPhase
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {    
    System.out.print
      ("Maya Texture Sync:\n" + 
       " Target Scene Node = " + pTargetScene + "\n" +
       "  Target Link Node = " + pTargetLink + "\n" +
       "     Relocate Path = " + pRelocatePath + "\n\n");


    /* get the current status of the Maya scene node */ 
    {
      pTargetSceneID = new NodeID(PackageInfo.sUser, pView, pTargetScene);
      pTargetSceneStatus = mclient.status(pTargetSceneID);
      pTargetSceneMod = pTargetSceneStatus.getDetails().getWorkingVersion();
      if(pTargetSceneMod == null) 
	throw new PipelineException
	  ("No working version of the Target Scene Node (" + pTargetScene + ") exists " + 
	   "in the (" + pView + ") working area owned by (" + PackageInfo.sUser + ")!");

      /* verify that it is a Maya scene */
      {
	FileSeq fseq = pTargetSceneMod.getPrimarySequence();
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || 
	   (suffix == null) || (!suffix.equals("ma") && !suffix.equals("mb"))) 
	  throw new PipelineException
	    ("The Target Scene Node (" + pTargetScene + ") must have a Maya Scene as " + 
	     "its primary file sequence!");
	pTargetSceneFile = fseq.getFile(0);
      }
    }
    
    /* get the current status of the node downstream of the texture node links */ 
    {
      pTargetLinkID = new NodeID(PackageInfo.sUser, pView, pTargetLink);
      pTargetLinkStatus = findNodeStatus(pTargetLinkID, pTargetSceneStatus);
      if(pTargetLinkStatus == null) 
	throw new PipelineException
	  ("The Target Link Node (" + pTargetLink + ") was not a member of the tree " +
	   "of nodes upstream of the Target Scene Node (" + pTargetScene + ") in the " + 
	   "(" + pView + ") working area owned by (" + PackageInfo.sUser + ")!");
      pTargetLinkMod = pTargetLinkStatus.getDetails().getWorkingVersion();
      if(pTargetLinkMod == null) 
	throw new PipelineException
	("No working version of the Target Link Node (" + pTargetLink + ") exists " + 
	 "in the (" + pView + ") working area owned by (" + PackageInfo.sUser + ")!");
    }

    /* collect texture file information from Maya scene */ 
    {  
      File script = null;
      try {
	script = File.createTempFile("MayaTextureSyncTool-GetFileInfo.", ".mel", 
				     PackageInfo.sTempDir);
	FileCleaner.add(script);
      }
      catch(IOException ex) {
	throw new PipelineException
	  ("Unable to create the temporary MEL script used to collect " + 
	   "texture information from the Maya scene!");
      }
      
      File info = null;
      try {
	info = File.createTempFile("MayaTextureSyncTool-FileInfo.", ".txt", 
				   PackageInfo.sTempDir);
	FileCleaner.add(info);
      }
      catch(IOException ex) {
	throw new PipelineException
	  ("Unable to create the temporary text file used to store the texture " +
	   "information collected from the Maya scene!");
      }
      
      try {
	FileWriter out = new FileWriter(script);
	out.write
	  ("$out = `fopen \"" + info + "\" \"w\"`;\n" + 
	   "string $fileNodes[] = `ls -type file`;\n" + 
	   "string $working = `getenv(\"WORKING\")`;\n" + 
	   "for($node in $fileNodes) {\n" + 
	   "  string $old = `getAttr ($node + \".fileTextureName\")`;\n" + 
	       "  if(size($old) == 0) \n" + 
	   "    $old = \"-\";\n" + 
	   "  string $sub = `substitute $working $old \"$WORKING\"`;\n" + 
	   "  string $base = \"-\";\n" + 
	   "  string $dir = \"-\";\n" + 
	   "  int $relocate = 0;\n" + 
	   "  if(size($sub) > 0) {\n" + 
	   "    if(`substring $sub 1 8` == \"$WORKING\") {\n" + 
	   "      int $len = size($sub);\n" + 
	   "      $sub = `substring $sub 9 $len`;\n" + 
	   "      $dir = `dirname $sub`;\n" + 
	   "    }\n" + 
	   "    $base = `basename $old \"\"`; \n" + 
	   "  }\n" + 
	   "  fprint $out " + 
	   "($node + \" \" + $old + \" \" + $dir + \" \" + $base + \"\\n\");\n" + 
	   "}\n" + 
	   "fclose $out;\n");
	
	out.close();
	  }
      catch(IOException ex) {
	throw new PipelineException
	  ("Unable to write the temporary MEL script (" + script + ") used to collect " + 
	   "texture information from the Maya scene!");
      }
      
      /* run Maya to collect the information */ 
      try {
	ArrayList<String> args = new ArrayList<String>();
	args.add("-batch");
	args.add("-script");
	args.add(script.getPath());
	args.add("-file");
	args.add(pTargetSceneFile.getPath());
	
	TreeMap<String,String> env = 
	  mclient.getToolsetEnvironment(pTargetSceneID.getAuthor(), pTargetSceneID.getView(), 
					pTargetSceneMod.getToolset());
	
	File wdir = 
	  new File(PackageInfo.sProdDir.getPath() + pTargetSceneID.getWorkingParent());

	SubProcessLight proc = 
	  new SubProcessLight("MayaTextureSync-FileInfo", "maya", args, env, wdir);
	try {
	  proc.start();
	  proc.join();
	  if(!proc.wasSuccessful()) {
	    throw new PipelineException
	      ("Failed to collect the texture information due to a Maya failure!\n\n" +
	       proc.getStdOut() + "\n\n" + 
	       proc.getStdErr());
	  }
	}
	catch(InterruptedException ex) {
	  throw new PipelineException(ex);
	}
      }
      catch(Exception ex) {
	throw new PipelineException(ex);
      }
      
      /* read the collection information in from the texture info file written by Maya */ 
      {
	pTextureInfo.clear();
	
	StringBuffer buf = new StringBuffer();
	try {
	  FileReader in = new FileReader(info);
	  char cs[] = new char[4096];
	  while(true) {
	    int n = in.read(cs);
	    if(n == -1) 
	      break;
	    buf.append(cs, 0, n);
	  }
	  in.close();
	}
	catch(IOException ex) {
	  throw new PipelineException 
	    ("Unable to read the collected texture information from (" + info + ")!");
	}
	
	String lines[] = buf.toString().split("\n");
	int lk;
	for(lk=0; lk<lines.length; lk++) {
	  String parts[] = lines[lk].split(" ");
	  if(parts.length != 4) 
	    throw new PipelineException
	      ("The the texture information file (" + info + ") was unreadable!");
	  
	  String fileNode = parts[0];
	  String names[] = new String[3];
	  
	  int wk;
	  for(wk=0; wk<names.length; wk++) {
	    if((parts[wk+1] != null) && !parts[wk+1].equals("-"))
	      names[wk] = parts[wk+1];
	  }
	  
	  pTextureInfo.put(fileNode, names);
	}
      }
    }

    /* determine the names of the texture nodes and the mapping from texture filename to 
         node name for the nodes directly upstream of the Target Link Node */ 
    {
      pTextureNodes.clear();
      pTextureToNode.clear();

      for(String name : pTargetLinkMod.getSourceNames()) {
	NodeStatus status = pTargetLinkStatus.getSource(name);
	NodeDetails details = status.getDetails();
	NodeMod mod = details.getWorkingVersion();
	
	FileSeq fseq = mod.getPrimarySequence();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix != null) && pTextureFormats.contains(suffix)) {
	  pTextureNodes.add(name);
	  
	  for(File file : mod.getPrimarySequence().getFiles()) 
	    pTextureToNode.put(file, mod);
	}
      }
    }
    
    /* initialize per-texture operation flags */ 
    {
      pShaderToNode.clear();
      pRegisterNodes.clear();
      pCheckOutNodes.clear();
      pLinkNodes.clear();
      pCopyTextures.clear();
      pFixTexturePaths.clear();
      pSkippedReason.clear();

      for(String fileShader : pTextureInfo.keySet()) {
	String names[] = pTextureInfo.get(fileShader);
	String oldPath = names[0];
	String wdir    = names[1];
	String wfile   = names[2];

	/* construct the node name from the texture file location */ 
	String nodeName = null;
	if((wdir != null) && (wfile != null)) {
	  String parts[] = wfile.split("\\.");
	  nodeName = wdir + "/" + parts[0];
	}

	System.out.print("     File Node = " + fileShader + "\n" + 
			 "      Old Path = " + names[0] + "\n" +
			 " New Directory = " + names[1] + "\n" +
			 "      Filename = " + names[2] + "\n" + 
			 "     Node Name = " + nodeName + "\n\n");
	
	if(wfile == null) {
	  pSkippedReason.put
	    (fileShader, 
	     "The texture filename was missing in the Maya scene for this shader.");
	}
	else {
	  File tex = new File(wfile);

	  /* the texture had a valid working area location */ 
	  if(nodeName != null) {
	    pShaderToNode.put(fileShader, nodeName);
	    
	    /* the node for the shader is not already linked */ 
	    if(!pTextureNodes.contains(nodeName)) {
	      NodeID nodeID = new NodeID(PackageInfo.sUser, pView, nodeName);
	      NodeStatus status = findNodeStatus(nodeID, pTargetSceneStatus);
	      if(status == null) {
		try {
		  status = mclient.status(nodeID);
		}
		catch(PipelineException ex) {
		}
	      }
	      
	      /* a node exists which matches the prefix of the texture */ 
	      if(status != null) {
		FileSeq fseq = null;
		NodeDetails details = status.getDetails();
// 		switch(details.getOverallNodeState()) {
// 		case NeedsCheckOut: 
// 		  {
// 		    pCheckOutNodes.put(fileShader, true);
// 		    fseq = details.getLatestVersion().getPrimarySequence();
// 		  }
// 		  break;

// 		default:
// 		  fseq = details.getWorkingVersion().getPrimarySequence();
// 		}

		if(details.getWorkingVersion() != null) {
		  fseq = details.getWorkingVersion().getPrimarySequence();
		}
		else {
		  pCheckOutNodes.put(fileShader, true);
		  fseq = details.getLatestVersion().getPrimarySequence();
		}

		boolean match = false;
		for(File file : fseq.getFiles()) {
		  if(wfile.equals(file.getPath())) {
		    match = true;
		    break;
		  }
		}
		    
		if(!match) {
		  pSkippedReason.put
		    (fileShader, 
		     "A node exists (" + nodeName + ") which matches the prefix of " + 
		     "the texture file (" + oldPath + ") associated with shader, but " + 
		     "no file associated with this node exactly matches the texture " + 
		     "filename.");		      
		}
	      }

	      /* a new node needs to be registered for the texture */ 
	      else {
		pRegisterNodes.put(fileShader, true);
	      }

	      pLinkNodes.put(fileShader, true);
	    }
	    
	    if(!oldPath.startsWith("$WORKING")) 
	      pFixTexturePaths.put(fileShader, true);
	  }

	  /* the texture was outside the working area */ 
	  else {

	    /* one of the nodes already linked has a texture with the same name, 
	         its reasonable to assume that this node should be used for the texture */ 
	    NodeMod mod = pTextureToNode.get(tex);
	    if(mod != null) {
	      pShaderToNode.put(fileShader, mod.getName());
	      pCopyTextures.put(fileShader, true);
	      pFixTexturePaths.put(fileShader, true);
	    }

	    else if(pRelocate) {
	      String parts[] = wfile.split("\\.");
	      nodeName = pRelocatePath + "/" + parts[0];
	      pShaderToNode.put(fileShader, nodeName);

	      NodeID nodeID = new NodeID(PackageInfo.sUser, pView, nodeName);
	      NodeStatus status = findNodeStatus(nodeID, pTargetSceneStatus);
	      if(status == null) {
		try {
		  status = mclient.status(nodeID);
		}
		catch(PipelineException ex) {
		}
	      }
	      
	      /* a node exists which matches the relocation prefix for the texture */  
	      if(status != null) {
		FileSeq fseq = null;
		NodeDetails details = status.getDetails();		
// 		switch(details.getOverallNodeState()) {
// 		case NeedsCheckOut: 
// 		  {
// 		    pCheckOutNodes.put(fileShader, true);
// 		    fseq = details.getLatestVersion().getPrimarySequence();
// 		  }
// 		  break;

// 		default:
// 		  fseq = details.getWorkingVersion().getPrimarySequence();
// 		}

		if(details.getWorkingVersion() != null) {
		  fseq = details.getWorkingVersion().getPrimarySequence();
		}
		else {
		  pCheckOutNodes.put(fileShader, true);
		  fseq = details.getLatestVersion().getPrimarySequence();
		}

		boolean match = false;
		for(File file : fseq.getFiles()) {
		  if(wfile.equals(file.getPath())) {
		    match = true;
		    break;
		  }
		}
		    
		if(!match) {
		  pSkippedReason.put
		    (fileShader, 
		     "A node exists (" + nodeName + ") which matches the prefix of " + 
		     "the relocated location of the texture file (" + oldPath + ") " + 
		     "associated with shader, but no file associated with this node " + 
		     "exactly matches the texture filename.");		      
		}
	      }

	      /* a new node needs to be registered for the texture */ 
	      else {
		pRegisterNodes.put(fileShader, true);
	      }

	      pLinkNodes.put(fileShader, true);
	      pCopyTextures.put(fileShader, true);
	      pFixTexturePaths.put(fileShader, true);
	    }

	    else {
	      pSkippedReason.put
		(fileShader, 
		 "The texture file (" + oldPath + ") for the shader was outside the " + 
		 "working area, but texture relocation was disabled.");
	    }
	  }
	}
      }

      for(String fileShader : pSkippedReason.keySet()) {
	pShaderToNode.remove(fileShader);
	pRegisterNodes.remove(fileShader);
	pCheckOutNodes.remove(fileShader);
	pLinkNodes.remove(fileShader);
	pCopyTextures.remove(fileShader);
	pFixTexturePaths.remove(fileShader);
      }
    }


    // DEBUG 
    System.out.print("Texture Nodes:\n");
    for(String name : pTextureNodes) 
      System.out.print("  " + name + "\n");
    System.out.print("\n");
    
    System.out.print("Textures:\n");
    for(File file : pTextureToNode.keySet()) 
      System.out.print("  " + file + " -> " + pTextureToNode.get(file).getName() + "\n");
    System.out.print("\n");

    for(String fileShader : pTextureInfo.keySet()) {
      System.out.print("SHADER: " + fileShader + "\n"); 

      String reason = pSkippedReason.get(fileShader);
      if(reason != null) {
	System.out.print("Skipped = " + reason + "\n");
      }
      else {
	System.out.print("Node = " + pShaderToNode.get(fileShader) + "\n");
	
	if(pRegisterNodes.containsKey(fileShader)) 
	  System.out.print("+ Register\n");

	if(pCheckOutNodes.containsKey(fileShader)) 
	  System.out.print("+ Check-Out\n");

	if(pLinkNodes.containsKey(fileShader)) 
	  System.out.print("+ Link\n");

	if(pCopyTextures.containsKey(fileShader)) 
	  System.out.print("+ Copy Textures\n");

	if(pFixTexturePaths.containsKey(fileShader)) 
	  System.out.print("+ Fix Texture Paths\n");
      }
      System.out.print("\n");
    }
    // DEBUG 
    
    
    pFirstPhase = false;
    return true;
  }

  /**
   * Perform node operations, copy texture files and fix the texture paths in the 
   * target Maya scene.
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
  private boolean
  executeSecondPhase
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {      
    

    // ... 
    
	
    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    String cmd = e.getActionCommand();
    if(cmd.equals("relocate-changed"))
      doRelocateChanged();
    else if(cmd.equals("relocate-browse"))
      doRelocateBrowse();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The relocate field has changed.
   */ 
  private void 
  doRelocateChanged()  
  {
    if(pRelocateField.getValue()) {
      pRelocatePathField.setEnabled(true);
      pRelocatePathButton.setEnabled(true);
      String text = pRelocatePathField.getText();
      if((text == null) || (text.length() == 0)) {
	File path = new File(pTargetLinkField.getText());
	pRelocatePathField.setText(path.getParent());
      }
    }
    else {
      pRelocatePathField.setText(null);
      pRelocatePathField.setEnabled(false);
      pRelocatePathButton.setEnabled(false);
    }    
  }

  /**
   * Browse for an alternative relocate directory path.
   */ 
  private void 
  doRelocateBrowse()  
  {
    File root = new File(PackageInfo.sWorkDir + "/" + PackageInfo.sUser + "/" + pView);
    pRelocateDialog.setRootDir(root);

    String text = pRelocatePathField.getText();
    if((text != null) && (text.length() > 0)) {
      File path = new File(root, text);
      pRelocateDialog.updateTargetFile(path.getParentFile());
    }

    pRelocateDialog.setVisible(true);
    if(pRelocateDialog.wasConfirmed()) {
      File dir = pRelocateDialog.getSelectedFile();
      if((dir != null) && (dir.isDirectory())) {
	String rpath = root.getPath();
	String path = dir.getPath();
	if(path.startsWith(rpath)) 
	  pRelocatePathField.setText(path.substring(rpath.length()));
      }
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Recursively search the upstream links for the status of the given node.
   * 
   * @return
   *    The status or <CODE>null</CODE> if unable to find the node.
   */ 
  private NodeStatus 
  findNodeStatus
  (
   NodeID nodeID,
   NodeStatus status
  ) 
  {
    if(status.getNodeID().equals(nodeID)) 
      return status;
    
    for(NodeStatus lstatus : status.getSources()) {
      NodeStatus found = findNodeStatus(nodeID, lstatus);
      if(found != null) 
	return found;
    }

    return null;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4965831379305113190L;

  private static final int sTSize = 150;
  private static final int sVSize = 300;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether this is the first execution phase. 
   */ 
  private boolean  pFirstPhase;

  /**
   * The current working area view.
   */ 
  private String  pView; 

  /**
   * The filename suffixes recognized as textures.
   */ 
  private ArrayList<String> pTextureFormats;


  /*-- FIRST PHASE -------------------------------------------------------------------------*/

  /**
   * The node associated with the Maya scene being checked.
   */ 
  private JPathField  pTargetSceneField; 
  private String      pTargetScene;

  /**
   * The node on the downstream side of all linked texture nodes being checked.
   */ 
  private JPathField  pTargetLinkField; 
  private String      pTargetLink; 

  /**
   * Whether to relocate texture files outside of the working area to the directory
   * specified by the relocate path.
   */ 
  private JBooleanField  pRelocateField; 
  private boolean        pRelocate; 

  /**
   * The fully resolved working area destination of relocated texture files.
   */
  private JPathField         pRelocatePathField;
  private JButton            pRelocatePathButton;
  private JFileSelectDialog  pRelocateDialog;
  private String             pRelocatePath; 


  /*-- SECOND PHASE ------------------------------------------------------------------------*/

  /**
   * Node information for the Target Scene Node.
   */ 
  private NodeID      pTargetSceneID;
  private NodeStatus  pTargetSceneStatus;
  private NodeMod     pTargetSceneMod;
  private File        pTargetSceneFile; 
  
  /**
   * Node information for the Target Link Node.
   */ 
  private NodeID      pTargetLinkID;
  private NodeStatus  pTargetLinkStatus;
  private NodeMod     pTargetLinkMod;
  
  /**
   * The filesystem names associated with each Maya File shader indexed by the name of 
   * the shader.  The three values stored in the String array value for each shader are: 
   *  [0]: The original "fileTextureName" value or <CODE>null</CODE> if unset.
   *  [1]: The path of the correct texture directory relative to the working area or 
   *       <CODE>null</CODE> if the texture is currently outside the working area.
   *  [2]: The name of the texture file or <CODE>null</CODE> if unset.
   */ 
  private TreeMap<String,String[]> pTextureInfo;

  /**
   * The names of the texture nodes already linked to the Target Link node.
   */ 
  private TreeSet<String> pTextureNodes;

  /**
   * The working versions of the texture nodes already linked to the Target Link node
   * indexed by the names of the texture files associated with the nodes.
   */ 
  private TreeMap<File,NodeMod> pTextureToNode;

  /**
   * The names of the nodes associated with Maya shaders.
   */ 
  private TreeMap<String,String> pShaderToNode; 

  /**
   * Whether to register, check-out and/or link the node associated with each Maya File
   * shader.
   */ 
  private TreeMap<String,Boolean>  pRegisterNodes;
  private TreeMap<String,Boolean>  pCheckOutNodes;
  private TreeMap<String,Boolean>  pLinkNodes;

  /**
   * Whether to copy the texture file associated with each Maya File shader into the 
   * current working area.
   */ 
  private TreeMap<String,Boolean>  pCopyTextures; 

  /**
   * Whether to replace the "fileTextureName" attributes of each Maya File shader with a 
   * path that begins with "$WORKING".
   */ 
  private TreeMap<String,Boolean>  pFixTexturePaths; 

  /**
   * Messages about why Maya File shaders where skipped during the process indexed by the
   * name of the Maya file shader.
   */ 
  private TreeMap<String,String>  pSkippedReason; 
  
}

