package com.thingworx.extension.custom.scriptengine;

import com.thingworx.logging.LogUtilities;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;
import org.python.core.PyBoolean;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;

public class ScriptEngineResource extends Resource {

  private final static Logger SCRIPT_LOGGER = LogUtilities.getInstance().getScriptLogger(ScriptEngineResource.class);
  private static final long serialVersionUID = 1L;

  @ThingworxServiceDefinition(name = "execPython", description = "", category = "", isAllowOverride = false, aspects = {"isAsync:false"})
  @ThingworxServiceResult(name = "result", description = "", baseType = "VARIANT")
  @SuppressWarnings("null")
  public Object execPython(
          @ThingworxServiceParameter(name = "parameters", description = "", baseType = "JSON", aspects = {"isRequired:true"}) JSONObject parameters,
          @ThingworxServiceParameter(name = "resultParameter", description = "", baseType = "STRING", aspects = {"isRequired:true"}) String resultParameter,
          @ThingworxServiceParameter(name = "code", description = "", baseType = "STRING", aspects = {"isRequired:true"}) String code) throws Exception {
    SCRIPT_LOGGER.debug("ScriptEngineResource - execPython -> Start");

    Object result;
    try (PythonInterpreter pyInterp = new PythonInterpreter()) {
      for (Iterator<String> iter = parameters.keys(); iter.hasNext();) {
        String name = iter.next();

        Object parameter = parameters.get(name);
        if (parameter instanceof Boolean) {
          pyInterp.set(name, new PyBoolean(parameters.getBoolean(name)));
        } else if (parameter instanceof Number) {
          pyInterp.set(name, new PyFloat(parameters.getDouble(name)));
        } else if (parameter instanceof String) {
          pyInterp.set(name, new PyString(parameters.getString(name)));
        } else if (parameter instanceof JSONArray) {
          pyInterp.set(name, this.putIntoArrayPython(name, parameters.getJSONArray(name)));
        } else {
          throw new Exception("parameter " + name + " is a not supported type " + parameter.getClass());
        }
      }

      pyInterp.exec(code);

      PyObject pyObject = pyInterp.get(resultParameter);
      if (pyObject instanceof PyBoolean) {
        result = ((PyBoolean) pyObject).getBooleanValue();
      } else if (pyObject instanceof PyFloat) {
        result = pyObject.asDouble();
      } else if (pyObject instanceof PyInteger) {
        result = pyObject.asDouble();
      } else if (pyObject instanceof PyLong) {
        result = pyObject.asDouble();
      } else if (pyObject instanceof PyString) {
        result = pyObject.asString();
      } else if (pyObject instanceof PyList) {
        result = new JSONObject(Collections.singletonMap(resultParameter, this.getFromArrayPython((PyList) pyObject)));
      } else {
        throw new Exception("result is a not supported type " + pyObject.getClass());
      }
    }

    SCRIPT_LOGGER.debug("ScriptEngineResource - execPython -> Stop");
    return result;
  }

  @SuppressWarnings("null")
  private PyList putIntoArrayPython(String name, JSONArray jsonArray) throws Exception {
    PyList pyList = new PyList();

    for (int index = 0; index < jsonArray.length(); index++) {
      Object cell = jsonArray.get(index);

      if (cell instanceof Boolean) {
        pyList.add(new PyBoolean(jsonArray.getBoolean(index)));
      } else if (cell instanceof Number) {
        pyList.add(new PyFloat(jsonArray.getDouble(index)));
      } else if (cell instanceof String) {
        pyList.add(new PyString(jsonArray.getString(index)));
      } else if (cell instanceof JSONArray) {
        pyList.add(this.putIntoArrayPython(name, jsonArray.getJSONArray(index)));
      } else {
        throw new Exception("array " + name + " has a cell with a not supported type " + cell.getClass());
      }
    }

    return pyList;
  }

  @SuppressWarnings("null")
  private JSONArray getFromArrayPython(PyList pyList) throws Exception {
    JSONArray jsonArray = new JSONArray();

    for (PyObject cell : pyList.getArray()) {
      if (cell instanceof PyBoolean) {
        jsonArray.put(((PyBoolean) cell).getBooleanValue());
      } else if (cell instanceof PyFloat) {
        jsonArray.put(cell.asDouble());
      } else if (cell instanceof PyInteger) {
        jsonArray.put(cell.asDouble());
      } else if (cell instanceof PyLong) {
        jsonArray.put(cell.asDouble());
      } else if (cell instanceof PyString) {
        jsonArray.put(cell.asString());
      } else if (cell instanceof PyList) {
        jsonArray.put(this.getFromArrayPython((PyList) cell));
      } else {
        throw new Exception("result array has a cell with a not supported type " + cell.getClass());
      }
    }

    return jsonArray;
  }

