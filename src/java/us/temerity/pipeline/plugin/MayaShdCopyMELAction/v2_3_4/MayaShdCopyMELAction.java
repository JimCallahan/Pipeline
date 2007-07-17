package us.temerity.pipeline.plugin.MayaShdCopyMELAction.v2_3_4;

import java.io.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.CommonActionUtils;


public 
class MayaShdCopyMELAction
  extends CommonActionUtils
{

  public
  MayaShdCopyMELAction() 
  {
    super("MayaShdCopyMEL", new VersionID("2.3.4"), "Temerity",
          "Creates a MEL script that can be used to copy shader assignments from one model to another.");
  
    underDevelopment();
    {
      ActionParam param = 
        new BooleanActionParam
        (aRemoveReferences,
         "Whether to include the MEL code that removes all remaining references.",
         true);
      addSingleParam(param);
    }
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a {@link SubProcessHeavy SubProcessHeavy} instance which when executed will 
   * fulfill the given action agenda. <P> 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   * 
   * @return 
   *   The SubProcess which will fulfill the agenda.
   * 
   * @throws PipelineException 
   *   If unable to prepare a SubProcess due to illegal, missing or imcompatable 
   *   information in the action agenda or a general failure of the prep method code.
   */
  public SubProcessHeavy
  prep
  (
   ActionAgenda agenda,
   File outFile, 
   File errFile 
  )
    throws PipelineException
  { 
    /* target MEL script */ 
    Path target = getPrimaryTargetPath(agenda, "mel", "MEL script");

    /* create a temporary file which will be copied to the target */ 
    File temp = createTemp(agenda, "mel");
    try {      
      FileWriter out = new FileWriter(temp);
      
      out.write
        ("global proc string getComponentPart(string $name)\n" + 
         "{\n" + 
         "  string $toReturn = \"\";\n" + 
         "  string $buffer[];\n" + 
         "  tokenize($name, \".\", $buffer);\n" + 
         "  if (size($buffer) > 2)\n" + 
         "    error(\"Mal-formed name \" + $name + \"\\n\");\n" + 
         "  if (size($buffer) > 1)\n" + 
         "    $toReturn = \".\" + $buffer[(size($buffer) -1)];\n" + 
         "  return $toReturn;\n" + 
        "}\n\n");
      
      out.write
        ("global proc string trimComponentPart(string $name)\n" + 
         "{\n" + 
         "  string $toReturn;\n" + 
         "  string $buffer[];\n" + 
         "  tokenize($name, \".\", $buffer);\n" + 
         "  if (size($buffer) > 2)\n" + 
         "    error(\"Mal-formed name \" + $name + \"\\n\");\n" + 
         "  if (size($buffer) > 1)\n" + 
         "    $toReturn = $buffer[0];\n" + 
         "  return $toReturn;\n" + 
         "}\n\n");
      
      out.write
        ("global proc string removePrefixLong(string $name)\n" + 
         "{\n" + 
         "    string $compPart = getComponentPart($name);\n" + 
         "    //print ($compPart + \"\\n\");\n" + 
         "    if ($compPart != \"\")\n" + 
         "      $name = trimComponentPart($name);\n" + 
         "    //print ($name + \"\\n\");\n" + 
         "    string $toReturn;\n" + 
         "    string $buffer[];\n" + 
         "    tokenize($name, \"|\", $buffer);\n" + 
         "    string $part;\n" + 
         "    for ($part in $buffer)\n" + 
         "    {\n" + 
         "        string $buffer2[];\n" + 
         "        tokenize($part, \":\", $buffer2);\n" + 
         "        if ($toReturn == \"\")\n" + 
         "            $toReturn += $buffer2[(size($buffer2) -1)];\n" + 
         "        else\n" + 
         "            $toReturn += \"|\" + $buffer2[(size($buffer2) -1)];\n" + 
         "    }\n" + 
         "    if ($compPart != \"\")\n" + 
         "      $toReturn += $compPart;\n" + 
         "    return $toReturn;    \n" + 
         "}\n\n");

      out.write
        ("global proc int hasNamespace(string $name)\n" + 
         "{\n" + 
         "  string $buffer[];\n" + 
         "  tokenize($name, \":\", $buffer);\n" + 
         "  if (size($buffer) > 1)\n" + 
         "    return 1;\n" + 
         "  return 0;\n" + 
        "}\n\n");
      
      out.write
        ("global proc copyLink(string $oldEngine, string $attr, string $newEngine)\n" + 
         "{\n" + 
         "  print (\"\\t\\t\" + $attr + \"\\n\");\n" + 
         "  if (`connectionInfo -id ($oldEngine + \".\" + $attr)`)\n" + 
         "  {\n" + 
         "    string $newDest = removePrefixLong(`connectionInfo -sfd ($oldEngine + \".\" + $attr)`);\n" + 
         "    print (\"\\t\\t\" + $newDest + \"\\n\");\n" + 
         "    connectAttr -f $newDest ($newEngine + \".\" + $attr);\n" + 
         "  }\n" + 
        "}\n\n");
      
      out.write
      ("{\n" + 
       "  string $shaderEngines[] = `ls -type shadingEngine \"source:*\"`;\n" + 
       "  string $shadeEngine;\n" + 
       "  \n" + 
       "  {\n" + 
       "    string $shaderEngines[] = `ls -type shadingEngine \"source:*\"`;\n" + 
       "    string $shadeEngine;\n" + 
       "    \n" + 
       "    \n" + 
       "    string $newEngines[];\n" + 
       "    for ($shadeEngine in $shaderEngines)\n" + 
       "    {\n" + 
       "      $newEngines[`size $newEngines`] = removePrefixLong($shadeEngine);\n" + 
       "    }\n" + 
       "    string $allEngines[] = `ls -type \"shadingEngine\" \"*\"`;\n" + 
       "    for ($shadeEngine in $allEngines)\n" + 
       "    {\n" + 
       "      print($shadeEngine + \"\\n\");\n" + 
       "      int $test = 0;\n" + 
       "      string $delete;\n" + 
       "      string $temp;\n" + 
       "      for($temp in $newEngines)\n" + 
       "        if ($temp == $shadeEngine)\n" + 
       "          $test = 1;\n" + 
       "      if (!$test)\n" + 
       "      {\n" + 
       "        print(\"Deleting engine \" + $shadeEngine);\n" + 
       "        select -r -ne $shadeEngine;\n" + 
       "        delete;\n" + 
       "      }\n" + 
       "    }\n" + 
       "  }\n" + 
       "  \n" + 
       "  for ($shadeEngine in $shaderEngines)\n" + 
       "  {\n" + 
       "    print($shadeEngine + \"\\n\");\n" + 
       "    select -r $shadeEngine;\n" + 
       "    string $objs[] = `ls -sl`;\n" + 
       "    string $newEngine = removePrefixLong($shadeEngine);\n" + 
       "    print(\"making \" +  $newEngine + \"\\n\");\n" + 
       "    \n" + 
       "    sets -renderable true -noSurfaceShader true -empty -name $newEngine;\n" + 
       "    \n" + 
       "    \n" + 
       "    \n" + 
       "    copyLink($shadeEngine, \"surfaceShader\", $newEngine);\n" + 
       "    copyLink($shadeEngine, \"displacementShader\", $newEngine);\n" + 
       "    copyLink($shadeEngine, \"volumeShader\", $newEngine);\n" + 
       "    copyLink($shadeEngine, \"mims\", $newEngine);\n" + 
       "    copyLink($shadeEngine, \"mivs\", $newEngine);\n" + 
       "    copyLink($shadeEngine, \"mips\", $newEngine);\n" + 
       "    copyLink($shadeEngine, \"mipv\", $newEngine);\n" + 
       "    copyLink($shadeEngine, \"mids\", $newEngine);\n" + 
       "    copyLink($shadeEngine, \"mies\", $newEngine);\n" + 
       "    copyLink($shadeEngine, \"milm\", $newEngine);\n" + 
       "    copyLink($shadeEngine, \"mics\", $newEngine);\n" + 
       "    \n" + 
       "    select -cl;\n" + 
       "    string $object;\n" + 
       "    //copying the shader information.\n" + 
       "    string $allObjects[];\n" + 
       "    clear($allObjects);\n" + 
       "    print(size($allObjects) + \"\\n\");\n" + 
       "    for ($object in $objs)\n" + 
       "    {\n" + 
       "      print(\"\\t\" + $object + \"\\n\");\n" + 
       "      if (hasNamespace($object))\n" + 
       "      {\n" + 
       "        string $newObj = removePrefixLong($object);\n" + 
       "        print(\"\\t\" + $newObj + \"\\n\");\n" + 
       "        $allObjects[size($allObjects)] = $newObj;\n" + 
       "        \n" + 
       "      } else\n" + 
       "      print(\"\\tSkipping\\n\");\n" + 
       "    }\n" + 
       "      select -cl;\n" + 
       "      string $crap;\n" + 
       "      for ($crap in $allObjects)\n" + 
       "        select -add $crap;\n" + 
       "      sets -e -fe $newEngine;\n" + 
       "      select -cl;\n" + 
       "  }\n" + 
       "  //copying the set information.\n" + 
       "  {\n" + 
       "    string $set;\n" + 
       "    string $allSets[] = `ls -type objectSet \"source:r_*\"`;\n" + 
       "    for ($set in $allSets)\n" + 
       "    {\n" + 
       "      select -cl;\n" + 
       "      string $newName = removePrefixLong($set);\n" + 
       "      string $newSet = `sets -n $newName`;\n" + 
       "      string $names[] = `sets -q $set`;\n" + 
       "      string $obj;\n" + 
       "      for ($obj in $names)\n" + 
       "      {  \n" + 
       "        string $newObj = removePrefixLong($obj);\n" + 
       "        select -r $newObj;\n" + 
       "        sets -fe $newSet;\n" + 
       "      }\n" + 
       "\n" + 
       "    }\n" + 
       "  }\n" + 
       "  MLdeleteUnused;\n" + 
      "}\n\n");
      
      
      if (getSingleBooleanParamValue(aRemoveReferences)) {
        out.write("{\n" + 
                  "  string $files[] = `file -q -r`;\n" + 
                  "  while (size($files) > 0)\n" + 
                  "  {\n" + 
                  "    $file = $files[0];\n" + 
                  "    file -rr $file;\n" + 
                  "    $files = `file -q -r`;\n" + 
                  "  }\n" + 
                  "}");
      }
      
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write the target MEL script file (" + temp + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }

    /* create the process to run the action */ 
    return createTempCopySubProcess(agenda, temp, target, outFile, errFile);
  }
    
  public static final String aRemoveReferences = "RemoveReferences";
  private static final long serialVersionUID = -5314337235691558249L;

}
