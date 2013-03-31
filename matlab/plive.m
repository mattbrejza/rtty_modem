

rcs=dsp.AudioRecorder('NumChannels',1,'SampleRate',11025,'SamplesPerFrame',10000);
%rcs=dsp.AudioFileReader('test.wma','SamplesPerFrame',12000);

log = dsp.SignalSink('BufferLength',7);

audio = step(rcs);
audio = single(audio(:,1));
[f1,f2] = findrtty(audio);
dem=fskdemodulate(300,f1,f2,7);

for i=1:200
    
    
    %step(log, audio);
    %  a = log.Buffer;
    %spectrogram(double(a),256,128,256,10000);
    %ars = resample(double(audio),1,4)/10000;
    %[f1,f2] = findrtty(ars);
    if (f1 > 0) && (f2 > 0) 
        if sum(ch)<100
            [f1,f2] = findrtty(audio);
            dem.F1 = f1;
            dem.F2 = f2;
        end
         ch = dem.getchars(dem.sync(dem.demod(double(audio))))
    else
        [f1,f2] = findrtty(audio);
        dem.F1 = f1;
        dem.F2 = f2;
    end
    drawnow;
    %pause(1);
    
    audio = step(rcs);
    audio = single(audio(:,1));
   
    
end

   

release(rcs);