  @ThingworxServiceDefinition(name = "execJavaScript", description = "", category = "", isAllowOverride = false, aspects = {"isAsync:false"})
  @ThingworxServiceResult(name = "result", description = "", baseType = "VARIANT")
  @SuppressWarnings("null")
  public Object execJavaScript(
          @ThingworxServiceParameter(name = "parameters", description = "", baseType = "JSON", aspects = {"isRequired:true"}) JSONObject parameters,
          @ThingworxServiceParameter(name = "resultParameter", description = "", baseType = "STRING", aspects = {"isRequired:true"}) String resultParameter,
          @ThingworxServiceParameter(name = "code", description = "", baseType = "STRING", aspects = {"isRequired:true"}) String code) throws Exception {
    SCRIPT_LOGGER.debug("ScriptEngineResource - execJavaScript -> Start");

    Object result;
    Context cx = new ContextFactory().enterContext();
    Scriptable scriptable = cx.initStandardObjects();

    for (Iterator<String> iter = parameters.keys(); iter.hasNext();) {
      String name = iter.next();

      Object parameter = parameters.get(name);
      if (parameter instanceof Boolean) {
        scriptable.put(name, scriptable, Context.javaToJS(parameter, scriptable));
      } else if (parameter instanceof Number) {
        scriptable.put(name, scriptable, Context.javaToJS(parameter, scriptable));
      } else if (parameter instanceof String) {
        scriptable.put(name, scriptable, Context.javaToJS(parameter, scriptable));
      } else if (parameter instanceof JSONArray) {
        scriptable.put(name, scriptable, Context.javaToJS(this.putIntoArrayJavaScript(name, parameters.getJSONArray(name)), scriptable));
      } else {
        throw new Exception("parameter " + name + " is a not supported type " + parameter.getClass());
      }
    }

    cx.compileString(code, "", 0, null).exec(cx, scriptable);

    Object object = scriptable.get(resultParameter, scriptable);
    if (object instanceof Wrapper) {
      object = ((Wrapper) object).unwrap();
    }
    if (object instanceof Boolean) {
      result = object;
    } else if (object instanceof Number) {
      result = object;
    } else if (object instanceof String) {
      result = object;
    } else if (object.getClass().isArray()) {
      result = new JSONObject(Collections.singletonMap(resultParameter, this.getFromArrayJavaScript((Object[]) object)));
    } else if (object instanceof Collection) {
      result = new JSONObject(Collections.singletonMap(resultParameter, this.getFromArrayJavaScript(((Collection) object).toArray())));
    } else {
      throw new Exception("result is a not supported type " + object.getClass());
    }

    SCRIPT_LOGGER.debug("ScriptEngineResource - execJavaScript -> Stop");
    return result;
  }

  @SuppressWarnings("null")
  private Object[] putIntoArrayJavaScript(String name, JSONArray jsonArray) throws Exception {
    List<Object> list = new ArrayList<>();

    for (int index = 0; index < jsonArray.length(); index++) {
      Object cell = jsonArray.get(index);

      if (cell instanceof Boolean) {
        list.add(cell);
      } else if (cell instanceof Number) {
        list.add(cell);
      } else if (cell instanceof String) {
        list.add(cell);
      } else if (cell instanceof JSONArray) {
        list.add(this.putIntoArrayJavaScript(name, jsonArray.getJSONArray(index)));
      } else {
        throw new Exception("array " + name + " has a cell with a not supported type " + cell.getClass());
      }
    }

    return list.toArray();
  }

  @SuppressWarnings("null")
  private JSONArray getFromArrayJavaScript(Object[] objectArray) throws Exception {
    JSONArray jsonArray = new JSONArray();

    for (Object cell : objectArray) {
      if (cell instanceof Wrapper) {
        cell = ((Wrapper) cell).unwrap();
      }
      if (cell instanceof Boolean) {
        jsonArray.put(cell);
      } else if (cell instanceof Number) {
        jsonArray.put(cell);
      } else if (cell instanceof String) {
        jsonArray.put(cell);
      } else if (cell.getClass().isArray()) {
        jsonArray.put(this.getFromArrayJavaScript((Object[]) cell));
      } else if (cell instanceof Collection) {
        jsonArray.put(this.getFromArrayJavaScript(((Collection) cell).toArray()));
      } else {
        throw new Exception("result array has a cell with a not supported type " + cell.getClass());
      }
    }

    return jsonArray;
  }
}
