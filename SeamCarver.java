package com.algorithmsii.week2;

import java.awt.Color;

import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.Stack;

public class SeamCarver {
	private double[][] cachedEnergy;
	private boolean transposed = false;
	private boolean isColorMatrixTransposed = false;
	private Color[][] colors;

	// create a seam carver object based on the given picture
	public SeamCarver(Picture picture) {
		if (null == picture) {
			throw new java.lang.IllegalArgumentException();
		}

		// defensive copy
		this.colors = new Color[picture.height()][picture.width()];
		this.cachedEnergy = new double[picture.height()][picture.width()];
		initializeCachedEnergy(picture);
	}

	private void initializeCachedEnergy(Picture pic) {
	   for (int c=0; c<pic.width(); c++) {
		   for (int r=0; r<pic.height(); r++) {
			   colors[r][c] = pic.get(c, r);
		   }
	   }

	   for (int c=0; c<pic.width(); c++) {
		   for (int r=0; r<pic.height(); r++) {
			   cachedEnergy[r][c] = energy(c,r);
		   }
	   }
	}

	// current picture
	public Picture picture() {
		if (isColorMatrixTransposed) {
			transposeColorMatrix();
		}

		Picture pic = new Picture(width(), height());
		for (int r=0; r<colors.length; r++) {
			for (int c=0; c<colors[0].length; c++) {
				pic.set(c, r, colors[r][c]);
			}
		}

		return pic;
	}

	// width of current picture
	public int width() {
		int width = this.colors[0].length;
	   	int height = this.colors.length;
	   	if (isColorMatrixTransposed) {
	   		width = height;
	   	}
	   	return width;
	}

	// height of current picture
	public int height() {
		int width = this.colors[0].length;
		int height = this.colors.length;
		if (isColorMatrixTransposed) {
			height = width;
		}
		return height;
	}

	// energy of pixel at column x and row y
	public double energy(int x, int y) {
		int colorMatrixX = y;
		int colorMatrixY = x;
		if (isColorMatrixTransposed) {
			colorMatrixX = y;
			colorMatrixY = colors[0].length - 1 - x;
		}

		if (isInvalidRange(colorMatrixX, colorMatrixY)) {
			throw new java.lang.IllegalArgumentException();
		}

		if (colorMatrixX==0 || colorMatrixX==colors.length-1 || colorMatrixY==0 || colorMatrixY==colors[0].length-1) {
			return 1000;
		}

		double energyXSquared = calcEnergy(colorMatrixX-1, colorMatrixY, colorMatrixX+1, colorMatrixY);
		double energyYSquared = calcEnergy(colorMatrixX, colorMatrixY-1, colorMatrixX, colorMatrixY+1);
		return Math.sqrt(energyXSquared+energyYSquared);
   	}

   	private boolean isInvalidRange(int x, int y) {
		boolean flag = false;
		if (x<0 || x>=colors.length || y<0 || y>= colors[0].length) {
			flag = true;
		}
   		return flag;
   	}

   	private double calcEnergy(int x1, int y1, int x2, int y2) {
	   double deltaR = colors[x1][y1].getRed() - colors[x2][y2].getRed();
	   double deltaG = colors[x1][y1].getGreen() - colors[x2][y2].getGreen();
	   double deltaB = colors[x1][y1].getBlue() - colors[x2][y2].getBlue();

	   double deltaSquare = deltaR*deltaR + deltaG*deltaG + deltaB*deltaB;
	   return deltaSquare;
   }

   	// sequence of indices for horizontal seam
   	public int[] findHorizontalSeam() {
	   	if (!transposed) {
		   	transpose();
	   	}

	   	int[] shortestPath = new int[cachedEnergy.length];
	   	findVerticalShortestPath(shortestPath);

	   	for (int i=0; i<shortestPath.length; i++) {
		   	shortestPath[i] = cachedEnergy[0].length - shortestPath[i] -1;
	   	}
	   	return shortestPath;
   	}


	// sequence of indices for vertical seam
	public int[] findVerticalSeam() {
		if (transposed) {
		   transpose();
		}

		int[] shortestPath = new int[cachedEnergy.length];
		findVerticalShortestPath(shortestPath);
		return shortestPath;
	}

