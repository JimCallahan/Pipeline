//Maya ASCII 5.0 scene
//Name: Logo2.ma
//Last modified: Tue, Jul 20, 2004 06:08:10 AM
requires maya "5.0";
currentUnit -l centimeter -a degree -t film;
fileInfo "application" "maya";
fileInfo "product" "Maya Unlimited 5.0";
fileInfo "version" "5.0";
fileInfo "cutIdentifier" "200304010002";
fileInfo "osv" "Linux 2.4.20-18.8smp #1 SMP Thu May 29 07:20:32 EDT 2003 i686";
createNode transform -n "camera1";
	setAttr ".v" no;
	setAttr ".t" -type "double3" -2.6208732639545707 0.032889058471816002 0.99850318098764901 ;
createNode camera -n "cameraShape1" -p "camera1";
	setAttr -k off ".v";
	setAttr ".rnd" no;
	setAttr ".cap" -type "double2" 1.41732 0.94488 ;
	setAttr ".ff" 0;
	setAttr ".ncp" 0.01;
	setAttr ".ow" 10.089289752888412;
	setAttr ".imn" -type "string" "camera1";
	setAttr ".den" -type "string" "camera1_depth";
	setAttr ".man" -type "string" "camera1_mask";
	setAttr ".o" yes;
createNode transform -s -n "persp";
	setAttr ".v" no;
	setAttr ".t" -type "double3" 479 359 479 ;
	setAttr ".r" -type "double3" -27.92184662526083 45.000000000000007 9.3056427591871977e-15 ;
createNode camera -s -n "perspShape" -p "persp";
	setAttr -k off ".v" no;
	setAttr ".rnd" no;
	setAttr ".coi" 766.65702892492925;
	setAttr ".imn" -type "string" "persp";
	setAttr ".den" -type "string" "persp_depth";
	setAttr ".man" -type "string" "persp_mask";
	setAttr ".hc" -type "string" "viewSet -p %camera";
createNode transform -s -n "top";
	setAttr ".v" no;
	setAttr ".t" -type "double3" 0 100 0 ;
	setAttr ".r" -type "double3" -89.999999999999986 0 0 ;
createNode camera -s -n "topShape" -p "top";
	setAttr -k off ".v" no;
	setAttr ".rnd" no;
	setAttr ".coi" 100;
	setAttr ".ow" 30;
	setAttr ".imn" -type "string" "top";
	setAttr ".den" -type "string" "top_depth";
	setAttr ".man" -type "string" "top_mask";
	setAttr ".hc" -type "string" "viewSet -t %camera";
	setAttr ".o" yes;
createNode transform -s -n "front";
	setAttr ".v" no;
	setAttr ".t" -type "double3" 2.2204460492503131e-16 0.024999999999999467 
		100.11609467072553 ;
createNode camera -s -n "frontShape" -p "front";
	setAttr -k off ".v" no;
	setAttr ".coi" 100;
	setAttr ".ow" 4.6862014395834306;
	setAttr ".imn" -type "string" "front";
	setAttr ".den" -type "string" "front_depth";
	setAttr ".man" -type "string" "front_mask";
	setAttr ".hc" -type "string" "viewSet -f %camera";
	setAttr ".o" yes;
createNode transform -s -n "side";
	setAttr ".v" no;
	setAttr ".t" -type "double3" 100 0 0 ;
	setAttr ".r" -type "double3" 0 89.999999999999986 0 ;
createNode camera -s -n "sideShape" -p "side";
	setAttr -k off ".v" no;
	setAttr ".rnd" no;
	setAttr ".coi" 100;
	setAttr ".ow" 30;
	setAttr ".imn" -type "string" "side";
	setAttr ".den" -type "string" "side_depth";
	setAttr ".man" -type "string" "side_mask";
	setAttr ".hc" -type "string" "viewSet -s %camera";
	setAttr ".o" yes;
createNode transform -n "group9";
createNode transform -n "curve2" -p "group9";
	setAttr ".t" -type "double3" -2 -2 0 ;
	setAttr ".s" -type "double3" 1.0092213619469037 1.1563994560932824 9.9999999999999998e-13 ;
createNode nurbsCurve -n "curveShape2" -p "curve2";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 1 0 no 3
		6 0 0 0 0.90008806200000002 0.90008806200000002 0.90008806200000002
		4
		1.5 2 0
		2.3848908516622664 2.8148311031025615 0
		3.3095920663279466 2.5898773356942137 0
		3.9613540982483553 2.1791285796566289 0
		;
createNode transform -n "curve3detachedCurve2" -p "group9";
	setAttr ".t" -type "double3" -2 -2 0 ;
	setAttr ".s" -type "double3" 0.88841848405607016 0.88841848405607016 9.9999999999999998e-13 ;
createNode nurbsCurve -n "curve3detachedCurveShape2" -p "curve3detachedCurve2";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 2 0 no 3
		3 2.3696806559999999 3 4
		3
		4.5 2.8364370480000005 0
		4.5 0 0
		0 0 0
		;
createNode transform -n "curve1" -p "group9";
	setAttr ".t" -type "double3" -2 -2 0 ;
	setAttr ".s" -type "double3" 1.0092213619469037 1.1563994560932824 9.9999999999999998e-13 ;
createNode nurbsCurve -n "curveShape1" -p "curve1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 8 0 no 3
		13 0 0 0 0.125 0.25 0.375 0.5 0.625 0.75 0.875 1 1 1
		11
		0 0 0
		0.20941607678402752 0.042612999011611447 0
		0.6284249381291529 0.13399136205091322 0
		1.2466950817028284 0.29644643224634787 0
		1.8543924307105566 0.4981513985993069 0
		2.4258398297553163 0.76584475010964403 0
		2.8710987967322508 1.3024635355037955 0
		2.7773547168666677 2.0522085370807188 0
		2.0939128133295806 2.2672814793115967 0
		1.6953561567716933 2.0716483841566737 0
		1.500000000000002 2.0000000000000013 0
		;
createNode transform -n "curve7" -p "group9";
	setAttr ".t" -type "double3" -2 -2 0 ;
	setAttr ".s" -type "double3" 1.0092213619469037 1.1563994560932824 9.9999999999999998e-13 ;
createNode nurbsCurve -n "curveShape7" -p "curve7";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 1 0 no 3
		6 0 0 0 0.90008806200000002 0.90008806200000002 0.90008806200000002
		4
		1.5 2 0
		2.3848908516622664 2.8148311031025615 0
		3.3095920663279466 2.5898773356942137 0
		3.9613540982483553 2.1791285796566289 0
		;
createNode transform -n "curve3detachedCurve7" -p "group9";
	setAttr ".t" -type "double3" -2 -2 0 ;
	setAttr ".s" -type "double3" 0.88841848405607016 0.88841848405607016 9.9999999999999998e-13 ;
createNode nurbsCurve -n "curve3detachedCurveShape7" -p "curve3detachedCurve7";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 2 0 no 3
		3 2.3696806559999999 3 4
		3
		4.5 2.8364370480000005 0
		4.5 0 0
		0 0 0
		;
createNode transform -n "curve8" -p "group9";
	setAttr ".t" -type "double3" -2 -2 0 ;
	setAttr ".s" -type "double3" 1.0092213619469037 1.1563994560932824 9.9999999999999998e-13 ;
createNode nurbsCurve -n "curveShape8" -p "curve8";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 8 0 no 3
		13 0 0 0 0.125 0.25 0.375 0.5 0.625 0.75 0.875 1 1 1
		11
		0 0 0
		0.20941607678402752 0.042612999011611447 0
		0.6284249381291529 0.13399136205091322 0
		1.2466950817028284 0.29644643224634787 0
		1.8543924307105566 0.4981513985993069 0
		2.4258398297553163 0.76584475010964403 0
		2.8710987967322508 1.3024635355037955 0
		2.7773547168666677 2.0522085370807188 0
		2.0939128133295806 2.2672814793115967 0
		1.6953561567716933 2.0716483841566737 0
		1.500000000000002 2.0000000000000013 0
		;
createNode transform -n "group10";
createNode transform -n "pasted__offsetNurbsCurve1" -p "group10";
	setAttr ".s" -type "double3" 1 1 9.9999999999999998e-13 ;
	setAttr ".it" no;
createNode nurbsCurve -n "pasted__offsetNurbsCurveShape1" -p "pasted__offsetNurbsCurve1";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 1 0 no 3
		6 0 0 0 0.87457439250000002 0.87457439250000002 0.87457439250000002
		4
		-0.88615424530414044 -0.064648258206637443 0
		0.091522825591361434 1.4261490197575775 0
		1.3150699675950281 1.1159316198252125 0
		1.9978831782159456 0.64262750585816153 0
		;
createNode transform -n "curve3detachedCurve6detachedCurve3" -p "group10";
	setAttr ".t" -type "double3" -2 -2 0 ;
	setAttr ".s" -type "double3" 0.88841848405607016 0.88841848405607016 9.9999999999999998e-13 ;
createNode nurbsCurve -n "curve3detachedCurve6detachedCurveShape3" -p "curve3detachedCurve6detachedCurve3";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 2 2.3389933150000002
		2
		4.5 4.5 0
		4.5 2.9745300824999994 0
		;
createNode transform -n "pasted__offsetNurbsCurve2" -p "group10";
	setAttr ".s" -type "double3" 1 1 9.9999999999999998e-13 ;
	setAttr ".it" no;
createNode nurbsCurve -n "pasted__offsetNurbsCurveShape2" -p "pasted__offsetNurbsCurve2";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 8 0 no 3
		13 0 0 0 0.125 0.25 0.375 0.5 0.625 0.75 0.875 1 1 1
		11
		-2 -1.9000000000000001 0
		-1.812850700911945 -1.8539388952845981 0
		-1.3911637764650566 -1.7478329428590653 0
		-0.77476469960531846 -1.5632172841680496 0
		-0.16632914420351805 -1.3304283056983701 0
		0.38364918391213415 -1.0353161199556129 0
		0.79307224574971225 -0.4602041712320874 0
		0.70194415523659892 0.3089420343997335 0
		0.1343758459765213 0.49994311159674226 0
		-0.42107656516575975 0.29122027586145072 0
		-0.88096350815712765 -0.061650316884164003 0
		;
createNode transform -n "curve3detachedCurve6detachedCurve2" -p "group10";
	setAttr ".t" -type "double3" -2 -2 0 ;
	setAttr ".s" -type "double3" 0.88841848405607016 0.88841848405607016 9.9999999999999998e-13 ;
createNode nurbsCurve -n "curve3detachedCurve6detachedCurveShape2" -p "curve3detachedCurve6detachedCurve2";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 1 2
		2
		0 4.5 0
		4.5 4.5 0
		;
createNode transform -n "curve3detachedCurve6" -p "group10";
	setAttr ".t" -type "double3" -2 -2 0 ;
	setAttr ".s" -type "double3" 0.88841848405607016 0.88841848405607016 9.9999999999999998e-13 ;
createNode nurbsCurve -n "curve3detachedCurveShape6" -p "curve3detachedCurve6";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 0.025689131470000001 1
		2
		0 0.11560109161500001 0
		0 4.5 0
		;
createNode transform -n "pasted__offsetNurbsCurve3" -p "group10";
	setAttr ".s" -type "double3" 1 1 9.9999999999999998e-13 ;
	setAttr ".it" no;
createNode nurbsCurve -n "pasted__offsetNurbsCurveShape3" -p "pasted__offsetNurbsCurve3";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 1 0 no 3
		6 0 0 0 0.87457439250000002 0.87457439250000002 0.87457439250000002
		4
		-0.88615424530414044 -0.064648258206637443 0
		0.091522825591361434 1.4261490197575775 0
		1.3150699675950281 1.1159316198252125 0
		1.9978831782159456 0.64262750585816153 0
		;
createNode transform -n "curve3detachedCurve6detachedCurve4" -p "group10";
	setAttr ".t" -type "double3" -2 -2 0 ;
	setAttr ".s" -type "double3" 0.88841848405607016 0.88841848405607016 9.9999999999999998e-13 ;
createNode nurbsCurve -n "curve3detachedCurve6detachedCurveShape4" -p "curve3detachedCurve6detachedCurve4";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 2 2.3389933150000002
		2
		4.5 4.5 0
		4.5 2.9745300824999994 0
		;
createNode transform -n "pasted__offsetNurbsCurve4" -p "group10";
	setAttr ".s" -type "double3" 1 1 9.9999999999999998e-13 ;
	setAttr ".it" no;
