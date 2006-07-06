// $Id: MiRenderPrep.java,v 1.3 2006/07/06 18:44:06 jim Exp $

package us.temerity.pipeline.utils.v2_0_14;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.GlueException;
import us.temerity.pipeline.glue.io.GlueDecoderImpl;

public class MiRenderPrep
{

  public MiRenderPrep(ArrayList<String> output, ActionAgenda agenda, BaseAction action) {
    this.output = output;
    this.agenda = agenda;
    this.action = action;
  }

  public void run() throws PipelineException
  {
    String command = (String) action.getSingleParamValue("Command");
    if(command.equals("RenderPrep")) {
      buildRenderMI();
    }
    else if(command.equals("TextureFix")) {
      fixTextures();
    }

  }

  private void fixTextures() throws PipelineException
  {
    NodeID nodeID = agenda.getNodeID();
    int numFrames = output.size();
    FileSeq target = agenda.getPrimaryTarget();
    if(target.numFrames() != numFrames)
      throw new PipelineException("The number of source "
	+ "mi files does not match the number of final images");
    int frame = 0;
    for(String file : output) {
      Path inputPath =
	new Path(PackageInfo.sProdPath, nodeID.getWorkingParent() + "/"
	  + target.getFile(frame));
      System.err.println(inputPath.toOsString());
      System.err.println(file);
      try {
	BufferedReader in = new BufferedReader(new FileReader(inputPath.toOsString()));
	System.err.println("Opened for reading");
	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
	System.err.println("Opened for writing");
	boolean read = true;
	String line = null;
	while(read) {
	  System.err.println("Reading line");
	  line = in.readLine();
	  System.err.println(line);
	  if(line != null) {
	    String finalLine = line;
	    if(line.matches(texturePattern1) || line.matches(texturePattern2)) {
	      String buffer[] = line.split(" ");
	      finalLine = "";
	      for(int j = 0; j < buffer.length - 1; j++) {
		finalLine += buffer[j] + " ";
	      }
	      String texPath = buffer[buffer.length - 1];
	      texPath = texPath.replaceAll("\"", "");
	      // Did they set an absolute working path?
	      if(texPath.contains(PackageInfo.sProdPath.toOsString())) {
		System.err.println("Working Path");
		texPath = texPath.replace(PackageInfo.sProdPath.toOsString(), "");
		Path p = new Path(texPath);
		ArrayList<String> comps = p.getComponents();
		comps.remove(0);
		comps.remove(0);
		comps.remove(0);
		texPath = "";
		for(String s : comps)
		  texPath += s + "/";
		texPath = texPath.substring(0, texPath.length() - 1);

	      }
	      else if(texPath.matches(mayaCrapPattern)) {
		System.err.println("Crap path");
		texPath = texPath.replace("renderData/mentalray/", "");
	      }
	      else if(texPath.matches(localTexPattern1) || texPath.matches(localTexPattern2)
		|| texPath.matches(localTexPattern3)) {
		System.err.println("Local path");
	      }
	      else {
		System.err.println("The file texture path ("
		  + buffer[buffer.length - 1] + ") is not valid.  Please correct.");
		System.exit(1);
	      }
	      
	      if (texPath.matches(workingPattern)) {
		System.err.println("Chopping Working");
		texPath = texPath.replace("$WORKING/", "");
	      }
	      
	      finalLine += "\"" + texPath + "\"";
	    }
	    System.err.println("Writing line: " + finalLine);
	    out.println(finalLine);

	  }
	  else {
	    read = false;
	  }
	}
	in.close();
	out.close();

      }
      catch(FileNotFoundException e) {
	System.err.println("The shader file is not "
	  + "being written correctly from maya.\n" + e.getMessage());
	System.exit(1);
      }
      catch(IOException e) {
	System.err.println("File IO error has occured.\n" + e.getMessage());
	System.exit(1);
      }

      // Open the file.
      // read each line
      // if texture
      // Fix line
      // write line to new file
    }
  }