	private void transpose() {
		if (!transposed) {
			int origWidth = cachedEnergy[0].length;
			int origHeight = cachedEnergy.length;

			double[][] tempEnergy = new double[origWidth][origHeight];
			for (int i=0; i<origHeight; i++) {
				for (int j=0; j<origWidth; j++) {
					tempEnergy[j][origHeight-i-1] = cachedEnergy[i][j];
				}
			}

			this.cachedEnergy = tempEnergy;
		}
		// transpose back
		else {
			int origWidth = cachedEnergy[0].length;
			int origHeight = cachedEnergy.length;

			double[][] tempEnergy = new double[origWidth][origHeight];
			for (int i=0; i<origHeight; i++) {
				for (int j=0; j<origWidth; j++) {
					tempEnergy[origWidth-1-j][i] = cachedEnergy[i][j];
				}
			}

			this.cachedEnergy = tempEnergy;
		}

		this.transposed = !transposed;
	}

	private void findVerticalShortestPath(int[] shortestPath) {
	   int width = cachedEnergy[0].length;
	   int height = cachedEnergy.length;
	   int length = width*height;
	   double[] distTo = new double[length+1];

	   for (int i=0; i<length+1; i++) {
		   distTo[i] = Double.POSITIVE_INFINITY;
	   }

	   int[] edgeTo = new int[length+1];

	   DepthFirstOrder dfo = new DepthFirstOrder(length, width, height);
	   // topological order
	   Stack<Integer> topologicalOrder = dfo.topologicalOrder();
	   for (int idx : topologicalOrder) {
		   int r = idx/width;
		   int c = idx%width;

		   // first row need to initialize
		   if (r==0) {
			   distTo[c] = cachedEnergy[r][c];
			   edgeTo[c] = c;
		   }

		   // visit all three edges pointed from this vertex
		   for (int offset =-1; offset <= 1; offset++) {
			   if (c+offset>=0 && c+offset<width) {
				   relax(r, c, offset, cachedEnergy[r][c], distTo, edgeTo);
			   }
		   }
	   }


	   int runner = edgeTo.length-1;
	   int shortestPathIdx = shortestPath.length-1;
	   while (edgeTo[runner] != runner) {
		   shortestPath[shortestPathIdx--] = edgeTo[runner]%width;
		   runner = edgeTo[runner];
	   }
	}

	private final class DepthFirstOrder {
		private boolean[] marked;
		private Stack<Integer> reversePost;
		private final int width;
		private final int height;
		public DepthFirstOrder(int length, int width, int height) {
			this.marked = new boolean[length+1];
			this.reversePost = new Stack<>();
			this.width = width;
			this.height = height;
			int r = 0;
			for (int c=0; c<width; c++) {
				dfs(c, r);
			}
		}

		private void dfs(int c, int r) {
			int idx = c + r*width;
			marked[idx] = true;
			if (r+1 != height) {
				for (int offset =-1; offset <= 1; offset++) {
					if (c+offset>=0 && c+offset<width) {
						int newCol = c+offset;
						int newRow = r+1;
						int newIdx = newCol + newRow*width;

						if (!marked[newIdx]) {
							dfs(newCol, newRow);
						}
					}
				}
			}
			reversePost.push(idx);
		}

		public Stack<Integer> topologicalOrder() {
			return reversePost;
		}

	}


	private void relax(int r, int c, int offset, double prevEnergy, double[] distTo, int[] edgeTo) {
		int width = cachedEnergy[0].length;
		int height = cachedEnergy.length;
		int targetRow = r+1;
		int targetCol = c+offset;
		if (r==height-1) {
			// go to last vertex
		   int idx = targetRow*width;
		   int prevIdx = c + r*width;
		   if (distTo[idx] > distTo[prevIdx] + prevEnergy) {
			   distTo[idx] = distTo[prevIdx] + prevEnergy;
			   edgeTo[idx] = prevIdx;
		   }
		} else {
		   int idx = targetCol+targetRow*width;
		   int prevIdx = c + r*width;
		   if (distTo[idx] > distTo[prevIdx] + prevEnergy) {
			   distTo[idx] = distTo[prevIdx] + prevEnergy;
			   edgeTo[idx] = prevIdx;
		   }
	   }
	}

