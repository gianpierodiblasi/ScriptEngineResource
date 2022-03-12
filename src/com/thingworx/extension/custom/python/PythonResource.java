package com.thingworx.extension.custom.python;

import com.thingworx.logging.LogUtilities;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.resources.Resource;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.python.core.PyBoolean;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;

public class PythonResource extends Resource {

  private final static Logger SCRIPT_LOGGER = LogUtilities.getInstance().getScriptLogger(PythonResource.class);
  private static final long serialVersionUID = 1L;

  @ThingworxServiceDefinition(name = "exec", description = "", category = "", isAllowOverride = false, aspects = {"isAsync:false"})
  @ThingworxServiceResult(name = "result", description = "", baseType = "VARIANT")
  @SuppressWarnings("null")
  public Object exec(
          @ThingworxServiceParameter(name = "parameters", description = "", baseType = "JSON", aspects = {"isRequired:true"}) JSONObject parameters,
          @ThingworxServiceParameter(name = "resultParameter", description = "", baseType = "STRING", aspects = {"isRequired:true"}) String resultParameter,
          @ThingworxServiceParameter(name = "code", description = "", baseType = "STRING", aspects = {"isRequired:true"}) String code) throws Exception {
    SCRIPT_LOGGER.debug("PythonResource - exec -> Start");

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
          pyInterp.set(name, this.putIntoArray(name, parameters.getJSONArray(name)));
        } else {
          throw new Exception("parameter " + name + " is a not supported type " + parameter.getClass());
        }
      }

      pyInterp.exec(code);

      PyObject pyObject = pyInterp.get(resultParameter);
      if (PyBoolean.class.isInstance(pyObject)) {
        result = ((PyBoolean) pyObject).getBooleanValue();
      } else if (PyFloat.class.isInstance(pyObject)) {
        result = pyObject.asDouble();
      } else if (PyInteger.class.isInstance(pyObject)) {
        result = pyObject.asDouble();
      } else if (PyLong.class.isInstance(pyObject)) {
        result = pyObject.asDouble();
      } else if (PyString.class.isInstance(pyObject)) {
        result = pyObject.asString();
      } else {
        throw new Exception("result is a not supported type " + pyObject.getClass());
      }
    }

    SCRIPT_LOGGER.debug("PythonResource - exec -> Stop");
    return result;
  }

  @SuppressWarnings("null")
  private PyList putIntoArray(String name, JSONArray jsonArray) throws Exception {
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
        pyList.add(this.putIntoArray(name, jsonArray.getJSONArray(index)));
      } else {
        throw new Exception("array " + name + " has a cell with a not supported type " + cell.getClass());
      }
    }

    return pyList;
  }
}
