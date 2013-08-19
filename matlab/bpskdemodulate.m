classdef bpskdemodulate < handle

    properties
        F1          %lower freq

        
        baud        %baud rate        
        data
        bits_per_char
   % end
   % properties (GetAccess=private)
        pre_data        %used to save data to allow overlapping in demod
        phase_lo_1=0;   %stores the phase of the LO between windows
        phase_lo_2=0;
        Hd              %filter object
        
        sync_pos = 0;   %counter in the sync, for position in the bit (0->6)
        sync_error = 0; %stores the error (currently an integrator)
        sync_error_int = 0;
        sync_late = 0;  %late gate value
        sync_thres = 2;%value over which the sync should add/skip a cycle
        last_bit = 0;   %last bit value the sync encountered
        
        sync_vco = 0;   %changes from 0 - 14 over a bit period
        sync_vco_actual = 0; %the actual will catch up to the wanted at one place in the bit period
        sync_nom_freq = 1;
        sync_vco_gain = 1/5;
        sync_vco_gain_int = 1/20;
        sync_error_max = 10;
        sync_error_min = -10;
        
        sync2_early = 0;
        sync2_late = 0;
        
        bit2char_i=0;   %counter for number of bits into char
        bit2char_last_bit = 0; %last bit in a window (used in bits2char)
        in_char=0;      %idle/active state of bits2char
        current_char=0; %current working value for bits2char
    end
    properties (Constant)
        Fs=11025    %sample rate
        N = 16      %filter order
        overlap=21*4     %ratio of 21/4
    end
    
    methods
        function obj= bpskdemodulate(baud, F1, bits)
            
            
            obj.baud = baud;
            obj.F1 = F1;

            obj.pre_data=zeros([4 obj.overlap]);
            obj.bits_per_char = bits;
            
            
            Fc   = 0.07285714286;  %;  % Cutoff Frequency
            TM   = 'Rolloff';      % Transition Mode
            R    = 0.4;            % Rolloff
            DT   = 'Normal';       % Design Type
            Beta = 0.5;            % Window Parameter

            % Create the window vector for the design algorithm.
            win = kaiser(obj.N+1, Beta);

            % Calculate the coefficients using the FIR1 function.
            b  = firrcos(obj.N, Fc, R, 2, TM, DT, [], win);
            obj.Hd = dfilt.dffir(b);


        end
        
        function out = getphase(obj, data)
            %outputs the carrier synced to the input
            
          

            N    = 360;         % Order
            Fc   = baud_rate;       % Cutoff Frequency
            TM   = 'Rolloff';  % Transition Mode
            R    = 0.2;        % Rolloff
            DT   = 'Normal';   % Design Type
            Beta = 0.5;        % Window Parameter

            % Create the window vector for the design algorithm.
            win = kaiser(N+1, Beta);

            % Calculate the coefficients using the FIR1 function.
            h  = firrcos(N, Fc/(11025/2), R, 2, TM, DT, [], win);
            Hd = dfilt.dffir(h);
            
            
            m_i = data .*
            
            
            
        end
        
        function out = demod(obj, data)
            %takes a 11025 ksps signal and outputs the filtered baseband at
            %7 samples per symbol (300). Currently only applies a filter
            %suitable for 300
            
            %data needs to overlap a bit due to FIR filter needing inital
            %values, hence 'pre_data's apperance
            
            ins = size(data);
            
            if (ins(1) > ins(2))
                data=transpose(data);
            end
            
            t=1:length(data);
            p= t.*obj.F1 + obj.phase_lo_1;      %generate LO phases
            obj.phase_lo_1 = mod(p(end),1);     %save last phase for next block
            
            pre_data_n = zeros([4 obj.overlap]);
            
            bb_h_s = data .* sin(2.*pi.*p);     %multiply input by LO1 sine
            bb_h_c = data .* cos(2.*pi.*p);     %multiply input by LO1 cos
            pre_data_n(1,:) = bb_h_s(end-obj.overlap+1:end);
            pre_data_n(2,:) = bb_h_c(end-obj.overlap+1:end);
            
            %now repeat for the other tone
            
            p= t.*obj.F2 + obj.phase_lo_2;
            obj.phase_lo_2 = mod(p(end),1);
            
            bb_l_s = data .* sin(2.*pi.*p);
            bb_l_c = data .* cos(2.*pi.*p);
            pre_data_n(3,:) = bb_l_s(end-obj.overlap+1:end);
            pre_data_n(4,:) = bb_l_c(end-obj.overlap+1:end);
            
            %resample, filter, square and add together
            
            bb_h = filter(obj.Hd,resample([ obj.pre_data(1,:) bb_h_s],8,21)).^2  +  filter(obj.Hd,resample([ obj.pre_data(2,:) bb_h_c],8,21)).^2;
            bb_l = filter(obj.Hd,resample([ obj.pre_data(3,:) bb_l_s],8,21)).^2  +  filter(obj.Hd,resample([ obj.pre_data(4,:) bb_l_c],8,21)).^2;
            obj.pre_data = pre_data_n;
          
            %work out how many of the resampled bits are overlap ones, so
            %that they are not outputted
            a=obj.overlap*8/21;
            out=bb_h(a+1:end)-bb_l(a+1:end);
            
        end
        
        function out = sync(obj, in)
            %applies early-late sync, outputting LLRs (AGC needed ideally
            %before block, but atm threshold for early-late based on
            %average of previous values
            
            out_ = zeros([1 int32(length(in)/6)]);  %temp out vector
            out_count = 0;                          %items in the out vector

            errors = zeros(size(in));
            
  plot(in.^2);
  hold on
            
            for i=1:length(in)
                errors(i) = obj.sync_error;
                switch  obj.sync_pos
                    
                    case 1      %early gate
                        if sign(in(i)) ~= sign(obj.last_bit)
                            obj.sync_error = obj.sync_error + in(i).^2 -  obj.sync_late;
                        end  
                        
                        if (obj.sync_error > obj.sync_thres)    %skip cycle if needed
                              obj.sync_pos = obj.sync_pos + 1;
                              obj.sync_error = obj.sync_error - obj.sync_thres;
      line([i i],[-.07 0],'color','yellow');  
                        end
                    case 2
                          if (obj.sync_error < -obj.sync_thres) %add cycle if needed
                              obj.sync_pos = obj.sync_pos - 1;
                              obj.sync_error = obj.sync_error + obj.sync_thres;
     line([i i],[0 .07],'color','green');  
                          end
                          
                    case 3      %sample gate
                        out_count = out_count + 1;
                        out_(out_count) = in(i);
                        obj.last_bit = in(i);
                        
     line([i i],[-.07 .07],'color','red');                       
                        
                    case 5      %late gate
                        obj.sync_late = in(i).^2;
                        
                end
                obj.sync_pos =  mod(obj.sync_pos + 1,7);
            end
  plot(errors*max(in.^2)/max(errors));         
 hold off
            
            out = out_(1:out_count);
            obj.sync_thres = (mean(out.^2));
        end
        
        
        
        
        function out = sync2(obj, in)
            %applies early-late sync, outputting LLRs (AGC needed ideally
            %before block, but atm threshold for early-late based on
            %average of previous values
            obj.sync_error = 0;
            obj.sync_error_int = 0;
            out_ = zeros([1 int32(length(in)/6)]);  %temp out vector
            out_count = 0;                          %items in the out vector

            errors = zeros(size(in));
            vco = zeros(size(in));
           % e = 0;
            
  plot(in);
  hold on
  
            
            for i=1:length(in)
                
                switch  obj.sync_vco_actual
                    case 0
                        obj.sync2_late = sign(in(i));
                    case 1      %early gate
                       obj.sync2_late = obj.sync2_late + sign(in(i));
                    case 2      %early gate
                       obj.sync2_late = obj.sync2_late + sign(in(i));
                    case 3      %early gate
                       obj.sync2_late = obj.sync2_late + sign(in(i));
                    case 4      %early gate
                       obj.sync2_late = obj.sync2_late + sign(in(i));
                    case 5
                       obj.sync2_late = obj.sync2_late + sign(in(i));
                       obj.sync_error =( +abs(obj.sync2_late) - abs(obj.sync2_early));% + obj.sync_error;
                       obj.sync_error_int = obj.sync_error_int + obj.sync_error;
                       %obj.sync_error = min( obj.sync_error, obj.sync_error_max);
                       %obj.sync_error = max( obj.sync_error, obj.sync_error_min);
                     case 7      %sample gate
                        out_count = out_count + 1;
                        out_(out_count) = in(i);
                        obj.last_bit = in(i);
                        line([i i],[-.7 .7],'color','red');  
                       
                       
                    case 6  %slow down/ speed up
                          
                        if obj.sync_vco > 6.6  %speed up
                            out_count = out_count + 1;
                            out_(out_count) = in(i);
                            obj.last_bit = in(i);
                           line([i i],[-.7 .7],'color','red');  
                            obj.sync_vco_actual = 7;
                        
                        else
                            if obj.sync_vco < 5.4
                                obj.sync_vco_actual = 5;
                            end
                        end
                        
                    
                                             
                    case 8
                        obj.sync2_early = sign(in(i));
                    case 9 
                        obj.sync2_early = obj.sync2_early + sign(in(i));
                    case 10
                        obj.sync2_early = obj.sync2_early + sign(in(i));
                    case 11
                        obj.sync2_early = obj.sync2_early + sign(in(i));
                    case 12
                        obj.sync2_early = obj.sync2_early + sign(in(i));
                    case 13
                        obj.sync2_early = obj.sync2_early + sign(in(i));
                        
                end
                
                r =  obj.sync_vco_gain * obj.sync_error + obj.sync_error_int * obj.sync_vco_gain_int;
                
                %r = min( r, 3);
                %r = max( r, -3);
                
                obj.sync_vco_actual =  mod(obj.sync_vco_actual + 1,14);
                obj.sync_vco =  mod(obj.sync_vco + obj.sync_nom_freq + (r/14),14);
                errors(i) = r;
                vco(i) = obj.sync_vco;
            end
  plot(errors,'color','green');  
 % plot(vco,'color','red'); 
 % plot(errors,'color','green');  
 hold off
            
            out = out_(1:out_count);
            obj.sync_thres = (mean(out.^2));
        end
        
        function out = getchars(obj, in)
            %converts a bit stream into chars
            %will in the end use $$ to sync as well as the start bit of
            %'rs232' rtty
            
            in = [obj.bit2char_last_bit in];    %add on the last character so that low to high (start bit) can be detected
            out_ = int32(length(in)/(obj.bits_per_char+2));
            out_c = 0;
            
            for i=2:length(in)
                
                %if currently processing a character, read next bit into
                %the character
                if obj.in_char
                    
                    if (in(i) < 0)  %if 1
                        obj.current_char = obj.current_char + 2^obj.bit2char_i;
                    end
    
                    %increase bits counter
                    obj.bit2char_i = obj.bit2char_i + 1;
                    
                    %if character has all its bits, reset everything
                    if obj.bit2char_i >= obj.bits_per_char
                       obj.in_char = 0;                 %no longer processing character
                       obj.bit2char_i = 0;
                       out_c = out_c + 1;
                       out_(out_c) = obj.current_char;  %place on OP buffer
                       obj.current_char = 0;
                    end
                    
                else            %otherwise look for the start of a char
                    if (in(i) > 0) && (in(i-1) < 0)     %look for low to high
                        obj.in_char = 1;                %flag start found
                    end
                end                
            end 
             out = char(out_(1:out_c));
             obj.bit2char_last_bit = in(end);
        end        
    end

end

