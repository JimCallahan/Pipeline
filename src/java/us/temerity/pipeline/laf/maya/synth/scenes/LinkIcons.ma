//Maya ASCII 7.0 scene
//Name: LinkIcons.ma
//Last modified: Mon, Jun 25, 2007 07:12:48 PM
requires maya "7.0";
currentUnit -l centimeter -a degree -t film;
fileInfo "application" "maya";
fileInfo "product" "Maya Unlimited 7.0";
fileInfo "version" "7.0.1";
fileInfo "cutIdentifier" "200511181718-660870";
fileInfo "osv" "Linux 2.6.16.27-0.9-smp #1 SMP Tue Feb 13 09:35:18 UTC 2007 i686";
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
	setAttr ".t" -type "double3" 0.18758833136609143 100 0.16917134098015088 ;
	setAttr ".r" -type "double3" -89.999999999999986 0 0 ;
createNode camera -s -n "topShape" -p "top";
	setAttr -k off ".v" no;
	setAttr ".rnd" no;
	setAttr ".coi" 100;
	setAttr ".ow" 6.7758849872429474;
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
createNode transform -n "persp1";
	setAttr ".v" no;
	setAttr ".t" -type "double3" -0.1302229917544129 0.46983869349382779 0.40991650671437652 ;
	setAttr ".r" -type "double3" -66.938352727165281 -708.59999999979482 -7.3470095557347142e-16 ;
createNode camera -n "perspShape2" -p "persp1";
	setAttr -k off ".v";
	setAttr ".rnd" no;
	setAttr ".coi" 0.54736915135514375;
	setAttr ".imn" -type "string" "persp1";
	setAttr ".den" -type "string" "persp1_depth";
	setAttr ".man" -type "string" "persp1_mask";
	setAttr ".hc" -type "string" "viewSet -p %camera";
createNode transform -n "group1";
createNode transform -n "nurbsCircle1" -p "group1";
	setAttr ".t" -type "double3" -0.5 0 0 ;
	setAttr ".s" -type "double3" 0.75 0.75 0.75 ;
createNode nurbsCurve -n "nurbsCircleShape1" -p "|group1|nurbsCircle1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode transform -n "detachedCurve1" -p "group1";
	setAttr ".t" -type "double3" 0.5 0 0 ;
	setAttr ".s" -type "double3" 0.75 0.75 0.75 ;
createNode nurbsCurve -n "detachedCurveShape1" -p "|group1|detachedCurve1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode transform -n "curve1" -p "group1";
createNode nurbsCurve -n "curveShape1" -p "|group1|curve1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 0 1
		2
		-0.49999999999999994 4.5922738268339147e-17 -0.74999999999999978
		0.5 4.5922738268339147e-17 -0.74999999999999978
		;
createNode transform -n "curve2" -p "group1";
createNode nurbsCurve -n "curveShape2" -p "|group1|curve2";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 0 1
		2
		-0.50000000000000033 -4.5922738268339147e-17 0.74999999999999978
		0.49999999999999967 -4.5922738268339147e-17 0.74999999999999978
		;
createNode transform -n "group2";
createNode transform -n "nurbsCircle1" -p "group2";
	setAttr ".t" -type "double3" -0.5 0 0 ;
	setAttr ".s" -type "double3" 0.675 0.675 0.675 ;
createNode nurbsCurve -n "nurbsCircleShape1" -p "|group2|nurbsCircle1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode transform -n "detachedCurve1" -p "group2";
	setAttr ".t" -type "double3" 0.5 0 0 ;
	setAttr ".s" -type "double3" 0.675 0.675 0.675 ;
createNode nurbsCurve -n "detachedCurveShape1" -p "|group2|detachedCurve1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode transform -n "curve1" -p "group2";
createNode nurbsCurve -n "curveShape1" -p "|group2|curve1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 0 1
		2
		-0.49999999999999994 4.1330464441505234e-17 -0.67499999999999993
		0.5 4.1330464441505234e-17 -0.67499999999999993
		;
createNode transform -n "curve2" -p "group2";
createNode nurbsCurve -n "curveShape2" -p "|group2|curve2";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 0 1
		2
		-0.50000000000000033 -4.1330464441505234e-17 0.67499999999999993
		0.49999999999999972 -4.1330464441505234e-17 0.67499999999999993
		;
createNode transform -n "nurbsCircle2";
	setAttr ".v" no;
	setAttr ".t" -type "double3" -0.5 0 -0.375 ;
	setAttr ".s" -type "double3" 0.15 0.15 0.15 ;
createNode nurbsCurve -n "nurbsCircleShape2" -p "nurbsCircle2";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode transform -n "nurbsCircle3";
	setAttr ".v" no;
	setAttr ".t" -type "double3" 0.5 0 -0.375 ;
	setAttr ".s" -type "double3" 0.15 0.15 0.15 ;
createNode nurbsCurve -n "nurbsCircleShape3" -p "nurbsCircle3";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode transform -n "nurbsCircle4";
	setAttr ".v" no;
	setAttr ".t" -type "double3" 0.5 0 0.375 ;
	setAttr ".s" -type "double3" 0.15 0.15 0.15 ;
createNode nurbsCurve -n "nurbsCircleShape4" -p "nurbsCircle4";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode transform -n "nurbsCircle5";
	setAttr ".v" no;
	setAttr ".t" -type "double3" -0.5 0 0.375 ;
	setAttr ".s" -type "double3" 0.15 0.15 0.15 ;
createNode nurbsCurve -n "nurbsCircleShape5" -p "nurbsCircle5";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode transform -n "nurbsCircle6";
	setAttr ".v" no;
	setAttr ".t" -type "double3" -0.5 0 0 ;
	setAttr ".s" -type "double3" 0.15 0.15 0.15 ;
createNode nurbsCurve -n "nurbsCircleShape6" -p "nurbsCircle6";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode transform -n "nurbsCircle7";
	setAttr ".v" no;
	setAttr ".t" -type "double3" 0.5 0 0 ;
	setAttr ".s" -type "double3" 0.15 0.15 0.15 ;
createNode nurbsCurve -n "nurbsCircleShape7" -p "nurbsCircle7";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode transform -n "curve3";
	setAttr ".v" no;
createNode nurbsCurve -n "curveShape3" -p "curve3";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 0 1
		2
		-0.4375 0 -0.4375
		0.4375 0 -0.4375
		;
createNode transform -n "curve4";
	setAttr ".v" no;
createNode nurbsCurve -n "curveShape4" -p "curve4";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 0 1
		2
		-0.4375 0 -0.3125
		0.4375 0 -0.3125
		;
createNode transform -n "curve5";
	setAttr ".v" no;
createNode nurbsCurve -n "curveShape5" -p "curve5";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 0 1
		2
		-0.4375 0 -0.0625
		0.4375 0 -0.0625
		;
createNode transform -n "curve6";
	setAttr ".v" no;
createNode nurbsCurve -n "curveShape6" -p "curve6";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 0 1
		2
		-0.4375 0 0.0625
		0.4375 0 0.0625
		;
createNode transform -n "curve7";
	setAttr ".v" no;
createNode nurbsCurve -n "curveShape7" -p "curve7";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 0 1
		2
		-0.4375 0 0.3125
		0.4375 0 0.3125
		;
createNode transform -n "curve8";
	setAttr ".v" no;
createNode nurbsCurve -n "curveShape8" -p "curve8";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 0 1
		2
		-0.4375 0 0.4375
		0.4375 0 0.4375
		;
createNode transform -n "group3";
	setAttr ".v" no;
	setAttr ".r" -type "double3" 0 37.079045163092765 0 ;
createNode transform -n "curve9" -p "group3";
createNode nurbsCurve -n "curveShape9" -p "|group3|curve9";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 0 1
		2
		-0.6875 0 -0.0625
		0.6875 0 -0.0625
		;
createNode transform -n "curve10" -p "group3";
createNode nurbsCurve -n "curveShape10" -p "|group3|curve10";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 0 1
		2
		-0.6875 0 0.0625
		0.6875 0 0.0625
		;
createNode transform -n "group4";
	setAttr ".v" no;
	setAttr ".r" -type "double3" 0 -37.079 0 ;
createNode transform -n "curve9" -p "group4";
createNode nurbsCurve -n "curveShape9" -p "|group4|curve9";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 0 1
		2
		-0.6875 0 -0.0625
		0.6875 0 -0.0625
		;
createNode transform -n "curve10" -p "group4";
createNode nurbsCurve -n "curveShape10" -p "|group4|curve10";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 0 1
		2
		-0.6875 0 0.0625
		0.6875 0 0.0625
		;
createNode transform -n "None";
	setAttr -k off ".tx";
	setAttr -k off ".ty";
	setAttr -k off ".tz";
	setAttr -k off ".rx";
	setAttr -k off ".ry";
	setAttr -k off ".rz";
	setAttr -k off ".sx";
	setAttr -k off ".sy";
	setAttr -k off ".sz";
createNode transform -n "nurbsPlane4" -p "None";
	setAttr ".v" no;
	setAttr ".s" -type "double3" 3 3 3 ;
createNode nurbsSurface -n "nurbsPlaneShape4" -p "nurbsPlane4";
	setAttr -k off ".v";
	setAttr ".io" yes;
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 0;
	setAttr ".dvv" 0;
	setAttr ".cpr" 4;
	setAttr ".cps" 1;
	setAttr ".nufa" 4.5;
	setAttr ".nvfa" 4.5;
createNode curveVarGroup -n "projectionCurve49" -p "nurbsPlaneShape4";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve49_1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve49";
createNode nurbsCurve -n "projectionCurve49_Shape1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve49|projectionCurve49_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve50" -p "nurbsPlaneShape4";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve50_1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve50";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve50_Shape1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve50|projectionCurve50_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve51" -p "nurbsPlaneShape4";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve51_1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve51";
createNode nurbsCurve -n "projectionCurve51_Shape1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve51|projectionCurve51_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve52" -p "nurbsPlaneShape4";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve52_1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve52";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve52_Shape1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve52|projectionCurve52_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve53" -p "nurbsPlaneShape4";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve53_1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve53";
createNode nurbsCurve -n "projectionCurve53_Shape1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve53|projectionCurve53_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve54" -p "nurbsPlaneShape4";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve54_1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve54";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve54_Shape1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve54|projectionCurve54_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve55" -p "nurbsPlaneShape4";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve55_1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve55";
createNode nurbsCurve -n "projectionCurve55_Shape1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve55|projectionCurve55_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve56" -p "nurbsPlaneShape4";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve56_1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve56";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve56_Shape1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve56|projectionCurve56_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve57" -p "nurbsPlaneShape4";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve57_1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve57";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve57_Shape1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve57|projectionCurve57_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve58" -p "nurbsPlaneShape4";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve58_1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve58";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve58_Shape1" -p "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve58|projectionCurve58_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode nurbsSurface -n "nurbsPlane4trimmedSurfaceShape1" -p "nurbsPlane4";
	setAttr -k off ".v";
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 3;
	setAttr ".dvv" 3;
	setAttr ".cpr" 15;
	setAttr ".cps" 4;
	setAttr ".nufa" 5;
	setAttr ".nvfa" 5;
	setAttr ".cvto" 0;
createNode transform -n "nurbsPlane5" -p "None";
	setAttr ".s" -type "double3" 3 3 3 ;
createNode nurbsSurface -n "nurbsPlaneShape5" -p "nurbsPlane5";
	setAttr -k off ".v";
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 3;
	setAttr ".dvv" 3;
	setAttr ".cpr" 15;
	setAttr ".cps" 4;
	setAttr ".nufa" 5;
	setAttr ".nvfa" 5;
	setAttr ".cvto" 0;
createNode curveVarGroup -n "projectionCurve49" -p "nurbsPlaneShape5";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve49_1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve49";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve49_Shape1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve49|projectionCurve49_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve50" -p "nurbsPlaneShape5";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve50_1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve50";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve50_Shape1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve50|projectionCurve50_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve51" -p "nurbsPlaneShape5";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve51_1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve51";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve51_Shape1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve51|projectionCurve51_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve52" -p "nurbsPlaneShape5";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve52_1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve52";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve52_Shape1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve52|projectionCurve52_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve53" -p "nurbsPlaneShape5";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve53_1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve53";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve53_Shape1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve53|projectionCurve53_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve54" -p "nurbsPlaneShape5";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve54_1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve54";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve54_Shape1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve54|projectionCurve54_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve55" -p "nurbsPlaneShape5";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve55_1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve55";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve55_Shape1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve55|projectionCurve55_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve56" -p "nurbsPlaneShape5";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve56_1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve56";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve56_Shape1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve56|projectionCurve56_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve57" -p "nurbsPlaneShape5";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve57_1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve57";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve57_Shape1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve57|projectionCurve57_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve58" -p "nurbsPlaneShape5";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve58_1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve58";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve58_Shape1" -p "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve58|projectionCurve58_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode nurbsSurface -n "nurbsPlaneShape5Original" -p "nurbsPlane5";
	setAttr -k off ".v";
	setAttr ".io" yes;
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 0;
	setAttr ".dvv" 0;
	setAttr ".cpr" 4;
	setAttr ".cps" 1;
createNode transform -n "OneToOne";
	setAttr -k off ".tx";
	setAttr -k off ".ty";
	setAttr -k off ".tz";
	setAttr -k off ".rx";
	setAttr -k off ".ry";
	setAttr -k off ".rz";
	setAttr -k off ".sx";
	setAttr -k off ".sy";
	setAttr -k off ".sz";
createNode transform -n "nurbsPlane3" -p "OneToOne";
	setAttr ".v" no;
	setAttr ".s" -type "double3" 3 3 3 ;
createNode nurbsSurface -n "nurbsPlaneShape3" -p "nurbsPlane3";
	setAttr -k off ".v";
	setAttr ".io" yes;
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 0;
	setAttr ".dvv" 0;
	setAttr ".cpr" 4;
	setAttr ".cps" 1;
	setAttr ".nufa" 4.5;
	setAttr ".nvfa" 4.5;
createNode curveVarGroup -n "projectionCurve37" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve37_1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve37";
createNode nurbsCurve -n "projectionCurve37_Shape1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve37|projectionCurve37_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve38" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve38_1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve38";
createNode nurbsCurve -n "projectionCurve38_Shape1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve38|projectionCurve38_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve39" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve39_1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve39";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve39_Shape1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve39|projectionCurve39_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve40" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve40_1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve40";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve40_Shape1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve40|projectionCurve40_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve41" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve41_1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve41";
createNode nurbsCurve -n "projectionCurve41_Shape1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve41|projectionCurve41_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve42" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve42_1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve42";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve42_Shape1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve42|projectionCurve42_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve43" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve43_1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve43";
createNode nurbsCurve -n "projectionCurve43_Shape1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve43|projectionCurve43_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve44" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve44_1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve44";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve44_Shape1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve44|projectionCurve44_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve45" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve45_1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve45";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve45_Shape1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve45|projectionCurve45_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve46" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve46_1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve46";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve46_Shape1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve46|projectionCurve46_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve47" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve47_1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve47";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve47_Shape1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve47|projectionCurve47_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve48" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve48_1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve48";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve48_Shape1" -p "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve48|projectionCurve48_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode nurbsSurface -n "nurbsPlane3trimmedSurfaceShape1" -p "nurbsPlane3";
	setAttr -k off ".v";
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 3;
	setAttr ".dvv" 3;
	setAttr ".cpr" 15;
	setAttr ".cps" 4;
	setAttr ".nufa" 5;
	setAttr ".nvfa" 5;
	setAttr ".cvto" 0;
createNode transform -n "nurbsPlane6" -p "OneToOne";
	setAttr ".s" -type "double3" 3 3 3 ;
createNode nurbsSurface -n "nurbsPlaneShape6" -p "nurbsPlane6";
	setAttr -k off ".v";
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 3;
	setAttr ".dvv" 3;
	setAttr ".cpr" 15;
	setAttr ".cps" 4;
	setAttr ".nufa" 5;
	setAttr ".nvfa" 5;
	setAttr ".cvto" 0;
createNode curveVarGroup -n "projectionCurve37" -p "nurbsPlaneShape6";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve37_1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve37";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve37_Shape1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve37|projectionCurve37_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve38" -p "nurbsPlaneShape6";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve38_1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve38";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve38_Shape1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve38|projectionCurve38_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve39" -p "nurbsPlaneShape6";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve39_1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve39";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve39_Shape1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve39|projectionCurve39_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve40" -p "nurbsPlaneShape6";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve40_1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve40";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve40_Shape1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve40|projectionCurve40_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve41" -p "nurbsPlaneShape6";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve41_1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve41";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve41_Shape1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve41|projectionCurve41_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve42" -p "nurbsPlaneShape6";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve42_1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve42";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve42_Shape1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve42|projectionCurve42_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve43" -p "nurbsPlaneShape6";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve43_1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve43";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve43_Shape1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve43|projectionCurve43_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve44" -p "nurbsPlaneShape6";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve44_1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve44";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve44_Shape1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve44|projectionCurve44_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve45" -p "nurbsPlaneShape6";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve45_1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve45";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve45_Shape1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve45|projectionCurve45_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve46" -p "nurbsPlaneShape6";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve46_1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve46";
createNode nurbsCurve -n "projectionCurve46_Shape1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve46|projectionCurve46_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve47" -p "nurbsPlaneShape6";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve47_1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve47";
createNode nurbsCurve -n "projectionCurve47_Shape1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve47|projectionCurve47_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve48" -p "nurbsPlaneShape6";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve48_1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve48";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve48_Shape1" -p "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve48|projectionCurve48_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode nurbsSurface -n "nurbsPlaneShape6Original" -p "nurbsPlane6";
	setAttr -k off ".v";
	setAttr ".io" yes;
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".covm[0]"  0 1 1;
	setAttr ".cdvm[0]"  0 1 1;
	setAttr ".dvu" 0;
	setAttr ".dvv" 0;
	setAttr ".cpr" 4;
	setAttr ".cps" 4;
createNode transform -n "nurbsPlane8" -p "OneToOne";
	setAttr ".s" -type "double3" 3 3 3 ;
createNode nurbsSurface -n "nurbsPlaneShape8" -p "nurbsPlane8";
	setAttr -k off ".v";
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 3;
	setAttr ".dvv" 3;
	setAttr ".cpr" 15;
	setAttr ".cps" 4;
	setAttr ".ipo" yes;
	setAttr ".nufa" 5;
	setAttr ".nvfa" 5;
	setAttr ".cvto" 0;
createNode curveVarGroup -n "projectionCurve37" -p "nurbsPlaneShape8";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve37_1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve37";
createNode nurbsCurve -n "projectionCurve37_Shape1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve37|projectionCurve37_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 0 no 2
		8 3 3 3 4.4999999999999991 4.4999999999999991 6 6 6
		6
		0.66666666666666663 0.25000000000000006
		0.80455390399351534 0.24999999999999994
		0.91564897025469283 0.36248722143338713
		0.91564897025469283 0.63751277856661304
		0.80455390399351534 0.74999999999999989
		0.66666666666666663 0.75
		;
createNode curveVarGroup -n "projectionCurve38" -p "nurbsPlaneShape8";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve38_1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve38";
createNode nurbsCurve -n "projectionCurve38_Shape1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve38|projectionCurve38_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.33333333333333331 0.25
		0.66666666666666663 0.25000000000000006
		;
createNode curveVarGroup -n "projectionCurve39" -p "nurbsPlaneShape8";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve39_1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve39";
createNode nurbsCurve -n "projectionCurve39_Shape1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve39|projectionCurve39_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 0 no 2
		8 3 3 3 4.4999999999999991 4.4999999999999991 6 6 6
		6
		0.66666666666666663 0.27500000000000002
		0.79076518026083042 0.27499999999999997
		0.89075073989589026 0.37623849929004849
		0.89075073989589026 0.62376150070995162
		0.79076518026083042 0.72499999999999987
		0.66666666666666663 0.72499999999999998
		;
createNode curveVarGroup -n "projectionCurve40" -p "nurbsPlaneShape8";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve40_1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve40";
createNode nurbsCurve -n "projectionCurve40_Shape1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve40|projectionCurve40_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.33333333333333331 0.27500000000000002
		0.66666666666666663 0.27500000000000002
		;
createNode curveVarGroup -n "projectionCurve41" -p "nurbsPlaneShape8";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve41_1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve41";
createNode nurbsCurve -n "projectionCurve41_Shape1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve41|projectionCurve41_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.33333333333333331 0.75
		0.66666666666666663 0.75
		;
createNode curveVarGroup -n "projectionCurve42" -p "nurbsPlaneShape8";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve42_1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve42";
createNode nurbsCurve -n "projectionCurve42_Shape1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve42|projectionCurve42_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.33333333333333331 0.72499999999999998
		0.66666666666666663 0.72499999999999998
		;
createNode curveVarGroup -n "projectionCurve43" -p "nurbsPlaneShape8";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve43_1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve43";
createNode nurbsCurve -n "projectionCurve43_Shape1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve43|projectionCurve43_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 0 no 2
		8 0 0 0 1.4999999999999976 1.4999999999999976 3 3 3
		6
		0.33333333333333331 0.75
		0.19544609600648491 0.75000000000000011
		0.084351029745307171 0.63751277856661259
		0.084351029745307171 0.36248722143338696
		0.19544609600648449 0.25000000000000006
		0.33333333333333331 0.25
		;
createNode curveVarGroup -n "projectionCurve44" -p "nurbsPlaneShape8";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve44_1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve44";
createNode nurbsCurve -n "projectionCurve44_Shape1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve44|projectionCurve44_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 0 no 2
		8 0 0 0 1.4999999999999993 1.4999999999999993 3 3 3
		6
		0.33333333333333331 0.72499999999999998
		0.20923481973916955 0.72499999999999998
		0.10924926010410985 0.62376150070995151
		0.10924926010410985 0.37623849929004838
		0.20923481973916949 0.27500000000000013
		0.33333333333333331 0.27500000000000002
		;
createNode curveVarGroup -n "projectionCurve45" -p "nurbsPlaneShape8";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve45_1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve45";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve45_Shape1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve45|projectionCurve45_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 1 no 2
		8 0 0 0 3 3 6 6 6
		6
		0.33333333333333331 0.55000000000000004
		0.2669380523765264 0.55000000000000004
		0.2669380523765264 0.45000000000000007
		0.39972861429014023 0.44999999999999996
		0.39972861429014023 0.55000000000000004
		0.33333333333333331 0.55000000000000004
		;
createNode curveVarGroup -n "projectionCurve46" -p "nurbsPlaneShape8";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve46_1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve46";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve46_Shape1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve46|projectionCurve46_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.35416666666666663 0.52083333333333337
		0.64583333333333337 0.52083333333333337
		;
createNode curveVarGroup -n "projectionCurve47" -p "nurbsPlaneShape8";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve47_1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve47";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve47_Shape1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve47|projectionCurve47_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.35416666666666663 0.47916666666666663
		0.64583333333333337 0.47916666666666669
		;
