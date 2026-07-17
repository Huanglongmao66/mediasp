(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['ktor-ktor-shared-ktor-sse'] = factory(typeof this['ktor-ktor-shared-ktor-sse'] === 'undefined' ? {} : this['ktor-ktor-shared-ktor-sse']);
}(this, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  return _;
}));

//# sourceMappingURL=ktor-ktor-shared-ktor-sse.js.map
