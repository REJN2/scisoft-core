/*-
 * Copyright 2014 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.io;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public class MerlinLoaderTest {
	
	final static String testFileFolder = "testfiles/gda/analysis/io/merlin/";
	
	@Test
	public void testMerlinDataLoader()  throws Exception {
		
		final String path = testFileFolder+"default1.bin";
		DataHolder dataHolder = LoaderFactory.getData(path, null);
 		
		AbstractDataset data = dataHolder.getDataset(0);
		int[] shape = data.getShape();
		assertEquals(512,shape[0], 0.0);
		assertEquals(512,shape[1], 0.0);
		assertEquals(4095,data.max().intValue(), 0.0);
//		assertEquals(2572.0, data.getDouble(1023, 1023), 0.0);
//		assertEquals(0.0, data.getDouble(2047, 2047), 0.0);
	}

}