createNode curveVarGroup -n "projectionCurve48" -p "nurbsPlaneShape8";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve48_1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve48";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve48_Shape1" -p "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve48|projectionCurve48_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 1 no 2
		8 0 0 0 2.9999999999999925 2.9999999999999925 6 6 6
		6
		0.66666666666666663 0.55000000000000004
		0.60027138570985983 0.55000000000000004
		0.60027138570985983 0.45000000000000007
		0.73306194762347376 0.44999999999999996
		0.73306194762347376 0.55000000000000004
		0.66666666666666663 0.55000000000000004
		;
createNode nurbsSurface -n "nurbsPlaneShape8Original" -p "nurbsPlane8";
	setAttr -k off ".v";
	setAttr ".io" yes;
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".covm[0]"  0 1 1;
	setAttr ".cdvm[0]"  0 1 1;
	setAttr ".dvu" 0;
	setAttr ".dvv" 0;
	setAttr ".cpr" 4;
	setAttr ".cps" 4;
	setAttr ".cc" -type "nurbsSurface" 
		3 3 0 0 no 
		11 0 0 0 0.16666666666666666 0.33333333333333331 0.5 0.66666666666666663 0.83333333333333326
		 1 1 1
		11 0 0 0 0.16666666666666666 0.33333333333333331 0.5 0.66666666666666663 0.83333333333333326
		 1 1 1
		
		81
		-0.49999999999999989 -3.0615158845559431e-17 0.49999999999999994
		-0.49999999999999989 -2.7213474529386164e-17 0.44444444444444442
		-0.49999999999999989 -2.0410105897039624e-17 0.33333333333333331
		-0.49999999999999989 -1.0205052948519812e-17 0.16666666666666666
		-0.49999999999999989 -1.0576126549149591e-33 1.7272695860409689e-17
		-0.49999999999999989 1.0205052948519809e-17 -0.1666666666666666
		-0.49999999999999989 2.0410105897039621e-17 -0.33333333333333326
		-0.49999999999999989 2.7213474529386164e-17 -0.44444444444444442
		-0.49999999999999989 3.0615158845559431e-17 -0.49999999999999994
		-0.44444444444444436 -3.0615158845559431e-17 0.49999999999999994
		-0.44444444444444436 -2.7213474529386164e-17 0.44444444444444442
		-0.44444444444444436 -2.0410105897039624e-17 0.33333333333333331
		-0.44444444444444436 -1.0205052948519812e-17 0.16666666666666666
		-0.44444444444444436 -1.0576126549149591e-33 1.7272695860409689e-17
		-0.44444444444444436 1.0205052948519809e-17 -0.1666666666666666
		-0.44444444444444436 2.0410105897039621e-17 -0.33333333333333326
		-0.44444444444444436 2.7213474529386164e-17 -0.44444444444444442
		-0.44444444444444436 3.0615158845559431e-17 -0.49999999999999994
		-0.33333333333333331 -3.0615158845559431e-17 0.49999999999999994
		-0.33333333333333331 -2.7213474529386164e-17 0.44444444444444442
		-0.33333333333333331 -2.0410105897039624e-17 0.33333333333333331
		-0.33333333333333331 -1.0205052948519812e-17 0.16666666666666666
		-0.33333333333333331 -1.0576126549149591e-33 1.7272695860409689e-17
		-0.33333333333333331 1.0205052948519809e-17 -0.1666666666666666
		-0.33333333333333331 2.0410105897039621e-17 -0.33333333333333326
		-0.33333333333333331 2.7213474529386164e-17 -0.44444444444444442
		-0.33333333333333331 3.0615158845559431e-17 -0.49999999999999994
		-0.16666666666666666 -3.0615158845559431e-17 0.49999999999999994
		-0.16666666666666666 -2.7213474529386164e-17 0.44444444444444442
		-0.16666666666666666 -2.0410105897039624e-17 0.33333333333333331
		-0.16666666666666666 -1.0205052948519812e-17 0.16666666666666666
		-0.16666666666666666 -1.0576126549149591e-33 1.7272695860409689e-17
		-0.16666666666666666 1.0205052948519809e-17 -0.1666666666666666
		-0.16666666666666666 2.0410105897039621e-17 -0.33333333333333326
		-0.16666666666666666 2.7213474529386164e-17 -0.44444444444444442
		-0.16666666666666666 3.0615158845559431e-17 -0.49999999999999994
		-1.7272695860409689e-17 -3.0615158845559431e-17 0.49999999999999994
		-1.7272695860409689e-17 -2.7213474529386164e-17 0.44444444444444442
		-1.7272695860409689e-17 -2.0410105897039624e-17 0.33333333333333331
		-1.7272695860409689e-17 -1.0205052948519812e-17 0.16666666666666666
		-1.7272695860409689e-17 -1.0576126549149591e-33 1.7272695860409689e-17
		-1.7272695860409689e-17 1.0205052948519809e-17 -0.1666666666666666
		-1.7272695860409689e-17 2.0410105897039621e-17 -0.33333333333333326
		-1.7272695860409689e-17 2.7213474529386164e-17 -0.44444444444444442
		-1.7272695860409689e-17 3.0615158845559431e-17 -0.49999999999999994
		0.1666666666666666 -3.0615158845559431e-17 0.49999999999999994
		0.1666666666666666 -2.7213474529386164e-17 0.44444444444444442
		0.1666666666666666 -2.0410105897039624e-17 0.33333333333333331
		0.1666666666666666 -1.0205052948519812e-17 0.16666666666666666
		0.1666666666666666 -1.0576126549149591e-33 1.7272695860409689e-17
		0.1666666666666666 1.0205052948519809e-17 -0.1666666666666666
		0.1666666666666666 2.0410105897039621e-17 -0.33333333333333326
		0.1666666666666666 2.7213474529386164e-17 -0.44444444444444442
		0.1666666666666666 3.0615158845559431e-17 -0.49999999999999994
		0.33333333333333326 -3.0615158845559431e-17 0.49999999999999994
		0.33333333333333326 -2.7213474529386164e-17 0.44444444444444442
		0.33333333333333326 -2.0410105897039624e-17 0.33333333333333331
		0.33333333333333326 -1.0205052948519812e-17 0.16666666666666666
		0.33333333333333326 -1.0576126549149591e-33 1.7272695860409689e-17
		0.33333333333333326 1.0205052948519809e-17 -0.1666666666666666
		0.33333333333333326 2.0410105897039621e-17 -0.33333333333333326
		0.33333333333333326 2.7213474529386164e-17 -0.44444444444444442
		0.33333333333333326 3.0615158845559431e-17 -0.49999999999999994
		0.44444444444444436 -3.0615158845559431e-17 0.49999999999999994
		0.44444444444444436 -2.7213474529386164e-17 0.44444444444444442
		0.44444444444444436 -2.0410105897039624e-17 0.33333333333333331
		0.44444444444444436 -1.0205052948519812e-17 0.16666666666666666
		0.44444444444444436 -1.0576126549149591e-33 1.7272695860409689e-17
		0.44444444444444436 1.0205052948519809e-17 -0.1666666666666666
		0.44444444444444436 2.0410105897039621e-17 -0.33333333333333326
		0.44444444444444436 2.7213474529386164e-17 -0.44444444444444442
		0.44444444444444436 3.0615158845559431e-17 -0.49999999999999994
		0.49999999999999989 -3.0615158845559431e-17 0.49999999999999994
		0.49999999999999989 -2.7213474529386164e-17 0.44444444444444442
		0.49999999999999989 -2.0410105897039624e-17 0.33333333333333331
		0.49999999999999989 -1.0205052948519812e-17 0.16666666666666666
		0.49999999999999989 -1.0576126549149591e-33 1.7272695860409689e-17
		0.49999999999999989 1.0205052948519809e-17 -0.1666666666666666
		0.49999999999999989 2.0410105897039621e-17 -0.33333333333333326
		0.49999999999999989 2.7213474529386164e-17 -0.44444444444444442
		0.49999999999999989 3.0615158845559431e-17 -0.49999999999999994
		
		;
createNode transform -n "All";
	setAttr -k off ".tx";
	setAttr -k off ".ty";
	setAttr -k off ".tz";
	setAttr -k off ".rx";
	setAttr -k off ".ry";
	setAttr -k off ".rz";
	setAttr -k off ".sx";
	setAttr -k off ".sy";
	setAttr -k off ".sz";
createNode transform -n "nurbsPlane1" -p "All";
	setAttr ".v" no;
	setAttr ".s" -type "double3" 3 3 3 ;
createNode nurbsSurface -n "nurbsPlaneShape1" -p "nurbsPlane1";
	setAttr -k off ".v";
	setAttr ".io" yes;
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 3;
	setAttr ".dvv" 3;
	setAttr ".cpr" 15;
	setAttr ".cps" 4;
	setAttr ".nufa" 4.5;
	setAttr ".nvfa" 4.5;
createNode curveVarGroup -n "projectionCurve1" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve1_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve1";
createNode nurbsCurve -n "projectionCurve1_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve1|projectionCurve1_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve2" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve2_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve2";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve2_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve2|projectionCurve2_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve3" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve3_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve3";
createNode nurbsCurve -n "projectionCurve3_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve3|projectionCurve3_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve4" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve4_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve4";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve4_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve4|projectionCurve4_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve5" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve5_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve5";
createNode nurbsCurve -n "projectionCurve5_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve5|projectionCurve5_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve6" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve6_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve6";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve6_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve6|projectionCurve6_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve7" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve7_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve7";
createNode nurbsCurve -n "projectionCurve7_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve7|projectionCurve7_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve8" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve8_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve8";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve8_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve8|projectionCurve8_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve9" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve9_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve9";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve9_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve9|projectionCurve9_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve10" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve10_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve10";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve10_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve10|projectionCurve10_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve11" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve11_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve11";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve11_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve11|projectionCurve11_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve12" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve12_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve12";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve12_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve12|projectionCurve12_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve13" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve13_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve13";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve13_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve13|projectionCurve13_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve14" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve14_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve14";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve14_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve14|projectionCurve14_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve15" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve15_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve15";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve15_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve15|projectionCurve15_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve16" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve16_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve16";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve16_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve16|projectionCurve16_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve17" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve17_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve17";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve17_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve17|projectionCurve17_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve18" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve18_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve18";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve18_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve18|projectionCurve18_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve19" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve19_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve19";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve19_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve19|projectionCurve19_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve20" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve20_1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve20";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve20_Shape1" -p "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve20|projectionCurve20_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode nurbsSurface -n "nurbsPlane1trimmedSurfaceShape1" -p "nurbsPlane1";
	setAttr -k off ".v";
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 3;
	setAttr ".dvv" 3;
	setAttr ".cpr" 15;
	setAttr ".cps" 4;
	setAttr ".nufa" 5;
	setAttr ".nvfa" 5;
	setAttr ".cvto" 0;
createNode transform -n "nurbsPlane7" -p "All";
	setAttr ".s" -type "double3" 3 3 3 ;
createNode nurbsSurface -n "nurbsPlaneShape7" -p "nurbsPlane7";
	setAttr -k off ".v";
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 3;
	setAttr ".dvv" 3;
	setAttr ".cpr" 15;
	setAttr ".cps" 4;
	setAttr ".nufa" 5;
	setAttr ".nvfa" 5;
	setAttr ".cvto" 0;
createNode curveVarGroup -n "projectionCurve1" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve1_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve1";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve1_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve1|projectionCurve1_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve2" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve2_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve2";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve2_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve2|projectionCurve2_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve3" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve3_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve3";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve3_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve3|projectionCurve3_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve4" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve4_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve4";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve4_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve4|projectionCurve4_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve5" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve5_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve5";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve5_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve5|projectionCurve5_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve6" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve6_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve6";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve6_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve6|projectionCurve6_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve7" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve7_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve7";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve7_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve7|projectionCurve7_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve8" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve8_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve8";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve8_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve8|projectionCurve8_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve9" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve9_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve9";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve9_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve9|projectionCurve9_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve10" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve10_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve10";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve10_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve10|projectionCurve10_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve11" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve11_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve11";
createNode nurbsCurve -n "projectionCurve11_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve11|projectionCurve11_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve12" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve12_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve12";
createNode nurbsCurve -n "projectionCurve12_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve12|projectionCurve12_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve13" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve13_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve13";
createNode nurbsCurve -n "projectionCurve13_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve13|projectionCurve13_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve14" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve14_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve14";
createNode nurbsCurve -n "projectionCurve14_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve14|projectionCurve14_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve15" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve15_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve15";
createNode nurbsCurve -n "projectionCurve15_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve15|projectionCurve15_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve16" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve16_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve16";
createNode nurbsCurve -n "projectionCurve16_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve16|projectionCurve16_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve17" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve17_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve17";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve17_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve17|projectionCurve17_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve18" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve18_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve18";
createNode nurbsCurve -n "projectionCurve18_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve18|projectionCurve18_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve19" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve19_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve19";
createNode nurbsCurve -n "projectionCurve19_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve19|projectionCurve19_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve20" -p "nurbsPlaneShape7";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve20_1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve20";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve20_Shape1" -p "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve20|projectionCurve20_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode nurbsSurface -n "nurbsPlaneShape7Original" -p "nurbsPlane7";
	setAttr -k off ".v";
	setAttr ".io" yes;
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".covm[0]"  0 1 1;
	setAttr ".cdvm[0]"  0 1 1;
	setAttr ".dvu" 0;
	setAttr ".dvv" 0;
	setAttr ".cpr" 4;
	setAttr ".cps" 4;
createNode transform -n "nurbsPlane9" -p "All";
	setAttr ".s" -type "double3" 3 3 3 ;
createNode nurbsSurface -n "nurbsPlaneShape9" -p "nurbsPlane9";
	setAttr -k off ".v";
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 3;
	setAttr ".dvv" 3;
	setAttr ".cpr" 15;
	setAttr ".cps" 4;
	setAttr ".ipo" yes;
	setAttr ".nufa" 5;
	setAttr ".nvfa" 5;
	setAttr ".cvto" 0;
createNode curveVarGroup -n "projectionCurve1" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve1_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve1";
createNode nurbsCurve -n "projectionCurve1_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve1|projectionCurve1_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 0 no 2
		8 3 3 3 4.4999999999999991 4.4999999999999991 6 6 6
		6
		0.66666666666666663 0.25000000000000006
		0.80455390399351534 0.24999999999999994
		0.91564897025469283 0.36248722143338713
		0.91564897025469283 0.63751277856661304
		0.80455390399351534 0.74999999999999989
		0.66666666666666663 0.75
		;
createNode curveVarGroup -n "projectionCurve2" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve2_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve2";
createNode nurbsCurve -n "projectionCurve2_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve2|projectionCurve2_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 0 no 2
		8 3 3 3 4.4999999999999991 4.4999999999999991 6 6 6
		6
		0.66666666666666663 0.27500000000000002
		0.79076518026083042 0.27499999999999997
		0.89075073989589026 0.37623849929004849
		0.89075073989589026 0.62376150070995162
		0.79076518026083042 0.72499999999999987
		0.66666666666666663 0.72499999999999998
		;
createNode curveVarGroup -n "projectionCurve3" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve3_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve3";
createNode nurbsCurve -n "projectionCurve3_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve3|projectionCurve3_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.33333333333333331 0.75
		0.66666666666666663 0.75
		;
createNode curveVarGroup -n "projectionCurve4" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve4_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve4";
createNode nurbsCurve -n "projectionCurve4_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve4|projectionCurve4_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.33333333333333331 0.72499999999999998
		0.66666666666666663 0.72499999999999998
		;
createNode curveVarGroup -n "projectionCurve5" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve5_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve5";
createNode nurbsCurve -n "projectionCurve5_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve5|projectionCurve5_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 0 no 2
		8 0 0 0 1.4999999999999976 1.4999999999999976 3 3 3
		6
		0.33333333333333331 0.75
		0.19544609600648491 0.75000000000000011
		0.084351029745307171 0.63751277856661259
		0.084351029745307171 0.36248722143338696
		0.19544609600648449 0.25000000000000006
		0.33333333333333331 0.25
		;
createNode curveVarGroup -n "projectionCurve6" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve6_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve6";
createNode nurbsCurve -n "projectionCurve6_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve6|projectionCurve6_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 0 no 2
		8 0 0 0 1.4999999999999993 1.4999999999999993 3 3 3
		6
		0.33333333333333331 0.72499999999999998
		0.20923481973916955 0.72499999999999998
		0.10924926010410985 0.62376150070995151
		0.10924926010410985 0.37623849929004838
		0.20923481973916949 0.27500000000000013
		0.33333333333333331 0.27500000000000002
		;
createNode curveVarGroup -n "projectionCurve7" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve7_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve7";
createNode nurbsCurve -n "projectionCurve7_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve7|projectionCurve7_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.33333333333333331 0.25
		0.66666666666666663 0.25000000000000006
		;
createNode curveVarGroup -n "projectionCurve8" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve8_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve8";
createNode nurbsCurve -n "projectionCurve8_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve8|projectionCurve8_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.33333333333333331 0.27500000000000002
		0.66666666666666663 0.27500000000000002
		;
createNode curveVarGroup -n "projectionCurve9" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve9_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve9";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve9_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve9|projectionCurve9_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 1 no 2
		8 0 0 0 3 3 6 6 6
		6
		0.33333333333333331 0.42499999999999999
		0.26693805237652635 0.42500000000000004
		0.26693805237652635 0.32500000000000023
		0.39972861429014028 0.3249999999999999
		0.39972861429014028 0.42499999999999999
		0.33333333333333331 0.42499999999999999
		;
createNode curveVarGroup -n "projectionCurve10" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve10_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve10";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve10_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve10|projectionCurve10_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 1 no 2
		8 0 0 0 2.9999999999999925 2.9999999999999925 6 6 6
		6
		0.66666666666666663 0.42499999999999999
		0.60027138570985983 0.42500000000000004
		0.60027138570985983 0.32500000000000023
		0.73306194762347376 0.3249999999999999
		0.73306194762347376 0.42499999999999999
		0.66666666666666663 0.42499999999999999
		;
createNode curveVarGroup -n "projectionCurve11" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve11_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve11";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve11_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve11|projectionCurve11_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.35416666666666663 0.35416666666666663
		0.64583333333333337 0.35416666666666663
		;
createNode curveVarGroup -n "projectionCurve12" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve12_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve12";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve12_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve12|projectionCurve12_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.35416666666666663 0.39583333333333331
		0.64583333333333337 0.39583333333333331
		;
createNode curveVarGroup -n "projectionCurve13" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve13_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve13";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve13_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve13|projectionCurve13_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.30460895405817945 0.62154722561478015
		0.67026956188109266 0.34521090094677315
		;
createNode curveVarGroup -n "projectionCurve14" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve14_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve14";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve14_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve14|projectionCurve14_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.3297304381189074 0.65478909905322691
		0.69539104594182055 0.37845277438521979
		;
createNode curveVarGroup -n "projectionCurve15" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve15_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve15";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve15_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve15|projectionCurve15_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.32973056013064245 0.34521076673274675
		0.69539095013280716 0.62154737963069273
		;
createNode curveVarGroup -n "projectionCurve16" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve16_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve16";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve16_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve16|projectionCurve16_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.30460904986719284 0.37845262036930721
		0.67026943986935761 0.65478923326725325
		;
createNode curveVarGroup -n "projectionCurve17" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve17_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve17";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve17_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve17|projectionCurve17_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 1 no 2
		8 0 0 0 3 3 6 6 6
		6
		0.33333333333333331 0.67500000000000004
		0.26693805237652635 0.67500000000000004
		0.26693805237652635 0.57500000000000007
		0.39972861429014028 0.57500000000000007
		0.39972861429014028 0.67500000000000004
		0.33333333333333331 0.67500000000000004
		;
createNode curveVarGroup -n "projectionCurve18" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve18_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve18";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve18_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve18|projectionCurve18_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.35416666666666663 0.60416666666666674
		0.64583333333333337 0.60416666666666674
		;
createNode curveVarGroup -n "projectionCurve19" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve19_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve19";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve19_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve19|projectionCurve19_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.35416666666666663 0.64583333333333337
		0.64583333333333337 0.64583333333333337
		;
createNode curveVarGroup -n "projectionCurve20" -p "nurbsPlaneShape9";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve20_1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve20";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve20_Shape1" -p "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve20|projectionCurve20_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 1 no 2
		8 0 0 0 2.9999999999999925 2.9999999999999925 6 6 6
		6
		0.66666666666666663 0.67500000000000004
		0.60027138570985983 0.67500000000000004
		0.60027138570985983 0.57500000000000007
		0.73306194762347376 0.57500000000000007
		0.73306194762347376 0.67500000000000004
		0.66666666666666663 0.67500000000000004
		;
