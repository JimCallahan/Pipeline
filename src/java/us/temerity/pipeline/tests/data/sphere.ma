//Maya ASCII 5.0 scene
//Name: sphere.ma
//Last modified: Mon, Feb 23, 2004 06:40:26 PM
requires maya "5.0";
currentUnit -l centimeter -a degree -t ntsc;
fileInfo "application" "maya";
fileInfo "product" "Maya Unlimited 5.0";
fileInfo "version" "5.0";
fileInfo "cutIdentifier" "200304010002";
fileInfo "osv" "Linux 2.4.20-18.8smp #1 SMP Thu May 29 07:20:32 EDT 2003 i686";
createNode transform -s -n "persp";
	setAttr ".v" no;
	setAttr ".t" -type "double3" 2.4875978755376158 4.1181653472326829 0.41181915779041045 ;
	setAttr ".r" -type "double3" -58.521846625260835 80.599999999999966 2.0633714759788525e-14 ;
createNode camera -s -n "perspShape" -p "persp";
	setAttr -k off ".v" no;
	setAttr ".coi" 4.8287704476657822;
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
	setAttr ".t" -type "double3" 0 0 100 ;
createNode camera -s -n "frontShape" -p "front";
	setAttr -k off ".v" no;
	setAttr ".rnd" no;
	setAttr ".coi" 100;
	setAttr ".ow" 30;
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
createNode transform -n "Mesh";
createNode mesh -n "MeshShape" -p "Mesh";
	setAttr -k off ".v";
	setAttr ".uvst[0].uvsn" -type "string" "map1";
	setAttr ".cuvs" -type "string" "map1";
	setAttr -s 242 ".vt";
	setAttr ".vt[0:165]"  0.166667 0.229397 -0.90043402 0 0.246131 -0.91288 
		0 2.0853701e-09 -0.95021701 0.23408499 0.076058798 -0.91288 0.166667 0.70278299 
		-0.60786498 0 0.70643097 -0.628398 -4.1392101e-10 0.49986699 -0.80880201 
		0.25 0.59387499 -0.68164802 0.61688399 0.375682 -0.60786498 0.48755401 0.42128199 
		-0.68164802 0.475402 0.154468 -0.80880201 0.67185599 0.2183 -0.628398 0.33333299 
		0.458794 -0.74234402 0.25 0.344096 -0.83601999 0.26967201 -0.087621801 -0.90043402 
		0.14467201 -0.19912399 -0.91288 0.71988899 0.058662798 -0.60786498 0.64206302 
		-0.0542465 -0.68164802 0.54792202 -0.470599 -0.60786498 0.55132502 -0.33350801 
		-0.68164802 0.29381499 -0.404401 -0.80880201 0.41523001 -0.57151502 -0.628398 
		0.53934503 -0.175244 -0.74234402 0.40450901 -0.131433 -0.83601999 0 -0.28354999 
		-0.90043402 -0.14467201 -0.19912399 -0.91288 0.278249 -0.66652799 -0.60786498 
		0.146817 -0.62740099 -0.68164802 -0.278249 -0.66652799 -0.60786498 -0.146817 
		-0.62740099 -0.68164802 -0.29381499 -0.404401 -0.80880201 -0.41523001 -0.57151502 
		-0.628398 0 -0.567101 -0.74234402 0 -0.42532501 -0.83601999 -0.26967201 -0.087621801 
		-0.90043402 -0.23408499 0.076058798 -0.91288 -0.54792202 -0.470599 -0.60786498 
		-0.55132502 -0.33350801 -0.68164802 -0.71988899 0.058662798 -0.60786498 -0.64206302 
		-0.0542465 -0.68164802 -0.475402 0.154468 -0.80880201 -0.67185599 0.2183 
		-0.628398 -0.53934503 -0.175244 -0.74234402 -0.40450901 -0.131433 -0.83601999 
		-0.166667 0.229397 -0.90043402 -0.61688399 0.375682 -0.60786498 -0.48755401 
		0.42128199 -0.68164802 -0.166667 0.70278299 -0.60786498 -0.25 0.59387499 
		-0.68164802 -0.33333299 0.458794 -0.74234402 -0.25 0.344096 -0.83601999 -0.26967201 
		0.844558 -0.32431501 -0.23408499 0.78249002 -0.47628099 2.9802301e-09 0.84990001 
		-0.42495 -0.14467201 0.90555602 -0.23015 -0.71988899 0.51745701 -0.32431501 
		-0.67185599 0.46443099 -0.47628099 -0.475402 0.65433502 -0.49986699 -0.64206302 
		0.63394499 -0.256322 -0.54792202 0.75414997 0.14907099 -0.55132502 0.758834 
		-0.0065431502 -0.29381499 0.90426898 -3.1026001e-08 -0.41523001 0.81764603 
		0.23015 -0.53934503 0.74234402 -0.175244 -0.40450799 0.80653799 -0.256322 
		-0.88655603 0.0045094099 -0.32431501 -0.81652802 0.019175099 -0.47628099 
		-0.808303 0.26263401 -0.42495 -0.90594101 0.142241 -0.23015 -0.71458799 -0.52475297 
		-0.32431501 -0.649315 -0.49545601 -0.47628099 -0.76921701 -0.249934 -0.49986699 
		-0.80132502 -0.414738 -0.256322 -0.88655603 -0.28806001 0.14907099 -0.89206302 
		-0.28984901 -0.0065431199 -0.950804 -2.48353e-09 2.48353e-09 -0.90594101 
		-0.142241 0.23015 -0.87267798 -0.28354999 -0.175244 -0.89206302 -0.13547701 
		-0.256322 -0.278249 -0.84177202 -0.32431501 -0.27055699 -0.770639 -0.47628099 
		-0.49955899 -0.68758398 -0.42495 -0.41523001 -0.81764603 -0.23015 0.278249 
		-0.84177101 -0.32431501 0.27055699 -0.770639 -0.47628099 0 -0.80880201 -0.49986699 
		0.146817 -0.89026701 -0.256322 0 -0.93217999 0.14907099 0 -0.93796998 -0.0065430901 
		-0.29381499 -0.90426898 2.81286e-08 -0.14467201 -0.90555602 0.23015 0 -0.917588 
		-0.175244 -0.146817 -0.89026701 -0.256322 0.71458799 -0.52475297 -0.32431501 
		0.649315 -0.49545601 -0.47628099 0.49955899 -0.68758398 -0.42495 0.649315 
		-0.64757401 -0.23015 0.88655603 0.0045094099 -0.32431501 0.81652802 0.019175099 
		-0.47628099 0.76921701 -0.249934 -0.49986699 0.89206302 -0.13547701 -0.256322 
		0.88655603 -0.28806001 0.14907099 0.89206302 -0.28984901 -0.0065431199 0.76921701 
		-0.558869 1.38172e-08 0.81652802 -0.41742399 0.23015 0.87267798 -0.28354999 
		-0.175244 0.80132502 -0.414738 -0.256322 0.71988899 0.51745701 -0.32431501 
		0.67185599 0.46443099 -0.47628099 0.808303 0.26263401 -0.42495 0.81652802 
		0.41742399 -0.23015 0.26967201 0.844558 -0.32431501 0.23408499 0.78249002 
		-0.47628099 0.475402 0.65433502 -0.49986699 0.40450799 0.80653799 -0.256322 
		0.54792202 0.75414997 0.14907099 0.55132502 0.758834 -0.0065431502 0.76921701 
		0.558869 -1.25755e-08 0.649315 0.64757401 0.23015 0.53934503 0.74234402 -0.175244 
		0.64206302 0.63394499 -0.256322 0 0.93217999 -0.14907099 0.14467201 0.90555602 
		-0.23015 -0.278249 0.84177101 0.32431501 -0.146817 0.89026701 0.256322 0.278249 
		0.84177202 0.32431501 0.146817 0.89026701 0.256322 0.29381499 0.90426898 
		-2.89564e-08 0.41523001 0.81764603 0.23015 0 0.917588 0.175244 0 0.93796998 
		0.0065430901 -0.88655603 0.28806001 -0.14907099 -0.81652802 0.41742399 -0.23015 
		-0.88655603 -0.0045094099 0.32431501 -0.89206302 0.13547701 0.256322 -0.71458799 
		0.52475297 0.32431501 -0.80132502 0.414738 0.256322 -0.76921701 0.558869 
		-1.21616e-08 -0.649315 0.64757401 0.23015 -0.87267798 0.28354999 0.175244 
		-0.89206302 0.28984901 0.0065431199 -0.54792202 -0.75414997 -0.14907099 -0.649315 
		-0.64757401 -0.23015 -0.26967201 -0.844558 0.32431501 -0.40450799 -0.80653799 
		0.256322 -0.71988899 -0.51745701 0.32431501 -0.64206302 -0.63394499 0.256322 
		-0.76921701 -0.558869 1.34033e-08 -0.81652802 -0.41742399 0.23015 -0.53934503 
		-0.74234402 0.175244 -0.55132502 -0.758834 0.0065431502 0.54792202 -0.75414997 
		-0.14907099 0.41523001 -0.81764603 -0.23015 0.71988899 -0.51745701 0.32431501 
		0.64206302 -0.63394499 0.256322 0.26967201 -0.844558 0.32431501 0.40450799 
		-0.80653799 0.256322 0.29381499 -0.90426898 2.68868e-08 0.14467201 -0.90555602 
		0.23015 0.53934503 -0.74234402 0.175244 0.55132502 -0.758834 0.0065431502 
		0.88655603 0.28806001 -0.14907099 0.90594101 0.142241 -0.23015 0.71458799 
		0.52475297 0.32431501 0.80132502 0.414738 0.256322 0.88655603 -0.0045094099 
		0.32431501;
	setAttr ".vt[166:241]" 0.89206302 0.13547701 0.256322 0.950804 -2.48353e-09 
		8.2784202e-10 0.90594101 -0.142241 0.23015 0.87267798 0.28354999 0.175244 
		0.89206302 0.28984901 0.0065431199 0 0.28354999 0.90043402 0.14467201 0.19912399 
		0.91288 1.19209e-09 2.98812e-10 0.95021701 -0.14467201 0.19912399 0.91288 
		0.278249 0.66652799 0.60786498 0.41523001 0.57151502 0.628398 0.29381499 
		0.404401 0.80880201 0.146817 0.62740099 0.68164802 -0.278249 0.66652799 0.60786498 
		-0.146817 0.62740099 0.68164802 -0.29381499 0.404401 0.80880201 -0.41523001 
		0.57151502 0.628398 0 0.567101 0.74234402 0 0.42532501 0.83601999 0.26967201 
		0.087621801 0.90043402 0.23408499 -0.076058798 0.91288 0.71988899 -0.058662798 
		0.60786498 0.67185599 -0.2183 0.628398 0.475402 -0.154468 0.80880201 0.64206302 
		0.0542465 0.68164802 0.54792202 0.470599 0.60786498 0.55132502 0.33350801 
		0.68164802 0.53934503 0.175244 0.74234402 0.40450901 0.131433 0.83601999 
		0.166667 -0.229397 0.90043402 0 -0.246131 0.91288 0.166667 -0.70278299 0.60786498 
		0 -0.70643097 0.628398 -4.1392101e-10 -0.49986699 0.80880201 0.25 -0.59387499 
		0.68164802 0.61688399 -0.375682 0.60786498 0.48755401 -0.42128199 0.68164802 
		0.33333299 -0.458794 0.74234402 0.25 -0.344096 0.83601999 -0.166667 -0.229397 
		0.90043402 -0.23408499 -0.076058798 0.91288 -0.61688399 -0.375682 0.60786498 
		-0.67185599 -0.2183 0.628398 -0.475402 -0.154468 0.80880201 -0.48755401 -0.42128199 
		0.68164802 -0.166667 -0.70278299 0.60786498 -0.25 -0.59387499 0.68164802 
		-0.33333299 -0.458794 0.74234402 -0.25 -0.344096 0.83601999 -0.26967201 0.087621801 
		0.90043402 -0.54792202 0.470599 0.60786498 -0.55132502 0.33350801 0.68164802 
		-0.71988899 -0.058662798 0.60786498 -0.64206302 0.0542465 0.68164802 -0.53934503 
		0.175244 0.74234402 -0.40450901 0.131433 0.83601999 -0.49955899 0.68758398 
		0.42495 -0.808303 -0.26263401 0.42495 1.19209e-09 -0.84990001 0.42495 0.808303 
		-0.26263401 0.42495 0.49955899 0.68758398 0.42495 -8.2784202e-10 0.80880201 
		0.49986699 -0.27055699 0.770639 0.47628099 0.27055699 0.770639 0.47628099 
		-0.76921701 0.249934 0.49986699 -0.81652802 -0.019175099 0.47628099 -0.649315 
		0.49545601 0.47628099 -0.475402 -0.65433502 0.49986699 -0.23408499 -0.78249002 
		0.47628099 -0.67185599 -0.46443099 0.47628099 0.475402 -0.65433502 0.49986699 
		0.67185599 -0.46443099 0.47628099 0.23408499 -0.78249002 0.47628099 0.76921701 
		0.249934 0.49986699 0.649315 0.49545601 0.47628099 0.81652802 -0.019175099 
		0.47628099;
	setAttr -s 480 ".ed";
	setAttr ".ed[0:165]"  0 1 0 1 2 0 2 3 0 3 0 0 4 5 0 5 6 0 6 7 0 7 4 0 8 
		9 0 9 10 0 10 11 0 11 8 0 12 7 0 6 13 0 13 12 0 14 3 0 2 15 0 15 14 0 16 
		11 0 10 17 0 17 16 0 18 19 0 19 20 0 20 21 0 21 18 0 22 17 0 10 23 0 23 22 
		0 24 15 0 2 25 0 25 24 0 26 21 0 20 27 0 27 26 0 28 29 0 29 30 0 30 31 0 
		31 28 0 32 27 0 20 33 0 33 32 0 34 25 0 2 35 0 35 34 0 36 31 0 30 37 0 37 
		36 0 38 39 0 39 40 0 40 41 0 41 38 0 42 37 0 30 43 0 43 42 0 44 35 0 1 44 
		0 45 41 0 40 46 0 46 45 0 47 48 0 48 6 0 5 47 0 49 46 0 40 50 0 50 49 0 51 
		52 0 52 53 0 53 54 0 54 51 0 55 56 0 56 57 0 57 58 0 58 55 0 59 60 0 60 61 
		0 61 62 0 62 59 0 63 58 0 57 64 0 64 63 0 65 66 0 66 67 0 67 68 0 68 65 0 
		69 70 0 70 71 0 71 72 0 72 69 0 73 74 0 74 75 0 75 76 0 76 73 0 77 72 0 71 
		78 0 78 77 0 79 80 0 80 81 0 81 82 0 82 79 0 83 84 0 84 85 0 85 86 0 86 83 
		0 87 88 0 88 89 0 89 90 0 90 87 0 91 86 0 85 92 0 92 91 0 93 94 0 94 95 0 
		95 96 0 96 93 0 97 98 0 98 99 0 99 100 0 100 97 0 101 102 0 102 103 0 103 
		104 0 104 101 0 105 100 0 99 106 0 106 105 0 107 108 0 108 109 0 109 110 
		0 110 107 0 111 112 0 112 113 0 113 114 0 114 111 0 115 116 0 116 117 0 117 
		118 0 118 115 0 119 114 0 113 120 0 120 119 0 121 54 0 53 122 0 122 121 0 
		123 62 0 61 124 0 124 123 0 125 126 0 126 127 0 127 128 0 128 125 0 129 124 
		0 61 130 0 130 129 0 131 68 0 67 132 0 132 131 0 133 76 0 75 134 0 134 133 
		0 135 136 0 136 137 0 137 138 0 138 135 0 139 134 0 75 140 0 140 139 0;
	setAttr ".ed[166:331]" 141 82 0 81 142 0 142 141 0 143 90 0 89 144 0 144 
		143 0 145 146 0 146 147 0 147 148 0 148 145 0 149 144 0 89 150 0 150 149 
		0 151 96 0 95 152 0 152 151 0 153 104 0 103 154 0 154 153 0 155 156 0 156 
		157 0 157 158 0 158 155 0 159 154 0 103 160 0 160 159 0 161 110 0 109 162 
		0 162 161 0 163 118 0 117 164 0 164 163 0 165 166 0 166 167 0 167 168 0 168 
		165 0 169 164 0 117 170 0 170 169 0 171 172 0 172 173 0 173 174 0 174 171 
		0 175 176 0 176 177 0 177 178 0 178 175 0 179 180 0 180 181 0 181 182 0 182 
		179 0 183 178 0 177 184 0 184 183 0 185 186 0 186 173 0 172 185 0 187 188 
		0 188 189 0 189 190 0 190 187 0 191 192 0 192 177 0 176 191 0 193 190 0 189 
		194 0 194 193 0 195 196 0 196 173 0 186 195 0 197 198 0 198 199 0 199 200 
		0 200 197 0 201 202 0 202 189 0 188 201 0 203 200 0 199 204 0 204 203 0 205 
		206 0 206 173 0 196 205 0 207 208 0 208 209 0 209 210 0 210 207 0 211 212 
		0 212 199 0 198 211 0 213 210 0 209 214 0 214 213 0 215 174 0 206 215 0 216 
		182 0 181 217 0 217 216 0 218 219 0 219 209 0 208 218 0 220 217 0 181 221 
		0 221 220 0 10 13 0 13 0 0 3 10 0 6 1 0 112 4 0 7 113 0 112 53 0 53 5 0 108 
		8 0 11 109 0 108 113 0 113 9 0 9 12 0 20 23 0 23 14 0 15 20 0 98 16 0 17 
		99 0 98 109 0 94 18 0 21 95 0 94 99 0 99 19 0 19 22 0 30 33 0 33 24 0 25 
		30 0 84 26 0 27 85 0 84 95 0 80 28 0 31 81 0 80 85 0 85 29 0 29 32 0 40 43 
		0 43 34 0 35 40 0 70 36 0 37 71 0 70 81 0 66 38 0 41 67 0 66 71 0 71 39 0 
		39 42 0 6 50 0 50 44 0 56 45 0 46 57 0 56 67 0 52 47 0 52 57 0 57 48 0 48 
		49 0 61 64 0 64 51 0 54 61 0 137 132 0 132 55 0 58 137 0 222 138 0;
	setAttr ".ed[332:479]" 138 59 0 62 222 0 137 60 0 60 63 0 75 78 0 78 65 
		0 68 75 0 147 142 0 142 69 0 72 147 0 223 148 0 148 73 0 76 223 0 147 74 
		0 74 77 0 89 92 0 92 79 0 82 89 0 157 152 0 152 83 0 86 157 0 224 158 0 158 
		87 0 90 224 0 157 88 0 88 91 0 103 106 0 106 93 0 96 103 0 167 162 0 162 
		97 0 100 167 0 225 168 0 168 101 0 104 225 0 167 102 0 102 105 0 117 120 
		0 120 107 0 110 117 0 127 122 0 122 111 0 114 127 0 226 128 0 128 115 0 118 
		226 0 127 116 0 116 119 0 127 130 0 130 121 0 227 228 0 228 123 0 124 227 
		0 228 222 0 226 229 0 229 125 0 229 227 0 227 126 0 126 129 0 137 140 0 140 
		131 0 230 231 0 231 133 0 134 230 0 231 223 0 222 232 0 232 135 0 232 230 
		0 230 136 0 136 139 0 147 150 0 150 141 0 233 234 0 234 143 0 144 233 0 234 
		224 0 223 235 0 235 145 0 235 233 0 233 146 0 146 149 0 157 160 0 160 151 
		0 236 237 0 237 153 0 154 236 0 237 225 0 224 238 0 238 155 0 238 236 0 236 
		156 0 156 159 0 167 170 0 170 161 0 239 240 0 240 163 0 164 239 0 240 226 
		0 225 241 0 241 165 0 241 239 0 239 166 0 166 169 0 181 184 0 184 171 0 174 
		181 0 177 172 0 229 175 0 178 227 0 226 176 0 228 179 0 182 222 0 227 180 
		0 180 183 0 177 194 0 194 185 0 189 186 0 241 187 0 190 239 0 225 188 0 240 
		191 0 239 192 0 192 193 0 189 204 0 204 195 0 199 196 0 238 197 0 200 236 
		0 224 198 0 237 201 0 236 202 0 202 203 0 199 214 0 214 205 0 209 206 0 235 
		207 0 210 233 0 223 208 0 234 211 0 233 212 0 212 213 0 209 221 0 221 215 
		0 232 216 0 217 230 0 231 218 0 230 219 0 219 220 0;
	setAttr -s 240 ".fc[0:239]" -type "polyFaces" 
		f 4 0 1 2 3 
		f 4 4 5 6 7 
		f 4 8 9 10 11 
		f 4 12 -7 13 14 
		f 4 15 -3 16 17 
		f 4 18 -11 19 20 
		f 4 21 22 23 24 
		f 4 25 -20 26 27 
		f 4 28 -17 29 30 
		f 4 31 -24 32 33 
		f 4 34 35 36 37 
		f 4 38 -33 39 40 
		f 4 41 -30 42 43 
		f 4 44 -37 45 46 
		f 4 47 48 49 50 
		f 4 51 -46 52 53 
		f 4 54 -43 -2 55 
		f 4 56 -50 57 58 
		f 4 59 60 -6 61 
		f 4 62 -58 63 64 
		f 4 65 66 67 68 
		f 4 69 70 71 72 
		f 4 73 74 75 76 
		f 4 77 -72 78 79 
		f 4 80 81 82 83 
		f 4 84 85 86 87 
		f 4 88 89 90 91 
		f 4 92 -87 93 94 
		f 4 95 96 97 98 
		f 4 99 100 101 102 
		f 4 103 104 105 106 
		f 4 107 -102 108 109 
		f 4 110 111 112 113 
		f 4 114 115 116 117 
		f 4 118 119 120 121 
		f 4 122 -117 123 124 
		f 4 125 126 127 128 
		f 4 129 130 131 132 
		f 4 133 134 135 136 
		f 4 137 -132 138 139 
		f 4 140 -68 141 142 
		f 4 143 -76 144 145 
		f 4 146 147 148 149 
		f 4 150 -145 151 152 
		f 4 153 -83 154 155 
		f 4 156 -91 157 158 
		f 4 159 160 161 162 
		f 4 163 -158 164 165 
		f 4 166 -98 167 168 
		f 4 169 -106 170 171 
		f 4 172 173 174 175 
		f 4 176 -171 177 178 
		f 4 179 -113 180 181 
		f 4 182 -121 183 184 
		f 4 185 186 187 188 
		f 4 189 -184 190 191 
		f 4 192 -128 193 194 
		f 4 195 -136 196 197 
		f 4 198 199 200 201 
		f 4 202 -197 203 204 
		f 4 205 206 207 208 
		f 4 209 210 211 212 
		f 4 213 214 215 216 
		f 4 217 -212 218 219 
		f 4 220 221 -207 222 
		f 4 223 224 225 226 
		f 4 227 228 -211 229 
		f 4 230 -226 231 232 
		f 4 233 234 -222 235 
		f 4 236 237 238 239 
		f 4 240 241 -225 242 
		f 4 243 -239 244 245 
		f 4 246 247 -235 248 
		f 4 249 250 251 252 
		f 4 253 254 -238 255 
		f 4 256 -252 257 258 
		f 4 259 -208 -248 260 
		f 4 261 -216 262 263 
		f 4 264 265 -251 266 
		f 4 267 -263 268 269 
		f 4 270 271 -4 272 
		f 4 -14 273 -1 -272 
		f 4 -131 274 -8 275 
		f 4 276 277 -5 -275 
		f 4 -127 278 -12 279 
		f 4 280 281 -9 -279 
		f 4 -10 282 -15 -271 
		f 4 -282 -276 -13 -283 
		f 4 283 284 -18 285 
		f 4 -27 -273 -16 -285 
		f 4 -116 286 -21 287 
		f 4 288 -280 -19 -287 
		f 4 -112 289 -25 290 
		f 4 291 292 -22 -290 
		f 4 -23 293 -28 -284 
		f 4 -293 -288 -26 -294 
		f 4 294 295 -31 296 
		f 4 -40 -286 -29 -296 
		f 4 -101 297 -34 298 
		f 4 299 -291 -32 -298 
		f 4 -97 300 -38 301 
		f 4 302 303 -35 -301 
		f 4 -36 304 -41 -295 
		f 4 -304 -299 -39 -305 
		f 4 305 306 -44 307 
		f 4 -53 -297 -42 -307 
		f 4 -86 308 -47 309 
		f 4 310 -302 -45 -309 
		f 4 -82 311 -51 312 
		f 4 313 314 -48 -312 
		f 4 -49 315 -54 -306 
		f 4 -315 -310 -52 -316 
		f 4 316 317 -56 -274 
		f 4 -64 -308 -55 -318 
		f 4 -71 318 -59 319 
		f 4 320 -313 -57 -319 
		f 4 -67 321 -62 -278 
		f 4 322 323 -60 -322 
		f 4 -61 324 -65 -317 
		f 4 -324 -320 -63 -325 
		f 4 325 326 -69 327 
		f 4 -79 -323 -66 -327 
		f 4 328 329 -73 330 
		f 4 -155 -321 -70 -330 
		f 4 331 332 -77 333 
		f 4 -162 334 -74 -333 
		f 4 -75 335 -80 -326 
		f 4 -335 -331 -78 -336 
		f 4 336 337 -84 338 
		f 4 -94 -314 -81 -338 
		f 4 339 340 -88 341 
		f 4 -168 -311 -85 -341 
		f 4 342 343 -92 344 
		f 4 -175 345 -89 -344 
		f 4 -90 346 -95 -337 
		f 4 -346 -342 -93 -347 
		f 4 347 348 -99 349 
		f 4 -109 -303 -96 -349 
		f 4 350 351 -103 352 
		f 4 -181 -300 -100 -352 
		f 4 353 354 -107 355 
		f 4 -188 356 -104 -355 
		f 4 -105 357 -110 -348 
		f 4 -357 -353 -108 -358 
		f 4 358 359 -114 360 
		f 4 -124 -292 -111 -360 
		f 4 361 362 -118 363 
		f 4 -194 -289 -115 -363 
		f 4 364 365 -122 366 
		f 4 -201 367 -119 -366 
		f 4 -120 368 -125 -359 
		f 4 -368 -364 -123 -369 
		f 4 369 370 -129 371 
		f 4 -139 -281 -126 -371 
		f 4 372 373 -133 374 
		f 4 -142 -277 -130 -374 
		f 4 375 376 -137 377 
		f 4 -149 378 -134 -377 
		f 4 -135 379 -140 -370 
		f 4 -379 -375 -138 -380 
		f 4 380 381 -143 -373 
		f 4 -152 -328 -141 -382 
		f 4 382 383 -146 384 
		f 4 385 -334 -144 -384 
		f 4 386 387 -150 -376 
		f 4 388 389 -147 -388 
		f 4 -148 390 -153 -381 
		f 4 -390 -385 -151 -391 
		f 4 391 392 -156 -329 
		f 4 -165 -339 -154 -393 
		f 4 393 394 -159 395 
		f 4 396 -345 -157 -395 
		f 4 397 398 -163 -332 
		f 4 399 400 -160 -399 
		f 4 -161 401 -166 -392 
		f 4 -401 -396 -164 -402 
		f 4 402 403 -169 -340 
		f 4 -178 -350 -167 -404 
		f 4 404 405 -172 406 
		f 4 407 -356 -170 -406 
		f 4 408 409 -176 -343 
		f 4 410 411 -173 -410 
		f 4 -174 412 -179 -403 
		f 4 -412 -407 -177 -413 
		f 4 413 414 -182 -351 
		f 4 -191 -361 -180 -415 
		f 4 415 416 -185 417 
		f 4 418 -367 -183 -417 
		f 4 419 420 -189 -354 
		f 4 421 422 -186 -421 
		f 4 -187 423 -192 -414 
		f 4 -423 -418 -190 -424 
		f 4 424 425 -195 -362 
		f 4 -204 -372 -193 -426 
		f 4 426 427 -198 428 
		f 4 429 -378 -196 -428 
		f 4 430 431 -202 -365 
		f 4 432 433 -199 -432 
		f 4 -200 434 -205 -425 
		f 4 -434 -429 -203 -435 
		f 4 435 436 -209 437 
		f 4 -219 438 -206 -437 
		f 4 -389 439 -213 440 
		f 4 -387 441 -210 -440 
		f 4 -386 442 -217 443 
		f 4 -383 444 -214 -443 
		f 4 -215 445 -220 -436 
		f 4 -445 -441 -218 -446 
		f 4 446 447 -223 -439 
		f 4 -232 448 -221 -448 
		f 4 -433 449 -227 450 
		f 4 -431 451 -224 -450 
		f 4 -430 452 -230 -442 
		f 4 -427 453 -228 -453 
		f 4 -229 454 -233 -447 
		f 4 -454 -451 -231 -455 
		f 4 455 456 -236 -449 
		f 4 -245 457 -234 -457 
		f 4 -422 458 -240 459 
		f 4 -420 460 -237 -459 
		f 4 -419 461 -243 -452 
		f 4 -416 462 -241 -462 
		f 4 -242 463 -246 -456 
		f 4 -463 -460 -244 -464 
		f 4 464 465 -249 -458 
		f 4 -258 466 -247 -466 
		f 4 -411 467 -253 468 
		f 4 -409 469 -250 -468 
		f 4 -408 470 -256 -461 
		f 4 -405 471 -254 -471 
		f 4 -255 472 -259 -465 
		f 4 -472 -469 -257 -473 
		f 4 473 474 -261 -467 
		f 4 -269 -438 -260 -475 
		f 4 -400 475 -264 476 
		f 4 -398 -444 -262 -476 
		f 4 -397 477 -267 -470 
		f 4 -394 478 -265 -478 
		f 4 -266 479 -270 -474 
		f 4 -479 -477 -268 -480 ;
