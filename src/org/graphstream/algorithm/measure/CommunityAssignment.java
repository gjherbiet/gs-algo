/*
 * This file is part of GraphStream.
 * 
 * GraphStream is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GraphStream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphStream.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Project copyright 2006 - 2011
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 *
 * This file is copyright 2011
 *  Guillaume-Jean Herbiet
 */
package org.graphstream.algorithm.measure;

import java.util.HashSet;

import org.graphstream.graph.Node;

/**
 * @author Guillaume
 *
 */
public class CommunityAssignment extends CommunityMeasure {
	
	/**
	 * Ignore communities smaller than this given threshold.
	 * This is useful when there a lot of isolated nodes.
	 */
	protected int threshold = 0;

	/**
	 * @param marker
	 */
	public CommunityAssignment(String marker) {
		super(marker);
	}
	
	/**
	 * Sets the threshold size for communities to be accounted.
	 */
	public void setMinimunCommunitySize(int threshold) {
		this.threshold = threshold;
	}

	/* (non-Javadoc)
	 * @see org.graphstream.algorithm.measure.CommunityMeasure#compute()
	 */
	@Override
	public void compute() {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Updates the distribution information and returns a string for an easy
	 * display of the results.
	 * 
	 * The string has the following format: [number of communities] [average
	 * size] [stdev size] [min size] ([smallest community]) [max size] ([biggest
	 * community])
	 * 
	 * @return a String containing all computed distribution information.
	 */
	@Override
	public String toString() {
		String s = "";
		for (Object c : communities.keySet()) {
			HashSet<Node> nodes = communities.get(c);
			if (nodes.size() > threshold) {
				s += "A = " + c.toString() + " [ ";
				for (Node n : nodes) {
					s += n.getId() + " ";
				}
				s += "]\n";
			}
		}
		return s;
	}

}
