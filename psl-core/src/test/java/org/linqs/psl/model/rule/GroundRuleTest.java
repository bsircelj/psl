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
package org.linqs.psl.model.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.linqs.psl.PSLTest;
import org.linqs.psl.TestModelFactory;
import org.linqs.psl.application.groundrulestore.GroundRuleStore;
import org.linqs.psl.config.ConfigBundle;
import org.linqs.psl.config.EmptyBundle;
import org.linqs.psl.database.DataStore;
import org.linqs.psl.database.Database;
import org.linqs.psl.database.Partition;
import org.linqs.psl.database.Queries;
import org.linqs.psl.database.loading.Inserter;
import org.linqs.psl.database.rdbms.RDBMSDataStore;
import org.linqs.psl.database.rdbms.RDBMSUniqueStringID;
import org.linqs.psl.database.rdbms.driver.H2DatabaseDriver;
import org.linqs.psl.database.rdbms.driver.H2DatabaseDriver.Type;
import org.linqs.psl.model.atom.Atom;
import org.linqs.psl.model.atom.AtomCache;
import org.linqs.psl.model.atom.AtomManager;
import org.linqs.psl.model.atom.GroundAtom;
import org.linqs.psl.model.atom.ObservedAtom;
import org.linqs.psl.model.atom.QueryAtom;
import org.linqs.psl.model.atom.SimpleAtomManager;
import org.linqs.psl.model.formula.Conjunction;
import org.linqs.psl.model.formula.Disjunction;
import org.linqs.psl.model.formula.Formula;
import org.linqs.psl.model.formula.Implication;
import org.linqs.psl.model.formula.Negation;
import org.linqs.psl.model.predicate.PredicateFactory;
import org.linqs.psl.model.predicate.SpecialPredicate;
import org.linqs.psl.model.predicate.StandardPredicate;
import org.linqs.psl.model.rule.GroundRule;
import org.linqs.psl.model.rule.Rule;
import org.linqs.psl.model.rule.arithmetic.UnweightedArithmeticRule;
import org.linqs.psl.model.rule.arithmetic.WeightedArithmeticRule;
import org.linqs.psl.model.rule.arithmetic.expression.ArithmeticRuleExpression;
import org.linqs.psl.model.rule.arithmetic.expression.SummationAtom;
import org.linqs.psl.model.rule.arithmetic.expression.SummationAtomOrAtom;
import org.linqs.psl.model.rule.arithmetic.expression.SummationVariable;
import org.linqs.psl.model.rule.arithmetic.expression.SummationVariableOrTerm;
import org.linqs.psl.model.rule.arithmetic.expression.coefficient.Cardinality;
import org.linqs.psl.model.rule.arithmetic.expression.coefficient.Coefficient;
import org.linqs.psl.model.rule.arithmetic.expression.coefficient.ConstantNumber;
import org.linqs.psl.model.rule.logical.UnweightedLogicalRule;
import org.linqs.psl.model.rule.logical.WeightedLogicalRule;
import org.linqs.psl.model.term.Constant;
import org.linqs.psl.model.term.ConstantType;
import org.linqs.psl.model.term.UniqueID;
import org.linqs.psl.model.term.Variable;
import org.linqs.psl.reasoner.admm.ADMMReasoner;
import org.linqs.psl.reasoner.function.FunctionComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Check for ground rules being created properly.
 */
public class GroundRuleTest {
	private TestModelFactory.ModelInformation model;
	private Database database;

