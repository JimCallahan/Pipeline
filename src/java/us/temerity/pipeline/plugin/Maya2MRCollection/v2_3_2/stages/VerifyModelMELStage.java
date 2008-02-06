package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages;


import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.StageFunction;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.FileWriterStage;

public 
class VerifyModelMELStage
  extends FileWriterStage
{
  public
  VerifyModelMELStage
  (
    StageInformation info,
    UtilContext context,
    MasterMgrClient client,
    String nodeName  
  )
    throws PipelineException
  {
    super("VerifyModelMEL",
      "Stage to make a verify model MEL that creates the right geo groups",
      info,
      context,
      client,
      nodeName,
      "mel",
      StageFunction.aScriptFile);
    
    String s = "// String to verify that all geometry has the right name.\n" + 
    		"{\n" + 
    		"\n" + 
    		"\n" + 
    		"string $badNames[];\n" + 
    		"string $meshes[];\n" + 
    		"string $poly;\n" + 
    		"for ($poly in `ls -type mesh`)\n" + 
    		"{\n" + 
    		"  string $parentz[] = `listRelatives -p $poly`;\n" + 
    		"  string $parent = $parentz[0];\n" + 
    		"  if(`gmatch $parent \"*_geo\"`)\n" + 
    		"    $meshes[size($meshes)] = $parent;\n" + 
    		"  else if(!`gmatch $parent \"*_hide\"`)\n" + 
    		"    $badNames[size($badNames)] = $parent;\n" + 
    		"}\n" + 
    		"\n" + 
    		"string $thing;\n" + 
    		"if (size($badNames) > 0)\n" + 
    		"{\n" + 
    		"  print \"The following models do not match the geometry naming convention!\\n\";\n" + 
    		"  for ($thing in $badNames)\n" + 
    		"  {\n" + 
    		"     print(\"\\t\" + $thing + \"\\n\");\n" + 
    		"  }\n" + 
    		"}\n" + 
    		"\n" + 
    		"if (size($badNames) > 0)\n" + 
    		"  error(\"All pieces of geometry must end with the _geo or the _hide suffix.\\n\");\n" + 
    		"\n" + 
    		"select -r $meshes;\n" + 
    		"sets -n GEO;\n" + 
    		"\n" + 
    		"}\n\n";
    setFileContents(s);
  }
  
  private static final long serialVersionUID = -1055531955036096685L;
}
