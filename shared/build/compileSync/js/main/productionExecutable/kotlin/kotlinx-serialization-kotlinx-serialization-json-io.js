(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['kotlinx-serialization-kotlinx-serialization-json-io'] = factory(typeof this['kotlinx-serialization-kotlinx-serialization-json-io'] === 'undefined' ? {} : this['kotlinx-serialization-kotlinx-serialization-json-io']);
}(this, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  return _;
}));

//# sourceMappingURL=kotlinx-serialization-kotlinx-serialization-json-io.js.map
