function ber = combine(ber,input,pos)

start_p = find(ber(:,1)>=input(1,1));
end_p = start_p + size(input,1)-1;

ber(start_p:end_p,pos+1) = input(:,end-2)./ input(:,2);
%ber(start_p:end_p,pos+1) = input(:,end)./ input(:,end-1);

end