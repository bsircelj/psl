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
package org.linqs.psl.application.groundrulestore;

import org.linqs.psl.model.rule.GroundRule;
import org.linqs.psl.model.rule.Rule;
import org.linqs.psl.model.rule.UnweightedGroundRule;
import org.linqs.psl.model.rule.WeightedGroundRule;

import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import com.google.common.collect.Iterables;

/**
 * A simple {@link GroundRuleStore} that just stores each {@link GroundRule}
 * in memory.
 * <p>
 * No action is taken by {@link #changedGroundRule(GroundRule)}.
 */
public class MemoryGroundRuleStore implements GroundRuleStore {

	protected final SetValuedMap<Rule, GroundRule> groundRules;
	
	public MemoryGroundRuleStore() {
		groundRules = new HashSetValuedHashMap<Rule, GroundRule>();
	}
	
	@Override
	public boolean containsGroundRule(GroundRule groundRule) {
		return groundRules.containsMapping(groundRule.getRule(), groundRule);
	}
	
	@Override
	public void addGroundRule(GroundRule groundRule) {
		if (!groundRules.put(groundRule.getRule(), groundRule))
			throw new IllegalArgumentException("GroundRule has already been added: " + groundRule);
	}
	
	@Override
	public void changedGroundRule(GroundRule groundRule) {
		/* Intentionally blank */
	}

	@Override
	public void changedGroundRuleWeight(WeightedGroundRule k) {
		/* Intentionally blank */
	}

	@Override
	public void changedGroundRuleWeights() {
		/* Intentionally blank */
	}
	
	@Override
	public void removeGroundRule(GroundRule groundRule) {
		groundRules.removeMapping(groundRule.getRule(), groundRule);
	}
	
	public Iterable<GroundRule> getGroundRules() {
		return groundRules.values();
	}
	
	@Override
	public Iterable<WeightedGroundRule> getCompatibilityRules() {
		return Iterables.filter(groundRules.values(), WeightedGroundRule.class);
	}
	
	public Iterable<UnweightedGroundRule> getConstraintRules() {
		return Iterables.filter(groundRules.values(), UnweightedGroundRule.class);
	}
	
	@Override
	public Iterable<GroundRule> getGroundRules(Rule k) {
		return groundRules.get(k);
	}
	
	@Override
	public int size() {
		return groundRules.size();
	}
}