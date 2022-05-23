# ScriptEngineResource
An extension to run external scripts (JavaScript and Python).

**This Extension is provided as-is and without warranty or support. It is not part of the PTC product suite and there is no PTC support.**

## Description
This extension allows to run external scripts (JavaScript and Python) inside a service.

## Services
- *execPython*: extecutes a Python code
  - input
    - parameters: the input parameters, represented as a JSON object containing numbers, strings, booleans and arrays of numbers, strings, booleans and arrays (recursively) - JSON (No default value)
    - resultParameter: parameter name of the result value - STRING (No default value)
    - code: the scripting code - STRING (No default value)
  - output: VARIANT (NUMBER, STRING, BOOLEAN, JSON)
- *execJavaScript*: extecutes a JavaScript code
  - input
    - parameters: the input parameters, represented as a JSON object containing numbers, strings, booleans and arrays of numbers, strings, booleans and arrays (recursively) - JSON (No default value)
    - resultParameter: parameter name of the result value - STRING (No default value)
    - code: the scripting code - STRING (No default value)
  - output: VARIANT (NUMBER, STRING, BOOLEAN, JSON)

## Dependencies
  - jython-standalone-2.7.2.jar

## Donate
If you would like to support the development of this and/or other extensions, consider making a [donation](https://www.paypal.com/donate/?business=HCDX9BAEYDF4C&no_recurring=0&currency_code=EUR).
