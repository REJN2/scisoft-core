/*-
 * Copyright © 2010 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.scisoft.analysis.io;

import static org.junit.Assert.assertEquals;
import gda.analysis.io.ScanFileHolderException;
import gda.util.TestUtils;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;

/**
 *
 */
public class RawBinaryTest {

	static String testScratchDirectoryName = null;
	static String testpath = null;
	static double testValue1, testValue2;
	AbstractDataset data;
	int sizex = 240, sizey = 240, range = sizex * sizey;
	
	/**
	 * Creates an empty directory for use by test code.
	 * 
	 * @throws Exception
	 *             if the directory is not created
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testScratchDirectoryName = TestUtils
				.generateDirectorynameFromClassname(RawBinaryTest.class
						.getCanonicalName());
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
	}	
	
	/* @throws ScanFileHolderException
	 * 
	 * Creates a random dataset and tries to save it as ASCII in a file
	 */
	@Test
	public void test2DSaveFile() throws ScanFileHolderException {
		String filePath2D = "test2DSaveFile.raw";
		DataHolder dh = new DataHolder();
		data = DatasetUtils.linSpace(0, range, range, AbstractDataset.FLOAT64);
		data.setShape(sizex, sizey);
		data.setName("test 2D");
		testValue1 = data.getDouble(sizex-1);
		testValue2 = data.getDouble(10,10);
		try {
			dh.addDataset("testing data", data);
			new RawBinarySaver(testScratchDirectoryName + filePath2D).saveFile(dh);
		} catch (Exception e) {
			throw new ScanFileHolderException(
					"Problem testing rawOutput class", e);
		}

		try {
			dh = new RawBinaryLoader(testScratchDirectoryName + filePath2D).loadFile();
			AbstractDataset data = dh.getDataset(0);
			assertEquals(data.getDtype(), AbstractDataset.FLOAT64);
			assertEquals(data.getSize(), range);
			assertEquals(data.getName(), "test 2D");
			assertEquals(data.getShape().length, 2);
			assertEquals(data.getShape()[0], sizey);
			assertEquals(data.getShape()[1], sizex);
			assertEquals(data.getDouble(sizex-1), testValue1, 0.01);
			assertEquals(data.getDouble(10,10), testValue2, 0.01);
		} catch (Exception e) {
			throw new ScanFileHolderException("Problem testing rawOutput class",e);
		}

	}

	/* @throws ScanFileHolderException
	 * 
	 * Creates a random dataset and tries to save it as ASCII in a file
	 */
	@Test
	public void test1DSaveFile() throws ScanFileHolderException {
		String filePath1D = "test1DSaveFile.raw";
		DataHolder dh = new DataHolder();
		data = DatasetUtils.linSpace(0, range, range, AbstractDataset.FLOAT32);
		data.setName("test 1D");
		try {
			dh.addDataset("testing data", data);
			new RawBinarySaver(testScratchDirectoryName + filePath1D).saveFile(dh);
		} catch (Exception e) {
			throw new ScanFileHolderException(
					"Problem testing rawOutput class", e);
		}

		try {
			dh = new RawBinaryLoader(testScratchDirectoryName + filePath1D).loadFile();
			AbstractDataset data = dh.getDataset(0);
			assertEquals(data.getDtype(), AbstractDataset.FLOAT32);
			assertEquals(data.getSize(), range);
			assertEquals(data.getName(), "test 1D");
			assertEquals(data.getShape().length, 1);
			assertEquals(data.getShape()[0], range);
		} catch (Exception e) {
			throw new ScanFileHolderException("Problem testing rawOutput class",e);
		}
	}

	@Test
	public void testLoaderFactory() throws Exception {
		String filePath1D = "testLoaderFactory.raw";
		DataHolder dh = new DataHolder();
		data = DatasetUtils.linSpace(0, range, range, AbstractDataset.INT16);
		data.setName("test factory");
		try {
			dh.addDataset("testing data", data);
			new RawBinarySaver(testScratchDirectoryName + filePath1D).saveFile(dh);
		} catch (Exception e) {
			throw new ScanFileHolderException(
					"Problem testing rawOutput class", e);
		}

		dh = LoaderFactory.getData(testScratchDirectoryName + filePath1D, null);
		if (dh==null || dh.getNames().length<1) throw new Exception();
		AbstractDataset data = dh.getDataset(0);
		assertEquals(data.getDtype(), AbstractDataset.INT16);
		assertEquals(data.getSize(), range);
		assertEquals(data.getName(), "test factory");
		assertEquals(data.getShape().length, 1);
		assertEquals(data.getShape()[0], range);
	}

}
