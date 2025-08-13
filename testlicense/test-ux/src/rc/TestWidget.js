
/**
 * A module defining `TestWidget`.
 *
 * @module nmodule/test/rc/TestWidget
 */
define([ 'nmodule/webEditors/rc/fe/baja/BaseEditor', 'jquery', 'Promise' ], function (BaseEditor, $, Promise) {

  'use strict';

  /**
   * Description of your widget.
   *
   * @class
   * @extends module:nmodule/webEditors/rc/fe/baja/BaseEditor
   * @alias module:nmodule/test/rc/TestWidget
   */
  var TestWidget = function TestWidget() {
    BaseEditor.apply(this, arguments);
  };

  //extend and set up prototype chain
  TestWidget.prototype = Object.create(BaseEditor.prototype);
  TestWidget.prototype.constructor = TestWidget;

  /**
   * Describe how your widget does its initial setup of the DOM.
   *
   * @param {jQuery} element the DOM element into which to load this widget
   */
  TestWidget.prototype.doInitialize = function (dom) {
    dom.html('<input type="text" value="value goes here" />');
  };

  /**
   * Describe how your widget loads in a value.
   *
   * @param value description of the value to be loaded into this widget
   */
  TestWidget.prototype.doLoad = function (value) {
    this.jq().find('input').val(String(value));
  };

  /**
   * Describe what kind of data you can read out of this widget.
   *
   * @returns {Promise} promise to be resolved with the current value
   */
  TestWidget.prototype.doRead = function () {
    return Promise.resolve(this.jq().find('input').val());
  };

  return TestWidget;
});

