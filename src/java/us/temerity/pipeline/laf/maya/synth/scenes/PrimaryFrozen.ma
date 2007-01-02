//Maya ASCII 7.0 scene
//Name: PrimaryFrozen.ma
//Last modified: Tue, Jan 02, 2007 01:48:45 AM
file -rdi 1 -rpr "Normal" -rfn "NormalRN" "/home/jim/code/src/pipeline/src/java/us/temerity/pipeline/laf/maya/synth//scenes/Normal.ma";
file -r -rpr "Normal" -dr 1 -rfn "NormalRN" "/home/jim/code/src/pipeline/src/java/us/temerity/pipeline/laf/maya/synth//scenes/Normal.ma";
requires maya "7.0";
currentUnit -l centimeter -a degree -t film;
fileInfo "application" "maya";
fileInfo "product" "Maya Unlimited 7.0";
fileInfo "version" "7.0.1";
fileInfo "cutIdentifier" "200511181718-660870";
fileInfo "osv" "Linux 2.6.16.21-0.25-smp #1 SMP Tue Sep 19 07:26:15 UTC 2006 i686";
createNode transform -s -n "persp";
	setAttr -l on ".v" no;
	setAttr ".t" -type "double3" 0 1.8003966942148764 0 ;
	setAttr -l on ".tx";
	setAttr -l on ".ty";
	setAttr -l on ".tz";
	setAttr ".r" -type "double3" -89.999999999999986 0 0 ;
	setAttr -l on ".rx";
	setAttr -l on ".ry";
	setAttr -l on ".rz";
	setAttr -l on ".sx";
	setAttr -l on ".sy";
	setAttr -l on ".sz";
createNode camera -s -n "perspShape" -p "persp";
	setAttr -k off ".v" no;
	setAttr ".cap" -type "double2" 1 1 ;
	setAttr -l on ".hfa";
	setAttr -l on ".vfa";
	setAttr ".ff" 0;
	setAttr ".ovr" 1.3;
	setAttr ".fl" 18.137505854296897;
	setAttr -l on ".lsr";
	setAttr -l on ".fs";
	setAttr -l on ".fd";
	setAttr -l on ".sa";
	setAttr -l on ".coi" 3;
	setAttr ".imn" -type "string" "persp";
	setAttr ".den" -type "string" "persp_depth";
	setAttr ".man" -type "string" "persp_mask";
	setAttr ".tp" -type "double3" -1.1258632546678049 1.8122357998731848 -1.3581139900309629 ;
	setAttr ".hc" -type "string" "viewSet -p %camera";
	setAttr ".dr" yes;
createNode transform -s -n "top";
	setAttr ".v" no;
	setAttr ".t" -type "double3" 0.22863506963296976 100 0.0025412713374216578 ;
	setAttr ".r" -type "double3" -89.999999999999986 0 0 ;
createNode camera -s -n "topShape" -p "top";
	setAttr -k off ".v" no;
	setAttr ".rnd" no;
	setAttr ".coi" 100;
	setAttr ".ow" 3.1878379624851463;
	setAttr ".imn" -type "string" "top";
	setAttr ".den" -type "string" "top_depth";
	setAttr ".man" -type "string" "top_mask";
	setAttr ".hc" -type "string" "viewSet -t %camera";
	setAttr ".o" yes;
createNode transform -s -n "front";
	setAttr ".v" no;
	setAttr ".t" -type "double3" -0.12498071985990589 -0.0040875503683205361 100 ;
createNode camera -s -n "frontShape" -p "front";
	setAttr -k off ".v" no;
	setAttr ".rnd" no;
	setAttr ".coi" 100;
	setAttr ".ow" 3.3188073296581679;
	setAttr ".imn" -type "string" "front";
	setAttr ".den" -type "string" "front_depth";
	setAttr ".man" -type "string" "front_mask";
	setAttr ".hc" -type "string" "viewSet -f %camera";
	setAttr ".o" yes;
createNode transform -s -n "side";
	setAttr ".v" no;
	setAttr ".t" -type "double3" 100 0 -0.001592767559530972 ;
	setAttr ".r" -type "double3" 0 89.999999999999986 0 ;
createNode camera -s -n "sideShape" -p "side";
	setAttr -k off ".v" no;
	setAttr ".rnd" no;
	setAttr ".coi" 100;
	setAttr ".ow" 4.7910588731065387;
	setAttr ".imn" -type "string" "side";
	setAttr ".den" -type "string" "side_depth";
	setAttr ".man" -type "string" "side_mask";
	setAttr ".hc" -type "string" "viewSet -s %camera";
	setAttr ".o" yes;
