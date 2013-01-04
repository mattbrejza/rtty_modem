import java.awt.image.BufferedImage;


public class Waterfall {

	private int[] _grad;
	private int _grad_max;
	
	private BufferedImage im1;
	private BufferedImage im2;
	private boolean _active_image = true;
	private int line1 = 399;
	private int line2 = 199;
	
	private int _imageHeight = 200;
	
	
	public Waterfall(BufferedImage gradient, int imageHeight) {
		
		_imageHeight = imageHeight;
		line1 = 2*imageHeight - 1;
		line2 = line1 - imageHeight;
		im1 = new BufferedImage(512,2*imageHeight,BufferedImage.TYPE_INT_RGB);
		im2 = new BufferedImage(512,2*imageHeight,BufferedImage.TYPE_INT_RGB);
		
		_grad = new int[gradient.getWidth()];
		for (int i = 0; i < gradient.getWidth(); i++)
		{
			_grad[i] = gradient.getRGB(i,0);
		}
		_grad_max = gradient.getWidth();	
	}
	
	public BufferedImage UpdateLine(double[] fftin)
	{
		if (fftin.length != 512)
			return null;
		
		int currentpix;
		
		for (int i = 0; i < 512; i++)
		{
			currentpix = -40 + (10 * (int)(Math.log10(fftin[i])));
			
			if (currentpix < 0)
				currentpix = 0;
			else if (currentpix >= _grad_max)
				currentpix = _grad_max -1;
			im1.setRGB(i, line1, _grad[currentpix]);
			im2.setRGB(i, line2, _grad[currentpix]);
			
		}
		
		BufferedImage output;
		
		if (_active_image)
			output = im2.getSubimage(0, line2, 512, 200);
		else
			output = im1.getSubimage(0, line1, 512, 200);
		
		
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
