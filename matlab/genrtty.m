function [ out ] = genrtty( samples, sample_rate, sequence, baud_rate, seperation, centre, SNR, bits, stops )
%GENRTTY Summary of this function goes here
%   Detailed explanation goes here

if nargin < 1
    samples = 50000;
end
if nargin < 2
    sample_rate = 48000;
end
if nargin < 3
    sequence = '';
end
if nargin < 4
    baud_rate = 300;
end
if nargin < 5
    seperation = 600;
end
if nargin < 6
    centre = 1400;
end
if nargin < 7
    SNR = 8;
end
if nargin < 8
    bits = 7;
end
if nargin < 9
    stops = 2;
end

out = zeros([1 samples]);

%approx number of symbols
bits_tot = ceil((samples/sample_rate)*baud_rate);
syms_tot = ceil(bits_tot/(bits+stops+1));
bits_tot = syms_tot * (bits+stops+1);

%generate random string
if length(sequence) == length('')
    symbols = [' ' 'a':'z' 'A':'Z' '0':'9' '$' ':' '*' ];
    numRands = length(symbols)-1;
    randmsn = ceil(round(rand(1,syms_tot).*numRands))+1;
    sequence = symbols( randmsn);



else
    
    if length(sequence) < syms_tot
        sequence = [sequence zeros([1 (syms_tot-length(sequence))])];
    else
        sequence = sequence(1:syms_tot);
    end
    
end

bitindex = 1;
bits_in = zeros([1 bits_tot]);

for i=1:length(sequence)
    
    bits_in(bitindex) = 1; %start bit
    bitindex = bitindex + 1;
    
    chara = sequence(i);
    k = 1;
    for j=1:bits
        if(bitand(k,chara+0));
            bits_in(bitindex) = -1;
        else
            bits_in(bitindex) = 1;
        end
        k = k * 2;
        bitindex = bitindex + 1;
    end
    
    for s=1:stops
       bits_in(bitindex) = -1; %stop bit
       bitindex = bitindex + 1;
    end
    
end

bits_in = [ones([1 100]).*-1  bits_in];

bits_in = bits_in .*(seperation/2);

samples_per_bit = sample_rate/baud_rate;

s = 1;
s = s + samples_per_bit;
last_s = 1;


for i=1:length(bits_in)
    if (s <= samples)
        out(int32(last_s):int32(s)) = bits_in(i);
        last_s = s;
        s = s + samples_per_bit;
        
    end
end


Fs = sample_rate;  % Sampling Frequency

N     = 10;   % Order
Fpass = 3.*baud_rate;  % Passband Frequency
Apass = 1;    % Passband Ripple (dB)

% Construct an FDESIGN object and call its CHEBY1 method.
h  = fdesign.lowpass('N,Fp,Ap', N, Fpass, Apass, Fs);
Hd = design(h, 'cheby1');

out = filter(Hd,out);

t=1:samples;
t = -1.*t./sample_rate;

integ = 0;
for i=1:samples
    integ = integ + (out(i)/sample_rate);
    out(i) = sin(2*pi*(centre*t(i)  +  integ));
end

%out = out + random('norm',0,5/SNR,[1 length(out)]);

end

