(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['ktor-ktor-utils'] = factory(typeof this['ktor-ktor-utils'] === 'undefined' ? {} : this['ktor-ktor-utils']);
}(this, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  var DISABLE_SFG;
  //region block: init
  DISABLE_SFG = false;
  //endregion
  return _;
}));

//# sourceMappingURL=ktor-ktor-utils.js.map
