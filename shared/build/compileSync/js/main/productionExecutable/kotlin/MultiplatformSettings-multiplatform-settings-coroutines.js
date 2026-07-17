(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['MultiplatformSettings-multiplatform-settings-coroutines'] = factory(typeof this['MultiplatformSettings-multiplatform-settings-coroutines'] === 'undefined' ? {} : this['MultiplatformSettings-multiplatform-settings-coroutines']);
}(this, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  return _;
}));

//# sourceMappingURL=MultiplatformSettings-multiplatform-settings-coroutines.js.map
