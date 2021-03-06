#!/bin/bash

# CREATE THE (double, int, long) VECTOR CLASSES FROM THE (float) VERSIONS

for f in Tuple*f.java Vector*f.java Point*f.java
do 
  d=`echo $f | sed -e 's/f.java/d.java/g'`

  echo "// DO NOT EDIT! -- Automatically generated by: permutations.bash" > $d
  cat $f | sed -e 's/%1$+.4f/%1$+.6f/g' \
         | sed -e 's/float/double/g' | sed -e 's/.0f/.0/g' \
         | sed -e 's/Nf/Nd/g' | sed -e 's/N F/N D/g' \
         | sed -e 's/2f/2d/g' | sed -e 's/2 F/2 D/g' \
         | sed -e 's/3f/3d/g' | sed -e 's/3 F/3 D/g' \
         | sed -e 's/4f/4d/g' | sed -e 's/4 F/4 D/g' \
         | tail +2 >> $d

  i=`echo $f | sed -e 's/f.java/i.java/g'`

  echo "// DO NOT EDIT! -- Automatically generated by: permutations.bash" > $i
  cat $f | sed -e 's/String.format("%1$+.4f", pComps\[i\])/pComps[i]/g' \
         | sed -e 's/float/int/g' | sed -e 's/.0f//g' \
         | sed -e 's/Nf/Ni/g' | sed -e 's/N F/N I/g' \
         | sed -e 's/2f/2i/g' | sed -e 's/2 F/2 I/g' \
         | sed -e 's/3f/3i/g' | sed -e 's/3 F/3 I/g' \
         | sed -e 's/4f/4i/g' | sed -e 's/4 F/4 I/g' \
         | tail +2 >> $i

  l=`echo $f | sed -e 's/f.java/l.java/g'`

  echo "// DO NOT EDIT! -- Automatically generated by: permutations.bash" > $l
  cat $f | sed -e 's/String.format("%1$+.4f", pComps\[i\])/pComps[i]/g' \
         | sed -e 's/float/long/g' | sed -e 's/.0f/L/g' \
         | sed -e 's/Nf/Nl/g' | sed -e 's/N F/N L/g' \
         | sed -e 's/2f/2l/g' | sed -e 's/2 F/2 L/g' \
         | sed -e 's/3f/3l/g' | sed -e 's/3 F/3 L/g' \
         | sed -e 's/4f/4l/g' | sed -e 's/4 F/4 L/g' \
         | tail +2 >> $l
done


# REMOVE THE lerp() METHODS FROM (int, long) VERSIONS

for x in Point*[il].java
do
  mv $x ORIG
  head -145 ORIG > $x
  tail +174 ORIG >> $x
  rm -f ORIG
done

for x in Vector*[il].java
do
  mv $x ORIG
  head -175 ORIG > $x
  tail +204 ORIG >> $x
  rm -f ORIG
done

for x in TupleN[il].java
do
  mv $x ORIG
  head -373 ORIG > $x
  tail +417 ORIG >> $x
  rm -f ORIG
done


# CREATE THE (double) VERSIONS OF THE COLOR CLASSES FROM THE (float) VERSIONS

for f in Color*f.java
do 
  d=`echo $f | sed -e 's/f.java/d.java/g'`

  echo "// DO NOT EDIT! -- Automatically generated by: permutations.bash" > $d
  cat $f | sed -e 's/float/double/g' | sed -e 's/.0f/.0/g' \
         | sed -e 's/Nf/Nd/g' | sed -e 's/N F/N D/g' \
         | sed -e 's/2f/2d/g' | sed -e 's/2 F/2 D/g' \
         | sed -e 's/3f/3d/g' | sed -e 's/3 F/3 D/g' \
         | sed -e 's/4f/4d/g' | sed -e 's/4 F/4 D/g' \
         | tail +2 >> $d
done


# CREATE THE (double) VERSIONS OF THE MATRIX CLASSES FROM THE (float) VERSIONS

for f in Matrix??f.java 
do 
  d=`echo $f | sed -e 's/f.java/d.java/g'`

  echo "// DO NOT EDIT! -- Automatically generated by: permutations.bash" > $d
  cat $f | sed -e 's/%1$+.4f/%1$+.6f/g' \
         | sed -e 's/float/double/g' | sed -e 's/.0f/.0/g' \
         | sed -e 's/MNf/MNd/g' | sed -e 's/Nf/Nd/g' | sed -e 's/M N F/M N D/g' \
         | sed -e 's/33f/33d/g' | sed -e 's/3 3 F/3 3 D/g' \
         | sed -e 's/2f/2d/g' | sed -e 's/2 F/2 D/g' \
         | sed -e 's/3f/3d/g' | sed -e 's/3 F/3 D/g' \
         | sed -e 's/4f/4d/g' | sed -e 's/4 F/4 D/g' \
         | tail +2 >> $d
done


# CREATE THE (double) VERSIONS OF THE COORD SYS CLASSES FROM THE (float) VERSIONS