createNode nurbsCurve -n "pasted__offsetNurbsCurveShape4" -p "pasted__offsetNurbsCurve4";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 8 0 no 3
		13 0 0 0 0.125 0.25 0.375 0.5 0.625 0.75 0.875 1 1 1
		11
		-2 -1.9000000000000001 0
		-1.812850700911945 -1.8539388952845981 0
		-1.3911637764650566 -1.7478329428590653 0
		-0.77476469960531846 -1.5632172841680496 0
		-0.16632914420351805 -1.3304283056983701 0
		0.38364918391213415 -1.0353161199556129 0
		0.79307224574971225 -0.4602041712320874 0
		0.70194415523659892 0.3089420343997335 0
		0.1343758459765213 0.49994311159674226 0
		-0.42107656516575975 0.29122027586145072 0
		-0.88096350815712765 -0.061650316884164003 0
		;
createNode transform -n "curve3detachedCurve6detachedCurve5" -p "group10";
	setAttr ".t" -type "double3" -2 -2 0 ;
	setAttr ".s" -type "double3" 0.88841848405607016 0.88841848405607016 9.9999999999999998e-13 ;
createNode nurbsCurve -n "curve3detachedCurve6detachedCurveShape5" -p "curve3detachedCurve6detachedCurve5";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 1 2
		2
		0 4.5 0
		4.5 4.5 0
		;
createNode transform -n "curve3detachedCurve8" -p "group10";
	setAttr ".t" -type "double3" -2 -2 0 ;
	setAttr ".s" -type "double3" 0.88841848405607016 0.88841848405607016 9.9999999999999998e-13 ;
createNode nurbsCurve -n "curve3detachedCurveShape8" -p "curve3detachedCurve8";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		1 1 0 no 3
		2 0.025689131470000001 1
		2
		0 0.11560109161500001 0
		0 4.5 0
		;
createNode transform -n "nurbsPlane1";
	setAttr ".s" -type "double3" 4 4 4 ;
createNode nurbsSurface -n "nurbsPlaneShape1" -p "nurbsPlane1";
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
createNode curveVarGroup -n "projectionCurve1" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 15;
createNode transform -n "projectionCurve1_1" -p "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve1";
createNode nurbsCurve -n "projectionCurve1_Shape1" -p "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve1|projectionCurve1_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve2" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve2_1" -p "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve2";
createNode nurbsCurve -n "projectionCurve2_Shape1" -p "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve2|projectionCurve2_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve3" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 15;
createNode transform -n "projectionCurve3_1" -p "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve3";
createNode nurbsCurve -n "projectionCurve3_Shape1" -p "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve3|projectionCurve3_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve4" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 15;
createNode transform -n "projectionCurve4_1" -p "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve4";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve4_Shape1" -p "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve4|projectionCurve4_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve5" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve5_1" -p "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve5";
createNode nurbsCurve -n "projectionCurve5_Shape1" -p "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve5|projectionCurve5_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve6" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 15;
createNode transform -n "projectionCurve6_1" -p "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve6";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve6_Shape1" -p "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve6|projectionCurve6_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve7" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve7_1" -p "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve7";
createNode nurbsCurve -n "projectionCurve7_Shape1" -p "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve7|projectionCurve7_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve8" -p "nurbsPlaneShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve8_1" -p "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve8";
createNode nurbsCurve -n "projectionCurve8_Shape1" -p "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve8|projectionCurve8_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode nurbsSurface -n "nurbsPlane1trimmedSurfaceShape1" -p "nurbsPlane1";
	setAttr -k off ".v";
	setAttr ".io" yes;
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 3;
	setAttr ".dvv" 3;
	setAttr ".cpr" 15;
	setAttr ".cps" 4;
createNode curveVarGroup -n "projectionCurve17" -p "nurbsPlane1trimmedSurfaceShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve17_1" -p "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape1->|projectionCurve17";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve17_Shape1" -p "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape1->|projectionCurve17|projectionCurve17_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve19" -p "nurbsPlane1trimmedSurfaceShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve19_1" -p "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape1->|projectionCurve19";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve19_Shape1" -p "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape1->|projectionCurve19|projectionCurve19_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode nurbsSurface -n "nurbsPlane1trimmedSurfaceShape2" -p "nurbsPlane1";
	setAttr -k off ".v";
	setAttr ".io" yes;
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 3;
	setAttr ".dvv" 3;
	setAttr ".cpr" 15;
	setAttr ".cps" 4;
createNode curveVarGroup -n "projectionCurve21" -p "nurbsPlane1trimmedSurfaceShape2";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve21_1" -p "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape2->|projectionCurve21";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve21_Shape1" -p "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape2->|projectionCurve21|projectionCurve21_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve22" -p "nurbsPlane1trimmedSurfaceShape2";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve22_1" -p "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape2->|projectionCurve22";
createNode nurbsCurve -n "projectionCurve22_Shape1" -p "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape2->|projectionCurve22|projectionCurve22_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode nurbsSurface -n "nurbsPlane1trimmedSurfaceShape3" -p "nurbsPlane1";
	setAttr -k off ".v";
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 0;
	setAttr ".dvv" 0;
	setAttr ".cpr" 4;
	setAttr ".cps" 1;
createNode transform -n "nurbsPlane2";
	setAttr ".s" -type "double3" 4 4 4 ;
createNode nurbsSurface -n "nurbsPlaneShape2" -p "nurbsPlane2";
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
createNode curveVarGroup -n "projectionCurve9" -p "nurbsPlaneShape2";
	setAttr ".mc" 1;
	setAttr ".ds" 15;
createNode transform -n "projectionCurve9_1" -p "projectionCurve9";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve9_Shape1" -p "projectionCurve9_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve10" -p "nurbsPlaneShape2";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve10_1" -p "projectionCurve10";
createNode nurbsCurve -n "projectionCurve10_Shape1" -p "projectionCurve10_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve11" -p "nurbsPlaneShape2";
	setAttr ".mc" 1;
	setAttr ".ds" 15;
createNode transform -n "projectionCurve11_1" -p "projectionCurve11";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve11_Shape1" -p "projectionCurve11_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve12" -p "nurbsPlaneShape2";
	setAttr ".mc" 1;
	setAttr ".ds" 15;
createNode transform -n "projectionCurve12_1" -p "projectionCurve12";
createNode nurbsCurve -n "projectionCurve12_Shape1" -p "projectionCurve12_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve13" -p "nurbsPlaneShape2";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve13_1" -p "projectionCurve13";
createNode nurbsCurve -n "projectionCurve13_Shape1" -p "projectionCurve13_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve14" -p "nurbsPlaneShape2";
	setAttr ".mc" 1;
	setAttr ".ds" 15;
createNode transform -n "projectionCurve14_1" -p "projectionCurve14";
createNode nurbsCurve -n "projectionCurve14_Shape1" -p "projectionCurve14_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve15" -p "nurbsPlaneShape2";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve15_1" -p "projectionCurve15";
createNode nurbsCurve -n "projectionCurve15_Shape1" -p "projectionCurve15_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve16" -p "nurbsPlaneShape2";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve16_1" -p "projectionCurve16";
createNode nurbsCurve -n "projectionCurve16_Shape1" -p "projectionCurve16_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode nurbsSurface -n "nurbsPlane2trimmedSurfaceShape1" -p "nurbsPlane2";
	setAttr -k off ".v";
	setAttr ".io" yes;
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 3;
	setAttr ".dvv" 3;
	setAttr ".cpr" 15;
	setAttr ".cps" 4;
createNode curveVarGroup -n "projectionCurve18" -p "nurbsPlane2trimmedSurfaceShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve18_1" -p "projectionCurve18";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve18_Shape1" -p "projectionCurve18_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve20" -p "nurbsPlane2trimmedSurfaceShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve20_1" -p "projectionCurve20";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve20_Shape1" -p "projectionCurve20_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode nurbsSurface -n "nurbsPlane2trimmedSurfaceShape2" -p "nurbsPlane2";
	setAttr -k off ".v";
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 3;
	setAttr ".dvv" 3;
	setAttr ".cpr" 15;
	setAttr ".cps" 4;
	setAttr ".ues" yes;
	setAttr ".esr" 0.999;
	setAttr ".nufa" 4;
	setAttr ".nvfa" 4;
	setAttr ".cvto" 0;
createNode transform -n "curve4";
createNode nurbsCurve -n "curveShape3" -p "curve4";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 1 0 no 3
		6 0 0 0 1 1 1
		4
		-0.049999999999999885 2.0999999999999952 0
		-0.049999999999999725 0.7166666666666659 0
		-0.049999999999999718 -0.66666666666666896 0
		-0.049999999999999906 -2.0499999999999963 0
		;
createNode transform -n "curve5";
createNode nurbsCurve -n "curveShape4" -p "curve5";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 1 0 no 3
		6 0 0 0 1 1 1
		4
		0.049999999999999885 2.0999999999999952 0
		0.049999999999999725 0.7166666666666659 0
		0.049999999999999718 -0.66666666666666896 0
		0.049999999999999906 -2.0499999999999963 0
		;
createNode transform -n "group12";
	setAttr ".t" -type "double3" 0 -0.1 0 ;
createNode transform -n "curve6" -p "group12";
createNode nurbsCurve -n "curveShape5" -p "curve6";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 1 0 no 3
		6 0 0 0 1 1 1
		4
		-2.0499999999999954 1.5999999999999963 0
		-0.6833333333333329 1.5999999999999912 0
		0.68333333333333535 1.599999999999991 0
		2.0499999999999963 1.599999999999997 0
		;
createNode transform -n "group11" -p "group12";
	setAttr ".t" -type "double3" -4.4408920985006262e-16 -0.099999999999993872 
		0 ;
	setAttr ".rp" -type "double3" 4.4408920985006262e-16 1.5999999999999939 
		0 ;
	setAttr ".sp" -type "double3" 4.4408920985006262e-16 1.5999999999999939 
		0 ;
createNode transform -n "pasted__curve6" -p "group11";
createNode nurbsCurve -n "pasted__curveShape5" -p "pasted__curve6";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 1 0 no 3
		6 0 0 0 1 1 1
		4
		-2.0499999999999954 1.5999999999999963 0
		-0.6833333333333329 1.5999999999999912 0
		0.68333333333333535 1.599999999999991 0
		2.0499999999999963 1.599999999999997 0
		;
createNode transform -n "pasted__curve7" -p "group11";
createNode nurbsCurve -n "pasted__curveShape7" -p "pasted__curve7";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 1 0 no 3
		6 0 0 0 1 1 1
		4
		-2.0499999999999954 1.5999999999999963 0
		-0.6833333333333329 1.5999999999999912 0
		0.68333333333333535 1.599999999999991 0
		2.0499999999999963 1.599999999999997 0
		;
createNode transform -n "curve11" -p "group12";
createNode nurbsCurve -n "curveShape11" -p "curve11";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 1 0 no 3
		6 0 0 0 1 1 1
		4
		-2.0499999999999954 1.5999999999999963 0
		-0.6833333333333329 1.5999999999999912 0
		0.68333333333333535 1.599999999999991 0
		2.0499999999999963 1.599999999999997 0
		;
createNode transform -n "curve9";
createNode nurbsCurve -n "curveShape9" -p "curve9";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 1 0 no 3
		6 0 0 0 1 1 1
		4
		-0.049999999999999885 2.0999999999999952 0
		-0.049999999999999725 0.7166666666666659 0
		-0.049999999999999718 -0.66666666666666896 0
		-0.049999999999999906 -2.0499999999999963 0
		;
createNode transform -n "curve10";
createNode nurbsCurve -n "curveShape10" -p "curve10";
	setAttr -k off ".v";
	setAttr ".cc" -type "nurbsCurve" 
		3 1 0 no 3
		6 0 0 0 1 1 1
		4
		0.049999999999999885 2.0999999999999952 0
		0.049999999999999725 0.7166666666666659 0
		0.049999999999999718 -0.66666666666666896 0
		0.049999999999999906 -2.0499999999999963 0
		;
