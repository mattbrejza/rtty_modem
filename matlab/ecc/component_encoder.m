% Encoder function for a terminated unity-rate recursive convolutional code
% having 3 memory elements, a generator polynomial of [1,1,0,1] and a feedback
% polynomial of [1,0,1,1]. This is as used in the UMTS turbo code, as specified 
% in ETSI TS 125 212 (search for it on Google if you like). For more 
% information, see Section 2.2.1 of Liang Li's nine-month report 
% (http://users.ecs.soton.ac.uk/rm/wp-content/liang_li_nine_month_report.pdf)
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

% uncoded_bits is a 1xK vector of uncoded bits
% encoded_bits is a 1x(K+3) vector of encoded bits
% termination_bits is a 1x3 vector of termination bits
function [encoded_bits, termination_bits] = component_encoder(uncoded_bits)

    % Initialise our output bit vectors
    encoded_bits = zeros(1,length(uncoded_bits)+3);
    termination_bits = zeros(1,3);
    
    % We start in the all-zeros state
    s1 = 0;
    s2 = 0;
    s3 = 0;
    
    % Encode the uncoded bit sequence
    for bit_index = 1:length(uncoded_bits)
        
        % Determine the next state
        s1_plus = mod(uncoded_bits(bit_index)+s2+s3, 2); % This uses the feedback polynomial
        s2_plus = s1;
        s3_plus = s2;
	
        % Determine the encoded bit
        encoded_bits(bit_index) = mod(s1_plus+s1+s3, 2); % This uses the generator polynomial
        
        % Enter the next state
        s1 = s1_plus;
        s2 = s2_plus;
        s3 = s3_plus;
    end
    
    % Terminate the convolutional code
    for bit_index = 1:3
                
        % Determine the next state
        s1_plus = 0; % During termination, zeros are clocked into the shift register
        s2_plus = s1;
        s3_plus = s2;
	
        % Determine the termination bit
        termination_bits(bit_index) = mod(s2+s3, 2); % This uses the feedback polynomial
        % Determine the encoded bit
        encoded_bits(length(uncoded_bits)+bit_index) = mod(s1_plus+s1+s3, 2); % This uses the generator polynomial
        
        % Enter the next state
        s1 = s1_plus;
        s2 = s2_plus;
        s3 = s3_plus;
    end
    
end