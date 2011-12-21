/*
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

package uk.ac.diamond.scisoft.analysis.rpc.instancedispatcher;
@SuppressWarnings("unused")

public class SingleArgumentPrimitiveArrays {
	public Class<boolean[]> call(boolean[] param) {
		return boolean[].class;
	}
	public Class<byte[]> call(byte[] param) {
		return byte[].class;
	}
	public Class<char[]> call(char[] param) {
		return char[].class;
	}
	public Class<double[]> call(double[] param) {
		return double[].class;
	}
	public Class<float[]> call(float[] param) {
		return float[].class;
	}
	public Class<int[]> call(int[] param) {
		return int[].class;
	}
	public Class<long[]> call(long[] param) {
		return long[].class;
	}
	public Class<short[]> call(short[] param) {
		return short[].class;
	}
}
