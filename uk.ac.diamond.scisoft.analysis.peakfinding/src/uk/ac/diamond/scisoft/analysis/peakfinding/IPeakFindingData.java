/*-
 * Copyright 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.peakfinding;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.peakfinding.IPeakFinderParameter;

public interface IPeakFindingData {

	/**
	 * Adds IPeakFinder, specified by unique ID, to the active IPeakFinders 
	 * collection in this instance
	 * @param id Unique string (e.g. Fully Qualified Class Name - FQCN)
	 * @throws IllegalArgumentException in case peak finder already active
	 * @throws NullPointerException in case the peak finder cannot be found in register
	 */
	public void activatePeakFinder(String id);

	/**
	 * Removes IPeakFinder, specified by unique ID, from active IPeakFinders
	 * collection in this instance
	 * @param id unique string (e.g. FQCN)
	 * @throws IllegalArgumentException in case the peak finder is not already active
	 */
	public void deactivatePeakFinder(String id);
	
	/**
	 * Returns a collection containing the IPeakFinders which are set active 
	 * in this instance
	 * @return 
	 */
	public Collection<String> getActivePeakFinders();
	
	/**
	 * Reports whether there are active peakFinders
	 */
	public boolean hasActivePeakFinders();
	
	/**
	 * Sets all the parameters of a specified peak finder using a supplied map 
	 * of parameter names and values. Method checks that parameter value types 
	 * are consistent with their expected types. N.B. Names in keys must be 
	 * identical to names in parameters.
	 * @param pfID String ID (FQCN) of peak finder
	 * @param pfParameters Map of parameter name keys and new parameter objects
	 * @throws IllegalArgumentException If Number type of any of the parameters  
	 *         is notconsistent with the expected type; change will not be 
	 *         accepted. Alternatively if the peak finder has never been marked 
	 *         active.
	 */
	public void setPFParametersByPeakFinder(String pfID, 
			Map<String, IPeakFinderParameter> pfParameters);
	
	/**
	 * Sets the value of a specified parameter in a named peak finder to a  
	 * given value. Method checks parameter value type is consistent with the 
	 * expected type
	 * @param pfID String ID (FQCN) of peak finder
	 * @param paramName Name of peak finder parameter
	 * @param paramValue New value of parameter
	 * @throws IllegalArgumentException If Number type of the parameter is not  
	 *         consistent with the expected type; change will not be accepted. 
	 *         Alternatively if the peak finder has never been marked active or
	 *         the parameter name does not exist.
	 */
	public void setPFParameterByName(String pfID, String paramName, 
			Number paramValue);
	
	/**
	 * Returns a map containing IDs of all peak finders which have been 
	 * activated in the lifetime of this instance and maps of all their 
	 * parameters with associated values. 
	 * @return Map<String peak finder IDs, Map<parameter string name, parameter>>
	 */
	public Map<String, Map<String, IPeakFinderParameter>> getAllPFParameters();
	
	/**
	 * Returns a set containing the names and values of the parameters of this 
	 * peak finder.
	 * @param pfID String ID (FQCN) pf peak finder
	 * @return Map<parameter string name, parameter>
	 * @throws NullPointerException If peak finder pfID has never been marked 
	 *         active.
	 */
	public Map<String, IPeakFinderParameter> getPFParametersByPeakFinder(String pfID);
	
	/**
	 * Returns a named parameter from a specified peak finder.
	 * @param pfID String ID (FQCN) of peak finder
	 * @param paramName String name of the parameter
	 * @return PeakFinderParameter containing value, isInt logic and name
	 * @throws NullPointerException If peak finder pfID has never been marked 
	 *         active.
	 * @throws IllegalArgument If parameter name does not exist.
	 */
	public IPeakFinderParameter getPFParameterByName(String pfID, String paramName);
	
	/**
	 * Returns the value of a named parameter from a specified peak finder.
	 * @param pfID String ID (FQCN) of peak finder
	 * @param paramName String name of the parameter
	 * @return Number value of peak finder parameter
	 * @throws NullPointerException If peak finder pfID has never been marked 
	 *         active.
	 * @throws IllegalArgument If parameter name does not exist.
	 */
	public Number getPFParameterValueByName(String pfID, String paramName);
	
	/**
	 * Returns boolean of whether parameter is an integer or not for a 
	 * specified peak finder.
	 * @param pfID String ID (FQCN) of peak finder
	 * @param paramName String name of the parameter
	 * @return Boolean value of isInt peak finder parameter
	 * @throws NullPointerException If peak finder pfID has never been marked 
	 *         active.
	 * @throws IllegalArgument If parameter name does not exist.
	 */
	public Boolean getPFParameterIsIntByName(String pfID, String paramName);
	
	/**
	 * Returns set of all the string names of the parameters associated with 
	 * this peak finder
	 * @param pfID String ID (FQCN) of peak finder
	 * @return Set containing parameter names
	 * @throws NullPointerException If peak finder pfID has never been marked 
	 *         active.
	 */
	public Set<String> getPFParameterNamesByPeakFinder(String pfID);
	
	/**
	 * Set all the data on this IPeakFindingData object which might change 
	 * between findPeaks calls
	 * @param xData
	 * @param yData
	 * @param nPeaks maximum number of peaks
	 */
	public void setData(IDataset xData, IDataset yData, Integer nPeaks);
	
	/**
	 * See {@link #setData(IDataset, IDataset, Integer)} method
	 * @param xData
	 * @param yData
	 */
	public void setData(IDataset xData, IDataset yData);
	
	/**
	 * Set the input x-axis data in which to find peaks
	 * @param xData
	 */
	public void setXData(IDataset xData);
	/**
	 * Set the input y-axis data in which to find peaks
	 * @param yData
	 */
	public void setYData(IDataset yData);
	
	/**
	 * Get the current data of this IPeakFindingData object (not nPeaks)
	 */
	public IDataset[] getData();
	
	/**
	 * Reports whether data has been set in this instance.
	 */
	public boolean hasData();
	
	/**
	 * Set the maximum number of peaks which will be found when this instance 
	 * is submitted to findPeaks
	 * @param nPeaks
	 */
	public void setNPeaks(Integer nPeaks);
	
	/**
	 * Get the maximum number of peaks which will be found
	 */
	public Integer getNPeaks();
	
	/**
	 * Sets the peaks which were found in the data and using the settings of this object,
	 * by the IPeakFindingService and stores them 
	 * @param newFoundPeaks
	 */
	public void setPeaks(Map<String, Map<Integer, Double>> newFoundPeaks);
	
	/**
	 * Returns a map with the unique IPeakFinder ID as the key and values which 
	 * are the results of the IPeakFinder findPeaks() method.
	 * @return Map of IPeakFinderIDs and IPeakFinder found peaks
	 * @throws NullPointerException in case no IPeakFinder result is found
	 */
	public Map<String, Map<Integer, Double>> getPeaks();

	/**
	 * Returns a map with key peak position and value significance for the 
	 * IPeakFinder specified by the unique ID.
	 * @param id Unique IPeakFinder ID (e.g. FQCN)
	 * @return Map of IPeakFinderIDs and IPeakFinder found peaks
	 * @throws NullPointerException in case no IPeakFinder result is found
	 * @throws IllegalArgumentException in case no IPeakFinder result is 
	 *         found for the given ID
	 */
	public Map<Integer, Double> getPeaks(String id);
}
