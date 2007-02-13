package com.sony.scea.pipeline.plugins.v1_0_0;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/**
 * A tool for copying shaders from the low-rez material scene to the hi-rez scene.
 * <p>
 * On lair, the texture team used the low-rez material scene as a place to assign
 * all of their textures to surface shaders to do look development.  This tool
 * exported those shaders, imported them into the hi-rez scene, converted them to
 * blinns and assigned them to the right geo.  Intended to give material artists
 * a nice jump start.
 *  <p>
 *  Select the hi-rez and the low-rez material scene and then run this tool.
 * @author Jesse Clemens
 *
 */
public class ShaderCopyTool extends BaseTool
{
   public ShaderCopyTool()
   {
      super("ShaderCopy", new VersionID("1.0.0"), "SCEA",
	 "Copies materials from a low-rez scene to a hi-rez scene "
	       + "and convert surface shaders to blinns.");

      addSupport(OsType.Windows);
      underDevelopment();
   }

   public synchronized String collectPhaseInput() throws PipelineException
   {
      if ( pSelected.size() != 2 )
	 throw new PipelineException("Must have two node selected.");

      for (String name : pSelected.keySet())
      {
	 if ( name.matches(hirezMatPattern) )
	    hiRezScene = name;
	 if ( name.matches(lorezMatPattern) )
	    loRezScene = name;
      }

      if ( hiRezScene == null || loRezScene == null )
	 throw new PipelineException("You did not select a low-rez "
	       + "and a hi-rez material node");

      return ": cause I'm a genie in a bottle . . . ";
   }

