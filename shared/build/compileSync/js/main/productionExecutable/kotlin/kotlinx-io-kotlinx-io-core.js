(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['kotlinx-io-kotlinx-io-core'] = factory(typeof this['kotlinx-io-kotlinx-io-core'] === 'undefined' ? {} : this['kotlinx-io-kotlinx-io-core']);
}(this, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  return _;
}));

//# sourceMappingURL=kotlinx-io-kotlinx-io-core.js.map