createNode transform -n "group13";
createNode transform -n "nurbsPlane3" -p "group13";
	setAttr ".s" -type "double3" 4 4 4 ;
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
createNode curveVarGroup -n "projectionCurve1" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 15;
createNode transform -n "projectionCurve1_1" -p "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve1";
createNode nurbsCurve -n "projectionCurve1_Shape1" -p "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve1|projectionCurve1_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve2" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve2_1" -p "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve2";
createNode nurbsCurve -n "projectionCurve2_Shape1" -p "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve2|projectionCurve2_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve3" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 15;
createNode transform -n "projectionCurve3_1" -p "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve3";
createNode nurbsCurve -n "projectionCurve3_Shape1" -p "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve3|projectionCurve3_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve4" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 15;
createNode transform -n "projectionCurve4_1" -p "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve4";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve4_Shape1" -p "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve4|projectionCurve4_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve5" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve5_1" -p "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve5";
createNode nurbsCurve -n "projectionCurve5_Shape1" -p "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve5|projectionCurve5_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve6" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 15;
createNode transform -n "projectionCurve6_1" -p "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve6";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve6_Shape1" -p "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve6|projectionCurve6_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve7" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve7_1" -p "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve7";
createNode nurbsCurve -n "projectionCurve7_Shape1" -p "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve7|projectionCurve7_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve8" -p "nurbsPlaneShape3";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve8_1" -p "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve8";
createNode nurbsCurve -n "projectionCurve8_Shape1" -p "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve8|projectionCurve8_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode nurbsSurface -n "nurbsPlane3trimmedSurfaceShape" -p "nurbsPlane3";
	setAttr -k off ".v";
	setAttr ".io" yes;
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 3;
	setAttr ".dvv" 3;
	setAttr ".cpr" 15;
	setAttr ".cps" 4;
createNode curveVarGroup -n "projectionCurve17" -p "nurbsPlane3trimmedSurfaceShape";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve17_1" -p "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape->|projectionCurve17";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve17_Shape1" -p "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape->|projectionCurve17|projectionCurve17_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve19" -p "nurbsPlane3trimmedSurfaceShape";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve19_1" -p "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape->|projectionCurve19";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve19_Shape1" -p "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape->|projectionCurve19|projectionCurve19_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode nurbsSurface -n "nurbsPlane3trimmedSurfaceShape1" -p "nurbsPlane3";
	setAttr -k off ".v";
	setAttr ".io" yes;
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 3;
	setAttr ".dvv" 3;
	setAttr ".cpr" 15;
	setAttr ".cps" 4;
createNode curveVarGroup -n "projectionCurve21" -p "nurbsPlane3trimmedSurfaceShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve21_1" -p "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape1->|projectionCurve21";
createNode nurbsCurve -n "projectionCurve21_Shape1" -p "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape1->|projectionCurve21|projectionCurve21_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode curveVarGroup -n "projectionCurve22" -p "nurbsPlane3trimmedSurfaceShape1";
	setAttr ".mc" 1;
	setAttr ".ds" 4;
createNode transform -n "projectionCurve22_1" -p "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape1->|projectionCurve22";
	setAttr ".v" no;
createNode nurbsCurve -n "projectionCurve22_Shape1" -p "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape1->|projectionCurve22|projectionCurve22_1";
	setAttr -k off ".v";
	setAttr ".tw" yes;
createNode nurbsSurface -n "nurbsPlane3trimmedSurfaceShape2" -p "nurbsPlane3";
	setAttr -k off ".v";
	setAttr ".vir" yes;
	setAttr ".vif" yes;
	setAttr ".tw" yes;
	setAttr ".dvu" 0;
	setAttr ".dvv" 0;
	setAttr ".cpr" 4;
	setAttr ".cps" 1;
	setAttr ".ues" yes;
	setAttr ".esr" 0.999;
	setAttr ".nufa" 4;
	setAttr ".nvfa" 4;
	setAttr ".cvto" 0;
createNode materialInfo -n "materialInfo1";
createNode shadingEngine -n "surfaceShader1SG";
	setAttr ".ihi" 0;
	setAttr ".ro" yes;
createNode surfaceShader -n "surfaceShader1";
	setAttr ".oc" -type "float3" 0.84313726 0.84313726 0 ;
createNode lightLinker -n "lightLinker1";
	setAttr ".ihi" 0;
	setAttr -s 5 ".lnk";
