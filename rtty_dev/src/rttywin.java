
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
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
import java.awt.image.BufferedImage;



import org.math.plot.*;

import edu.emory.mathcs.jtransforms.fft.*;

import ukhas.Gps_coordinate;
import ukhas.Habitat_interface;
import ukhas.Listener;
//import rtty.Mappoint_interface;
import rtty.StringRxEvent;
import ukhas.Telemetry_string;
import rtty.rtty_receiver;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class rttywin extends JFrame implements StringRxEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	AudioFormat audioFormat;
	TargetDataLine targetDataLine;
	
	private String _habitat_url = "habitat.habhub.org";
	private String _habitat_db = "habitat";
	
	boolean stopCapture = false;
	ByteArrayOutputStream byteArrayOutputStream;
	
	Waterfall wf;
	
	AudioInputStream audioInputStream;
	SourceDataLine sourceDataLine;
	  
	  //rtty_decode decoder = new rtty_decode(1200,1800,7);
	rtty_receiver rcv = new rtty_receiver();
	  
	DoubleFFT_1D ft = new DoubleFFT_1D(512);
	  
	//private graph_line grtty = new graph_line();

	  
	Plot2DPanel plot = new Plot2DPanel();
	int plotint;
	  
	JLabel lb_freqs;
	  
	JTextArea txtDecode;
	JLabel lbfreq;
	JScrollPane scrollPane;
	JCheckBox ckFreeze;
	@SuppressWarnings("rawtypes")
	JComboBox cbSoundCard;
	JCheckBox ck300b;
	JLabel lbStatus;
	JLabel lbimage;
	JCheckBox chkOnline;
	private JCheckBox ckPause;
	  
	  
	Habitat_interface hi;// = new Habitat_interface("MATT");
	private JTextField txtcall = new JTextField();
	private JTextField txtLat;
	private JTextField txtLong;;
	  
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

	public void StringRx(Telemetry_string str, boolean checksum)
	{
		System.out.println(str.getSentence() + "   " + checksum);
		if (chkOnline.isSelected())
			hi.upload_payload_telem(str);
	}
	
	/**
	 * Create the frame.
	 */
	public rttywin() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 546, 628);
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
			
			
			BufferedImage grad;
			
			grad = ImageIO.read(new File("C:/grad.png"));
			wf = new Waterfall(grad,200);
				
				
			
	        
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
		
		JButton btnStartst = new JButton("GO!");
		btnStartst.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hi = new Habitat_interface(_habitat_url, _habitat_db, new Listener(txtcall.getText(), new Gps_coordinate(txtLat.getText(), txtLong.getText(),"0")));
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
		
		JButton btnNewButton = new JButton("Newfbutton");
		btnNewButton.setEnabled(true);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hi = new Habitat_interface(_habitat_url, _habitat_db, new Listener(txtcall.getText(), new Gps_coordinate(txtLat.getText(), txtLong.getText(),"0")));
				//hi.queryAllPayloadDocs("APEX");
				hi.updateChaseCar(new Listener(txtcall.getText(), new Gps_coordinate(txtLat.getText(), txtLong.getText(),"0")));
				hi.addDataFetchTask("NANU",(System.currentTimeMillis() / 1000L)-(15*24*60*60),(System.currentTimeMillis() / 1000L),3000);
			
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
		ck300b.setBounds(216, 73, 62, 23);
		contentPane.add(ck300b);
		
		JLabel lblStatus = new JLabel("Status:");
		lblStatus.setBounds(254, 107, 40, 14);
		contentPane.add(lblStatus);
		
		lbStatus = new JLabel("New label");
		lbStatus.setBounds(307, 107, 137, 14);
		contentPane.add(lbStatus);
		
		JButton btnNewButton_1 = new JButton("New button");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Habitat_interface.test();
				
				//hi.upload_payload_telem(new Telemetry_string("$$TEST,3324,435,32fdfdf,423,423,4,5*4334\n",false));
				//Mappoint_interface mi = new Mappoint_interface();
				//mi.test();
				
				
			}
		});
		btnNewButton_1.setBounds(264, 137, 89, 23);
		contentPane.add(btnNewButton_1);
		txtcall.setText("MATT");
		
		
		txtcall.setBounds(350, 74, 137, 20);
		contentPane.add(txtcall);
		txtcall.setColumns(10);
		
		JLabel lblCallsign = new JLabel("callsign:");
		lblCallsign.setBounds(284, 77, 46, 14);
		contentPane.add(lblCallsign);
		
		txtLat = new JTextField();
		txtLat.setText("52.1");
		txtLat.setHorizontalAlignment(SwingConstants.LEFT);
		txtLat.setBounds(401, 104, 86, 20);
		contentPane.add(txtLat);
		txtLat.setColumns(10);
		
		txtLong = new JTextField();
		txtLong.setText("-0.3");
		txtLong.setBounds(401, 132, 86, 20);
		contentPane.add(txtLong);
		txtLong.setColumns(10);
		
	
		
		BufferedImage myPicture;
		try {
			myPicture = ImageIO.read(new File("C:/grad.png"));
			//myPicture.setRGB(3, 100, 0xFFFFFF);
			lbimage = new JLabel(new ImageIcon( myPicture ));
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		lbimage.setBounds(10, 388, 510, 190);
		contentPane.add(lbimage);
		
		chkOnline = new JCheckBox("Online");
		chkOnline.setBounds(133, 54, 97, 23);
		contentPane.add(chkOnline);
		
         

		
		
			
			
			
		
		
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
		  byte tempBuffer[] = new byte[4000];
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
		          
		          if (rcv.get_fft_updated())
		          {
			          //plotting graph bit
			         // grtty.clearMarkers();
			         // grtty.drawfft(rcv.get_fft());
			          
			         // if (rcv.get_peaklocs() != null)
			         // {
				     //     for (int z = 0; z < rcv.get_peaklocs().length; z++)
		  			//		grtty.addMarkers(rcv.get_peaklocs()[z]);
				     // }
			          
			         // grtty.addMarkers(rcv.get_f1()*(2*rcv.FFT_half_len),rcv.get_f2()*(2*rcv.FFT_half_len));
			        	  
			          
			          scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());//getHeight());
			          lbStatus.setText(rcv.current_state.toString());
		          }
		          if (rcv.get_fft_updated())
		        	  lbimage.setIcon(new ImageIcon(wf.UpdateLine(rcv.get_fft())));
	
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