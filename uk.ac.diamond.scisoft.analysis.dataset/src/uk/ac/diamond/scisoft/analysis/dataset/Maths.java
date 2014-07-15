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

package uk.ac.diamond.scisoft.analysis.dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.complex.Complex;

/**
 * Mathematics class
 */
public class Maths {
	/**
	 * This function returns the name of the dataset, bracketed if it already
	 * includes some mathematical symbol
	 * 
	 * @param dataset
	 * @return the bracketed if necessary method name
	 */
	private static StringBuilder bracketIfNecessary(final Dataset dataset) {
		final String name = dataset.getName().trim();
		StringBuilder newName = new StringBuilder(name);
		if (name.contains("+") || name.contains("-") || name.contains("*") ||
				name.contains("/") || name.contains("^") || name.contains("'")) {
			newName.insert(0, '(');
			newName.append(')');
		}

		return newName;
	}

	private static void addFunctionName(final Dataset dataset, final String fname) {
		StringBuilder name = new StringBuilder(dataset.getName());
		name.insert(0, '(');
		name.insert(0, fname);
		name.append(')');

		dataset.setName(name.toString());
	}


	private static long toLong(double d) {
		if (Double.isInfinite(d) || Double.isNaN(d))
			return 0;
		return (long) d;
	}

	/**
	 * Unwrap result from mathematical methods if necessary
	 * @param o
	 * @param a
	 * @return a dataset if a is a dataset or an object of the same class as o
	 */
	public static Object unwrap(final Dataset o, final Object a) {
		return a instanceof Dataset ? o : o.getObjectAbs(o.getOffset());
	}

	/**
	 * Unwrap result from mathematical methods if necessary
	 * @param o
	 * @param a
	 * @return a dataset if either a and b are datasets or an object of the same class as o
	 */
	public static Object unwrap(final Dataset o, final Object a, final Object b) {
		return (a instanceof Dataset || b instanceof Dataset) ? o : o.getObjectAbs(o.getOffset());
	}

	/**
	 * Unwrap result from mathematical methods if necessary
	 * @param o
	 * @param a
	 * @return a dataset if any inputs are datasets or an object of the same class as o
	 */
	public static Object unwrap(final Dataset o, final Object... a) {
		boolean isAnyDataset = false;
		for (Object obj : a) {
			if (obj instanceof Dataset) {
				isAnyDataset = true;
				break;
			}
		}
		return isAnyDataset ? o : o.getObjectAbs(o.getOffset());
	}

