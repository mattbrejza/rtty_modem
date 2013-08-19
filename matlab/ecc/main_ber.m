% Script for drawing the BER plot of the UMTS turbo code, as specified 
% in ETSI TS 125 212 (search for it on Google if you like). 
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

% This .m file can be easily converted into a Lyceum-friendly function.
% You can do this by removing the commenting from Section 1 and instead commenting out Section 2.

% Section 1
%===============================================================
function main_ber(frame_length, SNR_start, SNR_delta, SNR_stop, rate, chan, final_interleave)
%===============================================================

% Section 2
%===============================================================
%    clear all
%    frame_length = 40; % Choose your frame length K
%    SNR_start = -7; % Choose the starting SNR
%    SNR_delta = 1; % Choose the SNR hop
%    SNR_stop = inf; % Choose the stopping SNR
%===============================================================
tic;
if ischar(SNR_start)
    SNR_start = str2num(SNR_start);
end
if ischar(SNR_stop)
    SNR_stop = str2num(SNR_stop);
end
if ischar(SNR_delta)
    SNR_delta = str2num(SNR_delta);
end
if ischar(frame_length)
    frame_length = str2num(frame_length);
end
if ischar(rate)
    rate = str2num(rate);
end

if ~(nargin > 5)
    chan = '';
end
if ~(nargin > 6)
    final_interleave = '';
