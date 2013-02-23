package com.brejza.matt.habmodem;

import android.graphics.Bitmap;

public class Waterfall {

	private int[] _grad;
	private int _grad_max;
	
	private Bitmap im1;
	private Bitmap im2;
	private boolean _active_image = true;
	private int line1 = 399;
	private int line2 = 199;
	private int _imageHeight = 200;
	
	public Waterfall(Bitmap gradient, int imageHeight) {
		
		_imageHeight = imageHeight;
		line1 = 2*imageHeight - 1;
		line2 = line1 - imageHeight;
		im1 = Bitmap.createBitmap(512, 2*imageHeight, Bitmap.Config.ARGB_8888);
		im2 = Bitmap.createBitmap(512, 2*imageHeight, Bitmap.Config.ARGB_8888);
		
		_grad = new int[gradient.getWidth()];
		for (int i = 0; i < gradient.getWidth(); i++)
		{
			_grad[i] = gradient.getPixel(i,0);
		}
		_grad_max = gradient.getWidth();	
	}
	
	public Bitmap updateLine(double[] fftin, int f1, int f2)
	{
		if (fftin.length != 512)
			return null;
		
		if (_imageHeight <= 0)
			return null;
		
		int currentpix;
		
		for (int i = 0; i < 512; i++)
		{
			currentpix = -40 + (10 * (int)(Math.log10(fftin[i])));
			
			if (currentpix < 0)
				currentpix = 0;
			else if (currentpix >= _grad_max)
				currentpix = _grad_max -1;
			im1.setPixel(i, line1, _grad[currentpix]);
			im2.setPixel(i, line2, _grad[currentpix]);
			
		}
		
		Bitmap output;
		
		if (_active_image)
			output = Bitmap.createBitmap(im2,0,line2,512,_imageHeight);		
		else
			output = Bitmap.createBitmap(im1,0,line1,512,_imageHeight);
		
		int w0,w1,w2,w3;
		w0 = f1-1;
		w1 = f1+1;
		w2 = f2-1;
		w3 = f2+1;
		if (w0 < 0)
			w0 = 0;
		if (w2 < 0)
			w2 = 0;
		if (w1 >= output.getWidth())
			w1 = output.getWidth()-1;
		if (w3 >= output.getWidth())
			w3 = output.getWidth()-1;
		if (f1 < 0)
			f1 = 0;
		if (f1 >= output.getWidth())
			f1 = output.getWidth()-1;
		if (f2 < 0)
			f2 = 0;
		if (f2 >= output.getWidth())
			f2 = output.getWidth()-1;
		
		for (int i = 0; i < _imageHeight; i++)
		{
			output.setPixel(w0, i, 0xFF000000);
			output.setPixel(w1, i, 0xFF000000);
			output.setPixel(w2, i, 0xFF000000);
			output.setPixel(w3, i, 0xFF000000);
			output.setPixel(f1, i, 0xFF22FF22);
			output.setPixel(f2, i, 0xFF22FF22);			
		}
		
		line1--;
		line2--;
		
		if (_active_image)
		{
			if (line2 < 0)
			{
				line2 = 399;
				_active_image = false;
			}
		}
		else
		{
			if (line1 < 0)
			{
				line1 = 399;
				_active_image = true;
			}
		}
		
		return output;
	}

}
