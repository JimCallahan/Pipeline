package com.sony.scea.pipeline.plugins.v1_0_0;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/**
 * Mel script for comparing geometry names between two files, intended to be used
 * between the Rig and the Model files.<p>
 * If checks for nodes that appear in one file, but not the other file, and for nodes
 * that do not meet the naming convention.  It then creates a temp txt file, which it opens
 * using kwrite or notepad, depending on the operating system.
 * <p>
 * This tool actually launches Maya to get the lists of geometry.
 * @author Jesse Clemens
 *
 */
public class GeoNameCompareTool extends BaseTool
{
   public GeoNameCompareTool()
   {
      super("GeoNameCompare", new VersionID("1.0.0"), "SCEA",
	 "Utility to compare geometry names in two files.");

      underDevelopment();

      addSupport(OsType.MacOS);
      addSupport(OsType.Windows);
      mods = new ArrayList<NodeMod>();
   }
   
   public synchronized String collectPhaseInput() throws PipelineException
   {
      if ( pPrimary == null )
	 throw new PipelineException("Youse got to select something!");
      
      if (pSelected.size() != 2)
	 throw new PipelineException("Please select two nodes and two nodes only!");
      
      for (NodeStatus status : pSelected.values())
      {
	 NodeMod mod = status.getDetails().getWorkingVersion();
	 FileSeq fseq = mod.getPrimarySequence();
	 String suffix = fseq.getFilePattern().getSuffix();
	 if ( !fseq.isSingle() || ( suffix == null )
	       || !( suffix.equals("ma") || suffix.equals("mb") ) )
	    throw new PipelineException(
	       "You have to select two Maya scenes.");
	 mods.add(mod);
      }

      return ": Mirror, mirror on the wall . . . did they name stuff right at all?";
   }
   
