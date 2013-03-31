function [ out ] = el_test( in )
%UNTITLED3 Summary of this function goes here
%   Detailed explanation goes here
clf;
ea = zeros([7 length(in)]);
la = zeros([7 length(in)]);
er = zeros([7 length(in)]);
ear = 0;
hold off
for j=1:7
subplot(7,1,j);
hold on
for i=1:length(in)
   
    switch mod(i+j,7)
        case 0

       
        case 1
            ea(j,i) = in(i).^2;
            
            er(j,i) = in(i).^2 - ear.^2;
            
            if (i+j < 100) 
                line([i i],[-.1 .1],'color','yellow');
            end
            
        case 3
            if (i+j < 100)
                line([i i],[-.07 .07],'color','red');
            end
            if (sign(in(i)) == sign(ear)) && i-2>0
                er(j,i-2) = 0;
            end
        
        case 5
            la(j,i) = in(i).^2;
            ear = in(i);
            if (i+j < 100)
                line([i i],[-.1 .1],'color','yellow');
            end
            
        case 6

        
    end
    
end

%plot(ea(j,1:100));
%hold on
%plot(la(j,1:100),'color','red');
%hold on
plot(in(1:100).^2);
hold on
plot(er(j,1:100),'color','green');
hold off
end






end