for f in CoordSys?f.java 
do 
  d=`echo $f | sed -e 's/f.java/d.java/g'`

  echo "// DO NOT EDIT! -- Automatically generated by: permutations.bash" > $d
  cat $f | sed -e 's/%1$+.4f/%1$+.6f/g' \
         | sed -e 's/float/double/g' | sed -e 's/.0f/.0/g' \
         | sed -e 's/Nf/Nd/g' | sed -e 's/N F/N D/g' \
         | sed -e 's/2f/2d/g' | sed -e 's/2 F/2 D/g' \
         | sed -e 's/3f/3d/g' | sed -e 's/3 F/3 D/g' \
         | sed -e 's/4f/4d/g' | sed -e 's/4 F/4 D/g' \
         | tail +2 >> $d
done


# CREATE THE (double) VERSIONS OF THE BOUNDING BOX CLASSES FROM THE (float) VERSIONS

echo "// DO NOT EDIT! -- Automatically generated by: permutations.bash" > BBox3f.java
cat BBox2f.java | sed -e 's/2f/3f/g' | sed -e 's/2 F/3 F/g' \
                | tail +2 >> BBox3f.java

echo "// DO NOT EDIT! -- Automatically generated by: permutations.bash" > BBox4f.java
cat BBox2f.java | sed -e 's/2f/4f/g' | sed -e 's/2 F/4 F/g' \
                | tail +2 >> BBox4f.java

for f in BBox*f.java
do 
  d=`echo $f | sed -e 's/f.java/d.java/g'`

  echo "// DO NOT EDIT! -- Automatically generated by: permutations.bash" > $d
  cat $f | sed -e 's/float/double/g' | sed -e 's/.0f/.0/g' \
         | sed -e 's/2f/2d/g' | sed -e 's/2 F/2 D/g' \
         | sed -e 's/3f/3d/g' | sed -e 's/3 F/3 D/g' \
         | sed -e 's/4f/4d/g' | sed -e 's/4 F/4 D/g' \
         | tail +2 >> $d
done


# FIX THE SERIALIZATION IDS 

function replaceUID
{
  mv $1.java ORIG
  cat ORIG | sed -e 's/serialVersionUID = -*[0-9]*/serialVersionUID = '$2'/g' > $1.java
  rm -f ORIG
}

replaceUID Tuple2d   -2835770189069865341
#replaceUID Tuple2f   4287878958595610861
replaceUID Tuple2i   -3460530107208894367
replaceUID Tuple2l   -6624925205908032618
replaceUID Tuple3d   -143283502929447629
#replaceUID Tuple3f   3183566835872070149
replaceUID Tuple3i   -2338526748637687923
replaceUID Tuple3l   9166499938994702097
replaceUID Tuple4d   -873205338876336747
#replaceUID Tuple4f   -289659481288029971
replaceUID Tuple4i   7216628186865091035
replaceUID Tuple4l   8255563940826933134
replaceUID TupleNd   6895562152531916143
#replaceUID TupleNf   -681761072062846262
replaceUID TupleNi   7619482928737424566
replaceUID TupleNl   5567729237636881756
replaceUID Color3d   -861801977306311811
#replaceUID Color3f   -4390780477392240248
replaceUID Color4d   -6362156338845482665
#replaceUID Color4f   6294025356510313475
replaceUID Vector2d  4316416129453850294
#replaceUID Vector2f  6377427288913163382
replaceUID Vector2i  5560542871610101750
replaceUID Vector2l  4680854728302037518
replaceUID Vector3d  8807802067885508329
#replaceUID Vector3f  -4074371289412725210
replaceUID Vector3i  -3646303620663151552
replaceUID Vector3l  -3580766957858993018
replaceUID Vector4d  5158241118831329402
#replaceUID Vector4f  6905631586098765133
replaceUID Vector4i  -3938063223525408318
replaceUID Vector4l  -5240383941138044679
replaceUID Point2d   -806363389015108966
#replaceUID Point2f   -2418939175965276598
replaceUID Point2i   1737829702409090627
replaceUID Point2l   -1420525176872561294
replaceUID Point3d   -8165016325308320148
#replaceUID Point3f   6002120110155982131
replaceUID Point3i   6997682793454754689
replaceUID Point3l   5342940559792883146
replaceUID Point4d   3807928269275895806
#replaceUID Point4f   -2040548296340441756
replaceUID Point4i   7172362841064866784
replaceUID Point4l   -4359804890711701264
replaceUID BBox2d    -1429174433364053287
#replaceUID BBox2f    4684427252547347
replaceUID BBox3d    -2626818321975206766
replaceUID BBox3f    3684913278002275578
replaceUID BBox4d    -4550250439234661489
replaceUID BBox4f    -4768699580945478563
#replaceUID CoordSysNf -2044361182791073559
replaceUID CoordSysNd -4120832248146896026
#replaceUID CoordSys2f -7110489118183818121
replaceUID CoordSys2d -1719826926386389147
#replaceUID MatrixMNf 3794263972349534350
replaceUID MatrixMNd 6999223760955191120
#replaceUID Matrix33f -6713766254194327700
replaceUID Matrix33d 6205146094960282336
