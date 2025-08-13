define(['nmodule/test/rc/TestWidget', 'jquery'], function (TestWidget, $) {
  'use strict';

  describe('nmodule/test/rc/TestWidget', function () {
    var widget, elem;
    beforeEach(function () {
      widget = new TestWidget();
      elem = $('<div/>');
    });
    describe('#doInitialize()', function () {
      it('does something', function () {
        return widget.initialize(elem).then(function () {
          //assert something about the widget after initialization.
          //expect(widget.js().text()).toBe('ready to go');
        });
      });
    });
    describe('#doLoad()', function () {
      it('does something', function () {
        return widget.initialize(elem).then(function () {
          return widget.load('something');
        }).then(function () {
          //assert something about the widget after value is loaded.
          //expect(widget.jq().find('input').val()).toBe('something good'):
        });
      });
    });
    describe('#doRead()', function () {
      it('does something', function () {
        return widget.initialize(elem).then(function () {
          return widget.load('something good');
        }).then(function () {
          return widget.read();
        }).then(function (result) {
          //assert something about the result read from the widget.
          //expect(result).toBe('something resplendent');
        });
      });
    });
  });
});