	@Before
	public void setup() {
		model = TestModelFactory.getModel(true);
		Set<StandardPredicate> toClose = new HashSet<StandardPredicate>();
		toClose.add(model.predicates.get("Nice"));
		database = model.dataStore.getDatabase(model.targetPartition, toClose, model.observationPartition);
	}
	/*TEST
	@Test
	public void testLogicalBase() {
		GroundRuleStore store = new ADMMReasoner(model.config);
		AtomManager manager = new SimpleAtomManager(database);

		Rule rule;
		List<String> expected;

		// Nice(A) & Nice(B) -> Friends(A, B)
		rule = new WeightedLogicalRule(
			new Implication(
				new Conjunction(
					new QueryAtom(model.predicates.get("Nice"), new Variable("A")),
					new QueryAtom(model.predicates.get("Nice"), new Variable("B"))
				),
				new QueryAtom(model.predicates.get("Friends"), new Variable("A"), new Variable("B"))
			),
			1.0,
			true
		);

		// Remember, all rules will be in DNF.
		expected = Arrays.asList(
			"1.0: ( ~( NICE('Alice') ) | ~( NICE('Alice') ) | FRIENDS('Alice', 'Alice') ) ^2",
			"1.0: ( ~( NICE('Alice') ) | ~( NICE('Bob') ) | FRIENDS('Alice', 'Bob') ) ^2",
			"1.0: ( ~( NICE('Alice') ) | ~( NICE('Charlie') ) | FRIENDS('Alice', 'Charlie') ) ^2",
			"1.0: ( ~( NICE('Alice') ) | ~( NICE('Derek') ) | FRIENDS('Alice', 'Derek') ) ^2",
			"1.0: ( ~( NICE('Alice') ) | ~( NICE('Eugene') ) | FRIENDS('Alice', 'Eugene') ) ^2",
			"1.0: ( ~( NICE('Bob') ) | ~( NICE('Alice') ) | FRIENDS('Bob', 'Alice') ) ^2",
			"1.0: ( ~( NICE('Bob') ) | ~( NICE('Bob') ) | FRIENDS('Bob', 'Bob') ) ^2",
			"1.0: ( ~( NICE('Bob') ) | ~( NICE('Charlie') ) | FRIENDS('Bob', 'Charlie') ) ^2",
			"1.0: ( ~( NICE('Bob') ) | ~( NICE('Derek') ) | FRIENDS('Bob', 'Derek') ) ^2",
			"1.0: ( ~( NICE('Bob') ) | ~( NICE('Eugene') ) | FRIENDS('Bob', 'Eugene') ) ^2",
			"1.0: ( ~( NICE('Charlie') ) | ~( NICE('Alice') ) | FRIENDS('Charlie', 'Alice') ) ^2",
			"1.0: ( ~( NICE('Charlie') ) | ~( NICE('Bob') ) | FRIENDS('Charlie', 'Bob') ) ^2",
			"1.0: ( ~( NICE('Charlie') ) | ~( NICE('Charlie') ) | FRIENDS('Charlie', 'Charlie') ) ^2",
			"1.0: ( ~( NICE('Charlie') ) | ~( NICE('Derek') ) | FRIENDS('Charlie', 'Derek') ) ^2",
			"1.0: ( ~( NICE('Charlie') ) | ~( NICE('Eugene') ) | FRIENDS('Charlie', 'Eugene') ) ^2",
			"1.0: ( ~( NICE('Derek') ) | ~( NICE('Alice') ) | FRIENDS('Derek', 'Alice') ) ^2",
			"1.0: ( ~( NICE('Derek') ) | ~( NICE('Bob') ) | FRIENDS('Derek', 'Bob') ) ^2",
			"1.0: ( ~( NICE('Derek') ) | ~( NICE('Charlie') ) | FRIENDS('Derek', 'Charlie') ) ^2",
			"1.0: ( ~( NICE('Derek') ) | ~( NICE('Derek') ) | FRIENDS('Derek', 'Derek') ) ^2",
			"1.0: ( ~( NICE('Derek') ) | ~( NICE('Eugene') ) | FRIENDS('Derek', 'Eugene') ) ^2",
			"1.0: ( ~( NICE('Eugene') ) | ~( NICE('Alice') ) | FRIENDS('Eugene', 'Alice') ) ^2",
			"1.0: ( ~( NICE('Eugene') ) | ~( NICE('Bob') ) | FRIENDS('Eugene', 'Bob') ) ^2",
			"1.0: ( ~( NICE('Eugene') ) | ~( NICE('Charlie') ) | FRIENDS('Eugene', 'Charlie') ) ^2",
			"1.0: ( ~( NICE('Eugene') ) | ~( NICE('Derek') ) | FRIENDS('Eugene', 'Derek') ) ^2",
			"1.0: ( ~( NICE('Eugene') ) | ~( NICE('Eugene') ) | FRIENDS('Eugene', 'Eugene') ) ^2"
		);
		rule.groundAll(manager, store);
		PSLTest.compareGroundRules(expected, rule, store);
	}

	@Test
	public void testLogicalSpecialPredicates() {
		GroundRuleStore store = new ADMMReasoner(model.config);
		AtomManager manager = new SimpleAtomManager(database);

		Rule rule;
		List<String> expected;

		// Nice(A) & Nice(B) & (A == B) -> Friends(A, B)
		rule = new WeightedLogicalRule(
			new Implication(
				new Conjunction(
					new QueryAtom(model.predicates.get("Nice"), new Variable("A")),
					new QueryAtom(model.predicates.get("Nice"), new Variable("B")),
					new QueryAtom(SpecialPredicate.Equal, new Variable("A"), new Variable("B"))
				),
				new QueryAtom(model.predicates.get("Friends"), new Variable("A"), new Variable("B"))
			),
			1.0,
			true
		);

		// Remember, all rules will be in DNF.
		expected = Arrays.asList(
			"1.0: ( ~( NICE('Alice') ) | ~( NICE('Alice') ) | ~( ('Alice' == 'Alice') ) | FRIENDS('Alice', 'Alice') ) ^2",
			"1.0: ( ~( NICE('Bob') ) | ~( NICE('Bob') ) | ~( ('Bob' == 'Bob') ) | FRIENDS('Bob', 'Bob') ) ^2",
			"1.0: ( ~( NICE('Charlie') ) | ~( NICE('Charlie') ) | ~( ('Charlie' == 'Charlie') ) | FRIENDS('Charlie', 'Charlie') ) ^2",
			"1.0: ( ~( NICE('Derek') ) | ~( NICE('Derek') ) | ~( ('Derek' == 'Derek') ) | FRIENDS('Derek', 'Derek') ) ^2",
			"1.0: ( ~( NICE('Eugene') ) | ~( NICE('Eugene') ) | ~( ('Eugene' == 'Eugene') ) | FRIENDS('Eugene', 'Eugene') ) ^2"
		);
		rule.groundAll(manager, store);
		PSLTest.compareGroundRules(expected, rule, store);

		// Nice(A) & Nice(B) & (A != B) -> Friends(A, B)
		rule = new WeightedLogicalRule(
			new Implication(
				new Conjunction(
					new QueryAtom(model.predicates.get("Nice"), new Variable("A")),
					new QueryAtom(model.predicates.get("Nice"), new Variable("B")),
					new QueryAtom(SpecialPredicate.NotEqual, new Variable("A"), new Variable("B"))
				),
				new QueryAtom(model.predicates.get("Friends"), new Variable("A"), new Variable("B"))
			),
			1.0,
			true
		);

		// Remember, all rules will be in DNF.
		expected = Arrays.asList(
			"1.0: ( ~( NICE('Alice') ) | ~( NICE('Bob') ) | ~( ('Alice' != 'Bob') ) | FRIENDS('Alice', 'Bob') ) ^2",
			"1.0: ( ~( NICE('Alice') ) | ~( NICE('Charlie') ) | ~( ('Alice' != 'Charlie') ) | FRIENDS('Alice', 'Charlie') ) ^2",
			"1.0: ( ~( NICE('Alice') ) | ~( NICE('Derek') ) | ~( ('Alice' != 'Derek') ) | FRIENDS('Alice', 'Derek') ) ^2",
			"1.0: ( ~( NICE('Alice') ) | ~( NICE('Eugene') ) | ~( ('Alice' != 'Eugene') ) | FRIENDS('Alice', 'Eugene') ) ^2",
			"1.0: ( ~( NICE('Bob') ) | ~( NICE('Alice') ) | ~( ('Bob' != 'Alice') ) | FRIENDS('Bob', 'Alice') ) ^2",
			"1.0: ( ~( NICE('Bob') ) | ~( NICE('Charlie') ) | ~( ('Bob' != 'Charlie') ) | FRIENDS('Bob', 'Charlie') ) ^2",
			"1.0: ( ~( NICE('Bob') ) | ~( NICE('Derek') ) | ~( ('Bob' != 'Derek') ) | FRIENDS('Bob', 'Derek') ) ^2",
			"1.0: ( ~( NICE('Bob') ) | ~( NICE('Eugene') ) | ~( ('Bob' != 'Eugene') ) | FRIENDS('Bob', 'Eugene') ) ^2",
			"1.0: ( ~( NICE('Charlie') ) | ~( NICE('Alice') ) | ~( ('Charlie' != 'Alice') ) | FRIENDS('Charlie', 'Alice') ) ^2",
			"1.0: ( ~( NICE('Charlie') ) | ~( NICE('Bob') ) | ~( ('Charlie' != 'Bob') ) | FRIENDS('Charlie', 'Bob') ) ^2",
			"1.0: ( ~( NICE('Charlie') ) | ~( NICE('Derek') ) | ~( ('Charlie' != 'Derek') ) | FRIENDS('Charlie', 'Derek') ) ^2",
			"1.0: ( ~( NICE('Charlie') ) | ~( NICE('Eugene') ) | ~( ('Charlie' != 'Eugene') ) | FRIENDS('Charlie', 'Eugene') ) ^2",
			"1.0: ( ~( NICE('Derek') ) | ~( NICE('Alice') ) | ~( ('Derek' != 'Alice') ) | FRIENDS('Derek', 'Alice') ) ^2",
			"1.0: ( ~( NICE('Derek') ) | ~( NICE('Bob') ) | ~( ('Derek' != 'Bob') ) | FRIENDS('Derek', 'Bob') ) ^2",
			"1.0: ( ~( NICE('Derek') ) | ~( NICE('Charlie') ) | ~( ('Derek' != 'Charlie') ) | FRIENDS('Derek', 'Charlie') ) ^2",
			"1.0: ( ~( NICE('Derek') ) | ~( NICE('Eugene') ) | ~( ('Derek' != 'Eugene') ) | FRIENDS('Derek', 'Eugene') ) ^2",
			"1.0: ( ~( NICE('Eugene') ) | ~( NICE('Alice') ) | ~( ('Eugene' != 'Alice') ) | FRIENDS('Eugene', 'Alice') ) ^2",
			"1.0: ( ~( NICE('Eugene') ) | ~( NICE('Bob') ) | ~( ('Eugene' != 'Bob') ) | FRIENDS('Eugene', 'Bob') ) ^2",
			"1.0: ( ~( NICE('Eugene') ) | ~( NICE('Charlie') ) | ~( ('Eugene' != 'Charlie') ) | FRIENDS('Eugene', 'Charlie') ) ^2",
			"1.0: ( ~( NICE('Eugene') ) | ~( NICE('Derek') ) | ~( ('Eugene' != 'Derek') ) | FRIENDS('Eugene', 'Derek') ) ^2"
		);
		rule.groundAll(manager, store);
		PSLTest.compareGroundRules(expected, rule, store);

		// Nice(A) & Nice(B) & (A % B) -> Friends(A, B)
		rule = new WeightedLogicalRule(
			new Implication(
				new Conjunction(
					new QueryAtom(model.predicates.get("Nice"), new Variable("A")),
					new QueryAtom(model.predicates.get("Nice"), new Variable("B")),
					new QueryAtom(SpecialPredicate.NonSymmetric, new Variable("A"), new Variable("B"))
				),
				new QueryAtom(model.predicates.get("Friends"), new Variable("A"), new Variable("B"))
			),
			1.0,
			true
		);

		// Remember, all rules will be in DNF.
		expected = Arrays.asList(
			"1.0: ( ~( NICE('Alice') ) | ~( NICE('Bob') ) | ~( ('Alice' % 'Bob') ) | FRIENDS('Alice', 'Bob') ) ^2",
			"1.0: ( ~( NICE('Alice') ) | ~( NICE('Charlie') ) | ~( ('Alice' % 'Charlie') ) | FRIENDS('Alice', 'Charlie') ) ^2",
			"1.0: ( ~( NICE('Alice') ) | ~( NICE('Derek') ) | ~( ('Alice' % 'Derek') ) | FRIENDS('Alice', 'Derek') ) ^2",
			"1.0: ( ~( NICE('Alice') ) | ~( NICE('Eugene') ) | ~( ('Alice' % 'Eugene') ) | FRIENDS('Alice', 'Eugene') ) ^2",
			"1.0: ( ~( NICE('Bob') ) | ~( NICE('Charlie') ) | ~( ('Bob' % 'Charlie') ) | FRIENDS('Bob', 'Charlie') ) ^2",
			"1.0: ( ~( NICE('Bob') ) | ~( NICE('Derek') ) | ~( ('Bob' % 'Derek') ) | FRIENDS('Bob', 'Derek') ) ^2",
			"1.0: ( ~( NICE('Bob') ) | ~( NICE('Eugene') ) | ~( ('Bob' % 'Eugene') ) | FRIENDS('Bob', 'Eugene') ) ^2",
			"1.0: ( ~( NICE('Charlie') ) | ~( NICE('Derek') ) | ~( ('Charlie' % 'Derek') ) | FRIENDS('Charlie', 'Derek') ) ^2",
			"1.0: ( ~( NICE('Charlie') ) | ~( NICE('Eugene') ) | ~( ('Charlie' % 'Eugene') ) | FRIENDS('Charlie', 'Eugene') ) ^2",
			"1.0: ( ~( NICE('Derek') ) | ~( NICE('Eugene') ) | ~( ('Derek' % 'Eugene') ) | FRIENDS('Derek', 'Eugene') ) ^2"
		);
		rule.groundAll(manager, store);
		PSLTest.compareGroundRules(expected, rule, store);
	}

	@Test
	public void testArithmeticBase() {
		GroundRuleStore store = new ADMMReasoner(model.config);
		AtomManager manager = new SimpleAtomManager(database);

		Rule rule;
		List<String> expected;
		List<Coefficient> coefficients;
		List<SummationAtomOrAtom> atoms;

		// 1.0: Nice(A) + Nice(B) >= 1 ^2
		coefficients = Arrays.asList(
			(Coefficient)(new ConstantNumber(1)),
			(Coefficient)(new ConstantNumber(1))
		);

		atoms = Arrays.asList(
			(SummationAtomOrAtom)(new QueryAtom(model.predicates.get("Nice"), new Variable("A"))),
			(SummationAtomOrAtom)(new QueryAtom(model.predicates.get("Nice"), new Variable("B")))
		);

		rule = new WeightedArithmeticRule(
				new ArithmeticRuleExpression(coefficients, atoms, FunctionComparator.LargerThan, new ConstantNumber(1)),
				1.0,
				true
		);

		expected = Arrays.asList(
			"1.0: 1.0 NICE('Alice') 1.0 NICE('Alice') >= 1.0 ^2",
			"1.0: 1.0 NICE('Alice') 1.0 NICE('Bob') >= 1.0 ^2",
			"1.0: 1.0 NICE('Alice') 1.0 NICE('Charlie') >= 1.0 ^2",
			"1.0: 1.0 NICE('Alice') 1.0 NICE('Derek') >= 1.0 ^2",
			"1.0: 1.0 NICE('Alice') 1.0 NICE('Eugene') >= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') 1.0 NICE('Alice') >= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') 1.0 NICE('Bob') >= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') 1.0 NICE('Charlie') >= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') 1.0 NICE('Derek') >= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') 1.0 NICE('Eugene') >= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') 1.0 NICE('Alice') >= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') 1.0 NICE('Bob') >= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') 1.0 NICE('Charlie') >= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') 1.0 NICE('Derek') >= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') 1.0 NICE('Eugene') >= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') 1.0 NICE('Alice') >= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') 1.0 NICE('Bob') >= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') 1.0 NICE('Charlie') >= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') 1.0 NICE('Derek') >= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') 1.0 NICE('Eugene') >= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') 1.0 NICE('Alice') >= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') 1.0 NICE('Bob') >= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') 1.0 NICE('Charlie') >= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') 1.0 NICE('Derek') >= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') 1.0 NICE('Eugene') >= 1.0 ^2"
		);
		rule.groundAll(manager, store);
		PSLTest.compareGroundRules(expected, rule, store);

		// 1.0: Nice(A) + Nice(B) <= 1 ^2
		coefficients = Arrays.asList(
			(Coefficient)(new ConstantNumber(1)),
			(Coefficient)(new ConstantNumber(1))
		);

		atoms = Arrays.asList(
			(SummationAtomOrAtom)(new QueryAtom(model.predicates.get("Nice"), new Variable("A"))),
			(SummationAtomOrAtom)(new QueryAtom(model.predicates.get("Nice"), new Variable("B")))
		);

		rule = new WeightedArithmeticRule(
				new ArithmeticRuleExpression(coefficients, atoms, FunctionComparator.SmallerThan, new ConstantNumber(1)),
				1.0,
				true
		);

		expected = Arrays.asList(
			"1.0: 1.0 NICE('Alice') 1.0 NICE('Alice') <= 1.0 ^2",
			"1.0: 1.0 NICE('Alice') 1.0 NICE('Bob') <= 1.0 ^2",
			"1.0: 1.0 NICE('Alice') 1.0 NICE('Charlie') <= 1.0 ^2",
			"1.0: 1.0 NICE('Alice') 1.0 NICE('Derek') <= 1.0 ^2",
			"1.0: 1.0 NICE('Alice') 1.0 NICE('Eugene') <= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') 1.0 NICE('Alice') <= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') 1.0 NICE('Bob') <= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') 1.0 NICE('Charlie') <= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') 1.0 NICE('Derek') <= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') 1.0 NICE('Eugene') <= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') 1.0 NICE('Alice') <= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') 1.0 NICE('Bob') <= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') 1.0 NICE('Charlie') <= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') 1.0 NICE('Derek') <= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') 1.0 NICE('Eugene') <= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') 1.0 NICE('Alice') <= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') 1.0 NICE('Bob') <= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') 1.0 NICE('Charlie') <= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') 1.0 NICE('Derek') <= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') 1.0 NICE('Eugene') <= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') 1.0 NICE('Alice') <= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') 1.0 NICE('Bob') <= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') 1.0 NICE('Charlie') <= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') 1.0 NICE('Derek') <= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') 1.0 NICE('Eugene') <= 1.0 ^2"
		);
		rule.groundAll(manager, store);
		PSLTest.compareGroundRules(expected, rule, store);

		// 1.0: Nice(A) + -1 * Nice(B) = 0 ^2
		coefficients = Arrays.asList(
			(Coefficient)(new ConstantNumber(1)),
			(Coefficient)(new ConstantNumber(-1))
		);

		atoms = Arrays.asList(
			(SummationAtomOrAtom)(new QueryAtom(model.predicates.get("Nice"), new Variable("A"))),
			(SummationAtomOrAtom)(new QueryAtom(model.predicates.get("Nice"), new Variable("B")))
		);

		rule = new WeightedArithmeticRule(
				new ArithmeticRuleExpression(coefficients, atoms, FunctionComparator.Equality, new ConstantNumber(1)),
				1.0,
				true
		);

		// Remember, equality puts in a <= and >=.
		expected = Arrays.asList(
			"1.0: 1.0 NICE('Alice') -1.0 NICE('Alice') <= 1.0 ^2",
			"1.0: 1.0 NICE('Alice') -1.0 NICE('Bob') <= 1.0 ^2",
			"1.0: 1.0 NICE('Alice') -1.0 NICE('Charlie') <= 1.0 ^2",
			"1.0: 1.0 NICE('Alice') -1.0 NICE('Derek') <= 1.0 ^2",
			"1.0: 1.0 NICE('Alice') -1.0 NICE('Eugene') <= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') -1.0 NICE('Alice') <= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') -1.0 NICE('Bob') <= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') -1.0 NICE('Charlie') <= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') -1.0 NICE('Derek') <= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') -1.0 NICE('Eugene') <= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') -1.0 NICE('Alice') <= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') -1.0 NICE('Bob') <= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') -1.0 NICE('Charlie') <= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') -1.0 NICE('Derek') <= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') -1.0 NICE('Eugene') <= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') -1.0 NICE('Alice') <= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') -1.0 NICE('Bob') <= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') -1.0 NICE('Charlie') <= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') -1.0 NICE('Derek') <= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') -1.0 NICE('Eugene') <= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') -1.0 NICE('Alice') <= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') -1.0 NICE('Bob') <= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') -1.0 NICE('Charlie') <= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') -1.0 NICE('Derek') <= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') -1.0 NICE('Eugene') <= 1.0 ^2",

			"1.0: 1.0 NICE('Alice') -1.0 NICE('Alice') >= 1.0 ^2",
			"1.0: 1.0 NICE('Alice') -1.0 NICE('Bob') >= 1.0 ^2",
			"1.0: 1.0 NICE('Alice') -1.0 NICE('Charlie') >= 1.0 ^2",
			"1.0: 1.0 NICE('Alice') -1.0 NICE('Derek') >= 1.0 ^2",
			"1.0: 1.0 NICE('Alice') -1.0 NICE('Eugene') >= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') -1.0 NICE('Alice') >= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') -1.0 NICE('Bob') >= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') -1.0 NICE('Charlie') >= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') -1.0 NICE('Derek') >= 1.0 ^2",
			"1.0: 1.0 NICE('Bob') -1.0 NICE('Eugene') >= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') -1.0 NICE('Alice') >= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') -1.0 NICE('Bob') >= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') -1.0 NICE('Charlie') >= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') -1.0 NICE('Derek') >= 1.0 ^2",
			"1.0: 1.0 NICE('Charlie') -1.0 NICE('Eugene') >= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') -1.0 NICE('Alice') >= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') -1.0 NICE('Bob') >= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') -1.0 NICE('Charlie') >= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') -1.0 NICE('Derek') >= 1.0 ^2",
			"1.0: 1.0 NICE('Derek') -1.0 NICE('Eugene') >= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') -1.0 NICE('Alice') >= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') -1.0 NICE('Bob') >= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') -1.0 NICE('Charlie') >= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') -1.0 NICE('Derek') >= 1.0 ^2",
			"1.0: 1.0 NICE('Eugene') -1.0 NICE('Eugene') >= 1.0 ^2"
		);
		rule.groundAll(manager, store);
		PSLTest.compareGroundRules(expected, rule, store);
	}

	@Test
	// Everyone is 100% Nice in this test.
	public void testArithmeticSelectNice() {
		GroundRuleStore store = new ADMMReasoner(model.config);
		AtomManager manager = new SimpleAtomManager(database);

		Rule rule;
		List<String> expected;
		List<Coefficient> coefficients;
		List<SummationAtomOrAtom> atoms;
		Map<SummationVariable, Formula> selects;

		// 1.0: Friends(A, +B) >= 1 ^2 {B: Nice(B)}
		coefficients = Arrays.asList(
			(Coefficient)(new ConstantNumber(1))
		);

		atoms = Arrays.asList(
			(SummationAtomOrAtom)(new SummationAtom(
				model.predicates.get("Friends"),
				new SummationVariableOrTerm[]{new Variable("A"), new SummationVariable("B")}
			))
		);

		selects = new HashMap<SummationVariable, Formula>();
		selects.put(new SummationVariable("B"), new QueryAtom(model.predicates.get("Nice"), new Variable("B")));

		rule = new WeightedArithmeticRule(
				new ArithmeticRuleExpression(coefficients, atoms, FunctionComparator.LargerThan, new ConstantNumber(1)),
				1.0,
				true
		);

		expected = Arrays.asList(
			"1.0: 1.0 FRIENDS('Alice', 'Alice') 1.0 FRIENDS('Alice', 'Bob') 1.0 FRIENDS('Alice', 'Charlie') 1.0 FRIENDS('Alice', 'Derek') 1.0 FRIENDS('Alice', 'Eugene') >= 1.0 ^2",
			"1.0: 1.0 FRIENDS('Bob', 'Alice') 1.0 FRIENDS('Bob', 'Bob') 1.0 FRIENDS('Bob', 'Charlie') 1.0 FRIENDS('Bob', 'Derek') 1.0 FRIENDS('Bob', 'Eugene') >= 1.0 ^2",
			"1.0: 1.0 FRIENDS('Charlie', 'Alice') 1.0 FRIENDS('Charlie', 'Bob') 1.0 FRIENDS('Charlie', 'Charlie') 1.0 FRIENDS('Charlie', 'Derek') 1.0 FRIENDS('Charlie', 'Eugene') >= 1.0 ^2",
			"1.0: 1.0 FRIENDS('Derek', 'Alice') 1.0 FRIENDS('Derek', 'Bob') 1.0 FRIENDS('Derek', 'Charlie') 1.0 FRIENDS('Derek', 'Derek') 1.0 FRIENDS('Derek', 'Eugene') >= 1.0 ^2",
			"1.0: 1.0 FRIENDS('Eugene', 'Alice') 1.0 FRIENDS('Eugene', 'Bob') 1.0 FRIENDS('Eugene', 'Charlie') 1.0 FRIENDS('Eugene', 'Derek') 1.0 FRIENDS('Eugene', 'Eugene') >= 1.0 ^2"
		);
		rule.groundAll(manager, store);
		PSLTest.compareGroundRules(expected, rule, store, true);

		// 1.0: Friends(A, +B) >= 1 ^2 {B: !Nice(B)}
		coefficients = Arrays.asList(
			(Coefficient)(new ConstantNumber(1))
		);

		atoms = Arrays.asList(
			(SummationAtomOrAtom)(new SummationAtom(
				model.predicates.get("Friends"),
				new SummationVariableOrTerm[]{new Variable("A"), new SummationVariable("B")}
			))
		);

		selects = new HashMap<SummationVariable, Formula>();
		selects.put(new SummationVariable("B"), new Negation(new QueryAtom(model.predicates.get("Nice"), new Variable("B"))));

		rule = new WeightedArithmeticRule(
				new ArithmeticRuleExpression(coefficients, atoms, FunctionComparator.LargerThan, new ConstantNumber(1)),
				1.0,
				true
		);

		// All groundings should be removed by the select.
		expected = new ArrayList<String>();
		rule.groundAll(manager, store);
		PSLTest.compareGroundRules(expected, rule, store, false);
	}
	*/

