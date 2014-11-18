import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;

import javax.imageio.*;
import javax.swing.*;

public class Histogram {
	
	private BufferedImage changed_image;
	
	
	public Histogram(String dir, BufferedImage image){		
		int width,height,count=0,rgb = 0,grey;
		float array[] = new float[1];
		width = image.getWidth();
		height = image.getHeight();
		
		this.changed_image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		int[][] histogram = new int[width][height];
		
		if(dir.equals("vert")){
			System.out.println("==========Vertical==========");
			array = new float[height];
			
			for(int y=0;y<height-1;y++){
				count=0;
				for(int x=0;x<width-1;x++){
					Color c = new Color(image.getRGB(x, y));
					/*if ( 	c.getRed() >= 75 && c.getRed() <= 85 &&
							c.getGreen() >= 80 && c.getGreen() <= 100 &&
							c.getBlue() >= 90 && c.getBlue() <= 110 ) {	
							
								count++;
							
					}*/
					if ( c.getRed() > 250 ) {
						count++;
					}
				}
				float percent = ( (float)count/(float)width ) * 100;
				array[y] = percent;
				//System.out.println(percent);
				for(int x=0;x<width-1;x++){
					if(x<=count){
						histogram[x][y] = 0;
					}else{
						histogram[x][y] = 255;
					}
				}
			}
		}else if(dir.equals("horz")){
			System.out.println("==========Horizontal==========");
			array = new float[width];
			
			for(int x=0;x<width;x++){
				count=0;
				for(int y=0;y<height;y++){
					/*if(Math.abs(changed_image.getRGB(x, y)>>16) > 250){
						count ++;
					}*/
					Color c = new Color(image.getRGB(x, y));
					/*if ( 	c.getRed() >= 75 && c.getRed() <= 85 &&
							c.getGreen() >= 80 && c.getGreen() <= 100 &&
							c.getBlue() >= 90 && c.getBlue() <= 110 ) {	
							
								count++;
							
					}*/
					if ( c.getRed() > 250 ) {
						count++;
					}
				}
				
				float percent = ( (float)count/(float)height ) * 100;
				//System.out.println(percent);
				array[x] = percent;
				
				for(int y=0;y<height;y++){
					if(y<=count){
						histogram[x][y] = 0;
					}else{
						histogram[x][y] = 255;
					}
				}
			}
		}	else if ( dir.equals("both") ) {
			long start = System.nanoTime();
			initFindPlaque(width, height,2,2, image);
			long end = (System.nanoTime() - start)/1000000000;
			System.out.println("Took " + end + " seconds");
			return;
		}		
		
		array = removeModeAndLess(array);
		/*for ( int i = 0; i < array.length; i++ ) {
			System.out.println(array[i]);
		}*/
		//System.out.println("Printing Histogram");
		for(int x=0;x<width;x++){
			for(int y=0;y<height;y++){
				//rgb = (histogram[x][y] << 16) + (histogram[x][y] << 8) + histogram[x][y];
				rgb = new Color(255,255,255).getRGB();
				if ( x < array.length && array[x] > 0.0f && dir.equals("horz") ) {
					//rgb = (histogram[x][y] << 16) + (histogram[x][y] << 8) + histogram[x][y];
					rgb = new Color(histogram[x][y], histogram[x][y], histogram[x][y] ).getRGB();
				} else if ( y < array.length && array[y] > 0.0f && dir.equals("vert") ) {
					//rgb = (histogram[x][y] << 16) + (histogram[x][y] << 8) + histogram[x][y];
					rgb = new Color(histogram[x][y], histogram[x][y], histogram[x][y] ).getRGB();
				}
				changed_image.setRGB(x, y, rgb);
			}
		}
		
		
		
		
		//redrawImageFrame();
		
		//int rgb = (gray << 16) + (gray << 8) + gray;
	}
	
