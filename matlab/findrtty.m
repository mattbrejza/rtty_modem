function [ f1,f2 ] = findrtty( in )
%FINDRTTY Summary of this function goes here
%   sample rate should be in the order of 10ksps

ins = size(in);
if (ins(1) > ins(2))
    in=transpose(in);
end

if (length(in)>2048)
    fft_all=abs(fft(in(1:2048)));
else
    fft_all=abs(fft(in));
end
fft_all = 20.*log10(fft_all);
len = int32(length(fft_all)/2);
max_fft_all = max(fft_all(1:len));
win=int32(len/20);

thres = 5; %dB
last_max = fft_all(1);
last_min = fft_all(1);

peak_i=1;
peaks(1,2) = -50;
peaks(1,1) = 1;


for i=2:len-1
    
    if (fft_all(i-1) < fft_all(i)) && (fft_all(i) > fft_all(i+1))   %if point is higher than its neighbours
        if i < peaks(peak_i,1)+win                                  %if already a peak in this window
            if peaks(peak_i,2) < fft_all(i)
                peaks(peak_i,1)=i;
                peaks(peak_i,2)=fft_all(i);
            end
        else
           peaks(peak_i+1,1)=i;
           peaks(peak_i+1,2)=fft_all(i);
           peak_i=peak_i+1;
        end
    end
    
end

%hold off
subplot(2,1,1);
scatter(1:len,fft_all(1:len),'.')
hold on
plot(peaks(1:peak_i,1),peaks(1:peak_i,2),'x','color','red')
hold off

freqs = peaks(:,1);
freqs = freqs ./ length(fft_all);

Hd = dfilt.dffir(fir1(10, 150/(10000/2), 'low', kaiser(10+1, 0.5), 'scale'));
fcoe=[-0.00903981521632955 -0.0176057278961508 -0.0214888217308206 -0.00894080820836387 0.0281449413985572 0.0880962336584224 0.156738932301488 0.211778824066603 0.232865400131940 0.211778824066603 0.156738932301488 0.0880962336584224 0.0281449413985572 -0.00894080820836387 -0.0214888217308206 -0.0176057278961508 -0.00903981521632955];

bb=zeros([peak_i length(fft_all) ]);

t=1:length(fft_all);

maxs=1:peak_i;
mins=1:peak_i;
avs=1:peak_i;
maxsl=1:peak_i;
minsl=1:peak_i;
valid = zeros([1 peak_i]);

for i=1:peak_i
    s = sin(2.*pi.*t.*freqs(i));
    c = cos(2.*pi.*t.*freqs(i));
    ss = filter(Hd,s.*in(1: length(fft_all)));  %(Hd,s.*in(1: length(fft_all)));  
    cc = filter(Hd,c.*in(1: length(fft_all)));  %(Hd,s.*in(1: length(fft_all)));  
    an = (ss).^2  +  (cc).^2;
    bb(i,:) =  an;
    
    [mins(i), minsl(i)] = min(an(30:end));
    [maxs(i), maxsl(i)] = max(an(30:end));
    avs(i) = mean(an(30:end));
    
    upthre(i) = (maxs(i)-avs(i))*0.3 + avs(i);
    lothre(i) = avs(i) - (maxs(i)-avs(i))*0.3;
    
    if mins(i) < 0.3*maxs(i)
        valid(i) = 1;     
    end
    
    bbh(i,:) = bb(i,:) > (maxs(i)/4);  
    bbl(i,:) = bb(i,:) < (maxs(i)*3/4);  
    
    
    
 %  bb(i,:)= filter(Hd,bb(i,:));
end


for i=1:peak_i-1
    for j=i+1:peak_i
        
    
        %if (maxs(i) > maxs(j)*.5) && (maxs(i) < 1.5*maxs(j))
        if (peaks(i,2) > peaks(j,2)*.5) && (peaks(i,2) < 1.5*peaks(j,2))
        if (maxs(i) > mins(j)) && (maxs(j) > mins(i)) && (avs(i) > mins(j)) && (avs(j) > mins(i))
          
            
            transh=0;
            transl=0;
            last_state1 = bb(i,40) > bb(j,40);
            last_state = bb(i,40) > bb(j,40);
            for k=50:10:length(bb(1,:))-10
                current_state = bb(i,k) > bb(j,k);
                if last_state1 ~= current_state
                    if (current_state == (bb(i,k-5) > bb(j,k-5))) && (current_state == (bb(i,k+5) > bb(j,k+5)))
                       if current_state  %just a check to see which signal is higher
                           if bb(i,k) > upthre(i)   &&  bb(j,k) < lothre(j)
                               transh=transh+1;
                           end
                       else
                           if bb(j,k) > upthre(j)   &&  bb(i,k) < lothre(i)
                               transl=transl+1;
                           end
                       end  
                    end
                end           
                last_state1 = last_state;
                last_state = current_state;
            end
            
            [transh  transl]
            
            
            
            
            
            hf=0;
            lf=0;
                       
% % %             for k=100:10:length(fft_all)
% % %                 if (bbh(i,k) + bbh(j,k)) < 1
% % %                    hf = hf + 1; 
% % %                 end
% % %                 
% % %                 if (bbl(i,k) + bbl(j,k)) > 1
% % %                    lf = lf + 1; 
% % %                 end      
% % % 
% % %             end
% % %             
% % %             if (hf < 2)% && lf < 2)
                f1 = freqs(i);
                f2 = freqs(j);
                
                %%%%%%%
                subplot(2,1,2);
                plot(1:length(bb(1,:)),bb(i,:),'color','green');
                hold on
                plot(1:length(bb(1,:)),bb(j,:));
                hold off
                subplot(2,1,1);
                hold on
                line([f1 f1].*2048,[-40 40],'color','green');
                line([f2 f2].*2048,[-40 40],'color','yellow');
                hold off
                drawnow;
                return
% % %             end
        end
        end
    
       
    end
end

f1 = 0;
f2 = 0;

% 
% len_s=50;
% ratio=len_s/double(len);
% peak_v=zeros([10 peak_i]);
% 
% for i=1:10
%    fft_s = 20.*log10(abs(fft(in(100+20*i:199+20*i)))); 
%    stem(fft_s)
%    for j=1:peak_i
%        k=int32(peaks(j,1)*ratio);
%       peak_v(i,j) = mean(fft_s(k-1:k+1));
%    end
% end


end

