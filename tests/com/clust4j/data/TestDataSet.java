package com.clust4j.data;

import static org.junit.Assert.*;

import java.text.DecimalFormat;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.junit.Test;

import com.clust4j.algo.AbstractClusterer;
import com.clust4j.algo.AffinityPropagation;
import com.clust4j.algo.DBSCAN;
import com.clust4j.algo.HDBSCAN;
import com.clust4j.algo.HierarchicalAgglomerative;
import com.clust4j.algo.KMeans;
import com.clust4j.algo.KMedoids;
import com.clust4j.algo.MeanShift;
import com.clust4j.algo.UnsupervisedClassifier;
import com.clust4j.utils.MatUtils;
import com.clust4j.utils.VecUtils;

public class TestDataSet {
	private final static DecimalFormat df = new DecimalFormat("##.##");
	private final static DataSet IRIS = ExampleDataSets.loadIris();
	private final static DataSet WINE = ExampleDataSets.loadWine();
	private final static DataSet BC = ExampleDataSets.loadBreastCancer();

	@Test(expected=IllegalStateException.class)
	public void testIris() {
		final int len = IRIS.getDataRef().getRowDimension();
		DataSet shuffled = IRIS.shuffle();
		assertTrue(shuffled.getDataRef().getRowDimension() == len);
		
		// Test that no reference carried over...
		shuffled.getHeaderRef()[0] = "TESTING!";
		assertTrue( !IRIS.getHeaderRef()[0].equals(shuffled.getHeaderRef()[0]) );
		
		shuffled.setColumn("TESTING!", 
			VecUtils.rep(Double.POSITIVE_INFINITY, shuffled.numRows()));
		assertTrue(VecUtils.unique(shuffled.getColumn("TESTING!")).size() == 1);
		
		// Test piecewise col drops
		shuffled.dropCol("TESTING!");
		assertTrue(shuffled.numCols() == 3);
		
		shuffled.dropCol("Sepal Width");
		assertTrue(shuffled.numCols() == 2);
		
		shuffled.dropCol("Petal Length");
		assertTrue(shuffled.numCols() == 1);
		
		// Prepare for the throw...
		shuffled.dropCol("Petal Width"); // BOOM!
	}
	
	
	
	private static String formatPct(double num) {
		return df.format(num * 100) + "%";
	}
	
	private static void stdout(UnsupervisedClassifier model, boolean b, int[] labels) {
		String nm = ((AbstractClusterer)model).getName();
		System.out.println(nm+" (scale = "+b+"):  " + formatPct(model.indexAffinityScore(labels)) );
	}
	
	private static void stdout(UnsupervisedClassifier model, boolean b) {
		String nm = ((AbstractClusterer)model).getName();
		System.out.println(nm+" (scale = "+b+"):  " + model.silhouetteScore() );
	}

	private void testAlgos(boolean shuffle, DataSet ds, int k) {
		System.out.println(" ========== " + "Testing with shuffle" + 
				(shuffle ? " enabled" : " disabled") + 
				" ========== ");
		
		DataSet shuffled = shuffle ? ds.shuffle() : ds;
		int[] labels = shuffled.getLabels();
		
		final Array2DRowRealMatrix data = shuffled.getData();
		//final int[] actual = shuffled.getLabels();
		
		final boolean verbose = false;
		final boolean[] scale = new boolean[]{false, true};
		for(boolean b: scale) {
			
			// Go down the line alpha...
			AffinityPropagation ap = new AffinityPropagation(data, 
				new AffinityPropagation.AffinityPropagationPlanner()
					.setScale(b)
					.setVerbose(verbose)).fit();
			stdout(ap, b);
			
			
			DBSCAN db = new DBSCAN(data, 
				new DBSCAN.DBSCANPlanner()
					.setScale(b)
					.setVerbose(verbose)).fit();
			stdout(db, b);
			
			HDBSCAN hdb = new HDBSCAN(data, 
				new HDBSCAN.HDBSCANPlanner()
					.setScale(b)
					//.setAlgo(com.clust4j.algo.HDBSCAN.Algorithm.PRIMS_KDTREE)
					.setVerbose(verbose)).fit();
			stdout(hdb, b);
			
			HierarchicalAgglomerative ha = new HierarchicalAgglomerative(data, 
				new HierarchicalAgglomerative.HierarchicalPlanner()
					.setScale(b)
					.setVerbose(verbose)).fit();
			stdout(ha, b);
			
			
			KMeans kmn = new KMeans(data, 
				new KMeans.KMeansPlanner(k)
					.setScale(b)
					.setVerbose(verbose)).fit();
			stdout(kmn, b, labels);
			
			KMedoids kmd = new KMedoids(data, 
				new KMedoids.KMedoidsPlanner(k)
					.setScale(b)
					.setVerbose(verbose)).fit();
			stdout(kmd, b, labels);
			
			MeanShift ms = new MeanShift(data, 
				new MeanShift.MeanShiftPlanner()
					.setScale(b)
					.setVerbose(verbose)).fit();
			stdout(ms, b);
			
			
			System.out.println();
		}
		
		System.out.println();
	}
	
