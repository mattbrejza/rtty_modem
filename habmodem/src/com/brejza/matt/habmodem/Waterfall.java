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
	
	public Bitmap UpdateLine(double[] fftin, int f1, int f2)
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
		
		
		for (int i = 0; i < _imageHeight; i++)
		{
			output.setPixel(f1, i, 0xFF00FF00);
			output.setPixel(f2, i, 0xFF00FF00);
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
