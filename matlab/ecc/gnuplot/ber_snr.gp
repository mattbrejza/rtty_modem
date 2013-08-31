set xlabel 'SNR (dB)'
set ylabel 'BER'
set logscale y
set format y '10^{%L}'




#show label 1
#show label 2
#show label 3
#show label 4
#show label 5
#show label 6


load 'setup_labels.gp'



#set xrange[1:2]
#set yrange[0.000001:1]

#grph 1
set terminal png
set termoptions enhanced
set output 'out1.png'

unset arrow
unset label

set xlabel 'SNR (dB)'
set ylabel 'BER'
set xrange[-7:10]
set yrange[0.00001:1]
plot 'ber_rs_255_223.dat' using ($1):($2==0 ? NaN : $2) with linespoints ls 3 title 'RS(255,223) R=0.87' ,\
'ber_rs_7_3.dat' using ($1):($2==0 ? NaN : $2) with linespoints ls 2 title 'RS(7,3) R=0.43' ,\
'ber_rep3_hard.dat' using ($1):($2==0 ? NaN : $2) with linespoints ls 5 title 'Rep3 (hard) R=0.33' ,\
'ber_rep3_soft.dat' using ($1):($2==0 ? NaN : $2) with linespoints ls 4 title 'Rep3 (soft) R=0.33' ,\
'ber_uncoded.dat' using ($1):($2==0 ? NaN : $2) with linespoints ls 1 title 'Uncoded R=1'
replot


#grph 2
set terminal png
set termoptions enhanced
set output 'out2.png'

unset label
unset xlabel
unset ylabel
unset arrow
set xlabel 'E_b/N_0 (dB)'
set ylabel 'BER
set xrange[0:10]
set yrange[0.00001:1]
plot 'ber_rs_255_223.dat' using ($1-10*log10(0.87)):($2==0 ? NaN : $2) with linespoints ls 3 title 'RS(255,223) R=0.87' ,\
'ber_rs_7_3.dat' using ($1-10*log10(0.43)):($2==0 ? NaN : $2) with linespoints ls 2 title 'RS(7,3) R=0.43' ,\
'ber_rep3_hard.dat' using ($1-10*log10(0.33)):($2==0 ? NaN : $2) with linespoints ls 5 title 'Rep3 (hard) R=0.33' ,\
'ber_rep3_soft.dat' using ($1-10*log10(0.33)):($2==0 ? NaN : $2) with linespoints ls 4 title 'Rep3 (soft) R=0.33' ,\
'ber_uncoded.dat' using ($1-10*log10(1)):($2==0 ? NaN : $2) with linespoints ls 1 title 'Uncoded R=1'
replot


#grph 3
unset label
unset xlabel
unset ylabel
unset arrow

set terminal png
set termoptions enhanced
set output 'out3.png'


set xlabel 'SNR (dB)'
set ylabel 'BER
set xrange[-7:0]
set yrange[0.000001:1]
set arrow from -1.6+10*log10(.3333),1e-6 to -1.6+10*log10(.3333),1 nohead
set label "Shannon Limit" at -1.5+10*log10(.333)   , 8.2e-5 center rotate by 270
set label "1 Iteration" at -1.5, 0.008
set arrow from -1.19,0.0048 to -1.777,0.00088
set label "10 Iterations" at -5.6,0.000248
set arrow from -4.89,0.0003 to -4.34,0.002

