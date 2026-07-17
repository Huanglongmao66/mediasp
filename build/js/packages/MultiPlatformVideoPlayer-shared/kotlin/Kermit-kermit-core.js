(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['Kermit-kermit-core'] = factory(typeof this['Kermit-kermit-core'] === 'undefined' ? {} : this['Kermit-kermit-core']);
}(this, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  return _;
}));

//# sourceMappingURL=Kermit-kermit-core.js.map