createNode lightLinker -n "lightLinker1";
	setAttr -s 3 ".lnk";
createNode brush -n "brush1";
	setAttr ".lcl[0]"  0 0.5 1;
	setAttr ".pcl[0]"  0 0.5 1;
	setAttr ".wsc[0]"  0 1 1;
	setAttr ".lws[0]"  0 1 1;
	setAttr ".pws[0]"  0 1 1;
	setAttr ".tls[0]"  0 1 1;
	setAttr -s 3 ".env";
	setAttr ".env[0].envp" 0.20000000298023224;
	setAttr ".env[0].envc" -type "float3" 0 0 0.15000001 ;
	setAttr ".env[0].envi" 2;
	setAttr ".env[1].envp" 0.5;
	setAttr ".env[1].envc" -type "float3" 0.47999999 0.55000001 0.69999999 ;
	setAttr ".env[1].envi" 2;
	setAttr ".env[2].envp" 1;
	setAttr ".env[2].envc" -type "float3" 0 0.1 0.44999999 ;
	setAttr ".env[2].envi" 2;
	setAttr ".rro[0]"  0 1 1;
createNode displayLayerManager -n "layerManager";
createNode displayLayer -n "defaultLayer";
createNode renderLayerManager -n "renderLayerManager";
createNode renderLayer -n "defaultRenderLayer";
	setAttr ".g" yes;
createNode script -n "sceneConfigurationScriptNode";
	setAttr ".b" -type "string" "playbackOptions -min 0 -max 150 -ast 0 -aet 500 ";
	setAttr ".st" 6;
createNode brush -n "brush2";
	setAttr ".lcl[0]"  0 0.5 1;
	setAttr ".pcl[0]"  0 0.5 1;
	setAttr ".wsc[0]"  0 1 1;
	setAttr ".lws[0]"  0 1 1;
	setAttr ".pws[0]"  0 1 1;
	setAttr ".tls[0]"  0 1 1;
	setAttr -s 3 ".env";
	setAttr ".env[0].envp" 0.20000000298023224;
	setAttr ".env[0].envc" -type "float3" 0 0 0.15000001 ;
	setAttr ".env[0].envi" 2;
	setAttr ".env[1].envp" 0.5;
	setAttr ".env[1].envc" -type "float3" 0.47999999 0.55000001 0.69999999 ;
	setAttr ".env[1].envi" 2;
	setAttr ".env[2].envp" 1;
	setAttr ".env[2].envc" -type "float3" 0 0.1 0.44999999 ;
	setAttr ".env[2].envi" 2;
	setAttr ".rro[0]"  0 1 1;
createNode brush -n "brush3";
	setAttr ".lcl[0]"  0 0.5 1;
	setAttr ".pcl[0]"  0 0.5 1;
	setAttr ".wsc[0]"  0 1 1;
	setAttr ".lws[0]"  0 1 1;
	setAttr ".pws[0]"  0 1 1;
	setAttr ".tls[0]"  0 1 1;
	setAttr -s 3 ".env";
	setAttr ".env[0].envp" 0.20000000298023224;
	setAttr ".env[0].envc" -type "float3" 0 0 0.15000001 ;
	setAttr ".env[0].envi" 2;
	setAttr ".env[1].envp" 0.5;
	setAttr ".env[1].envc" -type "float3" 0.47999999 0.55000001 0.69999999 ;
	setAttr ".env[1].envi" 2;
	setAttr ".env[2].envp" 1;
	setAttr ".env[2].envc" -type "float3" 0 0.1 0.44999999 ;
	setAttr ".env[2].envi" 2;
	setAttr ".rro[0]"  0 1 1;