createNode lightLinker -n "lightLinker1";
createNode brush -n "brush1";
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
		+ "                -camera \"persp\" \n"
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
		+ "\t\tmodelPanel -edit -l \"Persp View\" -mbv $menusOkayInPanels  $panelName;\n"
		+ "\t\t$editorName = $panelName;\n"
		+ "        modelEditor -e \n"
		+ "            -camera \"persp\" \n"
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
		+ "                -showDagOnly 0\n"
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
		+ "            -showDagOnly 0\n"
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
		+ "\t\t\t\t\t\"$panelName = `modelPanel -unParent -l \\\"Persp View\\\" -mbv $menusOkayInPanels `;\\n$editorName = $panelName;\\nmodelEditor -e \\n    -cam `findStartUpCamera persp` \\n    -useInteractiveMode 0\\n    -displayLights \\\"default\\\" \\n    -displayAppearance \\\"wireframe\\\" \\n    -activeOnly 0\\n    -wireframeOnShaded 0\\n    -bufferMode \\\"double\\\" \\n    -twoSidedLighting 1\\n    -backfaceCulling 0\\n    -xray 0\\n    -displayTextures 0\\n    -smoothWireframe 0\\n    -textureAnisotropic 0\\n    -textureHilight 1\\n    -textureSampling 2\\n    -textureDisplay \\\"modulate\\\" \\n    -textureMaxSize 1024\\n    -fogging 0\\n    -fogSource \\\"fragment\\\" \\n    -fogMode \\\"linear\\\" \\n    -fogStart 0\\n    -fogEnd 100\\n    -fogDensity 0.1\\n    -fogColor 0.5 0.5 0.5 1 \\n    -sortTransparent 1\\n    -nurbsCurves 1\\n    -nurbsSurfaces 1\\n    -polymeshes 1\\n    -subdivSurfaces 1\\n    -planes 1\\n    -lights 1\\n    -cameras 1\\n    -controlVertices 1\\n    -hulls 1\\n    -grid 1\\n    -joints 1\\n    -ikHandles 1\\n    -deformers 1\\n    -dynamics 1\\n    -fluids 1\\n    -locators 1\\n    -dimensions 1\\n    -handles 1\\n    -pivots 1\\n    -textures 1\\n    -strokes 1\\n    -shadows 0\\n    $editorName;\\nmodelEditor -e -viewSelected 0 $editorName\"\n"
		+ "\t\t\t\t\t\"modelPanel -edit -l \\\"Persp View\\\" -mbv $menusOkayInPanels  $panelName;\\n$editorName = $panelName;\\nmodelEditor -e \\n    -cam `findStartUpCamera persp` \\n    -useInteractiveMode 0\\n    -displayLights \\\"default\\\" \\n    -displayAppearance \\\"wireframe\\\" \\n    -activeOnly 0\\n    -wireframeOnShaded 0\\n    -bufferMode \\\"double\\\" \\n    -twoSidedLighting 1\\n    -backfaceCulling 0\\n    -xray 0\\n    -displayTextures 0\\n    -smoothWireframe 0\\n    -textureAnisotropic 0\\n    -textureHilight 1\\n    -textureSampling 2\\n    -textureDisplay \\\"modulate\\\" \\n    -textureMaxSize 1024\\n    -fogging 0\\n    -fogSource \\\"fragment\\\" \\n    -fogMode \\\"linear\\\" \\n    -fogStart 0\\n    -fogEnd 100\\n    -fogDensity 0.1\\n    -fogColor 0.5 0.5 0.5 1 \\n    -sortTransparent 1\\n    -nurbsCurves 1\\n    -nurbsSurfaces 1\\n    -polymeshes 1\\n    -subdivSurfaces 1\\n    -planes 1\\n    -lights 1\\n    -cameras 1\\n    -controlVertices 1\\n    -hulls 1\\n    -grid 1\\n    -joints 1\\n    -ikHandles 1\\n    -deformers 1\\n    -dynamics 1\\n    -fluids 1\\n    -locators 1\\n    -dimensions 1\\n    -handles 1\\n    -pivots 1\\n    -textures 1\\n    -strokes 1\\n    -shadows 0\\n    $editorName;\\nmodelEditor -e -viewSelected 0 $editorName\"\n"
		+ "\t\t\t\t$configName;\n"
		+ "            setNamedPanelLayout \"Current Layout\";\n"
		+ "        }\n"
		+ "        panelHistory -e -clear mainPanelHistory;\n"
		+ "        setFocus `paneLayout -q -p1 $gMainPane`;\n"
		+ "        sceneUIReplacement -deleteRemaining;\n"
		+ "        sceneUIReplacement -clear;\n"
		+ "\t}\n"
		+ "grid -spacing 0.5 -size 200 -divisions 1 -displayAxes yes -displayGridLines yes -displayDivisionLines yes -displayPerspectiveLabels no -displayOrthographicLabels no -displayAxesBold yes -perspectiveLabelPosition axis -orthographicLabelPosition edge;\n"
		+ "}\n");
	setAttr ".st" 3;
