/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.diamond.scisoft.analysis.rpc.instancedispatcher;

@SuppressWarnings("unused")
public interface InterfaceAndImpl {
	public String interfacecall(int a);
	public String implcalloverloaded(int a);
	
	public class Impl implements InterfaceAndImpl {

		@Override
		public String interfacecall(int a) {
			return "interfacecall";
		}
		
		public String implcall(int a) {
			return "implcall";
		}	
		
		@Override
		public String implcalloverloaded(int a) {
			return "implcalloverloaded - interface";
		}	
		
		public String implcalloverloaded(String a) {
			return "implcalloverloaded - impl";
		}	
	}
}