set xlabel 'SNR (dB)'
set ylabel 'BER'
set logscale y
set format y '10^{%L}'
set style line 1 lc 1
#set xrange[1:2]
#set yrange[0.000001:1]


plot 'ber_6144.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'R=1/3' ,\
'ber_6144.dat' using ($1):($3==0 ? NaN : $3) with lines ls 1 title 'R=0.5' ,\
'ber_6144.dat' using ($1):($4==0 ? NaN : $4) with lines ls 1 title 'R=0.6' ,\
'ber_6144.dat' using ($1):($5==0 ? NaN : $5) with lines ls 1 title 'R=0.7' ,\
'ber_6144.dat' using ($1):($6==0 ? NaN : $6) with lines ls 1 title 'R=0.8' ,\
'ber_6144.dat' using ($1):($7==0 ? NaN : $7) with lines ls 1 title 'R=0.9' ,\
'ber_uncoded.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'uncoded' ,\
'ber_rep3_hard.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'rep3h' 


plot 'ber_922_ff.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'R=1/3' ,\
'ber_922_ff.dat' using ($1):($3==0 ? NaN : $3) with lines ls 1 title 'R=0.5' ,\
'ber_922_ff.dat' using ($1):($4==0 ? NaN : $4) with lines ls 1 title 'R=0.6' ,\
'ber_922_ff.dat' using ($1):($5==0 ? NaN : $5) with lines ls 1 title 'R=0.7' ,\
'ber_922_ff.dat' using ($1):($6==0 ? NaN : $6) with lines ls 1 title 'R=0.8' ,\
'ber_922_ff.dat' using ($1):($7==0 ? NaN : $7) with lines ls 1 title 'R=0.9' ,\
'ber_uncoded_fading.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'uncoded' 


plot 'ser_922_ff.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'R=1/3' ,\
'ser_922_ff.dat' using ($1):($3==0 ? NaN : $3) with lines ls 1 title 'R=0.5' ,\
'ser_922_ff.dat' using ($1):($4==0 ? NaN : $4) with lines ls 1 title 'R=0.6' ,\
'ser_922_ff.dat' using ($1):($5==0 ? NaN : $5) with lines ls 1 title 'R=0.7' ,\
'ser_922_ff.dat' using ($1):($6==0 ? NaN : $6) with lines ls 1 title 'R=0.8' ,\
'ser_922_ff.dat' using ($1):($7==0 ? NaN : $7) with lines ls 1 title 'R=0.9' ,\
'ser_uncoded_fading.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'uncoded' ,\
'ser_rep3_soft_fading.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'rep3s' ,\
'ser_rep3_hard_fading.dat' using ($1):($2==0 ? NaN : $2) with lines ls 1 title 'rep3s'


	 
	 
#set terminal png
#set termoptions enhanced
#set output 'out.png'