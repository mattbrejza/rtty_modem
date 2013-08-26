function [ out ] = maxstar_f( in1,in2 )
%MAXSTAR_F Summary of this function goes here
%   Detailed explanation goes here


%global approx_maxstar;

%if approx_maxstar
%    out = max(in); % Max-Log-MAP approximation
%else

out = max(in1,in2);
diff = abs(in1-in2);
out = out + log(1+exp(-(diff)));
           
end