plot 'ber_6144_set.dat' using ($1):(($3/$2)==0 ? NaN : ($3/$2)) with line ls 6 title 'Turbo' ,\
'ber_6144_set.dat' using ($1):(($4/$2)==0 ? NaN : ($4/$2)) with line ls 6 notitle ,\
'ber_6144_set.dat' using ($1):(($5/$2)==0 ? NaN : ($5/$2)) with line ls 6 notitle ,\
'ber_6144_set.dat' using ($1):(($6/$2)==0 ? NaN : ($6/$2)) with line ls 6 notitle ,\
'ber_6144_set.dat' using ($1):(($7/$2)==0 ? NaN : ($7/$2)) with line ls 6 notitle ,\
'ber_6144_set.dat' using ($1):(($8/$2)==0 ? NaN : ($8/$2)) with line ls 6 notitle ,\
'ber_6144_set.dat' using ($1):(($9/$2)==0 ? NaN : ($9/$2)) with line ls 6 notitle ,\
'ber_6144_set.dat' using ($1):(($10/$2)==0 ? NaN : ($10/$2)) with line ls 6 notitle ,\
'ber_6144_set.dat' using ($1):(($11/$2)==0 ? NaN : ($11/$2)) with line ls 6 notitle ,\
'ber_6144_set.dat' using ($1):(($12/$2)==0 ? NaN : ($12/$2)) with line ls 6 notitle ,\
'ber_6144_set.dat' using ($1):(($13/$2)==0 ? NaN : ($13/$2)) with line ls 6 notitle ,\
'ber_uncoded.dat' using ($1):($2==0 ? NaN : $2) with line ls 1 title 'Uncoded'

replot


#grph 4
unset label
unset xlabel
unset ylabel
unset arrow

set terminal png
set termoptions enhanced
set output 'out4.png'


set label 60 "Shannon Limit" at -1.4   , 1.2e-5 right rotate by 270
#set label 11 "1/3" at -4.5-10*log10(0.33)  , 0.00015 center
#set label 12 "0.5" at -2-10*log10(0.5)    , 0.00015 center
#set label 13 "0.6" at -.5-10*log10(0.6)   , 2.5e-5 center
#set label 14 "0.7" at 1-10*log10(0.7)     , 1e-5 center
#set label 15 "0.8" at 2.5-10*log10(0.8)   , 1e-5 center
#set label 16 "0.9" at 4.4-10*log10(0.9)   , 1e-5 center
set label "R=1/3" at -0.7,1e-3 center
set label "R=0.9" at 4.5,6e-4 left
set arrow from -0.7,1.3e-3 to 0.1,3e-3
set arrow from 5,4e-4 to 4.6,1.2e-4
set arrow from -1.6,1e-5 to -1.6,1 nohead
set xlabel 'E_b/N_0 (dB)'
set ylabel 'BER
set xrange[-2:10]
set yrange[0.00001:1]
plot 'ber_6144.dat' using ($1-10*log10(.333)):($2==0 ? NaN : $2) with linespoints ls 6 title 'Turbo' ,\
'ber_6144.dat' using ($1-10*log10(.5)):($3==0 ? NaN : $3) with linespoints ls 6 notitle ,\
'ber_6144.dat' using ($1-10*log10(.6)):($4==0 ? NaN : $4) with linespoints ls 6 notitle ,\
'ber_6144.dat' using ($1-10*log10(.7)):($5==0 ? NaN : $5) with linespoints ls 6 notitle ,\
'ber_6144.dat' using ($1-10*log10(.8)):($6==0 ? NaN : $6) with linespoints ls 6 notitle ,\
'ber_6144.dat' using ($1-10*log10(.9)):($7==0 ? NaN : $7) with linespoints ls 6 notitle ,\
'ber_rs_255_223.dat' using ($1-10*log10(0.87)):($2==0 ? NaN : $2) with linespoints ls 3 title 'RS(255,223) R=0.87' ,\
'ber_uncoded.dat' using ($1-10*log10(1)):($2==0 ? NaN : $2) with linespoints ls 1 title 'Uncoded R=1'


replot


#grph 5
unset label
unset xlabel
unset ylabel
unset arrow

set terminal png
set termoptions enhanced
set output 'out5.png'

