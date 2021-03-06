/*
 * Copyright (c) 2008-2017, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jet.stream.impl.reducers;

import com.hazelcast.jet.DAG;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.ProcessorMetaSupplier;
import com.hazelcast.jet.Vertex;
import com.hazelcast.jet.stream.DistributedCollector.Reducer;
import com.hazelcast.jet.stream.impl.pipeline.Pipeline;
import com.hazelcast.jet.stream.impl.pipeline.StreamContext;

import static com.hazelcast.jet.Edge.between;
import static com.hazelcast.jet.stream.impl.StreamUtil.executeJob;

abstract class AbstractSinkReducer<T, R> implements Reducer<T, R> {

    @Override
    public R reduce(StreamContext context, Pipeline<? extends T> upstream) {
        R target = getTarget(context.getJetInstance());
        DAG dag = new DAG();
        Vertex vertex = upstream.buildDAG(dag);
        Vertex writer = dag.newVertex("write-" + getName(), getSupplier());

        if (localParallelism() > 0) {
            writer.localParallelism(localParallelism());
        }

        dag.edge(between(vertex, writer));
        executeJob(context, dag);
        return target;
    }

    protected abstract R getTarget(JetInstance instance);

    protected abstract ProcessorMetaSupplier getSupplier();

    protected abstract int localParallelism();

    protected abstract String getName();
}
