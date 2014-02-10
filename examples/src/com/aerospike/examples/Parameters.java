/*******************************************************************************
 * Copyright 2012-2014 by Aerospike.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.aerospike.examples;

import java.util.Map;

import com.aerospike.client.Info;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.WritePolicy;

/**
 * Configuration data.
 */
public class Parameters {
	String host;
	int port;
	String namespace;
	String set;
	WritePolicy writePolicy;
	Policy policy;
	boolean singleBin;
	boolean hasUdf;
	
	protected Parameters(String host, int port, String namespace, String set) {
		this.host = host;
		this.port = port;
		this.namespace = namespace;
		this.set = set;
		this.writePolicy = new WritePolicy();
		this.policy = new Policy();
	}
	
	/**
	 * Some database calls need to know how the server is configured.
	 */
	protected void setServerSpecific() throws Exception {
		String featuresFilter = "features";
		String namespaceFilter = "namespace/" + namespace;
		Map<String,String> tokens = Info.request(host, port, featuresFilter, namespaceFilter);

		String features = tokens.get(featuresFilter);
		hasUdf = false;
		
		if (features != null) {
			String[] list = features.split(";");
			
			for (String s : list) {
				if (s.equals("udf")) {
					hasUdf = true;
					break;
				}
			}
		}
		
		String namespaceTokens = tokens.get(namespaceFilter);
		
		if (namespaceTokens == null) {
			throw new Exception(String.format(
				"Failed to get namespace info: host=%s port=%d namespace=%s",
				host, port, namespace));
		}

		String name = "single-bin";
		String search = name + '=';
		int begin = namespaceTokens.indexOf(search);

		if (begin < 0) {
			throw new Exception(String.format(
				"Failed to find namespace attribute: host=%s port=%d namespace=%s attribute=%s",
				host, port, namespace, name));
		}

		begin += search.length();
		int end = namespaceTokens.indexOf(';', begin);

		if (end < 0) {
			end = namespaceTokens.length();
		}

		String value = namespaceTokens.substring(begin, end);
		singleBin = Boolean.parseBoolean(value);
	}

	@Override
	public String toString() {
		return "Parameters: host=" + host + 
				" port=" + port + 
				" ns=" + namespace + 
				" set=" + set +
				" single-bin=" + singleBin;
	}

	public String getBinName(String name)
	{
		// Single bin servers don't need a bin name.
		return singleBin ? "" : name;
	}
}