   // remove horizontal seam from current picture
   public void removeHorizontalSeam(int[] seam) {
	   if (null == seam) {
		   throw new java.lang.IllegalArgumentException();
	   }
	   int picWidth = width();
	   int picHeight = height();

	   if (seam.length != picWidth) {
		   throw new java.lang.IllegalArgumentException();
	   }
	   if (picHeight <= 1) {
		   throw new java.lang.IllegalArgumentException();
	   }
	   if (isBrokenSeam(seam, picHeight)) {
		   throw new java.lang.IllegalArgumentException();
	   }


	   if (!isColorMatrixTransposed) {
		   transposeColorMatrix();
	   }
	   if (!transposed) {
		   transpose();
	   }

	   int width = colors[0].length;
	   // transfer this coordinates
	   for (int i=0; i<seam.length; i++) {
		   seam[i] =width - seam[i]-1;
	   }

	   colors = copyAndRemove(seam);
	   cachedEnergy = recalculateCachedEnergy(seam);
   }

   	private boolean isBrokenSeam(int[] seam, int range) {
   		int cache=seam[0];
   		boolean flag = false;
   		for (int i=0; i<seam.length; i++) {
   			// line is broken
   			if (Math.abs(seam[i]-cache) > 1) {
   				flag = true;
   				break;
   			}
   			// outside range
   			if (seam[i] >= range || seam[i]<0) {
   				flag = true;
   				break;
			}
			cache = seam[i];
		}
   		return flag;
   	}

   	// remove vertical seam from current picture
   public void removeVerticalSeam(int[] seam) {
	   if (null == seam) {
		   throw new java.lang.IllegalArgumentException();
	   }

	   int picWidth = width();
	   int picHeight = height();

	   if (seam.length != picHeight) {
		   throw new java.lang.IllegalArgumentException();
	   }
	   if (picWidth <= 1) {
		   throw new java.lang.IllegalArgumentException();
	   }
	   if (isBrokenSeam(seam, picWidth)) {
		   throw new java.lang.IllegalArgumentException();
	   }

	   if (isColorMatrixTransposed) {
		   transposeColorMatrix();
	   }
	   if (transposed) {
		   transpose();
	   }

	   colors = copyAndRemove(seam);
	   cachedEnergy = recalculateCachedEnergy(seam);
   }

   	private double[][] recalculateCachedEnergy(int[] seam) {
   		double[][] temp = new double[cachedEnergy.length][cachedEnergy[0].length-1];
   		int width = temp[0].length;
   		if (temp[0].length <= 2) {
//TODO
   		}
   		for (int i=0; i<temp.length; i++) {
   			if (seam[i]==0) {
   				System.arraycopy(cachedEnergy[i], 1, temp[i], 0, width-1);
   			} else if (seam[i]==width) {
   				System.arraycopy(cachedEnergy[i], 0, temp[i], 0, width-1);
   			} else {
   				System.arraycopy(cachedEnergy[i], 0, temp[i], 0, seam[i]-1);
   				System.arraycopy(cachedEnergy[i], seam[i]+1, temp[i], seam[i], width-1-seam[i]);

   				int recalCol1= seam[i]-1;
   	   			int recalCol2= seam[i];
   	   			temp[i][recalCol1] = energy(recalCol1, i);
   	   			temp[i][recalCol2] = energy(recalCol1, i);
   			}


   		}

   		return temp;
   	}

   	private void transposeColorMatrix() {

	   if (!isColorMatrixTransposed) {
		   int origWidth = colors[0].length;
		   int origHeight = colors.length;

		   Color[][] temp = new Color[origWidth][origHeight];
		   for (int i=0; i<origHeight; i++) {
			   for (int j=0; j<origWidth; j++) {
				   temp[j][origHeight-i-1] = colors[i][j];
			   }
		   }

		   this.colors = temp;
	   } else {
		   int origWidth = colors[0].length;
		   int origHeight = colors.length;

		   Color[][] temp = new Color[origWidth][origHeight];
		   for (int i=0; i<origHeight; i++) {
			   for (int j=0; j<origWidth; j++) {
				   temp[origWidth-1-j][i] = colors[i][j];
			   }
		   }

		   this.colors = temp;
	   }

	   this.isColorMatrixTransposed = !isColorMatrixTransposed;
   }

   	private Color[][] copyAndRemove(int[] seam) {
   		int width = colors[0].length;
   		int height = colors.length;
   		Color[][] temp = new Color[height][width-1];
   		for (int i =0; i<seam.length ;i++) {
   			if (seam[i] != 0) {
   				System.arraycopy(colors[i], 0, temp[i], 0, seam[i]);
   				System.arraycopy(colors[i], seam[i]+1, temp[i], seam[i], width-seam[i]-1);
   			} else {
   				System.arraycopy(colors[i], 1, temp[i], 0, width-1);
   			}
   		}
   		return temp;
	}
}