	@Test
	// Everyone is 100% Nice in this test.
	public void testArithmeticSelect() {
		GroundRuleStore store = new ADMMReasoner(model.config);
		AtomManager manager = new SimpleAtomManager(database);

		Rule rule;
		List<String> expected;
		List<Coefficient> coefficients;
		List<SummationAtomOrAtom> atoms;
		Map<SummationVariable, Formula> selects;

		// 1.0: |B| * Friends(A, +B) >= 1 ^2 {B: Nice(B)}
		coefficients = Arrays.asList(
			(Coefficient)(new Cardinality(new SummationVariable("B")))
		);

		atoms = Arrays.asList(
			(SummationAtomOrAtom)(new SummationAtom(
				model.predicates.get("Friends"),
				new SummationVariableOrTerm[]{new Variable("A"), new SummationVariable("B")}
			))
		);

		selects = new HashMap<SummationVariable, Formula>();
		// selects.put(new SummationVariable("B"), new QueryAtom(model.predicates.get("Nice"), new Variable("B")));
		selects.put(
				new SummationVariable("B"),
				new Disjunction(
						new QueryAtom(model.predicates.get("Nice"), new Variable("B")),
						new QueryAtom(model.predicates.get("Nice"), new Variable("A"))
				)
		);

		rule = new WeightedArithmeticRule(
				new ArithmeticRuleExpression(coefficients, atoms, FunctionComparator.LargerThan, new ConstantNumber(1)),
				selects,
				1.0,
				true
		);

		// TEST
		System.out.println("Rule: " + rule.toString());

		// TEST
		System.out.println("Nice Atoms:");
		for (Atom atom : Queries.getAllAtoms(database, model.predicates.get("Nice"))) {
			System.out.println("   " + atom + " - " + ((ObservedAtom)atom).getValue());
		}

		// Note that 'Eugene' is not present because Nice('Eugene') = 0.
		expected = Arrays.asList(
			"1.0: 4.0 FRIENDS('Alice', 'Alice') 4.0 FRIENDS('Alice', 'Bob') 4.0 FRIENDS('Alice', 'Charlie') 4.0 FRIENDS('Alice', 'Derek') >= 1.0 ^2",
			"1.0: 4.0 FRIENDS('Bob', 'Alice') 4.0 FRIENDS('Bob', 'Bob') 4.0 FRIENDS('Bob', 'Charlie') 4.0 FRIENDS('Bob', 'Derek') >= 1.0 ^2",
			"1.0: 4.0 FRIENDS('Charlie', 'Alice') 4.0 FRIENDS('Charlie', 'Bob') 4.0 FRIENDS('Charlie', 'Charlie') 4.0 FRIENDS('Charlie', 'Derek') >= 1.0 ^2",
			"1.0: 4.0 FRIENDS('Derek', 'Alice') 4.0 FRIENDS('Derek', 'Bob') 4.0 FRIENDS('Derek', 'Charlie') 4.0 FRIENDS('Derek', 'Derek') >= 1.0 ^2"
		);
		rule.groundAll(manager, store);
		// TEST: Don't worry about the actual comparison for now, we can just visually inspect.
		// PSLTest.compareGroundRules(expected, rule, store, true);
		PSLTest.compareGroundRules(expected, rule, store, false);
	}

