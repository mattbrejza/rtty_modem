load('results_rs___-7_24746000.mat')
rs=results;
fileID = fopen('ber_rs.dat','w');
fprintf(fileID,'#ber rs awgn\n');
fprintf(fileID,'#SNR BER\n');
fprintf(fileID,'%6.2f\t%12.8f\n',transpose([rs(:,1),rs(:,3)./rs(:,2)]));
fclose(fileID);
fileID = fopen('ser_rs.dat','w');
fprintf(fileID,'#ser rs awgn\n');
fprintf(fileID,'#SNR SER\n');
fprintf(fileID,'%6.2f\t%12.8f\n',transpose([rs(:,1),rs(:,5)./rs(:,4)]));
fclose(fileID);


load('results_rs_fade_full_-7_24906000.mat')
rs=results;
fileID = fopen('ber_rs_ff.dat','w');
fprintf(fileID,'#ber rs fade interleaved\n');
fprintf(fileID,'#SNR BER\n');
fprintf(fileID,'%6.2f\t%12.8f\n',transpose([rs(:,1),rs(:,3)./rs(:,2)]));
fclose(fileID);
fileID = fopen('ser_rs_ff.dat','w');
fprintf(fileID,'#ser rs fade interleaved\n');
fprintf(fileID,'#SNR SER\n');
fprintf(fileID,'%6.2f\t%12.8f\n',transpose([rs(:,1),rs(:,5)./rs(:,4)]));
fclose(fileID);

load('results_rs_fade_none_-7_24903000.mat')
rs=results;
fileID = fopen('ber_rs_fn.dat','w');
fprintf(fileID,'#ber rs fade\n');
fprintf(fileID,'#SNR BER\n');
fprintf(fileID,'%6.2f\t%12.8f\n',transpose([rs(:,1),rs(:,3)./rs(:,2)]));
fclose(fileID);
fileID = fopen('ser_rs_fn.dat','w');
fprintf(fileID,'#ser rs fade\n');
fprintf(fileID,'#SNR SER\n');
fprintf(fileID,'%6.2f\t%12.8f\n',transpose([rs(:,1),rs(:,5)./rs(:,4)]));
fclose(fileID);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%5

load('results_rep3_hard_-7_34626000.mat')
rep=results;
txt = 'rep3_hard';
fileID = fopen(['ber_',txt,'.dat'],'w');
fprintf(fileID,['#ber ',txt,'\n']);
fprintf(fileID,'#SNR BER\n');
fprintf(fileID,'%6.2f\t%12.8f\n',transpose([rep(:,1),rep(:,3)./rep(:,2)]));
fclose(fileID);
fileID = fopen(['ser_',txt,'.dat'],'w');
fprintf(fileID,['#ser ',txt,'\n']);
fprintf(fileID,'#SNR SER\n');
fprintf(fileID,'%6.2f\t%12.8f\n',transpose([rep(:,1),rep(:,5)./rep(:,4)]));
fclose(fileID);



load('results_rep3_hard_fading_-7_45664000.mat')
rep=results;
txt = 'rep3_hard_fading';
fileID = fopen(['ber_',txt,'.dat'],'w');
fprintf(fileID,['#ber ',txt,'\n']);
fprintf(fileID,'#SNR BER\n');
fprintf(fileID,'%6.2f\t%12.8f\n',transpose([rep(:,1),rep(:,3)./rep(:,2)]));
fclose(fileID);
fileID = fopen(['ser_',txt,'.dat'],'w');
fprintf(fileID,['#ser ',txt,'\n']);
fprintf(fileID,'#SNR SER\n');
fprintf(fileID,'%6.2f\t%12.8f\n',transpose([rep(:,1),rep(:,5)./rep(:,4)]));
fclose(fileID);


load('results_rep3_soft_-7_34478000.mat')
rep=results;
txt = 'rep3_soft';
fileID = fopen(['ber_',txt,'.dat'],'w');
fprintf(fileID,['#ber ',txt,'\n']);
fprintf(fileID,'#SNR BER\n');
fprintf(fileID,'%6.2f\t%12.8f\n',transpose([rep(:,1),rep(:,3)./rep(:,2)]));
fclose(fileID);
fileID = fopen(['ser_',txt,'.dat'],'w');
fprintf(fileID,['#ser ',txt,'\n']);
fprintf(fileID,'#SNR SER\n');
fprintf(fileID,'%6.2f\t%12.8f\n',transpose([rep(:,1),rep(:,5)./rep(:,4)]));
fclose(fileID);


load('results_rep3_soft_fading_-7_33907000.mat')
rep=results;
txt = 'rep3_soft_fading';
fileID = fopen(['ber_',txt,'.dat'],'w');
fprintf(fileID,['#ber ',txt,'\n']);
fprintf(fileID,'#SNR BER\n');
fprintf(fileID,'%6.2f\t%12.8f\n',transpose([rep(:,1),rep(:,3)./rep(:,2)]));
fclose(fileID);
fileID = fopen(['ser_',txt,'.dat'],'w');
fprintf(fileID,['#ser ',txt,'\n']);
fprintf(fileID,'#SNR SER\n');
fprintf(fileID,'%6.2f\t%12.8f\n',transpose([rep(:,1),rep(:,5)./rep(:,4)]));
fclose(fileID);

%%%%%%%%%%%%%%%%%%%%%%%%5555
load('compiled_all_ber.mat');
load('compiled_all_ser.mat');
load('results_fade_full_992_0_0.7_145671000.mat');

pos=4;
start_p = find(ber_ff(:,1)>=results(1,1));
end_p = start_p + size(results,1)-1;
ber_ff(start_p:end_p,pos+1) = results(:,end-2)./ results(:,2);
ser_ff(start_p:end_p,pos+1) = results(:,end)./ results(:,end-1);

load('results_fade_full_992_0_0.8_47180000.mat');

pos=5;
start_p = find(ber_ff(:,1)>=results(1,1));
end_p = start_p + size(results,1)-1;
ber_ff(start_p:end_p,pos+1) = results(:,end-2)./ results(:,2);
ser_ff(start_p:end_p,pos+1) = results(:,end)./ results(:,end-1);

load('results_fade_full_992_0_0.9_144212000.mat');

pos=6;
start_p = find(ber_ff(:,1)>=results(1,1));
end_p = start_p + size(results,1)-1;
ber_ff(start_p:end_p,pos+1) = results(:,end-2)./ results(:,2);
ser_ff(start_p:end_p,pos+1) = results(:,end)./ results(:,end-1);

ber_ff(:,1)=-8:.1:14;
ser_ff(:,1)=-8:.1:14;

txt = '992_ff';
fileID = fopen(['ber_',txt,'.dat'],'w');
fprintf(fileID,['#ber ',txt,'\n']);
fprintf(fileID,'#SNR 0.33 .5 .6 .7 .8 .9\n');
fprintf(fileID,'%6.2f\t%12.8f\t%12.8f\t%12.8f\t%12.8f\t%12.8f\t%12.8f\n',transpose(ber_ff));
fclose(fileID);
fileID = fopen(['ser_',txt,'.dat'],'w');
fprintf(fileID,['#ser ',txt,'\n']);
fprintf(fileID,'#SNR 0.33 .5 .6 .7 .8 .9\n');
fprintf(fileID,'%6.2f\t%12.8f\t%12.8f\t%12.8f\t%12.8f\t%12.8f\t%12.8f\n',transpose(ser_ff));
fclose(fileID);
