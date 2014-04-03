function [ out,out2 ] = sync_detect( sync_sequence , SNR)
%SYNC_DETECT Summary of this function goes here
%   Detailed explanation goes here
N0 = 1/(10^(SNR/10));
sync_sequence = (sync_sequence>0.3);

frame_length = 3 * numel(sync_sequence);

a = [round(rand(1,frame_length)), sync_sequence  ,round(rand(1,frame_length))];

% BPSK modulate them
a_tx = -2*(a-0.5);



% Send the BPSK signal over an AWGN channel
a_rx = a_tx + sqrt(N0/2)*(randn(size(a_tx))+1i*randn(size(a_tx)));

% BPSK demodulator
% These labels match those used in Figure 2.11 of Liang Li's nine month report.
a_c = (abs(a_rx+1).^2-abs(a_rx-1).^2)/N0;
                
sync_sequence = -2*sync_sequence+1;

out=xcorr(a_c,sync_sequence);
out2=xcorr((a_c > 0)-.5  ,sync_sequence);

end

