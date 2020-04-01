/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.eql.expression.function.scalar.string;

import org.elasticsearch.xpack.ql.execution.search.QlSourceBuilder;
import org.elasticsearch.xpack.ql.expression.Expression;
import org.elasticsearch.xpack.ql.expression.gen.pipeline.Pipe;
import org.elasticsearch.xpack.ql.tree.NodeInfo;
import org.elasticsearch.xpack.ql.tree.Source;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SubstringFunctionPipe extends Pipe {

    private final Pipe source, start, end;

    public SubstringFunctionPipe(Source source, Expression expression, Pipe src, Pipe start, Pipe end) {
        super(source, expression, Arrays.asList(src, start, end));
        this.source = src;
        this.start = start;
        this.end = end;
    }

    @Override
    public final Pipe replaceChildren(List<Pipe> newChildren) {
        if (newChildren.size() != 3) {
            throw new IllegalArgumentException("expected [3] children but received [" + newChildren.size() + "]");
        }
        return replaceChildren(newChildren.get(0), newChildren.get(1), newChildren.get(2));
    }

    @Override
    public final Pipe resolveAttributes(AttributeResolver resolver) {
        Pipe newSource = source.resolveAttributes(resolver);
        Pipe newStart = start.resolveAttributes(resolver);
        Pipe newEnd = end.resolveAttributes(resolver);
        if (newSource == source && newStart == start && newEnd == end) {
            return this;
        }
        return replaceChildren(newSource, newStart, newEnd);
    }

    @Override
    public boolean supportedByAggsOnlyQuery() {
        return source.supportedByAggsOnlyQuery() && start.supportedByAggsOnlyQuery() && end.supportedByAggsOnlyQuery();
    }

    @Override
    public boolean resolved() {
        return source.resolved() && start.resolved() && end.resolved();
    }

    protected Pipe replaceChildren(Pipe newSource, Pipe newStart, Pipe newEnd) {
        return new SubstringFunctionPipe(source(), expression(), newSource, newStart, newEnd);
    }

    @Override
    public final void collectFields(QlSourceBuilder sourceBuilder) {
        source.collectFields(sourceBuilder);
        start.collectFields(sourceBuilder);
        end.collectFields(sourceBuilder);
    }

    @Override
    protected NodeInfo<SubstringFunctionPipe> info() {
        return NodeInfo.create(this, SubstringFunctionPipe::new, expression(), source, start, end);
    }

    @Override
    public SubstringFunctionProcessor asProcessor() {
        return new SubstringFunctionProcessor(source.asProcessor(), start.asProcessor(), end.asProcessor());
    }
    
    public Pipe src() {
        return source;
    }
    
    public Pipe start() {
        return start;
    }
    
    public Pipe end() {
        return end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, start, end);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        SubstringFunctionPipe other = (SubstringFunctionPipe) obj;
        return Objects.equals(source, other.source)
                && Objects.equals(start, other.start)
                && Objects.equals(end, other.end);
    }
}
