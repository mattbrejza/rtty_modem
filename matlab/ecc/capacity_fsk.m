% Calculates the Discrete-input Continuous-output Memoryless Channel (DCMC) capacity of AWGN and uncorrelated Rayleigh fading channels for BPSK, QPSK, 8PSK and 16QAM.
% Rob Maunder 08/08/2008

% Copyright © 2008 Robert G. Maunder. This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

%clear all;

% +---------------------------------------------+
% | Choose the SNR, modulation and channel here |
% +---------------------------------------------+
function cap_fsk = capacity_fsk(order)
% Affects the accuracy and duration of the simulation
symbol_count = 100000;
rng(1);
% Channel SNR
snr = 7; % dB


% Modulation scheme
% -----------------

%order = 2;
% If you add more modulation schemes here, make sure their average transmit power is normalised to unity


% Channel
% -------

% Uncorrelated Rayleigh fading channel
%channel = sqrt(1/2)*(randn(1,symbol_count)+1i*randn(1,symbol_count));

% AWGN channel
channel = ones(order,symbol_count);


% +------------------------+
% | Simulation starts here |
% +------------------------+
snrs = -14:.5:10;
cap_fsk = snrs;
index = 1;

for i = -14:.5:10
snr=i;

    
    
    % Generate some random symbols
    symbols = ceil(order*rand(1,symbol_count));
    %a = round(rand(1,symbol_count));

    % Generate the transmitted signal
    %tx = modulation(symbols);
    tx = zeros([order,symbol_count]);
    for i=1:symbol_count
        tx(symbols(i),i) = 1;
    end

    % Generate some noise
    N0 = 1/(10^(snr/10));
    noise = sqrt(N0/2)*(randn(order,symbol_count)+1i*randn(order,symbol_count));
    

    % Generate the received signal
    rx = tx.*channel+noise;
    rx=rx./order;
    
    % demodulate
    %rx = abs(rx2)-abs(rx1);
    dem = abs(rx);
    for i = 1:order
        for j=1:order
            if i ~= j
                dem(i,:) =  dem(i,:) - abs(rx(j,:));
            end
        end
    end

    % Calculate the symbol probabilities
    %probabilities = max(exp(-(abs(ones(length(modulation),1)*rx - modulation.'*channel).^2)/N0),realmin);
    probabilities = max(exp(-(abs(dem - 1.'*channel).^2)/N0),realmin);

    % Normalise the symbol probabilities
    probabilities = probabilities ./ (ones(order,1)*sum(probabilities));

    % Calculate the capacity
    channel_capacity = log2(order)+mean(sum(probabilities.*log2(probabilities)));

    % Display the capacity
    %disp(['The channel capacity is ', num2str(channel_capacity), ' bits per channel use']);

    %disp([num2str(i), '   ', num2str(channel_capacity)]);
    cap_fsk(index) = channel_capacity;
    index = index + 1;
end

end