	/*
	@Test
	// Alice is 100% nice, Bob is 80%, and so on.
	public void testArithmeticSelect() {
		// Reset the model.
		database.close();
		model.dataStore.close();

		model = TestModelFactory.getModel(false);
		Set<StandardPredicate> toClose = new HashSet<StandardPredicate>();
		database = model.dataStore.getDatabase(model.targetPartition, toClose, model.observationPartition);

		GroundRuleStore store = new ADMMReasoner(model.config);
		AtomManager manager = new SimpleAtomManager(database);

		Rule rule;
		List<String> expected;
		List<Coefficient> coefficients;
		List<SummationAtomOrAtom> atoms;
		Map<SummationVariable, Formula> selects;

		// 1.0: Friends(A, +B) >= 1 ^2 {B: !Nice(B)}
		coefficients = Arrays.asList(
			(Coefficient)(new ConstantNumber(1))
		);

		atoms = Arrays.asList(
			(SummationAtomOrAtom)(new SummationAtom(
				model.predicates.get("Friends"),
				new SummationVariableOrTerm[]{new Variable("A"), new SummationVariable("B")}
			))
		);

		selects = new HashMap<SummationVariable, Formula>();
		selects.put(new SummationVariable("B"), new Negation(new QueryAtom(model.predicates.get("Nice"), new Variable("B"))));

		rule = new WeightedArithmeticRule(
				new ArithmeticRuleExpression(coefficients, atoms, FunctionComparator.LargerThan, new ConstantNumber(1)),
				1.0,
				true
		);

		expected = Arrays.asList(
			"1.0: 1.0 FRIENDS('Alice', 'Bob') 1.0 FRIENDS('Alice', 'Charlie') 1.0 FRIENDS('Alice', 'Derek') 1.0 FRIENDS('Alice', 'Eugene') >= 1.0 ^2",
			"1.0: 1.0 FRIENDS('Bob', 'Bob') 1.0 FRIENDS('Bob', 'Charlie') 1.0 FRIENDS('Bob', 'Derek') 1.0 FRIENDS('Bob', 'Eugene') >= 1.0 ^2",
			"1.0: 1.0 FRIENDS('Charlie', 'Bob') 1.0 FRIENDS('Charlie', 'Charlie') 1.0 FRIENDS('Charlie', 'Derek') 1.0 FRIENDS('Charlie', 'Eugene') >= 1.0 ^2",
			"1.0: 1.0 FRIENDS('Derek', 'Bob') 1.0 FRIENDS('Derek', 'Charlie') 1.0 FRIENDS('Derek', 'Derek') 1.0 FRIENDS('Derek', 'Eugene') >= 1.0 ^2",
			"1.0: 1.0 FRIENDS('Eugene', 'Bob') 1.0 FRIENDS('Eugene', 'Charlie') 1.0 FRIENDS('Eugene', 'Derek') 1.0 FRIENDS('Eugene', 'Eugene') >= 1.0 ^2"
		);
		rule.groundAll(manager, store);
		PSLTest.compareGroundRules(expected, rule, store, true);
	}
	*/