createNode reference -n "NormalRN";
	setAttr -s 21 ".phl";
	setAttr ".phl[7]" 0;
	setAttr ".phl[8]" 0;
	setAttr ".phl[9]" 0;
	setAttr ".phl[10]" 0;
	setAttr ".phl[11]" 0;
	setAttr ".phl[12]" 0;
	setAttr ".phl[13]" 0;
	setAttr ".phl[14]" 0;
	setAttr ".phl[15]" 0;
	setAttr ".phl[16]" 0;
	setAttr ".phl[17]" 0;
	setAttr ".phl[18]" 0;
	setAttr ".phl[19]" 0;
	setAttr ".phl[20]" 0;
	setAttr ".phl[21]" 0;
	setAttr ".phl[22]" 0;
	setAttr ".phl[23]" 0;
	setAttr ".phl[24]" 0;
	setAttr ".phl[25]" 0;
	setAttr ".phl[26]" 0;
	setAttr ".ed" -type "dataReferenceEdits" 
		"NormalRN"
		"NormalRN" 2
		3 "|Normal_MISSING|Normal_inset|Normal_insettrimmedSurfaceShape1.instObjGroups" 
		"Normal_lambert3SG.dagSetMembers" "-na"
		5 3 "NormalRN" "|Normal_MISSING|Normal_inset|Normal_insettrimmedSurfaceShape1.instObjGroups" 
		"NormalRN.placeHolderList[6]" "Normal_lambert3SG.dsm"
		"NormalRN" 35
		2 "Normal_select" "color" " -type \"float3\" 0 1 1"
		3 "|Normal_MISSING_NEWER|Normal_inset|Normal_insettrimmedSurfaceShape.instObjGroups" 
		"Normal_lambert3SG.dagSetMembers" "-na"
		3 "|Normal_NEEDSCO_MINOR|Normal_inset|Normal_insetShape.instObjGroups" "Normal_lambert3SG.dagSetMembers" 
		"-na"
		3 "|Normal_NEEDSCO_MEGA|Normal_inset|Normal_insetShape.instObjGroups" "Normal_lambert3SG.dagSetMembers" 
		"-na"
		3 "|Normal_OBSOLETE|Normal_inset|Normal_insettrimmedSurfaceShape.instObjGroups" 
		"Normal_lambert3SG.dagSetMembers" "-na"
		3 "|Normal_ADDED|Normal_inset|Normal_insetShape.instObjGroups" "Normal_lambert3SG.dagSetMembers" 
		"-na"
		3 "|Normal_MODIFIED_DEPEND|Normal_inset|Normal_insetShape.instObjGroups" "Normal_lambert3SG.dagSetMembers" 
		"-na"
		3 "|Normal_COLLAPSED|Normal_nurbsSphere3|Normal_nurbsSphereShape3.instObjGroups" 
		"Normal_lambert3SG.dagSetMembers" "-na"
		3 "|Normal_COLLAPSED|Normal_nurbsSphere2|Normal_nurbsSphereShape2.instObjGroups" 
		"Normal_lambert3SG.dagSetMembers" "-na"
		3 "|Normal_COLLAPSED|Normal_nurbsSphere1|Normal_nurbsSphereShape1.instObjGroups" 
		"Normal_lambert3SG.dagSetMembers" "-na"
		3 "|Normal_CONFLICTS|Normal_inset|Normal_insetShape.instObjGroups" "Normal_lambert3SG.dagSetMembers" 
		"-na"
		3 "|Normal_NEEDSCO_MODIFIED|Normal_inset|Normal_insetShape.instObjGroups" "Normal_lambert3SG.dagSetMembers" 
		"-na"
		3 "|Normal_IDENTICAL|Normal_inset|Normal_insettrimmedSurfaceShape.instObjGroups" 
		"Normal_lambert3SG.dagSetMembers" "-na"
		3 "|Normal_CHECKED_IN|Normal_inset|Normal_insetShape.instObjGroups" "Normal_lambert3SG.dagSetMembers" 
		"-na"
		3 "|Normal_PENDING|Normal_inset|Normal_insetShape.instObjGroups" "Normal_lambert3SG.dagSetMembers" 
		"-na"
		3 "|Normal_MODIFIED_LOCKS|Normal_inset|Normal_inset2|Normal_inset2Shape.instObjGroups" 
		"Normal_lambert3SG.dagSetMembers" "-na"
		3 "|Normal_MODIFIED_LOCKS|Normal_inset|Normal_inset1|Normal_inset1Shape.instObjGroups" 
		"Normal_lambert3SG.dagSetMembers" "-na"
		3 "|Normal_MISSING|Normal_inset|Normal_insettrimmedSurfaceShape.instObjGroups" 
		"Normal_lambert3SG.dagSetMembers" "-na"
		5 3 "NormalRN" "|Normal_PENDING|Normal_inset|Normal_insetShape.instObjGroups" 
		"NormalRN.placeHolderList[7]" "Normal_lambert3SG.dsm"
		5 3 "NormalRN" "|Normal_CHECKED_IN|Normal_inset|Normal_insetShape.instObjGroups" 
		"NormalRN.placeHolderList[8]" "Normal_lambert3SG.dsm"
		5 3 "NormalRN" "|Normal_IDENTICAL|Normal_inset|Normal_insettrimmedSurfaceShape.instObjGroups" 
		"NormalRN.placeHolderList[9]" "Normal_lambert3SG.dsm"
		5 3 "NormalRN" "|Normal_NEEDSCO_MODIFIED|Normal_inset|Normal_insetShape.instObjGroups" 
		"NormalRN.placeHolderList[10]" "Normal_lambert3SG.dsm"
		5 3 "NormalRN" "|Normal_CONFLICTS|Normal_inset|Normal_insetShape.instObjGroups" 
		"NormalRN.placeHolderList[11]" "Normal_lambert3SG.dsm"
		5 3 "NormalRN" "|Normal_MODIFIED_DEPEND|Normal_inset|Normal_insetShape.instObjGroups" 
		"NormalRN.placeHolderList[12]" "Normal_lambert3SG.dsm"
		5 3 "NormalRN" "|Normal_MISSING|Normal_inset|Normal_insettrimmedSurfaceShape.instObjGroups" 
		"NormalRN.placeHolderList[13]" "Normal_lambert3SG.dsm"
		5 3 "NormalRN" "|Normal_ADDED|Normal_inset|Normal_insetShape.instObjGroups" "NormalRN.placeHolderList[14]" 
		"Normal_lambert3SG.dsm"
		5 3 "NormalRN" "|Normal_OBSOLETE|Normal_inset|Normal_insettrimmedSurfaceShape.instObjGroups" 
		"NormalRN.placeHolderList[15]" "Normal_lambert3SG.dsm"
		5 3 "NormalRN" "|Normal_NEEDSCO_MEGA|Normal_inset|Normal_insetShape.instObjGroups" 
		"NormalRN.placeHolderList[16]" "Normal_lambert3SG.dsm"
		5 3 "NormalRN" "|Normal_NEEDSCO_MINOR|Normal_inset|Normal_insetShape.instObjGroups" 
		"NormalRN.placeHolderList[17]" "Normal_lambert3SG.dsm"
		5 3 "NormalRN" "|Normal_MISSING_NEWER|Normal_inset|Normal_insettrimmedSurfaceShape.instObjGroups" 
		"NormalRN.placeHolderList[18]" "Normal_lambert3SG.dsm"
		5 3 "NormalRN" "|Normal_MODIFIED_LOCKS|Normal_inset|Normal_inset1|Normal_inset1Shape.instObjGroups" 
		"NormalRN.placeHolderList[19]" "Normal_lambert3SG.dsm"
		5 3 "NormalRN" "|Normal_MODIFIED_LOCKS|Normal_inset|Normal_inset2|Normal_inset2Shape.instObjGroups" 
		"NormalRN.placeHolderList[20]" "Normal_lambert3SG.dsm"
		5 0 "NormalRN" "|Normal_COLLAPSED|Normal_nurbsSphere3|Normal_nurbsSphereShape3.instObjGroups" 
		"Normal_lambert3SG.dagSetMembers" "NormalRN.placeHolderList[21]" "NormalRN.placeHolderList[22]" 
		"Normal_lambert3SG.dsm"
		5 0 "NormalRN" "|Normal_COLLAPSED|Normal_nurbsSphere2|Normal_nurbsSphereShape2.instObjGroups" 
		"Normal_lambert3SG.dagSetMembers" "NormalRN.placeHolderList[23]" "NormalRN.placeHolderList[24]" 
		"Normal_lambert3SG.dsm"
		5 0 "NormalRN" "|Normal_COLLAPSED|Normal_nurbsSphere1|Normal_nurbsSphereShape1.instObjGroups" 
		"Normal_lambert3SG.dagSetMembers" "NormalRN.placeHolderList[25]" "NormalRN.placeHolderList[26]" 
		"Normal_lambert3SG.dsm";
