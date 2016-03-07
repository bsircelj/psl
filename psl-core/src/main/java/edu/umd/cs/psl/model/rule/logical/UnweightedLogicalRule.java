/*
 * This file is part of the PSL software.
 * Copyright 2011-2015 University of Maryland
 * Copyright 2013-2015 The Regents of the University of California
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
package edu.umd.cs.psl.model.rule.logical;

import java.util.List;

import edu.umd.cs.psl.model.atom.GroundAtom;
import edu.umd.cs.psl.model.formula.Formula;
import edu.umd.cs.psl.model.rule.UnweightedRule;
import edu.umd.cs.psl.model.rule.Rule;

public class UnweightedLogicalRule extends AbstractLogicalRule implements UnweightedRule {
	
	public UnweightedLogicalRule(Formula f) {
		super(f);
	}

	@Override
	protected AbstractGroundLogicalRule groundFormulaInstance(List<GroundAtom> posLiterals, List<GroundAtom> negLiterals) {
		return new UnweightedGroundLogicalRule(this, posLiterals, negLiterals);
	}
	
	@Override
	public String toString() {
		return "{constraint} " + formula; 
	}
	
	@Override
	public Rule clone() {
		return new UnweightedLogicalRule(formula);
	}
}