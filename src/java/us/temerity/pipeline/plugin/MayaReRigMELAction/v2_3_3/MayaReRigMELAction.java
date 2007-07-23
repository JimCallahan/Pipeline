/*
 * Created on Jul 8, 2007
 * Created by jesse
 * For Use in us.temerity.pipeline.plugin.MayaReRigMELAction.v2_3_3
 * 
 */
package us.temerity.pipeline.plugin.MayaReRigMELAction.v2_3_3;

import java.io.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.CommonActionUtils;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   R E R I G   M E L   A C T I O N                                              */
/*------------------------------------------------------------------------------------------*/

public 
class MayaReRigMELAction 
  extends CommonActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaReRigMELAction() 
  {
    super("MayaReRigMEL", new VersionID("2.3.3"), "Temerity",
          "Creates a MEL script that can be used to copy a rig from one model to another.");
  
    underDevelopment();
    
    {
      ActionParam param = 
        new BooleanActionParam
        (aCopyBind,
         "Whether to include the MEL code that copys binding information from " +
         "one rig to another.",
         true);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new BooleanActionParam
        (aCopyWeights,
         "Whether to include the MEL code that copys weighting information from " +
         "one bound model to another.",
         true);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new BooleanActionParam
        (aCopySets,
         "Whether to include the MEL code that copys sets " +
         "one bound model to another.",
         true);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new StringActionParam
        (aSourcePrefix,
         "The prefix that is going to be prepended to the source rig.",
         "source");
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new StringActionParam
        (aGeoSet,
         "The Geometry set containing all the geometry that is having the rig applied to it.",
         "GEO");
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
      
      boolean copyBind = getSingleBooleanParamValue(aCopyBind);
      boolean copyWeights = getSingleBooleanParamValue(aCopyWeights);
      boolean copySets = getSingleBooleanParamValue(aCopyWeights);
      String prefix = getSingleStringParamValue(aSourcePrefix);
      String geo = getSingleStringParamValue(aGeoSet);
      
      if (copyBind || copyWeights || copySets) {
	out.write
	  ("global proc string removePrefixLong(string $name)\n" + 
	    "{\n" + 
	    "  string $toReturn;\n" + 
	    "  string $buffer[];\n" + 
	    "  tokenize($name, \"|\", $buffer);\n" + 
	    "  string $part;\n" + 
	    "  for ($part in $buffer)\n" + 
	    "  {\n" + 
	    "    string $buffer2[];\n" + 
	    "    tokenize($part, \":\", $buffer2);\n" + 
	    "    if ($toReturn == \"\")\n" + 
	    "      $toReturn += $buffer2[(size($buffer2) -1)];\n" + 
	    "    else\n" + 
	    "      $toReturn += \"|\" + $buffer2[(size($buffer2) -1)];\n" + 
	    "  }\n" + 
	    "  return $toReturn;	\n" + 
	    "}\n\n");
	out.write
	  ("global proc string buildNewName(string $name, string $prefix)\n" + 
	   "{\n" + 
	   "  string $toReturn = \"\";\n" + 
	   "  if(startsWith($name , \"|\"))\n" + 
	   "    $toReturn = \"|\";\n" + 
	   "  string $buffer[];\n" + 
	   "  tokenize($name, \"|\", $buffer);\n" + 
	   "  int $i;\n" + 
	   "  for ($i = 0; $i < size($buffer); $i++)\n" + 
	   "  {\n" + 
	   "    if ($toReturn == \"|\" || $toReturn == \"\")\n" + 
	   "      $toReturn += $prefix + $buffer[$i];\n" + 
	   "    else\n" + 
	   "      $toReturn += \"|\" + $prefix + $buffer[$i];	\n" + 
	   "  }\n" + 
	   "  return $toReturn;	\n" + 
	   "}\n\n");
      }
      
      if (copyBind)
	out.write
	  ("global proc copyBind(string $prefix)\n" + 
	   "{\n" + 
	   "  string $polyz[] = `ls -sl -l`;\n" + 
	   "  print(\"poly list: \\n\");\n" + 
	   "  print($polyz);\n" + 
	   "  for ($poly in $polyz)\n" + 
	   "  {\n" + 
	   "    string $baseName = removePrefixLong($poly);\n" + 
	   "    string $oldName = buildNewName($baseName, $prefix);\n" + 
	   "    string $skinCluster = findRelatedSkinCluster($oldName);\n" + 
	   "    if ($skinCluster != \"\")\n" + 
	   "    {\n" + 
	   "      string $jointz[] = `listConnections -s true -d false -type joint $skinCluster`;\n" + 
	   "      $jointz = stringArrayRemoveDuplicates($jointz);\n" + 
	   "      print($jointz);\n" + 
	   "      string $newJoints[];\n" + 
	   "      string $joint;\n" + 
	   "      for ($joint in $jointz)\n" + 
	   "      {\n" + 
	   "        $newJoints[size($newJoints)] = removePrefixLong($joint);\n" + 
	   "      }\n" + 
	   "      print($newJoints);\n" + 
	   "      select -cl;\n" + 
	   "      for ($joint in $newJoints)\n" + 
	   "      {\n" + 
	   "        catch(`select -add $joint`);\n" + 
	   "      }\n" + 
	   "      select -add $poly;\n" + 
	   "      newSkinCluster \"-toSelectedBones -ignoreHierarchy -mi 1 -dr 2.5 -rui false\";\n" + 
	   "     }\n" + 
	   "   }\n" + 
	  "}\n\n");
      
      if (copyWeights)
	out.write
	  ("global proc copyWeights(string $prefix)\n" + 
	   "{\n" + 
	   "  string $polyz[] = `ls -sl -l`;\n" + 
	   "  for ($poly in $polyz)\n" + 
	   "  {\n" + 
	   "    string $baseName = removePrefixLong($poly);\n" + 
	   "    string $oldName = buildNewName($baseName, $prefix);\n" + 
	   "    string $newCluster = findRelatedSkinCluster($poly);\n" + 
	   "    string $oldCluster = findRelatedSkinCluster($oldName);\n" + 
	   "    print($newCluster + \"\\t\" + $oldCluster + \"\\n\");\n" + 
	   "    if ($oldCluster != \"\" && $newCluster != \"\")\n" + 
	   "    {\n" + 
	   "      copySkinWeights -ss $oldCluster -ds $newCluster -noMirror;\n" + 
	   "    }\n" + 
	   "  }\n" + 
  	   "}\n\n");
      
      if (copySets)
	out.write
	  ("global proc copySets(string $prefix)\n" + 
	   "{\n" + 
	   "  string $set;\n" + 
	   "  for ($set in `ls -type objectSet ($prefix + \"*\")`)\n" + 
	   "  {\n" + 
	   "    print($set + \"\\n\");\n" + 
	   "    string $baseName = removePrefixLong($set);\n" + 
	   "    if (!`objExists($baseName)`)\n" + 
	   "    {\n" + 
	   "      string $members[] = `sets -q $set`;\n" + 
	   "      string $newMembers[];\n" + 
	   "      string $mem;\n" + 
	   "      for ($mem in $members)\n" + 
	   "      {\n" + 
	   "        $newMembers[size($newMembers)] = removePrefixLong($mem);\n" + 
	   "      }\n" + 
	   "      select -r $newMembers;\n" + 
	   "      sets -n $baseName;\n" + 
	   "    }\n" + 
	   "  }\n" + 
 	   "}\n\n");
      
      if (copyBind) 
	out.write
	  ("select -r \"" + geo + "\";\n" + 
	   "copyBind(\"" + prefix + ":\");\n\n");
      if (copyWeights)
	out.write
	  ("select -r \"" + geo + "\";\n" + 
           "copyWeights(\"" + prefix + ":\");\n\n");
      
      if (copySets)
	out.write
	  ("copySets(\"" + prefix + ":\");\n\n");
      

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
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 5762268334218205732L;
  public static final String aCopyBind     = "CopyBind";
  public static final String aCopyWeights  = "CopyWeights";
  public static final String aCopySets     = "CopySets";
  public static final String aSourcePrefix = "SourcePrefix";
  public static final String aGeoSet       = "GeoSet";
  
}
