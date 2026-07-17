(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['Kermit-kermit'] = factory(typeof this['Kermit-kermit'] === 'undefined' ? {} : this['Kermit-kermit']);
}(this, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  var defaultTag;
  //region block: init
  defaultTag = '';
  //endregion
  return _;
}));

//# sourceMappingURL=Kermit-kermit.js.map
