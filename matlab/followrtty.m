function [ f1_n, f2_n ] = followrtty( samples, f1, f2 )
%FOLLOWRTTY Summary of this function goes here
%   Assuming 11025 sample rate
%   Inputs are actual freq (rather then /sample rate)
%   
%search range is 150Hz either side of carrier (150/11025*1024 = 14 bins)
w_r = 14;
f1_n = 0;
f2_n = 0;

if (length(samples) < 1024)
    return;
end

if (f1 - (150/11025)) < 0  || (f2 - (150/11025)) < 0
    return;
end

c = abs(fft(samples(1:1024)));

int_f1 = zeros([1 512]);
int_f2 = zeros([1 512]);

bin_f1 =int32(f1/11025 * 1024);
bin_f2 =int32(f2/11025 * 1024);

max_f1=0;
max_f2=0;

for i=bin_f1-w_r+1:bin_f1+w_r
    int_f1(i) = int_f1(i-1) + c(i);
end
max_f1=int_f1(i);

for i=bin_f2-w_r+1:bin_f2+14
    int_f2(i) = int_f2(i-1) + c(i);
end
max_f2=int_f2(i);

plot(1:512,int_f1,1:512,int_f2);
midbin_1=0;
midbin_2=0;
for i=1:512
    if (midbin_1 == 0) &&(int_f1(i) > (max_f1/2))
        midbin_1=i-1;
    end
    if (midbin_2 == 0) &&(int_f2(i) > (max_f2/2))
        midbin_2=i-1;
    end
end

f1=midbin_1/1024;
f2=midbin_2/1024;
f1*11025
f2*11025

end