	@Test
	public void testDifferentAlgorithmWithShuffleIRIS() {
		testAlgos(true, IRIS, 3);
	}
	
	@Test
	public void testDifferentAlgorithmNoShuffleIRIS() {
		testAlgos(false, IRIS, 3);
	}
	
	@Test
	public void testDifferentAlgorithmWithShuffleWINE() {
		testAlgos(true, WINE, 3);
	}
	
	@Test
	public void testDifferentAlgorithmNoShuffleWINE() {
		testAlgos(false, WINE, 3);
	}
	
	@Test
	public void testDifferentAlgorithmWithShuffleBC() {
		testAlgos(true, BC, 2);
	}
	
	@Test
	public void testDifferentAlgorithmNoShuffleBC() {
		testAlgos(false, BC, 2);
	}
	
	@Test
	public void testCopyIRIS() {
		DataSet iris = IRIS;
		DataSet shuffle = iris.copy();
		assertFalse(iris.equals(shuffle));
		
		shuffle = shuffle.shuffle();
		assertFalse(shuffle.equals(iris));
		
		assertFalse(shuffle.getDataRef().equals(iris.getDataRef()));
		assertFalse(shuffle.getHeaderRef().equals(iris.getHeaderRef()));
		assertFalse(shuffle.getLabelRef().equals(iris.getLabelRef()));
	}
	
	@Test
	public void testCopyWINE() {
		DataSet data = WINE;
		DataSet shuffle = data.copy();
		assertFalse(data.equals(shuffle));
		
		shuffle = shuffle.shuffle();
		assertFalse(shuffle.equals(data));
		
		assertFalse(shuffle.getDataRef().equals(data.getDataRef()));
		assertFalse(shuffle.getHeaderRef().equals(data.getHeaderRef()));
		assertFalse(shuffle.getLabelRef().equals(data.getLabelRef()));
	}
	
	@Test
	public void testDataSetColAddsRemoves() {
		DataSet iris = IRIS;
		DataSet shuffle = iris.copy();
		
		final int m = shuffle.getDataRef().getRowDimension();
		final int n = shuffle.getDataRef().getColumnDimension();
		
		double[] newCol = VecUtils.randomGaussian(m);
		shuffle.addColumn(newCol);
		assertTrue(shuffle.getHeaderRef()[n].equals("V" + n));
		
		newCol = VecUtils.randomGaussian(m);
		shuffle.addColumn("NewCol", newCol);
		assertTrue(shuffle.getHeaderRef()[n + 1].equals("NewCol"));
		
		double[][] newCols = MatUtils.randomGaussian(m, 3);
		shuffle.addColumns(newCols);
		assertTrue(shuffle.getDataRef().getColumnDimension() == 9);
		assertTrue(shuffle.getHeaderRef()[8].equals("V8"));
		
		shuffle.dropCol("V4");
		
		//Create a new duplicate named col, capture first val, drop that col
		//(it will drop the first one named that, so the already existing col, 
		//not the new one) and assert the new one remains
		newCol = VecUtils.randomGaussian(m);
		double val = newCol[0];
		shuffle.addColumn("V8", newCol);
		shuffle.dropCol("V8");
		assertTrue(shuffle.getColumn("V8")[0] == val);
		
		// Explicitly test the setCol method
		newCol = VecUtils.randomGaussian(m);
		val = newCol[0];
		shuffle.setColumn("V8", newCol);
		assertTrue(shuffle.getColumn("V8")[0] == val);
		
		shuffle.sortAscInPlace("Sepal Length");
		assertTrue(shuffle.getColumn("Sepal Length")[0] == 4.3); // min val

		shuffle.head();
		
		val = shuffle.getColumn("Sepal Length")[shuffle.numRows()-1];
		shuffle.sortDescInPlace("Sepal Length");
		assertTrue(shuffle.getColumn("Sepal Length")[0] == val); // max val
		
		shuffle.head();
	}
}
