/*
 * Copyright 2015 United States Government, as represented by the Administrator
 *                of the National Aeronautics and Space Administration. All Rights Reserved.
 *           2017-2021 The jConstraints Authors
 * SPDX-License-Identifier: Apache-2.0
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

/**
 * Redistribution with Modifications of jconstraints-z3:
 * https://github.com/tudo-aqua/jconstraints-z3/commit/a9ab06a29f426cc3f1dd1f8406ebba8b65cf9f5d
 *
 * <p>Copyright (C) 2015, United States Government, as represented by the Administrator of the
 * National Aeronautics and Space Administration. All rights reserved.
 *
 * <p>The PSYCO: A Predicate-based Symbolic Compositional Reasoning environment platform is licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * <p>Modifications are Copyright 2020 TU Dortmund, Malte Mues (@mmuesly, mail.mues@gmail.com) We
 * license the changes and additions to this repository under Apache License, Version 2.0.
 */
package gov.nasa.jpf.constraints.expressions;

import static gov.nasa.jpf.constraints.api.ConstraintSolver.Result.UNSAT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import gov.nasa.jpf.constraints.api.ConstraintSolver;
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.constraints.api.Variable;
import gov.nasa.jpf.constraints.solvers.ConstraintSolverFactory;
import gov.nasa.jpf.constraints.solvers.nativez3.NativeZ3Solver;
import gov.nasa.jpf.constraints.types.BuiltinTypes;
import java.util.Properties;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StringSupportTest {

  private NativeZ3Solver solver;

  @BeforeMethod
  public void initialize() {
    Properties conf = new Properties();
    conf.setProperty("symbolic.dp", "z3");
    conf.setProperty("z3.options", "smt.string_solver=seq");
    ConstraintSolverFactory factory = new ConstraintSolverFactory();
    solver = (NativeZ3Solver) factory.createSolver("z3", conf);
  }

  @Test
  public void strLenTest() {
    Constant c5 = Constant.create(BuiltinTypes.SINT32, 5);
    Variable string = Variable.create(BuiltinTypes.STRING, "x1");
    Expression len = StringIntegerExpression.createLength(string);
    len = CastExpression.create(len, BuiltinTypes.SINT32);
    NumericBooleanExpression compLen =
        NumericBooleanExpression.create(len, NumericComparator.EQ, c5);

    Valuation val = new Valuation();
    ConstraintSolver.Result res = solver.solve(compLen, val);
    assertEquals(res, ConstraintSolver.Result.SAT);
    if (res == ConstraintSolver.Result.SAT) {
      assertTrue(compLen.evaluate(val));
    }
  }

  @Test
  public void strLen2Test() {
    Constant c5 = Constant.create(BuiltinTypes.SINT32, 5);
    Variable string = Variable.create(BuiltinTypes.STRING, "x1");
    Expression len = StringIntegerExpression.createLength(string);
    len = CastExpression.create(len, BuiltinTypes.SINT32);
    NumericBooleanExpression compLen =
        NumericBooleanExpression.create(len, NumericComparator.EQ, c5);

    Constant<String> cHallo = Constant.create(BuiltinTypes.STRING, "Hallo");
    StringBooleanExpression strEq = StringBooleanExpression.createEquals(string, cHallo);

    Expression finalExpr = PropositionalCompound.create(compLen, LogicalOperator.AND, strEq);

    Valuation val = new Valuation();
    ConstraintSolver.Result res = solver.solve(finalExpr, val);
    assertEquals(res, ConstraintSolver.Result.SAT);
    boolean equals = compLen.evaluate(val);
    assertTrue(equals);
  }

  @Test
  public void autoCastStrAtTest() {
    Constant c4 = Constant.create(BuiltinTypes.SINT32, 5);
    Variable strVar = Variable.create(BuiltinTypes.STRING, "string0");
    Expression stringAt = StringCompoundExpression.createAt(strVar, c4);
    Constant stringExpected = Constant.create(BuiltinTypes.STRING, "c");
    stringAt = StringBooleanExpression.createEquals(stringAt, stringExpected);

    Valuation val = new Valuation();
    ConstraintSolver.Result res = solver.solve(stringAt, val);
    assertEquals(res, ConstraintSolver.Result.SAT);
    boolean equals = (boolean) stringAt.evaluate(val);
    assertTrue(equals);
  }

  @Test
  public void toAndFromIntEvaluationTest() {
    Variable x = Variable.create(BuiltinTypes.STRING, "x");
    Constant c = Constant.create(BuiltinTypes.STRING, "10");
    Expression toInt = StringIntegerExpression.createToInt(x);
    Expression fromInt = StringCompoundExpression.createToString(toInt);
    StringBooleanExpression equals = StringBooleanExpression.createEquals(fromInt, c);

    Valuation val = new Valuation();
    ConstraintSolver.Result res = solver.solve(equals, val);
    assertEquals(res, ConstraintSolver.Result.SAT);
    assertTrue(equals.evaluate(val));
  }

  @Test
  public void stringInReTest() {
    Constant c = Constant.create(BuiltinTypes.STRING, "av");
    RegExBooleanExpression rbe =
        RegExBooleanExpression.create(c, RegexOperatorExpression.createAllChar());
    Valuation val = new Valuation();
    ConstraintSolver.Result res = solver.solve(rbe, val);
    assertEquals(res, UNSAT);
  }

  @Test
  public void concatTest() {
    Variable a = Variable.create(BuiltinTypes.STRING, "a");
    Variable b = Variable.create(BuiltinTypes.STRING, "b");
    Variable c = Variable.create(BuiltinTypes.STRING, "c");
    Expression sce = StringCompoundExpression.createConcat(a, b, c);
    Expression sbe =
        StringBooleanExpression.createEquals(sce, Constant.create(BuiltinTypes.STRING, "hallo"));
    Valuation val = new Valuation();
    ConstraintSolver.Result res = solver.solve(sbe, val);
    assertEquals(res, ConstraintSolver.Result.SAT);
    assertTrue((Boolean) sbe.evaluate(val));
  }

  @Test
  public void concat2Test() {
    Constant a = Constant.create(BuiltinTypes.STRING, "ha");
    Constant b = Constant.create(BuiltinTypes.STRING, "ll");
    Constant c = Constant.create(BuiltinTypes.STRING, "o");
    Expression sce = StringCompoundExpression.createConcat(a, b, c);
    Expression sbe =
        StringBooleanExpression.createEquals(sce, Constant.create(BuiltinTypes.STRING, "hallo"));
    Valuation val = new Valuation();
    ConstraintSolver.Result res = solver.solve(sbe, val);
    assertEquals(res, ConstraintSolver.Result.SAT);
    assertTrue((Boolean) sbe.evaluate(val));
  }

  @Test
  public void concat3Test() {
    Variable a = Variable.create(BuiltinTypes.STRING, "a");
    Variable b = Variable.create(BuiltinTypes.STRING, "b");
    Constant c = Constant.create(BuiltinTypes.STRING, "o");
    Expression sce = StringCompoundExpression.createConcat(a, b, c);
    Expression sbe =
        StringBooleanExpression.createEquals(sce, Constant.create(BuiltinTypes.STRING, "hallo"));
    Valuation val = new Valuation();
    ConstraintSolver.Result res = solver.solve(sbe, val);
    assertEquals(res, ConstraintSolver.Result.SAT);
    assertTrue((Boolean) sbe.evaluate(val));
  }

  //	@Test
  //	public void nativeConcatTest() {
  //		Context ctx = new Context();
  //		Expr<SeqSort<BitVecSort>> a = ctx.mkConst("a", ctx.getStringSort());
  //		Expr<SeqSort<BitVecSort>> b = ctx.mkConst("b", ctx.getStringSort());
  //		SeqExpr<BitVecSort> constant = ctx.mkString("test");
  //		Expr concat = ctx.mkConcat(a, b);
  //		Expr eq = ctx.mkEq(concat, constant);
  //		Solver s = ctx.mkSolver();
  //		assertEquals(s.check(eq), Status.SATISFIABLE);
  //	}
}
