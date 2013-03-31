

rcs=dsp.AudioRecorder('NumChannels',1,'SampleRate',11025,'SamplesPerFrame',10000);
%rcs=dsp.AudioFileReader('test.wma','SamplesPerFrame',12000);

log = dsp.SignalSink('BufferLength',7);

audio = step(rcs);
audio = single(audio(:,1));
[f1,f2] = findrtty(audio);


for i=1:200
    
    
    %step(log, audio);
    %  a = log.Buffer;
    %spectrogram(double(a),256,128,256,10000);
    %ars = resample(double(audio),1,4)/10000;
    %[f1,f2] = findrtty(ars);


       
         
   [f1,f2] = findrtty(audio);


    drawnow;
    %pause(1);
    
    audio = step(rcs);
    audio = single(audio(:,1));
   
    
end

   

release(rcs);