	public float[] histVert(int width, int height) {
		//System.out.println("==========Vertical Lines==========");
		int count = 0;
		float array[] = new float[height];
		for(int y=0;y<height-1;y++){
			count=0;
			for(int x=0;x<width-1;x++){
				Color c = new Color(changed_image.getRGB(x, y));
				/*if ( 	c.getRed() >= 75 && c.getRed() <= 85 &&
							c.getGreen() >= 80 && c.getGreen() <= 100 &&
							c.getBlue() >= 90 && c.getBlue() <= 110 ) {	
							
								count++;
							
					}*/
					if ( c.getRed() > 250 ) {
						count++;
					}
			}
			//count used here
			float percent = ( (float)count/(float)width ) * 100;
			//System.out.println(percent);
			array[y] = percent;
		}
		
		return removeModeAndLess(array);
	}
	/**
		Counting columns from left to right
	*/
	public float[] histHoriz(int width, int height) {
		//System.out.println("==========Horizontal Lines==========");
		float array[] = new float[width];
		int count = 0;
		for(int x=0;x<width-1;x++){
			count=0;
			for(int y=0;y<height-1;y++){
				Color c = new Color(changed_image.getRGB(x, y));
				/*if ( 	c.getRed() >= 75 && c.getRed() <= 85 &&
							c.getGreen() >= 80 && c.getGreen() <= 100 &&
							c.getBlue() >= 90 && c.getBlue() <= 110 ) {	
							
								count++;
							
					}*/
					if ( c.getRed() > 250 ) {
						count++;
					}
			}
			//count used here
			float percent = ( (float)count/(float)height ) * 100;
			//System.out.println(percent);
			array[x] = percent;
		}
		
		return removeModeAndLess(array);
	}
	/**
		Finds and sets all values to 0 that are equal to or less than the
		mode of the given array;
		
		@param old	float[] - Contains the array of values to check
		
		@return A new float[] of removed values
	*/
	private float[] removeModeAndLess(float old[]) {
		ArrayList<Float> values = new ArrayList<>();
		ArrayList<Integer> counts = new ArrayList<>();
		values.add(old[0]);
		counts.add(1);
		int old_length = old.length;
		//finds the count of percents
		for ( int i = 1; i < old_length; i++ ) {
			float temp_value = old[i];
			for ( int j = 0; j < values.size(); j++ ) {
				if ( values.get(j) == temp_value ) {
					counts.set(j, counts.get(j) + 1 );
					break;
				}
			}
			if ( values.contains(temp_value) == false ) {
				values.add(temp_value);
				counts.add(1);
			}
		}
		
		//For printing
		/*
		for ( int i = 0; i < values.size(); i++ ) {
			System.out.println(values.get(i) + " occur " + counts.get(i) );
		}
		*/
		
		//get mode
		int mode_index = 0;
		int mode_greatest = counts.get(0);
		for ( int i = 1; i < values.size(); i++ ) {
			if ( counts.get(i) > mode_greatest ) {
				mode_greatest = counts.get(i);
				mode_index = i;
			}
		}
		
		//remove all mode and less than
		float mode = values.get(mode_index);
		float new_array[] = old;
		for ( int i = 0; i < old.length; i++ ) {
			if ( old[i] <= mode ) {
				new_array[i] = 0.0f;
			}
		}
		return new_array;
	}
	