end
    
    iteration_count = 14; % Choose the maximum number of decoding iterations to perform
    chances = 5; % Choose how many iterations should fail to improve the decoding before the iterations are stopped early
    random_interleaver = 0; % Choose whether to use a random interleaver or the UMTS interleaver. Longer simulations will be needed for the random interleaver.

    
    
    if ~random_interleaver
        % Use the UMTS interleaver
        interleaver = get_LTE_interleaver(frame_length);
    end
    
    % Setup the SNR for the first iteration of the loop.
    SNR_count = 1;
    SNR = SNR_start;
    
    % Store the BER achieved after every iteration
    BERs = ones(1,iteration_count);
    results = zeros(1,iteration_count+4);
    
    getenv('OS');
    seed = int32(toc*1e9);
    seed = int32(seed);
    rng(seed);

    % Choose a file to save the results into.
    filename = ['results_',chan,'_',final_interleave,'_',num2str(frame_length),'_',num2str(SNR_start),'_',num2str(rate),'_',num2str(seed),'.mat'];

    stopping_crit = 50000;
    
    while(1)
    
        % Loop until the job is killed or until the SNR target is reached.
        keepgoing = 1;
        while(keepgoing)
            keepgoing = 0;
            SNR_count = 1;
            SNR = SNR_start;
            while SNR <= SNR_stop

                flag = 0;
                if size(results,1) < SNR_count
                    flag = 1;
                else if results(SNR_count,iteration_count+4) < stopping_crit
                        flag = 1;
                    end
                end

                if flag    
                    keepgoing = 1;

                    % Create a figure to plot the results.
                    %figure
                    axes('YScale','log');
                    title('BPSK modulation in an AWGN channel');
                    ylabel('BER');
                    xlabel('SNR (in dB)');
                    if SNR_stop ~= inf
                        xlim([SNR_start, SNR_stop]);
                    end
                    hold on

                    % Convert from SNR (in dB) to noise power spectral density.
                    N0 = 1/(10^(SNR/10));

                    % Counters to store the number of errors and bits simulated so far.

                    bit_count=0;

                    % Keep going until enough errors have been observed. 
                    % This runs the simulation only as long as is required to keep the BER vs SNR curve smooth.
                    while bit_count < 10000% || error_counts(iteration_count) < 10
                        error_counts=zeros(1,iteration_count);
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
                        
                        it1 = 1:length(a_tx);
                        it2 = 1:length(c_tx);
                        it3 = 1:length(d_tx);
                        it4 = 1:length(e_tx);
                        it5 = 1:length(f_tx);
                        

                        if strcmp(chan,'fade')                           


                            if strcmp(final_interleave,'full')

                                it1 = randperm(length(a_tx));
                                it2 = randperm(length(c_tx));
                                it3 = randperm(length(d_tx));
                                it4 = randperm(length(e_tx));
                                it5 = randperm(length(f_tx));
                            else
                                if strcmp(final_interleave,'part')

                                    it1 = 1:length(a_tx);                                    
                                    it2 = randperm(length(c_tx));
                                    it3 = randperm(length(d_tx));
                                    it4 = randperm(length(e_tx));
                                    it5 = randperm(length(f_tx));
                                end
                            end

                            
                            a_tx = a_tx(it1);
                            c_tx = c_tx(it2);
                            d_tx = d_tx(it3);
                            e_tx = e_tx(it4);
                            f_tx = f_tx(it5);




                             %add wobble
                            u = (1:length(a_tx))./50;  
                            v = length(a_tx);
                            a_tx = a_tx.*0.2.*(sin(1.2*u)+.7*sin(2.1*u)+0.5*sin(3*u)+2.5);

                            u = (v+1:v+length(c_tx))./50;  
                            v = v + length(c_tx);
                            c_tx = c_tx.*0.2.*(sin(1.2*u)+.7*sin(2.1*u)+0.5*sin(3*u)+2.5);

                            u = (v+1:v+length(d_tx))./50;
                            v = v + length(d_tx);
                            d_tx = d_tx.*0.2.*(sin(1.2*u)+.7*sin(2.1*u)+0.5*sin(3*u)+2.5);

                            u = (v+1:v+length(e_tx))./50;  
                            v = v + length(e_tx);
                            e_tx = e_tx.*0.2.*(sin(1.2*u)+.7*sin(2.1*u)+0.5*sin(3*u)+2.5);

                            u = (v+1:v+length(f_tx))./50; 
                            f_tx = f_tx.*0.2.*(sin(1.2*u)+.7*sin(2.1*u)+0.5*sin(3*u)+2.5);
                        end

                        % Send the BPSK signal over an AWGN channel
                        a_rx(it1) = a_tx + sqrt(N0/2)*(randn(size(a_tx))+1i*randn(size(a_tx)));
                        c_rx(it2) = c_tx + sqrt(N0/2)*(randn(size(c_tx))+1i*randn(size(c_tx)));
                        d_rx(it3) = d_tx + sqrt(N0/2)*(randn(size(d_tx))+1i*randn(size(d_tx)));
                        e_rx(it4) = e_tx + sqrt(N0/2)*(randn(size(e_tx))+1i*randn(size(e_tx)));
                        f_rx(it5) = f_tx + sqrt(N0/2)*(randn(size(f_tx))+1i*randn(size(f_tx)));

                        % BPSK demodulator
                        % These labels match those used in Figure 2.11 of Liang Li's nine month report.
                        a_c = (abs(a_rx+1).^2-abs(a_rx-1).^2)/N0;
                        c_c = (abs(c_rx+1).^2-abs(c_rx-1).^2)/N0;
                        d_c = (abs(d_rx+1).^2-abs(d_rx-1).^2)/N0;
                        e_c = (abs(e_rx+1).^2-abs(e_rx-1).^2)/N0;
                        f_c = (abs(f_rx+1).^2-abs(f_rx-1).^2)/N0;

                        %puncturing time

                        %bits to throw away
                        no_par = round((frame_length/rate)-frame_length);            
                        punint = randperm(2*frame_length);
                        pun_pat = ([ones([1 no_par]), zeros([1 (2*frame_length-no_par)])]);
                        pun_pat = (pun_pat(punint));

                        c_c = (c_c .* [pun_pat(1:frame_length) 1 1 1]);
                        d_c = (d_c .* [pun_pat(frame_length+1:end) 1 1 1]);

                        % Interleave the systematic LLRs
                        % This label matches that used in Figure 2.11 of Liang Li's nine month report.
                        b_c = a_c(interleaver);

                        % We have no a priori information for the uncoded bits in the first decoding iteration.
                        % This label matches that used in Figure 2.11 of Liang Li's nine month report.
                        a_a = zeros(size(a));

                        % Get ready to start iterating.
                        best_IA = 0;
                        iteration_index = 1;
                        chance = 0;

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

                            % Obtain the a posteriori LLRs.
                            % These labels match those used in Figure 2.11 of Liang Li's nine month report.
                            a_p = a_a + a_c + a_e;

                            % Make a hard decision and see how many bit errors we have.
                            errors = sum((a_p < 0) ~= a);


                            if errors == 0
                                best_errors = 0;
                                % No need to carry on if all the errors have been removed.
                                % It is assumed that a CRC is used to detect that this has happened.
                                chance = chances; 
                            else
                                % See how well the decoding has done.
                                IA = measure_mutual_information_averaging(a_a);

                                % Have we seen an improvement in this iteration?
                                if IA > best_IA
                                    best_IA = IA;
                                    best_errors = errors;
                                else
                                    chance = chance + 1;
                                end
                            end

                            % Accumulate the number of errors and bits that have been simulated so far.
                            error_counts(iteration_index) = error_counts(iteration_index) + best_errors;

                            % Get ready for the next iteration.
                            iteration_index = iteration_index + 1;
                        end

                        % If iterative decoding was stopped early, assume that the same BER would 
                        % have been obtained for all subsequent decoding iterations.
                        while iteration_index <= iteration_count
                            error_counts(iteration_index) = error_counts(iteration_index) + best_errors;

                            iteration_index = iteration_index + 1;
                        end

                        bit_count = bit_count + length(a);

                        if error_counts(end) > 0
                            ser=1;
                        else
                            ser=0;
                        end

                        % Store the SNR and BERs in a matrix and display it.
                        if size(results,1) < SNR_count
                            results(SNR_count,1) = SNR;
                            results(SNR_count,2) = length(a);
                            results(SNR_count,(1:iteration_count)+2) = error_counts;
                            results(SNR_count,(iteration_count)+3) = 1;
                            results(SNR_count,(iteration_count)+4) = ser
                        else
                            results(SNR_count,1) = SNR;
                            results(SNR_count,2) = results(SNR_count,2)+length(a);
                            results(SNR_count,(1:iteration_count)+2) = results(SNR_count,(1:iteration_count)+2) + error_counts;
                            results(SNR_count,(iteration_count)+3) = results(SNR_count,(iteration_count)+3) + 1;
                            results(SNR_count,(iteration_count)+4) = results(SNR_count,(iteration_count)+4) + ser
                        end


                        % Save the results into a binary file. This avoids the loss of precision that is associated with ASCII files.


                    end

                    % For every SNR considered so far, plot the BER obtained after each decoding iteration.
                    for iteration_index = 1:iteration_count           
                        semilogy(results(:,1),results(:,iteration_index+2)./results(:,2));
                    end
                end
                save(filename, 'results', '-MAT');
                % Setup the SNR for the next iteration of the loop.
                SNR = SNR + SNR_delta;
                SNR_count = SNR_count + 1;
            end 
        end
       stopping_crit = stopping_crit * 2; 
        
    end

