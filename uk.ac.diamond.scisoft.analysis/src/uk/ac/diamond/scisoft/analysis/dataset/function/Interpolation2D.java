/*-
 * Copyright 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.dataset.function;

import org.apache.commons.math3.analysis.BivariateFunction;
import org.apache.commons.math3.analysis.interpolation.BicubicInterpolator;
import org.apache.commons.math3.analysis.interpolation.BivariateGridInterpolator;
import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolator;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;

public class Interpolation2D {
	
	public static Dataset bicubicInterpolation(IDataset oldx, IDataset oldy, IDataset oldxy, IDataset newx, IDataset newy) {
		
		return interpolate(oldx, oldy, oldxy, newx, newy, new BicubicInterpolator());
		
	}
	
	public static Dataset piecewiseBicubicSplineInterpolation(IDataset oldx, IDataset oldy, IDataset oldxy, IDataset newx, IDataset newy) {
		
		return interpolate(oldx,oldy, oldxy, newx, newy, new PiecewiseBicubicSplineInterpolator());
		
	}
	
	public static Dataset interpolate(IDataset oldx, IDataset oldy, IDataset oldxy, IDataset newx, IDataset newy, BivariateGridInterpolator interpolator) throws NonMonotonicSequenceException, NumberIsTooSmallException {
		
		//check shapes
		if (oldx.getRank() != 1)
			throw new IllegalArgumentException("oldx Shape must be 1D");
		if (oldy.getRank() != 1)
			throw new IllegalArgumentException("oldy Shape must be 1D");
		if (oldxy.getRank() != 2)
			throw new IllegalArgumentException("oldxy Shape must be 2D");
		if (oldx.getShape()[0] != oldxy.getShape()[0])
			throw new IllegalArgumentException("oldx Shape must match oldxy Shape[0]");
		if (oldy.getShape()[0] != oldxy.getShape()[1])
			throw new IllegalArgumentException("oldy Shape must match oldxy Shape[1]");
		if (newx.getRank() != 1)
			throw new IllegalArgumentException("newx Shape must be 1D");
		if (newy.getRank() != 1)
			throw new IllegalArgumentException("newx Shape must be 1D");
		if (newy.getSize() != newx.getSize())
			throw new IllegalArgumentException("newx and newy Size must be identical");
		
		DoubleDataset oldx_dd = (DoubleDataset)DatasetUtils.cast(oldx,Dataset.FLOAT64);
		DoubleDataset oldy_dd = (DoubleDataset)DatasetUtils.cast(oldy,Dataset.FLOAT64);
		DoubleDataset oldxy_dd = (DoubleDataset)DatasetUtils.cast(oldxy,Dataset.FLOAT64);

		//unlike in Interpolation1D, we will not be sorting here, as it just too complicated
		//the user will be responsible for ensuring the arrays are properly sorted
		
		//oldxy_dd needs to be transformed into a double[][] array
		//this call may throw an exception that needs handling by the calling method
		BivariateFunction func = interpolator.interpolate(oldx_dd.getData(), oldy_dd.getData(), convertDoubleDataset2DtoPrimitive(oldxy_dd));
		
		Dataset rv = DatasetFactory.zeros(new int[]{newx.getSize()}, Dataset.FLOAT64);
		rv.setName(oldxy.getName()+"_interpolated");
		
		for (int i = 0 ; i < newx.getSize() ; i++) {
			double val = 0.0;
			try {
				val = func.value(newx.getDouble(i), newy.getDouble(i));
				rv.set(val, i);
			} catch (OutOfRangeException e) {
				rv.set(0.0, i);
			}
		}
		
		return rv;
	}
	
	private static double[][] convertDoubleDataset2DtoPrimitive(DoubleDataset dataset) {
		if (dataset.getRank() != 2)
			throw new IllegalArgumentException("dataset Shape must be 2D");
		
		double[][] rv = new double[dataset.getShape()[0]][dataset.getShape()[1]];
		
		for (int row = 0 ; row < dataset.getShape()[0] ; row++) {
			System.arraycopy(dataset.getData(), row * dataset.getShape()[1], rv[row], 0, dataset.getShape()[1]);
		}
		
		return rv;
	}
}
