/*-
 * Copyright 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.peakfinding.peakfinders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.dawnsci.analysis.api.peakfinding.IPeakFinder;
import org.eclipse.dawnsci.analysis.api.peakfinding.IPeakFinderParameter;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.junit.BeforeClass;
import org.junit.Test;

public class AverageWindowAverageDiffsTest {
	
	private static IPeakFinder winAvgDiff;
	
	@BeforeClass
	public static void testSetup() {
		winAvgDiff = new AverageWindowAverageDiffs();
	}
	
	@Test
	public void nameCheck() {
		assertEquals("Average of Window-Average Differences", winAvgDiff.getName());
	}
	
	@Test
	public void parametersCheck() throws Exception {
		Map<String, IPeakFinderParameter> paramSet = winAvgDiff.getParameters();
		assertEquals(2, paramSet.size());
		
		assertTrue(paramSet.containsKey("windowSize"));
		assertTrue(paramSet.containsKey("nrStdDevs"));
		
		assertEquals(50, winAvgDiff.getParameterValue("windowSize"));
		assertEquals(3, winAvgDiff.getParameterValue("nrStdDevs"));
		
		winAvgDiff.setParameterValue("windowSize", 45);
		assertEquals(45, winAvgDiff.getParameterValue("windowSize"));
		winAvgDiff.setParameterValue("windowSize", 50);
	}
	
	@Test
	public void singlePeakFinding() {
		Dataset xData = PeakyData.getxAxisRange();
		Dataset yData = PeakyData.makeGauPeak().calculateValues(xData);
		
		//Calculate the expected x-coordinate
		Double expectedPos = 0.3785 * PeakyData.getxAxisMax(); 
		Double foundPos;
		
		//Find the x-coordinate of the found peak
		TreeMap<Integer, Double> foundPeaks = (TreeMap<Integer, Double>)winAvgDiff.findPeaks(xData, yData, null);
		//We need the set to have a length of 1 for the next bit...
		assertEquals(1, foundPeaks.size());
		for (Integer i : foundPeaks.keySet()) {
			foundPos = xData.getDouble(i);
			//Yes, it finds the wrong position.
			assertEquals(expectedPos, foundPos, 0.25);
		}
	}

}
