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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;

public class Serialize extends Example {

	public Serialize(Console console) {
		super(console);
	}

	/**
	 * Write complex objects using serializer.
	 */
	@Override
	public void runExample(AerospikeClient client, Parameters params) throws Exception {	
		testArray(client, params);
		testList(client, params);
		testComplex(client, params);
	}
	
	/**
	 * Write array of integers using serializer.
	 */
	public void testArray(AerospikeClient client, Parameters params) throws Exception {
		Key key = new Key(params.namespace, params.set, "serialarraykey");

		// Delete record if it already exists.
		client.delete(params.writePolicy, key);

		console.info("Initialize array");

		int[] array = new int[10000];

		for (int i = 0; i < 10000; i++) {
			array[i] = i * i;
		}

		Bin bin = new Bin(params.getBinName("serialbin"), array);

		// Do a test that pushes this complex object through the serializer
		console.info("Write array using serializer.");
		client.put(params.writePolicy, key, bin);

		console.info("Read array using serializer.");
		Record record = client.get(params.policy, key, bin.name);

		if (record == null) {
			throw new Exception(String.format(
				"Failed to get: namespace=%s set=%s key=%s",
				key.namespace, key.setName, key.userKey));
		}

		int[] received;

		try {
			received = (int[])record.getValue(bin.name);
		}
		catch (Exception e) {
			throw new Exception(String.format(
				"Failed to parse returned value: namespace=%s set=%s key=%s bin=%s", 
				key.namespace, key.setName, key.userKey, bin.name));
		}

		if (received.length != 10000) {
			throw new Exception(String.format(
				"Array length mismatch: Expected=%d Received=%d", 10000, received.length));
		}

		for (int i = 0; i < 10000; i++) {
			if (received[i] != i * i) {
				throw new Exception(String.format(
					"Mismatch: index=%d expected=%d received=%d", i, i*i, received[i])); 
			}
		}

		console.info("Read array successful.");
	}

	/**
	 * Write list object using serializer.
	 */
	public void testList(AerospikeClient client, Parameters params) throws Exception {
		Key key = new Key(params.namespace, params.set, "seriallistkey");

		// Delete record if it already exists.
		client.delete(params.writePolicy, key);

		console.info("Initialize list");
		
		ArrayList<String> list = new ArrayList<String>();
		list.add("string1");
		list.add("string2");
		list.add("string3");

		Bin bin = new Bin(params.getBinName("serialbin"), list);

		console.info("Write list using serializer.");
		client.put(params.writePolicy, key, bin);

		console.info("Read list using serializer.");
		Record record = client.get(params.policy, key, bin.name);

		if (record == null) {
			throw new Exception(String.format(
				"Failed to get: namespace=%s set=%s key=%s",
				key.namespace, key.setName, key.userKey));
		}

		List<?> received;

		try {
			received = (List<?>) record.getValue(bin.name);
		}
		catch (Exception e) {
			throw new Exception(String.format(
				"Failed to parse returned value: namespace=%s set=%s key=%s bin=%s", 
				key.namespace, key.setName, key.userKey, bin.name));
		}

		if (received.size() != 3) {
			throw new Exception(String.format(
				"Array length mismatch: Expected=%d Received=%d", 3, received.size()));
		}

		for (int i = 0; i < received.size(); i++) {
			String expected = "string" + (i + 1);
			if (! received.get(i).equals(expected)) {
				Object obj = received.get(i);
				throw new Exception(String.format(
					"Mismatch: index=%d expected=%s received=%s", i, expected, obj)); 
			}
		}

		console.info("Read list successful.");
	}
	
	/**
	 * Write complex object using serializer.
	 */
	public void testComplex(AerospikeClient client, Parameters params) throws Exception {
		Key key = new Key(params.namespace, params.set, "serialcomplexkey");

		// Delete record if it already exists.
		client.delete(params.writePolicy, key);

		console.info("Initialize complex object");
		
		ArrayList<Object> inner = new ArrayList<Object>();
		inner.add("string2");
		inner.add(8);
		
		HashMap<Object,Object> innerMap = new HashMap<Object,Object>();
		innerMap.put("a", 1);
		innerMap.put(2, "b");
		innerMap.put("list", inner);
		
		ArrayList<Object> list = new ArrayList<Object>();
		list.add("string1");
		list.add(4);
		list.add(inner);
		list.add(innerMap);

		Bin bin = new Bin(params.getBinName("complexbin"), list);

		console.info("Write complex object using serializer.");
		client.put(params.writePolicy, key, bin);

		console.info("Read complex object using serializer.");
		Record record = client.get(params.policy, key, bin.name);

		if (record == null) {
			throw new Exception(String.format(
				"Failed to get: namespace=%s set=%s key=%s",
				key.namespace, key.setName, key.userKey));
		}

		Object received;

		try {
			received = (List<?>) record.getValue(bin.name);
		}
		catch (Exception e) {
			throw new Exception(String.format(
				"Failed to parse returned value: namespace=%s set=%s key=%s bin=%s", 
				key.namespace, key.setName, key.userKey, bin.name));
		}

		if (received != null && received.equals(list)) {
			console.info("Data matched: namespace=%s set=%s key=%s bin=%s value=%s", 
				key.namespace, key.setName, key.userKey, bin.name, received);
		}
		else {
			console.error("Data mismatch");
			console.error("Expected " + list);
			console.error("Received " + received);
		}
		console.info("Read complex object successful.");
	}
}
