% Generate Gaussian distributed a priori LLRs
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

% bits is a 1xK vector of bits
% mutual_information is a scalar in the range 0 to 1
% llrs is a 1xK vector of LLRs
function llrs = generate_llrs(bits, mutual_information)

    if mutual_information < 0
        error('mutual information is too low!');
    elseif mutual_information > 1;
        error('mutual information is too high!');        
    elseif mutual_information == 0
        llrs = zeros(size(bits)); % Output a vector of zero valued LLRs when the requested MI is 0
    elseif mutual_information == 1;
       llrs = -2*(bits-0.5)*inf; % Output a vector of 
    else
        % Generate some Gaussian distributed random numbers having a mean of 0 and a standard deviation of 1
        random = randn(1,length(bits));
        
        % The initial range of MI values to consider
        lower = 0;
        upper = 1;
        
        % Keep narrowing down the considered range of MI values until we reach the MI specified in the function parameter
        keep_going = 1;        
        while keep_going
            
            % Approximate the standard deviation that corresponds to the MI in the middle of the range
            sigma = (-1.0/0.3073*log(1.0-((lower+upper)/2)^(1.0/1.1064))/log(2.0))^(1.0/(2.0*0.8935));

            % Give the standard deviation sigma and the mean sigma^2/2 to the LLRs
            llrs = random*sigma - (bits-0.5)*sigma^2;
            
            % See what MI we ended up with
            true_mutual_information = measure_mutual_information_averaging(llrs);
            
            % Adjust the range if the MI we ended up with is too high or too low
            if abs(true_mutual_information-mutual_information) < 0.00001
                keep_going = 0;
            elseif true_mutual_information > mutual_information
                upper = (lower+upper)/2;
            else
                lower = (lower+upper)/2;
            end          
        end
    end
end

