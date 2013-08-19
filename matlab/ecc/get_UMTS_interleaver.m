% Generate the UMTS interleaver as specified in ETSI TS 125 212 (search for it on Google if you like)
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

% K is a scalar in the range 40 to 5114 that specifies the length of the interleaver
% interleaver is a 1xK vector of unique integers in the range 1 to K
function interleaver = get_UMTS_interleaver(K)

    sequence = 1:K;

    p_values = [7    11    13    17    19    23    29    31    37    41    43    47    53    59    61    67    71    73    79    83    89 97   101   103   107   109   113   127   131   137   139   149   151   157   163   167   173   179   181   191   193   197   199   211   223 227   229   233   239   241   251   257];
    v_values = [3 2 2 3 2 5 2 3 2 6 3 5 2 2 2 2 7 5 3 2 3 5 2 5 2 6 3 3 2 3 2 2 6 5 2 5 2 2 2 19 5 2 3 2 3 2 6 3 7 7 6 3];
    
    value_index = 0;
    
    if K <= 39
        error('K < 40!');
    elseif K <= 159
        R = 5;
        T = [4 3 2 1 0];
    elseif K <= 200
        R = 10;
        T = [9 8 7 6 5 4 3 2 1 0];
    elseif K <= 480
        R = 20;
        T = [19 9 14 4 0 2 5 7 12 18 10 8 13 17 3 1 16 6 15 11];        
    elseif K <= 530
        value_index = 13;
        p = p_values(value_index);
        C = p;
        R = 10;
        T = [9 8 7 6 5 4 3 2 1 0];
    elseif K <= 2280;
        R = 20;
        T = [19 9 14 4 0 2 5 7 12 18 10 8 13 17 3 1 16 6 15 11];
    elseif K <= 2480;
        R = 20;
        T = [19 9 14 4 0 2 5 7 12 18 16 13 17 15 3 1 6 11 8 10];
    elseif K <= 3160;
        R = 20;
        T = [19 9 14 4 0 2 5 7 12 18 10 8 13 17 3 1 16 6 15 11];
    elseif K <= 3210;
        R = 20;
        T = [19 9 14 4 0 2 5 7 12 18 16 13 17 15 3 1 6 11 8 10];
    elseif K <= 5114;
        R = 20;
        T = [19 9 14 4 0 2 5 7 12 18 10 8 13 17 3 1 16 6 15 11];
    else
        error('K > 5114!');
    end

    if value_index == 0
    
        
        value_index = 1;
        while K > R*(p_values(value_index)+1)
            value_index = value_index+1;
        end
        p = p_values(value_index);
        
        if K <= R*(p-1)
            C = p-1;
        elseif K <= R*p
            C = p;
        else
            C = p+1;
        end
    end
    
    matrix = reshape([sequence,zeros(1,R*C-length(sequence))],C,R)';
    
    v = v_values(value_index);
    
    s = zeros(1,p-1);
    s(1) = 1;
    for j = 2:length(s)
        s(j) = mod(v*s(j-1),p);
    end
    
    q = zeros(1,R);
    q(1) = 1;
    for i = 2:length(q)
        
        prime_index = 1;
        while ~(p_values(prime_index) > q(i-1) && gcd(p_values(prime_index),p-1) == 1)
            prime_index = prime_index+1;
        end
        q(i) = p_values(prime_index);
    end
    
    r=zeros(size(q));
    r(T+1) = q;

    for i = 0:(R-1)
        if C == p
            U = [s(mod((0:(p-2))*r(i+1), p-1)+1),0];
        elseif C == p+1
            U = [s(mod((0:(p-2))*r(i+1), p-1)+1),0,p];
            if i == R-1 && K == R*C
                U([1,length(U)]) = U([length(U),1]);
            end
        elseif C == p-1
            U = s(mod((0:(p-2))*r(i+1), p-1)+1)-1;
        end
        
        matrix(i+1,:) = matrix(i+1,U+1);    
        
    end
    
    matrix(:,:) = matrix(T+1,:);
    
    interleaver = reshape(matrix, 1, R*C);
    
    interleaver = interleaver(interleaver > 0);
    
end