  private void buildRenderMI() throws PipelineException
  {
    NodeID nodeID = agenda.getNodeID();
    int numFrames = output.size();
    FileSeq target = agenda.getPrimaryTarget();
    if(target.numFrames() != numFrames)
      throw new PipelineException("The number of source "
	+ "mi files does not match the number of final images");

    TreeMap<String, FileSeq> singleSources = new TreeMap<String, FileSeq>();
    TreeMap<String, FileSeq> perFrameSources = new TreeMap<String, FileSeq>();
    TreeMap<String, FileSeq> instGroupSources = new TreeMap<String, FileSeq>();
    FileSeq camOverSeq = null;
    FileSeq cameraSeq = null;
    boolean hasCamera = false;
    boolean hasCamOverride = false;
    String camOverName = null;
    String outputFormat = (String) action.getSingleParamValue("FramebufferType");

    { // Checking the camera parameter, and setting cameraSeq if it exists
      String camera = (String) action.getSingleParamValue("CameraMIFiles");
      if(camera != null) {
	hasCamera = true;
	cameraSeq = agenda.getPrimarySource(camera);
      }
    }

    TreeMap<Integer, LinkedList<FileSeq>> sortedSeqs =
      new TreeMap<Integer, LinkedList<FileSeq>>();
    {
      Set<String> sources = agenda.getSourceNames();
      if(DEBUG)
	System.err.println("Looping over the sources");
      for(String source : sources) {
	if(DEBUG)
	  System.err.println("Source Name: " + source);
	FileSeq sourceSeq = agenda.getPrimarySource(source);
	ActionInfo info = agenda.getSourceActionInfo(source);
	String actionName = null;
	if(info != null)
	  actionName = info.getName();
	if(action.hasSourceParams(source) && !(("MRayCamOverride").equals(actionName))) {

	  Integer order = (Integer) action.getSourceParamValue(source, "Order");
	  if(order != null) {

	    LinkedList<FileSeq> orderList = sortedSeqs.get(order);
	    if(orderList == null) {
	      orderList = new LinkedList<FileSeq>();
	      sortedSeqs.put(order, orderList);
	    }

	    orderList.add(sourceSeq);

	    // Separates out the single files and the per-frame files
	    if(sourceSeq.isSingle()) {
	      if(DEBUG)
		System.err.println("\tThis is a single source");
	      singleSources.put(source, sourceSeq);
	    }
	    else {
	      if(DEBUG)
		System.err.println("\tThis is a perFrame source");
	      perFrameSources.put(source, sourceSeq);
	    }
	  }
	}

	if(actionName != null) {
	  if(DEBUG)
	    System.err.println("\tAction Name: " + actionName);
	  if(actionName.equals("MRayInstGroup")) {
	    if(DEBUG)
	      System.err.println("\tThis is an instGroup");
	    instGroupSources.put(source, sourceSeq);
	  }
	  else if(actionName.equals("MRayCamOverride")) {
	    if(DEBUG)
	      System.err.println("\t This is a camera Override");
	    if(hasCamOverride)
	      throw new PipelineException("Only one camera override can be hooked up at once");

	    hasCamOverride = true;
	    camOverSeq = sourceSeq;
	    camOverName = source;
	  }
	}
      }
    }
    if(DEBUG) {
      System.err.println();
      System.err.println();
    }
    // Read in all the info from the single files.

    ArrayList<String> singleInstances = new ArrayList<String>();
    ArrayList<String> singleCameras = new ArrayList<String>();
    ArrayList<String> singleCameraInstances = new ArrayList<String>();
    ArrayList<String> singleOptions = new ArrayList<String>();
    ArrayList<String> singleInstGroups = new ArrayList<String>();
    TreeMap<FileSeq, String> singleIncludeStatements = new TreeMap<FileSeq, String>();

    TreeSet<String> instGroupNameSet = new TreeSet<String>(instGroupSources.keySet());
    for(String source : singleSources.keySet()) {
      if(DEBUG)
	System.err.println("Reading Single Source File: " + source);
      FileSeq sourceSeq = singleSources.get(source);
      NodeID snodeID = new NodeID(nodeID, source);
      Path sourcePath = buildPath(snodeID, sourceSeq, 0);
      singleIncludeStatements.put(sourceSeq, "$include \"" + sourcePath.toOsString() + "\"");

      if(hasCamOverride && sourceSeq.equals(camOverSeq))
	continue;

      BufferedReader in = makeFileReader(sourcePath);
      boolean read = true;
      boolean isInstGroup = instGroupNameSet.contains(source);
      while(read) {
	String line = null;
	try {
	  line = in.readLine();
	}
	catch(IOException e) {
	  throw new PipelineException("Error Reading the file" + " assoicated with ("
	    + source + ")");
	}

	if(line != null) {
	  if(DEBUG2)
	    System.err.println(line);
	  // So if this is a contruscted inst group built by pipeline
	  if(isInstGroup) {
	    if(line.matches(instGroupPattern)) {
	      String buffer[] = line.split(" ");
	      singleInstGroups.add(buffer[1]);
	      if(DEBUG)
		System.err.println("\tFound an instGroup: " + buffer[1]);
	      read = false;
	    }
	  }
	  else {
	    String buffer[] = line.split(" ");
	    // Checking if it is a camera declaration
	    if(line.matches(cameraPattern)) {
	      singleCameras.add(buffer[1]);
	      if(DEBUG)
		System.err.println("\t\tFound a camera declaration: " + buffer[1]);
	      // Now checking if it is an instance
	    }
	    else if(line.matches(instPattern)) {
	      String instName = buffer[1];
	      String objName = buffer[2];
	      singleInstances.add(instName);
	      if(DEBUG)
		System.err.println("\tFound an instance: " + instName);
	      // checking if it is also a camera instance
	      if(!hasCamera || (hasCamera && cameraSeq.equals(sourceSeq))) {
		for(String camName : singleCameras) {
		  if(objName.equals(camName)) {
		    if(DEBUG)
		      System.err.println("\tIt's a camera instance");
		    singleCameraInstances.add(instName);
		    break;
		  }
		}
	      }
	      // checking if it is an option block
	    }
	    else if(line.matches(optionPattern)) {
	      if(DEBUG)
		System.err.println("\tFound an option block: " + buffer[1]);
	      singleOptions.add(buffer[1]);
	    }
	  }
	}
	else
	  read = false;
      }
    }

    for(int frame = 0; frame < numFrames; frame++) {
      ArrayList<String> perFrameInstances = new ArrayList<String>();
      ArrayList<String> perFrameCameras = new ArrayList<String>();
      ArrayList<String> perFrameCameraInstances = new ArrayList<String>();
      ArrayList<String> perFrameOptions = new ArrayList<String>();
      ArrayList<String> perFrameInstGroups = new ArrayList<String>();
      TreeMap<FileSeq, String> perFrameIncludeStatements = new TreeMap<FileSeq, String>();

      for(String source : perFrameSources.keySet()) {
	FileSeq sourceSeq = perFrameSources.get(source);
	NodeID snodeID = new NodeID(nodeID, source);
	Path sourcePath = buildPath(snodeID, sourceSeq, frame);
	perFrameIncludeStatements.put(sourceSeq, "$include \"" + sourcePath.toOsString()
	  + "\"");
	if(DEBUG)
	  System.err.println("Reading Per Frame Source File: " + sourcePath.toOsString());

	BufferedReader in = makeFileReader(sourcePath);
	boolean read = true;
	boolean isInstGroup = instGroupNameSet.contains(source);
	while(read) {
	  String line = null;
	  try {
	    line = in.readLine();
	  }
	  catch(IOException e) {
	    throw new PipelineException("Error Reading the file" + " assoicated with ("
	      + source + ")");
	  }
	  if(line != null) {
	    if(DEBUG2)
	      System.err.println(line);

	    // So if this is a contruscted inst group built by pipeline
	    if(isInstGroup) {
	      if(line.matches(instGroupPattern)) {
		String buffer[] = line.split(" ");
		perFrameInstGroups.add(buffer[1]);
		if(DEBUG)
		  System.err.println("\tFound an instGroup: " + buffer[1]);
		read = false;
	      }
	    }
	    else {
	      String buffer[] = line.split(" ");
	      // Checking if it is a camera declaration
	      if(line.matches(cameraPattern)) {
		perFrameCameras.add(buffer[1]);
		if(DEBUG)
		  System.err.println("\tFound a camera declaration: " + buffer[1]);
		// Now checking if it is an instance
	      }
	      else if(line.matches(instPattern)) {
		String instName = buffer[1];
		String objName = buffer[2];
		perFrameInstances.add(instName);
		if(DEBUG)
		  System.err.println("\tFound an instance: " + instName);
		// checking if it is also a camera instance
		if(!hasCamera || (hasCamera && cameraSeq.equals(sourceSeq))) {
		  // You might have a per scene camera instance which
		  // is only
		  // defined once. So you have to check both arrays.
		  // You should never have it the other way.
		  // Unless people are really dumb
		  ArrayList<String> allCams = new ArrayList<String>(singleCameras);
		  allCams.addAll(perFrameCameras);
		  for(String camName : allCams) {
		    if(objName.equals(camName)) {
		      if(DEBUG)
			System.err.println("\t\tIt's a camera instance");
		      perFrameCameraInstances.add(instName);
		      break;
		    }
		  }
		}
		// checking if it is an option block
	      }
	      else if(line.matches(optionPattern)) {
		if(DEBUG)
		  System.err.println("\tFound an option block: " + buffer[1]);
		perFrameOptions.add(buffer[1]);
	      }
	    }
	  }
	  else {
	    read = false;
	  }
	}
      }

      Path outputPath = new Path(output.get(frame));
      PrintWriter out = makeFileWriter(outputPath);

      String base = (String) action.getSingleParamValue("BaseLibraries");
      if(DEBUG)
	System.err.println("Base is set to : " + base);
      if(base.equals("MI_ROOT") || base.equals("MAYA_LOCATION")) {
	if(DEBUG)
	  System.err.println("sticking in all the libs.");
	out.println("link \"base.so\"");
	out.println("$include \"base.mi\"");
	out.println("link \"physics.so\"");
	out.println("$include \"physics.mi\"");
	out.println("link \"contour.so\"");
	out.println("$include \"contour.mi\"");
	out.println("link \"subsurface.so\"");
	out.println("$include \"subsurface.mi\"");
	out.println("link \"paint.so\"");
	out.println("$include \"paint.mi\"");
      }

      if(base.equals("MAYA_LOCATION")) {
	out.println("link \"mayabase.so\"");
	out.println("$include \"mayabase.mi\"");
	out.println("link \"mayahair.so\"");
	out.println("$include \"mayahair.mi\"");
      }
      perFrameIncludeStatements.putAll(singleIncludeStatements);

      // Writing Includes
      for(int order : sortedSeqs.keySet()) {
	LinkedList<FileSeq> list = sortedSeqs.get(order);
	for(FileSeq seq : list) {
	  String s = perFrameIncludeStatements.get(seq);
	  if(s != null) {
	    out.println(s);
	  }
	}
      }

      // Writing inst group to instance declarations
      perFrameInstGroups.addAll(singleInstGroups);
      for(String instGroup : perFrameInstGroups) {
	String instance = instGroup.replace("\"", "");
	instance += "_instance";
	instance = "\"" + instance + "\"";
	String toWrite = "instance " + instance + " " + instGroup;
	out.println(toWrite);
	out.println("end instance");
	perFrameInstances.add(instance);
      }

      String camOverText = "";
      if(hasCamOverride) {
	Path camOverPath = buildPath(new NodeID(nodeID, camOverName), camOverSeq, 0);
	BufferedReader in = makeFileReader(camOverPath);
	boolean read = true;
	while(read) {
	  String line = null;
	  try {
	    line = in.readLine();
	  }
	  catch(IOException e) {
	    throw new PipelineException("Error Reading the file" + " assoicated with ("
	      + camOverName + ")");
	  }
	  if(line != null) {
	    camOverText += "\t" + line + "\n";
	  }
	  else
	    read = false;
	}
      }

      // Doing the incremental camera statements
      perFrameCameras.addAll(singleCameras);
      String suffix = target.getFilePattern().getSuffix();
      String fileName = target.getPath(frame).getName();
      for(String camera : perFrameCameras) {
	out.println("incremental camera " + camera);
	out.println("\toutput \"" + outputFormat + "\" \"" + suffix + "\" \"" + fileName
	  + "\"");
	if(hasCamOverride)
	  out.print(camOverText);
	out.println("end camera");
      }

      out.println();
      out.println();

      // final inst group
      perFrameInstances.addAll(singleInstances);
      out.println("instgroup \"TheFinalRenderGroup\"");
      for(String instance : perFrameInstances) {
	out.println("\t" + instance);
      }
      out.println("end instgroup");

      perFrameCameraInstances.addAll(singleCameraInstances);
      perFrameOptions.addAll(singleOptions);
      for(String camera : perFrameCameraInstances)
	for(String options : perFrameOptions) {
	  out.println("render \"TheFinalRenderGroup\" " + camera + " " + options);
	}
      out.close();
    }

  }

