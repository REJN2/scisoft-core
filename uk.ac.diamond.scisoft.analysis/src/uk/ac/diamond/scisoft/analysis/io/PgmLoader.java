/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.api.metadata.IMetaLoader;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.api.metadata.Metadata;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.ShortDataset;

/**
 * This class should be used to load .pgm datafiles (Portable Grey Map)
 * into the ScanFileHolder object.
 * <p>
 * <b>Note</b>: the header data from this loader is left as strings
 */
public class PgmLoader extends AbstractFileLoader implements IMetaLoader {

	private String fileName;
	private Map<String, String> textMetadata = new HashMap<String, String>();
	private Metadata metadata;
	private static final String DATA_NAME = "Portable Grey Map";

	public PgmLoader() {
		
	}
	/**
	 * @param fileName
	 */
	public PgmLoader(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public DataHolder loadFile() throws ScanFileHolderException {
		return loadFile(null);
	}
	@Override
	public DataHolder loadFile(IMonitor mon) throws ScanFileHolderException {

		Dataset data = null;
		DataHolder output = new DataHolder();
		File f = null;
		FileInputStream fi = null;
		try {

			f = new File(fileName);
			fi = new FileInputStream(f);

			BufferedReader br = new BufferedReader(new FileReader(f));
			
			int[] vals = readMetaData(br, mon);
			int index  = vals[0];
			int width  = vals[1];
			int height = vals[2];
			int maxval = vals[3];

			// Now read the data
			if (maxval < 256) {
				data = new ShortDataset(height, width);
				Utils.readByte(fi, (ShortDataset) data, index);
			} else {
				data = new IntegerDataset(height, width);
				Utils.readBeShort(fi, (IntegerDataset) data, index, false);
			}
			data.setName(DEF_IMAGE_NAME);
		} catch (Exception e) {
			throw new ScanFileHolderException("File failed to load " + fileName, e);
		} finally {
			if (fi != null) {
				try {
					fi.close();
				} catch (IOException ex) {
					// do nothing
				}
				fi = null;
			}
		}

		output.addDataset(DATA_NAME, data);
		if (loadMetadata) {
		    createMetadata();
			data.setMetadata(metadata);
			output.setMetadata(metadata);
		}
		return output;
	}

	private int[] readMetaData(BufferedReader br, IMonitor mon) throws Exception {
		int width  = 0;
		int height = 0;
		int maxval = 0;

		textMetadata.clear();
		if (mon!=null) mon.worked(1);
		if (mon!=null&&mon.isCancelled()) throw new ScanFileHolderException("Loader cancelled during reading!");

		String line = br.readLine();
		if (line == null) {
			throw new ScanFileHolderException("End of file reached with no metadata found");
		}
		int index   = line.length()+1;
		String token;
		StringTokenizer s1 = new StringTokenizer(line);
		token = s1.nextToken();
		textMetadata.put("MagicNumber", token);
		if (token.startsWith("P5")) {
			if (!s1.hasMoreTokens()) {
				do {
					line = br.readLine();
					if (line == null) {
						throw new ScanFileHolderException("End of file reached with width not found");
					}
					index += line.length() + 1;
				} while (line.startsWith("#")); // ignore comment lines
				s1 = new StringTokenizer(line);
			}
			token = s1.nextToken();
			textMetadata.put("Width", token);
			width = Integer.parseInt(token);
			if (!s1.hasMoreTokens()) {
				line = br.readLine();
				if (line == null) {
					throw new ScanFileHolderException("End of file reached with height not found");
				}
				index += line.length()+1;
				s1 = new StringTokenizer(line);	
			}
			token = s1.nextToken();
			textMetadata.put("Height", token);
			height = Integer.parseInt(token);
			if (!s1.hasMoreTokens()) {
				line = br.readLine();
				if (line == null) {
					throw new ScanFileHolderException("End of file reached with max value not found");
				}
				index += line.length()+1;
				s1 = new StringTokenizer(line);	
			}
			token = s1.nextToken();
			textMetadata.put("Maxval", token);
			maxval = Integer.parseInt(token);
		}
		
		return new int[]{index, width, height, maxval};
	}

	@Override
	public void loadMetadata(final IMonitor mon) throws Exception {

		final BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
		try {
		    readMetaData(br, mon);
		    createMetadata();
		} finally {
			br.close();
		}
	}

	private void createMetadata() {
		metadata = new Metadata(textMetadata);
		metadata.setFilePath(fileName);
		metadata.addDataInfo(DATA_NAME, Integer.parseInt(textMetadata.get("Height")), Integer.parseInt(textMetadata.get("Width")));
	}
	
	@Override
	public IMetadata getMetadata() {
		return metadata;
	}

	public String getHeaderValue(String key) {
		return textMetadata.get(key);	
	}
}