	/*
			'Alice', 'Alice',
			'Alice', 'Bob',
			'Alice', 'Charlie',
			'Alice', 'Derek',
			'Alice', 'Eugene',
			'Bob', 'Alice',
			'Bob', 'Bob',
			'Bob', 'Charlie',
			'Bob', 'Derek',
			'Bob', 'Eugene',
			'Charlie', 'Alice',
			'Charlie', 'Bob',
			'Charlie', 'Charlie',
			'Charlie', 'Derek',
			'Charlie', 'Eugene',
			'Derek', 'Alice',
			'Derek', 'Bob',
			'Derek', 'Charlie',
			'Derek', 'Derek',
			'Derek', 'Eugene',
			'Eugene', 'Alice',
			'Eugene', 'Bob',
			'Eugene', 'Charlie',
			'Eugene', 'Derek',
			'Eugene', 'Eugene'
	@Test
	public void testGroundLogicalRuleString() {
		GroundRuleStore store = new ADMMReasoner(config);
		AtomManager manager = new SimpleAtomManager(database);

		Rule rule;
		List<String> expected;

		// Unweighted (Not Squared)
		rule = new UnweightedLogicalRule(logicalBaseRule);
		// Remember, all rules will be in DNF.
		expected = Arrays.asList(
			"( ~( NICE('Alice') ) | ~( NICE('Alice') ) | Friends('Alice', 'Alice') ) .",
			"( ~( NICE('Alice') ) | ~( NICE('Bob') ) | Friends('Alice', 'Bob') ) .",
			"( ~( NICE('Bob') ) | ~( NICE('Alice') ) | Friends('Bob', 'Alice') ) .",
			"( ~( NICE('Bob') ) | ~( NICE('Bob') ) | Friends('Bob', 'Bob') ) ."
		);
		rule.groundAll(manager, store);
		PSLTest.compareGroundRules(expected, rule, store);

		// Weighted, Squared
		rule = new WeightedLogicalRule(logicalBaseRule, 10.0, true);
		expected = Arrays.asList(
			"10.0: ( ~( NICE('Alice') ) | ~( NICE('Alice') ) | Friends('Alice', 'Alice') ) ^2",
			"10.0: ( ~( NICE('Alice') ) | ~( NICE('Bob') ) | Friends('Alice', 'Bob') ) ^2",
			"10.0: ( ~( NICE('Bob') ) | ~( NICE('Alice') ) | Friends('Bob', 'Alice') ) ^2",
			"10.0: ( ~( NICE('Bob') ) | ~( NICE('Bob') ) | Friends('Bob', 'Bob') ) ^2"
		);
		rule.groundAll(manager, store);
		PSLTest.compareGroundRules(expected, rule, store);

		// Weighted, Not Squared
		rule = new WeightedLogicalRule(logicalBaseRule, 10.0, false);
		expected = Arrays.asList(
			"10.0: ( ~( NICE('Alice') ) | ~( NICE('Alice') ) | Friends('Alice', 'Alice') )",
			"10.0: ( ~( NICE('Alice') ) | ~( NICE('Bob') ) | Friends('Alice', 'Bob') )",
			"10.0: ( ~( NICE('Bob') ) | ~( NICE('Alice') ) | Friends('Bob', 'Alice') )",
			"10.0: ( ~( NICE('Bob') ) | ~( NICE('Bob') ) | Friends('Bob', 'Bob') )"
		);
		rule.groundAll(manager, store);
		PSLTest.compareGroundRules(expected, rule, store);
	}

	@Test
	public void testGroundArithmeticRuleString() {
		GroundRuleStore store = new ADMMReasoner(config);
		AtomManager manager = new SimpleAtomManager(database);

		Rule rule;
		List<String> expected;

		// Unweighted (Not Squared)
		rule = new UnweightedArithmeticRule(arithmeticBaseRule);
		// Remember, equality inserts two rules (<= and >=).
		expected = Arrays.asList(
			"1.0 NICE('Alice') 1.0 NICE('Alice') <= 1.0 .",
			"1.0 NICE('Alice') 1.0 NICE('Alice') >= 1.0 .",
			"1.0 NICE('Alice') 1.0 NICE('Bob') <= 1.0 .",
			"1.0 NICE('Alice') 1.0 NICE('Bob') >= 1.0 .",
			"1.0 NICE('Bob') 1.0 NICE('Alice') <= 1.0 .",
			"1.0 NICE('Bob') 1.0 NICE('Alice') >= 1.0 .",
			"1.0 NICE('Bob') 1.0 NICE('Bob') <= 1.0 .",
			"1.0 NICE('Bob') 1.0 NICE('Bob') >= 1.0 ."
		);
		rule.groundAll(manager, store);
		PSLTest.compareGroundRules(expected, rule, store);

		// Weighted, Squared
		rule = new WeightedArithmeticRule(arithmeticBaseRule,	10.0, true);
		expected = Arrays.asList(
			"10.0: 1.0 NICE('Alice') 1.0 NICE('Alice') <= 1.0 ^2",
			"10.0: 1.0 NICE('Alice') 1.0 NICE('Alice') >= 1.0 ^2",
			"10.0: 1.0 NICE('Alice') 1.0 NICE('Bob') <= 1.0 ^2",
			"10.0: 1.0 NICE('Alice') 1.0 NICE('Bob') >= 1.0 ^2",
			"10.0: 1.0 NICE('Bob') 1.0 NICE('Alice') <= 1.0 ^2",
			"10.0: 1.0 NICE('Bob') 1.0 NICE('Alice') >= 1.0 ^2",
			"10.0: 1.0 NICE('Bob') 1.0 NICE('Bob') <= 1.0 ^2",
			"10.0: 1.0 NICE('Bob') 1.0 NICE('Bob') >= 1.0 ^2"
		);
		rule.groundAll(manager, store);
		PSLTest.compareGroundRules(expected, rule, store);

		// Weighted, Not Squared
		rule = new WeightedArithmeticRule(arithmeticBaseRule,	10.0, false);
		expected = Arrays.asList(
			"10.0: 1.0 NICE('Alice') 1.0 NICE('Alice') <= 1.0",
			"10.0: 1.0 NICE('Alice') 1.0 NICE('Alice') >= 1.0",
			"10.0: 1.0 NICE('Alice') 1.0 NICE('Bob') <= 1.0",
			"10.0: 1.0 NICE('Alice') 1.0 NICE('Bob') >= 1.0",
			"10.0: 1.0 NICE('Bob') 1.0 NICE('Alice') <= 1.0",
			"10.0: 1.0 NICE('Bob') 1.0 NICE('Alice') >= 1.0",
			"10.0: 1.0 NICE('Bob') 1.0 NICE('Bob') <= 1.0",
			"10.0: 1.0 NICE('Bob') 1.0 NICE('Bob') >= 1.0"
		);
		rule.groundAll(manager, store);
		PSLTest.compareGroundRules(expected, rule, store);
	}
	*/

	@After
	public void cleanup() {
		database.close();
		model.dataStore.close();
	}
}