createNode displayLayerManager -n "layerManager";
createNode displayLayer -n "defaultLayer";
createNode renderLayerManager -n "renderLayerManager";
createNode renderLayer -n "defaultRenderLayer";
createNode renderLayer -s -n "globalRender";
createNode script -n "uiConfigurationScriptNode";
	setAttr ".b" -type "string" (
		"// Maya Mel UI Configuration File.\n"
		+ "//\n"
		+ "//  This script is machine generated.  Edit at your own risk.\n"
		+ "//\n"
		+ "//\n"
		+ "global string $gMainPane;\n"
		+ "if (`paneLayout -exists $gMainPane`) {\n"
		+ "\tglobal int $gUseScenePanelConfig;\n"
		+ "\tint    $useSceneConfig = $gUseScenePanelConfig;\n"
		+ "\tint    $menusOkayInPanels = `optionVar -q allowMenusInPanels`;\tint    $nVisPanes = `paneLayout -q -nvp $gMainPane`;\n"
		+ "\tint    $nPanes = 0;\n"
		+ "\tstring $editorName;\n"
		+ "\tstring $panelName;\n"
		+ "\tstring $itemFilterName;\n"
		+ "\tstring $panelConfig;\n"
		+ "\t//\n"
		+ "\t//  get current state of the UI\n"
		+ "\t//\n"
		+ "\tsceneUIReplacement -update $gMainPane;\n"
		+ "\t$panelName = `sceneUIReplacement -getNextPanel \"modelPanel\" \"Top View\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\t$panelName = `modelPanel -unParent -l \"Top View\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t\t$editorName = $panelName;\n"
		+ "            modelEditor -e \n"
		+ "                -camera \"top\" \n"
		+ "                -useInteractiveMode 0\n"
		+ "                -displayLights \"default\" \n"
		+ "                -displayAppearance \"wireframe\" \n"
		+ "                -activeOnly 0\n"
		+ "                -wireframeOnShaded 0\n"
		+ "                -bufferMode \"double\" \n"
		+ "                -twoSidedLighting 1\n"
		+ "                -backfaceCulling 0\n"
		+ "                -xray 0\n"
		+ "                -displayTextures 0\n"
		+ "                -smoothWireframe 0\n"
		+ "                -textureAnisotropic 0\n"
		+ "                -textureHilight 1\n"
		+ "                -textureSampling 2\n"
		+ "                -textureDisplay \"modulate\" \n"
		+ "                -textureMaxSize 1024\n"
		+ "                -fogging 0\n"
		+ "                -fogSource \"fragment\" \n"
		+ "                -fogMode \"linear\" \n"
		+ "                -fogStart 0\n"
		+ "                -fogEnd 100\n"
		+ "                -fogDensity 0.1\n"
		+ "                -fogColor 0.5 0.5 0.5 1 \n"
		+ "                -sortTransparent 1\n"
		+ "                -nurbsCurves 1\n"
		+ "                -nurbsSurfaces 1\n"
		+ "                -polymeshes 1\n"
		+ "                -subdivSurfaces 1\n"
		+ "                -planes 1\n"
		+ "                -lights 1\n"
		+ "                -cameras 1\n"
		+ "                -controlVertices 1\n"
		+ "                -hulls 1\n"
		+ "                -grid 1\n"
		+ "                -joints 1\n"
		+ "                -ikHandles 1\n"
		+ "                -deformers 1\n"
		+ "                -dynamics 1\n"
		+ "                -fluids 1\n"
		+ "                -locators 1\n"
		+ "                -dimensions 1\n"
		+ "                -handles 1\n"
		+ "                -pivots 1\n"
		+ "                -textures 1\n"
		+ "                -strokes 1\n"
		+ "                -shadows 0\n"
		+ "                $editorName;\n"
		+ "modelEditor -e -viewSelected 0 $editorName;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tmodelPanel -edit -l \"Top View\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\t$editorName = $panelName;\n"
		+ "        modelEditor -e \n"
		+ "            -camera \"top\" \n"
		+ "            -useInteractiveMode 0\n"
		+ "            -displayLights \"default\" \n"
		+ "            -displayAppearance \"wireframe\" \n"
		+ "            -activeOnly 0\n"
		+ "            -wireframeOnShaded 0\n"
		+ "            -bufferMode \"double\" \n"
		+ "            -twoSidedLighting 1\n"
		+ "            -backfaceCulling 0\n"
		+ "            -xray 0\n"
		+ "            -displayTextures 0\n"
		+ "            -smoothWireframe 0\n"
		+ "            -textureAnisotropic 0\n"
		+ "            -textureHilight 1\n"
		+ "            -textureSampling 2\n"
		+ "            -textureDisplay \"modulate\" \n"
		+ "            -textureMaxSize 1024\n"
		+ "            -fogging 0\n"
		+ "            -fogSource \"fragment\" \n"
		+ "            -fogMode \"linear\" \n"
		+ "            -fogStart 0\n"
		+ "            -fogEnd 100\n"
		+ "            -fogDensity 0.1\n"
		+ "            -fogColor 0.5 0.5 0.5 1 \n"
		+ "            -sortTransparent 1\n"
		+ "            -nurbsCurves 1\n"
		+ "            -nurbsSurfaces 1\n"
		+ "            -polymeshes 1\n"
		+ "            -subdivSurfaces 1\n"
		+ "            -planes 1\n"
		+ "            -lights 1\n"
		+ "            -cameras 1\n"
		+ "            -controlVertices 1\n"
		+ "            -hulls 1\n"
		+ "            -grid 1\n"
		+ "            -joints 1\n"
		+ "            -ikHandles 1\n"
		+ "            -deformers 1\n"
		+ "            -dynamics 1\n"
		+ "            -fluids 1\n"
		+ "            -locators 1\n"
		+ "            -dimensions 1\n"
		+ "            -handles 1\n"
		+ "            -pivots 1\n"
		+ "            -textures 1\n"
		+ "            -strokes 1\n"
		+ "            -shadows 0\n"
		+ "            $editorName;\n"
		+ "modelEditor -e -viewSelected 0 $editorName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextPanel \"modelPanel\" \"Side View\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\t$panelName = `modelPanel -unParent -l \"Side View\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t\t$editorName = $panelName;\n"
		+ "            modelEditor -e \n"
		+ "                -camera \"side\" \n"
		+ "                -useInteractiveMode 0\n"
		+ "                -displayLights \"default\" \n"
		+ "                -displayAppearance \"wireframe\" \n"
		+ "                -activeOnly 0\n"
		+ "                -wireframeOnShaded 0\n"
		+ "                -bufferMode \"double\" \n"
		+ "                -twoSidedLighting 1\n"
		+ "                -backfaceCulling 0\n"
		+ "                -xray 0\n"
		+ "                -displayTextures 0\n"
		+ "                -smoothWireframe 0\n"
		+ "                -textureAnisotropic 0\n"
		+ "                -textureHilight 1\n"
		+ "                -textureSampling 2\n"
		+ "                -textureDisplay \"modulate\" \n"
		+ "                -textureMaxSize 1024\n"
		+ "                -fogging 0\n"
		+ "                -fogSource \"fragment\" \n"
		+ "                -fogMode \"linear\" \n"
		+ "                -fogStart 0\n"
		+ "                -fogEnd 100\n"
		+ "                -fogDensity 0.1\n"
		+ "                -fogColor 0.5 0.5 0.5 1 \n"
		+ "                -sortTransparent 1\n"
		+ "                -nurbsCurves 1\n"
		+ "                -nurbsSurfaces 1\n"
		+ "                -polymeshes 1\n"
		+ "                -subdivSurfaces 1\n"
		+ "                -planes 1\n"
		+ "                -lights 1\n"
		+ "                -cameras 1\n"
		+ "                -controlVertices 1\n"
		+ "                -hulls 1\n"
		+ "                -grid 1\n"
		+ "                -joints 1\n"
		+ "                -ikHandles 1\n"
		+ "                -deformers 1\n"
		+ "                -dynamics 1\n"
		+ "                -fluids 1\n"
		+ "                -locators 1\n"
		+ "                -dimensions 1\n"
		+ "                -handles 1\n"
		+ "                -pivots 1\n"
		+ "                -textures 1\n"
		+ "                -strokes 1\n"
		+ "                -shadows 0\n"
		+ "                $editorName;\n"
		+ "modelEditor -e -viewSelected 0 $editorName;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tmodelPanel -edit -l \"Side View\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\t$editorName = $panelName;\n"
		+ "        modelEditor -e \n"
		+ "            -camera \"side\" \n"
		+ "            -useInteractiveMode 0\n"
		+ "            -displayLights \"default\" \n"
		+ "            -displayAppearance \"wireframe\" \n"
		+ "            -activeOnly 0\n"
		+ "            -wireframeOnShaded 0\n"
		+ "            -bufferMode \"double\" \n"
		+ "            -twoSidedLighting 1\n"
		+ "            -backfaceCulling 0\n"
		+ "            -xray 0\n"
		+ "            -displayTextures 0\n"
		+ "            -smoothWireframe 0\n"
		+ "            -textureAnisotropic 0\n"
		+ "            -textureHilight 1\n"
		+ "            -textureSampling 2\n"
		+ "            -textureDisplay \"modulate\" \n"
		+ "            -textureMaxSize 1024\n"
		+ "            -fogging 0\n"
		+ "            -fogSource \"fragment\" \n"
		+ "            -fogMode \"linear\" \n"
		+ "            -fogStart 0\n"
		+ "            -fogEnd 100\n"
		+ "            -fogDensity 0.1\n"
		+ "            -fogColor 0.5 0.5 0.5 1 \n"
		+ "            -sortTransparent 1\n"
		+ "            -nurbsCurves 1\n"
		+ "            -nurbsSurfaces 1\n"
		+ "            -polymeshes 1\n"
		+ "            -subdivSurfaces 1\n"
		+ "            -planes 1\n"
		+ "            -lights 1\n"
		+ "            -cameras 1\n"
		+ "            -controlVertices 1\n"
		+ "            -hulls 1\n"
		+ "            -grid 1\n"
		+ "            -joints 1\n"
		+ "            -ikHandles 1\n"
		+ "            -deformers 1\n"
		+ "            -dynamics 1\n"
		+ "            -fluids 1\n"
		+ "            -locators 1\n"
		+ "            -dimensions 1\n"
		+ "            -handles 1\n"
		+ "            -pivots 1\n"
		+ "            -textures 1\n"
		+ "            -strokes 1\n"
		+ "            -shadows 0\n"
		+ "            $editorName;\n"
		+ "modelEditor -e -viewSelected 0 $editorName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextPanel \"modelPanel\" \"Front View\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\t$panelName = `modelPanel -unParent -l \"Front View\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t\t$editorName = $panelName;\n"
		+ "            modelEditor -e \n"
		+ "                -camera \"front\" \n"
		+ "                -useInteractiveMode 0\n"
		+ "                -displayLights \"default\" \n"
		+ "                -displayAppearance \"wireframe\" \n"
		+ "                -activeOnly 0\n"
		+ "                -wireframeOnShaded 0\n"
		+ "                -bufferMode \"double\" \n"
		+ "                -twoSidedLighting 1\n"
		+ "                -backfaceCulling 0\n"
		+ "                -xray 0\n"
		+ "                -displayTextures 0\n"
		+ "                -smoothWireframe 0\n"
		+ "                -textureAnisotropic 0\n"
		+ "                -textureHilight 1\n"
		+ "                -textureSampling 2\n"
		+ "                -textureDisplay \"modulate\" \n"
		+ "                -textureMaxSize 1024\n"
		+ "                -fogging 0\n"
		+ "                -fogSource \"fragment\" \n"
		+ "                -fogMode \"linear\" \n"
		+ "                -fogStart 0\n"
		+ "                -fogEnd 100\n"
		+ "                -fogDensity 0.1\n"
		+ "                -fogColor 0.5 0.5 0.5 1 \n"
		+ "                -sortTransparent 1\n"
		+ "                -nurbsCurves 1\n"
		+ "                -nurbsSurfaces 1\n"
		+ "                -polymeshes 1\n"
		+ "                -subdivSurfaces 1\n"
		+ "                -planes 1\n"
		+ "                -lights 1\n"
		+ "                -cameras 1\n"
		+ "                -controlVertices 1\n"
		+ "                -hulls 1\n"
		+ "                -grid 1\n"
		+ "                -joints 1\n"
		+ "                -ikHandles 1\n"
		+ "                -deformers 1\n"
		+ "                -dynamics 1\n"
		+ "                -fluids 1\n"
		+ "                -locators 1\n"
		+ "                -dimensions 1\n"
		+ "                -handles 1\n"
		+ "                -pivots 1\n"
		+ "                -textures 1\n"
		+ "                -strokes 1\n"
		+ "                -shadows 0\n"
		+ "                $editorName;\n"
		+ "modelEditor -e -viewSelected 0 $editorName;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tmodelPanel -edit -l \"Front View\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\t$editorName = $panelName;\n"
		+ "        modelEditor -e \n"
		+ "            -camera \"front\" \n"
		+ "            -useInteractiveMode 0\n"
		+ "            -displayLights \"default\" \n"
		+ "            -displayAppearance \"wireframe\" \n"
		+ "            -activeOnly 0\n"
		+ "            -wireframeOnShaded 0\n"
		+ "            -bufferMode \"double\" \n"
		+ "            -twoSidedLighting 1\n"
		+ "            -backfaceCulling 0\n"
		+ "            -xray 0\n"
		+ "            -displayTextures 0\n"
		+ "            -smoothWireframe 0\n"
		+ "            -textureAnisotropic 0\n"
		+ "            -textureHilight 1\n"
		+ "            -textureSampling 2\n"
		+ "            -textureDisplay \"modulate\" \n"
		+ "            -textureMaxSize 1024\n"
		+ "            -fogging 0\n"
		+ "            -fogSource \"fragment\" \n"
		+ "            -fogMode \"linear\" \n"
		+ "            -fogStart 0\n"
		+ "            -fogEnd 100\n"
		+ "            -fogDensity 0.1\n"
		+ "            -fogColor 0.5 0.5 0.5 1 \n"
		+ "            -sortTransparent 1\n"
		+ "            -nurbsCurves 1\n"
		+ "            -nurbsSurfaces 1\n"
		+ "            -polymeshes 1\n"
		+ "            -subdivSurfaces 1\n"
		+ "            -planes 1\n"
		+ "            -lights 1\n"
		+ "            -cameras 1\n"
		+ "            -controlVertices 1\n"
		+ "            -hulls 1\n"
		+ "            -grid 1\n"
		+ "            -joints 1\n"
		+ "            -ikHandles 1\n"
		+ "            -deformers 1\n"
		+ "            -dynamics 1\n"
		+ "            -fluids 1\n"
		+ "            -locators 1\n"
		+ "            -dimensions 1\n"
		+ "            -handles 1\n"
		+ "            -pivots 1\n"
		+ "            -textures 1\n"
		+ "            -strokes 1\n"
		+ "            -shadows 0\n"
		+ "            $editorName;\n"
		+ "modelEditor -e -viewSelected 0 $editorName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextPanel \"modelPanel\" \"Persp View\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\t$panelName = `modelPanel -unParent -l \"Persp View\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t\t$editorName = $panelName;\n"
		+ "            modelEditor -e \n"
		+ "                -camera \"front\" \n"
		+ "                -useInteractiveMode 0\n"
		+ "                -displayLights \"default\" \n"
		+ "                -displayAppearance \"smoothShaded\" \n"
		+ "                -activeOnly 0\n"
		+ "                -wireframeOnShaded 0\n"
		+ "                -bufferMode \"double\" \n"
		+ "                -twoSidedLighting 1\n"
		+ "                -backfaceCulling 0\n"
		+ "                -xray 0\n"
		+ "                -displayTextures 0\n"
		+ "                -smoothWireframe 0\n"
		+ "                -textureAnisotropic 0\n"
		+ "                -textureHilight 1\n"
		+ "                -textureSampling 2\n"
		+ "                -textureDisplay \"modulate\" \n"
		+ "                -textureMaxSize 1024\n"
		+ "                -fogging 0\n"
		+ "                -fogSource \"fragment\" \n"
		+ "                -fogMode \"linear\" \n"
		+ "                -fogStart 0\n"
		+ "                -fogEnd 100\n"
		+ "                -fogDensity 0.1\n"
		+ "                -fogColor 0.5 0.5 0.5 1 \n"
		+ "                -sortTransparent 1\n"
		+ "                -nurbsCurves 1\n"
		+ "                -nurbsSurfaces 1\n"
		+ "                -polymeshes 1\n"
		+ "                -subdivSurfaces 1\n"
		+ "                -planes 1\n"
		+ "                -lights 1\n"
		+ "                -cameras 1\n"
		+ "                -controlVertices 1\n"
		+ "                -hulls 1\n"
		+ "                -grid 1\n"
		+ "                -joints 1\n"
		+ "                -ikHandles 1\n"
		+ "                -deformers 1\n"
		+ "                -dynamics 1\n"
		+ "                -fluids 1\n"
		+ "                -locators 1\n"
		+ "                -dimensions 1\n"
		+ "                -handles 1\n"
		+ "                -pivots 1\n"
		+ "                -textures 1\n"
		+ "                -strokes 1\n"
		+ "                -shadows 0\n"
		+ "                $editorName;\n"
		+ "modelEditor -e -viewSelected 0 $editorName;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tmodelPanel -edit -l \"Persp View\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\t$editorName = $panelName;\n"
		+ "        modelEditor -e \n"
		+ "            -camera \"front\" \n"
		+ "            -useInteractiveMode 0\n"
		+ "            -displayLights \"default\" \n"
		+ "            -displayAppearance \"smoothShaded\" \n"
		+ "            -activeOnly 0\n"
		+ "            -wireframeOnShaded 0\n"
		+ "            -bufferMode \"double\" \n"
		+ "            -twoSidedLighting 1\n"
		+ "            -backfaceCulling 0\n"
		+ "            -xray 0\n"
		+ "            -displayTextures 0\n"
		+ "            -smoothWireframe 0\n"
		+ "            -textureAnisotropic 0\n"
		+ "            -textureHilight 1\n"
		+ "            -textureSampling 2\n"
		+ "            -textureDisplay \"modulate\" \n"
		+ "            -textureMaxSize 1024\n"
		+ "            -fogging 0\n"
		+ "            -fogSource \"fragment\" \n"
		+ "            -fogMode \"linear\" \n"
		+ "            -fogStart 0\n"
		+ "            -fogEnd 100\n"
		+ "            -fogDensity 0.1\n"
		+ "            -fogColor 0.5 0.5 0.5 1 \n"
		+ "            -sortTransparent 1\n"
		+ "            -nurbsCurves 1\n"
		+ "            -nurbsSurfaces 1\n"
		+ "            -polymeshes 1\n"
		+ "            -subdivSurfaces 1\n"
		+ "            -planes 1\n"
		+ "            -lights 1\n"
		+ "            -cameras 1\n"
		+ "            -controlVertices 1\n"
		+ "            -hulls 1\n"
		+ "            -grid 1\n"
		+ "            -joints 1\n"
		+ "            -ikHandles 1\n"
		+ "            -deformers 1\n"
		+ "            -dynamics 1\n"
		+ "            -fluids 1\n"
		+ "            -locators 1\n"
		+ "            -dimensions 1\n"
		+ "            -handles 1\n"
		+ "            -pivots 1\n"
		+ "            -textures 1\n"
		+ "            -strokes 1\n"
		+ "            -shadows 0\n"
		+ "            $editorName;\n"
		+ "modelEditor -e -viewSelected 0 $editorName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextPanel \"outlinerPanel\" \"Outliner\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\t$panelName = `outlinerPanel -unParent -l \"Outliner\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t\t$editorName = $panelName;\n"
		+ "            outlinerEditor -e \n"
		+ "                -mainListConnection \"worldList\" \n"
		+ "                -selectionConnection \"modelList\" \n"
		+ "                -showShapes 0\n"
		+ "                -showAttributes 0\n"
		+ "                -showConnected 0\n"
		+ "                -showAnimCurvesOnly 0\n"
		+ "                -autoExpand 0\n"
		+ "                -showDagOnly 1\n"
		+ "                -ignoreDagHierarchy 0\n"
		+ "                -expandConnections 0\n"
		+ "                -showUnitlessCurves 1\n"
		+ "                -showCompounds 1\n"
		+ "                -showLeafs 1\n"
		+ "                -showNumericAttrsOnly 0\n"
		+ "                -highlightActive 1\n"
		+ "                -autoSelectNewObjects 0\n"
		+ "                -doNotSelectNewObjects 0\n"
		+ "                -dropIsParent 1\n"
		+ "                -transmitFilters 0\n"
		+ "                -setFilter \"defaultSetFilter\" \n"
		+ "                -showSetMembers 1\n"
		+ "                -allowMultiSelection 1\n"
		+ "                -alwaysToggleSelect 0\n"
		+ "                -directSelect 0\n"
		+ "                -displayMode \"DAG\" \n"
		+ "                -expandObjects 0\n"
		+ "                -setsIgnoreFilters 1\n"
		+ "                -editAttrName 0\n"
		+ "                -showAttrValues 0\n"
		+ "                -highlightSecondary 0\n"
		+ "                -showUVAttrsOnly 0\n"
		+ "                -showTextureNodesOnly 0\n"
		+ "                -sortOrder \"none\" \n"
		+ "                -longNames 0\n"
		+ "                -niceNames 1\n"
		+ "                $editorName;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\toutlinerPanel -edit -l \"Outliner\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\t$editorName = $panelName;\n"
		+ "        outlinerEditor -e \n"
		+ "            -mainListConnection \"worldList\" \n"
		+ "            -selectionConnection \"modelList\" \n"
		+ "            -showShapes 0\n"
		+ "            -showAttributes 0\n"
		+ "            -showConnected 0\n"
		+ "            -showAnimCurvesOnly 0\n"
		+ "            -autoExpand 0\n"
		+ "            -showDagOnly 1\n"
		+ "            -ignoreDagHierarchy 0\n"
		+ "            -expandConnections 0\n"
		+ "            -showUnitlessCurves 1\n"
		+ "            -showCompounds 1\n"
		+ "            -showLeafs 1\n"
		+ "            -showNumericAttrsOnly 0\n"
		+ "            -highlightActive 1\n"
		+ "            -autoSelectNewObjects 0\n"
		+ "            -doNotSelectNewObjects 0\n"
		+ "            -dropIsParent 1\n"
		+ "            -transmitFilters 0\n"
		+ "            -setFilter \"defaultSetFilter\" \n"
		+ "            -showSetMembers 1\n"
		+ "            -allowMultiSelection 1\n"
		+ "            -alwaysToggleSelect 0\n"
		+ "            -directSelect 0\n"
		+ "            -displayMode \"DAG\" \n"
		+ "            -expandObjects 0\n"
		+ "            -setsIgnoreFilters 1\n"
		+ "            -editAttrName 0\n"
		+ "            -showAttrValues 0\n"
		+ "            -highlightSecondary 0\n"
		+ "            -showUVAttrsOnly 0\n"
		+ "            -showTextureNodesOnly 0\n"
		+ "            -sortOrder \"none\" \n"
		+ "            -longNames 0\n"
		+ "            -niceNames 1\n"
		+ "            $editorName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"graphEditor\" \"Graph Editor\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\t$panelName = `scriptedPanel -unParent  -type \"graphEditor\" -l \"Graph Editor\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t\t$editorName = ($panelName+\"OutlineEd\");\n"
		+ "            outlinerEditor -e \n"
		+ "                -mainListConnection \"graphEditorList\" \n"
		+ "                -selectionConnection \"graphEditor1FromOutliner\" \n"
		+ "                -highlightConnection \"keyframeList\" \n"
		+ "                -showShapes 1\n"
		+ "                -showAttributes 1\n"
		+ "                -showConnected 1\n"
		+ "                -showAnimCurvesOnly 1\n"
		+ "                -autoExpand 1\n"
		+ "                -showDagOnly 0\n"
		+ "                -ignoreDagHierarchy 0\n"
		+ "                -expandConnections 1\n"
		+ "                -showUnitlessCurves 1\n"
		+ "                -showCompounds 0\n"
		+ "                -showLeafs 1\n"
		+ "                -showNumericAttrsOnly 1\n"
		+ "                -highlightActive 0\n"
		+ "                -autoSelectNewObjects 1\n"
		+ "                -doNotSelectNewObjects 0\n"
		+ "                -dropIsParent 1\n"
		+ "                -transmitFilters 1\n"
		+ "                -setFilter \"0\" \n"
		+ "                -showSetMembers 0\n"
		+ "                -allowMultiSelection 1\n"
		+ "                -alwaysToggleSelect 0\n"
		+ "                -directSelect 0\n"
		+ "                -displayMode \"DAG\" \n"
		+ "                -expandObjects 0\n"
		+ "                -setsIgnoreFilters 1\n"
		+ "                -editAttrName 0\n"
		+ "                -showAttrValues 0\n"
		+ "                -highlightSecondary 0\n"
		+ "                -showUVAttrsOnly 0\n"
		+ "                -showTextureNodesOnly 0\n"
		+ "                -sortOrder \"none\" \n"
		+ "                -longNames 0\n"
		+ "                -niceNames 1\n"
		+ "                $editorName;\n"
		+ "\t\t\t$editorName = ($panelName+\"GraphEd\");\n"
		+ "            animCurveEditor -e \n"
		+ "                -mainListConnection \"graphEditor1FromOutliner\" \n"
		+ "                -displayKeys 1\n"
		+ "                -displayTangents 0\n"
		+ "                -displayActiveKeys 0\n"
		+ "                -displayActiveKeyTangents 1\n"
		+ "                -displayInfinities 0\n"
		+ "                -autoFit 0\n"
		+ "                -snapTime \"integer\" \n"
		+ "                -snapValue \"none\" \n"
		+ "                -showResults \"off\" \n"
		+ "                -showBufferCurves \"off\" \n"
		+ "                -smoothness \"fine\" \n"
		+ "                -resultSamples 1\n"
		+ "                -resultScreenSamples 0\n"
		+ "                -resultUpdate \"delayed\" \n"
		+ "                $editorName;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tscriptedPanel -edit -l \"Graph Editor\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\t\t$editorName = ($panelName+\"OutlineEd\");\n"
		+ "            outlinerEditor -e \n"
		+ "                -mainListConnection \"graphEditorList\" \n"
		+ "                -selectionConnection \"graphEditor1FromOutliner\" \n"
		+ "                -highlightConnection \"keyframeList\" \n"
		+ "                -showShapes 1\n"
		+ "                -showAttributes 1\n"
		+ "                -showConnected 1\n"
		+ "                -showAnimCurvesOnly 1\n"
		+ "                -autoExpand 1\n"
		+ "                -showDagOnly 0\n"
		+ "                -ignoreDagHierarchy 0\n"
		+ "                -expandConnections 1\n"
		+ "                -showUnitlessCurves 1\n"
		+ "                -showCompounds 0\n"
		+ "                -showLeafs 1\n"
		+ "                -showNumericAttrsOnly 1\n"
		+ "                -highlightActive 0\n"
		+ "                -autoSelectNewObjects 1\n"
		+ "                -doNotSelectNewObjects 0\n"
		+ "                -dropIsParent 1\n"
		+ "                -transmitFilters 1\n"
		+ "                -setFilter \"0\" \n"
		+ "                -showSetMembers 0\n"
		+ "                -allowMultiSelection 1\n"
		+ "                -alwaysToggleSelect 0\n"
		+ "                -directSelect 0\n"
		+ "                -displayMode \"DAG\" \n"
		+ "                -expandObjects 0\n"
		+ "                -setsIgnoreFilters 1\n"
		+ "                -editAttrName 0\n"
		+ "                -showAttrValues 0\n"
		+ "                -highlightSecondary 0\n"
		+ "                -showUVAttrsOnly 0\n"
		+ "                -showTextureNodesOnly 0\n"
		+ "                -sortOrder \"none\" \n"
		+ "                -longNames 0\n"
		+ "                -niceNames 1\n"
		+ "                $editorName;\n"
		+ "\t\t\t$editorName = ($panelName+\"GraphEd\");\n"
		+ "            animCurveEditor -e \n"
		+ "                -mainListConnection \"graphEditor1FromOutliner\" \n"
		+ "                -displayKeys 1\n"
		+ "                -displayTangents 0\n"
		+ "                -displayActiveKeys 0\n"
		+ "                -displayActiveKeyTangents 1\n"
		+ "                -displayInfinities 0\n"
		+ "                -autoFit 0\n"
		+ "                -snapTime \"integer\" \n"
		+ "                -snapValue \"none\" \n"
		+ "                -showResults \"off\" \n"
		+ "                -showBufferCurves \"off\" \n"
		+ "                -smoothness \"fine\" \n"
		+ "                -resultSamples 1\n"
		+ "                -resultScreenSamples 0\n"
		+ "                -resultUpdate \"delayed\" \n"
		+ "                $editorName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"dopeSheetPanel\" \"Dope Sheet\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\t$panelName = `scriptedPanel -unParent  -type \"dopeSheetPanel\" -l \"Dope Sheet\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t\t$editorName = ($panelName+\"OutlineEd\");\n"
		+ "            outlinerEditor -e \n"
		+ "                -mainListConnection \"animationList\" \n"
		+ "                -selectionConnection \"dopeSheetPanel1OutlinerSelection\" \n"
		+ "                -highlightConnection \"keyframeList\" \n"
		+ "                -showShapes 1\n"
		+ "                -showAttributes 1\n"
		+ "                -showConnected 1\n"
		+ "                -showAnimCurvesOnly 1\n"
		+ "                -autoExpand 0\n"
		+ "                -showDagOnly 0\n"
		+ "                -ignoreDagHierarchy 0\n"
		+ "                -expandConnections 1\n"
		+ "                -showUnitlessCurves 0\n"
		+ "                -showCompounds 1\n"
		+ "                -showLeafs 1\n"
		+ "                -showNumericAttrsOnly 1\n"
		+ "                -highlightActive 0\n"
		+ "                -autoSelectNewObjects 0\n"
		+ "                -doNotSelectNewObjects 1\n"
		+ "                -dropIsParent 1\n"
		+ "                -transmitFilters 0\n"
		+ "                -setFilter \"0\" \n"
		+ "                -showSetMembers 0\n"
		+ "                -allowMultiSelection 1\n"
		+ "                -alwaysToggleSelect 0\n"
		+ "                -directSelect 0\n"
		+ "                -displayMode \"DAG\" \n"
		+ "                -expandObjects 0\n"
		+ "                -setsIgnoreFilters 1\n"
		+ "                -editAttrName 0\n"
		+ "                -showAttrValues 0\n"
		+ "                -highlightSecondary 0\n"
		+ "                -showUVAttrsOnly 0\n"
		+ "                -showTextureNodesOnly 0\n"
		+ "                -sortOrder \"none\" \n"
		+ "                -longNames 0\n"
		+ "                -niceNames 1\n"
		+ "                $editorName;\n"
		+ "\t\t\t$editorName = ($panelName+\"DopeSheetEd\");\n"
		+ "            dopeSheetEditor -e \n"
		+ "                -mainListConnection \"dopeSheetPanel1FromOutliner\" \n"
		+ "                -highlightConnection \"dopeSheetPanel1OutlinerSelection\" \n"
		+ "                -displayKeys 1\n"
		+ "                -displayTangents 0\n"
		+ "                -displayActiveKeys 0\n"
		+ "                -displayActiveKeyTangents 0\n"
		+ "                -displayInfinities 0\n"
		+ "                -autoFit 0\n"
		+ "                -snapTime \"integer\" \n"
		+ "                -snapValue \"none\" \n"
		+ "                -outliner \"dopeSheetPanel1OutlineEd\" \n"
		+ "                -showSummary 1\n"
		+ "                -showScene 0\n"
		+ "                -hierarchyBelow 0\n"
		+ "                $editorName;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tscriptedPanel -edit -l \"Dope Sheet\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\t\t$editorName = ($panelName+\"OutlineEd\");\n"
		+ "            outlinerEditor -e \n"
		+ "                -mainListConnection \"animationList\" \n"
		+ "                -selectionConnection \"dopeSheetPanel1OutlinerSelection\" \n"
		+ "                -highlightConnection \"keyframeList\" \n"
		+ "                -showShapes 1\n"
		+ "                -showAttributes 1\n"
		+ "                -showConnected 1\n"
		+ "                -showAnimCurvesOnly 1\n"
		+ "                -autoExpand 0\n"
		+ "                -showDagOnly 0\n"
		+ "                -ignoreDagHierarchy 0\n"
		+ "                -expandConnections 1\n"
		+ "                -showUnitlessCurves 0\n"
		+ "                -showCompounds 1\n"
		+ "                -showLeafs 1\n"
		+ "                -showNumericAttrsOnly 1\n"
		+ "                -highlightActive 0\n"
		+ "                -autoSelectNewObjects 0\n"
		+ "                -doNotSelectNewObjects 1\n"
		+ "                -dropIsParent 1\n"
		+ "                -transmitFilters 0\n"
		+ "                -setFilter \"0\" \n"
		+ "                -showSetMembers 0\n"
		+ "                -allowMultiSelection 1\n"
		+ "                -alwaysToggleSelect 0\n"
		+ "                -directSelect 0\n"
		+ "                -displayMode \"DAG\" \n"
		+ "                -expandObjects 0\n"
		+ "                -setsIgnoreFilters 1\n"
		+ "                -editAttrName 0\n"
		+ "                -showAttrValues 0\n"
		+ "                -highlightSecondary 0\n"
		+ "                -showUVAttrsOnly 0\n"
		+ "                -showTextureNodesOnly 0\n"
		+ "                -sortOrder \"none\" \n"
		+ "                -longNames 0\n"
		+ "                -niceNames 1\n"
		+ "                $editorName;\n"
		+ "\t\t\t$editorName = ($panelName+\"DopeSheetEd\");\n"
		+ "            dopeSheetEditor -e \n"
		+ "                -mainListConnection \"dopeSheetPanel1FromOutliner\" \n"
		+ "                -highlightConnection \"dopeSheetPanel1OutlinerSelection\" \n"
		+ "                -displayKeys 1\n"
		+ "                -displayTangents 0\n"
		+ "                -displayActiveKeys 0\n"
		+ "                -displayActiveKeyTangents 0\n"
		+ "                -displayInfinities 0\n"
		+ "                -autoFit 0\n"
		+ "                -snapTime \"integer\" \n"
		+ "                -snapValue \"none\" \n"
		+ "                -outliner \"dopeSheetPanel1OutlineEd\" \n"
		+ "                -showSummary 1\n"
		+ "                -showScene 0\n"
		+ "                -hierarchyBelow 0\n"
		+ "                $editorName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"clipEditorPanel\" \"Trax Editor\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\t$panelName = `scriptedPanel -unParent  -type \"clipEditorPanel\" -l \"Trax Editor\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t\t$editorName = ($panelName+\"ClipEditor\");\n"
		+ "            clipEditor -e \n"
		+ "                -characterOutline \"clipEditorPanel1OutlineEditor\" \n"
		+ "                -menuContext \"track\" \n"
		+ "                $editorName;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tscriptedPanel -edit -l \"Trax Editor\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\t\t$editorName = ($panelName+\"ClipEditor\");\n"
		+ "            clipEditor -e \n"
		+ "                -characterOutline \"clipEditorPanel1OutlineEditor\" \n"
		+ "                -menuContext \"track\" \n"
		+ "                $editorName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"hyperGraphPanel\" \"Hypergraph\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\t$panelName = `scriptedPanel -unParent  -type \"hyperGraphPanel\" -l \"Hypergraph\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t\t$editorName = ($panelName+\"HyperGraphEd\");\n"
		+ "            hyperGraph -e \n"
		+ "                -orientation \"horiz\" \n"
		+ "                -zoom 1\n"
		+ "                -animateTransition 0\n"
		+ "                -showShapes 0\n"
		+ "                -showDeformers 0\n"
		+ "                -showExpressions 0\n"
		+ "                -showConstraints 0\n"
		+ "                -showUnderworld 0\n"
		+ "                -showInvisible 0\n"
		+ "                -transitionFrames 1\n"
		+ "                -freeform 0\n"
		+ "                -imageEnabled 0\n"
		+ "                -graphType \"DAG\" \n"
		+ "                -updateSelection 1\n"
		+ "                -updateNodeAdded 1\n"
		+ "                $editorName;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tscriptedPanel -edit -l \"Hypergraph\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\t\t$editorName = ($panelName+\"HyperGraphEd\");\n"
		+ "            hyperGraph -e \n"
		+ "                -orientation \"horiz\" \n"
		+ "                -zoom 1\n"
		+ "                -animateTransition 0\n"
		+ "                -showShapes 0\n"
		+ "                -showDeformers 0\n"
		+ "                -showExpressions 0\n"
		+ "                -showConstraints 0\n"
		+ "                -showUnderworld 0\n"
		+ "                -showInvisible 0\n"
		+ "                -transitionFrames 1\n"
		+ "                -freeform 0\n"
		+ "                -imageEnabled 0\n"
		+ "                -graphType \"DAG\" \n"
		+ "                -updateSelection 1\n"
		+ "                -updateNodeAdded 1\n"
		+ "                $editorName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"hyperShadePanel\" \"Hypershade\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\t$panelName = `scriptedPanel -unParent  -type \"hyperShadePanel\" -l \"Hypershade\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tscriptedPanel -edit -l \"Hypershade\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"visorPanel\" \"Visor\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\t$panelName = `scriptedPanel -unParent  -type \"visorPanel\" -l \"Visor\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tscriptedPanel -edit -l \"Visor\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"polyTexturePlacementPanel\" \"UV Texture Editor\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\t$panelName = `scriptedPanel -unParent  -type \"polyTexturePlacementPanel\" -l \"UV Texture Editor\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tscriptedPanel -edit -l \"UV Texture Editor\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"multiListerPanel\" \"Multilister\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\t$panelName = `scriptedPanel -unParent  -type \"multiListerPanel\" -l \"Multilister\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tscriptedPanel -edit -l \"Multilister\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"renderWindowPanel\" \"Render View\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\t$panelName = `scriptedPanel -unParent  -type \"renderWindowPanel\" -l \"Render View\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tscriptedPanel -edit -l \"Render View\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextPanel \"blendShapePanel\" \"Blend Shape\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\tblendShapePanel -unParent -l \"Blend Shape\" -mbv $menusOkayInPanels ;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tblendShapePanel -edit -l \"Blend Shape\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"dynRelEdPanel\" \"Dynamic Relationships\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\t$panelName = `scriptedPanel -unParent  -type \"dynRelEdPanel\" -l \"Dynamic Relationships\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tscriptedPanel -edit -l \"Dynamic Relationships\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextPanel \"devicePanel\" \"Devices\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\tdevicePanel -unParent -l \"Devices\" -mbv $menusOkayInPanels ;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tdevicePanel -edit -l \"Devices\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"relationshipPanel\" \"Relationship Editor\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\t$panelName = `scriptedPanel -unParent  -type \"relationshipPanel\" -l \"Relationship Editor\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tscriptedPanel -edit -l \"Relationship Editor\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"referenceEditorPanel\" \"Reference Editor\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\t$panelName = `scriptedPanel -unParent  -type \"referenceEditorPanel\" -l \"Reference Editor\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tscriptedPanel -edit -l \"Reference Editor\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"componentEditorPanel\" \"Component Editor\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\t$panelName = `scriptedPanel -unParent  -type \"componentEditorPanel\" -l \"Component Editor\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tscriptedPanel -edit -l \"Component Editor\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\t$panelName = `sceneUIReplacement -getNextScriptedPanel \"dynPaintScriptedPanelType\" \"Paint Effects\"`;\n"
		+ "\tif (\"\" == $panelName) {\n"
		+ "\t\tif ($useSceneConfig) {\n"
		+ "\t\t\t$panelName = `scriptedPanel -unParent  -type \"dynPaintScriptedPanelType\" -l \"Paint Effects\" -mbv $menusOkayInPanels `;\n"
		+ "\t\t}\n"
		+ "\t} else {\n"
		+ "\t\t$label = `panel -q -label $panelName`;\n"
		+ "\t\tscriptedPanel -edit -l \"Paint Effects\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\tif (!$useSceneConfig) {\n"
		+ "\t\t\tpanel -e -l $label $panelName;\n"
		+ "\t\t}\n"
		+ "\t}\n"
		+ "\tif ($useSceneConfig) {\n"
		+ "        string $configName = `getPanel -cwl \"Current Layout\"`;\n"
		+ "        if (\"\" != $configName) {\n"
		+ "\t\t\tpanelConfiguration -edit -label \"Current Layout\"\n"
		+ "\t\t\t\t-defaultImage \"\"\n"
		+ "\t\t\t\t-image \"\"\n"
		+ "\t\t\t\t-sc false\n"
		+ "\t\t\t\t-configString \"global string $gMainPane; paneLayout -e -cn \\\"single\\\" -ps 1 100 100 $gMainPane;\"\n"
		+ "\t\t\t\t-removeAllPanels\n"
		+ "\t\t\t\t-ap false\n"
		+ "\t\t\t\t\t\"Persp View\"\n"
		+ "\t\t\t\t\t\"modelPanel\"\n"
		+ "\t\t\t\t\t\"$panelName = `modelPanel -unParent -l \\\"Persp View\\\" -mbv $menusOkayInPanels `;\\n$editorName = $panelName;\\nmodelEditor -e \\n    -camera \\\"front\\\" \\n    -useInteractiveMode 0\\n    -displayLights \\\"default\\\" \\n    -displayAppearance \\\"smoothShaded\\\" \\n    -activeOnly 0\\n    -wireframeOnShaded 0\\n    -bufferMode \\\"double\\\" \\n    -twoSidedLighting 1\\n    -backfaceCulling 0\\n    -xray 0\\n    -displayTextures 0\\n    -smoothWireframe 0\\n    -textureAnisotropic 0\\n    -textureHilight 1\\n    -textureSampling 2\\n    -textureDisplay \\\"modulate\\\" \\n    -textureMaxSize 1024\\n    -fogging 0\\n    -fogSource \\\"fragment\\\" \\n    -fogMode \\\"linear\\\" \\n    -fogStart 0\\n    -fogEnd 100\\n    -fogDensity 0.1\\n    -fogColor 0.5 0.5 0.5 1 \\n    -sortTransparent 1\\n    -nurbsCurves 1\\n    -nurbsSurfaces 1\\n    -polymeshes 1\\n    -subdivSurfaces 1\\n    -planes 1\\n    -lights 1\\n    -cameras 1\\n    -controlVertices 1\\n    -hulls 1\\n    -grid 1\\n    -joints 1\\n    -ikHandles 1\\n    -deformers 1\\n    -dynamics 1\\n    -fluids 1\\n    -locators 1\\n    -dimensions 1\\n    -handles 1\\n    -pivots 1\\n    -textures 1\\n    -strokes 1\\n    -shadows 0\\n    $editorName;\\nmodelEditor -e -viewSelected 0 $editorName\"\n"
		+ "\t\t\t\t\t\"modelPanel -edit -l \\\"Persp View\\\" -mbv $menusOkayInPanels  $panelName;\\n$editorName = $panelName;\\nmodelEditor -e \\n    -camera \\\"front\\\" \\n    -useInteractiveMode 0\\n    -displayLights \\\"default\\\" \\n    -displayAppearance \\\"smoothShaded\\\" \\n    -activeOnly 0\\n    -wireframeOnShaded 0\\n    -bufferMode \\\"double\\\" \\n    -twoSidedLighting 1\\n    -backfaceCulling 0\\n    -xray 0\\n    -displayTextures 0\\n    -smoothWireframe 0\\n    -textureAnisotropic 0\\n    -textureHilight 1\\n    -textureSampling 2\\n    -textureDisplay \\\"modulate\\\" \\n    -textureMaxSize 1024\\n    -fogging 0\\n    -fogSource \\\"fragment\\\" \\n    -fogMode \\\"linear\\\" \\n    -fogStart 0\\n    -fogEnd 100\\n    -fogDensity 0.1\\n    -fogColor 0.5 0.5 0.5 1 \\n    -sortTransparent 1\\n    -nurbsCurves 1\\n    -nurbsSurfaces 1\\n    -polymeshes 1\\n    -subdivSurfaces 1\\n    -planes 1\\n    -lights 1\\n    -cameras 1\\n    -controlVertices 1\\n    -hulls 1\\n    -grid 1\\n    -joints 1\\n    -ikHandles 1\\n    -deformers 1\\n    -dynamics 1\\n    -fluids 1\\n    -locators 1\\n    -dimensions 1\\n    -handles 1\\n    -pivots 1\\n    -textures 1\\n    -strokes 1\\n    -shadows 0\\n    $editorName;\\nmodelEditor -e -viewSelected 0 $editorName\"\n"
		+ "\t\t\t\t$configName;\n"
		+ "            setNamedPanelLayout \"Current Layout\";\n"
		+ "        }\n"
		+ "        panelHistory -e -clear mainPanelHistory;\n"
		+ "        setFocus `paneLayout -q -p1 $gMainPane`;\n"
		+ "        sceneUIReplacement -deleteRemaining;\n"
		+ "        sceneUIReplacement -clear;\n"
		+ "\t}\n"
		+ "grid -spacing 0.5 -size 200 -divisions 10 -displayAxes yes -displayGridLines yes -displayDivisionLines yes -displayPerspectiveLabels no -displayOrthographicLabels no -displayAxesBold yes -perspectiveLabelPosition axis -orthographicLabelPosition edge;\n"
		+ "}\n");
	setAttr ".st" 3;
