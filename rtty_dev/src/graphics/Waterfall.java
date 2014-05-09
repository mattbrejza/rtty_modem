package graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;



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
	

	public Waterfall(int imageHeight) {
		
		_imageHeight = imageHeight;
		line1 = 2*imageHeight - 1;
		line2 = line1 - imageHeight;
		im1 = new BufferedImage(512,2*imageHeight,BufferedImage.TYPE_INT_RGB);
		im2 = new BufferedImage(512,2*imageHeight,BufferedImage.TYPE_INT_RGB);
		
		//_grad
		int[] g = {0x282828, 0x26292B, 0x232B2E, 0x1F2D31, 0x1C2F35, 0x17313A, 0x13343F, 0x0F3744, 0x0B3A4A, 0x073E50, 0x044256, 0x02475D, 0x004C65, 0x00526D, 0x005977, 0x006082, 0x00688C, 0x007197, 0x0079A2, 0x0081AD, 0x0088B7, 0x008FC0, 0x0095C7, 0x009ACD, 0x029CD1, 0x039FD4, 0x04A1D8, 0x05A3DC, 0x06A4E0, 0x07A6E4, 0x09A7E5, 0x0AA8E5, 0x0CA9E5, 0x0EAAE5, 0x11ABE5, 0x14ACE5, 0x17ADE5, 0x1BAEE5, 0x1FB0E5, 0x23B1E5, 0x29B2E5, 0x2FB4E5, 0x3AB7DF, 0x56BFC3, 0x79C89D, 0x9ED172, 0xC3D946, 0xE3DE20, 0xFBDE04, 0xFFDD00, 0xFFDB00, 0xFFD900, 0xFFD700, 0xFFD400, 0xFFD100, 0xFFCD00, 0xFFC900, 0xFFC500, 0xFFC100, 0xFFBC00, 0xFFB800, 0xFFB300, 0xFFAF00, 0xFFAA00, 0xFFA500, 0xFFA000, 0xFF9C00, 0xFF9700, 0xFF9300, 0xFF8F00, 0xFF8B00, 0xFF8800, 0xFF8204, 0xFF7C09, 0xFF760F, 0xFF7116, 0xFF6C1D, 0xFF6624, 0xFF612C, 0xFF5C33, 0xFF5739, 0xFF523E, 0xFF4C42, 0xFF4744, 0xFE4144, 0xFB3B41, 0xF6343D, 0xF12D37, 0xEC2630, 0xE71F28, 0xE1181F, 0xDB1217, 0xD60C0F, 0xD20709, 0xCE0303, 0xCC0000, 0xCC0000, 0xCC0000, 0xCC0000, 0xCC0000};
		_grad = g;
		
		_grad_max = g.length;	
	}
	
	public BufferedImage UpdateLine(double[] fftin, double f1, double f2, int sample_rate)
	{
		
		return UpdateLine( fftin,(int)(f1/sample_rate*512), (int)(f2/sample_rate*512));
	
	}
	
	public BufferedImage UpdateLine(double[] fftin, int f1, int f2)
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
		
		BufferedImage output = new BufferedImage(512,200,BufferedImage.TYPE_INT_RGB);

		if (_active_image)
			//copySrcIntoDstAt(im2.getSubimage(0, line2, 512, 200),output,0,0);
			copySrcIntoDstAt(im2,output,0,line2);
			//output = ;
		else
			copySrcIntoDstAt(im1,output,0,line1);
		
		if (f1 < 0)
			f1 = 0;
		if (f1 >= output.getWidth())
			f1 = output.getWidth()-1;
		if (f2 < 0)
			f2 = 0;
		if (f2 >= output.getWidth())
			f2 = output.getWidth()-1;
		
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

		
		for (int i = 0; i < _imageHeight; i++)
		{
			output.setRGB(w0, i, 0xFF000000);
			output.setRGB(w1, i, 0xFF000000);
			output.setRGB(w2, i, 0xFF000000);
			output.setRGB(w3, i, 0xFF000000);
			output.setRGB(f1, i, 0xFF22FF22);
			output.setRGB(f2, i, 0xFF22FF22);			
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
	
	private static void copySrcIntoDstAt(final BufferedImage src,
	        final BufferedImage dst, final int dx, final int dy) {
	    int[] srcbuf = ((DataBufferInt) src.getRaster().getDataBuffer()).getData();
	    int[] dstbuf = ((DataBufferInt) dst.getRaster().getDataBuffer()).getData();
	    int width = src.getWidth();
	    int height = dst.getHeight();
	    int dstoffs = 0;
	    int srcoffs = dx + dy * dst.getWidth();
	    //System.arraycopy(srcbuf, 0 , dstbuf, 0, srcbuf.length);
	    for (int y = 0 ; y < height ; y++ , dstoffs+= dst.getWidth(), srcoffs += width ) {
	        System.arraycopy(srcbuf, srcoffs , dstbuf, dstoffs, width);
	    }
	}

}
