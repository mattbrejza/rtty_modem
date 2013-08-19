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



% apriori_uncoded_llrs is a 1x(K+3) vector of a priori uncoded LLRs
% apriori_encoded_llrs is a 1x(K+3) vector of a priori encoded LLRs
% extrinsic_uncoded_llrs is a 1x(K+3) vector of extrinsic encoded LLRs
function extrinsic_uncoded_llrs = component_decoder(apriori_uncoded_llrs, apriori_encoded_llrs)

    if(length(apriori_uncoded_llrs) ~= length(apriori_encoded_llrs))
        error('LLR sequences must have the same length');
    end


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
    % An addition of two confidences is achieved using the maxstarobian logarithm
    % of the corresponding log-confidences. The maxstarobian logarithm is defined
    % in the maxstar.m file. If A = log(a) and B = log(b), then 
    % log(a+b) = max(A,B) + log(1+exp(-abs(A-B))) (Equation 2.12 in Liang Li's
    % nine month report).

    % Matrix to describe the trellis
    % Each row describes one transition in the trellis
    % Each state is allocated an index 1,2,3,... Note that this list starts 
    % from 1 rather than 0.
    %               FromState,  ToState,    UncodedBit, EncodedBit
    transitions =  [1,          1,          0,          0; 
                    2,          5,          0,          0; 
                    3,          6,          0,          1; 
                    4,          2,          0,          1; 
                    5,          3,          0,          1; 
                    6,          7,          0,          1; 
                    7,          8,          0,          0; 
                    8,          4,          0,          0; 
                    1,          5,          1,          1; 
                    2,          1,          1,          1; 
                    3,          2,          1,          0; 
                    4,          6,          1,          0; 
                    5,          7,          1,          0; 
                    6,          3,          1,          0; 
                    7,          4,          1,          1; 
                    8,          8,          1,          1];
               
    % Find the largest state index in the transitions matrix           
    % In this example, we have eight states since the code has three memory elements
    state_count = max(max(transitions(:,1)),max(transitions(:,2)));

    % Calculate the uncoded a priori transition log-confidences by adding the
    % log-confidences associated with each corresponding bit value. This is
    % similar to Equation 2.14 in Liang Li's nine month report or Equation 9 in the 
    % BCJR paper.
    uncoded_gammas=zeros(size(transitions,1),length(apriori_uncoded_llrs));
    for bit_index = 1:length(apriori_uncoded_llrs)
       for transition_index = 1:size(transitions,1)
          if transitions(transition_index, 3)==0
              uncoded_gammas(transition_index, bit_index) = apriori_uncoded_llrs(bit_index); 
          end
       end
    end

    % Calculate the encoded a priori transition log-confidences by adding the
    % log-confidences associated with each corresponding bit value. This is
    % similar to Equation 2.15 in Liang Li's nine month report or Equation 9 in the 
    % BCJR paper.
    encoded_gammas=zeros(size(transitions,1),length(apriori_uncoded_llrs));
    for bit_index = 1:length(apriori_uncoded_llrs)
       for transition_index = 1:size(transitions,1)
          if transitions(transition_index, 4)==0
              encoded_gammas(transition_index, bit_index) = apriori_encoded_llrs(bit_index); 
          end
       end
    end
    
    % Forward recursion to calculate state log-confidences. This is similar to
    % Equation 2.16 in Liang Li's nine month report or Equations 5 and 6 in the BCJR paper.
    alphas=zeros(state_count,length(apriori_uncoded_llrs));
    alphas=alphas-10000;
    alphas(1,1)=0; % We know that this is the first state
   
    for bit_index = 2:length(apriori_uncoded_llrs)
    	temp1 = zeros([8 1]);
        temp2 = zeros([8 1]);
       for transition_index = 1:size(transitions,1)
           if temp1(transitions(transition_index,2)) == 0
               temp1(transitions(transition_index,2)) = alphas(transitions(transition_index,1),bit_index-1) + uncoded_gammas(transition_index, bit_index-1) + encoded_gammas(transition_index, bit_index-1);
           else
               temp2(transitions(transition_index,2)) = alphas(transitions(transition_index,1),bit_index-1) + uncoded_gammas(transition_index, bit_index-1) + encoded_gammas(transition_index, bit_index-1);
              % alphas(transitions(transition_index,2),bit_index) = maxstar([alphas(transitions(transition_index,2),bit_index), dgggegeg ]);   
           end
       end
       alphas(:,bit_index) = maxstar_f(temp1,temp2);
    end
    

    % Backwards recursion to calculate state log-confidences. This is similar
    % to Equation 2.17 in Liang Li's nine month report or Equations 7 and 8 in the BCJR paper.
    betas=zeros(state_count,length(apriori_uncoded_llrs));
    betas=betas-10000;
    betas(1,length(apriori_uncoded_llrs))=0; % We know that this is the last state because the trellis is terminated
    for bit_index = length(apriori_uncoded_llrs)-1:-1:1
       temp1 = zeros([8 1]);
       temp2 = zeros([8 1]);
       for transition_index = 1:size(transitions,1)
           if temp1(transitions(transition_index,1)) == 0
               temp1(transitions(transition_index,1)) = betas(transitions(transition_index,2),bit_index+1) + uncoded_gammas(transition_index, bit_index+1) + encoded_gammas(transition_index, bit_index+1);
           else
               temp2(transitions(transition_index,1)) = betas(transitions(transition_index,2),bit_index+1) + uncoded_gammas(transition_index, bit_index+1) + encoded_gammas(transition_index, bit_index+1);
              % alphas(transitions(transition_index,2),bit_index) = maxstar([alphas(transitions(transition_index,2),bit_index), dgggegeg ]);   
           end
          % betas(transitions(transition_index,1),bit_index) = maxstar([betas(transitions(transition_index,1),bit_index),betas(transitions(transition_index,2),bit_index+1) + uncoded_gammas(transition_index, bit_index+1) + encoded_gammas(transition_index, bit_index+1)]);   
       end
       betas(:,bit_index) = maxstar_f(temp1,temp2);
    end
    

    % Calculate uncoded extrinsic transition log-confidences. This is similar to
    % Equation 2.18 in Liang Li's nine month report or Equation 4 in the BCJR paper.
    deltas=zeros(size(transitions,1),length(apriori_uncoded_llrs));
    %for bit_index = 1:length(apriori_uncoded_llrs)
       for transition_index = 1:size(transitions,1)
           deltas(transition_index, :) = alphas(transitions(transition_index,1),:) + encoded_gammas(transition_index, :) + betas(transitions(transition_index,2),:);
       end
    %end

    % Calculate the uncoded extrinsic LLRs. This is similar to Equation 2.19 in
    % Liang Li's nine month report.
    extrinsic_uncoded_llrs = zeros(1,length(apriori_uncoded_llrs));
   % for bit_index = 1:length(apriori_uncoded_llrs)    
       prob0=zeros([1 length(apriori_uncoded_llrs)]) - 10000;
       prob1=prob0;
       for transition_index = 1:size(transitions,1)
           if transitions(transition_index,3)==0
               prob0 = maxstar_f(prob0, deltas(transition_index,:));
           else
               prob1 = maxstar_f(prob1, deltas(transition_index,:));
           end      
       end
       extrinsic_uncoded_llrs = prob0-prob1;
   % end

end