createNode script -n "sceneConfigurationScriptNode";
	setAttr ".b" -type "string" "playbackOptions -min 0 -max 36 -ast 0 -aet 500 ";
	setAttr ".st" 6;
createNode makeNurbPlane -n "makeNurbPlane1";
	setAttr ".ax" -type "double3" 0 0 1 ;
	setAttr ".u" 6;
	setAttr ".v" 6;
createNode projectCurve -n "projectCurve1";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve2";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve3";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve4";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve5";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve6";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve7";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve8";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode makeNurbPlane -n "makeNurbPlane2";
	setAttr ".ax" -type "double3" 0 0 1 ;
	setAttr ".u" 6;
	setAttr ".v" 6;
createNode projectCurve -n "projectCurve9";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve10";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve11";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve12";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve13";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve14";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve15";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve16";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode trim -n "trim1";
	setAttr -s 8 ".ic";
	setAttr ".lu[0]"  0.26205708367282288;
	setAttr ".lv[0]"  0.74253126979698736;
createNode trim -n "trim2";
	setAttr -s 8 ".ic";
	setAttr ".lu[0]"  0.87630140800670286;
	setAttr ".lv[0]"  0.30191831981888495;
createNode surfaceShader -n "surfaceShader2";
	setAttr ".oc" -type "float3" 0 0.49000001 0.49000001 ;