createNode nurbsSurface -n "nurbsPlaneShape9Original" -p "nurbsPlane9";
	setAttr -k off ".v";
	setAttr ".io" yes;
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".covm[0]"  0 1 1;
	setAttr ".cdvm[0]"  0 1 1;
	setAttr ".dvu" 0;
	setAttr ".dvv" 0;
	setAttr ".cpr" 4;
	setAttr ".cps" 4;
	setAttr ".cc" -type "nurbsSurface" 
		3 3 0 0 no 
		11 0 0 0 0.16666666666666666 0.33333333333333331 0.5 0.66666666666666663 0.83333333333333326
		 1 1 1
		11 0 0 0 0.16666666666666666 0.33333333333333331 0.5 0.66666666666666663 0.83333333333333326
		 1 1 1
		
		81
		-0.49999999999999989 -3.0615158845559431e-17 0.49999999999999994
		-0.49999999999999989 -2.7213474529386164e-17 0.44444444444444442
		-0.49999999999999989 -2.0410105897039624e-17 0.33333333333333331
		-0.49999999999999989 -1.0205052948519812e-17 0.16666666666666666
		-0.49999999999999989 -1.0576126549149591e-33 1.7272695860409689e-17
		-0.49999999999999989 1.0205052948519809e-17 -0.1666666666666666
		-0.49999999999999989 2.0410105897039621e-17 -0.33333333333333326
		-0.49999999999999989 2.7213474529386164e-17 -0.44444444444444442
		-0.49999999999999989 3.0615158845559431e-17 -0.49999999999999994
		-0.44444444444444436 -3.0615158845559431e-17 0.49999999999999994
		-0.44444444444444436 -2.7213474529386164e-17 0.44444444444444442
		-0.44444444444444436 -2.0410105897039624e-17 0.33333333333333331
		-0.44444444444444436 -1.0205052948519812e-17 0.16666666666666666
		-0.44444444444444436 -1.0576126549149591e-33 1.7272695860409689e-17
		-0.44444444444444436 1.0205052948519809e-17 -0.1666666666666666
		-0.44444444444444436 2.0410105897039621e-17 -0.33333333333333326
		-0.44444444444444436 2.7213474529386164e-17 -0.44444444444444442
		-0.44444444444444436 3.0615158845559431e-17 -0.49999999999999994
		-0.33333333333333331 -3.0615158845559431e-17 0.49999999999999994
		-0.33333333333333331 -2.7213474529386164e-17 0.44444444444444442
		-0.33333333333333331 -2.0410105897039624e-17 0.33333333333333331
		-0.33333333333333331 -1.0205052948519812e-17 0.16666666666666666
		-0.33333333333333331 -1.0576126549149591e-33 1.7272695860409689e-17
		-0.33333333333333331 1.0205052948519809e-17 -0.1666666666666666
		-0.33333333333333331 2.0410105897039621e-17 -0.33333333333333326
		-0.33333333333333331 2.7213474529386164e-17 -0.44444444444444442
		-0.33333333333333331 3.0615158845559431e-17 -0.49999999999999994
		-0.16666666666666666 -3.0615158845559431e-17 0.49999999999999994
		-0.16666666666666666 -2.7213474529386164e-17 0.44444444444444442
		-0.16666666666666666 -2.0410105897039624e-17 0.33333333333333331
		-0.16666666666666666 -1.0205052948519812e-17 0.16666666666666666
		-0.16666666666666666 -1.0576126549149591e-33 1.7272695860409689e-17
		-0.16666666666666666 1.0205052948519809e-17 -0.1666666666666666
		-0.16666666666666666 2.0410105897039621e-17 -0.33333333333333326
		-0.16666666666666666 2.7213474529386164e-17 -0.44444444444444442
		-0.16666666666666666 3.0615158845559431e-17 -0.49999999999999994
		-1.7272695860409689e-17 -3.0615158845559431e-17 0.49999999999999994
		-1.7272695860409689e-17 -2.7213474529386164e-17 0.44444444444444442
		-1.7272695860409689e-17 -2.0410105897039624e-17 0.33333333333333331
		-1.7272695860409689e-17 -1.0205052948519812e-17 0.16666666666666666
		-1.7272695860409689e-17 -1.0576126549149591e-33 1.7272695860409689e-17
		-1.7272695860409689e-17 1.0205052948519809e-17 -0.1666666666666666
		-1.7272695860409689e-17 2.0410105897039621e-17 -0.33333333333333326
		-1.7272695860409689e-17 2.7213474529386164e-17 -0.44444444444444442
		-1.7272695860409689e-17 3.0615158845559431e-17 -0.49999999999999994
		0.1666666666666666 -3.0615158845559431e-17 0.49999999999999994
		0.1666666666666666 -2.7213474529386164e-17 0.44444444444444442
		0.1666666666666666 -2.0410105897039624e-17 0.33333333333333331
		0.1666666666666666 -1.0205052948519812e-17 0.16666666666666666
		0.1666666666666666 -1.0576126549149591e-33 1.7272695860409689e-17
		0.1666666666666666 1.0205052948519809e-17 -0.1666666666666666
		0.1666666666666666 2.0410105897039621e-17 -0.33333333333333326
		0.1666666666666666 2.7213474529386164e-17 -0.44444444444444442
		0.1666666666666666 3.0615158845559431e-17 -0.49999999999999994
		0.33333333333333326 -3.0615158845559431e-17 0.49999999999999994
		0.33333333333333326 -2.7213474529386164e-17 0.44444444444444442
		0.33333333333333326 -2.0410105897039624e-17 0.33333333333333331
		0.33333333333333326 -1.0205052948519812e-17 0.16666666666666666
		0.33333333333333326 -1.0576126549149591e-33 1.7272695860409689e-17
		0.33333333333333326 1.0205052948519809e-17 -0.1666666666666666
		0.33333333333333326 2.0410105897039621e-17 -0.33333333333333326
		0.33333333333333326 2.7213474529386164e-17 -0.44444444444444442
		0.33333333333333326 3.0615158845559431e-17 -0.49999999999999994
		0.44444444444444436 -3.0615158845559431e-17 0.49999999999999994
		0.44444444444444436 -2.7213474529386164e-17 0.44444444444444442
		0.44444444444444436 -2.0410105897039624e-17 0.33333333333333331
		0.44444444444444436 -1.0205052948519812e-17 0.16666666666666666
		0.44444444444444436 -1.0576126549149591e-33 1.7272695860409689e-17
		0.44444444444444436 1.0205052948519809e-17 -0.1666666666666666
		0.44444444444444436 2.0410105897039621e-17 -0.33333333333333326
		0.44444444444444436 2.7213474529386164e-17 -0.44444444444444442
		0.44444444444444436 3.0615158845559431e-17 -0.49999999999999994
		0.49999999999999989 -3.0615158845559431e-17 0.49999999999999994
		0.49999999999999989 -2.7213474529386164e-17 0.44444444444444442
		0.49999999999999989 -2.0410105897039624e-17 0.33333333333333331
		0.49999999999999989 -1.0205052948519812e-17 0.16666666666666666
		0.49999999999999989 -1.0576126549149591e-33 1.7272695860409689e-17
		0.49999999999999989 1.0205052948519809e-17 -0.1666666666666666
		0.49999999999999989 2.0410105897039621e-17 -0.33333333333333326
		0.49999999999999989 2.7213474529386164e-17 -0.44444444444444442
		0.49999999999999989 3.0615158845559431e-17 -0.49999999999999994
		
		;
createNode transform -n "BG";
	setAttr -k off ".tx";
	setAttr -k off ".ty";
	setAttr -k off ".tz";
	setAttr -k off ".rx";
	setAttr -k off ".ry";
	setAttr -k off ".rz";
	setAttr ".s" -type "double3" 3 3 3 ;
	setAttr -k off ".sx";
	setAttr -k off ".sy";
	setAttr -k off ".sz";
createNode nurbsSurface -n "BGShape" -p "BG";
	setAttr -k off ".v";
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".dvu" 3;
	setAttr ".dvv" 3;
	setAttr ".cpr" 15;
	setAttr ".cps" 4;
	setAttr ".cc" -type "nurbsSurface" 
		3 3 0 0 no 
		11 0 0 0 0.16666666666666666 0.33333333333333331 0.5 0.66666666666666663 0.83333333333333326
		 1 1 1
		11 0 0 0 0.16666666666666666 0.33333333333333331 0.5 0.66666666666666663 0.83333333333333326
		 1 1 1
		
		81
		-0.49999999999999989 -3.0615158845559431e-17 0.49999999999999994
		-0.49999999999999989 -2.7213474529386164e-17 0.44444444444444442
		-0.49999999999999989 -2.0410105897039624e-17 0.33333333333333331
		-0.49999999999999989 -1.0205052948519812e-17 0.16666666666666666
		-0.49999999999999989 -1.0576126549149591e-33 1.7272695860409689e-17
		-0.49999999999999989 1.0205052948519809e-17 -0.1666666666666666
		-0.49999999999999989 2.0410105897039621e-17 -0.33333333333333326
		-0.49999999999999989 2.7213474529386164e-17 -0.44444444444444442
		-0.49999999999999989 3.0615158845559431e-17 -0.49999999999999994
		-0.44444444444444436 -3.0615158845559431e-17 0.49999999999999994
		-0.44444444444444436 -2.7213474529386164e-17 0.44444444444444442
		-0.44444444444444436 -2.0410105897039624e-17 0.33333333333333331
		-0.44444444444444436 -1.0205052948519812e-17 0.16666666666666666
		-0.44444444444444436 -1.0576126549149591e-33 1.7272695860409689e-17
		-0.44444444444444436 1.0205052948519809e-17 -0.1666666666666666
		-0.44444444444444436 2.0410105897039621e-17 -0.33333333333333326
		-0.44444444444444436 2.7213474529386164e-17 -0.44444444444444442
		-0.44444444444444436 3.0615158845559431e-17 -0.49999999999999994
		-0.33333333333333331 -3.0615158845559431e-17 0.49999999999999994
		-0.33333333333333331 -2.7213474529386164e-17 0.44444444444444442
		-0.33333333333333331 -2.0410105897039624e-17 0.33333333333333331
		-0.33333333333333331 -1.0205052948519812e-17 0.16666666666666666
		-0.33333333333333331 -1.0576126549149591e-33 1.7272695860409689e-17
		-0.33333333333333331 1.0205052948519809e-17 -0.1666666666666666
		-0.33333333333333331 2.0410105897039621e-17 -0.33333333333333326
		-0.33333333333333331 2.7213474529386164e-17 -0.44444444444444442
		-0.33333333333333331 3.0615158845559431e-17 -0.49999999999999994
		-0.16666666666666666 -3.0615158845559431e-17 0.49999999999999994
		-0.16666666666666666 -2.7213474529386164e-17 0.44444444444444442
		-0.16666666666666666 -2.0410105897039624e-17 0.33333333333333331
		-0.16666666666666666 -1.0205052948519812e-17 0.16666666666666666
		-0.16666666666666666 -1.0576126549149591e-33 1.7272695860409689e-17
		-0.16666666666666666 1.0205052948519809e-17 -0.1666666666666666
		-0.16666666666666666 2.0410105897039621e-17 -0.33333333333333326
		-0.16666666666666666 2.7213474529386164e-17 -0.44444444444444442
		-0.16666666666666666 3.0615158845559431e-17 -0.49999999999999994
		-1.7272695860409689e-17 -3.0615158845559431e-17 0.49999999999999994
		-1.7272695860409689e-17 -2.7213474529386164e-17 0.44444444444444442
		-1.7272695860409689e-17 -2.0410105897039624e-17 0.33333333333333331
		-1.7272695860409689e-17 -1.0205052948519812e-17 0.16666666666666666
		-1.7272695860409689e-17 -1.0576126549149591e-33 1.7272695860409689e-17
		-1.7272695860409689e-17 1.0205052948519809e-17 -0.1666666666666666
		-1.7272695860409689e-17 2.0410105897039621e-17 -0.33333333333333326
		-1.7272695860409689e-17 2.7213474529386164e-17 -0.44444444444444442
		-1.7272695860409689e-17 3.0615158845559431e-17 -0.49999999999999994
		0.1666666666666666 -3.0615158845559431e-17 0.49999999999999994
		0.1666666666666666 -2.7213474529386164e-17 0.44444444444444442
		0.1666666666666666 -2.0410105897039624e-17 0.33333333333333331
		0.1666666666666666 -1.0205052948519812e-17 0.16666666666666666
		0.1666666666666666 -1.0576126549149591e-33 1.7272695860409689e-17
		0.1666666666666666 1.0205052948519809e-17 -0.1666666666666666
		0.1666666666666666 2.0410105897039621e-17 -0.33333333333333326
		0.1666666666666666 2.7213474529386164e-17 -0.44444444444444442
		0.1666666666666666 3.0615158845559431e-17 -0.49999999999999994
		0.33333333333333326 -3.0615158845559431e-17 0.49999999999999994
		0.33333333333333326 -2.7213474529386164e-17 0.44444444444444442
		0.33333333333333326 -2.0410105897039624e-17 0.33333333333333331
		0.33333333333333326 -1.0205052948519812e-17 0.16666666666666666
		0.33333333333333326 -1.0576126549149591e-33 1.7272695860409689e-17
		0.33333333333333326 1.0205052948519809e-17 -0.1666666666666666
		0.33333333333333326 2.0410105897039621e-17 -0.33333333333333326
		0.33333333333333326 2.7213474529386164e-17 -0.44444444444444442
		0.33333333333333326 3.0615158845559431e-17 -0.49999999999999994
		0.44444444444444436 -3.0615158845559431e-17 0.49999999999999994
		0.44444444444444436 -2.7213474529386164e-17 0.44444444444444442
		0.44444444444444436 -2.0410105897039624e-17 0.33333333333333331
		0.44444444444444436 -1.0205052948519812e-17 0.16666666666666666
		0.44444444444444436 -1.0576126549149591e-33 1.7272695860409689e-17
		0.44444444444444436 1.0205052948519809e-17 -0.1666666666666666
		0.44444444444444436 2.0410105897039621e-17 -0.33333333333333326
		0.44444444444444436 2.7213474529386164e-17 -0.44444444444444442
		0.44444444444444436 3.0615158845559431e-17 -0.49999999999999994
		0.49999999999999989 -3.0615158845559431e-17 0.49999999999999994
		0.49999999999999989 -2.7213474529386164e-17 0.44444444444444442
		0.49999999999999989 -2.0410105897039624e-17 0.33333333333333331
		0.49999999999999989 -1.0205052948519812e-17 0.16666666666666666
		0.49999999999999989 -1.0576126549149591e-33 1.7272695860409689e-17
		0.49999999999999989 1.0205052948519809e-17 -0.1666666666666666
		0.49999999999999989 2.0410105897039621e-17 -0.33333333333333326
		0.49999999999999989 2.7213474529386164e-17 -0.44444444444444442
		0.49999999999999989 3.0615158845559431e-17 -0.49999999999999994
		
		;
	setAttr -s 4 ".tf";
	setAttr ".tf[0]" -type "nurbsTrimface" no 2
		0 4
		0 0 0 0 
		1
		
		3 3 0 no 3
		8 0 0 0 1.5000000000000002 1.5000000000000002 3 3 3
		6
		-0.16666666666666666 1.5307579422779719e-17 -0.24999999999999997
		-0.30455421618786338 1.5307579422779719e-17 -0.24999999999999997
		-0.41564897025469311 8.419970182068811e-18 -0.13751308991313763
		-0.41564897025469244 -8.4199701820688973e-18 0.13751308991313904
		-0.30455421618786332 -1.5307579422779719e-17 0.25
		-0.16666666666666666 -1.5307579422779719e-17 0.25
		
		0.001 0 -1
		2
		
		3 1 0 no 2
		6 0 0 0 1.4999999999999976 1.4999999999999976 1.4999999999999976
		4
		0.33333333333333331 0.75
		0.19544609600648491 0.75
		0.084351029745307171 0.63751277856661259
		0.084351029745307171 0.49999999999999994
		
		3 1 0 no 2
		6 1.4999999999999976 1.4999999999999976 1.4999999999999976 3 3 3
		4
		0.084351029745307171 0.49999999999999994
		0.084351029745307171 0.36248722143338696
		0.19544609600648449 0.25
		0.33333333333333331 0.25
		
		1 0.001
		
		1
		
		1 1 0 no 3
		2 0 1
		2
		-0.16666666666666666 -1.5307579422779719e-17 0.25
		0.1666666666666666 -1.5307579422779713e-17 0.24999999999999994
		
		0.001 0 -1
		1
		
		1 1 0 no 2
		2 0 1
		2
		0.33333333333333331 0.25
		0.66666666666666663 0.25000000000000006
		
		1 0.001
		
		1
		
		3 3 0 no 3
		8 3 3 3 4.5000000000000018 4.5000000000000018 6 6 6
		6
		0.1666666666666666 -1.5307579422779713e-17 0.24999999999999994
		0.30455421618786355 -1.5307579422779713e-17 0.24999999999999994
		0.41564897025469294 -8.4199701820688511e-18 0.13751308991313826
		0.4156489702546925 8.4199701820688711e-18 -0.1375130899131386
		0.30455421618786327 1.5307579422779719e-17 -0.24999999999999997
		0.1666666666666666 1.5307579422779719e-17 -0.24999999999999997
		
		0.001 0 -1
		2
		
		3 1 0 no 2
		6 3 3 3 4.4999999999999991 4.4999999999999991 4.4999999999999991
		4
		0.66666666666666663 0.25000000000000006
		0.80455390399351534 0.25000000000000006
		0.91564897025469283 0.36248722143338713
		0.91564897025469283 0.5
		
		3 1 0 no 2
		6 4.4999999999999991 4.4999999999999991 4.4999999999999991 6 6 6
		4
		0.91564897025469283 0.5
		0.91564897025469283 0.63751277856661304
		0.80455390399351534 0.75
		0.66666666666666663 0.75
		
		1 0.001
		
		1
		
		1 1 0 no 3
		2 0 1
		2
		-0.16666666666666666 1.5307579422779719e-17 -0.24999999999999997
		0.1666666666666666 1.5307579422779719e-17 -0.24999999999999997
		
		0.001 1 -1
		1
		
		1 1 0 no 2
		2 -1 0
		2
		0.66666666666666663 0.75
		0.33333333333333331 0.75
		
		1 0.001
		1 4
		0 0 0 0 
		1
		
		3 3 0 no 3
		8 3 3 3 4.5000000000000009 4.5000000000000009 6 6 6
		6
		0.1666666666666666 -1.3776821480501744e-17 0.22499999999999995
		0.29076546123574365 -1.3776821480501744e-17 0.22499999999999995
		0.39075073989589043 -7.5779731638619547e-18 0.12376178092182427
		0.39075073989588999 7.5779731638619886e-18 -0.12376178092182483
		0.29076546123574359 1.3776821480501745e-17 -0.22499999999999992
		0.1666666666666666 1.3776821480501745e-17 -0.22499999999999992
		
		0.001 1 -1
		2
		
		3 1 0 no 2
		6 -6 -6 -6 -4.4999999999999991 -4.4999999999999991 -4.4999999999999991
		4
		0.66666666666666663 0.72499999999999998
		0.79076518026083042 0.72499999999999998
		0.89075073989589026 0.62376150070995162
		0.89075073989589026 0.5
		
		3 1 0 no 2
		6 -4.4999999999999991 -4.4999999999999991 -4.4999999999999991 -3 -3 -3
		4
		0.89075073989589026 0.5
		0.89075073989589026 0.37623849929004849
		0.79076518026083042 0.27500000000000002
		0.66666666666666663 0.27500000000000002
		
		1 0.001
		
		1
		
		1 1 0 no 3
		2 0 1
		2
		-0.16666666666666666 -1.3776821480501744e-17 0.22499999999999995
		0.1666666666666666 -1.3776821480501744e-17 0.22499999999999995
		
		0.001 1 -1
		1
		
		1 1 0 no 2
		2 -1 0
		2
		0.66666666666666663 0.27500000000000002
		0.33333333333333331 0.27500000000000002
		
		1 0.001
		
		1
		
		3 3 0 no 3
		8 0 0 0 1.5 1.5 3 3 3
		6
		-0.16666666666666666 1.3776821480501745e-17 -0.22499999999999992
		-0.29076546123574376 1.3776821480501745e-17 -0.22499999999999992
		-0.39075073989589021 7.5779731638619624e-18 -0.12376178092182437
		-0.39075073989588999 -7.5779731638619824e-18 0.12376178092182469
		-0.29076546123574371 -1.3776821480501744e-17 0.22499999999999995
		-0.16666666666666666 -1.3776821480501744e-17 0.22499999999999995
		
		0.001 1 -1
		2
		
		3 1 0 no 2
		6 -3 -3 -3 -1.4999999999999993 -1.4999999999999993 -1.4999999999999993
		4
		0.33333333333333331 0.27500000000000002
		0.20923481973916949 0.27500000000000002
		0.10924926010410985 0.37623849929004838
		0.10924926010410985 0.5
		
		3 1 0 no 2
		6 -1.4999999999999993 -1.4999999999999993 -1.4999999999999993 0 0 0
		4
		0.10924926010410985 0.5
		0.10924926010410985 0.62376150070995151
		0.20923481973916955 0.72499999999999998
		0.33333333333333331 0.72499999999999998
		
		1 0.001
		
		1
		
		1 1 0 no 3
		2 0 1
		2
		-0.16666666666666666 1.3776821480501745e-17 -0.22499999999999992
		0.1666666666666666 1.3776821480501745e-17 -0.22499999999999992
		
		0.001 0 -1
		1
		
		1 1 0 no 2
		2 0 1
		2
		0.33333333333333331 0.72499999999999998
		0.66666666666666663 0.72499999999999998
		
		1 0.001
		;
	setAttr ".tf[1]" -type "nurbsTrimface" no 3
		0 4
		0 0 0 0 
		1
		
		3 3 0 no 3
		8 0 0 0 1.5000000000000002 1.5000000000000002 3 3 3
		6
		-0.16666666666666666 1.3776821480501745e-17 -0.22499999999999992
		-0.29076546123574376 1.3776821480501745e-17 -0.22499999999999992
		-0.39075073989589021 7.577973163861964e-18 -0.12376178092182438
		-0.39075073989588999 -7.5779731638619824e-18 0.12376178092182469
		-0.29076546123574371 -1.3776821480501744e-17 0.22499999999999995
		-0.16666666666666666 -1.3776821480501744e-17 0.22499999999999995
		
		0.001 0 -1
		2
		
		3 1 0 no 2
		6 0 0 0 1.4999999999999993 1.4999999999999993 1.4999999999999993
		4
		0.33333333333333331 0.72499999999999998
		0.20923481973916955 0.72499999999999998
		0.10924926010410985 0.62376150070995151
		0.10924926010410985 0.5
		
		3 1 0 no 2
		6 1.4999999999999993 1.4999999999999993 1.4999999999999993 3 3 3
		4
		0.10924926010410985 0.5
		0.10924926010410985 0.37623849929004838
		0.20923481973916949 0.27500000000000002
		0.33333333333333331 0.27500000000000002
		
		1 0.001
		
		1
		
		1 1 0 no 3
		2 0 1
		2
		-0.16666666666666666 -1.3776821480501744e-17 0.22499999999999995
		0.1666666666666666 -1.3776821480501744e-17 0.22499999999999995
		
		0.001 0 -1
		1
		
		1 1 0 no 2
		2 0 1
		2
		0.33333333333333331 0.27500000000000002
		0.66666666666666663 0.27500000000000002
		
		1 0.001
		
		1
		
		3 3 0 no 3
		8 3 3 3 4.5000000000000009 4.5000000000000009 6 6 6
		6
		0.1666666666666666 -1.3776821480501744e-17 0.22499999999999995
		0.29076546123574371 -1.3776821480501744e-17 0.22499999999999995
		0.39075073989589043 -7.5779731638619547e-18 0.12376178092182429
		0.39075073989588999 7.5779731638619855e-18 -0.12376178092182477
		0.29076546123574354 1.3776821480501745e-17 -0.22499999999999992
		0.1666666666666666 1.3776821480501745e-17 -0.22499999999999992
		
		0.001 0 -1
		2
		
		3 1 0 no 2
		6 3 3 3 4.4999999999999991 4.4999999999999991 4.4999999999999991
		4
		0.66666666666666663 0.27500000000000002
		0.79076518026083042 0.27500000000000002
		0.89075073989589026 0.37623849929004849
		0.89075073989589026 0.5
		
		3 1 0 no 2
		6 4.4999999999999991 4.4999999999999991 4.4999999999999991 6 6 6
		4
		0.89075073989589026 0.5
		0.89075073989589026 0.62376150070995162
		0.79076518026083042 0.72499999999999998
		0.66666666666666663 0.72499999999999998
		
		1 0.001
		
		1
		
		1 1 0 no 3
		2 0 1
		2
		-0.16666666666666666 1.3776821480501745e-17 -0.22499999999999992
		0.1666666666666666 1.3776821480501745e-17 -0.22499999999999992
		
		0.001 1 -1
		1
		
		1 1 0 no 2
		2 -1 0
		2
		0.66666666666666663 0.72499999999999998
		0.33333333333333331 0.72499999999999998
		
		1 0.001
		1 1
		0 
		1
		
		3 3 1 no 3
		8 0 0 0 3.0000000000000009 3.0000000000000009 6 6 6
		6
		-0.16666666666666666 3.0615158845559459e-18 -0.050000000000000037
		-0.23306194762347357 3.0615158845559459e-18 -0.050000000000000037
		-0.23306194762347357 -3.0615158845559425e-18 0.049999999999999982
		-0.10027138570985979 -3.0615158845559425e-18 0.049999999999999982
		-0.10027138570985979 3.0615158845559459e-18 -0.050000000000000037
		-0.16666666666666666 3.0615158845559459e-18 -0.050000000000000037
		
		0.001 1 -1
		4
		
		3 1 0 no 2
		6 -6 -6 -6 -4.5 -4.5 -4.5
		4
		0.33333333333333331 0.55000000000000004
		0.36653097381173677 0.55000000000000004
		0.3831297940509385 0.52500000000000002
		0.3831297940509385 0.5
		
		3 1 0 no 2
		6 -4.5 -4.5 -4.5 -3 -3 -3
		4
		0.3831297940509385 0.5
		0.3831297940509385 0.47499999999999998
		0.36653097381173677 0.45000000000000001
		0.33333333333333331 0.45000000000000001
		
		3 1 0 no 2
		6 -3 -3 -3 -1.5 -1.5 -1.5
		4
		0.33333333333333331 0.45000000000000001
		0.30013569285492986 0.45000000000000001
		0.28353687261572813 0.47500000000000003
		0.28353687261572813 0.5
		
		3 1 0 no 2
		6 -1.5 -1.5 -1.5 0 0 0
		4
		0.28353687261572813 0.5
		0.28353687261572813 0.52500000000000002
		0.30013569285492986 0.55000000000000004
		0.33333333333333331 0.55000000000000004
		
		1 0.001
		1 1
		0 
		1
		
		3 3 1 no 3
		8 0 0 0 3.0000000000000089 3.0000000000000089 6 6 6
		6
		0.1666666666666666 3.0615158845559459e-18 -0.050000000000000037
		0.10027138570985951 3.0615158845559459e-18 -0.050000000000000037
		0.10027138570986018 -3.0615158845559698e-18 0.050000000000000426
		0.23306194762347396 -3.0615158845559151e-18 0.049999999999999538
		0.23306194762347329 3.0615158845559459e-18 -0.050000000000000037
		0.1666666666666666 3.0615158845559459e-18 -0.050000000000000037
		
		0.001 1 -1
		4
		
		3 1 0 no 2
		6 -6 -6 -6 -4.4999999999999964 -4.4999999999999964 -4.4999999999999964
		4
		0.66666666666666663 0.55000000000000004
		0.6998643071450702 0.55000000000000004
		0.71646312738427198 0.52500000000000013
		0.71646312738427198 0.50000000000000011
		
		3 1 0 no 2
		6 -4.4999999999999964 -4.4999999999999964 -4.4999999999999964 -2.9999999999999925
		 -2.9999999999999925 -2.9999999999999925
		4
		0.71646312738427198 0.50000000000000011
		0.71646312738427198 0.47500000000000009
		0.6998643071450702 0.45000000000000001
		0.66666666666666663 0.45000000000000001
		
		3 1 0 no 2
		6 -2.9999999999999925 -2.9999999999999925 -2.9999999999999925 -1.4999999999999962
		 -1.4999999999999962 -1.4999999999999962
		4
		0.66666666666666663 0.45000000000000001
		0.63346902618826328 0.45000000000000001
		0.61687020594906161 0.47500000000000003
		0.61687020594906161 0.5
		
		3 1 0 no 2
		6 -1.4999999999999962 -1.4999999999999962 -1.4999999999999962 0 0 0
		4
		0.61687020594906161 0.5
		0.61687020594906161 0.52500000000000002
		0.63346902618826317 0.55000000000000004
		0.66666666666666663 0.55000000000000004
		
		1 0.001
		;
	setAttr ".tf[2]" -type "nurbsTrimface" no 1
		0 1
		0 
		1
		
		3 3 1 no 3
		8 0 0 0 3.0000000000000009 3.0000000000000009 6 6 6
		6
		-0.16666666666666666 3.0615158845559459e-18 -0.050000000000000037
		-0.23306194762347357 3.0615158845559459e-18 -0.050000000000000037
		-0.23306194762347357 -3.0615158845559425e-18 0.049999999999999982
		-0.10027138570985979 -3.0615158845559425e-18 0.049999999999999982
		-0.10027138570985979 3.0615158845559459e-18 -0.050000000000000037
		-0.16666666666666666 3.0615158845559459e-18 -0.050000000000000037
		
		0.001 0 -1
		4
		
		3 1 0 no 2
		6 0 0 0 1.5 1.5 1.5
		4
		0.33333333333333331 0.55000000000000004
		0.30013569285492986 0.55000000000000004
		0.28353687261572813 0.52500000000000002
		0.28353687261572813 0.5
		
		3 1 0 no 2
		6 1.5 1.5 1.5 3 3 3
		4
		0.28353687261572813 0.5
		0.28353687261572813 0.47500000000000003
		0.30013569285492986 0.45000000000000001
		0.33333333333333331 0.45000000000000001
		
		3 1 0 no 2
		6 3 3 3 4.5 4.5 4.5
		4
		0.33333333333333331 0.45000000000000001
		0.36653097381173677 0.45000000000000001
		0.3831297940509385 0.47499999999999998
		0.3831297940509385 0.5
		
		3 1 0 no 2
		6 4.5 4.5 4.5 6 6 6
		4
		0.3831297940509385 0.5
		0.3831297940509385 0.52500000000000002
		0.36653097381173677 0.55000000000000004
		0.33333333333333331 0.55000000000000004
		
		1 0.001
		;
	setAttr ".tf[3]" -type "nurbsTrimface" no 1
		0 1
		0 
		1
		
		3 3 1 no 3
		8 0 0 0 3.0000000000000089 3.0000000000000089 6 6 6
		6
		0.1666666666666666 3.0615158845559459e-18 -0.050000000000000037
		0.10027138570985951 3.0615158845559459e-18 -0.050000000000000037
		0.10027138570986018 -3.0615158845559698e-18 0.050000000000000426
		0.23306194762347396 -3.0615158845559151e-18 0.049999999999999538
		0.23306194762347329 3.0615158845559459e-18 -0.050000000000000037
		0.1666666666666666 3.0615158845559459e-18 -0.050000000000000037
		
		0.001 0 -1
		4
		
		3 1 0 no 2
		6 0 0 0 1.4999999999999962 1.4999999999999962 1.4999999999999962
		4
		0.66666666666666663 0.55000000000000004
		0.63346902618826317 0.55000000000000004
		0.61687020594906161 0.52500000000000002
		0.61687020594906161 0.5
		
		3 1 0 no 2
		6 1.4999999999999962 1.4999999999999962 1.4999999999999962 2.9999999999999925 2.9999999999999925
		 2.9999999999999925
		4
		0.61687020594906161 0.5
		0.61687020594906161 0.47500000000000003
		0.63346902618826328 0.45000000000000001
		0.66666666666666663 0.45000000000000001
		
		3 1 0 no 2
		6 2.9999999999999925 2.9999999999999925 2.9999999999999925 4.4999999999999964 4.4999999999999964
		 4.4999999999999964
		4
		0.66666666666666663 0.45000000000000001
		0.6998643071450702 0.45000000000000001
		0.71646312738427198 0.47500000000000009
		0.71646312738427198 0.50000000000000011
		
		3 1 0 no 2
		6 4.4999999999999964 4.4999999999999964 4.4999999999999964 6 6 6
		4
		0.71646312738427198 0.50000000000000011
		0.71646312738427198 0.52500000000000013
		0.6998643071450702 0.55000000000000004
		0.66666666666666663 0.55000000000000004
		
		1 0.001
		;
	setAttr ".nufa" 5;
	setAttr ".nvfa" 5;
	setAttr ".cvto" 0;
