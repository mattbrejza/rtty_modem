% BCJR algorithm for a terminated unity-rate recursive convolutional code
% having 3 memory elements, a generator polynomial of [1,1,0,1] and a feedback
% polynomial of [1,0,1,1]. This is as used in the UMTS turbo code, as specified
% in ETSI TS 125 212 (search for it on Google if you like). For more
% information, see Section 2.2.2 of Liang Li's nine-month report
% (http://users.ecs.soton.ac.uk/rm/wp-content/liang_li_nine_month_report.pdf)
% or the BCJR paper (http://ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=1055186).
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



% c_tilde_a is a matrix of a priori encoded LLRs
% b_tilde_a is a vector of a priori uncoded LLRs
% b_tilde_e is a vector of extrinsic uncoded LLRs
function [b_tilde_e, c_tilde_e,pivi_out] = URC_decoder_bcjr_LTE_dual(c_tilde_a, b_tilde_a,win_len,pivi_mem)

bit_count = length(b_tilde_a);

if(size(c_tilde_a,2) ~= bit_count)
    error('LLR sequences must have the same length');
end



en_pivi = 0;
if nargin >= 4
    en_pivi = 1;
    if size(pivi_mem) ~= size(zeros([8 bit_count/win_len]))
        error('incorrect pivi mem size');
    end   
end

if mod(bit_count,win_len) > 0  && en_pivi>0
    error('WIN_LEN must divide into bit_count');
end

global maxstar_mode;
maxstar_mode = 0;

% All calculations are performed in the logarithmic domain in order to
% avoid numerical issues. These occur in the normal domain, because some of
% the confidences can get smaller than the smallest number the computer can
% store. See Section 2.2.2 of Liang Li's nine month report for more information
% on this.
%
% A multiplication of two confidences is achieved using the addition of the
% corresponding log-confidences. If A = log(a) and B = log(b), then
% log(a*b) = A+B (Equation 2.12 in Liang Li's nine month report).
%
% An addition of two confidences is achieved using the Jacobian logarithm
% of the corresponding log-confidences. The Jacobian logarithm is defined
% in the jac.m file. If A = log(a) and B = log(b), then
% log(a+b) = max(A,B) + log(1+exp(-abs(A-B))) (Equation 2.12 in Liang Li's
% nine month report).

% Matrix to describe the trellis
% Each row describes one transition in the trellis
% Each state is allocated an index 1,2,3,... Note that this list starts
% from 1 rather than 0.

% %LTE:
% %               FromState,  ToState,    UncodedBit, EncodedBit1
%     transitions =  [1,          1,          0,          0; 
%                     2,          5,          0,          0; 
%                     3,          6,          0,          1; 
%                     4,          2,          0,          1; 
%                     5,          3,          0,          1; 
%                     6,          7,          0,          1; 
%                     7,          8,          0,          0; 
%                     8,          4,          0,          0; 
%                     1,          5,          1,          1; 
%                     2,          1,          1,          1; 
%                     3,          2,          1,          0; 
%                     4,          6,          1,          0; 
%                     5,          7,          1,          0; 
%                     6,          3,          1,          0; 
%                     7,          4,          1,          1; 
%                     8,          8,          1,          1];

%URC8:
%               FromState,  ToState,    UncodedBit, EncodedBit1
    transitions =  [1,          1,          0,          0; 
                    2,          5,          0,          1; 
                    3,          6,          0,          1; 
                    4,          2,          0,          0; 
                    5,          7,          0,          1; 
                    6,          3,          0,          0; 
                    7,          4,          0,          0; 
                    8,          8,          0,          1; 
                    1,          5,          1,          1; 
                    2,          1,          1,          0; 
                    3,          2,          1,          0; 
                    4,          6,          1,          1; 
                    5,          3,          1,          0; 
                    6,          7,          1,          1; 
                    7,          8,          1,          1; 
                    8,          4,          1,          0];


% Find the largest state index in the transitions matrix
% In this example, we have eight states since the code has three memory elements
state_count = max(max(transitions(:,1)),max(transitions(:,2)));
transition_count = size(transitions,1);
codeword_length = size(c_tilde_a,1);

gammas = zeros(transition_count, bit_count);
for transition_index = 1:transition_count
    if transitions(transition_index, 3)
        gammas(transition_index, b_tilde_a ~= inf) = b_tilde_a(b_tilde_a ~= inf);
    else
        gammas(transition_index, b_tilde_a == inf) = -inf;
    end
end
for codebit_index = 1:codeword_length
    for transition_index = 1:transition_count
        if transitions(transition_index, 3+codebit_index)
            gammas(transition_index, c_tilde_a(codebit_index,:) ~= inf) = (gammas(transition_index, c_tilde_a(codebit_index,:) ~= inf) + c_tilde_a(codebit_index, c_tilde_a(codebit_index,:) ~= inf));
            %gammas(transition_index, c_tilde_a(codebit_index,:) ~= inf) = add(gammas(transition_index, c_tilde_a(codebit_index,:) ~= inf),c_tilde_a(codebit_index, c_tilde_a(codebit_index,:) ~= inf));
        else
            gammas(transition_index, c_tilde_a(codebit_index,:) == inf) = -inf;
        end
    end
end

% Forward recursion to calculate state log-confidences. This is similar to
% Equation 1.13 in Rob's thesis or Equations 5 and 6 in the BCJR paper.
alphas=-inf*ones(state_count,bit_count);
alphas(1,1)=0; % We know that this is the first state
for bit_index = 2:bit_count
    set = zeros(1,state_count);
    for transition_index = 1:transition_count
        if(set(transitions(transition_index,2)))
            alphas(transitions(transition_index,2),bit_index) = max(alphas(transitions(transition_index,2),bit_index), (alphas(transitions(transition_index,1),bit_index-1)+ gammas(transition_index, bit_index-1)));
           % alphas(transitions(transition_index,2),bit_index) = maxstar(alphas(transitions(transition_index,2),bit_index), add(alphas(transitions(transition_index,1),bit_index-1), gammas(transition_index, bit_index-1)));
        else
            alphas(transitions(transition_index,2),bit_index) = (alphas(transitions(transition_index,1),bit_index-1) + gammas(transition_index, bit_index-1));
           % alphas(transitions(transition_index,2),bit_index) = add(alphas(transitions(transition_index,1),bit_index-1), gammas(transition_index, bit_index-1));
            set(transitions(transition_index,2)) = 1;
        end
    end
end

%changed to PIVI
% Backwards recursion to calculate state log-confidences. This is similar
% to Equation 1.14 in Rob's thesis or Equations 7 and 8 in the BCJR paper.

%pivi_mem = zeros([transition_count bit_count]);
inter_cnt = 0;
prev_betas = zeros([transition_count 1]);
win_cnt = 1;
betas=-inf*ones(state_count,bit_count);
betas(:,end)=0; % We don'y know which is the final state
inter_cnt = 1;

for bit_index = bit_count-1:-1:1
    if (inter_cnt >= win_len) && (en_pivi>0)     %save last beta data to pivi and load 
        inter_cnt = 0;
        
        prev_betas = pivi_mem(:,win_cnt); 
        pivi_mem(:,win_cnt) = betas(:,bit_index+1); 
        
        win_cnt = win_cnt + 1;
    else
       prev_betas = betas(:,bit_index+1); 
    end
        
    set = zeros(1,state_count);
    for transition_index = 1:transition_count
        if(set(transitions(transition_index,1)))
             betas(transitions(transition_index,1),bit_index) = max(betas(transitions(transition_index,1),bit_index), (prev_betas(transitions(transition_index,2))+ gammas(transition_index, bit_index+1)));
           % betas(transitions(transition_index,1),bit_index) = maxstar(betas(transitions(transition_index,1),bit_index), add(prev_betas(transitions(transition_index,2)), gammas(transition_index, bit_index+1)));
        else
            betas(transitions(transition_index,1),bit_index) = (prev_betas(transitions(transition_index,2)) + gammas(transition_index, bit_index+1));
            %betas(transitions(transition_index,1),bit_index) = add(prev_betas(transitions(transition_index,2)), gammas(transition_index, bit_index+1));
            set(transitions(transition_index,1)) = 1;
        end
    end
    
    inter_cnt=inter_cnt+1;
end

% Calculate a posteriori transition log-confidences. This is similar to
% Equation 1.15 in Rob's thesis or Equation 4 in the BCJR paper.
deltas=zeros(transition_count,bit_count);
for transition_index = 1:transition_count
    deltas(transition_index, :) = add(alphas(transitions(transition_index,1),:), betas(transitions(transition_index,2),:), gammas(transition_index, :));
end



% Calculate the extrinsic LLRs. This is similar to Equation 1.16 in
% Rob's thesis.
prob1=-inf*ones(1,bit_count);
prob0=-inf*ones(1,bit_count);
set1 = 0;
set0 = 0;
for transition_index = 1:transition_count
    if transitions(transition_index,3)
        if set1
            prob1 = max(prob1, deltas(transition_index,:));
        else
            prob1 = deltas(transition_index,:);
            set1 = 1;
        end
    else
        if set0
            prob0 = max(prob0, deltas(transition_index,:));
        else
            prob0 = deltas(transition_index,:);
            set0 = 1;
        end
    end
    
end
bc_tilde_p = add(prob1,-prob0);

if en_pivi
    pivi_out = pivi_mem;
else
    pivi_out = 0; % size(zeros([8 bit_count/win_len]));
end
    
    
b_tilde_e(~isinf(b_tilde_a)) = add(bc_tilde_p(1,~isinf(b_tilde_a)), -b_tilde_a(~isinf(b_tilde_a)));
b_tilde_e(isinf(b_tilde_a)) = b_tilde_a(isinf(b_tilde_a));



prob1=-inf*ones(1,bit_count);
prob0=-inf*ones(1,bit_count);
set1 = 0;
set0 = 0;
for transition_index = 1:transition_count
    if transitions(transition_index,4)
        if set1
            prob1 = max(prob1, deltas(transition_index,:));
        else
            prob1 = deltas(transition_index,:);
            set1 = 1;
        end
    else
        if set0
            prob0 = max(prob0, deltas(transition_index,:));
        else
            prob0 = deltas(transition_index,:);
            set0 = 1;
        end
    end
    
end
bc_tilde_p = add(prob1,-prob0);




c_tilde_e(~isinf(c_tilde_a)) = add(bc_tilde_p(1,~isinf(c_tilde_a)), -c_tilde_a(~isinf(c_tilde_a)));
c_tilde_e(isinf(c_tilde_a)) = c_tilde_a(isinf(c_tilde_a));


