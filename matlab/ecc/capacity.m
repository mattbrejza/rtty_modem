% Calculates the Discrete-input Continuous-output Memoryless Channel (DCMC) capacity of AWGN and uncorrelated Rayleigh fading channels for BPSK, QPSK, 8PSK and 16QAM.
% Rob Maunder 08/08/2008

% Copyright © 2008 Robert G. Maunder. This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

%clear all;

% +---------------------------------------------+
% | Choose the SNR, modulation and channel here |
% +---------------------------------------------+

% Affects the accuracy and duration of the simulation
symbol_count = 10;%00000;

% Channel SNR
snr = 7; % dB


% Modulation scheme
% -----------------

% 2PSK
%modulation = [+1, -1];

% 4PSK
%modulation = [+1, +i, -1, -i];

% 8PSK
modulation = [+1, sqrt(1/2)*(+1+i), +i, sqrt(1/2)*(-1+i), -1, sqrt(1/2)*(-1-i), -i, sqrt(1/2)*(+1-i)];

% 16QAM
%modulation = sqrt(1/10)*[-3+3*1i, -1+3*1i, +1+3*1i, +3+3*1i, -3+1*1i, -1+1*1i, +1+1*1i, +3+1*1i, -3-1*1i, -1-1*1i, +1-1*1i, +3-1*1i, -3-3*1i, -1-3*1i, +1-3*1i, +3-3*1i];

% If you add more modulation schemes here, make sure their average transmit power is normalised to unity


% Channel
% -------

% Uncorrelated Rayleigh fading channel
%channel = sqrt(1/2)*(randn(1,symbol_count)+1i*randn(1,symbol_count));

% AWGN channel
channel = ones(1,symbol_count);


% +------------------------+
% | Simulation starts here |
% +------------------------+
snrs = -14:.5:10;
cap = snrs;
index = 1;

for i = -14:.5:10
snr=i;

    
    
    % Generate some random symbols
    symbols = ceil(length(modulation)*rand(1,symbol_count));

    % Generate the transmitted signal
    tx = modulation(symbols);

    % Generate some noise
    N0 = 1/(10^(snr/10));
    noise = sqrt(N0/2)*(randn(1,symbol_count)+1i*randn(1,symbol_count));

    % Generate the received signal
    rx = tx.*channel+noise;

    % Calculate the symbol probabilities
    probabilities = max(exp(-(abs(ones(length(modulation),1)*rx - modulation.'*channel).^2)/N0),realmin);

    % Normalise the symbol probabilities
    probabilities = probabilities ./ (ones(length(modulation),1)*sum(probabilities));

    % Calculate the capacity
    channel_capacity = log2(length(modulation))+mean(sum(probabilities.*log2(probabilities)));

    % Display the capacity
    %disp(['The channel capacity is ', num2str(channel_capacity), ' bits per channel use']);

    %disp([num2str(i), '   ', num2str(channel_capacity)]);
    cap(index) = channel_capacity;
    index = index + 1;
end