createNode curveVarGroup -n "projectionCurve49" -p "BGShape";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve49_1" -p "|BG|BGShape->|projectionCurve49";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve49_Shape1" -p "|BG|BGShape->|projectionCurve49|projectionCurve49_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 0 no 2
		8 3 3 3 4.4999999999999991 4.4999999999999991 6 6 6
		6
		0.66666666666666663 0.25000000000000006
		0.80455390399351534 0.24999999999999994
		0.91564897025469283 0.36248722143338713
		0.91564897025469283 0.63751277856661304
		0.80455390399351534 0.74999999999999989
		0.66666666666666663 0.75
		;
createNode curveVarGroup -n "projectionCurve50" -p "BGShape";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve50_1" -p "|BG|BGShape->|projectionCurve50";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve50_Shape1" -p "|BG|BGShape->|projectionCurve50|projectionCurve50_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 0 no 2
		8 3 3 3 4.4999999999999991 4.4999999999999991 6 6 6
		6
		0.66666666666666663 0.27500000000000002
		0.79076518026083042 0.27499999999999997
		0.89075073989589026 0.37623849929004849
		0.89075073989589026 0.62376150070995162
		0.79076518026083042 0.72499999999999987
		0.66666666666666663 0.72499999999999998
		;
createNode curveVarGroup -n "projectionCurve51" -p "BGShape";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve51_1" -p "|BG|BGShape->|projectionCurve51";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve51_Shape1" -p "|BG|BGShape->|projectionCurve51|projectionCurve51_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.33333333333333331 0.25
		0.66666666666666663 0.25000000000000006
		;
createNode curveVarGroup -n "projectionCurve52" -p "BGShape";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve52_1" -p "|BG|BGShape->|projectionCurve52";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve52_Shape1" -p "|BG|BGShape->|projectionCurve52|projectionCurve52_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.33333333333333331 0.27500000000000002
		0.66666666666666663 0.27500000000000002
		;
createNode curveVarGroup -n "projectionCurve53" -p "BGShape";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve53_1" -p "|BG|BGShape->|projectionCurve53";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve53_Shape1" -p "|BG|BGShape->|projectionCurve53|projectionCurve53_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 0 no 2
		8 0 0 0 1.4999999999999976 1.4999999999999976 3 3 3
		6
		0.33333333333333331 0.75
		0.19544609600648491 0.75000000000000011
		0.084351029745307171 0.63751277856661259
		0.084351029745307171 0.36248722143338696
		0.19544609600648449 0.25000000000000006
		0.33333333333333331 0.25
		;
createNode curveVarGroup -n "projectionCurve54" -p "BGShape";
	setAttr ".mc" 1;
	setAttr ".ds" 7;
createNode transform -n "projectionCurve54_1" -p "|BG|BGShape->|projectionCurve54";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve54_Shape1" -p "|BG|BGShape->|projectionCurve54|projectionCurve54_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 0 no 2
		8 0 0 0 1.4999999999999993 1.4999999999999993 3 3 3
		6
		0.33333333333333331 0.72499999999999998
		0.20923481973916955 0.72499999999999998
		0.10924926010410985 0.62376150070995151
		0.10924926010410985 0.37623849929004838
		0.20923481973916949 0.27500000000000013
		0.33333333333333331 0.27500000000000002
		;
createNode curveVarGroup -n "projectionCurve55" -p "BGShape";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve55_1" -p "|BG|BGShape->|projectionCurve55";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve55_Shape1" -p "|BG|BGShape->|projectionCurve55|projectionCurve55_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.33333333333333331 0.75
		0.66666666666666663 0.75
		;
createNode curveVarGroup -n "projectionCurve56" -p "BGShape";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve56_1" -p "|BG|BGShape->|projectionCurve56";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve56_Shape1" -p "|BG|BGShape->|projectionCurve56|projectionCurve56_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 2
		2 0 1
		2
		0.33333333333333331 0.72499999999999998
		0.66666666666666663 0.72499999999999998
		;
createNode curveVarGroup -n "projectionCurve57" -p "BGShape";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve57_1" -p "|BG|BGShape->|projectionCurve57";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve57_Shape1" -p "|BG|BGShape->|projectionCurve57|projectionCurve57_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 1 no 2
		8 0 0 0 3 3 6 6 6
		6
		0.33333333333333331 0.55000000000000004
		0.2669380523765264 0.55000000000000004
		0.2669380523765264 0.45000000000000007
		0.39972861429014023 0.44999999999999996
		0.39972861429014023 0.55000000000000004
		0.33333333333333331 0.55000000000000004
		;
createNode curveVarGroup -n "projectionCurve58" -p "BGShape";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve58_1" -p "|BG|BGShape->|projectionCurve58";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve58_Shape1" -p "|BG|BGShape->|projectionCurve58|projectionCurve58_1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 3 1 no 2
		8 0 0 0 2.9999999999999925 2.9999999999999925 6 6 6
		6
		0.66666666666666663 0.55000000000000004
		0.60027138570985983 0.55000000000000004
		0.60027138570985983 0.45000000000000007
		0.73306194762347376 0.44999999999999996
		0.73306194762347376 0.55000000000000004
		0.66666666666666663 0.55000000000000004
		;
createNode nurbsSurface -n "BGShape5Original" -p "BG";
	setAttr -k off ".v";
	setAttr ".io" yes;
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".dvu" 0;
	setAttr ".dvv" 0;
	setAttr ".cpr" 4;
	setAttr ".cps" 1;
	setAttr ".cc" -type "nurbsSurface" 
		3 3 0 0 no 
		11 0 0 0 0.16666666666666666 0.33333333333333331 0.5 0.66666666666666663 0.83333333333333326
		 1 1 1
		11 0 0 0 0.16666666666666666 0.33333333333333331 0.5 0.66666666666666663 0.83333333333333326
		 1 1 1
		
		81
		-0.49999999999999989 -3.0615158845559431e-17 0.49999999999999994
		-0.49999999999999989 -2.7213474529386164e-17 0.44444444444444442
		-0.49999999999999989 -2.0410105897039624e-17 0.33333333333333331
		-0.49999999999999989 -1.0205052948519812e-17 0.16666666666666666
		-0.49999999999999989 -1.0576126549149591e-33 1.7272695860409689e-17
		-0.49999999999999989 1.0205052948519809e-17 -0.1666666666666666
		-0.49999999999999989 2.0410105897039621e-17 -0.33333333333333326
		-0.49999999999999989 2.7213474529386164e-17 -0.44444444444444442
		-0.49999999999999989 3.0615158845559431e-17 -0.49999999999999994
		-0.44444444444444436 -3.0615158845559431e-17 0.49999999999999994
		-0.44444444444444436 -2.7213474529386164e-17 0.44444444444444442
		-0.44444444444444436 -2.0410105897039624e-17 0.33333333333333331
		-0.44444444444444436 -1.0205052948519812e-17 0.16666666666666666
		-0.44444444444444436 -1.0576126549149591e-33 1.7272695860409689e-17
		-0.44444444444444436 1.0205052948519809e-17 -0.1666666666666666
		-0.44444444444444436 2.0410105897039621e-17 -0.33333333333333326
		-0.44444444444444436 2.7213474529386164e-17 -0.44444444444444442
		-0.44444444444444436 3.0615158845559431e-17 -0.49999999999999994
		-0.33333333333333331 -3.0615158845559431e-17 0.49999999999999994
		-0.33333333333333331 -2.7213474529386164e-17 0.44444444444444442
		-0.33333333333333331 -2.0410105897039624e-17 0.33333333333333331
		-0.33333333333333331 -1.0205052948519812e-17 0.16666666666666666
		-0.33333333333333331 -1.0576126549149591e-33 1.7272695860409689e-17
		-0.33333333333333331 1.0205052948519809e-17 -0.1666666666666666
		-0.33333333333333331 2.0410105897039621e-17 -0.33333333333333326
		-0.33333333333333331 2.7213474529386164e-17 -0.44444444444444442
		-0.33333333333333331 3.0615158845559431e-17 -0.49999999999999994
		-0.16666666666666666 -3.0615158845559431e-17 0.49999999999999994
		-0.16666666666666666 -2.7213474529386164e-17 0.44444444444444442
		-0.16666666666666666 -2.0410105897039624e-17 0.33333333333333331
		-0.16666666666666666 -1.0205052948519812e-17 0.16666666666666666
		-0.16666666666666666 -1.0576126549149591e-33 1.7272695860409689e-17
		-0.16666666666666666 1.0205052948519809e-17 -0.1666666666666666
		-0.16666666666666666 2.0410105897039621e-17 -0.33333333333333326
		-0.16666666666666666 2.7213474529386164e-17 -0.44444444444444442
		-0.16666666666666666 3.0615158845559431e-17 -0.49999999999999994
		-1.7272695860409689e-17 -3.0615158845559431e-17 0.49999999999999994
		-1.7272695860409689e-17 -2.7213474529386164e-17 0.44444444444444442
		-1.7272695860409689e-17 -2.0410105897039624e-17 0.33333333333333331
		-1.7272695860409689e-17 -1.0205052948519812e-17 0.16666666666666666
		-1.7272695860409689e-17 -1.0576126549149591e-33 1.7272695860409689e-17
		-1.7272695860409689e-17 1.0205052948519809e-17 -0.1666666666666666
		-1.7272695860409689e-17 2.0410105897039621e-17 -0.33333333333333326
		-1.7272695860409689e-17 2.7213474529386164e-17 -0.44444444444444442
		-1.7272695860409689e-17 3.0615158845559431e-17 -0.49999999999999994
		0.1666666666666666 -3.0615158845559431e-17 0.49999999999999994
		0.1666666666666666 -2.7213474529386164e-17 0.44444444444444442
		0.1666666666666666 -2.0410105897039624e-17 0.33333333333333331
		0.1666666666666666 -1.0205052948519812e-17 0.16666666666666666
		0.1666666666666666 -1.0576126549149591e-33 1.7272695860409689e-17
		0.1666666666666666 1.0205052948519809e-17 -0.1666666666666666
		0.1666666666666666 2.0410105897039621e-17 -0.33333333333333326
		0.1666666666666666 2.7213474529386164e-17 -0.44444444444444442
		0.1666666666666666 3.0615158845559431e-17 -0.49999999999999994
		0.33333333333333326 -3.0615158845559431e-17 0.49999999999999994
		0.33333333333333326 -2.7213474529386164e-17 0.44444444444444442
		0.33333333333333326 -2.0410105897039624e-17 0.33333333333333331
		0.33333333333333326 -1.0205052948519812e-17 0.16666666666666666
		0.33333333333333326 -1.0576126549149591e-33 1.7272695860409689e-17
		0.33333333333333326 1.0205052948519809e-17 -0.1666666666666666
		0.33333333333333326 2.0410105897039621e-17 -0.33333333333333326
		0.33333333333333326 2.7213474529386164e-17 -0.44444444444444442
		0.33333333333333326 3.0615158845559431e-17 -0.49999999999999994
		0.44444444444444436 -3.0615158845559431e-17 0.49999999999999994
		0.44444444444444436 -2.7213474529386164e-17 0.44444444444444442
		0.44444444444444436 -2.0410105897039624e-17 0.33333333333333331
		0.44444444444444436 -1.0205052948519812e-17 0.16666666666666666
		0.44444444444444436 -1.0576126549149591e-33 1.7272695860409689e-17
		0.44444444444444436 1.0205052948519809e-17 -0.1666666666666666
		0.44444444444444436 2.0410105897039621e-17 -0.33333333333333326
		0.44444444444444436 2.7213474529386164e-17 -0.44444444444444442
		0.44444444444444436 3.0615158845559431e-17 -0.49999999999999994
		0.49999999999999989 -3.0615158845559431e-17 0.49999999999999994
		0.49999999999999989 -2.7213474529386164e-17 0.44444444444444442
		0.49999999999999989 -2.0410105897039624e-17 0.33333333333333331
		0.49999999999999989 -1.0205052948519812e-17 0.16666666666666666
		0.49999999999999989 -1.0576126549149591e-33 1.7272695860409689e-17
		0.49999999999999989 1.0205052948519809e-17 -0.1666666666666666
		0.49999999999999989 2.0410105897039621e-17 -0.33333333333333326
		0.49999999999999989 2.7213474529386164e-17 -0.44444444444444442
		0.49999999999999989 3.0615158845559431e-17 -0.49999999999999994
		
		;
