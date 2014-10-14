import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;

public class FilterImage {
	
	JFrame imageFrame = new JFrame("Images");
    JFrame editorFrame = new JFrame("Editor");
    BufferedImage changed_image = null;
    BufferedImage image = null;
    
    JTextField matrix_size_text_fieled = null;
    JPanel matrix_panel = new JPanel();
    JTextField resize_text_field = null;
    JPanel image_panel = new JPanel();
    
    
  public static void main(String[] args) throws Exception
  {
    new FilterImage("image.jpg");
  }

  public FilterImage(final String filename) throws Exception
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        
        editorFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        imageFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        imageFrameLayout(filename);
        editorFrameLayout();
        
        editorFrame.pack();
        editorFrame.setLocationRelativeTo(imageFrame);
        editorFrame.setVisible(true);

        imageFrame.pack();
        imageFrame.setLocationRelativeTo(null);
        imageFrame.setVisible(true);
      }
    });
  }
  

  /**
   * editorFrameLayout	-	Creates a layout for the editor frame
   */
  private void editorFrameLayout() {
	  JLabel matrix_size_label = new JLabel("Matrix Size:");
	  matrix_size_text_fieled = new JTextField();
	  JButton matrix_size_apply = new JButton("Apply");
	  matrix_size_apply.addActionListener(new MatrixSizeListener());
	  
	  JPanel matrix_size_panel = new JPanel(new GridLayout(1, 3));
	  matrix_size_panel.add(matrix_size_label);
	  matrix_size_panel.add(matrix_size_text_fieled);
	  matrix_size_panel.add(matrix_size_apply);
	  
	  JPanel resize_outer_panel = new JPanel(new BorderLayout());
	  JPanel resize_inner_panel = new JPanel(new GridLayout(1, 2));
	  
	  resize_text_field = new JTextField();
	  JButton resize_button = new JButton("Resize Picture");
	  resize_button.addActionListener(new ResizePicture());
	  
	  resize_inner_panel.add(new JLabel("Resize value:") );
	  resize_inner_panel.add( resize_text_field );
	  
	  resize_outer_panel.add(resize_inner_panel, BorderLayout.NORTH);
	  resize_outer_panel.add(resize_button, BorderLayout.SOUTH);
	  
	  editorFrame.getContentPane().add(matrix_size_panel, BorderLayout.NORTH);
	  editorFrame.getContentPane().add(matrix_panel, BorderLayout.CENTER);
	  editorFrame.getContentPane().add(resize_outer_panel, BorderLayout.SOUTH);
  }
  /**
   * imageFrameLayout 	-	creates a layout for the frame with the images
   * @param filename	-	the image filename
   */
  private void imageFrameLayout(String filename) {
	  
		
      try {
        image = ImageIO.read(new File(filename));
        changed_image = convertToGrayScale(image); 
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(1);
      }
      ImageIcon imageIcon = new ImageIcon(image);
      JLabel jLabel = new JLabel();
      jLabel.setIcon(imageIcon);
	  ImageIcon changed_icon = new ImageIcon(changed_image);
	  JLabel j_label = new JLabel();
      j_label.setIcon(changed_icon);
      
      image_panel = new JPanel( new BorderLayout() );
      image_panel.add(jLabel, BorderLayout.NORTH);
      image_panel.add(j_label, BorderLayout.SOUTH);     
      
      imageFrame.add(image_panel);
  }
  
  /**
   * filterImage	-	applies a filter to the old image
   * @param first	-	the old image
   * @param filter	-	the filter to be applied, can be low or high pass
   * @return	The new image with a filter applied to it
   */
  private BufferedImage filterImage( BufferedImage first, int[] filter ) {
	  BufferedImage old = deepCopy(first);
	  int[] old_rgb = old.getRGB(0,0, old.getWidth(), old.getHeight(), null, 0, old.getWidth());
	  int[] new_rgb = old.getRGB(0,0, old.getWidth(), old.getHeight(), null, 0, old.getWidth());
	  
	  for ( int i = 0; i < new_rgb.length; i++ ) {
		  new_rgb[i] = (int)filterImageAux(old_rgb, i, filter, old.getWidth(), old.getHeight());
	  }
	  
	  old.setRGB(0,0,old.getWidth(), old.getHeight(),new_rgb, 0, old.getWidth());
	  return old;
  }
  /**
   * filterImageAux - 	given the starting pixel it will apply a filter
   * 
   * @param rgb		-	the color values	
   * @param center	-	the center pixel
   * @param filter	-	the filter to apply
   * @param img_width	-	the old images width
   * @param img_height	-	the old images height
   * @return	An averaged value for the pixel
   */
  private double filterImageAux(int[] rgb, int center, int[] filter, int img_width, int img_height) {;
  	  int sum = 0;  
	  int x_filter = 0;
	  int y_filter = 0;
	  			// move up															move left
	  int start = center - ( ( (int)Math.sqrt(filter.length) -1 ) / 2 )*img_width - ( (int)Math.sqrt(filter.length) -1 ) / 2;
	  //check if with in the bounds of the image
	  if ( start < 0 ) {
		  return rgb[center];
	  } else if ( Math.sqrt(filter.length) + start + Math.sqrt(filter.length)*img_width > img_width*img_height  ) {
		  return rgb[center];
	  }
	  
	  
	  for ( int x = start; x < Math.sqrt(filter.length) + start; x++ ) {
		  y_filter = 0;
		  for ( int y = 0; y < Math.sqrt(filter.length)*img_width; y+= img_width ) {
			  Color c = new Color(rgb[x+y]);
			  sum += c.getRed() * filter[x_filter+y_filter];		  
			  y_filter+=Math.sqrt(filter.length);
		  }
		  x_filter++;
	  }
	 
	  //checks for different possible sums
	  int temp = sum;
	  if ( arraySum(filter) != 0 ) { 
		temp = (int)((double)sum / (double)arraySum(filter)); 
	  }
	  if ( temp < 0 ) {
		  temp = 0;
	  }
	  
	  if ( temp > 255 ) {
		  temp = 255;
	  }
	  
	  return new Color(temp,temp,temp).getRGB();
  }
  /**
   * arraySum - sums a given array
   * @param array	The array to sum
   * @return	An int of the summed array
   */
  private int arraySum(int[] array ) {
	  int sum = 0;
	  for ( int i = 0; i < array.length; i++) {
		  sum += array[i];
	  }
	  return sum;
  }
  /**
   * convertToGrayScale - converts the image to grayscale
   * @param first	The old image
   * @return	A new image with grayscale color
   */
  private BufferedImage convertToGrayScale(BufferedImage first ) {
	  BufferedImage old = deepCopy(first);
	  int[] new_rgb = old.getRGB(0,0, old.getWidth(), old.getHeight(), null, 0, old.getWidth());
	  
	  for ( int i = 0; i < new_rgb.length; i++ ) {
		  Color c = new Color(new_rgb[i]);
		  int red = (int)(c.getRed() * 0.299);
		  int green =(int)(c.getGreen() * 0.587);
		  int blue = (int)(c.getBlue() *0.114);
		  c = new Color(red+green+blue, red+green+blue,red+green+blue);
		  new_rgb[i] = c.getRGB();
	  }
	  
	  old.setRGB(0,0,old.getWidth(), old.getHeight(),new_rgb, 0, old.getWidth());
	  return old;
  }
  	/**
  	 * deepCopy - takes an image and does a deep copy
  	 *  
  	 * @param bi	The image to copy
  	 * @return	The deep copied image
  	 */
	private BufferedImage deepCopy(BufferedImage bi) {
		 ColorModel cm = bi.getColorModel();
		 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		 WritableRaster raster = bi.copyData(null);
		 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	/**
	 * redrawImageFrame - when an event happens to change the new image
	 */
	public void redrawImageFrame() {
		ImageIcon imageIcon = new ImageIcon(image);
	    JLabel jLabel = new JLabel();
		jLabel.setIcon(imageIcon);
		ImageIcon changed_icon = new ImageIcon(changed_image);
		JLabel j_label = new JLabel();
	    j_label.setIcon(changed_icon);
	    
	    image_panel.removeAll();
	    
	    image_panel.add(jLabel, BorderLayout.NORTH);
	    image_panel.add(j_label, BorderLayout.SOUTH);   
		
		imageFrame.pack();
		imageFrame.revalidate();
		imageFrame.repaint();
	}
	
//=================================================================================================
//	Events
	
	/**
	 * 	MatrixSizeListener - A listener that will act when the matrix button is clicked.
	 * 		It will create a square matrix depending on what number was entered.
	 */
	public class MatrixSizeListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent ae) {
		int matrix_size = 0;
		try {
			matrix_size = Integer.parseInt( matrix_size_text_fieled.getText().trim() );
			
			matrix_panel.removeAll();
			matrix_panel.setLayout( new BorderLayout() );
			
			JPanel text_field_panel = new JPanel( new GridLayout( matrix_size, matrix_size ) );
			for ( int x = 0; x < matrix_size; x++ ) {
				for ( int y = 0; y < matrix_size; y++ ) {
					text_field_panel.add(new JTextField());
				}
			}
			
			JButton apply_pic_btn = new JButton("Apply to Picture");
			apply_pic_btn.addActionListener(new ApplyToPicture());
			
			matrix_panel.add(text_field_panel, BorderLayout.NORTH);
			matrix_panel.add(apply_pic_btn, BorderLayout.SOUTH);
			
			editorFrame.pack();
			editorFrame.revalidate();
			editorFrame.repaint();
			
		}catch(Exception e) {
			System.out.println("Error on matrix size input: " + e.getMessage() );
		}
	}
		
	}
	
	/**
	 * 	ApplyToPicture - is a listener for the apply matrix to image button.
	 * 		It will take the matrix entered and apply it to the image.
	 */
	public class ApplyToPicture implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent ae) {
			try {
				JPanel temp_panel = (JPanel)matrix_panel.getComponent(0);	//list of text fields
				
				int count = temp_panel.getComponentCount();
				int[] filter = new int[count];
				count = (int)Math.sqrt(count);
				
				for ( int x = 0; x < count; x++ ) {
					for ( int y = 0; y < count; y++ ) {
						JTextField temp_text_field = (JTextField)temp_panel.getComponent(x*count + y);
						filter[x*count + y] = Integer.parseInt(temp_text_field.getText().trim());
					}
				}
				
				
			    
				changed_image = filterImage(image, filter);
				redrawImageFrame();
				
			}catch(Exception e) {
				System.out.println("Error on matrix input: " + e.getMessage() );
			}
		}
		
	}
	

	/**
	 * 	ResizePicture - is a listener for the resize button.
	 * 		It will either shrink or grow the image depending on if
	 * 		the number entered is less than 1, it will shrink,
	 * 		if greater than 1 it will grow.
	 */
	public class ResizePicture implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent ae) {
			try {
				double resize = Double.parseDouble( resize_text_field.getText() );

				int[] old_rgb = image.getRGB(0,0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
				
				int width = (int)(image.getWidth() * resize);		
				int height = (int)(image.getHeight() * resize);		
				int width_division = image.getWidth()/width;		
				int height_division = image.getHeight()/height;		
				
				int[] new_rgb = new int[width*height];

				System.out.println("Resized: " + width + "x" + height);
				
				if ( resize > 1 ) {
					for ( int x = 0; x < image.getWidth(); x++ ) {
						for ( int y = 0; y < image.getHeight(); y++ ) {
							new_rgb = growImage(old_rgb, resize, x, y, width, height, new_rgb, image.getWidth());
						}
					}
				} else if ( resize > 0 ) {
					for ( int x = 0; x < width; x++ ) {
						for ( int y = 0; y < height; y++ ) {
							new_rgb[x+y*width] =  averageShrink(old_rgb, width_division, height_division, x, y, image.getWidth(), image.getHeight());
						}
					}
				}
				changed_image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				changed_image.setRGB(0, 0, width, height, new_rgb, 0, width );
				redrawImageFrame();
			} catch (Exception e) {
				System.out.println("Error on Resize input: " + e.getMessage() );
			}
		}
		/**
		 * 	growImage 		- 	a method to take a portion of the image and expand it
		 * 
		 * @param rgb		- 	the colour values
		 * @param resize	- 	the amount the image will be resized
		 * @param pos_x		-	start position in the x
		 * @param pos_y		-	start position in the y
		 * @param width		-	width of the new image
		 * @param height	-	height of the new image
		 * @param new_rgb	-	the new image
		 * @param image_width	-	the old image width
		 * @return
		 */
		private int[] growImage(int[] rgb, double resize, int pos_x, int pos_y, int width, int height, int[] new_rgb, int image_width ) {
			Color c = new Color(rgb[pos_x + pos_y*image_width]);
			int[] rgb_copy = new_rgb;
			for ( int x = pos_x*(int)resize; x < pos_x*(int)resize + (int)resize; x++ ) {
				for ( int y = pos_y*width*(int)resize; y < pos_y*width*(int)resize + width*(int)resize; y+= width ) {
					rgb_copy[x+y] = c.getRGB(); 
				}
			}
			
			return rgb_copy;
		}
		/**
		 * 	averageShrink 	- 	a method to shrink the image with an averaged sum
		 * 
		 * @param rgb		- 	the colour values
		 * @param resize	- 	the amount the image will be resized
		 * @param pos_x		-	start position in the x
		 * @param pos_y		-	start position in the y
		 * @param width		-	width of the new image
		 * @param height	-	height of the new image
		 * @param new_rgb	-	the new image
		 * @param image_width	-	the old image width
		 * @return
		 */
		private int averageShrink(int[] rgb, int width_div, int height_div, int pos_x, int pos_y, int image_width, int image_height) {
			int sum = 0;
			for ( int x = pos_x*width_div; x < pos_x*width_div + width_div; x++ ) {
				for ( int y = pos_y*image_width*height_div; y < pos_y*image_width*height_div + image_width*height_div; y+= image_width ) {
					Color c = new Color(rgb[x+y]);
					sum += c.getRed();
				}
			}
			
			int temp = sum/(width_div*height_div);
			Color c = new Color(temp,temp,temp);

			return c.getRGB();
		}
		
	}
  
}