createNode animCurveTU -n "white_incandescenceR";
	setAttr ".tan" 5;
	setAttr ".wgt" no;
	setAttr -s 3 ".ktv[0:2]"  0 1 5 0.5 8 1;
	setAttr -s 3 ".kit[0:2]"  3 3 9;
	setAttr ".pst" 3;
createNode animCurveTU -n "white_incandescenceG";
	setAttr ".tan" 5;
	setAttr ".wgt" no;
	setAttr -s 3 ".ktv[0:2]"  0 0.25 5 0.5 8 0.25;
	setAttr -s 3 ".kit[0:2]"  3 3 9;
	setAttr ".pst" 3;
createNode animCurveTU -n "white_incandescenceB";
	setAttr ".tan" 5;
	setAttr ".wgt" no;
	setAttr -s 3 ".ktv[0:2]"  0 1 5 1 8 1;
	setAttr -s 3 ".kit[0:2]"  3 3 9;
	setAttr ".pst" 3;
createNode lambert -n "white";
	setAttr ".c" -type "float3" 0 0 0 ;
	setAttr ".miic" -type "float3" 3.1415927 3.1415927 3.1415927 ;
createNode shadingEngine -n "whiteSG";
	setAttr ".ihi" 0;
	setAttr -s 15 ".dsm";
	setAttr ".ro" yes;
