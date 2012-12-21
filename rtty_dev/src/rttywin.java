// Copyright 2012 (C) Matthew Brejza
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.


import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;



import org.math.plot.*;

import edu.emory.mathcs.jtransforms.fft.*;

import rtty.StringRxEvent;
import rtty.rtty_receiver;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

public class rttywin extends JFrame implements StringRxEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	AudioFormat audioFormat;
	TargetDataLine targetDataLine;
	
	  boolean stopCapture = false;
	  ByteArrayOutputStream byteArrayOutputStream;
	
	
	  AudioInputStream audioInputStream;
	  SourceDataLine sourceDataLine;
	  
	  //rtty_decode decoder = new rtty_decode(1200,1800,7);
	  rtty_receiver rcv = new rtty_receiver();
	  
	  DoubleFFT_1D ft = new DoubleFFT_1D(512);
	  
	  Plot2DPanel plot = new Plot2DPanel();
	  int plotint;
	  
	  JLabel lb_freqs;
	  
	  JTextArea txtDecode;
	  JLabel lbfreq;
	  JScrollPane scrollPane;
	  JCheckBox ckFreeze;
	  JComboBox cbSoundCard;
	  JCheckBox ck300b;
	  JLabel lbStatus;
	  private JCheckBox ckPause;
	  
	 // graph_line grtty = new graph_line();
	  
	  

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
					rttywin frame = new rttywin();
					frame.setVisible(true);
					
					
			        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();  
			          
			        //Output Available Mixers  
			        System.out.println("Available mixers:");  
			        for(int cnt = 0; cnt < mixerInfo.length; cnt++){  
			        	
			          System.out.println(cnt + ": " + mixerInfo[cnt].getName());  
			        }  
			        
			        
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void StringRx(String str, boolean checksum)
	{
		System.out.println(str + "   " + checksum);
	}
	
	/**
	 * Create the frame.
	 */
	public rttywin() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 546, 428);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		rcv.addStringRecievedListener(this);

		
		try 
		{
		
	        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();  
	          
	        //Output Available Mixers  
	        System.out.println("Available mixers:");  
	        String[] devices = new String[mixerInfo.length];
	        for(int cnt = 0; cnt < mixerInfo.length; cnt++){  
	        	devices[cnt] = mixerInfo[cnt].getName();
	        }  		
	        
			cbSoundCard = new JComboBox<Object>(devices);
			cbSoundCard.setBounds(269, 27, 208, 20);
			contentPane.add(cbSoundCard);
	        
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		
		
		
		JButton btnStart = new JButton("New button");
		btnStart.setEnabled(false);
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		btnStart.setBounds(50, 103, 89, 23);
		contentPane.add(btnStart);
		
		JButton btnStop = new JButton("New button");
		btnStop.setEnabled(false);
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		          targetDataLine.stop();
		          targetDataLine.close();
			}
		});
		btnStop.setBounds(149, 103, 89, 23);
		contentPane.add(btnStop);
		
		JButton btnStartst = new JButton("New button");
		btnStartst.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startDSP();
			}
		});
		btnStartst.setBounds(50, 137, 89, 23);
		contentPane.add(btnStartst);
		
		JButton btnStopst = new JButton("New button");
		btnStopst.setEnabled(false);
		btnStopst.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopCapture = true;
			}
		});
		btnStopst.setBounds(149, 137, 89, 23);
		contentPane.add(btnStopst);
		
		JButton btnNewButton = new JButton("New button");
		btnNewButton.setEnabled(false);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				  
				
			}
		});
		btnNewButton.setBounds(152, 24, 89, 23);
		contentPane.add(btnNewButton);
		
		lb_freqs = new JLabel("______");
		lb_freqs.setBounds(50, 28, 46, 14);
		contentPane.add(lb_freqs);
		
		
		lbfreq = new JLabel("New label");
		lbfreq.setBounds(60, 172, 153, 14);
		contentPane.add(lbfreq);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 197, 510, 180);
		contentPane.add(scrollPane);
		
		txtDecode = new JTextArea();
		scrollPane.setViewportView(txtDecode);
		txtDecode.setLineWrap(true);
		
		ckFreeze = new JCheckBox("Freeze");
		ckFreeze.setBounds(42, 73, 89, 23);
		contentPane.add(ckFreeze);
		
		ckPause = new JCheckBox("Pause");
		ckPause.setBounds(133, 73, 69, 23);
		contentPane.add(ckPause);
		
		ck300b = new JCheckBox("300?");
		ck300b.setSelected(true);
		ck300b.setBounds(216, 73, 137, 23);
		contentPane.add(ck300b);
		
		JLabel lblStatus = new JLabel("Status:");
		lblStatus.setBounds(254, 107, 40, 14);
		contentPane.add(lblStatus);
		
		lbStatus = new JLabel("New label");
		lbStatus.setBounds(307, 107, 137, 14);
		contentPane.add(lbStatus);
		
		
	}


	
	protected void startDSP() {
		// TODO Auto-generated method stub
		try{
		      //Get and display a list of
		      // available mixers.
		      Mixer.Info[] mixerInfo = 
		                      AudioSystem.getMixerInfo();
		      System.out.println("Available mixers:");
		      for(int cnt = 0; cnt < mixerInfo.length;
		                                          cnt++){
		      	System.out.println(mixerInfo[cnt].
		      	                              getName());
		      }//end for loop

		      //Get everything set up for capture
		      audioFormat = getAudioFormat();

		      DataLine.Info dataLineInfo =
		                            new DataLine.Info(
		                            TargetDataLine.class,
		                            audioFormat);

		      //Select one of the available
		      // mixers.
		      Mixer mixer = AudioSystem.
		                          getMixer(mixerInfo[cbSoundCard.getSelectedIndex()]);
		      
		      //Get a TargetDataLine on the selected
		      // mixer.
		      targetDataLine = (TargetDataLine)
		                     mixer.getLine(dataLineInfo);
		      //Prepare the line for use.
		      targetDataLine.open(audioFormat);
		      targetDataLine.start();

		      //Create a thread to capture the microphone
		      // data and start it running.  It will run
		      // until the Stop button is clicked.
		      Thread captureThread = new CaptureThread();
		      captureThread.start();
		    } catch (Exception e) {
		      System.out.println(e);
		      System.exit(0);
		    }//end catch    
	}

	private double[] bytes2double(byte[] in)
	{
		
    double[] out = new double[in.length / 2]; // will drop last byte if odd number
    ByteBuffer bb = ByteBuffer.wrap(in);
    for (int i = 0; i < out.length; i++) {
        out[i] = (double)bb.getShort();
    }
    return out;
		
		
		/*
		double[] out = new double[(int)in.length/2];

		for (int i=0; i < in.length; i=i+2)
		{
			out[i/2] = in[i] * 2^8 + in[i];
		}
		
		return out;*/
	}
	
	 private AudioFormat getAudioFormat(){
		
		    float sampleRate = 8000.0F;
		    //8000,11025,16000,22050,44100
		    int sampleSizeInBits = 16;
		    //8,16
		    int channels = 1;
		    //1,2
		    boolean signed = true;
		    //true,false
		    boolean bigEndian = true;
		    //true,false
		    return new AudioFormat(sampleRate,
		                           sampleSizeInBits,
		                           channels,
		                           signed,
		                           bigEndian);
		  }//end getAudioFormat
	
	
	//Inner class to capture data from microphone
	// and write it to an output audio file.
	 class CaptureThread extends Thread{
		  //An arbitrary-size temporary holding buffer
		  byte tempBuffer[] = new byte[20000];
		  public void run(){
		    byteArrayOutputStream = new ByteArrayOutputStream();
		    stopCapture = false;
		    try{//Loop until stopCapture is set by
		        // another thread that services the Stop
		        // button.
		      while(!stopCapture){
		        //Read data from the internal buffer of
		        // the data line.
		        int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
		        if(cnt > 0){
		          //Save data in output stream object.
		          byteArrayOutputStream.write(tempBuffer, 0, cnt);
		         // plot.removePlot(plotint);
		          //plotint = plot.addLinePlot("my plot", bytes2double(tempBuffer));
		          double a[] = bytes2double(tempBuffer);
		          /*System.out.println(*/ //rcv.processBlock(a); //);
		          //txtDecode.setText("MOO");
		          if (!ckPause.isSelected())
		          {
		        	  if (ck300b.isSelected())
		        		  txtDecode.append(rcv.processBlock(a,300));
		        	  else
		        		  txtDecode.append(rcv.processBlock(a,50));
		        	 
		          }
		        	  
		          
		          //if (ckFreeze.isSelected())
		          //{
		        //	  rcv.followRTTY(a);
		         // }
		         // else
		         // {
		        	//  double[] fqs = rcv.findRTTY(a,!ckFreeze.isSelected());
		        	//  if (fqs[1] > 0)
			        //  {
			        	 // lb_freqs = new JLabel("f1: " + Double.toString(fqs[0]) + "  f2:  " + Double.toString(fqs[1]));
			        	 // System.out.println("f1: " + Double.toString(fqs[0]) + "  f2:  " + Double.toString(fqs[1]));
			        //	  lbfreq.setText("f1: " + Double.toString(fqs[0]) + "  f2:  " + Double.toString(fqs[1]));
			        	  //lb_freqs.setBounds(50, 28, 46, 14);
			    		//contentPane.add(lb_freqs);
			         // }
		         // }
		          scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());//getHeight());
		          lbStatus.setText(rcv.current_state.toString());
		         
	
		          //System.arraycopy(a, 0, b, 0, 512);
		         /* ft.realForward(a);
		          double c[] = new double[256];
		          for (int i = 0; i < 256; i++)
		          {
		        	  c[i] = Math.pow(a[i*2], 2) + Math.pow(a[i*2 +1], 2);
		          }*/
		         // plotint = plot.addLinePlot("my plot", c);
		        }//end if
		      }//end while
		      byteArrayOutputStream.close();
		    }catch (Exception e) {
		      System.out.println(e);
		      System.exit(0);
		    }//end catch
		  }//end run
		}//end inner class CaptureThread
}
