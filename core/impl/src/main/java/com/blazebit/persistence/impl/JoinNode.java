/*
 * Copyright 2014 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.persistence.impl;

import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class JoinNode {

    private JoinAliasInfo aliasInfo;
    private JoinType type = JoinType.LEFT;
    private boolean fetch = false;
    // This flag indicates if this join node is required from a select expression only
    // We need this for count and id queries where we do not need all the joins
    private boolean selectOnly = true;
    private final JoinNode parent;
    private final Class<?> propertyClass;
    private final Map<String, JoinTreeNode> nodes = new TreeMap<String, JoinTreeNode>(); // Use TreeMap so that joins get applied alphabetically for easier testing
    private final boolean collection;

    private AndPredicate withPredicate;

    public JoinNode(JoinNode parent, JoinAliasInfo aliasInfo, JoinType type, Class<?> propertyClass, boolean collection) {
        this.parent = parent;
        this.aliasInfo = aliasInfo;
        this.type = type;
        this.propertyClass = propertyClass;
        this.collection = collection;
    }

    public void accept(Predicate.Visitor visitor) {
        if (withPredicate != null) {
            withPredicate.accept(visitor);
        }
        for (JoinTreeNode treeNode : nodes.values()) {
            for (JoinNode joinNode : treeNode.getJoinNodes().values()) {
                joinNode.accept(visitor);
            }
        }
    }

    public JoinNode getParent() {
        return parent;
    }

    public boolean isSelectOnly() {
        return selectOnly;
    }

    public void setSelectOnly(boolean selectOnly) {
        this.selectOnly = selectOnly;
    }

    public JoinAliasInfo getAliasInfo() {
        return aliasInfo;
    }

    public void setAliasInfo(JoinAliasInfo aliasInfo) {
        this.aliasInfo = aliasInfo;
    }

    public JoinType getType() {
        return type;
    }

    public void setType(JoinType type) {
        this.type = type;
    }

    public boolean isFetch() {
        return fetch;
    }

    public void setFetch(boolean fetch) {
        this.fetch = fetch;
    }

    public Map<String, JoinTreeNode> getNodes() {
        return nodes;
    }

    public JoinTreeNode getOrCreateTreeNode(String joinRelationName) {
        JoinTreeNode node = nodes.get(joinRelationName);

        if (node == null) {
            node = new JoinTreeNode();
            nodes.put(joinRelationName, node);
        }

        return node;
    }

    public Class<?> getPropertyClass() {
        return propertyClass;
    }

    public AndPredicate getWithPredicate() {
        return withPredicate;
    }

    public void setWithPredicate(AndPredicate withPredicate) {
        this.withPredicate = withPredicate;
    }

    public boolean isCollection() {
        return collection;
    }
}
