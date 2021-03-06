/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.google.gwt.dev.js;

import com.google.gwt.dev.js.ast.JsBlock;
import com.google.gwt.dev.js.ast.JsBreak;
import com.google.gwt.dev.js.ast.JsContext;
import com.google.gwt.dev.js.ast.JsDebugger;
import com.google.gwt.dev.js.ast.JsDoWhile;
import com.google.gwt.dev.js.ast.JsEmpty;
import com.google.gwt.dev.js.ast.JsExprStmt;
import com.google.gwt.dev.js.ast.JsFor;
import com.google.gwt.dev.js.ast.JsForIn;
import com.google.gwt.dev.js.ast.JsIf;
import com.google.gwt.dev.js.ast.JsLabel;
import com.google.gwt.dev.js.ast.JsReturn;
import com.google.gwt.dev.js.ast.JsStatement;
import com.google.gwt.dev.js.ast.JsSwitch;
import com.google.gwt.dev.js.ast.JsThrow;
import com.google.gwt.dev.js.ast.JsTry;
import com.google.gwt.dev.js.ast.JsVars;
import com.google.gwt.dev.js.ast.JsVisitor;
import com.google.gwt.dev.js.ast.JsWhile;

/**
 * Determines if a statement at the end of a block requires a semicolon.
 * 
 * For example, the following statements require semicolons:<br>
 * <ul>
 * <li>if (cond);</li>
 * <li>while (cond);</li>
 * </ul>
 * 
 * The following do not require semicolons:<br>
 * <ul>
 * <li>return 1</li>
 * <li>do {} while(true)</li>
 * </ul>
 */
public class JsRequiresSemiVisitor extends JsVisitor {

  public static boolean exec(JsStatement lastStatement) {
    JsRequiresSemiVisitor visitor = new JsRequiresSemiVisitor();
    visitor.accept(lastStatement);
    return visitor.needsSemicolon;
  }

  private boolean needsSemicolon = false;

  private JsRequiresSemiVisitor() {
  }

  public boolean visit(JsBlock x, JsContext<JsStatement> ctx) {
    return false;
  }

  public boolean visit(JsBreak x, JsContext<JsStatement> ctx) {
    return false;
  }

  public boolean visit(JsDebugger x, JsContext<JsStatement> ctx) {
    return false;
  }

  public boolean visit(JsDoWhile x, JsContext<JsStatement> ctx) {
    return false;
  }

  public boolean visit(JsEmpty x, JsContext<JsStatement> ctx) {
    return false;
  }

  public boolean visit(JsExprStmt x, JsContext<JsStatement> ctx) {
    return false;
  }

  public boolean visit(JsFor x, JsContext<JsStatement> ctx) {
    if (x.getBody() instanceof JsEmpty) {
      needsSemicolon = true;
    }
    return false;
  }

  public boolean visit(JsForIn x, JsContext<JsStatement> ctx) {
    if (x.getBody() instanceof JsEmpty) {
      needsSemicolon = true;
    }
    return false;
  }

  public boolean visit(JsIf x, JsContext<JsStatement> ctx) {
    JsStatement thenStmt = x.getThenStmt();
    JsStatement elseStmt = x.getElseStmt();
    JsStatement toCheck = thenStmt;
    if (elseStmt != null) {
      toCheck = elseStmt;
    }
    if (toCheck instanceof JsEmpty) {
      needsSemicolon = true;
    } else {
      // Must recurse to determine last statement (possible if-else chain).
      accept(toCheck);
    }
    return false;
  }

  public boolean visit(JsLabel x, JsContext<JsStatement> ctx) {
    if (x.getStmt() instanceof JsEmpty) {
      needsSemicolon = true;
    }
    return false;
  }

  public boolean visit(JsReturn x, JsContext<JsStatement> ctx) {
    return false;
  }

  public boolean visit(JsSwitch x, JsContext<JsStatement> ctx) {
    return false;
  }

  public boolean visit(JsThrow x, JsContext<JsStatement> ctx) {
    return false;
  }

  public boolean visit(JsTry x, JsContext<JsStatement> ctx) {
    return false;
  }

  public boolean visit(JsVars x, JsContext<JsStatement> ctx) {
    return false;
  }

  public boolean visit(JsWhile x, JsContext<JsStatement> ctx) {
    if (x.getBody() instanceof JsEmpty) {
      needsSemicolon = true;
    }
    return false;
  }

}