	/**
	 * @param a
	 * @param b
	 * @param o output can be null - in which case, a new dataset is created
	 * @return a op b, operation on a and b
	 */
	public static AbstractDataset operate(final Object a, final Object b, final Dataset o, final BinaryOperation op) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final Dataset db = b instanceof Dataset ? (Dataset) b : DatasetFactory.createFromObject(b);
		final BroadcastIterator it = new BroadcastIterator(da, db, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();
		switch (dt) {
		case Dataset.BOOL:
			boolean[] bdata = ((BooleanDataset) result).getData();
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				bdata[it.oIndex] = op.booleanOperate(it.aLong, it.aLong);
			}
			break;
		case Dataset.INT8:
			byte[] i8data = ((ByteDataset) result).getData();
			it.setDoubleOutput(false);
	
			while (it.hasNext()) {
				i8data[it.oIndex] = (byte) op.longOperate(it.aLong, it.aLong);
			}
			break;
		case Dataset.INT16:
			short[] i16data = ((ShortDataset) result).getData();
			it.setDoubleOutput(false);
	
			while (it.hasNext()) {
				i16data[it.oIndex] = (short) op.longOperate(it.aLong, it.aLong);
			}
			break;
		case Dataset.INT32:
			int[] i32data = ((IntegerDataset) result).getData();
			it.setDoubleOutput(false);
			
			while (it.hasNext()) {
				i32data[it.oIndex] = (int) op.longOperate(it.aLong, it.aLong);
			}
			break;
		case Dataset.INT64:
			long[] i64data = ((LongDataset) result).getData();
			it.setDoubleOutput(false);
	
			while (it.hasNext()) {
				i64data[it.oIndex] = op.longOperate(it.aLong, it.aLong);
			}
			break;
		case Dataset.FLOAT32:
			float[] f32data = ((FloatDataset) result).getData();
			it.setDoubleOutput(false);
	
			while (it.hasNext()) {
				f32data[it.oIndex] = (float) op.doubleOperate(it.aDouble, it.bDouble);
			}
			break;
		case Dataset.FLOAT64:
			double[] f64data = ((DoubleDataset) result).getData();
	
			while (it.hasNext()) {
				f64data[it.oIndex] = op.doubleOperate(it.aDouble, it.bDouble);
			}
			break;
		case Dataset.ARRAYINT8:
			byte[] ai8data = ((CompoundByteDataset) result).getData();
			it.setDoubleOutput(false);
	
			if (is == 1) {
				while (it.hasNext()) {
					ai8data[it.oIndex] = (byte) op.longOperate(it.aLong, it.bLong);
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai8data[it.oIndex] = (byte) op.longOperate(it.aLong, it.bLong);
					for (int j = 1; j < is; j++) {
						ai8data[it.oIndex + j] = (byte) op.longOperate(it.aLong, db.getElementLongAbs(it.bIndex + j));
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai8data[it.oIndex] = (byte) op.longOperate(it.aLong, it.bLong);
					for (int j = 1; j < is; j++) {
						ai8data[it.oIndex + j] = (byte) op.longOperate(da.getElementLongAbs(it.aIndex + j), it.bLong);
					}
				}
			} else {
				while (it.hasNext()) {
					ai8data[it.oIndex] = (byte) op.longOperate(it.aLong, it.bLong);
					for (int j = 1; j < is; j++) {
						ai8data[it.oIndex + j] = (byte) op.longOperate(da.getElementLongAbs(it.aIndex + j), db.getElementLongAbs(it.bIndex + j));
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			short[] ai16data = ((CompoundShortDataset) result).getData();
			it.setDoubleOutput(false);
	
			if (is == 1) {
				while (it.hasNext()) {
					ai16data[it.oIndex] = (short) op.longOperate(it.aLong, it.bLong);
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai16data[it.oIndex] = (short) op.longOperate(it.aLong, it.bLong);
					for (int j = 1; j < is; j++) {
						ai16data[it.oIndex + j] = (short) op.longOperate(it.aLong, db.getElementLongAbs(it.bIndex + j));
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai16data[it.oIndex] = (short) op.longOperate(it.aLong, it.bLong);
					for (int j = 1; j < is; j++) {
						ai16data[it.oIndex + j] = (short) op.longOperate(da.getElementLongAbs(it.aIndex + j), it.bLong);
					}
				}
			} else {
				while (it.hasNext()) {
					ai16data[it.oIndex] = (short) op.longOperate(it.aLong, it.bLong);
					for (int j = 1; j < is; j++) {
						ai16data[it.oIndex + j] = (short) op.longOperate(da.getElementLongAbs(it.aIndex + j), db.getElementLongAbs(it.bIndex + j));
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			int[] ai32data = ((CompoundIntegerDataset) result).getData();
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					ai32data[it.oIndex] = (int) op.longOperate(it.aLong, it.bLong);
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai32data[it.oIndex] = (int) op.longOperate(it.aLong, it.bLong);
					for (int j = 1; j < is; j++) {
						ai32data[it.oIndex + j] = (int) op.longOperate(it.aLong, db.getElementLongAbs(it.bIndex + j));
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai32data[it.oIndex] = (int) op.longOperate(it.aLong, it.bLong);
					for (int j = 1; j < is; j++) {
						ai32data[it.oIndex + j] = (int) op.longOperate(da.getElementLongAbs(it.aIndex + j), it.bLong);
					}
				}
			} else {
				while (it.hasNext()) {
					ai32data[it.oIndex] = (int) op.longOperate(it.aLong, it.bLong);
					for (int j = 1; j < is; j++) {
						ai32data[it.oIndex + j] = (int) op.longOperate(da.getElementLongAbs(it.aIndex + j), db.getElementLongAbs(it.bIndex + j));
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			long[] ai64data = ((CompoundLongDataset) result).getData();
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					ai64data[it.oIndex] = op.longOperate(it.aLong, it.bLong);
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai64data[it.oIndex] = op.longOperate(it.aLong, it.bLong);
					for (int j = 1; j < is; j++) {
						ai64data[it.oIndex + j] = op.longOperate(it.aLong, db.getElementLongAbs(it.bIndex + j));
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai64data[it.oIndex] = op.longOperate(it.aLong, it.bLong);
					for (int j = 1; j < is; j++) {
						ai64data[it.oIndex + j] = op.longOperate(da.getElementLongAbs(it.aIndex + j), it.bLong);
					}
				}
			} else {
				while (it.hasNext()) {
					ai64data[it.oIndex] = op.longOperate(it.aLong, it.bLong);
					for (int j = 1; j < is; j++) {
						ai64data[it.oIndex + j] = (long) op.doubleOperate(da.getElementLongAbs(it.aIndex + j), db.getElementLongAbs(it.bIndex + j));
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT32:
			float[] a32data = ((CompoundFloatDataset) result).getData();
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					a32data[it.oIndex] = (float) op.doubleOperate(it.aDouble, it.bDouble);
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					a32data[it.oIndex] = (float) op.doubleOperate(it.aDouble, it.bDouble);
					for (int j = 1; j < is; j++) {
						a32data[it.oIndex + j] = (float) op.doubleOperate(it.aDouble, db.getElementDoubleAbs(it.bIndex + j));
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					a32data[it.oIndex] = (float) op.doubleOperate(it.aDouble, it.bDouble);
					for (int j = 1; j < is; j++) {
						a32data[it.oIndex + j] = (float) op.doubleOperate(da.getElementDoubleAbs(it.aIndex + j), it.bDouble);
					}
				}
			} else {
				while (it.hasNext()) {
					a32data[it.oIndex] = (float) op.doubleOperate(it.aDouble, it.bDouble);
					for (int j = 1; j < is; j++) {
						a32data[it.oIndex + j] = (float) op.doubleOperate(da.getElementDoubleAbs(it.aIndex + j), db.getElementDoubleAbs(it.bIndex + j));
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			double[] a64data = ((CompoundDoubleDataset) result).getData();
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					a64data[it.oIndex] = op.doubleOperate(it.aDouble, it.bDouble);
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					a64data[it.oIndex] = op.doubleOperate(it.aDouble, it.bDouble);
					for (int j = 1; j < is; j++) {
						a64data[it.oIndex + j] = op.doubleOperate(it.aDouble, db.getElementDoubleAbs(it.bIndex + j));
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					a64data[it.oIndex] = op.doubleOperate(it.aDouble, it.bDouble);
					for (int j = 1; j < is; j++) {
						a64data[it.oIndex + j] = op.doubleOperate(da.getElementDoubleAbs(it.aIndex + j), it.bDouble);
					}
				}
			} else {
				while (it.hasNext()) {
					a64data[it.oIndex] = op.doubleOperate(it.aDouble, it.bDouble);
					for (int j = 1; j < is; j++) {
						a64data[it.oIndex + j] = op.doubleOperate(da.getElementDoubleAbs(it.aIndex + j), db.getElementDoubleAbs(it.bIndex + j));
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			float[] c64data = ((ComplexFloatDataset) result).getData();
			it.setDoubleOutput(true);

			if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ib = db.getElementDoubleAbs(it.bIndex + 1);
					c64data[it.oIndex]     = (float) op.realComplexOperate(it.aDouble, 0, it.bDouble, ib);
					c64data[it.oIndex + 1] = (float) op.imagComplexOperate(it.aDouble, 0, it.bDouble, ib);
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ia = da.getElementDoubleAbs(it.aIndex + 1);
					c64data[it.oIndex]     = (float) op.realComplexOperate(it.aDouble, ia, it.bDouble, 0);
					c64data[it.oIndex + 1] = (float) op.imagComplexOperate(it.aDouble, ia, it.bDouble, 0);
				}
			} else {
				while (it.hasNext()) {
					final double ia = da.getElementDoubleAbs(it.aIndex + 1);
					final double ib = db.getElementDoubleAbs(it.bIndex + 1);
					c64data[it.oIndex]     = (float) op.realComplexOperate(it.aDouble, ia, it.bDouble, ib);
					c64data[it.oIndex + 1] = (float) op.imagComplexOperate(it.aDouble, ia, it.bDouble, ib);
				}
			}
			break;
		case Dataset.COMPLEX128:
			double[] c128data = ((ComplexDoubleDataset) result).getData();
			it.setDoubleOutput(true);

			if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ib = db.getElementDoubleAbs(it.bIndex + 1);
					c128data[it.oIndex]     = op.realComplexOperate(it.aDouble, 0, it.bDouble, ib);
					c128data[it.oIndex + 1] = op.imagComplexOperate(it.aDouble, 0, it.bDouble, ib);
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ia = da.getElementDoubleAbs(it.aIndex + 1);
					c128data[it.oIndex]     = op.realComplexOperate(it.aDouble, ia, it.bDouble, 0);
					c128data[it.oIndex + 1] = op.imagComplexOperate(it.aDouble, ia, it.bDouble, 0);
				}
			} else {
				while (it.hasNext()) {
					final double ia = da.getElementDoubleAbs(it.aIndex + 1);
					final double ib = db.getElementDoubleAbs(it.bIndex + 1);
					c128data[it.oIndex]     = op.realComplexOperate(it.aDouble, ia, it.bDouble, ib);
					c128data[it.oIndex + 1] = op.imagComplexOperate(it.aDouble, ia, it.bDouble, ib);
				}
			}
			break;
		default:
			throw new UnsupportedOperationException("add does not support this dataset type");
		}
	
		// set the name based on the changes made
		result.setName(bracketIfNecessary(da).append(op.toString()).append(bracketIfNecessary(db)).toString());
	
		return (AbstractDataset) result;
	}

	/**
	 * @param a
	 * @param b
	 * @return floor division of a and b
	 */
	public static AbstractDataset floorDivide(final Object a, final Object b) {
		return floorDivide(a, b, null);
	}

	/**
	 * @param a
	 * @param b
	 * @param o output can be null - in which case, a new dataset is created
	 * @return floor division of a and b
	 */
	public static AbstractDataset floorDivide(final Object a, final Object b, final Dataset o) {
		Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		Dataset db = b instanceof Dataset ? (Dataset) b : DatasetFactory.createFromObject(b);
		return divide(da, db, o).ifloor();
	}

	/**
	 * Find reciprocal from dataset
	 * @param a
	 * @return reciprocal dataset
	 */
	public static AbstractDataset reciprocal(final Object a) {
		return reciprocal(a, null);
	}

	/**
	 * Find reciprocal from dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return reciprocal dataset
	 */
	public static AbstractDataset reciprocal(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		return divide(1, da, o);
	}

	/**
	 * abs - absolute value of each element
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset abs(final Object a) {
		return abs(a, null);
	}

	/**
	 * abs - absolute value of each element
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset abs(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true, true, false);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();
	
		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			it.setDoubleOutput(false);
	
			while (it.hasNext()) {
				final long ix = it.aLong;
				byte ox;
				ox = (byte) toLong(Math.abs(ix));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			it.setDoubleOutput(false);
	
			while (it.hasNext()) {
				final long ix = it.aLong;
				short ox;
				ox = (short) toLong(Math.abs(ix));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			it.setDoubleOutput(false);
	
			while (it.hasNext()) {
				final long ix = it.aLong;
				long ox;
				ox = toLong(Math.abs(ix));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			it.setDoubleOutput(false);
	
			while (it.hasNext()) {
				final long ix = it.aLong;
				int ox;
				ox = (int) toLong(Math.abs(ix));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			it.setDoubleOutput(false);
	
			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					byte ox;
					ox = (byte) toLong(Math.abs(ix));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					byte ox;
					ox = (byte) toLong(Math.abs(ix));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.abs(ix));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			it.setDoubleOutput(false);
	
			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					short ox;
					ox = (short) toLong(Math.abs(ix));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					short ox;
					ox = (short) toLong(Math.abs(ix));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.abs(ix));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			it.setDoubleOutput(false);
	
			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					long ox;
					ox = toLong(Math.abs(ix));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					long ox;
					ox = toLong(Math.abs(ix));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.abs(ix));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			it.setDoubleOutput(false);
	
			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					int ox;
					ox = (int) toLong(Math.abs(ix));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					int ox;
					ox = (int) toLong(Math.abs(ix));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.abs(ix));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.abs(ix));
					of32data[it.oIndex] = ox;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					ox = (float) (Math.hypot(ix, iy));
					of32data[it.oIndex] = ox;
				}
				
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.abs(ix));
					of64data[it.oIndex] = ox;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					ox = (Math.hypot(ix, iy));
					of64data[it.oIndex] = ox;
				}
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.abs(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.abs(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.abs(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.abs(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.abs(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.abs(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.hypot(ix, iy));
					oc64data[it.oIndex] = ox;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					ox = (float) (Math.hypot(ix, iy));
					oc64data[it.oIndex] = ox;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.hypot(ix, iy));
					oc128data[it.oIndex] = ox;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					ox = (Math.hypot(ix, iy));
					oc128data[it.oIndex] = ox;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("abs supports integer, compound integer, real, compound real, complex datasets only");
		}
	
		addFunctionName(result, "abs");
		return (AbstractDataset) result;
	}

	/**
	 * @param a
	 * @return a^*, complex conjugate of a
	 */
	public static AbstractDataset conjugate(final Object a) {
		return conjugate(a, null);
	}

	/**
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return a^*, complex conjugate of a
	 */
	public static AbstractDataset conjugate(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		int at = da.getDtype();
		IndexIterator it1 = da.getIterator();

		SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true, true, true);
		Dataset result = it.getOutput();

		switch (at) {
		case Dataset.COMPLEX64:
			float[] c64data = ((ComplexFloatDataset) result).getData();

			for (int i = 0; it1.hasNext();) {
				c64data[i++] = (float) da.getElementDoubleAbs(it1.index);
				c64data[i++] = (float) -da.getElementDoubleAbs(it1.index+1);
			}
			result.setName(bracketIfNecessary(da).append("^*").toString());
			break;
		case Dataset.COMPLEX128:
			double[] c128data = ((ComplexDoubleDataset) result).getData();

			for (int i = 0; it1.hasNext();) {
				c128data[i++] = da.getElementDoubleAbs(it1.index);
				c128data[i++] = -da.getElementDoubleAbs(it1.index+1);
			}
			result.setName(bracketIfNecessary(da).append("^*").toString());
			break;
		default:
			result = da;
		}

		return (AbstractDataset) result;
	}

	/**
	 * @param a side of right-angled triangle
	 * @param b side of right-angled triangle
	 * @return hypotenuse of right-angled triangle: sqrt(a^2 + a^2)
	 */
	public static AbstractDataset hypot(final Object a, final Object b) {
		return hypot(a, b, null);
	}

	/**
	 * @param a side of right-angled triangle
	 * @param b side of right-angled triangle
	 * @param o output can be null - in which case, a new dataset is created
	 * @return hypotenuse of right-angled triangle: sqrt(a^2 + a^2)
	 */
	public static AbstractDataset hypot(final Object a, final Object b, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final Dataset db = b instanceof Dataset ? (Dataset) b : DatasetFactory.createFromObject(b);

		final BroadcastIterator it = new BroadcastIterator(da, db, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();
		switch (dt) {
		case Dataset.BOOL:
			boolean[] bdata = ((BooleanDataset) result).getData();

			while (it.hasNext()) {
				bdata[it.oIndex] = Math.hypot(it.aDouble, it.bDouble) != 0;
			}
			break;
		case Dataset.INT8:
			byte[] i8data = ((ByteDataset) result).getData();

			while (it.hasNext()) {
				i8data[it.oIndex] = (byte) toLong(Math.hypot(it.aDouble, it.bDouble));
			}
			break;
		case Dataset.INT16:
			short[] i16data = ((ShortDataset) result).getData();

			while (it.hasNext()) {
				i16data[it.oIndex] = (short) toLong(Math.hypot(it.aDouble, it.bDouble));
			}
			break;
		case Dataset.INT32:
			int[] i32data = ((IntegerDataset) result).getData();
			
			while (it.hasNext()) {
				i32data[it.oIndex] = (int) toLong(Math.hypot(it.aDouble, it.bDouble));
			}
			break;
		case Dataset.INT64:
			long[] i64data = ((LongDataset) result).getData();

			while (it.hasNext()) {
				i64data[it.oIndex] = toLong(Math.hypot(it.aDouble, it.bDouble));
			}
			break;
		case Dataset.FLOAT32:
			float[] f32data = ((FloatDataset) result).getData();

			while (it.hasNext()) {
				f32data[it.oIndex] = (float) Math.hypot(it.aDouble, it.bDouble);
			}
			break;
		case Dataset.FLOAT64:
			double[] f64data = ((DoubleDataset) result).getData();

			while (it.hasNext()) {
				f64data[it.oIndex] = Math.hypot(it.aDouble, it.bDouble);
			}
			break;
		case Dataset.ARRAYINT8:
			byte[] ai8data = ((CompoundByteDataset) result).getData();

			if (is == 1) {
				while (it.hasNext()) {
					ai8data[it.oIndex] = (byte) toLong(Math.hypot(it.aDouble, it.bDouble));
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai8data[it.oIndex] = (byte) toLong(Math.hypot(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai8data[it.oIndex + j] = (byte) toLong(Math.hypot(it.aDouble, db.getElementDoubleAbs(it.bIndex + j)));
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai8data[it.oIndex] = (byte) toLong(Math.hypot(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai8data[it.oIndex + j] = (byte) toLong(Math.hypot(da.getElementDoubleAbs(it.aIndex + j), it.bDouble));
					}
				}
			} else {
				while (it.hasNext()) {
					ai8data[it.oIndex] = (byte) toLong(Math.hypot(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai8data[it.oIndex + j] = (byte) toLong(Math.hypot(da.getElementDoubleAbs(it.aIndex + j), db.getElementDoubleAbs(it.bIndex + j)));
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			short[] ai16data = ((CompoundShortDataset) result).getData();

			if (is == 1) {
				while (it.hasNext()) {
					ai16data[it.oIndex] = (short) toLong(Math.hypot(it.aDouble, it.bDouble));
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai16data[it.oIndex] = (short) toLong(Math.hypot(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai16data[it.oIndex + j] = (short) toLong(Math.hypot(it.aDouble, db.getElementDoubleAbs(it.bIndex + j)));
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai16data[it.oIndex] = (short) toLong(Math.hypot(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai16data[it.oIndex + j] = (short) toLong(Math.hypot(da.getElementDoubleAbs(it.aIndex + j), it.bDouble));
					}
				}
			} else {
				while (it.hasNext()) {
					ai16data[it.oIndex] = (short) toLong(Math.hypot(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai16data[it.oIndex + j] = (short) toLong(Math.hypot(da.getElementDoubleAbs(it.aIndex + j), db.getElementDoubleAbs(it.bIndex + j)));
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			int[] ai32data = ((CompoundIntegerDataset) result).getData();

			if (is == 1) {
				while (it.hasNext()) {
					ai32data[it.oIndex] = (int) toLong(Math.hypot(it.aDouble, it.bDouble));
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai32data[it.oIndex] = (int) toLong(Math.hypot(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai32data[it.oIndex + j] = (int) toLong(Math.hypot(it.aDouble, db.getElementDoubleAbs(it.bIndex + j)));
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai32data[it.oIndex] = (int) toLong(Math.hypot(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai32data[it.oIndex + j] = (int) toLong(Math.hypot(da.getElementDoubleAbs(it.aIndex + j), it.bDouble));
					}
				}
			} else {
				while (it.hasNext()) {
					ai32data[it.oIndex] = (int) toLong(Math.hypot(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai32data[it.oIndex + j] = (int) toLong(Math.hypot(da.getElementDoubleAbs(it.aIndex + j), db.getElementDoubleAbs(it.bIndex + j)));
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			long[] ai64data = ((CompoundLongDataset) result).getData();

			if (is == 1) {
				while (it.hasNext()) {
					ai64data[it.oIndex] = toLong(Math.hypot(it.aDouble, it.bDouble));
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai64data[it.oIndex] = toLong(Math.hypot(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai64data[it.oIndex + j] = toLong(Math.hypot(it.aDouble, db.getElementDoubleAbs(it.bIndex + j)));
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai64data[it.oIndex] = toLong(Math.hypot(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai64data[it.oIndex + j] = toLong(Math.hypot(da.getElementDoubleAbs(it.aIndex + j), it.bDouble));
					}
				}
			} else {
				while (it.hasNext()) {
					ai64data[it.oIndex] = toLong(Math.hypot(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai64data[it.oIndex + j] = toLong(Math.hypot(da.getElementDoubleAbs(it.aIndex + j), db.getElementDoubleAbs(it.bIndex + j)));
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT32:
			float[] a32data = ((CompoundFloatDataset) result).getData();

			if (is == 1) {
				while (it.hasNext()) {
					a32data[it.oIndex] = (float) Math.hypot(it.aDouble, it.bDouble);
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					a32data[it.oIndex] = (float) Math.hypot(it.aDouble, it.bDouble);
					for (int j = 1; j < is; j++) {
						a32data[it.oIndex + j] = (float) Math.hypot(it.aDouble, db.getElementDoubleAbs(it.bIndex + j));
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					a32data[it.oIndex] = (float) Math.hypot(it.aDouble, it.bDouble);
					for (int j = 1; j < is; j++) {
						a32data[it.oIndex + j] = (float) Math.hypot(da.getElementDoubleAbs(it.aIndex + j), it.bDouble);
					}
				}
			} else {
				while (it.hasNext()) {
					a32data[it.oIndex] = (float) Math.hypot(it.aDouble, it.bDouble);
					for (int j = 1; j < is; j++) {
						a32data[it.oIndex + j] = (float) Math.hypot(da.getElementDoubleAbs(it.aIndex + j), db.getElementDoubleAbs(it.bIndex + j));
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			double[] a64data = ((CompoundDoubleDataset) result).getData();

			if (is == 1) {
				while (it.hasNext()) {
					a64data[it.oIndex] = Math.hypot(it.aDouble, it.bDouble);
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					a64data[it.oIndex] = Math.hypot(it.aDouble, it.bDouble);
					for (int j = 1; j < is; j++) {
						a64data[it.oIndex + j] = Math.hypot(it.aDouble, db.getElementDoubleAbs(it.bIndex + j));
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					a64data[it.oIndex] = Math.hypot(it.aDouble, it.bDouble);
					for (int j = 1; j < is; j++) {
						a64data[it.oIndex + j] = Math.hypot(da.getElementDoubleAbs(it.aIndex + j), it.bDouble);
					}
				}
			} else {
				while (it.hasNext()) {
					a64data[it.oIndex] = Math.hypot(it.aDouble, it.bDouble);
					for (int j = 1; j < is; j++) {
						a64data[it.oIndex + j] = Math.hypot(da.getElementDoubleAbs(it.aIndex + j), db.getElementDoubleAbs(it.bIndex + j));
					}
				}
			}
			break;
		default:
			throw new UnsupportedOperationException("hypot does not support this dataset type");
		}

		// set the name based on the changes made
		result.setName(new StringBuilder("hypot(").append(da.getName()).append(", ").append(db.getName()).append(")").toString());

		return (AbstractDataset) result;
	}

	/**
	 * @param a opposite side of right-angled triangle
	 * @param b adjacent side of right-angled triangle
	 * @return angle of triangle: atan(a/b)
	 */
	public static AbstractDataset arctan2(final Object a, final Object b) {
		return arctan2(a, b, null);
	}

	/**
	 * @param a opposite side of right-angled triangle
	 * @param b adjacent side of right-angled triangle
	 * @param o output can be null - in which case, a new dataset is created
	 * @return angle of triangle: atan(a/b)
	 */
	public static AbstractDataset arctan2(final Object a, final Object b, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final Dataset db = b instanceof Dataset ? (Dataset) b : DatasetFactory.createFromObject(b);

		final BroadcastIterator it = new BroadcastIterator(da, db, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();
		switch (dt) {
		case Dataset.BOOL:
			boolean[] bdata = ((BooleanDataset) result).getData();

			while (it.hasNext()) {
				bdata[it.oIndex] = Math.atan2(it.aDouble, it.bDouble) != 0;
			}
			break;
		case Dataset.INT8:
			byte[] i8data = ((ByteDataset) result).getData();

			while (it.hasNext()) {
				i8data[it.oIndex] = (byte) toLong(Math.atan2(it.aDouble, it.bDouble));
			}
			break;
		case Dataset.INT16:
			short[] i16data = ((ShortDataset) result).getData();

			while (it.hasNext()) {
				i16data[it.oIndex] = (short) toLong(Math.atan2(it.aDouble, it.bDouble));
			}
			break;
		case Dataset.INT32:
			int[] i32data = ((IntegerDataset) result).getData();
			
			while (it.hasNext()) {
				i32data[it.oIndex] = (int) toLong(Math.atan2(it.aDouble, it.bDouble));
			}
			break;
		case Dataset.INT64:
			long[] i64data = ((LongDataset) result).getData();

			while (it.hasNext()) {
				i64data[it.oIndex] = toLong(Math.atan2(it.aDouble, it.bDouble));
			}
			break;
		case Dataset.FLOAT32:
			float[] f32data = ((FloatDataset) result).getData();

			while (it.hasNext()) {
				f32data[it.oIndex] = (float) Math.atan2(it.aDouble, it.bDouble);
			}
			break;
		case Dataset.FLOAT64:
			double[] f64data = ((DoubleDataset) result).getData();

			while (it.hasNext()) {
				f64data[it.oIndex] = Math.atan2(it.aDouble, it.bDouble);
			}
			break;
		case Dataset.ARRAYINT8:
			byte[] ai8data = ((CompoundByteDataset) result).getData();

			if (is == 1) {
				while (it.hasNext()) {
					ai8data[it.oIndex] = (byte) toLong(Math.atan2(it.aDouble, it.bDouble));
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai8data[it.oIndex] = (byte) toLong(Math.atan2(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai8data[it.oIndex + j] = (byte) toLong(Math.atan2(it.aDouble, db.getElementDoubleAbs(it.bIndex + j)));
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai8data[it.oIndex] = (byte) toLong(Math.atan2(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai8data[it.oIndex + j] = (byte) toLong(Math.atan2(da.getElementDoubleAbs(it.aIndex + j), it.bDouble));
					}
				}
			} else {
				while (it.hasNext()) {
					ai8data[it.oIndex] = (byte) toLong(Math.atan2(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai8data[it.oIndex + j] = (byte) toLong(Math.atan2(da.getElementDoubleAbs(it.aIndex + j), db.getElementDoubleAbs(it.bIndex + j)));
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			short[] ai16data = ((CompoundShortDataset) result).getData();

			if (is == 1) {
				while (it.hasNext()) {
					ai16data[it.oIndex] = (short) toLong(Math.atan2(it.aDouble, it.bDouble));
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai16data[it.oIndex] = (short) toLong(Math.atan2(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai16data[it.oIndex + j] = (short) toLong(Math.atan2(it.aDouble, db.getElementDoubleAbs(it.bIndex + j)));
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai16data[it.oIndex] = (short) toLong(Math.atan2(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai16data[it.oIndex + j] = (short) toLong(Math.atan2(da.getElementDoubleAbs(it.aIndex + j), it.bDouble));
					}
				}
			} else {
				while (it.hasNext()) {
					ai16data[it.oIndex] = (short) toLong(Math.atan2(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai16data[it.oIndex + j] = (short) toLong(Math.atan2(da.getElementDoubleAbs(it.aIndex + j), db.getElementDoubleAbs(it.bIndex + j)));
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			int[] ai32data = ((CompoundIntegerDataset) result).getData();

			if (is == 1) {
				while (it.hasNext()) {
					ai32data[it.oIndex] = (int) toLong(Math.atan2(it.aDouble, it.bDouble));
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai32data[it.oIndex] = (int) toLong(Math.atan2(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai32data[it.oIndex + j] = (int) toLong(Math.atan2(it.aDouble, db.getElementDoubleAbs(it.bIndex + j)));
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai32data[it.oIndex] = (int) toLong(Math.atan2(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai32data[it.oIndex + j] = (int) toLong(Math.atan2(da.getElementDoubleAbs(it.aIndex + j), it.bDouble));
					}
				}
			} else {
				while (it.hasNext()) {
					ai32data[it.oIndex] = (int) toLong(Math.atan2(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai32data[it.oIndex + j] = (int) toLong(Math.atan2(da.getElementDoubleAbs(it.aIndex + j), db.getElementDoubleAbs(it.bIndex + j)));
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			long[] ai64data = ((CompoundLongDataset) result).getData();

			if (is == 1) {
				while (it.hasNext()) {
					ai64data[it.oIndex] = toLong(Math.atan2(it.aDouble, it.bDouble));
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai64data[it.oIndex] = toLong(Math.atan2(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai64data[it.oIndex + j] = toLong(Math.atan2(it.aDouble, db.getElementDoubleAbs(it.bIndex + j)));
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					ai64data[it.oIndex] = toLong(Math.atan2(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai64data[it.oIndex + j] = toLong(Math.atan2(da.getElementDoubleAbs(it.aIndex + j), it.bDouble));
					}
				}
			} else {
				while (it.hasNext()) {
					ai64data[it.oIndex] = toLong(Math.atan2(it.aDouble, it.bDouble));
					for (int j = 1; j < is; j++) {
						ai64data[it.oIndex + j] = toLong(Math.atan2(da.getElementDoubleAbs(it.aIndex + j), db.getElementDoubleAbs(it.bIndex + j)));
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT32:
			float[] a32data = ((CompoundFloatDataset) result).getData();

			if (is == 1) {
				while (it.hasNext()) {
					a32data[it.oIndex] = (float) Math.atan2(it.aDouble, it.bDouble);
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					a32data[it.oIndex] = (float) Math.atan2(it.aDouble, it.bDouble);
					for (int j = 1; j < is; j++) {
						a32data[it.oIndex + j] = (float) Math.atan2(it.aDouble, db.getElementDoubleAbs(it.bIndex + j));
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					a32data[it.oIndex] = (float) Math.atan2(it.aDouble, it.bDouble);
					for (int j = 1; j < is; j++) {
						a32data[it.oIndex + j] = (float) Math.atan2(da.getElementDoubleAbs(it.aIndex + j), it.bDouble);
					}
				}
			} else {
				while (it.hasNext()) {
					a32data[it.oIndex] = (float) Math.atan2(it.aDouble, it.bDouble);
					for (int j = 1; j < is; j++) {
						a32data[it.oIndex + j] = (float) Math.atan2(da.getElementDoubleAbs(it.aIndex + j), db.getElementDoubleAbs(it.bIndex + j));
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			double[] a64data = ((CompoundDoubleDataset) result).getData();

			if (is == 1) {
				while (it.hasNext()) {
					a64data[it.oIndex] = Math.atan2(it.aDouble, it.bDouble);
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					a64data[it.oIndex] = Math.atan2(it.aDouble, it.bDouble);
					for (int j = 1; j < is; j++) {
						a64data[it.oIndex + j] = Math.atan2(it.aDouble, db.getElementDoubleAbs(it.bIndex + j));
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					a64data[it.oIndex] = Math.atan2(it.aDouble, it.bDouble);
					for (int j = 1; j < is; j++) {
						a64data[it.oIndex + j] = Math.atan2(da.getElementDoubleAbs(it.aIndex + j), it.bDouble);
					}
				}
			} else {
				while (it.hasNext()) {
					a64data[it.oIndex] = Math.atan2(it.aDouble, it.bDouble);
					for (int j = 1; j < is; j++) {
						a64data[it.oIndex + j] = Math.atan2(da.getElementDoubleAbs(it.aIndex + j), db.getElementDoubleAbs(it.bIndex + j));
					}
				}
			}
			break;
		default:
			throw new UnsupportedOperationException("atan2 does not support multiple-element dataset");
		}

		// set the name based on the changes made
		result.setName(new StringBuilder("atan2(").append(da.getName()).append(", ").append(db.getName()).append(")").toString());

		return (AbstractDataset) result;
	}

	/**
	 * Create a dataset of the arguments from a complex dataset
	 * @param a
	 * @return dataset of angles
	 */
	public static AbstractDataset angle(final Object a) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		Dataset ds = null;
		int dt = da.getDtype();
		IndexIterator it = da.getIterator();
	
		switch (dt) {
		case Dataset.COMPLEX64:
			ds = DatasetFactory.zeros(da, Dataset.FLOAT32);
			float[] f32data = ((FloatDataset) ds).getData();
			float[] c64data = ((ComplexFloatDataset) da).getData();
	
			for (int i = 0; it.hasNext();) {
				f32data[i++] = (float) Math.atan2(c64data[it.index+1], c64data[it.index]);
			}
			break;
		case Dataset.COMPLEX128:
			ds = DatasetFactory.zeros(da, Dataset.FLOAT64);
			double[] f64data = ((DoubleDataset) ds).getData();
			double[] c128data = ((ComplexDoubleDataset) da).getData();
	
			for (int i = 0; it.hasNext();) {
				f64data[i++] = Math.atan2(c128data[it.index+1], c128data[it.index]);
			}
			break;
		default:
			throw new UnsupportedOperationException("angle does not support this dataset type");
		}
	
		return (AbstractDataset) ds;
	}

	/**
	 * Create a phase only dataset. NB it will contain NaNs if there are any items with zero amplitude
	 * @param a dataset
	 * @param keepZeros if true then zero items are returned as zero rather than NaNs
	 * @return complex dataset where items have unit amplitude
	 */
	public static AbstractDataset phaseAsComplexNumber(final Object a, final boolean keepZeros) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		Dataset ds = null;
		int dt = da.getDtype();
		IndexIterator it = da.getIterator();
	
		switch (dt) {
		case Dataset.COMPLEX64:
			ds = DatasetFactory.zeros(da);
	
			float[] z64data = ((ComplexFloatDataset) ds).getData();
			float[] c64data = ((ComplexFloatDataset) da).getData();
	
			if (keepZeros) {
				for (int i = 0; it.hasNext();) {
					double rr = c64data[it.index];
					double ri = c64data[it.index+1];
					double am = Math.hypot(rr, ri);
					if (am == 0) {
						z64data[i++] = 0;
						z64data[i++] = 0;
					} else {
						z64data[i++] = (float) (rr/am);
						z64data[i++] = (float) (ri/am);
					}
				}
			} else {
				for (int i = 0; it.hasNext();) {
					double rr = c64data[it.index];
					double ri = c64data[it.index+1];
					double am = Math.hypot(rr, ri);
					z64data[i++] = (float) (rr/am);
					z64data[i++] = (float) (ri/am);
				}
			}
			break;
		case Dataset.COMPLEX128:
			ds = DatasetFactory.zeros(da);
	
			double[] z128data = ((ComplexDoubleDataset) ds).getData();
			double[] c128data = ((ComplexDoubleDataset) da).getData();
	
			if (keepZeros) {
				for (int i = 0; it.hasNext();) {
					double rr = c128data[it.index];
					double ri = c128data[it.index+1];
					double am = Math.hypot(rr, ri);
					if (am == 0) {
						z128data[i++] = 0;
						z128data[i++] = 0;
					} else {
						z128data[i++] = rr/am;
						z128data[i++] = ri/am;
					}
				}
			} else {
				for (int i = 0; it.hasNext();) {
					double rr = c128data[it.index];
					double ri = c128data[it.index+1];
					double am = Math.hypot(rr, ri);
					z128data[i++] = rr/am;
					z128data[i++] = ri/am;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("Dataset is not of complex type");
		}
	
		return (AbstractDataset) ds;
	}

	/**
	 * Adds all sets passed in together
	 * 
	 * The first IDataset must cast to AbstractDataset
	 * 
	 * For memory efficiency sake if add(...) is called with a
	 * set of size one, no clone is done, the original object is
	 * returned directly. Otherwise a new data set is returned,
	 * the sum of those passed in.
	 * 
	 * @param sets
	 * @param requireClone
	 * @return sum of collection
	 */
	public static AbstractDataset add(final Collection<IDataset> sets, boolean requireClone) {
		if (sets.isEmpty())
			return null;
		final Iterator<IDataset> it = sets.iterator();
		if (sets.size() == 1)
			return (AbstractDataset) DatasetUtils.convertToDataset(it.next());
	
		Dataset sum = requireClone ? ((Dataset) it.next()).clone() : (Dataset) it.next();
	
		while (it.hasNext()) {
			add(sum, it.next(), sum);
		}
	
		return (AbstractDataset) sum;
	}

	/**
	 * Multiplies all sets passed in together
	 * 
	 * The first IDataset must cast to AbstractDataset
	 * 
	 * @param sets
	 * @param requireClone
	 * @return product of collection
	 */
	public static AbstractDataset multiply(final Collection<IDataset> sets, boolean requireClone) {
		if (sets.isEmpty())
			return null;
		final Iterator<IDataset> it = sets.iterator();
		if (sets.size() == 1)
			return (AbstractDataset) DatasetUtils.convertToDataset(it.next());
		Dataset product = requireClone ? ((Dataset) it.next()).clone() : (Dataset) it.next();
	
		while (it.hasNext()) {
			multiply(product, it.next(), product);
		}
	
		return (AbstractDataset) product;
	}

	/**
	 * Linearly interpolate a value at a point in a 1D dataset. The dataset is considered to have
	 * zero support outside its bounds. Thus points just outside are interpolated from the boundary
	 * value to zero.
	 * @param d input dataset
	 * @param x0 coordinate
	 * @return interpolated value
	 */
	public static double interpolate(final IDataset d, final double x0) {
		assert d.getRank() == 1;

		final int i0 = (int) Math.floor(x0);
		final int e0 = d.getSize() - 1;
		if (i0 < -1 || i0 > e0)
			return 0;

		final double u0 = x0 - i0;

		double r = 0;
		final double f1 = i0 < 0 ? 0 : d.getDouble(i0);
		if (u0 > 0) {
			r = (1 - u0) * f1 + (i0 == e0 ? 0 : u0 * d.getDouble(i0 + 1));
		} else {
			r = f1;
		}
		return r;
	}

	/**
	 * Linearly interpolate a value at a point in a 1D dataset with a mask. The dataset is considered
	 * to have zero support outside its bounds. Thus points just outside are interpolated from the
	 * boundary value to zero.
	 * @param d input dataset
	 * @param m mask dataset
	 * @param x0 coordinate
	 * @return interpolated value
	 */
	public static double interpolate(final IDataset d, final IDataset m, final double x0) {
		assert d.getRank() == 1;
		assert m.getRank() == 1;

		final int i0 = (int) Math.floor(x0);
		final int e0 = d.getSize() - 1;
		if (i0 < -1 || i0 > e0)
			return 0;

		final double u0 = x0 - i0;

		double r = 0;
		final double f1 = i0 < 0 ? 0 : d.getDouble(i0) * m.getDouble(i0);
		if (u0 > 0) {
			r = (1 - u0) * f1 + (i0 == e0 ? 0 : u0 * d.getDouble(i0 + 1) * m.getDouble(i0 + 1));
		} else {
			r = f1;
		}
		return r;
	}

	/**
	 * Linearly interpolate an array of values at a point in a compound 1D dataset. The dataset is
	 * considered to have zero support outside its bounds. Thus points just outside are interpolated
	 * from the boundary value to zero.
	 * @param values interpolated array
	 * @param d input dataset
	 * @param x0 coordinate
	 */
	public static void interpolate(final double[] values, final CompoundDataset d, final double x0) {
		assert d.getRank() == 1;

		final int is = d.getElementsPerItem();
		if (is != values.length)
			throw new IllegalArgumentException("Output array length must match elements in item");
		final double[] f1, f2;
	
		final int i0 = (int) Math.floor(x0);
		final int e0 = d.getSize() - 1;
		if (i0 < -1 || i0 > e0) {
			Arrays.fill(values, 0);
			return;
		}
		final double u0 = x0 - i0;
	
		if (u0 > 0) {
			f1 = new double[is];
			if (i0 >= 0)
				d.getDoubleArray(f1, i0);
			double t = 1 - u0;
			if (i0 == e0) {
				for (int j = 0; j < is; j++)
					values[j] = t * f1[j];
			} else {
				f2 = new double[is];
				d.getDoubleArray(f2, i0 + 1);
				for (int j = 0; j < is; j++)
					values[j] = t * f1[j] + u0 * f2[j];
			}
		} else {
			if (i0 >= 0)
				d.getDoubleArray(values, i0);
			else
				Arrays.fill(values, 0);
		}
	}

	/**
	 * Linearly interpolate a value at a point in a 2D dataset. The dataset is considered to have
	 * zero support outside its bounds. Thus points just outside are interpolated from the boundary
	 * value to zero.
	 * @param d input dataset
	 * @param x0 coordinate
	 * @param x1 coordinate
	 * @return bilinear interpolation
	 */
	public static double interpolate(final IDataset d, final double x0, final double x1) {
		final int[] s = d.getShape();
		assert s.length == 2;
	
		final int e0 = s[0] - 1;
		final int e1 = s[1] - 1;
		final int i0 = (int) Math.floor(x0);
		final int i1 = (int) Math.floor(x1);
		final double u0 = x0 - i0;
		final double u1 = x1 - i1;
		if (i0 < -1 || i0 > e0 || i1 < -1 || i1 > e1)
			return 0;

		// use bilinear interpolation
		double r = 0;
		final double f1, f2, f3, f4;
		f1 = i0 < 0 || i1 < 0 ? 0 : d.getDouble(i0, i1);
		if (u1 > 0) {
			if (u0 > 0) {
				if (i0 == e0) {
					f2 = 0;
					f4 = 0;
					f3 = i1 == e1 ? 0 : d.getDouble(i0, i1 + 1);
				} else {
					f2 = i1 < 0 ? 0 : d.getDouble(i0 + 1, i1);
					if (i1 == e1) {
						f4 = 0;
						f3 = 0;
					} else {
						f4 = d.getDouble(i0 + 1, i1 + 1);
						f3 = i0 < 0 ? 0 : d.getDouble(i0, i1 + 1);
					}
				}
				r = (1 - u0) * (1 - u1) * f1 + u0 * (1 - u1) * f2 + (1 - u0) * u1 * f3 + u0 * u1 * f4;
			} else {
				f3 = i0 < 0 || i1 == e1 ? 0 : d.getDouble(i0, i1 + 1);
				r = (1 - u1) * f1 + u1 * f3;
			}
		} else { // exactly on axis 1
			if (u0 > 0) {
				f2 = i0 == e0 || i1 < 0 ? 0 : d.getDouble(i0 + 1, i1);
				r = (1 - u0) * f1 + u0 * f2;
			} else { // exactly on axis 0
				r = f1;
			}
		}
		return r;
	}

	/**
	 * Linearly interpolate a value at a point in a 2D dataset with a mask. The dataset is considered
	 * to have zero support outside its bounds. Thus points just outside are interpolated from the
	 * boundary value to zero.
	 * @param d input dataset
	 * @param m mask dataset
	 * @param x0 coordinate
	 * @param x1 coordinate
	 * @return bilinear interpolation
	 */
	public static double interpolate(final IDataset d, final IDataset m, final double x0, final double x1) {
		if (m == null)
			return interpolate(d, x0, x1);
	
		final int[] s = d.getShape();
		assert s.length == 2;
		assert m.getRank() == 2;

		final int e0 = s[0] - 1;
		final int e1 = s[1] - 1;
		final int i0 = (int) Math.floor(x0);
		final int i1 = (int) Math.floor(x1);
		final double u0 = x0 - i0;
		final double u1 = x1 - i1;
		if (i0 < -1 || i0 > e0 || i1 < -1 || i1 > e1)
			return 0;

		// use bilinear interpolation
		double r = 0;
		final double f1, f2, f3, f4;
		f1 = i0 < 0 || i1 < 0 ? 0 : d.getDouble(i0, i1) * m.getDouble(i0, i1);
		if (u1 > 0) {
			if (i0 == e0) {
				f2 = 0;
				f4 = 0;
				f3 = i1 == e1 ? 0 : d.getDouble(i0, i1 + 1) * m.getDouble(i0, i1 + 1);
			} else {
				f2 = i1 < 0 ? 0 : d.getDouble(i0 + 1, i1) * m.getDouble(i0 + 1, i1);
				if (i1 == e1) {
					f4 = 0;
					f3 = 0;
				} else {
					f4 = d.getDouble(i0 + 1, i1 + 1) * m.getDouble(i0 + 1, i1 + 1);
					f3 = i0 < 0 ? 0 : d.getDouble(i0, i1 + 1) * m.getDouble(i0, i1 + 1);
				}
			}
			r = (1 - u0) * (1 - u1) * f1 + u0 * (1 - u1) * f2 + (1 - u0) * u1 * f3 + u0 * u1 * f4;
		} else { // exactly on axis 1
			if (u0 > 0) {
				f2 = i0 == e0 || i1 < 0 ? 0 : d.getDouble(i0 + 1, i1) * m.getDouble(i0 + 1, i1);
				r = (1 - u0) * f1 + u0 * f2;
			} else { // exactly on axis 0
				r = f1;
			}
		}
		return r;
	}

	/**
	 * Linearly interpolate an array of values at a point in a compound 2D dataset. The dataset is
	 * considered to have zero support outside its bounds. Thus points just outside are interpolated
	 * from the boundary value to zero.
	 * @param values bilinear interpolated array
	 * @param d
	 * @param x0
	 * @param x1
	 */
	public static void interpolate(final double[] values, final CompoundDataset d, final double x0, final double x1) {
		final int[] s = d.getShapeRef();
		assert s.length == 2;

		final int is = d.getElementsPerItem();
		if (is != values.length)
			throw new IllegalArgumentException("Output array length must match elements in item");

		final int e0 = s[0] - 1;
		final int e1 = s[1] - 1;
		final int i0 = (int) Math.floor(x0);
		final int i1 = (int) Math.floor(x1);
		final double u0 = x0 - i0;
		final double u1 = x1 - i1;
		if (i0 < -1 || i0 > e0 || i1 < -1 || i1 > e1) {
			Arrays.fill(values, 0);
			return;
		}
		// use bilinear interpolation
		double[] f1 = new double[is];
		if (i0 >= 0 && i1 >= 0)
			d.getDoubleArray(f1, i0, i1);
	
		if (u1 > 0) {
			if (u0 > 0) {
				double[] f2 = new double[is];
				double[] f3 = new double[is];
				double[] f4 = new double[is];
				if (i0 != e0) {
					if (i1 != e1)
						d.getDoubleArray(f3, i0 + 1, i1 + 1);
					if (i1 >= 0)
						d.getDoubleArray(f4, i0 + 1, i1);
				}
				if (i0 >= 0 && i1 != e1)
					d.getDoubleArray(f2, i0, i1 + 1);
				final double t0 = 1 - u0;
				final double t1 = 1 - u1;
				final double w1 = t0 * t1;
				final double w2 = t0 * u1;
				final double w3 = u0 * u1;
				final double w4 = u0 * t1;
				for (int j = 0; j < is; j++)
					values[j] = w1 * f1[j] + w2 * f2[j] + w3 * f3[j] + w4 * f4[j];
			} else {
				double[] f2 = new double[is];
				if (i0 >= 0 && i1 != e1)
					d.getDoubleArray(f2, i0, i1 + 1);
				final double t1 = 1 - u1;
				for (int j = 0; j < is; j++)
					values[j] = t1 * f1[j] + u1 * f2[j];
			}
		} else { // exactly on axis 1
			if (u0 > 0) {
				double[] f4 = new double[is];
				if (i0 != e0 && i1 >= 0)
					d.getDoubleArray(f4, i0 + 1, i1);
				final double t0 = 1 - u0;
				for (int j = 0; j < is; j++)
					values[j] = t0 * f1[j] + u0 * f4[j];
			} else { // exactly on axis 0
				if (i0 >= 0 && i1 >= 0)
					d.getDoubleArray(values, i0, i1);
				else
					Arrays.fill(values, 0);
			}
		}
	}

	/**
	 * Linearly interpolate a value at a point in a n-D dataset. The dataset is considered to have
	 * zero support outside its bounds. Thus points just outside are interpolated from the boundary
	 * value to zero. The number of coordinates must match the rank of the dataset.
	 * @param d input dataset
	 * @param x coordinates
	 * @return interpolated value
	 */
	public static double interpolate(final IDataset d, final double... x) {
		return interpolate(d, null, x);
	}

	/**
	 * Linearly interpolate a value at a point in a n-D dataset with a mask. The dataset is considered to have
	 * zero support outside its bounds. Thus points just outside are interpolated from the boundary
	 * value to zero. The number of coordinates must match the rank of the dataset.
	 * @param d input dataset
	 * @param m mask dataset (can be null)
	 * @param x coordinates
	 * @return interpolated value
	 */
	public static double interpolate(final IDataset d, final IDataset m, final double... x) {
		int r = d.getRank();
		if (r != x.length) {
			throw new IllegalArgumentException("Number of coordinates must be equal to rank of dataset");
		}

		switch (r) {
		case 1:
			return m == null ? interpolate(d, x[0]) : interpolate(d, m, x[0]);
		case 2:
			return m == null ? interpolate(d, x[0], x[1]) : interpolate(d, m, x[0], x[1]);
		}

		if (m != null && r != m.getRank()) {
			throw new IllegalArgumentException("Rank of mask dataset must be equal to rank of dataset");
		}

		// now do it iteratively
		int[] l = new int[r];       // lower indexes
		double[] f = new double[r]; // fractions
		for (int i = 0; i < r; i++) {
			double xi = x[i];
			l[i] = (int) Math.floor(xi);
			f[i] = xi - l[i];
		}

		int[] s = d.getShape();
		
		int n = 1 << r;
		double[] results = new double[n];

		// iterate over permutations {l} and {l+1}
		int[] twos = new int[r];
		Arrays.fill(twos, 2);
		PositionIterator it = new PositionIterator(twos);
		int[] ip = it.getPos();
		int j = 0;
		if (m == null) {
			while (it.hasNext()) {
				int[] p = l.clone();
				boolean omit = false;
				for (int i = 0; i < r; i++) {
					int pi = p[i] + ip[i];
					if (pi < 0 || pi >= s[i]) {
						omit = true;
						break;
					}
					p[i] = pi;
				}
				results[j++] = omit ? 0 : d.getDouble(p);
			}
		} else {
			while (it.hasNext()) {
				int[] p = l.clone();
				boolean omit = false;
				for (int i = 0; i < r; i++) {
					int pi = p[i] + ip[i];
					if (pi < 0 || pi >= s[i]) {
						omit = true;
						break;
					}
					p[i] = pi;
				}
				results[j++] = omit ? 0 : d.getDouble(p) * m.getDouble(p);
			}
		}

		// reduce recursively
		for (int i = r - 1; i >= 0; i--) {
			results = combine(results, f[i], 1 << i);
		}
		return results[0];
	}

	private static double[] combine(double[] values, double f, int n) {
		double g = 1 - f;
		double[] results = new double[n];
		for (int j = 0; j < n; j++) {
			int tj = 2 * j;
			results[j] = g * values[tj] + f * values[tj + 1];
		}

		return results;
	}

	/**
	 * Linearly interpolate an array of values at a point in a compound n-D dataset. The dataset is
	 * considered to have zero support outside its bounds. Thus points just outside are interpolated
	 * from the boundary value to zero.
	 * @param values linearly interpolated array
	 * @param d
	 * @param x
	 */
	public static void interpolate(final double[] values, final CompoundDataset d, final double... x) {
		int r = d.getRank();
		if (r != x.length) {
			throw new IllegalArgumentException("Number of coordinates must be equal to rank of dataset");
		}

		switch (r) {
		case 1:
			interpolate(values, d, x[0]);
			return;
		case 2:
			interpolate(values, d, x[0], x[1]);
			return;
		}

		final int is = d.getElementsPerItem();
		if (is != values.length)
			throw new IllegalArgumentException("Output array length must match elements in item");

		// now do it iteratively
		int[] l = new int[r];       // lower indexes
		double[] f = new double[r]; // fractions
		for (int i = 0; i < r; i++) {
			double xi = x[i];
			l[i] = (int) Math.floor(xi);
			f[i] = xi - l[i];
		}

		int[] s = d.getShape();
		
		int n = 1 << r;
		double[][] results = new double[n][is];

		// iterate over permutations {l} and {l+1}
		int[] twos = new int[r];
		Arrays.fill(twos, 2);
		PositionIterator it = new PositionIterator(twos);
		int[] ip = it.getPos();
		int j = 0;
		while (it.hasNext()) {
			int[] p = l.clone();
			boolean omit = false;
			for (int i = 0; i < r; i++) {
				int pi = p[i] + ip[i];
				if (pi < 0 || pi >= s[i]) {
					omit = true;
					break;
				}
				p[i] = pi;
			}
			if (!omit) {
				d.getDoubleArray(results[j++], p);
			}
		}

		// reduce recursively
		for (int i = r - 1; i >= 0; i--) {
			results = combineArray(is, results, f[i], 1 << i);
		}
		for (int k = 0; k < is; k++) {
			values[k] = results[0][k];
		}
	}

	private static double[][] combineArray(int is, double[][] values, double f, int n) {
		double g = 1 - f;
		double[][] results = new double[n][is];
		for (int j = 0; j < n; j++) {
			int tj = 2 * j;
			for (int k = 0; k < is; k++) {
				results[j][k] = g * values[tj][k] + f * values[tj + 1][k];
			}
		}

		return results;
	}

	/**
	 * Linearly interpolate a value at a point in a 1D dataset. The dataset is considered to have
	 * zero support outside its bounds. Thus points just outside are interpolated from the boundary
	 * value to zero.
	 * @param d input dataset
	 * @param x0 coordinate
	 * @return interpolated value
	 * @deprecated Use {@link #interpolate(IDataset, double)}
	 */
	@Deprecated
	public static double getLinear(final IDataset d, final double x0) {
		return interpolate(d, x0);
	}

	/**
	 * Linearly interpolate a value at a point in a compound 1D dataset. The dataset is considered
	 * to have zero support outside its bounds. Thus points just outside are interpolated from the
	 * boundary value to zero.
	 * @param values interpolated array
	 * @param d input dataset
	 * @param x0 coordinate
	 * @deprecated Use {@link #interpolate(double[], CompoundDataset, double)}
	 */
	@Deprecated
	public static void getLinear(final double[] values, final CompoundDataset d, final double x0) {
		interpolate(values, d, x0);
	}

	/**
	 * Interpolated a value from 2D dataset
	 * @param d input dataset
	 * @param x0 coordinate
	 * @param x1 coordinate
	 * @return bilinear interpolation
	 * @deprecated Use {@link #interpolate(IDataset, double, double)}
	 */
	@Deprecated
	public static double getBilinear(final IDataset d, final double x0, final double x1) {
		return interpolate(d, x0, x1);
	}

	/**
	 * Interpolated a value from 2D dataset with mask
	 * @param d input dataset
	 * @param m mask dataset
	 * @param x0 coordinate
	 * @param x1 coordinate
	 * @return bilinear interpolation
	 * @deprecated Use {@link #interpolate(IDataset, IDataset, double, double)}
	 */
	@Deprecated
	public static double getBilinear(final IDataset d, final IDataset m, final double x0, final double x1) {
		return interpolate(d, m, x0, x1);
	}

	/**
	 * Interpolated a value from 2D compound dataset
	 * @param values bilinear interpolated array
	 * @param d
	 * @param x0
	 * @param x1
	 * @deprecated Use {@link #interpolate(double[], CompoundDataset, double, double)}
	 */
	@Deprecated
	public static void getBilinear(final double[] values, final CompoundDataset d, final double x0, final double x1) {
		interpolate(values, d, x0, x1);
	}

	/**
	 * generate binomial coefficients with negative sign:
	 * <p>
	 * <pre>
	 *  (-1)^i n! / ( i! (n-i)! )
	 * </pre>
	 * @param n
	 * @return array of coefficients
	 */
	private static int[] bincoeff(final int n) {
		final int[] b = new int[n+1];
		final int hn = n/2;

		int bc = 1;
		b[0] = bc;
		for (int i = 1; i <= hn; i++) {
			bc = -(bc*(n-i+1))/i;
			b[i] = bc;
		}
		if (n % 2 != 0) {
			for (int i = hn+1; i <= n; i++) {
				b[i] = -b[n-i];
			}
		} else {
			for (int i = hn+1; i <= n; i++) {
				b[i] = b[n-i];
			}
		}
		return b;
	}

	/**
	 * 1st order discrete difference of dataset along flattened dataset using finite difference
	 * @param a is 1d dataset
	 * @param out is 1d dataset
	 */
	private static void difference(final Dataset a, final Dataset out) {
		final int isize = a.getElementsPerItem();

		final IndexIterator it = a.getIterator();
		if (!it.hasNext())
			return;
		int oi = it.index;

		switch (a.getDtype()) {
		case Dataset.INT8:
			final byte[] i8data = ((ByteDataset) a).data;
			final byte[] oi8data = ((ByteDataset) out).getData();
			for (int i = 0; it.hasNext();) {
				oi8data[i++] = (byte) (i8data[it.index] - i8data[oi]);
				oi = it.index;
			}
			break;
		case Dataset.INT16:
			final short[] i16data = ((ShortDataset) a).data;
			final short[] oi16data = ((ShortDataset) out).getData();
			for (int i = 0; it.hasNext();) {
				oi16data[i++] = (short) (i16data[it.index] - i16data[oi]);
				oi = it.index;
			}
			break;
		case Dataset.INT32:
			final int[] i32data = ((IntegerDataset) a).data;
			final int[] oi32data = ((IntegerDataset) out).getData();
			for (int i = 0; it.hasNext();) {
				oi32data[i++] = i32data[it.index] - i32data[oi];
				oi = it.index;
			}
			break;
		case Dataset.INT64:
			final long[] i64data = ((LongDataset) a).data;
			final long[] oi64data = ((LongDataset) out).getData();
			for (int i = 0; it.hasNext();) {
				oi64data[i++] = i64data[it.index] - i64data[oi];
				oi = it.index;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] ai8data = ((CompoundByteDataset) a).data;
			final byte[] oai8data = ((CompoundByteDataset) out).getData();
			for (int i = 0; it.hasNext();) {
				for (int k = 0; k < isize; k++) {
					oai8data[i++] = (byte) (ai8data[it.index + k] - ai8data[oi++]);
				}
				oi = it.index;
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] ai16data = ((CompoundShortDataset) a).data;
			final short[] oai16data = ((CompoundShortDataset) out).getData();
			for (int i = 0; it.hasNext();) {
				for (int k = 0; k < isize; k++) {
					oai16data[i++] = (short) (ai16data[it.index + k] - ai16data[oi++]);
				}
				oi = it.index;
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] ai32data = ((CompoundIntegerDataset) a).data;
			final int[] oai32data = ((CompoundIntegerDataset) out).getData();
			for (int i = 0; it.hasNext();) {
				for (int k = 0; k < isize; k++) {
					oai32data[i++] = ai32data[it.index + k] - ai32data[oi++];
				}
				oi = it.index;
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] ai64data = ((CompoundLongDataset) a).data;
			final long[] oai64data = ((CompoundLongDataset) out).getData();
			for (int i = 0; it.hasNext();) {
				for (int k = 0; k < isize; k++) {
					oai64data[i++] = ai64data[it.index + k] - ai64data[oi++];
				}
				oi = it.index;
			}
			break;
		case Dataset.FLOAT32:
			final float[] f32data = ((FloatDataset) a).data;
			final float[] of32data = ((FloatDataset) out).getData();
			for (int i = 0; it.hasNext();) {
				of32data[i++] = f32data[it.index] - f32data[oi];
				oi = it.index;
			}
			break;
		case Dataset.FLOAT64:
			final double[] f64data = ((DoubleDataset) a).data;
			final double[] of64data = ((DoubleDataset) out).getData();
			for (int i = 0; it.hasNext();) {
				of64data[i++] = f64data[it.index] - f64data[oi];
				oi = it.index;
			}
			break;
		case Dataset.COMPLEX64:
			final float[] c64data = ((ComplexFloatDataset) a).data;
			final float[] oc64data = ((ComplexFloatDataset) out).getData();
			for (int i = 0; it.hasNext();) {
				oc64data[i++] = c64data[it.index] - c64data[oi];
				oc64data[i++] = c64data[it.index + 1] - c64data[oi + 1];
				oi = it.index;
			}
			break;
		case Dataset.COMPLEX128:
			final double[] c128data = ((ComplexDoubleDataset) a).data;
			final double[] oc128data = ((ComplexDoubleDataset) out).getData();
			for (int i = 0; it.hasNext();) {
				oc128data[i++] = c128data[it.index] - c128data[oi];
				oc128data[i++] = c128data[it.index + 1] - c128data[oi + 1];
				oi = it.index;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] af32data = ((CompoundFloatDataset) a).data;
			final float[] oaf32data = ((CompoundFloatDataset) out).getData();
			for (int i = 0; it.hasNext();) {
				for (int k = 0; k < isize; k++) {
					oaf32data[i++] = af32data[it.index + k] - af32data[oi++];
				}
				oi = it.index;
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] af64data = ((CompoundDoubleDataset) a).data;
			final double[] oaf64data = ((CompoundDoubleDataset) out).getData();
			for (int i = 0; it.hasNext();) {
				for (int k = 0; k < isize; k++) {
					oaf64data[i++] = af64data[it.index + k] - af64data[oi++];
				}
				oi = it.index;
			}
			break;
		default:
			throw new UnsupportedOperationException("difference does not support this dataset type");
		}
	}

	/**
	 * Get next set of indexes
	 * @param it
	 * @param indexes
	 * @return true if there is more
	 */
	private static boolean nextIndexes(IndexIterator it, int[] indexes) {
		if (!it.hasNext())
			return false;
		int m = indexes.length;
		int i = 0;
		for (i = 0; i < m - 1; i++) {
			indexes[i] = indexes[i+1];
		}
		indexes[i] = it.index;
		return true;
	}

	
	/**
	 * General order discrete difference of dataset along flattened dataset using finite difference
	 * @param a is 1d dataset
	 * @param out is 1d dataset
	 * @param n order of difference
	 */
	private static void difference(final Dataset a, final Dataset out, final int n) {
		if (n == 1) {
			difference(a, out);
			return;
		}

		final int isize = a.getElementsPerItem();

		final int[] coeff = bincoeff(n);
		final int m = n + 1;
		final int[] indexes = new int[m]; // store for index values

		final IndexIterator it = a.getIterator();
		for (int i = 0; i < n; i++) {
			indexes[i] = it.index;
			it.hasNext();
		}
		indexes[n] = it.index;

		switch (a.getDtype()) {
		case Dataset.INT8:
			final byte[] i8data = ((ByteDataset) a).data;
			final byte[] oi8data = ((ByteDataset) out).getData();
			for (int i = 0; nextIndexes(it, indexes);) {
				int ox = 0;
				for (int j = 0; j < m; j++) {
					ox += i8data[indexes[j]] * coeff[j];
				}
				oi8data[i++] = (byte) ox;
			}
			break;
		case Dataset.INT16:
			final short[] i16data = ((ShortDataset) a).data;
			final short[] oi16data = ((ShortDataset) out).getData();
			for (int i = 0; nextIndexes(it, indexes);) {
				int ox = 0;
				for (int j = 0; j < m; j++) {
					ox += i16data[indexes[j]] * coeff[j];
				}
				oi16data[i++] = (short) ox;
			}
			break;
		case Dataset.INT32:
			final int[] i32data = ((IntegerDataset) a).data;
			final int[] oi32data = ((IntegerDataset) out).getData();
			for (int i = 0; nextIndexes(it, indexes);) {
				int ox = 0;
				for (int j = 0; j < m; j++) {
					ox += i32data[indexes[j]] * coeff[j];
				}
				oi32data[i++] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] i64data = ((LongDataset) a).data;
			final long[] oi64data = ((LongDataset) out).getData();
			for (int i = 0; nextIndexes(it, indexes);) {
				long ox = 0;
				for (int j = 0; j < m; j++) {
					ox += i64data[indexes[j]] * coeff[j];
				}
				oi64data[i++] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] ai8data = ((CompoundByteDataset) a).data;
			final byte[] oai8data = ((CompoundByteDataset) out).getData();
			int[] box = new int[isize];
			for (int i = 0; nextIndexes(it, indexes);) {
				Arrays.fill(box, 0);
				for (int j = 0; j < m; j++) {
					double c = coeff[j];
					int l = indexes[j];
					for (int k = 0; k < isize; k++) {
						box[k] += ai8data[l++] * c;
					}
				}
				for (int k = 0; k < isize; k++) {
					oai8data[i++] = (byte) box[k];
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] ai16data = ((CompoundShortDataset) a).data;
			final short[] oai16data = ((CompoundShortDataset) out).getData();
			int[] sox = new int[isize];
			for (int i = 0; nextIndexes(it, indexes);) {
				Arrays.fill(sox, 0);
				for (int j = 0; j < m; j++) {
					double c = coeff[j];
					int l = indexes[j];
					for (int k = 0; k < isize; k++) {
						sox[k] += ai16data[l++] * c;
					}
				}
				for (int k = 0; k < isize; k++) {
					oai16data[i++] = (short) sox[k];
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] ai32data = ((CompoundIntegerDataset) a).data;
			final int[] oai32data = ((CompoundIntegerDataset) out).getData();
			int[] iox = new int[isize];
			for (int i = 0; nextIndexes(it, indexes);) {
				Arrays.fill(iox, 0);
				for (int j = 0; j < m; j++) {
					double c = coeff[j];
					int l = indexes[j];
					for (int k = 0; k < isize; k++) {
						iox[k] += ai32data[l++] * c;
					}
				}
				for (int k = 0; k < isize; k++) {
					oai32data[i++] = iox[k];
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] ai64data = ((CompoundLongDataset) a).data;
			final long[] oai64data = ((CompoundLongDataset) out).getData();
			long[] lox = new long[isize];
			for (int i = 0; nextIndexes(it, indexes);) {
				Arrays.fill(lox, 0);
				for (int j = 0; j < m; j++) {
					double c = coeff[j];
					int l = indexes[j];
					for (int k = 0; k < isize; k++) {
						lox[k] += ai64data[l++] * c;
					}
				}
				for (int k = 0; k < isize; k++) {
					oai64data[i++] = lox[k];
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] f32data = ((FloatDataset) a).data;
			final float[] of32data = ((FloatDataset) out).getData();
			for (int i = 0; nextIndexes(it, indexes);) {
				float ox = 0;
				for (int j = 0; j < m; j++) {
					ox += f32data[indexes[j]] * coeff[j];
				}
				of32data[i++] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] f64data = ((DoubleDataset) a).data;
			final double[] of64data = ((DoubleDataset) out).getData();
			for (int i = 0; nextIndexes(it, indexes);) {
				double ox = 0;
				for (int j = 0; j < m; j++) {
					ox += f64data[indexes[j]] * coeff[j];
				}
				of64data[i++] = ox;
			}
			break;
		case Dataset.COMPLEX64:
			final float[] c64data = ((ComplexFloatDataset) a).data;
			final float[] oc64data = ((ComplexFloatDataset) out).getData();
			for (int i = 0; nextIndexes(it, indexes);) {
				float ox = 0;
				float oy = 0;
				for (int j = 0; j < m; j++) {
					int l = indexes[j];
					ox += c64data[l++] * coeff[j];
					oy += c64data[l] * coeff[j];
				}
				oc64data[i++] = ox;
				oc64data[i++] = oy;
			}
			break;
		case Dataset.COMPLEX128:
			final double[] c128data = ((ComplexDoubleDataset) a).data;
			final double[] oc128data = ((ComplexDoubleDataset) out).getData();
			for (int i = 0; nextIndexes(it, indexes);) {
				double ox = 0;
				double oy = 0;
				for (int j = 0; j < m; j++) {
					int l = indexes[j];
					ox += c128data[l++] * coeff[j];
					oy += c128data[l] * coeff[j];
				}
				oc128data[i++] = ox;
				oc128data[i++] = oy;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] af32data = ((CompoundFloatDataset) a).data;
			final float[] oaf32data = ((CompoundFloatDataset) out).getData();
			float[] fox = new float[isize];
			for (int i = 0; nextIndexes(it, indexes);) {
				Arrays.fill(fox, 0);
				for (int j = 0; j < m; j++) {
					double c = coeff[j];
					int l = indexes[j];
					for (int k = 0; k < isize; k++) {
						fox[k] += af32data[l++] * c;
					}
				}
				for (int k = 0; k < isize; k++) {
					oaf32data[i++] = fox[k];
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] af64data = ((CompoundDoubleDataset) a).data;
			final double[] oaf64data = ((CompoundDoubleDataset) out).getData();
			double[] dox = new double[isize];
			for (int i = 0; nextIndexes(it, indexes);) {
				Arrays.fill(dox, 0);
				for (int j = 0; j < m; j++) {
					double c = coeff[j];
					int l = indexes[j];
					for (int k = 0; k < isize; k++) {
						dox[k] += af64data[l++] * c;
					}
				}
				for (int k = 0; k < isize; k++) {
					oaf64data[i++] = dox[k];
				}
			}
			break;
		default:
			throw new UnsupportedOperationException("difference does not support multiple-element dataset");
		}
	}

	/**
	 * Discrete difference of dataset along axis using finite difference
	 * @param a
	 * @param n order of difference
	 * @param axis
	 * @return difference
	 */
	public static AbstractDataset difference(Dataset a, final int n, int axis) {
		Dataset ds;
		final int dt = a.getDtype();
		final int rank = a.getRank();
		final int is = a.getElementsPerItem();

		if (axis < 0) {
			axis += rank;
		}
		if (axis < 0 || axis >= rank) {
			throw new IllegalArgumentException("Axis is out of range");
		}
		
		int[] nshape = a.getShape();
		if (nshape[axis] <= n) {
			nshape[axis] = 0;
			return (AbstractDataset) DatasetFactory.zeros(is, nshape, dt);
		}

		nshape[axis] -= n;
		ds = DatasetFactory.zeros(is, nshape, dt);
		if (rank == 1) {
			difference(DatasetUtils.convertToAbstractDataset(a), ds, n);
		} else {
			final Dataset src = DatasetFactory.zeros(is, new int[] { a.getShapeRef()[axis] }, dt);
			final Dataset dest = DatasetFactory.zeros(is, new int[] { nshape[axis] }, dt);
			final PositionIterator pi = a.getPositionIterator(axis);
			final int[] pos = pi.getPos();
			final boolean[] hit = pi.getOmit();
			while (pi.hasNext()) {
				a.copyItemsFromAxes(pos, hit, src);
				difference(src, dest, n);
				ds.setItemsOnAxes(pos, hit, dest.getBuffer());
			}
		}

		return (AbstractDataset) ds;
	}

	private static double SelectedMean(Dataset data, int Min, int Max) {

		double result = 0.0;
		for (int i = Min, imax = data.getSize(); i <= Max; i++) {
			// clip i appropriately, imagine that effectively the two ends continue
			// straight out.
			int pos = i;
			if (pos < 0) {
				pos = 0;
			} else if (pos >= imax) {
				pos = imax - 1;
			}
			result += data.getElementDoubleAbs(pos);
		}

		// now the sum is complete, average the values.
		result /= (Max - Min) + 1;
		return result;
	}

	private static void SelectedMeanArray(double[] out, Dataset data, int Min, int Max) {
		final int isize = out.length;
		for (int j = 0; j < isize; j++)
			out[j] = 0.;

		for (int i = Min, imax = data.getSize(); i <= Max; i++) {
			// clip i appropriately, imagine that effectively the two ends continue
			// straight out.
			int pos = i*isize;
			if (pos < 0) {
				pos = 0;
			} else if (pos >= imax) {
				pos = imax - isize;
			}
			for (int j = 0; j < isize; j++)
				out[j] += data.getElementDoubleAbs(pos+j);
		}

		// now the sum is complete, average the values.
		double norm = 1./ (Max - Min + 1.);
		for (int j = 0; j < isize; j++)
			out[j] *= norm;

	}

	/**
	 * Calculates the derivative of a line described by two datasets (x,y) given a spread of n either
	 * side of the point
	 * 
	 * @param x
	 *            The x values of the function to take the derivative of.
	 * @param y
	 *            The y values of the function to take the derivative of.
	 * @param n
	 *            The spread the derivative is calculated from, i.e. the
	 *            smoothing, the higher the value, the more smoothing occurs.
	 * @return A dataset which contains all the derivative point for point.
	 */
	public static AbstractDataset derivative(Dataset x, Dataset y, int n) {
		if (x.getRank() != 1 || y.getRank() != 1) {
			throw new IllegalArgumentException("Only one dimensional dataset supported");
		}
		if (y.getSize() > x.getSize()) {
			throw new IllegalArgumentException("Length of x dataset should be greater than or equal to y's");
		}
		int dtype = y.getDtype();
		Dataset result;
		switch (dtype) {
		case Dataset.BOOL:
		case Dataset.INT8:
		case Dataset.INT16:
		case Dataset.ARRAYINT8:
		case Dataset.ARRAYINT16:
			result = DatasetFactory.zeros(y, Dataset.FLOAT32);
			break;
		case Dataset.INT32:
		case Dataset.INT64:
		case Dataset.ARRAYINT32:
		case Dataset.ARRAYINT64:
			result = DatasetFactory.zeros(y, Dataset.FLOAT64);
			break;
		case Dataset.FLOAT32:
		case Dataset.FLOAT64:
		case Dataset.COMPLEX64:
		case Dataset.COMPLEX128:
		case Dataset.ARRAYFLOAT32:
		case Dataset.ARRAYFLOAT64:
			result = DatasetFactory.zeros(y);
			break;
		default:
			throw new UnsupportedOperationException("derivative does not support multiple-element dataset");
		}

		final int isize = y.getElementsPerItem();
		if (isize == 1) {
			for (int i = 0, imax = x.getSize(); i < imax; i++) {
				double LeftValue = SelectedMean(y, i - n, i - 1);
				double RightValue = SelectedMean(y, i + 1, i + n);
				double LeftPosition = SelectedMean(x, i - n, i - 1);
				double RightPosition = SelectedMean(x, i + 1, i + n);

				// now the values and positions are calculated, the derivative can be
				// calculated.
				result.set(((RightValue - LeftValue) / (RightPosition - LeftPosition)), i);
			}
		} else {
			double[] leftValues = new double[isize];
			double[] rightValues = new double[isize];
			for (int i = 0, imax = x.getSize(); i < imax; i++) {
				SelectedMeanArray(leftValues, y, i - n, i - 1);
				SelectedMeanArray(rightValues, y, i + 1, i + n);
				double delta = SelectedMean(x, i - n, i - 1);
				delta = 1./(SelectedMean(x, i + 1, i + n) - delta);
				for (int j = 0; j < isize; j++) {
					rightValues[j] -= leftValues[j];
					rightValues[j] *= delta;
				}
				result.set(rightValues, i);
			}
		}

		// set the name based on the changes made
		result.setName(y.getName() + "'");

		return (AbstractDataset) result;
	}

	/**
	 * Discrete difference of dataset along axis using finite central difference
	 * @param a
	 * @param axis
	 * @return difference
	 */
	public static AbstractDataset centralDifference(Dataset a, int axis) {
		Dataset ds;
		final int dt = a.getDtype();
		final int rank = a.getRank();
		final int is = a.getElementsPerItem();

		if (axis < 0) {
			axis += rank;
		}
		if (axis < 0 || axis >= rank) {
			throw new IllegalArgumentException("Axis is out of range");
		}

		final int len = a.getShapeRef()[axis];
		if (len < 2) {
			throw new IllegalArgumentException("Dataset should have a size > 1 along given axis");
		}
		ds = DatasetFactory.zeros(is, a.getShapeRef(), dt);
		if (rank == 1) {
			centralDifference(a, ds);
		} else {
			final Dataset src = DatasetFactory.zeros(is, new int[] { len }, dt);
			final Dataset dest = DatasetFactory.zeros(is, new int[] { len }, dt);
			final PositionIterator pi = a.getPositionIterator(axis);
			final int[] pos = pi.getPos();
			final boolean[] hit = pi.getOmit();
			while (pi.hasNext()) {
				a.copyItemsFromAxes(pos, hit, src);
				centralDifference(src, dest);
				ds.setItemsOnAxes(pos, hit, dest.getBuffer());
			}
		}

		return (AbstractDataset) ds;
	}

	/**
	 * 1st order discrete difference of dataset along flattened dataset using central difference
	 * @param a is 1d dataset
	 * @param out is 1d dataset
	 */
	private static void centralDifference(final Dataset a, final Dataset out) {
		final int isize = a.getElementsPerItem();
		final int dt = a.getDtype();

		final int nlen = (out.getShapeRef()[0] - 1)*isize;
		if (nlen < 1) {
			throw new IllegalArgumentException("Dataset should have a size > 1 along given axis");
		}
		final IndexIterator it = a.getIterator();
		if (!it.hasNext())
			return;
		int oi = it.index;
		if (!it.hasNext())
			return;
		int pi = it.index;

		switch (dt) {
		case Dataset.INT8:
			final byte[] i8data = ((ByteDataset) a).data;
			final byte[] oi8data = ((ByteDataset) out).getData();
			oi8data[0] = (byte) (i8data[pi] - i8data[oi]);
			for (int i = 1; it.hasNext(); i++) {
				oi8data[i] = (byte) ((i8data[it.index] - i8data[oi])/2);
				oi = pi;
				pi = it.index;
			}
			oi8data[nlen] = (byte) (i8data[pi] - i8data[oi]);
			break;
		case Dataset.INT16:
			final short[] i16data = ((ShortDataset) a).data;
			final short[] oi16data = ((ShortDataset) out).getData();
			oi16data[0] = (short) (i16data[pi] - i16data[oi]);
			for (int i = 1; it.hasNext(); i++) {
				oi16data[i] = (short) ((i16data[it.index] - i16data[oi])/2);
				oi = pi;
				pi = it.index;
			}
			oi16data[nlen] = (short) (i16data[pi] - i16data[oi]);
			break;
		case Dataset.INT32:
			final int[] i32data = ((IntegerDataset) a).data;
			final int[] oi32data = ((IntegerDataset) out).getData();
			oi32data[0] = i32data[pi] - i32data[oi];
			for (int i = 1; it.hasNext(); i++) {
				oi32data[i] = (i32data[it.index] - i32data[oi])/2;
				oi = pi;
				pi = it.index;
			}
			oi32data[nlen] = i32data[pi] - i32data[oi];
			break;
		case Dataset.INT64:
			final long[] i64data = ((LongDataset) a).data;
			final long[] oi64data = ((LongDataset) out).getData();
			oi64data[0] = i64data[pi] - i64data[oi];
			for (int i = 1; it.hasNext(); i++) {
				oi64data[i] = (i64data[it.index] - i64data[oi])/2;
				oi = pi;
				pi = it.index;
			}
			oi64data[nlen] = i64data[pi] - i64data[oi];
			break;
		case Dataset.ARRAYINT8:
			final byte[] ai8data = ((CompoundByteDataset) a).data;
			final byte[] oai8data = ((CompoundByteDataset) out).getData();
			for (int k = 0; k < isize; k++) {
				oai8data[k] = (byte) (ai8data[pi+k] - ai8data[oi+k]);
			}
			for (int i = isize; it.hasNext();) {
				int l = it.index;
				for (int k = 0; k < isize; k++) {
					oai8data[i++] = (byte) ((ai8data[l++] - ai8data[oi++])/2);
				}
				oi = pi;
				pi = it.index;
			}
			for (int k = 0; k < isize; k++) {
				oai8data[nlen+k] = (byte) (ai8data[pi+k] - ai8data[oi+k]);
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] ai16data = ((CompoundShortDataset) a).data;
			final short[] oai16data = ((CompoundShortDataset) out).getData();
			for (int k = 0; k < isize; k++) {
				oai16data[k] = (short) (ai16data[pi+k] - ai16data[oi+k]);
			}
			for (int i = isize; it.hasNext();) {
				int l = it.index;
				for (int k = 0; k < isize; k++) {
					oai16data[i++] = (short) ((ai16data[l++] - ai16data[oi++])/2);
				}
				oi = pi;
				pi = it.index;
			}
			for (int k = 0; k < isize; k++) {
				oai16data[nlen+k] = (short) (ai16data[pi+k] - ai16data[oi+k]);
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] ai32data = ((CompoundIntegerDataset) a).data;
			final int[] oai32data = ((CompoundIntegerDataset) out).getData();
			for (int k = 0; k < isize; k++) {
				oai32data[k] = ai32data[pi+k] - ai32data[oi+k];
			}
			for (int i = isize; it.hasNext();) {
				int l = it.index;
				for (int k = 0; k < isize; k++) {
					oai32data[i++] = (ai32data[l++] - ai32data[oi++])/2;
				}
				oi = pi;
				pi = it.index;
			}
			for (int k = 0; k < isize; k++) {
				oai32data[nlen+k] = ai32data[pi+k] - ai32data[oi+k];
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] ai64data = ((CompoundLongDataset) a).data;
			final long[] oai64data = ((CompoundLongDataset) out).getData();
			for (int k = 0; k < isize; k++) {
				oai64data[k] = ai64data[pi+k] - ai64data[oi+k];
			}
			for (int i = isize; it.hasNext();) {
				int l = it.index;
				for (int k = 0; k < isize; k++) {
					oai64data[i++] = (ai64data[l++] - ai64data[oi++])/2;
				}
				oi = pi;
				pi = it.index;
			}
			for (int k = 0; k < isize; k++) {
				oai64data[nlen+k] = ai64data[pi+k] - ai64data[oi+k];
			}
			break;
		case Dataset.FLOAT32:
			final float[] f32data = ((FloatDataset) a).data;
			final float[] of32data = ((FloatDataset) out).getData();
			of32data[0] = f32data[pi] - f32data[oi];
			for (int i = 1; it.hasNext(); i++) {
				of32data[i] = (f32data[it.index] - f32data[oi])*0.5f;
				oi = pi;
				pi = it.index;
			}
			of32data[nlen] = f32data[pi] - f32data[oi];
			break;
		case Dataset.FLOAT64:
			final double[] f64data = ((DoubleDataset) a).data;
			final double[] of64data = ((DoubleDataset) out).getData();
			of64data[0] = f64data[pi] - f64data[oi];
			for (int i = 1; it.hasNext(); i++) {
				of64data[i] = (f64data[it.index] - f64data[oi])*0.5f;
				oi = pi;
				pi = it.index;
			}
			of64data[nlen] = f64data[pi] - f64data[oi];
			break;
		case Dataset.COMPLEX64:
			final float[] c64data = ((ComplexFloatDataset) a).data;
			final float[] oc64data = ((ComplexFloatDataset) out).getData();
			oc64data[0] = c64data[pi] - c64data[oi];
			oc64data[1] = c64data[pi+1] - c64data[oi+1];
			for (int i = 2; it.hasNext();) {
				oc64data[i++] = (c64data[it.index] - c64data[oi++])*0.5f;
				oc64data[i++] = (c64data[it.index + 1] - c64data[oi])*0.5f;
				oi = pi;
				pi = it.index;
			}
			oc64data[nlen] = c64data[pi] - c64data[oi];
			oc64data[nlen+1] = c64data[pi+1] - c64data[oi+1];
			break;
		case Dataset.COMPLEX128:
			final double[] c128data = ((ComplexDoubleDataset) a).data;
			final double[] oc128data = ((ComplexDoubleDataset) out).getData();
			oc128data[0] = c128data[pi] - c128data[oi];
			oc128data[1] = c128data[pi+1] - c128data[oi+1];
			for (int i = 2; it.hasNext();) {
				oc128data[i++] = (c128data[it.index] - c128data[oi++])*0.5f;
				oc128data[i++] = (c128data[it.index + 1] - c128data[oi])*0.5f;
				oi = pi;
				pi = it.index;
			}
			oc128data[nlen] = c128data[pi] - c128data[oi];
			oc128data[nlen+1] = c128data[pi+1] - c128data[oi+1];
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] af32data = ((CompoundFloatDataset) a).data;
			final float[] oaf32data = ((CompoundFloatDataset) out).getData();
			for (int k = 0; k < isize; k++) {
				oaf32data[k] = af32data[pi+k] - af32data[oi+k];
			}
			for (int i = isize; it.hasNext();) {
				int l = it.index;
				for (int k = 0; k < isize; k++) {
					oaf32data[i++] = (af32data[l++] - af32data[oi++])*0.5f;
				}
				oi = pi;
				pi = it.index;
			}
			for (int k = 0; k < isize; k++) {
				oaf32data[nlen+k] = af32data[pi+k] - af32data[oi+k];
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] af64data = ((CompoundDoubleDataset) a).data;
			final double[] oaf64data = ((CompoundDoubleDataset) out).getData();
			for (int k = 0; k < isize; k++) {
				oaf64data[k] = af64data[pi+k] - af64data[oi+k];
			}
			for (int i = isize; it.hasNext();) {
				int l = it.index;
				for (int k = 0; k < isize; k++) {
					oaf64data[i++] = (af64data[l++] - af64data[oi++])*0.5;
				}
				oi = pi;
				pi = it.index;
			}
			for (int k = 0; k < isize; k++) {
				oaf64data[nlen+k] = af64data[pi+k] - af64data[oi+k];
			}
			break;
		default:
			throw new UnsupportedOperationException("difference does not support this dataset type");
		}
	}

	/**
	 * Calculate gradient (or partial derivatives) by central difference
	 * @param y
	 * @param x one or more datasets for dependent variables
	 * @return a list of datasets (one for each dimension in y)
	 */
	public static List<AbstractDataset> gradient(Dataset y, Dataset... x) {
		final int rank = y.getRank();

		if (x.length > 0) {
			if (x.length != rank) {
				throw new IllegalArgumentException("Number of dependent datasets must be equal to rank of first argument");
			}
			for (int a = 0; a < rank; a++) {
				int rx = x[a].getRank();
				if (rx != rank && rx != 1) {
					throw new IllegalArgumentException("Dependent datasets must be 1-D or match rank of first argument");
				}
				if (rx == 1) {
					if (y.getShapeRef()[a] != x[a].getShapeRef()[0]) {
						throw new IllegalArgumentException("Length of dependent dataset must match axis length");
					}
				} else {
					y.checkCompatibility(x[a]);
				}
			}
		}

		List<AbstractDataset> grad = new ArrayList<AbstractDataset>(rank);

		for (int a = 0; a < rank; a++) {
			AbstractDataset g = centralDifference(y, a);
			grad.add(g);
		}

		if (x.length > 0) {
			for (int a = 0; a < rank; a++) {
				AbstractDataset g = grad.get(a);
				Dataset dx = x[a];
				int r = dx.getRank();
				if (r == rank) {
					g.idivide(centralDifference(dx, a));
				} else {
					final int dt = dx.getDtype();
					final int is = dx.getElementsPerItem();
					final Dataset bdx = DatasetFactory.zeros(is, y.getShapeRef(), dt);
					final PositionIterator pi = y.getPositionIterator(a);
					final int[] pos = pi.getPos();
					final boolean[] hit = pi.getOmit();
					dx = centralDifference(dx, 0);

					while (pi.hasNext()) {
						bdx.setItemsOnAxes(pos, hit, dx.getBuffer());
					}
					g.idivide(bdx);
				}
			}
		}
		return grad;
	}


// Start of generated code - see functions.txt and generatefunctions.py
	/**
	 * add operator
	 * @param a
	 * @param b
	 * @return a + b, addition of a and b
	 */
	public static AbstractDataset add(final Object a, final Object b) {
		return add(a, b, null);
	}

	/**
	 * add operator
	 * @param a
	 * @param b
	 * @param o output can be null - in which case, a new dataset is created
	 * @return a + b, addition of a and b
	 */
	public static AbstractDataset add(final Object a, final Object b, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final Dataset db = b instanceof Dataset ? (Dataset) b : DatasetFactory.createFromObject(b);
		final BroadcastIterator it = new BroadcastIterator(da, db, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				byte ox;
				ox = (byte) (iax + ibx);
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				short ox;
				ox = (short) (iax + ibx);
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				long ox;
				ox = (iax + ibx);
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				int ox;
				ox = (int) (iax + ibx);
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					byte ox;
					ox = (byte) (iax + ibx);
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					byte ox;
					ox = (byte) (iax + ibx);
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (byte) (iax + ibx);
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					byte ox;
					ox = (byte) (iax + ibx);
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (byte) (iax + ibx);
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					byte ox;
					ox = (byte) (iax + ibx);
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (byte) (iax + ibx);
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					short ox;
					ox = (short) (iax + ibx);
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					short ox;
					ox = (short) (iax + ibx);
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (short) (iax + ibx);
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					short ox;
					ox = (short) (iax + ibx);
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (short) (iax + ibx);
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					short ox;
					ox = (short) (iax + ibx);
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (short) (iax + ibx);
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					long ox;
					ox = (iax + ibx);
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					long ox;
					ox = (iax + ibx);
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (iax + ibx);
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					long ox;
					ox = (iax + ibx);
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (iax + ibx);
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					long ox;
					ox = (iax + ibx);
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (iax + ibx);
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					int ox;
					ox = (int) (iax + ibx);
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					int ox;
					ox = (int) (iax + ibx);
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (int) (iax + ibx);
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					int ox;
					ox = (int) (iax + ibx);
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (int) (iax + ibx);
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					int ox;
					ox = (int) (iax + ibx);
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (int) (iax + ibx);
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			it.setDoubleOutput(true);

			while (it.hasNext()) {
				final double iax = it.aDouble;
				final double ibx = it.bDouble;
				float ox;
				ox = (float) (iax + ibx);
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			it.setDoubleOutput(true);

			while (it.hasNext()) {
				final double iax = it.aDouble;
				final double ibx = it.bDouble;
				double ox;
				ox = (iax + ibx);
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					float ox;
					ox = (float) (iax + ibx);
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					double ibx = it.bDouble;
					float ox;
					ox = (float) (iax + ibx);
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (float) (iax + ibx);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					double iax = it.aDouble;
					final double ibx = it.bDouble;
					float ox;
					ox = (float) (iax + ibx);
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ox = (float) (iax + ibx);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					double iax = it.aDouble;
					double ibx = it.bDouble;
					float ox;
					ox = (float) (iax + ibx);
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (float) (iax + ibx);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					double ox;
					ox = (iax + ibx);
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					double ibx = it.bDouble;
					double ox;
					ox = (iax + ibx);
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (iax + ibx);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					double iax = it.aDouble;
					final double ibx = it.bDouble;
					double ox;
					ox = (iax + ibx);
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ox = (iax + ibx);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					double iax = it.aDouble;
					double ibx = it.bDouble;
					double ox;
					ox = (iax + ibx);
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (iax + ibx);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			it.setDoubleOutput(true);

			if (da.getElementsPerItem() == 1) {
				final double iay = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					float ox;
					float oy;
					ox = (float) (iax + ibx);
					oy = (float) (iay + iby);
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else if (db.getElementsPerItem() == 1) {
				final double iby = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					ox = (float) (iax + ibx);
					oy = (float) (iay + iby);
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					float ox;
					float oy;
					ox = (float) (iax + ibx);
					oy = (float) (iay + iby);
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			it.setDoubleOutput(true);

			if (da.getElementsPerItem() == 1) {
				final double iay = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					double ox;
					double oy;
					ox = (iax + ibx);
					oy = (iay + iby);
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else if (db.getElementsPerItem() == 1) {
				final double iby = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					ox = (iax + ibx);
					oy = (iay + iby);
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					double ox;
					double oy;
					ox = (iax + ibx);
					oy = (iay + iby);
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("add supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "add");
		return (AbstractDataset) result;
	}

	/**
	 * subtract operator
	 * @param a
	 * @param b
	 * @return a - b, subtraction of a by b
	 */
	public static AbstractDataset subtract(final Object a, final Object b) {
		return subtract(a, b, null);
	}

	/**
	 * subtract operator
	 * @param a
	 * @param b
	 * @param o output can be null - in which case, a new dataset is created
	 * @return a - b, subtraction of a by b
	 */
	public static AbstractDataset subtract(final Object a, final Object b, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final Dataset db = b instanceof Dataset ? (Dataset) b : DatasetFactory.createFromObject(b);
		final BroadcastIterator it = new BroadcastIterator(da, db, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				byte ox;
				ox = (byte) (iax - ibx);
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				short ox;
				ox = (short) (iax - ibx);
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				long ox;
				ox = (iax - ibx);
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				int ox;
				ox = (int) (iax - ibx);
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					byte ox;
					ox = (byte) (iax - ibx);
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					byte ox;
					ox = (byte) (iax - ibx);
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (byte) (iax - ibx);
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					byte ox;
					ox = (byte) (iax - ibx);
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (byte) (iax - ibx);
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					byte ox;
					ox = (byte) (iax - ibx);
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (byte) (iax - ibx);
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					short ox;
					ox = (short) (iax - ibx);
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					short ox;
					ox = (short) (iax - ibx);
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (short) (iax - ibx);
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					short ox;
					ox = (short) (iax - ibx);
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (short) (iax - ibx);
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					short ox;
					ox = (short) (iax - ibx);
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (short) (iax - ibx);
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					long ox;
					ox = (iax - ibx);
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					long ox;
					ox = (iax - ibx);
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (iax - ibx);
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					long ox;
					ox = (iax - ibx);
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (iax - ibx);
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					long ox;
					ox = (iax - ibx);
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (iax - ibx);
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					int ox;
					ox = (int) (iax - ibx);
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					int ox;
					ox = (int) (iax - ibx);
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (int) (iax - ibx);
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					int ox;
					ox = (int) (iax - ibx);
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (int) (iax - ibx);
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					int ox;
					ox = (int) (iax - ibx);
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (int) (iax - ibx);
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			it.setDoubleOutput(true);

			while (it.hasNext()) {
				final double iax = it.aDouble;
				final double ibx = it.bDouble;
				float ox;
				ox = (float) (iax - ibx);
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			it.setDoubleOutput(true);

			while (it.hasNext()) {
				final double iax = it.aDouble;
				final double ibx = it.bDouble;
				double ox;
				ox = (iax - ibx);
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					float ox;
					ox = (float) (iax - ibx);
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					double ibx = it.bDouble;
					float ox;
					ox = (float) (iax - ibx);
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (float) (iax - ibx);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					double iax = it.aDouble;
					final double ibx = it.bDouble;
					float ox;
					ox = (float) (iax - ibx);
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ox = (float) (iax - ibx);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					double iax = it.aDouble;
					double ibx = it.bDouble;
					float ox;
					ox = (float) (iax - ibx);
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (float) (iax - ibx);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					double ox;
					ox = (iax - ibx);
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					double ibx = it.bDouble;
					double ox;
					ox = (iax - ibx);
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (iax - ibx);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					double iax = it.aDouble;
					final double ibx = it.bDouble;
					double ox;
					ox = (iax - ibx);
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ox = (iax - ibx);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					double iax = it.aDouble;
					double ibx = it.bDouble;
					double ox;
					ox = (iax - ibx);
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (iax - ibx);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			it.setDoubleOutput(true);

			if (da.getElementsPerItem() == 1) {
				final double iay = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					float ox;
					float oy;
					ox = (float) (iax - ibx);
					oy = (float) (iay - iby);
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else if (db.getElementsPerItem() == 1) {
				final double iby = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					ox = (float) (iax - ibx);
					oy = (float) (iay - iby);
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					float ox;
					float oy;
					ox = (float) (iax - ibx);
					oy = (float) (iay - iby);
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			it.setDoubleOutput(true);

			if (da.getElementsPerItem() == 1) {
				final double iay = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					double ox;
					double oy;
					ox = (iax - ibx);
					oy = (iay - iby);
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else if (db.getElementsPerItem() == 1) {
				final double iby = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					ox = (iax - ibx);
					oy = (iay - iby);
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					double ox;
					double oy;
					ox = (iax - ibx);
					oy = (iay - iby);
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("subtract supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "subtract");
		return (AbstractDataset) result;
	}

	/**
	 * multiply operator
	 * @param a
	 * @param b
	 * @return a * b, product of a and b
	 */
	public static AbstractDataset multiply(final Object a, final Object b) {
		return multiply(a, b, null);
	}

	/**
	 * multiply operator
	 * @param a
	 * @param b
	 * @param o output can be null - in which case, a new dataset is created
	 * @return a * b, product of a and b
	 */
	public static AbstractDataset multiply(final Object a, final Object b, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final Dataset db = b instanceof Dataset ? (Dataset) b : DatasetFactory.createFromObject(b);
		final BroadcastIterator it = new BroadcastIterator(da, db, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				byte ox;
				ox = (byte) (iax * ibx);
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				short ox;
				ox = (short) (iax * ibx);
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				long ox;
				ox = (iax * ibx);
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				int ox;
				ox = (int) (iax * ibx);
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					byte ox;
					ox = (byte) (iax * ibx);
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					byte ox;
					ox = (byte) (iax * ibx);
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (byte) (iax * ibx);
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					byte ox;
					ox = (byte) (iax * ibx);
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (byte) (iax * ibx);
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					byte ox;
					ox = (byte) (iax * ibx);
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (byte) (iax * ibx);
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					short ox;
					ox = (short) (iax * ibx);
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					short ox;
					ox = (short) (iax * ibx);
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (short) (iax * ibx);
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					short ox;
					ox = (short) (iax * ibx);
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (short) (iax * ibx);
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					short ox;
					ox = (short) (iax * ibx);
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (short) (iax * ibx);
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					long ox;
					ox = (iax * ibx);
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					long ox;
					ox = (iax * ibx);
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (iax * ibx);
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					long ox;
					ox = (iax * ibx);
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (iax * ibx);
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					long ox;
					ox = (iax * ibx);
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (iax * ibx);
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					int ox;
					ox = (int) (iax * ibx);
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					int ox;
					ox = (int) (iax * ibx);
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (int) (iax * ibx);
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					int ox;
					ox = (int) (iax * ibx);
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (int) (iax * ibx);
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					int ox;
					ox = (int) (iax * ibx);
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (int) (iax * ibx);
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			it.setDoubleOutput(true);

			while (it.hasNext()) {
				final double iax = it.aDouble;
				final double ibx = it.bDouble;
				float ox;
				ox = (float) (iax * ibx);
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			it.setDoubleOutput(true);

			while (it.hasNext()) {
				final double iax = it.aDouble;
				final double ibx = it.bDouble;
				double ox;
				ox = (iax * ibx);
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					float ox;
					ox = (float) (iax * ibx);
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					double ibx = it.bDouble;
					float ox;
					ox = (float) (iax * ibx);
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (float) (iax * ibx);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					double iax = it.aDouble;
					final double ibx = it.bDouble;
					float ox;
					ox = (float) (iax * ibx);
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ox = (float) (iax * ibx);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					double iax = it.aDouble;
					double ibx = it.bDouble;
					float ox;
					ox = (float) (iax * ibx);
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (float) (iax * ibx);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					double ox;
					ox = (iax * ibx);
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					double ibx = it.bDouble;
					double ox;
					ox = (iax * ibx);
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (iax * ibx);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					double iax = it.aDouble;
					final double ibx = it.bDouble;
					double ox;
					ox = (iax * ibx);
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ox = (iax * ibx);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					double iax = it.aDouble;
					double ibx = it.bDouble;
					double ox;
					ox = (iax * ibx);
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (iax * ibx);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			it.setDoubleOutput(true);

			if (da.getElementsPerItem() == 1) {
				final double iay = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					float ox;
					float oy;
					ox = (float) (iax * ibx - iay * iby);
					oy = (float) (iax * iby + iay * ibx);
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else if (db.getElementsPerItem() == 1) {
				final double iby = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					ox = (float) (iax * ibx - iay * iby);
					oy = (float) (iax * iby + iay * ibx);
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					float ox;
					float oy;
					ox = (float) (iax * ibx - iay * iby);
					oy = (float) (iax * iby + iay * ibx);
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			it.setDoubleOutput(true);

			if (da.getElementsPerItem() == 1) {
				final double iay = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					double ox;
					double oy;
					ox = (iax * ibx - iay * iby);
					oy = (iax * iby + iay * ibx);
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else if (db.getElementsPerItem() == 1) {
				final double iby = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					ox = (iax * ibx - iay * iby);
					oy = (iax * iby + iay * ibx);
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					double ox;
					double oy;
					ox = (iax * ibx - iay * iby);
					oy = (iax * iby + iay * ibx);
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("multiply supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "multiply");
		return (AbstractDataset) result;
	}

	/**
	 * divide operator
	 * @param a
	 * @param b
	 * @return a / b, division of a by b
	 */
	public static AbstractDataset divide(final Object a, final Object b) {
		return divide(a, b, null);
	}

	/**
	 * divide operator
	 * @param a
	 * @param b
	 * @param o output can be null - in which case, a new dataset is created
	 * @return a / b, division of a by b
	 */
	public static AbstractDataset divide(final Object a, final Object b, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final Dataset db = b instanceof Dataset ? (Dataset) b : DatasetFactory.createFromObject(b);
		final BroadcastIterator it = new BroadcastIterator(da, db, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				byte ox;
				ox = (byte) (ibx == 0 ? 0 : iax / ibx);
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				short ox;
				ox = (short) (ibx == 0 ? 0 : iax / ibx);
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				long ox;
				ox = (ibx == 0 ? 0 : iax / ibx);
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				int ox;
				ox = (int) (ibx == 0 ? 0 : iax / ibx);
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					byte ox;
					ox = (byte) (ibx == 0 ? 0 : iax / ibx);
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					byte ox;
					ox = (byte) (ibx == 0 ? 0 : iax / ibx);
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (byte) (ibx == 0 ? 0 : iax / ibx);
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					byte ox;
					ox = (byte) (ibx == 0 ? 0 : iax / ibx);
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (byte) (ibx == 0 ? 0 : iax / ibx);
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					byte ox;
					ox = (byte) (ibx == 0 ? 0 : iax / ibx);
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (byte) (ibx == 0 ? 0 : iax / ibx);
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					short ox;
					ox = (short) (ibx == 0 ? 0 : iax / ibx);
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					short ox;
					ox = (short) (ibx == 0 ? 0 : iax / ibx);
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (short) (ibx == 0 ? 0 : iax / ibx);
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					short ox;
					ox = (short) (ibx == 0 ? 0 : iax / ibx);
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (short) (ibx == 0 ? 0 : iax / ibx);
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					short ox;
					ox = (short) (ibx == 0 ? 0 : iax / ibx);
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (short) (ibx == 0 ? 0 : iax / ibx);
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					long ox;
					ox = (ibx == 0 ? 0 : iax / ibx);
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					long ox;
					ox = (ibx == 0 ? 0 : iax / ibx);
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (ibx == 0 ? 0 : iax / ibx);
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					long ox;
					ox = (ibx == 0 ? 0 : iax / ibx);
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (ibx == 0 ? 0 : iax / ibx);
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					long ox;
					ox = (ibx == 0 ? 0 : iax / ibx);
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (ibx == 0 ? 0 : iax / ibx);
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					int ox;
					ox = (int) (ibx == 0 ? 0 : iax / ibx);
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					int ox;
					ox = (int) (ibx == 0 ? 0 : iax / ibx);
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (int) (ibx == 0 ? 0 : iax / ibx);
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					int ox;
					ox = (int) (ibx == 0 ? 0 : iax / ibx);
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (int) (ibx == 0 ? 0 : iax / ibx);
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					int ox;
					ox = (int) (ibx == 0 ? 0 : iax / ibx);
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (int) (ibx == 0 ? 0 : iax / ibx);
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			it.setDoubleOutput(true);

			while (it.hasNext()) {
				final double iax = it.aDouble;
				final double ibx = it.bDouble;
				float ox;
				ox = (float) (iax / ibx);
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			it.setDoubleOutput(true);

			while (it.hasNext()) {
				final double iax = it.aDouble;
				final double ibx = it.bDouble;
				double ox;
				ox = (iax / ibx);
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					float ox;
					ox = (float) (iax / ibx);
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					double ibx = it.bDouble;
					float ox;
					ox = (float) (iax / ibx);
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (float) (iax / ibx);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					double iax = it.aDouble;
					final double ibx = it.bDouble;
					float ox;
					ox = (float) (iax / ibx);
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ox = (float) (iax / ibx);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					double iax = it.aDouble;
					double ibx = it.bDouble;
					float ox;
					ox = (float) (iax / ibx);
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (float) (iax / ibx);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					double ox;
					ox = (iax / ibx);
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					double ibx = it.bDouble;
					double ox;
					ox = (iax / ibx);
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (iax / ibx);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					double iax = it.aDouble;
					final double ibx = it.bDouble;
					double ox;
					ox = (iax / ibx);
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ox = (iax / ibx);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					double iax = it.aDouble;
					double ibx = it.bDouble;
					double ox;
					ox = (iax / ibx);
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (iax / ibx);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			it.setDoubleOutput(true);

			if (da.getElementsPerItem() == 1) {
				final double iay = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					float q;
					float den;
					float ox;
					float oy;
					if (Math.abs(ibx) < Math.abs(iby)) {
						q = (float) (ibx / iby);
						den = (float) (ibx * q + iby);
						ox = (float) ((iax * q + iay) / den);
						oy = (float) ((iay * q - ibx) / den);
					} else {
						q = (float) (iby / ibx);
						den = (float) (iby * q + ibx);
						ox = (float) ((iay * q + iax) / den);
						oy = (float) ((iay - iax * q) / den);
					}
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else if (db.getElementsPerItem() == 1) {
				final double iby = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					float q;
					float den;
					float ox;
					float oy;
					if (Math.abs(ibx) < Math.abs(iby)) {
						q = (float) (ibx / iby);
						den = (float) (ibx * q + iby);
						ox = (float) ((iax * q + iay) / den);
						oy = (float) ((iay * q - ibx) / den);
					} else {
						q = (float) (iby / ibx);
						den = (float) (iby * q + ibx);
						ox = (float) ((iay * q + iax) / den);
						oy = (float) ((iay - iax * q) / den);
					}
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					float q;
					float den;
					float ox;
					float oy;
					if (Math.abs(ibx) < Math.abs(iby)) {
						q = (float) (ibx / iby);
						den = (float) (ibx * q + iby);
						ox = (float) ((iax * q + iay) / den);
						oy = (float) ((iay * q - ibx) / den);
					} else {
						q = (float) (iby / ibx);
						den = (float) (iby * q + ibx);
						ox = (float) ((iay * q + iax) / den);
						oy = (float) ((iay - iax * q) / den);
					}
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			it.setDoubleOutput(true);

			if (da.getElementsPerItem() == 1) {
				final double iay = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					double q;
					double den;
					double ox;
					double oy;
					if (Math.abs(ibx) < Math.abs(iby)) {
						q = (ibx / iby);
						den = (ibx * q + iby);
						ox = ((iax * q + iay) / den);
						oy = ((iay * q - ibx) / den);
					} else {
						q = (iby / ibx);
						den = (iby * q + ibx);
						ox = ((iay * q + iax) / den);
						oy = ((iay - iax * q) / den);
					}
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else if (db.getElementsPerItem() == 1) {
				final double iby = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					double q;
					double den;
					double ox;
					double oy;
					if (Math.abs(ibx) < Math.abs(iby)) {
						q = (ibx / iby);
						den = (ibx * q + iby);
						ox = ((iax * q + iay) / den);
						oy = ((iay * q - ibx) / den);
					} else {
						q = (iby / ibx);
						den = (iby * q + ibx);
						ox = ((iay * q + iax) / den);
						oy = ((iay - iax * q) / den);
					}
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					double q;
					double den;
					double ox;
					double oy;
					if (Math.abs(ibx) < Math.abs(iby)) {
						q = (ibx / iby);
						den = (ibx * q + iby);
						ox = ((iax * q + iay) / den);
						oy = ((iay * q - ibx) / den);
					} else {
						q = (iby / ibx);
						den = (iby * q + ibx);
						ox = ((iay * q + iax) / den);
						oy = ((iay - iax * q) / den);
					}
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("divide supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "divide");
		return (AbstractDataset) result;
	}

	/**
	 * dividez operator
	 * @param a
	 * @param b
	 * @return a / b, division of a by b
	 */
	public static AbstractDataset dividez(final Object a, final Object b) {
		return dividez(a, b, null);
	}

	/**
	 * dividez operator
	 * @param a
	 * @param b
	 * @param o output can be null - in which case, a new dataset is created
	 * @return a / b, division of a by b
	 */
	public static AbstractDataset dividez(final Object a, final Object b, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final Dataset db = b instanceof Dataset ? (Dataset) b : DatasetFactory.createFromObject(b);
		final BroadcastIterator it = new BroadcastIterator(da, db, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				byte ox;
				ox = (byte) (ibx == 0 ? 0 : iax / ibx);
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				short ox;
				ox = (short) (ibx == 0 ? 0 : iax / ibx);
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				long ox;
				ox = (ibx == 0 ? 0 : iax / ibx);
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				int ox;
				ox = (int) (ibx == 0 ? 0 : iax / ibx);
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					byte ox;
					ox = (byte) (ibx == 0 ? 0 : iax / ibx);
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					byte ox;
					ox = (byte) (ibx == 0 ? 0 : iax / ibx);
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (byte) (ibx == 0 ? 0 : iax / ibx);
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					byte ox;
					ox = (byte) (ibx == 0 ? 0 : iax / ibx);
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (byte) (ibx == 0 ? 0 : iax / ibx);
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					byte ox;
					ox = (byte) (ibx == 0 ? 0 : iax / ibx);
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (byte) (ibx == 0 ? 0 : iax / ibx);
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					short ox;
					ox = (short) (ibx == 0 ? 0 : iax / ibx);
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					short ox;
					ox = (short) (ibx == 0 ? 0 : iax / ibx);
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (short) (ibx == 0 ? 0 : iax / ibx);
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					short ox;
					ox = (short) (ibx == 0 ? 0 : iax / ibx);
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (short) (ibx == 0 ? 0 : iax / ibx);
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					short ox;
					ox = (short) (ibx == 0 ? 0 : iax / ibx);
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (short) (ibx == 0 ? 0 : iax / ibx);
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					long ox;
					ox = (ibx == 0 ? 0 : iax / ibx);
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					long ox;
					ox = (ibx == 0 ? 0 : iax / ibx);
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (ibx == 0 ? 0 : iax / ibx);
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					long ox;
					ox = (ibx == 0 ? 0 : iax / ibx);
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (ibx == 0 ? 0 : iax / ibx);
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					long ox;
					ox = (ibx == 0 ? 0 : iax / ibx);
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (ibx == 0 ? 0 : iax / ibx);
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					int ox;
					ox = (int) (ibx == 0 ? 0 : iax / ibx);
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					int ox;
					ox = (int) (ibx == 0 ? 0 : iax / ibx);
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (int) (ibx == 0 ? 0 : iax / ibx);
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					int ox;
					ox = (int) (ibx == 0 ? 0 : iax / ibx);
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (int) (ibx == 0 ? 0 : iax / ibx);
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					int ox;
					ox = (int) (ibx == 0 ? 0 : iax / ibx);
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (int) (ibx == 0 ? 0 : iax / ibx);
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			it.setDoubleOutput(true);

			while (it.hasNext()) {
				final double iax = it.aDouble;
				final double ibx = it.bDouble;
				float ox;
				ox = (float) (ibx == 0 ? 0 : iax / ibx);
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			it.setDoubleOutput(true);

			while (it.hasNext()) {
				final double iax = it.aDouble;
				final double ibx = it.bDouble;
				double ox;
				ox = (ibx == 0 ? 0 : iax / ibx);
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					float ox;
					ox = (float) (ibx == 0 ? 0 : iax / ibx);
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					double ibx = it.bDouble;
					float ox;
					ox = (float) (ibx == 0 ? 0 : iax / ibx);
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (float) (ibx == 0 ? 0 : iax / ibx);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					double iax = it.aDouble;
					final double ibx = it.bDouble;
					float ox;
					ox = (float) (ibx == 0 ? 0 : iax / ibx);
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ox = (float) (ibx == 0 ? 0 : iax / ibx);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					double iax = it.aDouble;
					double ibx = it.bDouble;
					float ox;
					ox = (float) (ibx == 0 ? 0 : iax / ibx);
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (float) (ibx == 0 ? 0 : iax / ibx);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					double ox;
					ox = (ibx == 0 ? 0 : iax / ibx);
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					double ibx = it.bDouble;
					double ox;
					ox = (ibx == 0 ? 0 : iax / ibx);
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (ibx == 0 ? 0 : iax / ibx);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					double iax = it.aDouble;
					final double ibx = it.bDouble;
					double ox;
					ox = (ibx == 0 ? 0 : iax / ibx);
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ox = (ibx == 0 ? 0 : iax / ibx);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					double iax = it.aDouble;
					double ibx = it.bDouble;
					double ox;
					ox = (ibx == 0 ? 0 : iax / ibx);
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (ibx == 0 ? 0 : iax / ibx);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			it.setDoubleOutput(true);

			if (da.getElementsPerItem() == 1) {
				final double iay = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					float ox;
					float oy;
					float q;
					float den;
					if (ibx == 0 && iby == 0) {
						ox = 0;
						oy = 0;
					} else if (Math.abs(ibx) < Math.abs(iby)) {
						q = (float) (ibx / iby);
						den = (float) (ibx * q + iby);
						ox = (float) ((iax * q + iay) / den);
						oy = (float) ((iay * q - ibx) / den);
					} else {
						q = (float) (iby / ibx);
						den = (float) (iby * q + ibx);
						ox = (float) ((iay * q + iax) / den);
						oy = (float) ((iay - iax * q) / den);
					}
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else if (db.getElementsPerItem() == 1) {
				final double iby = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					float q;
					float den;
					if (ibx == 0 && iby == 0) {
						ox = 0;
						oy = 0;
					} else if (Math.abs(ibx) < Math.abs(iby)) {
						q = (float) (ibx / iby);
						den = (float) (ibx * q + iby);
						ox = (float) ((iax * q + iay) / den);
						oy = (float) ((iay * q - ibx) / den);
					} else {
						q = (float) (iby / ibx);
						den = (float) (iby * q + ibx);
						ox = (float) ((iay * q + iax) / den);
						oy = (float) ((iay - iax * q) / den);
					}
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					float ox;
					float oy;
					float q;
					float den;
					if (ibx == 0 && iby == 0) {
						ox = 0;
						oy = 0;
					} else if (Math.abs(ibx) < Math.abs(iby)) {
						q = (float) (ibx / iby);
						den = (float) (ibx * q + iby);
						ox = (float) ((iax * q + iay) / den);
						oy = (float) ((iay * q - ibx) / den);
					} else {
						q = (float) (iby / ibx);
						den = (float) (iby * q + ibx);
						ox = (float) ((iay * q + iax) / den);
						oy = (float) ((iay - iax * q) / den);
					}
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			it.setDoubleOutput(true);

			if (da.getElementsPerItem() == 1) {
				final double iay = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					double ox;
					double oy;
					double q;
					double den;
					if (ibx == 0 && iby == 0) {
						ox = 0;
						oy = 0;
					} else if (Math.abs(ibx) < Math.abs(iby)) {
						q = (ibx / iby);
						den = (ibx * q + iby);
						ox = ((iax * q + iay) / den);
						oy = ((iay * q - ibx) / den);
					} else {
						q = (iby / ibx);
						den = (iby * q + ibx);
						ox = ((iay * q + iax) / den);
						oy = ((iay - iax * q) / den);
					}
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else if (db.getElementsPerItem() == 1) {
				final double iby = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					double q;
					double den;
					if (ibx == 0 && iby == 0) {
						ox = 0;
						oy = 0;
					} else if (Math.abs(ibx) < Math.abs(iby)) {
						q = (ibx / iby);
						den = (ibx * q + iby);
						ox = ((iax * q + iay) / den);
						oy = ((iay * q - ibx) / den);
					} else {
						q = (iby / ibx);
						den = (iby * q + ibx);
						ox = ((iay * q + iax) / den);
						oy = ((iay - iax * q) / den);
					}
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					double ox;
					double oy;
					double q;
					double den;
					if (ibx == 0 && iby == 0) {
						ox = 0;
						oy = 0;
					} else if (Math.abs(ibx) < Math.abs(iby)) {
						q = (ibx / iby);
						den = (ibx * q + iby);
						ox = ((iax * q + iay) / den);
						oy = ((iay * q - ibx) / den);
					} else {
						q = (iby / ibx);
						den = (iby * q + ibx);
						ox = ((iay * q + iax) / den);
						oy = ((iay - iax * q) / den);
					}
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("dividez supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "dividez");
		return (AbstractDataset) result;
	}

	/**
	 * power operator
	 * @param a
	 * @param b
	 * @return a ** b, raise a to power of b
	 */
	public static AbstractDataset power(final Object a, final Object b) {
		return power(a, b, null);
	}

	/**
	 * power operator
	 * @param a
	 * @param b
	 * @param o output can be null - in which case, a new dataset is created
	 * @return a ** b, raise a to power of b
	 */
	public static AbstractDataset power(final Object a, final Object b, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final Dataset db = b instanceof Dataset ? (Dataset) b : DatasetFactory.createFromObject(b);
		final BroadcastIterator it = new BroadcastIterator(da, db, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			it.setDoubleOutput(true);

			while (it.hasNext()) {
				final double iax = it.aDouble;
				final double ibx = it.bDouble;
				byte ox;
				ox = (byte) toLong(Math.pow(iax, ibx));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			it.setDoubleOutput(true);

			while (it.hasNext()) {
				final double iax = it.aDouble;
				final double ibx = it.bDouble;
				short ox;
				ox = (short) toLong(Math.pow(iax, ibx));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			it.setDoubleOutput(true);

			while (it.hasNext()) {
				final double iax = it.aDouble;
				final double ibx = it.bDouble;
				long ox;
				ox = toLong(Math.pow(iax, ibx));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			it.setDoubleOutput(true);

			while (it.hasNext()) {
				final double iax = it.aDouble;
				final double ibx = it.bDouble;
				int ox;
				ox = (int) toLong(Math.pow(iax, ibx));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					byte ox;
					ox = (byte) toLong(Math.pow(iax, ibx));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					double ibx = it.bDouble;
					byte ox;
					ox = (byte) toLong(Math.pow(iax, ibx));
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (byte) toLong(Math.pow(iax, ibx));
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					double iax = it.aDouble;
					final double ibx = it.bDouble;
					byte ox;
					ox = (byte) toLong(Math.pow(iax, ibx));
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ox = (byte) toLong(Math.pow(iax, ibx));
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					double iax = it.aDouble;
					double ibx = it.bDouble;
					byte ox;
					ox = (byte) toLong(Math.pow(iax, ibx));
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (byte) toLong(Math.pow(iax, ibx));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					short ox;
					ox = (short) toLong(Math.pow(iax, ibx));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					double ibx = it.bDouble;
					short ox;
					ox = (short) toLong(Math.pow(iax, ibx));
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (short) toLong(Math.pow(iax, ibx));
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					double iax = it.aDouble;
					final double ibx = it.bDouble;
					short ox;
					ox = (short) toLong(Math.pow(iax, ibx));
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ox = (short) toLong(Math.pow(iax, ibx));
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					double iax = it.aDouble;
					double ibx = it.bDouble;
					short ox;
					ox = (short) toLong(Math.pow(iax, ibx));
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (short) toLong(Math.pow(iax, ibx));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					long ox;
					ox = toLong(Math.pow(iax, ibx));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					double ibx = it.bDouble;
					long ox;
					ox = toLong(Math.pow(iax, ibx));
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = toLong(Math.pow(iax, ibx));
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					double iax = it.aDouble;
					final double ibx = it.bDouble;
					long ox;
					ox = toLong(Math.pow(iax, ibx));
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ox = toLong(Math.pow(iax, ibx));
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					double iax = it.aDouble;
					double ibx = it.bDouble;
					long ox;
					ox = toLong(Math.pow(iax, ibx));
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = toLong(Math.pow(iax, ibx));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					int ox;
					ox = (int) toLong(Math.pow(iax, ibx));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					double ibx = it.bDouble;
					int ox;
					ox = (int) toLong(Math.pow(iax, ibx));
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (int) toLong(Math.pow(iax, ibx));
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					double iax = it.aDouble;
					final double ibx = it.bDouble;
					int ox;
					ox = (int) toLong(Math.pow(iax, ibx));
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ox = (int) toLong(Math.pow(iax, ibx));
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					double iax = it.aDouble;
					double ibx = it.bDouble;
					int ox;
					ox = (int) toLong(Math.pow(iax, ibx));
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (int) toLong(Math.pow(iax, ibx));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			it.setDoubleOutput(true);

			while (it.hasNext()) {
				final double iax = it.aDouble;
				final double ibx = it.bDouble;
				float ox;
				ox = (float) (Math.pow(iax, ibx));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			it.setDoubleOutput(true);

			while (it.hasNext()) {
				final double iax = it.aDouble;
				final double ibx = it.bDouble;
				double ox;
				ox = (Math.pow(iax, ibx));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					float ox;
					ox = (float) (Math.pow(iax, ibx));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					double ibx = it.bDouble;
					float ox;
					ox = (float) (Math.pow(iax, ibx));
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (float) (Math.pow(iax, ibx));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					double iax = it.aDouble;
					final double ibx = it.bDouble;
					float ox;
					ox = (float) (Math.pow(iax, ibx));
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ox = (float) (Math.pow(iax, ibx));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					double iax = it.aDouble;
					double ibx = it.bDouble;
					float ox;
					ox = (float) (Math.pow(iax, ibx));
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (float) (Math.pow(iax, ibx));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					double ox;
					ox = (Math.pow(iax, ibx));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					double ibx = it.bDouble;
					double ox;
					ox = (Math.pow(iax, ibx));
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (Math.pow(iax, ibx));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					double iax = it.aDouble;
					final double ibx = it.bDouble;
					double ox;
					ox = (Math.pow(iax, ibx));
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ox = (Math.pow(iax, ibx));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					double iax = it.aDouble;
					double ibx = it.bDouble;
					double ox;
					ox = (Math.pow(iax, ibx));
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (Math.pow(iax, ibx));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			it.setDoubleOutput(true);

			if (da.getElementsPerItem() == 1) {
				final double iay = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					Complex tz;
					float ox;
					float oy;
					tz = new Complex(iax, iay).pow(new Complex(ibx, iby));
					ox = (float) (tz.getReal());
					oy = (float) (tz.getImaginary());
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else if (db.getElementsPerItem() == 1) {
				final double iby = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					Complex tz;
					float ox;
					float oy;
					tz = new Complex(iax, iay).pow(new Complex(ibx, iby));
					ox = (float) (tz.getReal());
					oy = (float) (tz.getImaginary());
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					Complex tz;
					float ox;
					float oy;
					tz = new Complex(iax, iay).pow(new Complex(ibx, iby));
					ox = (float) (tz.getReal());
					oy = (float) (tz.getImaginary());
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			it.setDoubleOutput(true);

			if (da.getElementsPerItem() == 1) {
				final double iay = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					Complex tz;
					double ox;
					double oy;
					tz = new Complex(iax, iay).pow(new Complex(ibx, iby));
					ox = (tz.getReal());
					oy = (tz.getImaginary());
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else if (db.getElementsPerItem() == 1) {
				final double iby = 0;
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					Complex tz;
					double ox;
					double oy;
					tz = new Complex(iax, iay).pow(new Complex(ibx, iby));
					ox = (tz.getReal());
					oy = (tz.getImaginary());
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					final double iay = da.getElementDoubleAbs(it.aIndex + 1);
					final double iby = db.getElementDoubleAbs(it.bIndex + 1);
					Complex tz;
					double ox;
					double oy;
					tz = new Complex(iax, iay).pow(new Complex(ibx, iby));
					ox = (tz.getReal());
					oy = (tz.getImaginary());
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("power supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "power");
		return (AbstractDataset) result;
	}

	/**
	 * remainder operator
	 * @param a
	 * @param b
	 * @return a % b, remainder of division of a by b
	 */
	public static AbstractDataset remainder(final Object a, final Object b) {
		return remainder(a, b, null);
	}

	/**
	 * remainder operator
	 * @param a
	 * @param b
	 * @param o output can be null - in which case, a new dataset is created
	 * @return a % b, remainder of division of a by b
	 */
	public static AbstractDataset remainder(final Object a, final Object b, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final Dataset db = b instanceof Dataset ? (Dataset) b : DatasetFactory.createFromObject(b);
		final BroadcastIterator it = new BroadcastIterator(da, db, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				byte ox;
				ox = (byte) (ibx == 0 ? 0 : iax % ibx);
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				short ox;
				ox = (short) (ibx == 0 ? 0 : iax % ibx);
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				long ox;
				ox = (ibx == 0 ? 0 : iax % ibx);
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long iax = it.aLong;
				final long ibx = it.bLong;
				int ox;
				ox = (int) (ibx == 0 ? 0 : iax % ibx);
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					byte ox;
					ox = (byte) (ibx == 0 ? 0 : iax % ibx);
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					byte ox;
					ox = (byte) (ibx == 0 ? 0 : iax % ibx);
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (byte) (ibx == 0 ? 0 : iax % ibx);
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					byte ox;
					ox = (byte) (ibx == 0 ? 0 : iax % ibx);
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (byte) (ibx == 0 ? 0 : iax % ibx);
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					byte ox;
					ox = (byte) (ibx == 0 ? 0 : iax % ibx);
					oai8data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (byte) (ibx == 0 ? 0 : iax % ibx);
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					short ox;
					ox = (short) (ibx == 0 ? 0 : iax % ibx);
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					short ox;
					ox = (short) (ibx == 0 ? 0 : iax % ibx);
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (short) (ibx == 0 ? 0 : iax % ibx);
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					short ox;
					ox = (short) (ibx == 0 ? 0 : iax % ibx);
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (short) (ibx == 0 ? 0 : iax % ibx);
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					short ox;
					ox = (short) (ibx == 0 ? 0 : iax % ibx);
					oai16data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (short) (ibx == 0 ? 0 : iax % ibx);
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					long ox;
					ox = (ibx == 0 ? 0 : iax % ibx);
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					long ox;
					ox = (ibx == 0 ? 0 : iax % ibx);
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (ibx == 0 ? 0 : iax % ibx);
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					long ox;
					ox = (ibx == 0 ? 0 : iax % ibx);
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (ibx == 0 ? 0 : iax % ibx);
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					long ox;
					ox = (ibx == 0 ? 0 : iax % ibx);
					oai64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (ibx == 0 ? 0 : iax % ibx);
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					final long ibx = it.bLong;
					int ox;
					ox = (int) (ibx == 0 ? 0 : iax % ibx);
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long iax = it.aLong;
					long ibx = it.bLong;
					int ox;
					ox = (int) (ibx == 0 ? 0 : iax % ibx);
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (int) (ibx == 0 ? 0 : iax % ibx);
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					long iax = it.aLong;
					final long ibx = it.bLong;
					int ox;
					ox = (int) (ibx == 0 ? 0 : iax % ibx);
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ox = (int) (ibx == 0 ? 0 : iax % ibx);
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					long iax = it.aLong;
					long ibx = it.bLong;
					int ox;
					ox = (int) (ibx == 0 ? 0 : iax % ibx);
					oai32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementLongAbs(it.aIndex + j);
						ibx = db.getElementLongAbs(it.bIndex + j);
						ox = (int) (ibx == 0 ? 0 : iax % ibx);
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			it.setDoubleOutput(true);

			while (it.hasNext()) {
				final double iax = it.aDouble;
				final double ibx = it.bDouble;
				float ox;
				ox = (float) (iax % ibx);
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			it.setDoubleOutput(true);

			while (it.hasNext()) {
				final double iax = it.aDouble;
				final double ibx = it.bDouble;
				double ox;
				ox = (iax % ibx);
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					float ox;
					ox = (float) (iax % ibx);
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					double ibx = it.bDouble;
					float ox;
					ox = (float) (iax % ibx);
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (float) (iax % ibx);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					double iax = it.aDouble;
					final double ibx = it.bDouble;
					float ox;
					ox = (float) (iax % ibx);
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ox = (float) (iax % ibx);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					double iax = it.aDouble;
					double ibx = it.bDouble;
					float ox;
					ox = (float) (iax % ibx);
					oaf32data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (float) (iax % ibx);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			it.setDoubleOutput(true);

			if (is == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					final double ibx = it.bDouble;
					double ox;
					ox = (iax % ibx);
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double iax = it.aDouble;
					double ibx = it.bDouble;
					double ox;
					ox = (iax % ibx);
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (iax % ibx);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else if (db.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					double iax = it.aDouble;
					final double ibx = it.bDouble;
					double ox;
					ox = (iax % ibx);
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ox = (iax % ibx);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					double iax = it.aDouble;
					double ibx = it.bDouble;
					double ox;
					ox = (iax % ibx);
					oaf64data[it.oIndex] = ox;
					for (int j = 1; j < is; j++) {
						iax = da.getElementDoubleAbs(it.aIndex + j);
						ibx = db.getElementDoubleAbs(it.bIndex + j);
						ox = (iax % ibx);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		default:
			throw new IllegalArgumentException("remainder supports integer, compound integer, real, compound real datasets only");
		}

		addFunctionName(result, "remainder");
		return (AbstractDataset) result;
	}

	/**
	 * sin - evaluate the sine function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset sin(final Object a) {
		return sin(a, null);
	}

	/**
	 * sin - evaluate the sine function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset sin(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.sin(ix));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.sin(ix));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.sin(ix));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.sin(ix));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.sin(ix));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.sin(ix));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.sin(ix));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.sin(ix));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.sin(ix));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.sin(ix));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.sin(ix));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.sin(ix));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.sin(ix));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.sin(ix));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.sin(ix));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.sin(ix));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.sin(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.sin(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.sin(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.sin(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.sin(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.sin(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.sin(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.sin(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					float oy;
					ox = (float) (Math.sin(ix)*Math.cosh(iy));
					oy = (float) (Math.cos(ix)*Math.sinh(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					ox = (float) (Math.sin(ix)*Math.cosh(iy));
					oy = (float) (Math.cos(ix)*Math.sinh(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					double oy;
					ox = (Math.sin(ix)*Math.cosh(iy));
					oy = (Math.cos(ix)*Math.sinh(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					ox = (Math.sin(ix)*Math.cosh(iy));
					oy = (Math.cos(ix)*Math.sinh(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("sin supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "sin");
		return (AbstractDataset) result;
	}

	/**
	 * cos - evaluate the cosine function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset cos(final Object a) {
		return cos(a, null);
	}

	/**
	 * cos - evaluate the cosine function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset cos(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.cos(ix));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.cos(ix));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.cos(ix));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.cos(ix));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.cos(ix));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.cos(ix));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.cos(ix));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.cos(ix));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.cos(ix));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.cos(ix));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.cos(ix));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.cos(ix));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.cos(ix));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.cos(ix));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.cos(ix));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.cos(ix));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.cos(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.cos(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.cos(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.cos(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.cos(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.cos(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.cos(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.cos(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					float oy;
					ox = (float) (Math.cos(ix)*Math.cosh(iy));
					oy = (float) (-Math.sin(ix)*Math.sinh(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					ox = (float) (Math.cos(ix)*Math.cosh(iy));
					oy = (float) (-Math.sin(ix)*Math.sinh(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					double oy;
					ox = (Math.cos(ix)*Math.cosh(iy));
					oy = (-Math.sin(ix)*Math.sinh(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					ox = (Math.cos(ix)*Math.cosh(iy));
					oy = (-Math.sin(ix)*Math.sinh(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("cos supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "cos");
		return (AbstractDataset) result;
	}

	/**
	 * tan - evaluate the tangent function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset tan(final Object a) {
		return tan(a, null);
	}

	/**
	 * tan - evaluate the tangent function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset tan(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.tan(ix));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.tan(ix));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.tan(ix));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.tan(ix));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.tan(ix));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.tan(ix));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.tan(ix));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.tan(ix));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.tan(ix));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.tan(ix));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.tan(ix));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.tan(ix));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.tan(ix));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.tan(ix));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.tan(ix));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.tan(ix));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.tan(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.tan(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.tan(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.tan(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.tan(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.tan(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.tan(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.tan(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float x;
					float y;
					float tf;
					float ox;
					float oy;
					x = (float) (2.*ix);
					y = (float) (2.*iy);
					tf = (float) (1./(Math.cos(x)+Math.cosh(y)));
					ox = (float) (tf*Math.sin(x));
					oy = (float) (tf*Math.sinh(y));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float x;
					float y;
					float tf;
					float ox;
					float oy;
					x = (float) (2.*ix);
					y = (float) (2.*iy);
					tf = (float) (1./(Math.cos(x)+Math.cosh(y)));
					ox = (float) (tf*Math.sin(x));
					oy = (float) (tf*Math.sinh(y));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double x;
					double y;
					double tf;
					double ox;
					double oy;
					x = (2.*ix);
					y = (2.*iy);
					tf = (1./(Math.cos(x)+Math.cosh(y)));
					ox = (tf*Math.sin(x));
					oy = (tf*Math.sinh(y));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double x;
					double y;
					double tf;
					double ox;
					double oy;
					x = (2.*ix);
					y = (2.*iy);
					tf = (1./(Math.cos(x)+Math.cosh(y)));
					ox = (tf*Math.sin(x));
					oy = (tf*Math.sinh(y));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("tan supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "tan");
		return (AbstractDataset) result;
	}

	/**
	 * arcsin - evaluate the inverse sine function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset arcsin(final Object a) {
		return arcsin(a, null);
	}

	/**
	 * arcsin - evaluate the inverse sine function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset arcsin(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.asin(ix));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.asin(ix));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.asin(ix));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.asin(ix));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.asin(ix));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.asin(ix));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.asin(ix));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.asin(ix));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.asin(ix));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.asin(ix));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.asin(ix));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.asin(ix));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.asin(ix));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.asin(ix));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.asin(ix));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.asin(ix));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.asin(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.asin(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.asin(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.asin(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.asin(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.asin(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.asin(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.asin(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					Complex tz;
					float ox;
					float oy;
					tz = new Complex(ix, iy).asin();
					ox = (float) (tz.getReal());
					oy = (float) (tz.getImaginary());
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					Complex tz;
					float ox;
					float oy;
					tz = new Complex(ix, iy).asin();
					ox = (float) (tz.getReal());
					oy = (float) (tz.getImaginary());
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					Complex tz;
					double ox;
					double oy;
					tz = new Complex(ix, iy).asin();
					ox = (tz.getReal());
					oy = (tz.getImaginary());
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					Complex tz;
					double ox;
					double oy;
					tz = new Complex(ix, iy).asin();
					ox = (tz.getReal());
					oy = (tz.getImaginary());
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("arcsin supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "arcsin");
		return (AbstractDataset) result;
	}

	/**
	 * arccos - evaluate the inverse cosine function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset arccos(final Object a) {
		return arccos(a, null);
	}

	/**
	 * arccos - evaluate the inverse cosine function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset arccos(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.acos(ix));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.acos(ix));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.acos(ix));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.acos(ix));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.acos(ix));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.acos(ix));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.acos(ix));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.acos(ix));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.acos(ix));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.acos(ix));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.acos(ix));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.acos(ix));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.acos(ix));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.acos(ix));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.acos(ix));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.acos(ix));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.acos(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.acos(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.acos(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.acos(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.acos(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.acos(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.acos(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.acos(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					Complex tz;
					float ox;
					float oy;
					tz = new Complex(ix, iy).acos();
					ox = (float) (tz.getReal());
					oy = (float) (tz.getImaginary());
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					Complex tz;
					float ox;
					float oy;
					tz = new Complex(ix, iy).acos();
					ox = (float) (tz.getReal());
					oy = (float) (tz.getImaginary());
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					Complex tz;
					double ox;
					double oy;
					tz = new Complex(ix, iy).acos();
					ox = (tz.getReal());
					oy = (tz.getImaginary());
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					Complex tz;
					double ox;
					double oy;
					tz = new Complex(ix, iy).acos();
					ox = (tz.getReal());
					oy = (tz.getImaginary());
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("arccos supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "arccos");
		return (AbstractDataset) result;
	}

	/**
	 * arctan - evaluate the inverse tangent function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset arctan(final Object a) {
		return arctan(a, null);
	}

	/**
	 * arctan - evaluate the inverse tangent function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset arctan(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.atan(ix));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.atan(ix));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.atan(ix));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.atan(ix));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.atan(ix));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.atan(ix));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.atan(ix));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.atan(ix));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.atan(ix));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.atan(ix));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.atan(ix));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.atan(ix));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.atan(ix));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.atan(ix));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.atan(ix));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.atan(ix));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.atan(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.atan(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.atan(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.atan(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.atan(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.atan(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.atan(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.atan(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					Complex tz;
					float ox;
					float oy;
					tz = new Complex(ix, iy).atan();
					ox = (float) (tz.getReal());
					oy = (float) (tz.getImaginary());
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					Complex tz;
					float ox;
					float oy;
					tz = new Complex(ix, iy).atan();
					ox = (float) (tz.getReal());
					oy = (float) (tz.getImaginary());
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					Complex tz;
					double ox;
					double oy;
					tz = new Complex(ix, iy).atan();
					ox = (tz.getReal());
					oy = (tz.getImaginary());
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					Complex tz;
					double ox;
					double oy;
					tz = new Complex(ix, iy).atan();
					ox = (tz.getReal());
					oy = (tz.getImaginary());
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("arctan supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "arctan");
		return (AbstractDataset) result;
	}

	/**
	 * sinh - evaluate the hyperbolic sine function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset sinh(final Object a) {
		return sinh(a, null);
	}

	/**
	 * sinh - evaluate the hyperbolic sine function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset sinh(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.sinh(ix));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.sinh(ix));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.sinh(ix));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.sinh(ix));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.sinh(ix));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.sinh(ix));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.sinh(ix));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.sinh(ix));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.sinh(ix));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.sinh(ix));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.sinh(ix));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.sinh(ix));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.sinh(ix));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.sinh(ix));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.sinh(ix));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.sinh(ix));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.sinh(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.sinh(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.sinh(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.sinh(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.sinh(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.sinh(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.sinh(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.sinh(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					float oy;
					ox = (float) (Math.sinh(ix)*Math.cos(iy));
					oy = (float) (Math.cosh(ix)*Math.sin(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					ox = (float) (Math.sinh(ix)*Math.cos(iy));
					oy = (float) (Math.cosh(ix)*Math.sin(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					double oy;
					ox = (Math.sinh(ix)*Math.cos(iy));
					oy = (Math.cosh(ix)*Math.sin(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					ox = (Math.sinh(ix)*Math.cos(iy));
					oy = (Math.cosh(ix)*Math.sin(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("sinh supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "sinh");
		return (AbstractDataset) result;
	}

	/**
	 * cosh - evaluate the hyperbolic cosine function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset cosh(final Object a) {
		return cosh(a, null);
	}

	/**
	 * cosh - evaluate the hyperbolic cosine function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset cosh(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.cosh(ix));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.cosh(ix));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.cosh(ix));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.cosh(ix));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.cosh(ix));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.cosh(ix));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.cosh(ix));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.cosh(ix));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.cosh(ix));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.cosh(ix));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.cosh(ix));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.cosh(ix));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.cosh(ix));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.cosh(ix));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.cosh(ix));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.cosh(ix));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.cosh(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.cosh(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.cosh(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.cosh(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.cosh(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.cosh(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.cosh(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.cosh(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					float oy;
					ox = (float) (Math.cosh(ix)*Math.cos(iy));
					oy = (float) (Math.sinh(ix)*Math.sin(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					ox = (float) (Math.cosh(ix)*Math.cos(iy));
					oy = (float) (Math.sinh(ix)*Math.sin(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					double oy;
					ox = (Math.cosh(ix)*Math.cos(iy));
					oy = (Math.sinh(ix)*Math.sin(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					ox = (Math.cosh(ix)*Math.cos(iy));
					oy = (Math.sinh(ix)*Math.sin(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("cosh supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "cosh");
		return (AbstractDataset) result;
	}

	/**
	 * tanh - evaluate the tangent hyperbolic function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset tanh(final Object a) {
		return tanh(a, null);
	}

	/**
	 * tanh - evaluate the tangent hyperbolic function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset tanh(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.tanh(ix));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.tanh(ix));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.tanh(ix));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.tanh(ix));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.tanh(ix));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.tanh(ix));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.tanh(ix));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.tanh(ix));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.tanh(ix));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.tanh(ix));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.tanh(ix));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.tanh(ix));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.tanh(ix));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.tanh(ix));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.tanh(ix));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.tanh(ix));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.tanh(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.tanh(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.tanh(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.tanh(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.tanh(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.tanh(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.tanh(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.tanh(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float tx;
					float ty;
					float tf;
					float ox;
					float oy;
					tx = (float) (2.*ix);
					ty = (float) (2.*iy);
					tf = (float) (1./(Math.cos(tx)+Math.cosh(ty)));
					ox = (float) (tf*Math.sinh(tx));
					oy = (float) (tf*Math.sin(ty));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float tx;
					float ty;
					float tf;
					float ox;
					float oy;
					tx = (float) (2.*ix);
					ty = (float) (2.*iy);
					tf = (float) (1./(Math.cos(tx)+Math.cosh(ty)));
					ox = (float) (tf*Math.sinh(tx));
					oy = (float) (tf*Math.sin(ty));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double tx;
					double ty;
					double tf;
					double ox;
					double oy;
					tx = (2.*ix);
					ty = (2.*iy);
					tf = (1./(Math.cos(tx)+Math.cosh(ty)));
					ox = (tf*Math.sinh(tx));
					oy = (tf*Math.sin(ty));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double tx;
					double ty;
					double tf;
					double ox;
					double oy;
					tx = (2.*ix);
					ty = (2.*iy);
					tf = (1./(Math.cos(tx)+Math.cosh(ty)));
					ox = (tf*Math.sinh(tx));
					oy = (tf*Math.sin(ty));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("tanh supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "tanh");
		return (AbstractDataset) result;
	}

	/**
	 * arcsinh - evaluate the inverse hyperbolic sine function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset arcsinh(final Object a) {
		return arcsinh(a, null);
	}

	/**
	 * arcsinh - evaluate the inverse hyperbolic sine function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset arcsinh(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.log(ix + Math.sqrt(ix*ix + 1)));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.log(ix + Math.sqrt(ix*ix + 1)));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.log(ix + Math.sqrt(ix*ix + 1)));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.log(ix + Math.sqrt(ix*ix + 1)));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.log(ix + Math.sqrt(ix*ix + 1)));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.log(ix + Math.sqrt(ix*ix + 1)));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.log(ix + Math.sqrt(ix*ix + 1)));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.log(ix + Math.sqrt(ix*ix + 1)));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.log(ix + Math.sqrt(ix*ix + 1)));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.log(ix + Math.sqrt(ix*ix + 1)));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.log(ix + Math.sqrt(ix*ix + 1)));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.log(ix + Math.sqrt(ix*ix + 1)));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.log(ix + Math.sqrt(ix*ix + 1)));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.log(ix + Math.sqrt(ix*ix + 1)));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.log(ix + Math.sqrt(ix*ix + 1)));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.log(ix + Math.sqrt(ix*ix + 1)));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.log(ix + Math.sqrt(ix*ix + 1)));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.log(ix + Math.sqrt(ix*ix + 1)));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.log(ix + Math.sqrt(ix*ix + 1)));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.log(ix + Math.sqrt(ix*ix + 1)));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.log(ix + Math.sqrt(ix*ix + 1)));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.log(ix + Math.sqrt(ix*ix + 1)));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.log(ix + Math.sqrt(ix*ix + 1)));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.log(ix + Math.sqrt(ix*ix + 1)));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					Complex tz;
					float ox;
					float oy;
					tz = new Complex(-iy, ix).asin();
					ox = (float) (tz.getImaginary());
					oy = (float) (-tz.getReal());
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					Complex tz;
					float ox;
					float oy;
					tz = new Complex(-iy, ix).asin();
					ox = (float) (tz.getImaginary());
					oy = (float) (-tz.getReal());
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					Complex tz;
					double ox;
					double oy;
					tz = new Complex(-iy, ix).asin();
					ox = (tz.getImaginary());
					oy = (-tz.getReal());
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					Complex tz;
					double ox;
					double oy;
					tz = new Complex(-iy, ix).asin();
					ox = (tz.getImaginary());
					oy = (-tz.getReal());
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("arcsinh supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "arcsinh");
		return (AbstractDataset) result;
	}

	/**
	 * arccosh - evaluate the inverse hyperbolic cosine function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset arccosh(final Object a) {
		return arccosh(a, null);
	}

	/**
	 * arccosh - evaluate the inverse hyperbolic cosine function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset arccosh(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.log(ix + Math.sqrt(ix*ix - 1)));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.log(ix + Math.sqrt(ix*ix - 1)));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.log(ix + Math.sqrt(ix*ix - 1)));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.log(ix + Math.sqrt(ix*ix - 1)));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.log(ix + Math.sqrt(ix*ix - 1)));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.log(ix + Math.sqrt(ix*ix - 1)));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.log(ix + Math.sqrt(ix*ix - 1)));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.log(ix + Math.sqrt(ix*ix - 1)));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.log(ix + Math.sqrt(ix*ix - 1)));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.log(ix + Math.sqrt(ix*ix - 1)));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.log(ix + Math.sqrt(ix*ix - 1)));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.log(ix + Math.sqrt(ix*ix - 1)));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.log(ix + Math.sqrt(ix*ix - 1)));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.log(ix + Math.sqrt(ix*ix - 1)));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.log(ix + Math.sqrt(ix*ix - 1)));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.log(ix + Math.sqrt(ix*ix - 1)));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.log(ix + Math.sqrt(ix*ix - 1)));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.log(ix + Math.sqrt(ix*ix - 1)));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.log(ix + Math.sqrt(ix*ix - 1)));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.log(ix + Math.sqrt(ix*ix - 1)));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.log(ix + Math.sqrt(ix*ix - 1)));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.log(ix + Math.sqrt(ix*ix - 1)));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.log(ix + Math.sqrt(ix*ix - 1)));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.log(ix + Math.sqrt(ix*ix - 1)));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					Complex tz;
					float ox;
					float oy;
					tz = new Complex(-iy, ix).acos();
					ox = (float) (tz.getImaginary());
					oy = (float) (-tz.getReal());
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					Complex tz;
					float ox;
					float oy;
					tz = new Complex(-iy, ix).acos();
					ox = (float) (tz.getImaginary());
					oy = (float) (-tz.getReal());
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					Complex tz;
					double ox;
					double oy;
					tz = new Complex(-iy, ix).acos();
					ox = (tz.getImaginary());
					oy = (-tz.getReal());
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					Complex tz;
					double ox;
					double oy;
					tz = new Complex(-iy, ix).acos();
					ox = (tz.getImaginary());
					oy = (-tz.getReal());
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("arccosh supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "arccosh");
		return (AbstractDataset) result;
	}

	/**
	 * arctanh - evaluate the inverse hyperbolic tangent function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset arctanh(final Object a) {
		return arctanh(a, null);
	}

	/**
	 * arctanh - evaluate the inverse hyperbolic tangent function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset arctanh(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(0.5*Math.log((1 + ix)/(1 - ix)));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(0.5*Math.log((1 + ix)/(1 - ix)));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(0.5*Math.log((1 + ix)/(1 - ix)));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(0.5*Math.log((1 + ix)/(1 - ix)));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(0.5*Math.log((1 + ix)/(1 - ix)));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(0.5*Math.log((1 + ix)/(1 - ix)));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(0.5*Math.log((1 + ix)/(1 - ix)));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(0.5*Math.log((1 + ix)/(1 - ix)));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(0.5*Math.log((1 + ix)/(1 - ix)));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(0.5*Math.log((1 + ix)/(1 - ix)));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(0.5*Math.log((1 + ix)/(1 - ix)));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(0.5*Math.log((1 + ix)/(1 - ix)));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(0.5*Math.log((1 + ix)/(1 - ix)));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(0.5*Math.log((1 + ix)/(1 - ix)));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(0.5*Math.log((1 + ix)/(1 - ix)));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(0.5*Math.log((1 + ix)/(1 - ix)));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (0.5*Math.log((1 + ix)/(1 - ix)));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (0.5*Math.log((1 + ix)/(1 - ix)));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (0.5*Math.log((1 + ix)/(1 - ix)));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (0.5*Math.log((1 + ix)/(1 - ix)));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (0.5*Math.log((1 + ix)/(1 - ix)));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (0.5*Math.log((1 + ix)/(1 - ix)));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (0.5*Math.log((1 + ix)/(1 - ix)));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (0.5*Math.log((1 + ix)/(1 - ix)));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					Complex tz;
					float ox;
					float oy;
					tz = new Complex(-iy, ix).atan();
					ox = (float) (tz.getImaginary());
					oy = (float) (-tz.getReal());
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					Complex tz;
					float ox;
					float oy;
					tz = new Complex(-iy, ix).atan();
					ox = (float) (tz.getImaginary());
					oy = (float) (-tz.getReal());
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					Complex tz;
					double ox;
					double oy;
					tz = new Complex(-iy, ix).atan();
					ox = (tz.getImaginary());
					oy = (-tz.getReal());
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					Complex tz;
					double ox;
					double oy;
					tz = new Complex(-iy, ix).atan();
					ox = (tz.getImaginary());
					oy = (-tz.getReal());
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("arctanh supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "arctanh");
		return (AbstractDataset) result;
	}

	/**
	 * log - evaluate the logarithm function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset log(final Object a) {
		return log(a, null);
	}

	/**
	 * log - evaluate the logarithm function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset log(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.log(ix));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.log(ix));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.log(ix));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.log(ix));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.log(ix));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.log(ix));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.log(ix));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.log(ix));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.log(ix));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.log(ix));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.log(ix));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.log(ix));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.log(ix));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.log(ix));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.log(ix));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.log(ix));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.log(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.log(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.log(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.log(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.log(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.log(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.log(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.log(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					float oy;
					ox = (float) (Math.log(Math.hypot(ix, iy)));
					oy = (float) (Math.atan2(iy, ix));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					ox = (float) (Math.log(Math.hypot(ix, iy)));
					oy = (float) (Math.atan2(iy, ix));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					double oy;
					ox = (Math.log(Math.hypot(ix, iy)));
					oy = (Math.atan2(iy, ix));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					ox = (Math.log(Math.hypot(ix, iy)));
					oy = (Math.atan2(iy, ix));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("log supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "log");
		return (AbstractDataset) result;
	}

	/**
	 * log2 - evaluate the logarithm function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset log2(final Object a) {
		return log2(a, null);
	}

	/**
	 * log2 - evaluate the logarithm function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset log2(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.log(ix)/Math.log(2.));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.log(ix)/Math.log(2.));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.log(ix)/Math.log(2.));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.log(ix)/Math.log(2.));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.log(ix)/Math.log(2.));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.log(ix)/Math.log(2.));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.log(ix)/Math.log(2.));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.log(ix)/Math.log(2.));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.log(ix)/Math.log(2.));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.log(ix)/Math.log(2.));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.log(ix)/Math.log(2.));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.log(ix)/Math.log(2.));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.log(ix)/Math.log(2.));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.log(ix)/Math.log(2.));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.log(ix)/Math.log(2.));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.log(ix)/Math.log(2.));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.log(ix)/Math.log(2.));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.log(ix)/Math.log(2.));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.log(ix)/Math.log(2.));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.log(ix)/Math.log(2.));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.log(ix)/Math.log(2.));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.log(ix)/Math.log(2.));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.log(ix)/Math.log(2.));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.log(ix)/Math.log(2.));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					float oy;
					ox = (float) (Math.log(Math.hypot(ix, iy))/Math.log(2.));
					oy = (float) (Math.atan2(iy, ix));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					ox = (float) (Math.log(Math.hypot(ix, iy))/Math.log(2.));
					oy = (float) (Math.atan2(iy, ix));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					double oy;
					ox = (Math.log(Math.hypot(ix, iy))/Math.log(2.));
					oy = (Math.atan2(iy, ix));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					ox = (Math.log(Math.hypot(ix, iy))/Math.log(2.));
					oy = (Math.atan2(iy, ix));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("log2 supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "log2");
		return (AbstractDataset) result;
	}

	/**
	 * log10 - evaluate the logarithm function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset log10(final Object a) {
		return log10(a, null);
	}

	/**
	 * log10 - evaluate the logarithm function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset log10(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.log10(ix));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.log10(ix));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.log10(ix));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.log10(ix));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.log10(ix));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.log10(ix));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.log10(ix));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.log10(ix));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.log10(ix));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.log10(ix));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.log10(ix));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.log10(ix));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.log10(ix));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.log10(ix));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.log10(ix));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.log10(ix));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.log10(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.log10(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.log10(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.log10(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.log10(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.log10(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.log10(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.log10(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					float oy;
					ox = (float) (Math.log10(Math.hypot(ix, iy)));
					oy = (float) (Math.atan2(iy, ix));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					ox = (float) (Math.log10(Math.hypot(ix, iy)));
					oy = (float) (Math.atan2(iy, ix));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					double oy;
					ox = (Math.log10(Math.hypot(ix, iy)));
					oy = (Math.atan2(iy, ix));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					ox = (Math.log10(Math.hypot(ix, iy)));
					oy = (Math.atan2(iy, ix));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("log10 supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "log10");
		return (AbstractDataset) result;
	}

	/**
	 * log1p - evaluate the logarithm function of 1 plus on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset log1p(final Object a) {
		return log1p(a, null);
	}

	/**
	 * log1p - evaluate the logarithm function of 1 plus on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset log1p(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.log1p(ix));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.log1p(ix));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.log1p(ix));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.log1p(ix));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.log1p(ix));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.log1p(ix));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.log1p(ix));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.log1p(ix));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.log1p(ix));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.log1p(ix));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.log1p(ix));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.log1p(ix));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.log1p(ix));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.log1p(ix));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.log1p(ix));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.log1p(ix));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.log1p(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.log1p(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.log1p(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.log1p(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.log1p(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.log1p(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.log1p(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.log1p(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					float oy;
					ox = (float) (0.5*Math.log1p(ix*ix + 2.*ix + iy*iy));
					oy = (float) (Math.atan2(iy, ix+1));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					ox = (float) (0.5*Math.log1p(ix*ix + 2.*ix + iy*iy));
					oy = (float) (Math.atan2(iy, ix+1));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					double oy;
					ox = (0.5*Math.log1p(ix*ix + 2.*ix + iy*iy));
					oy = (Math.atan2(iy, ix+1));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					ox = (0.5*Math.log1p(ix*ix + 2.*ix + iy*iy));
					oy = (Math.atan2(iy, ix+1));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("log1p supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "log1p");
		return (AbstractDataset) result;
	}

	/**
	 * exp - evaluate the exponential function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset exp(final Object a) {
		return exp(a, null);
	}

	/**
	 * exp - evaluate the exponential function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset exp(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.exp(ix));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.exp(ix));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.exp(ix));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.exp(ix));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.exp(ix));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.exp(ix));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.exp(ix));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.exp(ix));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.exp(ix));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.exp(ix));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.exp(ix));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.exp(ix));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.exp(ix));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.exp(ix));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.exp(ix));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.exp(ix));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.exp(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.exp(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.exp(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.exp(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.exp(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.exp(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.exp(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.exp(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float tf;
					float ox;
					float oy;
					tf = (float) (Math.exp(ix));
					ox = (float) (tf*Math.cos(iy));
					oy = (float) (tf*Math.sin(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float tf;
					float ox;
					float oy;
					tf = (float) (Math.exp(ix));
					ox = (float) (tf*Math.cos(iy));
					oy = (float) (tf*Math.sin(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double tf;
					double ox;
					double oy;
					tf = (Math.exp(ix));
					ox = (tf*Math.cos(iy));
					oy = (tf*Math.sin(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double tf;
					double ox;
					double oy;
					tf = (Math.exp(ix));
					ox = (tf*Math.cos(iy));
					oy = (tf*Math.sin(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("exp supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "exp");
		return (AbstractDataset) result;
	}

	/**
	 * expm1 - evaluate the exponential function - 1 on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset expm1(final Object a) {
		return expm1(a, null);
	}

	/**
	 * expm1 - evaluate the exponential function - 1 on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset expm1(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.expm1(ix));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.expm1(ix));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.expm1(ix));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.expm1(ix));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.expm1(ix));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.expm1(ix));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.expm1(ix));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.expm1(ix));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.expm1(ix));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.expm1(ix));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.expm1(ix));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.expm1(ix));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.expm1(ix));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.expm1(ix));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.expm1(ix));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.expm1(ix));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.expm1(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.expm1(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.expm1(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.expm1(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.expm1(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.expm1(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.expm1(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.expm1(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float tf;
					float ox;
					float oy;
					tf = (float) (Math.expm1(ix));
					ox = (float) (tf*Math.cos(iy));
					oy = (float) (tf*Math.sin(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float tf;
					float ox;
					float oy;
					tf = (float) (Math.expm1(ix));
					ox = (float) (tf*Math.cos(iy));
					oy = (float) (tf*Math.sin(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double tf;
					double ox;
					double oy;
					tf = (Math.expm1(ix));
					ox = (tf*Math.cos(iy));
					oy = (tf*Math.sin(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double tf;
					double ox;
					double oy;
					tf = (Math.expm1(ix));
					ox = (tf*Math.cos(iy));
					oy = (tf*Math.sin(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("expm1 supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "expm1");
		return (AbstractDataset) result;
	}

	/**
	 * sqrt - evaluate the square root function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset sqrt(final Object a) {
		return sqrt(a, null);
	}

	/**
	 * sqrt - evaluate the square root function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset sqrt(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.sqrt(ix));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.sqrt(ix));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.sqrt(ix));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.sqrt(ix));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.sqrt(ix));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.sqrt(ix));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.sqrt(ix));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.sqrt(ix));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.sqrt(ix));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.sqrt(ix));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.sqrt(ix));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.sqrt(ix));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.sqrt(ix));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.sqrt(ix));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.sqrt(ix));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.sqrt(ix));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.sqrt(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.sqrt(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.sqrt(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.sqrt(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.sqrt(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.sqrt(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.sqrt(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.sqrt(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					Complex tz;
					float ox;
					float oy;
					tz = new Complex(ix, iy).sqrt();
					ox = (float) (tz.getReal());
					oy = (float) (tz.getImaginary());
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					Complex tz;
					float ox;
					float oy;
					tz = new Complex(ix, iy).sqrt();
					ox = (float) (tz.getReal());
					oy = (float) (tz.getImaginary());
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					Complex tz;
					double ox;
					double oy;
					tz = new Complex(ix, iy).sqrt();
					ox = (tz.getReal());
					oy = (tz.getImaginary());
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					Complex tz;
					double ox;
					double oy;
					tz = new Complex(ix, iy).sqrt();
					ox = (tz.getReal());
					oy = (tz.getImaginary());
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("sqrt supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "sqrt");
		return (AbstractDataset) result;
	}

	/**
	 * cbrt - evaluate the cube root function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset cbrt(final Object a) {
		return cbrt(a, null);
	}

	/**
	 * cbrt - evaluate the cube root function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset cbrt(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.cbrt(ix));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.cbrt(ix));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.cbrt(ix));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.cbrt(ix));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.cbrt(ix));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.cbrt(ix));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.cbrt(ix));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.cbrt(ix));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.cbrt(ix));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.cbrt(ix));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.cbrt(ix));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.cbrt(ix));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.cbrt(ix));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.cbrt(ix));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.cbrt(ix));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.cbrt(ix));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.cbrt(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.cbrt(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.cbrt(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.cbrt(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.cbrt(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.cbrt(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.cbrt(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.cbrt(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					Complex tz;
					float ox;
					float oy;
					tz = new Complex(ix, iy).pow(new Complex(1./3.,0));
					ox = (float) (tz.getReal());
					oy = (float) (tz.getImaginary());
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					Complex tz;
					float ox;
					float oy;
					tz = new Complex(ix, iy).pow(new Complex(1./3.,0));
					ox = (float) (tz.getReal());
					oy = (float) (tz.getImaginary());
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					Complex tz;
					double ox;
					double oy;
					tz = new Complex(ix, iy).pow(new Complex(1./3.,0));
					ox = (tz.getReal());
					oy = (tz.getImaginary());
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					Complex tz;
					double ox;
					double oy;
					tz = new Complex(ix, iy).pow(new Complex(1./3.,0));
					ox = (tz.getReal());
					oy = (tz.getImaginary());
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("cbrt supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "cbrt");
		return (AbstractDataset) result;
	}

	/**
	 * square - square each element
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset square(final Object a) {
		return square(a, null);
	}

	/**
	 * square - square each element
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset square(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(ix*ix);
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(ix*ix);
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(ix*ix);
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(ix*ix);
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(ix*ix);
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(ix*ix);
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(ix*ix);
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(ix*ix);
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(ix*ix);
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(ix*ix);
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(ix*ix);
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(ix*ix);
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(ix*ix);
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(ix*ix);
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(ix*ix);
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(ix*ix);
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (ix*ix);
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (ix*ix);
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (ix*ix);
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (ix*ix);
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (ix*ix);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (ix*ix);
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (ix*ix);
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (ix*ix);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					float oy;
					ox = (float) (ix*ix - iy*iy);
					oy = (float) (2.*ix*iy);
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					ox = (float) (ix*ix - iy*iy);
					oy = (float) (2.*ix*iy);
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					double oy;
					ox = (ix*ix - iy*iy);
					oy = (2.*ix*iy);
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					ox = (ix*ix - iy*iy);
					oy = (2.*ix*iy);
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("square supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "square");
		return (AbstractDataset) result;
	}

	/**
	 * floor - evaluate the floor function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset floor(final Object a) {
		return floor(a, null);
	}

	/**
	 * floor - evaluate the floor function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset floor(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				byte ox;
				ox = (byte) toLong(ix);
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				short ox;
				ox = (short) toLong(ix);
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				long ox;
				ox = toLong(ix);
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				int ox;
				ox = (int) toLong(ix);
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					byte ox;
					ox = (byte) toLong(ix);
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					byte ox;
					ox = (byte) toLong(ix);
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(ix);
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					short ox;
					ox = (short) toLong(ix);
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					short ox;
					ox = (short) toLong(ix);
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(ix);
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					long ox;
					ox = toLong(ix);
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					long ox;
					ox = toLong(ix);
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						long ox;
						ox = toLong(ix);
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					int ox;
					ox = (int) toLong(ix);
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					int ox;
					ox = (int) toLong(ix);
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(ix);
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.floor(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.floor(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.floor(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.floor(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.floor(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.floor(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.floor(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.floor(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					float oy;
					ox = (float) (Math.floor(ix));
					oy = (float) (Math.floor(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					ox = (float) (Math.floor(ix));
					oy = (float) (Math.floor(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					double oy;
					ox = (Math.floor(ix));
					oy = (Math.floor(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					ox = (Math.floor(ix));
					oy = (Math.floor(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("floor supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "floor");
		return (AbstractDataset) result;
	}

	/**
	 * ceil - evaluate the ceiling function on each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset ceil(final Object a) {
		return ceil(a, null);
	}

	/**
	 * ceil - evaluate the ceiling function on each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset ceil(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				byte ox;
				ox = (byte) toLong(ix);
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				short ox;
				ox = (short) toLong(ix);
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				long ox;
				ox = toLong(ix);
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				int ox;
				ox = (int) toLong(ix);
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					byte ox;
					ox = (byte) toLong(ix);
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					byte ox;
					ox = (byte) toLong(ix);
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(ix);
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					short ox;
					ox = (short) toLong(ix);
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					short ox;
					ox = (short) toLong(ix);
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(ix);
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					long ox;
					ox = toLong(ix);
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					long ox;
					ox = toLong(ix);
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						long ox;
						ox = toLong(ix);
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					int ox;
					ox = (int) toLong(ix);
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					int ox;
					ox = (int) toLong(ix);
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(ix);
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.ceil(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.ceil(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.ceil(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.ceil(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.ceil(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.ceil(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.ceil(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.ceil(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					float oy;
					ox = (float) (Math.ceil(ix));
					oy = (float) (Math.ceil(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					ox = (float) (Math.ceil(ix));
					oy = (float) (Math.ceil(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					double oy;
					ox = (Math.ceil(ix));
					oy = (Math.ceil(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					ox = (Math.ceil(ix));
					oy = (Math.ceil(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("ceil supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "ceil");
		return (AbstractDataset) result;
	}

	/**
	 * rint - round each element of the dataset
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset rint(final Object a) {
		return rint(a, null);
	}

	/**
	 * rint - round each element of the dataset
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset rint(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				byte ox;
				ox = (byte) toLong(ix);
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				short ox;
				ox = (short) toLong(ix);
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				long ox;
				ox = toLong(ix);
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				int ox;
				ox = (int) toLong(ix);
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					byte ox;
					ox = (byte) toLong(ix);
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					byte ox;
					ox = (byte) toLong(ix);
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(ix);
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					short ox;
					ox = (short) toLong(ix);
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					short ox;
					ox = (short) toLong(ix);
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(ix);
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					long ox;
					ox = toLong(ix);
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					long ox;
					ox = toLong(ix);
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						long ox;
						ox = toLong(ix);
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					int ox;
					ox = (int) toLong(ix);
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					int ox;
					ox = (int) toLong(ix);
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(ix);
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.rint(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.rint(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.rint(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.rint(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.rint(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.rint(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.rint(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.rint(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					float oy;
					ox = (float) (Math.rint(ix));
					oy = (float) (Math.rint(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					ox = (float) (Math.rint(ix));
					oy = (float) (Math.rint(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					double oy;
					ox = (Math.rint(ix));
					oy = (Math.rint(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					ox = (Math.rint(ix));
					oy = (Math.rint(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("rint supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "rint");
		return (AbstractDataset) result;
	}

	/**
	 * toDegrees - convert to degrees
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset toDegrees(final Object a) {
		return toDegrees(a, null);
	}

	/**
	 * toDegrees - convert to degrees
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset toDegrees(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.toDegrees(ix));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.toDegrees(ix));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.toDegrees(ix));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.toDegrees(ix));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.toDegrees(ix));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.toDegrees(ix));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.toDegrees(ix));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.toDegrees(ix));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.toDegrees(ix));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.toDegrees(ix));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.toDegrees(ix));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.toDegrees(ix));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.toDegrees(ix));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.toDegrees(ix));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.toDegrees(ix));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.toDegrees(ix));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.toDegrees(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.toDegrees(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.toDegrees(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.toDegrees(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.toDegrees(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.toDegrees(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.toDegrees(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.toDegrees(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					float oy;
					ox = (float) (Math.toDegrees(ix));
					oy = (float) (Math.toDegrees(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					ox = (float) (Math.toDegrees(ix));
					oy = (float) (Math.toDegrees(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					double oy;
					ox = (Math.toDegrees(ix));
					oy = (Math.toDegrees(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					ox = (Math.toDegrees(ix));
					oy = (Math.toDegrees(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("toDegrees supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "toDegrees");
		return (AbstractDataset) result;
	}

	/**
	 * toRadians - convert to radians
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset toRadians(final Object a) {
		return toRadians(a, null);
	}

	/**
	 * toRadians - convert to radians
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset toRadians(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				ox = (byte) toLong(Math.toRadians(ix));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				ox = (short) toLong(Math.toRadians(ix));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				ox = toLong(Math.toRadians(ix));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				ox = (int) toLong(Math.toRadians(ix));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.toRadians(ix));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					ox = (byte) toLong(Math.toRadians(ix));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(Math.toRadians(ix));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.toRadians(ix));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					ox = (short) toLong(Math.toRadians(ix));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(Math.toRadians(ix));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.toRadians(ix));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					ox = toLong(Math.toRadians(ix));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						ox = toLong(Math.toRadians(ix));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.toRadians(ix));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					ox = (int) toLong(Math.toRadians(ix));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(Math.toRadians(ix));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.toRadians(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.toRadians(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.toRadians(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.toRadians(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.toRadians(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.toRadians(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.toRadians(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.toRadians(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					float oy;
					ox = (float) (Math.toRadians(ix));
					oy = (float) (Math.toRadians(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					ox = (float) (Math.toRadians(ix));
					oy = (float) (Math.toRadians(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					double oy;
					ox = (Math.toRadians(ix));
					oy = (Math.toRadians(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					ox = (Math.toRadians(ix));
					oy = (Math.toRadians(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("toRadians supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "toRadians");
		return (AbstractDataset) result;
	}

	/**
	 * signum - sign of each element
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset signum(final Object a) {
		return signum(a, null);
	}

	/**
	 * signum - sign of each element
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset signum(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				byte ox;
				ox = (byte) toLong(ix > 0 ? 1 : (ix < 0 ? -1 : 0));
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				short ox;
				ox = (short) toLong(ix > 0 ? 1 : (ix < 0 ? -1 : 0));
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				long ox;
				ox = toLong(ix > 0 ? 1 : (ix < 0 ? -1 : 0));
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				int ox;
				ox = (int) toLong(ix > 0 ? 1 : (ix < 0 ? -1 : 0));
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					byte ox;
					ox = (byte) toLong(ix > 0 ? 1 : (ix < 0 ? -1 : 0));
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					byte ox;
					ox = (byte) toLong(ix > 0 ? 1 : (ix < 0 ? -1 : 0));
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(ix > 0 ? 1 : (ix < 0 ? -1 : 0));
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					short ox;
					ox = (short) toLong(ix > 0 ? 1 : (ix < 0 ? -1 : 0));
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					short ox;
					ox = (short) toLong(ix > 0 ? 1 : (ix < 0 ? -1 : 0));
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(ix > 0 ? 1 : (ix < 0 ? -1 : 0));
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					long ox;
					ox = toLong(ix > 0 ? 1 : (ix < 0 ? -1 : 0));
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					long ox;
					ox = toLong(ix > 0 ? 1 : (ix < 0 ? -1 : 0));
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						long ox;
						ox = toLong(ix > 0 ? 1 : (ix < 0 ? -1 : 0));
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					int ox;
					ox = (int) toLong(ix > 0 ? 1 : (ix < 0 ? -1 : 0));
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					int ox;
					ox = (int) toLong(ix > 0 ? 1 : (ix < 0 ? -1 : 0));
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(ix > 0 ? 1 : (ix < 0 ? -1 : 0));
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (Math.signum(ix));
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (Math.signum(ix));
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.signum(ix));
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (Math.signum(ix));
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (Math.signum(ix));
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.signum(ix));
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (Math.signum(ix));
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (Math.signum(ix));
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					float oy;
					ox = (float) (Math.signum(ix));
					oy = (float) (Math.signum(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					ox = (float) (Math.signum(ix));
					oy = (float) (Math.signum(iy));
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					double oy;
					ox = (Math.signum(ix));
					oy = (Math.signum(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					ox = (Math.signum(ix));
					oy = (Math.signum(iy));
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("signum supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "signum");
		return (AbstractDataset) result;
	}

	/**
	 * negative - negative value of each element
	 * @param a
	 * @return dataset
	 */
	public static AbstractDataset negative(final Object a) {
		return negative(a, null);
	}

	/**
	 * negative - negative value of each element
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @return dataset
	 */
	public static AbstractDataset negative(final Object a, final Dataset o) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				byte ox;
				ox = (byte) toLong(-ix);
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				short ox;
				ox = (short) toLong(-ix);
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				long ox;
				ox = toLong(-ix);
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			it.setDoubleOutput(false);

			while (it.hasNext()) {
				final long ix = it.aLong;
				int ox;
				ox = (int) toLong(-ix);
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					byte ox;
					ox = (byte) toLong(-ix);
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					byte ox;
					ox = (byte) toLong(-ix);
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						byte ox;
						ox = (byte) toLong(-ix);
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					short ox;
					ox = (short) toLong(-ix);
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					short ox;
					ox = (short) toLong(-ix);
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						short ox;
						ox = (short) toLong(-ix);
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					long ox;
					ox = toLong(-ix);
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					long ox;
					ox = toLong(-ix);
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						long ox;
						ox = toLong(-ix);
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			it.setDoubleOutput(false);

			if (is == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					int ox;
					ox = (int) toLong(-ix);
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final long ix = it.aLong;
					int ox;
					ox = (int) toLong(-ix);
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final long ix = da.getElementLongAbs(it.aIndex + j);
						int ox;
						ox = (int) toLong(-ix);
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				ox = (float) (-ix);
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				ox = (-ix);
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (-ix);
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					ox = (float) (-ix);
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						ox = (float) (-ix);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (-ix);
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					ox = (-ix);
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						ox = (-ix);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.COMPLEX64:
			final float[] oc64data = ((ComplexFloatDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					float oy;
					ox = (float) (-ix);
					oy = (float) (-iy);
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					float ox;
					float oy;
					ox = (float) (-ix);
					oy = (float) (-iy);
					oc64data[it.oIndex] = ox;
					oc64data[it.oIndex + 1] = oy;
				}
			}
			break;
		case Dataset.COMPLEX128:
			final double[] oc128data = ((ComplexDoubleDataset) result).data;
			if (da.getElementsPerItem() == 1) {
				final double iy = 0;
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					double oy;
					ox = (-ix);
					oy = (-iy);
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			} else {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					final double iy = da.getElementDoubleAbs(it.aIndex + 1);
					double ox;
					double oy;
					ox = (-ix);
					oy = (-iy);
					oc128data[it.oIndex] = ox;
					oc128data[it.oIndex + 1] = oy;
				}
			}
			break;
		default:
			throw new IllegalArgumentException("negative supports integer, compound integer, real, compound real, complex datasets only");
		}

		addFunctionName(result, "negative");
		return (AbstractDataset) result;
	}

	/**
	 * clip - clip elements to limits
	 * @param a
	 * @param pa
	 * @param pb
	 * @return dataset
	 */
	public static AbstractDataset clip(final Object a, final Object pa, final Object pb) {
		return clip(a, null, pa, pb);
	}

	/**
	 * clip - clip elements to limits
	 * @param a
	 * @param o output can be null - in which case, a new dataset is created
	 * @param pa
	 * @param pb
	 * @return dataset
	 */
	public static AbstractDataset clip(final Object a, final Dataset o, final Object pa, final Object pb) {
		final Dataset da = a instanceof Dataset ? (Dataset) a : DatasetFactory.createFromObject(a);
		final SingleInputBroadcastIterator it = new SingleInputBroadcastIterator(da, o, true);
		final Dataset result = it.getOutput();
		final int is = result.getElementsPerItem();
		final int dt = result.getDtype();
		final double pax = AbstractDataset.toReal(pa);
		final double pbx = AbstractDataset.toReal(pb);

		switch(dt) {
		case Dataset.INT8:
			final byte[] oi8data = ((ByteDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				byte ox;
				if (ix < pax)
					ox = (byte) toLong(pax);
				else if (ix > pbx)
					ox = (byte) toLong(pbx);
				else
					ox = (byte) toLong(ix);
				oi8data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT16:
			final short[] oi16data = ((ShortDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				short ox;
				if (ix < pax)
					ox = (short) toLong(pax);
				else if (ix > pbx)
					ox = (short) toLong(pbx);
				else
					ox = (short) toLong(ix);
				oi16data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT64:
			final long[] oi64data = ((LongDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				long ox;
				if (ix < pax)
					ox = toLong(pax);
				else if (ix > pbx)
					ox = toLong(pbx);
				else
					ox = toLong(ix);
				oi64data[it.oIndex] = ox;
			}
			break;
		case Dataset.INT32:
			final int[] oi32data = ((IntegerDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				int ox;
				if (ix < pax)
					ox = (int) toLong(pax);
				else if (ix > pbx)
					ox = (int) toLong(pbx);
				else
					ox = (int) toLong(ix);
				oi32data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYINT8:
			final byte[] oai8data = ((CompoundByteDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					if (ix < pax)
						ox = (byte) toLong(pax);
					else if (ix > pbx)
						ox = (byte) toLong(pbx);
					else
						ox = (byte) toLong(ix);
					oai8data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					byte ox;
					if (ix < pax)
						ox = (byte) toLong(pax);
					else if (ix > pbx)
						ox = (byte) toLong(pbx);
					else
						ox = (byte) toLong(ix);
					for (int j = 0; j < is; j++) {
						oai8data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						byte ox;
						if (ix < pax)
							ox = (byte) toLong(pax);
						else if (ix > pbx)
							ox = (byte) toLong(pbx);
						else
							ox = (byte) toLong(ix);
						oai8data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT16:
			final short[] oai16data = ((CompoundShortDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					if (ix < pax)
						ox = (short) toLong(pax);
					else if (ix > pbx)
						ox = (short) toLong(pbx);
					else
						ox = (short) toLong(ix);
					oai16data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					short ox;
					if (ix < pax)
						ox = (short) toLong(pax);
					else if (ix > pbx)
						ox = (short) toLong(pbx);
					else
						ox = (short) toLong(ix);
					for (int j = 0; j < is; j++) {
						oai16data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						short ox;
						if (ix < pax)
							ox = (short) toLong(pax);
						else if (ix > pbx)
							ox = (short) toLong(pbx);
						else
							ox = (short) toLong(ix);
						oai16data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT64:
			final long[] oai64data = ((CompoundLongDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					if (ix < pax)
						ox = toLong(pax);
					else if (ix > pbx)
						ox = toLong(pbx);
					else
						ox = toLong(ix);
					oai64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					long ox;
					if (ix < pax)
						ox = toLong(pax);
					else if (ix > pbx)
						ox = toLong(pbx);
					else
						ox = toLong(ix);
					for (int j = 0; j < is; j++) {
						oai64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						long ox;
						if (ix < pax)
							ox = toLong(pax);
						else if (ix > pbx)
							ox = toLong(pbx);
						else
							ox = toLong(ix);
						oai64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYINT32:
			final int[] oai32data = ((CompoundIntegerDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					if (ix < pax)
						ox = (int) toLong(pax);
					else if (ix > pbx)
						ox = (int) toLong(pbx);
					else
						ox = (int) toLong(ix);
					oai32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					int ox;
					if (ix < pax)
						ox = (int) toLong(pax);
					else if (ix > pbx)
						ox = (int) toLong(pbx);
					else
						ox = (int) toLong(ix);
					for (int j = 0; j < is; j++) {
						oai32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						int ox;
						if (ix < pax)
							ox = (int) toLong(pax);
						else if (ix > pbx)
							ox = (int) toLong(pbx);
						else
							ox = (int) toLong(ix);
						oai32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.FLOAT32:
			final float[] of32data = ((FloatDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				float ox;
				if (Double.isNaN(ix))
					ox = (float) ((pax+pbx)/2.);
				else if (ix < pax)
					ox = (float) (pax);
				else if (ix > pbx)
					ox = (float) (pbx);
				else
					ox = (float) (ix);
				of32data[it.oIndex] = ox;
			}
			break;
		case Dataset.FLOAT64:
			final double[] of64data = ((DoubleDataset) result).data;
			while (it.hasNext()) {
				final double ix = it.aDouble;
				double ox;
				if (Double.isNaN(ix))
					ox = ((pax+pbx)/2.);
				else if (ix < pax)
					ox = (pax);
				else if (ix > pbx)
					ox = (pbx);
				else
					ox = (ix);
				of64data[it.oIndex] = ox;
			}
			break;
		case Dataset.ARRAYFLOAT32:
			final float[] oaf32data = ((CompoundFloatDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					if (Double.isNaN(ix))
						ox = (float) ((pax+pbx)/2.);
					else if (ix < pax)
						ox = (float) (pax);
					else if (ix > pbx)
						ox = (float) (pbx);
					else
						ox = (float) (ix);
					oaf32data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					float ox;
					if (Double.isNaN(ix))
						ox = (float) ((pax+pbx)/2.);
					else if (ix < pax)
						ox = (float) (pax);
					else if (ix > pbx)
						ox = (float) (pbx);
					else
						ox = (float) (ix);
					for (int j = 0; j < is; j++) {
						oaf32data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						float ox;
						if (Double.isNaN(ix))
							ox = (float) ((pax+pbx)/2.);
						else if (ix < pax)
							ox = (float) (pax);
						else if (ix > pbx)
							ox = (float) (pbx);
						else
							ox = (float) (ix);
						oaf32data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		case Dataset.ARRAYFLOAT64:
			final double[] oaf64data = ((CompoundDoubleDataset) result).data;
			if (is == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					if (Double.isNaN(ix))
						ox = ((pax+pbx)/2.);
					else if (ix < pax)
						ox = (pax);
					else if (ix > pbx)
						ox = (pbx);
					else
						ox = (ix);
					oaf64data[it.oIndex] = ox;
				}
			} else if (da.getElementsPerItem() == 1) {
				while (it.hasNext()) {
					final double ix = it.aDouble;
					double ox;
					if (Double.isNaN(ix))
						ox = ((pax+pbx)/2.);
					else if (ix < pax)
						ox = (pax);
					else if (ix > pbx)
						ox = (pbx);
					else
						ox = (ix);
					for (int j = 0; j < is; j++) {
						oaf64data[it.oIndex + j] = ox;
					}
				}
			} else {
				while (it.hasNext()) {
					for (int j = 0; j < is; j++) {
						final double ix = da.getElementDoubleAbs(it.aIndex + j);
						double ox;
						if (Double.isNaN(ix))
							ox = ((pax+pbx)/2.);
						else if (ix < pax)
							ox = (pax);
						else if (ix > pbx)
							ox = (pbx);
						else
							ox = (ix);
						oaf64data[it.oIndex + j] = ox;
					}
				}
			}
			break;
		default:
			throw new IllegalArgumentException("clip supports integer, compound integer, real, compound real datasets only");
		}

		addFunctionName(result, "clip");
		return (AbstractDataset) result;
	}

// End of generated code
}