createNode materialInfo -n "materialInfo1";
createNode reference -n "_UNKNOWN_REF_NODE_";
	setAttr ".ed" -type "dataReferenceEdits" 
		"_UNKNOWN_REF_NODE_"
		"_UNKNOWN_REF_NODE_" 0;
select -ne :time1;
	setAttr ".o" 56;
select -ne :renderPartition;
	setAttr -s 8 ".st";
select -ne :renderGlobalsList1;
select -ne :defaultShaderList1;
	setAttr -s 8 ".s";
select -ne :postProcessList1;
	setAttr -s 2 ".p";
select -ne :lightList1;
	setAttr -s 2 ".l";
	setAttr -s 2 ".ln";
select -ne :lambert1;
	setAttr ".miic" -type "float3" 3.1415927 3.1415927 3.1415927 ;
select -ne :initialShadingGroup;
	setAttr ".ro" yes;
select -ne :initialParticleSE;
	setAttr ".ro" yes;
select -ne :defaultRenderGlobals;
	addAttr -ci true -sn "inputPath" -ln "inputPath" -dt "string";
	addAttr -ci true -sn "shaderPath" -ln "shaderPath" -dt "string";
	addAttr -ci true -sn "texturePath" -ln "texturePath" -dt "string";
	addAttr -ci true -sn "generatorPath" -ln "generatorPath" -dt "string";
	addAttr -ci true -sn "imageioPath" -ln "imageioPath" -dt "string";
	addAttr -ci true -sn "gelatoTextureLocation" -ln "gelatoTextureLocation" -dt "string";
	addAttr -ci true -sn "gelatoViewer" -ln "gelatoViewer" -dt "string";
	addAttr -ci true -sn "gelatoVerbosity" -ln "gelatoVerbosity" -dv 1 -at "long";
	addAttr -ci true -sn "gelatoUserScript" -ln "gelatoUserScript" -dt "string";
	addAttr -ci true -sn "gelatoBinary" -ln "gelatoBinary" -dv 1 -min 0 -max 1 -at "bool";
	addAttr -ci true -sn "gelatoFullPathNames" -ln "gelatoFullPathNames" -min 0 -max 
		1 -at "bool";
	addAttr -ci true -sn "gelatoNetRender" -ln "gelatoNetRender" -min 0 -max 1 -at "bool";
	addAttr -ci true -sn "gelatoNetRenderList" -ln "gelatoNetRenderList" -dt "string";
	addAttr -ci true -sn "gelatoRenderCmd" -ln "gelatoRenderCmd" -dt "string";
	addAttr -ci true -sn "gelatoProgressive" -ln "gelatoProgressive" -min 0 -max 1 -at "bool";
	addAttr -ci true -sn "gelatoPreview" -ln "gelatoPreview" -min 0 -max 1 -at "bool";
	addAttr -ci true -sn "gelatoPreviewSpatialQuality" -ln "gelatoPreviewSpatialQuality" 
		-dv 0.1 -min 0 -max 1 -smx 1 -at "float";
	addAttr -ci true -sn "gelatoSpatialQuality" -ln "gelatoSpatialQuality" -at "long2" 
		-nc 2;
	addAttr -ci true -sn "gelatoSpatialQualityX" -ln "gelatoSpatialQualityX" -dv 4 -min 
		1 -max 16 -at "long" -p "gelatoSpatialQuality";
	addAttr -ci true -sn "gelatoSpatialQualityY" -ln "gelatoSpatialQualityY" -dv 4 -min 
		1 -max 16 -at "long" -p "gelatoSpatialQuality";
	addAttr -ci true -sn "gelatoDOFQuality" -ln "gelatoDOFQuality" -dv 16 -at "long";
	addAttr -ci true -sn "gelatoPixelFilter" -ln "gelatoPixelFilter" -dt "string";
	addAttr -ci true -sn "gelatoPixelFilterWidth" -ln "gelatoPixelFilterWidth" -at "float2" 
		-nc 2;
	addAttr -ci true -sn "gelatoPixelFilterWidthX" -ln "gelatoPixelFilterWidthX" -dv 
		2 -min 0 -max 64 -at "float" -p "gelatoPixelFilterWidth";
	addAttr -ci true -sn "gelatoPixelFilterWidthY" -ln "gelatoPixelFilterWidthY" -dv 
		2 -min 0 -max 64 -at "float" -p "gelatoPixelFilterWidth";
	addAttr -ci true -sn "gelatoTemporalQuality" -ln "gelatoTemporalQuality" -dv 16 
		-at "long";
	addAttr -ci true -sn "gelatoGain" -ln "gelatoGain" -dv 1 -at "float";
	addAttr -ci true -sn "gelatoDither" -ln "gelatoDither" -dv 0.5 -at "float";
	addAttr -ci true -sn "gelatoBucketSize" -ln "gelatoBucketSize" -at "long2" -nc 2;
	addAttr -ci true -sn "gelatoBucketSizeX" -ln "gelatoBucketSizeX" -dv 32 -at "long" 
		-p "gelatoBucketSize";
	addAttr -ci true -sn "gelatoBucketSizeY" -ln "gelatoBucketSizeY" -dv 32 -at "long" 
		-p "gelatoBucketSize";
	addAttr -ci true -sn "gelatoBucketOrder" -ln "gelatoBucketOrder" -dt "string";
	addAttr -ci true -sn "gelatoGridSize" -ln "gelatoGridSize" -dv 256 -at "long";
	addAttr -ci true -sn "gelatoTextureMemory" -ln "gelatoTextureMemory" -dv 20480 -at "long";
	addAttr -ci true -sn "gelatoTrimCurveQuality" -ln "gelatoTrimCurveQuality" -dv 1 
		-at "float";
	addAttr -ci true -sn "gelatoTrimMemory" -ln "gelatoTrimMemory" -dv 10240 -at "long";
	addAttr -ci true -sn "gelatoThreads" -ln "gelatoThreads" -dv 1 -min 1 -max 100 -smx 
		8 -at "long";
	addAttr -ci true -sn "gelatoImageFormat" -ln "gelatoImageFormat" -min 0 -max 1000 
		-smx 1000 -at "long";
	addAttr -ci true -sn "gelatoUseOverrideSurfaceShader" -ln "gelatoUseOverrideSurfaceShader" 
		-min 0 -max 1 -at "bool";
	addAttr -ci true -sn "gelatoOverrideSurfaceShader" -ln "gelatoOverrideSurfaceShader" 
		-dt "string";
	addAttr -ci true -sn "gelatoOverrideOcclusion" -ln "gelatoOverrideOcclusion" -min 
		0 -max 1 -at "bool";
	addAttr -ci true -sn "gelatoOcclusionDBName" -ln "gelatoOcclusionDBName" -dt "string";
	addAttr -ci true -sn "gelatoOcclusionMinSamples" -ln "gelatoOcclusionMinSamples" 
		-dv 3 -min 1 -max 10 -smx 10 -at "long";
	addAttr -ci true -sn "gelatoOcclusionMaxErr" -ln "gelatoOcclusionMaxErr" -dv 0.25 
		-min 0 -max 1 -smx 1 -at "float";
	addAttr -ci true -sn "gelatoOcclusionMaxPixelDist" -ln "gelatoOcclusionMaxPixelDist" 
		-dv 20 -min 0 -max 1000000 -smx 10000 -at "float";
	addAttr -ci true -sn "gelatoOverrideIndirect" -ln "gelatoOverrideIndirect" -min 
		0 -max 1 -at "bool";
	addAttr -ci true -sn "gelatoIndirectDBName" -ln "gelatoIndirectDBName" -dt "string";
	addAttr -ci true -sn "gelatoIndirectSampleRays" -ln "gelatoIndirectSampleRays" -dv 
		64 -min 16 -max 16384 -smx 1024 -at "long";
	addAttr -ci true -sn "gelatoIndirectMinSamples" -ln "gelatoIndirectMinSamples" -dv 
		3 -min 1 -max 10 -smx 10 -at "long";
	addAttr -ci true -sn "gelatoIndirectMaxErr" -ln "gelatoIndirectMaxErr" -dv 0.25 
		-min 0 -max 1 -smx 1 -at "float";
	addAttr -ci true -sn "gelatoIndirectMaxPixelDist" -ln "gelatoIndirectMaxPixelDist" 
		-dv 20 -min 0 -max 100 -smx 30 -at "float";
	addAttr -ci true -sn "gelatoIndirectMaxHitDist" -ln "gelatoIndirectMaxHitDist" -dv 
		1000000 -min 0 -max 1000000 -smx 10000 -at "float";
	addAttr -ci true -sn "gelatoIndirectBias" -ln "gelatoIndirectBias" -dv 0.01 -min 
		0 -max 100 -smx 10 -at "float";
	addAttr -ci true -sn "gelatoIndirectFalloff" -ln "gelatoIndirectFalloff" -min 0 
		-max 100 -smx 10 -at "float";
	addAttr -ci true -sn "gelatoIndirectFalloffMode" -ln "gelatoIndirectFalloffMode" 
		-min 0 -max 1 -en "Exponential:Polynomial" -at "enum";
	addAttr -ci true -sn "gelatoSubsurfaceScattering" -ln "gelatoSubsurfaceScattering" 
		-dv 1 -min 0 -max 1 -at "bool";
	addAttr -ci true -sn "gelatoSubsurfaceMaterial" -ln "gelatoSubsurfaceMaterial" -dt "string";
	addAttr -ci true -sn "gelatoRerenderDepthMaps" -ln "gelatoRerenderDepthMaps" -dv 
		1 -min 0 -max 1 -at "bool";
	addAttr -ci true -sn "gelatoLiveUpdates" -ln "gelatoLiveUpdates" -min 0 -max 1 -at "bool";
	addAttr -ci true -sn "gelatoShadowMapUpdates" -ln "gelatoShadowMapUpdates" -dv 1 
		-min 0 -max 1 -at "bool";
	addAttr -ci true -sn "gelatoReshadeRays" -ln "gelatoReshadeRays" -min 0 -max 1 -at "bool";
	addAttr -ci true -sn "gelatoOverrideMisc" -ln "gelatoOverrideMisc" -dv 1 -min 0 
		-max 1 -at "bool";
	addAttr -ci true -sn "gelatoShadingQuality" -ln "gelatoShadingQuality" -dv 1 -min 
		0 -max 10 -smx 4 -at "float";
	addAttr -ci true -sn "gelatoMaxRadius" -ln "gelatoMaxRadius" -min 0 -max 1000000 
		-smx 10 -at "float";
	addAttr -ci true -sn "gelatoOpaqueShadows" -ln "gelatoOpaqueShadows" -dv 1 -min 
		0 -max 1 -at "bool";
	addAttr -ci true -sn "gelatoStereo" -ln "gelatoStereo" -min 0 -max 1 -at "bool";
	addAttr -ci true -sn "gelatoStereoSeparation" -ln "gelatoStereoSeparation" -min 
		0 -max 1000 -smx 10 -at "float";
	addAttr -ci true -sn "gelatoStereoConvergence" -ln "gelatoStereoConvergence" -min 
		0 -max 1000000 -smx 1000 -at "float";
	addAttr -ci true -sn "gelatoStereoShade" -ln "gelatoStereoShade" -min 0 -max 2 -en 
		"Center:Left:Right" -at "enum";
	setAttr ".mcfr" 48;
	setAttr ".an" yes;
	setAttr ".fs" 0;
	setAttr ".ef" 114;
	setAttr ".ep" 4;
	setAttr ".pff" yes;
	setAttr ".inputPath" -type "string" "$MANGOHOME/inputs:$GELATOHOME/inputs";
	setAttr ".shaderPath" -type "string" "$MANGOHOME/shaders:$GELATOHOME/shaders";
	setAttr ".texturePath" -type "string" "$MANGOHOME/textures:$GELATOHOME/textures";
	setAttr ".generatorPath" -type "string" "$MANGOHOME/lib:$GELATOHOME/lib";
	setAttr ".imageioPath" -type "string" "$MANGOHOME/lib:$GELATOHOME/lib";
	setAttr ".gelatoTextureLocation" -type "string" "$MAYA_PROJECT/gelatoTextures";
	setAttr ".gelatoViewer" -type "string" "iv";
	setAttr ".gelatoRenderCmd" -type "string" "gelato";
	setAttr ".gelatoPixelFilter" -type "string" "gaussian";
	setAttr ".gelatoBucketOrder" -type "string" "horizontal";
	setAttr ".gelatoOverrideSurfaceShader" -type "string" "";
	setAttr ".gelatoOcclusionDBName" -type "string" "occlusion.sdb";
	setAttr ".gelatoIndirectDBName" -type "string" "indirect.sdb";
	setAttr ".gelatoSubsurfaceMaterial" -type "string" "gelatoSubsurface";
