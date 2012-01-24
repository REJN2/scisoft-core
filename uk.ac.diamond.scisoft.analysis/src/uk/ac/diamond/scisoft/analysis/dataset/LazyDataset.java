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

package uk.ac.diamond.scisoft.analysis.dataset;

import gda.analysis.io.ScanFileHolderException;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.ILazyLoader;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.gda.monitor.IMonitor;

/**
 * Class that implements lazy dataset interface
 */
public class LazyDataset implements ILazyDataset {
	private static final long serialVersionUID = -903717887381144620L;
	transient protected static final Logger logger = LoggerFactory.getLogger(LazyDataset.class);

	protected String name;
	protected int[] shape;
	protected int size; // number of items, this can be smaller than dataSize for discontiguous datasets
	protected ILazyLoader loader;
	private int dtype;

	/**
	 * Create a lazy dataset
	 * @param name
	 * @param dtype dataset type
	 * @param shape
	 * @param loader
	 */
	public LazyDataset(String name, int dtype, int[] shape, ILazyLoader loader) {
		this.name = name;
		this.shape = shape;
		this.loader = loader;
		this.dtype = dtype;
		try {
			size = AbstractDataset.calcSize(shape);
		} catch (IllegalArgumentException e) {
			size = Integer.MAX_VALUE; // this indicates that the entire dataset cannot be read in! 
		}
	}

	@Override
	public LazyDataset clone() {
		return new LazyDataset(new String(name), dtype, shape.clone(), loader);
	}

	@Override
	public Class<?> elementClass() {
		return AbstractDataset.elementClass(dtype);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}

		LazyDataset other = (LazyDataset) obj;
		if (dtype != other.dtype) {
			return false;
		}
		if (!Arrays.equals(shape, other.shape)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = dtype;
		int rank = shape.length;
		for (int i = 0; i < rank; i++) {
			hash = hash*17 + shape[i];
		}
		return hash;
	}

	/**
	 * @return type of dataset item
	 */
	public int getDtype() {
		return dtype;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public int[] getShape() {
		return shape.clone();
	}

	@Override
	public int getRank() {
		return shape.length;
	}

	@Override
	public void setShape(int... shape) {
		int nsize = AbstractDataset.calcSize(shape);
		if (nsize != size) {
			throw new IllegalArgumentException("New shape (" + Arrays.toString(shape)
					+ ") is not compatible with old shape (" + Arrays.toString(this.shape) + ")");
		}
		this.shape = shape.clone();
	}

	@Override
	public ILazyDataset squeeze() {
		return squeeze(false);
	}

	@Override
	public ILazyDataset squeeze(boolean onlyFromEnd) {
		shape = AbstractDataset.squeezeShape(shape, onlyFromEnd);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();

		if (name != null && name.length() > 0) {
			out.append("Lazy dataset '");
			out.append(name);
			out.append("' has shape [");
		} else {
			out.append("Lazy dataset shape is [");
		}
		int rank = shape == null ? 0 : shape.length;

		if (rank > 0 && shape[0] > 0) {
			out.append(shape[0]);
		}
		for (int i = 1; i < rank; i++) {
			out.append(", " + shape[i]);
		}
		out.append(']');

		return out.toString();
	}

	@Override
	public AbstractDataset getSlice(Slice... slice) {
		final int rank = shape.length;
		final int[] start = new int[rank];
		final int[] stop = new int[rank];
		final int[] step = new int[rank];
		Slice.convertFromSlice(slice, shape, start, stop, step);
		return getSlice(start, stop, step);
	}

	@Override
	public AbstractDataset getSlice(int[] start, int[] stop, int[] step) {
		if (loader.isFileReadable()) {
			try {
				return loader.getDataset(null, shape, start, stop, step);
			} catch (ScanFileHolderException e) {
				// return a fake dataset to show that this has not worked, should not be used in general though.
				logger.debug("Problem getting {}: {}",
						String.format("slice %s %s %s", Arrays.toString(start), Arrays.toString(stop), Arrays.toString(step)),
						e);
				return new DoubleDataset(1);
			}
		}

		return null; // TODO add interaction to use plot server to load dataset
	}

	@Override
	public AbstractDataset getSlice(IMonitor monitor, Slice... slice) throws ScanFileHolderException {
		final int rank = shape.length;
		final int[] start = new int[rank];
		final int[] stop = new int[rank];
		final int[] step = new int[rank];
		Slice.convertFromSlice(slice, shape, start, stop, step);
		return getSlice(monitor, start, stop, step);
	}

	@Override
	public AbstractDataset getSlice(IMonitor monitor, int[] start, int[] stop, int[] step) throws ScanFileHolderException {
		if (loader.isFileReadable())
			return loader.getDataset(monitor, shape, start, stop, step);

		return null; // TODO add interaction to use plot server to load dataset
	}

	private IMetaData metadata = null;
	
	@Override
	public void setMetadata(IMetaData metadata) {
		this.metadata = metadata;
	}

	@Override
	public IMetaData getMetadata() {
		return metadata;
	}

}