createNode script -n "sceneConfigurationScriptNode";
	setAttr ".b" -type "string" "playbackOptions -min 130 -max 162 -ast 130 -aet 162 ";
	setAttr ".st" 6;
select -ne :time1;
	setAttr ".o" 130;
select -ne :renderPartition;
	setAttr -s 2 ".st";
select -ne :renderGlobalsList1;
select -ne :defaultShaderList1;
	setAttr -s 2 ".s";
select -ne :postProcessList1;
	setAttr -s 2 ".p";
select -ne :lightList1;
select -ne :initialShadingGroup;
	setAttr ".ro" yes;
select -ne :initialParticleSE;
	setAttr ".ro" yes;
select -ne :defaultRenderGlobals;
	addAttr -ci true -sn "currentRenderer" -ln "currentRenderer" -dt "string";
	setAttr ".fs" 1;
	setAttr ".ef" 10;
	setAttr ".currentRenderer" -type "string" "mayaSoftware";
select -ne :defaultHardwareRenderGlobals;
	setAttr ".fn" -type "string" "im";
	setAttr ".res" -type "string" "ntsc_4d 646 485 1.333";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[0].llnk";
connectAttr ":initialShadingGroup.msg" "lightLinker1.lnk[0].olnk";
connectAttr ":defaultLightSet.msg" "lightLinker1.lnk[1].llnk";
connectAttr ":initialParticleSE.msg" "lightLinker1.lnk[1].olnk";
connectAttr "layerManager.dli[0]" "defaultLayer.id";
connectAttr "renderLayerManager.rlmi[0]" "defaultRenderLayer.rlid";
connectAttr "lightLinker1.msg" ":lightList1.ln" -na;
connectAttr "MeshShape.iog" ":initialShadingGroup.dsm" -na;
// End of sphere.ma