createNode shadingEngine -n "surfaceShader2SG";
	setAttr ".ihi" 0;
	setAttr -s 2 ".dsm";
	setAttr ".ro" yes;
createNode materialInfo -n "materialInfo2";
createNode surfaceShader -n "surfaceShader3";
	setAttr ".oc" -type "float3" 0.5 0.5 0.5 ;
createNode shadingEngine -n "surfaceShader3SG";
	setAttr ".ihi" 0;
	setAttr -s 4 ".dsm";
	setAttr ".ro" yes;
createNode materialInfo -n "materialInfo3";
createNode projectCurve -n "projectCurve17";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve18";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve19";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve20";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode trim -n "trim3";
	setAttr -s 2 ".ic";
	setAttr -s 4 ".lu[0:3]"  0.37026862466729249 0.64310574392370812 0.4037045461447944 
		0.57355902725050412;
	setAttr -s 4 ".lv[0:3]"  0.84171511750006944 0.85241461237287008 0.37093734309684262 
		0.39099889598334375;
createNode projectCurve -n "projectCurve21";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve22";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode trim -n "trim6";
	setAttr -s 2 ".ic";
	setAttr -s 4 ".lu[0:3]"  0.47245903313083887 0.49470366021746903 0.6080453315636325 
		0.41208075960998541;
	setAttr -s 4 ".lv[0:3]"  0.62859544897646602 0.6434252003675528 0.68473665067129463 
		0.047057340854562042;
