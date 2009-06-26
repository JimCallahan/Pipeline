set terminal jpeg size 900,500
set output "usage.jpg"
set bmargin 7
set tmargin 2
set key left top
set style line 1 lw 2 lc rgb "green"
set xtics rotate by 90 scale 0
plot 'daily-users.raw' using 1:2:xtic(6) t "Unique Daily Users", 'daily-users.raw' using 1:5 with lines t "Quarterly Average Users" ls 1