select -ne :defaultRenderQuality;
	setAttr ".rfl" 10;
	setAttr ".rfr" 10;
	setAttr ".sl" 10;
	setAttr ".eaa" 0;
	setAttr ".ufil" yes;
	setAttr ".ss" 2;
	setAttr ".ert" yes;
select -ne :defaultResolution;
	setAttr ".w" 128;
	setAttr ".h" 128;
	setAttr ".pa" 1;
	setAttr ".dar" 1;
select -ne :defaultLightSet;
	setAttr -s 2 ".dsm";
select -ne :hardwareRenderGlobals;
	addAttr -ci true -sn "ani" -ln "animation" -bt "ANIM" -min 0 -max 1 -at "bool";
	setAttr ".enpt" no;
	setAttr ".hgcd" no;
	setAttr ".hgci" no;
	setAttr ".hwfr" 48;
	setAttr -k on ".ani" yes;
select -ne :defaultHardwareRenderGlobals;
	setAttr ".fn" -type "string" "im";
	setAttr ".res" -type "string" "ntsc_4d 646 485 1.333";
connectAttr "NormalRN.phl[7]" "whiteSG.dsm" -na;
connectAttr "NormalRN.phl[8]" "whiteSG.dsm" -na;
connectAttr "NormalRN.phl[9]" "whiteSG.dsm" -na;
connectAttr "NormalRN.phl[10]" "whiteSG.dsm" -na;
connectAttr "NormalRN.phl[11]" "whiteSG.dsm" -na;
connectAttr "NormalRN.phl[12]" "whiteSG.dsm" -na;
connectAttr "NormalRN.phl[13]" "whiteSG.dsm" -na;
connectAttr "NormalRN.phl[14]" "whiteSG.dsm" -na;
connectAttr "NormalRN.phl[15]" "whiteSG.dsm" -na;
connectAttr "NormalRN.phl[16]" "whiteSG.dsm" -na;
connectAttr "NormalRN.phl[17]" "whiteSG.dsm" -na;
connectAttr "NormalRN.phl[18]" "whiteSG.dsm" -na;
connectAttr "NormalRN.phl[19]" "whiteSG.dsm" -na;
connectAttr "NormalRN.phl[20]" "whiteSG.dsm" -na;
connectAttr "NormalRN.phl[21]" "NormalRN.phl[22]";
connectAttr "NormalRN.phl[23]" "NormalRN.phl[24]";
connectAttr "NormalRN.phl[25]" "NormalRN.phl[26]";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[0].llnk";
connectAttr ":initialShadingGroup.msg" "lightLinker1.lnk[0].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[1].llnk";
connectAttr ":initialParticleSE.msg" "lightLinker1.lnk[1].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[2].llnk";
connectAttr "whiteSG.msg" "lightLinker1.lnk[2].olnk";
connectAttr "layerManager.dli[0]" "defaultLayer.id";
connectAttr "renderLayerManager.rlmi[0]" "defaultRenderLayer.rlid";
connectAttr "_UNKNOWN_REF_NODE_.ur" "NormalRN.ur";
connectAttr "white_incandescenceR.o" "white.ir";
connectAttr "white_incandescenceG.o" "white.ig";
connectAttr "white_incandescenceB.o" "white.ib";
connectAttr "NormalRN.phl[6]" "whiteSG.dsm" -na;
connectAttr "white.oc" "whiteSG.ss";
connectAttr "whiteSG.msg" "materialInfo1.sg";
connectAttr "white.msg" "materialInfo1.m";
connectAttr "whiteSG.pa" ":renderPartition.st" -na;
connectAttr "white.msg" ":defaultShaderList1.s" -na;
connectAttr "lightLinker1.msg" ":lightList1.ln" -na;
// End of PrimaryFrozen.ma
