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
package com.aerospike.client.async;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Key;
import com.aerospike.client.command.BatchNode;
import com.aerospike.client.command.BatchNode.BatchNamespace;
import com.aerospike.client.listener.ExistsSequenceListener;
import com.aerospike.client.policy.Policy;

public final class AsyncBatchExistsSequenceExecutor extends AsyncBatchExecutor {
	private final ExistsSequenceListener listener;

	public AsyncBatchExistsSequenceExecutor(
		AsyncCluster cluster,
		Policy policy, 
		Key[] keys,
		ExistsSequenceListener listener
	) throws AerospikeException {
		super(cluster, keys);
		this.listener = listener;
		
		if (policy == null) {
			policy = new Policy();
		}

		// Dispatch asynchronous commands to nodes.
		for (BatchNode batchNode : batchNodes) {			
			for (BatchNamespace batchNamespace : batchNode.batchNamespaces) {
				AsyncBatchExistsSequence async = new AsyncBatchExistsSequence(this, cluster, (AsyncNode)batchNode.node, batchNamespace, policy, listener);
				async.execute();
			}
		}
	}
	
	protected void onSuccess() {
		listener.onSuccess();
	}
	
	protected void onFailure(AerospikeException ae) {
		listener.onFailure(ae);
	}		
}