createNode makeNurbPlane -n "makeNurbPlane3";
	setAttr ".ax" -type "double3" 0 0 1 ;
	setAttr ".u" 6;
	setAttr ".v" 6;
createNode projectCurve -n "projectCurve23";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve24";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve25";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve26";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve27";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve28";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve29";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve30";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode trim -n "trim7";
	setAttr -s 8 ".ic";
	setAttr ".lu[0]"  0.26205708367282288;
	setAttr ".lv[0]"  0.74253126979698736;
createNode projectCurve -n "projectCurve31";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve32";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode trim -n "trim8";
	setAttr -s 2 ".ic";
	setAttr -s 4 ".lu[0:3]"  0.37026862466729249 0.64310574392370812 0.4037045461447944 
		0.57355902725050412;
	setAttr -s 4 ".lv[0:3]"  0.84171511750006944 0.85241461237287008 0.37093734309684262 
		0.39099889598334375;
createNode projectCurve -n "projectCurve33";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode projectCurve -n "projectCurve34";
	setAttr ".d" -type "double3" 0 0 -1 ;
createNode trim -n "trim9";
	setAttr -s 2 ".ic";
	setAttr -s 2 ".lu[0:1]"  0.42479197508805983 0.65889019347593014;
	setAttr -s 2 ".lv[0:1]"  0.92307194088518962 0.91883486905916478;
createNode trim -n "trim10";
	setAttr -s 2 ".ic";
	setAttr -s 4 ".lu[0:3]"  0.44068099443565284 0.59109704425953324 0.41843636734902268 
		0.60910459952013873;
	setAttr -s 4 ".lv[0:3]"  0.75782613967022239 0.80443392975649508 0.48453500689162277 
		0.4358086808923376;
select -ne :time1;
	setAttr ".o" 0;
select -ne :renderPartition;
	setAttr -s 5 ".st";
select -ne :renderGlobalsList1;
select -ne :defaultShaderList1;
	setAttr -s 5 ".s";
select -ne :postProcessList1;
	setAttr -s 2 ".p";
select -ne :lightList1;
select -ne :initialShadingGroup;
	setAttr -s 2 ".dsm";
	setAttr ".ro" yes;
select -ne :initialParticleSE;
	setAttr ".ro" yes;
select -ne :defaultRenderGlobals;
	addAttr -ci true -sn "currentRenderer" -ln "currentRenderer" -dt "string";
	setAttr ".outf" 5;
	setAttr ".top" 798;
	setAttr ".rght" 798;
	setAttr ".currentRenderer" -type "string" "mayaSoftware";
select -ne :defaultRenderQuality;
	setAttr ".eaa" 0;
	setAttr ".ufil" yes;
	setAttr ".pft" 4;
	setAttr ".pfwx" 3;
	setAttr ".pfwy" 3;
select -ne :defaultResolution;
	setAttr ".w" 800;
	setAttr ".h" 800;
	setAttr ".dar" 1;
	setAttr ".ldar" yes;
select -ne :hardwareRenderGlobals;
	setAttr ".fn" -type "string" "default-%4n";
select -ne :defaultHardwareRenderGlobals;
	setAttr ".fn" -type "string" "im";
	setAttr ".res" -type "string" "ntsc_4d 646 485 1.333";
connectAttr "makeNurbPlane1.os" "nurbsPlaneShape1.cr";
connectAttr "projectCurve1.oc" "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve1.cr"
		;
connectAttr "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve1.l[0]" "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve1|projectionCurve1_1|projectionCurve1_Shape1.cr"
		;
connectAttr "projectCurve2.oc" "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve2.cr"
		;
connectAttr "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve2.l[0]" "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve2|projectionCurve2_1|projectionCurve2_Shape1.cr"
		;
connectAttr "projectCurve3.oc" "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve3.cr"
		;
connectAttr "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve3.l[0]" "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve3|projectionCurve3_1|projectionCurve3_Shape1.cr"
		;
connectAttr "projectCurve4.oc" "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve4.cr"
		;
connectAttr "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve4.l[0]" "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve4|projectionCurve4_1|projectionCurve4_Shape1.cr"
		;
connectAttr "projectCurve5.oc" "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve5.cr"
		;
connectAttr "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve5.l[0]" "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve5|projectionCurve5_1|projectionCurve5_Shape1.cr"
		;
connectAttr "projectCurve6.oc" "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve6.cr"
		;
connectAttr "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve6.l[0]" "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve6|projectionCurve6_1|projectionCurve6_Shape1.cr"
		;
connectAttr "projectCurve7.oc" "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve7.cr"
		;
connectAttr "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve7.l[0]" "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve7|projectionCurve7_1|projectionCurve7_Shape1.cr"
		;
connectAttr "projectCurve8.oc" "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve8.cr"
		;
connectAttr "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve8.l[0]" "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve8|projectionCurve8_1|projectionCurve8_Shape1.cr"
		;
connectAttr "trim1.os" "nurbsPlane1trimmedSurfaceShape1.cr";
connectAttr "projectCurve17.oc" "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape1->|projectionCurve17.cr"
		;
connectAttr "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape1->|projectionCurve17.l[0]" "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape1->|projectionCurve17|projectionCurve17_1|projectionCurve17_Shape1.cr"
		;
connectAttr "projectCurve19.oc" "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape1->|projectionCurve19.cr"
		;
connectAttr "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape1->|projectionCurve19.l[0]" "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape1->|projectionCurve19|projectionCurve19_1|projectionCurve19_Shape1.cr"
		;
connectAttr "trim3.os" "nurbsPlane1trimmedSurfaceShape2.cr";
connectAttr "projectCurve21.oc" "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape2->|projectionCurve21.cr"
		;
connectAttr "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape2->|projectionCurve21.l[0]" "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape2->|projectionCurve21|projectionCurve21_1|projectionCurve21_Shape1.cr"
		;
connectAttr "projectCurve22.oc" "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape2->|projectionCurve22.cr"
		;
connectAttr "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape2->|projectionCurve22.l[0]" "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape2->|projectionCurve22|projectionCurve22_1|projectionCurve22_Shape1.cr"
		;
connectAttr "trim9.os" "nurbsPlane1trimmedSurfaceShape3.cr";
connectAttr "makeNurbPlane2.os" "nurbsPlaneShape2.cr";
connectAttr "projectCurve9.oc" "projectionCurve9.cr";
connectAttr "projectionCurve9.l[0]" "projectionCurve9_Shape1.cr";
connectAttr "projectCurve10.oc" "projectionCurve10.cr";
connectAttr "projectionCurve10.l[0]" "projectionCurve10_Shape1.cr";
connectAttr "projectCurve11.oc" "projectionCurve11.cr";
connectAttr "projectionCurve11.l[0]" "projectionCurve11_Shape1.cr";
connectAttr "projectCurve12.oc" "projectionCurve12.cr";
connectAttr "projectionCurve12.l[0]" "projectionCurve12_Shape1.cr";
connectAttr "projectCurve13.oc" "projectionCurve13.cr";
connectAttr "projectionCurve13.l[0]" "projectionCurve13_Shape1.cr";
connectAttr "projectCurve14.oc" "projectionCurve14.cr";
connectAttr "projectionCurve14.l[0]" "projectionCurve14_Shape1.cr";
connectAttr "projectCurve15.oc" "projectionCurve15.cr";
connectAttr "projectionCurve15.l[0]" "projectionCurve15_Shape1.cr";
connectAttr "projectCurve16.oc" "projectionCurve16.cr";
connectAttr "projectionCurve16.l[0]" "projectionCurve16_Shape1.cr";
connectAttr "trim2.os" "nurbsPlane2trimmedSurfaceShape1.cr";
connectAttr "projectCurve18.oc" "projectionCurve18.cr";
connectAttr "projectionCurve18.l[0]" "projectionCurve18_Shape1.cr";
connectAttr "projectCurve20.oc" "projectionCurve20.cr";
connectAttr "projectionCurve20.l[0]" "projectionCurve20_Shape1.cr";
connectAttr "trim6.os" "nurbsPlane2trimmedSurfaceShape2.cr";
connectAttr "makeNurbPlane3.os" "nurbsPlaneShape3.cr";
connectAttr "projectCurve23.oc" "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve1.cr"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve1.l[0]" "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve1|projectionCurve1_1|projectionCurve1_Shape1.cr"
		;
connectAttr "projectCurve24.oc" "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve2.cr"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve2.l[0]" "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve2|projectionCurve2_1|projectionCurve2_Shape1.cr"
		;
connectAttr "projectCurve25.oc" "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve3.cr"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve3.l[0]" "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve3|projectionCurve3_1|projectionCurve3_Shape1.cr"
		;
connectAttr "projectCurve26.oc" "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve4.cr"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve4.l[0]" "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve4|projectionCurve4_1|projectionCurve4_Shape1.cr"
		;
connectAttr "projectCurve27.oc" "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve5.cr"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve5.l[0]" "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve5|projectionCurve5_1|projectionCurve5_Shape1.cr"
		;
connectAttr "projectCurve28.oc" "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve6.cr"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve6.l[0]" "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve6|projectionCurve6_1|projectionCurve6_Shape1.cr"
		;
connectAttr "projectCurve29.oc" "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve7.cr"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve7.l[0]" "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve7|projectionCurve7_1|projectionCurve7_Shape1.cr"
		;
connectAttr "projectCurve30.oc" "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve8.cr"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve8.l[0]" "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve8|projectionCurve8_1|projectionCurve8_Shape1.cr"
		;
connectAttr "trim7.os" "nurbsPlane3trimmedSurfaceShape.cr";
connectAttr "projectCurve31.oc" "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape->|projectionCurve17.cr"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape->|projectionCurve17.l[0]" "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape->|projectionCurve17|projectionCurve17_1|projectionCurve17_Shape1.cr"
		;
connectAttr "projectCurve32.oc" "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape->|projectionCurve19.cr"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape->|projectionCurve19.l[0]" "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape->|projectionCurve19|projectionCurve19_1|projectionCurve19_Shape1.cr"
		;
connectAttr "trim8.os" "nurbsPlane3trimmedSurfaceShape1.cr";
connectAttr "projectCurve33.oc" "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape1->|projectionCurve21.cr"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape1->|projectionCurve21.l[0]" "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape1->|projectionCurve21|projectionCurve21_1|projectionCurve21_Shape1.cr"
		;
connectAttr "projectCurve34.oc" "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape1->|projectionCurve22.cr"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape1->|projectionCurve22.l[0]" "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape1->|projectionCurve22|projectionCurve22_1|projectionCurve22_Shape1.cr"
		;
