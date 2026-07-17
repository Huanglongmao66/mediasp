(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['ktor-ktor-client-ktor-client-core'] = factory(typeof this['ktor-ktor-client-ktor-client-core'] === 'undefined' ? {} : this['ktor-ktor-client-ktor-client-core']);
}(this, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  return _;
}));

//# sourceMappingURL=ktor-ktor-client-ktor-client-core.js.map
