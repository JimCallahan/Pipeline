#!/bin/bash

for f in Tuple*f.java Vector*f.java Point*f.java
do 
  d=`echo $f | sed -e 's/f.java/d.java/g'`

  cat $f | sed -e 's/float/double/g' | sed -e 's/0.0f/0.0/g' \
         | sed -e 's/Nf/Nd/g' | sed -e 's/N F/N D/g' \
         | sed -e 's/2f/2d/g' | sed -e 's/2 F/2 D/g' \
         | sed -e 's/3f/3d/g' | sed -e 's/3 F/3 D/g' \
         | sed -e 's/4f/4d/g' | sed -e 's/4 F/4 D/g' > $d

  i=`echo $f | sed -e 's/f.java/i.java/g'`

  cat $f | sed -e 's/float/int/g' | sed -e 's/0.0f/0/g' \
         | sed -e 's/Nf/Ni/g' | sed -e 's/N F/N I/g' \
         | sed -e 's/2f/2i/g' | sed -e 's/2 F/2 I/g' \
         | sed -e 's/3f/3i/g' | sed -e 's/3 F/3 I/g' \
         | sed -e 's/4f/4i/g' | sed -e 's/4 F/4 I/g' > $i

  l=`echo $f | sed -e 's/f.java/l.java/g'`

  cat $f | sed -e 's/float/long/g' | sed -e 's/0.0f/0L/g' \
         | sed -e 's/Nf/Nl/g' | sed -e 's/N F/N L/g' \
         | sed -e 's/2f/2l/g' | sed -e 's/2 F/2 L/g' \
         | sed -e 's/3f/3l/g' | sed -e 's/3 F/3 L/g' \
         | sed -e 's/4f/4l/g' | sed -e 's/4 F/4 L/g' > $l
done

for f in Color*f.java
do 
  d=`echo $f | sed -e 's/f.java/d.java/g'`

  cat $f | sed -e 's/float/double/g' | sed -e 's/0.0f/0.0/g' \
         | sed -e 's/Nf/Nd/g' | sed -e 's/N F/N D/g' \
         | sed -e 's/2f/2d/g' | sed -e 's/2 F/2 D/g' \
         | sed -e 's/3f/3d/g' | sed -e 's/3 F/3 D/g' \
         | sed -e 's/4f/4d/g' | sed -e 's/4 F/4 D/g' > $d
done
