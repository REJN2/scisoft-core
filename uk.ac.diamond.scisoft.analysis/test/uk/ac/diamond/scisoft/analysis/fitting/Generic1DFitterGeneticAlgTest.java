/*-
 * Copyright 2011 Diamond Light Source Ltd.
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

package uk.ac.diamond.scisoft.analysis.fitting;

import uk.ac.diamond.scisoft.analysis.optimize.GeneticAlg;
import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;

public class Generic1DFitterGeneticAlgTest extends Generic1DFitterTestBase {
	static {
		name = "Genetic algorithm";
	}

	@Override
	public IOptimizer createOptimizer() {
		return new GeneticAlg(0.0001, seed);
	}
}
