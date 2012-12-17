
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class rtty {

	/**
	 * @param args
	 * @throws LineUnavailableException 
	 */
	public static void main(String[] args) throws LineUnavailableException {
		// TODO Auto-generated method stub
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
	    float sampleRate = 8000;
	    int sampleSizeInBits = 8;
	    int channels = 1;
	    boolean signed = true;
	    boolean bigEndian = true;
	    final AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed,bigEndian);
	    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
	    final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
	    line.open(format);
	    line.start();
	    Runnable runner = new Runnable() {
	      int bufferSize = (int) format.getSampleRate() * format.getFrameSize();

	      byte buffer[] = new byte[bufferSize];

	      public void run() {
	        try {

	          int count = line.read(buffer, 0, buffer.length);
	          if (count > 0) {
	            out.write(buffer, 0, count);
	          }

	          out.close();
	        } catch (IOException e) {
	          System.err.println("I/O problems: " + e);
	          System.exit(-1);
	        }
	      }
	    };
	    Thread captureThread = new Thread(runner);
	    captureThread.start();

	    byte audio[] = out.toByteArray();
	    InputStream input = new ByteArrayInputStream(audio);
	    final SourceDataLine line1 = (SourceDataLine) AudioSystem.getLine(info);
	    final AudioInputStream ais = new AudioInputStream(input, format, audio.length
	        / format.getFrameSize());
	    line1.open(format);
	    line1.start();

	    runner = new Runnable() {
	      int bufferSize = (int) format.getSampleRate() * format.getFrameSize();

	      byte buffer[] = new byte[bufferSize];

	      public void run() {
	        try {
	          int count;
	          while ((count = ais.read(buffer, 0, buffer.length)) != -1) {
	            if (count > 0) {
	              line1.write(buffer, 0, count);
	            }
	          }
	          line1.drain();
	          line1.close();
	        } catch (IOException e) {
	          System.err.println("I/O problems: " + e);
	          System.exit(-3);
	        }
	      }
	    };
	    Thread playThread = new Thread(runner);
	    playThread.start();

		

	}

}
