/*******************************************************************************
 *    Copyright 2015, 2016 Taylor G Smith
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *******************************************************************************/
package com.clust4j.utils.parallel;

import static org.junit.Assert.*;

import java.util.concurrent.RejectedExecutionException;

import org.junit.Test;

import com.clust4j.utils.MatUtils;

public class ParallelTests {
	
	@Test
	public void testMult() {
		try {
			final double[][] a = MatUtils.randomGaussian(1000, 20);
			final double[][] b = MatUtils.randomGaussian(20, 6000);
			
			long start = System.currentTimeMillis();
			final double[][] ca = MatUtils.multiplyForceSerial(a, b);
			long serialTime = System.currentTimeMillis() - start;
			
			start = System.currentTimeMillis();
			final double[][] cb = MatUtils.multiplyDistributed(a, b);
			long paraTime = System.currentTimeMillis() - start;
			
			assertTrue(MatUtils.equalsExactly(ca, cb));
			System.out.println("Dist MatMult test:\tParallel="+paraTime+", Serial="+serialTime);
		} catch(OutOfMemoryError e) {
			// don't propagate these...
		} catch(RejectedExecutionException r) {
			// don't propagate these...
		}
	}

	@Test
	public void testMultRealBig() {
		try {
			final double[][] a = MatUtils.randomGaussian(5000, 20);
			final double[][] b = MatUtils.randomGaussian(20, 6000);
			
			long start = System.currentTimeMillis();
			final double[][] ca = MatUtils.multiplyForceSerial(a, b);
			long serialTime = System.currentTimeMillis() - start;
			
			start = System.currentTimeMillis();
			final double[][] cb = MatUtils.multiplyDistributed(a, b);
			long paraTime = System.currentTimeMillis() - start;
			
			assertTrue(MatUtils.equalsWithTolerance(ca, cb, 1e-8));
			System.out.println("Dist MatMult test:\tParallel="+paraTime+", Serial="+serialTime);
		} catch(OutOfMemoryError e) {
			// don't propagate these...
		} catch(RejectedExecutionException r) {
			// don't propagate these...
		}
	}
}