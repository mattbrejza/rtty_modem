% Copyright (C) 2013  Robert G. Maunder

% This program is free software: you can redistribute it and/or modify it
% under the terms of the GNU General Public License as published by the
% Free Software Foundation, either version 3 of the License, or (at your
% option) any later version.

% This program is distributed in the hope that it will be useful, but
% WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
% Public License for more details.

% The GNU General Public License can be seen at http://www.gnu.org/licenses/.

% Performs the maxstar operation
% in is a vector or matrix of operands
% out is a scalar or vector, provding the maxstar combination of the operands
function out = maxstar(in)

%global approx_maxstar;

%if approx_maxstar
%    out = max(in); % Max-Log-MAP approximation
%else
    if size(in,1) == 1
        in = in';
    end
    out = in(1,:);
    for index = 2:size(in,1)        
        difference = out-in(index,:);
        difference(isnan(difference)) = 0;
        out = max(out,in(index,:)) + log(1+exp(-abs(difference))); % Log-MAP
%    end
end
   

    