	/**
		Finds the plaque from horizontal and vertical histogram percent data

		@param	horz_percent	The percentage of black pixels in each column
		@param	vert_percent	The percentage of black pixels in each row
	*/
	public void findPlaque(float horz_percent[], float vert_percent[], int width, int height, BufferedImage image) {
		ArrayList<int[]> horz_lines = new ArrayList<>();		//the inner itn arrays are len 2, {pos, length}
		ArrayList<int[]> vert_lines = new ArrayList<>();
		//find for horizontal
		int start = 0;
		int len = 0;
		for ( int i = 0; i < width-1; i++ ) {
			if ( horz_percent[i] > 0.0 ) {// || horz_percent[i+1] > 0 ) {
				len++;
			} else if ( horz_percent[i] == 0.0 && len > 50 ) {
				int container[] = new int[2];
				container[0] = start;
				container[1] = len;
				horz_lines.add(container);
				start = i;
				len = 1;
			} else {
				start = i;
				len = 1;
			}
		}
		//find for vertical
		start = 0;
		len = 0;
		for ( int i = 0; i < height-1; i++ ) {
			if ( vert_percent[i] > 0.0 ||  vert_percent[i+1] > 0.0) {
				len++;
			} else if ( vert_percent[i] == 0.0 && len > 50 ) {
				int container[] = new int[2];
				container[0] = start;
				container[1] = len;
				vert_lines.add(container);
				start = i;
				len = 1;
			} else {
				start = i;
				len = 1;
			}
		}
		
		//TESTING
		
		System.out.println("For Horizontal Line(s)");
		for ( int i = 0; i < horz_lines.size(); i++ ) {
			int container[] = horz_lines.get(i);
			System.out.println("pos= " + container[0] + " length= " + container[1] );
		}
		System.out.println("For Vertical Line(s)");
		for ( int i = 0; i < vert_lines.size(); i++ ) {
			int container[] = vert_lines.get(i);
			System.out.println("pos= " + container[0] + " length= " + container[1] );
		}
		
		/*if ( horz_lines.size() != 1 ) {
			if ( top_center_value > 9 ) {
				System.out.println("Horz looped 10 times, exiting");
				System.exit(-1);
			}
			System.out.println("running horz again");
			initFindPlaque(width, height,1,0);
			return;
		} else if ( vert_lines.size() != 1 ) {
			if ( left_center_value > 9 ) {
				System.out.println("Vert looped 10 times, exiting");
				System.exit(-1);
			}
			System.out.println("running vert again");
			initFindPlaque(width, height,0,1);
			return;
		}*/
		
		if ( horz_lines.size() > 0 && vert_lines.size() > 0 ) {
			int array_pos[] = compareLinesToColorDB(horz_lines, vert_lines, image);
			
			int container[] = horz_lines.get(array_pos[0]);	
			int start_x = container[0];
			int plaque_width = container[1];
			
			container = vert_lines.get(array_pos[1]);
			int start_y = container[0];
			int plaque_height = container[1];
			
			System.out.println("Pos: (" + start_x + "," + start_y + " length: (" + plaque_width + "," + plaque_height + ")");
			
			int new_rgb[] = new int[ (plaque_width) * (plaque_height)];
			int new_rgb_counter = 0;
			for ( int y = start_y; y < plaque_height+start_y; y++ ) {
				for ( int x = start_x; x < plaque_width+start_x; x++ ) {
					new_rgb[new_rgb_counter] = image.getRGB(x,y);
					new_rgb_counter++;
				}
			}
			changed_image = new BufferedImage(plaque_width, plaque_height, BufferedImage.TYPE_INT_RGB);
			changed_image.setRGB(0, 0, plaque_width, plaque_height, new_rgb, 0, plaque_width );
			//redrawImageFrame();
		} else {
			System.out.println(	"ERROR: Could not find plaque, found " + horz_lines.size() + 
								" horizontal lines and found " + vert_lines.size() + " vertical lines.");
		}
	}
	/**
		Compares the plaque lines found to the color positions found
		
		@return 	A int[] containing the array pos for horz_lines and vert_lines
	*/
	private int[] compareLinesToColorDB(ArrayList<int[]> horz_lines, ArrayList<int[]> vert_lines, BufferedImage image ) {
		FindPlaque fp = new FindPlaque(image);
		//fp.printCounts();
		int horz_count[] = new int[horz_lines.size()];
		int vert_count[] = new int[vert_lines.size()];
		
		for ( int i = 0; i < horz_lines.size(); i++ ) {
			horz_count[i] = 0;
			for ( int j = 0; j < fp.positions.size(); j++ ) {
				if ( fp.positions.get(j)[0] > horz_lines.get(i)[0] - horz_lines.get(i)[1] && fp.positions.get(j)[0] < horz_lines.get(i)[0] + horz_lines.get(i)[1] ) {
					horz_count[i]++;
				}
			}
		}
		
		for ( int i = 0; i < vert_lines.size(); i++ ) {
			vert_count[i] = 0;
			for ( int j = 0; j < fp.positions.size(); j++ ) {
				if ( fp.positions.get(j)[1] > vert_lines.get(i)[0] - vert_lines.get(i)[1] && fp.positions.get(j)[1] < vert_lines.get(i)[0] + vert_lines.get(i)[1] ) {
					vert_count[i]++;
				}
			}
		}
		
		int horz_index = 0;
		int horz_max = 0;
		for ( int i = 0; i < horz_count.length; i++ ) {
			if ( horz_max < horz_count[i] ) {
				horz_max = horz_count[i];
				horz_index = i;
			}
		}
		int vert_index = 0;
		int vert_max = 0;
		for ( int i = 0; i < vert_count.length; i++ ) {
			if ( vert_max < vert_count[i] ) {
				vert_max = vert_count[i];
				vert_index = i;
			}
		}
		//System.out.println("Max: " + horz_max + "," + vert_max);
		int array[] = {horz_index, vert_index};
		return array;
	}
	
	
	private int top_center_value = 0;
	private int left_center_value = 0;
	public void initFindPlaque(int width, int height, int incr_top, int incr_left, BufferedImage image ) {
		top_center_value+=incr_top;
		left_center_value+=incr_left;
		int top_filter[] = {	-1,-top_center_value,-1,
								 0, 0, 0,
								 1, top_center_value, 1 };
		int left_filter[] = {	-1, 0, 1,
								-left_center_value, 0, left_center_value,
								-1, 0, 1 };
		int right_filter[] = {	1, 0, -1,
								2, 0, -2,
								1, 0, -1 };
		int high_pass_filter[] = {	-1, -1, -1,
									-1, 8, -1,
									-1, -1, -1 };
		//horizontal
		changed_image = FilterImage.filterImage(image, top_filter);
		float horz_percent[] = histHoriz(width, height);
		//vertical
		changed_image = FilterImage.filterImage(image, left_filter);
		float vert_percent[] = histVert(width, height);
		findPlaque(horz_percent, vert_percent, width, height, image);
		return;
	}
	
//=================================================================================================
//	Getters

	public BufferedImage getImage() {
		return this.changed_image;
	}
	
	
}