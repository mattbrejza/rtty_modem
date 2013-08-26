% Script for drawing the EXIT chart of the UMTS turbo code, as specified 
% in ETSI TS 125 212 (search for it on Google if you like)
% BPSK modulation over an AWGN channel is assumed.
% Copyright (C) 2010  Robert G. Maunder

% This program is free software: you can redistribute it and/or modify it 
% under the terms of the GNU General Public License as published by the
% Free Software Foundation, either version 3 of the License, or (at your 
% option) any later version.

% This program is distributed in the hope that it will be useful, but 
% WITHOUT ANY WARRANTY; without even the implied warranty of 
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General 
% Public License for more details.

% The GNU General Public License can be seen at http://www.gnu.org/licenses/.

clear all
frame_length = 40; % Choose your frame length K
SNR = -4; % Choose the SNR
frame_count = 1000; % Choose how many frames to simulate
IA_count = 11; % Choose how many points to plot in the EXIT functions
histogram_method = 0; % Choose whether to use the histogram or the averaging method of measuring mutual information

% Convert from SNR (in dB) to noise power spectral density.
N0 = 1/(10^(SNR/10));

% Calculate the MIs to use for the a priori LLRs
IAs = (0:(IA_count-1))/(IA_count-1);
IE_means = zeros(1,IA_count);
IE_stds = zeros(1,IA_count);

% Determine each point in the EXIT functions
for IA_index = 1:IA_count
    
    IEs = zeros(1,frame_count);
    
    % This runs the simulation long enough to produce smooth EXIT functions.
    for frame_index = 1:frame_count

        % Generate some random bits.
        % This label matches that used in Figure 2.13 of Liang Li's nine month report.
        a = round(rand(1,frame_length));
        
        % Encode them
        % These labels match those used in Figure 2.13 of Liang Li's nine month report.
        [c,e] = component_encoder(a);

        % BPSK modulate them
        a_tx = -2*(a-0.5);
        c_tx = -2*(c-0.5);
        e_tx = -2*(e-0.5);

        % Send the BPSK signal over an AWGN channel
        a_rx = a_tx + sqrt(N0/2)*(randn(size(a_tx))+i*randn(size(a_tx)));
        c_rx = c_tx + sqrt(N0/2)*(randn(size(c_tx))+i*randn(size(c_tx)));
        e_rx = e_tx + sqrt(N0/2)*(randn(size(e_tx))+i*randn(size(e_tx)));
 
        % BPSK demodulator
        % These labels match those used in Figure 2.13 of Liang Li's nine month report.
        a_c = (abs(a_rx+1).^2-abs(a_rx-1).^2)/N0;
        c_c = (abs(c_rx+1).^2-abs(c_rx-1).^2)/N0;
        e_c = (abs(e_rx+1).^2-abs(e_rx-1).^2)/N0;
 
        % Generate some random a priori LLRs.
        % These labels match those used in Figure 2.13 of Liang Li's nine month report.
        a_a = generate_llrs(a, IAs(IA_index));
        
        % Obtain the uncoded a priori input for component decoder 1.
        % These labels match those used in Figure 2.13 of Liang Li's nine month report.
        y_a = [a_a+a_c,e_c];
        
        % Perform decoding.
        % These labels match those used in Figure 2.13 of Liang Li's nine month report.
        y_e = component_decoder(y_a,c_c);
        
        % Remove the LLRs corresponding to the termination bits.
        % These labels match those used in Figure 2.13 of Liang Li's nine month report.
        a_e = y_e(1:length(a));

        % Measure the mutual information
        if histogram_method
            IEs(frame_index) = measure_mutual_information_histogram(a_e,a);
        else
            IEs(frame_index) = measure_mutual_information_averaging(a_e);
        end
        
    end
    
    % Store the mean and standard deviation of the results
    IE_means(IA_index) = mean(IEs);
    IE_stds(IA_index) = std(IEs);
end

% Create a figure to plot the results.
figure;
axis square;
title('BPSK modulation in an AWGN channel');
ylabel('I_E');
xlabel('I_A');
xlim([0,1]);
ylim([0,1]);

hold on;

% Plot the EXIT function for component decoder 1
plot(IAs,IE_means,'-');
plot(IAs,IE_means+IE_stds,'--');
plot(IAs,IE_means-IE_stds,'--');

% Plot the inverted EXIT function for component decoder 2
plot(IE_means,IAs,'-');
plot(IE_means+IE_stds,IAs,'--');
plot(IE_means-IE_stds,IAs,'--');

