(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['ktor-ktor-io'] = factory(typeof this['ktor-ktor-io'] === 'undefined' ? {} : this['ktor-ktor-io']);
}(this, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  return _;
}));

//# sourceMappingURL=ktor-ktor-io.js.map