connectAttr "trim10.os" "nurbsPlane3trimmedSurfaceShape2.cr";
connectAttr "surfaceShader1SG.msg" "materialInfo1.sg";
connectAttr "surfaceShader1.oc" "surfaceShader1SG.ss";
connectAttr "nurbsPlane1trimmedSurfaceShape3.iog" "surfaceShader1SG.dsm"
		 -na;
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[0].llnk";
connectAttr ":initialShadingGroup.msg" "lightLinker1.lnk[0].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[1].llnk";
connectAttr ":initialParticleSE.msg" "lightLinker1.lnk[1].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[2].llnk";
connectAttr "surfaceShader1SG.msg" "lightLinker1.lnk[2].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[3].llnk";
connectAttr "surfaceShader2SG.msg" "lightLinker1.lnk[3].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[4].llnk";
connectAttr "surfaceShader3SG.msg" "lightLinker1.lnk[4].olnk";
connectAttr "layerManager.dli[0]" "defaultLayer.id";
connectAttr "renderLayerManager.rlmi[0]" "defaultRenderLayer.rlid";
connectAttr "curveShape2.ws" "projectCurve1.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve1.is";
connectAttr "curve3detachedCurveShape2.ws" "projectCurve2.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve2.is";
connectAttr "curveShape1.ws" "projectCurve3.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve3.is";
connectAttr "pasted__offsetNurbsCurveShape1.ws" "projectCurve4.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve4.is";
connectAttr "curve3detachedCurve6detachedCurveShape3.ws" "projectCurve5.ic"
		;
connectAttr "nurbsPlaneShape1.ws" "projectCurve5.is";
connectAttr "pasted__offsetNurbsCurveShape2.ws" "projectCurve6.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve6.is";
connectAttr "curve3detachedCurve6detachedCurveShape2.ws" "projectCurve7.ic"
		;
connectAttr "nurbsPlaneShape1.ws" "projectCurve7.is";
connectAttr "curve3detachedCurveShape6.ws" "projectCurve8.ic";
connectAttr "nurbsPlaneShape1.ws" "projectCurve8.is";
connectAttr "curveShape2.ws" "projectCurve9.ic";
connectAttr "nurbsPlaneShape2.ws" "projectCurve9.is";
connectAttr "curve3detachedCurveShape2.ws" "projectCurve10.ic";
connectAttr "nurbsPlaneShape2.ws" "projectCurve10.is";
connectAttr "curveShape1.ws" "projectCurve11.ic";
connectAttr "nurbsPlaneShape2.ws" "projectCurve11.is";
connectAttr "pasted__offsetNurbsCurveShape1.ws" "projectCurve12.ic";
connectAttr "nurbsPlaneShape2.ws" "projectCurve12.is";
connectAttr "curve3detachedCurve6detachedCurveShape3.ws" "projectCurve13.ic"
		;
connectAttr "nurbsPlaneShape2.ws" "projectCurve13.is";
connectAttr "pasted__offsetNurbsCurveShape2.ws" "projectCurve14.ic";
connectAttr "nurbsPlaneShape2.ws" "projectCurve14.is";
connectAttr "curve3detachedCurve6detachedCurveShape2.ws" "projectCurve15.ic"
		;
connectAttr "nurbsPlaneShape2.ws" "projectCurve15.is";
connectAttr "curve3detachedCurveShape6.ws" "projectCurve16.ic";
connectAttr "nurbsPlaneShape2.ws" "projectCurve16.is";
connectAttr "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve1|projectionCurve1_1|projectionCurve1_Shape1.ws" "trim1.ic[0]"
		;
connectAttr "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve2|projectionCurve2_1|projectionCurve2_Shape1.ws" "trim1.ic[1]"
		;
connectAttr "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve3|projectionCurve3_1|projectionCurve3_Shape1.ws" "trim1.ic[2]"
		;
connectAttr "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve4|projectionCurve4_1|projectionCurve4_Shape1.ws" "trim1.ic[3]"
		;
connectAttr "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve5|projectionCurve5_1|projectionCurve5_Shape1.ws" "trim1.ic[4]"
		;
connectAttr "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve6|projectionCurve6_1|projectionCurve6_Shape1.ws" "trim1.ic[5]"
		;
connectAttr "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve7|projectionCurve7_1|projectionCurve7_Shape1.ws" "trim1.ic[6]"
		;
connectAttr "|nurbsPlane1|nurbsPlaneShape1->|projectionCurve8|projectionCurve8_1|projectionCurve8_Shape1.ws" "trim1.ic[7]"
		;
connectAttr "nurbsPlaneShape1.l" "trim1.is";
connectAttr "projectionCurve9_Shape1.ws" "trim2.ic[0]";
connectAttr "projectionCurve10_Shape1.ws" "trim2.ic[1]";
connectAttr "projectionCurve11_Shape1.ws" "trim2.ic[2]";
connectAttr "projectionCurve12_Shape1.ws" "trim2.ic[3]";
connectAttr "projectionCurve13_Shape1.ws" "trim2.ic[4]";
connectAttr "projectionCurve14_Shape1.ws" "trim2.ic[5]";
connectAttr "projectionCurve15_Shape1.ws" "trim2.ic[6]";
connectAttr "projectionCurve16_Shape1.ws" "trim2.ic[7]";
connectAttr "nurbsPlaneShape2.l" "trim2.is";
connectAttr "surfaceShader2.oc" "surfaceShader2SG.ss";
connectAttr "nurbsPlane2trimmedSurfaceShape1.iog" "surfaceShader2SG.dsm"
		 -na;
connectAttr "nurbsPlane2trimmedSurfaceShape2.iog" "surfaceShader2SG.dsm"
		 -na;
connectAttr "surfaceShader2SG.msg" "materialInfo2.sg";
connectAttr "surfaceShader3.oc" "surfaceShader3SG.ss";
connectAttr "nurbsPlane1trimmedSurfaceShape1.iog" "surfaceShader3SG.dsm"
		 -na;
connectAttr "nurbsPlane1trimmedSurfaceShape2.iog" "surfaceShader3SG.dsm"
		 -na;
connectAttr "nurbsPlane3trimmedSurfaceShape1.iog" "surfaceShader3SG.dsm"
		 -na;
connectAttr "nurbsPlane3trimmedSurfaceShape2.iog" "surfaceShader3SG.dsm"
		 -na;
connectAttr "surfaceShader3SG.msg" "materialInfo3.sg";
connectAttr "curveShape3.ws" "projectCurve17.ic";
connectAttr "nurbsPlane1trimmedSurfaceShape1.ws" "projectCurve17.is";
connectAttr "curveShape3.ws" "projectCurve18.ic";
connectAttr "nurbsPlane2trimmedSurfaceShape1.ws" "projectCurve18.is";
connectAttr "curveShape4.ws" "projectCurve19.ic";
connectAttr "nurbsPlane1trimmedSurfaceShape1.ws" "projectCurve19.is";
connectAttr "curveShape4.ws" "projectCurve20.ic";
connectAttr "nurbsPlane2trimmedSurfaceShape1.ws" "projectCurve20.is";
connectAttr "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape1->|projectionCurve17|projectionCurve17_1|projectionCurve17_Shape1.ws" "trim3.ic[0]"
		;
connectAttr "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape1->|projectionCurve19|projectionCurve19_1|projectionCurve19_Shape1.ws" "trim3.ic[1]"
		;
connectAttr "nurbsPlane1trimmedSurfaceShape1.l" "trim3.is";
connectAttr "curveShape5.ws" "projectCurve21.ic";
connectAttr "nurbsPlane1trimmedSurfaceShape2.ws" "projectCurve21.is";
connectAttr "pasted__curveShape5.ws" "projectCurve22.ic";
connectAttr "nurbsPlane1trimmedSurfaceShape2.ws" "projectCurve22.is";
connectAttr "projectionCurve18_Shape1.ws" "trim6.ic[0]";
connectAttr "projectionCurve20_Shape1.ws" "trim6.ic[1]";
connectAttr "nurbsPlane2trimmedSurfaceShape1.l" "trim6.is";
connectAttr "curveShape7.ws" "projectCurve23.ic";
connectAttr "nurbsPlaneShape3.ws" "projectCurve23.is";
connectAttr "curve3detachedCurveShape7.ws" "projectCurve24.ic";
connectAttr "nurbsPlaneShape3.ws" "projectCurve24.is";
connectAttr "curveShape8.ws" "projectCurve25.ic";
connectAttr "nurbsPlaneShape3.ws" "projectCurve25.is";
connectAttr "pasted__offsetNurbsCurveShape3.ws" "projectCurve26.ic";
connectAttr "nurbsPlaneShape3.ws" "projectCurve26.is";
connectAttr "curve3detachedCurve6detachedCurveShape4.ws" "projectCurve27.ic"
		;
connectAttr "nurbsPlaneShape3.ws" "projectCurve27.is";
connectAttr "pasted__offsetNurbsCurveShape4.ws" "projectCurve28.ic";
connectAttr "nurbsPlaneShape3.ws" "projectCurve28.is";
connectAttr "curve3detachedCurve6detachedCurveShape5.ws" "projectCurve29.ic"
		;
connectAttr "nurbsPlaneShape3.ws" "projectCurve29.is";
connectAttr "curve3detachedCurveShape8.ws" "projectCurve30.ic";
connectAttr "nurbsPlaneShape3.ws" "projectCurve30.is";
connectAttr "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve1|projectionCurve1_1|projectionCurve1_Shape1.ws" "trim7.ic[0]"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve2|projectionCurve2_1|projectionCurve2_Shape1.ws" "trim7.ic[1]"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve3|projectionCurve3_1|projectionCurve3_Shape1.ws" "trim7.ic[2]"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve4|projectionCurve4_1|projectionCurve4_Shape1.ws" "trim7.ic[3]"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve5|projectionCurve5_1|projectionCurve5_Shape1.ws" "trim7.ic[4]"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve6|projectionCurve6_1|projectionCurve6_Shape1.ws" "trim7.ic[5]"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve7|projectionCurve7_1|projectionCurve7_Shape1.ws" "trim7.ic[6]"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlaneShape3->|projectionCurve8|projectionCurve8_1|projectionCurve8_Shape1.ws" "trim7.ic[7]"
		;
connectAttr "nurbsPlaneShape3.l" "trim7.is";
connectAttr "curveShape9.ws" "projectCurve31.ic";
connectAttr "nurbsPlane3trimmedSurfaceShape.ws" "projectCurve31.is";
connectAttr "curveShape10.ws" "projectCurve32.ic";
connectAttr "nurbsPlane3trimmedSurfaceShape.ws" "projectCurve32.is";
connectAttr "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape->|projectionCurve17|projectionCurve17_1|projectionCurve17_Shape1.ws" "trim8.ic[0]"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape->|projectionCurve19|projectionCurve19_1|projectionCurve19_Shape1.ws" "trim8.ic[1]"
		;
connectAttr "nurbsPlane3trimmedSurfaceShape.l" "trim8.is";
connectAttr "curveShape11.ws" "projectCurve33.ic";
connectAttr "nurbsPlane3trimmedSurfaceShape1.ws" "projectCurve33.is";
connectAttr "pasted__curveShape7.ws" "projectCurve34.ic";
connectAttr "nurbsPlane3trimmedSurfaceShape1.ws" "projectCurve34.is";
connectAttr "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape2->|projectionCurve21|projectionCurve21_1|projectionCurve21_Shape1.ws" "trim9.ic[0]"
		;
connectAttr "|nurbsPlane1|nurbsPlane1trimmedSurfaceShape2->|projectionCurve22|projectionCurve22_1|projectionCurve22_Shape1.ws" "trim9.ic[1]"
		;
connectAttr "nurbsPlane1trimmedSurfaceShape2.l" "trim9.is";
connectAttr "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape1->|projectionCurve21|projectionCurve21_1|projectionCurve21_Shape1.ws" "trim10.ic[0]"
		;
connectAttr "|group13|nurbsPlane3|nurbsPlane3trimmedSurfaceShape1->|projectionCurve22|projectionCurve22_1|projectionCurve22_Shape1.ws" "trim10.ic[1]"
		;
connectAttr "nurbsPlane3trimmedSurfaceShape1.l" "trim10.is";
connectAttr "surfaceShader1SG.pa" ":renderPartition.st" -na;
connectAttr "surfaceShader2SG.pa" ":renderPartition.st" -na;
connectAttr "surfaceShader3SG.pa" ":renderPartition.st" -na;
connectAttr "surfaceShader1.msg" ":defaultShaderList1.s" -na;
connectAttr "surfaceShader2.msg" ":defaultShaderList1.s" -na;
connectAttr "surfaceShader3.msg" ":defaultShaderList1.s" -na;
connectAttr "lightLinker1.msg" ":lightList1.ln" -na;
connectAttr "nurbsPlaneShape1.iog" ":initialShadingGroup.dsm" -na;
connectAttr "nurbsPlaneShape2.iog" ":initialShadingGroup.dsm" -na;
// End of Logo2.ma
