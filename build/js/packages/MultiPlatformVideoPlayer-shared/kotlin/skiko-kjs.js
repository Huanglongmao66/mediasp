(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['skiko-kjs'] = factory(typeof this['skiko-kjs'] === 'undefined' ? {} : this['skiko-kjs']);
}(this, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  function _createLocalCallbackScope$accessor$wmqves() {
    return _createLocalCallbackScope();
  }
  function _releaseLocalCallbackScope$accessor$wmqves() {
    return _releaseLocalCallbackScope();
  }
  return _;
}));

//# sourceMappingURL=skiko-kjs.js.map
