package com.algorithmsii.week2;

import java.awt.Color;

import edu.princeton.cs.algs4.Picture;

public class SeamCarver {
	private Picture pic;
	private double[][] cachedEnergy;
	private boolean transposed = false;
	private Color[][] colors;

	// create a seam carver object based on the given picture
   public SeamCarver(Picture picture) {
	   if (null == picture) {
		   throw new java.lang.IllegalArgumentException();
	   }

	   // defensive copy
	   this.pic = new Picture(picture);
	   this.colors = new Color[pic.width()][pic.height()];

	   this.cachedEnergy = new double[pic.width()][pic.height()];
	   calculateEnergy();
   }

	private void calculateEnergy() {
	   for (int i=0; i<pic.width(); i++) {
		   for (int j=0;j <pic.height(); j++) {
			   colors[i][j] = pic.get(i, j);
			   cachedEnergy[i][j] = energy(i,j);
		   }
	   }
	}

   // current picture
   public Picture picture() {
	   return this.pic;
   }

   // width of current picture
   public int width() {
	   return this.pic.width();
   }

   // height of current picture
   public int height() {
	   return this.pic.height();
   }

   // energy of pixel at column x and row y
   public double energy(int x, int y) {
	   if (x==0 || x==pic.width()-1 || y==0 || y==pic.height()-1) {
		   return 1000;
	   }

	   double energyXSquared = calcEnergy(x-1, y, x+1, y);
	   double energyYSquared = calcEnergy(x, y-1, x, y+1);
	   return Math.sqrt(energyXSquared+energyYSquared);
   }

   private double calcEnergy(int x1, int y1, int x2, int y2) {
	   double deltaR = pic.get(x1, y1).getRed() - pic.get(x2, y2).getRed();
	   double deltaG = pic.get(x1, y1).getGreen() - pic.get(x2, y2).getGreen();
	   double deltaB = pic.get(x1, y1).getBlue() - pic.get(x2, y2).getBlue();

	   double deltaSquare = deltaR*deltaR + deltaG*deltaG + deltaB*deltaB;
	   return deltaSquare;
   }

   // sequence of indices for horizontal seam
   public int[] findHorizontalSeam() {

	   if (!transposed) {
		   transpose();
	   }
	   int[] shortestPath = findVerticalSeam();

	   return shortestPath;
   }


	// sequence of indices for vertical seam
	public int[] findVerticalSeam() {

		if (transposed) {
		   transpose();
		}

		int[] shortestPath = new int[pic.height()];
		findShortestPath(shortestPath);
		return shortestPath;
	}

	private void transpose() {
		// TODO Auto-generated method stub

	}

	private void findShortestPath(int[] shortestPath) {
	   int width = pic.width();
	   int height = pic.height();
	   int length = width*height;
	   double[] distTo = new double[length+1];
	   for (int i=0; i<length+1; i++) {
		   distTo[i] = Double.POSITIVE_INFINITY;
	   }

	   int[] edgeTo = new int[length+1];
	   for (int c = 0; c <width; c++) {
		   dfs(c, 0, distTo, edgeTo, cachedEnergy[c][0], 0);
	   }

	   int runner = edgeTo.length-1;
	   int shortestPathIdx = shortestPath.length-1;
	   while (edgeTo[runner] != runner) {
		   shortestPath[shortestPathIdx--] = edgeTo[runner]%width;
		   runner = edgeTo[runner];
	   }
   }

   private void dfs(int c, int r, double[] distTo, int[] edgeTo, double prevEnergy, int prevC) {
	   relax(c, r, distTo, edgeTo, prevEnergy, prevC);
	   // reached the bottom row, terminate
	   if (r==pic.height()) {
		   return;
	   }

	   if (c-1>=0) {
		   dfs(c-1, r+1, distTo, edgeTo, cachedEnergy[c][r], c);
	   }
	   dfs(c, r+1, distTo, edgeTo, cachedEnergy[c][r], c);
	   if (c+1<=pic.width()-1) {
		   dfs(c+1, r+1, distTo, edgeTo, cachedEnergy[c][r], c);
	   }
   }

   private void relax(int c, int r, double[] distTo, int[] edgeTo, double prevEnergy, int prevC) {
	   int width = pic.width();
	   if (r==0) {
		   distTo[c] = prevEnergy;
		   edgeTo[c] = c;
	   } else if (r==pic.height()) {
		   // go to last vertex
		   int idx = r*width;
		   int prevIdx = prevC + (r-1)*width;
		   if (distTo[idx] > distTo[prevIdx] + prevEnergy) {
			   distTo[idx] = distTo[prevIdx] + prevEnergy;
			   edgeTo[idx] = prevIdx;
		   }
	   } else {
		   int idx = c+r*width;
		   int prevIdx = prevC + (r-1)*width;
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
	   if (seam.length != pic.width()) {
		   throw new java.lang.IllegalArgumentException();
	   }
	   if (pic.width() <= 1) {
		   throw new java.lang.IllegalArgumentException();
	   }

	   int cache=seam[0];
	   int height = pic.height();
	   for (int i =1; i<seam.length ;i++) {
		   // line is broken
		   if (seam[i]-cache > 1) {
			   throw new java.lang.IllegalArgumentException();
		   }
		   // outside range
		   if (seam[i] >= height) {
			   throw new java.lang.IllegalArgumentException();
		   }
		   cache = seam[i];

	   }
   }

   // remove vertical seam from current picture
   public void removeVerticalSeam(int[] seam) {
	   if (null == seam) {
		   throw new java.lang.IllegalArgumentException();
	   }
	   if (seam.length != pic.height()) {
		   throw new java.lang.IllegalArgumentException();
	   }
	   if (pic.height() <= 1) {
		   throw new java.lang.IllegalArgumentException();
	   }

	   int cache=seam[0];
	   int width = pic.width();
	   Color[][] temp = new Color[pic.width()-1][pic.height()];
	   for (int i =1; i<seam.length ;i++) {
		   // line is broken
		   if (seam[i]-cache > 1) {
			   throw new java.lang.IllegalArgumentException();
		   }
		   // outside range
		   if (seam[i] >= width || seam[i]<0) {
			   throw new java.lang.IllegalArgumentException();
		   }
		   cache = seam[i];


		   //System.arraycopy(colors[i], srcPos, dest, destPos, length);

	   }

   }
}