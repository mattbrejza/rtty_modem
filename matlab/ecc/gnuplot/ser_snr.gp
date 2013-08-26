set xlabel 'SNR (dB)'
set ylabel 'BER'
set logscale y
set format y '10^{%L}'
set style line 1 lc 1
#set xrange[1:2]
#set yrange[0.0001:1]
plot 'ber_6144.dat' using ($1-10*log10(0.333)):($2==0 ? NaN : $2) with lines ls 1 title 'R=1/3' ,\
'ber_6144.dat' using ($1-10*log10(0.5)):($3==0 ? NaN : $3) with lines ls 1 title 'R=0.5' ,\
'ber_6144.dat' using ($1-10*log10(0.6)):($4==0 ? NaN : $4) with lines ls 1 title 'R=0.6' ,\
'ber_6144.dat' using ($1-10*log10(0.7)):($5==0 ? NaN : $5) with lines ls 1 title 'R=0.7' ,\
'ber_6144.dat' using ($1-10*log10(0.8)):($6==0 ? NaN : $6) with lines ls 1 title 'R=0.8' ,\
'ber_6144.dat' using ($1-10*log10(0.9)):($7==0 ? NaN : $7) with lines ls 1 title 'R=0.9' 

	 
	 
#set terminal png
#set termoptions enhanced
#set output 'out.png'