#set label 60 "Shannon Limit" at -1.4   , 1.2e-5 center rotate by 270
#set label 11 "1/3" at -4.5-10*log10(0.33)  , 0.00015 center
#set label 12 "0.5" at -2-10*log10(0.5)    , 0.00015 center
#set label 13 "0.6" at -.5-10*log10(0.6)   , 2.5e-5 center
#set label 14 "0.7" at 1-10*log10(0.7)     , 1e-5 center
#set label 15 "0.8" at 2.5-10*log10(0.8)   , 1e-5 center
#set label 16 "0.9" at 4.4-10*log10(0.9)   , 1e-5 center
#
set xlabel 'E_b/N_0 (dB)'
set ylabel 'FER
set xrange[-1:10]
set yrange[0.001:1]
set label "R=1/3" at 0,1e-2 center
set label "R=0.9" at 7,1e-2 centre
set arrow from 0,9e-3 to .7,3e-3
set arrow from 7,1.3e-2 to 4.8,5e-2

plot 'ser_992.dat' using ($1-10*log10(.333)):($2==0 ? NaN : $2) with linespoints ls 6 title 'Turbo' ,\
'ser_992.dat' using ($1-10*log10(.5)):($3==0 ? NaN : $3) with linespoints ls 6 notitle ,\
'ser_992.dat' using ($1-10*log10(.6)):($4==0 ? NaN : $4) with linespoints ls 6 notitle ,\
'ser_992.dat' using ($1-10*log10(.7)):($5==0 ? NaN : $5) with linespoints ls 6 notitle ,\
'ser_992.dat' using ($1-10*log10(.8)):($6==0 ? NaN : $6) with linespoints ls 6 notitle ,\
'ser_992.dat' using ($1-10*log10(.9)):($7==0 ? NaN : $7) with linespoints ls 6 notitle ,\
'ser_rs_255_223.dat' using ($1-10*log10(0.87)):($2==0 ? NaN : $2) with linespoints ls 3 title 'RS(255,223) R=0.87' ,\
'ser_uncoded.dat' using ($1-10*log10(1)):($2==0 ? NaN : $2) with linespoints ls 1 title 'Uncoded R=1'


replot


#grph 6
set terminal png
set termoptions enhanced
set output 'out6.png'

unset label
unset xlabel
unset ylabel
unset arrow
set xlabel 'E_b/N_0 (dB)'
set ylabel 'FER
set xrange[0:30]
set yrange[0.001:1]

set label "R=1/3" at 5,1e-2 center
set label "R=0.9" at 24,1e-2 centre
set arrow from 5,9e-3 to 7,3e-3
set arrow from 24,1.3e-2 to 15.5,5e-2

plot 'ser_992_ff.dat' using ($1-10*log10(.333)):($2==0 ? NaN : $2) with linespoints ls 6 title 'Turbo' ,\
'ser_992_ff.dat' using ($1-10*log10(.5)):($3==0 ? NaN : $3) with linespoints ls 6 notitle ,\
'ser_992_ff.dat' using ($1-10*log10(.6)):($4==0 ? NaN : $4) with linespoints ls 6 notitle ,\
'ser_992_ff.dat' using ($1-10*log10(.7)):($5==0 ? NaN : $5) with linespoints ls 6 notitle ,\
'ser_992_ff.dat' using ($1-10*log10(.8)):($6==0 ? NaN : $6) with linespoints ls 6 notitle ,\
'ser_992_ff.dat' using ($1-10*log10(.9)):($7==0 ? NaN : $7) with linespoints ls 6 notitle ,\
'ser_rs_255_223_ff.dat' using ($1-10*log10(0.87)):($2==0 ? NaN : $2) with linespoints ls 3 title 'RS(255,223) R=0.87' ,\
'ser_uncoded_fading.dat' using ($1-10*log10(1)):($2==0 ? NaN : $2) with linespoints ls 1 title 'Uncoded R=1'


replot
unset terminal
replot