createNode lightLinker -n "lightLinker1";
	setAttr -s 21 ".lnk";
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
createNode script -n "uiConfigurationScriptNode";
	setAttr ".b" -type "string" (
		"// Maya Mel UI Configuration File.\n//\n//  This script is machine generated.  Edit at your own risk.\n//\n//\n\nglobal string $gMainPane;\nif (`paneLayout -exists $gMainPane`) {\n\n\tglobal int $gUseScenePanelConfig;\n\tint    $useSceneConfig = $gUseScenePanelConfig;\n\tint    $menusOkayInPanels = `optionVar -q allowMenusInPanels`;\tint    $nVisPanes = `paneLayout -q -nvp $gMainPane`;\n\tint    $nPanes = 0;\n\tstring $editorName;\n\tstring $panelName;\n\tstring $itemFilterName;\n\tstring $panelConfig;\n\n\t//\n\t//  get current state of the UI\n\t//\n\tsceneUIReplacement -update $gMainPane;\n\n\t$panelName = `sceneUIReplacement -getNextPanel \"modelPanel\" \"Top View\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `modelPanel -unParent -l \"Top View\" -mbv $menusOkayInPanels `;\n\t\t\t$editorName = $panelName;\n            modelEditor -e \n                -camera \"top\" \n                -useInteractiveMode 0\n                -displayLights \"default\" \n                -displayAppearance \"wireframe\" \n                -activeOnly 0\n                -wireframeOnShaded 0\n"
		+ "                -useDefaultMaterial 0\n                -bufferMode \"double\" \n                -twoSidedLighting 1\n                -backfaceCulling 0\n                -xray 0\n                -displayTextures 0\n                -smoothWireframe 0\n                -textureAnisotropic 0\n                -textureHilight 1\n                -textureSampling 2\n                -textureDisplay \"modulate\" \n                -textureMaxSize 4096\n                -fogging 0\n                -fogSource \"fragment\" \n                -fogMode \"linear\" \n                -fogStart 0\n                -fogEnd 100\n                -fogDensity 0.1\n                -fogColor 0.5 0.5 0.5 1 \n                -maxConstantTransparency 1\n                -rendererName \"base_OpenGL_Renderer\" \n                -colorResolution 256 256 \n                -bumpResolution 512 512 \n                -textureCompression 0\n                -transparencyAlgorithm \"frontAndBackCull\" \n                -transpInShadows 0\n                -cullingOverride \"none\" \n                -lowQualityLighting 0\n"
		+ "                -maximumNumHardwareLights 1\n                -occlusionCulling 0\n                -useBaseRenderer 0\n                -useReducedRenderer 0\n                -smallObjectCulling 0\n                -smallObjectThreshold -1 \n                -interactiveDisableShadows 0\n                -interactiveBackFaceCull 0\n                -sortTransparent 1\n                -nurbsCurves 1\n                -nurbsSurfaces 1\n                -polymeshes 1\n                -subdivSurfaces 1\n                -planes 1\n                -lights 1\n                -cameras 1\n                -controlVertices 1\n                -hulls 1\n                -grid 1\n                -joints 1\n                -ikHandles 1\n                -deformers 1\n                -dynamics 1\n                -fluids 1\n                -hairSystems 1\n                -follicles 1\n                -locators 1\n                -dimensions 1\n                -handles 1\n                -pivots 1\n                -textures 1\n                -strokes 1\n                -shadows 0\n"
		+ "                $editorName;\nmodelEditor -e -viewSelected 0 $editorName;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tmodelPanel -edit -l \"Top View\" -mbv $menusOkayInPanels  $panelName;\n\t\t$editorName = $panelName;\n        modelEditor -e \n            -camera \"top\" \n            -useInteractiveMode 0\n            -displayLights \"default\" \n            -displayAppearance \"wireframe\" \n            -activeOnly 0\n            -wireframeOnShaded 0\n            -useDefaultMaterial 0\n            -bufferMode \"double\" \n            -twoSidedLighting 1\n            -backfaceCulling 0\n            -xray 0\n            -displayTextures 0\n            -smoothWireframe 0\n            -textureAnisotropic 0\n            -textureHilight 1\n            -textureSampling 2\n            -textureDisplay \"modulate\" \n            -textureMaxSize 4096\n            -fogging 0\n            -fogSource \"fragment\" \n            -fogMode \"linear\" \n            -fogStart 0\n            -fogEnd 100\n            -fogDensity 0.1\n            -fogColor 0.5 0.5 0.5 1 \n"
		+ "            -maxConstantTransparency 1\n            -rendererName \"base_OpenGL_Renderer\" \n            -colorResolution 256 256 \n            -bumpResolution 512 512 \n            -textureCompression 0\n            -transparencyAlgorithm \"frontAndBackCull\" \n            -transpInShadows 0\n            -cullingOverride \"none\" \n            -lowQualityLighting 0\n            -maximumNumHardwareLights 1\n            -occlusionCulling 0\n            -useBaseRenderer 0\n            -useReducedRenderer 0\n            -smallObjectCulling 0\n            -smallObjectThreshold -1 \n            -interactiveDisableShadows 0\n            -interactiveBackFaceCull 0\n            -sortTransparent 1\n            -nurbsCurves 1\n            -nurbsSurfaces 1\n            -polymeshes 1\n            -subdivSurfaces 1\n            -planes 1\n            -lights 1\n            -cameras 1\n            -controlVertices 1\n            -hulls 1\n            -grid 1\n            -joints 1\n            -ikHandles 1\n            -deformers 1\n            -dynamics 1\n            -fluids 1\n"
		+ "            -hairSystems 1\n            -follicles 1\n            -locators 1\n            -dimensions 1\n            -handles 1\n            -pivots 1\n            -textures 1\n            -strokes 1\n            -shadows 0\n            $editorName;\nmodelEditor -e -viewSelected 0 $editorName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextPanel \"modelPanel\" \"Side View\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `modelPanel -unParent -l \"Side View\" -mbv $menusOkayInPanels `;\n\t\t\t$editorName = $panelName;\n            modelEditor -e \n                -camera \"side\" \n                -useInteractiveMode 0\n                -displayLights \"default\" \n                -displayAppearance \"wireframe\" \n                -activeOnly 0\n                -wireframeOnShaded 0\n                -useDefaultMaterial 0\n                -bufferMode \"double\" \n                -twoSidedLighting 1\n                -backfaceCulling 0\n                -xray 0\n                -displayTextures 0\n"
		+ "                -smoothWireframe 0\n                -textureAnisotropic 0\n                -textureHilight 1\n                -textureSampling 2\n                -textureDisplay \"modulate\" \n                -textureMaxSize 4096\n                -fogging 0\n                -fogSource \"fragment\" \n                -fogMode \"linear\" \n                -fogStart 0\n                -fogEnd 100\n                -fogDensity 0.1\n                -fogColor 0.5 0.5 0.5 1 \n                -maxConstantTransparency 1\n                -rendererName \"base_OpenGL_Renderer\" \n                -colorResolution 256 256 \n                -bumpResolution 512 512 \n                -textureCompression 0\n                -transparencyAlgorithm \"frontAndBackCull\" \n                -transpInShadows 0\n                -cullingOverride \"none\" \n                -lowQualityLighting 0\n                -maximumNumHardwareLights 1\n                -occlusionCulling 0\n                -useBaseRenderer 0\n                -useReducedRenderer 0\n                -smallObjectCulling 0\n"
		+ "                -smallObjectThreshold -1 \n                -interactiveDisableShadows 0\n                -interactiveBackFaceCull 0\n                -sortTransparent 1\n                -nurbsCurves 1\n                -nurbsSurfaces 1\n                -polymeshes 1\n                -subdivSurfaces 1\n                -planes 1\n                -lights 1\n                -cameras 1\n                -controlVertices 1\n                -hulls 1\n                -grid 1\n                -joints 1\n                -ikHandles 1\n                -deformers 1\n                -dynamics 1\n                -fluids 1\n                -hairSystems 1\n                -follicles 1\n                -locators 1\n                -dimensions 1\n                -handles 1\n                -pivots 1\n                -textures 1\n                -strokes 1\n                -shadows 0\n                $editorName;\nmodelEditor -e -viewSelected 0 $editorName;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tmodelPanel -edit -l \"Side View\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\t$editorName = $panelName;\n        modelEditor -e \n            -camera \"side\" \n            -useInteractiveMode 0\n            -displayLights \"default\" \n            -displayAppearance \"wireframe\" \n            -activeOnly 0\n            -wireframeOnShaded 0\n            -useDefaultMaterial 0\n            -bufferMode \"double\" \n            -twoSidedLighting 1\n            -backfaceCulling 0\n            -xray 0\n            -displayTextures 0\n            -smoothWireframe 0\n            -textureAnisotropic 0\n            -textureHilight 1\n            -textureSampling 2\n            -textureDisplay \"modulate\" \n            -textureMaxSize 4096\n            -fogging 0\n            -fogSource \"fragment\" \n            -fogMode \"linear\" \n            -fogStart 0\n            -fogEnd 100\n            -fogDensity 0.1\n            -fogColor 0.5 0.5 0.5 1 \n            -maxConstantTransparency 1\n            -rendererName \"base_OpenGL_Renderer\" \n            -colorResolution 256 256 \n            -bumpResolution 512 512 \n            -textureCompression 0\n"
		+ "            -transparencyAlgorithm \"frontAndBackCull\" \n            -transpInShadows 0\n            -cullingOverride \"none\" \n            -lowQualityLighting 0\n            -maximumNumHardwareLights 1\n            -occlusionCulling 0\n            -useBaseRenderer 0\n            -useReducedRenderer 0\n            -smallObjectCulling 0\n            -smallObjectThreshold -1 \n            -interactiveDisableShadows 0\n            -interactiveBackFaceCull 0\n            -sortTransparent 1\n            -nurbsCurves 1\n            -nurbsSurfaces 1\n            -polymeshes 1\n            -subdivSurfaces 1\n            -planes 1\n            -lights 1\n            -cameras 1\n            -controlVertices 1\n            -hulls 1\n            -grid 1\n            -joints 1\n            -ikHandles 1\n            -deformers 1\n            -dynamics 1\n            -fluids 1\n            -hairSystems 1\n            -follicles 1\n            -locators 1\n            -dimensions 1\n            -handles 1\n            -pivots 1\n            -textures 1\n            -strokes 1\n"
		+ "            -shadows 0\n            $editorName;\nmodelEditor -e -viewSelected 0 $editorName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextPanel \"modelPanel\" \"Front View\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `modelPanel -unParent -l \"Front View\" -mbv $menusOkayInPanels `;\n\t\t\t$editorName = $panelName;\n            modelEditor -e \n                -camera \"front\" \n                -useInteractiveMode 0\n                -displayLights \"default\" \n                -displayAppearance \"smoothShaded\" \n                -activeOnly 0\n                -wireframeOnShaded 0\n                -useDefaultMaterial 0\n                -bufferMode \"double\" \n                -twoSidedLighting 1\n                -backfaceCulling 0\n                -xray 0\n                -displayTextures 0\n                -smoothWireframe 0\n                -textureAnisotropic 0\n                -textureHilight 1\n                -textureSampling 2\n                -textureDisplay \"modulate\" \n"
		+ "                -textureMaxSize 4096\n                -fogging 0\n                -fogSource \"fragment\" \n                -fogMode \"linear\" \n                -fogStart 0\n                -fogEnd 100\n                -fogDensity 0.1\n                -fogColor 0.5 0.5 0.5 1 \n                -maxConstantTransparency 1\n                -rendererName \"base_OpenGL_Renderer\" \n                -colorResolution 256 256 \n                -bumpResolution 512 512 \n                -textureCompression 0\n                -transparencyAlgorithm \"frontAndBackCull\" \n                -transpInShadows 0\n                -cullingOverride \"none\" \n                -lowQualityLighting 0\n                -maximumNumHardwareLights 1\n                -occlusionCulling 0\n                -useBaseRenderer 0\n                -useReducedRenderer 0\n                -smallObjectCulling 0\n                -smallObjectThreshold -1 \n                -interactiveDisableShadows 0\n                -interactiveBackFaceCull 0\n                -sortTransparent 1\n                -nurbsCurves 1\n"
		+ "                -nurbsSurfaces 1\n                -polymeshes 1\n                -subdivSurfaces 1\n                -planes 1\n                -lights 1\n                -cameras 1\n                -controlVertices 1\n                -hulls 1\n                -grid 1\n                -joints 1\n                -ikHandles 1\n                -deformers 1\n                -dynamics 1\n                -fluids 1\n                -hairSystems 1\n                -follicles 1\n                -locators 1\n                -dimensions 1\n                -handles 1\n                -pivots 1\n                -textures 1\n                -strokes 1\n                -shadows 0\n                $editorName;\nmodelEditor -e -viewSelected 0 $editorName;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tmodelPanel -edit -l \"Front View\" -mbv $menusOkayInPanels  $panelName;\n\t\t$editorName = $panelName;\n        modelEditor -e \n            -camera \"front\" \n            -useInteractiveMode 0\n            -displayLights \"default\" \n            -displayAppearance \"smoothShaded\" \n"
		+ "            -activeOnly 0\n            -wireframeOnShaded 0\n            -useDefaultMaterial 0\n            -bufferMode \"double\" \n            -twoSidedLighting 1\n            -backfaceCulling 0\n            -xray 0\n            -displayTextures 0\n            -smoothWireframe 0\n            -textureAnisotropic 0\n            -textureHilight 1\n            -textureSampling 2\n            -textureDisplay \"modulate\" \n            -textureMaxSize 4096\n            -fogging 0\n            -fogSource \"fragment\" \n            -fogMode \"linear\" \n            -fogStart 0\n            -fogEnd 100\n            -fogDensity 0.1\n            -fogColor 0.5 0.5 0.5 1 \n            -maxConstantTransparency 1\n            -rendererName \"base_OpenGL_Renderer\" \n            -colorResolution 256 256 \n            -bumpResolution 512 512 \n            -textureCompression 0\n            -transparencyAlgorithm \"frontAndBackCull\" \n            -transpInShadows 0\n            -cullingOverride \"none\" \n            -lowQualityLighting 0\n            -maximumNumHardwareLights 1\n"
		+ "            -occlusionCulling 0\n            -useBaseRenderer 0\n            -useReducedRenderer 0\n            -smallObjectCulling 0\n            -smallObjectThreshold -1 \n            -interactiveDisableShadows 0\n            -interactiveBackFaceCull 0\n            -sortTransparent 1\n            -nurbsCurves 1\n            -nurbsSurfaces 1\n            -polymeshes 1\n            -subdivSurfaces 1\n            -planes 1\n            -lights 1\n            -cameras 1\n            -controlVertices 1\n            -hulls 1\n            -grid 1\n            -joints 1\n            -ikHandles 1\n            -deformers 1\n            -dynamics 1\n            -fluids 1\n            -hairSystems 1\n            -follicles 1\n            -locators 1\n            -dimensions 1\n            -handles 1\n            -pivots 1\n            -textures 1\n            -strokes 1\n            -shadows 0\n            $editorName;\nmodelEditor -e -viewSelected 0 $editorName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextPanel \"modelPanel\" \"Persp View\"`;\n"
		+ "\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `modelPanel -unParent -l \"Persp View\" -mbv $menusOkayInPanels `;\n\t\t\t$editorName = $panelName;\n            modelEditor -e \n                -camera \"persp\" \n                -useInteractiveMode 0\n                -displayLights \"default\" \n                -displayAppearance \"smoothShaded\" \n                -activeOnly 0\n                -wireframeOnShaded 0\n                -useDefaultMaterial 0\n                -bufferMode \"double\" \n                -twoSidedLighting 1\n                -backfaceCulling 0\n                -xray 0\n                -displayTextures 0\n                -smoothWireframe 0\n                -textureAnisotropic 0\n                -textureHilight 1\n                -textureSampling 2\n                -textureDisplay \"modulate\" \n                -textureMaxSize 4096\n                -fogging 0\n                -fogSource \"fragment\" \n                -fogMode \"linear\" \n                -fogStart 0\n                -fogEnd 100\n                -fogDensity 0.1\n"
		+ "                -fogColor 0.5 0.5 0.5 1 \n                -maxConstantTransparency 1\n                -rendererName \"base_OpenGL_Renderer\" \n                -colorResolution 256 256 \n                -bumpResolution 512 512 \n                -textureCompression 0\n                -transparencyAlgorithm \"frontAndBackCull\" \n                -transpInShadows 0\n                -cullingOverride \"none\" \n                -lowQualityLighting 0\n                -maximumNumHardwareLights 1\n                -occlusionCulling 0\n                -useBaseRenderer 0\n                -useReducedRenderer 0\n                -smallObjectCulling 0\n                -smallObjectThreshold -1 \n                -interactiveDisableShadows 0\n                -interactiveBackFaceCull 0\n                -sortTransparent 1\n                -nurbsCurves 1\n                -nurbsSurfaces 1\n                -polymeshes 1\n                -subdivSurfaces 1\n                -planes 1\n                -lights 1\n                -cameras 1\n                -controlVertices 1\n"
		+ "                -hulls 1\n                -grid 1\n                -joints 1\n                -ikHandles 1\n                -deformers 1\n                -dynamics 1\n                -fluids 1\n                -hairSystems 1\n                -follicles 1\n                -locators 1\n                -dimensions 1\n                -handles 1\n                -pivots 1\n                -textures 1\n                -strokes 1\n                -shadows 0\n                $editorName;\nmodelEditor -e -viewSelected 0 $editorName;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tmodelPanel -edit -l \"Persp View\" -mbv $menusOkayInPanels  $panelName;\n\t\t$editorName = $panelName;\n        modelEditor -e \n            -camera \"persp\" \n            -useInteractiveMode 0\n            -displayLights \"default\" \n            -displayAppearance \"smoothShaded\" \n            -activeOnly 0\n            -wireframeOnShaded 0\n            -useDefaultMaterial 0\n            -bufferMode \"double\" \n            -twoSidedLighting 1\n            -backfaceCulling 0\n"
		+ "            -xray 0\n            -displayTextures 0\n            -smoothWireframe 0\n            -textureAnisotropic 0\n            -textureHilight 1\n            -textureSampling 2\n            -textureDisplay \"modulate\" \n            -textureMaxSize 4096\n            -fogging 0\n            -fogSource \"fragment\" \n            -fogMode \"linear\" \n            -fogStart 0\n            -fogEnd 100\n            -fogDensity 0.1\n            -fogColor 0.5 0.5 0.5 1 \n            -maxConstantTransparency 1\n            -rendererName \"base_OpenGL_Renderer\" \n            -colorResolution 256 256 \n            -bumpResolution 512 512 \n            -textureCompression 0\n            -transparencyAlgorithm \"frontAndBackCull\" \n            -transpInShadows 0\n            -cullingOverride \"none\" \n            -lowQualityLighting 0\n            -maximumNumHardwareLights 1\n            -occlusionCulling 0\n            -useBaseRenderer 0\n            -useReducedRenderer 0\n            -smallObjectCulling 0\n            -smallObjectThreshold -1 \n            -interactiveDisableShadows 0\n"
		+ "            -interactiveBackFaceCull 0\n            -sortTransparent 1\n            -nurbsCurves 1\n            -nurbsSurfaces 1\n            -polymeshes 1\n            -subdivSurfaces 1\n            -planes 1\n            -lights 1\n            -cameras 1\n            -controlVertices 1\n            -hulls 1\n            -grid 1\n            -joints 1\n            -ikHandles 1\n            -deformers 1\n            -dynamics 1\n            -fluids 1\n            -hairSystems 1\n            -follicles 1\n            -locators 1\n            -dimensions 1\n            -handles 1\n            -pivots 1\n            -textures 1\n            -strokes 1\n            -shadows 0\n            $editorName;\nmodelEditor -e -viewSelected 0 $editorName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextPanel \"outlinerPanel\" \"Outliner\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `outlinerPanel -unParent -l \"Outliner\" -mbv $menusOkayInPanels `;\n\t\t\t$editorName = $panelName;\n"
		+ "            outlinerEditor -e \n                -mainListConnection \"worldList\" \n                -selectionConnection \"modelList\" \n                -showShapes 0\n                -showAttributes 0\n                -showConnected 0\n                -showAnimCurvesOnly 0\n                -showMuteInfo 0\n                -autoExpand 0\n                -showDagOnly 1\n                -ignoreDagHierarchy 0\n                -expandConnections 0\n                -showUnitlessCurves 1\n                -showCompounds 1\n                -showLeafs 1\n                -showNumericAttrsOnly 0\n                -highlightActive 1\n                -autoSelectNewObjects 0\n                -doNotSelectNewObjects 0\n                -dropIsParent 1\n                -transmitFilters 0\n                -setFilter \"defaultSetFilter\" \n                -showSetMembers 1\n                -allowMultiSelection 1\n                -alwaysToggleSelect 0\n                -directSelect 0\n                -displayMode \"DAG\" \n                -expandObjects 0\n                -setsIgnoreFilters 1\n"
		+ "                -editAttrName 0\n                -showAttrValues 0\n                -highlightSecondary 0\n                -showUVAttrsOnly 0\n                -showTextureNodesOnly 0\n                -sortOrder \"none\" \n                -longNames 0\n                -niceNames 1\n                -showNamespace 1\n                $editorName;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\toutlinerPanel -edit -l \"Outliner\" -mbv $menusOkayInPanels  $panelName;\n\t\t$editorName = $panelName;\n        outlinerEditor -e \n            -mainListConnection \"worldList\" \n            -selectionConnection \"modelList\" \n            -showShapes 0\n            -showAttributes 0\n            -showConnected 0\n            -showAnimCurvesOnly 0\n            -showMuteInfo 0\n            -autoExpand 0\n            -showDagOnly 1\n            -ignoreDagHierarchy 0\n            -expandConnections 0\n            -showUnitlessCurves 1\n            -showCompounds 1\n            -showLeafs 1\n            -showNumericAttrsOnly 0\n            -highlightActive 1\n"
		+ "            -autoSelectNewObjects 0\n            -doNotSelectNewObjects 0\n            -dropIsParent 1\n            -transmitFilters 0\n            -setFilter \"defaultSetFilter\" \n            -showSetMembers 1\n            -allowMultiSelection 1\n            -alwaysToggleSelect 0\n            -directSelect 0\n            -displayMode \"DAG\" \n            -expandObjects 0\n            -setsIgnoreFilters 1\n            -editAttrName 0\n            -showAttrValues 0\n            -highlightSecondary 0\n            -showUVAttrsOnly 0\n            -showTextureNodesOnly 0\n            -sortOrder \"none\" \n            -longNames 0\n            -niceNames 1\n            -showNamespace 1\n            $editorName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\tif ($useSceneConfig) {\n\t\toutlinerPanel -e -to $panelName;\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"graphEditor\" \"Graph Editor\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `scriptedPanel -unParent  -type \"graphEditor\" -l \"Graph Editor\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t\t$editorName = ($panelName+\"OutlineEd\");\n            outlinerEditor -e \n                -mainListConnection \"graphEditorList\" \n                -selectionConnection \"graphEditor1FromOutliner\" \n                -highlightConnection \"keyframeList\" \n                -showShapes 1\n                -showAttributes 1\n                -showConnected 1\n                -showAnimCurvesOnly 1\n                -showMuteInfo 0\n                -autoExpand 1\n                -showDagOnly 0\n                -ignoreDagHierarchy 0\n                -expandConnections 1\n                -showUnitlessCurves 1\n                -showCompounds 0\n                -showLeafs 1\n                -showNumericAttrsOnly 1\n                -highlightActive 0\n                -autoSelectNewObjects 1\n                -doNotSelectNewObjects 0\n                -dropIsParent 1\n                -transmitFilters 1\n                -setFilter \"0\" \n                -showSetMembers 0\n                -allowMultiSelection 1\n                -alwaysToggleSelect 0\n                -directSelect 0\n"
		+ "                -displayMode \"DAG\" \n                -expandObjects 0\n                -setsIgnoreFilters 1\n                -editAttrName 0\n                -showAttrValues 0\n                -highlightSecondary 0\n                -showUVAttrsOnly 0\n                -showTextureNodesOnly 0\n                -sortOrder \"none\" \n                -longNames 0\n                -niceNames 1\n                -showNamespace 1\n                $editorName;\n\n\t\t\t$editorName = ($panelName+\"GraphEd\");\n            animCurveEditor -e \n                -mainListConnection \"graphEditor1FromOutliner\" \n                -displayKeys 1\n                -displayTangents 0\n                -displayActiveKeys 0\n                -displayActiveKeyTangents 1\n                -displayInfinities 0\n                -autoFit 0\n                -snapTime \"integer\" \n                -snapValue \"none\" \n                -showResults \"off\" \n                -showBufferCurves \"off\" \n                -smoothness \"fine\" \n                -resultSamples 1\n                -resultScreenSamples 0\n"
		+ "                -resultUpdate \"delayed\" \n                $editorName;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tscriptedPanel -edit -l \"Graph Editor\" -mbv $menusOkayInPanels  $panelName;\n\n\t\t\t$editorName = ($panelName+\"OutlineEd\");\n            outlinerEditor -e \n                -mainListConnection \"graphEditorList\" \n                -selectionConnection \"graphEditor1FromOutliner\" \n                -highlightConnection \"keyframeList\" \n                -showShapes 1\n                -showAttributes 1\n                -showConnected 1\n                -showAnimCurvesOnly 1\n                -showMuteInfo 0\n                -autoExpand 1\n                -showDagOnly 0\n                -ignoreDagHierarchy 0\n                -expandConnections 1\n                -showUnitlessCurves 1\n                -showCompounds 0\n                -showLeafs 1\n                -showNumericAttrsOnly 1\n                -highlightActive 0\n                -autoSelectNewObjects 1\n                -doNotSelectNewObjects 0\n                -dropIsParent 1\n"
		+ "                -transmitFilters 1\n                -setFilter \"0\" \n                -showSetMembers 0\n                -allowMultiSelection 1\n                -alwaysToggleSelect 0\n                -directSelect 0\n                -displayMode \"DAG\" \n                -expandObjects 0\n                -setsIgnoreFilters 1\n                -editAttrName 0\n                -showAttrValues 0\n                -highlightSecondary 0\n                -showUVAttrsOnly 0\n                -showTextureNodesOnly 0\n                -sortOrder \"none\" \n                -longNames 0\n                -niceNames 1\n                -showNamespace 1\n                $editorName;\n\n\t\t\t$editorName = ($panelName+\"GraphEd\");\n            animCurveEditor -e \n                -mainListConnection \"graphEditor1FromOutliner\" \n                -displayKeys 1\n                -displayTangents 0\n                -displayActiveKeys 0\n                -displayActiveKeyTangents 1\n                -displayInfinities 0\n                -autoFit 0\n                -snapTime \"integer\" \n"
		+ "                -snapValue \"none\" \n                -showResults \"off\" \n                -showBufferCurves \"off\" \n                -smoothness \"fine\" \n                -resultSamples 1\n                -resultScreenSamples 0\n                -resultUpdate \"delayed\" \n                $editorName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"dopeSheetPanel\" \"Dope Sheet\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `scriptedPanel -unParent  -type \"dopeSheetPanel\" -l \"Dope Sheet\" -mbv $menusOkayInPanels `;\n\n\t\t\t$editorName = ($panelName+\"OutlineEd\");\n            outlinerEditor -e \n                -mainListConnection \"animationList\" \n                -selectionConnection \"dopeSheetPanel1OutlinerSelection\" \n                -highlightConnection \"keyframeList\" \n                -showShapes 1\n                -showAttributes 1\n                -showConnected 1\n                -showAnimCurvesOnly 1\n                -showMuteInfo 0\n"
		+ "                -autoExpand 0\n                -showDagOnly 0\n                -ignoreDagHierarchy 0\n                -expandConnections 1\n                -showUnitlessCurves 0\n                -showCompounds 1\n                -showLeafs 1\n                -showNumericAttrsOnly 1\n                -highlightActive 0\n                -autoSelectNewObjects 0\n                -doNotSelectNewObjects 1\n                -dropIsParent 1\n                -transmitFilters 0\n                -setFilter \"0\" \n                -showSetMembers 0\n                -allowMultiSelection 1\n                -alwaysToggleSelect 0\n                -directSelect 0\n                -displayMode \"DAG\" \n                -expandObjects 0\n                -setsIgnoreFilters 1\n                -editAttrName 0\n                -showAttrValues 0\n                -highlightSecondary 0\n                -showUVAttrsOnly 0\n                -showTextureNodesOnly 0\n                -sortOrder \"none\" \n                -longNames 0\n                -niceNames 1\n                -showNamespace 1\n"
		+ "                $editorName;\n\n\t\t\t$editorName = ($panelName+\"DopeSheetEd\");\n            dopeSheetEditor -e \n                -mainListConnection \"dopeSheetPanel1FromOutliner\" \n                -highlightConnection \"dopeSheetPanel1OutlinerSelection\" \n                -displayKeys 1\n                -displayTangents 0\n                -displayActiveKeys 0\n                -displayActiveKeyTangents 0\n                -displayInfinities 0\n                -autoFit 0\n                -snapTime \"integer\" \n                -snapValue \"none\" \n                -outliner \"dopeSheetPanel1OutlineEd\" \n                -showSummary 1\n                -showScene 0\n                -hierarchyBelow 0\n                -showTicks 0\n                -selectionWindow 0 0 0 0 \n                $editorName;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tscriptedPanel -edit -l \"Dope Sheet\" -mbv $menusOkayInPanels  $panelName;\n\n\t\t\t$editorName = ($panelName+\"OutlineEd\");\n            outlinerEditor -e \n                -mainListConnection \"animationList\" \n"
		+ "                -selectionConnection \"dopeSheetPanel1OutlinerSelection\" \n                -highlightConnection \"keyframeList\" \n                -showShapes 1\n                -showAttributes 1\n                -showConnected 1\n                -showAnimCurvesOnly 1\n                -showMuteInfo 0\n                -autoExpand 0\n                -showDagOnly 0\n                -ignoreDagHierarchy 0\n                -expandConnections 1\n                -showUnitlessCurves 0\n                -showCompounds 1\n                -showLeafs 1\n                -showNumericAttrsOnly 1\n                -highlightActive 0\n                -autoSelectNewObjects 0\n                -doNotSelectNewObjects 1\n                -dropIsParent 1\n                -transmitFilters 0\n                -setFilter \"0\" \n                -showSetMembers 0\n                -allowMultiSelection 1\n                -alwaysToggleSelect 0\n                -directSelect 0\n                -displayMode \"DAG\" \n                -expandObjects 0\n                -setsIgnoreFilters 1\n"
		+ "                -editAttrName 0\n                -showAttrValues 0\n                -highlightSecondary 0\n                -showUVAttrsOnly 0\n                -showTextureNodesOnly 0\n                -sortOrder \"none\" \n                -longNames 0\n                -niceNames 1\n                -showNamespace 1\n                $editorName;\n\n\t\t\t$editorName = ($panelName+\"DopeSheetEd\");\n            dopeSheetEditor -e \n                -mainListConnection \"dopeSheetPanel1FromOutliner\" \n                -highlightConnection \"dopeSheetPanel1OutlinerSelection\" \n                -displayKeys 1\n                -displayTangents 0\n                -displayActiveKeys 0\n                -displayActiveKeyTangents 0\n                -displayInfinities 0\n                -autoFit 0\n                -snapTime \"integer\" \n                -snapValue \"none\" \n                -outliner \"dopeSheetPanel1OutlineEd\" \n                -showSummary 1\n                -showScene 0\n                -hierarchyBelow 0\n                -showTicks 0\n                -selectionWindow 0 0 0 0 \n"
		+ "                $editorName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"clipEditorPanel\" \"Trax Editor\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `scriptedPanel -unParent  -type \"clipEditorPanel\" -l \"Trax Editor\" -mbv $menusOkayInPanels `;\n\n\t\t\t$editorName = clipEditorNameFromPanel($panelName);\n            clipEditor -e \n                -mainListConnection \"lockedList1\" \n                -highlightConnection \"clipEditorPanel1HighlightConnection\" \n                -displayKeys 0\n                -displayTangents 0\n                -displayActiveKeys 0\n                -displayActiveKeyTangents 0\n                -displayInfinities 0\n                -autoFit 0\n                -snapTime \"none\" \n                -snapValue \"none\" \n                $editorName;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tscriptedPanel -edit -l \"Trax Editor\" -mbv $menusOkayInPanels  $panelName;\n\n\t\t\t$editorName = clipEditorNameFromPanel($panelName);\n"
		+ "            clipEditor -e \n                -mainListConnection \"lockedList1\" \n                -highlightConnection \"clipEditorPanel1HighlightConnection\" \n                -displayKeys 0\n                -displayTangents 0\n                -displayActiveKeys 0\n                -displayActiveKeyTangents 0\n                -displayInfinities 0\n                -autoFit 0\n                -snapTime \"none\" \n                -snapValue \"none\" \n                $editorName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"hyperGraphPanel\" \"Hypergraph\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `scriptedPanel -unParent  -type \"hyperGraphPanel\" -l \"Hypergraph\" -mbv $menusOkayInPanels `;\n\n\t\t\t$editorName = ($panelName+\"HyperGraphEd\");\n            hyperGraph -e \n                -orientation \"horiz\" \n                -zoom 1\n                -animateTransition 0\n                -showShapes 0\n                -showDeformers 0\n                -showExpressions 0\n"
		+ "                -showConstraints 0\n                -showUnderworld 0\n                -showInvisible 0\n                -transitionFrames 1\n                -freeform 0\n                -imageEnabled 0\n                -graphType \"DAG\" \n                -updateSelection 1\n                -updateNodeAdded 1\n                -useDrawOverrideColor 0\n                -iconSize \"smallIcons\" \n                -showCachedConnections 0\n                $editorName;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tscriptedPanel -edit -l \"Hypergraph\" -mbv $menusOkayInPanels  $panelName;\n\n\t\t\t$editorName = ($panelName+\"HyperGraphEd\");\n            hyperGraph -e \n                -orientation \"horiz\" \n                -zoom 1\n                -animateTransition 0\n                -showShapes 0\n                -showDeformers 0\n                -showExpressions 0\n                -showConstraints 0\n                -showUnderworld 0\n                -showInvisible 0\n                -transitionFrames 1\n                -freeform 0\n                -imageEnabled 0\n"
		+ "                -graphType \"DAG\" \n                -updateSelection 1\n                -updateNodeAdded 1\n                -useDrawOverrideColor 0\n                -iconSize \"smallIcons\" \n                -showCachedConnections 0\n                $editorName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"hyperShadePanel\" \"Hypershade\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `scriptedPanel -unParent  -type \"hyperShadePanel\" -l \"Hypershade\" -mbv $menusOkayInPanels `;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tscriptedPanel -edit -l \"Hypershade\" -mbv $menusOkayInPanels  $panelName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\tif ($useSceneConfig) {\n\t\tscriptedPanel -e -to $panelName;\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"visorPanel\" \"Visor\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `scriptedPanel -unParent  -type \"visorPanel\" -l \"Visor\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tscriptedPanel -edit -l \"Visor\" -mbv $menusOkayInPanels  $panelName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"polyTexturePlacementPanel\" \"UV Texture Editor\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `scriptedPanel -unParent  -type \"polyTexturePlacementPanel\" -l \"UV Texture Editor\" -mbv $menusOkayInPanels `;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tscriptedPanel -edit -l \"UV Texture Editor\" -mbv $menusOkayInPanels  $panelName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"multiListerPanel\" \"Multilister\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `scriptedPanel -unParent  -type \"multiListerPanel\" -l \"Multilister\" -mbv $menusOkayInPanels `;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tscriptedPanel -edit -l \"Multilister\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"renderWindowPanel\" \"Render View\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `scriptedPanel -unParent  -type \"renderWindowPanel\" -l \"Render View\" -mbv $menusOkayInPanels `;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tscriptedPanel -edit -l \"Render View\" -mbv $menusOkayInPanels  $panelName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextPanel \"blendShapePanel\" \"Blend Shape\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\tblendShapePanel -unParent -l \"Blend Shape\" -mbv $menusOkayInPanels ;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tblendShapePanel -edit -l \"Blend Shape\" -mbv $menusOkayInPanels  $panelName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"dynRelEdPanel\" \"Dynamic Relationships\"`;\n"
		+ "\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `scriptedPanel -unParent  -type \"dynRelEdPanel\" -l \"Dynamic Relationships\" -mbv $menusOkayInPanels `;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tscriptedPanel -edit -l \"Dynamic Relationships\" -mbv $menusOkayInPanels  $panelName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextPanel \"devicePanel\" \"Devices\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\tdevicePanel -unParent -l \"Devices\" -mbv $menusOkayInPanels ;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tdevicePanel -edit -l \"Devices\" -mbv $menusOkayInPanels  $panelName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"relationshipPanel\" \"Relationship Editor\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `scriptedPanel -unParent  -type \"relationshipPanel\" -l \"Relationship Editor\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tscriptedPanel -edit -l \"Relationship Editor\" -mbv $menusOkayInPanels  $panelName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"referenceEditorPanel\" \"Reference Editor\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `scriptedPanel -unParent  -type \"referenceEditorPanel\" -l \"Reference Editor\" -mbv $menusOkayInPanels `;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tscriptedPanel -edit -l \"Reference Editor\" -mbv $menusOkayInPanels  $panelName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"componentEditorPanel\" \"Component Editor\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `scriptedPanel -unParent  -type \"componentEditorPanel\" -l \"Component Editor\" -mbv $menusOkayInPanels `;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tscriptedPanel -edit -l \"Component Editor\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"dynPaintScriptedPanelType\" \"Paint Effects\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `scriptedPanel -unParent  -type \"dynPaintScriptedPanelType\" -l \"Paint Effects\" -mbv $menusOkayInPanels `;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tscriptedPanel -edit -l \"Paint Effects\" -mbv $menusOkayInPanels  $panelName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"webBrowserPanel\" \"Web Browser\"`;\n\tif (\"\" == $panelName) {\n\t\tif ($useSceneConfig) {\n\t\t\t$panelName = `scriptedPanel -unParent  -type \"webBrowserPanel\" -l \"Web Browser\" -mbv $menusOkayInPanels `;\n\t\t}\n\t} else {\n\t\t$label = `panel -q -label $panelName`;\n\t\tscriptedPanel -edit -l \"Web Browser\" -mbv $menusOkayInPanels  $panelName;\n\t\tif (!$useSceneConfig) {\n\t\t\tpanel -e -l $label $panelName;\n\t\t}\n\t}\n\n\n\tif ($useSceneConfig) {\n        string $configName = `getPanel -cwl \"Current Layout\"`;\n"
		+ "        if (\"\" != $configName) {\n\t\t\tpanelConfiguration -edit -label \"Current Layout\"\n\t\t\t\t-defaultImage \"\"\n\t\t\t\t-image \"\"\n\t\t\t\t-sc false\n\t\t\t\t-configString \"global string $gMainPane; paneLayout -e -cn \\\"single\\\" -ps 1 100 100 $gMainPane;\"\n\t\t\t\t-removeAllPanels\n\t\t\t\t-ap false\n\t\t\t\t\t\"Persp View\"\n\t\t\t\t\t\"modelPanel\"\n"
		+ "\t\t\t\t\t\"$panelName = `modelPanel -unParent -l \\\"Persp View\\\" -mbv $menusOkayInPanels `;\\n$editorName = $panelName;\\nmodelEditor -e \\n    -cam `findStartUpCamera persp` \\n    -useInteractiveMode 0\\n    -displayLights \\\"default\\\" \\n    -displayAppearance \\\"smoothShaded\\\" \\n    -activeOnly 0\\n    -wireframeOnShaded 0\\n    -useDefaultMaterial 0\\n    -bufferMode \\\"double\\\" \\n    -twoSidedLighting 1\\n    -backfaceCulling 0\\n    -xray 0\\n    -displayTextures 0\\n    -smoothWireframe 0\\n    -textureAnisotropic 0\\n    -textureHilight 1\\n    -textureSampling 2\\n    -textureDisplay \\\"modulate\\\" \\n    -textureMaxSize 4096\\n    -fogging 0\\n    -fogSource \\\"fragment\\\" \\n    -fogMode \\\"linear\\\" \\n    -fogStart 0\\n    -fogEnd 100\\n    -fogDensity 0.1\\n    -fogColor 0.5 0.5 0.5 1 \\n    -maxConstantTransparency 1\\n    -rendererName \\\"base_OpenGL_Renderer\\\" \\n    -colorResolution 256 256 \\n    -bumpResolution 512 512 \\n    -textureCompression 0\\n    -transparencyAlgorithm \\\"frontAndBackCull\\\" \\n    -transpInShadows 0\\n    -cullingOverride \\\"none\\\" \\n    -lowQualityLighting 0\\n    -maximumNumHardwareLights 1\\n    -occlusionCulling 0\\n    -useBaseRenderer 0\\n    -useReducedRenderer 0\\n    -smallObjectCulling 0\\n    -smallObjectThreshold -1 \\n    -interactiveDisableShadows 0\\n    -interactiveBackFaceCull 0\\n    -sortTransparent 1\\n    -nurbsCurves 1\\n    -nurbsSurfaces 1\\n    -polymeshes 1\\n    -subdivSurfaces 1\\n    -planes 1\\n    -lights 1\\n    -cameras 1\\n    -controlVertices 1\\n    -hulls 1\\n    -grid 1\\n    -joints 1\\n    -ikHandles 1\\n    -deformers 1\\n    -dynamics 1\\n    -fluids 1\\n    -hairSystems 1\\n    -follicles 1\\n    -locators 1\\n    -dimensions 1\\n    -handles 1\\n    -pivots 1\\n    -textures 1\\n    -strokes 1\\n    -shadows 0\\n    $editorName;\\nmodelEditor -e -viewSelected 0 $editorName\"\n"
		+ "\t\t\t\t\t\"modelPanel -edit -l \\\"Persp View\\\" -mbv $menusOkayInPanels  $panelName;\\n$editorName = $panelName;\\nmodelEditor -e \\n    -cam `findStartUpCamera persp` \\n    -useInteractiveMode 0\\n    -displayLights \\\"default\\\" \\n    -displayAppearance \\\"smoothShaded\\\" \\n    -activeOnly 0\\n    -wireframeOnShaded 0\\n    -useDefaultMaterial 0\\n    -bufferMode \\\"double\\\" \\n    -twoSidedLighting 1\\n    -backfaceCulling 0\\n    -xray 0\\n    -displayTextures 0\\n    -smoothWireframe 0\\n    -textureAnisotropic 0\\n    -textureHilight 1\\n    -textureSampling 2\\n    -textureDisplay \\\"modulate\\\" \\n    -textureMaxSize 4096\\n    -fogging 0\\n    -fogSource \\\"fragment\\\" \\n    -fogMode \\\"linear\\\" \\n    -fogStart 0\\n    -fogEnd 100\\n    -fogDensity 0.1\\n    -fogColor 0.5 0.5 0.5 1 \\n    -maxConstantTransparency 1\\n    -rendererName \\\"base_OpenGL_Renderer\\\" \\n    -colorResolution 256 256 \\n    -bumpResolution 512 512 \\n    -textureCompression 0\\n    -transparencyAlgorithm \\\"frontAndBackCull\\\" \\n    -transpInShadows 0\\n    -cullingOverride \\\"none\\\" \\n    -lowQualityLighting 0\\n    -maximumNumHardwareLights 1\\n    -occlusionCulling 0\\n    -useBaseRenderer 0\\n    -useReducedRenderer 0\\n    -smallObjectCulling 0\\n    -smallObjectThreshold -1 \\n    -interactiveDisableShadows 0\\n    -interactiveBackFaceCull 0\\n    -sortTransparent 1\\n    -nurbsCurves 1\\n    -nurbsSurfaces 1\\n    -polymeshes 1\\n    -subdivSurfaces 1\\n    -planes 1\\n    -lights 1\\n    -cameras 1\\n    -controlVertices 1\\n    -hulls 1\\n    -grid 1\\n    -joints 1\\n    -ikHandles 1\\n    -deformers 1\\n    -dynamics 1\\n    -fluids 1\\n    -hairSystems 1\\n    -follicles 1\\n    -locators 1\\n    -dimensions 1\\n    -handles 1\\n    -pivots 1\\n    -textures 1\\n    -strokes 1\\n    -shadows 0\\n    $editorName;\\nmodelEditor -e -viewSelected 0 $editorName\"\n"
		+ "\t\t\t\t$configName;\n\n            setNamedPanelLayout \"Current Layout\";\n        }\n\n        panelHistory -e -clear mainPanelHistory;\n        setFocus `paneLayout -q -p1 $gMainPane`;\n        sceneUIReplacement -deleteRemaining;\n        sceneUIReplacement -clear;\n\t}\n\n\ngrid -spacing 0.5 -size 200 -divisions 8 -displayAxes yes -displayGridLines yes -displayDivisionLines yes -displayPerspectiveLabels no -displayOrthographicLabels no -displayAxesBold yes -perspectiveLabelPosition axis -orthographicLabelPosition edge;\n}\n");
	setAttr ".st" 3;
