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
package com.google.gdt.eclipse.designer.moz.jsni;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;

import com.google.gwt.dev.shell.designtime.DispatchIdOracle;
import com.google.gwt.dev.shell.designtime.JsValue;
import com.google.gwt.dev.shell.designtime.JsValueGlue;
import com.google.gwt.dev.shell.designtime.MethodAdaptor;
import com.google.gwt.dev.shell.designtime.ModuleSpace;

/**
 * Wraps an arbitrary Java Method as a Dispatchable component. The class was
 * motivated by the need to expose Java objects into JavaScript.
 */
abstract class MethodDispatch {

  private final WeakReference<ClassLoader> classLoaderRef;
  private final WeakReference<DispatchIdOracle> dispatchIdOracleRef;
  private final MethodAdaptor method;
	  
  public MethodDispatch(ClassLoader classLoader, DispatchIdOracle dispIdOracle, MethodAdaptor method) {
    this.classLoaderRef = new WeakReference<ClassLoader>(classLoader);
    this.dispatchIdOracleRef = new WeakReference<DispatchIdOracle>(dispIdOracle);
    this.method = method;
  }

  /**
   * Invoke a Java method from JavaScript. This is called solely from native
   * code.
   * 
   * @param jsthis JavaScript reference to Java object
   * @param jsargs array of JavaScript values for parameters
   * @param returnValue JavaScript value to return result in
   * @throws RuntimeException if improper arguments are supplied
   * 
   * TODO(jat): lift most of this interface to platform-independent code (only
   * exceptions still need to be made platform-independent)
   */
  protected void invoke0(JsValue jsthis, JsValue[] jsargs, JsValue returnValue) {
    ClassLoader classLoader = classLoaderRef.get();
    if (classLoader == null) {
      throw new RuntimeException("Invalid class loader.");
    }
    DispatchIdOracle dispatchIdOracle = dispatchIdOracleRef.get();
    if (dispatchIdOracle == null) {
      throw new RuntimeException("Invalid dispatch oracle.");
    }	  
  	Class<?>[] paramTypes = method.getParameterTypes();
    int argc = paramTypes.length;
    Object args[] = new Object[argc];
    // too many arguments are ok: the extra will be silently ignored
    if (jsargs.length < argc) {
      throw new RuntimeException("Not enough arguments to " + method);
    }
    Object jthis = null;
    if (method.needsThis()) {
      jthis = JsValueGlue.get(jsthis, classLoader, method.getDeclaringClass(),
          "invoke this");
    }
    for (int i = 0; i < argc; ++i) {
      args[i] = JsValueGlue.get(jsargs[i], classLoader, paramTypes[i],
          "invoke arguments");
    }
    try {
      Object result;
      try {
        result = method.invoke(jthis, args);
      } catch (IllegalAccessException e) {
        // should never, ever happen
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      JsValueGlue.set(returnValue, classLoader, dispatchIdOracle, method.getReturnType(), result);
    } catch (InstantiationException e) {
      // If we get here, it means an exception is being thrown from
      // Java back into JavaScript
      Throwable t = e.getCause();
      // TODO(jat): if this was originally JavaScript exception, re-throw the
      // original exception rather than just a null.
      ModuleSpace.setThrownJavaException(t);
      LowLevelMoz.raiseJavaScriptException();
    } catch (InvocationTargetException e) {
      // If we get here, it means an exception is being thrown from
      // Java back into JavaScript
      Throwable t = e.getTargetException();
      // TODO(jat): if this was originally JavaScript exception, re-throw the
      // original exception rather than just a null.
      ModuleSpace.setThrownJavaException(t);
      LowLevelMoz.raiseJavaScriptException();
    } catch (IllegalArgumentException e) {
      // TODO(jat): log to treelogger instead? If so, how do I get to it?
      System.err.println("MethodDispatch.invoke, method=" + method.toString()
          + ": argument mismatch");
      for (int i = 0; i < argc; ++i) {
        System.err.println(" param " + i + " type is "
            + paramTypes[i].toString() + " value is type "
            + jsargs[i].getTypeString() + " = " + args[i].toString());
      }
      throw e;
    }
  }
}
