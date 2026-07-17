(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['ktor-ktor-shared-ktor-serialization'] = factory(typeof this['ktor-ktor-shared-ktor-serialization'] === 'undefined' ? {} : this['ktor-ktor-shared-ktor-serialization']);
}(this, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  return _;
}));

//# sourceMappingURL=ktor-ktor-shared-ktor-serialization.js.map
