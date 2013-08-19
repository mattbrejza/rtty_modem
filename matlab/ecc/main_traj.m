% Script for drawing the iterative decoding trajectories of the UMTS turbo code, as specified 
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
frame_count = 10; % Choose how many trajectories to plot
iteration_count = 8; % Choose the maximum number of decoding iterations to perform
chances = 3; % Choose how many iterations should fail to improve the decoding before the iterations are stopped early
random_interleaver = 0; % Choose whether to use a random interleaver or the UMTS interleaver
histogram_method = 0; % Choose whether to use the histogram or the averaging method of measuring mutual information

if ~random_interleaver
    % Use the UMTS interleaver
    interleaver = get_UMTS_interleaver(frame_length);
end


% Convert from SNR (in dB) to noise power spectral density.
N0 = 1/(10^(SNR/10));

% Create a figure to plot the results.
figure;
axis square;
title('BPSK modulation in an AWGN channel');
ylabel('I_E');
xlabel('I_A');
xlim([0,1]);
ylim([0,1]);
hold on;

% This runs the simulation long enough to produce smooth EXIT functions.
for frame_index = 1:frame_count

    if random_interleaver
        % Use a different random interleaver for every frame so that we get the average results
        interleaver = randperm(frame_length);
    end
    
    % Generate some random bits.
    % This label matches that used in Figure 2.7 of Liang Li's nine month report.
    a = round(rand(1,frame_length));
            
    % Interleave them
    % This label matches that used in Figure 2.7 of Liang Li's nine month report.
    b = a(interleaver);
            
    % Encode them
    % These labels match those used in Figure 2.7 of Liang Li's nine month report.
    [c,e] = component_encoder(a);            
    [d,f] = component_encoder(b);


    % BPSK modulate them
    a_tx = -2*(a-0.5);
    c_tx = -2*(c-0.5);
    d_tx = -2*(d-0.5);
    e_tx = -2*(e-0.5);
    f_tx = -2*(f-0.5);

    % Send the BPSK signal over an AWGN channel
    a_rx = a_tx + sqrt(N0/2)*(randn(size(a_tx))+i*randn(size(a_tx)));
    c_rx = c_tx + sqrt(N0/2)*(randn(size(c_tx))+i*randn(size(c_tx)));
    d_rx = d_tx + sqrt(N0/2)*(randn(size(d_tx))+i*randn(size(d_tx)));
    e_rx = e_tx + sqrt(N0/2)*(randn(size(e_tx))+i*randn(size(e_tx)));
    f_rx = f_tx + sqrt(N0/2)*(randn(size(f_tx))+i*randn(size(f_tx)));

    % BPSK demodulator
    % These labels match those used in Figure 2.11 of Liang Li's nine month report.
    a_c = (abs(a_rx+1).^2-abs(a_rx-1).^2)/N0;
    c_c = (abs(c_rx+1).^2-abs(c_rx-1).^2)/N0;
    d_c = (abs(d_rx+1).^2-abs(d_rx-1).^2)/N0;
    e_c = (abs(e_rx+1).^2-abs(e_rx-1).^2)/N0;
    f_c = (abs(f_rx+1).^2-abs(f_rx-1).^2)/N0;

    % Interleave the systematic LLRs
    % This label matches that used in Figure 2.11 of Liang Li's nine month report.
    b_c = a_c(interleaver);

    % We have no a priori information for the uncoded bits in the first decoding iteration.
    % This label matches that used in Figure 2.11 of Liang Li's nine month report.
    a_a = zeros(size(a));

    % Get ready to start iterating.
    best_IA = 0;
    iteration_index = 1;
    IA = 0;
    IE = 0;
    chance = 0;

    results = zeros(1,2);

    % Iterate until the iterations stop improving the decoding or until the iteration limit is reached.
    while chance < chances && iteration_index <= iteration_count
        
        % Obtain the uncoded a priori input for component decoder 1.
        % These labels match those used in Figure 2.11 of Liang Li's nine month report.
        y_a = [a_a+a_c,e_c];

        % Perform decoding.
        % These labels match those used in Figure 2.11 of Liang Li's nine month report.
        y_e = component_decoder(y_a,c_c);

        % Remove the LLRs corresponding to the termination bits.
        % These labels match those used in Figure 2.11 of Liang Li's nine month report.
        a_e = y_e(1:length(a));
  
        % Measure the mutual information
        if histogram_method
            IE = measure_mutual_information_histogram(a_e,a);
        else
            IE = measure_mutual_information_averaging(a_e);
        end

        % Store the co-ordinate of a trajectory corner point
        results(2*iteration_index,:) = [IA,IE];                
                
        % Interleave.
        % These labels match those used in Figure 2.11 of Liang Li's nine month report.
        b_a = a_e(interleaver);

        % Obtain the uncoded a priori input for component decoder 2.
        % These labels match those used in Figure 2.11 of Liang Li's nine month report.
        z_a = [b_a+b_c,f_c];

        % Perform decoding.
        % These labels match those used in Figure 2.11 of Liang Li's nine month report.
        z_e = component_decoder(z_a,d_c);

        % Remove the LLRs corresponding to the termination bits.
        % These labels match those used in Figure 2.11 of Liang Li's nine month report.
        b_e = z_e(1:length(b));
                
        % Deinterleave.
        % These labels match those used in Figure 2.11 of Liang Li's nine month report.
        a_a(interleaver) = b_e;

        % Measure the mutual information
        if histogram_method
            IA = measure_mutual_information_histogram(a_a,a);
        else
            IA = measure_mutual_information_averaging(a_a);
        end
        
        % Store the co-ordinate of a trajectory corner point
        results(2*iteration_index+1,:) = [IA,IE];
                    
        % Obtain the a posteriori LLRs.
        % These labels match those used in Figure 2.11 of Liang Li's nine month report.
        a_p = a_a + a_c + a_e;

        % Make a hard decision and see how many bit errors we have.
        errors = sum((a_p < 0) ~= a);

        if errors == 0
            % No need to carry on if all the errors have been removed.
            % It is assumed that a CRC is used to detect that this has happened.
            chance = chances; 
        else
            % Have we seen an improvement in this iteration?
            if IA > best_IA
                best_IA = IA;
            else
                chance = chance + 1;
            end
        end

        % Get ready for the next iteration.
        iteration_index = iteration_index + 1;
    end

    % Plot the results.
    plot(results(:,1),results(:,2));

end