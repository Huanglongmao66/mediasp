(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['compose-multiplatform-core-compose-ui-ui-text'] = factory(typeof this['compose-multiplatform-core-compose-ui-ui-text'] === 'undefined' ? {} : this['compose-multiplatform-core-compose-ui-ui-text']);
}(this, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  var DefaultCacheSize;
  //region block: init
  DefaultCacheSize = 8;
  //endregion
  return _;
}));

//# sourceMappingURL=compose-multiplatform-core-compose-ui-ui-text.js.map
