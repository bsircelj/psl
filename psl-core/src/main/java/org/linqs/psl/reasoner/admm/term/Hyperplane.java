/*
 * This file is part of the PSL software.
 * Copyright 2011-2015 University of Maryland
 * Copyright 2013-2018 The Regents of the University of California
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
package org.linqs.psl.reasoner.admm.term;

import org.linqs.psl.util.ArrayUtils;

/**
 * Information representing a raw hyperplane.
 */
public class Hyperplane {
    private LocalVariable[] variables;
    private float[] coefficients;
    private int size = 0;
    private float constant;

    public Hyperplane(int maxSize, float constant) {
        this(new LocalVariable[maxSize], new float[maxSize], constant, 0);
    }

    public Hyperplane(LocalVariable[] variables, float[] coefficients, float constant, int size) {
        this.variables = variables;
        this.coefficients = coefficients;
        this.constant = constant;
        this.size = size;
    }

    public void addTerm(LocalVariable variable, float coefficient) {
        variables[size] = variable;
        coefficients[size] = coefficient;
        size++;
    }

    public int size() {
        return size;
    }

    public LocalVariable getVariable(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException("Tried to access variable at index " + index + ", but only " + size + " exist.");
        }

        return variables[index];
    }

    public float getCoefficient(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException("Tried to access coefficient at index " + index + ", but only " + size + " exist.");
        }

        return coefficients[index];
    }

    public void appendCoefficient(int index, float value) {
        if (index >= size) {
            throw new IndexOutOfBoundsException("Tried to access coefficient at index " + index + ", but only " + size + " exist.");
        }

        coefficients[index] += value;
    }

    public float getConstant() {
        return constant;
    }

    public void setConstant(float constant) {
        this.constant = constant;
    }

    public int indexOfVariable(LocalVariable needle) {
        return ArrayUtils.indexOf(variables, size, needle);
    }

    public LocalVariable[] getVariables() {
        return variables;
    }

    public float[] getCoefficients() {
        return coefficients;
    }
}