/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.diamond.scisoft.analysis.plotserver;

import gda.data.nexus.tree.INexusTree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5File;

/**
 * This bean contains all the information required by a GUI to perform a plot,
 * including the data, as well as the axis information.
 */
public class DataBean implements Serializable {

	private static final long serialVersionUID = -2033109932408452451L;

	private List<DataSetWithAxisInformation> data;

	private Map<String, AbstractDataset> axisData;

	private List<INexusTree> nexusTrees;
	
	private List<HDF5File> hdf5Trees;
	
	private GuiPlotMode guiPlotMode;
	
	private GuiBean plotParameters;

	/**
	 * Constructor to initialise all the collection objects
	 */
	public DataBean(GuiPlotMode plotMode) {
		guiPlotMode = plotMode;
		data = new ArrayList<DataSetWithAxisInformation>();
		axisData = new HashMap<String, AbstractDataset>();
		nexusTrees = new ArrayList<INexusTree>();
		hdf5Trees = new ArrayList<HDF5File>();
	}
	
	public DataBean() {
		guiPlotMode = null;
		data = new ArrayList<DataSetWithAxisInformation>();
		axisData = new HashMap<String, AbstractDataset>();
		nexusTrees = new ArrayList<INexusTree>();
		hdf5Trees = new ArrayList<HDF5File>();
	}
	

	/**
	 * @return a shallow copy of data bean
	 */
	public DataBean copy() {
		DataBean bean = new DataBean();
		bean.data.addAll(data);
		bean.axisData.putAll(axisData);
		bean.nexusTrees.addAll(nexusTrees);
		bean.hdf5Trees.addAll(hdf5Trees);
		bean.guiPlotMode = guiPlotMode;
		return bean;
	}
	
	/**
	 * Adds the provided data to the bean, one element at a time
	 * 
	 * @param dataToAdd
	 * @throws DataBeanException 
	 */
	public void addData(DataSetWithAxisInformation dataToAdd) throws DataBeanException {
		// check that the dataset's axis mapping has IDs that
		// correspond to ones in axisData
		AxisMapBean mapping = dataToAdd.getAxisMap();
		String[] axisID = mapping.getAxisID();
		if (axisID != null) {
			for (String s : axisID) {
				if (!axisData.containsKey(s)) {
					throw new DataBeanException();
				}
			}
		}
		data.add(dataToAdd);
	}

	/**
	 * Adds the provided axis data to the bean
	 * 
	 * @param axisName
	 * @param axisDataset
	 */
	public void addAxis(String axisName, IDataset axisDataset) {
		axisData.put(axisName, DatasetUtils.convertToAbstractDataset(axisDataset));
	}

	/**
	 * Adds the provided NeXus tree to the bean, one element at a time
	 * 
	 * @param nexusTreeToAdd
	 */
	public void addNexusTree(INexusTree nexusTreeToAdd) {
		nexusTrees.add(nexusTreeToAdd);
	}

	/**
	 * Adds the provided HDF5 tree to the bean, one element at a time
	 * 
	 * @param hdf5TreeToAdd
	 */
	public void addHDF5Tree(HDF5File hdf5TreeToAdd) {
		hdf5Trees.add(hdf5TreeToAdd);
	}

	/**
	 * gets the axis data of the specified name
	 * 
	 * @param axisName
	 * @return the axis dataset
	 */
	public AbstractDataset getAxis(String axisName) {
		return axisData.get(axisName);
	}

	/**
	 * gets all the data which is to be plotted as a collection
	 * 
	 * @return Returns the data.
	 */
	public List<DataSetWithAxisInformation> getData() {
		return data;
	}

	/**
	 * Fills the beans collection with one created external, useful for filling
	 * all the data in one go.
	 * 
	 * @param data
	 *            The data to set.
	 */
	public void setData(List<DataSetWithAxisInformation> data) {
		this.data = data;
	}

	/**
	 * 
	 * @param nexusTrees
	 */
	public void setNexusTrees(List<INexusTree> nexusTrees) {
		this.nexusTrees = nexusTrees;
	}

	/**
	 * gets all the nexusTrees as a collection
	 * 
	 * @return nexusTrees
	 */
	public List<INexusTree> getNexusTrees() {
		return nexusTrees;
	}

	/**
	 * gets all the nexusTrees as a collection
	 * 
	 * @return nexusTrees
	 */
	public List<HDF5File> getHDF5Trees() {
		return hdf5Trees;
	}
	
	/**
	 * 
	 * @param hdf5Trees
	 */
	public void setHDF5Trees(List<HDF5File> hdf5Trees) {
		this.hdf5Trees = hdf5Trees;
	}

	/**
	 * @return map of names to axis datasets
	 */
	public Map<String, AbstractDataset> getAxisData() {
		return axisData;
	}

	/**
	 * Set map of names to axis datasets
	 * @param axisData
	 */
	public void setAxisData(Map<String, AbstractDataset> axisData) {
		this.axisData = axisData;
	}

	public GuiPlotMode getGuiPlotMode() {
		return guiPlotMode;
	}

	public void setGuiPlotMode(GuiPlotMode guiPlotMode) {
		this.guiPlotMode = guiPlotMode;
	}

	public void putGuiParameter(GuiParameters key, Serializable value) {
		if (plotParameters == null) {
				plotParameters = new GuiBean();
		}
		plotParameters.put(key, value);
	}
	
	public GuiBean getGuiParameters() {
		return plotParameters;
	}
	
	@Override
	public String toString() {
		return "data =" + data.toString() + "\n" + "axisData = " + axisData.toString(); 
	}
}
