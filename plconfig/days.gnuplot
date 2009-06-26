set terminal jpeg size 900,500
set output "days.jpg"
set bmargin 7
set tmargin 2
set key left top
set xtics rotate by 90 scale 0
set boxwidth 0.65 relative 
set style fill solid 0.4 border
plot 'user-days.raw' using 1:2:xtic(3) with boxes t "Total Days Used"