createNode script -n "sceneConfigurationScriptNode";
	setAttr ".b" -type "string" "playbackOptions -min 0 -max 150 -ast 0 -aet 500 ";
	setAttr ".st" 6;
createNode shadingEngine -n "anisotropic1SG";
	setAttr ".ihi" 0;
	setAttr ".ro" yes;
createNode materialInfo -n "materialInfo1";
createNode shadingEngine -n "blinn1SG";
	setAttr ".ihi" 0;
	setAttr ".ro" yes;
createNode materialInfo -n "materialInfo2";
createNode shadingEngine -n "blinn2SG";
	setAttr ".ihi" 0;
	setAttr ".ro" yes;
createNode materialInfo -n "materialInfo3";
createNode shadingEngine -n "lambert2SG";
	setAttr ".ihi" 0;
	setAttr ".ro" yes;
createNode materialInfo -n "materialInfo4";
createNode shadingEngine -n "lambert3SG";
	setAttr ".ihi" 0;
	setAttr ".ro" yes;
createNode materialInfo -n "materialInfo5";
createNode shadingEngine -n "lambert6SG";
	setAttr ".ihi" 0;
	setAttr ".ro" yes;
createNode materialInfo -n "materialInfo6";
createNode shadingEngine -n "lambert5SG";
	setAttr ".ihi" 0;
	setAttr ".ro" yes;
createNode materialInfo -n "materialInfo7";
createNode shadingEngine -n "lambert4SG";
	setAttr ".ihi" 0;
	setAttr ".ro" yes;
createNode materialInfo -n "materialInfo8";
createNode shadingEngine -n "greySG";
	setAttr ".ihi" 0;
	setAttr ".ro" yes;
createNode materialInfo -n "materialInfo9";
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
createNode surfaceShader -n "surfaceShader1";
	setAttr ".oc" -type "float3" 0.96078432 0.96078432 0.96078432 ;
createNode shadingEngine -n "surfaceShader1SG";
	setAttr ".ihi" 0;
	setAttr -s 3 ".dsm";
	setAttr ".ro" yes;
createNode materialInfo -n "materialInfo10";
createNode shadingEngine -n "surfaceSG";
	setAttr ".ihi" 0;
	setAttr ".ro" yes;
createNode materialInfo -n "materialInfo11";
createNode shadingEngine -n "lambert7SG";
	setAttr ".ihi" 0;
	setAttr ".ro" yes;
createNode materialInfo -n "materialInfo12";
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
createNode materialInfo -n "prefix_materialInfo5";
createNode shadingEngine -n "prefix_lambert3SG";
	setAttr ".ihi" 0;
	setAttr ".ro" yes;
createNode materialInfo -n "prefix_materialInfo11";
createNode shadingEngine -n "prefix_surfaceSG";
	setAttr ".ihi" 0;
	setAttr ".ro" yes;
createNode materialInfo -n "prefix_materialInfo12";
createNode shadingEngine -n "prefix_lambert3SG1";
	setAttr ".ihi" 0;
	setAttr ".ro" yes;
createNode materialInfo -n "prefix_materialInfo13";
createNode shadingEngine -n "prefix_surfaceSG1";
	setAttr ".ihi" 0;
	setAttr ".ro" yes;
createNode shadingEngine -n "surfaceSG1";
	setAttr ".ihi" 0;
	setAttr ".ro" yes;
createNode materialInfo -n "materialInfo13";
createNode makeNurbCircle -n "makeNurbCircle1";
	setAttr ".nr" -type "double3" 0 1 0 ;
	setAttr ".s" 6;
createNode detachCurve -n "detachCurve1";
	setAttr -s 2 ".oc";
	setAttr -s 2 ".p[0:1]"  0 3;
	setAttr -s 3 ".k[0:2]" yes yes yes;
createNode makeNurbCircle -n "makeNurbCircle2";
	setAttr ".nr" -type "double3" 0 1 0 ;
	setAttr ".s" 6;
createNode makeNurbPlane -n "makeNurbPlane1";
	setAttr ".ax" -type "double3" 0 1 0 ;
	setAttr ".u" 6;
	setAttr ".v" 6;
createNode projectCurve -n "projectCurve1";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve2";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve3";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve4";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve5";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve6";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve7";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve8";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve9";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve10";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve11";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve12";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve13";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve14";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve15";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve16";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve17";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve18";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve19";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve20";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve37";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve38";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve39";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve40";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve41";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve42";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve43";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve44";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve45";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve46";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve47";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve48";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode makeNurbPlane -n "makeNurbPlane2";
	setAttr ".ax" -type "double3" 0 1 0 ;
	setAttr ".u" 6;
	setAttr ".v" 6;
createNode projectCurve -n "projectCurve49";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve50";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve51";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve52";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve53";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve54";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve55";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve56";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve57";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode projectCurve -n "projectCurve58";
	setAttr ".d" -type "double3" 0 -1 -1.7932703932910243e-16 ;
createNode trim -n "trim1";
	setAttr -s 10 ".ic";
	setAttr -s 3 ".lu[0:2]"  0.4797743717757374 0.66701817107778782 0.34101334193582505;
	setAttr -s 3 ".lv[0:2]"  0.26054530279613558 0.49794369119694948 0.47788185555744406;
createNode trim -n "trim2";
	setAttr -s 10 ".ic";
	setAttr ".lu[0]"  0.76732734927531476;
	setAttr ".lv[0]"  0.41602452900230241;
createNode trim -n "trim4";
	setAttr -s 12 ".ic";
	setAttr -s 2 ".lu[0:1]"  0.5550062554238826 0.39451157030783945;
	setAttr -s 2 ".lv[0:1]"  0.39261905408954612 0.61329924612410547;
createNode trim -n "trim6";
	setAttr -s 20 ".ic";
	setAttr -s 4 ".lu[0:3]"  0.75729643145556202 0.52658532160124993 0.52156986269137362 
		0.26243781901442892;
	setAttr -s 4 ".lv[0:3]"  0.46116365919118957 0.57986285339159649 0.4110090700924261 
		0.46116365919118957;