   public synchronized boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient)
   	throws PipelineException
   {
      NodeID id = pSelected.get(mods.get(0).getName()).getNodeID();
      TreeSet<String> fileNames = new TreeSet<String>(); 
      for (NodeMod mod : mods)
      {
	 FileSeq seq = mod.getPrimarySequence();
	 String name = "$WORKING" + new Path(mod.getName()).getParent() + "/" +  seq.getFile(0);
	 fileNames.add(name);
      }
      File script = null;
      File info1 = null;
      File info2 = null;
      TreeSet<String> in1 = new TreeSet<String>();
      TreeSet<String> in2 = new TreeSet<String>();


      {	 
	 try
	 {
	    script = File.createTempFile("GeoNameCompareTool-GetFileInfo.", ".mel",
	       PackageInfo.sTempPath.toFile());
	    FileCleaner.add(script);
	 } catch ( IOException ex )
	 {
	    throw new PipelineException(
	       "Unable to create the temporary MEL script used to collect "
		     + "texture information from the Maya scene!");
	 }
	 
	 try
	 {
	    info1 = File.createTempFile("GeoNameCompareTool-FileInfo.", ".txt",
	       PackageInfo.sTempPath.toFile());
	    FileCleaner.add(info1);
	    info2 = File.createTempFile("GeoNameCompareTool-FileInfo.", ".txt",
	       PackageInfo.sTempPath.toFile());
	    FileCleaner.add(info2);

	 } catch ( IOException ex )
	 {
	    throw new PipelineException(
	       "Unable to create the temporary text file used to store the texture "
		     + "information collected from the Maya scene!");
	 }
	 try
	 {
	    FileWriter out = new FileWriter(script);
	    out.write("file -f -o \""+ fileNames.first() +"\";\n");
	    String fileName = info1.getAbsolutePath();
	    if (PackageInfo.sOsType.equals(OsType.Windows))
	       fileName = fileName.replaceAll("\\\\", "/");
	    out
	       .write("$out = `fopen \""
		     + fileName
		     + "\" \"w\"`;\n"
		     + "string $geoNodes[] = `ls -type surfaceShape`;\n"
		     + "for($node in $geoNodes) {\n"
		     + "  string $parents[] = `listRelatives -parent $node`;\n"
		     + "  string $transform = $parents[0];\n"
		     + "  fprint $out ($transform + \"\\n\");\n"
		     + "}\n" + "fclose $out;\n");

	    out.write("file -f -o \""+ fileNames.last() +"\";\n");
	    fileName = info2.getAbsolutePath();
	    if (PackageInfo.sOsType.equals(OsType.Windows))
	       fileName = fileName.replaceAll("\\\\", "/");
	    out
	       .write("$out = `fopen \""
		     + fileName
		     + "\" \"w\"`;\n"
		     + "string $geoNodes[] = `ls -type surfaceShape`;\n"
		     + "for($node in $geoNodes) {\n"
		     + "  string $parents[] = `listRelatives -parent $node`;\n"
		     + "  string $transform = $parents[0];\n"
		     + "  fprint $out ($transform + \"\\n\");\n"
		     + "}\n" + "fclose $out;\n");


	    out.close();
	 } catch ( IOException ex )
	 {
	    throw new PipelineException("Unable to write the temporary MEL script ("
		  + script + ") used to collect "
		  + "texture information from the Maya scene!");
	 }
      }
      
      /* run Maya to collect the information */ 
      try {
	ArrayList<String> args = new ArrayList<String>();
	args.add("-batch");
	args.add("-script");
	args.add(script.getPath());
	
	TreeMap<String,String> env = 
	  mclient.getToolsetEnvironment
	  (id.getAuthor(), id.getView(), 
	   mods.get(0).getToolset(), PackageInfo.sOsType);
	
	Path wpath = new Path(PackageInfo.sProdPath, id.getWorkingParent());

	/* added custom Mental Ray shader path to the environment */ 
	Map<String, String> nenv = env;
	String midefs = env.get("PIPELINE_MI_SHADER_PATH");
	if(midefs != null) {
	    nenv = new TreeMap<String, String>(env);
	    Path dpath = new Path(wpath, midefs);
	    nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toOsString());
	}
	
	String command = "maya";
	if (PackageInfo.sOsType.equals(OsType.Windows))
	   command = "maya.exe";

	SubProcessLight proc = 
	  new SubProcessLight("GeoNameCompareTool-FileInfo", command, args, nenv, wpath.toFile());
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
      
      /*
       * TEMPSHIT
       * 
       */
//      File tempFile = null;
//      try
//      {
//	 tempFile = File.createTempFile("GeoNameCompareTool-Temp.", ".txt",
//	    PackageInfo.sTempPath.toFile());
//	 FileCleaner.add(tempFile);
//      } catch ( IOException ex )
//      {
//	 throw new PipelineException(
//	    "Unable to create the temporary text file used to store the texture "
//		  + "information collected from the Maya scene!");
//      }
      
      //END TEMP
      TreeSet<String> names1 = new TreeSet<String>();
      TreeSet<String> names2 = new TreeSet<String>();
      TreeSet<String> nonStandard1 = new TreeSet<String>();
      TreeSet<String> nonStandard2 = new TreeSet<String>();
      try
      {
	 BufferedReader in = new BufferedReader(new FileReader(info1));
	 String line = in.readLine(); 
	 while (line != null)
	 {
	    names1.add(line);
	    line = in.readLine();
	 }
	 in.close();
	 in = new BufferedReader(new FileReader(info2));
	 line = in.readLine(); 
	 while (line != null)
	 {
	    names2.add(line);
	    line = in.readLine();
	 } 
      } catch ( FileNotFoundException e )
      {
	 throw new PipelineException(e.getMessage());
      } catch ( IOException e )
      {
	 throw new PipelineException(e.getMessage());
      }
      
      Iterator<String> it1 = names1.iterator();
      Iterator<String> it2 = names2.iterator();
      String from1 = it1.next();
      String from2 = it2.next();
      //Temp
//      PrintWriter to;
//      try
//      {
//	 to = new PrintWriter(new BufferedWriter(new FileWriter(tempFile)));
//      } catch ( IOException e1 )
//      {
//	 throw new PipelineException(e1.getMessage());
//      }
      //Temp
      while (it2.hasNext() && it1.hasNext())
      {
	 //to.println("Comparing: " + from1 + "\t" + from2);
	 int compare = from1.compareTo(from2);
	 //to.println("Compare value is : " + compare);
	 if(compare == 0)
	 {
	    from1 = it1.next();
	    from2 = it2.next();
	 } else if (compare > 0)
	 {
	    in2.add(from2);
	    //to.println("Adding :" + from2 + " to List2");
	    from2 = it2.next();
	 } else
	 {
	    in1.add(from1);
	    //to.println("Adding :" + from1 + " to List1");
	    from1 = it1.next();
	 }
	 if (!from1.matches(geoPattern))
	    nonStandard1.add(from1);
	 if (!from2.matches(geoPattern))
	    nonStandard2.add(from2);
      }
      if (from1 != null && from2 == null)
      {
	 in1.add(from1);
	 //to.println("Adding :" + from1 + " to List1");
	 if (!from1.matches(geoPattern))
	    nonStandard1.add(from1);
      }
      if (from2 != null && from1 == null)
      {
	 in2.add(from2);
	 //to.println("Adding :" + from2 + " to List2");
	 if (!from2.matches(geoPattern))
	    nonStandard2.add(from2);
      }
      while(it1.hasNext())
      {
	 from1= it1.next();
	 in1.add(from1);
	 //to.println("Adding :" + from1 + " to List1");
	 if (!from1.matches(geoPattern))
	    nonStandard1.add(from1);
      }
      while(it2.hasNext())
      {
	 from2= it2.next();
	 in2.add(from2);
	 //to.println("Adding :" + from2 + " to List2");
	 if (!from2.matches(geoPattern))
	    nonStandard2.add(from2);
      }
      
      //to.close();
      File finalFile = null;
      try
      {
	 finalFile = File.createTempFile("GeoNameCompareTool-Compare.", ".txt",
	    PackageInfo.sTempPath.toFile());

      } catch ( IOException ex )
      {
	 throw new PipelineException(
	    "Unable to create the temporary text file used to store the texture "
		  + "information collected from the Maya scene!");
      }
      
      try
      {
	 PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(finalFile)));
	 out.println("File 1: " + fileNames.first() );
	 out.println("Unique Names:");
	 for (String a : in1)
	    out.println(a);
	 out.println();
	 out.println("\tBAD NAMES: " + fileNames.last() );
	 for (String a : nonStandard1)
	    out.println("\t" + a);
	 out.println();
	 out.println();
	 out.println();
	 out.println("File 2: " + fileNames.last() );
	 out.println("Unique Names:");
	 for (String a : in2)
	    out.println(a);
	 out.println();
	 out.println("\tBAD NAMES: " + fileNames.last() );
	 for (String a : nonStandard2)
	    out.println("\t" + a);


	 out.close();
	 
      } catch ( IOException e )
      {
	 throw new PipelineException(
	    "Unable to create the text file to write out the comparison.");
      } 
      
      ArrayList<String> args = new ArrayList<String>();
      args.add(finalFile.getAbsolutePath());

      TreeMap<String, String> env = mclient.getToolsetEnvironment(id.getAuthor(), id
	 .getView(), mods.get(0).getToolset(), PackageInfo.sOsType);

      String appName = null;
      if ( PackageInfo.sOsType.equals(OsType.Unix) )
	 appName = "kwrite";
      else if ( PackageInfo.sOsType.equals(OsType.Windows) )
	 appName = "notepad.exe";

      SubProcessLight proc = new SubProcessLight("GeoNameCompareTool", appName, args, env,
	 finalFile.getParentFile());
      proc.start();
      
      
      return false;
   }

   private ArrayList<NodeMod> mods;
   
   private static final long serialVersionUID = -1838971325516667449L;
   
   private static String geoPattern = "(r|l|m)_.*_(geo|lorez|geoBase|wrdBase)";
}