   public synchronized boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient)
      throws PipelineException
   {
      /*
       * TEMPSHIT
       * 
       */
      File tempFile = null;
      try
      {
	 tempFile = File.createTempFile("ShaderCopyTool-Temp.", ".txt",
	    PackageInfo.sTempPath.toFile());
	 FileCleaner.add(tempFile);
      } catch ( IOException ex )
      {
	 throw new PipelineException(
	    "Unable to create the temporary text file used to store the texture "
		  + "information collected from the Maya scene!");
      }

      PrintWriter to;
      try
      {
	 to = new PrintWriter(new BufferedWriter(new FileWriter(tempFile)));
      } catch ( IOException e1 )
      {
	 throw new PipelineException(e1.getMessage());
      }

      //END TEMP
      Path hiRezPath;
      Path loRezPath;
      NodeID loID;
      NodeID hiID;
      NodeMod loMod;
      NodeMod hiMod;
      {
	 NodeStatus stat = pSelected.get(hiRezScene);
	 hiID = stat.getNodeID();
	 hiMod = stat.getDetails().getWorkingVersion();
	 FileSeq fseq = hiMod.getPrimarySequence();
	 hiRezPath = new Path(PackageInfo.sProdPath, hiID.getWorkingParent() + "/"
	       + fseq.getPath(0));
      }

      {
	 NodeStatus stat = pSelected.get(loRezScene);
	 loID = stat.getNodeID();
	 loMod = stat.getDetails().getWorkingVersion();
	 FileSeq fseq = loMod.getPrimarySequence();
	 loRezPath = new Path(PackageInfo.sProdPath, loID.getWorkingParent() + "/"
	       + fseq.getPath(0));
      }

      File eScript;
      File iScript;
      File info;
      File exportedScene;
      {
	 try
	 {
	    eScript = File.createTempFile("ShaderCopyTool-Export.", ".mel",
	       PackageInfo.sTempPath.toFile());
	    FileCleaner.add(eScript);
	 } catch ( IOException ex )
	 {
	    throw new PipelineException("Unable to create the temporary MEL script used "
		  + "to do the shader export!");
	 }
	 try
	 {
	    info = File.createTempFile("ShaderCopyTool-Info.", ".txt",
	       PackageInfo.sTempPath.toFile());
	    FileCleaner.add(info);
	 } catch ( IOException ex )
	 {
	    throw new PipelineException(
	       "Unable to create the temporary text file used to store the texture "
		     + "information collected from the Maya scene!");
	 }
	 try
	 {
	    iScript = File.createTempFile("ShaderCopyTool-Import.", ".mel",
	       PackageInfo.sTempPath.toFile());
	    FileCleaner.add(iScript);
	 } catch ( IOException ex )
	 {
	    throw new PipelineException("Unable to create the temporary MEL script used "
		  + "to do the shader import!");
	 }

	 try
	 {
	    exportedScene = File.createTempFile("ShaderCopyTool-Exported.", ".ma",
	       PackageInfo.sTempPath.toFile());
	    FileCleaner.add(exportedScene);
	 } catch ( IOException ex )
	 {
	    throw new PipelineException("Unable to create the temporary MEL script used "
		  + "to do the shader import!");
	 }

	 try
	 {
	    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(eScript)));
	    out.println("string $shaderNode;");
	    out.println("for ($shaderNode in `ls -type \"surfaceShader\"`)");
	    out.println("{");
	    out.println("	string $replaceNode = `createNode blinn`;");
	    out.println("");
	    out.println("	string $connections[] = "
		  + "`listConnections ($shaderNode+\".message\")`;");
	    out.println("	string $item;");
	    out.println("	for ($item in $connections)");
	    out.println("	{");
	    out.println("		string $info;");
	    out.println("        	if (`objectType $item` == \"materialInfo\")");
	    out.println("		{");
	    out.println("			catch(`disconnectAttr "
		  + "($shaderNode+\".message\") ($info + \".material\")`);");
	    out.println("		}");
	    out.println("	}");
	    out.println("");
	    out.println("	string $texs[] = "
		  + "`listConnections -p true -d false ($shaderNode+\".outColor\")`;");
	    out.println("");
	    out.println("        replaceNode $shaderNode $replaceNode;");
	    out.println("        delete $shaderNode;");
	    out.println("	rename $replaceNode $shaderNode;");
	    out.println("	connectAttr -f $texs[0] ($shaderNode + \".color\");");
	    out.println("}");
	    out.println("select -r -ne `ls -type shadingEngine \"*\"`;");
	    out.println("file -f -type \"mayaAscii\" -options \"v=0\" -es \""
		  + fixPath(exportedScene.getAbsolutePath()) + "\";");
	    out
	       .println("$out = `fopen \"" + fixPath(info.getAbsolutePath()) + "\" \"w\"`;");
	    out.println("string $engine;");
	    out.println("for ($engine in `ls -type \"shadingEngine\" \"*\"`)");
	    out.println("{");
	    out.println("	fprint($out, \"Shader:\" + $engine + \"\\n\");");
	    out.println("	string $names[] = `sets -q $engine`;");
	    out.println("	string $name;");
	    out.println("	for ($name in $names)");
	    out.println("		fprint($out, $name + \"\\n\");");
	    out.println("}");
	    out.println("fclose $out;");
	    out.close();
	 } catch ( IOException e )
	 {
	    throw new PipelineException("Unable to write the temporary MEL script ("
		  + eScript + ") used to export the shaders");
	 }

	 try
	 {
	    ArrayList<String> args = new ArrayList<String>();
	    args.add("-batch");
	    args.add("-script");
	    args.add(eScript.getPath());
	    args.add("-file");
	    args.add(loRezPath.toOsString());

	    Path wdir = new Path(PackageInfo.sProdPath.toOsString()
		  + loID.getWorkingParent());
	    TreeMap<String, String> env = mclient.getToolsetEnvironment(loID.getAuthor(),
	       loID.getView(), loMod.getToolset(), PackageInfo.sOsType);

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

	    SubProcessLight proc = new SubProcessLight("ShaderCopyTool", command, args,
	       env, wdir.toFile());
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
	    to.println(proc.getStdErr());
	    to.println(proc.getCommand());
	 } catch ( Exception ex )
	 {
	    throw new PipelineException(ex);
	 }
      }
      
      try
	 {
	    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(iScript)));
	    out.println("file -import -type \"mayaAscii\" -options \"v=0\" \""+fixPath(exportedScene.getAbsolutePath()) +"\";");
	    out.println("$in = `fopen \""+fixPath(info.getAbsolutePath())+"\" \"r\"`;");
	    out.println("string $line =`fgetline $in`;");
	    out.println("string $shader;");
	    out.println("int $start = 1;");
	    out.println("string $geo[];");
	    out.println("while (size($line) > 0)");
	    out.println("{");
	    out.println("	if (startsWith($line, \"Shader:\"))");
	    out.println("	{");
	    out.println("		if (!$start)");
	    out.println("		{");
	    out.println("			string $a;");
	    out.println("			for ($a in $geo)");
	    out.println("			{");
	    out.println("				$a = strip($a);	");
	    out.println("				select -r $a;");
	    out.println("				sets -e -fe $shader;");
	    out.println("			}");
	    out.println("			clear $geo;");
	    out.println("			string $buffer[];");
	    out.println("			tokenize(strip($line), \":\", $buffer);");
	    out.println("			$shader = $buffer[1];");
	    out.println("		}");
	    out.println("		if ($start)");
	    out.println("		{");
	    out.println("			$start = 0;");
	    out.println("			string $buffer[];");
	    out.println("			tokenize(strip($line), \":\", $buffer);");
	    out.println("			$shader = $buffer[1];");
	    out.println("		}");
	    out.println("	}");
	    out.println("	else");
	    out.println("		$geo[size($geo)] = strip($line);");
	    out.println("");
	    out.println("	$line =`fgetline $in`;");
	    out.println("}");
	    out.println("file -save;");
	    out.close();
	 } catch ( IOException e )
	 {
	    throw new PipelineException("Unable to write the temporary MEL script ("
		  + iScript + ") used to import the shaders");
	 }
      
	 try
	 {
	    ArrayList<String> args = new ArrayList<String>();
	    args.add("-batch");
	    args.add("-script");
	    args.add(iScript.getPath());
	    args.add("-file");
	    args.add(hiRezPath.toOsString());

	    Path wdir = new Path(PackageInfo.sProdPath.toOsString()
		  + hiID.getWorkingParent());
	    TreeMap<String, String> env = mclient.getToolsetEnvironment(hiID.getAuthor(),
	       hiID.getView(), hiMod.getToolset(), PackageInfo.sOsType);

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

	    SubProcessLight proc = new SubProcessLight("ShaderCopyTool", command, args,
	       env, wdir.toFile());
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
	    to.println(proc.getStdErr());
	    to.println(proc.getCommand());
	 } catch ( Exception ex )
	 {
	    throw new PipelineException(ex);
	 }
      
	 to.close();
      return false;
   }

   private static final String fixPath(String path)
   {
      return path.replaceAll("\\\\", "/");
   }

   private String hiRezScene = null;
   private String loRezScene = null;

   private static final long serialVersionUID = 4157713505988545669L;
   private static String hirezMatPattern = ".*/assets/(character|set|prop)/.*/material/.*_mat";
   private static String lorezMatPattern = ".*/assets/(character|set|prop)/.*/material/.*_mat_lr";
}