createNode surfaceShader -n "surfaceShader2";
	setAttr ".oc" -type "float3" 0.44999999 0.44999999 0.44999999 ;
createNode shadingEngine -n "surfaceShader2SG";
	setAttr ".ihi" 0;
	setAttr -s 4 ".dsm";
	setAttr ".ro" yes;
createNode materialInfo -n "materialInfo14";
createNode animCurveTU -n "None_visibility";
	setAttr ".tan" 9;
	setAttr ".wgt" no;
	setAttr -s 3 ".ktv[0:2]"  0 0 1 1 2 0;
	setAttr -s 3 ".kot[0:2]"  5 5 5;
createNode animCurveTU -n "OneToOne_visibility";
	setAttr ".tan" 9;
	setAttr ".wgt" no;
	setAttr -s 4 ".ktv[0:3]"  0 0 1 0 2 1 3 0;
	setAttr -s 4 ".kot[0:3]"  5 5 5 5;
createNode animCurveTU -n "All_visibility";
	setAttr ".tan" 9;
	setAttr ".wgt" no;
	setAttr -s 5 ".ktv[0:4]"  0 0 1 0 2 0 3 1 4 0;
	setAttr -s 5 ".kot[0:4]"  5 5 5 5 5;
createNode animCurveTU -n "BG_visibility";
	setAttr ".tan" 9;
	setAttr ".wgt" no;
	setAttr -s 2 ".ktv[0:1]"  0 1 1 0;
	setAttr -s 2 ".kot[0:1]"  5 5;
createNode surfaceShader -n "surfaceShader3";
	setAttr ".oc" -type "float3" 0.79607844 0.79607844 0.79607844 ;
createNode trim -n "trim8";
	setAttr -s 12 ".ic";
	setAttr -s 3 ".lu[0:2]"  0.45786477724302754 0.3234448030862403 0.66785014967121825;
	setAttr -s 3 ".lv[0:2]"  0.73761502669505807 0.52354069292183469 0.50414444814003023;
createNode trim -n "trim9";
	setAttr -s 12 ".ic";
	setAttr ".lu[0]"  0.4203770680585005;
	setAttr ".lv[0]"  0.5;
createNode shadingEngine -n "surfaceShader3SG";
	setAttr ".ihi" 0;
	setAttr -s 2 ".dsm";
	setAttr ".ro" yes;
createNode materialInfo -n "materialInfo15";
createNode trim -n "trim10";
	setAttr -s 20 ".ic";
	setAttr -s 5 ".lu[0:4]"  0.33175328294100553 0.45786477724302754 0.67517045027129885 
		0.66824671705899452 0.34283125608069243;
	setAttr -s 5 ".lv[0:4]"  0.6329356776762427 0.73208909584168469 0.62047295789409496 
		0.36291008239637496 0.36706432232375757;
createNode trim -n "trim11";
	setAttr -s 20 ".ic";
	setAttr -s 12 ".lu[0:11]"  0.47134375531271622 0.49725373880071144 0.52954960100008241 
		0.48104956969171969 0.54129556870582052 0.38539407711552459 0.40746533212082464 0.61768005162140061 
		0.6147320028328539 0.60843756935636806 0.3851155277847802 0.39731923950848469;
	setAttr -s 12 ".lv[0:11]"  0.52413120450646589 0.50619511720259658 0.47384345258517085 
		0.46907544973154852 0.52152453336718474 0.60760559862638475 0.61642266266301649 0.60691888855084652 
		0.39298982421248047 0.3870417865272216 0.39345716603994957 0.40820685898276238;
select -ne :time1;
	setAttr ".o" 3;
select -ne :renderPartition;
	setAttr -s 21 ".st";
select -ne :renderGlobalsList1;
select -ne :defaultShaderList1;
	setAttr -s 5 ".s";
select -ne :postProcessList1;
	setAttr -s 2 ".p";
select -ne :lightList1;
select -ne :lambert1;
	setAttr ".miic" -type "float3" 3.1415927 3.1415927 3.1415927 ;
select -ne :initialShadingGroup;
	setAttr -s 3 ".dsm";
	setAttr ".ro" yes;
select -ne :initialParticleSE;
	setAttr ".ro" yes;
select -ne :defaultRenderGlobals;
	setAttr ".outf" 5;
	setAttr ".an" yes;
	setAttr ".fs" 0;
	setAttr ".ef" 3;
	setAttr ".ep" 4;
	setAttr ".pff" yes;
	setAttr ".comp" yes;
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
select -ne :hardwareRenderGlobals;
	setAttr ".enpt" no;
	setAttr ".hgcd" no;
	setAttr ".hgci" no;
select -ne :defaultHardwareRenderGlobals;
	setAttr ".fn" -type "string" "im";
	setAttr ".res" -type "string" "ntsc_4d 646 485 1.333";
connectAttr "detachCurve1.oc[0]" "|group1|nurbsCircle1|nurbsCircleShape1.cr";
connectAttr "detachCurve1.oc[1]" "|group1|detachedCurve1|detachedCurveShape1.cr"
		;
connectAttr "detachCurve1.oc[0]" "|group2|nurbsCircle1|nurbsCircleShape1.cr";
connectAttr "detachCurve1.oc[1]" "|group2|detachedCurve1|detachedCurveShape1.cr"
		;
connectAttr "makeNurbCircle2.oc" "nurbsCircleShape2.cr";
connectAttr "makeNurbCircle2.oc" "nurbsCircleShape3.cr";
connectAttr "makeNurbCircle2.oc" "nurbsCircleShape4.cr";
connectAttr "makeNurbCircle2.oc" "nurbsCircleShape5.cr";
connectAttr "makeNurbCircle2.oc" "nurbsCircleShape6.cr";
connectAttr "makeNurbCircle2.oc" "nurbsCircleShape7.cr";
connectAttr "None_visibility.o" "None.v";
connectAttr "makeNurbPlane2.os" "nurbsPlaneShape4.cr";
connectAttr "projectCurve49.oc" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve49.cr"
		;
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve49.l[0]" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve49|projectionCurve49_1|projectionCurve49_Shape1.cr"
		;
connectAttr "projectCurve50.oc" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve50.cr"
		;
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve50.l[0]" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve50|projectionCurve50_1|projectionCurve50_Shape1.cr"
		;
connectAttr "projectCurve51.oc" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve51.cr"
		;
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve51.l[0]" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve51|projectionCurve51_1|projectionCurve51_Shape1.cr"
		;
connectAttr "projectCurve52.oc" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve52.cr"
		;
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve52.l[0]" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve52|projectionCurve52_1|projectionCurve52_Shape1.cr"
		;
connectAttr "projectCurve53.oc" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve53.cr"
		;
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve53.l[0]" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve53|projectionCurve53_1|projectionCurve53_Shape1.cr"
		;
connectAttr "projectCurve54.oc" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve54.cr"
		;
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve54.l[0]" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve54|projectionCurve54_1|projectionCurve54_Shape1.cr"
		;
connectAttr "projectCurve55.oc" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve55.cr"
		;
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve55.l[0]" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve55|projectionCurve55_1|projectionCurve55_Shape1.cr"
		;
connectAttr "projectCurve56.oc" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve56.cr"
		;
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve56.l[0]" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve56|projectionCurve56_1|projectionCurve56_Shape1.cr"
		;
connectAttr "projectCurve57.oc" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve57.cr"
		;
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve57.l[0]" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve57|projectionCurve57_1|projectionCurve57_Shape1.cr"
		;
connectAttr "projectCurve58.oc" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve58.cr"
		;
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve58.l[0]" "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve58|projectionCurve58_1|projectionCurve58_Shape1.cr"
		;
connectAttr "trim2.os" "nurbsPlane4trimmedSurfaceShape1.cr";
connectAttr "trim1.os" "nurbsPlaneShape5.cr";
connectAttr "projectCurve49.oc" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve49.cr"
		;
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve49.l[0]" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve49|projectionCurve49_1|projectionCurve49_Shape1.cr"
		;
connectAttr "projectCurve50.oc" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve50.cr"
		;
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve50.l[0]" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve50|projectionCurve50_1|projectionCurve50_Shape1.cr"
		;
connectAttr "projectCurve51.oc" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve51.cr"
		;
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve51.l[0]" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve51|projectionCurve51_1|projectionCurve51_Shape1.cr"
		;
connectAttr "projectCurve52.oc" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve52.cr"
		;
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve52.l[0]" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve52|projectionCurve52_1|projectionCurve52_Shape1.cr"
		;
connectAttr "projectCurve53.oc" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve53.cr"
		;
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve53.l[0]" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve53|projectionCurve53_1|projectionCurve53_Shape1.cr"
		;
connectAttr "projectCurve54.oc" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve54.cr"
		;
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve54.l[0]" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve54|projectionCurve54_1|projectionCurve54_Shape1.cr"
		;
connectAttr "projectCurve55.oc" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve55.cr"
		;
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve55.l[0]" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve55|projectionCurve55_1|projectionCurve55_Shape1.cr"
		;
connectAttr "projectCurve56.oc" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve56.cr"
		;
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve56.l[0]" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve56|projectionCurve56_1|projectionCurve56_Shape1.cr"
		;
connectAttr "projectCurve57.oc" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve57.cr"
		;
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve57.l[0]" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve57|projectionCurve57_1|projectionCurve57_Shape1.cr"
		;
connectAttr "projectCurve58.oc" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve58.cr"
		;
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve58.l[0]" "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve58|projectionCurve58_1|projectionCurve58_Shape1.cr"
		;
connectAttr "makeNurbPlane2.os" "nurbsPlaneShape5Original.cr";
connectAttr "OneToOne_visibility.o" "OneToOne.v";
connectAttr "makeNurbPlane1.os" "nurbsPlaneShape3.cr";
connectAttr "projectCurve37.oc" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve37.cr"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve37.l[0]" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve37|projectionCurve37_1|projectionCurve37_Shape1.cr"
		;
connectAttr "projectCurve38.oc" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve38.cr"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve38.l[0]" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve38|projectionCurve38_1|projectionCurve38_Shape1.cr"
		;
connectAttr "projectCurve39.oc" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve39.cr"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve39.l[0]" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve39|projectionCurve39_1|projectionCurve39_Shape1.cr"
		;
connectAttr "projectCurve40.oc" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve40.cr"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve40.l[0]" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve40|projectionCurve40_1|projectionCurve40_Shape1.cr"
		;
connectAttr "projectCurve41.oc" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve41.cr"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve41.l[0]" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve41|projectionCurve41_1|projectionCurve41_Shape1.cr"
		;
connectAttr "projectCurve42.oc" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve42.cr"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve42.l[0]" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve42|projectionCurve42_1|projectionCurve42_Shape1.cr"
		;
connectAttr "projectCurve43.oc" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve43.cr"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve43.l[0]" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve43|projectionCurve43_1|projectionCurve43_Shape1.cr"
		;
connectAttr "projectCurve44.oc" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve44.cr"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve44.l[0]" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve44|projectionCurve44_1|projectionCurve44_Shape1.cr"
		;
connectAttr "projectCurve45.oc" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve45.cr"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve45.l[0]" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve45|projectionCurve45_1|projectionCurve45_Shape1.cr"
		;
connectAttr "projectCurve46.oc" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve46.cr"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve46.l[0]" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve46|projectionCurve46_1|projectionCurve46_Shape1.cr"
		;
connectAttr "projectCurve47.oc" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve47.cr"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve47.l[0]" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve47|projectionCurve47_1|projectionCurve47_Shape1.cr"
		;
connectAttr "projectCurve48.oc" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve48.cr"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve48.l[0]" "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve48|projectionCurve48_1|projectionCurve48_Shape1.cr"
		;
connectAttr "trim4.os" "nurbsPlane3trimmedSurfaceShape1.cr";
connectAttr "trim8.os" "nurbsPlaneShape6.cr";
connectAttr "projectCurve37.oc" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve37.cr"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve37.l[0]" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve37|projectionCurve37_1|projectionCurve37_Shape1.cr"
		;
connectAttr "projectCurve38.oc" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve38.cr"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve38.l[0]" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve38|projectionCurve38_1|projectionCurve38_Shape1.cr"
		;
connectAttr "projectCurve39.oc" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve39.cr"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve39.l[0]" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve39|projectionCurve39_1|projectionCurve39_Shape1.cr"
		;
connectAttr "projectCurve40.oc" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve40.cr"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve40.l[0]" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve40|projectionCurve40_1|projectionCurve40_Shape1.cr"
		;
connectAttr "projectCurve41.oc" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve41.cr"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve41.l[0]" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve41|projectionCurve41_1|projectionCurve41_Shape1.cr"
		;
connectAttr "projectCurve42.oc" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve42.cr"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve42.l[0]" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve42|projectionCurve42_1|projectionCurve42_Shape1.cr"
		;
connectAttr "projectCurve43.oc" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve43.cr"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve43.l[0]" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve43|projectionCurve43_1|projectionCurve43_Shape1.cr"
		;
connectAttr "projectCurve44.oc" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve44.cr"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve44.l[0]" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve44|projectionCurve44_1|projectionCurve44_Shape1.cr"
		;
connectAttr "projectCurve45.oc" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve45.cr"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve45.l[0]" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve45|projectionCurve45_1|projectionCurve45_Shape1.cr"
		;
connectAttr "projectCurve46.oc" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve46.cr"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve46.l[0]" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve46|projectionCurve46_1|projectionCurve46_Shape1.cr"
		;
connectAttr "projectCurve47.oc" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve47.cr"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve47.l[0]" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve47|projectionCurve47_1|projectionCurve47_Shape1.cr"
		;
connectAttr "projectCurve48.oc" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve48.cr"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve48.l[0]" "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve48|projectionCurve48_1|projectionCurve48_Shape1.cr"
		;
connectAttr "makeNurbPlane1.os" "nurbsPlaneShape6Original.cr";
connectAttr "trim9.os" "nurbsPlaneShape8.cr";
connectAttr "All_visibility.o" "All.v";
connectAttr "makeNurbPlane1.os" "nurbsPlaneShape1.cr";
connectAttr "projectCurve1.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve1.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve1.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve1|projectionCurve1_1|projectionCurve1_Shape1.cr"
		;
connectAttr "projectCurve2.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve2.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve2.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve2|projectionCurve2_1|projectionCurve2_Shape1.cr"
		;
connectAttr "projectCurve3.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve3.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve3.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve3|projectionCurve3_1|projectionCurve3_Shape1.cr"
		;
connectAttr "projectCurve4.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve4.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve4.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve4|projectionCurve4_1|projectionCurve4_Shape1.cr"
		;
connectAttr "projectCurve5.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve5.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve5.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve5|projectionCurve5_1|projectionCurve5_Shape1.cr"
		;
connectAttr "projectCurve6.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve6.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve6.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve6|projectionCurve6_1|projectionCurve6_Shape1.cr"
		;
connectAttr "projectCurve7.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve7.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve7.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve7|projectionCurve7_1|projectionCurve7_Shape1.cr"
		;
connectAttr "projectCurve8.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve8.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve8.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve8|projectionCurve8_1|projectionCurve8_Shape1.cr"
		;
connectAttr "projectCurve9.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve9.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve9.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve9|projectionCurve9_1|projectionCurve9_Shape1.cr"
		;
connectAttr "projectCurve10.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve10.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve10.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve10|projectionCurve10_1|projectionCurve10_Shape1.cr"
		;
connectAttr "projectCurve11.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve11.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve11.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve11|projectionCurve11_1|projectionCurve11_Shape1.cr"
		;
connectAttr "projectCurve12.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve12.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve12.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve12|projectionCurve12_1|projectionCurve12_Shape1.cr"
		;
connectAttr "projectCurve13.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve13.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve13.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve13|projectionCurve13_1|projectionCurve13_Shape1.cr"
		;
connectAttr "projectCurve14.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve14.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve14.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve14|projectionCurve14_1|projectionCurve14_Shape1.cr"
		;
connectAttr "projectCurve15.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve15.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve15.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve15|projectionCurve15_1|projectionCurve15_Shape1.cr"
		;
connectAttr "projectCurve16.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve16.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve16.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve16|projectionCurve16_1|projectionCurve16_Shape1.cr"
		;
connectAttr "projectCurve17.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve17.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve17.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve17|projectionCurve17_1|projectionCurve17_Shape1.cr"
		;
connectAttr "projectCurve18.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve18.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve18.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve18|projectionCurve18_1|projectionCurve18_Shape1.cr"
		;
connectAttr "projectCurve19.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve19.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve19.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve19|projectionCurve19_1|projectionCurve19_Shape1.cr"
		;
connectAttr "projectCurve20.oc" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve20.cr"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve20.l[0]" "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve20|projectionCurve20_1|projectionCurve20_Shape1.cr"
		;
connectAttr "trim6.os" "nurbsPlane1trimmedSurfaceShape1.cr";
connectAttr "trim10.os" "nurbsPlaneShape7.cr";
connectAttr "projectCurve1.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve1.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve1.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve1|projectionCurve1_1|projectionCurve1_Shape1.cr"
		;
connectAttr "projectCurve2.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve2.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve2.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve2|projectionCurve2_1|projectionCurve2_Shape1.cr"
		;
connectAttr "projectCurve3.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve3.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve3.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve3|projectionCurve3_1|projectionCurve3_Shape1.cr"
		;
connectAttr "projectCurve4.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve4.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve4.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve4|projectionCurve4_1|projectionCurve4_Shape1.cr"
		;
connectAttr "projectCurve5.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve5.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve5.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve5|projectionCurve5_1|projectionCurve5_Shape1.cr"
		;
connectAttr "projectCurve6.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve6.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve6.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve6|projectionCurve6_1|projectionCurve6_Shape1.cr"
		;
connectAttr "projectCurve7.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve7.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve7.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve7|projectionCurve7_1|projectionCurve7_Shape1.cr"
		;
connectAttr "projectCurve8.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve8.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve8.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve8|projectionCurve8_1|projectionCurve8_Shape1.cr"
		;
connectAttr "projectCurve9.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve9.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve9.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve9|projectionCurve9_1|projectionCurve9_Shape1.cr"
		;
connectAttr "projectCurve10.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve10.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve10.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve10|projectionCurve10_1|projectionCurve10_Shape1.cr"
		;
connectAttr "projectCurve11.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve11.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve11.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve11|projectionCurve11_1|projectionCurve11_Shape1.cr"
		;
connectAttr "projectCurve12.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve12.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve12.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve12|projectionCurve12_1|projectionCurve12_Shape1.cr"
		;
connectAttr "projectCurve13.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve13.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve13.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve13|projectionCurve13_1|projectionCurve13_Shape1.cr"
		;
connectAttr "projectCurve14.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve14.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve14.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve14|projectionCurve14_1|projectionCurve14_Shape1.cr"
		;
connectAttr "projectCurve15.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve15.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve15.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve15|projectionCurve15_1|projectionCurve15_Shape1.cr"
		;
connectAttr "projectCurve16.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve16.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve16.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve16|projectionCurve16_1|projectionCurve16_Shape1.cr"
		;
connectAttr "projectCurve17.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve17.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve17.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve17|projectionCurve17_1|projectionCurve17_Shape1.cr"
		;
connectAttr "projectCurve18.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve18.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve18.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve18|projectionCurve18_1|projectionCurve18_Shape1.cr"
		;
connectAttr "projectCurve19.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve19.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve19.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve19|projectionCurve19_1|projectionCurve19_Shape1.cr"
		;
connectAttr "projectCurve20.oc" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve20.cr"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve20.l[0]" "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve20|projectionCurve20_1|projectionCurve20_Shape1.cr"
		;