#plot 'ber_6144.dat' using ($1):($2==0 ? NaN : $2) with linespoints ls 6 title 'turbo' ,\
#'ber_6144.dat' using ($1):($3==0 ? NaN : $3) with linespoints ls 6 notitle ,\
#'ber_6144.dat' using ($1):($4==0 ? NaN : $4) with linespoints ls 6 notitle ,\
#'ber_6144.dat' using ($1):($5==0 ? NaN : $5) with linespoints ls 6 notitle ,\
#'ber_6144.dat' using ($1):($6==0 ? NaN : $6) with linespoints ls 6 notitle ,\
#'ber_6144.dat' using ($1):($7==0 ? NaN : $7) with linespoints ls 6 notitle ,\
#'ber_uncoded.dat' using ($1):($2==0 ? NaN : $2) with linespoints ls 1 title 'uncoded' ,\
#'ber_rep3_hard.dat' using ($1):($2==0 ? NaN : $2) with linespoints ls 5 title 'rep3h' ,\

#plot 'ber_uncoded_fading.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'uncoded' ,\
#'ber_rs_7_3_ff.dat' using ($1):($2==0 ? NaN : $2) with lines ls 2 title 'rs75' ,\
#'ber_rs_255_223_ff.dat' using ($1):($2==0 ? NaN : $2) with lines ls 2 title 'rs255223' 
#'ber_6144.dat' using ($1):($4==0 ? NaN : $4) with lines ls 1 title 'R=0.6' ,\
#
#'ber_rs_7_3_fn.dat' using ($1):($2==0 ? NaN : $2) with lines ls 2 title 'rs75' ,\
#
#plot 'ber_uncoded_fading.dat' using ($1):($2==0 ? NaN : $2) with linespoints ls 1 title 'uncoded' ,\
#'ber_rs_7_3_ff.dat' using ($1):($2==0 ? NaN : $2) with lines ls 2 title 'rs75' ,\
#'ber_rs_7_3_fn.dat' using ($1):($2==0 ? NaN : $2) with lines ls 2 title 'rs75' ,\
#'ber_rs_255_223_ff.dat' using ($1):($2==0 ? NaN : $2) with lines ls 2 title 'rs255223' ,\
#'ber_992_ff.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'R=0.6' ,\
#'ber_rep3_hard_fading.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'rep3h'
#
#
#plot 'ber_992_ff.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'R=1/3' ,\
#'ber_992_ff.dat' using ($1):($3==0 ? NaN : $3) with lines ls 1 title 'R=0.5' ,\
#'ber_992_ff.dat' using ($1):($4==0 ? NaN : $4) with lines ls 1 title 'R=0.6' ,\
#'ber_992_ff.dat' using ($1):($5==0 ? NaN : $5) with lines ls 1 title 'R=0.7' ,\
#'ber_992_ff.dat' using ($1):($6==0 ? NaN : $6) with lines ls 1 title 'R=0.8' ,\
#'ber_992_ff.dat' using ($1):($7==0 ? NaN : $7) with lines ls 1 title 'R=0.9' ,\
#'ber_uncoded_fading.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'uncoded' ,\
#'ber_rep3_soft_fading.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'rep3s' ,\
#'ber_rep3_hard_fading.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'rep3s'
#
#
#plot 'ser_992_ff.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'R=1/3' ,\
#'ser_992_ff.dat' using ($1):($3==0 ? NaN : $3) with lines ls 1 title 'R=0.5' ,\
#'ser_992_ff.dat' using ($1):($4==0 ? NaN : $4) with lines ls 1 title 'R=0.6' ,\
#'ser_992_ff.dat' using ($1):($5==0 ? NaN : $5) with lines ls 1 title 'R=0.7' ,\
#'ser_992_ff.dat' using ($1):($6==0 ? NaN : $6) with lines ls 1 title 'R=0.8' ,\
#'ser_992_ff.dat' using ($1):($7==0 ? NaN : $7) with lines ls 1 title 'R=0.9' ,\
#'ser_uncoded_fading.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'uncoded' ,\
#'ser_rep3_soft_fading.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'rep3s' ,\
#'ser_rep3_hard_fading.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'rep3s' ,\
#'ser_rs_ff.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'rs'
#
#
	 #
#	 
#set terminal png
#set termoptions enhanced
#set output 'out.png'