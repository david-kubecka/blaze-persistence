/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.parser.expression;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapEntryExpression extends AbstractExpression implements PathElementExpression, QualifiedExpression {

    private PathExpression path;

    public MapEntryExpression(PathExpression path) {
        this.path = path;
    }

    @Override
    public MapEntryExpression copy(ExpressionCopyContext copyContext) {
        return new MapEntryExpression((PathExpression) path.copy(copyContext));
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public PathExpression getPath() {
        return path;
    }

    @Override
    public String getQualificationExpression() {
        return "ENTRY";
    }

    public void setPath(PathExpression path) {
        this.path = path;
    }

    @Override
    public int hashCode() {
        return getPath() != null ? getPath().hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        MapEntryExpression that = (MapEntryExpression) o;

        return getPath() != null ? getPath().equals(that.getPath()) : that.getPath() == null;
    }

}