  private Path buildPath(NodeID nodeID, FileSeq sourceSeq, int frame)
  {
    Path toReturn =
      new Path(PackageInfo.sProdPath, nodeID.getWorkingParent() + "/"
	+ sourceSeq.getPath(frame));
    return toReturn;
  }

  private PrintWriter makeFileWriter(Path outputPath) throws PipelineException
  {
    File file = new File(outputPath.toOsString());

    PrintWriter toReturn = null;
    try {
      toReturn = new PrintWriter(new BufferedWriter(new FileWriter(file)));
    }
    catch(IOException e) {
      throw new PipelineException("The file (" + outputPath.toOsString()
	+ ") cannot be opened for writing.\n" + e.getMessage());
    }

    return toReturn;
  }

  private BufferedReader makeFileReader(Path sourcePath) throws PipelineException
  {
    File file = new File(sourcePath.toOsString());
    if(!file.exists())
      throw new PipelineException("The file (" + sourcePath.toOsString()
	+ ") does not exists");
    BufferedReader toReturn = null;
    try {
      toReturn = new BufferedReader(new FileReader(file));
    }
    catch(FileNotFoundException e) {
      throw new PipelineException("The file (" + sourcePath.toOsString()
	+ ") cannot be opened for reading.\n" + e.getMessage());
    }
    return toReturn;
  }