connectAttr "makeNurbPlane1.os" "nurbsPlaneShape7Original.cr";
connectAttr "trim11.os" "nurbsPlaneShape9.cr";
connectAttr "BG_visibility.o" "BG.v";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[0].llnk";
connectAttr ":initialShadingGroup.msg" "lightLinker1.lnk[0].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[1].llnk";
connectAttr ":initialParticleSE.msg" "lightLinker1.lnk[1].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[2].llnk";
connectAttr "anisotropic1SG.msg" "lightLinker1.lnk[2].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[3].llnk";
connectAttr "blinn1SG.msg" "lightLinker1.lnk[3].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[4].llnk";
connectAttr "blinn2SG.msg" "lightLinker1.lnk[4].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[5].llnk";
connectAttr "lambert2SG.msg" "lightLinker1.lnk[5].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[6].llnk";
connectAttr "lambert3SG.msg" "lightLinker1.lnk[6].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[7].llnk";
connectAttr "lambert6SG.msg" "lightLinker1.lnk[7].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[8].llnk";
connectAttr "lambert5SG.msg" "lightLinker1.lnk[8].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[9].llnk";
connectAttr "lambert4SG.msg" "lightLinker1.lnk[9].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[10].llnk";
connectAttr "greySG.msg" "lightLinker1.lnk[10].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[11].llnk";
connectAttr "surfaceShader1SG.msg" "lightLinker1.lnk[11].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[12].llnk";
connectAttr "surfaceSG.msg" "lightLinker1.lnk[12].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[13].llnk";
connectAttr "lambert7SG.msg" "lightLinker1.lnk[13].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[14].llnk";
connectAttr "prefix_lambert3SG.msg" "lightLinker1.lnk[14].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[15].llnk";
connectAttr "prefix_surfaceSG.msg" "lightLinker1.lnk[15].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[16].llnk";
connectAttr "prefix_lambert3SG1.msg" "lightLinker1.lnk[16].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[17].llnk";
connectAttr "prefix_surfaceSG1.msg" "lightLinker1.lnk[17].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[18].llnk";
connectAttr "surfaceSG1.msg" "lightLinker1.lnk[18].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[19].llnk";
connectAttr "surfaceShader2SG.msg" "lightLinker1.lnk[19].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[20].llnk";
connectAttr "surfaceShader3SG.msg" "lightLinker1.lnk[20].olnk";
connectAttr "layerManager.dli[0]" "defaultLayer.id";
connectAttr "renderLayerManager.rlmi[0]" "defaultRenderLayer.rlid";
connectAttr "anisotropic1SG.msg" "materialInfo1.sg";
connectAttr "blinn1SG.msg" "materialInfo2.sg";
connectAttr "blinn2SG.msg" "materialInfo3.sg";
connectAttr "lambert2SG.msg" "materialInfo4.sg";
connectAttr "lambert3SG.msg" "materialInfo5.sg";
connectAttr "lambert6SG.msg" "materialInfo6.sg";
connectAttr "lambert5SG.msg" "materialInfo7.sg";
connectAttr "lambert4SG.msg" "materialInfo8.sg";
connectAttr "greySG.msg" "materialInfo9.sg";
connectAttr "surfaceShader1.oc" "surfaceShader1SG.ss";
connectAttr "nurbsPlaneShape5.iog" "surfaceShader1SG.dsm" -na;
connectAttr "nurbsPlaneShape6.iog" "surfaceShader1SG.dsm" -na;
connectAttr "nurbsPlaneShape7.iog" "surfaceShader1SG.dsm" -na;
connectAttr "surfaceShader1SG.msg" "materialInfo10.sg";
connectAttr "surfaceShader1.msg" "materialInfo10.m";
connectAttr "surfaceShader1.msg" "materialInfo10.t" -na;
connectAttr "surfaceSG.msg" "materialInfo11.sg";
connectAttr "lambert7SG.msg" "materialInfo12.sg";
connectAttr "prefix_lambert3SG.msg" "prefix_materialInfo5.sg";
connectAttr "prefix_surfaceSG.msg" "prefix_materialInfo11.sg";
connectAttr "prefix_lambert3SG1.msg" "prefix_materialInfo12.sg";
connectAttr "prefix_surfaceSG1.msg" "prefix_materialInfo13.sg";
connectAttr "surfaceSG1.msg" "materialInfo13.sg";
connectAttr "makeNurbCircle1.oc" "detachCurve1.ic";
connectAttr "|group1|detachedCurve1|detachedCurveShape1.ws" "projectCurve1.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve1.is";
connectAttr "|group2|detachedCurve1|detachedCurveShape1.ws" "projectCurve2.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve2.is";
connectAttr "|group1|curve1|curveShape1.ws" "projectCurve3.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve3.is";
connectAttr "|group2|curve1|curveShape1.ws" "projectCurve4.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve4.is";
connectAttr "|group1|nurbsCircle1|nurbsCircleShape1.ws" "projectCurve5.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve5.is";
connectAttr "|group2|nurbsCircle1|nurbsCircleShape1.ws" "projectCurve6.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve6.is";
connectAttr "|group1|curve2|curveShape2.ws" "projectCurve7.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve7.is";
connectAttr "|group2|curve2|curveShape2.ws" "projectCurve8.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve8.is";
connectAttr "nurbsCircleShape5.ws" "projectCurve9.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve9.is";
connectAttr "nurbsCircleShape4.ws" "projectCurve10.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve10.is";
connectAttr "curveShape8.ws" "projectCurve11.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve11.is";
connectAttr "curveShape7.ws" "projectCurve12.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve12.is";
connectAttr "|group4|curve10|curveShape10.ws" "projectCurve13.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve13.is";
connectAttr "|group4|curve9|curveShape9.ws" "projectCurve14.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve14.is";
connectAttr "|group3|curve10|curveShape10.ws" "projectCurve15.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve15.is";
connectAttr "|group3|curve9|curveShape9.ws" "projectCurve16.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve16.is";
connectAttr "nurbsCircleShape2.ws" "projectCurve17.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve17.is";
connectAttr "curveShape4.ws" "projectCurve18.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve18.is";
connectAttr "curveShape3.ws" "projectCurve19.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve19.is";
connectAttr "nurbsCircleShape3.ws" "projectCurve20.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve20.is";
connectAttr "|group1|detachedCurve1|detachedCurveShape1.ws" "projectCurve37.ic";
connectAttr "nurbsPlaneShape3.ws" "projectCurve37.is";
connectAttr "|group1|curve2|curveShape2.ws" "projectCurve38.ic";
connectAttr "nurbsPlaneShape3.ws" "projectCurve38.is";
connectAttr "|group2|detachedCurve1|detachedCurveShape1.ws" "projectCurve39.ic";
connectAttr "nurbsPlaneShape3.ws" "projectCurve39.is";
connectAttr "|group2|curve2|curveShape2.ws" "projectCurve40.ic";
connectAttr "nurbsPlaneShape3.ws" "projectCurve40.is";
connectAttr "|group1|curve1|curveShape1.ws" "projectCurve41.ic";
connectAttr "nurbsPlaneShape3.ws" "projectCurve41.is";
connectAttr "|group2|curve1|curveShape1.ws" "projectCurve42.ic";
connectAttr "nurbsPlaneShape3.ws" "projectCurve42.is";
connectAttr "|group1|nurbsCircle1|nurbsCircleShape1.ws" "projectCurve43.ic";
connectAttr "nurbsPlaneShape3.ws" "projectCurve43.is";
connectAttr "|group2|nurbsCircle1|nurbsCircleShape1.ws" "projectCurve44.ic";
connectAttr "nurbsPlaneShape3.ws" "projectCurve44.is";
connectAttr "nurbsCircleShape6.ws" "projectCurve45.ic";
connectAttr "nurbsPlaneShape3.ws" "projectCurve45.is";
connectAttr "curveShape5.ws" "projectCurve46.ic";
connectAttr "nurbsPlaneShape3.ws" "projectCurve46.is";
connectAttr "curveShape6.ws" "projectCurve47.ic";
connectAttr "nurbsPlaneShape3.ws" "projectCurve47.is";
connectAttr "nurbsCircleShape7.ws" "projectCurve48.ic";
connectAttr "nurbsPlaneShape3.ws" "projectCurve48.is";
connectAttr "|group1|detachedCurve1|detachedCurveShape1.ws" "projectCurve49.ic";
connectAttr "nurbsPlaneShape4.ws" "projectCurve49.is";
connectAttr "|group2|detachedCurve1|detachedCurveShape1.ws" "projectCurve50.ic";
connectAttr "nurbsPlaneShape4.ws" "projectCurve50.is";
connectAttr "|group1|curve2|curveShape2.ws" "projectCurve51.ic";
connectAttr "nurbsPlaneShape4.ws" "projectCurve51.is";
connectAttr "|group2|curve2|curveShape2.ws" "projectCurve52.ic";
connectAttr "nurbsPlaneShape4.ws" "projectCurve52.is";
connectAttr "|group1|nurbsCircle1|nurbsCircleShape1.ws" "projectCurve53.ic";
connectAttr "nurbsPlaneShape4.ws" "projectCurve53.is";
connectAttr "|group2|nurbsCircle1|nurbsCircleShape1.ws" "projectCurve54.ic";
connectAttr "nurbsPlaneShape4.ws" "projectCurve54.is";
connectAttr "|group1|curve1|curveShape1.ws" "projectCurve55.ic";
connectAttr "nurbsPlaneShape4.ws" "projectCurve55.is";
connectAttr "|group2|curve1|curveShape1.ws" "projectCurve56.ic";
connectAttr "nurbsPlaneShape4.ws" "projectCurve56.is";
connectAttr "nurbsCircleShape6.ws" "projectCurve57.ic";
connectAttr "nurbsPlaneShape4.ws" "projectCurve57.is";
connectAttr "nurbsCircleShape7.ws" "projectCurve58.ic";
connectAttr "nurbsPlaneShape4.ws" "projectCurve58.is";
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve49|projectionCurve49_1|projectionCurve49_Shape1.ws" "trim1.ic[0]"
		;
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve50|projectionCurve50_1|projectionCurve50_Shape1.ws" "trim1.ic[1]"
		;
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve51|projectionCurve51_1|projectionCurve51_Shape1.ws" "trim1.ic[2]"
		;
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve52|projectionCurve52_1|projectionCurve52_Shape1.ws" "trim1.ic[3]"
		;
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve53|projectionCurve53_1|projectionCurve53_Shape1.ws" "trim1.ic[4]"
		;
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve54|projectionCurve54_1|projectionCurve54_Shape1.ws" "trim1.ic[5]"
		;
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve55|projectionCurve55_1|projectionCurve55_Shape1.ws" "trim1.ic[6]"
		;
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve56|projectionCurve56_1|projectionCurve56_Shape1.ws" "trim1.ic[7]"
		;
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve57|projectionCurve57_1|projectionCurve57_Shape1.ws" "trim1.ic[8]"
		;
connectAttr "|None|nurbsPlane5|nurbsPlaneShape5->|projectionCurve58|projectionCurve58_1|projectionCurve58_Shape1.ws" "trim1.ic[9]"
		;
connectAttr "nurbsPlaneShape5Original.l" "trim1.is";
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve49|projectionCurve49_1|projectionCurve49_Shape1.ws" "trim2.ic[0]"
		;
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve50|projectionCurve50_1|projectionCurve50_Shape1.ws" "trim2.ic[1]"
		;
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve51|projectionCurve51_1|projectionCurve51_Shape1.ws" "trim2.ic[2]"
		;
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve52|projectionCurve52_1|projectionCurve52_Shape1.ws" "trim2.ic[3]"
		;
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve53|projectionCurve53_1|projectionCurve53_Shape1.ws" "trim2.ic[4]"
		;
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve54|projectionCurve54_1|projectionCurve54_Shape1.ws" "trim2.ic[5]"
		;
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve55|projectionCurve55_1|projectionCurve55_Shape1.ws" "trim2.ic[6]"
		;
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve56|projectionCurve56_1|projectionCurve56_Shape1.ws" "trim2.ic[7]"
		;
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve57|projectionCurve57_1|projectionCurve57_Shape1.ws" "trim2.ic[8]"
		;
connectAttr "|None|nurbsPlane4|nurbsPlaneShape4->|projectionCurve58|projectionCurve58_1|projectionCurve58_Shape1.ws" "trim2.ic[9]"
		;
connectAttr "nurbsPlaneShape4.l" "trim2.is";
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve37|projectionCurve37_1|projectionCurve37_Shape1.ws" "trim4.ic[0]"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve38|projectionCurve38_1|projectionCurve38_Shape1.ws" "trim4.ic[1]"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve39|projectionCurve39_1|projectionCurve39_Shape1.ws" "trim4.ic[2]"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve40|projectionCurve40_1|projectionCurve40_Shape1.ws" "trim4.ic[3]"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve41|projectionCurve41_1|projectionCurve41_Shape1.ws" "trim4.ic[4]"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve42|projectionCurve42_1|projectionCurve42_Shape1.ws" "trim4.ic[5]"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve43|projectionCurve43_1|projectionCurve43_Shape1.ws" "trim4.ic[6]"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve44|projectionCurve44_1|projectionCurve44_Shape1.ws" "trim4.ic[7]"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve45|projectionCurve45_1|projectionCurve45_Shape1.ws" "trim4.ic[8]"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve46|projectionCurve46_1|projectionCurve46_Shape1.ws" "trim4.ic[9]"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve47|projectionCurve47_1|projectionCurve47_Shape1.ws" "trim4.ic[10]"
		;
connectAttr "|OneToOne|nurbsPlane3|nurbsPlaneShape3->|projectionCurve48|projectionCurve48_1|projectionCurve48_Shape1.ws" "trim4.ic[11]"
		;
connectAttr "nurbsPlaneShape3.l" "trim4.is";
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve1|projectionCurve1_1|projectionCurve1_Shape1.ws" "trim6.ic[0]"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve2|projectionCurve2_1|projectionCurve2_Shape1.ws" "trim6.ic[1]"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve3|projectionCurve3_1|projectionCurve3_Shape1.ws" "trim6.ic[2]"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve4|projectionCurve4_1|projectionCurve4_Shape1.ws" "trim6.ic[3]"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve5|projectionCurve5_1|projectionCurve5_Shape1.ws" "trim6.ic[4]"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve6|projectionCurve6_1|projectionCurve6_Shape1.ws" "trim6.ic[5]"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve7|projectionCurve7_1|projectionCurve7_Shape1.ws" "trim6.ic[6]"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve8|projectionCurve8_1|projectionCurve8_Shape1.ws" "trim6.ic[7]"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve9|projectionCurve9_1|projectionCurve9_Shape1.ws" "trim6.ic[8]"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve10|projectionCurve10_1|projectionCurve10_Shape1.ws" "trim6.ic[9]"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve11|projectionCurve11_1|projectionCurve11_Shape1.ws" "trim6.ic[10]"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve12|projectionCurve12_1|projectionCurve12_Shape1.ws" "trim6.ic[11]"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve13|projectionCurve13_1|projectionCurve13_Shape1.ws" "trim6.ic[12]"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve14|projectionCurve14_1|projectionCurve14_Shape1.ws" "trim6.ic[13]"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve15|projectionCurve15_1|projectionCurve15_Shape1.ws" "trim6.ic[14]"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve16|projectionCurve16_1|projectionCurve16_Shape1.ws" "trim6.ic[15]"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve17|projectionCurve17_1|projectionCurve17_Shape1.ws" "trim6.ic[16]"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve18|projectionCurve18_1|projectionCurve18_Shape1.ws" "trim6.ic[17]"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve19|projectionCurve19_1|projectionCurve19_Shape1.ws" "trim6.ic[18]"
		;
connectAttr "|All|nurbsPlane1|nurbsPlaneShape1->|projectionCurve20|projectionCurve20_1|projectionCurve20_Shape1.ws" "trim6.ic[19]"
		;
connectAttr "nurbsPlaneShape1.l" "trim6.is";
connectAttr "surfaceShader2.oc" "surfaceShader2SG.ss";
connectAttr "nurbsPlane4trimmedSurfaceShape1.iog" "surfaceShader2SG.dsm" -na;
connectAttr "nurbsPlane3trimmedSurfaceShape1.iog" "surfaceShader2SG.dsm" -na;
connectAttr "nurbsPlane1trimmedSurfaceShape1.iog" "surfaceShader2SG.dsm" -na;
connectAttr "BGShape.iog" "surfaceShader2SG.dsm" -na;
connectAttr "surfaceShader2SG.msg" "materialInfo14.sg";
connectAttr "surfaceShader2.msg" "materialInfo14.m";
connectAttr "surfaceShader2.msg" "materialInfo14.t" -na;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve37|projectionCurve37_1|projectionCurve37_Shape1.ws" "trim8.ic[0]"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve38|projectionCurve38_1|projectionCurve38_Shape1.ws" "trim8.ic[1]"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve39|projectionCurve39_1|projectionCurve39_Shape1.ws" "trim8.ic[2]"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve40|projectionCurve40_1|projectionCurve40_Shape1.ws" "trim8.ic[3]"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve41|projectionCurve41_1|projectionCurve41_Shape1.ws" "trim8.ic[4]"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve42|projectionCurve42_1|projectionCurve42_Shape1.ws" "trim8.ic[5]"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve43|projectionCurve43_1|projectionCurve43_Shape1.ws" "trim8.ic[6]"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve44|projectionCurve44_1|projectionCurve44_Shape1.ws" "trim8.ic[7]"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve45|projectionCurve45_1|projectionCurve45_Shape1.ws" "trim8.ic[8]"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve46|projectionCurve46_1|projectionCurve46_Shape1.ws" "trim8.ic[9]"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve47|projectionCurve47_1|projectionCurve47_Shape1.ws" "trim8.ic[10]"
		;
connectAttr "|OneToOne|nurbsPlane6|nurbsPlaneShape6->|projectionCurve48|projectionCurve48_1|projectionCurve48_Shape1.ws" "trim8.ic[11]"
		;
connectAttr "nurbsPlaneShape6Original.l" "trim8.is";
connectAttr "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve37|projectionCurve37_1|projectionCurve37_Shape1.ws" "trim9.ic[0]"
		;
connectAttr "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve38|projectionCurve38_1|projectionCurve38_Shape1.ws" "trim9.ic[1]"
		;
connectAttr "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve39|projectionCurve39_1|projectionCurve39_Shape1.ws" "trim9.ic[2]"
		;
connectAttr "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve40|projectionCurve40_1|projectionCurve40_Shape1.ws" "trim9.ic[3]"
		;
connectAttr "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve41|projectionCurve41_1|projectionCurve41_Shape1.ws" "trim9.ic[4]"
		;
connectAttr "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve42|projectionCurve42_1|projectionCurve42_Shape1.ws" "trim9.ic[5]"
		;
connectAttr "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve43|projectionCurve43_1|projectionCurve43_Shape1.ws" "trim9.ic[6]"
		;
connectAttr "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve44|projectionCurve44_1|projectionCurve44_Shape1.ws" "trim9.ic[7]"
		;
connectAttr "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve45|projectionCurve45_1|projectionCurve45_Shape1.ws" "trim9.ic[8]"
		;
connectAttr "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve46|projectionCurve46_1|projectionCurve46_Shape1.ws" "trim9.ic[9]"
		;
connectAttr "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve47|projectionCurve47_1|projectionCurve47_Shape1.ws" "trim9.ic[10]"
		;
connectAttr "|OneToOne|nurbsPlane8|nurbsPlaneShape8->|projectionCurve48|projectionCurve48_1|projectionCurve48_Shape1.ws" "trim9.ic[11]"
		;
connectAttr "nurbsPlaneShape8Original.l" "trim9.is";
connectAttr "surfaceShader3.oc" "surfaceShader3SG.ss";
connectAttr "nurbsPlaneShape8.iog" "surfaceShader3SG.dsm" -na;
connectAttr "nurbsPlaneShape9.iog" "surfaceShader3SG.dsm" -na;
connectAttr "surfaceShader3SG.msg" "materialInfo15.sg";
connectAttr "surfaceShader3.msg" "materialInfo15.m";
connectAttr "surfaceShader3.msg" "materialInfo15.t" -na;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve1|projectionCurve1_1|projectionCurve1_Shape1.ws" "trim10.ic[0]"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve2|projectionCurve2_1|projectionCurve2_Shape1.ws" "trim10.ic[1]"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve3|projectionCurve3_1|projectionCurve3_Shape1.ws" "trim10.ic[2]"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve4|projectionCurve4_1|projectionCurve4_Shape1.ws" "trim10.ic[3]"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve5|projectionCurve5_1|projectionCurve5_Shape1.ws" "trim10.ic[4]"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve6|projectionCurve6_1|projectionCurve6_Shape1.ws" "trim10.ic[5]"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve7|projectionCurve7_1|projectionCurve7_Shape1.ws" "trim10.ic[6]"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve8|projectionCurve8_1|projectionCurve8_Shape1.ws" "trim10.ic[7]"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve9|projectionCurve9_1|projectionCurve9_Shape1.ws" "trim10.ic[8]"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve10|projectionCurve10_1|projectionCurve10_Shape1.ws" "trim10.ic[9]"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve11|projectionCurve11_1|projectionCurve11_Shape1.ws" "trim10.ic[10]"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve12|projectionCurve12_1|projectionCurve12_Shape1.ws" "trim10.ic[11]"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve13|projectionCurve13_1|projectionCurve13_Shape1.ws" "trim10.ic[12]"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve14|projectionCurve14_1|projectionCurve14_Shape1.ws" "trim10.ic[13]"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve15|projectionCurve15_1|projectionCurve15_Shape1.ws" "trim10.ic[14]"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve16|projectionCurve16_1|projectionCurve16_Shape1.ws" "trim10.ic[15]"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve17|projectionCurve17_1|projectionCurve17_Shape1.ws" "trim10.ic[16]"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve18|projectionCurve18_1|projectionCurve18_Shape1.ws" "trim10.ic[17]"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve19|projectionCurve19_1|projectionCurve19_Shape1.ws" "trim10.ic[18]"
		;
connectAttr "|All|nurbsPlane7|nurbsPlaneShape7->|projectionCurve20|projectionCurve20_1|projectionCurve20_Shape1.ws" "trim10.ic[19]"
		;
connectAttr "nurbsPlaneShape7Original.l" "trim10.is";
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve1|projectionCurve1_1|projectionCurve1_Shape1.ws" "trim11.ic[0]"
		;
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve2|projectionCurve2_1|projectionCurve2_Shape1.ws" "trim11.ic[1]"
		;
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve3|projectionCurve3_1|projectionCurve3_Shape1.ws" "trim11.ic[2]"
		;
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve4|projectionCurve4_1|projectionCurve4_Shape1.ws" "trim11.ic[3]"
		;
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve5|projectionCurve5_1|projectionCurve5_Shape1.ws" "trim11.ic[4]"
		;
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve6|projectionCurve6_1|projectionCurve6_Shape1.ws" "trim11.ic[5]"
		;
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve7|projectionCurve7_1|projectionCurve7_Shape1.ws" "trim11.ic[6]"
		;
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve8|projectionCurve8_1|projectionCurve8_Shape1.ws" "trim11.ic[7]"
		;
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve9|projectionCurve9_1|projectionCurve9_Shape1.ws" "trim11.ic[8]"
		;
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve10|projectionCurve10_1|projectionCurve10_Shape1.ws" "trim11.ic[9]"
		;
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve11|projectionCurve11_1|projectionCurve11_Shape1.ws" "trim11.ic[10]"
		;
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve12|projectionCurve12_1|projectionCurve12_Shape1.ws" "trim11.ic[11]"
		;
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve13|projectionCurve13_1|projectionCurve13_Shape1.ws" "trim11.ic[12]"
		;
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve14|projectionCurve14_1|projectionCurve14_Shape1.ws" "trim11.ic[13]"
		;
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve15|projectionCurve15_1|projectionCurve15_Shape1.ws" "trim11.ic[14]"
		;
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve16|projectionCurve16_1|projectionCurve16_Shape1.ws" "trim11.ic[15]"
		;
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve17|projectionCurve17_1|projectionCurve17_Shape1.ws" "trim11.ic[16]"
		;
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve18|projectionCurve18_1|projectionCurve18_Shape1.ws" "trim11.ic[17]"
		;
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve19|projectionCurve19_1|projectionCurve19_Shape1.ws" "trim11.ic[18]"
		;
connectAttr "|All|nurbsPlane9|nurbsPlaneShape9->|projectionCurve20|projectionCurve20_1|projectionCurve20_Shape1.ws" "trim11.ic[19]"
		;
connectAttr "nurbsPlaneShape9Original.l" "trim11.is";
connectAttr "anisotropic1SG.pa" ":renderPartition.st" -na;
connectAttr "blinn1SG.pa" ":renderPartition.st" -na;
connectAttr "blinn2SG.pa" ":renderPartition.st" -na;
connectAttr "lambert2SG.pa" ":renderPartition.st" -na;
connectAttr "lambert3SG.pa" ":renderPartition.st" -na;
connectAttr "lambert6SG.pa" ":renderPartition.st" -na;
connectAttr "lambert5SG.pa" ":renderPartition.st" -na;
connectAttr "lambert4SG.pa" ":renderPartition.st" -na;
connectAttr "greySG.pa" ":renderPartition.st" -na;
connectAttr "surfaceShader1SG.pa" ":renderPartition.st" -na;
connectAttr "surfaceSG.pa" ":renderPartition.st" -na;
connectAttr "lambert7SG.pa" ":renderPartition.st" -na;
connectAttr "prefix_lambert3SG.pa" ":renderPartition.st" -na;
connectAttr "prefix_surfaceSG.pa" ":renderPartition.st" -na;
connectAttr "prefix_lambert3SG1.pa" ":renderPartition.st" -na;
connectAttr "prefix_surfaceSG1.pa" ":renderPartition.st" -na;
connectAttr "surfaceSG1.pa" ":renderPartition.st" -na;
connectAttr "surfaceShader2SG.pa" ":renderPartition.st" -na;
connectAttr "surfaceShader3SG.pa" ":renderPartition.st" -na;
connectAttr "surfaceShader1.msg" ":defaultShaderList1.s" -na;
connectAttr "surfaceShader2.msg" ":defaultShaderList1.s" -na;
connectAttr "surfaceShader3.msg" ":defaultShaderList1.s" -na;
connectAttr "lightLinker1.msg" ":lightList1.ln" -na;
connectAttr "nurbsPlaneShape1.iog" ":initialShadingGroup.dsm" -na;
connectAttr "nurbsPlaneShape3.iog" ":initialShadingGroup.dsm" -na;
connectAttr "nurbsPlaneShape4.iog" ":initialShadingGroup.dsm" -na;
// End of LinkIcons.ma
