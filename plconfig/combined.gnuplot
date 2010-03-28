set terminal jpeg size 900,500
set output "combined.jpg"
set bmargin 7
set tmargin 2
set key left top
set style line 1 lw 2 lc rgb "green"
set style line 2 lw 2 lc rgb "blue"
set xtics rotate by 90 scale 0
plot 'combined-users.raw' using 1:2:xtic(4) t "Unique Daily Users", 'combined-users.raw' using 1:3 with lines t "Quarterly Average Users" ls 1, 'paid.raw' using 1:2 with lines t "Paid Licenses" ls 2