  /**
   * @param args
   * @throws Exception
   */

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws PipelineException
  {
    if(args.length != 1)
      throw new PipelineException("Wrong Number of Args passed to Render Prep");

    String fileName = args[0];

    ActionAgenda agenda;
    ArrayList<String> output;
    BaseAction action;
    try {
      FileReader in = new FileReader(fileName);
      GlueDecoderImpl decode = new GlueDecoderImpl(in);

      TreeMap<String, Object> fromGlue = (TreeMap<String, Object>) decode.getObject();
      agenda = (ActionAgenda) fromGlue.get("agenda");
      output = (ArrayList<String>) fromGlue.get("file");
      action = (BaseAction) fromGlue.get("action");
    }
    catch(GlueException e) {
      throw new PipelineException("Error decoding Glue file\n" + e.getMessage());
    }
    catch(FileNotFoundException e) {
      throw new PipelineException("Cannot find Glue file\n" + e.getMessage());
    }

    MiRenderPrep object = new MiRenderPrep(output, agenda, action);
    object.run();
  }

  ActionAgenda agenda;

  BaseAction action;

  ArrayList<String> output;

  private final static String instGroupPattern = "^instgroup.*";

  private final static String instPattern = "^instance.*";

  private final static String cameraPattern = "^camera.*";

  private final static String optionPattern = "^options.*";

  private final static String texturePattern1 = "^color texture.*";

  private final static String texturePattern2 = "^filter.*color texture.*";

  private final static String localTexPattern1 = "^/usr/tmp/.*";

  private final static String localTexPattern2 = "^/var/tmp/.*";

  private final static String localTexPattern3 = "^/tmp/.*";

  private final static String mayaCrapPattern = "^renderData/mentalray/.*";
  
  private final static String workingPattern = "^\\$WORKING/.*";

  private final static boolean DEBUG = true;

  private final static boolean DEBUG2 = false;

}
